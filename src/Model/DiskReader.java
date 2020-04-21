package Model;

import javafx.util.Pair;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;

public class DiskReader {

    /**
     * The method reads the dictionary into the main memory
     *
     * @param toLoadDic
     * @return
     */
    public HashMap<String, String[]> readDictionaryFromDisk(File toLoadDic) {
        HashMap<String, String[]> dictionary = new HashMap<>();
        BufferedReader br = null;
        String line = "";
        try {
            br = new BufferedReader(new FileReader(toLoadDic.getPath() + ".txt"));
            line = "";
            while ((line = br.readLine()) != null) {
                String term = line;
                line = br.readLine();
                String[] termInfo = line.split("[|]");
                dictionary.put(term, termInfo);
            }
            br.close();

        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        }

        return dictionary;
    }

    /**
     * The method reads the Entities dictionary into the =main memory
     *
     * @param toLoadDocsEntities
     * @return
     */
    public HashMap<String, LinkedList<Pair<String, Integer>>> readDocsEntitiesFromDisk(String toLoadDocsEntities) {
        HashMap<String, LinkedList<Pair<String, Integer>>> entities = new HashMap<>();

        BufferedReader br = null;
        String line = "";
        try {
            br = new BufferedReader(new FileReader(toLoadDocsEntities));
            line = "";
            while ((line = br.readLine()) != null) {
                String docID = line;
                line = br.readLine();
                String[] docInfo = line.split("[|;]");
                LinkedList<Pair<String, Integer>> pairList = new LinkedList<>();
                for (int i = 0; i < docInfo.length; ) {
                    pairList.add(new Pair<String, Integer>(docInfo[i++], Integer.valueOf(docInfo[i++])));
                }
                entities.put(docID, pairList);
            }
            br.close();

        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            return null;
        }

        return entities;
    }
}