package it.necst.grabnrun;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

import javax.security.auth.x500.X500Principal;

import android.content.ContextWrapper;
import android.content.pm.PackageManager;
//import android.net.ConnectivityManager;
//import android.net.NetworkInfo;
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
	//private ConnectivityManager mConnectivityManager;
	private PackageManager mPackageManager;
	
	private Map<String, String> packageNameToCertificateMap, packageNameToContainerPathMap;
	
	SecureDexClassLoader(	String dexPath, String optimizedDirectory,
							String libraryPath, ClassLoader parent,
							ContextWrapper parentContextWrapper) {
		super(dexPath, optimizedDirectory, libraryPath, parent);
		
		certificateFolder = parentContextWrapper.getDir("valid_certs", ContextWrapper.MODE_PRIVATE);
		//mConnectivityManager = (ConnectivityManager) parentContextWrapper.getSystemService(Context.CONNECTIVITY_SERVICE);
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
			if (mPackageManager.getPackageArchiveInfo(containerPath, 0) != null)
				return mPackageManager.getPackageArchiveInfo(containerPath, 0).packageName;
			
			return null;
		}
			
		if (extension.equals(".jar")) {
				
			// JAR container case:
			// 1. Open classes.dex file (JAR container with "classes.dex" inside)
			// 2. Find the first class name from it
			// 3. Extract and return package name from previous class name 
				
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
			
			// A valid package name for JAR container was found..
			return packageName;				
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
	@Override
	public Class<?> loadClass(String className) throws ClassNotFoundException {
		
		// A map which links package names to certificate locations
		// must be provided before calling this method..
		if (packageNameToCertificateMap == null) return null;
		
		// At first the name of the certificate possibly stored 
		// in the application private directory is generated
		// from the package name.
		String packageName = className.substring(0, className.lastIndexOf('.'));
		
		// Retrieve the path of the container from package name.
		// If there is not such a path, then no class can be loaded.
		String containerPath = packageNameToContainerPathMap.get(packageName);
		if(containerPath == null) return null;
		
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
			
				
			// Retrieve the correct apk or jar file containing the class that we should load
			// Check whether the selected resource is a jar or apk container
			int extensionIndex = containerPath.lastIndexOf(".");
			String extension = containerPath.substring(extensionIndex);
				
			boolean signatureCheckIsSuccessful = false;
				
			// Depending on the container extension the process for
			// signature verification changes
			if (extension.equals(".apk")) {
					
				try {
					
					// APK container case:
					// Initialize the signature object with the appropriate 
					// signing algorithm (the one used in the trusted certificate).
					Signature mSignature = Signature.getInstance(verifiedCertificate.getSigAlgName());
					
					// Fill signature object with the data which was initially signed (e.g. the APK container)
					FileInputStream containerFIS = null;
					BufferedInputStream containerBufIn = null;
					
					try {
						
						containerFIS = new FileInputStream(containerPath);
						containerBufIn = new BufferedInputStream(containerFIS);
						
						byte[] buffer = new byte[1024];
						int len;
						while (containerBufIn.available() != 0) {
						    len = containerBufIn.read(buffer);
						    mSignature.update(buffer, 0, len);
						};
						
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} finally {
						if (containerBufIn != null) {
							try {
								containerBufIn.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					
					// Provide the trusted certificate from which the public
					// key will be extracted for the signature verification.
					mSignature.initVerify(verifiedCertificate);
					
					// Use PackageManager field to retrieve the signature 					
					android.content.pm.Signature apkSignature = mPackageManager.getPackageArchiveInfo(containerPath, PackageManager.GET_SIGNATURES).signatures[0];
					
					// Trigger the signature verification procedure
					signatureCheckIsSuccessful = mSignature.verify(apkSignature.toByteArray());
				
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				} catch (InvalidKeyException e) {
					e.printStackTrace();
				} catch (SignatureException e) {
					e.printStackTrace();
				}
			}
			else {
					
				if (extension.equals(".jar")) {
						
					// JAR container case:
					JarFile jarContainerToVerify = null;
						
					try {
							
						jarContainerToVerify = new JarFile(containerPath);
						// This method will throw an IOException whenever
						// the JAR container was not signed with the trusted certificate
						verifyJARContainer(jarContainerToVerify, verifiedCertificate);
							
						// No exception raised so the signature 
						// verification succeeded
						signatureCheckIsSuccessful = true;
							
					} catch (IOException e) {
						// Signature process failed since it triggered
						// this exception
						signatureCheckIsSuccessful = false;
					} finally {
						if (jarContainerToVerify != null)
							try {
								jarContainerToVerify.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
					}
				}
			}
				
			// Signature verification result..
			if (signatureCheckIsSuccessful) {
					
				// The signature of the related .apk or .jar container
				// was successfully verified against the valid certificate.
				// Integrity was granted and the class can be loaded.
				return super.loadClass(className);
			}
				
			// The signature of the .apk or .jar container
			// was not valid when compared against the selected certificate.
			// No class loading should be allowed and the container 
			// should be removed as well.
			File containerToRemove = new File(containerPath);
			containerToRemove.delete();
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

	    // Ensure the jar file is signed.
	    Manifest man = jarFile.getManifest();
	    if (man == null) throw new SecurityException("The provider is not signed");

	    // Ensure all the entries' signatures verify correctly
	    byte[] buffer = new byte[8192];
	    Enumeration<JarEntry> entries = jarFile.entries();

	    while (entries.hasMoreElements()) {
			
		    JarEntry je = (JarEntry) entries.nextElement();

		    // Skip directories.
		    if (je.isDirectory()) continue;
		    entriesVec.addElement(je);
		    InputStream is = jarFile.getInputStream(je);

			// Read in each jar entry. A security exception will
			// be thrown if a signature/digest check fails.
			while (is.read(buffer, 0, buffer.length) != -1) {
			    // Don't care
			}
			
			is.close();
	    }

		// Get the list of signer certificates
		Enumeration<JarEntry> e = entriesVec.elements();

		while (e.hasMoreElements()) {
			
			JarEntry je = (JarEntry) e.nextElement();

			// Every file must be signed except files in META-INF.
			Certificate[] certs = je.getCertificates();
			if ((certs == null) || (certs.length == 0)) {
			    if (!je.getName().startsWith("META-INF"))
			    	throw new SecurityException("The container has unsigned class files.");
			} 
			else {
			    // Check whether the file is signed by the expected
			    // signer. The jar may be signed by multiple signers.
			    // See if one of the signers is 'targetCert'.
			    int startIndex = 0;
			    X509Certificate[] certChain;
			    boolean signedAsExpected = false;

			    while ((certChain = getAChain(certs, startIndex)) != null) {
			    	
			    	if (certChain[0].equals(trustedCert)) {
			    		// Stop since one trusted signer is found.
			    		signedAsExpected = true;
			    		break;
			    	}
			    	// Proceed to the next chain.
			    	startIndex += certChain.length;
			    }

			    if (!signedAsExpected)
			    	throw new SecurityException("The provider is not signed by a trusted signer");
			}
	    }
	}
	
	private X509Certificate[] getAChain(Certificate[] certs, int startIndex) {
	    
		if (startIndex > certs.length - 1)
			return null;

	    int i;
	    // Keep going until the next certificate is not the
	    // issuer of this certificate.
	    for (i = startIndex; i < certs.length - 1; i++) {
	    	if (!((X509Certificate)certs[i + 1]).getSubjectDN().equals(((X509Certificate)certs[i]).getIssuerDN())) {
	    		break;
	    	}
	    }
	    
	    // Construct and return the found certificate chain.
	    int certChainSize = (i-startIndex) + 1;
	    X509Certificate[] ret = new X509Certificate[certChainSize];
	    
	    for (int j = 0; j < certChainSize; j++ ) {
	    	ret[j] = (X509Certificate) certs[startIndex + j];
	    }
	    
	    return ret;
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
					int keyCertSignIndex = 5;
					if(!verifiedCertificate.getKeyUsage()[keyCertSignIndex])
						throw new CertificateExpiredException("These certificate can't be used for signature verification!");
					
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
		return SecureLoaderFactory.FileDownloader.downloadRemoteUrl(certificateRemoteURL, localCertPath);
	}
}
