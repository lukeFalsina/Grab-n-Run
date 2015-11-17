package it.necst.grabnrun;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;

@RunWith(MockitoJUnitRunner.class)
public class FileFilterByNameMatchTest {

    public static final String EMPTY_FILE_NAME = "";
    private static final String TEST_FILE_NAME = "testCertificate";
    private static final String INVALID_FILE_EXTENSION = ".tooLongSinceUpToFourCharsAreSupported";

    private static final String VALID_FILE_EXTENSION = ".txt";
    private static final String ANOTHER_VALID_FILE_EXTENSION_WITH_UPPERCASE = ".DOCX";
    private static final String CERTIFICATE_WITH_MATCHING_NAME_BUT_NOT_EXTENSION =
            TEST_FILE_NAME + ".doc";
    private static final String CERTIFICATE_WITH_MATCHING_EXTENSION_BUT_NOT_NAME =
            "anotherTestFileName" + VALID_FILE_EXTENSION;
    private static final String CERTIFICATE_WITH_MATCHING_NAME_AND_EXTENSION =
            TEST_FILE_NAME + VALID_FILE_EXTENSION;

    @Mock File fileMock;
    private FileFilterByNameMatch testFileFilter;

    @Test (expected = IllegalArgumentException.class)
    public void givenAnEmptyFileName_whenCreateFileFilter_thenThrows() {
        new FileFilterByNameMatch(EMPTY_FILE_NAME, VALID_FILE_EXTENSION);
    }

    @Test (expected = IllegalArgumentException.class)
    public void givenAFileExtensionNotMatchingDotAndThreeCharacters_whenCreateFileFilter_thenThrows() {
        new FileFilterByNameMatch(TEST_FILE_NAME, INVALID_FILE_EXTENSION);
    }

    @Test
    public void givenAFileExtensionWithFourUppercaseCharacters_whenCreateFileFilter_thenTheTestSucceeds() {
        new FileFilterByNameMatch(TEST_FILE_NAME, ANOTHER_VALID_FILE_EXTENSION_WITH_UPPERCASE);
    }

    @Test
    public void givenADirectory_whenAccept_thenReturnFalse() {
        // GIVEN
        initializeFileFilterWithTestValues();
        setupFileMockAsADirectory(true);

        // WHEN
        boolean isAccepted = testFileFilter.accept(fileMock);

        // THEN
        assertFalse(isAccepted);
    }

    @Test
    public void givenAFileWithMatchingFileNameButNotExtension_whenAccept_thenReturnFalse() {
        // GIVEN
        initializeFileFilterWithTestValues();
        setupFileMockAsADirectory(false);
        when(fileMock.getName())
                .thenReturn(CERTIFICATE_WITH_MATCHING_NAME_BUT_NOT_EXTENSION);

        // WHEN
        boolean isAccepted = testFileFilter.accept(fileMock);

        // THEN
        assertFalse(isAccepted);
    }

    @Test
    public void givenAFileWithMatchingExtensionButNotName_whenAccept_thenReturnFalse() {
        // GIVEN
        initializeFileFilterWithTestValues();
        setupFileMockAsADirectory(false);
        when(fileMock.getName())
                .thenReturn(CERTIFICATE_WITH_MATCHING_EXTENSION_BUT_NOT_NAME);

        // WHEN
        boolean isAccepted = testFileFilter.accept(fileMock);

        // THEN
        assertFalse(isAccepted);
    }

    @Test
    public void givenAFileWithMatchingNameAndExtension_whenAccept_thenReturnTrue() {
        // GIVEN
        initializeFileFilterWithTestValues();
        setupFileMockAsADirectory(false);
        when(fileMock.getName())
                .thenReturn(CERTIFICATE_WITH_MATCHING_NAME_AND_EXTENSION);

        // WHEN
        boolean isAccepted = testFileFilter.accept(fileMock);

        // THEN
        assertTrue(isAccepted);
    }

    private void initializeFileFilterWithTestValues() {
        testFileFilter = new FileFilterByNameMatch(TEST_FILE_NAME, VALID_FILE_EXTENSION);
    }

    private void setupFileMockAsADirectory(boolean shouldBeADirectory) {
        when(fileMock.isDirectory()).thenReturn(shouldBeADirectory);
        when(fileMock.isFile()).thenReturn(!shouldBeADirectory);
    }
}