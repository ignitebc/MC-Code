package com.daqem.arc.config;

import com.daqem.arc.Arc;
import com.daqem.yamlconfig.api.config.ConfigExtension;
import com.daqem.yamlconfig.api.config.ConfigType;
import com.daqem.yamlconfig.api.config.IConfigBuilder;
import com.daqem.yamlconfig.api.config.entry.IConfigEntry;
import com.daqem.yamlconfig.impl.config.ConfigBuilder;

public class ArcCommonConfig {

    public static void init() {
    }

    public static final IConfigEntry<Integer> maxBlockPosCacheSize;
    public static final IConfigEntry<Boolean> isDebug;


    static {
        IConfigBuilder config = new ConfigBuilder(Arc.MOD_ID, "arc-common", ConfigExtension.YAML, ConfigType.COMMON);

        config.push("block");
        maxBlockPosCacheSize = config.defineInteger("max_block_pos_cache_size", 10_000)
                .withComments("The maximum size of the block pos cache, used to not drop double drops on placed blocks");
        config.pop();

        config.push("debug");
        isDebug = config.defineBoolean("is_debug", false)
                .withComments("if true, debug mode is enabled");
        config.pop();

        config.build();
    }
}
