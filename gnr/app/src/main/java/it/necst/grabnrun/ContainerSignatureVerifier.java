package it.necst.grabnrun;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static it.necst.grabnrun.FileHelper.APK_EXTENSION;
import static it.necst.grabnrun.FileHelper.JAR_EXTENSION;
import static it.necst.grabnrun.FileHelper.extractExtensionFromFilePath;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.common.base.Optional;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

class ContainerSignatureVerifier {
    private static final String TAG_CONTAINER_SIGNATURE_VERIFIER =
            ContainerSignatureVerifier.class.getSimpleName();

    private final PackageManager packageManager;
    private final CertificateFactory certificateFactory;

    ContainerSignatureVerifier(
            @NonNull PackageManager packageManager,
            @NonNull CertificateFactory certificateFactory) {
        this.packageManager = checkNotNull(packageManager);
        this.certificateFactory = checkNotNull(certificateFactory);
    }

    // TODO(falsinal): It may be possible to simplify the logic here by
    // removing the APK branch

    // Given the path to a jar/apk container and a valid certificate instance this method returns
    // whether the container is signed properly against the verified certificate.
    boolean verifyContainerSignatureAgainstCertificate(
            @NonNull String containerPath,
            @NonNull X509Certificate trustedCertificate) {
        checkNotNull(containerPath);
        checkArgument(
                !containerPath.isEmpty() && (new File(containerPath).exists()),
                "The container path must be associated to an existing file");
        checkNotNull(trustedCertificate);

        // Check whether the selected resource is a jar or apk container
        Optional<String> optionalExtension = extractExtensionFromFilePath(containerPath);
        if (!optionalExtension.isPresent()) {
            return false;
        }
        String extension = optionalExtension.get();

        boolean signatureCheckIsSuccessful = false;

        // Depending on the container extension the process for
        // signature verification changes
        if (extension.equals(APK_EXTENSION)) {

            // APK container case:
            // At first look for the certificates used to sign the apk
            // and check whether at least one of them is the verified one..

            PackageInfo mPackageInfo =
                    packageManager.getPackageArchiveInfo(containerPath, PackageManager.GET_SIGNATURES);

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
                                if (certFromSign.equals(trustedCertificate))
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
                    Log.i(TAG_CONTAINER_SIGNATURE_VERIFIER, "An invalid/corrupted signature is associated with the source archive.");
                }
            } else {
                Log.i(TAG_CONTAINER_SIGNATURE_VERIFIER, "An invalid/corrupted container was found.");
            }
        }

        // This branch must be taken by all jar containers and by those apk containers
        // whose certificates list contains also the trusted verified certificate.
        if (extension.equals(JAR_EXTENSION) ||
                (extension.equals(APK_EXTENSION) && signatureCheckIsSuccessful)) {

            // Verify that each entry of the container has been signed properly
            JarFile containerToVerify = null;

            try {

                containerToVerify = new JarFile(containerPath);
                // This method will throw an IOException whenever
                // the JAR container was not signed with the trusted certificate
                // N.B. apk are an extension of a jar container..
                verifyJARContainer(containerToVerify, trustedCertificate);

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

        return signatureCheckIsSuccessful;
    }

    private void verifyJARContainer(
            @NonNull JarFile jarFile,
            @NonNull X509Certificate trustedCertificate) throws IOException {

        Vector<JarEntry> entriesVec = new Vector<>();

        // Ensure the jar file is at least signed.
        Manifest man = jarFile.getManifest();
        if (man == null) {
            Log.i(TAG_CONTAINER_SIGNATURE_VERIFIER, jarFile.getName() + "is not signed.");
            throw new SecurityException("The container is not signed");
        }

        // Ensure all the entries' signatures verify correctly
        byte[] buffer = new byte[8192];
        Enumeration<JarEntry> entries = jarFile.entries();

        while (entries.hasMoreElements()) {

            // Current entry in the jar container
            JarEntry je = entries.nextElement();

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
                    Log.i(TAG_CONTAINER_SIGNATURE_VERIFIER, signedEntry.getName() + " is an unsigned class file");
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
                        Log.i(TAG_CONTAINER_SIGNATURE_VERIFIER, "One of the certificates used to sign " + signedEntry.getName() + " is expired");
                        throw new SecurityException("One of the used certificates is expired!");
                    } catch (Exception e) {
                        // It was impossible to cast the general certificate into an X.509 one..
                    }

                    if (signerCert.equals(trustedCertificate))
                        // The trusted certificate was used to sign this entry
                        signedAsExpected = true;
                }

                if (!signedAsExpected) {
                    Log.i(TAG_CONTAINER_SIGNATURE_VERIFIER, "The trusted certificate was not used to sign " + signedEntry.getName());
                    throw new SecurityException("The provider is not signed by a trusted signer");
                }
            }
        }
    }
}
