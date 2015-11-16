package it.necst.grabnrun;

import static it.necst.grabnrun.CertificateFileFilterByNameMatch.PEM_EXTENSION;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;

@RunWith(MockitoJUnitRunner.class)
public class CertificateFileFilterByNameMatchTest {

    public static final String EMPTY_CERTIFICATE_NAME = "";
    private static final String TEST_CERTIFICATE_NAME = "testCertificate";
    private static final String CERTIFICATE_WITH_MATCHING_NAME_BUT_UNSUPPORTED_EXTENSION =
            TEST_CERTIFICATE_NAME + ".cert";
    private static final String CERTIFICATE_WITH_SUPPORTED_PEM_EXTENSION_BUT_NOT_MATCHING_NAME =
            "anotherTestCertificate" + PEM_EXTENSION;
    private static final String CERTIFICATE_WITH_MATCHING_NAME_AND_SUPPORTED_PEM_EXTENSION =
            TEST_CERTIFICATE_NAME + PEM_EXTENSION;

    @Mock File fileMock;
    CertificateFileFilterByNameMatch testCertFileFilter;

    @Before
    public void initializeCertificateFileFilterWithTestCertificateName() {
        testCertFileFilter = new CertificateFileFilterByNameMatch(TEST_CERTIFICATE_NAME);
    }

    @Test (expected = IllegalArgumentException.class)
    public void givenAnEmptyFileName_whenCreateCertificateFileFilter_thenThrows() {
        new CertificateFileFilterByNameMatch(EMPTY_CERTIFICATE_NAME);
    }

    @Test
    public void givenADirectory_whenAccept_thenReturnFalse() {
        // GIVEN
        setupFileMockAsADirectory(true);

        // WHEN
        boolean isAccepted = testCertFileFilter.accept(fileMock);

        // THEN
        assertFalse(isAccepted);
    }

    @Test
    public void givenAFileWithMatchingFileNameButUnsupportedExtension_whenAccept_thenReturnFalse() {
        // GIVEN
        setupFileMockAsADirectory(false);
        when(fileMock.getName())
                .thenReturn(CERTIFICATE_WITH_MATCHING_NAME_BUT_UNSUPPORTED_EXTENSION);

        // WHEN
        boolean isAccepted = testCertFileFilter.accept(fileMock);

        // THEN
        assertFalse(isAccepted);
    }

    @Test
    public void givenAFileWithSupportedExtensionButNotMatchingFileName_whenAccept_thenReturnFalse() {
        // GIVEN
        setupFileMockAsADirectory(false);
        when(fileMock.getName())
                .thenReturn(CERTIFICATE_WITH_SUPPORTED_PEM_EXTENSION_BUT_NOT_MATCHING_NAME);

        // WHEN
        boolean isAccepted = testCertFileFilter.accept(fileMock);

        // THEN
        assertFalse(isAccepted);
    }

    @Test
    public void givenAFileWithMatchingFileNameAndSupportedExtension_whenAccept_thenReturnTrue() {
        // GIVEN
        setupFileMockAsADirectory(false);
        when(fileMock.getName())
                .thenReturn(CERTIFICATE_WITH_MATCHING_NAME_AND_SUPPORTED_PEM_EXTENSION);

        // WHEN
        boolean isAccepted = testCertFileFilter.accept(fileMock);

        // THEN
        assertTrue(isAccepted);
    }

    private void setupFileMockAsADirectory(boolean shouldBeADirectory) {
        when(fileMock.isDirectory()).thenReturn(shouldBeADirectory);
        when(fileMock.isFile()).thenReturn(!shouldBeADirectory);
    }
}