//
//  ThumbnailCache.java
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;

public class ThumbnailCache {

	private final String TAG = "ThumbnailCache";
	// use SoftReference so Android can free memory, if needed
	// ThumbnailCache can notify if a download is required or use the built-in 
	// ImageDownloader
	// See setImageListener and setImageProvider
	private static ThumbnailCache mInstance = null;
	
	private final Map <String, SoftReference<Bitmap>> mImages = new HashMap <String, SoftReference<Bitmap>> ();
	
	private final Object lock = new Object();
	
	private Bitmap mDefaultImage = null;
	private WeakReference<ImageListener> mListener = null;
	private WeakReference<ImageProvider> mProvider = null;
	private ImageDownloader mDownloader = new ImageDownloader();
	
	private ThumbnailCache() {}
	
	
	public static ThumbnailCache create()
	{
		if (mInstance == null) {
			mInstance = new ThumbnailCache();
		}
		
		return mInstance;
	}
	
	
	public void setDefaultImage(Bitmap defaultImage)
	{
		mDefaultImage = defaultImage;
	}
	
	public void destroy()
	{
		mDownloader.setPause(true);
		mDownloader = null;
		mImages.clear();
		mInstance = null;
	}
	
	public boolean contains(String key)
	{
		synchronized(lock) {
			if (mImages.containsKey(key)) {
				return true;
			}
		}
		
		return false;
	}
	
	public void add(String key, byte[] image)
	{
		add(key, BitmapFactory.decodeByteArray(image, 0, image.length));
	}
	
	public void add(String key, Bitmap bitmap)
	{
		add(key, bitmap, true);
	}
	
	public void add(String key, Bitmap bitmap, boolean resize)
	{
		add(key, bitmap, resize, false);
	}
	
	protected void add(String key, Bitmap bitmap, boolean resize, boolean notify)
	{
		if (bitmap == null) {
			throw new IllegalArgumentException("bitmap");
		}
		
		if (key == null) {
			throw new IllegalArgumentException("key");
		}
		
		if (resize) {
			bitmap = Utils.resize(bitmap, 40, 40);
		}
		
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		if (bitmap.compress(CompressFormat.JPEG, 100, out)) {
			if (out != null) {
				bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(out.toByteArray()));
			}
		}
		
		synchronized(lock) {
			mImages.put(key, new SoftReference<Bitmap>(bitmap));
			if (mListener != null) {
				ImageListener listener = mListener.get();
				if (notify && listener != null) {
					listener.onImageReady(key);
				}
			}
		}
	}
	
	public boolean remove(String key)
	{
		synchronized(lock) {
			if (mImages.containsKey(key)) {
				mImages.remove(key);
				return true;
			}
		}
		
		return false;
	}
	
	public Bitmap get(String key) 
	{
		Bitmap image = null;
		
		synchronized(lock) {
			if (mImages.containsKey(key)) {
				image = mImages.get(key).get();
				if (image == null) {
					if (mDefaultImage != null) {
						mImages.put(key, new SoftReference<Bitmap>(mDefaultImage));
						image = mDefaultImage;
					}
					ImageProvider provider = null;
					if (mProvider != null) {
						provider = mProvider.get();
					}
					
					if (provider == null) {
						mDownloader.download(key);
					}
					else {
						mImages.remove(key);
						provider.onImageRequired(key);
					}
				}
			}
		}
		
		return image;
	}
	
	// this can be used in onPause and onResume to conserve
	// battery life by terminating the looping downloader
	// thread
	public void togglePauseOnDownloader(boolean value)
	{
		mDownloader.setPause(value);
	}
	
	public void setImageListener(ImageListener listener)
	{
		synchronized(lock) {
			mListener = new WeakReference<ImageListener>(listener);
		}
	}
	
	public void setImageProvider(ImageProvider provider)
	{
		synchronized(lock) {
			mProvider = new WeakReference<ImageProvider>(provider);
		}
	}
	
	public interface ImageListener {
		void onImageReady(String url);
	}
	
	public interface ImageProvider {
		boolean onImageRequired(String url);
	}
	
	private class ImageDownloader {
		
		private final BlockingQueue<String> urlQueue = new LinkedBlockingQueue<String>();
		private Thread downloadThread;
		private boolean paused = false;
		
		public ImageDownloader()
		{
			setupThread();
		}

		private void setupThread()
		{
			downloadThread = new Thread(new Runnable() {
				public void run() {
					String url;
					while(!paused) {
						try {
							url = urlQueue.take();
							Bitmap image;
							image = Utils.downloadPictureAsBitmap(url);
							add(url, image, true, true);
						} catch (InterruptedException e) {
							Log.d(TAG, "INTERRUPTED!");
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			});
			
			downloadThread.start();
		}
		
		public void setPause(boolean value)
		{
			if (paused == value) {
				return;
			}
			
			Log.d(TAG, "setPause called with " + value);
			paused = value;
			if (paused && downloadThread != null) {
				downloadThread.interrupt();
			} else if (!paused) {
				setupThread();
			}
		}
		
		public void download(String url)
		{
			if (url == null) {
				return;
			}
			
			try {
				urlQueue.put(url);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
