package it.necst.grabnrun;

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
import java.util.Map;
import java.util.regex.Pattern;
//import android.content.Context;
import android.content.ContextWrapper;
import android.util.Base64;
//import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
import android.util.Log;

/**
 * A Factory class that generates instances of classes used to 
 * retrieve dynamic code in a secure way at run time.
 * 
 * @author Luca Falsina
 */
public class SecureLoaderFactory {
	
	// Unique identifier used for Log entries
	private static final String TAG_SECURE_FACTORY = SecureLoaderFactory.class.getSimpleName();

	private ContextWrapper mContextWrapper;
	
	// Objects used to check availability of Internet connection
	//private ConnectivityManager mConnectivityManager;
	//private NetworkInfo activeNetworkInfo;
	
	// Object used for retrieving file from remote URL
	private FileDownloader mFileDownloader;
	
	// Final name of the folder user to store imported containers (both coming from remote or local resources)
	static final String CONT_IMPORT_DIR = "imported_cont";
	
	// Used to compute the finger print on different containers 
	// in order to check which has been already cached.
	private MessageDigest messageDigest;
	
	/**
	 * When a remote container URL is encountered, this field specifies the time interval, 
	 * expressed in days, before which a local copy of a remote container, stored in an 
	 * application-private directory, will be considered fresh and so cached.
	 * 
	 * On the other hand, all of those containers, whose life time is greater than this
	 * field value, will be immediately erased from the device storage in stead of being cached.
	 */
	public static int DAYS_BEFORE_CACHE_EXPIRATION = 5;
	
	/**
	 * Creates a {@code SecureLoaderFactory} used to check and generate instances 
	 * from secure dynamic code loader classes.
	 * 
	 * It requires a {@link ContextWrapper} (i.e. the launching activity) which 
	 * should be used to manage and retrieve internal directories 
	 * of the application.
	 * 
	 * @param parentContextWrapper
	 *  The content wrapper coming from the launching Activity
	 */
	public SecureLoaderFactory(ContextWrapper parentContextWrapper) {
	
		mContextWrapper = parentContextWrapper;
		//mConnectivityManager = (ConnectivityManager) parentContextWrapper.getSystemService(Context.CONNECTIVITY_SERVICE);
		mFileDownloader = new FileDownloader(mContextWrapper);
		
		try {
			messageDigest = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG_SECURE_FACTORY, "Wrong algorithm choice for message digest!");
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a {@link SecureDexClassLoader} that finds interpreted and native code in a set of
	 * provided locations (either local or remote via HTTP or HTTPS) in dexPath.
	 * Interpreted classes are found in a set of DEX files contained in Jar or Apk files and 
	 * stored into an application-private, writable directory.
	 * 
	 * Before executing one of these classes the signature of the target class is 
	 * verified against the certificate associated with its package name.
	 * Certificates location are provided by filling appropriately {@link packageNameToCertificateMap}};
	 * each package name must be linked with the remote location of the certificate that
	 * should be used to validate all the classes of that package. It's important 
	 * that each one of these locations uses HTTPS as its protocol; otherwise this 
	 * choice will be enforced!
	 * If a class package name do not match any of the provided entries in the map, 
	 * certificate location will be constructed by simply reverting package name and 
	 * transforming it into a web-based URL using HTTPS.
	 * 
	 * Note that this method returns null if no matching Jar or Apk file is found at the
	 * provided dexPath parameter; otherwise a {@link SecureDexClassLoader} instance is returned.
	 * 
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
	 *  the list of directories containing native libraries; it may be null.
	 * @param packageNameToCertificateMap
	 *  a map that couples each package name to a URL which contains the certificate
	 *  that must be used to validate all the classes that belong to that package
	 *  before launching them at run time.
	 * @param parent
	 *  the parent class loader
	 * @return secureDexClassLoader
	 */
	public SecureDexClassLoader createDexClassLoader(	String dexPath, 
														String libraryPath, 
														Map<String, String> packageNameToCertificateMap, 
														ClassLoader parent) {
		
		// The default behavior by now is using LAZY evaluation.
		// In order to change it, simply modify the last boolean parameter from "true" to "false".
		return createDexClassLoader(dexPath, libraryPath, packageNameToCertificateMap, parent, true);
		
	}
	
	/**
	 * This method returns a SecureDexClassLoader instance in the same way as it is 
	 * explained in the previous createDexClassLoader() method.
	 * 
	 * In addition it is possible to specify the mode in which the signature verification
	 * process will be carried.
	 * 
	 * In particular by setting the performLazyEvaluation parameter on true, 
	 * a lazy verification process will be chosen. This means that the signature
	 * of the single container associated with the target class will be evaluated 
	 * only when the loadClass() method will be invoked on {@link SecureDexClassLoader} 
	 * object. If this check succeeds the target class will be loaded.
	 * 
	 * On the other hand, by setting the parameter performLazyEvaluation to false 
	 * an eager evaluation will be carried out. This means that before returning this 
	 * object, the signature verification procedure will be carried out on all the 
	 * provided containers and all of those that do not succeed in the process will
	 * be blocked from loading their classes in the following loadClass() calls.
	 * 
	 * The use of one mode in stead of the other is merely a performance-related choice.
	 * If you do not care that much about it, just invoke the overloaded method 
	 * createDexClassLoader() which does not require to provide the performLazyEvaluation
	 * parameter. Otherwise as a general guideline, prefer the lazy mode whenever you
	 * need to load classes from many and heavy containers; use the eager mode when
	 * the number of involved containers is small and so the penalty for evaluating all
	 * of them immediately in one shot won't be so high. 
	 * 
	 * @param dexPath
	 *  the list of jar/apk files containing classes and resources; these paths could
	 *  be either local URLs pointing to a location in the device or URLs that links
	 *  to a resource stored in the web via HTTP/HTTPS. In the latter case, if Internet
	 *  connectivity is available, the resource will be imported in a private-application 
	 *  directory before being used.
	 * @param libraryPath
	 *  the list of directories containing native libraries; it may be null.
	 * @param packageNameToCertificateMap
	 *  a map that couples each package name to a URL which contains the certificate
	 *  that must be used to validate all the classes that belong to that package
	 *  before launching them at run time.
	 * @param parent
	 *  the parent class loader
	 * @param performLazyEvaluation
	 *  the mode in which the verification will be handled. True for lazy verification;
	 *  false for the eager one.
	 * @return secureDexClassLoader
	 */
	public SecureDexClassLoader createDexClassLoader(	String dexPath, 
														String libraryPath, 
														Map<String, String> packageNameToCertificateMap, 
														ClassLoader parent,
														boolean performLazyEvaluation) {
		
		// Final dex path list will be constructed incrementally
		// while scanning dexPath variable
		StringBuilder finalDexPath = new StringBuilder();
		
		/*
		 * After discussion it results useless to force https while 
		 * downloading apk/jar files (MITM may even be allowed here).
		 * What we really need to enforce is retrieving the matching 
		 * certificate securely (so if it's downloaded, use https).
		if (dexPath.contains("http://")) {
			// This dexPath must be forced to use https (avoid MITM attacks)..
			finalDexPath = finalDexPath.replace("http://", "https://");
			
			Log.d(TAG_SECURE_FACTORY, "Dex Path has been modified to: " + finalDexPath);
		} */
		
		// Necessary workaround to avoid remote URL being split 
		// in a wrong way..
		String tempPath = dexPath.replaceAll("http://", "http//");
		tempPath = tempPath.replaceAll("https://", "https//");
		
		// Evaluate incoming paths. If one of those starts with http or https
		// retrieve the related resources through a download and import it 
		// into an internal application private directory.
		String[] strings = tempPath.split(Pattern.quote(File.pathSeparator));
		
		// New container resources should be imported or cached from an application private folder..
		// Initialize directory for imported containers.
		File importedContainerDir = mContextWrapper.getDir(CONT_IMPORT_DIR, ContextWrapper.MODE_PRIVATE);
		Log.d(TAG_SECURE_FACTORY, "Download Resource Dir has been mounted at: " + importedContainerDir.getAbsolutePath());
		
		CacheLogger mCacheLogger = new CacheLogger(importedContainerDir.getAbsolutePath(), DAYS_BEFORE_CACHE_EXPIRATION);
		
		for (String path : strings) {
			
			// Compute the extension of the file (Useful for later operations)
			int extensionIndex = path.lastIndexOf(".");
			String extension = path.substring(extensionIndex);
			
			if (path.startsWith("http//") || path.startsWith("https//")) {
				
				// Used to fix previous workaround on remote URL..
				//String fixedPath = path.replaceAll("http//", "http://");
				//fixedPath = fixedPath.replaceAll("https//", "https://");
				String fixedRemotePath;
				if (path.startsWith("http//")) fixedRemotePath = "http:" + path.substring(4);
				else fixedRemotePath = "https:" + path.substring(5);
				
				String cachedContainerFileName = mCacheLogger.checkForCachedEntry(fixedRemotePath);
				
				if (cachedContainerFileName != null) {
					
					// A valid and fresh enough cached copy of the remote container is present
					// on the device storage, so this copy can be used in stead of downloading 
					// the remote container again.
					finalDexPath.append(importedContainerDir.getAbsolutePath() + File.separator + cachedContainerFileName + File.pathSeparator);
					Log.d(TAG_SECURE_FACTORY, "Dex Path has been modified into: " + finalDexPath);
				}
				else {
					
					// No cached copy so it is necessary to download the remote resource from the web.

					//Trace.beginSection("Download Container");
					// Log.i("Profile","[Start]	Download Container: " + System.currentTimeMillis() + " ms.");
					String downloadedContainerPath = downloadContainerIntoFolder(fixedRemotePath, importedContainerDir);
					// Log.i("Profile","[End]	Download Container: " + System.currentTimeMillis() + " ms.");
					//Trace.endSection(); // end of "Download Container" section
					
					if (downloadedContainerPath != null) {
						
						// In such a case the download was successful.
						// Now the downloaded container is renamed according to its finger print.
						String containerDigest = computeDigestFromFilePath(downloadedContainerPath);
						
						File downloadedContainer = new File(downloadedContainerPath);
						
						if (containerDigest == null) {
							
							// Fingerprint computation fails. Delete the resource container and do not add
							// this file to the path
							if (!downloadedContainer.delete())
								Log.w(TAG_SECURE_FACTORY, "Issue while deleting " + downloadedContainerPath);
						}
						else {
							
							// Rename the previous container file according to the containerDigest and its extension.
							String downloadedContainerFinalPath = importedContainerDir.getAbsolutePath() + File.separator + containerDigest + extension;
							
							File downloadContainerFinalPosition = new File(downloadedContainerFinalPath);
							
							if (downloadContainerFinalPosition.exists())
								if (!downloadContainerFinalPosition.delete())
									Log.w(TAG_SECURE_FACTORY, "Issue while deleting " + downloadedContainerFinalPath);
							
							if (downloadedContainer.renameTo(downloadContainerFinalPosition)) {
								
								// Successful renaming..
								// It is necessary to replace the current web-like path to access the resource with the new local version.
								finalDexPath.append(downloadedContainerFinalPath + File.pathSeparator);
								Log.d(TAG_SECURE_FACTORY, "Dex Path has been modified into: " + finalDexPath);
								
								// It is also relevant to add this resource to the Log file of the cached remote containers.
								mCacheLogger.addCachedEntryToLog(fixedRemotePath, containerDigest + extension);
							}
							else {
								// Renaming operation failed..
								// Erase downloaded container.
								if (!downloadedContainer.delete())
									Log.w(TAG_SECURE_FACTORY, "Issue while deleting " + downloadedContainerPath);
							}
							
						}	
					}
				}
				
			}
			else {
				
				//if (performLazyEvaluation) {

					// In lazy evaluation it is not required to import the container on external
					// storage into the library private directory for containers.
					// So simply copy current path into the final dex path list
					//finalDexPath.append(path + File.pathSeparator);
				//}
				//else {
					
				//}
				
				// On the other hand when the developer provides a local URI for the container 
				// SecureLoaderFactory has to import this file into an application private folder on the device.
				
				// At first compute the digest on the provided local container.
				String encodedContainerDigest = null;
				
				// If a container exists on the device storage, compute its digest.
				if (new File(path).exists()) encodedContainerDigest = computeDigestFromFilePath(path);
				
				// Take this branch if the digest was correctly computed on the container..
				if (encodedContainerDigest != null) {
					
					// Check if a file whose name is "encodedContainerDigest.(jar/apk)" is already present in
					// the cached certificate folder.					
					File[] matchingContainerArray = importedContainerDir.listFiles(new FileFilterByName(encodedContainerDigest, extension));
					
					if (matchingContainerArray != null && matchingContainerArray.length > 0) {
						
						// A cached version of the container already exists.
						// So simply use that cached version
						finalDexPath.append(matchingContainerArray[0].getAbsolutePath() + File.pathSeparator);
					}
					else {
						
						// No cached copy of the container in the directory.
						// So it is necessary to import the container into the application folder
						// before using it.
						InputStream inStream = null;
						OutputStream outStream = null;
						String cachedContainerPath = importedContainerDir.getAbsolutePath() + File.separator + encodedContainerDigest + extension;
						
						try {
							
							inStream = new FileInputStream(path);
							outStream = new FileOutputStream(cachedContainerPath);
							
							byte[] buf = new byte[8192];
						    int length;
						    
						    // Copying the external container into the application
						    // private folder.
						    while ((length = inStream.read(buf)) > 0) {
						    	outStream.write(buf, 0, length);
						    }
							
						    // In the end add the internal path of the container
						    finalDexPath.append(cachedContainerPath + File.pathSeparator);
						    
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
		
		// Finally remove the last unnecessary separator from finalDexPath (if finalDexPath has at least one path inside)
		if (finalDexPath.lastIndexOf(File.pathSeparator) != -1)
			finalDexPath.deleteCharAt(finalDexPath.lastIndexOf(File.pathSeparator));
		
		// Finalize the CacheLogger object and update the helper file on the device
		mCacheLogger.finalizeLog();
		
		// Now the location of the final loaded classes is created.
		// Since it is assumed that the developer do not care where
		// exactly the dex classes will be stored, an application-private, 
		// writable directory is created ad hoc.
		
		File dexOutputDir = mContextWrapper.getDir("dex_classes", ContextWrapper.MODE_PRIVATE);
		
		Log.d(TAG_SECURE_FACTORY, "Dex Output Dir has been mounted at: " + dexOutputDir.getAbsolutePath());
		
		// TODO: Discuss about this aspect with Federico..
		// Up to now libraryPath is not checked and left untouched..
		// This is not necessary a bad choice..
		
		// Sanitize fields in packageNameToCertificateMap:
		// - Check the syntax of packages names (only not empty strings divided by single separator char)
		// - Enforce that all the certificates URLs in the map can be parsed and use HTTPS as their protocol
		Map<String, String> santiziedPackageNameToCertificateMap = sanitizePackageNameToCertificateMap(packageNameToCertificateMap);
		
		// Initialize SecureDexClassLoader instance
		SecureDexClassLoader mSecureDexClassLoader = new SecureDexClassLoader(	finalDexPath.toString(),
																				dexOutputDir.getAbsolutePath(),
																				libraryPath,
																				parent,
																				mContextWrapper,
																				performLazyEvaluation);
		
		// Provide packageNameToCertificateMap to mSecureDexClassLoader..
		if (mSecureDexClassLoader != null) mSecureDexClassLoader.setCertificateLocationMap(santiziedPackageNameToCertificateMap);
		
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
		    digestString = new String(Base64.encode(digestBytes, Base64.URL_SAFE));
		    
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

	private Map<String, String> sanitizePackageNameToCertificateMap(Map<String, String> packageNameToCertificateMap) {
		
		if (packageNameToCertificateMap == null || packageNameToCertificateMap.isEmpty()) return null;
		
		// Copy the initial map and start validating it..
		Map<String, String> santiziedPackageNameToCertificateMap = packageNameToCertificateMap;
		
		// Retrieves all the package names (keys of the map)
		Iterator<String> packageNamesIterator = santiziedPackageNameToCertificateMap.keySet().iterator();
		
		while(packageNamesIterator.hasNext()) {
			
			String currentPackageName = packageNamesIterator.next();
			String[] packStrings = currentPackageName.split(".");
			boolean isValidPackageName = true;
			boolean removeThisPackageName = false;
			
			for (String packString : packStrings) {
				
				// Heuristic: all the subfields should contain at least one char..
				if (packString.isEmpty()) isValidPackageName = false;
			}
			
			if (isValidPackageName) {
				
				// Check that the certificate location is a valid URL
				// and its protocol is HTTPS
				URL certificateURL;
				try {
					String certificateURLString = santiziedPackageNameToCertificateMap.get(currentPackageName);
					certificateURL = new URL(certificateURLString);
					
					if (certificateURL.getProtocol().equals("http")) {
						// In this case enforce HTTPS protocol
						santiziedPackageNameToCertificateMap.put(currentPackageName, certificateURLString.replace("http", "https"));
					}
					else {
						if (!certificateURL.getProtocol().equals("https")) {
							// If the certificate URL protocol is different from HTTPS
							// or HTTP, this entry is not valid
							removeThisPackageName = true;
						}
					}
				} catch (MalformedURLException e) {
					removeThisPackageName = true;
				}
			}
			else removeThisPackageName = true;
			
			if (removeThisPackageName) {

				// Remove invalid entry from the map (removing from the iterator is enough..)
				packageNamesIterator.remove();
				// santiziedPackageNameToCertificateMap.remove(currentPackageName);
			}
		}
		
		return santiziedPackageNameToCertificateMap;
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
		int extensionIndex = containerName.lastIndexOf(".");
		String extension = containerName.substring(extensionIndex);
		if (!extension.equals(".jar") && !extension.equals(".apk")) return null;
		
		// The new file name is fixed after having checked that its file name
		// is unique.
		File checkFile = new File(resOutputDir.getAbsolutePath() + containerName);
		String finalContainerName;
		
		if (checkFile.exists()) {
		
			int currentIndex = 0;
		
			do {
				currentIndex ++;
				finalContainerName = containerName.substring(0, extensionIndex) + currentIndex + extension;
				checkFile = new File(resOutputDir.getAbsolutePath() + finalContainerName);
					
			} while (checkFile.exists());
		}
		else {
			finalContainerName = containerName;
		}
		
		// Finally the container file can be downloaded from the URL
		// and stored in the local folder
		String localContainerPath = resOutputDir.getAbsolutePath() + finalContainerName;
		
		boolean isDownloadSuccessful = mFileDownloader.downloadRemoteUrl(url, localContainerPath);
		
		if (isDownloadSuccessful)
			// If this branch is reached, the download 
			// worked properly and the path of the output
			// file container is returned.
			return localContainerPath;
		
		// Return null if any of the download
		// steps failed.
		return null;
	}
}
