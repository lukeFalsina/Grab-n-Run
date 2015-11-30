package it.necst.grabnrun.shadows;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.internal.Shadow;

import java.io.File;

import dalvik.system.BaseDexClassLoader;
import dalvik.system.DexClassLoader;

@Implements(BaseDexClassLoader.class)
public class BaseDexClassLoaderShadow extends Shadow {
    private static DexClassLoader dexClassLoader;

    private String dexPath;
    private File optimizedDirectory;
    private String libraryPath;
    private ClassLoader parent;

    public static void setDexClassLoaderShadow(DexClassLoader dexClassLoader) {
        BaseDexClassLoaderShadow.dexClassLoader = dexClassLoader;
    }

    public void __constructor__(String dexPath, File optimizedDirectory, String libraryPath, ClassLoader parent) {
        this.dexPath = dexPath;
        this.optimizedDirectory = optimizedDirectory;
        this.libraryPath = libraryPath;
        this.parent = parent;
    }

    @Implementation
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        return dexClassLoader.loadClass(className);
    }
}
