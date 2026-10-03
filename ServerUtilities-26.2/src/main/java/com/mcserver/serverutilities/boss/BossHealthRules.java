package com.mcserver.serverutilities.boss;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.enderdragon.phases.EnderDragonPhase;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.warden.Warden;

/** 신규 생성과 저장된 보스 모두에 같은 기본 체력을 적용한다. */
public final class BossHealthRules {
    public static final double MAX_HEALTH_LIMIT = 2048.0D;
    public static final double WARDEN_HEALTH = 1000.0D;
    public static final double WITHER_HEALTH = 600.0D;
    public static final double DRAGON_HEALTH = 2000.0D;

    private BossHealthRules() { }

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> apply(entity));
    }

    private static void apply(Entity entity) {
        double targetHealth;
        if (entity instanceof Warden) targetHealth = WARDEN_HEALTH;
        else if (entity instanceof WitherBoss) targetHealth = WITHER_HEALTH;
        else if (entity instanceof EnderDragon) targetHealth = DRAGON_HEALTH;
        else return;

        LivingEntity boss = (LivingEntity) entity;
        AttributeInstance maxHealth = boss.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) return;
        boolean dyingDragon = boss instanceof EnderDragon dragon
                && dragon.getPhaseManager().getCurrentPhase().getPhase() == EnderDragonPhase.DYING;
        boss.setHealth(applyHealth(maxHealth, boss.getHealth(), targetHealth, dyingDragon));
    }

    static float applyHealth(AttributeInstance maxHealth, float health, double targetHealth, boolean dyingDragon) {
        // 배율을 누적하지 않고 저장되는 기본값을 고정한다. 재로드 시에는 현재 체력도 유지된다.
        if (maxHealth.getBaseValue() == targetHealth) return health;
        double oldMaximum = maxHealth.getValue();
        maxHealth.setBaseValue(targetHealth);
        if (dyingDragon) return health;
        double fraction = oldMaximum > 0 ? Math.clamp(health / oldMaximum, 0.0D, 1.0D) : 0.0D;
        return (float) (maxHealth.getValue() * fraction);
    }
}
