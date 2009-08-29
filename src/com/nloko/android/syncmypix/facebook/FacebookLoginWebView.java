//
//    FacebookLoginWebView.java is part of SyncMyPix
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

package com.nloko.android.syncmypix.facebook;

import java.net.URL;
import java.net.URLDecoder;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.nloko.android.Log;
import com.nloko.android.Utils;
import com.nloko.android.syncmypix.GlobalConfig;
import com.nloko.android.syncmypix.R;
import com.nloko.android.syncmypix.R.id;
import com.nloko.android.syncmypix.R.layout;
import com.nloko.android.syncmypix.R.string;
import com.nloko.simplyfacebook.net.login.FacebookLogin;

public class FacebookLoginWebView extends Activity {

	private final String TAG = "FacebookLoginWebView";
	
	WebView webview;
	final FacebookLogin login = new FacebookLogin ();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.facebookloginwebview);	
        
        // Login page can take awhile to load
        showDialog(PROGRESS_KEY);

        // Mobile page returns an auth_token. WTF?
        /*try {
        	login.setUrl("https://m.facebook.com/login.php");
        }
        catch (Exception e) {}*/
        
        login.setAPIKey(GlobalConfig.API_KEY);
        
        webview = (WebView) findViewById(R.id.webview);
        webview.setWebViewClient(new FacebookWebViewClient ());
        webview.getSettings().setJavaScriptEnabled(true);
                
        Log.d(TAG, login.getFullLoginUrl());
        webview.loadUrl(login.getFullLoginUrl());
    }
    
    private class FacebookWebViewClient extends WebViewClient {
    	
    	
        @Override
		public void onLoadResource(WebView view, String url) {
	
			super.onLoadResource(view, url);
			
			showDialog(PROGRESS_KEY);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			
			if (progress != null) {
				dismissDialog(PROGRESS_KEY);
			}
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			// TODO Auto-generated method stub
			super.onPageStarted(view, url, favicon);
			
			if (progress != null) {
				dismissDialog(PROGRESS_KEY);
			}
			
			try	{
            	Log.d(TAG, url);
            	Log.d(TAG, login.getNextUrl().getPath());
            	
                URL page = new URL(URLDecoder.decode(url).trim());

                if (page.getPath().equals(login.getNextUrl().getPath())) {
                	login.setResponseFromExternalBrowser(page);
                    Toast.makeText(getBaseContext(), "Thank you for logging in", Toast.LENGTH_LONG).show();
                    
                    if (login.isLoggedIn()) {
                    	Utils.setString(getSharedPreferences(GlobalConfig.PREFS_NAME, 0), "session_key", login.getSessionKey());
                    	Utils.setString(getSharedPreferences(GlobalConfig.PREFS_NAME, 0), "secret", login.getSecret());
                    	Utils.setString(getSharedPreferences(GlobalConfig.PREFS_NAME, 0), "uid", login.getUid());
                    }
                    
                    finish();
                }
                else if (page.getPath().equals(login.getCancelUrl().getPath())) {
                	finish();
                }
            } 
            catch (Exception ex) {
                Toast.makeText(getBaseContext(), R.string.facebooklogin_urlError, Toast.LENGTH_LONG).show();
                android.util.Log.getStackTraceString(ex);
            }
        }
    }
    
	private final int PROGRESS_KEY = 0;
	private ProgressDialog progress;
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch(id) {
			case PROGRESS_KEY:
				progress = new ProgressDialog(FacebookLoginWebView.this);
				progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progress.setMessage("Please wait while login page loads...");
				progress.setCancelable(true);
				return progress;
		}
		
		return null;
	}
}
