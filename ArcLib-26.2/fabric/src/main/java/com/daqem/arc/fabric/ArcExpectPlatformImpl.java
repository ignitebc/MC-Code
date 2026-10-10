package com.daqem.arc.fabric;

import com.daqem.arc.ArcExpectPlatform;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class ArcExpectPlatformImpl {
    /**
     * {@link ArcExpectPlatform#getConfigDirectory()}의 실제 구현.
     */
    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
