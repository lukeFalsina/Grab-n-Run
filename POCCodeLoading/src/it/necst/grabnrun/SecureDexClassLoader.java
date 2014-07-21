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
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.security.auth.x500.X500Principal;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;

/**
 * A class that provides an extension of default {@link DexClassLoader} 
 * provided by the Android system and it is used to load classes 
 * from Jar and Apk files containing a classes.dex entry in a secure way.
 * 
 * In order to instantiate this class a call to the method createDexClassLoader
 * from a SecureLoaderFactory object must be performed.
 * 
 * {@code SecureDexClassLoader} ensures integrity of loaded external remote 
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
	private ConnectivityManager mConnectivityManager;
	private PackageManager mPackageManager;
	private Map<String, String> packageNameToCertificateMap, packageNameToContainerPathMap;
	
	SecureDexClassLoader(	String dexPath, String optimizedDirectory,
							String libraryPath, ClassLoader parent,
							ContextWrapper parentContextWrapper) {
		super(dexPath, optimizedDirectory, libraryPath, parent);
		
		certificateFolder = parentContextWrapper.getDir("valid_certs", ContextWrapper.MODE_PRIVATE);
		mConnectivityManager = (ConnectivityManager) parentContextWrapper.getSystemService(Context.CONNECTIVITY_SERVICE);
		mPackageManager = parentContextWrapper.getPackageManager();
		
		// Map initialization
		packageNameToCertificateMap = null;
		packageNameToContainerPathMap = new HashMap<String, String>();
		
		// Analyze each path in dexPath, find its package name and 
		// populate packageNameToContainerPathMap accordingly
		String[] pathStrings = dexPath.split(Pattern.quote(File.pathSeparator));
		
		for (String currentPath : pathStrings) {
			
			String packageName = getPackageNameFromContainerPath(currentPath);
			
			if (packageName != null) {
				
				// This is a valid entry so it must be added to packageNameToContainerPathMap
				String previousPath = packageNameToContainerPathMap.put(packageName, currentPath);
				
				// If previous path is not null, it means that one of the previous analyzed
				// path had the same package name (this is a possibility for JAR containers..)
				if (previousPath != null) {
					
					// TODO Up to now only a warning message is registered in the logs
					Log.w(	TAG_SECURE_DEX_CLASS_LOADER, "Package Name " + packageName + " is not unique!\n Previous path: " 
							+ previousPath + ";\n New path: " + currentPath + ";" );
				}
			}
		}
	}

	private String getPackageNameFromContainerPath(String containerPath) {
		
		// Check whether the selected resource is a container (jar or apk)
		int extensionIndex = containerPath.lastIndexOf(".");
		String extension = containerPath.substring(extensionIndex);
		
		if (extension.equals(".apk")) {
			
			// APK container case:
			// Use PackageManager to retrieve the package name of the APK container
			return mPackageManager.getPackageArchiveInfo(containerPath, 0).packageName;
		}
			
		if (extension.equals(".jar")) {
				
			// JAR container case:
			// 1. Unzip the JAR container
			// 2. Look for "classes.dex" file and open it
			// 3. Find a valid class entry and retrieve package name from it
				
			// Open classes.dex file
			DexFile classesDexFile = null;
			String packageName;
				
			try {
				classesDexFile = new DexFile(containerPath);
					
				Enumeration<String> classesNames  = classesDexFile.entries();
				String firstClassName = classesNames.nextElement().replaceAll(Pattern.quote(File.separator), ".");
				packageName = firstClassName.substring(0, firstClassName.lastIndexOf('.'));
					
			} catch (IOException e) {
				// No valid package name here..
				return null;
			} finally {
				if (classesDexFile != null) {
			         try {
			        	 classesDexFile.close();
					} catch (IOException e) {
						// Problem while closing this file..
						e.printStackTrace();
					}
			     }
			}
				
			return packageName;				
		}
		
		// Any other file format is not supported so 
		// package name is returned..
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.ClassLoader#loadClass(java.lang.String)
	 */
	@Override
	public Class<?> loadClass(String className) throws ClassNotFoundException {
		
		// A map which links package names and certificate location
		// must be provided before calling this method..
		if (packageNameToCertificateMap == null) return null;
		
		// Instantiate a certificate object used to check 
		// the signature of .apk or .jar container
		X509Certificate verifiedCertificate;
		
		// TODO Decide the policy to apply with cached certificates
		// i.e. Always keep them, cancel when the VM is terminated..
		
		// At first the name of the certificate possibly stored 
		// in the application private directory is generated
		// from the package name.
		String packageName = className.substring(0, className.lastIndexOf('.'));
		
		// At first check if the correct certificate has been 
		// already imported in the application-private certificate directory.
		verifiedCertificate = importCertificateFromAppPrivateDir(packageName);
		
		if (verifiedCertificate == null) {
			
			// No matching certificate or an expired one was found 
			// locally and so it's necessary to download the 
			// certificate through an Https request.
			boolean isCertificateDownloadSuccessful = downloadCertificateRemotelyViaHttps(packageName);
			
			if (isCertificateDownloadSuccessful) {
				
				// Download procedure works fine and the new 
				// certificate should now be in the local folder.
				// So let's try to retrieve it once again..
				verifiedCertificate = importCertificateFromAppPrivateDir(packageName);
			}
		}
		
		if (verifiedCertificate != null) {
				
			// We were able to get a valid certificate either directly
			// from the local cache directory or after having 
			// downloaded it from the web securely.
			// Now it's time to check whether this certificate
			// was used to sign the class to be loaded.
			
			// TODO Need to be tested!!
			// Check whether the certificate used to verify is the one 
			// used by Android in Debug Mode. If so, discard the request
			// since it's not secure.
			String androidDebugModeDN = "C=US,O=Android,CN=Android Debug";
			X500Principal androidDebugModePrincipal = new X500Principal(androidDebugModeDN);
			if (	verifiedCertificate.getIssuerX500Principal().equals(androidDebugModePrincipal) ||
					verifiedCertificate.getSubjectX500Principal().equals(androidDebugModePrincipal)	)
				return null;
			
			try {
				
				// Retrieve the correct apk or jar file 
				// containing the class that we should load
				byte[] data = retrieveContainerFromClassName(className);
				
				// Signature verification..
				Signature mSignature = Signature.getInstance(verifiedCertificate.getSigAlgName());
				mSignature.update(data);
				mSignature.initVerify(verifiedCertificate);
				if (mSignature.verify(data)) {
					
					// The signature of the related .apk or .jar container
					// was successfully verified against the valid certificate.
					// Integrity was granted and the class can be loaded.
					return super.loadClass(className);
				}
				
				// The signature of the .apk or jar.container
				// was not obtained from the selected certificate.
				// No class loading should start!
				return null;
				
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			} catch (SignatureException e) {
				e.printStackTrace();
			}
			
		}
		
		// Download procedure fails and the required
		// certificate has not been cached locally.
		// No class should be loaded since its signature
		// can't be verified..
		return null;
	}
	
	private byte[] retrieveContainerFromClassName(String className) {
		// TODO Auto-generated method stub
		return null;
	}

	private X509Certificate importCertificateFromAppPrivateDir(String packageName) {
		
		// The procedure looks for the correct certificate and 
		// if a match is found, it will import it and return it.
		File[] certMatchingFiles = certificateFolder.listFiles(new CertFileFilter(packageName));
		
		X509Certificate verifiedCertificate = null;
				
		if (certMatchingFiles != null && certMatchingFiles.length != 0) {
					
			// Import just the first matching certificate from file..
			InputStream inStream = null;
			
			try {
					
				// Since certificate files has unique package names as their own
				// name, either no one or one matching certificate file will
				// be found.
				inStream = new FileInputStream(certMatchingFiles[0]);
			    CertificateFactory cf = CertificateFactory.getInstance("X.509");
			    verifiedCertificate = (X509Certificate) cf.generateCertificate(inStream);
					    
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (CertificateException e) {
				e.printStackTrace();
			} finally {
			     if (inStream != null) {
			         try {
						inStream.close();
					} catch (IOException e) {
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
		
		// At the end return the result of the procedure: 
		// either null or a valid and not expired certificate
		return verifiedCertificate;
	}

	private boolean downloadCertificateRemotelyViaHttps(String packageName) {
		
		// Check whether Internet access is granted..
		NetworkInfo activeNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
		if (activeNetworkInfo != null && activeNetworkInfo.isConnected()) {
						
			// Reconstruct URL of the certificate from the class
			// package name.
			String urlString, firstLevelDomain, secondLevelDomain;
						
			int firstPointChar = packageName.indexOf('.');
			int secondPointChar = packageName.indexOf('.', firstPointChar + 1);
			firstLevelDomain = packageName.substring(0, firstPointChar);
			secondLevelDomain = packageName.substring(firstPointChar + 1, secondPointChar);
						
			urlString = "https://" + secondLevelDomain + firstLevelDomain 
						+ packageName.substring(secondPointChar).replaceAll(".", "/")
						+ "/certificate.pem";
			
			Log.i(TAG_SECURE_DEX_CLASS_LOADER, "Certificate Remote Location: " + urlString);
						
			// Open an Https connection by trusting default CA on
			// the Android device.
			HttpsURLConnection urlConnection = null;
			InputStream inputStream = null;
			OutputStream outputStream = null;
						
			try {
					
				URL certificateURL = new URL(urlString);
				urlConnection = (HttpsURLConnection) certificateURL.openConnection();
				// TODO Discuss how to interact with a web site that has just 
				// a self signed certificate.. Up to now they're rejected..
				// And it makes sense..
				urlConnection.connect();
				
				Log.i(TAG_SECURE_DEX_CLASS_LOADER, "A connection to the URL was set up.");
							
				inputStream = urlConnection.getInputStream();
				// The new certificate is stored in the application private directory
				// and its name is the same as the package name.
				String downloadPath = certificateFolder.getAbsolutePath() + "/" + packageName + ".pem";
				outputStream = new FileOutputStream(downloadPath);
						
				int read = 0;
				byte[] bytes = new byte[1024];
				
				while ((read = inputStream.read(bytes)) != -1) {
					outputStream.write(bytes, 0, read);
				}
				
				Log.i(TAG_SECURE_DEX_CLASS_LOADER, "Download complete. Certificate Path: " + downloadPath);
							
			} catch (MalformedURLException e) {
				return false;
			} catch (IOException e) {
				return false;
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
				if (urlConnection != null)	urlConnection.disconnect();
				
				Log.i(TAG_SECURE_DEX_CLASS_LOADER, "Clean up of all pending streams completed.");
			}

			// If the code reaches this point, it means that the Https
			// request was correctly instantiated and a certificate
			// was properly downloaded in the local folder.
			return true;
		}
		
		Log.w(TAG_SECURE_DEX_CLASS_LOADER, "No connectivity is available for the device!");
		
		// If this branch is reached it means that no Internet 
		// connectivity was available..
		// So the procedure fails..
		return false;
	}

	void setCertificateLocationMap(	Map<String, String> packageNameToCertificateMap) {
		
		this.packageNameToCertificateMap = packageNameToCertificateMap;
	}
}
