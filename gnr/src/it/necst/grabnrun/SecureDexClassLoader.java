package it.necst.grabnrun;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

import javax.security.auth.x500.X500Principal;

import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Debug;
import android.util.Log;
import dalvik.system.DexClassLoader;

/**
 * A class that provides an extension of default {@link DexClassLoader} 
 * provided by the Android system and it is used to load classes 
 * from jar and apk container files including a classes.dex entry in a secure way.
 * 
 * In order to instantiate this class a call to the method createDexClassLoader
 * from a {@link SecureLoaderFactory} object must be performed.
 * 
 * {@link SecureDexClassLoader} ensures integrity of loaded external remote 
 * classes by comparing them with the developer certificate, which
 * is retrieved either by a provided associative map between package names 
 * and certificate remote URL or by simply reverting the 
 * first two words of the package name of the loaded class and then 
 * by adding each following word in the same order and separated by 
 * a slash "/".
 * 
 * Package name reversion example:
 * Class name = it.necst.grabnrun.example.TestClassImpl
 * Constructed URL = https://necst.it/grabnrun/example
 * Final certificate location = https://necst.it/grabnrun/example/certificate.pem
 * 
 * A request is pointed to the final certificate location and if 
 * the file is found, it is imported in the local private 
 * application directory.
 * 
 * Please note that in the current implementation certificates obtained 
 * by reverting package name must have been saved at the described 
 * location as "certificate.pem". Moreover all the certificates must 
 * fit requirements of a standard X.509 certificate, they must 
 * be valid in the current time frame and of course they must have been 
 * used to sign the jar or apk, which contains the classes to be loaded.
 * 
 * If any of these previous requirements is violated no class is loaded 
 * and this class immediately returns without executing any class code 
 * loading operation.
 * 
 * @author Luca Falsina
 */
public class SecureDexClassLoader {
	
	// Unique identifier used for Log entries
	private static final String TAG_SECURE_DEX_CLASS_LOADER = SecureDexClassLoader.class.getSimpleName();
	
	private File certificateFolder, resDownloadFolder;
	//private ConnectivityManager mConnectivityManager;
	private PackageManager mPackageManager;
	
	private FileDownloader mFileDownloader;
	
	// The internal DexClassLoader used to load classes that
	// passes all the checks..
	private DexClassLoader mDexClassLoader;
	
	private Map<String, String> packageNameToCertificateMap, packageNameToContainerPathMap;
	
	// Final name of the folder user to store certificates for the verification
	private static final String CERTIFICATE_DIR = "valid_certs";
	
	// Used to verify if a call to the wiped out method has
	// been performed.
	private boolean hasBeenWipedOut;
	
	SecureDexClassLoader(	String dexPath, String optimizedDirectory,
							String libraryPath, ClassLoader parent,
							ContextWrapper parentContextWrapper) {
		
		// Initialization of the linked internal DexClassLoader
		mDexClassLoader = new DexClassLoader(dexPath, optimizedDirectory, libraryPath, parent);
		
		certificateFolder = parentContextWrapper.getDir(CERTIFICATE_DIR, ContextWrapper.MODE_PRIVATE);
		resDownloadFolder = parentContextWrapper.getDir(SecureLoaderFactory.RES_DOWNLOAD_DIR, ContextWrapper.MODE_PRIVATE);
		
		//mConnectivityManager = (ConnectivityManager) parentContextWrapper.getSystemService(Context.CONNECTIVITY_SERVICE);
		mPackageManager = parentContextWrapper.getPackageManager();
		
		mFileDownloader = new FileDownloader(parentContextWrapper);
		
		hasBeenWipedOut = false;
		
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
			if (mPackageManager.getPackageArchiveInfo(containerPath, 0) != null)
				return mPackageManager.getPackageArchiveInfo(containerPath, 0).packageName;
			
			return null;
		}
			
		if (extension.equals(".jar")) {
				
			// JAR container case:
			// 1. Open jar file
			// 2. Scan all the entries till one .java is found
			// 3. Retrieve package name from this entry class name
			
			String packageName = null;
			boolean isAValidJar = false;
			JarFile containerJar = null;
			
			try {
				
				containerJar = new JarFile(containerPath);
				Enumeration<JarEntry> entries = containerJar.entries();
				
				// Scan all the entries in the jar archive
				while (entries.hasMoreElements()) {
				
					JarEntry currentEntry = entries.nextElement();
					
					if (currentEntry.getName().endsWith(".java")) {
						
						// A valid java file of a class was found so 
						// package name could be extracted from here
						String fullClassName = currentEntry.getName();
						
						// Cancel white spaces before processing the full class name..
						// It may happen to find them while parsing JarEntry objects..
						while (fullClassName.startsWith(" "))
							fullClassName = fullClassName.substring(1, fullClassName.length());
						
						int lastIndexPackageName = fullClassName.lastIndexOf(File.separator);
						if (lastIndexPackageName != -1)
							packageName = fullClassName.substring(0, lastIndexPackageName).replaceAll(File.separator, ".");
						
					} else {
						
						// It is necessary that the jar container has an entry "classes.dex" in 
						// order to be correctly executed by a DexClassLoader..
						if (currentEntry.getName().endsWith("classes.dex")) 
							isAValidJar = true;
					}
				}
				
			} catch (IOException e) {
				return null;
			} finally {
				if (containerJar != null)
					try {
						containerJar.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
			
			if (isAValidJar)
				return packageName;
			
			// If classes.dex is not present in the jar, the jar container is not valid
			return null;			
		}
		
		// Any other file format is not supported so 
		// no package name is returned..
		return null;
	}
	
	void setCertificateLocationMap(	Map<String, String> extPackageNameToCertificateMap) {
		
		// Either initialize a new map or copy the provided one if it's valid.
		if (extPackageNameToCertificateMap == null) 
			packageNameToCertificateMap = new HashMap<String, String>();
		else
			packageNameToCertificateMap = extPackageNameToCertificateMap;
		
		// Now check all the package names inside packageNameToContainerPathMap.
		// For each one of those which is missing in packageNameToCertificateMap
		// add a new entry (package name, URL certificate = reverted package name)
		// to the latter map.
		Iterator<String> packageNameIterator = packageNameToContainerPathMap.keySet().iterator();
		
		while (packageNameIterator.hasNext()) {
			
			String currentPackageName = packageNameIterator.next();
			
			if (packageNameToCertificateMap.get(currentPackageName) == null) {
				
				// No certificate URL was defined for this package name
				// so certificate URL must be constructed by reverting package name
				// and a new entry is put in the map.
				String certificateRemoteURL = revertPackageNameToURL(currentPackageName);
				packageNameToCertificateMap.put(currentPackageName, certificateRemoteURL);
				
				Log.d(	TAG_SECURE_DEX_CLASS_LOADER, "Package Name: " + currentPackageName + 
						"; Certificate Remote Location: " + certificateRemoteURL + ";");
			}
		}
	}

	private String revertPackageNameToURL(String packageName) {
		
		// Reconstruct URL of the certificate from the class package name.
		String firstLevelDomain, secondLevelDomain;
								
		int firstPointChar = packageName.indexOf('.');
		
		if (firstPointChar == -1) {
			// No point inside the package name.. NO SENSE
			// Forced to .com domain
			return "https://" + packageName + ".com/certificate.pem";
		}
		
		firstLevelDomain = packageName.substring(0, firstPointChar);
		int secondPointChar = packageName.indexOf('.', firstPointChar + 1);
		
		if (secondPointChar == -1) {
			// Just two substrings in the package name..
			return "https://" + packageName.substring(firstPointChar + 1) + "." + firstLevelDomain + "/certificate.pem";
		
		} 
		
		// The rest of the package name is interpreted as a location
		secondLevelDomain = packageName.substring(firstPointChar + 1, secondPointChar);
								
		return	"https://" + secondLevelDomain + "." + firstLevelDomain 
				+ packageName.substring(secondPointChar).replaceAll(".", "/")
				+ "/certificate.pem";
		
	}

	/* (non-Javadoc)
	 * @see java.lang.ClassLoader#loadClass(java.lang.String)
	 */
	public Class<?> loadClass(String className) throws ClassNotFoundException {
		
		// A map which links package names to certificate locations
		// must be provided before calling this method..
		if (packageNameToCertificateMap == null) return null;
		
		// Cached data have been wiped out so some of the required
		// resources may have been erased..
		if (hasBeenWipedOut) return null;
		
		// At first the name of the certificate possibly stored 
		// in the application private directory is generated
		// from the package name.
		String packageName = className.substring(0, className.lastIndexOf('.'));
		
		// Retrieve the path of the container from package name.
		// If there is not such a path, then no class can be loaded.
		String containerPath = packageNameToContainerPathMap.get(packageName);
		if(containerPath == null) return null;
		
		Debug.startMethodTracing("Import Certificate");
		
		// Instantiate a certificate object used to check 
		// the signature of .apk or .jar container
		X509Certificate verifiedCertificate;
		
		// TODO Decide the policy to apply with cached certificates
		// i.e. Always keep them, cancel when the VM is terminated..
		
		// At first check if the correct certificate has been 
		// already imported in the application-private certificate directory.
		verifiedCertificate = importCertificateFromAppPrivateDir(packageName);
		
		if (verifiedCertificate == null) {
			
			// No matching certificate or an expired one was found 
			// locally and so it's necessary to download the 
			// certificate through an Https request.
			Debug.startMethodTracing("Download Certificate");
			boolean isCertificateDownloadSuccessful = downloadCertificateRemotelyViaHttps(packageName);
			Debug.stopMethodTracing(); // end of "Download Certificate" section
			
			if (isCertificateDownloadSuccessful) {
				
				// Download procedure works fine and the new 
				// certificate should now be in the local folder.
				// So let's try to retrieve it once again..
				verifiedCertificate = importCertificateFromAppPrivateDir(packageName);
			}
		}
		
		Debug.stopMethodTracing(); // end of "Import Certificate" section
		
		if (verifiedCertificate != null) {
				
			// We were able to get a valid certificate either directly
			// from the local cache directory or after having 
			// downloaded it from the web securely.
			// Now it's time to check whether this certificate
			// was used to sign the class to be loaded.
			
			Debug.startMethodTracing("Verify Signature");
			
			// Retrieve the correct apk or jar file containing the class that we should load
			// Check whether the selected resource is a jar or apk container
			int extensionIndex = containerPath.lastIndexOf(".");
			String extension = containerPath.substring(extensionIndex);
				
			boolean signatureCheckIsSuccessful = false;
				
			// Depending on the container extension the process for
			// signature verification changes
			if (extension.equals(".apk")) {
					
				// APK container case:
				// At first look for the certificates used to sign the apk
				// and check whether at least one of them is the verified one..
				
				// Use PackageManager field to retrieve the certificates used to sign the apk
				Signature[] signatures = mPackageManager.getPackageArchiveInfo(containerPath, PackageManager.GET_SIGNATURES).signatures;
				
				if (signatures != null) {
					for (Signature sign : signatures) {
						if (sign != null) {
							
							X509Certificate certFromSign = null;
							InputStream inStream = null;
							
							try {
								
								// Recreate the certificate starting from this signature
								inStream = new ByteArrayInputStream(sign.toByteArray());
								CertificateFactory cf = CertificateFactory.getInstance("X.509");
								certFromSign = (X509Certificate) cf.generateCertificate(inStream);
								
								// Check that the reconstructed certificate is not expired..
								certFromSign.checkValidity();
								
								// Check whether the reconstructed certificate and the trusted one match
								// Please note that certificates may be self-signed but it's not an issue..
								if (certFromSign.equals(verifiedCertificate))
									// This a necessary but not sufficient condition to
									// prove that the apk container has not been repackaged..
									signatureCheckIsSuccessful = true;

							} catch (CertificateException e) {
								// If this branch is reached certificateFromSign is not valid..
							} finally {
							     if (inStream != null) {
							         try {
										inStream.close();
									} catch (IOException e) {
										e.printStackTrace();
									}
							     }
							}
							
						}
					}
				}	
			}
			
			// This branch must be taken by all jar containers and by those apk containers
			// whose certificates list contains also the trusted verified certificate.
			if (extension.equals(".jar") || (extension.equals(".apk") && signatureCheckIsSuccessful == true)) {
				
				// Verify that each entry of the container has been signed properly
				JarFile containerToVerify = null;
				
				try {
					
					containerToVerify = new JarFile(containerPath);
					// This method will throw an IOException whenever
					// the JAR container was not signed with the trusted certificate
					// N.B. apk are an extension of a jar container..
					verifyJARContainer(containerToVerify, verifiedCertificate);
					
					// No exception raised so the signature 
					// verification succeeded
					signatureCheckIsSuccessful = true;
					
				} catch (Exception e) {
					// Signature process failed since it triggered
					// an exception (either an IOException or a SecurityException)
					signatureCheckIsSuccessful = false;
					
				} finally {
					if (containerToVerify != null)
						try {
							containerToVerify.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					
				}
				
			}
			
			Debug.stopMethodTracing(); // end of "Verify Signature" section
			
			// Signature verification result..
			if (signatureCheckIsSuccessful) {
					
				// The signature of the related .apk or .jar container
				// was successfully verified against the valid certificate.
				// Integrity was granted and the class can be loaded.
				return mDexClassLoader.loadClass(className);
			}
				
			// The signature of the .apk or .jar container
			// was not valid when compared against the selected certificate.
			// No class loading should be allowed and the container 
			// should be removed as well.
			// TODO NO PERMISSION REQUIRED IN THE MANIFEST TILL NOW --> It won't erase data on external storage..
			//File containerToRemove = new File(containerPath);
			//if (!containerToRemove.delete())
				//Log.w(TAG_SECURE_DEX_CLASS_LOADER, "It was impossible to delete " + containerPath);
			packageNameToContainerPathMap.remove(packageName);
				
			return null;
		}
		
		// Download procedure fails and the required
		// certificate has not been cached locally.
		// No class should be loaded since its signature
		// can't be verified..
		return null;
	}
	
	private void verifyJARContainer(JarFile jarFile, X509Certificate trustedCert) throws IOException {
		
		// Sanity checking
		if (jarFile == null || trustedCert == null) 
		   	throw new SecurityException("JarFile or certificate are missing");

		Vector<JarEntry> entriesVec = new Vector<JarEntry>();

	    // Ensure the jar file is at least signed.
	    Manifest man = jarFile.getManifest();
	    if (man == null) {
	    	Log.i(TAG_SECURE_DEX_CLASS_LOADER, jarFile.getName() + "is not signed.");
	    	throw new SecurityException("The container is not signed");
	    }

	    // Ensure all the entries' signatures verify correctly
	    byte[] buffer = new byte[8192];
	    Enumeration<JarEntry> entries = jarFile.entries();

	    while (entries.hasMoreElements()) {
			
	    	// Current entry in the jar container
		    JarEntry je = (JarEntry) entries.nextElement();

		    // Skip directories.
		    if (je.isDirectory()) continue;
		    entriesVec.addElement(je);
		    InputStream inStream = jarFile.getInputStream(je);

			// Read in each jar entry. A security exception will
			// be thrown if a signature/digest check fails.
			while (inStream.read(buffer, 0, buffer.length) != -1) {
			    // Don't care as soon as no exception is raised..
			}
			
			// Close the input stream
			inStream.close();
	    }

	    // Get the list of signed entries from which certificates
	    // will be extracted..
		Enumeration<JarEntry> signedEntries = entriesVec.elements();

		while (signedEntries.hasMoreElements()) {
			
			JarEntry signedEntry = (JarEntry) signedEntries.nextElement();

			// Every file must be signed except files in META-INF.
			Certificate[] certificates = signedEntry.getCertificates();
			if ((certificates == null) || (certificates.length == 0)) {
				if (!signedEntry.getName().startsWith("META-INF")) {
					Log.i(TAG_SECURE_DEX_CLASS_LOADER, signedEntry.getName() + " is an unsigned class file");
					throw new SecurityException("The container has unsigned class files.");
				}
			} 
			else {
				// Check whether the file is signed by the expected
				// signer. The jar may be signed by multiple signers.
				// So see if one of the signers is 'trustedCert'.
				boolean signedAsExpected = false;
				
				for (Certificate signerCert : certificates) {
					
					try {
						
						((X509Certificate) signerCert).checkValidity();
					} catch (CertificateExpiredException
							| CertificateNotYetValidException e) {
						// Usually expired certificate are not such a relevant issue; nevertheless
						// on Android a common practice is using certificates (even self signed) but 
						// with at least a long life span and so temporal validity should be enforced..
						Log.i(TAG_SECURE_DEX_CLASS_LOADER, "One of the certificates used to sign " + signedEntry.getName() + " is expired");
						throw new SecurityException("One of the used certificates is expired!");
					} catch (Exception e) {
						// It was impossible to cast the general certificate into an X.509 one..
					}
					
					if (signerCert.equals(trustedCert))
						// The trusted certificate was used to sign this entry
						signedAsExpected = true;
				}
				
			    if (!signedAsExpected) {
			    	Log.i(TAG_SECURE_DEX_CLASS_LOADER, "The trusted certificate was not used to sign " + signedEntry.getName());
			    	throw new SecurityException("The provider is not signed by a trusted signer");
			    }
			}
	    }
	}

	private X509Certificate importCertificateFromAppPrivateDir(String packageName) {
		
		// The procedure looks for the correct certificate and 
		// if a match is found, it will import it and return it.
		File[] certMatchingFiles = certificateFolder.listFiles(new CertFileFilter(packageName));
		
		X509Certificate verifiedCertificate = null;
				
		if (certMatchingFiles != null && certMatchingFiles.length != 0) {
					
			// Import the first (and only) matching certificate from file..
			InputStream inStream = null;
			
			try {
					
				// Since certificate files has unique package names as their own
				// name, either no one or exactly one matching certificate file will
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
					
					// Evaluate whether the certificate can be used for signature verification
					// keyCertSignIndex is a magic number from ASN.1 definition of Key Usage.
					if (verifiedCertificate.getKeyUsage() != null) {
						
						int keyCertSignIndex = 5;
						if(verifiedCertificate.getKeyUsage()[keyCertSignIndex])
							throw new CertificateExpiredException("This certificate should not be used for code verification!");
						
						Log.d(TAG_SECURE_DEX_CLASS_LOADER, verifiedCertificate.getKeyUsage().toString());
					}
					
					// TODO Need to be tested!!
					// Check whether the certificate used to verify is the one 
					// used by Android in Debug Mode. If so, discard this certificate
					// since it's not secure.
					String androidDebugModeDN = "C=US,O=Android,CN=Android Debug";
					X500Principal androidDebugModePrincipal = new X500Principal(androidDebugModeDN);
					if (	verifiedCertificate.getIssuerX500Principal().equals(androidDebugModePrincipal) ||
							verifiedCertificate.getSubjectX500Principal().equals(androidDebugModePrincipal)	)
						throw new CertificateExpiredException("Android Debug Certificate can't be accepted to sign containers!");
					
				} catch (CertificateExpiredException
						| CertificateNotYetValidException e) {
					// This certificate is not valid!
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
		
		// Find remote URL of the certificate from the related map through class package name.
		String urlString = packageNameToCertificateMap.get(packageName);
		
		// All URLs here use method HTTPS.
		URL certificateRemoteURL;
		
		try {
			certificateRemoteURL = new URL(urlString);
		} catch (MalformedURLException e) {
			// Not valid remote URL for the certificate..
			return false;
		}
		
		// The new certificate should be stored in the application private directory
		// and its name should be the same as the package name.
		String localCertPath = certificateFolder.getAbsolutePath() + "/" + packageName + ".pem";
		
		// Return the result of the download procedure..
		return mFileDownloader.downloadRemoteUrl(certificateRemoteURL, localCertPath);
	}
	
	/**
	 * Sometimes it may be useful to remove those data that have been cached in 
	 * the private application folder (basically for performance reason or for saving 
	 * disk space on the device). A call to this method solves the issue.
	 * 
	 * Please notice that a call to this method with both the parameters set to false 
	 * has no effect.
	 * 
	 * In any of the other cases the content of the related folder(s) will be erased and 
	 * since some of the data may have been used by {@link SecureDexClassLoader} instances, it is 
	 * required to the caller to create a new {@link SecureDexClassLoader} object through 
	 * {@link SecureLoaderFactory} since the already present object is going to be disabled 
	 * from loading classes dynamically.
	 * 
	 * @param containerPrivateFolder
	 * if the private folder containing jar and apk containers downloaded from remote URL needs to be wiped out
	 * @param certificatePrivateFolder
	 * if the private folder containing certificates needs to be wiped out
	 */
	public void wipeOutPrivateAppCachedData(boolean containerPrivateFolder, boolean certificatePrivateFolder) {
		
		// This is a useless call.. Nothing will happen..
		if (!containerPrivateFolder && !certificatePrivateFolder) return;
		
		List<File> fileToEraseList = new ArrayList<File>();
		
		if (containerPrivateFolder) {
			
			// It is required to erase all the files in the application
			// private container folder..
			File[] containerFiles = resDownloadFolder.listFiles();
			
			for (File file : containerFiles) {
				
				fileToEraseList.add(file);
			}
		}
		
		if (certificatePrivateFolder) {
			
			// It is required to erase all the files in the application
			// private certificate folder..
			File[] certificateFiles = certificateFolder.listFiles();
			
			for (File file : certificateFiles) {
				
				fileToEraseList.add(file);
			}
		}
		
		Iterator<File> fileToEraseIterator = fileToEraseList.iterator();
		
		while (fileToEraseIterator.hasNext()) {
			
			File file = fileToEraseIterator.next();
			
			// Check whether the selected resource is a container (jar or apk)
			// or a certificate (pem)
			String filePath = file.getAbsolutePath();
			int extensionIndex = filePath.lastIndexOf(".");
			String extension = filePath.substring(extensionIndex);
			
			if (extension.equals(".apk") || extension.equals(".jar") || extension.equals(".pem")) {
				
				if (file.delete())
					Log.i(TAG_SECURE_DEX_CLASS_LOADER, filePath + " has been erased.");
				else
					Log.w(TAG_SECURE_DEX_CLASS_LOADER, filePath + " was NOT erased.");
			}
		}
		
		hasBeenWipedOut = true;

	}
}
