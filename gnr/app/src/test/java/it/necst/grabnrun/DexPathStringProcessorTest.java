package it.necst.grabnrun;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.util.NoSuchElementException;

@RunWith(MockitoJUnitRunner.class)
public class DexPathStringProcessorTest {

    private static final String TEST_EMPTY_PATH = "";

    private static final String TEST_LOCAL_PATH = "/example/path/for/test/file.jar";
    private static final String TEST_REMOTE_HTTP_PATH = "http://myexample.com/file.apk";
    private static final String TEST_REMOTE_HTTPS_PATH = "https://myexample.com/file.apk";
    private static final String TEST_COMPOSITE_DEX_PATH = concatenateDexPathStrings(
            TEST_LOCAL_PATH, TEST_REMOTE_HTTP_PATH, TEST_REMOTE_HTTPS_PATH);

    private DexPathStringProcessor testDexPathStringProcessor;

    @Test (expected = IllegalArgumentException.class)
    public void givenAnEmptyDexPath_whenCreateDexPathStringProcessor_thenThrows() {
        // WHEN
        testDexPathStringProcessor = new DexPathStringProcessor(TEST_EMPTY_PATH);
    }

    @Test
    public void givenASingleTestLocalPath_whenHasNextDexPathString_thenReturnsTrue() {
        // GIVEN
        testDexPathStringProcessor = new DexPathStringProcessor(TEST_LOCAL_PATH);

        // WHEN
        final boolean hasNextDexPathString = testDexPathStringProcessor.hasNextDexPathString();

        // THEN
        assertTrue(hasNextDexPathString);
    }

    @Test
    public void givenASingleTestLocalPath_whenNextDexPathString_thenReturnsTheTestLocalPath() {
        // GIVEN
        testDexPathStringProcessor = new DexPathStringProcessor(TEST_LOCAL_PATH);

        // WHEN
        final String nextDexPathString = testDexPathStringProcessor.nextDexPathString();

        // THEN
        assertThat(nextDexPathString, is(equalTo(TEST_LOCAL_PATH)));
    }

    @Test
    public void givenASingleTestRemoteHttpPath_whenHasNextDexPathString_thenReturnsTrue() {
        // GIVEN
        testDexPathStringProcessor = new DexPathStringProcessor(TEST_REMOTE_HTTP_PATH);

        // WHEN
        final boolean hasNextDexPathString = testDexPathStringProcessor.hasNextDexPathString();

        // THEN
        assertTrue(hasNextDexPathString);
    }

    @Test
    public void givenASingleTestRemoteHttpPath_whenNextDexPathString_thenReturnsTheTestRemoteHttpPath() {
        // GIVEN
        testDexPathStringProcessor = new DexPathStringProcessor(TEST_REMOTE_HTTP_PATH);

        // WHEN
        final String nextDexPathString = testDexPathStringProcessor.nextDexPathString();

        // THEN
        assertThat(nextDexPathString, is(equalTo(TEST_REMOTE_HTTP_PATH)));
    }

    @Test
    public void givenASingleTestRemoteHttpsPath_whenHasNextDexPathString_thenReturnsTrue() {
        // GIVEN
        testDexPathStringProcessor = new DexPathStringProcessor(TEST_REMOTE_HTTPS_PATH);

        // WHEN
        final boolean hasNextDexPathString = testDexPathStringProcessor.hasNextDexPathString();

        // THEN
        assertTrue(hasNextDexPathString);
    }

    @Test
    public void givenASingleTestRemoteHttpsPath_whenNextDexPathString_thenReturnsTheTestRemoteHttpsPath() {
        // GIVEN
        testDexPathStringProcessor = new DexPathStringProcessor(TEST_REMOTE_HTTPS_PATH);

        // WHEN
        final String nextDexPathString = testDexPathStringProcessor.nextDexPathString();

        // THEN
        assertThat(nextDexPathString, is(equalTo(TEST_REMOTE_HTTPS_PATH)));
    }

    @Test
    public void givenATestDexPathWhichConcatenatesThreePaths_whenHasNextDexPathString_thenReturnsTrueThreeTimesAndFalseAtTheFourth() {
        // GIVEN
        testDexPathStringProcessor = new DexPathStringProcessor(TEST_COMPOSITE_DEX_PATH);

        // WHEN + THEN
        assertTrue(testDexPathStringProcessor.hasNextDexPathString());
        testDexPathStringProcessor.nextDexPathString();
        assertTrue(testDexPathStringProcessor.hasNextDexPathString());
        testDexPathStringProcessor.nextDexPathString();
        assertTrue(testDexPathStringProcessor.hasNextDexPathString());
        testDexPathStringProcessor.nextDexPathString();
        assertFalse(testDexPathStringProcessor.hasNextDexPathString());
    }

    @Test
    public void givenATestDexPathWhichConcatenatesThreePaths_whenNextDexPathString_thenReturnsThreeStringsInTheSameOrderOfTheInputDexPath() {
        // GIVEN
        testDexPathStringProcessor = new DexPathStringProcessor(TEST_COMPOSITE_DEX_PATH);

        // WHEN + THEN
        assertThat(testDexPathStringProcessor.nextDexPathString(), is(equalTo(TEST_LOCAL_PATH)));
        assertThat(testDexPathStringProcessor.nextDexPathString(), is(equalTo(TEST_REMOTE_HTTP_PATH)));
        assertThat(testDexPathStringProcessor.nextDexPathString(), is(equalTo(TEST_REMOTE_HTTPS_PATH)));
    }

    @Test (expected = NoSuchElementException.class)
    public void givenATestDexPathWhichConcatenatesThreePaths_whenNextDexPathString_thenThrowsAfterThirdInvocation() {
        // GIVEN
        testDexPathStringProcessor = new DexPathStringProcessor(TEST_COMPOSITE_DEX_PATH);

        // WHEN
        testDexPathStringProcessor.nextDexPathString();
        testDexPathStringProcessor.nextDexPathString();
        testDexPathStringProcessor.nextDexPathString();
        testDexPathStringProcessor.nextDexPathString();
    }

    private static String concatenateDexPathStrings(String... dexPathStrings) {
        StringBuilder dexPathComplexStringBuilder = new StringBuilder();

        for (String dexPathString : dexPathStrings) {
            dexPathComplexStringBuilder.append(dexPathString).append(File.pathSeparator);
        }

        return dexPathComplexStringBuilder
                .deleteCharAt(dexPathComplexStringBuilder.lastIndexOf(File.pathSeparator))
                .toString();
    }
}
