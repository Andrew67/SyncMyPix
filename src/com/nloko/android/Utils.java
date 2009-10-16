//
//  Utils.java
//
//  Authors:
// 		Neil Loknath <neil.loknath@gmail.com>
//
//  Copyright 2009 Neil Loknath
//
//  Licensed under the Apache License, Version 2.0 (the "License"); 
//  you may not use this file except in compliance with the License. 
//  You may obtain a copy of the License at 
//
//  http://www.apache.org/licenses/LICENSE-2.0 
//
//  Unless required by applicable law or agreed to in writing, software 
//  distributed under the License is distributed on an "AS IS" BASIS, 
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
//  See the License for the specific language governing permissions and 
//  limitations under the License. 
//

package com.nloko.android;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.nloko.android.Log;

public final class Utils {

	private Utils() {}
	
	public static String join(String[] array, char separator)
	{
		if (array == null) {
			return null;
		}
		
		StringBuffer sb = new StringBuffer();
		
		for(int i = 0; i < array.length; i++) {
			sb.append(array[i]);
			sb.append(separator);
		}
		
		return sb.toString();
	}
	
	public static boolean hasInternetConnection(Context context)
	{
		if (context == null) {
			throw new IllegalArgumentException("context");
		}
		
		ConnectivityManager connMgr=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info=connMgr.getActiveNetworkInfo();

		return(info!=null && info.isConnected()); 
	}
	
    public static String getMd5Hash(byte[] input) 
    {
    	  if (input == null) {
    		  throw new IllegalArgumentException("input");
    	  }
    	  
          try {
              MessageDigest md = MessageDigest.getInstance("MD5");
              byte[] messageDigest = md.digest(input);
              BigInteger number = new BigInteger(1,messageDigest);
              String md5 = number.toString(16);

              while (md5.length() < 32) {
                  md5 = "0" + md5;
              }
              
              return md5;
          } 
          catch(NoSuchAlgorithmException e) {
        	  Log.e("MD5", e.getMessage());
              return null;
          }
    }
      
	public static String buildNameSelection (String field, String firstName, String lastName)
	{
		if (field == null) {
			throw new IllegalArgumentException ("field");
		}
		
		if (firstName == null) {
			throw new IllegalArgumentException ("firstName");
		}
		
		if (lastName == null) {
			throw new IllegalArgumentException ("lastName");
		}
		
		// escape single quotes
		firstName = firstName.replace("'", "''");
		lastName = lastName.replace("'", "''");
		
		StringBuilder sb = new StringBuilder();
		sb.append(String.format("%s = '%s %s' OR ", field, firstName, lastName));
		sb.append(String.format("%s = '%s, %s' OR ", field, lastName, firstName));
		sb.append(String.format("%s = '%s,%s'", field, lastName, firstName));
		
		return sb.toString();
	}
	
	public static byte[] getByteArrayFromInputStream(InputStream is)
	{
		if (is == null) {
			throw new IllegalArgumentException("is");
		}
		
		int size = 8192;
		int read = 0;
		ByteArrayOutputStream bytes = new ByteArrayOutputStream(size);
		byte[] buffer = new byte[size];
		
		try {
			while ((read = is.read(buffer, 0, buffer.length)) > 0) {
				bytes.write(buffer, 0, read);
			}
		}
		catch (IOException ex) {
			return null;
		}
		
		return bytes.toByteArray();
	}

	public static Bitmap centerCrop (Bitmap bitmap, int destHeight, int destWidth)
	{
		Bitmap resized;
		if (bitmap.getHeight() > bitmap.getWidth()) {
			resized = resize(bitmap, 0, destWidth);
		}
		else {
			resized = resize(bitmap, destHeight, 0);
		}
		
		return crop(resized, destWidth, destWidth);
	}
	
	public static Bitmap crop(Bitmap bitmapToCrop, int destHeight, int destWidth)
	{
		Bitmap b = Bitmap.createBitmap(destHeight, destWidth, Bitmap.Config.RGB_565);
        Canvas c1 = new Canvas(b);

        int width = bitmapToCrop.getWidth();
        int height = bitmapToCrop.getHeight();
        if (width <= destWidth && height <= destHeight) {
        	return bitmapToCrop;
        }
        
        int midpointX =  width / 2;
        int midpointY =  height / 2;
        
        Rect r = new Rect(midpointX - destWidth / 2, 
        		midpointY - destHeight / 2,
        		midpointX + destWidth / 2, 
        		midpointY + destHeight / 2);
        
        int left = 0; //(width / 2) - (bitmapToCrop.getWidth() / 2);
        int top = 0; //(height / 2) - (bitmapToCrop.getWidth() / 2);
        c1.drawBitmap(bitmapToCrop, r, new Rect(left, top, left
                + destWidth, top + destHeight), null);

        return b;
	}
	
	public static Bitmap resize(Bitmap bitmap, int maxHeight, int maxWidth)
	{
		if (bitmap == null) {
			throw new IllegalArgumentException("bitmap");
		}
		
		int height = bitmap.getHeight();
		int width  = bitmap.getWidth();

		if ((maxHeight > 0 && height <= maxHeight) && (maxWidth > 0 && width <= maxWidth)) {
			return bitmap;
		}
		
		int newHeight = height;
		int newWidth  = width;
		
		float ratio;
		
		if (newHeight > maxHeight && maxHeight > 0) {
			ratio  =  (float)newWidth / (float)newHeight;
			newHeight = maxHeight;
			newWidth  = Math.round(ratio * (float)newHeight);
		}
		
		if (newWidth > maxWidth && maxWidth > 0) {
			ratio  = (float)newHeight / (float)newWidth;
			newWidth   = maxWidth;
			newHeight  = Math.round(ratio * (float)newWidth);
		}
		
		float scaleWidth = ((float) newWidth) / width; 
		float scaleHeight = ((float) newHeight) / height;
		Matrix matrix = new Matrix();  
		matrix.postScale(scaleWidth, scaleHeight);

		return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true); 
	}
	
	public static Bitmap downloadPictureAsBitmap (String url) throws IOException
	{
		if (url == null) {
    		throw new IllegalArgumentException ("url");
    	}
    	
    	Bitmap image = null;
    	try {
	    	URL fetchUrl = new URL(url);
	    	HttpURLConnection conn = (HttpURLConnection) fetchUrl.openConnection();
	    	InputStream stream = conn.getInputStream();
	    	
	    	image = BitmapFactory.decodeStream(stream);
    	}
	    catch (IOException ex) {
	    	Log.e(null, android.util.Log.getStackTraceString(ex));
	    	throw ex;
	    }
	    
	    return image;
	}
	
	public static byte[] downloadPicture (String url) throws IOException
    {
		Bitmap bitmap = downloadPictureAsBitmap(url);
    	return bitmapToJpeg(bitmap, 100);
    }
    
	public static byte[] bitmapToJpeg(Bitmap bitmap, int quality)
	{
		byte[] image = null;
		
		if (bitmap != null) {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			
			bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bytes);
			image =  bytes.toByteArray();
		}

		return image;
	}
	
	public static byte[] bitmapToPNG(Bitmap bitmap)
	{
		byte[] image = null;
		
		if (bitmap != null) {
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			
			bitmap.compress(Bitmap.CompressFormat.JPEG, 0, bytes);
			image =  bytes.toByteArray();
		}

		return image;
	}
	
	public static void setBoolean (SharedPreferences settings, String key, boolean value)
    {
    	if (settings == null) {
    		throw new IllegalArgumentException ("settings");
    	}
    	
    	if (key == null) {
    		throw new IllegalArgumentException ("key");
    	}
        
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);

        // Don't forget to commit your edits!!!
        editor.commit();
    }
	
	public static void setString (SharedPreferences settings, String key, String value)
    {
    	if (settings == null) {
    		throw new IllegalArgumentException ("settings");
    	}
    	
    	if (key == null) {
    		throw new IllegalArgumentException ("key");
    	}
    	
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);

        // Don't forget to commit your edits!!!
        editor.commit();
    }
	
	public static void setInt (SharedPreferences settings, String key, int value)
    {
    	if (settings == null) {
    		throw new IllegalArgumentException ("settings");
    	}
    	
    	if (key == null) {
    		throw new IllegalArgumentException ("key");
    	}
    	
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(key, value);

        // Don't forget to commit your edits!!!
        editor.commit();
    }
	
	public static void setLong (SharedPreferences settings, String key, long value)
    {
    	if (settings == null) {
    		throw new IllegalArgumentException ("settings");
    	}
    	
    	if (key == null) {
    		throw new IllegalArgumentException ("key");
    	}
    	
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(key, value);

        // Don't forget to commit your edits!!!
        editor.commit();
    }

}
