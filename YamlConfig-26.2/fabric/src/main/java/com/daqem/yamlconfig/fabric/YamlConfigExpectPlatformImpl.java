package com.daqem.yamlconfig.fabric;

import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class YamlConfigExpectPlatformImpl {
    /**
     * {@link com.daqem.yamlconfig.YamlConfigExpectPlatform#getConfigDirectory()}의 실제 구현 메서드.
     */
    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }
}
