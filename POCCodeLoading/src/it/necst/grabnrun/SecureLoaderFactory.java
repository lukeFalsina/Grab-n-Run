package it.necst.grabnrun;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import android.content.ContextWrapper;
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
	
	/**
	 * Creates a {@code SecureLoaderFactory} used to check and generate instances 
	 * from secure dynamic code loader classes.
	 * 
	 * It requires a {@link ContextWrapper} (i.e. the launching activity) which 
	 * should be used to manage and retrieve internal directories 
	 * of the application.
	 * 
	 * @param parentContentWrapper
	 *  The content wrapper coming from the launching Activity
	 */
	public SecureLoaderFactory(ContextWrapper parentContentWrapper) {
	
		mContextWrapper = parentContentWrapper;
	}
	
	/**
	 * Creates a {@link SecureDexClassLoader} that finds interpreted and native code in a secure location
	 * by enforcing the use of https for remote location provided in dexPath.
	 * Interpreted classes are found in a set of DEX files contained in Jar or Apk files and 
	 * stored into an application-private, writable directory.
	 * 
	 * Note that this method return null if no matching Jar or Apk file is found at the
	 * provided dexPath parameter; otherwise a SecureDexClassLoader instance is returned.
	 * 
	 * @param dexPath
	 *  the list of jar/apk files containing classes and resources
	 * @param libraryPath
	 *  the list of directories containing native libraries; it may be null
	 * @param parent
	 *  the parent class loader
	 * @return secureDexClassLoader
	 */
	public SecureDexClassLoader createDexClassLoader( String dexPath, String libraryPath, ClassLoader parent) {
		
		String finalDexPath = dexPath;
		
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
		
		// Evaluate incoming paths. If one of those starts with http or https
		// retrieve the related resources through a download and import it 
		// into an internal application private directory.
		String[] strings = finalDexPath.split(Pattern.quote(File.pathSeparator));
		
		File resDownloadDir = null;
		boolean isResourceFolderInitialized = false;
		
		for (String path : strings) {
			
			if (path.startsWith("http://") || path.startsWith("https://")) {
				
				// A new resource should be retrieved from the web..
				// Check whether the final directory for downloaded resources
				// has been already initialized
				if (!isResourceFolderInitialized) {
					
					// TODO Policy for dismissing this folder and its contents???
					resDownloadDir = mContextWrapper.getDir("downloaded_res", ContextWrapper.MODE_PRIVATE);
					Log.i(TAG_SECURE_FACTORY, "Download Resource Dir has been mounted at: " + resDownloadDir.getAbsolutePath());
					isResourceFolderInitialized = true;
				}
				
				String downloadedContainerPath = downloadContainerIntoFolder(path, resDownloadDir);
				
				if (downloadedContainerPath != null) {
					
					// In such a case the download was successful and so
					// it is necessary to replace the older web path to access the 
					// resource with the new local one.
					finalDexPath.replaceFirst(path, downloadedContainerPath);
					Log.i(TAG_SECURE_FACTORY, "Dex Path has been modified into: " + finalDexPath);
				}
			}
		}
		
		// Now the location of the final loaded classes is created.
		// Since it is assumed that the developer do not care where
		// exactly the dex classes will be stored, an application-private, 
		// writable directory is created ad hoc.
		
		File dexOutputDir = mContextWrapper.getDir("dex_classes", ContextWrapper.MODE_PRIVATE);
		
		Log.i(TAG_SECURE_FACTORY, "Dex Output Dir has been mounted at: " + dexOutputDir.getAbsolutePath());
		
		// TODO: Discuss about this aspect with Federico..
		// Up to now libraryPath is not checked and left untouched..
		
		SecureDexClassLoader mSecureDexClassLoader = new SecureDexClassLoader(	finalDexPath,
																				dexOutputDir.getAbsolutePath(),
																				libraryPath,
																				parent,
																				mContextWrapper);
		
		return mSecureDexClassLoader;
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
		
		if (url.getProtocol()!= "http" && url.getProtocol()!= "https") return null;
		
		// Check whether the selected resource is not empty
		int finalSeparatorIndex = url.getPath().lastIndexOf("/");
		String containerName = url.getPath().substring(finalSeparatorIndex);
		
		if (containerName == null || containerName.isEmpty()) return null;
		
		// Check whether the selected resource is a container (jar or apk)
		int extensionIndex = containerName.lastIndexOf(".");
		String extension = containerName.substring(extensionIndex);
		if (!extension.equals(".jar") && !extension.equals(".apk")) return null;
		
		// The new file name is fixed after having checked that its 
		// is unique.
		File checkFile = new File(resOutputDir.getAbsolutePath() + "/" + containerName);
		String finalContainerName;
		
		if (checkFile.exists()) {
		
			int currentIndex = 0;
		
			do {
				currentIndex ++;
				finalContainerName = containerName.substring(0, extensionIndex) + currentIndex + extension;
				checkFile = new File(resOutputDir.getAbsolutePath()+ "/" + finalContainerName);
					
			} while (checkFile.exists());
		}
		else {
			finalContainerName = containerName;
		}
		
		// Finally the container file can be downloaded from the URL
		// and stored in the local folder
		URLConnection urlConnection = null;
		InputStream inputStream = null;
		OutputStream outputStream = null;
		String localContainerPath = resOutputDir.getAbsolutePath()+ "/" + finalContainerName;
		
		try {
			
			if (url.getProtocol()!= "https") {
				// HTTPS protocol
				urlConnection = (HttpsURLConnection) url.openConnection();
			}
			else {
				// HTTP protocol
				urlConnection = (HttpURLConnection) url.openConnection();
			}
			urlConnection.connect();
			
			Log.i(TAG_SECURE_FACTORY, "A connection was set up: " + url.toString());
				
			inputStream = urlConnection.getInputStream();
			outputStream = new FileOutputStream(localContainerPath);
				
			int read = 0;
			byte[] bytes = new byte[1024];
				
			while ((read = inputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
				
			Log.i(TAG_SECURE_FACTORY, "Download complete. Container Path: " + localContainerPath);
				
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
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
			if (urlConnection != null)	((HttpURLConnection) urlConnection).disconnect();
			
			Log.i(TAG_SECURE_FACTORY, "Clean up of all pending streams completed.");
		}
			
		// If this part of the method is reached, the download 
		// procedure worked properly and the path of the output
		// file container is returned.
		return localContainerPath;
	}
}
