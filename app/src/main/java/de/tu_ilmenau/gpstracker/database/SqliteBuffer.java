package de.tu_ilmenau.gpstracker.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import de.tu_ilmenau.gpstracker.model.BufferValue;

/**
 * This class realize connection to local SQlite database storage as buffer
 */
public class SqliteBuffer extends SQLiteOpenHelper {

    private Logger LOG = LoggerFactory.getLogger(SQLiteOpenHelper.class);

    /* Database Version */
    private static final int DATABASE_VERSION = 1;

    /* Database Name */
    private static final String DATABASE_NAME = "notes_db";


    public SqliteBuffer(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(BufferValue.CREATE_TABLE);
    }

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
        final String[] buffer = new String[ids.size()];
        int i = 0;
        for (Integer id : ids) {
            buffer[i] = String.valueOf(id);
            i++;
        }
        deleteChunk(buffer);
    }

    public void deleteChunk(String[] ids) {
        SQLiteDatabase db = this.getWritableDatabase();
        String args = TextUtils.join(", ", ids);
        db.execSQL(String.format("DELETE FROM %s WHERE %s IN (%s);", BufferValue.TABLE_NAME, BufferValue.COLUMN_ID, args));
        db.close();
        LOG.info(String.format("deleted ids: %s", args));
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
                LOG.info(String.format("readed from db: %s", note.getValue()));
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
        return count;
    }
}
