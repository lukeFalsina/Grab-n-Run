package it.necst.grabnrun;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import com.google.common.base.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PackageNameTrieTest {
    private static final String PREFIX_OF_TEST_PACKAGE_NAME = "com.example";
    private static final String TEST_PACKAGE_NAME =
            PREFIX_OF_TEST_PACKAGE_NAME + ".polimi.application";

    private static final String ANOTHER_TEST_PACKAGE_NAME_WITH_NO_COMMON_PREFIX =
            "it.test.system.application";
    private static final String ANOTHER_TEST_PACKAGE_NAME_WITH_A_COMMON_PREFIX =
            PREFIX_OF_TEST_PACKAGE_NAME + ".system.myapplication";

    private PackageNameTrie testPackageNameTrie;

    @Before
    public void initializePackageNameTrie() {
        testPackageNameTrie = new PackageNameTrie();
    }

    @Test
    public void givenNoEntriesForPackageNameWereGenerated_whenGetPackageNameWithAssociatedCertificate_thenReturnsAnAbsentOptional() {
        // WHEN
        final Optional<String> optionalPackageNameWithAssociatedCertificate =
                testPackageNameTrie.getPackageNameWithAssociatedCertificate(TEST_PACKAGE_NAME);

        // THEN
        assertThat(optionalPackageNameWithAssociatedCertificate, equalTo(Optional.<String>absent()));
    }

    @Test
    public void givenEntriesForTestPackageNameWereGeneratedButNoPackageNameWasSetToHaveACertificate_whenGetPackageNameWithAssociatedCertificate_thenReturnsAnAbsentOptional() {
        // GIVEN
        testPackageNameTrie.generateEntriesForPackageName(TEST_PACKAGE_NAME);

        // WHEN
        final Optional<String> optionalPackageNameWithAssociatedCertificate =
                testPackageNameTrie.getPackageNameWithAssociatedCertificate(TEST_PACKAGE_NAME);

        // THEN
        assertThat(optionalPackageNameWithAssociatedCertificate, equalTo(Optional.<String>absent()));
    }

    @Test
    public void givenNoEntriesForTestPackageNameWereGeneratedButPackageNameWasSetToHaveACertificate_whenGetPackageNameWithAssociatedCertificate_thenReturnsAnAbsentOptional() {
        // GIVEN
        testPackageNameTrie.setPackageNameHasAssociatedCertificate(TEST_PACKAGE_NAME);

        // WHEN
        final Optional<String> optionalPackageNameWithAssociatedCertificate =
                testPackageNameTrie.getPackageNameWithAssociatedCertificate(TEST_PACKAGE_NAME);

        // THEN
        assertThat(optionalPackageNameWithAssociatedCertificate, equalTo(Optional.<String>absent()));
    }

    @Test
    public void givenEntriesForTestPackageNameWereGeneratedAndTestPackageNameWasSetToHaveACertificate_whenGetPackageNameWithAssociatedCertificate_thenReturnsAnOptionalOfTestPackageName() {
        // GIVEN
        testPackageNameTrie.generateEntriesForPackageName(TEST_PACKAGE_NAME);
        testPackageNameTrie.setPackageNameHasAssociatedCertificate(TEST_PACKAGE_NAME);

        // WHEN
        final Optional<String> optionalPackageNameWithAssociatedCertificate =
                testPackageNameTrie.getPackageNameWithAssociatedCertificate(TEST_PACKAGE_NAME);

        // THEN
        assertThat(
                optionalPackageNameWithAssociatedCertificate,
                equalTo(Optional.of(TEST_PACKAGE_NAME)));
    }

    @Test
    public void givenEntriesForTestPackageNameWereGeneratedAndPrefixOfTestPackageNameWasSetToHaveACertificate_whenGetPackageNameWithAssociatedCertificateWithTestPackageName_thenReturnsAnOptionalOfPrefixOfTestPackageName() {
        // GIVEN
        testPackageNameTrie.generateEntriesForPackageName(TEST_PACKAGE_NAME);
        testPackageNameTrie.setPackageNameHasAssociatedCertificate(PREFIX_OF_TEST_PACKAGE_NAME);

        // WHEN
        final Optional<String> optionalPackageNameWithAssociatedCertificate =
                testPackageNameTrie.getPackageNameWithAssociatedCertificate(TEST_PACKAGE_NAME);

        // THEN
        assertThat(
                optionalPackageNameWithAssociatedCertificate,
                equalTo(Optional.of(PREFIX_OF_TEST_PACKAGE_NAME)));
    }

    @Test
    public void givenEntriesForTestPackageNameWereGeneratedAndTestPackageNameWasSetToHaveACertificate_whenGetPackageNameWithAssociatedCertificateWithPrefixOfTestPackageName_thenReturnsAnAbsentOptional() {
        // GIVEN
        testPackageNameTrie.generateEntriesForPackageName(TEST_PACKAGE_NAME);
        testPackageNameTrie.setPackageNameHasAssociatedCertificate(TEST_PACKAGE_NAME);

        // WHEN
        final Optional<String> optionalPackageNameWithAssociatedCertificate =
                testPackageNameTrie.getPackageNameWithAssociatedCertificate(PREFIX_OF_TEST_PACKAGE_NAME);

        // THEN
        assertThat(optionalPackageNameWithAssociatedCertificate, equalTo(Optional.<String>absent()));
    }

    @Test
    public void givenEntriesForTestPackageNameAndAnotherPackageNameWithNoCommonPrefixWereGeneratedAndPrefixOfTestPackageNameWasSetToHaveACertificate_whenGetPackageNameWithAssociatedCertificateWithTheOtherTestPackageName_thenReturnsAnAbsentOptional() {
        // GIVEN
        testPackageNameTrie.generateEntriesForPackageName(TEST_PACKAGE_NAME);
        testPackageNameTrie.generateEntriesForPackageName(ANOTHER_TEST_PACKAGE_NAME_WITH_NO_COMMON_PREFIX);
        testPackageNameTrie.setPackageNameHasAssociatedCertificate(PREFIX_OF_TEST_PACKAGE_NAME);

        // WHEN
        final Optional<String> optionalPackageNameWithAssociatedCertificate =
                testPackageNameTrie.getPackageNameWithAssociatedCertificate(
                        ANOTHER_TEST_PACKAGE_NAME_WITH_NO_COMMON_PREFIX);

        // THEN
        assertThat(optionalPackageNameWithAssociatedCertificate, equalTo(Optional.<String>absent()));
    }

    @Test
    public void givenEntriesForTestPackageNameAndAnotherPackageNameWithACommonPrefixWereGeneratedAndPrefixOfTestPackageNameWasSetToHaveACertificate_whenGetPackageNameWithAssociatedCertificateWithTheOtherTestPackageName_thenReturnsAnOptionalOfPrefixOfTestPackageName() {
        // GIVEN
        testPackageNameTrie.generateEntriesForPackageName(TEST_PACKAGE_NAME);
        testPackageNameTrie.generateEntriesForPackageName(ANOTHER_TEST_PACKAGE_NAME_WITH_A_COMMON_PREFIX);
        testPackageNameTrie.setPackageNameHasAssociatedCertificate(PREFIX_OF_TEST_PACKAGE_NAME);

        // WHEN
        final Optional<String> optionalPackageNameWithAssociatedCertificate =
                testPackageNameTrie.getPackageNameWithAssociatedCertificate(
                        ANOTHER_TEST_PACKAGE_NAME_WITH_A_COMMON_PREFIX);

        // THEN
        assertThat(
                optionalPackageNameWithAssociatedCertificate,
                equalTo(Optional.of(PREFIX_OF_TEST_PACKAGE_NAME)));
    }
}
