package it.necst.grabnrun.shadows;

import org.robolectric.annotation.Implements;
import org.robolectric.internal.Shadow;

import java.io.File;

import dalvik.system.BaseDexClassLoader;

@Implements(BaseDexClassLoader.class)
public class BaseDexClassLoaderShadow extends Shadow {

    private String dexPath;
    private File optimizedDirectory;
    private String libraryPath;
    private ClassLoader parent;

    public void __constructor__(
            String dexPath,
            File optimizedDirectory,
            String libraryPath,
            ClassLoader parent) {
        this.dexPath = dexPath;
        this.optimizedDirectory = optimizedDirectory;
        this.libraryPath = libraryPath;
        this.parent = parent;
    }
}
