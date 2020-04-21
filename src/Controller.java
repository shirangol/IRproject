import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.*;
import Model.Model;
import javafx.util.Callback;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static javafx.scene.control.TableColumn.SortType.DESCENDING;

public class Controller {

    //region Fields
    public Stage mainStage;
    public File browseDirPostingsDic, queriesFile;
    public javafx.scene.control.TextField corpusStopWordsPath;
    public javafx.scene.control.TextField postingsDicPath;
    public javafx.scene.control.TextField queryTextFiled;
    public String CorpusStopWordsPath;
    public String postingsDicStr;
    public Model myModel = new Model();
    private String browseDirResults;
    private boolean loadDictionary;
    //endregion

    //region Initialization

    /**
     * Initiate a new stage and scene. Tnstead of constructor
     *
     * @param primaryStage
     * @param myModel
     */
    public void initialize(Stage primaryStage, Model myModel) {
        mainStage = primaryStage;
        this.myModel = myModel;
        Scene scene = mainStage.getScene();
        scene.getStylesheets().add(getClass().getResource("Style2.css").toExternalForm());
        mainStage.setScene(scene);
    }
    //endregion

    //region Indexing Buttons Methods

    /**
     * Opens an already built dictionary or builds a new one.
     *
     * @param actionEvent
     */
    public void startButton(ActionEvent actionEvent) {

        //region start Button Validation
        //Alerts in case text fields are empty and no item was browsed
        if ((corpusStopWordsPath.getText().trim().isEmpty() && CorpusStopWordsPath == null) || (postingsDicPath.getText().trim().isEmpty() && browseDirPostingsDic == null)) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Please insert posting files or corpus and Stop-words path, \n or browse a file and a directory.");
            alert.setTitle("Invalid path!");
            alert.setHeaderText("No path was inserted and no item was browsed!");
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.show();
            return;
        }
        //endregion//


        //Build a new dictionary out of given path/directory
        if (CorpusStopWordsPath == null) {
            CorpusStopWordsPath = corpusStopWordsPath.getText();
        }

        // Saving directory:
        //get the dictionary's path from browse button or textfield
        if (browseDirPostingsDic == null)
            postingsDicStr = postingsDicPath.getText();
        else postingsDicStr = browseDirPostingsDic.getPath();

        // Set saving directory of dictionary and posting files
        myModel.setDirPostingsDic(postingsDicStr);

        // Measure generating dictionary runtime
        long start = System.currentTimeMillis();
        this.myModel.generateDictionary(CorpusStopWordsPath, postingsDicStr);
        long totalTime = System.currentTimeMillis() - start;

        // Enable display dictionary button
        loadDictionary=true;

        // Display a massage with the relevant information, after dictionary building resumed
        String info = "";
        info += "Number of Indexed Files: " + myModel.getDocsDictionarySize() + ".\n";
        info += "Number of unique terms in the corpus: " + myModel.getDictionarySize() + ".\n";
        info += "total runtime in seconds: " + TimeUnit.MILLISECONDS.toSeconds(totalTime) + ".\n";
        informationAlert(info);

    }

    /**
     * Loading the dictionary on the RAM. Based on a path that has been already given previous
     * clicking the button "load dictionary".
     *
     * @param actionEvent
     */
    public void loadDictionary(ActionEvent actionEvent) {

        if (browseDirPostingsDic == null && postingsDicPath.getText().trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Please insert posting files and Dictionary path, \n or browse a file and a directory.");
            alert.setTitle("Invalid path!");
            alert.setHeaderText("No path was inserted and no item was browsed!");
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.show();
            return;
        }


        //get the dictionary's path from browse button or textfield
        if (browseDirPostingsDic == null) postingsDicStr = postingsDicPath.getText();
        else postingsDicStr = browseDirPostingsDic.getPath();

        // Set saving directory of dictionary and posting files
        myModel.setDirPostingsDic(postingsDicStr);

        //load dictionary and display a massage accordingly
        if (myModel.loadDic()) {
            informationAlert("Dictionary was successfully loaded from: \n" + postingsDicStr + ".");
            this.loadDictionary = true;
        } else {
            informationAlert("Something went wrong. Please insert the path again");
            this.loadDictionary = false;
        }
    }

    /**
     * If stemming check-box is selected, updates all "toStem" boolean expression to True accordingly.
     * When unselected,updates all "toStem" boolean expression to False accordingly.
     *
     * @param actionEvent
     */
    public void stemmingCheckBox(ActionEvent actionEvent) {
        CheckBox chk = (CheckBox) actionEvent.getSource();
        if (chk.isSelected()) {
            myModel.toStem = true;
        }
        if (!chk.isSelected()) {
            myModel.toStem = false;
        }
    }

    /**
     * Browser for posting files and dictionary's directory
     * browseDirPostingsDic - directory, holds the path.
     *
     * @param actionEvent
     */
    public void browseDirPostingsDictionary(ActionEvent actionEvent) {
        DirectoryChooser dirChooser = new DirectoryChooser();
        String tempDirectoryPath = System.getProperty("java.io.tmpdir");
        dirChooser.setInitialDirectory(new File(tempDirectoryPath));
        browseDirPostingsDic = dirChooser.showDialog(mainStage);
    }

    /**
     * Browser for posting files and dictionary's directory
     * browseDirCorpusStopwords - directory, holds the path.
     *
     * @param actionEvent
     */
    public void BrowseDirCorpusStopwords(ActionEvent actionEvent) {
        CorpusStopWordsPath = dirBrowser();
    }

    public String dirBrowser() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        String tempDirectoryPath = System.getProperty("java.io.tmpdir");
        dirChooser.setInitialDirectory(new File(tempDirectoryPath));
        return dirChooser.showDialog(mainStage).getPath();
    }

    /**
     * Deletes all dictionary and posting files,
     * and clears all memory from Model,Indexer,Parser and ReadFile
     *
     * @param actionEvent
     */
    public void restart(ActionEvent actionEvent) {
        String postingsSTR="";
        if (!postingsDicPath.getText().isEmpty())
            postingsSTR= postingsDicPath.getText();
        else if (browseDirPostingsDic != null) {
            postingsSTR=browseDirPostingsDic.getPath();
        } else {
            informationAlert("There is nothing to delete, there are no posting files");
            return;
        }
        File dir= new File(postingsSTR);
        if(dir.isDirectory()){
            if((dir.listFiles()).length==0){
                informationAlert("There is nothing to delete, there are no posting files");
                return;
            }
        }
        myModel.clearAllData(postingsSTR);
        if (browseDirPostingsDic != null)
            if (myModel.clearAllData(browseDirPostingsDic.getPath()))
                informationAlert("all memory was cleaned");
            else informationAlert("Something went wrong. Please insert the path again");
    }

    /**
     * Displays the dictionary in a new window
     *
     * @param actionEvent
     */
    public void displayDictionary(ActionEvent actionEvent) {

        if (loadDictionary == false) {
            informationAlert("Cannot display a dictionary without loading it.\nPlease load a dictionary and try again.");
            return;
        }

        HashMap<String, String[]> dictionary = myModel.getDictionary();
        TreeMap<String, String[]> dicToDisplay = new TreeMap<>(dictionary);

        TableView<String> table = new TableView();

        // fill table with keys
        table.getItems().addAll(dicToDisplay.keySet());

        TableColumn<String, Integer> keyColumn = new TableColumn<>("Term");
        keyColumn.setCellValueFactory(cd -> new SimpleObjectProperty(cd.getValue()));

        TableColumn<String, Integer> valueColumn = new TableColumn<>("Total Frequency");
        valueColumn.setCellValueFactory(cd -> new SimpleObjectProperty<>(Integer.valueOf(dicToDisplay.get(cd.getValue())[1])));

        table.getColumns().addAll(keyColumn, valueColumn);
        //endregion

        setNewWindow("Display Dictionary", table, 700, 700);

    }
    //endregion

    //region Assistance Methods

    /**
     * The method receives a massage, and opens al alert with it.
     *
     * @param s - a massage to display in the alert.
     */
    private void informationAlert(String s) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, s);
        alert.setTitle("Information Alert");
        DialogPane dialogPane = alert.getDialogPane();
        alert.initStyle(StageStyle.DECORATED);
        alert.initModality(Modality.WINDOW_MODAL);
        alert.show();
    }
    //endregion


    //region Searching methods

    public void browseQueriesButton(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        queriesFile = fileChooser.showOpenDialog(mainStage);
    }


    /**
     * If Identify entities check-box is selected, updates "identifyEntities" boolean expression to True accordingly.
     * When unselected,updates "identifyEntities" boolean expression to False accordingly.
     *
     * @param actionEvent
     */
    public void EntitiesCheckBox(ActionEvent actionEvent) {
        CheckBox chk = (CheckBox) actionEvent.getSource();
        if (chk.isSelected()) {
            myModel.identifyEntities = true;
        }
        if (!chk.isSelected()) {
            myModel.identifyEntities = false;
        }
    }

    /**
     * If Use Semantic Model check-box is selected, updates "useSemanticModel" boolean expression to True accordingly.
     * When unselected,updates "useSemanticModel" boolean expression to False accordingly.
     *
     * @param actionEvent
     */
    public void semanticModelCheckBox(ActionEvent actionEvent) {
        CheckBox chk = (CheckBox) actionEvent.getSource();
        if (chk.isSelected()) {
            myModel.useSemanticModel = true;
        }
        if (!chk.isSelected()) {
            myModel.useSemanticModel = false;
        }
    }
    //endregion

    /**
     * Operates search button
     *
     * @param actionEvent
     */
    public void runSearchButton(ActionEvent actionEvent) {

        if (queriesFile == null && queryTextFiled.getText().trim().isEmpty()) {
            informationAlert("Please insert a query or browse a file before hitting 'RUN' button.");
            return;
        }
        if (loadDictionary == false) {
                informationAlert("Cannot search a query without a loaded dictionary.\nPlease load a dictionary and try again.");
                return;
        }
         else if (!queryTextFiled.getText().trim().isEmpty()) {
            runSearchSingleQuery();
            queryTextFiled.clear();
        } else if (queriesFile != null) {
            runSearchQueriesFile();
            queriesFile = null;
        }
    }

    private void displayResults(HashMap<String, TreeMap<Double, String>> docsRank) {  // HashMap<q_id,TreeMap<score,docID>>

        TableView<String> table = new TableView();

        // fill table with keys
        table.getItems().addAll(docsRank.keySet());

        TableColumn<String, String> query_id_Column = new TableColumn<>("Query ID");
        query_id_Column.setCellValueFactory(cd -> new SimpleObjectProperty(cd.getValue()));

        TableColumn<String, String> retrievalNum = new TableColumn<>("Amount of retrieved documents");
        retrievalNum.setCellValueFactory(cd -> new SimpleObjectProperty(String.valueOf(docsRank.get(cd.getValue()).size())));

        table.getColumns().addAll(query_id_Column, retrievalNum);
        addButtonToTable(table, docsRank);
        setNewWindow("Display Results", table, 600, 500);
    }


    private void addButtonToTable(TableView<String> table, HashMap<String, TreeMap<Double, String>> docsRank) {
        TableColumn<String, String> colBtn = new TableColumn("Button Column");
        Callback<TableColumn<String, String>, TableCell<String, String>> cellFactory = new Callback<TableColumn<String, String>, TableCell<String, String>>() {
            @Override
            public TableCell<String, String> call(final TableColumn<String, String> param) {
                final TableCell<String, String> cell = new TableCell<String, String>() {

                    private final Button btn = new Button("View DocumentID");

                    {
                        btn.setOnAction((ActionEvent event) -> {
                            String qid = getTableView().getItems().get(getIndex());
                            displayDocs(qid, docsRank);
                        });
                    }

                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }
                };
                return cell;
            }
        };
        colBtn.setCellFactory(cellFactory);

        table.getColumns().add(2, colBtn);
    }

    private void displayDocs(String qid, HashMap<String, TreeMap<Double, String>> docsRank) {
        mainStage = new Stage();

        TableView<String> table = new TableView();
        TreeMap<Double, String> qidTree = docsRank.get(qid);

        TreeMap<String, Double> copy50_qidTree = new TreeMap<>();//<Q_ID,Score>
        // Copy HashMap into a sized 50 HashMap
        int counter = 0;
        for (Map.Entry<Double, String> entry : qidTree.entrySet()) {
            if (counter == 50) break;
            copy50_qidTree.put(entry.getValue(), entry.getKey());
            counter += 1;
        }

        // fill table with keys
        table.getItems().addAll(copy50_qidTree.keySet());

        TableColumn<String, Double> qid_Col = new TableColumn<>("Query ID");
        qid_Col.setCellValueFactory(cd -> new SimpleObjectProperty(cd.getValue()));

        TableColumn<String, Double> score_Col = new TableColumn<>("Score");
        score_Col.setCellValueFactory(cd -> new SimpleObjectProperty<>(Double.valueOf(copy50_qidTree.get(cd.getValue()))));
        score_Col.setSortType(DESCENDING);

        table.getColumns().addAll(qid_Col, score_Col);
        table.getSortOrder().add(score_Col);
        addEntityButtonToTable(table, docsRank);
        setNewWindow("Documents for query ID " + qid, table, 600, 500);
    }

    private void addEntityButtonToTable(TableView<String> table, HashMap<String, TreeMap<Double, String>> docsRank) {
        TableColumn<String, String> colBtn = new TableColumn("Button Column");
        Callback<TableColumn<String, String>, TableCell<String, String>> cellFactory = new Callback<TableColumn<String, String>, TableCell<String, String>>() {
            @Override
            public TableCell<String, String> call(final TableColumn<String, String> param) {
                final TableCell<String, String> cell = new TableCell<String, String>() {

                    private final Button btn = new Button("View Entities");

                    {
                        btn.setOnAction((ActionEvent event) -> {
                            String docID = getTableView().getItems().get(getIndex());
                            displayEntitiesForDoc(docID);
                        });
                    }

                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btn);
                        }
                    }
                };
                return cell;
            }
        };
        colBtn.setCellFactory(cellFactory);
        table.getColumns().add(colBtn);
    }

    private void displayEntitiesForDoc(String docID) {
        TableView<String> table = new TableView();
        TreeMap<String, Double> topEntities = myModel.getTopEntities(docID); // TreeMap<entity, score>
        // fill table with keys

        table.getItems().addAll(topEntities.keySet());

        TableColumn<String, Double> keyColumn = new TableColumn<>("Entity");
        keyColumn.setCellValueFactory(cd -> new SimpleObjectProperty(cd.getValue()));

        TableColumn<String, Double> valueColumn = new TableColumn<>("Score");
        valueColumn.setCellValueFactory(cd -> new SimpleObjectProperty<>(Double.valueOf(topEntities.get(cd.getValue()))));

        table.getColumns().addAll(keyColumn, valueColumn);

        setNewWindow("Entities Display for Query " + docID, table, 600, 500);
    }

    void setNewWindow(String title, TableView<String> table, int height, int width) {
        mainStage = new Stage();
        // New window (Stage)
        Stage newWindow = new Stage();
        newWindow.setTitle(title);
        newWindow.setHeight(height);
        newWindow.setWidth(width);

        // Specifies the modality for new window.
        newWindow.initModality(Modality.WINDOW_MODAL);

        // Specifies the owner Window (parent) for new window
        newWindow.initOwner(mainStage);

        // Adding the scrollPane into the window, inside a StackPane container
        StackPane root = new StackPane();
        root.getChildren().add(table);

        // Set a new scene
        Scene scene = new Scene(root, width, height);
        scene.getStylesheets().add(getClass().getResource("Style2.css").toExternalForm());

        // Initiate the window and displays it
        newWindow.setTitle("Better Than Google Dictionary");
        newWindow.setScene(scene);
        newWindow.show();
    }

    /**
     * Performs a search for a single query
     */
    private void runSearchSingleQuery() {
        myModel.runSingleQuery(queryTextFiled.getText()); // queryTextFiled is not empty
        TreeMap<Double, String> rankedDocs = myModel.getDocsRank();
        HashMap<String, TreeMap<Double, String>> toDisplay = new HashMap<>();
        toDisplay.put(" ", rankedDocs);
        displayResults(toDisplay);
        if (browseDirResults != null) myModel.saveResults(toDisplay,browseDirResults);
    }

    /**
     * Performs a search for a queries file
     */
    private void runSearchQueriesFile() {
        HashMap<String, TreeMap<Double, String>> toDisplay = myModel.runQueriesFile(queriesFile);
        displayResults(toDisplay);
        if (browseDirResults != null) myModel.saveResults(toDisplay,browseDirResults);
    }



    public void browseQueryResultsPath(ActionEvent actionEvent) {
        DirectoryChooser dirChooser = new DirectoryChooser();
        String tempDirectoryPath = System.getProperty("java.io.tmpdir");
        dirChooser.setInitialDirectory(new File(tempDirectoryPath));
        browseDirResults = dirChooser.showDialog(mainStage).getPath();
    }

    public void useDataMuseAPI(ActionEvent actionEvent) {
        CheckBox chk = (CheckBox) actionEvent.getSource();
        if (chk.isSelected()) {
            myModel.useDataMuseAPI = true;
        }
        if (!chk.isSelected()) {
            myModel.useDataMuseAPI = false;
        }
    }
}


