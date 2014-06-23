package it.necst.grabnrun;

import java.io.File;
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
		
		if (dexPath.contains("http://")) {
			// This dexPath must be forced to use https (avoid MITM attacks)..
			finalDexPath = finalDexPath.replace("http://", "https://");
			
			Log.i(TAG_SECURE_FACTORY, "Dex Path has been modified to: " + finalDexPath);
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
}
