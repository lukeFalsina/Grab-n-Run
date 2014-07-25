package it.necst.grabnrun;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;
//import android.content.Context;
import android.content.ContextWrapper;
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
	static FileDownloader FileDownloader;
	
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
		FileDownloader = FileDownloader.getInstance(mContextWrapper);
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
	 *  the list of directories containing native libraries; it may be null
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
			
			Log.i(TAG_SECURE_FACTORY, "Dex Path has been modified to: " + finalDexPath);
		} */
		
		// Necessary workaround to avoid remote URL being split 
		// in a wrong way..
		String tempPath = dexPath.replaceAll("http://", "http//");
		tempPath = tempPath.replaceAll("https://", "https//");
		
		// Evaluate incoming paths. If one of those starts with http or https
		// retrieve the related resources through a download and import it 
		// into an internal application private directory.
		String[] strings = tempPath.split(Pattern.quote(File.pathSeparator));
		
		File resDownloadDir = null;
		boolean isResourceFolderInitialized = false;
		
		for (String path : strings) {
			
			if (path.startsWith("http//") || path.startsWith("https//")) {
				
				// Used to fix previous workaround on remote URL..
				String fixedPath = path.replaceAll("http//", "http://");
				fixedPath = fixedPath.replaceAll("https//", "https://");
				
				// A new resource should be retrieved from the web..
				// Check whether the final directory for downloaded resources
				// has been already initialized
				if (!isResourceFolderInitialized) {
					
					// TODO Policy for dismissing this folder and its contents???
					resDownloadDir = mContextWrapper.getDir("downloaded_res", ContextWrapper.MODE_PRIVATE);
					Log.d(TAG_SECURE_FACTORY, "Download Resource Dir has been mounted at: " + resDownloadDir.getAbsolutePath());
					isResourceFolderInitialized = true;
				}
				
				String downloadedContainerPath = downloadContainerIntoFolder(fixedPath, resDownloadDir);
				
				if (downloadedContainerPath != null) {
					
					// In such a case the download was successful and so
					// it is necessary to replace the current web-like path 
					// to access the resource with the new local version.
					finalDexPath.append(downloadedContainerPath + File.pathSeparator);
					Log.d(TAG_SECURE_FACTORY, "Dex Path has been modified into: " + finalDexPath);
				}
			}
			else {
				
				// Simply copy current path into the final dex path list
				finalDexPath.append(path + File.pathSeparator);
			}
		}
		
		// Finally remove the last unnecessary separator from finalDexPath
		finalDexPath.deleteCharAt(finalDexPath.lastIndexOf(File.pathSeparator));
		
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
																				mContextWrapper);
		
		// Provide packageNameToCertificateMap to mSecureDexClassLoader..
		if (mSecureDexClassLoader != null) mSecureDexClassLoader.setCertificateLocationMap(santiziedPackageNameToCertificateMap);
		
		return mSecureDexClassLoader;
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
				// TODO Check whether this call affects also the 
				// map and not just the iterator..
				
				// Remove invalid entry from the map
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
		
		boolean isDownloadSuccessful = FileDownloader.downloadRemoteUrl(url, localContainerPath);
		
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
