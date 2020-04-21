
package Model;

import com.medallia.word2vec.Word2VecModel;
import javafx.util.Pair;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Searcher {

    //region Fields
    public boolean toStem;
    public boolean useSemanticModel;
    public boolean useDataMuseAPI;
    String dirPostingsDic;
    Parser parser;
    HashMap<String, String[]> dictionary;
    HashMap<String, List<Pair<String,Integer>>> docsQuery;
    //endregion

    //region Constructor
    public Searcher(HashMap<String, String[]> dictionary, String dirPostingsDic) {
        this.parser = new Parser(dirPostingsDic+"\\stop_words.txt");
        this.dictionary = dictionary;
        this.dirPostingsDic =dirPostingsDic;
    }
    //endregion

    //region Setters & Getters
    public void setUseDataMuseAPI(boolean useDataMuseAPI) {
        this.useDataMuseAPI = useDataMuseAPI;
    }

    public void setUseSemanticModel(boolean useSemanticModel) {
        this.useSemanticModel = useSemanticModel;
    }

    public void setToStem(boolean toStem) {
        this.toStem = toStem;
    }
    //endregion




    //region Searching methods

    /**
     * The method receives a search query, and returns a set of all docIDs related to the query's terms,
     * and for each docID returns a list of query terms appears in the document itself.
     * @param query
     * @return
     */
    public HashMap<String, List<Pair<String, Integer>>>  search(String query) {
        docsQuery=new HashMap<>();
        HashMap<String, Integer> parsedQuery = textOperations(query);

        for (String elem : parsedQuery.keySet()) {
            if (dictionary.containsKey(elem)) {
                getDocsFromPostings(elem,dirPostingsDic+"\\"+ dictionary.get(elem)[2], Integer.valueOf(dictionary.get(elem)[3]));
            }
        }

        return docsQuery;
    }

    public HashMap<String, List<Pair<String, Integer>>>  searchSemantic( String query) {
        HashMap<String, Integer> parsedQuery = textOperations(query);
        HashMap<String, Integer> parsedSynonyms= new HashMap<>();
        if(useDataMuseAPI){
            DataMuseConnection datMuse= new DataMuseConnection();

            for (String qi:parsedQuery.keySet()) {
                String[] synonyms= datMuse.findSynonyms(qi);
                for (String syn:synonyms) {
                    parsedSynonyms.put(syn,0);
                }
            }
            for (String syn:parsedSynonyms.keySet()) {
                parsedSynonyms.put(syn,0);
            }
        }
        else if(useSemanticModel) {
            parsedSynonyms = getSemanticWord(parsedQuery);
        }

        for (String elem : parsedSynonyms.keySet()) {
            if (dictionary.containsKey(elem)) {
                getDocsFromPostings(elem,dirPostingsDic+"\\"+ dictionary.get(elem)[2], Integer.valueOf(dictionary.get(elem)[3]));
            }
        }
        return docsQuery;

    }
    //endregion

    private void getDocsFromPostings(String elem, String postingURL, int position) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(postingURL)));
            String term = "";
            for (int i = 0; i <= position; i++) {
                term = br.readLine();
            }
            br.close();
            String[] termDocs = term.split("[:|;]");
            for (int i = 1; i < termDocs.length - 1; i = i + 2) {
                String docID= termDocs[i];
                if(docsQuery.containsKey(docID)) {
                    List<Pair<String,Integer>> tempDocList= docsQuery.get(docID);
                    Pair<String, Integer> q= new Pair<>(elem,Integer.valueOf(termDocs[i + 1]));
                    tempDocList.add(q);
                    docsQuery.put(docID,tempDocList);
                }else{
                    List<Pair<String,Integer>> tempDocList=new LinkedList<>();
                    Pair<String, Integer> q= new Pair<>(elem,Integer.valueOf(termDocs[i + 1]));
                    tempDocList.add(q);
                    docsQuery.put(docID,tempDocList);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HashMap<String, Integer> textOperations(String query) {
        parser.setToStem(toStem);
        HashMap<String, Integer> parsedQuery = parser.parse(query, "noDocID");

        // If query has an entity in it, it won't bring it back all parts together
        if (!parser.entities.isEmpty()) {
            for (String entity : parser.entities.keySet()) {
                parsedQuery.put(entity, 0);
            }
        }
        HashMap<String, Integer> parsedQueryRes= new HashMap<>();
        parsedQuery.forEach((term,noDocID)->{
            parsedQueryRes.put(term.toLowerCase(),noDocID);
            parsedQueryRes.put(term.toUpperCase(),noDocID);
        });

        return parsedQueryRes;
    }

    private HashMap<String, Integer> getSemanticWord(HashMap<String, Integer> parsedQuery){
        HashMap<String, Integer> parseWithSemantic= new HashMap<>();
        try {
            Word2VecModel mod = Word2VecModel.fromTextFile(new File(".\\word2vec.c.output.model.txt"));
            com.medallia.word2vec.Searcher search = mod.forSearch();
            int num = 2;
            for (String qi : parsedQuery.keySet()) {
                if (!qi.contains(" ") && !qi.contains("-")) {
                    try {
                        List<com.medallia.word2vec.Searcher.Match> matches = search.getMatches(qi.toLowerCase(), num);
                        String str;
                        int counter=0;
                        for (com.medallia.word2vec.Searcher.Match match : matches) {
                            str = match.match();
                            if(counter>0){
                                parseWithSemantic.put(str, 0);
                            }
                            counter++;

                        }
                    }catch (com.medallia.word2vec.Searcher.UnknownWordException e){
                        continue;
                    }
                }
            }
        } catch (Exception e) {
        }
        return parseWithSemantic;

    }
    /**
     * Clears all Searcher's data structures of the RAM.
     */
    public void clear() {
        if(parser!=null) parser.clear();
        if(dictionary!=null) dictionary.clear();
        if(docsQuery!=null) docsQuery.clear();
    }


}