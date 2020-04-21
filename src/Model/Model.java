package Model;

import javafx.util.Pair;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.*;

public class Model {

    //region Fields
    public boolean toStem;
    public boolean identifyEntities;
    public boolean useSemanticModel;
    public boolean useDataMuseAPI;
    String DirPostingsDic;
    ReadFile readFile;
    HashMap<String, String[]> dictionary;//{df, ttf, postingName,position}
    HashMap<String, int[]> docsDictionary;//{max_tf,number_of_terms,size,number_entities}
    HashMap<String, LinkedList<Pair<String, Integer>>> docsEntities; //{docID,{tf,entity}}
    TreeMap<Double,String> docsRank;

    Searcher searcher;
    Ranker ranker;
    DiskWriter diskWriter;
    DiskReader diskReader;
    //endregion

    //region Constructors

    /**
     * Constructor
     */
    public Model() {
        this.toStem = false;
        this.diskWriter= new DiskWriter();
        this.diskReader= new DiskReader();
    }

    /**
     * Starts the main Ranker
     */
    private void startRanker() {
        if (ranker == null) {
            ranker = new Ranker(dictionary, docsDictionary,docsEntities);
        }
    }

    /**
     * Starts the main searcher
     */
    private void startSearcher() {
        if (searcher == null) {
            searcher = new Searcher(dictionary, DirPostingsDic);
        }
        searcher.setToStem(this.toStem);
        searcher.setUseSemanticModel(this.useSemanticModel);
        searcher.setUseDataMuseAPI(useDataMuseAPI);
    }

    //endregion

    //region Getters and Setters

    public void setDirPostingsDic(String dirPostingsDic) {
        DirPostingsDic = dirPostingsDic;
    }

    /**
     * Returns the dictionary
     *
     * @return
     */
    public HashMap<String, String[]> getDictionary() {
        return this.dictionary;
    }

    /**
     * Returns the amount of files that were indexed.
     *
     * @return
     */
    public int getDocsDictionarySize() {
        docsDictionary = readFile.getdocsDictionary();
        return docsDictionary.size();
    }

    /**
     * Returns the amount of unique terms in the dictionary.
     *
     * @return
     */
    public int getDictionarySize() {
        return dictionary.size();
    }


    /**
     * returns docsRank tree map, holding the ranked documents of a single search query.
     * @return
     */
    public TreeMap<Double, String> getDocsRank() {
        return docsRank;
    }

    /**
     * returns a document's 5 top entities
     * @param docID
     * @return
     */
    public TreeMap<String, Double> getTopEntities(String docID) {
        return ranker.getTopEntities(docID);
    }

    //endregion

    //region Assistance Methods

    /**
     * Loads The dictionary out of a given path (a different dictionary, according to stemmed or not),
     * and stores it in the Model's dictionary field.
     */
    public boolean loadDic() {
        File toLoadDic;
        File toLoadDocsDictionary;
        String toLoadDocsEntities = "";

        // Choose the path based on stemming user's choice.
        if (toStem == true) {
            toLoadDic = new File(DirPostingsDic + "\\stemmed_Dictionary");//path for stemmed dictionary
            toLoadDocsDictionary = new File(DirPostingsDic + "\\stemmed_DocsDictionary");
            toLoadDocsEntities= DirPostingsDic+ "\\stemmed_DocsEntities.txt";
        } else {
            toLoadDic = new File(DirPostingsDic + "\\un_stemmed_Dictionary");//path for un-stemmed dictionary.
            toLoadDocsDictionary = new File(DirPostingsDic + "\\un_stemmed_DocsDictionary");
            toLoadDocsEntities= DirPostingsDic+ "\\un_stemmed_DocsEntities.txt";
        }

        // Load the Dictionary out of object file
        try {
            dictionary=diskReader.readDictionaryFromDisk(toLoadDic);
            docsEntities= diskReader.readDocsEntitiesFromDisk(toLoadDocsEntities);
            if(dictionary==null||docsEntities==null){
                return false;
            }

            ObjectInputStream inDocsEntities = new ObjectInputStream(new FileInputStream(toLoadDocsDictionary));
            docsDictionary = (HashMap<String, int[]>) inDocsEntities.readObject();
            inDocsEntities.close();


            return true;
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * receives corpus and stop-words path, generates a new dictionary and saves it to the disk:
     * working_path\\stemmed_Dictionary if stemmed, working_path\\un_stemmed_Dictionary otherwise.
     *
     * @param corpusStopwordsStr
     * @param toSavedir
     */
    public void generateDictionary(String corpusStopwordsStr, String toSavedir) {
        File toSaveDic;
        File toSaveDocsDictionary;
        String stopWordsPath = corpusStopwordsStr + "\\stop_words.txt";
        String toSavedocsEntities = "";
        //generating dictionary- starting Readfile
        readFile = new ReadFile(stopWordsPath, corpusStopwordsStr + "\\corpus", toSavedir);
        readFile.setToStem(toStem);
        readFile.readCorpusFiles();
        dictionary = readFile.getDictionary();
        docsDictionary = readFile.getdocsDictionary();
        docsEntities = readFile.getDocsEntities();

        // saving dictionary object into file
        if (toStem == true) {
            toSaveDic = new File(toSavedir + "\\stemmed_Dictionary");//path for stemmed dictionary
            toSaveDocsDictionary = new File(toSavedir + "\\stemmed_DocsDictionary");
            toSavedocsEntities = toSavedir + "\\stemmed_DocsEntities.txt";

        } else {
            toSaveDic = new File(toSavedir + "\\un_stemmed_Dictionary");//path for un-stemmed dictionary.
            toSaveDocsDictionary = new File(toSavedir + "\\un_stemmed_DocsDictionary");
            toSavedocsEntities = toSavedir + "\\un_stemmed_DocsEntities.txt";
        }
        try {
            diskWriter.writeDictionaryToDisk(toSaveDic,dictionary);
            diskWriter.writeDocsEntitiesToDisk(toSavedocsEntities,docsEntities);

            OutputStream outDocsEntities = new ObjectOutputStream(new FileOutputStream(toSaveDocsDictionary));
            ((ObjectOutputStream) outDocsEntities).writeObject(docsDictionary);
            outDocsEntities.close();
        } catch (IOException e) {
        }
        // Copy stop-words file in the saving directory
        File source = new File(stopWordsPath);
        File dest = new File(toSavedir + "\\stop_words.txt");
        try {
            FileUtils.copyFile(source, dest);
        } catch (IOException e) {
        }
    }


    /**
     * The method receives a path to the posting file and dictionary's directory,
     * clears all files in it and clears all data structures on the RAM.
     *
     * @param postingsDicStr - a path to a directory that holds all files to be deleted
     */
    public boolean clearAllData(String postingsDicStr) {
        // Clear dictionary data structure and call readFile clearing method
        if (dictionary != null) dictionary.clear();
        if (readFile != null) readFile.clear();
        if(docsDictionary!= null) docsDictionary.clear();
        if(docsEntities!=null) docsEntities.clear();
        if(docsRank!=null) docsRank.clear();
        if(searcher!=null) searcher.clear();
        if(ranker!=null) ranker.clear();

        // Clear all posting & dictionary files
        if (postingsDicStr != null) {
            File dir = new File(postingsDicStr);
            for (File fileEntry : dir.listFiles()) {
                if (fileEntry.getName().matches("(.*)stop_words(.*)|(.*)Stemmed_Posting_(.*)|(.*)Un_Stemmed_Posting_(.*)|(.*)stemmed_Dictionary(.*)|(.*)un_stemmed_Dictionary(.*)|" +
                        "(.*)un_stemmed_DocsEntities(.*)|(.*)stemmed_DocsEntities(.*)|(.*)stemmed_DocsDictionary(.*)")) {
                    fileEntry.delete();
                } else if (fileEntry.getName().matches("tmpPostings") && fileEntry.isDirectory()) {
                    try {
                        FileUtils.deleteDirectory(fileEntry);
                    } catch (IOException e) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    //endregion

    //region Search methods
    /**
     * Performs a search on multiple queries in a file
     *
     * @param queriesFile
     * @return
     */
    public HashMap<String,TreeMap<Double,String>> runQueriesFile(File queriesFile) {
        startSearcher();
        HashMap<String,TreeMap<Double,String>> results = new HashMap<>();
        if (!queriesFile.isDirectory()) {

            // Parse file with Jsoup and split it into several query titles
            Document queryDoc = null;
            try {
                queryDoc = Jsoup.parse(queriesFile, "utf-8");
                Elements media = queryDoc.select("top").tagName("top");
                for (Element el : media) {
                    String q_id= el.select("num").tagName("num").text();
                    q_id= q_id.split(": | ")[1];
                    String qTitle = el.select("title").tagName("title").text();
                    runSingleQuery(qTitle);
                    results.put(q_id,docsRank);
                }
            } catch (IOException e) {
            }
        }
        return results;
    }

    /**
     * Performs a single query search
     *
     * @param query
     * @return
     */
    public void runSingleQuery(String query) {
        startSearcher();
        startRanker();
        ranker.setToStem(toStem);
        if(useSemanticModel||useDataMuseAPI){
            runSingleQueryFor2(query);
        }else{
            HashMap<String, List<Pair<String, Integer>>> docResult = searcher.search(query);
            docsRank=ranker.rank(docResult);
        }
    }

    /**
     * performs a search with the use of semantic model.
     * @param title
     */
    private void runSingleQueryFor2(String title) {
        HashMap<String, List<Pair<String, Integer>>> docResult1 = searcher.search(title);
        HashMap<String, List<Pair<String, Integer>>> docResult2 = searcher.searchSemantic(title);
        docsRank=ranker.rankWithWeight(docResult1, docResult2);
    }

    /**
     * Saves query's search results into a file
     * @param toDisplay
     * @param path
     */
    public void saveResults(HashMap<String, TreeMap<Double, String>> toDisplay, String path) {
        diskWriter.writeResultsToDisk(toDisplay,path);
    }
    //endregion
}
