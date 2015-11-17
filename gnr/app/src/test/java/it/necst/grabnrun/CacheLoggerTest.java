package it.necst.grabnrun;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.base.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URL;

@RunWith(MockitoJUnitRunner.class)
public class CacheLoggerTest {
    private static final int POSITIVE_INTEGER_FOR_DAYS_TILL_CONSIDERED_FRESH = 1;
    private static final String TEST_REMOTE_URL_AS_STRING = "https://test.com/myExample.jar";
    private static final String ANOTHER_TEST_REMOTE_URL_AS_STRING = "https://test2.com/anotherExample.apk";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test (expected = IllegalArgumentException.class)
    public void givenNotPositiveIntegerForDaysTillConsideredFresh_whenCreateCacheLogger_thenThrows() {
        // GIVEN
        int notPositiveIntegerForDaysTillConsideredFresh = -1;

        // WHEN
        new CacheLogger(getTemporaryFolderPath(), notPositiveIntegerForDaysTillConsideredFresh);
    }

    @Test
    public void givenNoHelperFileWasStored_whenCheckForCachedEntry_thenReturnsAnAbsentOptional()
            throws Exception {
        // GIVEN
        final CacheLogger testCacheLogger = initializeCacheLoggerWithTestValues();

        // WHEN
        final Optional<String> optionalLocalFileNameOfTheCachedResource =
                testCacheLogger.checkForCachedEntry(new URL(TEST_REMOTE_URL_AS_STRING));

        // THEN
        assertThat(optionalLocalFileNameOfTheCachedResource, equalTo(Optional.<String>absent()));
    }

    @Test
    public void givenNoHelperFileWasStoredAndAddAURLAsACachedEntry_whenCheckForCachedEntryWithADifferentURL_thenReturnsAnAbsentOptional()
            throws Exception {
        // GIVEN
        final CacheLogger testCacheLogger = initializeCacheLoggerWithTestValues();
        final URL testRemoteURL = new URL(TEST_REMOTE_URL_AS_STRING);
        createFakeLocalFileAndAddItsNameToTheCacheLogger(testCacheLogger, testRemoteURL);

        // WHEN
        final Optional<String> optionalLocalFileNameOfTheCachedResource =
                testCacheLogger.checkForCachedEntry(new URL(ANOTHER_TEST_REMOTE_URL_AS_STRING));

        // THEN
        assertThat(optionalLocalFileNameOfTheCachedResource, equalTo(Optional.<String>absent()));
    }

    @Test
    public void givenNoHelperFileWasStoredAndAddAURLAsACachedEntry_whenCheckForCachedEntryWithThatURL_thenReturnsTheLocalFileName()
            throws Exception {
        // GIVEN
        final CacheLogger testCacheLogger = initializeCacheLoggerWithTestValues();
        final URL testRemoteURL = new URL(TEST_REMOTE_URL_AS_STRING);
        final String testLocalFileName =
                createFakeLocalFileAndAddItsNameToTheCacheLogger(testCacheLogger, testRemoteURL);

        // WHEN
        final Optional<String> optionalLocalFileNameOfTheCachedResource =
                testCacheLogger.checkForCachedEntry(testRemoteURL);

        // THEN
        assertThat(optionalLocalFileNameOfTheCachedResource, equalTo(Optional.of(testLocalFileName)));
    }

    @Test
    public void givenNoHelperFileWasStoredAndCacheLoggerIsFinalizedAndAURLIsAddedAsACachedEntry_whenCheckForCachedEntryWithThatURL_thenReturnsAnAbsentOptional()
            throws Exception {
        // GIVEN
        final CacheLogger testCacheLogger = initializeCacheLoggerWithTestValues();
        testCacheLogger.finalizeLog();
        final URL testRemoteURL = new URL(TEST_REMOTE_URL_AS_STRING);
        createFakeLocalFileAndAddItsNameToTheCacheLogger(testCacheLogger, testRemoteURL);

        // WHEN
        final Optional<String> optionalLocalFileNameOfTheCachedResource =
                testCacheLogger.checkForCachedEntry(testRemoteURL);

        // THEN
        assertThat(optionalLocalFileNameOfTheCachedResource, equalTo(Optional.<String>absent()));
    }

    @Test
    public void givenHelperFileWasStoredWithAURLAsACachedEntry_whenCheckForCachedEntryWithThatURLOnANewCacheLogger_thenReturnsTheLocalFileName()
            throws Exception {
        // GIVEN
        final CacheLogger testCacheLogger = initializeCacheLoggerWithTestValues();
        final URL testRemoteURL = new URL(TEST_REMOTE_URL_AS_STRING);
        final String testLocalFileName =
                createFakeLocalFileAndAddItsNameToTheCacheLogger(testCacheLogger, testRemoteURL);
        testCacheLogger.finalizeLog();

        final CacheLogger anotherTestCacheLogger = initializeCacheLoggerWithTestValues();

        // WHEN
        final Optional<String> optionalLocalFileNameOfTheCachedResource =
                anotherTestCacheLogger.checkForCachedEntry(testRemoteURL);

        // THEN
        assertThat(optionalLocalFileNameOfTheCachedResource, equalTo(Optional.of(testLocalFileName)));
    }

    private CacheLogger initializeCacheLoggerWithTestValues() {
        return new CacheLogger(
                getTemporaryFolderPath(), POSITIVE_INTEGER_FOR_DAYS_TILL_CONSIDERED_FRESH);
    }

    private String getTemporaryFolderPath() {
        return temporaryFolder.getRoot().getPath();
    }

    private String createFakeLocalFileAndAddItsNameToTheCacheLogger(
            CacheLogger testCacheLogger, URL testRemoteURL) throws IOException {
        final String testLocalFileName = computeFileNameFromURL(testRemoteURL);
        temporaryFolder.newFile(testLocalFileName);
        testCacheLogger.addCachedEntryToLog(testRemoteURL, testLocalFileName);
        return testLocalFileName;
    }

    private String computeFileNameFromURL(URL testRemoteURL) {
        return testRemoteURL.getPath().substring(1);
    }
}
