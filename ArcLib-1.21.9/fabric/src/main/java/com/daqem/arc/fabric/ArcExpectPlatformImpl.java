package com.daqem.arc.fabric;

import com.daqem.arc.ArcExpectPlatform;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class ArcExpectPlatformImpl {
    /**
     * This is our actual method to {@link ArcExpectPlatform#getConfigDirectory()}.
     */
    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
