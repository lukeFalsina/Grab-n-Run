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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * {@link CacheLogger} is an helper class used by {@link SecureLoaderFactory} to keep lined
 * remote URLs with the corresponding local resources, which have been already cached in the
 * application private folder.
 * <p>
 * It provides methods to look for the associated local file of a remote URL, as well as adding a
 * new reference between a URL, and a local file. For dismissing it, invoke the finalizeLog()
 * method, which saves all the fresh references into an helper file on the device filesystem,
 * in the same private folder.
 * 
 * @author Luca Falsina
 */
final class CacheLogger {

    private static final String TAG_CACHE_LOGGER = CacheLogger.class.getSimpleName();
	
	// Constants used for the formatting the helper log file
	private static final int ELEMENTS_PER_LOG_LINE = 3;
	private static final int REMOTE_URL_INDEX = 0;
	private static final int LOCAL_FILE_NAME_INDEX = 1;
	private static final int CREATION_TIMESTAMP_INDEX = 2;
	public static final int HOURS_PER_DAY = 24;
	public static final int MINUTES_PER_HOUR = 60;
	public static final int SEC_TO_MILLISEC = 1000;
    @VisibleForTesting static final String FIELD_SEPARATOR_FOR_LOG_LINE = " ";
    @VisibleForTesting static final String TERMINATOR_FOR_LOG_LINE = ";";

    @VisibleForTesting static final String HELPER_FILE_NAME = "helper.txt";
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private boolean hasBeenAlreadyFinalized;
    private final String cacheDirectoryPath;

    private final int daysTillConsideredFresh;
    private final Map<URL, String> remoteURLToLocalFileNameMap;
	private final Map<URL, Long> remoteURLToCreationTimestampMap;

    private File helperFile;

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
     * @throws IllegalArgumentException if the number of days is not greater then zero
	 */
	CacheLogger(@NonNull String cacheDirectoryPath, int daysTillConsideredFresh) {
		this.cacheDirectoryPath = checkNotNull(
				cacheDirectoryPath, "cacheDirectoryPath was null.");
		checkArgument(
				daysTillConsideredFresh > 0, "daysTillConsideredFresh must be a positive integer.");
        this.daysTillConsideredFresh = daysTillConsideredFresh;

		this.remoteURLToLocalFileNameMap = new HashMap<>();
		this.remoteURLToCreationTimestampMap = new HashMap<>();

		hasBeenAlreadyFinalized = false;
		
		// Check whether the "helper" file exists
		// In this case, populate the map accordingly to it.
		// Otherwise just skip this step.
        initializeMapsThroughHelperFile();
	}

    private void initializeMapsThroughHelperFile() {
        helperFile = new File(cacheDirectoryPath, HELPER_FILE_NAME);

        if (helperFile.exists()) {
            Scanner helperFileScanner = null;

            try {
                helperFileScanner = new Scanner(helperFile)
                        .useDelimiter(TERMINATOR_FOR_LOG_LINE + LINE_SEPARATOR);

                while (helperFileScanner.hasNext()) {
                    parseLineInHelperFile(helperFileScanner.next());
                }
            } catch (FileNotFoundException e) {
                Log.w(TAG_CACHE_LOGGER, "Issue while opening the helper file");
            } finally {
                if (helperFileScanner != null) {
                    helperFileScanner.close();
                }
            }
        }
    }

    private void parseLineInHelperFile(String currentLine) {
        checkNotNull(currentLine, "The current parsed line was empty.");
        String[] lineTokens = currentLine.split(FIELD_SEPARATOR_FOR_LOG_LINE);

        if (lineTokens.length == ELEMENTS_PER_LOG_LINE) {
            File checkContainerFile = new File(cacheDirectoryPath, lineTokens[LOCAL_FILE_NAME_INDEX]);

            if (checkContainerFile.exists()) {
                try {
                    // The associated file is present. Now it is necessary
                    // to check whether it is fresh enough.
                    checkLocalFileFreshness(Long.valueOf(lineTokens[CREATION_TIMESTAMP_INDEX]));

                    // Cached file is fresh enough and it should be added to the hash maps.
                    URL reconstructedRemoteURL = new URL(lineTokens[REMOTE_URL_INDEX]);
                    remoteURLToLocalFileNameMap.put(
                            reconstructedRemoteURL, lineTokens[LOCAL_FILE_NAME_INDEX]);
                    remoteURLToCreationTimestampMap.put(
                            reconstructedRemoteURL, Long.valueOf(lineTokens[CREATION_TIMESTAMP_INDEX]));
                } catch (IllegalStateException unused) {
                    // File is not fresh anymore so it should be erased..
                    if (!checkContainerFile.delete())
                        Log.w(TAG_CACHE_LOGGER,
                                "Issue while erasing " + checkContainerFile.getAbsolutePath());
                } catch (MalformedURLException e) {
                    Log.w(
                            TAG_CACHE_LOGGER,
                            "The string " + lineTokens[REMOTE_URL_INDEX] + " can not be " +
                                    "converted back to a valid remote URL.");
                }
            }
        }
    }

    private void checkLocalFileFreshness(Long fileCreationTimestamp) {
        long currentLivedTime = System.currentTimeMillis() - fileCreationTimestamp;
        long maximumTimeToLive =
				daysTillConsideredFresh * HOURS_PER_DAY * MINUTES_PER_HOUR * SEC_TO_MILLISEC;
        checkState(currentLivedTime < maximumTimeToLive, "The current local copy is not fresh enough.");
    }

    /**
	 * This method checks inside the {@link CacheLogger} data structure whether a certain URL of
     * a remote container is associated to one local file stored in the private application folder.
	 * 
	 * @param remoteURL
	 *  a {@link java.net.URL} pointing to a remote resource.
	 * @return
	 *  an optional {@link java.lang.String} pointing to the local file associated to the remote URL.
	 */
	final @NonNull Optional<String> checkForCachedEntry(@NonNull URL remoteURL) {
        checkNotNull(remoteURL, "The remote URL must not be null");
		if (hasBeenAlreadyFinalized) return Optional.absent();

		// If the remote URL is contained in the map, return the
		// linked fresh local container
		if (remoteURLToLocalFileNameMap.containsKey(remoteURL))
			if (new File(cacheDirectoryPath, remoteURLToLocalFileNameMap.get(remoteURL)).exists())
				return Optional.of(remoteURLToLocalFileNameMap.get(remoteURL));
		
		// Otherwise no cached entry..
		return Optional.absent();
	}
	
	/**
	 * Every time that a remote resource is successfully imported into the local
	 * cache folder, this method should be invoked to link the initial remote 
	 * {@link java.net.URL} and the corresponding local file stored on the mobile.
	 * 
	 * @param remoteURL
	 *  the remote {@link java.net.URL} from which the resource was retrieved.
	 * @param localFileName
	 *  the name of the file on the mobile where the resource will be stored.
	 */
	final void addCachedEntryToLog(@NonNull URL remoteURL, @NonNull String localFileName) {
        checkNotNull(remoteURL, "The remote URL must not be null");
        checkNotNull(localFileName, "The file name  must not be null");
        if (hasBeenAlreadyFinalized) return;

		// Add also a timestamp for verifying the freshness of the new log entry later.
		remoteURLToCreationTimestampMap.put(remoteURL, System.currentTimeMillis());
		remoteURLToLocalFileNameMap.put(remoteURL, localFileName);
	}

	/**
	 * This method must be called before dismissing the {@link CacheLogger} object.
	 * <p>
	 * It writes back to the helper file all the saved linkages between remote 
	 * {@link java.net.URL} and local resources imported into the application private
	 * directory.
	 */
	final void finalizeLog() {
		if (hasBeenAlreadyFinalized) return;
		hasBeenAlreadyFinalized = true;

        deletePreviousHelperFile();

        if (!remoteURLToLocalFileNameMap.isEmpty()) {
            updateHelperFileWithNewMappings();
		}
	}

    private void deletePreviousHelperFile() {
        if (helperFile.exists())
            if (!helperFile.delete())
                Log.w(TAG_CACHE_LOGGER, "Problem while erasing old copy of helper file!");
    }

    private void updateHelperFileWithNewMappings() {
        PrintWriter mPrintWriter = null;

        try {
            mPrintWriter = new PrintWriter(helperFile);

            for (URL currentRemoteURL : remoteURLToLocalFileNameMap.keySet()) {
                if (remoteURLToCreationTimestampMap.containsKey(currentRemoteURL)) {
                    mPrintWriter.println(getLogLineFromURL(currentRemoteURL));
                }
            }

            if (mPrintWriter.checkError()) { throw new IOException(); }
            Log.d(TAG_CACHE_LOGGER, "Helper file was correctly stored on the device.");
        } catch (IOException e) {
            Log.w(TAG_CACHE_LOGGER, "Problem while storing the updated helper file.");
        } finally {
            if (mPrintWriter != null)
                mPrintWriter.close();
        }
    }

    private String getLogLineFromURL(URL currentRemoteURL) {
        return currentRemoteURL.toExternalForm() + FIELD_SEPARATOR_FOR_LOG_LINE
                + remoteURLToLocalFileNameMap.get(currentRemoteURL) + FIELD_SEPARATOR_FOR_LOG_LINE
                + remoteURLToCreationTimestampMap.get(currentRemoteURL) + TERMINATOR_FOR_LOG_LINE;
    }
}