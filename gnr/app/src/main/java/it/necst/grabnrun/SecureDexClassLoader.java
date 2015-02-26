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
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

import javax.security.auth.x500.X500Principal;

import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.pm.PackageInfo;
import android.util.Log;
import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;

/**
 * A class that provides an extension of default {@link dalvik.system.DexClassLoader} 
 * provided by the Android system and it is used to load classes 
 * from jar and apk container files including a classes.dex entry in a secure way.
 * <p>
 * In order to instantiate this class a call to 
 * {@link SecureLoaderFactory#createDexClassLoader(String, String, ClassLoader, Map)} 
 * must be performed.
 * <p>
 * {@link SecureDexClassLoader} ensures integrity of loaded external remote 
 * classes by comparing them with the developer certificate, which
 * is retrieved either by a provided associative map between package names 
 * and certificate remote URL or by simply reverting the 
 * first two words of the package name of the loaded class and then 
 * by adding each following word in the same order and separated by 
 * a slash "/".
 * <p>
 * Package name reversion example:<br>
 * Class name = it.necst.grabnrun.example.TestClassImpl<br>
 * Constructed URL = https://necst.it/grabnrun/example<br>
 * Final certificate location = https://necst.it/grabnrun/example/certificate.pem<br>
 * <p>
 * A request is pointed to the final certificate location and if 
 * the file is found, it is imported in the local private 
 * application directory.
 * <p>
 * Please note that in the current implementation certificates obtained 
 * by reverting package name must have been saved at the described 
 * location as "certificate.pem". Moreover all the certificates must 
 * fit requirements of a standard {@link java.security.cert.X509Certificate}, 
 * they must be valid in the current time frame and of course they must have been 
 * used to sign the jar or apk, which contains the classes to be loaded.
 * <p>
 * If any of these previous requirements is violated no class is loaded 
 * and this class returns without executing any class loading operation.
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
	
	// The Certificate Factory instance
	private CertificateFactory certificateFactory;
	
	private final Map<String, String> packageNameToContainerPathMap;
	private Map<String, URL> packageNameToCertificateMap;
	
	// Final name of the folder user to store certificates for the verification
	private static final String CERTIFICATE_DIR = "valid_certs";

	// Constant used to tune concurrent vs standard verification in Eager mode.
	private static final int MINIMUM_NUMBER_OF_CONTAINERS_FOR_CONCURRENT_VERIFICATION = 2;
	
	// Sets the Time Unit to milliseconds.
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.MILLISECONDS;
	
	// Used to verify if a call to the wiped out method has
	// been performed.
	private boolean hasBeenWipedOut;
	
	// Variable used to understand whether SecureDexClassLoader will immediately verify all
	// the incoming containers or if it will just verify each one of those lazily when the
	// loadClass() method will be invoked.
	private boolean performLazyEvaluation;
	
	// Helper cache set used in lazy mode in order to check only once that the container
	// associated to a package name is valid (This works fine since each used container is
	// previously imported in an application-private folder).
	private final Set<String> lazyAlreadyVerifiedPackageNameSet;
	
	// An helper data structure used to connect each package name of a possible target class
	// to the certificate of the closest package name. The closeness relation here is considered
	// in terms of hierarchy on the package name.
	private PackageNameTrie mPackageNameTrie;
	
	SecureDexClassLoader(	String dexPath, String optimizedDirectory,
							String libraryPath, ClassLoader parent,
							ContextWrapper parentContextWrapper,
							boolean performLazyEvaluation) {
		
		// Initialization of the linked internal DexClassLoader
		mDexClassLoader = new DexClassLoader(dexPath, optimizedDirectory, libraryPath, parent);
		
		certificateFolder = parentContextWrapper.getDir(CERTIFICATE_DIR, ContextWrapper.MODE_PRIVATE);
		resDownloadFolder = parentContextWrapper.getDir(SecureLoaderFactory.CONT_IMPORT_DIR, ContextWrapper.MODE_PRIVATE);
		
		//mConnectivityManager = (ConnectivityManager) parentContextWrapper.getSystemService(Context.CONNECTIVITY_SERVICE);
		mPackageManager = parentContextWrapper.getPackageManager();
		
		mFileDownloader = new FileDownloader(parentContextWrapper);
		
		hasBeenWipedOut = false;
		
		this.performLazyEvaluation = performLazyEvaluation;
		
		lazyAlreadyVerifiedPackageNameSet = Collections.synchronizedSet(new HashSet<String>());
		
		mPackageNameTrie = new PackageNameTrie();
		
		// Initialize the certificate factory
		try {
			certificateFactory = CertificateFactory.getInstance("X.509");
		} catch (CertificateException e) {
			e.printStackTrace();
		}
		
		// Map initialization
		packageNameToCertificateMap = new LinkedHashMap<String, URL>();
		// packageNameToContainerPathMap = new LinkedHashMap<String, String>();
		packageNameToContainerPathMap = Collections.synchronizedMap(new LinkedHashMap<String, String>());
		
		// Analyze each path in dexPath, find its package name and 
		// populate packageNameToContainerPathMap accordingly
		String[] pathStrings = dexPath.split(Pattern.quote(File.pathSeparator));
		
		for (String currentPath : pathStrings) {
			
			// In jar containers you may have classes from different package names, while in apk
			// there is usually only one of those.
			Set<String> packageNameList = getPackageNamesFromContainerPath(currentPath);
			
			if (packageNameList != null && !packageNameList.isEmpty()) {
				
				for (String packageName : packageNameList) {
					
					// This is a valid entry so it must be added to packageNameToContainerPathMap
					String previousPath = packageNameToContainerPathMap.put(packageName, currentPath);
					
					// Also fill auxiliary Trie-like data structure
					mPackageNameTrie.generateEntriesForPackageName(packageName);
					
					// If previous path is not null, it means that one of the previous analyzed
					// path had the same package name (this is a possibility for JAR containers..)
					if (previousPath != null) {
						
						// TODO Up to now only a warning message is registered in the logs and the most
						// fresh of the two references is stored.
						Log.w(	TAG_SECURE_DEX_CLASS_LOADER, "Package Name " + packageName + " is not unique!\n Previous path: " 
								+ previousPath + ";\n New path: " + currentPath + ";" );
					}
				}
			}
		}
	}

	private Set<String> getPackageNamesFromContainerPath(String containerPath) {
		
		// Filter empty or missing path input
		if (containerPath == null || containerPath.isEmpty() || !(new File(containerPath).exists())) return null;
		
		// Check whether the selected resource is a container (jar or apk)
		int extensionIndex = containerPath.lastIndexOf(".");
		
		if (extensionIndex == -1) return null;
		
		String extension = containerPath.substring(extensionIndex);
		
		Set<String> packageNameSet = new HashSet<String>();
		
		if (extension.equals(".apk")) {
			
			// APK container case:
			// Use PackageManager to retrieve the package name of the APK container
			if (mPackageManager.getPackageArchiveInfo(containerPath, 0) != null) {

				packageNameSet.add(mPackageManager.getPackageArchiveInfo(containerPath, 0).packageName);
				return packageNameSet;
			}
			
			return null;
		}
			
		if (extension.equals(".jar")) {
				
			// JAR container case:
			// 1. Open the jar file.
			// 2. Look for the "classes.dex" entry inside the container.
			// 3. If it is present, retrieve package names by parsing it as a DexFile.
			
			boolean isAValidJar = false;
			JarFile containerJar = null;
			
			try {
				
				// Open the jar container..
				containerJar = new JarFile(containerPath);
				
				// Look for the "classes.dex" entry inside the container.
				if (containerJar.getJarEntry("classes.dex") != null)
					isAValidJar = true;
				
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
			
			if (isAValidJar) {

				// Use a DexFile object to parse the classes inside of the jar container and retrieve package names..
				DexFile dexFile;
				
				// Since in a jar there may be different package names for each class
				// but at the same time I want to keep just one record for each package
				// name, a set data structure fits well while processing.
				// Set<String> packageNameSet = new HashSet<String>();
				
				try {
					
					// Temporary file location for the loaded classes inside of the jar container
					String outputDexTempPath = containerPath.substring(0, extensionIndex) + ".odex";
					
					// Load the dex classes inside the temporary file.
					dexFile = DexFile.loadDex(containerPath, outputDexTempPath, 0);
					
					Enumeration<String> dexEntries = dexFile.entries();
					
					while (dexEntries.hasMoreElements()) {
						
						// Full class name, used to extract a valid package name.
						String fullClassName = dexEntries.nextElement();
						//Log.i(TAG_SECURE_DEX_CLASS_LOADER, fullClassName);
						
						// Cancel white spaces before processing the full class name..
						// It may happen to find them while parsing class names..
						while (fullClassName.startsWith(" "))
							fullClassName = fullClassName.substring(1, fullClassName.length());
						
						int lastIndexPackageName = fullClassName.lastIndexOf(".");
						
						if (lastIndexPackageName != -1) {
							
							String packageName = fullClassName.substring(0, lastIndexPackageName);
							packageNameSet.add(packageName);
						}
						
					}
					
					// Finally erase the .odex file since it's not necessary anymore..
					new File(outputDexTempPath).delete();
					
				} catch (IOException e) {
					// Problem parsing the attached classes.dex so no valid package name
					return null;
				}
				
				return packageNameSet;
			}
			
			// If classes.dex is not present in the jar, the jar container is not valid
			return null;			
		}
		
		// Any other file format is not supported so 
		// no package name is returned..
		return null;
	}
	
	void setCertificateLocationMap(	Map<String, URL> extPackageNameToCertificateMap) {
		
		// Copy the external map only if it is not empty..
		if (extPackageNameToCertificateMap != null && !extPackageNameToCertificateMap.isEmpty()) 
			packageNameToCertificateMap = extPackageNameToCertificateMap;
		
		// Now check all the package names inside packageNameToCertificateMap.
		// For each one of those which has a null value add a new entry
		// (package name, URL certificate = reverted package name) to packageNameToCertificateMap.
		Iterator<String> packageNameIterator = packageNameToCertificateMap.keySet().iterator();
		
		while (packageNameIterator.hasNext()) {
			
			String currentPackageName = packageNameIterator.next();
			
			if (packageNameToCertificateMap.get(currentPackageName) == null) {
				
				URL certificateRemoteURL;
				
				try {
				
					// No certificate URL was defined for this package name
					// so certificate URL must be constructed by reverting package name
					// and a new entry is put in the map.
					certificateRemoteURL = revertPackageNameToURL(currentPackageName);
					
					// A consistent remote URL was created..
					packageNameToCertificateMap.put(currentPackageName, certificateRemoteURL);
					
					Log.d(	TAG_SECURE_DEX_CLASS_LOADER, "Package Name: " + currentPackageName + 
							"; Certificate Remote Location: " + certificateRemoteURL + ";");
					
				} catch (MalformedURLException e) {
					// It was impossible to create a valid URL for this package name.
					// so just remove it from packageNameToCertificateMap.
					packageNameIterator.remove();
					
					Log.d(TAG_SECURE_DEX_CLASS_LOADER, "It was impossible to revert package name " + 
					currentPackageName + " into a valid URL!");
				}

			}
			
			if (packageNameToCertificateMap.containsKey(currentPackageName)) {
				
				// Either by reverting the package name or from provided URL this
				// package name has now a certificate URL associated to it.
				// So update the Trie-like data structure accordingly
				mPackageNameTrie.setEntryHasAssociatedCertificate(currentPackageName);
			}
		}
		
		if (!performLazyEvaluation) {
			
			// If an eager approach is chosen now it is time to verify all the containers
			// and remove the invalid ones.
			
			// Get the distinct set of containers path..
			Set<String> containersToVerifySet = new HashSet<String>(packageNameToContainerPathMap.values());
			
			// Check how many containers need to be verified..
			if (containersToVerifySet.size() < MINIMUM_NUMBER_OF_CONTAINERS_FOR_CONCURRENT_VERIFICATION) {
				
				// Choose standard single thread verification.
				verifyAllContainersSignature();
			} else {
				
				// Perform a concurrent container verification.
				verifyAllContainersSignatureConcurrently(containersToVerifySet);
			}
			
		}
	}

	private URL revertPackageNameToURL(String packageName) throws MalformedURLException {
		
		// Reconstruct URL of the certificate from the class package name.
		String firstLevelDomain, secondLevelDomain;
								
		int firstPointChar = packageName.indexOf('.');
		
		if (firstPointChar == -1) {
			// No point inside the package name.. NO SENSE
			// Forced to .com domain
			return new URL("https", packageName + ".com", "certificate.pem");
			//return "https://" + packageName + ".com/certificate.pem";
		}
		
		firstLevelDomain = packageName.substring(0, firstPointChar);
		int secondPointChar = packageName.indexOf('.', firstPointChar + 1);
		
		if (secondPointChar == -1) {
			// Just two substrings in the package name..
			return new URL("https", packageName.substring(firstPointChar + 1) + "." + firstLevelDomain, "/certificate.pem");
			//return "https://" + packageName.substring(firstPointChar + 1) + "." + firstLevelDomain + "/certificate.pem";
		
		} 
		
		// The rest of the package name is interpreted as a location
		secondLevelDomain = packageName.substring(firstPointChar + 1, secondPointChar);
		
		return new URL("https", secondLevelDomain + "." + firstLevelDomain, packageName.substring(secondPointChar + 1).replace('.', File.separatorChar) + "/certificate.pem");
		
		//return	"https://" + secondLevelDomain + "." + firstLevelDomain 
		//		+ packageName.substring(secondPointChar).replaceAll(".", "/")
		//		+ "/certificate.pem";
		
	}
	
	// This method is invoked only in the case of an eager evaluation.
	// It will check that all the provided containers successfully
	// execute the signature verification step against the certificate associated
	// to those. Containers which fail the test will be removed.
	private void verifyAllContainersSignature() {
		
		// This map is used to check whether one container has been already verified and the
		// result of the signature verification process.
		Map<String, Boolean> alreadyCheckedContainerMap = new HashMap<String, Boolean>();
		
		// Analyze all the package names which are linked to a container.
		Iterator<String> packageNamesIterator = packageNameToContainerPathMap.keySet().iterator();
		
		while (packageNamesIterator.hasNext()) {
			
			String currentPackageName = packageNamesIterator.next();
			String containerPath = packageNameToContainerPathMap.get(currentPackageName);
			
			// At first check whether the signature verification on this container has been already performed
			if (alreadyCheckedContainerMap.containsKey(containerPath)) {
				
				// In this case depending on the previous verification result
				// decide whether this package name should be removed or not
				if (!alreadyCheckedContainerMap.get(containerPath))
					packageNamesIterator.remove();
				
			} else {
				
				// A complete signature verification on the container must be performed and 
				// depending on the final result the alreadyCheckedContainerMap will be updated.
				
				// At first find the package name which is closest in hierarchy to the target one
				// and has an associated URL for a certificate.
				String rootPackageNameWithCertificate = mPackageNameTrie.getPackageNameWithAssociatedCertificate(currentPackageName);
				
				X509Certificate verifiedCertificate = null;
				
				// Check that such a package name exists and, in this case, try to import the certificate.
				if (!rootPackageNameWithCertificate.isEmpty()) {
					
					// Try to find and import the certificate used to check the signature of .apk or .jar container
					verifiedCertificate = importCertificateFromPackageName(rootPackageNameWithCertificate);
				}
				
				// Relevant only if a verified certificate object is found.
				boolean signatureCheckIsSuccessful = true;
				
				if (verifiedCertificate != null) {
					
					// We were able to get a valid certificate either directly from the local cache directory or after having 
					// downloaded it from the web securely.
					// Now it's time to check whether this certificate was used to sign the class to be loaded.
					
					signatureCheckIsSuccessful = verifyContainerSignatureAgainstCertificate(containerPath, verifiedCertificate);
					
					// Signature verification result..
					if (signatureCheckIsSuccessful) {
						
						// This container is valid so all of those package names which load
						// classes from it should be successful when loadClass() is called.
						alreadyCheckedContainerMap.put(containerPath, true);
					}
				}
				
				if ((verifiedCertificate == null) || ((verifiedCertificate != null) && (signatureCheckIsSuccessful == false))) {
					
					// In this case the map must be updated stating that this container has been
					// already checked and it fails the signature verification.
					alreadyCheckedContainerMap.put(containerPath, false);
					
					// Then the container should be erased.
					File containerToRemove = new File(containerPath);
					if (!containerToRemove.delete())
						Log.w(TAG_SECURE_DEX_CLASS_LOADER, "It was impossible to delete " + containerPath);
					
					// Finally this package name should be removed from the map of those
					// which are allowed to load classes.
					packageNamesIterator.remove();
				}
			}
		}
		
	}
	
	// This method is invoked only in the case of an eager evaluation.
	// It will check that all the provided containers successfully
	// execute the signature verification step against the certificate associated
	// to those. Containers which fail the test will be removed.
	private void verifyAllContainersSignatureConcurrently(Set<String> containersPathToVerifySet) {
		
		
		// Initialize helper map which links a container to the certificate to validate it..
		Map<String, String> containerPathToRootPackageNameMap = new LinkedHashMap<String, String>();
		
		// Analyze all the package names which are linked to a container.
		Iterator<String> packageNamesIterator = packageNameToContainerPathMap.keySet().iterator();
				
		// Scan all package names and find a suitable root package name 
		// with an associated certificate to validate each container.
		while (packageNamesIterator.hasNext()) {
			
			String currentPackageName = packageNamesIterator.next();
			
			// At first find the package name which is closest in hierarchy to the target one
			// and has an associated URL for a certificate.
			String rootPackageNameWithCertificate = mPackageNameTrie.getPackageNameWithAssociatedCertificate(currentPackageName);
			
			if (!rootPackageNameWithCertificate.isEmpty()) {
				
				// Insert a valid entry into the container to certificate Map
				containerPathToRootPackageNameMap.put(packageNameToContainerPathMap.get(currentPackageName), rootPackageNameWithCertificate);
			}
		}
		
		// Initialize the set of successfully verified containers
		Set<String> successVerifiedContainerPathSet = Collections.synchronizedSet(new HashSet<String>());
		
		if (!containerPathToRootPackageNameMap.isEmpty()) {
			
			// Initialize the thread pool executor with number of thread equals to the
			// number of containers to verify..
			ExecutorService threadSignatureVerificationPool = Executors.newFixedThreadPool(containerPathToRootPackageNameMap.size());			
			List<Future<?>> futureTaskList = new ArrayList<Future<?>>();
			
			Iterator<String> containerPathIterator = containerPathToRootPackageNameMap.keySet().iterator();
			
			while (containerPathIterator.hasNext()) {
				
				String currentContainerPath = containerPathIterator.next();
				
				// Submit a new signature verification thread on a container and store a 
				// reference in the future objects list.
				Future<?> futureTask = threadSignatureVerificationPool.submit(new SignatureVerificationTask(currentContainerPath, containerPathToRootPackageNameMap.get(currentContainerPath), successVerifiedContainerPathSet));
				futureTaskList.add(futureTask);
			}
			
			// Stop accepting new tasks for the current threadSignatureVerificationPool
			threadSignatureVerificationPool.shutdown();
			
			for (Future<?> futureTask : futureTaskList) {
				
				try {
					
					// Wait till the current task for signature verification is finished..
					futureTask.get();
					
				} catch (InterruptedException | ExecutionException e) {
					
					// Issue while executing the verification on a thread
					Log.w(TAG_SECURE_DEX_CLASS_LOADER, "One of the thread failed during signature verification because of " + e.getCause().toString());
				}
			}
			
			try {
				
				// Join all the threads here..
				threadSignatureVerificationPool.awaitTermination(MINIMUM_NUMBER_OF_CONTAINERS_FOR_CONCURRENT_VERIFICATION, KEEP_ALIVE_TIME_UNIT);
			} catch (InterruptedException e) {
				
				// One or more of the thread were still busy.. This should not happen..
				Log.w(TAG_SECURE_DEX_CLASS_LOADER, "At least one thread for signature verification was still busy and so it was interrupted");
			}
		}
		
		// Now all the package names are scanned again and removed from the packageNameToContainerPathMap 
		// if their container is not one of the valid ones..
		Iterator<String> packageNamesAfterVerificationIterator = packageNameToContainerPathMap.keySet().iterator();
		
		while (packageNamesAfterVerificationIterator.hasNext()) {
			
			String currentPackageName = packageNamesAfterVerificationIterator.next();
			
			// Verify that at least one of the prefix of the current package name was designated 
			// for loading its classes.
			String rootPackageNameAllowedForLoading = mPackageNameTrie.getPackageNameWithAssociatedCertificate(currentPackageName); 
			
			if (rootPackageNameAllowedForLoading.isEmpty() || !successVerifiedContainerPathSet.contains(packageNameToContainerPathMap.get(currentPackageName))) {
				
				// The container linked to this package name did not succeed in the verification process.
				// No class with this package name can be loaded..
				packageNamesAfterVerificationIterator.remove();
			}
				
		}
		
		// In the end all the containers that failed the verification are deleted..
		Iterator<String> containersPathToVerifyIterator = containersPathToVerifySet.iterator();
		
		while (containersPathToVerifyIterator.hasNext()) {
			
			String currentContainerPath = containersPathToVerifyIterator.next();
			
			if (!successVerifiedContainerPathSet.contains(currentContainerPath)) {
				
				// This container did not overcome successfully the signature verification
				// so it should be deleted from the cached container directory.
				if (!(new File(currentContainerPath).delete()))
						Log.w(TAG_SECURE_DEX_CLASS_LOADER, "Issue while deleting conainer located at " + currentContainerPath);
			}
		}
	}
	
	class SignatureVerificationTask implements Runnable {

		// Location of the container to verify.
		private String containerPath;
		// Package name associated with a certificate.
		private String rootPackageNameWithCertificate;
		// Concurrent set of containers that has been successfully verified.
		private final Set<String> successVerifiedContainerSet;
		
		public SignatureVerificationTask(String containerPath, String rootPackageNameWithCertificate, Set<String> successVerifiedContainerSet) {
			
			// Simply copy all the incoming parameters..
			this.containerPath = containerPath;
			this.rootPackageNameWithCertificate = rootPackageNameWithCertificate;
			this.successVerifiedContainerSet = successVerifiedContainerSet;
		}
		
		@Override
		public void run() {
			
			// Moves the current Thread into the background
	        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_DEFAULT);
			
			// This runnable class performs a full signature verification on the 
			// associated container
			
			// Try to find and import the certificate used to check the signature of .apk or .jar container
			X509Certificate verifiedCertificate = importCertificateFromPackageName(rootPackageNameWithCertificate);
			
			if (verifiedCertificate != null) {
				
				// We were able to get a valid certificate either directly from the local cache directory or after having 
				// downloaded it from the web securely.
				// Now it's time to check whether this certificate was used to sign the class to be loaded.
				
				boolean signatureCheckIsSuccessful = verifyContainerSignatureAgainstCertificate(containerPath, verifiedCertificate);
				
				// Signature verification result..
				if (signatureCheckIsSuccessful) {
				
					// If the signature verification on the container succeeds, insert this container path
					// into the set of those containers which successfully pass the signature verification process.
					synchronized (successVerifiedContainerSet) {
					
						successVerifiedContainerSet.add(containerPath);
					}
				}
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see java.lang.ClassLoader#loadClass(java.lang.String)
	 */
	/**
	 * @see java.lang.ClassLoader#loadClass(java.lang.String)
	 * @param className
	 *  the full class name to load. It must fit the form "package name + . + class name".
	 *  A valid full class name is for example "it.polimi.myapplication.classA"; while "classA" is not
	 *  enough since it misses the package name and so {@link SecureDexClassLoader} will not find any
	 *  class to load.
	 * @return
	 *  Either a class that needs to be casted at runtime accordingly to className if the verification
	 *  process succeeds or a {@code null} pointer in case that at least one of the security
	 *  constraints for secure dynamic class loading is violated.
	 * @throws ClassNotFoundException
	 *  this exception is raised whenever no security constraint is violated but still the target class is
	 *  not found in any of the available containers used to instantiate this {@link SecureDexClassLoader} object.
	 */
	public Class<?> loadClass(String className) throws ClassNotFoundException {
		
		// A meaningful map which links package names to certificate locations
		// must have been provided before calling this method..
		if (packageNameToCertificateMap.isEmpty()) return null;
		
		// Cached data have been wiped out so some of the required
		// resources may have been erased..
		if (hasBeenWipedOut) return null;
		
		// At first the name of the certificate possibly stored 
		// in the application private directory is generated
		// from the package name.
		String packageName = className.substring(0, className.lastIndexOf('.'));
		
		// Retrieve the path of the container from package name.
		// If there is not such a path, then no class can be loaded.
		String containerPath;
		
		synchronized (packageNameToContainerPathMap) {
			
			containerPath = packageNameToContainerPathMap.get(packageName);
		}
		
		if(containerPath == null) return null;
		
		if (performLazyEvaluation) {
			
			// If SecureDexClassLoader is running in LAZY mode, now it is time 
			// to verify the signature of the container associated with the class to load..
			boolean alreadyVerifiedPackageName;
			
			// Force synchronization on this check on the set..
			synchronized (lazyAlreadyVerifiedPackageNameSet) {
			
				// At first check whether this package name has been already verified..
				alreadyVerifiedPackageName = lazyAlreadyVerifiedPackageNameSet.contains(packageName);
			}
			
			if (alreadyVerifiedPackageName) {

				// Even if the container associated to this package name may have been verified correctly,
				// it is necessary to verify that the user also wants to dynamically load classes from this package name.
				String rootPackageNameWithCertificate = mPackageNameTrie.getPackageNameWithAssociatedCertificate(packageName);
				
				if (!rootPackageNameWithCertificate.isEmpty()) {
					
					// The container associated to this package name has been already verified once so classes
					// belonging to this package name can be immediately loaded.
					return mDexClassLoader.loadClass(className);
				}
			}
			else {
				
				// This branch represents those classes, whose package name and related container has not been analyzed yet..
				
				// At first find the package name which is closest in hierarchy to the target one
				// and has an associated URL for a certificate.
				String rootPackageNameWithCertificate = mPackageNameTrie.getPackageNameWithAssociatedCertificate(packageName);
				
				X509Certificate verifiedCertificate = null;
				
				// Check that such a package name exists and, in this case, try to import the certificate.
				if (!rootPackageNameWithCertificate.isEmpty()) {
					
					// Try to find and import the certificate used to check the signature of .apk or .jar container
					verifiedCertificate = importCertificateFromPackageName(rootPackageNameWithCertificate);
				}
				
				if (verifiedCertificate != null) {
					
					// We were able to get a valid certificate either directly from the local cache directory or after having 
					// downloaded it from the web securely.
					// Now it's time to check whether this certificate was used to sign the class to be loaded.
					
					boolean signatureCheckIsSuccessful = verifyContainerSignatureAgainstCertificate(containerPath, verifiedCertificate);
					
					// Signature verification result..
					if (signatureCheckIsSuccessful) {
						
						// The signature of the related .apk or .jar container
						// was successfully verified against the valid certificate.
						// Integrity was granted and the class can be loaded.
						// Before doing so this package name is stored into the set of 
						// those which have been already successfully verified.
						// lazyAlreadyVerifiedPackageNameSet.add(packageName);
						
						// Look for all the package names linked to the same container.
						// Since the verification steps is performed on the whole container, 
						// all the package names linked to it will automatically succeed on the
						// signature verification and so they need to be stored into the set of 
						// those package names which have been already successfully verified..
						
						synchronized (lazyAlreadyVerifiedPackageNameSet) {
							
							Iterator<String> packageNamesIterator = packageNameToContainerPathMap.keySet().iterator();
							
							while (packageNamesIterator.hasNext()) {
								
								String currentPackageName = packageNamesIterator.next();
								
								if (packageNameToContainerPathMap.get(currentPackageName).equals(containerPath)) {
									
									// This collection won't be modified if it already contains the current analyzed package name..
									lazyAlreadyVerifiedPackageNameSet.add(currentPackageName);
								}		
							}
						}						
						
						return mDexClassLoader.loadClass(className);
					}
					
					// The signature of the .apk or .jar container was not valid when compared against the selected certificate.
					// No class loading should be allowed and the container should be removed as well.
					File containerToRemove = new File(containerPath);
					if (!containerToRemove.delete())
						Log.i(TAG_SECURE_DEX_CLASS_LOADER, "It was impossible to delete " + containerPath);
					
					// packageNameToContainerPathMap.remove(packageName);
					// Remove from the associative map all of those package names which are linked to this container.
					// In fact since this container fails the verification steps, all the classes inside of it won't be loaded.
					synchronized (packageNameToContainerPathMap) {
					
						Iterator<String> packageNamesIterator = packageNameToContainerPathMap.keySet().iterator();
						
						while (packageNamesIterator.hasNext()) {
							
							String currentPackageName = packageNamesIterator.next();
							
							if (packageNameToContainerPathMap.get(currentPackageName).equals(containerPath))
								packageNamesIterator.remove();
						}
					}
										
					return null;
				}
				
				// Either download procedure fails and the required certificate has not been cached locally or
				// a package name with no certificate associated to its hierarchy was provided.
				// No class should be loaded since its signature can't be verified..
				// But on the other hand this do not imply that the container is necessarily malicious.
				return null;
			}
			
		}

		// If SecureDexClassLoader is running in EAGER mode, all the required checks
		// on the containers signatures have been already performed so we can simply 
		// invoke the super method loadClass() of DexClassLoader.
		return mDexClassLoader.loadClass(className);
	}

	// Given a package name, at first try to locate the associated certificate from the cached
	// certificate directory. If this check fails, try to download and store on the device the 
	// certificate provided by the developer. If one of the two ways is successful return 
	// a certificate instance.
	private X509Certificate importCertificateFromPackageName(String packageName) {
		
		//Trace.beginSection("Import Certificate");
		// Log.i("Profile","[Start]	Import Certificate: " + System.currentTimeMillis() + " ms.");		
		
		// At first check if the correct certificate has been 
		// already imported in the application-private certificate directory.
		X509Certificate verifiedCertificate = importCertificateFromAppPrivateDir(packageName);
				
		if (verifiedCertificate == null) {
					
			// No matching certificate or an expired one was found 
			// locally and so it's necessary to download the 
			// certificate through an Https request.
			//Trace.beginSection("Download Certificate");
			// Log.i("Profile","[Start]	Download Certificate: " + System.currentTimeMillis() + " ms.");
			boolean isCertificateDownloadSuccessful = downloadCertificateRemotelyViaHttps(packageName);
			// Log.i("Profile","[End]	Download Certificate: " + System.currentTimeMillis() + " ms.");
			//Trace.endSection(); // end of "Download Certificate" section
					
			if (isCertificateDownloadSuccessful) {
						
				// Download procedure works fine and the new 
				// certificate should now be in the local folder.
				// So let's try to retrieve it once again..
				verifiedCertificate = importCertificateFromAppPrivateDir(packageName);
			}
		}
		
		// Log.i("Profile","[End]	Import Certificate: " + System.currentTimeMillis() + " ms.");
		//Trace.endSection(); // end of "Import Certificate" section
		
		return verifiedCertificate;
	}
	
	// Given the path to a jar/apk container and a valid certificate instance this method returns
	// whether the container is signed properly against the verified certificate.
	private boolean verifyContainerSignatureAgainstCertificate(String containerPath, X509Certificate verifiedCertificate) {
		
		//Trace.beginSection("Verify Signature");
		// Log.i("Profile","[Start]	Verify Signature: " + System.currentTimeMillis() + " ms.");
		
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

            PackageInfo mPackageInfo = mPackageManager.getPackageArchiveInfo(containerPath, PackageManager.GET_SIGNATURES);

            if (mPackageInfo != null) {

                // Use PackageManager field to retrieve the certificates used to sign the apk
                Signature[] signatures = mPackageInfo.signatures;

                if (signatures != null) {
                    for (Signature sign : signatures) {
                        if (sign != null) {

                            X509Certificate certFromSign;
                            InputStream inStream = null;

                            try {

                                // Recreate the certificate starting from this signature
                                inStream = new ByteArrayInputStream(sign.toByteArray());
                                certFromSign = (X509Certificate) certificateFactory.generateCertificate(inStream);

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
                } else {
                    Log.i(TAG_SECURE_DEX_CLASS_LOADER, "An invalid/corrupted signature is associated with the source archive.");
                }
            } else {
                Log.i(TAG_SECURE_DEX_CLASS_LOADER, "An invalid/corrupted container was found.");
            }
		}
		
		// This branch must be taken by all jar containers and by those apk containers
		// whose certificates list contains also the trusted verified certificate.
		if (extension.equals(".jar") || (extension.equals(".apk") && signatureCheckIsSuccessful)) {
			
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
		
		// Log.i("Profile","[End]	Verify Signature: " + System.currentTimeMillis() + " ms.");
		//Trace.endSection(); // end of "Verify Signature" section

		return signatureCheckIsSuccessful;
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
			
			JarEntry signedEntry = signedEntries.nextElement();

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
			    //CertificateFactory cf = CertificateFactory.getInstance("X.509");
			    verifiedCertificate = (X509Certificate) certificateFactory.generateCertificate(inStream);
					    
			} catch (FileNotFoundException | CertificateException e) {
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
						
						Log.d(TAG_SECURE_DEX_CLASS_LOADER, Arrays.toString(verifiedCertificate.getKeyUsage()));
					}
					
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
		// All URLs here use method HTTPS.
		URL certificateRemoteURL = packageNameToCertificateMap.get(packageName);
		
		// The new certificate should be stored in the application private directory
		// and its name should be the same as the package name.
		String localCertPath = certificateFolder.getAbsolutePath() + "/" + packageName + ".pem";
		
		// Return the result of the download procedure (redirect here is not permitted).
		return mFileDownloader.downloadRemoteUrl(certificateRemoteURL, localCertPath, false);
	}
	
	/**
	 * Sometimes it may be useful to remove those data that have been cached in 
	 * the private application folders (basically for performance reason or for making 
	 * {@link SecureDexClassLoader} works also partially offline). A call to this method solves the issue.
	 * <p>
	 * Please notice that a call to this method with both the parameters set to false 
	 * has no effect.
	 * <p>
	 * In any of the other cases the content of the related folder(s) will be erased and 
	 * since some of the data may have been used by {@link SecureDexClassLoader} instances, it is 
	 * required to the caller to create a new {@link SecureDexClassLoader} object through 
	 * {@link SecureLoaderFactory} since the already present object is going to be disabled 
	 * from loading classes dynamically.
	 * 
	 * @param containerPrivateFolder
	 * if the private folder where jar and apk containers downloaded from remote URL or imported from local storage needs to be wiped out.
	 * @param certificatePrivateFolder
	 * if the private folder containing certificates needs to be wiped out.
	 */
	public void wipeOutPrivateAppCachedData(boolean containerPrivateFolder, boolean certificatePrivateFolder) {
		
		// This is a useless call.. Nothing will happen..
		if (!containerPrivateFolder && !certificatePrivateFolder) return;
		
		List<File> fileToEraseList = new ArrayList<File>();
		
		if (containerPrivateFolder) {
			
			// It is required to erase all the files in the application
			// private container folder..
			File[] containerFiles = resDownloadFolder.listFiles();

            Collections.addAll(fileToEraseList, containerFiles);
		}
		
		if (certificatePrivateFolder) {
			
			// It is required to erase all the files in the application
			// private certificate folder..
			File[] certificateFiles = certificateFolder.listFiles();

            Collections.addAll(fileToEraseList, certificateFiles);
		}

        for (File file : fileToEraseList) {

            // Check whether the selected resource is a container (jar or apk)
            // or a certificate (pem)
            String filePath = file.getAbsolutePath();
            int extensionIndex = filePath.lastIndexOf(".");
            String extension = filePath.substring(extensionIndex);

            if (extension.equals(".apk") || extension.equals(".jar") || extension.equals(".pem")) {

                if (file.delete())
                    Log.i(TAG_SECURE_DEX_CLASS_LOADER, filePath + " has been erased.");
                else
                    Log.i(TAG_SECURE_DEX_CLASS_LOADER, filePath + " was NOT erased.");
            }
        }
		
		hasBeenWipedOut = true;

	}
}
