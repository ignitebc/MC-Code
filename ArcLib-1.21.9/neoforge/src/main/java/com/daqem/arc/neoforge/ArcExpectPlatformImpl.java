package com.daqem.arc.neoforge;

import com.daqem.arc.ArcExpectPlatform;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class ArcExpectPlatformImpl {
    /**
     * This is our actual method to {@link ArcExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }
}