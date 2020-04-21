package Model;

import javafx.util.Pair;

import java.util.*;

public class Ranker {
    //region Fields
    public boolean toStem;
    public HashMap<String, String[]> dictionary;//{df, ttf, postingName,position}
    public HashMap<String, int[]> docsDictionary;//{max_tf,number_of_terms,size,number_entities}
    HashMap<String, LinkedList<Pair<String, Integer>>> docsEntities;
    double B = 0.5;
    double K = 0.8; // K = [1.2,2]
    double AVG_LENGTH = 2668;
    //endregion

    //region Constructors

    /**
     * Constructor
     *
     * @param dictionary
     * @param docsDictionary
     * @param docsEntities
     */
    public Ranker(HashMap<String, String[]> dictionary, HashMap<String, int[]> docsDictionary, HashMap<String, LinkedList<Pair<String, Integer>>> docsEntities) {
        this.dictionary = dictionary;
        this.docsDictionary = docsDictionary;
        this.docsEntities = docsEntities;
    }
    //endregion

    //region Setters & Getters
    public void setToStem(boolean toStem) {
        this.toStem = toStem;
    }
    //endregion

    //region Ranking

    /**
     * The method activates the ranking process.
     *
     * @param docResult
     * @return
     */
    public TreeMap<Double, String> rank(HashMap<String, List<Pair<String, Integer>>> docResult) {//docResult: <docID, list< term, tf > >:
        return getBM25(docResult);
    }

    /**
     * the method receives the semantic search results and the normal search results, and computes a total rank of the documents.
     *
     * @param docResult1
     * @param docResult2
     * @return
     */
    public TreeMap<Double, String> rankWithWeight(HashMap<String, List<Pair<String, Integer>>> docResult1, HashMap<String, List<Pair<String, Integer>>> docResult2) {
        TreeMap<Double, String> BM25 = new TreeMap<>(new Comparator<Double>() {
            @Override
            public int compare(Double o1, Double o2) {
                if (o1 > o2) return -1;
                else if (o1 == o2) return 0;
                else return 1;
            }
        });

        HashSet<String> oldDoc = new HashSet<>();

        for (Map.Entry<String, List<Pair<String, Integer>>> docEntry1 : docResult1.entrySet()) {
            double score;
            double score1 = 0;
            double score2 = 0;
            String docID = docEntry1.getKey();
            oldDoc.add(docID);
            if (docResult2.containsKey(docEntry1.getKey())) {
                List<Pair<String, Integer>> docList2 = docResult2.get(docID);//get term list for docID
                for (Pair<String, Integer> qi : docEntry1.getValue()) {
                    score1 += getBM25(qi, docID);
                }
                for (Pair<String, Integer> qi : docList2) {
                    score2 += getBM25(qi, docID);
                }
                score = 0.9 * score1 + 0.1 * score2;

                BM25.put(score, docID);
            } else {
                for (Pair<String, Integer> qi : docEntry1.getValue()) {
                    score1 += 0.9 * getBM25(qi, docID);
                }
                BM25.put(score1, docID);
            }

        }
        for (Map.Entry<String, List<Pair<String, Integer>>> docEntry2 : docResult2.entrySet()) {
            double score2 = 0;
            String docID = docEntry2.getKey();
            if (!oldDoc.contains(docID)) {
                oldDoc.add(docID);
                for (Pair<String, Integer> qi : docEntry2.getValue()) {
                    score2 += 0.1 * getBM25(qi, docID);
                }
                BM25.put(score2, docID);
            }
        }

        return BM25;


    }
    //endregion

    //region Top entities

    /**
     * The method receives a docID, and returns it's top 5 dominant entities.
     *
     * @param docID
     * @return
     */
    public TreeMap<String, Double> getTopEntities(String docID) {
        TreeMap<String, Double> topEntities = new TreeMap<>();

        LinkedList<Pair<String, Integer>> entities = docsEntities.get(docID);
        if (entities == null) {
            return new TreeMap<>();
        }
        Collections.sort(entities, new Comparator<Pair<String, Integer>>() {
            @Override
            public int compare(Pair<String, Integer> p1, Pair<String, Integer> p2) {
                return p2.getValue() - p1.getValue();
            }
        });

        double max_tf = entities.get(0).getValue();
        int counter = 1;
        for (Pair<String, Integer> entity : entities) {
            if (counter <= 5) {
                topEntities.put(entity.getKey(), Double.valueOf(entity.getValue() / max_tf));
                counter++;
            } else {
                break;
            }
        }

        return topEntities;
    }
    //endregion

    //region Assistance methods

    /**
     * Clears all Ranker's data structures on the RAM.
     */
    public void clear() {
        if (dictionary != null) dictionary.clear();
        if (docsDictionary != null) docsDictionary.clear();
        if (docsDictionary != null) docsEntities.clear();
    }
    //endregion

    //region BM25 calculations

    /**
     * The method receives a map of < docID, list of query terms (and tf) which appear on that doc >,
     * and computes it's score(Q,D) based on BM25 formula.
     *
     * @param docResult
     * @return
     */
    private TreeMap<Double, String> getBM25(HashMap<String, List<Pair<String, Integer>>> docResult) {//docResult: <docID, list< term, tf > >:
        TreeMap<Double, String> BM25 = new TreeMap<>(new Comparator<Double>() {
            @Override
            public int compare(Double o1, Double o2) {
                if (o1 > o2) return -1;
                else if (o1 == o2) return 0;
                else return 1;
            }
        });
        for (Map.Entry<String, List<Pair<String, Integer>>> entry : docResult.entrySet()) {
            double score = 0;
            for (Pair<String, Integer> qi : entry.getValue()) {
                score += getBM25(qi, entry.getKey());
            }
            BM25.put(score-entry.getValue().size(), entry.getKey());
        }
        return BM25;
    }

    /**
     * The method computes a part of BM25 value to query Q and document D, score(Q,D)
     *
     * @param qi
     * @param docID
     * @return
     */
    private double getBM25(Pair<String, Integer> qi, String docID) {
        double IDF = getIDF(qi.getKey());
        double tf = getTF(qi, docID);
        int docLength = docsDictionary.get(docID)[2];// docsDictionary(docID)[1] holds the num of terms in docID
        if (toStem) {
            B = 0.01;
            K = 1.2;
            return IDF * ((tf * (K + 1)) / (tf + (K * (1 - B + (B * (docLength / AVG_LENGTH))))));
        }
        B = 0.5;
        K = 0.8;
        return IDF * ((tf * (K + 1))/ (tf + (K * (1 - B + (B * (docLength / AVG_LENGTH))))));
    }

    /**
     * The method computes IDF value for a term i in query Q
     *
     * @param q_i
     * @return
     */
    private double getIDF(String q_i) {
        double df = Integer.valueOf(dictionary.get(q_i)[0]);//dictionary.get(q_i)[0]) holds the df
        double IDF = (Math.log((docsDictionary.size()+0.5-df)/(df+0.5)) );
        return IDF;
    }

    /**
     * The method computes and returns the query's term-frequency.
     *
     * @param qi
     * @param docID
     * @return
     */
    private double getTF(Pair<String, Integer> qi, String docID) {
        double fi = qi.getValue();
        double max_fi = 1;//docsDictionary.get(docID)[2];
        double tf = fi / max_fi;
        return tf;
    }

    //endregion

}
