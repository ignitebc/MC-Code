package com.tacz.guns.config;

import com.tacz.guns.config.sync.SyncConfig;
import net.minecraftforge.common.ForgeConfigSpec;

public class ServerConfig {
    /**
     * Forge 설정 파일의 로드 시점 문제로 일부 위치에서 설정을 미리 호출하므로, 이미 로드되었는지 캐시해 확인한다
     */
    public static ForgeConfigSpec SERVER_CONFIG_SPEC;

    public static ForgeConfigSpec init() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        SyncConfig.init(builder);
        SERVER_CONFIG_SPEC = builder.build();
        return SERVER_CONFIG_SPEC;
    }
}
