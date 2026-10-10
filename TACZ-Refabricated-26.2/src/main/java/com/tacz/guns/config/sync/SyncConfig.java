package com.tacz.guns.config.sync;

import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class SyncConfig {
    public static ForgeConfigSpec.BooleanValue ENABLE_TABLE_FILTER;
    public static ForgeConfigSpec.BooleanValue SERVER_SHOOT_NETWORK_V;
    public static ForgeConfigSpec.BooleanValue SERVER_SHOOT_COOLDOWN_V;

    // 클라이언트 총기 설명 문구에 쓰는 전역 계수 세 개. 동기화가 필요하다
    public static ForgeConfigSpec.DoubleValue DAMAGE_BASE_MULTIPLIER;
    public static ForgeConfigSpec.DoubleValue ARMOR_IGNORE_BASE_MULTIPLIER;
    public static ForgeConfigSpec.DoubleValue HEAD_SHOT_BASE_MULTIPLIER;
    public static ForgeConfigSpec.DoubleValue WEIGHT_SPEED_MULTIPLIER;

    // 클라이언트에서 충돌 상자를 디버그 표시하기 쉽도록 클라이언트로 동기화해야 한다
    public static ForgeConfigSpec.ConfigValue<List<String>> HEAD_SHOT_AABB;
    // 클라이언트가 내려받아야 하는 총기 팩
    public static ForgeConfigSpec.ConfigValue<List<List<String>>> CLIENT_GUN_PACK_DOWNLOAD_URLS;
    // 엎드리기 전술 동작 비활성화
    public static ForgeConfigSpec.BooleanValue ENABLE_CRAWL;

    public static void init(ForgeConfigSpec.Builder builder) {
        baseMultiplier(builder);
        misc(builder);
    }

    private static void baseMultiplier(ForgeConfigSpec.Builder builder) {
        builder.push("base_multiplier");

        builder.comment("All base damage number is multiplied by this factor");
        DAMAGE_BASE_MULTIPLIER = builder.defineInRange("DamageBaseMultiplier", 1, 0, Double.MAX_VALUE);

        builder.comment("All armor ignore damage number is multiplied by this factor");
        ARMOR_IGNORE_BASE_MULTIPLIER = builder.defineInRange("ArmorIgnoreBaseMultiplier", 1, 0, Double.MAX_VALUE);

        builder.comment("All head shot damage number is multiplied by this factor");
        HEAD_SHOT_BASE_MULTIPLIER = builder.defineInRange("HeadShotBaseMultiplier", 1, 0, Double.MAX_VALUE);

        builder.comment("The movement speed will decrease per kg of weight. 0.015 means 1.5% speed decrease per kg. Set a negative value to disable this feature");
        WEIGHT_SPEED_MULTIPLIER = builder.defineInRange("WeightSpeedMultiplier", 0.015, -1, Double.MAX_VALUE);

        builder.pop();
    }

    private static void misc(ForgeConfigSpec.Builder builder) {
        builder.push("misc");

        builder.comment("The entity's head hitbox during the headshot");
        builder.comment("Format: touhou_little_maid:maid [-0.5, 1.0, -0.5, 0.5, 1.5, 0.5]");
        HEAD_SHOT_AABB = builder.define("HeadShotAABB", Lists.newArrayList());

        // 탄약상자 용량은 AmmoBoxItem에 단계별 고정값(200/400/600발)으로 정한다. 예전 AmmoBoxStackSize 항목은 쓰지 않는다.

        builder.comment("Deprecated. Use vanilla server resource pack");
        CLIENT_GUN_PACK_DOWNLOAD_URLS = builder.define("ClientGunPackDownloadUrls", Lists.newArrayList());

        builder.comment("Whether or not players are allowed to use the crawl feature");
        ENABLE_CRAWL = builder.define("EnableCrawl", true);

        builder.comment("Enable the recipe limit of default gunsmith table or not");
        ENABLE_TABLE_FILTER = builder.define("EnableDefaultGunSmithTableFilter", true);

        builder.comment("[Debug Option] Do server-side network check while shooting or not");
        SERVER_SHOOT_NETWORK_V = builder.define("ServerShootNetworkCheck", true);

        builder.comment("[Debug Option] Do server-side shoot cooldown check or not." +
                " WARNING: Close this will disable the shoot cooldown check in server-side at all," +
                " which may lead to potential for cheating." +
                " Only consider to close this when you can't shoot at all sometimes.");
        SERVER_SHOOT_COOLDOWN_V = builder.define("ServerShootCooldownCheck", true);
        builder.pop();
    }
}
