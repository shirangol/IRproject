package Model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import opennlp.tools.stemmer.PorterStemmer;

public class Parser {

    //region Fields
    Boolean toStem = false;
    private int i = 0;
    public Hashtable<String, String> stopWords;
    public HashMap<String, Integer/*counter*/> terms;
    private LinkedList<SingleDoc> docsList;
    public Hashtable<String/*term*/, HashMap<String,Integer>/*docID,tf*/> entities;
    public Hashtable<String, String> monthsSet;
    public Hashtable<String, String> distanceSet;
    public Hashtable<String, String> weightMeasureSet;
    public HashMap<String, Integer> docMax_tf;
    int max_tf = 1;
    //endregion

    //region Constructors

    /**
     * Constructor
     *
     * @param stopWordsURL
     */
    public Parser(String stopWordsURL /*LinkedList<SingleDoc> docsList*/) {
        entities = new Hashtable<>();
        createStopWordsMap(stopWordsURL);
        this.docsList = new LinkedList<>();
        monthsSetBuilder();
        distanceSetBuilder();
        weightMeasureSetBuilder();
    }
//endregion

    //region Assistance Data Structures Building

    /**
     * The method adds capital letter version to the stop words's list
     * and inserts the list into a hashMap
     *
     * @param stopWordsURL- stopWords URL
     */
    public void createStopWordsMap(String stopWordsURL) {
        stopWords = new Hashtable<>();
        String word = null;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(stopWordsURL));
            while ((word = reader.readLine()) != null) {
                String CapitalWord = word.substring(0, 1).toUpperCase() + word.substring(1);
                stopWords.put(word, word);
                stopWords.put(CapitalWord, CapitalWord);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a set of measures for measures parsing
     */
    private void weightMeasureSetBuilder() {
        weightMeasureSet = new Hashtable<>();
        weightMeasureSet.put("Tonne", "Tonne");
        weightMeasureSet.put("tonne", "Tonne");
        weightMeasureSet.put("Tonnes", "Tonne");
        weightMeasureSet.put("tonnes", "Tonne");
        weightMeasureSet.put("Ton", "Tonne");
        weightMeasureSet.put("ton", "Tonne");
        weightMeasureSet.put("Tons", "Tonne");
        weightMeasureSet.put("tons", "Tonne");

        weightMeasureSet.put("kilogram", "kilogram");
        weightMeasureSet.put("Kilogram", "kilogram");
        weightMeasureSet.put("kilogramme", "kilogram");
        weightMeasureSet.put("Kilogramme", "kilogram");
        weightMeasureSet.put("kilogrammes", "kilogram");
        weightMeasureSet.put("Kilogrammes", "kilogram");
        weightMeasureSet.put("kilograms", "kilogram");
        weightMeasureSet.put("Kilograms", "kilogram");
        weightMeasureSet.put("KG", "kilogram");
        weightMeasureSet.put("kg", "kilogram");

        weightMeasureSet.put("g", "gram");
        weightMeasureSet.put("gr", "gram");
        weightMeasureSet.put("gm", "gram");
        weightMeasureSet.put("gramme", "gram");
        weightMeasureSet.put("grammes", "gram");
        weightMeasureSet.put("Gramme", "gram");
        weightMeasureSet.put("Grammes", "gram");
        weightMeasureSet.put("gram", "gram");
        weightMeasureSet.put("Gram", "gram");
        weightMeasureSet.put("grams", "gram");
        weightMeasureSet.put("Grams", "gram");

        weightMeasureSet.put("mg", "milligram");
        weightMeasureSet.put("milligram", "milligram");
        weightMeasureSet.put("Milligram", "milligram");
        weightMeasureSet.put("milligrams", "milligram");
        weightMeasureSet.put("Milligrams", "milligram");
        weightMeasureSet.put("milligramme", "milligram");
        weightMeasureSet.put("Milligramme", "milligram");
        weightMeasureSet.put("milligrammes", "milligram");
        weightMeasureSet.put("Milligrammes", "milligram");
    }

    /**
     * Creates a set of distances for distances parsing
     */
    private void distanceSetBuilder() {
        distanceSet = new Hashtable<>();
        distanceSet.put("KM", "Kilometer");
        distanceSet.put("km", "Kilometer");
        distanceSet.put("KILOMETERS", "Kilometer");
        distanceSet.put("kilometer", "Kilometer");
        distanceSet.put("KILOMETER", "Kilometer");
        distanceSet.put("Kilometer", "Kilometer");
        distanceSet.put("sq km", "square Kilometer");
        distanceSet.put("sq km.", "square Kilometer");
        distanceSet.put("sq km", "square Kilometer");
        distanceSet.put("square Kilometers", "square Kilometer");
        distanceSet.put("square Kilometer", "square Kilometer");
        distanceSet.put("square km", "square Kilometer");

        distanceSet.put("m", "Meter");
        distanceSet.put("M", "Meter");
        distanceSet.put("meters", "Meter");
        distanceSet.put("METERS", "Meter");
        distanceSet.put("Meters", "Meter");
        distanceSet.put("meter", "Meter");
        distanceSet.put("Meter", "Meter");
        distanceSet.put("METER", "Meter");
        distanceSet.put("metre", "Meter");
        distanceSet.put("Metre", "Meter");
        distanceSet.put("metres", "Meter");
        distanceSet.put("Metres", "Meter");
        distanceSet.put("sq m", "square Meter");
        distanceSet.put("square meters", "square Meter");
        distanceSet.put("square meter", "square Meter");

        distanceSet.put("cm", "centimeter");
        distanceSet.put("Centimeter", "centimeter");
        distanceSet.put("centimeter", "centimeter");
        distanceSet.put("CENTIMETER", "centimeter");
        distanceSet.put("CENTIMETERS", "centimeter");
        distanceSet.put("Centimeters", "centimeter");
        distanceSet.put("centimeters", "centimeter");
        distanceSet.put("Centimetre", "centimeter");
        distanceSet.put("centimetre", "centimeter");

        distanceSet.put("mm", "millimeter");
        distanceSet.put("millimeter", "millimeter");
        distanceSet.put("Millimeter", "millimeter");
        distanceSet.put("MILLIMETER", "millimeter");
        distanceSet.put("MILLIMETERS", "millimeter");
        distanceSet.put("millimeters", "millimeter");
        distanceSet.put("Millimeters", "millimeter");
        distanceSet.put("millimetres", "millimeter");
        distanceSet.put("Millimetres", "millimeter");
        distanceSet.put("millimetre", "millimeter");
        distanceSet.put("Millimetre", "millimeter");
    }

    /**
     * Creates a set of months for dates parsing
     */
    private void monthsSetBuilder() {
        monthsSet = new Hashtable<>();
        monthsSet.put("January", "01");
        monthsSet.put("JANUARY", "01");
        monthsSet.put("january", "01");
        monthsSet.put("Jan", "01");
        monthsSet.put("JAN", "01");

        monthsSet.put("February", "02");
        monthsSet.put("FEBRUARY", "02");
        monthsSet.put("february", "02");
        monthsSet.put("Feb", "02");
        monthsSet.put("FEB", "02");

        monthsSet.put("March", "03");
        monthsSet.put("MARCH", "03");
        monthsSet.put("march", "03");
        monthsSet.put("Mar", "03");
        monthsSet.put("MAR", "03");

        monthsSet.put("April", "04");
        monthsSet.put("APRIL", "04");
        monthsSet.put("april", "04");
        monthsSet.put("Apr", "04");
        monthsSet.put("APR", "04");

        monthsSet.put("May", "05");
        monthsSet.put("MAY", "05");/* monthsSet.add("may");*/

        monthsSet.put("June", "06");
        monthsSet.put("JUNE", "06");
        monthsSet.put("june", "06");
        monthsSet.put("Jun", "06");
        monthsSet.put("JUN", "06");

        monthsSet.put("July", "07");
        monthsSet.put("JULY", "07");
        monthsSet.put("july", "07");
        monthsSet.put("Jul", "07");
        monthsSet.put("JUL", "07");

        monthsSet.put("August", "08");
        monthsSet.put("AUGUST", "08");
        monthsSet.put("august", "08");
        monthsSet.put("Aug", "08");
        monthsSet.put("AUG", "08");

        monthsSet.put("September", "09");
        monthsSet.put("SEPTEMBER", "09");
        monthsSet.put("september", "09");
        monthsSet.put("Sep", "09");
        monthsSet.put("SEP", "09");

        monthsSet.put("October", "10");
        monthsSet.put("OCTOBER", "10");
        monthsSet.put("october", "10");
        monthsSet.put("Oct", "10");
        monthsSet.put("OCT", "10");

        monthsSet.put("November", "11");
        monthsSet.put("NOVEMBER", "11");
        monthsSet.put("november", "11");
        monthsSet.put("Nov", "11");
        monthsSet.put("NOV", "11");

        monthsSet.put("December", "12");
        monthsSet.put("DECEMBER", "12");
        monthsSet.put("december", "12");
        monthsSet.put("Dec", "12");
        monthsSet.put("DEC", "12");
    }
    //endregion

    //region Setters and Getters

    /**
     * Set a new List of docs for parsing
     *
     * @param docsList
     */
    public void setDocsList(LinkedList<SingleDoc> docsList) {
        this.docsList = docsList;
    }

    /**
     * Sets 'toStem' boolean field according to user's will.
     *
     * @param toStem - is 'True' if stemming is required, and 'False' otherwise
     */
    public void setToStem(boolean toStem) {
        this.toStem = toStem;
    }
    //endregion

    //region Parse functions

    /**
     * The method calls the parse method for each doc in the parser's docs list,
     * and holds a terms set for each doc that is being parsed.
     *
     * @return
     */
    public HashMap<String, HashMap<String, Integer/*counter*/>> parse() {
        docMax_tf = new HashMap<>();
        HashMap<String, HashMap<String, Integer/*counter*/>> allTerms = new HashMap<>();
        for (int j = 0; j < docsList.size(); j++) {
            String docID = docsList.get(j).docID;

            // Send for parsing
            allTerms.put(docID, parse(docsList.get(j).text, docID));

            //Add doc's Max_tf to docMax_tf HashMap
            if (!docMax_tf.containsKey(docID)) {
                docMax_tf.put(docID, max_tf);
            }
        }
        return (allTerms);
    }

    /**
     * The method receives docID and it's TEXT field, and parse the text into terms.
     *
     * @param textField
     * @param docId
     * @return
     */
    public HashMap<String, Integer/*counter*/> parse(String textField, String docId) {
        max_tf = 1;
        terms = new HashMap<>();

        // Split the text into an array of tokens
        String[] allWords = splitText(textField);
        String allwordsi;

        // Iterating through all tokens and parsing them
        for (i = 0; i < allWords.length && !allWords[i].equals(""); i++) {
            allwordsi = allWords[i];

            // Make sure the token isn't a stop-word
            if (stopWords.containsKey(allwordsi) && !allWords.equals("May")) {
                continue;
            }
            // trims "." and "-" at the end of the token
            if ((allwordsi.endsWith(".") || allwordsi.endsWith("-")) && allwordsi.length() > 1) {//for term:"X-||X."
                allwordsi = allwordsi.substring(0, allwordsi.length() - 1);
            }
            // trims "." and "-" at the begining of the token
            if (allwordsi.startsWith("-") || allwordsi.startsWith(".")) {//for term:"-X||.X"
                allwordsi = allwordsi.substring(1);
            }
            try {
                if(allwordsi.equals("")) continue;

                // save a term with the pattern: word--word
                if (allwordsi.contains("--")) {
                    addTerms(allwordsi);
                    continue;

                } else if (allwordsi.length() > 1 && !allwordsi.contains("-") && isNumber(allwordsi)) {//the string is numeric
                    if (isDate(allWords)) continue;
                    if (isPercentage(allWords)) continue;
                    if (isPrice(allWords)) continue;
                    if (isDistance(allWords)) continue;
                    if (isweight(allWords)) continue;
                    if (isRegularNum(allWords)) continue;
                } else if (allwordsi.length() > 1 && !(allwordsi.charAt(0) == '-')) {
                    if (isRange(allWords)) continue;
                    if (isDate(allWords)) continue;
                    if (isEntity(allWords, docId)) continue;
                }
                // The token is none of the above, Stemm it if required and make sure it is not a stop-word
                if (allwordsi.length() > 1 || (allwordsi.charAt(0) >= 0 && allwordsi.charAt(0) <= 9)) {
                    if (!stopWords.containsKey(allwordsi) && toStem) {
                        PorterStemmer stemmer = new PorterStemmer();
                        allwordsi = stemmer.stem(allwordsi);
                        addTerms(allwordsi);
                    } else if (!stopWords.containsKey(allwordsi)) {
                        addTerms(allwordsi);
                    }
                }

            } catch (Exception e) {

            }
        }
        return upperLowerLetters(terms);
    }
    //endregion

    //region Classification of tokens Methods

    /**
     * Checks if the token is a weight token, and parses it accordingly.
     *
     * @param allWords
     * @return
     */
    private boolean isweight(String[] allWords) {
        if (i + 1 < allWords.length) {
            if (weightMeasureSet.containsKey(allWords[i + 1])) {
                String num = getNum(allWords[i]);
                String k = weightMeasureSet.get(allWords[i + 1]);
                addTerms(num + " " + weightMeasureSet.get(allWords[i + 1]));
                i += 1;
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the token is a distance token, and parses it accordingly.
     *
     * @param allWords
     * @return
     */
    private boolean isDistance(String[] allWords) {
        if (i + 1 < allWords.length) {
            if (distanceSet.containsKey(allWords[i + 1])) {
                String num = getNum(allWords[i]);
                addTerms(num + " " + distanceSet.get(allWords[i + 1]));
                i += 1;
                return true;
            }
        }
        if (i + 2 < allWords.length) {
            String sqKm = allWords[i + 1] + " " + allWords[i + 2];
            if (distanceSet.containsKey(sqKm)) {// sq km
                String num = getNum(allWords[i]);
                addTerms(num + " " + distanceSet.get(sqKm));
                i += 1;
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the token is an entity token, and accumulates it if it fits the entities criteria
     *
     * @param allWords
     * @param docID
     * @return
     */
    private boolean isEntity(String[] allWords, String docID) {
        int k = i;
        String str = "";
        boolean isToAdd = false;
        for (; k < allWords.length && !allWords[k].contains("--"); k = k + 1) {
            if (allWords[k].contains("-")) {
                String[] arr = allWords[k].split("-");
                String tempStr = "";
                for (int j = 0; j < arr.length && arr.length > 1 && !arr[0].equals(""); j++) {
                    if (/*arr[j].matches("^[a-zA-Z]*$") && */Character.isUpperCase(arr[j].charAt(0))) {
                        tempStr = tempStr + " " + arr[j];
                        isToAdd = true;
                    } else {
                        isToAdd = false;
                        continue;
                    }
                }
                if (isToAdd) {//check if to add "temp" to str
                    str = str + tempStr;
                    i = k;
                } else {
                    break;
                }
            } else if (Character.isUpperCase(allWords[k].charAt(0))) {
                str += " " + allWords[k];
                i = k;
            } else {
                //k--;
                break;
            }
            //k++;
        }

        if (str.length() > 0) {//check if add to terms (term!="")
            str = str.substring(1);//remove space in the begging of term
            String[] check = str.split(" ");
            if (check.length <= 1) {
                //i=i-1;
                return false;
            }
            if (!str.contains(" ") && stopWords.containsKey(str)) {
                //i=i-1;
                return false;
            }
            String[] entityArr = str.split(" ");
            for (int j = 0; j < entityArr.length; j++) {
                String term = entityArr[j];
                if (!stopWords.contains(term)) {
                    if (term.contains(".")) {
                        if (term.endsWith(".")) {
                            term = term.substring(0, term.length() - 1);
                        } else if (term.startsWith(".")) {
                            term = term.substring(1);
                        }
                    }
                    if(toStem){
                        PorterStemmer stemmer = new PorterStemmer();
                        term = stemmer.stem(term);
                    }
                    addTerms(term);
                }
            }
            addEntities(str, docID);
            return true;
        }
        return false;
    }

    /**
     * Checks if the token is a range token, and parses it accordingly.
     *
     * @param allWords
     * @return
     */
    private boolean isRange(String[] allWords) {
        try {
            NumConverter numConverter = new NumConverter();
            if (allWords[i].matches("Between") && i + 3 < allWords.length) {
                if (isNumber(allWords[i + 1]) && allWords[i + 2].matches("and") && isNumber(allWords[i + 3])) {
                    String num1 = numConverter.convert(allWords[i + 1]);
                    String num2 = numConverter.convert(allWords[i + 3]);
                    addTerms(num1);
                    addTerms(num2);
                    addTerms(num1 + "-" + num2);
                    i += 3;
                }
            } else if (allWords[i].contains("-")) {
                String[] arr = allWords[i].split("-");
                if (arr.length > 1) {
                    if (isNumber(arr[0])) {
                        if (isNumber(arr[1])) { //Number-Number
                            String num1 = numConverter.convert(arr[0]);
                            String num2 = numConverter.convert(arr[1]);
                            addTerms(num1);
                            addTerms(num2);
                            addTerms(num1 + "-" + num2);
                        } else addTerms(allWords[i]); //Number-word
                        return true;
                    } else if (isNumber(arr[1]) || (Character.isUpperCase(arr[0].charAt(0)) && Character.isLowerCase(arr[1].charAt(0)))) {//Word-word || Word-word-word || Word-Number
                        addTerms(allWords[i]);
                        return true;
                    }
                }
            }
        } catch (Exception e) {

        }
        return false;
    }

    /**
     * Checks if the token is a regular number token, and parses it accordingly.
     *
     * @param allWords
     * @return
     */
    private boolean isRegularNum(String[] allWords) {
        String toConvert;
        if (i + 1 < allWords.length && (allWords[i + 1].matches("Thousand|Million|Billion") || allWords[i + 1].contains("/"))) {
            toConvert = allWords[i] + " " + allWords[i + 1];
            i = i + 1;
        } else {
            toConvert = allWords[i];
        }
        String str;
        if (toConvert.contains("/")) {
            str = getNum(allWords[i - 1]);
        } else {
            str = getNum(toConvert);
        }
        double val = Double.valueOf(str);
        if (val < 1000) {
            str = getRoundNumber(str);
            StringTokenizer sFraction = new StringTokenizer(toConvert, " ");
            String s = " ";
            if (sFraction.hasMoreTokens()) {
                s = sFraction.nextToken();
            }
            if (sFraction.hasMoreTokens()) {
                s = sFraction.nextToken();
            }
            if (s.contains("/")) {
                addTerms(str + " " + s);
            } else if (s.equals("Million")) {
                addTerms(str + "M");
            } else if (s.equals("Billion")) {
                addTerms(str + "B");
            } else if (s.equals("Thousand")) {
                addTerms(str + "K");
            } else {
                addTerms(str);
            }
        }
        //K
        else if (val >= 1000 && val < 1000000) {
            str = getRoundNumber(String.valueOf(val / 1000));
            return addTerms(str + "K");
        }
        //M
        else if (val >= 1000000 && val < Math.pow(10, 9)) {
            val = val / (Math.pow(10, 6));
            str = getRoundNumber((String.valueOf(val)));
            addTerms(str + "M");
        }
        //B
        else {
            val = val / (Math.pow(10, 9));
            str = getRoundNumber((String.valueOf(val)));
            addTerms(str + "B");
        }
        return true;
    }

    /**
     * Checks if the token is a price token, and parses it accordingly.
     *
     * @param allWords
     * @return
     */
    private boolean isPrice(String[] allWords) {
        Double num = Double.parseDouble(getNum(allWords[i]));
        String priceTerm = "";
        if (allWords[i].contains("$")) {//$price || $price X
            if (i + 1 < allWords.length) {
                switch (allWords[i + 1]) {
                    case ("Dollars"): // $price Dollars ---> price Dollars
                        priceTerm = NumberFormat.getNumberInstance(Locale.US).format(num) + " Dollars";
                        i = i + 1;
                        break;
                    case ("million"):
                        priceTerm = NumberFormat.getNumberInstance(Locale.US).format(num) + " M Dollars";
                        i = i + 1;
                        break;
                    case ("billion"):
                        priceTerm = (int) (num * 1000) + " M Dollars";
                        i = i + 1;
                        break;
                }
                addTerms(priceTerm);
                return true;
            } else {
                if (num >= 1000000) addTerms((int) (num / 1000000) + " M Dollars");//$450,000,000 ----> 450 M Dollars
                else
                    addTerms(NumberFormat.getNumberInstance(Locale.US).format(num) + " Dollars");//$450,000 ----> 450,000 Dollars
                return true;
            }
        } else if (i + 1 < allWords.length && allWords[i + 1].matches("Dollars")) {  // price Dollars
            switch (allWords[i].charAt(allWords[i].length() - 1)) {
                case ('m')://20.6m Dollars ----> 20.6 M Dollars
                    priceTerm = NumberFormat.getNumberInstance(Locale.US).format(num) + " M Dollars";
                    break;
                case ('n')://100bn Dollars ----> 100000 M Dollars
                    priceTerm = (int) (num * 1000) + " M Dollars";
                    break;
                default:
                    if (num >= 1000000) priceTerm = (int) (num / 1000000) + " M Dollars";
                    else priceTerm = NumberFormat.getNumberInstance(Locale.US).format(num) + " Dollars";
            }
            addTerms(priceTerm);
            i = i + 1;
            return true;
        } else if (i + 2 < allWords.length) {
            if (allWords[i + 2].matches("Dollars")) {  // Price X Dollars
                priceTerm = allWords[i] + " " + allWords[i + 1] + " Dollars";
                addTerms(priceTerm);
                i = i + 2;
                return true;
            } else if (allWords[i + 2].matches("U.S.") && i + 3 < allWords.length && allWords[i + 3].matches("dollars")) { //Price X U.S. dollars
                switch (allWords[i + 1]) {
                    case ("million"):
                        priceTerm = NumberFormat.getNumberInstance(Locale.US).format(num) + " M Dollars";
                        break;
                    case ("millions"):
                        priceTerm = NumberFormat.getNumberInstance(Locale.US).format(num) + " M Dollars";
                        break;
                    case ("billion"):
                        priceTerm = (int) (num * 1000) + " M Dollars";
                        break;
                    case ("trillion"):
                        priceTerm = (int) (num * 1000000) + " M Dollars";
                        break;
                    case ("thousand"):
                        priceTerm = (int) (num * 1) + " k Dollars";
                        break;
                }
                addTerms(priceTerm);
                i = i + 3;
                return true;
            }

        }
        return false;
    }

    /**
     * Checks if the token is a date token, and parses it accordingly.
     *
     * @param allWords
     * @return
     */
    private boolean isDate(String[] allWords) {
        if (i + 1 < allWords.length) {
            String allwordsi = allWords[i];
            String allwordsi1 = allWords[i + 1];
            if (monthsSet.containsKey(allwordsi)) {
                if (isNumber(allwordsi1)) {
                    String num = getNum(allwordsi1);
                    switch (num.length()) {
                        case (1):
                            addTerms(monthsSet.get(allwordsi) + "-0" + num);
                            break;
                        case (2):
                            addTerms(monthsSet.get(allwordsi) + "-" + num);
                            break;
                        case (4):
                            addTerms(num + "-" + monthsSet.get(allwordsi));
                            break;
                    }
                    i += 1;
                    return true;
                }
            } else if (monthsSet.containsKey(allWords[i + 1])) {
                String num = getNum(allwordsi);
                switch (num.length()) {
                    case (1):
                        addTerms(monthsSet.get(allwordsi1) + "-0" + num);
                        break;
                    case (2):
                        addTerms(monthsSet.get(allwordsi1) + "-" + num);
                        break;
                }
                i += 1;
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the token is a percentage token, and parses it accordingly.
     *
     * @param allWords
     * @return
     */
    private boolean isPercentage(String[] allWords) {
        if (allWords[i].contains("%")) {
            addTerms(getNum(allWords[i]) + "%");
            return true;
        }
        if(i + 1 < allWords.length && (allWords[i + 1].endsWith(".")|| allWords[i + 1].endsWith(","))){//for X. or X,->X
            allWords[i + 1]=allWords[i + 1].substring(0,allWords[i + 1].length()-1);
        }
        if (i + 1 < allWords.length && allWords[i + 1].matches("percent|percentage|Percent|Percentage")) {
            addTerms(getNum(allWords[i]) + "%");
            i += 1;
            return true;
        }
        return false;
    }
    //endregion

    //region Adding terms and Entities + Entities Processing

    /**
     * The method adds new terms into the term set.
     *
     * @param term
     * @return
     */
    private boolean addTerms(String term) {
        if (!term.equals("")) {
            if (terms.containsKey(term)) {
                int tf = terms.get(term) + 1;
                terms.put(term, terms.get(term) + 1);
                if (max_tf < tf) {
                    max_tf = tf;
                }
                return true;
            } else
                terms.put(term, 1);
        }
        return false;
    }

    /**
     * The method adds new entity into the entities set.
     *
     * @param term
     * @param docID
     * @return
     */
    private boolean addEntities(String term, String docID) {
        HashMap<String, Integer> docs = new HashMap<>();
        if (entities.isEmpty() || !entities.containsKey(term)) {
            docs.put(docID,1);
            entities.put(term, docs);
        } else {
            docs = entities.get(term);
            Integer doc= getDocFromEntities(docID,docs);
            if (doc==null) {
                docs.put(docID,1);
            }
            else{
                docs.put(docID,doc);
            }
            entities.put(term, docs);
        }
        return true;
    }

    //return doc pair if exist in entities
    private Integer getDocFromEntities(String docID,HashMap<String, Integer> docs ){
        for(String doc:docs.keySet()){
            if(doc.equals(docID)) {
                return docs.get(doc) + 1;
            }
        }
        return null;
    }

    /**
     * The method Iterates through all entities accumulated during the parsing phase,
     * determines for each of the tokens if its an entity or not,
     * and parses them accordingly.
     */
    public void entityProcessor() {
        max_tf = 1;
        docMax_tf = new HashMap<>();
        terms = new HashMap<>();
        Hashtable<String, HashMap<String,Integer>> entitiesCopy = new Hashtable<>();

        // Iterating through the entities set
        entities.forEach((term, docsList) -> {
            if (docsList.size() > 1)
                entitiesCopy.put(term.toUpperCase(), docsList);
        });
        entities = entitiesCopy;
    }
    //endregion

    //region Assistance Methods

    /**
     * uniting all upper & lower letters of the same term to one lowerCase term.
     * @param terms
     * @return
     */
    private HashMap<String, Integer> upperLowerLetters(HashMap<String, Integer> terms) {
        HashMap<String, Integer> termsCopy = new HashMap<>();
        for (String term : terms.keySet()) {
            if (term.length() > 0 && Character.isUpperCase(term.charAt(0))) {
                String notCapitalized = term.toLowerCase();/*Character.toLowerCase(term.charAt(0)) + term.substring(1, term.length() - 1);*/
                if (terms.containsKey(notCapitalized)) {
                    //in case that there is a non-capitalized version of the term, add both counters to the temp map
                    if (!termsCopy.containsKey(notCapitalized)) termsCopy.put(notCapitalized, terms.get(term));
                    else termsCopy.put(notCapitalized, terms.get(term) + terms.get(notCapitalized));
                } else {
                    termsCopy.put(term.toUpperCase(), terms.get(term));
                }
            } else termsCopy.put(term, terms.get(term));
        }
        return termsCopy;
    }
    /**
     * Clears all Parser's data structures.
     */
    public void clear() {
        if (stopWords != null) stopWords.clear();
        if (terms != null) terms.clear();
        if (docsList != null) docsList.clear();
        if (entities != null) entities.clear();
        if (monthsSet != null) monthsSet.clear();
        if (distanceSet != null) distanceSet.clear();
        if (weightMeasureSet != null) weightMeasureSet.clear();
        if(docMax_tf != null) docMax_tf.clear();
    }

    /**
     * Returns 'True' if the string is numeric, and 'False' otherwise
     *
     * @param str
     * @return
     */
    private boolean isNumber(String str) {
        return str.matches(".\\d.") || str.matches("\\d+(\\.\\d+)?") || str.contains("$") || str.contains(",");
    }

    /**
     * Receives a string and returns its numeric rounded value.
     *
     * @param str
     * @return
     */
    private String getRoundNumber(String str) {
        if (str.contains(".")) {
            for (int i = 0; i < str.length(); i++) {
                if (str.charAt(i) == '.') {
                    if (str.length() - i > 3) {
                        str = str.substring(0, i + 4);
                    }
                    for (int j = str.length() - 1; j > i; j--) {
                        if (str.charAt(j) != '0') {
                            return str.substring(0, j + 1);
                        }
                    }
                    str = str.substring(0, i);
                }
            }
        }
        return str;
    }

    /**
     * Receives a string and returns its numeric value.
     *
     * @param s
     * @return
     */
    private String getNum(String s) {
        String str = "";
        Pattern p = Pattern.compile("(\\d+)" + "?\\d+(\\.\\d+)?");
        Matcher m = p.matcher(s);
        while (m.find()) {
            str += m.group();
        }
        return str;
    }

    /**
     * Splits the text into an array of tokens
     *
     * @param textField
     * @return
     */
    private String[] splitText(String textField) {
        textField = textField.replaceAll("!|,|\\*|\\(|\\)|'|\"|:|;|`|\\{|}|\\?|\\[|]|\\\\|#|--|\\+|---|&|\\.\\.\\.|\\.\\.|\\||=|>|<|//|/|\n|_", "");
        String[] allWords = textField.trim().split("\\s+");
        return allWords;
    }
    //endregion
}