import Model.Model;
import Model.Searcher;
import com.medallia.word2vec.Word2VecModel;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.util.Pair;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;


public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Controller controller = new Controller();
        Parent root = FXMLLoader.load(getClass().getResource("IRgui.fxml"));
        primaryStage.setTitle("Better than google");
        primaryStage.setScene(new Scene(root, 700, 400));
        Model myModel = new Model();
        controller.initialize(primaryStage, myModel);
        primaryStage.show();

    }


    public static void main(String[] args) {

      /*  File toLoadDocsEntities = new File("C:\\Users\\User\\Desktop\\Amit\\ISE\\3rdYear\\IR\\פרויקט מנוע\\tmpsave\\un_stemmed_DocsEntities");
        try {
            ObjectInputStream inDic = new ObjectInputStream(new FileInputStream(toLoadDocsEntities));

        HashMap<String, int[]> docsDictionary = null;

            HashMap<String, int[]> dictionary = ((HashMap<String, int[]>) inDic.readObject());
            System.out.println("size = "+dictionary.size());
            int sum=0;
            for (int[] arr: dictionary.values()) {
                sum+=arr[2];//size of doc
            }
            System.out.println("sum = "+sum+ "avg= "+sum/dictionary.size());


        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
*/
//        try{
//            Word2VecModel mod= Word2VecModel.fromTextFile(new File("d:\\documents\\users\\golzar\\Downloads\\w2vJAR\\word2vec.c.output.model.txt"));
//            com.medallia.word2vec.Searcher search= mod.forSearch();
//            int num=10;
//            List<com.medallia.word2vec.Searcher.Match> matches= search.getMatches("mango",num);
//            String str;
//            for (com.medallia.word2vec.Searcher.Match match: matches) {
//                str=match.match();
//                System.out.println(str);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        launch(args);

    }
}
