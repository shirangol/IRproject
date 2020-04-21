package Model;

import javafx.util.Pair;

import java.util.*;
import java.io.*;



public class Indexer {

    //region Fields
    boolean tostem = false;
    String postingFile;
    String postingDirectory;
    String tmpPostingDirectory;
    int STEMMED_TMP_POSTING_FILE_ID;
    int UN_STEMMED_TMP_POSTING_FILE_ID;
    HashMap<String, String[]> dictionary;//{df, ttf, postingName,position}

    HashSet<String> terms;
    TreeMap<String, List<Pair<String, Integer>>> tempDicToPost;// String{ List<docID,tf>}
    //endregion

    //region Constructors and Setters

    public Indexer(String postingsDicStr) {
        terms = new HashSet<>();
        this.postingDirectory = postingsDicStr;
        new File(postingDirectory + "\\tmpPostings").mkdir();
        this.tmpPostingDirectory = postingsDicStr+ "\\tmpPostings";
        dictionary=new HashMap();
    }

    /**
     * Clears all Indexer's data structures
     */
    public void clear() {
        terms.clear();
        tempDicToPost.clear();
    }

    //endregion
    //region Index Functions

    /**
     * Receives Hash full of terms from the parser, and sends the terms list of
     * each document to indexing. Then, sends the batch to creating
     * a temporary posting file method.
     * @param allTerms
     */
    public void index(HashMap<String/*docID*/, HashMap<String/*term*/, Integer/*counter*/>> allTerms) {
        tempDicToPost = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareToIgnoreCase(o2);
            }
        });
        terms.clear();
        allTerms.forEach((docID, docTermsMap) -> {
            index(docID, docTermsMap);
        });
        createNewPostingFile();
    }

    /**
     * For each term of a doc's terms list, adds it to a temporary dictionary.
     * @param docID
     * @param terms
     */
    public void index(String docID, HashMap<String, Integer> terms) {

        terms.forEach((term, tf) -> {
            addToTempDicToPost(docID, term, tf);
        });
    }
    /**
     *  Adds a new term into the temporary dictionary or updates a present record in it
     * @param docID
     * @param term
     * @param tf
     */
    private void addToTempDicToPost(String docID, String term, Integer tf) {
        Pair<String, Integer> termPairInfo = new Pair(docID, tf);

        if (/*terms.contains(term)*/ tempDicToPost.containsKey(term)) {
            List<Pair<String, Integer>> termPairsList = tempDicToPost.get(term);
            termPairsList.add(termPairInfo);
            tempDicToPost.put(term/*.toLowerCase()*/, termPairsList);
        } else {
            List<Pair<String, Integer>> toAdd = new LinkedList<>();
            toAdd.add(termPairInfo);
            tempDicToPost.put(term, toAdd);
        }
    }

    /**
     * Sets 'toStem' boolean field, based on user's will.
     * @param toStem - 'True' if stemming is required, 'False' otherwise.
     */
    public void setToStem(boolean toStem) {
        this.tostem = toStem;
    }

    //endregion
    /**
     * Writes the temporary dictionary's tree map into the disk
     */
    private void createNewPostingFile() {
        try {
            // Choose the path according to stemming requirement
            String postingFilePath = "";
            if (tostem == true) {
                postingFilePath = tmpPostingDirectory + "\\Stemmed_Posting_" + (STEMMED_TMP_POSTING_FILE_ID++) + ".txt";

            } else postingFilePath = tmpPostingDirectory + "\\Un_Stemmed_Posting_" + (UN_STEMMED_TMP_POSTING_FILE_ID++) + ".txt";

            // Write the temporary dictionary into a file
            BufferedWriter bw = new BufferedWriter(new FileWriter(postingFilePath));
            tempDicToPost.forEach((term, termDocList) -> {
                try {
                    bw.write(term + ":");
                    for (Pair<String, Integer> pair : termDocList) {
                        bw.write(pair.getKey() + "|" + pair.getValue() + ";");
                    }
                    bw.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Merges all temporary posting files into one.
     */
    public void mergePostings() {
        BufferedWriter bw = null;

        // Choose values according to stemming requirements
        String stemmedOrNot=stemmedOrNot();
        int TMP_POSTING_FILE_ID;
        if(tostem==true){
            TMP_POSTING_FILE_ID=STEMMED_TMP_POSTING_FILE_ID;
        }else{
            TMP_POSTING_FILE_ID=UN_STEMMED_TMP_POSTING_FILE_ID;
        }
        try {
            // Open the posting's directory
            File dir = new File(tmpPostingDirectory);
            String[] fileNames;

            int postingNumber = 0;
            // Iterate through all temporary posting files
            while ((fileNames = dir.list()).length > 1) {
                if (fileNames.length==2&&((fileNames[0].startsWith("Stemmed_Posting_") && fileNames[1].startsWith("Un_Stemmed_Posting_")) ||
                        (fileNames[0].startsWith("Un_Stemmed_Posting_") && fileNames[1].startsWith("Stemmed_Posting_")))) {
                    break;
                }
                for (int i = 0; i < fileNames.length-1; i = i + 2) {

                    // Open two files to merge
                    File file1 = new File(tmpPostingDirectory + "\\" + stemmedOrNot + postingNumber++ + ".txt");
                    File file2 = new File(tmpPostingDirectory + "\\" + stemmedOrNot + (postingNumber++) + ".txt");

                    // Open BufferedReaders for both files
                    BufferedReader br1 = new BufferedReader(new FileReader(file1));
                    BufferedReader br2 = new BufferedReader(new FileReader(file2));

                    // Open a BufferedReader merge the files to
                    bw = new BufferedWriter(new FileWriter(tmpPostingDirectory + "\\" + stemmedOrNot + (TMP_POSTING_FILE_ID++) + ".txt"));

                    // Read first line
                    String lineFile1 = br1.readLine();
                    String lineFile2 = br2.readLine();

                    while (lineFile1 != null && lineFile2 != null) {

                        // Split into term and docs list
                        String[] term1Arr = lineFile1.split(":");
                        String[] term2Arr = lineFile2.split(":");

                        // Compare between terms for alphabetic sorting
                        int copmparResult = term1Arr[0].compareToIgnoreCase(term2Arr[0]);
                        if (copmparResult == 0) {
                            if (term1Arr[0].compareTo(term2Arr[0]) != 0) {
                                term1Arr[0] = term1Arr[0].toLowerCase();
                            }else if(Character.isUpperCase(term1Arr[0].charAt(0))){
                                term1Arr[0] = term1Arr[0].toUpperCase();
                            }
                            bw.write(term1Arr[0] + ":" + term1Arr[1] + term2Arr[1] + "\n");
                            lineFile1 = br1.readLine();
                            lineFile2 = br2.readLine();
                        } else if (copmparResult > 0) {
                            bw.write(lineFile2 + "\n");
                            lineFile2 = br2.readLine();
                        } else if (copmparResult < 0) {
                            bw.write(lineFile1 + "\n");
                            lineFile1 = br1.readLine();
                        }
                    }
                    //when finished reading one of the files, write the rest of the other.
                    while (lineFile1 != null) {
                        bw.write(lineFile1 + "\n");
                        lineFile1 = br1.readLine();
                    }

                    while (lineFile2 != null) {
                        bw.write(lineFile2 + "\n");
                        lineFile2 = br2.readLine();
                    }
                    //writing the merged file to the Disk

                    // closing resources
                    br1.close();
                    br2.close();
                    bw.close();

                    //deleting already merged files
                    file1.delete();
                    file2.delete();
                }
            }

            //FileUtils.deleteDirectory(new File("tmpPostings"));
        } catch(IOException e){
            e.printStackTrace();
        }

        // Saving the last posting file's path for a later use.
        postingFile = tmpPostingDirectory + "\\"+stemmedOrNot + (TMP_POSTING_FILE_ID - 1) + ".txt";
    }

    /**
     *  Indexing the entities set
     * @param entitiesCopy
     */
    public void indexEntity(Hashtable<String, HashMap<String,Integer>> entitiesCopy) {
        HashMap<String/*docID*/, HashMap<String/*term*/, Integer/*counter*/>> allTerms = new HashMap<>();

        entitiesCopy.forEach((term, termDocList) -> {
            HashMap<String, Integer> tempTerm;
            for (Map.Entry doc: termDocList.entrySet()) {
                String docID = (String)doc.getKey();
                if (allTerms.containsKey(docID)) {
                    tempTerm = allTerms.get(docID);
                    tempTerm.put(term, (Integer)doc.getValue());
                    allTerms.put(docID, tempTerm);
                } else {
                    tempTerm = new HashMap<>();
                    tempTerm.put(term, (Integer)doc.getValue());
                    allTerms.put(docID, tempTerm);
                }
            }
        });
        HashMap<String, HashMap<String, Integer/*counter*/>> partSOfAllTerms = new HashMap<>();
        allTerms.forEach((docID, terms) -> {
            partSOfAllTerms.put(docID, terms);
            if (partSOfAllTerms.size() >= 3000) {
                index(partSOfAllTerms);
                partSOfAllTerms.clear();
            }
        });
        if (partSOfAllTerms.size() >= 1) {
            index(partSOfAllTerms);
        }

    }
    /**
     * Split the merged posting file into many, ordered on ABC order,
     * and return the dictionary object
     * @return
     */
    public HashMap<String, String[]> splitPostingFile() {
        //determine if stemmed or not
        String stemmedOrNot=stemmedOrNot();
        String[] term= null;
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(postingFile)));
            char postingName = 'a';
            String line;

            PrintWriter pw = new PrintWriter(postingDirectory +"\\numbers_"+stemmedOrNot+".txt");



            //for number posting
            int position= 0;
            while ((line = br.readLine()) != null && !(line.charAt(0) == postingName || line.charAt(0) == postingName - 32)) {
                //add to dictionary
                term = line.split("[:|;]");
                if(term[0].equals("1")){
                    int s=0;
                }
                int totalTF=0;
                for (int j = 2; j <term.length ; j+=2) {
                    totalTF+=Integer.valueOf(term[j]);
                }
                String totalTfStr=String.valueOf(totalTF);
                String df=String.valueOf((term.length - 1)/2);
                String [] termInfo= new String[]{df,totalTfStr,"numbers_"+stemmedOrNot+".txt",String.valueOf(position)};
                dictionary.put(term[0],termInfo );
                position++;

                //print to Numbers file
                pw.println(line);


            }
            pw.flush();
            pw.close();

            for (char i = 'a'; i <= 'z'; i++) {
                position=0;
                pw = new PrintWriter(postingDirectory +"\\"+i+"_"+stemmedOrNot+ ".txt");
                while ((line = br.readLine()) != null && (line.charAt(0) == i || line.charAt(0) == i - 32)) {
                    //add to dictionary
                    term = line.split("[:|;]");
                    int totalTF=0;
                    for (int j = 2; j <term.length ; j+=2) {
                        totalTF+=Integer.valueOf(term[j]);
                    }
                    String totalTfStr=String.valueOf(totalTF);
                    String df=String.valueOf((term.length - 1)/2);
                    String [] termInfo= new String[]{df,totalTfStr,i+"_"+stemmedOrNot+ ".txt",String.valueOf(position)};
                    dictionary.put(term[0],termInfo );
                    position++;

                    //print to latter 'i' file
                    pw.println(line);
                }
                pw.flush();
                pw.close();
            }
            br.close();
        } catch (Exception e) {
        }

        return dictionary;
    }
    /**
     * Returns a string according to 'toStem' boolean field
     * @return
     */
    String stemmedOrNot(){
        if(tostem==true)
            return "Stemmed_Posting_";
        else return "Un_Stemmed_Posting_";
    }


}