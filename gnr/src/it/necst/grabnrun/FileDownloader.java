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

import android.content.Context;
import android.content.ContextWrapper;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * {@link FileDownloader} is a class used to download remote resources like containers or certificates, which are then used 
 * for dynamic class loading or signature verification.
 * 
 * @author Luca Falsina
 */
final class FileDownloader {
	
	// Unique identifier used for Log entries
	private static final String TAG_FILE_DOWNLOADER = FileDownloader.class.getSimpleName();
	
	// Objects used to check availability of Internet connection
	private ConnectivityManager mConnectivityManager;
	private NetworkInfo activeNetworkInfo;

	/**
	 * This constructor initializes a {@link FileDownloader} object for downloading remote
	 * resources.
	 * 
	 * @param parentContextWrapper
	 *  a {@link ContextWrapper} coming from the parent {@link Activity} and used to
	 *  retrieve the state of the connectivity service of the mobile.
	 */
	FileDownloader(ContextWrapper parentContextWrapper) {

		mConnectivityManager = (ConnectivityManager) parentContextWrapper.getSystemService(Context.CONNECTIVITY_SERVICE);		
	}
	
	// Assumption: input URL and output URI has been already validated by the caller of this method..
	/**
	 * This method takes a remote {@link URL} and instantiate a data {@link Thread} responsible for
	 * downloading the resource and storing it on the mobile at the location provided by the second 
	 * parameter.
	 * <p>
	 * It is also possible to specify whether a redirect link should be followed or ignored.
	 * 
	 * @param remoteURL
	 *  the remote {@link URL} from which the remote resources is downloaded.
	 * @param localURI
	 *  the local path at which the final resource is expected to be in case of a successful download.
	 * @param isRedirectAllowed
	 *  a {@code boolean} stating whether {@link FileDownloader} should follow a redirect link or not.
	 * @return
	 *  a boolean indicating whether the downloading procedure succeeded.
	 */
	final boolean downloadRemoteUrl(final URL remoteURL, final String localURI, final boolean isRedirectAllowed) {
	
		// Check whether Internet access is granted..
		activeNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
		if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
			
			Log.w(TAG_FILE_DOWNLOADER, "No connectivity is available. Download failed!");
			return false;
		}
		
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
    				
    				// Fix timeout for the connection..
    				urlConnection.setConnectTimeout(1000);
    				// It should not be necessary to disable redirect manually..
    				// urlConnection.setInstanceFollowRedirects(false);
    				
    				Log.d(TAG_FILE_DOWNLOADER, "A connection was set up: " + remoteURL.toString());
    				
    				// When it is allowed, check if this URL asks for a redirect..
    				if (isRedirectAllowed) {
    					
    					boolean redirect = false;
    					
    					// Normally 301 or 302 is redirect..
    					int connection_status = urlConnection.getResponseCode();
    					if (connection_status != HttpURLConnection.HTTP_OK) {
    						if (connection_status == HttpURLConnection.HTTP_MOVED_TEMP
    							|| connection_status == HttpURLConnection.HTTP_MOVED_PERM
    								|| connection_status == HttpURLConnection.HTTP_SEE_OTHER)
    							// A redirect is required
    							redirect = true;
    					}
    					
    					if (redirect) {
    						 
    						// Get redirect URL from "location" header field
    						URL redirectedURL = new URL(urlConnection.getHeaderField("Location"));
    				 
    						// Get the cookie for login if provided
    						String cookies = urlConnection.getHeaderField("Set-Cookie");
    						
    						// Open the new redirected connection again..
    						if (redirectedURL.getProtocol().equals("https")) {
    	    					// HTTPS protocol
    	    					urlConnection = (HttpsURLConnection) redirectedURL.openConnection();
    	    				}
    	    				else {
    	    					// HTTP protocol
    	    					urlConnection = (HttpURLConnection) redirectedURL.openConnection();
    	    				}
    						
    						// Fix timeout for the connection..
    	    				urlConnection.setConnectTimeout(1000);
    	    				
    						urlConnection.setRequestProperty("Cookie", cookies);
    				 
    						Log.d(TAG_FILE_DOWNLOADER, "The connection was redirected to: " + redirectedURL.toString());
    					}
    				}
    				
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