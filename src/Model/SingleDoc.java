package Model;

public class SingleDoc {

    //region Fields
    public String docID;
    public String date;
    public String title;
    public String text;
    int max_tf;
    int size;
    //endregion

    /**
     * Constructor
     *
     * @param docID
     * @param date
     * @param title
     * @param text
     */
    public SingleDoc(String docID, String date, String title, String text) {
        this.docID = docID;
        this.date = date;
        this.title = title;
        this.text = text;
        size = text.length();
        max_tf = 1;
    }

}
