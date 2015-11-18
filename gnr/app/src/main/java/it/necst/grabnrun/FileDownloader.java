/*******************************************************************************
 * Copyright 2014 Luca Falsina
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package it.necst.grabnrun;

import static android.content.Context.*;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * {@link FileDownloader} retrieves remote resources like containers or certificates,
 * which are then used for dynamic class loading or signature verification.
 * 
 * @author Luca Falsina
 */
final class FileDownloader {

    // Unique identifier used for Log entries
	private static final String TAG_FILE_DOWNLOADER = FileDownloader.class.getSimpleName();

    public static final ImmutableMap<String, String> SUPPORTED_MIME_TYPE_TO_FILE_EXTENSION_MAP =
            ImmutableMap.of(
                    "application/vnd.android.package-archive", ".apk",
                    "application/java-archive", ".jar",
                    "application/octet-stream", ".pem");

    private static final String HTTPS_PROTOCOL_AS_STRING = "https";
    private static final String HTTP_PROTOCOL_AS_STRING = "http";

    // Objects used to check availability of Internet connection
	private ConnectivityManager mConnectivityManager;

	// private NetworkInfo activeNetworkInfo;
	private Optional<String> retrievedFileMimeType;

	/**
	 * This constructor initializes a {@link FileDownloader} for retrieving remote resources.
	 *
	 * @param parentContext
	 *  a {@link Context} coming from the parent {@link android.app.Activity} and used to
	 *  retrieve the state of the connectivity service on the mobile.
	 */
	FileDownloader(@NonNull Context parentContext) {

        checkNotNull(parentContext, "The parent context must be not null");
		mConnectivityManager = (ConnectivityManager) parentContext.getSystemService(CONNECTIVITY_SERVICE);
		retrievedFileMimeType = Optional.absent();
	}

	/**
	 * This method takes a remote {@link URL}, downloads the corresponding resource,
     * and stores it on the mobile at the location provided by the second parameter.
     * The download task is carried on a separate thread.
	 * <p>
	 * It is also possible to specify whether redirect link should be followed for one hop,
     * or simply ignored.
	 * 
	 * @param remoteURL
	 *  the {@link URL} where the remote resource is located.
	 * @param localURI
	 *  the local path where the final resource will be stored in case of a successful download.
	 * @param isRedirectAllowed
	 *  a {@code boolean} stating whether {@link FileDownloader} should follow redirect link
     *  for one hop, or not.
	 * @return
	 *  a boolean indicating whether the download was successful.
	 */
	final boolean downloadRemoteResource(
            @NonNull final URL remoteURL,
            @NonNull final String localURI,
            final boolean isRedirectAllowed) {

        checkNotNull(
                remoteURL,
                "The URL at which the remote resource is located must not be null");
        checkNotNull(
                localURI,
                "The URI at which storing the file pointed by the remote URL must not be null");
        checkArgument(
                remoteURL.getProtocol().equalsIgnoreCase(HTTPS_PROTOCOL_AS_STRING) ||
                        remoteURL.getProtocol().equalsIgnoreCase(HTTP_PROTOCOL_AS_STRING),
                "The FileDownloader supports only HTTP, and HTTPS protocols");

		// Check whether Internet access is granted..
		NetworkInfo activeNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
		if (activeNetworkInfo == null || !activeNetworkInfo.isConnected()) {
			
			Log.w(TAG_FILE_DOWNLOADER, "No connectivity is available. Download failed!");
			return false;
		}
		
    	// Data are retrieved in a separate thread.
    	Thread dataThread = new Thread() {
    		
    		@Override
    		public void run() {
    			
    			HttpURLConnection urlConnection = null;
    			InputStream inputStream = null;
    			OutputStream outputStream = null;
    			
    			try {

    				urlConnection = openAndReturnConnectionAssociatedToURL(remoteURL);

    				// Fix timeout for the connection..
    				urlConnection.setConnectTimeout(1000);
    				
    				Log.d(TAG_FILE_DOWNLOADER, "A connection was set up: " + remoteURL.toString());
    				
    				// When it is allowed, check if this URL asks for a redirect..
    				if (isRedirectAllowed) {
    					
    					// Normally 301, 302, or 303 is redirect..
    					int connection_status = urlConnection.getResponseCode();

    					if (connection_status != HttpURLConnection.HTTP_OK) {
    						if (connection_status == HttpURLConnection.HTTP_MOVED_TEMP
    							|| connection_status == HttpURLConnection.HTTP_MOVED_PERM
    								|| connection_status == HttpURLConnection.HTTP_SEE_OTHER) {
                                urlConnection = followURLRedirection(urlConnection);
                            }
                        }
    				}

                    if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {

                        retrievedFileMimeType = Optional.fromNullable(urlConnection.getContentType());

                        inputStream = new BufferedInputStream(urlConnection.getInputStream());
                        outputStream = new FileOutputStream(localURI);

                        copyReadContentFromInputStreamToOutputStream(inputStream, outputStream);
                    }

    			} catch (IOException e) {
    				// No file was found at the remote URL!
    				// Nothing should have been written at the local path 
    				// and so false should be returned.
    			} finally {
                    closeConnectionAndStreams(urlConnection, inputStream, outputStream);
    			}
    		}

            private HttpURLConnection followURLRedirection(HttpURLConnection urlConnection)
                    throws IOException {
                // Get redirect URL from "location" header field
                URL redirectedURL = new URL(urlConnection.getHeaderField("Location"));

                // Get the cookie for login if provided
                String cookies = urlConnection.getHeaderField("Set-Cookie");

                // Open the new redirected connection again..
                urlConnection = openAndReturnConnectionAssociatedToURL(redirectedURL);

                urlConnection.setConnectTimeout(1000);
                urlConnection.setRequestProperty("Cookie", cookies);

                Log.d(TAG_FILE_DOWNLOADER, "The connection was redirected to: " + redirectedURL.toString());
                return urlConnection;
            }

            private HttpURLConnection openAndReturnConnectionAssociatedToURL(URL remoteURL)
                    throws IOException {
                if (remoteURL.getProtocol().equals(HTTPS_PROTOCOL_AS_STRING)) {
                    return (HttpsURLConnection) remoteURL.openConnection();
                } else {
                    return (HttpURLConnection) remoteURL.openConnection();
                }
            }

            private void copyReadContentFromInputStreamToOutputStream(
                    InputStream inputStream,
                    OutputStream outputStream) throws IOException {
                int read;
                byte[] bytes = new byte[1024];

                while ((read = inputStream.read(bytes)) > 0) {
                    outputStream.write(bytes, 0, read);
                }

                Log.i(TAG_FILE_DOWNLOADER, "Download complete. Container Path: " + localURI);
            }

            private void closeConnectionAndStreams(
                    HttpURLConnection urlConnection,
                    InputStream inputStream,
                    OutputStream outputStream) {
                Log.d(TAG_FILE_DOWNLOADER, "Clean up all pending streams..");
                if (urlConnection != null)
                    urlConnection.disconnect();

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
        };
    	
    	dataThread.start();
    	
    	try {
			dataThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}

        // Download is successful when a not empty file was retrieved, and stored.
        File fileAtLocalURI = new File(localURI);
        return fileAtLocalURI.exists() && fileAtLocalURI.length() > 0;
	}
	
	/**
	 * This method analyzes the MIME-type of the last downloaded file
	 * and returns the related extension.
	 * 
	 * @return
	 *  an optional with either the extension of the last downloaded file,
     *  or absent if MIME-type is unknown
     *  (see {@link #SUPPORTED_MIME_TYPE_TO_FILE_EXTENSION_MAP}).
	 */
	Optional<String> getDownloadedFileExtension() {

        if (retrievedFileMimeType.isPresent() &&
                SUPPORTED_MIME_TYPE_TO_FILE_EXTENSION_MAP.containsKey(retrievedFileMimeType.get())) {
            return Optional.of(
                    SUPPORTED_MIME_TYPE_TO_FILE_EXTENSION_MAP.get(retrievedFileMimeType.get()));
        }

        return Optional.absent();
	}
}