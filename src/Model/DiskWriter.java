package Model;

import javafx.util.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

public class DiskWriter {
    /**
     * The method saves the dictionary to a file.
     * @param toSaveDic
     * @param dictionary
     */
    public void writeDictionaryToDisk(File toSaveDic, HashMap<String, String[]> dictionary) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(toSaveDic.getPath() + ".txt");
            for (Map.Entry<String, String[]> entry : dictionary.entrySet()) {
                pw.println(entry.getKey() + "\n" + entry.getValue()[0] + "|" + entry.getValue()[1] + "|" + entry.getValue()[2] + "|" + entry.getValue()[3]);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        pw.flush();
        pw.close();
    }

    /**
     * The method saves the entities dictionary to a file.
     * @param toSaveDocsEntities
     * @param docsEntities
     */
    public void writeDocsEntitiesToDisk(String toSaveDocsEntities, HashMap<String, LinkedList<Pair<String, Integer>>> docsEntities) {
        try {
            PrintWriter pw = new PrintWriter(toSaveDocsEntities);
            for (String docID : docsEntities.keySet()) {
                pw.println(docID);
                String line="";
                for (Pair<String, Integer> entity : docsEntities.get(docID)) {
                    line = line + entity.getKey() + "|" + entity.getValue() + ";";
                }
                pw.println(line);
            }
            pw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * the method saves the query's search results on to a file.
     * @param toDisplay
     * @param browseDirResults
     */
    public void writeResultsToDisk(HashMap<String, TreeMap<Double, String>> toDisplay, String browseDirResults) {
        try {
            PrintWriter pw = new PrintWriter(browseDirResults + "\\results.txt");
            TreeMap<String, TreeMap<Double, String>> toDisplaySort=new TreeMap<>();
            for(Map.Entry entry: toDisplay.entrySet()){
                toDisplaySort.put((String)entry.getKey(),(TreeMap<Double, String>) entry.getValue());
            }
            for (String q_id : toDisplaySort.keySet()) {
                TreeMap<Double, String> docsRank = toDisplaySort.get(q_id);
                int counter = 0;

                for (Map.Entry<Double, String> rank : docsRank.entrySet()) {
                    if (counter >= 50) {
                        break;
                    }else {
                        String line = q_id + " 0 " + rank.getValue() + " " + rank.getKey() + " 00.00 0";
                        pw.println(line);
                        counter++;
                    }
                }
            }
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
