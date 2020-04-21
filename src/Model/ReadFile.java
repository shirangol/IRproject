package Model;

import javafx.util.Pair;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import java.io.*;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;

public class ReadFile {

    //region Fields
    Indexer mainIndexer;
    Parser mainParser;
    LinkedList<SingleDoc> docsList;
    File mainCorpusFolder;
    HashMap<String, int[]> docsDictionary;//{max_tf,number_of_terms,size,number_entities}
    HashMap<String,LinkedList<Pair<String,Integer>>> docsEntities; //{docID,{tf,entity}}
    HashMap<String, String[]> dictionary;
    int BATCH_SIZE= 3000;
    //endregion

    //region Constructors
    /**
     * Constructor
     * @param stopWordsURL
     * @param corpusURL
     * @param postingsDicStr
     */
    public ReadFile(String stopWordsURL, String corpusURL, String postingsDicStr) {
        this.docsList = new LinkedList<>();
        mainParser= new Parser(stopWordsURL);
        mainCorpusFolder= new File(corpusURL);
        mainIndexer=new Indexer(postingsDicStr);
        docsDictionary = new HashMap<>();
        docsEntities= new HashMap<>();

    }
    //endregion

    //region Setters and Getters
    /**
     * Sets 'toStem' boolean field on the parser and the indexer,
     * according to user's wish(selected or unselected check-box).
     * @param toStem - 'True' if stemming is required, 'False' otherwise.
     */
    public void setToStem(boolean toStem){
        mainParser.setToStem(toStem);
        mainIndexer.setToStem(toStem);
    }

    /**
     * Returns the dictionary
     * @return
     */
    public HashMap<String, String[]> getDictionary() {
        return dictionary;
    }

    /**
     * Returns the amount of files that were indexed.
     * @return
     */
    public HashMap<String, int[]> getdocsDictionary() {
        return docsDictionary;
    }

    public HashMap<String, LinkedList<Pair<String, Integer>>> getDocsEntities() {
        return docsEntities;
    }

    //endregion

    //region Assistance methods
    /**
     * The method clears all of ReadFile's data structures,
     * and call parser's and indexer's clearing method.
     */
    public void clear() {
        if(docsList!=null)docsList.clear();
        if(dictionary!=null) dictionary.clear();
        if(docsDictionary!=null) docsDictionary.clear();
        if(mainParser!=null) mainParser.clear();
        if(mainIndexer!=null) mainIndexer.clear();
    }
    //endregion

    //region Main ReadFile methods
    /**
     * The method enters the Corpus's directory, and extracts all files out of it.
     * It splits each file into the documents it is composed of, and creat a new
     * SingleDoc object that holds the doc's ID, text, title, etc.
     */
    public void readCorpusFiles() {

        // Iterating through all files in the corpus
        for ( File fileEntry : mainCorpusFolder.listFiles()) {
            if (fileEntry.isDirectory()) {
                for (File docFile: fileEntry.listFiles()) {
                    try {

                        // Parse file with Jsoup and split it into several docs
                        Document doc= Jsoup.parse(docFile, "utf-8");
                        Elements media = doc.select("doc").tagName("DOC");

                        // Iterate through all docs in a file and extract TEXT and DOC fields.
                        for (Element el : media) {
                            String docNo=el.select("DOCNO").tagName("DOCNO").text();
                           /* String date=el.select("DATE1").tagName("DATE1").text();
                            String title=el.select("TI").tagName("TI").text();
                            */
                            String text=el.select("text").tagName("TEXT").text();

                            // Create a new SingleDoc object
                            SingleDoc docToList= new SingleDoc(docNo,"date","title",text);

                            // Add the doc to the docs data structure and doc's list
                            docsDictionary.put(docNo,new int[]{0,0,text.length(),0});
                            docsList.add(docToList);

                            // Send a batch of Docs to parsing
                            if(docsList.size()>=BATCH_SIZE ){
                                mainParser.setDocsList(docsList);

                                // Recieve parsed terms from parser
                                HashMap<String, HashMap<String, Integer/*counter*/>> allTerms=mainParser.parse();

                                // Send recieved terms to indexer
                                mainIndexer.index(allTerms);

                                // Calculating Max_tf for each doc
                                mainParser.docMax_tf.forEach((docID,max_tf)->{
                                    int[] docInfo=docsDictionary.get(docID);
                                    docInfo= new int[]{max_tf,allTerms.get(docID).size(),docInfo[2],0};
                                    docsDictionary.put(docID,docInfo);
                                });
                                docsList.clear();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }

        // When finished reading all files, send for last parsing and indexing
        if(!docsList.isEmpty()) {
            mainParser.setDocsList(docsList);
            HashMap<String, HashMap<String, Integer/*counter*/>> allTerms=mainParser.parse();
            mainIndexer.index(allTerms);
            mainParser.docMax_tf.forEach((docID,max_tf)->{
                int[] docInfo=docsDictionary.get(docID);
                docInfo= new int[]{max_tf,allTerms.get(docID).size(),docInfo[2],0};
                docsDictionary.put(docID,docInfo);
            });
            docsList.clear();
        }

        // Start processing entities
        mainParser.entityProcessor();

        // Receive parsed entities
        Hashtable<String/*term*/, HashMap<String,Integer>> entities=mainParser.entities;

        // Iterating through the entity map, update doc's dictionary
        entities.forEach((term,docs)->{ //counter entity show in doc
            for(String docID: docs.keySet()){
                int[] docInfo=docsDictionary.get(docID);
                docInfo[3]=docInfo[3]+1;
                docsDictionary.put(docID,docInfo);

                addToDocsEntities(docID, term, docs.get(docID));
            }
        });
        // Send entities for indexing
        mainIndexer.indexEntity(entities);


        // Merge all temporary posting files
        mainParser.clear();
        mainIndexer.mergePostings();

        // Create the dictionary object and create small alphabetic orderd posting files
        dictionary=mainIndexer.splitPostingFile();
    }

    //add entity to docsEntities
    private void addToDocsEntities(String docID, String term, Integer tf) {
        if(docsEntities.containsKey(docID)){
            LinkedList<Pair<String,Integer>> entities= docsEntities.get(docID);
            entities.add(new Pair<>(term,tf));
        }else{
            LinkedList<Pair<String,Integer>> entities=new LinkedList<>();
            entities.add(new Pair<>(term,tf));
            docsEntities.put(docID,entities);
        }
    }
    //endregion

}