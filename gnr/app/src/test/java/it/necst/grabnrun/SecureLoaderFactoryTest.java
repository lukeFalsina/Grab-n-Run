package it.necst.grabnrun;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static it.necst.grabnrun.SecureLoaderFactory.IMPORTED_CONTAINERS_PRIVATE_DIRECTORY_NAME;
import static it.necst.grabnrun.SecureLoaderFactory.OUTPUT_DEX_CLASSES_DIRECTORY_NAME;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

import dalvik.system.DexFile;
import it.necst.grabnrun.shadows.BaseDexClassLoaderShadow;
import it.necst.grabnrun.shadows.DexFileShadow;
import it.polimi.necst.gnr.BuildConfig;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, shadows={BaseDexClassLoaderShadow.class, DexFileShadow.class})
public class SecureLoaderFactoryTest {

    private static final String EMPTY_DEX_PATH = "";
    private static final String TEST_REMOTE_CONTAINER_URL_AS_STRING =
            "https://dl.dropboxusercontent.com/u/28681922/componentModifier.jar";
    private static final String TEST_PACKAGE_NAME = "com.example.application";

    private static final String TEST_REMOTE_CERTIFICATE_URL_AS_STRING =
            "https://dl.dropboxusercontent.com/u/28681922/test_cert.pem";

    private static final ImmutableSet<String> TEST_SET_OF_CLASSES_IN_THE_REMOTE_CONTAINER = ImmutableSet.of(
            "it.polimi.componentmodifier.FirstComponentModifierImpl",
            "it.polimi.componentmodifier.SecondComponentModifierImpl",
            "it.polimi.componentmodifier.SecondComponentModifierImpl$1");

    @Rule public TemporaryFolder temporaryImportedContainersFolder = new TemporaryFolder();
    @Rule public TemporaryFolder temporaryOutputDexFolder = new TemporaryFolder();
    @Rule public TemporaryFolder temporaryFolderHoldingALocalContainer = new TemporaryFolder();

    @Mock Context mockContext = mock(Context.class);
    @Mock ConnectivityManager mockConnectivityManager = mock(ConnectivityManager.class);
    @Mock NetworkInfo mockNetworkInfo = mock(NetworkInfo.class);
    @Mock ClassLoader mockClassLoader = mock(ClassLoader.class);
    @Mock PackageManager mockPackageManager = mock(PackageManager.class);

    @Mock DexFile mockDexFile = mock(DexFile.class);

    SecureLoaderFactory testSecureLoaderFactory;

    @Before
    public void setupMocksForSecureLoaderFactory() {
        when(mockContext.getSystemService(CONNECTIVITY_SERVICE))
                .thenReturn(mockConnectivityManager);
        when(mockConnectivityManager.getActiveNetworkInfo()).thenReturn(mockNetworkInfo);
        when(mockNetworkInfo.isConnected()).thenReturn(true);

        when(mockContext.getPackageManager())
                .thenReturn(mockPackageManager);

        when(mockContext.getDir(eq(IMPORTED_CONTAINERS_PRIVATE_DIRECTORY_NAME), eq(MODE_PRIVATE)))
                .thenReturn(temporaryImportedContainersFolder.getRoot());
        when(mockContext.getDir(eq(OUTPUT_DEX_CLASSES_DIRECTORY_NAME), eq(MODE_PRIVATE)))
                .thenReturn(temporaryOutputDexFolder.getRoot());

        testSecureLoaderFactory = new SecureLoaderFactory(mockContext);

        // IMPORTANT: Always create a new enumeration for this set!
        // Otherwise, the iterator will return all the objects only for the first test case.
        when(mockDexFile.entries()).thenReturn(
                Collections.enumeration(TEST_SET_OF_CLASSES_IN_THE_REMOTE_CONTAINER));

        DexFileShadow.setDexFileShadow(mockDexFile);
    }

    @Test (expected = IllegalArgumentException.class)
    public void givenANonPositiveNumberOfDaysBeforeContainerCacheExpiration_whenCreateSecureLoaderFactory_thenThrows() {
        // GIVEN
        int notPositiveNumberOfDaysBeforeContainerCacheExpiration = 0;

        // WHEN
        new SecureLoaderFactory(mockContext, notPositiveNumberOfDaysBeforeContainerCacheExpiration);
    }

    @Test (expected = IllegalArgumentException.class)
    public void givenAnEmptyDexPath_whenCreateDexClassLoader_thenThrows() throws Exception {
        // WHEN
        testSecureLoaderFactory.createDexClassLoader(
                EMPTY_DEX_PATH,
                null,
                mockClassLoader,
                ImmutableMap.of(TEST_PACKAGE_NAME, new URL(TEST_REMOTE_CERTIFICATE_URL_AS_STRING)));
    }

    @Test (expected = IllegalArgumentException.class)
    public void givenADexPathPointingToARemoteResourceButThePackageNameToCertificateMapIsEmpty_whenCreateDexClassLoader_thenThrows() throws Exception {
        // GIVEN
        ImmutableMap<String, URL> emptyPackageNameToCertificateMap = ImmutableMap.of();

        // WHEN
        testSecureLoaderFactory.createDexClassLoader(
                TEST_REMOTE_CONTAINER_URL_AS_STRING,
                null,
                mockClassLoader,
                emptyPackageNameToCertificateMap);
    }

    @Test
    public void givenADexPathPointingToARemoteResourceAndThePackageNameToCertificateMapContainsPackageNamesAssociatedToNullValues_whenCreateDexClassLoader_thenReturnsASecureDexClassLoaderObject() throws Exception {
        // GIVEN
        Map<String, URL> packageNamePointingToNullCertificateMap = Maps.newHashMap();
        packageNamePointingToNullCertificateMap.put(TEST_PACKAGE_NAME, null);

        // WHEN
        SecureDexClassLoader secureDexClassLoader = testSecureLoaderFactory.createDexClassLoader(
                TEST_REMOTE_CONTAINER_URL_AS_STRING,
                null,
                mockClassLoader,
                packageNamePointingToNullCertificateMap);

        // THEN
        assertThat(secureDexClassLoader, is(notNullValue()));
    }

    @Test
    public void givenADexPathPointingToARemoteResourceAndThePackageNameOfTheTargetClassHasACertificate_whenCreateDexClassLoader_thenReturnsASecureDexClassLoaderObject() throws Exception {
        // WHEN
        SecureDexClassLoader secureDexClassLoader = testSecureLoaderFactory.createDexClassLoader(
                TEST_REMOTE_CONTAINER_URL_AS_STRING,
                null,
                mockClassLoader,
                ImmutableMap.of(TEST_PACKAGE_NAME, new URL(TEST_REMOTE_CERTIFICATE_URL_AS_STRING)));

        // THEN
        assertThat(secureDexClassLoader, is(notNullValue()));
    }

    @Test
    public void givenADexPathPointingToALocalResourceAndThePackageNameOfTheTargetClassHasACertificate_whenCreateDexClassLoader_thenReturnsASecureDexClassLoaderObject() throws Exception {
        // GIVEN
        String testRemoteContainerURI =
                downloadRemoteContainerIntoTemporaryFolderHoldingALocalContainer();

        // WHEN
        SecureDexClassLoader secureDexClassLoader = testSecureLoaderFactory.createDexClassLoader(
                testRemoteContainerURI,
                null,
                mockClassLoader,
                ImmutableMap.of(TEST_PACKAGE_NAME, new URL(TEST_REMOTE_CERTIFICATE_URL_AS_STRING)));

        // THEN
        assertThat(secureDexClassLoader, is(notNullValue()));
    }

    private String downloadRemoteContainerIntoTemporaryFolderHoldingALocalContainer() throws Exception {
        String localContainerURI = temporaryFolderHoldingALocalContainer.getRoot().toString()
                + File.separator + "componentModifier.jar";

        boolean isRemoteResourceSuccessful = new FileDownloader(mockContext).downloadRemoteResource(
                new URL(TEST_REMOTE_CONTAINER_URL_AS_STRING),
                localContainerURI,
                false);

        assertTrue(isRemoteResourceSuccessful);
        return localContainerURI;
    }
}
