package it.necst.grabnrun;

import static it.necst.grabnrun.FileHelper.endsWithJarOrApkExtension;
import static it.necst.grabnrun.FileHelper.extractExtensionFromFilePath;
import static it.necst.grabnrun.FileHelper.extractFileNameFromFilePath;
import static it.necst.grabnrun.FileHelper.extractFileNameWithoutExtensionFromFilePath;
import static it.necst.grabnrun.FileHelper.extractFilePathWithoutExtensionFromFilePath;
import static junit.framework.Assert.assertFalse;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class FileHelperTest {

    private static final String TEST_FILE_PATH = "/testPath/";
    private static final String TEST_FILE_NAME_WITH_NO_EXTENSION = "testFile";
    private static final String TEST_FILE_EXTENSION_FOR_A_CONTAINER = FileHelper.APK_EXTENSION;

    private static final String TEST_FILE_NAME_WITH_CONTAINER_EXTENSION =
            TEST_FILE_NAME_WITH_NO_EXTENSION + TEST_FILE_EXTENSION_FOR_A_CONTAINER;

    private static final String TEST_FILE_PATH_WITH_NO_EXTENSION =
             TEST_FILE_PATH + TEST_FILE_NAME_WITH_NO_EXTENSION;
    private static final String TEST_FILE_PATH_WITH_CONTAINER_EXTENSION =
             TEST_FILE_PATH + TEST_FILE_NAME_WITH_CONTAINER_EXTENSION;

    private static final String TEST_FILE_EXTENSION_NOT_FOR_A_CONTAINER = ".txt";

    @Test (expected = IllegalArgumentException.class)
    public void givenAnEmptyFilePath_whenExtractFilePathWithoutExtensionFromFilePath_thenThrows() {
        // GIVEN
        String emptyFilePath = "";

        // WHEN
        extractFilePathWithoutExtensionFromFilePath(emptyFilePath);
    }

    @Test
    public void givenFileNameWithoutExtension_whenExtractFilePathWithoutExtensionFromFilePath_thenReturnsTheSameString() {
        // WHEN
        String extractedFilePath =
                extractFilePathWithoutExtensionFromFilePath(TEST_FILE_NAME_WITH_NO_EXTENSION);

        // THEN
        assertThat(extractedFilePath, is(equalTo(TEST_FILE_NAME_WITH_NO_EXTENSION)));
    }

    @Test
    public void givenFileNameWithExtension_whenExtractFilePathWithoutExtensionFromFilePath_thenReturnsTheFileNameWithoutTheExtension() {
        // WHEN
        String extractedFilePath =
                extractFilePathWithoutExtensionFromFilePath(TEST_FILE_NAME_WITH_CONTAINER_EXTENSION);

        // THEN
        assertThat(extractedFilePath, is(equalTo(TEST_FILE_NAME_WITH_NO_EXTENSION)));
    }

    @Test
    public void givenFilePathWithoutExtension_whenExtractFilePathWithoutExtensionFromFilePath_thenReturnsTheSameString() {
        // WHEN
        String extractedFilePath =
                extractFilePathWithoutExtensionFromFilePath(TEST_FILE_PATH_WITH_NO_EXTENSION);

        // THEN
        assertThat(extractedFilePath, is(equalTo(TEST_FILE_PATH_WITH_NO_EXTENSION)));
    }

    @Test
    public void givenFilePathWithExtension_whenExtractFilePathWithoutExtensionFromFilePath_thenReturnsTheFilePathWithoutTheExtension() {
        // WHEN
        String extractedFilePath =
                extractFilePathWithoutExtensionFromFilePath(TEST_FILE_PATH_WITH_CONTAINER_EXTENSION);

        // THEN
        assertThat(extractedFilePath, is(equalTo(TEST_FILE_PATH_WITH_NO_EXTENSION)));
    }

    @Test (expected = IllegalArgumentException.class)
    public void givenAnEmptyFilePath_whenExtractFileNameWithoutExtensionFromFilePath_thenThrows() {
        // GIVEN
        String emptyFilePath = "";

        // WHEN
        extractFileNameWithoutExtensionFromFilePath(emptyFilePath);
    }

    @Test
    public void givenAFilePathWithoutAFileNameAndExtension_whenExtractFileNameWithoutExtensionFromFilePath_thenReturnsAnEmptyString() {
        // WHEN
        String extractedFileName = extractFileNameWithoutExtensionFromFilePath(TEST_FILE_PATH);

        // THEN
        assertTrue(extractedFileName.isEmpty());
    }

    @Test
    public void givenFileNameWithoutExtension_whenExtractFileNameWithoutExtensionFromFilePath_thenReturnsTheSameString() {
        // WHEN
        String extractedFileName =
                extractFileNameWithoutExtensionFromFilePath(TEST_FILE_NAME_WITH_NO_EXTENSION);

        // THEN
        assertThat(extractedFileName, is(equalTo(TEST_FILE_NAME_WITH_NO_EXTENSION)));
    }

    @Test
    public void givenFileNameWithExtension_whenExtractFileNameWithoutExtensionFromFilePath_thenReturnsTheFileNameWithoutTheExtension() {
        // WHEN
        String extractedFileName =
                extractFileNameWithoutExtensionFromFilePath(TEST_FILE_NAME_WITH_CONTAINER_EXTENSION);

        // THEN
        assertThat(extractedFileName, is(equalTo(TEST_FILE_NAME_WITH_NO_EXTENSION)));
    }

    @Test
    public void givenFilePathWithoutExtension_whenExtractFileNameWithoutExtensionFromFilePath_thenReturnsTheFileNameWithoutTheExtension() {
        // WHEN
        String extractedFileName =
                extractFileNameWithoutExtensionFromFilePath(TEST_FILE_PATH_WITH_NO_EXTENSION);

        // THEN
        assertThat(extractedFileName, is(equalTo(TEST_FILE_NAME_WITH_NO_EXTENSION)));
    }

    @Test
    public void givenFilePathWithExtension_whenExtractFileNameWithoutExtensionFromFilePath_thenReturnsTheFileNameWithoutTheExtension() {
        // WHEN
        String extractedFileName =
                extractFileNameWithoutExtensionFromFilePath(TEST_FILE_PATH_WITH_CONTAINER_EXTENSION);

        // THEN
        assertThat(extractedFileName, is(equalTo(TEST_FILE_NAME_WITH_NO_EXTENSION)));
    }

    @Test (expected = IllegalArgumentException.class)
    public void givenAnEmptyFilePath_whenExtractFileNameFromFilePath_thenThrows() {
        // GIVEN
        String emptyFilePath = "";

        // WHEN
        extractFileNameFromFilePath(emptyFilePath);
    }

    @Test
    public void givenAFilePathWithoutAFileNameAndExtension_whenExtractFileNameFromFilePath_thenReturnsAnEmptyString() {
        // WHEN
        String extractedFileName = extractFileNameFromFilePath(TEST_FILE_PATH);

        // THEN
        assertTrue(extractedFileName.isEmpty());
    }

    @Test
    public void givenFileNameWithoutExtension_whenExtractFileNameFromFilePath_thenReturnsTheSameString() {
        // WHEN
        String extractedFileName = extractFileNameFromFilePath(TEST_FILE_NAME_WITH_NO_EXTENSION);

        // THEN
        assertThat(extractedFileName, is(equalTo(TEST_FILE_NAME_WITH_NO_EXTENSION)));
    }

    @Test
    public void givenFileNameWithExtension_whenExtractFileNameFromFilePath_thenReturnsTheSameString() {
        // WHEN
        String extractedFileName = extractFileNameFromFilePath(TEST_FILE_NAME_WITH_CONTAINER_EXTENSION);

        // THEN
        assertThat(extractedFileName, is(equalTo(TEST_FILE_NAME_WITH_CONTAINER_EXTENSION)));
    }

    @Test
    public void givenFilePathWithoutExtension_whenExtractFileNameFromFilePath_thenReturnsTheFileName() {
        // WHEN
        String extractedFileName = extractFileNameFromFilePath(TEST_FILE_PATH_WITH_NO_EXTENSION);

        // THEN
        assertThat(extractedFileName, is(equalTo(TEST_FILE_NAME_WITH_NO_EXTENSION)));
    }

    @Test
    public void givenFilePathWithExtension_whenExtractFileNameFromFilePath_thenReturnsTheFileName() {
        // WHEN
        String extractedFileName =
                extractFileNameFromFilePath(TEST_FILE_PATH_WITH_CONTAINER_EXTENSION);

        // THEN
        assertThat(extractedFileName, is(equalTo(TEST_FILE_NAME_WITH_CONTAINER_EXTENSION)));
    }

    @Test (expected = IllegalArgumentException.class)
    public void givenAnEmptyFilePath_whenExtractExtensionFromFilePath_thenThrows() {
        // GIVEN
        String emptyFilePath = "";

        // WHEN
        extractExtensionFromFilePath(emptyFilePath);
    }

    @Test
    public void givenAFileNameWithNoExtensionOnTheFile_whenExtractExtensionFromFilePath_thenReturnsAnAbsentOptional() {
        // WHEN
        Optional<String> optionalFileExtension =
                extractExtensionFromFilePath(TEST_FILE_NAME_WITH_NO_EXTENSION);

        // THEN
        assertThat(optionalFileExtension, is(equalTo(Optional.<String>absent())));
    }

    @Test
    public void givenAFilePathWithNoExtensionOnTheFile_whenExtractExtensionFromFilePath_thenReturnsAnAbsentOptional() {
        // WHEN
        Optional<String> optionalFileExtension =
                extractExtensionFromFilePath(TEST_FILE_PATH_WITH_NO_EXTENSION);

        // THEN
        assertThat(optionalFileExtension, is(equalTo(Optional.<String>absent())));
    }

    @Test
    public void givenAFileNameWithExtensionForTheFile_whenExtractExtensionFromFilePath_thenReturnsAnOptionalContainingTheExtension() {
        // WHEN
        Optional<String> optionalFileExtension =
                extractExtensionFromFilePath(TEST_FILE_NAME_WITH_CONTAINER_EXTENSION);

        // THEN
        assertThat(optionalFileExtension, is(equalTo(Optional.of(TEST_FILE_EXTENSION_FOR_A_CONTAINER))));
    }

    @Test
    public void givenAFilePathWithExtensionForTheFile_whenExtractExtensionFromFilePath_thenReturnsAnOptionalContainingTheExtension() {
        // WHEN
        Optional<String> optionalFileExtension =
                extractExtensionFromFilePath(TEST_FILE_PATH_WITH_CONTAINER_EXTENSION);

        // THEN
        assertThat(optionalFileExtension, is(equalTo(Optional.of(TEST_FILE_EXTENSION_FOR_A_CONTAINER))));
    }

    @Test (expected = IllegalArgumentException.class)
    public void givenAnEmptyFilePath_whenEndsWithJarOrApkExtension_thenThrows() {
        // GIVEN
        String emptyFilePath = "";

        // WHEN
        endsWithJarOrApkExtension(emptyFilePath);
    }

    @Test
    public void givenAFileNameWithNoExtension_whenEndsWithJarOrApkExtension_thenReturnsFalse() {
        // WHEN + THEN
        assertFalse(endsWithJarOrApkExtension(TEST_FILE_NAME_WITH_NO_EXTENSION));
    }

    @Test
    public void givenAFileNameWithANotContainerExtension_whenEndsWithJarOrApkExtension_thenReturnsFalse() {
        // WHEN + THEN
        assertFalse(endsWithJarOrApkExtension(
                TEST_FILE_NAME_WITH_NO_EXTENSION + TEST_FILE_EXTENSION_NOT_FOR_A_CONTAINER));
    }

    @Test
    public void givenAFileExtensionNotForAContainer_whenEndsWithJarOrApkExtension_thenReturnsFalse() {
        // WHEN + THEN
        assertFalse(endsWithJarOrApkExtension(TEST_FILE_EXTENSION_NOT_FOR_A_CONTAINER));
    }

    @Test
    public void givenAFileExtensionForAContainer_whenEndsWithJarOrApkExtension_thenReturnsTrue() {
        // WHEN + THEN
        assertTrue(endsWithJarOrApkExtension(TEST_FILE_EXTENSION_FOR_A_CONTAINER));
    }

    @Test
    public void givenAFileNameWithAContainerExtension_whenEndsWithJarOrApkExtension_thenReturnsTrue() {
        // WHEN + THEN
        assertTrue(endsWithJarOrApkExtension(TEST_FILE_NAME_WITH_CONTAINER_EXTENSION));
    }

    @Test
    public void givenAFilePathWithAContainerExtension_whenEndsWithJarOrApkExtension_thenReturnsTrue() {
        // WHEN + THEN
        assertTrue(endsWithJarOrApkExtension(TEST_FILE_PATH_WITH_CONTAINER_EXTENSION));
    }
}
