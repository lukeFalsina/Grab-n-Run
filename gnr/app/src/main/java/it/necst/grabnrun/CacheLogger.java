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

import java.io.File;
import java.io.FileNotFoundException;
// import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import android.util.Log;

/**
 * {@link CacheLogger} is an helper class used by {@link SecureLoaderFactory} in order to keep track of the connection
 * between remote resources URL and their respective files, which has been already cached in the 
 * application private folder.
 * <p>
 * It provides methods to look for the associated local file of a remote URL, as well as adding a
 * new reference between a URL and a local file. When it is dismissed by invoking the method
 * finalizeLog(), it saves back all the fresh references into an helper file on the device filesystem.
 * 
 * @author Luca Falsina
 */
final class CacheLogger {

	// Unique identifier used for Log entries
	private static final String TAG_FILE_CACHE_LOGGER = CacheLogger.class.getSimpleName();
	
	// Magic numbers for the helper log file
	private static final int ELEMENTS_PER_LOG_LINE = 3;
	private static final int REMOTE_URL = 0;
	private static final int LOCAL_FILE_NAME = 1;
	private static final int CREATION_TIMESTAMP = 2;
	
	// Field to record whether this class has been already finalized
	private boolean hasBeenAlreadyFinalized;
	
	private String cacheDirectoryPath;
	// private int daysTillConsideredFresh;
	
	private Map<String, String> remoteURLToLocalFileMap;
	private Map<String, Long> remoteURLToCreationTimestamp;
	private File helperFile;
	
	private static final String helperFileName = "helper.txt";
	
	/**
	 * This constructor generates a {@link CacheLogger} instance which will
	 * analyze the helper.txt file in the provided directory and 
	 * create all the references between remote URLs and local cached
	 * resources.
	 * <p>
	 * All of those resources, whose life time is bigger than the one 
	 * stated by the daysTillConsideredFresh parameter, will be 
	 * automatically erased. 
	 * 
	 * @param cacheDirectoryPath
	 *  the path to the directory which contains both the cached local
	 *  resources and the Log helper file.
	 * @param daysTillConsideredFresh
	 *  the number of days till a resource will be considered fresh and so
	 *  good to be cached. 
	 */
	CacheLogger(String cacheDirectoryPath, int daysTillConsideredFresh) {
	
		// this.cacheDirectory = cacheDirectory;
		// this.daysTillConsideredFresh = daysTillConsideredFresh;
		this.remoteURLToLocalFileMap = new HashMap<String, String>();
		this.remoteURLToCreationTimestamp = new HashMap<String, Long>();
		
		this.cacheDirectoryPath = cacheDirectoryPath;
		
		hasBeenAlreadyFinalized = false;
		
		// Check whether the "helper" file exists
		// If so, open it and populate the map accordingly.
		// Otherwise just skip this step
		helperFile = new File(cacheDirectoryPath + File.separator + helperFileName);
		
		if (helperFile.exists()) {
			
			Scanner in = null;
			
			// Read single entries in the helper file
			try {
				
				// Open the helper file and parse it through a Scanner.
				in = new Scanner(helperFile).useDelimiter(";\n");
				
				while (in.hasNext()) {
				
					// Parse helper file line by line.
					String currentLine = in.next();
					String[] lineTokens = currentLine.split(" ");
					
					if (lineTokens.length == ELEMENTS_PER_LOG_LINE) {
						
						File checkContainerFile = new File(cacheDirectoryPath + File.separator + lineTokens[LOCAL_FILE_NAME]);
						
						if (checkContainerFile.exists()) {
							
							// The associated file is present. Now it is necessary
							// to check whether it is fresh enough.
							long currentLivedTime = System.currentTimeMillis() - Long.valueOf(lineTokens[CREATION_TIMESTAMP]);
							// This time is obtained by multiplying:
							// maximumTimeToLive = daysTillConsideredFresh * 24 hours * 60 minutes * 1000 [ms]
							long maximumTimeToLive = daysTillConsideredFresh * 24 * 60 * 1000;
							
							if (currentLivedTime < maximumTimeToLive) {
								
								// Cached file is fresh enough and it should be added to the hash maps.
								remoteURLToLocalFileMap.put(lineTokens[REMOTE_URL], lineTokens[LOCAL_FILE_NAME]);
								remoteURLToCreationTimestamp.put(lineTokens[REMOTE_URL], Long.valueOf(lineTokens[CREATION_TIMESTAMP]));
							}
							else {
								
								// File is not fresh anymore so it should be erased..
								if (checkContainerFile.delete())
									Log.w(TAG_FILE_CACHE_LOGGER, "Issue while erasing " + checkContainerFile.getAbsolutePath());
							}
						}
					}
				}
				
			} catch (FileNotFoundException e) {
				Log.w(TAG_FILE_CACHE_LOGGER, "Issue while opening helper file!");
			} finally {
				
				if (in != null) {
					in.close();
				}
			}
		}
	}

	/**
	 * This method checks inside the {@link CacheLogger} data structure whether a certain URL of a remote container
	 * is associated to one local file stored in the private application folder.
	 * 
	 * @param remoteURL
	 *  a {@link java.net.URL} pointing to a remote resource.
	 * @return
	 *  either the name of the local file associated to the remote URL or {@code null}.
	 */
	final String checkForCachedEntry(String remoteURL) {
		
		// Check on the hasBeenAlreadyFinalized field..
		if (hasBeenAlreadyFinalized) return null;
		
		// If the remote URL is contained in the map, return the
		// linked fresh local container
		if (remoteURLToLocalFileMap.containsKey(remoteURL))
			if (new File(cacheDirectoryPath + File.separator + remoteURLToLocalFileMap.get(remoteURL)).exists())
				return remoteURLToLocalFileMap.get(remoteURL);
		
		// Otherwise no cached entry..
		return null;
	}
	
	/**
	 * Every time that a remote resource is successfully imported into the local
	 * cache folder, this method should be invoked to link the initial remote 
	 * {@link java.net.URL} and the corresponding local file stored on the mobile.
	 * 
	 * @param remoteURL
	 *  the remote {@link java.net.URL} from which the resource was retrieved.
	 * @param localFileName
	 *  the final location on the mobile where the resource has been stored.
	 */
	final void addCachedEntryToLog(String remoteURL, String localFileName) {
		
		// Check on the hasBeenAlreadyFinalized field..
		if (hasBeenAlreadyFinalized) return;
		
		// Compute timestamp as well to add to the new log entry.
		remoteURLToCreationTimestamp.put(remoteURL, System.currentTimeMillis());
		remoteURLToLocalFileMap.put(remoteURL, localFileName);
	}
	
	// Save back the helper log file on the filesystem..
	/**
	 * This method must be called before dismissing the {@link CacheLogger} object.
	 * <p>
	 * It writes back to the helper file all the saved linkages between remote 
	 * {@link java.net.URL} and local resources imported into the application private
	 * directory.
	 * 
	 */
	final void finalizeLog() {
		
		// Check on the hasBeenAlreadyFinalized field..
		if (hasBeenAlreadyFinalized) return;
		else hasBeenAlreadyFinalized = true;
		
		// At first clean the helper file, if it exists..
		if (helperFile.exists())
			if (!helperFile.delete())
				Log.w(TAG_FILE_CACHE_LOGGER, "Problem while erasing old copy of helper file!");
		
		if (!remoteURLToLocalFileMap.isEmpty()) {
			
			// In this branch it is necessary to write back
			// map entries into the helper file.
			
			PrintWriter mPrintWriter = null;
			
			try {
				// FileWriter mFileWriter = new FileWriter(helperFile);
				mPrintWriter = new PrintWriter(helperFile);
				
				Iterator<String> remoteURLIterator = remoteURLToLocalFileMap.keySet().iterator();
				
				while (remoteURLIterator.hasNext()) {
					
					String currentRemoteURL = remoteURLIterator.next();
					
					if (remoteURLToCreationTimestamp.containsKey(currentRemoteURL)) {
						
						// A valid entry is saved into a line of the Log helper file with the following format:
						// Remote URL + blank space + Local File Name + blank space + Creation Timestamp
						mPrintWriter.println(currentRemoteURL + " " + remoteURLToLocalFileMap.get(currentRemoteURL) + " " + remoteURLToCreationTimestamp.get(currentRemoteURL) + ";");
					}
				}
				
				if (mPrintWriter.checkError())
					throw new IOException();
				
				Log.d(TAG_FILE_CACHE_LOGGER, "Helper file was correctly stored on the device.");
				
			} catch (IOException e) {
				Log.w(TAG_FILE_CACHE_LOGGER, "Problem while updating helper file!");
			} finally {
				
				if (mPrintWriter != null)
					mPrintWriter.close();
			}
		}
	}
}