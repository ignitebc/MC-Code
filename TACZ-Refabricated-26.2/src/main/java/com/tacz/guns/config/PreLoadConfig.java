package com.tacz.guns.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.tacz.guns.GunMod;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import net.neoforged.fml.config.ModConfig;
import net.minecraftforge.common.ForgeConfigSpec;

import java.nio.file.Path;

/**
 * 26.2 재구성판 PreLoadConfig
 * <p>
 * 예전 구현: {@code ModConfig}를 상속한 뒤 {@code ConfigTracker}에서 꼼수로 제거
 * <p>
 * 26.2.x 문제: {@code net.neoforged.fml.config.ModConfig} 생성자가 package-private이라
 * 하위 클래스에서 {@code super(type, spec, modId, fileName)}를 호출할 수 없다
 * <p>
 * 새 구현: nightconfig로 사용자 지정 경로의 설정 파일을 바로 로드하고, {@link ConfigRegistry}로 ForgeConfigSpec을 등록해
 * 설정 이벤트 알림에 쓴다. 더는 ConfigTracker를 꼼수로 건드리지 않는다.
 */
public class PreLoadConfig {
    public static ForgeConfigSpec spec;
    public static ForgeConfigSpec.BooleanValue override;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("gunpack");
        builder.comment("When enabled, the mod will not try to overwrite the default pack under .minecraft/tacz\n" +
                "Since 1.0.4, the overwriting will only run when you start client or a dedicated server");
        override = builder.define("DefaultPackDebug", false);
        builder.pop();
        spec = builder.build();
    }

    public static void init() {
        // ConfigRegistry에 등록(정상적인 ModConfigEvents를 발생시킨다)
        ConfigRegistry.INSTANCE.register(GunMod.MOD_ID, ModConfig.Type.COMMON, spec, "tacz-pre.toml");
    }

    /**
     * 26.2 재구성: ModConfig 상속을 거치지 않고 nightconfig로 바로 로드한다
     *
     * @param configBasePath 설정 파일 기본 경로
     */
    public static void load(Path configBasePath) {
        if (spec.isLoaded()) return;

        Path configFile = configBasePath.resolve("tacz-pre.toml");
        CommentedFileConfig configData = CommentedFileConfig.builder(configFile)
                .autosave()
                .preserveInsertionOrder()
                .build();
        configData.load();

        // spec으로 해석
        spec.acceptConfig(configData);
    }
}
