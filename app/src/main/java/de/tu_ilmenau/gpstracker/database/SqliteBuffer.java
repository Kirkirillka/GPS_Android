package de.tu_ilmenau.gpstracker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class SqliteBuffer extends SQLiteOpenHelper {

    private Logger LOG = LoggerFactory.getLogger(SQLiteOpenHelper.class);

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "notes_db";


    public SqliteBuffer(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        // create notes table
        db.execSQL(BufferValue.CREATE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + BufferValue.TABLE_NAME);

        // Create tables again
        onCreate(db);
    }


    public long insertValue(String value) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        // `id` and `timestamp` will be inserted automatically.
        // no need to add them
        values.put(BufferValue.COLUMN_NOTE, value);

        // insert row
        long id = db.insert(BufferValue.TABLE_NAME, null, values);

        // close db connection
        db.close();

        // return newly inserted row id
        LOG.info(String.format("inserted to db: %s", value));
        return id;
    }

    public void deleteNote(BufferValue note) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(BufferValue.TABLE_NAME, BufferValue.COLUMN_ID + " = ?",
                new String[]{String.valueOf(note.getId())});
        db.close();
    }

    public void delete(List<Integer> ids) {
        String[] buffer = new String[100];
        int bufferSize = 0;
        for (Integer id : ids) {
            buffer[bufferSize] = String.valueOf(id);
            bufferSize++;
            if (bufferSize == 100) {
                deleteChunk(buffer);
                bufferSize = 0;
                buffer = new String[100];
            }

        }
    }

    public void deleteChunk(String[] ids) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(BufferValue.TABLE_NAME, BufferValue.COLUMN_ID + " = ?", ids);
        db.close();
        String val = "";
        for (String id: ids) {
            val += id + ", ";
        }
        LOG.info( String.format("deleted ids: %s", val));
    }

    public List<BufferValue> getAll() {
        List<BufferValue> notes = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + BufferValue.TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                BufferValue note = new BufferValue();
                note.setId(cursor.getInt(cursor.getColumnIndex(BufferValue.COLUMN_ID)));
                note.setValue(cursor.getString(cursor.getColumnIndex(BufferValue.COLUMN_NOTE)));
                notes.add(note);
                LOG.info( String.format("readed from db: %s", note.getValue()));
            } while (cursor.moveToNext());
        }

        db.close();

        return notes;
    }

    public int getCount() {
        String countQuery = "SELECT  * FROM " + BufferValue.TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();


        // return count
        return count;
    }
}
