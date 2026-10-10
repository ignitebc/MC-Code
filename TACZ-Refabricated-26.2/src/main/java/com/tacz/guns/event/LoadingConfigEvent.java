package com.tacz.guns.event;

import com.tacz.guns.config.util.HeadShotAABBConfigRead;
import net.neoforged.fml.config.ModConfig;

public class LoadingConfigEvent {
    private static final String CONFIG_NAME = "tacz-server.toml";

    /**
     * 클라이언트와 서버가 시작할 때 이 이벤트가 발생한다
     */
    public static void onLoadingConfig(ModConfig config) {
        String fileName = config.getFileName();
        if (CONFIG_NAME.equals(fileName)) {
            HeadShotAABBConfigRead.init();
        }
    }

    /**
     * 플레이어가 서버에 들어오거나 서버가 설정을 자동으로 초기화할 때 이 메서드가 호출된다
     */
    public static void onReloadingConfig(ModConfig config) {
        String fileName = config.getFileName();
        if (CONFIG_NAME.equals(fileName)) {
            HeadShotAABBConfigRead.init();
        }
    }
}
