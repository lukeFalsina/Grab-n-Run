package it.necst.grabnrun;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import android.support.annotation.NonNull;

import com.google.common.annotations.VisibleForTesting;

import java.net.MalformedURLException;
import java.net.URL;

public class PackageNameHelper {
    @VisibleForTesting static final String HTTPS_PROTOCOL_AS_STRING = "https";
    @VisibleForTesting static final String DEFAULT_CERTIFICATE_FILE_NAME = "/certificate.pem";

    private static final String DOT_AS_REGULAR_EXPRESSION = "\\.";
    private static final int MINIMUM_NUMBER_OF_FIELDS_IN_A_PACKAGE_NAME = 2;
    private static final int FIRST_LEVEL_DOMAIN_INDEX = 0;
    private static final int SECOND_LEVEL_DOMAIN_INDEX = 1;

    public static boolean isAValidPackageName(@NonNull String candidatePackageName) {
        checkNotNull(candidatePackageName, "The candidate package name must not be null");

        if (candidatePackageName.isEmpty()) return false;
        if (candidatePackageName.endsWith(".")) return false;

        String[] packageNameFields = candidatePackageName.split(DOT_AS_REGULAR_EXPRESSION);
        if (packageNameFields.length < MINIMUM_NUMBER_OF_FIELDS_IN_A_PACKAGE_NAME) {
            return false;
        }

        for (String packageNameField : packageNameFields) {
            if (packageNameField.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    public static URL revertPackageNameToURL(@NonNull String candidatePackageName)
            throws MalformedURLException {
        checkArgument(
                isAValidPackageName(candidatePackageName),
                "A valid package name must be provided");

        // If a package name is valid, it has at least two fields.
        String[] packageNameFields = candidatePackageName.split(DOT_AS_REGULAR_EXPRESSION);
        StringBuilder urlAsStringBuilder = new StringBuilder(HTTPS_PROTOCOL_AS_STRING + "://")
                .append(packageNameFields[SECOND_LEVEL_DOMAIN_INDEX])
                .append(".")
                .append(packageNameFields[FIRST_LEVEL_DOMAIN_INDEX]);

        for (int fieldIndex = 2; fieldIndex < packageNameFields.length; fieldIndex++) {
            urlAsStringBuilder.append("/")
                    .append(packageNameFields[fieldIndex]);
        }

        urlAsStringBuilder.append(DEFAULT_CERTIFICATE_FILE_NAME);

        return new URL(urlAsStringBuilder.toString());
    }
}
