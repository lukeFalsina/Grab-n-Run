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

import static android.content.Context.MODE_PRIVATE;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static dalvik.system.DexFile.loadDex;
import static it.necst.grabnrun.FileHelper.APK_EXTENSION;
import static it.necst.grabnrun.FileHelper.JAR_EXTENSION;
import static it.necst.grabnrun.FileHelper.extractExtensionFromFilePath;
import static it.necst.grabnrun.FileHelper.extractFilePathWithoutExtensionFromFilePath;
import static it.necst.grabnrun.SecureLoaderFactory.IMPORTED_CONTAINERS_PRIVATE_DIRECTORY_NAME;
import static it.necst.grabnrun.SecureLoaderFactory.X_509_CERTIFICATE;
import static java.util.Collections.synchronizedMap;
import static java.util.Collections.synchronizedSet;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;

import javax.security.auth.x500.X500Principal;

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

    // Final name of the folder user to store certificates for the verification
    @VisibleForTesting static final String IMPORTED_CERTIFICATE_PRIVATE_DIRECTORY_NAME = "valid_certs";

    // Constant used to tune concurrent vs standard verification in Eager mode.
    private static final int MINIMUM_NUMBER_OF_CONTAINERS_FOR_CONCURRENT_VERIFICATION = 2;

    // Sets the Time Unit to milliseconds.
    private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.MILLISECONDS;

    // Relevant file entries, and file extensions
    private static final String CLASSES_DEX_ENTRY_NAME = "classes.dex";
    private static final String ODEX_EXTENSION = ".odex";
    private static final String PEM_EXTENSION = ".pem";

    private File certificateFolder, containersFolder;
    private FileDownloader fileDownloader;
    private CertificateFactory certificateFactory;
    private final ContainerSignatureVerifier containerSignatureVerifier;

	// The internal DexClassLoader used to load classes that passes all the checks..
	private final DexClassLoader dexClassLoader;

    private final Map<String, String> packageNameToContainerPathMap;
    private final ImmutableMap<String, URL> packageNameToCertificateMap;

	// Used to verify if a call to the wiped out method has been performed.
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
	private PackageNameTrie packageNameTrie;
	
	@VisibleForTesting SecureDexClassLoader(
            @NonNull String dexPath,
            @NonNull DexClassLoader dexClassLoader,
            @NonNull Context parentContext,
            @NonNull Map<String, URL> sanitizePackageNameToCertificateMap,
			boolean performLazyEvaluation,
            @NonNull ContainerSignatureVerifier containerSignatureVerifier,
            @NonNull CertificateFactory certificateFactory) {
		
		this.dexClassLoader = dexClassLoader;
        this.performLazyEvaluation = performLazyEvaluation;
        this.containerSignatureVerifier = containerSignatureVerifier;
        this.certificateFactory = certificateFactory;
		
		certificateFolder =
				parentContext.getDir(IMPORTED_CERTIFICATE_PRIVATE_DIRECTORY_NAME, MODE_PRIVATE);
		containersFolder =
				parentContext.getDir(IMPORTED_CONTAINERS_PRIVATE_DIRECTORY_NAME, MODE_PRIVATE);

		fileDownloader = new FileDownloader(parentContext);
		
		hasBeenWipedOut = false;
		
		lazyAlreadyVerifiedPackageNameSet = synchronizedSet(new HashSet<String>());

        packageNameToContainerPathMap =
                synchronizedMap(generatePackageNameToContainerPathMap(dexPath));

        packageNameToCertificateMap = ImmutableMap.copyOf(sanitizePackageNameToCertificateMap);

        packageNameTrie = initializePackageNameTrieBasedOnContainerAndCertificateMaps(
                packageNameToContainerPathMap, sanitizePackageNameToCertificateMap);

        if (!performLazyEvaluation) {
            // If an eager approach is chosen, all containers have to be verified now,
            // and invalid ones must be removed.
            performEagerEvaluationOnAllContainers();
        }
	}

    private static Map<String, String> generatePackageNameToContainerPathMap(
            @NonNull String dexPath) {
        Map<String, String> packageNameToContainerPathMap = new LinkedHashMap<>();

        // Analyze each path in dexPath, find its package name and
        // populate packageNameToContainerPathMap accordingly
        DexPathStringProcessor dexPathStringProcessor = new DexPathStringProcessor(dexPath);

        while (dexPathStringProcessor.hasNextDexPathString()) {
            String currentPath = dexPathStringProcessor.nextDexPathString();

            // In jar containers you may have classes from different package names, while in apk
            // there is usually only one of those.
            Optional<ImmutableSet<String>> optionalPackageNameSet =
                    getPackageNamesFromContainerPath(currentPath);

            if (optionalPackageNameSet.isPresent() && !optionalPackageNameSet.get().isEmpty()) {

                for (String packageName : optionalPackageNameSet.get()) {

                    // This is a valid entry so it must be added to packageNameToContainerPathMap
                    String previousPath = packageNameToContainerPathMap.put(packageName, currentPath);

                    // If previous path is not null, it means that one of the previous analyzed
                    // path had the same package name (this is a possibility for JAR containers..)
                    if (previousPath != null) {

                        // TODO Up to now only a warning message is registered in the logs and the most
                        // fresh of the two references is stored.
                        Log.w(TAG_SECURE_DEX_CLASS_LOADER, "Package Name " + packageName + " is not unique!\n Previous path: "
                                + previousPath + ";\n New path: " + currentPath + ";");
                    }
                }
            }
        }

        return packageNameToContainerPathMap;
    }

    private static Optional<ImmutableSet<String>> getPackageNamesFromContainerPath(
			@NonNull String containerPath) {

		// Filter empty or missing path input
		if (containerPath.isEmpty() ||
                !(new File(containerPath).exists())) {
            return Optional.absent();
        }

		// JAR container case (APK are simply an extension of jar files):
		// 1. Open the jar file.
		// 2. Look for the "classes.dex" entry inside the container.
		// 3. If it is present, retrieve package names by parsing it as a DexFile.

		boolean isAValidJar = false;
		JarFile containerJar = null;

		try {

			// Open the jar container..
			containerJar = new JarFile(containerPath);

			// Look for the "classes.dex" entry inside the container.
			if (containerJar.getJarEntry(CLASSES_DEX_ENTRY_NAME) != null)
				isAValidJar = true;

		} catch (IOException e) {
			return Optional.absent();
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
			ImmutableSet.Builder<String> packageNameSetBuilder = ImmutableSet.builder();

			try {

				// Temporary file location for the loaded classes inside of the jar container
				String outputDexTempPath =
						extractFilePathWithoutExtensionFromFilePath(containerPath) + ODEX_EXTENSION;

				// Load the dex classes inside the temporary file.
				dexFile = loadDex(containerPath, outputDexTempPath, 0);

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
						packageNameSetBuilder.add(packageName);
					}

				}

				// Finally erase the .odex file since it's not necessary anymore..
				new File(outputDexTempPath).delete();

			} catch (IOException e) {
				// Problem parsing the attached classes.dex so no valid package name
				return Optional.absent();
			}

			return Optional.of(packageNameSetBuilder.build());
		}

		// If classes.dex is not present in the jar, the jar container is not valid
		return Optional.absent();
	}

    private static PackageNameTrie initializePackageNameTrieBasedOnContainerAndCertificateMaps(
            @NonNull Map<String, String> packageNameToContainerPathMap,
            @NonNull Map<String, URL> packageNameToCertificateMap) {
        PackageNameTrie packageNameTrie = new PackageNameTrie();

        for (String packageNameIdentifyingAContainer : packageNameToContainerPathMap.keySet()) {
            packageNameTrie.generateEntriesForPackageName(packageNameIdentifyingAContainer);
        }

        for (String packageNameWithACertificate : packageNameToCertificateMap.keySet()) {
            packageNameTrie.setPackageNameHasAssociatedCertificate(packageNameWithACertificate);
        }

        return packageNameTrie;
    }

    private void performEagerEvaluationOnAllContainers() {
        // Get the distinct set of containers path..
        Set<String> containersToVerifySet = new HashSet<>(packageNameToContainerPathMap.values());

        // Check how many containers need to be verified..
        if (containersToVerifySet.size() < MINIMUM_NUMBER_OF_CONTAINERS_FOR_CONCURRENT_VERIFICATION) {

            // Choose standard single thread verification.
            verifyAllContainersSignature();
        } else {

            // Perform a concurrent container verification.
            verifyAllContainersSignatureConcurrently(containersToVerifySet);
        }
    }

	// This method is invoked only in the case of an eager evaluation.
	// It will check that all the provided containers successfully
	// execute the signature verification step against the certificate associated
	// to those. Containers which fail the test will be removed.
	private void verifyAllContainersSignature() {
		
		// This map is used to check whether one container has been already verified and the
		// result of the signature verification process.
		Map<String, Boolean> alreadyCheckedContainerMap = new HashMap<>();
		
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
				Optional<String> optionalRootPackageNameWithCertificate =
						packageNameTrie.getPackageNameWithAssociatedCertificate(currentPackageName);
				
				X509Certificate verifiedCertificate = null;
				
				// Check that such a package name exists and, in this case, try to import the certificate.
				if (optionalRootPackageNameWithCertificate.isPresent()) {
					
					// Try to find and import the certificate used to check the signature of .apk or .jar container
					verifiedCertificate = importCertificateFromPackageName(
                            optionalRootPackageNameWithCertificate.get());
				}
				
				// Relevant only if a verified certificate object is found.
				boolean signatureCheckIsSuccessful = true;
				
				if (verifiedCertificate != null) {
					
					// We were able to get a valid certificate either directly from the local cache directory or after having 
					// downloaded it from the web securely.
					// Now it's time to check whether this certificate was used to sign the class to be loaded.
					
					signatureCheckIsSuccessful =
                            containerSignatureVerifier.verifyContainerSignatureAgainstCertificate(
                                    containerPath, verifiedCertificate);
					
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
		Map<String, String> containerPathToRootPackageNameMap = new LinkedHashMap<>();
		
		// Analyze all the package names which are linked to a container.
		Iterator<String> packageNamesIterator = packageNameToContainerPathMap.keySet().iterator();
				
		// Scan all package names and find a suitable root package name 
		// with an associated certificate to validate each container.
		while (packageNamesIterator.hasNext()) {
			
			String currentPackageName = packageNamesIterator.next();
			
			// At first find the package name which is closest in hierarchy to the target one
			// and has an associated URL for a certificate.
			Optional<String> optionalRootPackageNameWithCertificate =
                    packageNameTrie.getPackageNameWithAssociatedCertificate(currentPackageName);
			
			if (optionalRootPackageNameWithCertificate.isPresent()) {
				
				// Insert a valid entry into the container to certificate Map
				containerPathToRootPackageNameMap.put(packageNameToContainerPathMap.get(
                        currentPackageName), optionalRootPackageNameWithCertificate.get());
			}
		}
		
		// Initialize the set of successfully verified containers
		Set<String> successVerifiedContainerPathSet = synchronizedSet(new HashSet<String>());
		
		if (!containerPathToRootPackageNameMap.isEmpty()) {
			
			// Initialize the thread pool executor with number of thread equals to the
			// number of containers to verify..
			ExecutorService threadSignatureVerificationPool = Executors.newFixedThreadPool(containerPathToRootPackageNameMap.size());			
			List<Future<?>> futureTaskList = new ArrayList<>();
			
			Iterator<String> containerPathIterator = containerPathToRootPackageNameMap.keySet().iterator();
			
			while (containerPathIterator.hasNext()) {
				
				String currentContainerPath = containerPathIterator.next();
				
				// Submit a new signature verification thread on a container and store a 
				// reference in the future objects list.
				Future<?> futureTask = threadSignatureVerificationPool.submit(
                        new SignatureVerificationTask(
                                currentContainerPath,
                                containerPathToRootPackageNameMap.get(currentContainerPath),
                                successVerifiedContainerPathSet));
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
			Optional<String> optionalRootPackageNameAllowedForLoading =
                    packageNameTrie.getPackageNameWithAssociatedCertificate(currentPackageName);
			
			if (!optionalRootPackageNameAllowedForLoading.isPresent() ||
                    !successVerifiedContainerPathSet.contains(packageNameToContainerPathMap.get(currentPackageName))) {
				
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
				
				boolean signatureCheckIsSuccessful =
                        containerSignatureVerifier.verifyContainerSignatureAgainstCertificate(
                                containerPath, verifiedCertificate);
				
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
	public Class<?> loadClass(@NonNull String className) throws ClassNotFoundException {
		
        checkNotNull(className, "The name of the class to load must be not null");
        checkArgument(!className.isEmpty(), "The name of the class to load must be not empty");

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
		
		if (containerPath == null) return null;
		
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
				Optional<String> optionalRootPackageNameWithCertificate =
                        packageNameTrie.getPackageNameWithAssociatedCertificate(packageName);
				
				if (optionalRootPackageNameWithCertificate.isPresent()) {
					
					// The container associated to this package name has been already verified once so classes
					// belonging to this package name can be immediately loaded.
					return dexClassLoader.loadClass(className);
				}
			}
			else {
				
				// This branch represents those classes, whose package name and related container has not been analyzed yet..
				
				// At first find the package name which is closest in hierarchy to the target one
				// and has an associated URL for a certificate.
				Optional<String> optionalRootPackageNameWithCertificate =
                        packageNameTrie.getPackageNameWithAssociatedCertificate(packageName);
				
				X509Certificate verifiedCertificate = null;
				
				// Check that such a package name exists and, in this case, try to import the certificate.
				if (optionalRootPackageNameWithCertificate.isPresent()) {
					
					// Try to find and import the certificate used to check the signature of .apk or .jar container
					verifiedCertificate = importCertificateFromPackageName(
                            optionalRootPackageNameWithCertificate.get());
				}
				
				if (verifiedCertificate != null) {
					
					// We were able to get a valid certificate either directly from the local cache directory or after having 
					// downloaded it from the web securely.
					// Now it's time to check whether this certificate was used to sign the class to be loaded.
					
					boolean signatureCheckIsSuccessful =
                            containerSignatureVerifier.verifyContainerSignatureAgainstCertificate(
                                    containerPath, verifiedCertificate);
					
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
						
						return dexClassLoader.loadClass(className);
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
		return dexClassLoader.loadClass(className);
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

	private X509Certificate importCertificateFromAppPrivateDir(String packageName) {
		
		// The procedure looks for the correct certificate and 
		// if a match is found, it will import it and return it.
		File[] certMatchingFiles = certificateFolder.listFiles(new CertificateFileFilterByNameMatch(packageName));
		
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
		String localCertPath =
                certificateFolder.getAbsolutePath() + File.separator + packageName + PEM_EXTENSION;
		
		// Return the result of the download procedure (redirect here is not permitted).
		return fileDownloader.downloadRemoteResource(certificateRemoteURL, localCertPath, false);
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
		
		List<File> fileToEraseList = new ArrayList<>();
		
		if (containerPrivateFolder) {
			
			// It is required to erase all the files in the application
			// private container folder..
			File[] containerFiles = containersFolder.listFiles();

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
            Optional<String> optionalExtension = extractExtensionFromFilePath(file.getAbsolutePath());

            if (optionalExtension.isPresent() &&
                    (optionalExtension.get().equals(APK_EXTENSION) ||
                            optionalExtension.get().equals(JAR_EXTENSION) ||
                            optionalExtension.get().equals(PEM_EXTENSION))) {

                if (file.delete())
                    Log.i(TAG_SECURE_DEX_CLASS_LOADER, file.getPath() + " has been erased.");
                else
                    Log.i(TAG_SECURE_DEX_CLASS_LOADER, file.getPath() + " was NOT erased.");
            }
        }
		
		hasBeenWipedOut = true;
	}
}
