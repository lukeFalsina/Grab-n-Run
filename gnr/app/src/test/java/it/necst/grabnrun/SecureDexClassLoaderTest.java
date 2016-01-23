package it.necst.grabnrun;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static it.necst.grabnrun.FileHelper.extractFileNameFromFilePath;
import static it.necst.grabnrun.SecureDexClassLoader.IMPORTED_CERTIFICATE_PRIVATE_DIRECTORY_NAME;
import static it.necst.grabnrun.SecureLoaderFactory.IMPORTED_CONTAINERS_PRIVATE_DIRECTORY_NAME;
import static it.necst.grabnrun.SecureLoaderFactory.X_509_CERTIFICATE;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.endsWith;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Map;

import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;
import it.necst.grabnrun.shadows.BaseDexClassLoaderShadow;
import it.necst.grabnrun.shadows.DexFileShadow;
import it.polimi.necst.gnr.BuildConfig;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, shadows={BaseDexClassLoaderShadow.class, DexFileShadow.class})
public class SecureDexClassLoaderTest {

    private static final String EMPTY_CLASS_NAME = "";
    private static final String TEST_REMOTE_CONTAINER_EXTENSION = ".jar";
    private static final String TEST_REMOTE_CONTAINER_FILE_NAME_WITHOUT_EXTENSION = "componentModifier";
    private static final String TEST_REMOTE_CONTAINER_FILE_NAME =
            TEST_REMOTE_CONTAINER_FILE_NAME_WITHOUT_EXTENSION + TEST_REMOTE_CONTAINER_EXTENSION;

    private static final String TEST_REMOTE_DOMAIN_URL = "https://dl.dropboxusercontent.com/u/28681922/";

    private static final String TEST_REMOTE_CONTAINER_URL_AS_STRING =
            TEST_REMOTE_DOMAIN_URL + TEST_REMOTE_CONTAINER_FILE_NAME;
    // This container is a repack of the one above. One of its entries was modified,
    // and now it breaks the signature verification
    private static final String TEST_REMOTE_REPACK_CONTAINER_FILE_NAME = "componentModifierRepack";
    private static final String TEST_REMOTE_REPACK_CONTAINER_URL_AS_STRING =
            TEST_REMOTE_DOMAIN_URL + TEST_REMOTE_REPACK_CONTAINER_FILE_NAME + TEST_REMOTE_CONTAINER_EXTENSION;

    private static final FileFilterByNameMatch TEST_FILE_FILTER_MATCHING_FILE_NAMED_AS_THE_TEST_CONTAINER =
            new FileFilterByNameMatch(
                    TEST_REMOTE_CONTAINER_FILE_NAME_WITHOUT_EXTENSION, TEST_REMOTE_CONTAINER_EXTENSION);

    private static final FileFilterByNameMatch TEST_FILE_FILTER_MATCHING_FILE_NAMED_AS_THE_TEST_REPACK_CONTAINER =
            new FileFilterByNameMatch(TEST_REMOTE_REPACK_CONTAINER_FILE_NAME, TEST_REMOTE_CONTAINER_EXTENSION);

    private static final String TEST_PACKAGE_NAME = "it.polimi.componentmodifier";
    private static final String TEST_A_PACKAGE_NAME_NOT_IN_THE_CONTAINER = "com.example.application";

    private static final String TEST_CERTIFICATE_FILE_NAME = "test_cert";
    private static final String TEST_CERTIFICATE_EXTENSION = ".pem";
    private static final String TEST_REMOTE_CERTIFICATE_URL_AS_STRING =
            TEST_REMOTE_DOMAIN_URL + TEST_CERTIFICATE_FILE_NAME + TEST_CERTIFICATE_EXTENSION;
    private static final String TEST_A_REMOTE_CERTIFICATE_NOT_USED_TO_SIGN_THE_CONTAINER_URL_AS_STRING =
            TEST_REMOTE_DOMAIN_URL + "wm" + TEST_CERTIFICATE_EXTENSION;

    // Internally certificates are renamed as the package name they are associated with..
    private static final FileFilterByNameMatch TEST_FILE_FILTER_MATCHING_CERTIFICATE_NAMED_AS_THE_PACKAGE_NAME =
            new FileFilterByNameMatch(TEST_PACKAGE_NAME, TEST_CERTIFICATE_EXTENSION);

    private static final String TEST_CLASS_TO_LOAD =
            "it.polimi.componentmodifier.FirstComponentModifierImpl";
    // This class is not contained in the remote test container, although it has the same package name
    private static final String TEST_A_CLASS_TO_LOAD_NOT_IN_THE_CONTAINER =
            "it.polimi.componentmodifier.NotPresentComponentModifierImpl";

    private static final ImmutableSet<String> TEST_SET_OF_CLASSES_IN_THE_REMOTE_CONTAINER = ImmutableSet.of(
            "it.polimi.componentmodifier.FirstComponentModifierImpl",
            "it.polimi.componentmodifier.SecondComponentModifierImpl",
            "it.polimi.componentmodifier.SecondComponentModifierImpl$1");

    private static final boolean EAGER_EVALUATION = false;
    private static final boolean LAZY_EVALUATION = true;

    @Rule public TemporaryFolder temporaryImportedContainersFolder = new TemporaryFolder();
    @Rule public TemporaryFolder temporaryImportedCertificatesFolder = new TemporaryFolder();
    @Rule public TemporaryFolder temporaryTrustedCertificateFolder = new TemporaryFolder();

    @Mock Context mockContext = mock(Context.class);
    @Mock ConnectivityManager mockConnectivityManager = mock(ConnectivityManager.class);
    @Mock NetworkInfo mockNetworkInfo = mock(NetworkInfo.class);
    @Mock PackageManager mockPackageManager = mock(PackageManager.class);
    @Mock ContainerSignatureVerifier mockContainerSignatureVerifier =
            mock(ContainerSignatureVerifier.class);

    @Mock DexFile mockDexFile = mock(DexFile.class);
    @Mock DexClassLoader mockDexClassLoader = mock(DexClassLoader.class);

    @Before
    public void setupMocksForSecureDexClassLoader() throws Exception {
        when(mockContext.getSystemService(CONNECTIVITY_SERVICE))
                .thenReturn(mockConnectivityManager);
        when(mockConnectivityManager.getActiveNetworkInfo()).thenReturn(mockNetworkInfo);
        when(mockNetworkInfo.isConnected()).thenReturn(true);

        when(mockContext.getDir(eq(IMPORTED_CONTAINERS_PRIVATE_DIRECTORY_NAME), eq(MODE_PRIVATE)))
                .thenReturn(temporaryImportedContainersFolder.getRoot());
        when(mockContext.getDir(eq(IMPORTED_CERTIFICATE_PRIVATE_DIRECTORY_NAME), eq(MODE_PRIVATE)))
                .thenReturn(temporaryImportedCertificatesFolder.getRoot());

        DexFileShadow.setDexFileShadow(mockDexFile);

        // IMPORTANT: Always create a new enumeration for this set!
        // Otherwise, the iterator will return all the objects only for the first test case.
        when(mockDexFile.entries()).thenReturn(
                Collections.enumeration(TEST_SET_OF_CLASSES_IN_THE_REMOTE_CONTAINER));

        when(mockContainerSignatureVerifier
                .verifyContainerSignatureAgainstCertificate(
                        any(String.class),
                        any(X509Certificate.class)))
                .thenReturn(false);
        // IMPORTANT: Do not inline this variable!
        // Otherwise, Mockito will complaint.
        X509Certificate trustedCertificate =
                retrieveAndGenerateTrustedCertificate(TEST_REMOTE_CERTIFICATE_URL_AS_STRING);
        when(mockContainerSignatureVerifier
                .verifyContainerSignatureAgainstCertificate(
                        endsWith(TEST_REMOTE_CONTAINER_FILE_NAME),
                        eq(trustedCertificate)))
                .thenReturn(true);

        when(mockDexClassLoader.loadClass(eq(TEST_A_CLASS_TO_LOAD_NOT_IN_THE_CONTAINER)))
                .thenThrow(new ClassNotFoundException());
        when(mockDexClassLoader.loadClass(eq(TEST_CLASS_TO_LOAD)))
                .thenReturn((Class) "notRelevantForTheTest".getClass());
    }

    @Test (expected = IllegalArgumentException.class)
    public void givenASecureDexClassLoaderWithAContainerAndACertificateForVerification_whenLoadClassWithAnEmptyClassName_thenThrows() throws Exception {
        // GIVEN
        SecureDexClassLoader secureDexClassLoader = initializeSecureDexClassLoaderWithTestValues(
                downloadRemoteContainerIntoTemporaryImportedContainersFolder(
                        TEST_REMOTE_CONTAINER_URL_AS_STRING),
                ImmutableMap.of(TEST_PACKAGE_NAME, new URL(TEST_REMOTE_CERTIFICATE_URL_AS_STRING)),
                EAGER_EVALUATION);

        // WHEN
        secureDexClassLoader.loadClass(EMPTY_CLASS_NAME);
    }

    @Test
    public void givenASecureDexClassLoaderWithAContainerAndNoCertificateForItsPackageNameForVerification_whenLoadClassInTheContainerWithEagerEvaluation_thenRemovesRetrievedContainerAndReturnsNull() throws Exception {
        // GIVEN
        SecureDexClassLoader secureDexClassLoader = initializeSecureDexClassLoaderWithTestValues(
                downloadRemoteContainerIntoTemporaryImportedContainersFolder(
                        TEST_REMOTE_CONTAINER_URL_AS_STRING),
                ImmutableMap.of(
                        TEST_A_PACKAGE_NAME_NOT_IN_THE_CONTAINER,
                        new URL(TEST_REMOTE_CERTIFICATE_URL_AS_STRING)),
                EAGER_EVALUATION);

        // WHEN
        Class<?> loadedClass = secureDexClassLoader.loadClass(TEST_CLASS_TO_LOAD);

        // THEN
        assertThatNoFileIsPresentInTheImportedContainersFolderMatchingTheFilter(
                TEST_FILE_FILTER_MATCHING_FILE_NAMED_AS_THE_TEST_CONTAINER);
        assertThat(loadedClass, is(nullValue()));
    }

    @Test
    public void givenASecureDexClassLoaderWithAContainerAndNoCertificateForItsPackageNameForVerification_whenLoadClassInTheContainerWithLazyEvaluation_thenImportsRetrievedContainerButReturnsNull() throws Exception {
        // GIVEN
        SecureDexClassLoader secureDexClassLoader = initializeSecureDexClassLoaderWithTestValues(
                downloadRemoteContainerIntoTemporaryImportedContainersFolder(
                        TEST_REMOTE_CONTAINER_URL_AS_STRING),
                ImmutableMap.of(
                        TEST_A_PACKAGE_NAME_NOT_IN_THE_CONTAINER,
                        new URL(TEST_REMOTE_CERTIFICATE_URL_AS_STRING)),
                LAZY_EVALUATION);

        // WHEN
        Class<?> loadedClass = secureDexClassLoader.loadClass(TEST_CLASS_TO_LOAD);

        // THEN
        assertThatOnlyOneFileIsPresentInTheImportedContainersFolderMatchingTheFilter(
                TEST_FILE_FILTER_MATCHING_FILE_NAMED_AS_THE_TEST_CONTAINER);
        assertThat(loadedClass, is(nullValue()));
    }

    @Test
    public void givenASecureDexClassLoaderWithARepackedContainerAndACertificateUsedToSignThatContainerForItsPackageNameForVerification_whenLoadClassInTheContainerWithEagerEvaluation_thenRemovesRetrievedContainerAndReturnsNull() throws Exception {
        // GIVEN
        SecureDexClassLoader secureDexClassLoader = initializeSecureDexClassLoaderWithTestValues(
                downloadRemoteContainerIntoTemporaryImportedContainersFolder(
                        TEST_REMOTE_REPACK_CONTAINER_URL_AS_STRING),
                ImmutableMap.of(TEST_PACKAGE_NAME, new URL(TEST_REMOTE_CERTIFICATE_URL_AS_STRING)),
                EAGER_EVALUATION);

        // WHEN
        Class<?> loadedClass = secureDexClassLoader.loadClass(TEST_CLASS_TO_LOAD);

        // THEN
        assertThatNoFileIsPresentInTheImportedContainersFolderMatchingTheFilter(
                TEST_FILE_FILTER_MATCHING_FILE_NAMED_AS_THE_TEST_REPACK_CONTAINER);
        assertThat(loadedClass, is(nullValue()));
    }

    @Test
    public void givenASecureDexClassLoaderWithARepackedContainerAndACertificateUsedToSignThatContainerForItsPackageNameForVerification_whenLoadClassInTheContainerWithLazyEvaluation_thenRemovesRetrievedContainerAndReturnsNull() throws Exception {
        // GIVEN
        SecureDexClassLoader secureDexClassLoader = initializeSecureDexClassLoaderWithTestValues(
                downloadRemoteContainerIntoTemporaryImportedContainersFolder(
                        TEST_REMOTE_REPACK_CONTAINER_URL_AS_STRING),
                ImmutableMap.of(TEST_PACKAGE_NAME, new URL(TEST_REMOTE_CERTIFICATE_URL_AS_STRING)),
                LAZY_EVALUATION);

        // WHEN
        Class<?> loadedClass = secureDexClassLoader.loadClass(TEST_CLASS_TO_LOAD);

        // THEN
        assertThatNoFileIsPresentInTheImportedContainersFolderMatchingTheFilter(
                TEST_FILE_FILTER_MATCHING_FILE_NAMED_AS_THE_TEST_REPACK_CONTAINER);
        assertThat(loadedClass, is(nullValue()));
    }

    @Test
    public void givenASecureDexClassLoaderWithAContainerAndACertificateNotUsedForSigningTheContainerForItsPackageNameForVerification_whenLoadClassInTheContainerWithEagerEvaluation_thenRemovesRetrievedContainerAndReturnsNull() throws Exception {
        // GIVEN
        SecureDexClassLoader secureDexClassLoader = initializeSecureDexClassLoaderWithTestValues(
                downloadRemoteContainerIntoTemporaryImportedContainersFolder(
                        TEST_REMOTE_CONTAINER_URL_AS_STRING),
                ImmutableMap.of(
                        TEST_PACKAGE_NAME,
                        new URL(TEST_A_REMOTE_CERTIFICATE_NOT_USED_TO_SIGN_THE_CONTAINER_URL_AS_STRING)),
                EAGER_EVALUATION);

        // WHEN
        Class<?> loadedClass = secureDexClassLoader.loadClass(TEST_CLASS_TO_LOAD);

        // THEN
        assertThatNoFileIsPresentInTheImportedContainersFolderMatchingTheFilter(
                TEST_FILE_FILTER_MATCHING_FILE_NAMED_AS_THE_TEST_CONTAINER);
        assertThat(loadedClass, is(nullValue()));
    }

    @Test
    public void givenASecureDexClassLoaderWithAContainerAndACertificateNotUsedForSigningTheContainerForItsPackageNameForVerification_whenLoadClassInTheContainerWithLazyEvaluation_thenRemovesRetrievedContainerAndReturnsNull() throws Exception {
        // GIVEN
        SecureDexClassLoader secureDexClassLoader = initializeSecureDexClassLoaderWithTestValues(
                downloadRemoteContainerIntoTemporaryImportedContainersFolder(
                        TEST_REMOTE_CONTAINER_URL_AS_STRING),
                ImmutableMap.of(
                        TEST_PACKAGE_NAME,
                        new URL(TEST_A_REMOTE_CERTIFICATE_NOT_USED_TO_SIGN_THE_CONTAINER_URL_AS_STRING)),
                LAZY_EVALUATION);

        // WHEN
        Class<?> loadedClass = secureDexClassLoader.loadClass(TEST_CLASS_TO_LOAD);

        // THEN
        assertThatNoFileIsPresentInTheImportedContainersFolderMatchingTheFilter(
                TEST_FILE_FILTER_MATCHING_FILE_NAMED_AS_THE_TEST_CONTAINER);
        assertThat(loadedClass, is(nullValue()));
    }

    @Test (expected = ClassNotFoundException.class)
    public void givenASecureDexClassLoaderWithAContainerAndACertificateUsedToSignThatContainerForItsPackageNameForVerification_whenLoadClassNotInTheContainerWithEagerEvaluation_thenImportRetrievedContainerButThrows() throws Exception {
        // GIVEN
        SecureDexClassLoader secureDexClassLoader = initializeSecureDexClassLoaderWithTestValues(
                downloadRemoteContainerIntoTemporaryImportedContainersFolder(
                        TEST_REMOTE_CONTAINER_URL_AS_STRING),
                ImmutableMap.of(
                        TEST_PACKAGE_NAME,
                        new URL(TEST_REMOTE_CERTIFICATE_URL_AS_STRING)),
                EAGER_EVALUATION);

        // WHEN + THEN
        assertThatOnlyOneFileIsPresentInTheImportedContainersFolderMatchingTheFilter(
                TEST_FILE_FILTER_MATCHING_FILE_NAMED_AS_THE_TEST_CONTAINER);
        secureDexClassLoader.loadClass(TEST_A_CLASS_TO_LOAD_NOT_IN_THE_CONTAINER);
    }

    @Test (expected = ClassNotFoundException.class)
    public void givenASecureDexClassLoaderWithAContainerAndACertificateUsedToSignThatContainerForItsPackageNameForVerification_whenLoadClassNotInTheContainerWithLazyEvaluation_thenImportRetrievedContainerButThrows() throws Exception {
        // GIVEN
        SecureDexClassLoader secureDexClassLoader = initializeSecureDexClassLoaderWithTestValues(
                downloadRemoteContainerIntoTemporaryImportedContainersFolder(
                        TEST_REMOTE_CONTAINER_URL_AS_STRING),
                ImmutableMap.of(
                        TEST_PACKAGE_NAME,
                        new URL(TEST_REMOTE_CERTIFICATE_URL_AS_STRING)),
                LAZY_EVALUATION);

        // WHEN + THEN
        assertThatOnlyOneFileIsPresentInTheImportedContainersFolderMatchingTheFilter(
                TEST_FILE_FILTER_MATCHING_FILE_NAMED_AS_THE_TEST_CONTAINER);
        secureDexClassLoader.loadClass(TEST_A_CLASS_TO_LOAD_NOT_IN_THE_CONTAINER);
    }

    @Test
    public void givenASecureDexClassLoaderWithAContainerAndACertificateUsedToSignThatContainerForItsPackageNameForVerification_whenLoadClassInTheContainerWithEagerEvaluation_thenImportRetrievedContainerAndReturnsAClassInstance() throws Exception {
        // GIVEN
        SecureDexClassLoader secureDexClassLoader = initializeSecureDexClassLoaderWithTestValues(
                downloadRemoteContainerIntoTemporaryImportedContainersFolder(
                        TEST_REMOTE_CONTAINER_URL_AS_STRING),
                ImmutableMap.of(
                        TEST_PACKAGE_NAME,
                        new URL(TEST_REMOTE_CERTIFICATE_URL_AS_STRING)),
                EAGER_EVALUATION);

        // WHEN
        Class<?> loadedClass = secureDexClassLoader.loadClass(TEST_CLASS_TO_LOAD);

        // THEN
        assertThatOnlyOneFileIsPresentInTheImportedContainersFolderMatchingTheFilter(
                TEST_FILE_FILTER_MATCHING_FILE_NAMED_AS_THE_TEST_CONTAINER);
        verify(mockDexClassLoader).loadClass(eq(TEST_CLASS_TO_LOAD));
        assertThat(loadedClass, is(notNullValue()));
    }

    @Test
    public void givenASecureDexClassLoaderWithAContainerAndACertificateUsedToSignThatContainerForItsPackageNameForVerification_whenLoadClassInTheContainerWithLazyEvaluation_thenImportRetrievedContainerAndReturnsAClassInstance() throws Exception {
        // GIVEN
        SecureDexClassLoader secureDexClassLoader = initializeSecureDexClassLoaderWithTestValues(
                downloadRemoteContainerIntoTemporaryImportedContainersFolder(
                        TEST_REMOTE_CONTAINER_URL_AS_STRING),
                ImmutableMap.of(
                        TEST_PACKAGE_NAME,
                        new URL(TEST_REMOTE_CERTIFICATE_URL_AS_STRING)),
                LAZY_EVALUATION);

        // WHEN
        Class<?> loadedClass = secureDexClassLoader.loadClass(TEST_CLASS_TO_LOAD);

        // THEN
        assertThatOnlyOneFileIsPresentInTheImportedContainersFolderMatchingTheFilter(
                TEST_FILE_FILTER_MATCHING_FILE_NAMED_AS_THE_TEST_CONTAINER);
        verify(mockDexClassLoader).loadClass(eq(TEST_CLASS_TO_LOAD));
        assertThat(loadedClass, is(notNullValue()));
    }

    @Test
    public void givenASecureDexClassLoaderWithAnAPKContainerAndACertificateUsedToSignThatContainerForItsPackageNameForVerification_whenLoadMultipleClassesInTheContainerWhosePackageNameHasTheVerifiedOneAsPrefix_thenReturnsMultipleNotNullClassInstances() throws Exception {
        // GIVEN
        final String testRemoteContainerName = "CardReaderApplication-release.apk";
        final String testRemoteApkContainerUrlAsString =
                "https://dl.dropboxusercontent.com/s/8s840gb9qhhr843/" + testRemoteContainerName;
        final String testRemoteCertificateUrlAsString =
                "https://dl.dropboxusercontent.com/s/ala85tq3ocimgi9/cardReaderTestCertificate.pem";

        final String testCommonPrefixPackageName = "com.example.android";
        final String testFirstActivityClassToLoad = "com.example.android.cardreader.MainActivity";
        final String testSecondActivityClassToLoad = "com.example.android.common.activities.SampleActivityBase";

        // When reading the classes stored in the Dex file, return
        // the set with the two classes to load
        when(mockDexFile.entries()).thenReturn(
                Collections.enumeration(
                        ImmutableSet.of(testFirstActivityClassToLoad, testSecondActivityClassToLoad)));
        when(mockDexClassLoader.loadClass(eq(testFirstActivityClassToLoad)))
                .thenReturn((Class) "notRelevantForTheTest".getClass());
        when(mockDexClassLoader.loadClass(eq(testSecondActivityClassToLoad)))
                .thenReturn((Class) "notRelevantForTheTest".getClass());

        // IMPORTANT: Do not inline this variable!
        // Otherwise, Mockito will complaint.
        X509Certificate trustedCertificate =
                retrieveAndGenerateTrustedCertificate(testRemoteCertificateUrlAsString);
        when(mockContainerSignatureVerifier
                .verifyContainerSignatureAgainstCertificate(
                        endsWith(testRemoteContainerName),
                        eq(trustedCertificate)))
                .thenReturn(true);

        SecureDexClassLoader secureDexClassLoader = initializeSecureDexClassLoaderWithTestValues(
                downloadRemoteContainerIntoTemporaryImportedContainersFolder(
                        testRemoteApkContainerUrlAsString),
                ImmutableMap.of(
                        testCommonPrefixPackageName,
                        new URL(testRemoteCertificateUrlAsString)),
                LAZY_EVALUATION);

        // WHEN
        Class<?> firstLoadedActivityClass = secureDexClassLoader.loadClass(testFirstActivityClassToLoad);
        Class<?> secondLoadedActivityClass = secureDexClassLoader.loadClass(testSecondActivityClassToLoad);

        // THEN
        verify(mockDexClassLoader).loadClass(eq(testFirstActivityClassToLoad));
        assertThat(firstLoadedActivityClass, is(notNullValue()));
        verify(mockDexClassLoader).loadClass(eq(testSecondActivityClassToLoad));
        assertThat(secondLoadedActivityClass, is(notNullValue()));
    }

    @Test
    public void givenASecureDexClassLoaderWithAContainerAndACertificateAssociatedToIt_whenWipeOutPrivateAppCachedDataForNoneContainersAndCertificates_thenAllImportedFilesAreNotErasedAndLoadClassReturnsAClass() throws Exception {
        // GIVEN
        SecureDexClassLoader secureDexClassLoader = initializeSecureDexClassLoaderWithTestValues(
                downloadRemoteContainerIntoTemporaryImportedContainersFolder(
                        TEST_REMOTE_CONTAINER_URL_AS_STRING),
                ImmutableMap.of(
                        TEST_PACKAGE_NAME,
                        new URL(TEST_REMOTE_CERTIFICATE_URL_AS_STRING)),
                EAGER_EVALUATION);

        // WHEN
        secureDexClassLoader.wipeOutPrivateAppCachedData(false, false);

        // THEN
        assertThatOnlyOneFileIsPresentInTheImportedContainersFolderMatchingTheFilter(
                TEST_FILE_FILTER_MATCHING_FILE_NAMED_AS_THE_TEST_CONTAINER);
        assertThatOnlyOneFileIsPresentInTheImportedCertificateFolderMatchingTheFilter(
                TEST_FILE_FILTER_MATCHING_CERTIFICATE_NAMED_AS_THE_PACKAGE_NAME);
        assertThat(secureDexClassLoader.loadClass(TEST_CLASS_TO_LOAD), is(notNullValue()));
    }

    @Test
    public void givenASecureDexClassLoaderWithAContainerAndACertificateAssociatedToIt_whenWipeOutPrivateAppCachedDataForContainers_thenAllImportedContainersAreErasedAndLoadClassReturnsNull() throws Exception {
        // GIVEN
        SecureDexClassLoader secureDexClassLoader = initializeSecureDexClassLoaderWithTestValues(
                downloadRemoteContainerIntoTemporaryImportedContainersFolder(
                        TEST_REMOTE_CONTAINER_URL_AS_STRING),
                ImmutableMap.of(
                        TEST_PACKAGE_NAME,
                        new URL(TEST_REMOTE_CERTIFICATE_URL_AS_STRING)),
                EAGER_EVALUATION);

        // WHEN
        secureDexClassLoader.wipeOutPrivateAppCachedData(true, false);

        // THEN
        assertThatNoFileIsPresentInTheImportedContainersFolderMatchingTheFilter(
                TEST_FILE_FILTER_MATCHING_FILE_NAMED_AS_THE_TEST_CONTAINER);
        assertThatOnlyOneFileIsPresentInTheImportedCertificateFolderMatchingTheFilter(
                TEST_FILE_FILTER_MATCHING_CERTIFICATE_NAMED_AS_THE_PACKAGE_NAME);
        assertThat(secureDexClassLoader.loadClass(TEST_CLASS_TO_LOAD), is(nullValue()));
    }

    @Test
    public void givenASecureDexClassLoaderWithAContainerAndACertificateAssociatedToIt_whenWipeOutPrivateAppCachedDataForCertificates_thenAllImportedCertificatesAreErasedAndLoadClassReturnsNull() throws Exception {
        // GIVEN
        SecureDexClassLoader secureDexClassLoader = initializeSecureDexClassLoaderWithTestValues(
                downloadRemoteContainerIntoTemporaryImportedContainersFolder(
                        TEST_REMOTE_CONTAINER_URL_AS_STRING),
                ImmutableMap.of(
                        TEST_PACKAGE_NAME,
                        new URL(TEST_REMOTE_CERTIFICATE_URL_AS_STRING)),
                EAGER_EVALUATION);

        // WHEN
        secureDexClassLoader.wipeOutPrivateAppCachedData(false, true);

        // THEN
        assertThatOnlyOneFileIsPresentInTheImportedContainersFolderMatchingTheFilter(
                TEST_FILE_FILTER_MATCHING_FILE_NAMED_AS_THE_TEST_CONTAINER);
        assertThatNoFileIsPresentInTheImportedCertificateFolderMatchingTheFilter(
                TEST_FILE_FILTER_MATCHING_CERTIFICATE_NAMED_AS_THE_PACKAGE_NAME);
        assertThat(secureDexClassLoader.loadClass(TEST_CLASS_TO_LOAD), is(nullValue()));
    }

    @Test
    public void givenASecureDexClassLoaderWithAContainerAndACertificateAssociatedToIt_whenWipeOutPrivateAppCachedDataForBothContainersAndCertificates_thenAllImportedFilesAreErasedAndLoadClassReturnsNull() throws Exception {
        // GIVEN
        SecureDexClassLoader secureDexClassLoader = initializeSecureDexClassLoaderWithTestValues(
                downloadRemoteContainerIntoTemporaryImportedContainersFolder(
                        TEST_REMOTE_CONTAINER_URL_AS_STRING),
                ImmutableMap.of(
                        TEST_PACKAGE_NAME,
                        new URL(TEST_REMOTE_CERTIFICATE_URL_AS_STRING)),
                EAGER_EVALUATION);

        // WHEN
        secureDexClassLoader.wipeOutPrivateAppCachedData(true, true);

        // THEN
        assertThatNoFileIsPresentInTheImportedContainersFolderMatchingTheFilter(
                TEST_FILE_FILTER_MATCHING_FILE_NAMED_AS_THE_TEST_CONTAINER);
        assertThatNoFileIsPresentInTheImportedCertificateFolderMatchingTheFilter(
                TEST_FILE_FILTER_MATCHING_CERTIFICATE_NAMED_AS_THE_PACKAGE_NAME);
        assertThat(secureDexClassLoader.loadClass(TEST_CLASS_TO_LOAD), is(nullValue()));
    }

    private SecureDexClassLoader initializeSecureDexClassLoaderWithTestValues(
            String dexPathWithLocalContainers,
            Map<String, URL> sanitizedPackageNameToCertificateURLMap,
            boolean performLazyEvaluation) throws CertificateException {
        return new SecureDexClassLoader(
                dexPathWithLocalContainers,
                mockDexClassLoader,
                mockContext,
                sanitizedPackageNameToCertificateURLMap,
                performLazyEvaluation,
                mockContainerSignatureVerifier,
                CertificateFactory.getInstance(X_509_CERTIFICATE));
    }

    private String downloadRemoteContainerIntoTemporaryImportedContainersFolder(
            String remoteContainerURLAsString) throws Exception {
        return downloadRemoteResourceIntoTemporaryFolder(
                remoteContainerURLAsString, temporaryImportedContainersFolder);
    }

    private X509Certificate retrieveAndGenerateTrustedCertificate(
            String remoteCertificateURLAsString) throws Exception {
        String trustedCertificatePath = downloadRemoteResourceIntoTemporaryFolder(
                remoteCertificateURLAsString, temporaryTrustedCertificateFolder);

        InputStream inStream = new FileInputStream(trustedCertificatePath);
        X509Certificate trustedCertificate = (X509Certificate) CertificateFactory
                .getInstance(X_509_CERTIFICATE)
                .generateCertificate(inStream);

        assertNotNull(inStream);
        inStream.close();

        return trustedCertificate;
    }

    private String downloadRemoteResourceIntoTemporaryFolder(
            String remoteURLAsString,
            TemporaryFolder temporaryFolder) throws Exception {
        URL remoteURL = new URL(remoteURLAsString);

        String localContainerURI = temporaryFolder.getRoot().toString()
                + File.separator + extractFileNameFromFilePath(remoteURL.getPath());

        boolean isRemoteResourceSuccessful = new FileDownloader(mockContext).downloadRemoteResource(
                remoteURL,
                localContainerURI,
                false);

        assertTrue(isRemoteResourceSuccessful);
        return localContainerURI;
    }

    private void assertThatNoFileIsPresentInTheImportedContainersFolderMatchingTheFilter(
            FileFilter testFileFilter) {
        assertThatFileIsPresentInTheTemporaryFolderMatchingTheFilterInQuantity(
                temporaryImportedContainersFolder, testFileFilter, 0);
    }

    private void assertThatOnlyOneFileIsPresentInTheImportedContainersFolderMatchingTheFilter(
            FileFilter testFileFilter) {
        assertThatFileIsPresentInTheTemporaryFolderMatchingTheFilterInQuantity(
                temporaryImportedContainersFolder, testFileFilter, 1);
    }

    private void assertThatNoFileIsPresentInTheImportedCertificateFolderMatchingTheFilter(
            FileFilterByNameMatch testFileFilter) {
        assertThatFileIsPresentInTheTemporaryFolderMatchingTheFilterInQuantity(
                temporaryImportedCertificatesFolder, testFileFilter, 0);
    }

    private void assertThatOnlyOneFileIsPresentInTheImportedCertificateFolderMatchingTheFilter(
            FileFilterByNameMatch testFileFilter) {
        assertThatFileIsPresentInTheTemporaryFolderMatchingTheFilterInQuantity(
                temporaryImportedCertificatesFolder, testFileFilter, 1);
    }

    private void assertThatFileIsPresentInTheTemporaryFolderMatchingTheFilterInQuantity(
            TemporaryFolder temporaryFolder, FileFilter testFileFilter, int numberOfMatches) {
        assertThat(
                temporaryFolder
                        .getRoot()
                        .listFiles(testFileFilter)
                        .length,
                is(equalTo(numberOfMatches)));
    }
}
