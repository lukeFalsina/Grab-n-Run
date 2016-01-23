package it.necst.grabnrun;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static it.necst.grabnrun.FileHelper.extractFileNameFromFilePath;
import static it.necst.grabnrun.SecureLoaderFactory.X_509_CERTIFICATE;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

// TODO(falsinal): Once the code of the tested classes will be simplified, evaluate
// whether to add extra test cases for APK containers

@RunWith(MockitoJUnitRunner.class)
public class ContainerSignatureVerifierTest {

    @Rule public TemporaryFolder temporaryResourceFolder = new TemporaryFolder();

    private static final String EMPTY_CONTAINER_PATH = "";

    private static final String UNSIGNED_JAR_CONTAINER_URL_AS_STRING =
            "https://dl.dropboxusercontent.com/s/ofl0q47rhggfugg/unsigned-jsoup-1.8.3.jar";
    private static final String REPACKAGED_JAR_CONTAINER_SIGNED_WITH_TRUSTED_CERTIFICATE_URL_AS_STRING =
            "https://dl.dropboxusercontent.com/s/dm0v5gnrli6fu31/componentModifierRepack.jar";
    private static final String JAR_CONTAINER_SIGNED_WITH_TRUSTED_CERTIFICATE_URL_AS_STRING =
            "https://dl.dropboxusercontent.com/u/28681922/componentModifier.jar";
    private static final String TRUSTED_CERTIFICATE_URL_AS_STRING =
            "https://dl.dropboxusercontent.com/u/28681922/test_cert.pem";
    private static final String A_DIFFERENT_TRUSTED_CERTIFICATE_URL_AS_STRING =
            "https://dl.dropboxusercontent.com/s/px59dqbwsbhvln5/verify_cert.pem";

    @Mock PackageManager mockPackageManager = mock(PackageManager.class);
    @Mock Context mockContext = mock(Context.class);
    @Mock ConnectivityManager mockConnectivityManager = mock(ConnectivityManager.class);
    @Mock NetworkInfo mockNetworkInfo = mock(NetworkInfo.class);

    ContainerSignatureVerifier testContainerSignatureVerifier;
    X509Certificate trustedCertificate;

    @Before
    public void initializeTestContainerSignatureVerifierAndSetupMocks() throws Exception {

        testContainerSignatureVerifier = new ContainerSignatureVerifier(
                mockPackageManager, CertificateFactory.getInstance(X_509_CERTIFICATE));

        when(mockContext.getSystemService(CONNECTIVITY_SERVICE))
                .thenReturn(mockConnectivityManager);
        when(mockConnectivityManager.getActiveNetworkInfo()).thenReturn(mockNetworkInfo);
        when(mockNetworkInfo.isConnected()).thenReturn(true);

        trustedCertificate = retrieveAndGenerateTrustedCertificate(TRUSTED_CERTIFICATE_URL_AS_STRING);
    }

    @Test (expected = IllegalArgumentException.class)
    public void givenAnEmptyContainerPath_whenVerifyContainerAgainstCertificate_thenThrows() {
        // GIVEN + WHEN
        testContainerSignatureVerifier
                .verifyContainerSignatureAgainstCertificate(EMPTY_CONTAINER_PATH, trustedCertificate);
    }

    @Test (expected = IllegalArgumentException.class)
    public void givenAnUnsignedJarContainer_whenVerifyContainerAgainstCertificate_thenThrows() {
        // GIVEN
        String containerPathPointingToNoFile = temporaryResourceFolder.getRoot()
                + File.pathSeparator + "NotExistingContainer.jar";
        assertFalse(new File(containerPathPointingToNoFile).exists());

        // WHEN
        testContainerSignatureVerifier
                .verifyContainerSignatureAgainstCertificate(
                        containerPathPointingToNoFile, trustedCertificate);
    }

    @Test
    public void givenAnUnsignedJarContainer_whenVerifyContainerAgainstCertificate_thenReturnsFalse() throws Exception {
        // GIVEN + WHEN
        boolean isContainerVerificationSuccessful =
                testContainerSignatureVerifier.verifyContainerSignatureAgainstCertificate(
                        downloadRemoteResourceIntoTemporaryFolder(
                                UNSIGNED_JAR_CONTAINER_URL_AS_STRING),
                        trustedCertificate);

        // THEN
        assertFalse(isContainerVerificationSuccessful);
    }
    @Test
    public void givenAJarContainerSignedWithAnOtherTrustedCertificate_whenVerifyContainerAgainstCertificate_thenReturnsFalse() throws Exception {
        // GIVEN
        X509Certificate aDifferentTrustedCertificate =
                retrieveAndGenerateTrustedCertificate(A_DIFFERENT_TRUSTED_CERTIFICATE_URL_AS_STRING);

        // WHEN
        boolean isContainerVerificationSuccessful =
                testContainerSignatureVerifier.verifyContainerSignatureAgainstCertificate(
                        downloadRemoteResourceIntoTemporaryFolder(
                                JAR_CONTAINER_SIGNED_WITH_TRUSTED_CERTIFICATE_URL_AS_STRING),
                        aDifferentTrustedCertificate);

        // THEN
        assertFalse(isContainerVerificationSuccessful);
    }

    @Test
    public void givenARepackagedJarContainerPreviouslySignedWithTheTrustedCertificate_whenVerifyContainerAgainstCertificate_thenReturnsFalse() throws Exception {
        // GIVEN + WHEN
        boolean isContainerVerificationSuccessful =
                testContainerSignatureVerifier.verifyContainerSignatureAgainstCertificate(
                        downloadRemoteResourceIntoTemporaryFolder(
                                REPACKAGED_JAR_CONTAINER_SIGNED_WITH_TRUSTED_CERTIFICATE_URL_AS_STRING),
                        trustedCertificate);

        // THEN
        assertFalse(isContainerVerificationSuccessful);
    }

    @Test
    public void givenAJarContainerSignedWithTheTrustedCertificate_whenVerifyContainerAgainstCertificate_thenReturnsTrue() throws Exception {
        // GIVEN + WHEN
        boolean isContainerVerificationSuccessful =
                testContainerSignatureVerifier.verifyContainerSignatureAgainstCertificate(
                        downloadRemoteResourceIntoTemporaryFolder(
                                JAR_CONTAINER_SIGNED_WITH_TRUSTED_CERTIFICATE_URL_AS_STRING),
                        trustedCertificate);

        // THEN
        assertTrue(isContainerVerificationSuccessful);
    }

    private X509Certificate retrieveAndGenerateTrustedCertificate(
            String remoteCertificateURLAsString) throws Exception {
        String trustedCertificatePath =
                downloadRemoteResourceIntoTemporaryFolder(remoteCertificateURLAsString);

        InputStream inStream = new FileInputStream(trustedCertificatePath);
        X509Certificate trustedCertificate = (X509Certificate) CertificateFactory
                .getInstance(X_509_CERTIFICATE)
                .generateCertificate(inStream);

        assertNotNull(inStream);
        inStream.close();

        return trustedCertificate;
    }

    private String downloadRemoteResourceIntoTemporaryFolder(String remoteURLAsString) throws Exception {
        URL remoteURL = new URL(remoteURLAsString);

        String localContainerURI = temporaryResourceFolder.getRoot().toString()
                + File.separator + extractFileNameFromFilePath(remoteURL.getPath());

        boolean isRemoteResourceSuccessful = new FileDownloader(mockContext).downloadRemoteResource(
                remoteURL,
                localContainerURI,
                false);

        assertTrue(isRemoteResourceSuccessful);
        return localContainerURI;
    }

}