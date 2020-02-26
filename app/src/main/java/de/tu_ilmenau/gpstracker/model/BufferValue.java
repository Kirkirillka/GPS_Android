package de.tu_ilmenau.gpstracker.model;

/**
 * This class contains data to retrieve and load to Sqlite database
 */
public class BufferValue {
    public static final String TABLE_NAME = "buffer";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NOTE = "value";
    private int id;
    private String value;

    // Create table SQL query
    public static final String CREATE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "("
                    + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_NOTE + " TEXT"
                    + ")";

    public BufferValue() {
    }

    public BufferValue(int id, String note) {
        this.id = id;
        this.value = note;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
