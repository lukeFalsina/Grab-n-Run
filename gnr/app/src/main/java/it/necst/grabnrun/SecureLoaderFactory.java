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

import android.content.ContextWrapper;
import android.util.Base64;
import android.util.Log;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A factory class that generates instances of classes used to
 * retrieve containers holding code to execute dynamically in a secure way.
 * 
 * @author Luca Falsina
 */
public class SecureLoaderFactory {

    // Unique identifier used for Log entries
	private static final String TAG_SECURE_FACTORY = SecureLoaderFactory.class.getSimpleName();

    // Name of the folder user to store imported containers (both coming from remote or local resources)
    @VisibleForTesting static final String CONT_IMPORT_DIR = "imported_cont";

    /**
     * When a URL for a remote container is found, this field specifies the default time interval,
     * expressed in days, before a local copy of it, stored in an application-private directory,
     * will be considered rotten, and so not acceptable to be cached.
     * <p>
     * Those local copies of remote containers, whose life time is greater than this
     * field value, will be erased from the device storage in stead of being cached.
     * <p>
     * You can change this duration by generating the {@link SecureLoaderFactory} instance
     * with {@link SecureLoaderFactory#SecureLoaderFactory(android.content.ContextWrapper, int)}.
     */
    public static final int DEFAULT_DAYS_BEFORE_CONTAINER_EXPIRATION = 5;

    private static final String HTTP_PROTOCOL_STRING = "http://";
    private static final String HTTPS_PROTOCOL_STRING = "https://";

    private ContextWrapper contextWrapper;

	private FileDownloader fileDownloader;

	// Used to compute the digest of different containers
	// in order to check which has been already cached.
	private MessageDigest messageDigest;

	private int daysBeforeContainerCacheExpiration;
	
	/**
	 * Creates a {@link SecureLoaderFactory} used to check and generate instances 
	 * from secure dynamic code loader classes.
	 * <p>
	 * It requires a {@link android.content.ContextWrapper} (i.e. the launching activity) which 
	 * should be used to manage and retrieve internal directories 
	 * of the application.
     * <p>
     * The number of days for which a local copy of a remote resource is considered acceptable
     * is set to the value of {@link SecureLoaderFactory#DEFAULT_DAYS_BEFORE_CONTAINER_EXPIRATION}.
	 * 
	 * @param parentContextWrapper
	 *  The content wrapper coming from the launching Activity.
	 */
	public SecureLoaderFactory(ContextWrapper parentContextWrapper) {
	
		this(parentContextWrapper, DEFAULT_DAYS_BEFORE_CONTAINER_EXPIRATION);
	}
	
	/**
     * Creates a {@link SecureLoaderFactory} used to check and generate instances
     * from secure dynamic code loader classes.
     * <p>
     * It requires a {@link android.content.ContextWrapper} (i.e. the launching activity) which
     * should be used to manage and retrieve internal directories
     * of the application.
     * <p>
	 * It allows to decide the time interval in days before which a local copy of a remote
     * container, will be considered fresh and so acceptable to be cached.
	 * <p>
	 * If a negative value is provided for the second parameter, {@link SecureLoaderFactory} will
	 * throws an {@link IllegalArgumentException}.
	 * 
	 * @param parentContextWrapper
	 *  The content wrapper coming from the launching Activity.
	 * @param daysBeforeContainerCacheExpiration
	 *  The value in days for which a local copy of a remote container is considered fresh,
     *  thus acceptable to be cached.
     * @throws IllegalArgumentException if the number of days is not greater than zero
	 */
	public SecureLoaderFactory(ContextWrapper parentContextWrapper, int daysBeforeContainerCacheExpiration) {

        checkArgument(
                daysBeforeContainerCacheExpiration > 0,
                "The number of days before considering a container rotten must be greater than zero");
        this.daysBeforeContainerCacheExpiration = daysBeforeContainerCacheExpiration;

		contextWrapper = parentContextWrapper;
		fileDownloader = new FileDownloader(contextWrapper);
		
		try {
			messageDigest = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG_SECURE_FACTORY, "Wrong algorithm choice for message digest!");
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a {@link SecureDexClassLoader} that finds interpreted and native code in a set of
	 * provided locations (either local, or remote via HTTP, or HTTPS protocol) in dexPath.
	 * Interpreted classes are found in a set of DEX files contained in Jar or Apk files and 
	 * stored into an application-private directory.
	 * <p>
	 * Before executing one of these classes the signature of the target class is 
	 * verified against the certificate associated with its package name.
	 * Certificates location are provided by filling appropriately packageNameToCertificateMap;
	 * each package name must be linked with the remote location of the certificate that
	 * should be used to validate all the classes of that package. It's important 
	 * that each one of these locations uses HTTPS as its protocol; otherwise this 
	 * choice will be enforced!
	 * If a class package name do not match any of the provided entries in the map, 
	 * certificate location will be constructed by simply reverting package name and 
	 * transforming it into a web-based URL using HTTPS.
	 * <p>
	 * Note that this method returns {@code null} if no matching Jar, or Apk file is found at the
	 * provided dexPath parameter; otherwise a {@link SecureDexClassLoader} instance is returned.
	 * <p>
	 * Dynamic class loading with the returned {@link SecureDexClassLoader} will fail whether
	 * at least one of these conditions is not accomplished: target class is not found in dexPath
	 * or is in a missing remote container (i.e. Internet connectivity is not present), missing or
	 * invalid (i.e. expired) certificate is associated with the package name of the target class,
	 * target class signature check fails against the associated certificate.
	 * 
	 * @param dexPath
	 *  the list of jar/apk files containing classes and resources; these paths could
	 *  be either local URLs pointing to a location in the device or URLs that links
	 *  to a resource stored in the web via HTTP/HTTPS. In the latter case, if Internet
	 *  connectivity is available, the resource will be imported in a private-application 
	 *  directory before being used.
	 * @param libraryPath
	 *  the list of directories containing native libraries; it may be {@code null}.
	 * @param parent
	 *  the parent class loader.
	 * @param packageNameToCertificateMap
	 *  a map that couples each package name to a URL which contains the certificate
	 *  that must be used to validate all the classes that belong to that package
	 *  before launching them at run time. Please notice that any URL in this map 
	 *  using HTTP protocol will be enforced to use HTTPS in stead.
	 * @return
	 *  a {@link SecureDexClassLoader} object which can be used to load dynamic code securely and 
	 *  uses a Eager strategy for container signature verification.
	 */
	public SecureDexClassLoader createDexClassLoader(
            String dexPath,
			String libraryPath,
			ClassLoader parent,
			Map<String, URL> packageNameToCertificateMap) {
		
		// The default behavior is EAGER evaluation.
		// In order to change it, simply modify the last boolean parameter from "false" to "true".
		return createDexClassLoader(dexPath, libraryPath, parent, packageNameToCertificateMap, false);
	}
	
	/**
	 * This method returns a {@link SecureDexClassLoader} instance in the same way as it is 
	 * explained in the {@link SecureLoaderFactory#createDexClassLoader(String, String, ClassLoader, Map)}. In addition it is 
	 * possible to specify the mode in which the signature verification process will be carried.
	 * <p>
	 * In particular by setting the performLazyEvaluation parameter on true, 
	 * a lazy verification process will be chosen. This means that the signature
	 * of the single container associated with the target class will be evaluated 
	 * only when {@link SecureDexClassLoader#loadClass(String)} will be invoked . 
	 * If this check succeeds the target class will be loaded.
	 * <p>
	 * On the other hand, by setting the parameter performLazyEvaluation to false 
	 * an eager evaluation will be carried out. This means that before returning this 
	 * object, the signature verification procedure will be carried out on all the 
	 * provided containers in a CONCURRENT way and all of those that do not succeed in the process will
	 * be blocked from loading their classes in the following loadClass() method calls.
	 * <p>
	 * The use of one mode in stead of the other is merely a performance-related choice.
	 * If you do not care that much about it, just invoke
	 * {@link SecureLoaderFactory#createDexClassLoader(String, String, ClassLoader, Map)} 
	 * which does not require to provide the performLazyEvaluation
	 * parameter and it will perform an Eager evaluation. Otherwise as a general guideline, 
	 * prefer the lazy mode whenever you think that you won't need to load classes 
	 * from all the provided containers.
	 * 
	 * @param dexPath
	 *  the list of jar/apk files containing classes and resources; these paths could
	 *  be either local URLs pointing to a location in the device or URLs that links
	 *  to a resource stored in the web via HTTP/HTTPS. In the latter case, if Internet
	 *  connectivity is available, the resource will be imported in a private-application 
	 *  directory before being used.
	 * @param libraryPath
	 *  the list of directories containing native libraries; it may be {@code null}.
	 * @param parent
	 *  the parent class loader.
	 * @param packageNameToCertificateMap
	 *  a map that couples each package name to a URL which contains the certificate
	 *  that must be used to validate all the classes that belong to that package
	 *  before launching them at run time. Please notice that any URL in this map 
	 *  using HTTP protocol will be enforced to use HTTPS in stead.
	 * @param performLazyEvaluation
	 *  the mode in which the verification will be handled. True for lazy verification;
	 *  false for the eager one.
	 * @return
	 * 	a {@link SecureDexClassLoader} object which can be used to load dynamic code securely and 
	 *  uses a either Lazy or an Eager strategy for container signature verification depending
	 *  on the last parameter provided to this constructor.
	 */
	public SecureDexClassLoader createDexClassLoader(
            String dexPath,
            String libraryPath,
			ClassLoader parent,
			Map<String, URL> packageNameToCertificateMap,
			boolean performLazyEvaluation) {

		StringBuilder finalDexPathStringBuilder = new StringBuilder();
        DexPathStringProcessor dexPathStringProcessor = new DexPathStringProcessor(dexPath);

        // New container resources will be imported, or cached into this application private folder.
		File importedContainerDir = contextWrapper.getDir(CONT_IMPORT_DIR, ContextWrapper.MODE_PRIVATE);
		Log.d(TAG_SECURE_FACTORY, "Download Resource Dir has been mounted at: " + importedContainerDir.getAbsolutePath());
		
		CacheLogger remoteContainersCacheLogger = new CacheLogger(
                importedContainerDir.getAbsolutePath(), daysBeforeContainerCacheExpiration);
		
		while (dexPathStringProcessor.hasNextDexPathString()) {

            String singleDexPath = dexPathStringProcessor.nextDexPathString();

			if (singleDexPath.startsWith(HTTP_PROTOCOL_STRING) ||
                    singleDexPath.startsWith(HTTPS_PROTOCOL_STRING)) {
				
                try {
                    URL currentSingleDexPathAsURL = new URL(singleDexPath);

                    Optional<String> cachedContainerFileName =
                            remoteContainersCacheLogger.checkForCachedEntry(currentSingleDexPathAsURL);

                    if (cachedContainerFileName.isPresent()) {

                        // A valid and fresh enough cached copy of the remote container is present
                        // on the device storage, so this copy can be used in stead of downloading
                        // the remote container again.
                        finalDexPathStringBuilder
                                .append(importedContainerDir.getAbsolutePath())
                                .append(File.separator)
                                .append(cachedContainerFileName.get())
                                .append(File.pathSeparator);
                        Log.d(TAG_SECURE_FACTORY, "Dex Path has been modified into: " + finalDexPathStringBuilder);
                    } else {

                        // No cached copy so it is necessary to download the remote resource from the web.

                        //Trace.beginSection("Download Container");
                        // Log.i("Profile","[Start]	Download Container: " + System.currentTimeMillis() + " ms.");
                        String downloadedContainerPath = downloadContainerIntoFolder(singleDexPath, importedContainerDir);
                        // Log.i("Profile","[End]	Download Container: " + System.currentTimeMillis() + " ms.");
                        //Trace.endSection(); // end of "Download Container" section

                        if (downloadedContainerPath != null) {

                            // In such a case the download was successful.
                            // Now the downloaded container is renamed according to its finger print.
                            String containerDigest = computeDigestFromFilePath(downloadedContainerPath);

                            File downloadedContainer = new File(downloadedContainerPath);

                            if (containerDigest == null) {

                                // Fingerprint computation fails. Delete the resource container and do not add
                                // this file to the singleDexPath
                                if (!downloadedContainer.delete())
                                    Log.w(TAG_SECURE_FACTORY, "Issue while deleting " + downloadedContainerPath);
                            } else {

                                // Compute the extension of the file.
                                int extensionIndex = downloadedContainerPath.lastIndexOf(".");
                                String extension = downloadedContainerPath.substring(extensionIndex);

                                // Rename the previous container file according to the containerDigest and its extension.
                                String downloadedContainerFinalPath = importedContainerDir.getAbsolutePath() + File.separator + containerDigest + extension;

                                File downloadContainerFinalPosition = new File(downloadedContainerFinalPath);

                                if (downloadContainerFinalPosition.exists())
                                    if (!downloadContainerFinalPosition.delete())
                                        Log.w(TAG_SECURE_FACTORY, "Issue while deleting " + downloadedContainerFinalPath);

                                if (downloadedContainer.renameTo(downloadContainerFinalPosition)) {

                                    // Successful renaming..
                                    // It is necessary to replace the current web-like singleDexPath to access the resource with the new local version.
                                    finalDexPathStringBuilder.append(downloadedContainerFinalPath).append(File.pathSeparator);
                                    Log.d(TAG_SECURE_FACTORY, "Dex Path has been modified into: " + finalDexPathStringBuilder);

                                    // It is also relevant to add this resource to the Log file of the cached remote containers.
                                    remoteContainersCacheLogger.addCachedEntryToLog(currentSingleDexPathAsURL, containerDigest + extension);
                                } else {
                                    // Renaming operation failed..
                                    // Erase downloaded container.
                                    if (!downloadedContainer.delete())
                                        Log.w(TAG_SECURE_FACTORY, "Issue while deleting " + downloadedContainerPath);
                                }

                            }
                        }
                    }
                } catch (MalformedURLException e) {
                    Log.d(
                            TAG_SECURE_FACTORY,
                            "The provided singleDexPath " + singleDexPath + " is not a valid remote URL");
                }
			}
			else {
				
				//if (performLazyEvaluation) {

					// In lazy evaluation it is not required to import the container on external
					// storage into the library private directory for containers.
					// So simply copy current singleDexPath into the final dex singleDexPath list
					//finalDexPathStringBuilder.append(singleDexPath + File.pathSeparator);
				//}
				//else {
					
				//}
				
				// On the other hand when the developer provides a local URI for the container 
				// SecureLoaderFactory has to import this file into an application private folder on the device.
				
				// At first compute the digest on the provided local container.
				String encodedContainerDigest = null;
				
				// If a container exists on the device storage, compute its digest.
				if (new File(singleDexPath).exists()) encodedContainerDigest = computeDigestFromFilePath(singleDexPath);
				
				// Take this branch if the digest was correctly computed on the container..
				if (encodedContainerDigest != null) {
					
					// Compute the extension of the file.
					int extensionIndex = singleDexPath.lastIndexOf(".");
					String extension = singleDexPath.substring(extensionIndex);
					
					// Check if a file whose name is "encodedContainerDigest.(jar/apk)" is already present in
					// the cached certificate folder.					
					File[] matchingContainerArray = importedContainerDir.listFiles(new FileFilterByNameMatch(encodedContainerDigest, extension));
					
					if (matchingContainerArray != null && matchingContainerArray.length > 0) {
						
						// A cached version of the container already exists.
						// So simply use that cached version
						finalDexPathStringBuilder.append(matchingContainerArray[0].getAbsolutePath()).append(File.pathSeparator);
					}
					else {
						
						// No cached copy of the container in the directory.
						// So it is necessary to import the container into the application folder
						// before using it.
						InputStream inStream = null;
						OutputStream outStream = null;
						String cachedContainerPath = importedContainerDir.getAbsolutePath() + File.separator + encodedContainerDigest + extension;
						
						try {
							
							inStream = new FileInputStream(singleDexPath);
							outStream = new FileOutputStream(cachedContainerPath);
							
							byte[] buf = new byte[8192];
						    int length;
						    
						    // Copying the external container into the application
						    // private folder.
						    while ((length = inStream.read(buf)) > 0) {
						    	outStream.write(buf, 0, length);
						    }
							
						    // In the end add the internal singleDexPath of the container
						    finalDexPathStringBuilder.append(cachedContainerPath).append(File.pathSeparator);
						    
						} catch (FileNotFoundException e) {
							Log.w(TAG_SECURE_FACTORY, "Problem in locating container to import in the application private folder!");
						} catch (IOException e) {
							Log.w(TAG_SECURE_FACTORY, "Problem while importing a local container into the application private folder!");
						} finally {
							
							try {
								
								if (inStream != null) {
									inStream.close();
								}
								if (outStream != null) {
									outStream.close();
								}
							
							} catch (IOException e) {
								Log.w(TAG_SECURE_FACTORY, "Issue in closing file streams while importing a container!");
							}
						}	
					}
				}
			}
		}
		
		// Finally remove the last unnecessary separator from finalDexPathStringBuilder (if finalDexPathStringBuilder has at least one path inside)
		if (finalDexPathStringBuilder.lastIndexOf(File.pathSeparator) != -1)
			finalDexPathStringBuilder.deleteCharAt(finalDexPathStringBuilder.lastIndexOf(File.pathSeparator));
		
		// Finalize the CacheLogger object and update the helper file on the device
		remoteContainersCacheLogger.finalizeLog();
		
		// Now the location of the final loaded classes is created.
		// Since it is assumed that the developer do not care where
		// exactly the dex classes will be stored, an application-private, 
		// writable directory is created ad hoc.
		
		File dexOutputDir = contextWrapper.getDir("dex_classes", ContextWrapper.MODE_PRIVATE);
		
		Log.d(TAG_SECURE_FACTORY, "Dex Output Dir has been mounted at: " + dexOutputDir.getAbsolutePath());
		
		// Up to now libraryPath is not checked and left untouched..
		// This is not necessary a bad choice..
		
		// Sanitize fields in packageNameToCertificateMap:
		// - Check the syntax of packages names (only not empty extractedDexPathStrings divided by single separator char)
		// - Enforce that all the certificates URLs in the map can be parsed and use HTTPS as their protocol
		Map<String, URL> sanitizedPackageNameToCertificateMap =
                sanitizePackageNameToCertificateMap(packageNameToCertificateMap);
		
		// Initialize SecureDexClassLoader instance
		SecureDexClassLoader mSecureDexClassLoader = new SecureDexClassLoader(
                finalDexPathStringBuilder.toString(),
				dexOutputDir.getAbsolutePath(),
				libraryPath,
				parent,
                contextWrapper,
				performLazyEvaluation);
		
		// Provide packageNameToCertificateMap to mSecureDexClassLoader..
        mSecureDexClassLoader.setCertificateLocationMap(sanitizedPackageNameToCertificateMap);
		
		return mSecureDexClassLoader;
	}

    // Given the path of a file this function returns the encoded base 64 of SHA-1 digest of the file.
	private String computeDigestFromFilePath(String filePath) {
		
		FileInputStream inStream = null;
		String digestString = null;
		
		try {
			// A stream used to parse the bytes of the file
			inStream = new FileInputStream(filePath);
			
			byte[] buffer = new byte[8192];
		    int length;
		    while( (length = inStream.read(buffer)) != -1 ) {
		    	// File is loaded by considering chunks of it..
		    	messageDigest.update(buffer, 0, length);
		    }
		    // The digest is finally computed..
		    byte[] digestBytes = messageDigest.digest();
		    
		    // ..and translated into a human readable string through Base64 encoding (Url safe).
		    // Also remove the last /n part of the string
		    digestString = Base64.encodeToString(digestBytes, Base64.URL_SAFE);
		    digestString = digestString.replace(System.getProperty("line.separator"), "").replace("\r", "");
		    
		} catch (FileNotFoundException e) {
			Log.w(TAG_SECURE_FACTORY, "No file found at " + filePath);
		} catch (IOException e) {
			Log.w(TAG_SECURE_FACTORY, "Something went wrong while calculating the digest!");
		} finally {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
					Log.w(TAG_SECURE_FACTORY, "Issue while closing file stream in message digest computation!");
				}
			}
		}
		
		// Finally return the digest string..
		return digestString;
	}

	private Map<String, URL> sanitizePackageNameToCertificateMap(Map<String, URL> packageNameToCertificateMap) {
		
		if (packageNameToCertificateMap == null || packageNameToCertificateMap.isEmpty()) return null;
		
		// Copy the initial map and start validating it..
		Map<String, URL> sanitizedPackageNameToCertificateMap = new LinkedHashMap<String, URL>(packageNameToCertificateMap);
		
		// Retrieves all the package names (keys of the map)
		Iterator<String> packageNamesIterator = sanitizedPackageNameToCertificateMap.keySet().iterator();
		
		while(packageNamesIterator.hasNext()) {
			
			String currentPackageName = packageNamesIterator.next();
			String[] packStrings = currentPackageName.split("\\.");
			boolean isValidPackageName = true;
			boolean removeThisPackageName = false;
			
			for (String packString : packStrings) {
				
				// Heuristic: all the subfields should contain at least one char..
				if (packString.isEmpty())
					isValidPackageName = false;
			}
			
			// Package names should not be too general..
			// At least two subnames dot separated.
			// Example: "com" is rejected, while "com.polimi" is not.
			if (packStrings.length < 2)
				removeThisPackageName = true;
			
			if (isValidPackageName) {
				
				// Check that the certificate location is a valid URL
				// and its protocol is HTTPS
				URL certificateURL;
				try {
					//String certificateURLString = sanitizedPackageNameToCertificateMap.get(currentPackageName);
					//certificateURL = new URL(certificateURLString);
					certificateURL = sanitizedPackageNameToCertificateMap.get(currentPackageName);
					
					// Check that the certificate URL is not null..
					if (certificateURL != null) {
						
						if (certificateURL.getProtocol().equals("http")) {
							// In this case enforce HTTPS protocol
							// sanitizedPackageNameToCertificateMap.put(currentPackageName, new URL(certificateURL.toString().replace("http", "https")));
							sanitizedPackageNameToCertificateMap.put(currentPackageName, new URL("https", certificateURL.getHost(), certificateURL.getPort(), certificateURL.getFile()));
						}
						else {
							if (!certificateURL.getProtocol().equals("https")) {
								// If the certificate URL protocol is different from HTTPS
								// or HTTP, this entry is not valid
								removeThisPackageName = true;
							}
						}
					}
					
					// If the certificate URL is null no action is performed here.
					// Reverting package name to obtain a valid URL will be performed in SecureDexClassLoader.
					
				} catch (MalformedURLException e) {
					removeThisPackageName = true;
				}
			} else 
				removeThisPackageName = true;
			
			if (removeThisPackageName) {

				// Remove invalid entry from the map (removing from the iterator is enough..)
				packageNamesIterator.remove();
				// sanitizedPackageNameToCertificateMap.remove(currentPackageName);
			}
		}
		
		return sanitizedPackageNameToCertificateMap;
	}

	private String downloadContainerIntoFolder(String urlPath, File resOutputDir) {
		
		// Precondition check on URL path variable..
		if (urlPath == null) return null;
		
		// Precondition check on the output local folder..
		if (resOutputDir == null || !resOutputDir.exists()) return null;
		if (!resOutputDir.isDirectory() || !resOutputDir.canRead() || !resOutputDir.canWrite()) return null;
		
		URL url;
		try {
			url = new URL(urlPath);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
		
		if (!url.getProtocol().equals("http") && !url.getProtocol().equals("https")) return null;
		
		// Check whether the selected resource is not empty
		int finalSeparatorIndex = url.getPath().lastIndexOf("/");
		String containerName = url.getFile().substring(finalSeparatorIndex);
		
		if (containerName == null || containerName.isEmpty()) return null;
		
		// Check whether the selected resource is a container (jar or apk)
		int containerExtensionIndex = containerName.lastIndexOf(".");
		String containerExtension = null;
		
		if (containerExtensionIndex != -1) {

            containerExtension = containerName.substring(containerExtensionIndex);
			if (!containerExtension.equals(".jar") && !containerExtension.equals(".apk"))
                return null;
		}
		
		// The new file name is fixed after having checked that its file name
		// is unique.
		/* File checkFile = new File(resOutputDir.getAbsolutePath() + containerName);
		String finalContainerName;
		
		if (checkFile.exists()) {
		
			int currentIndex = 0;
		
			do {
				currentIndex ++;
				finalContainerName = containerName.substring(0, containerExtensionIndex) + currentIndex + extension;
				checkFile = new File(resOutputDir.getAbsolutePath() + finalContainerName);
					
			} while (checkFile.exists());
		}
		else {
			finalContainerName = containerName;
		} */

        // Check that no file is present at this location.
		File checkFile = new File(resOutputDir.getAbsolutePath() + containerName);
		
		// In case, just delete the old file..
		if (checkFile.exists())
			checkFile.delete();
		
		// Finally the container file can be downloaded from the URL
		// and stored in the local folder
		String localContainerPath = resOutputDir.getAbsolutePath() + containerName;
		
		// Redirect may be allowed here while downloading a remote container..
		boolean isDownloadSuccessful = fileDownloader.downloadRemoteResource(url, localContainerPath, true);
		
		if (isDownloadSuccessful) {
			
			// If this branch is reached, the download 
			// worked properly and the path of the output
			// file container is returned.
			if (containerExtension == null) {

				// In such a situation, try to identify the extension of the downloaded file
				Optional<String> retrievedFileExtension = fileDownloader.getDownloadedFileExtension();
				
				// Check that an extension was found and it is a suitable one..
				if (retrievedFileExtension.isPresent() &&
                        (retrievedFileExtension.get().equals(".jar") || retrievedFileExtension.get().equals(".apk"))) {

					// In such a case rename the previous file by adding the extension
					File containerToRename = new File(localContainerPath);
					File finalContainerWithExtension = new File(localContainerPath + retrievedFileExtension);
					
					if (finalContainerWithExtension.exists())
						if (!finalContainerWithExtension.delete())
							Log.w(TAG_SECURE_FACTORY, "Issue while deleting " + finalContainerWithExtension);
					
					if (!containerToRename.renameTo(finalContainerWithExtension)) {
						
						// Renaming operation failed..
						// Erase downloaded container.
						if (!containerToRename.delete())
							Log.w(TAG_SECURE_FACTORY, "Issue while deleting " + localContainerPath);
						
						return null;
					}
						
					// Return the local path to the renamed container.
					return localContainerPath + retrievedFileExtension;
				}
			} else {
				
				// An extension is already present so just return the path..
				return localContainerPath;
			}
		}
		
		// Return null if any of the download
		// steps failed.
		return null;
	}
}
