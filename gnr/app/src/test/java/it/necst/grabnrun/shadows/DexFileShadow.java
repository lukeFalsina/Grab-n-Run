package it.necst.grabnrun.shadows;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.io.IOException;
import java.util.Enumeration;

import dalvik.system.DexFile;

@Implements(DexFile.class)
public class DexFileShadow {
    private static DexFile dexFile;

    public static void setDexFileShadow(DexFile dexFile) {
        DexFileShadow.dexFile = dexFile;
    }

    @Implementation
    public static DexFile loadDex(String sourcePathName, String outputPathName, int flags) throws IOException {
        return dexFile;
    }

    @Implementation
    public Enumeration<String> entries() {
        return dexFile.entries();
    }
}
