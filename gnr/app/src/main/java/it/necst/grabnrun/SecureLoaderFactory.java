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
import static it.necst.grabnrun.FileHelper.endsWithJarOrApkExtension;
import static it.necst.grabnrun.FileHelper.extractExtensionFromFilePath;
import static it.necst.grabnrun.FileHelper.extractFileNameFromFilePath;
import static it.necst.grabnrun.PackageNameHelper.isAValidPackageName;
import static it.necst.grabnrun.PackageNameHelper.revertPackageNameToURL;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.HashMap;
import java.util.Map;

import dalvik.system.DexClassLoader;

/**
 * A factory class that generates instances of classes used to
 * retrieve containers holding code to execute dynamically in a secure way.
 * 
 * @author Luca Falsina
 */
public class SecureLoaderFactory {

    // Unique identifier used for Log entries
	private static final String TAG_SECURE_FACTORY = SecureLoaderFactory.class.getSimpleName();

    // Name of the folder user to store imported containers (both coming from remote, or local resources)
    static final String IMPORTED_CONTAINERS_PRIVATE_DIRECTORY_NAME = "imported_cont";
    @VisibleForTesting static final String OUTPUT_DEX_CLASSES_DIRECTORY_NAME = "dex_classes";
    @VisibleForTesting static final String X_509_CERTIFICATE = "X.509";

    /**
     * When a URL for a remote container is found, this field specifies the default time interval,
     * expressed in days, before a local copy of it, stored in an application-private directory,
     * will be considered rotten, and so not acceptable to be cached.
     * <p>
     * Those local copies of remote containers, whose life time is greater than this
     * field value, will be erased from the device storage in stead of being cached.
     * <p>
     * You can change this duration by generating the {@link SecureLoaderFactory} instance
     * with {@link SecureLoaderFactory#SecureLoaderFactory(android.content.Context, int)}.
     */
    public static final int DEFAULT_DAYS_BEFORE_CONTAINER_EXPIRATION = 5;
    private static final String HTTP_PROTOCOL_STRING = "http";
    private static final String HTTPS_PROTOCOL_STRING = "https";


    private Context context;

	// Used to compute the digest of different containers
	// in order to check which has been already cached.
	private MessageDigest messageDigest;

	private int daysBeforeContainerCacheExpiration;

	/**
	 * Creates a {@link SecureLoaderFactory} used to check and generate instances 
	 * from secure dynamic code loader classes.
	 * <p>
	 * It requires a {@link android.content.Context} (i.e. the launching activity) which
	 * should be used to manage and retrieve internal directories 
	 * of the application.
     * <p>
     * The number of days for which a local copy of a remote resource is considered acceptable
     * is set to the value of {@link SecureLoaderFactory#DEFAULT_DAYS_BEFORE_CONTAINER_EXPIRATION}.
	 * 
	 * @param parentContext
	 *  The content wrapper coming from the launching Activity.
	 */
	public SecureLoaderFactory(Context parentContext) {
	
		this(parentContext, DEFAULT_DAYS_BEFORE_CONTAINER_EXPIRATION);
	}
	
	/**
     * Creates a {@link SecureLoaderFactory} used to check and generate instances
     * from secure dynamic code loader classes.
     * <p>
     * It requires a {@link android.content.ContextWrapper} (i.e. the launching activity) which
     * should be used to manage and retrieve internal directories
     * of the application.
     * <p>
	 * It allows to decide the time interval in days before which a local copy of a remote
     * container, will be considered fresh and so acceptable to be cached.
	 * <p>
	 * If a negative value is provided for the second parameter, {@link SecureLoaderFactory} will
	 * throws an {@link IllegalArgumentException}.
	 * 
	 * @param parentContext
	 *  The content wrapper coming from the launching Activity.
	 * @param daysBeforeContainerCacheExpiration
	 *  The value in days for which a local copy of a remote container is considered fresh,
     *  thus acceptable to be cached.
     * @throws IllegalArgumentException if the number of days is not greater than zero
	 */
	public SecureLoaderFactory(Context parentContext, int daysBeforeContainerCacheExpiration) {

        checkArgument(
                daysBeforeContainerCacheExpiration > 0,
                "The number of days before considering a container rotten must be greater than zero");
        this.daysBeforeContainerCacheExpiration = daysBeforeContainerCacheExpiration;

		context = parentContext;

		try {
			messageDigest = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			Log.e(TAG_SECURE_FACTORY, "Wrong algorithm choice for message digest!");
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a {@link SecureDexClassLoader} that finds interpreted and native code in a set of
	 * provided locations (either local, or remote via HTTP, or HTTPS protocol) in dexPath.
	 * Interpreted classes are found in a set of DEX files contained in Jar or Apk files and 
	 * stored into an application-private directory.
	 * <p>
	 * Before executing one of these classes the signature of the target class is 
	 * verified against the certificate associated with its package name.
	 * Certificates location are provided by filling appropriately packageNameToCertificateMap;
	 * each package name must be linked with the remote location of the certificate that
	 * should be used to validate all the classes of that package. It's important 
	 * that each one of these locations uses HTTPS as its protocol; otherwise this 
	 * choice will be enforced!
	 * If a class package name do not match any of the provided entries in the map, 
	 * certificate location will be constructed by simply reverting package name and 
	 * transforming it into a web-based URL using HTTPS.
	 * <p>
	 * Note that this method returns {@code null} if no matching Jar, or Apk file is found at the
	 * provided dexPath parameter; otherwise a {@link SecureDexClassLoader} instance is returned.
	 * <p>
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
	 *  the list of directories containing native libraries; it may be {@code null}.
	 * @param parent
	 *  the parent class loader.
	 * @param packageNameToCertificateMap
	 *  a map that couples each package name to a URL which contains the certificate
	 *  that must be used to validate all the classes that belong to that package
	 *  before launching them at run time. Please notice that any URL in this map 
	 *  using HTTP protocol will be enforced to use HTTPS in stead.
	 * @return
	 *  a {@link SecureDexClassLoader} object which can be used to load dynamic code securely and 
	 *  uses a Eager strategy for container signature verification.
	 */
	public SecureDexClassLoader createDexClassLoader(
            @NonNull String dexPath,
			@Nullable String libraryPath,
            @NonNull ClassLoader parent,
            @NonNull Map<String, URL> packageNameToCertificateMap) {
		
		// The default behavior is EAGER evaluation.
		// In order to change it, simply modify the last boolean parameter from "false" to "true".
		return createDexClassLoader(dexPath, libraryPath, parent, packageNameToCertificateMap, false);
	}
	
	/**
	 * This method returns a {@link SecureDexClassLoader} instance in the same way as it is 
	 * explained in the {@link SecureLoaderFactory#createDexClassLoader(String, String, ClassLoader, Map)}. In addition it is 
	 * possible to specify the mode in which the signature verification process will be carried.
	 * <p>
	 * In particular by setting the performLazyEvaluation parameter on true, 
	 * a lazy verification process will be chosen. This means that the signature
	 * of the single container associated with the target class will be evaluated 
	 * only when {@link SecureDexClassLoader#loadClass(String)} will be invoked . 
	 * If this check succeeds the target class will be loaded.
	 * <p>
	 * On the other hand, by setting the parameter performLazyEvaluation to false 
	 * an eager evaluation will be carried out. This means that before returning this 
	 * object, the signature verification procedure will be carried out on all the 
	 * provided containers in a CONCURRENT way and all of those that do not succeed in the process will
	 * be blocked from loading their classes in the following loadClass() method calls.
	 * <p>
	 * The use of one mode in stead of the other is merely a performance-related choice.
	 * If you do not care that much about it, just invoke
	 * {@link SecureLoaderFactory#createDexClassLoader(String, String, ClassLoader, Map)} 
	 * which does not require to provide the performLazyEvaluation
	 * parameter and it will perform an Eager evaluation. Otherwise as a general guideline, 
	 * prefer the lazy mode whenever you think that you won't need to load classes 
	 * from all the provided containers.
	 * 
	 * @param dexPath
	 *  the list of jar/apk files containing classes and resources; these paths could
	 *  be either local URLs pointing to a location in the device or URLs that links
	 *  to a resource stored in the web via HTTP/HTTPS. In the latter case, if Internet
	 *  connectivity is available, the resource will be imported in a private-application 
	 *  directory before being used.
	 * @param libraryPath
	 *  the list of directories containing native libraries; it may be {@code null}.
	 * @param parent
	 *  the parent class loader.
	 * @param packageNameToCertificateMap
	 *  a map that couples each package name to a URL which contains the certificate
	 *  that must be used to validate all the classes that belong to that package
	 *  before launching them at run time. Please notice that any URL in this map 
	 *  using HTTP protocol will be enforced to use HTTPS in stead.
	 * @param performLazyEvaluation
	 *  the mode in which the verification will be handled. True for lazy verification;
	 *  false for the eager one.
	 * @return
	 * 	a {@link SecureDexClassLoader} object which can be used to load dynamic code securely and 
	 *  uses a either Lazy, or an Eager strategy for container signature verification depending
	 *  on the last parameter provided to this constructor.
	 */
	public SecureDexClassLoader createDexClassLoader(
            @NonNull String dexPath,
            @Nullable String libraryPath,
            @NonNull ClassLoader parent,
            @NonNull Map<String, URL> packageNameToCertificateMap,
			boolean performLazyEvaluation) {

        checkPreconditionsOnArgumentsForSecureDexClassLoaderCreation(
                dexPath, parent, packageNameToCertificateMap);

        // New container resources will be imported, or cached into this application private folder.
        File importedContainerDirectory = context.getDir(
                IMPORTED_CONTAINERS_PRIVATE_DIRECTORY_NAME, MODE_PRIVATE);
        Log.d(TAG_SECURE_FACTORY, "Download Resource Dir has been mounted at: " +
                importedContainerDirectory.getAbsolutePath());

        // Now the location of the final loaded classes is created.
        // Since it is assumed that the developer do not care where
        // exactly the dex classes will be stored, an application-private,
        // writable directory is created ad hoc.
        File dexOutputDirectory = context.getDir(OUTPUT_DEX_CLASSES_DIRECTORY_NAME, MODE_PRIVATE);
        Log.d(TAG_SECURE_FACTORY, "Dex Output Dir has been mounted at: " +
                dexOutputDirectory.getAbsolutePath());

        String finalDexPathString =
                processDexPathStringAndImportContainers(dexPath, importedContainerDirectory);

		// Til now libraryPath is left untouched..

        // Initialize the certificate factory
        CertificateFactory certificateFactory = null;
        try {
            certificateFactory = CertificateFactory.getInstance(X_509_CERTIFICATE);
        } catch (CertificateException e) {
            e.printStackTrace();
        }

		return new SecureDexClassLoader(
                finalDexPathString,
                new DexClassLoader(
                        finalDexPathString,
                        dexOutputDirectory.getAbsolutePath(),
                        libraryPath,
                        parent),
                context,
                processPackageNameToCertificateMap(packageNameToCertificateMap),
				performLazyEvaluation,
                new ContainerSignatureVerifier(
                        context.getPackageManager(), certificateFactory),
                certificateFactory);
	}

    private static void checkPreconditionsOnArgumentsForSecureDexClassLoaderCreation(
            @NonNull String dexPath,
            @NonNull ClassLoader parent,
            @NonNull Map<String, URL> packageNameToCertificateMap) {
        checkNotNull(dexPath, "The dex path string of the containers used as source must not be null");
        checkArgument(
                !dexPath.isEmpty(),
                "The dex path string of the containers used as source must not be empty");

        checkNotNull(parent, "The parent class loader must not be null");

        checkNotNull(
                packageNameToCertificateMap,
                "The map associating package names with the URL of the certificate for " +
                        "signature check must not be null");
        checkArgument(
                !packageNameToCertificateMap.isEmpty(),
                "The map associating package names with the URL of the certificate for " +
                        "signature check must have at least one package name as key");
    }

    private String processDexPathStringAndImportContainers(String dexPath, File importedContainerDir) {
        StringBuilder finalDexPathStringBuilder = new StringBuilder();

        CacheLogger remoteContainersCacheLogger = new CacheLogger(
                importedContainerDir.getAbsolutePath(), daysBeforeContainerCacheExpiration);

        DexPathStringProcessor dexPathStringProcessor = new DexPathStringProcessor(dexPath);

        while (dexPathStringProcessor.hasNextDexPathString()) {

            String singleDexPath = dexPathStringProcessor.nextDexPathString();
            Optional<String> optionalSuccessfullyProcessedSingleDexPath;

			if (isARemoteHttpOrHttpsResource(singleDexPath)) {

                optionalSuccessfullyProcessedSingleDexPath = processPathPointingToARemoteContainer(
                        singleDexPath, importedContainerDir, remoteContainersCacheLogger);
            }
			else {

                optionalSuccessfullyProcessedSingleDexPath = processPathPointingToALocallyStoredContainer(
                        singleDexPath, importedContainerDir);
            }

            if (optionalSuccessfullyProcessedSingleDexPath.isPresent()) {

                finalDexPathStringBuilder
                        .append(optionalSuccessfullyProcessedSingleDexPath.get())
                        .append(File.pathSeparator);
                Log.d(TAG_SECURE_FACTORY, "Dex Path has been modified into: " + finalDexPathStringBuilder);
            }
		}

        // Remove the last unnecessary separator from finalDexPathStringBuilder
        // (if finalDexPathStringBuilder has at least one path inside)
        if (finalDexPathStringBuilder.lastIndexOf(File.pathSeparator) != -1)
            finalDexPathStringBuilder.deleteCharAt(finalDexPathStringBuilder.lastIndexOf(File.pathSeparator));

        // Finalize the CacheLogger object and update the helper file on the device
        remoteContainersCacheLogger.finalizeLog();

        return finalDexPathStringBuilder.toString();
    }

    private static boolean isARemoteHttpOrHttpsResource(String resourcePath) {
        return resourcePath.startsWith(HTTP_PROTOCOL_STRING) ||
                resourcePath.startsWith(HTTPS_PROTOCOL_STRING);
    }

    private Optional<String> processPathPointingToARemoteContainer(
            String singleDexPath,
            File importedContainerDir,
            CacheLogger remoteContainersCacheLogger) {
        try {
            URL currentSingleDexPathAsURL = new URL(singleDexPath);

            Optional<String> cachedContainerFileName =
                    remoteContainersCacheLogger.checkForCachedEntry(currentSingleDexPathAsURL);

            if (cachedContainerFileName.isPresent()) {

                // A fresh enough cached copy of the remote container is present
                // on the device storage, so this copy can be used in stead of downloading
                // the remote container again.
                return Optional.of(importedContainerDir.getAbsolutePath() + File.separator +
                        cachedContainerFileName.get());
            } else {

                // No cached copy so it is necessary to download the remote resource from the web.

                //Trace.beginSection("Download Container");
                // Log.i("Profile","[Start]	Download Container: " + System.currentTimeMillis() + " ms.");
                String downloadedContainerPath = downloadContainerIntoFolder(singleDexPath, importedContainerDir);
                // Log.i("Profile","[End]	Download Container: " + System.currentTimeMillis() + " ms.");
                //Trace.endSection(); // end of "Download Container" section

                if (downloadedContainerPath != null) {

                    // In such a case the download was successful.
                    // Now the downloaded container is renamed according to its finger print.
                    String containerDigest = computeDigestFromFilePath(downloadedContainerPath);

                    File downloadedContainer = new File(downloadedContainerPath);

                    if (containerDigest == null) {

                        // Fingerprint computation fails. Delete the resource container and do not add
                        // this file to the singleDexPath
                        if (!downloadedContainer.delete())
                            Log.w(TAG_SECURE_FACTORY, "Issue while deleting " + downloadedContainerPath);
                    } else {

                        // Compute the extension of the file.
                        String extension = extractExtensionFromFilePath(downloadedContainerPath).get();

                        // Rename the previous container file according to the containerDigest and its extension.
                        String downloadedContainerFinalPath = importedContainerDir.getAbsolutePath() + File.separator + containerDigest + extension;

                        File downloadContainerFinalPosition = new File(downloadedContainerFinalPath);

                        if (downloadContainerFinalPosition.exists())
                            if (!downloadContainerFinalPosition.delete())
                                Log.w(TAG_SECURE_FACTORY, "Issue while deleting " + downloadedContainerFinalPath);

                        if (downloadedContainer.renameTo(downloadContainerFinalPosition)) {

                            // It is relevant to add this resource to the Log file of the cached remote containers.
                            remoteContainersCacheLogger.addCachedEntryToLog(currentSingleDexPathAsURL, containerDigest + extension);

                            // Successful renaming..
                            // It is necessary to replace the current web-like singleDexPath to access the resource with the new local version.
                            return Optional.of(downloadedContainerFinalPath);

                        } else {
                            // Renaming operation failed..
                            // Erase downloaded container.
                            if (!downloadedContainer.delete())
                                Log.w(TAG_SECURE_FACTORY, "Issue while deleting " + downloadedContainerPath);

                            return Optional.absent();
                        }
                    }
                }
            }
        } catch (MalformedURLException e) {
            Log.d(
                    TAG_SECURE_FACTORY,
                    "The provided singleDexPath " + singleDexPath + " is not a valid remote URL");
        }

        return Optional.absent();
    }

    private Optional<String> processPathPointingToALocallyStoredContainer(
            String singleDexPath, File importedContainerDir) {
        // On the other hand when the developer provides a local URI for the container
        // SecureLoaderFactory has to import this file into an application private folder on the device.

        // At first compute the digest on the provided local container.
        String encodedContainerDigest = null;

        // If a container exists on the device storage, compute its digest.
        if (new File(singleDexPath).exists()) encodedContainerDigest = computeDigestFromFilePath(singleDexPath);

        // Take this branch if the digest was correctly computed on the container..
        if (encodedContainerDigest != null) {

            // Compute the extension of the file.
            String extension = extractExtensionFromFilePath(singleDexPath).get();

            // Check if a file whose name is "encodedContainerDigest.(jar/apk)" is already present in
            // the cached certificate folder.
            File[] matchingContainerArray = importedContainerDir.listFiles(new FileFilterByNameMatch(encodedContainerDigest, extension));

            if (matchingContainerArray != null && matchingContainerArray.length > 0) {

                // A cached version of the container already exists.
                // So simply use that cached version
                return Optional.of(matchingContainerArray[0].getAbsolutePath());
            }
            else {

                // No cached copy of the container in the directory.
                // So it is necessary to import the container into the application folder
                // before using it.
                InputStream inStream = null;
                OutputStream outStream = null;
                String cachedContainerPath = importedContainerDir.getAbsolutePath() + File.separator + encodedContainerDigest + extension;

                try {

                    inStream = new FileInputStream(singleDexPath);
                    outStream = new FileOutputStream(cachedContainerPath);

                    byte[] buf = new byte[8192];
                    int length;

                    // Copying the external container into the application
                    // private folder.
                    while ((length = inStream.read(buf)) > 0) {
                        outStream.write(buf, 0, length);
                    }

                    // In the end add the internal singleDexPath of the container
                    return Optional.of(cachedContainerPath);

                } catch (FileNotFoundException e) {
                    Log.w(TAG_SECURE_FACTORY, "Problem in locating container to import in the application private folder!");
                } catch (IOException e) {
                    Log.w(TAG_SECURE_FACTORY, "Problem while importing a local container into the application private folder!");
                } finally {

                    try {

                        if (inStream != null) {
                            inStream.close();
                        }
                        if (outStream != null) {
                            outStream.close();
                        }

                    } catch (IOException e) {
                        Log.w(TAG_SECURE_FACTORY, "Issue in closing file streams while importing a container!");
                    }
                }
            }
        }

        return Optional.absent();
    }

    // Given the path of a file this function returns the encoded base 64 of SHA-1 digest of the file.
	private String computeDigestFromFilePath(String filePath) {
		
		FileInputStream inStream = null;
		String digestString = null;
		
		try {
			// A stream used to parse the bytes of the file
			inStream = new FileInputStream(filePath);
			
			byte[] buffer = new byte[8192];
		    int length;
		    while( (length = inStream.read(buffer)) != -1 ) {
		    	// File is loaded by considering chunks of it..
		    	messageDigest.update(buffer, 0, length);
		    }
		    // The digest is finally computed..
		    byte[] digestBytes = messageDigest.digest();
		    
		    // ..and translated into a human readable string through Base64 encoding (Url safe).
		    // Also remove the last /n part of the string
		    digestString = Base64.encodeToString(digestBytes, Base64.URL_SAFE)
                    .replace(System.getProperty("line.separator"), "")
                    .replace("\r", "");

		} catch (FileNotFoundException e) {
			Log.w(TAG_SECURE_FACTORY, "No file found at " + filePath);
		} catch (IOException e) {
			Log.w(TAG_SECURE_FACTORY, "Something went wrong while calculating the digest!");
		} finally {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
					Log.w(TAG_SECURE_FACTORY, "Issue while closing file stream in message digest computation!");
				}
			}
		}
		
		// Finally return the digest string..
		return digestString;
	}

	private Map<String, URL> processPackageNameToCertificateMap(
            Map<String, URL> packageNameToCertificateMap) {
		Map<String, URL> sanitizedPackageNameToCertificateMap = new HashMap<>();
		
        for (String currentPackageName : packageNameToCertificateMap.keySet()) {
            if (isAValidPackageName(currentPackageName)) {
                try {
                    URL certificateURL = packageNameToCertificateMap.get(currentPackageName);

                    if (certificateURL != null) {
                        // If the URL for the certificate is present, check that it is valid one,
                        // and enforce HTTPS protocol..
                        if (certificateURL.getProtocol().equals(HTTP_PROTOCOL_STRING) ||
                                certificateURL.getProtocol().equals(HTTPS_PROTOCOL_STRING)) {
                            sanitizedPackageNameToCertificateMap.put(
                                    currentPackageName,
                                    new URL(
                                            HTTPS_PROTOCOL_STRING,
                                            certificateURL.getHost(),
                                            certificateURL.getPort(),
                                            certificateURL.getFile()));
                        }
                    } else {
                        // Otherwise, revert the package name, and use it as the certificate URL.
                        sanitizedPackageNameToCertificateMap.put(
                                currentPackageName, revertPackageNameToURL(currentPackageName));
                    }
                } catch (MalformedURLException e) {
                    // Ignore this package name if an exception on the URL is raised.
                    Log.w(TAG_SECURE_FACTORY, "Issue while enforcing certificate URL " +
                            packageNameToCertificateMap.get(currentPackageName) +
                            " to use HTTPS protocol");
                }
            }
        }
		
		return sanitizedPackageNameToCertificateMap;
	}

	private String downloadContainerIntoFolder(String urlPath, File resOutputDir) {
		
		// Precondition check on URL path variable..
		if (urlPath == null) return null;
		
		// Precondition check on the output local folder..
		if (resOutputDir == null || !resOutputDir.exists()) return null;
		if (!resOutputDir.isDirectory() || !resOutputDir.canRead() || !resOutputDir.canWrite()) return null;

        if (!isARemoteHttpOrHttpsResource(urlPath)) return null;

        URL url;
        try {
            url = new URL(urlPath);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }

        // Check whether the path of the file pointed by the URL is not empty
        if (url.getFile().isEmpty()) return null;

        String containerName = extractFileNameFromFilePath(url.getFile());

        // Check whether the name of the file pointed by the URL is not empty
        if (containerName.isEmpty()) return null;
		
		// Check whether the selected resource is a container (jar or apk)
        Optional<String> optionalContainerExtension = extractExtensionFromFilePath(containerName);

        if (optionalContainerExtension.isPresent() &&
                !endsWithJarOrApkExtension(optionalContainerExtension.get())) {
            return null;
        }

        // Check that no file is present at this location.
		File checkFile = new File(resOutputDir.getAbsolutePath() + containerName);
		
		// In case, just delete the old file..
		if (checkFile.exists())
			checkFile.delete();
		
		// Finally the container file can be downloaded from the URL
		// and stored in the local folder
		String localContainerPath = resOutputDir.getAbsolutePath() + containerName;

        FileDownloader fileDownloader = new FileDownloader(context);
		
		// Redirect may be allowed here while downloading a remote container..
		boolean isDownloadSuccessful = fileDownloader.downloadRemoteResource(url, localContainerPath, true);
		
		if (isDownloadSuccessful) {
			
			// If this branch is reached, the download worked properly and the path of the output
			// file container is returned.
			if (!optionalContainerExtension.isPresent()) {

				// In such a situation, try to identify the extension of the downloaded file
				Optional<String> retrievedFileExtension = fileDownloader.getDownloadedFileExtension();
				
				// Check that an extension was found and it is a suitable one..
				if (retrievedFileExtension.isPresent() &&
                        endsWithJarOrApkExtension(retrievedFileExtension.get())) {

					// In such a case rename the previous file by adding the extension
					File containerToRename = new File(localContainerPath);
					File finalContainerWithExtension = new File(localContainerPath + retrievedFileExtension);
					
					if (finalContainerWithExtension.exists())
						if (!finalContainerWithExtension.delete())
							Log.w(TAG_SECURE_FACTORY, "Issue while deleting " + finalContainerWithExtension);
					
					if (!containerToRename.renameTo(finalContainerWithExtension)) {
						
						// Renaming operation failed..
						// Erase downloaded container.
						if (!containerToRename.delete())
							Log.w(TAG_SECURE_FACTORY, "Issue while deleting " + localContainerPath);
						
						return null;
					}
						
					// Return the local path to the renamed container.
					return localContainerPath + retrievedFileExtension;
				}
			} else {
				
				// An extension is already present so just return the path..
				return localContainerPath;
			}
		}
		
		// Return null if any of the download steps failed.
		return null;
	}
}
