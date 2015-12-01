package it.necst.grabnrun;

import static it.necst.grabnrun.PackageNameHelper.isAValidPackageName;
import static it.necst.grabnrun.PackageNameHelper.revertPackageNameToURL;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

import java.net.URL;

public class PackageNameHelperTest {

    private static final String TEST_INVALID_PACKAGE_NAME_STARTING_WITH_A_DOT = ".com.example";
    private static final String TEST_INVALID_PACKAGE_NAME_WITH_AN_EMPTY_FIELD_BETWEEN_TWO_DOTS =
            "com..example";
    private static final String TEST_INVALID_PACKAGE_NAME_ENDING_WITH_A_DOT = "com.example.";
    private static final String TEST_INVALID_PACKAGE_NAME_WITH_ONLY_ONE_FIELD = "com";

    private static final String TEST_VALID_PACKAGE_NAME_WITH_TWO_FIELDS = "com.example";
    private static final String TEST_VALID_PACKAGE_NAME_WITH_FOUR_FIELDS =
            "com.example.polimi.application";

    private static final String TEST_EXPECTED_URL_STRING_WITH_VALID_PACKAGE_NAME_WITH_TWO_FIELDS =
            "https://example.com/certificate.pem";
    private static final String TEST_EXPECTED_URL_STRING_WITH_VALID_PACKAGE_NAME_WITH_FOUR_FIELDS =
            "https://example.com/polimi/application/certificate.pem";

    @Test
    public void givenAnEmptyPackageName_whenIsAValidPackageName_thenReturnFalse() {
        // GIVEN
        String emptyPackageName = "";

        // WHEN
        boolean isAValidPackageName = isAValidPackageName(emptyPackageName);

        // THEN
        assertFalse(isAValidPackageName);
    }

    @Test
    public void givenAPackageNameStartingWithADot_whenIsAValidPackageName_thenReturnFalse() {
        // WHEN
        boolean isAValidPackageName = isAValidPackageName(TEST_INVALID_PACKAGE_NAME_STARTING_WITH_A_DOT);

        // THEN
        assertFalse(isAValidPackageName);
    }

    @Test
    public void givenAPackageNameWithAnEmptyFieldBetweenTwoDots_whenIsAValidPackageName_thenReturnFalse() {
        // WHEN
        boolean isAValidPackageName = isAValidPackageName(
                TEST_INVALID_PACKAGE_NAME_WITH_AN_EMPTY_FIELD_BETWEEN_TWO_DOTS);

        // THEN
        assertFalse(isAValidPackageName);
    }

    @Test
    public void givenAPackageNameEndingWithADot_whenIsAValidPackageName_thenReturnFalse() {
        // WHEN
        boolean isAValidPackageName = isAValidPackageName(TEST_INVALID_PACKAGE_NAME_ENDING_WITH_A_DOT);

        // THEN
        assertFalse(isAValidPackageName);
    }

    @Test
    public void givenAPackageNameWithOnlyOneField_whenIsAValidPackageName_thenReturnFalse() {
        // WHEN
        boolean isAValidPackageName = isAValidPackageName(TEST_INVALID_PACKAGE_NAME_WITH_ONLY_ONE_FIELD);

        // THEN
        assertFalse(isAValidPackageName);
    }

    @Test
    public void givenAPackageNameWithAtLeastTwoFieldsSeparatedByASingleDot_whenIsAValidPackageName_thenReturnTrue() {
        // WHEN
        boolean isAValidPackageName = isAValidPackageName(TEST_VALID_PACKAGE_NAME_WITH_TWO_FIELDS);

        // THEN
        assertTrue(isAValidPackageName);
    }

    @Test (expected = IllegalArgumentException.class)
    public void givenANotValidPackageName_whenRevertPackageNameToURL_thenThrows() throws Exception {
        // WHEN
        revertPackageNameToURL(TEST_INVALID_PACKAGE_NAME_WITH_ONLY_ONE_FIELD);
    }

    @Test
    public void givenAValidPackageNameWithTwoFields_whenRevertPackageNameToURL_thenReturnsTheRevertedPackageNameAsDomainName() throws Exception {
        // WHEN
        URL revertPackageNameToURL = revertPackageNameToURL(TEST_VALID_PACKAGE_NAME_WITH_TWO_FIELDS);

        // THEN
        assertThat(
                revertPackageNameToURL,
                is(equalTo(new URL(TEST_EXPECTED_URL_STRING_WITH_VALID_PACKAGE_NAME_WITH_TWO_FIELDS))));
    }

    @Test
    public void givenAValidPackageNameWithThreeOrMoreFields_whenRevertPackageNameToURL_thenReturnsTheRevertedTwoFieldsOfPackageNameAsDomainNameAndTheRestAsFilePart() throws Exception {
        // WHEN
        URL revertPackageNameToURL = revertPackageNameToURL(TEST_VALID_PACKAGE_NAME_WITH_FOUR_FIELDS);

        // THEN
        assertThat(
                revertPackageNameToURL,
                is(equalTo(new URL(TEST_EXPECTED_URL_STRING_WITH_VALID_PACKAGE_NAME_WITH_FOUR_FIELDS))));
    }
}
