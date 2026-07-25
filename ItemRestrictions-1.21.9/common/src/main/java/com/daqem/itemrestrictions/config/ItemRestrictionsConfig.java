package com.daqem.itemrestrictions.config;

import com.daqem.itemrestrictions.ItemRestrictions;
import com.daqem.yamlconfig.api.config.ConfigExtension;
import com.daqem.yamlconfig.api.config.ConfigType;
import com.daqem.yamlconfig.api.config.IConfigBuilder;
import com.daqem.yamlconfig.impl.config.ConfigBuilder;

import java.util.function.Supplier;

public class ItemRestrictionsConfig {

    public static final Supplier<Boolean> isDebug;

    static {
        IConfigBuilder config = new ConfigBuilder(ItemRestrictions.MOD_ID, "item-restrictions-common", ConfigExtension.YAML, ConfigType.COMMON);

        config.push("debug");
        isDebug = config.defineBoolean("is_debug", false)
                .withComments("if true, debug mode is enabled");
        config.pop();

        config.build();
    }

    public static void init() {
    }

}
