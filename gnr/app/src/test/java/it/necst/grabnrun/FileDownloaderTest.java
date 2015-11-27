package it.necst.grabnrun;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.test.mock.MockContext;

import com.google.common.base.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.net.URL;

@RunWith(MockitoJUnitRunner.class)
public class FileDownloaderTest {

    private static final String TEST_REMOTE_CERTIFICATE_URL_AS_STRING =
            "https://dl.dropboxusercontent.com/u/28681922/test_cert.pem";
    private static final String TEST_REMOTE_CERTIFICATE_URL_NEEDING_REDIRECT_AS_STRING =
            "http://bit.ly/1Yh5kft";
    private static final String TEST_REMOTE_URL_USING_UNSUPPORTED_FTP_PROTOCOL_AS_STRING =
            "ftp://ftp.funet.fi/pub/standards/RFC/rfc959.txt";

    private static final String LOCAL_FILE_NAME = "localFile";

    @Rule public TemporaryFolder temporaryTestFolder = new TemporaryFolder();

    @Mock MockContext mockContext = new MockContext();
    @Mock ConnectivityManager mockConnectivityManager = mock(ConnectivityManager.class);
    @Mock NetworkInfo mockNetworkInfo = mock(NetworkInfo.class);

    FileDownloader testFileDownloader;

    @Before
    public void setupMocksForFileDownloader() {
        when(mockContext.getSystemService(Context.CONNECTIVITY_SERVICE))
                .thenReturn(mockConnectivityManager);
        when(mockConnectivityManager.getActiveNetworkInfo()).thenReturn(mockNetworkInfo);
        when(mockNetworkInfo.isConnected()).thenReturn(true);
        testFileDownloader = new FileDownloader(mockContext);
    }

    @Test
    public void givenNoNetworkConnectivityIsAvailable_whenDownloadRemoteResource_thenDownloadFailsAndRetrievesNoFile()
            throws Exception {
        // GIVEN
        when(mockNetworkInfo.isConnected()).thenReturn(false);
        final String localFileURIInTemporaryTestDirectory =
                getLocalFileURIInTemporaryTestDirectory(LOCAL_FILE_NAME);

        // WHEN
        final boolean wasDownloadSuccessful = testFileDownloader.downloadRemoteResource(
                new URL(TEST_REMOTE_CERTIFICATE_URL_AS_STRING),
                localFileURIInTemporaryTestDirectory,
                false);

        // THEN
        assertFalse(wasDownloadSuccessful);
        assertFalse(fileExists(localFileURIInTemporaryTestDirectory));
    }

    @Test (expected = IllegalArgumentException.class)
    public void givenNetworkConnectivityIsAvailable_whenDownloadRemoteResourceWithTestURLUsingUnsupportedProtocol_thenThrows()
            throws Exception {
        // GIVEN
        final String localFileURIInTemporaryTestDirectory =
                getLocalFileURIInTemporaryTestDirectory(LOCAL_FILE_NAME);

        // WHEN
        testFileDownloader.downloadRemoteResource(
                new URL(TEST_REMOTE_URL_USING_UNSUPPORTED_FTP_PROTOCOL_AS_STRING),
                localFileURIInTemporaryTestDirectory,
                false);
    }

    @Test
    public void givenNetworkConnectivityIsAvailable_whenDownloadRemoteResourceWithTestRemoteCertificateURL_thenDownloadSucceedsAndRetrievesAFile()
            throws Exception {
        // GIVEN
        final String localFileURIInTemporaryTestDirectory =
                getLocalFileURIInTemporaryTestDirectory(LOCAL_FILE_NAME);

        // WHEN
        final boolean wasDownloadSuccessful = testFileDownloader.downloadRemoteResource(
                new URL(TEST_REMOTE_CERTIFICATE_URL_AS_STRING),
                localFileURIInTemporaryTestDirectory,
                false);

        // THEN
        assertTrue(wasDownloadSuccessful);
        assertTrue(fileExists(localFileURIInTemporaryTestDirectory));
    }

    @Test
    public void givenNetworkConnectivityIsAvailableAndNoRedirectIsAllowed_whenDownloadRemoteResourceWithTestRemoteCertificateURLNeedingRedirect_thenDownloadFailsAndRetrievesNoFile()
            throws Exception {
        // GIVEN
        final String localFileURIInTemporaryTestDirectory =
                getLocalFileURIInTemporaryTestDirectory(LOCAL_FILE_NAME);
        final boolean redirectIsNotAllowed = false;

        // WHEN
        final boolean wasDownloadSuccessful = testFileDownloader.downloadRemoteResource(
                new URL(TEST_REMOTE_CERTIFICATE_URL_NEEDING_REDIRECT_AS_STRING),
                localFileURIInTemporaryTestDirectory,
                redirectIsNotAllowed);

        // THEN
        assertFalse(wasDownloadSuccessful);
        assertFalse(fileExists(localFileURIInTemporaryTestDirectory));
    }

    @Test
    public void givenNetworkConnectivityIsAvailableAndRedirectIsAllowed_whenDownloadRemoteResourceWithTestRemoteCertificateURLNeedingRedirect_thenDownloadSucceedsAndRetrievesAFile()
            throws Exception {
        // GIVEN
        final String localFileURIInTemporaryTestDirectory =
                getLocalFileURIInTemporaryTestDirectory(LOCAL_FILE_NAME);
        final boolean redirectIsAllowed = true;

        // WHEN
        final boolean wasDownloadSuccessful = testFileDownloader.downloadRemoteResource(
                new URL(TEST_REMOTE_CERTIFICATE_URL_NEEDING_REDIRECT_AS_STRING),
                localFileURIInTemporaryTestDirectory,
                redirectIsAllowed);

        // THEN
        assertTrue(wasDownloadSuccessful);
        assertTrue(fileExists(localFileURIInTemporaryTestDirectory));
    }

    @Test
    public void givenNoRemoteResourceWasPreviouslyDownloaded_whenGetDownloadedFileExtension_thenReturnsAnOptionalAbsent() {
        // WHEN
        final Optional<String> optionalDownloadedFileExtension =
                testFileDownloader.getDownloadedFileExtension();

        // THEN
        assertThat(optionalDownloadedFileExtension, equalTo(Optional.<String>absent()));
    }

    @Test
    public void givenRemoteResourceWasPreviouslyDownloaded_whenGetDownloadedFileExtension_thenReturnsAnOptionalStringMatchingTheFileExtension()
            throws Exception {
        // GIVEN
        testFileDownloader.downloadRemoteResource(
                new URL(TEST_REMOTE_CERTIFICATE_URL_AS_STRING),
                getLocalFileURIInTemporaryTestDirectory(LOCAL_FILE_NAME),
                false);
        // WHEN
        final Optional<String> optionalDownloadedFileExtension =
                testFileDownloader.getDownloadedFileExtension();

        // THEN
        final String expectedFileExtensionForCertificateFile = ".pem";
        assertThat(
                optionalDownloadedFileExtension,
                equalTo(Optional.of(expectedFileExtensionForCertificateFile)));
    }

    @Test
    public void givenRemoteResourceRequiringRedirectWasPreviouslyDownloaded_whenGetDownloadedFileExtension_thenReturnsAnOptionalStringMatchingTheFileExtension()
            throws Exception {
        // GIVEN
        final boolean redirectIsAllowed = true;
        testFileDownloader.downloadRemoteResource(
                new URL(TEST_REMOTE_CERTIFICATE_URL_NEEDING_REDIRECT_AS_STRING),
                getLocalFileURIInTemporaryTestDirectory(LOCAL_FILE_NAME),
                redirectIsAllowed);
        // WHEN
        final Optional<String> optionalDownloadedFileExtension =
                testFileDownloader.getDownloadedFileExtension();

        // THEN
        final String expectedFileExtensionForCertificateFile = ".pem";
        assertThat(
                optionalDownloadedFileExtension,
                equalTo(Optional.of(expectedFileExtensionForCertificateFile)));
    }

    private String getLocalFileURIInTemporaryTestDirectory(String localFileName) {
        return temporaryTestFolder.getRoot().getPath() + File.separator + localFileName;
    }

    private static boolean fileExists(String path) {
        return new File(path).exists();
    }
}
