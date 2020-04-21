package Model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * A handler for making calls to the Datamuse RESTful API.
 *
 * @author sjblair
 * @since 21/02/15
 */
public class DataMuseConnection {

    public DataMuseConnection() {

    }

    /**
     * Returns a list of similar words to the word/phrase supplied.
     * @param word A word of phrase.
     * @return A list of similar words.
     */
    public String[] findSynonyms(String word) {
        String s = word.replaceAll(" ", "+");
        String response=getJSON("http://api.datamuse.com/words?rel_syn="+s);
        //response+=speltSimilar(word);
        String[] result= getArr(response);
        return result;
    }


    private String[] getArr(String response) {
        String[] result;
        response=response.replaceAll("[\"{}]","");
       // response=response.replaceAll("word","");
        response=response.replaceAll("score","");
        response=response.replaceAll("]","");
        response=response.replaceAll("\\[","");
        response=response.replaceAll("\\d","");
        response=response.replaceAll("tags:syn,n","");
        response=response.replaceAll("tags:n","");
        response=response.replaceAll(",","");
        response=response.replaceAll(":","");
        response=response.replaceAll("word",",");
        result= response.split(",");//\{"word":"
        return result;
    }




    /**
     * Query a URL for their source code.
     * @param url The page's URL.
     * @return The source code.
     */
    private String getJSON(String url) {
        URL datamuse;
        URLConnection dc;
        StringBuilder s = null;
        try {
            datamuse = new URL(url);
            dc = datamuse.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(dc.getInputStream(), "UTF-8"));
            String inputLine;
            s = new StringBuilder();
            while ((inputLine = in.readLine()) != null)
                s.append(inputLine);
            in.close();
        } catch (MalformedURLException e) {
        } catch (IOException e) {
        }
        return s != null ? s.toString() : null;
    }
}