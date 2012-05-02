package net.tarilabs.abcscannotes;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class ABCScanNotesActivity extends ListActivity {
	private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;
    private static final int ACTIVITY_PREF=2;

    private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;
    private static final int DELETE_ALL_ID = Menu.FIRST + 2;
    private static final int PREF_ID = Menu.FIRST + 3;
	private static final int SAVETOFILE_ID = Menu.FIRST + 4;

    private ABCScanNotesDbAdapter mDbHelper;

    
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notes_list);
        mDbHelper = new ABCScanNotesDbAdapter(this);
        mDbHelper.open();
        fillData();
        registerForContextMenu(getListView());
    }

    private void fillData() {
    	
        
    	
        // Get all of the rows from the database and create the item list
    	Cursor notesCursor = mDbHelper.fetchAllNotes();
    	startManagingCursor(notesCursor);

        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{ABCScanNotesDbAdapter.KEY_ROWID, ABCScanNotesDbAdapter.KEY_SLOTA, ABCScanNotesDbAdapter.KEY_SLOTB};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.text0, R.id.text1, R.id.text2};

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter notes = 
            new SimpleCursorAdapter(this, R.layout.notes_row, notesCursor, from, to);
        setListAdapter(notes);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, INSERT_ID, 0, "New ABCNote");
        menu.add(0, DELETE_ALL_ID, 0, "Delete ALL");
        menu.add(0, SAVETOFILE_ID, 0, "Save to File");
        menu.add(0, PREF_ID, 0, "Settings");
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) {
            case INSERT_ID:
                createNote();
                return true;
            case DELETE_ALL_ID:
            	AlertDialog.Builder builder = new AlertDialog.Builder(this);
            	builder.setMessage("Are you sure you want to erase all the notes?")
            		   .setCancelable(false)
            	       .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            	           public void onClick(DialogInterface dialog, int id) {
            	        	   mDbHelper.deleteAllNotes();
            	        	   fillData();
            	           }
            	       })
            	       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            	           public void onClick(DialogInterface dialog, int id) {
            	               // Handle Cancel
            	           }
            	       })
            	       .create().show();
            	return true;
            case SAVETOFILE_ID:
            	final ProgressDialog dialog = ProgressDialog.show(this, "Save to File", "Saving data to File...", true);
            	final Handler handler = new Handler() {
            		public void handleMessage(Message msg) {
            			dialog.dismiss();
            		}
            	};
            	Thread checkUpdate = new Thread() {  
            		public void run()  {
            			final File path = new File( Environment.getExternalStorageDirectory(), getApplicationContext().getPackageName() );
            			  if(!path.exists()){
            			    path.mkdir();
            			  }
            			File exportFile = new File(path, "ABCScanNotesExport.txt");
            			FileWriter f;
            			try {
            				f = new FileWriter(exportFile);
            				f.write("This file is the export of ABCScanNotes\nCOLUMNS\n");
            				Cursor notesCursor = mDbHelper.fetchAllNotes();
            		    	notesCursor.moveToFirst();
            		    	String[] colNames = notesCursor.getColumnNames();
            		    	for (String a : colNames) {
            		    		f.append(a+"\t");
            		    	}
            		    	f.append("\nDATA RECORDS\n");
            		    	while (notesCursor.isAfterLast() == false) {
            		    		for (int i = 0; i < notesCursor.getColumnCount(); i++) {
            		    			f.append(notesCursor.getString(i)+"\t");
            		    		}
            		    		f.append("\n");
            		    		notesCursor.moveToNext();
            		    	}
            		    	notesCursor.close();
            				f.append("End of file export.");
            				f.flush();
            				f.close();
            			} catch (IOException e) {
            				// TODO Auto-generated catch block
            				e.printStackTrace();
            			}

            			handler.sendEmptyMessage(0);
            		}
            	};
            	checkUpdate.start();

            	return true;
            case PREF_ID:
            	Intent i = new Intent(this, ABCPreferences.class);
                startActivityForResult(i, ACTIVITY_PREF);
            	return true;
        }

        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, "Delete ABCNote");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case DELETE_ID:
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                mDbHelper.deleteNote(info.id);
                fillData();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void createNote() {
        Intent i = new Intent(this, ABCNoteEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Intent i = new Intent(this, ABCNoteEdit.class);
        i.putExtra(ABCScanNotesDbAdapter.KEY_ROWID, id);

        startActivityForResult(i, ACTIVITY_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
        if (resultCode == ABCNoteEdit.RESULT_SAVENEXT) {
        	createNote();
        }
        Log.i("main", "onActivityResult ended.");
    }
}