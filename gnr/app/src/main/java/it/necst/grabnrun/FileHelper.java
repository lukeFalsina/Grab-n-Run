package it.necst.grabnrun;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.google.common.base.Optional;

import java.io.File;

public class FileHelper {
    public static final String DOT_SEPARATOR = ".";
    public static final String APK_EXTENSION = DOT_SEPARATOR + "apk";
    public static final String JAR_EXTENSION = DOT_SEPARATOR + "jar";

    public static String extractFileNameWithoutExtensionFromFilePath(@NonNull String filePath) {
        checkPreconditionsOnFilePath(filePath);

        String fileName = extractFileNameFromFilePath(filePath);

        int firstCharacterOfAnExtensionIndex = fileName.lastIndexOf(DOT_SEPARATOR);

        if (firstCharacterOfAnExtensionIndex > 0) {
            return fileName.substring(0, firstCharacterOfAnExtensionIndex);
        } else {
            return fileName;
        }
    }

    public static String extractFileNameFromFilePath(@NonNull String filePath) {
        checkPreconditionsOnFilePath(filePath);

        int lastOccurrenceOfFileSeparatorIndex = filePath.lastIndexOf(File.separator);

        if (lastOccurrenceOfFileSeparatorIndex == -1)
            return filePath;

        return filePath.substring(lastOccurrenceOfFileSeparatorIndex + 1);
    }

    public static Optional<String> extractExtensionFromFilePath(@NonNull String filePath) {
        checkPreconditionsOnFilePath(filePath);

        int initialCharacterOfAnExtensionIndex = filePath.lastIndexOf(DOT_SEPARATOR);

        if (initialCharacterOfAnExtensionIndex == -1)
            return Optional.absent();

        return Optional.of(filePath.substring(initialCharacterOfAnExtensionIndex));
    }

    public static boolean endsWithJarOrApkExtension(@NonNull String filePath) {
        checkPreconditionsOnFilePath(filePath);

        int initialCharacterOfAnExtensionIndex = filePath.lastIndexOf(DOT_SEPARATOR);

        if (initialCharacterOfAnExtensionIndex == -1)
            return false;

        String fileExtension = filePath.substring(initialCharacterOfAnExtensionIndex);

        return fileExtension.equalsIgnoreCase(APK_EXTENSION) ||
                fileExtension.equalsIgnoreCase(JAR_EXTENSION);
    }

    private static void checkPreconditionsOnFilePath(@NonNull String filePath) {
        checkNotNull(filePath, "The path of the input file must not be null");
        checkArgument(!filePath.isEmpty(), "The path of the input file must not be empty");
    }
}
