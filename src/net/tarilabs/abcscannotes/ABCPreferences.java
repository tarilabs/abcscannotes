package net.tarilabs.abcscannotes;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;

/**
 * An Activity to manage the user Preferences.
 * 
 * @author mmortari
 *
 */
public class ABCPreferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    addPreferencesFromResource(R.xml.preferences);
        setTitle("ABCNotes preferences");
        PreferenceManager.setDefaultValues(ABCPreferences.this, R.xml.preferences, false);

        for(int i=0;i<getPreferenceScreen().getPreferenceCount();i++){
         initSummary(getPreferenceScreen().getPreference(i));
        }

	}
	
	@Override
	protected void onResume() {
	    super.onResume();
	    getPreferenceScreen().getSharedPreferences()
	            .registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
	    super.onPause();
	    getPreferenceScreen().getSharedPreferences()
	            .unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) { 
        updatePrefSummary(findPreference(key));
    }

    /**
     * Cycle on the Preference passed as a parameter to update the Preference Summary on the Preferences included,
     * ignoring PreferenceCategory while cycling
     * 
     * @param p the Preference to cycle
     */
    private void initSummary(Preference p){
        if (p instanceof PreferenceCategory){
             PreferenceCategory pCat = (PreferenceCategory)p;
             for(int i=0;i<pCat.getPreferenceCount();i++){
                 initSummary(pCat.getPreference(i));
             }
         }else{
             updatePrefSummary(p);
         }

     }

    /**
     * If Preference is a ListPreference or a EditTextPreference, will update the Summary of such Preference with the
     * user input.
     * 
     * @param p the Preference to potentially update its Summary
     */
    private void updatePrefSummary(Preference p){
         if (p instanceof ListPreference) {
             ListPreference listPref = (ListPreference) p; 
             p.setSummary(listPref.getEntry()); 
         }
         if (p instanceof EditTextPreference) {
             EditTextPreference editTextPref = (EditTextPreference) p; 
             p.setSummary(editTextPref.getText()); 
         }

     }


}
