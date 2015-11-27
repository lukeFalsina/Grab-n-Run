package it.necst.grabnrun;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static android.content.Context.MODE_PRIVATE;
import static it.necst.grabnrun.SecureLoaderFactory.IMPORTED_CONTAINERS_PRIVATE_DIRECTORY_NAME;
import static it.necst.grabnrun.SecureLoaderFactory.OUTPUT_DEX_CLASSES_DIRECTORY_NAME;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.test.mock.MockContext;

import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URL;

@RunWith(MockitoJUnitRunner.class)
public class SecureLoaderFactoryTest {

    private static final String EMPTY_DEX_PATH = "";
    private static final String TEST_REMOTE_CONTAINER_URL_AS_STRING =
            "https://dl.dropboxusercontent.com/u/28681922/componentModifier.jar";
    private static final String TEST_PACKAGE_NAME = "com.example.application";
    private static final String TEST_REMOTE_CERTIFICATE_URL_AS_STRING =
            "https://dl.dropboxusercontent.com/u/28681922/test_cert.pem";

    @Rule public TemporaryFolder temporaryImportedContainersFolder = new TemporaryFolder();
    @Rule public TemporaryFolder temporaryOutputDexFolder = new TemporaryFolder();

    @Mock MockContext mockContext = new MockContext();
    @Mock ConnectivityManager mockConnectivityManager = mock(ConnectivityManager.class);
    @Mock NetworkInfo mockNetworkInfo = mock(NetworkInfo.class);
    @Mock ClassLoader mockClassLoader = mock(ClassLoader.class);

    SecureLoaderFactory testSecureLoaderFactory;

    @Before
    public void setupMocksForSecureLoaderFactory() {
        when(mockContext.getSystemService(CONNECTIVITY_SERVICE))
                .thenReturn(mockConnectivityManager);
        when(mockConnectivityManager.getActiveNetworkInfo()).thenReturn(mockNetworkInfo);
        when(mockNetworkInfo.isConnected()).thenReturn(true);

        when(mockContext.getDir(eq(IMPORTED_CONTAINERS_PRIVATE_DIRECTORY_NAME), eq(MODE_PRIVATE)))
                .thenReturn(temporaryImportedContainersFolder.getRoot());
        when(mockContext.getDir(eq(OUTPUT_DEX_CLASSES_DIRECTORY_NAME), eq(MODE_PRIVATE)))
                .thenReturn(temporaryOutputDexFolder.getRoot());

        testSecureLoaderFactory = new SecureLoaderFactory(mockContext);
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

    @Ignore("It throws a NullPointerException because some internals of the android.jar " +
            "are not mocked in the dalvik.system package.")
    @Test
    public void givenADexPathPointingToARemoteResource_whenCreateDexClassLoader_thenASecureDexClassLoaderObjectIsReturned() throws Exception {

        // WHEN
        testSecureLoaderFactory.createDexClassLoader(
                TEST_REMOTE_CONTAINER_URL_AS_STRING,
                null,
                mockClassLoader,
                ImmutableMap.of(TEST_PACKAGE_NAME, new URL(TEST_REMOTE_CERTIFICATE_URL_AS_STRING)));
    }
}
