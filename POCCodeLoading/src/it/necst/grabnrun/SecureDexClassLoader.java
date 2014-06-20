package it.necst.grabnrun;

import dalvik.system.DexClassLoader;

/**
 * A class loader that extends the default one provided by the Android
 * system and it is used to load classes from Jar and Apk files 
 * containing a classes.dex entry in a secure way.
 * 
 * In order to instantiate this class a call to the method createDexClassLoader
 * from a SecureLoaderFactory object must be performed.
 * 
 * SecureDexClassLoader ensures integrity of loaded external remote 
 * classes by comparing them with the developer certificate, which
 * is retrieved, as a first implementation, by reverting the 
 * package name of the loaded class and downloading the 
 * certificate at that location.
 * 
 * Please note that in the current implementation certificate must be 
 * saved at the described location as "certificate.pem", it must 
 * fit all the requirements of a standard X.509 certificate, it must 
 * be valid and it must have been used to sign the Jar or Apk, which
 * contains the classes to be loaded.
 * 
 * If any of these previous requirements is violated no class is loaded 
 * and this class immediately returns.
 * 
 * @author Luca Falsina
 */
public class SecureDexClassLoader extends DexClassLoader {

	SecureDexClassLoader(	String dexPath, String optimizedDirectory,
							String libraryPath, ClassLoader parent) {
		super(dexPath, optimizedDirectory, libraryPath, parent);
	}

	/* (non-Javadoc)
	 * @see java.lang.ClassLoader#loadClass(java.lang.String)
	 */
	@Override
	public Class<?> loadClass(String className) throws ClassNotFoundException {
		// TODO Auto-generated method stub
		return super.loadClass(className);
	}

}
