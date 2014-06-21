package it.necst.grabnrun;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;

import android.content.ContextWrapper;
import android.util.Log;
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
 * is retrieved, as a first implementation, by simply reverting the 
 * first two worlds of the package name of the loaded class and then 
 * by adding each following world in the same order and separated by 
 * a slash "/".
 * 
 * Example:
 * Class name = it.necst.grabnrun.example.TestClassImpl
 * Constructed URL = https://necst.it/grabnrun/example
 * Final certificate location = https://necst.it/grabnrun/example/certificate.pem
 * 
 * A request is pointed to the final certificate location and if 
 * the file is found, it is imported in the local private 
 * application directory.
 * 
 * Please note that in the current implementation certificate must be 
 * saved at the described location as "certificate.pem", it must 
 * fit all the requirements of a standard X.509 certificate, it must 
 * be valid and of course it must have been used to sign the Jar or 
 * Apk, which contains the classes to be loaded.
 * 
 * If any of these previous requirements is violated no class is loaded 
 * and this class immediately returns without executing any class code 
 * loading operation.
 * 
 * @author Luca Falsina
 */
public class SecureDexClassLoader extends DexClassLoader {
	
	// Unique identifier used for Log entries
	private static final String TAG_SECURE_DEX_CLASS_LOADER = SecureDexClassLoader.class.getSimpleName();
	
	private File certificateFolder;
	
	SecureDexClassLoader(	String dexPath, String optimizedDirectory,
							String libraryPath, ClassLoader parent,
							ContextWrapper parentContextWrapper) {
		super(dexPath, optimizedDirectory, libraryPath, parent);
		
		certificateFolder = parentContextWrapper.getDir("valid_certs", ContextWrapper.MODE_PRIVATE);		
	}

	/* (non-Javadoc)
	 * @see java.lang.ClassLoader#loadClass(java.lang.String)
	 */
	@Override
	public Class<?> loadClass(String className) throws ClassNotFoundException {
		
		// Instantiate a certificate object used to check 
		// the signature of .apk or .jar container
		X509Certificate verifiedCertificate = null;
		
		// TODO Decide the policy to apply with cached certificates
		// i.e. Always keep them, cancel when the VM is terminated..
		
		// At first the name of the certificate possibly stored 
		// in the application private directory is generated
		// from the package name.
		String packageName = className.substring(0, className.lastIndexOf('.'));
		
		// Now the procedure looks for the correct certificate and 
		// if a match is found, it will import it.
		File[] certMatchingFiles = certificateFolder.listFiles(new CertFileFilter(packageName));
		
		if (certMatchingFiles != null && certMatchingFiles.length != 0) {
			
			// Import just the first matching certificate from file..
			InputStream inStream = null;
			
			try {
			
				inStream = new FileInputStream(certMatchingFiles[0]);
			    CertificateFactory cf = CertificateFactory.getInstance("X.509");
			    verifiedCertificate = (X509Certificate) cf.generateCertificate(inStream);
			    
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CertificateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
			     if (inStream != null) {
			         try {
						inStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			     }
			}
			
			// If the certificate was correctly created, check 
			// if it's currently valid
			if (verifiedCertificate != null) {
				
				try {
					verifiedCertificate.checkValidity();
				} catch (CertificateExpiredException
						| CertificateNotYetValidException e) {
					// This certificate is no longer valid!
					// Discard it and erase the copy of 
					// the file on the device memory
					verifiedCertificate = null;
					String certFileToErase = certMatchingFiles[0].getName();
					if (certMatchingFiles[0].delete()) {
						Log.i(TAG_SECURE_DEX_CLASS_LOADER, "Expired certificate " + certFileToErase + " has been erased.");
					}else {
						Log.w(TAG_SECURE_DEX_CLASS_LOADER, "Problems while deleting expired certificate " + certFileToErase + "!");
					}
				}
			}
			
		}
		
		if (verifiedCertificate == null) {
			
			// No matching or not expired certificate was found 
			// locally and so it's necessary to download the 
			// certificate through an https request.
			
			// TODO Missing Implementation..
			
		}
		
		if (verifiedCertificate != null) {
		
			// We were able to get a valid certificate either
			// from the local cache directory or by downloading
			// it now we would like to check if this certificate
			// was used to sign the class to be loaded.
			
			// TODO Missing Implementation..
			
			return super.loadClass(className);
		}
		
		// TODO Think better about this scenario..
		// Maybe instead a CertificateException should be thrown..
		// But than the signature of this method becomes different 
		// from the one of the parent..
		return null;
	}

}
