package it.necst.grabnrun;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

public final class FileDownloader {
	
	// Unique identifier used for Log entries
	private static final String TAG_FILE_DOWNLOADER = FileDownloader.class.getSimpleName();
	
	private ContextWrapper mContextWrapper;
	
	// Objects used to check availability of Internet connection
	private ConnectivityManager mConnectivityManager;
	private NetworkInfo activeNetworkInfo;
	
	// Used to avoid users from clicking while downloading 
	// other components..
	private ProgressDialog dialog;
	
	// Used to dismiss the dialog
	private Handler handler;

	FileDownloader(ContextWrapper parentContextWrapper) {
		
		handler = new Handler();
		mContextWrapper = parentContextWrapper;
		mConnectivityManager = (ConnectivityManager) parentContextWrapper.getSystemService(Context.CONNECTIVITY_SERVICE);		
	}
	
	// Assumption: input URL and output URI has been already validated
	// before calling this method..
	final boolean downloadRemoteUrl(final URL remoteURL, final String localURI) {
	
		// Check whether Internet access is granted..
		activeNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
		if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
			
			Log.w(TAG_FILE_DOWNLOADER, "No connectivity is available. Download failed!");
			return false;
		}
		
		// A progress dialog is shown to let the user know about the downloading process
    	dialog = ProgressDialog.show((Activity) mContextWrapper, "Downloading", "Downloading a remote resource..");
		
    	// Data are retrieved here by an auxiliary thread.
    	Thread dataThread = new Thread() {
    		
    		@Override
    		public void run() {
    			
    			HttpURLConnection urlConnection = null;
    			InputStream inputStream = null;
    			OutputStream outputStream = null;
    			
    			try {
    				
    				if (remoteURL.getProtocol().equals("https")) {
    					// HTTPS protocol
    					urlConnection = (HttpsURLConnection) remoteURL.openConnection();
    				}
    				else {
    					// HTTP protocol
    					urlConnection = (HttpURLConnection) remoteURL.openConnection();
    				}
    				
    				Log.d(TAG_FILE_DOWNLOADER, "A connection was set up: " + remoteURL.toString());
    				
    				inputStream = new BufferedInputStream(urlConnection.getInputStream());
					outputStream = new FileOutputStream(localURI);
					
					int read = 0;
					byte[] bytes = new byte[1024];
					
					while ((read = inputStream.read(bytes)) > 0) {
						outputStream.write(bytes, 0, read);
					}
					
					Log.i(TAG_FILE_DOWNLOADER, "Download complete. Container Path: " + localURI);
					
    			} catch (IOException e) {
    				// No file was found at the remote URL!
    				// Nothing should have been written at the local path 
    				// and so null should be returned.
    				
    			} finally {
    				Log.d(TAG_FILE_DOWNLOADER, "Clean up all pending streams..");
    				if (urlConnection != null)	((HttpURLConnection) urlConnection).disconnect();

    				if (inputStream != null) {
    					try {
    						inputStream.close();
    					} catch (IOException e) {
    						e.printStackTrace();
    					}
    				}
    				if (outputStream != null) {
    					try {
    						// outputStream.flush();
    						outputStream.close();
    					} catch (IOException e) {
    						e.printStackTrace();
    					}			 
    				}    			
    			}
    			
    			// Finally dismiss the dialog..
    			handler.post(new Runnable () {

					@Override
					public void run() {
				             
				        // The progress dialog is dismissed here..
				        dialog.dismiss();
						
					}
    				
    			});
    		}
    	};
    	
    	dataThread.start();
    	
    	try {
    		// Wait for the data thread to finish its job..
			dataThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
    	
    	File fileAtLocalURI = new File(localURI);
    	
    	// Check whether the download was successful..
    	if (fileAtLocalURI.exists() && fileAtLocalURI.length() > 0)
    		return true;
    	else
    		return false;
	}
}