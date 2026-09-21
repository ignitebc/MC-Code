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

    // 이동 보상 악용 차단
    public static final IConfigEntry<Boolean> movementRequiresNewGround;
    public static final IConfigEntry<Double> movementMaxCreditPerBlock;
    public static final IConfigEntry<Integer> movementRevisitCooldownTicks;
    public static final IConfigEntry<Integer> movementVisitCacheSize;

    static {
        IConfigBuilder config = new ConfigBuilder(Arc.MOD_ID, "arc-common", ConfigExtension.YAML, ConfigType.COMMON);

        config.push("block");
        maxBlockPosCacheSize = config.defineInteger("max_block_pos_cache_size", 1_000)
                .withComments("The block pos cache is limited to at most 1,000 positions");
        config.pop();

        config.push("movement");
        movementRequiresNewGround = config.defineBoolean("requires_new_ground", true)
                .withComments("true 이면 이동 보상이 새 블록 좌표를 밟았을 때만 지급된다. 제자리 왕복 악용을 막는다.");
        movementMaxCreditPerBlock = config.defineDouble("max_credit_per_block", 2.5D, 1.0D, 64.0D)
                .withComments("블록 좌표 하나에 머무는 동안 인정할 최대 이동 거리(블록). 한 칸 위 왕복은 이 값까지만 인정된다.");
        movementRevisitCooldownTicks = config.defineInteger("revisit_cooldown_ticks", 6_000, 0, 1_728_000)
                .withComments("같은 좌표를 다시 밟았을 때 보상을 인정하기까지의 대기 틱. 기본 6000틱(5분).");
        movementVisitCacheSize = config.defineInteger("visit_cache_size", 4_096, 64, 65_536)
                .withComments("플레이어별로 기억할 최근 방문 좌표 수. 넘치면 오래된 것부터 지운다.");
        config.pop();

        config.push("debug");
        isDebug = config.defineBoolean("is_debug", false)
                .withComments("if true, debug mode is enabled");
        config.pop();

        config.build();
    }
}
