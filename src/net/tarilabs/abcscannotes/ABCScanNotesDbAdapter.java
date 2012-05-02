package net.tarilabs.abcscannotes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ABCScanNotesDbAdapter {

    public static final String KEY_SLOTA = "slotA";
    public static final String KEY_SLOTB = "slotB";
    public static final String KEY_SLOTC = "slotC";
    public static final String KEY_SLOTD = "slotD";
    public static final String KEY_SLOTE = "slotE";
    public static final String KEY_SLOTF = "slotF";
    public static final String KEY_NOTES = "notes";
    public static final String KEY_ROWID = "_id";

    private static final String DATABASE_CREATE =
        "create table notes (_id integer primary key autoincrement, "
        + "slotA text not null, " +
        "slotB text not null, " +
        "slotC text not null, " +
        "slotD text not null, " +
        "slotE text not null, " +
        "slotF text not null, " +
        "notes text not null);";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "notes";
    private static final int DATABASE_VERSION = 1;
    private static final String TAG = "ABCScanNotesDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    
    private final Context mCtx;
    
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS "+DATABASE_TABLE);
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public ABCScanNotesDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public ABCScanNotesDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new note using the title and body provided. If the note is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     * 
     * @return rowId or -1 if failed
     */
    public long createNote(String slotA, String slotB, String slotC, String slotD, String slotE, String slotF, String notes) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_SLOTA, slotA==null?"EMPTY SLOT A":slotA);
        initialValues.put(KEY_SLOTB, slotB==null?"":slotB);
        initialValues.put(KEY_SLOTC, slotC==null?"":slotC);
        initialValues.put(KEY_SLOTD, slotD==null?"":slotD);
        initialValues.put(KEY_SLOTE, slotE==null?"":slotE);
        initialValues.put(KEY_SLOTF, slotF==null?"":slotF);
        initialValues.put(KEY_NOTES, notes==null?"":notes);
        Log.i(TAG, "create Note a:"+slotA);
        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the note with the given rowId
     * 
     * @param rowId id of note to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteNote(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }
    
    public boolean deleteAllNotes() {
    	return mDb.delete(DATABASE_TABLE, null, null) > 0;
    }

    /**
     * Return a Cursor over the list of all notes in the database
     * 
     * @return Cursor over all notes
     */
    public Cursor fetchAllNotes() {
    	
    	Cursor results = mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID,
        		KEY_SLOTA, KEY_SLOTB, KEY_SLOTC, KEY_SLOTD, KEY_SLOTE, KEY_SLOTF,
                KEY_NOTES}, null, null, null, null, null);
    	Log.i(TAG, "fetchAllNotes "+results.getCount());
    	return results;
    }

    /**
     * Return a Cursor positioned at the note that matches the given rowId
     * 
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchNote(long rowId) throws SQLException {
    	Log.i(TAG, "fetchNote "+rowId);
        Cursor mCursor =

            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
            		KEY_SLOTA, KEY_SLOTB, KEY_SLOTC, KEY_SLOTD, KEY_SLOTE, KEY_SLOTF,
                    KEY_NOTES}, KEY_ROWID + "=" + rowId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Update the note using the details provided. The note to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     * 
     * @param rowId id of note to update
     * @param title value to set note title to
     * @param body value to set note body to
     * @return true if the note was successfully updated, false otherwise
     */
    public boolean updateNote(long rowId, String slotA, String slotB, String slotC, String slotD, String slotE, String slotF, String notes) {
        ContentValues args = new ContentValues();
        args.put(KEY_SLOTA, slotA==null?"EMPTY SLOT A":slotA);
        args.put(KEY_SLOTB, slotB==null?"":slotB);
        args.put(KEY_SLOTC, slotC==null?"":slotC);
        args.put(KEY_SLOTD, slotD==null?"":slotD);
        args.put(KEY_SLOTE, slotE==null?"":slotE);
        args.put(KEY_SLOTF, slotF==null?"":slotF);
        args.put(KEY_NOTES, notes==null?"":notes);
        Log.i(TAG, "Update note "+rowId);
        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
