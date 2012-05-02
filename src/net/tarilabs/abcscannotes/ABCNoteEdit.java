package net.tarilabs.abcscannotes;

import java.io.File;
import java.io.FilenameFilter;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ABCNoteEdit extends Activity {
	
	public static final int RESULT_SAVENEXT = 47;

    private static final int SCAN_SLOTA = 0;
    private static final int SCAN_SLOTB = SCAN_SLOTA+1;
    private static final int SCAN_SLOTC = SCAN_SLOTA+2;
    private static final int SCAN_SLOTD = SCAN_SLOTA+3;
    private static final int SCAN_SLOTE = SCAN_SLOTA+4;
    private static final int SCAN_SLOTF = SCAN_SLOTA+5;
    private static final int TAKE_PHOTO_CODE = SCAN_SLOTA+6;
	
	private EditText mTextA;
    private EditText mTextB;
    private EditText mTextC;
	private EditText mTextD;
    private EditText mTextE;
    private EditText mTextF;
    private EditText mTextNotes;
    private TextView mTextViewStatus;

    private int scannedSlot;
    private String scannedResult;
    
    private Long mRowId;
    
    private ABCScanNotesDbAdapter mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mDbHelper = new ABCScanNotesDbAdapter(this);

        mDbHelper.open();
        
        PreferenceManager.setDefaultValues(getApplicationContext(), R.xml.preferences, false);
        SharedPreferences sp=PreferenceManager.
                        getDefaultSharedPreferences(getApplicationContext());
        
        
        setContentView(R.layout.note_edit);
        setTitle("Edit ABCNote");

        mTextA = (EditText) findViewById(R.id.editTextA);
        mTextA.setHint(sp.getString("slotAhint",null));
        if ( sp.getString("slotAkeyboard",null).equals("Num. dialpad keyboard") ) {
        	mTextA.setKeyListener(new CustomDigitsKeyListener());
        }
        mTextB = (EditText) findViewById(R.id.editTextB);
        mTextB.setHint(sp.getString("slotBhint",null));
        if ( sp.getString("slotBkeyboard",null).equals("Num. dialpad keyboard") ) {
        	mTextB.setKeyListener(new CustomDigitsKeyListener());
        }
        mTextC = (EditText) findViewById(R.id.editTextC);
        mTextC.setHint(sp.getString("slotChint",null));
        if ( sp.getString("slotCkeyboard",null).equals("Num. dialpad keyboard") ) {
        	mTextC.setKeyListener(new CustomDigitsKeyListener());
        }
        mTextD = (EditText) findViewById(R.id.editTextD);
        mTextD.setHint(sp.getString("slotDhint",null));
        if ( sp.getString("slotDkeyboard",null).equals("Num. dialpad keyboard") ) {
        	mTextD.setKeyListener(new CustomDigitsKeyListener());
        }
        mTextE = (EditText) findViewById(R.id.editTextE);
        mTextE.setHint(sp.getString("slotEhint",null));
        if ( sp.getString("slotEkeyboard",null).equals("Num. dialpad keyboard") ) {
        	mTextE.setKeyListener(new CustomDigitsKeyListener());
        }
        mTextF = (EditText) findViewById(R.id.editTextF);
        mTextF.setHint(sp.getString("slotFhint",null));
        if ( sp.getString("slotFkeyboard",null).equals("Num. dialpad keyboard") ) {
        	mTextF.setKeyListener(new CustomDigitsKeyListener());
        }
        
        mTextNotes = (EditText) findViewById(R.id.editTextNotes);

        

        mRowId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(ABCScanNotesDbAdapter.KEY_ROWID);
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = extras != null ? extras.getLong(ABCScanNotesDbAdapter.KEY_ROWID)
                                    : null;
        }

        populateFields();
        
        Button confirmButton = (Button) findViewById(R.id.btnSave);
        confirmButton.setOnClickListener(new View.OnClickListener() {

        	public void onClick(View view) {
        	    setResult(RESULT_OK);
        	    finish();
        	}

        });
        

        
        Button saveNextButton = (Button) findViewById(R.id.btnSaveNext);
        saveNextButton.setOnClickListener(new View.OnClickListener() {

        	public void onClick(View view) {
        	    setResult(RESULT_SAVENEXT);
        	    finish();
        	}

        });
        
        ((Button)findViewById(R.id.addPhoto)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile( getPhotoFile()) );
				intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY , 0);
				startActivityForResult(intent, TAKE_PHOTO_CODE);
			}
		});
        ((Button)findViewById(R.id.deletePhotos)).setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		// TODO possibly put a spinner to not hold GUI on deletion.
        		if (mRowId != null) {
        			File[] counted = getPhotoFiles();
        			for (File asd : counted) {
        				asd.delete();
        			}
        			counted = getPhotoFiles();
    				if (counted != null ) {
    					mTextViewStatus.setText("Counted "+counted.length+" photos.");
    				} else {
    					mTextViewStatus.setText("Couldn't find any related photo.");
    				}
        		}
        	}
        });
        
        mTextViewStatus = (TextView) findViewById(R.id.textViewStatus);
        mTextViewStatus.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				// TODO possibly put a spinner to not hold GUI on deletion.
				File[] counted = getPhotoFiles();
				if (counted != null ) {
					mTextViewStatus.setText("Counted "+counted.length+" photos.");
				} else {
					mTextViewStatus.setText("Couldn't find any related photo.");
				}
			}
        });
        
        ((Button)findViewById(R.id.buttonScanA)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent intent = new Intent(
						"com.google.zxing.client.android.SCAN");
				intent.putExtra(
						"com.google.zxing.client.android.SCAN.SCAN_MODE",
						"ONE_D_MODE");
				startActivityForResult(intent, SCAN_SLOTA);
			}
		});
        ((Button)findViewById(R.id.buttonScanB)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent intent = new Intent(
						"com.google.zxing.client.android.SCAN");
				intent.putExtra(
						"com.google.zxing.client.android.SCAN.SCAN_MODE",
						"ONE_D_MODE");
				startActivityForResult(intent, SCAN_SLOTB);
			}
		});
        ((Button)findViewById(R.id.buttonScanC)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent intent = new Intent(
						"com.google.zxing.client.android.SCAN");
				intent.putExtra(
						"com.google.zxing.client.android.SCAN.SCAN_MODE",
						"ONE_D_MODE");
				startActivityForResult(intent, SCAN_SLOTC);
			}
		});
        ((Button)findViewById(R.id.buttonScanD)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent intent = new Intent(
						"com.google.zxing.client.android.SCAN");
				intent.putExtra(
						"com.google.zxing.client.android.SCAN.SCAN_MODE",
						"ONE_D_MODE");
				startActivityForResult(intent, SCAN_SLOTD);
			}
		});
        ((Button)findViewById(R.id.buttonScanE)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent intent = new Intent(
						"com.google.zxing.client.android.SCAN");
				intent.putExtra(
						"com.google.zxing.client.android.SCAN.SCAN_MODE",
						"ONE_D_MODE");
				startActivityForResult(intent, SCAN_SLOTE);
			}
		});
        ((Button)findViewById(R.id.buttonScanF)).setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent intent = new Intent(
						"com.google.zxing.client.android.SCAN");
				intent.putExtra(
						"com.google.zxing.client.android.SCAN.SCAN_MODE",
						"ONE_D_MODE");
				startActivityForResult(intent, SCAN_SLOTF);
			}
		});
        
    }
    
	private File getPhotoFile() {
		final File path = new File(Environment.getExternalStorageDirectory(),
				getApplicationContext().getPackageName());
		if (!path.exists()) {
			path.mkdir();
		}
		saveState(null, null, null, null, null, null);
		int i = 0;
		File output = null;
		do {
			output = new File(path, "ABCScanNotes-ID" + mRowId + "-PHOTO" + i
					+ ".jpg");
			i++;
		} while (output.exists() && i < 100);
		Log.i("getPhotoFile", "output file will be: "+output.toString());
		return output;
	}
	
	/**
	 * 
	 * @return or null if no files found.
	 */
	private File[] getPhotoFiles() {
		final File path = new File(Environment.getExternalStorageDirectory(),
				getApplicationContext().getPackageName());
		if (mRowId != null) {
			SimpleFileListFilter sFilter = new SimpleFileListFilter("ABCScanNotes-ID"+mRowId, "jpg");
			return path.listFiles(sFilter);
		} else {
			return null;	
		}
	}
	
	
    
    private void populateFields() {
    	Log.i("edit", "edit: "+mRowId);
        if (mRowId != null) {
            Cursor note = mDbHelper.fetchNote(mRowId);
            startManagingCursor(note);
            mTextA.setText(note.getString(note.getColumnIndexOrThrow(ABCScanNotesDbAdapter.KEY_SLOTA)));
            mTextB.setText(note.getString(note.getColumnIndexOrThrow(ABCScanNotesDbAdapter.KEY_SLOTB)));
            mTextC.setText(note.getString(note.getColumnIndexOrThrow(ABCScanNotesDbAdapter.KEY_SLOTC)));
            mTextD.setText(note.getString(note.getColumnIndexOrThrow(ABCScanNotesDbAdapter.KEY_SLOTD)));
            mTextE.setText(note.getString(note.getColumnIndexOrThrow(ABCScanNotesDbAdapter.KEY_SLOTE)));
            mTextF.setText(note.getString(note.getColumnIndexOrThrow(ABCScanNotesDbAdapter.KEY_SLOTF)));
            mTextNotes.setText(note.getString(note.getColumnIndexOrThrow(ABCScanNotesDbAdapter.KEY_NOTES)));
            
        }
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState(null, null, null, null, null, null);
        outState.putSerializable(ABCScanNotesDbAdapter.KEY_ROWID, mRowId);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        saveState(null, null, null, null, null, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }
    
    private void saveState(String ovrA, String ovrB, String ovrC, String ovrD, String ovrE, String ovrF) {
        String textA = ovrA==null?mTextA.getText().toString():ovrA;
        String textB = ovrB==null?mTextB.getText().toString():ovrB;
        String textC = ovrC==null?mTextC.getText().toString():ovrC;
        String textD = ovrD==null?mTextD.getText().toString():ovrD;
        String textE = ovrE==null?mTextE.getText().toString():ovrE;
        String textF = ovrF==null?mTextF.getText().toString():ovrF;
        String textNotes = mTextNotes.getText().toString();
        Log.i("edit", "saveState: "+textA);
        if (mRowId == null) {
            long id = mDbHelper.createNote(textA, textB, textC, textD, textE, textF, textNotes);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            mDbHelper.updateNote(mRowId, textA, textB, textC, textD, textE, textF, textNotes);
        }
    }
  
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == SCAN_SLOTA) {
			if (resultCode == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");
				//String format = intent.getStringExtra("SCAN_RESULT_FORMAT");
				saveState(contents, null, null, null, null, null);
			} else if (resultCode == RESULT_CANCELED) {
				// Handle cancel
			}
		}
		if (requestCode == SCAN_SLOTB) {
			if (resultCode == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");
				saveState(null, contents, null, null, null, null);
			}
		}
		if (requestCode == SCAN_SLOTC) {
			if (resultCode == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");
				saveState(null, null, contents, null, null, null);
			}
		} 
		if (requestCode == SCAN_SLOTD) {
			if (resultCode == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");
				saveState(null, null, null, contents, null, null);
			}
		} 
		if (requestCode == SCAN_SLOTE) {
			if (resultCode == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");
				saveState(null, null, null, null, contents, null);
			}
		} 
		if (requestCode == SCAN_SLOTF) {
			if (resultCode == RESULT_OK) {
				String contents = intent.getStringExtra("SCAN_RESULT");
				saveState(null, null, null, null, null, contents);
			}
		} 
	}
}
class SimpleFileListFilter implements FilenameFilter {
	private String name; 
	private String extension; 

	public SimpleFileListFilter(String name, String extension) {
		this.name = name;
		this.extension = extension;
	}

	public boolean accept(File directory, String filename) {
		boolean fileOK = true;
		if (name != null) {
			fileOK &= filename.startsWith(name);
		}
		if (extension != null) {
			fileOK &= filename.endsWith('.' + extension);
		}
		Log.d("SimpleFileListFilter", filename+" for "+name+" is "+fileOK);
		return fileOK;
	}
}