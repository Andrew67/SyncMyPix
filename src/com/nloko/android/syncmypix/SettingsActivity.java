//
//    GlobalPreferences.java is part of SyncMyPix
//
//    Authors:
//        Neil Loknath <neil.loknath@gmail.com>
//
//    Copyright (c) 2009 Neil Loknath
//
//    SyncMyPix is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    SyncMyPix is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with SyncMyPix.  If not, see <http://www.gnu.org/licenses/>.
//

package com.nloko.android.syncmypix;

import com.nloko.android.Log;
import com.nloko.android.Utils;
import com.nloko.android.syncmypix.R;

import android.app.AlarmManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;

public class SettingsActivity extends PreferenceActivity {
	
	private static final String TAG = "GlobalConfig";
	public static final String PREFS_NAME = "SyncMyPixPrefs";
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupViews(savedInstanceState);
    }
    
    @Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setupViews(null);
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		Log.d(TAG, "FINALIZED");
	}
	
    private void setupViews(Bundle savedInstanceState)
    {
    	addPreferencesFromResource(R.layout.preferences);	
    	
    	ListPreference schedule = (ListPreference) findPreference("sched_freq");
        schedule.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {

				int position = Integer.parseInt((String)newValue);
				
				long firstTriggerTime;
				long interval;
				
				Utils.setInt(getSharedPreferences(SettingsActivity.PREFS_NAME, 0), "sched_freq", position);
				
				if ((interval = SettingsActivity.getScheduleInterval(position)) > 0) {
					firstTriggerTime = System.currentTimeMillis() + interval;
					Utils.setLong(getSharedPreferences(SettingsActivity.PREFS_NAME, 0), "sched_time", firstTriggerTime);
					SyncService.updateSchedule(SettingsActivity.this, 
							MainActivity.getSyncSource(getBaseContext()), 
							firstTriggerTime, 
							interval);
				}
				else {
					SyncService.cancelSchedule(SettingsActivity.this, MainActivity.getSyncSource(getBaseContext()));
				}
				return true;
			}
        });
        
        if (MainActivity.isLoggedInFromSyncSource(getBaseContext(), MainActivity.getSyncSource(getBaseContext()))) {
        	setLoginStatus(R.string.preferences_loggedin);
        }
    }

    public static long getScheduleInterval(int pos)
    {
    	long interval;
    	switch (pos) {
    		case 1:
				interval = AlarmManager.INTERVAL_DAY;
				break;
			case 2:
				interval = AlarmManager.INTERVAL_DAY * 7;
				break;
			case 3:
				interval = AlarmManager.INTERVAL_DAY * 30;
				break;
			default:
				interval = -1;
    	}
    	
    	return interval;
    }
    
    private void setLoginStatus(int status)
    {
   		EditTextPreference loginStatus = (EditTextPreference) findPreference("loginStatus");
   		loginStatus.setSummary(status);
    }
}