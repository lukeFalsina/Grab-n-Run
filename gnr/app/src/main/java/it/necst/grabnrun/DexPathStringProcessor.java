package it.necst.grabnrun;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

class DexPathStringProcessor {
    private static final String HTTP_PROTOCOL_STRING = "http://";
    private static final String HTTPS_PROTOCOL_STRING = "https://";
    private static final String HTTP_PROTOCOL_STRING_WITHOUT_COLON = "http//";
    private static final String HTTPS_PROTOCOL_STRING_WITHOUT_COLON = "https//";

    private final List<String> extractedSingleDexPathStrings;
    private final ListIterator<String> extractedSingleDexPathStringsIterator;

    DexPathStringProcessor(@NonNull String fullDexPathString) {

        checkNotNull(fullDexPathString, "The dex path string must not be null");
        checkArgument(!fullDexPathString.isEmpty(), "The dex path string must not be empty");

        extractedSingleDexPathStrings = extractSingleDexPathStringsFromFullOne(fullDexPathString);

        extractedSingleDexPathStringsIterator = extractedSingleDexPathStrings.listIterator();
    }

    private List<String> extractSingleDexPathStringsFromFullOne(String fullDexPathString) {
        // Necessary workaround to avoid remote URL being split in a wrong way..
        String temporaryDexPathCompleteString = fullDexPathString
                .replaceAll(HTTP_PROTOCOL_STRING, HTTP_PROTOCOL_STRING_WITHOUT_COLON)
                .replaceAll(HTTPS_PROTOCOL_STRING, HTTPS_PROTOCOL_STRING_WITHOUT_COLON);

        return Arrays.asList(
                temporaryDexPathCompleteString.split(Pattern.quote(File.pathSeparator)));
    }

    public boolean hasNextDexPathString() {
        return extractedSingleDexPathStringsIterator.hasNext();
    }

    public String nextDexPathString() throws NoSuchElementException {
        String nextDexPathString = extractedSingleDexPathStringsIterator.next();

        if (nextDexPathString.startsWith(HTTP_PROTOCOL_STRING_WITHOUT_COLON)) {
            return nextDexPathString.replaceFirst(
                    HTTP_PROTOCOL_STRING_WITHOUT_COLON, HTTP_PROTOCOL_STRING);
        }

        if (nextDexPathString.startsWith(HTTPS_PROTOCOL_STRING_WITHOUT_COLON)) {
            return nextDexPathString.replaceFirst(
                    HTTPS_PROTOCOL_STRING_WITHOUT_COLON, HTTPS_PROTOCOL_STRING);
        }

        return nextDexPathString;
    }
}
