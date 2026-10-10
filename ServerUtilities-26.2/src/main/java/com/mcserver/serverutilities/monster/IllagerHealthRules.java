package com.mcserver.serverutilities.monster;

import com.mcserver.serverutilities.ServerUtilities;
import com.mcserver.serverutilities.boss.BossHealthRules;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;

import java.util.Map;

/**
 * 총기 도입에 맞춰 일리저 계열(바닐라 약탈자 계열과 illagerinvasion 몹)의 기본 최대 체력을 올린다.
 * <p>
 * 일리저 계열은 좀비·스켈레톤처럼 방어구를 추첨하지 않아 체력이 유일한 방어 수단이다. 총의 초당 피해(기본 총기 중앙값 약 25)가
 * 다이아몬드 검(약 11)의 두 배를 넘어 바닐라 체력으로는 1초 안팎에 쓰러진다. 바닐라 몹은 1.5배, 이미 원본의 2배인
 * illagerinvasion 몹은 1.25배로 올리고, 파괴수와 찬란한 기원자는 따로 정했다. 벡스와 항복한 자는 여럿이 소환되고
 * 벽을 통과하지 못하게 바꿨으므로 그대로 둔다.
 * <p>
 * 보스 체력과 같이 월드에 들어올 때 기본값을 고정하고, 이미 있던 개체는 남은 체력 비율을 유지한다. 몹 ID로 찾으므로
 * illagerinvasion이 없어도 동작하고, 설정을 끄면 다음 로드부터 바닐라·모드가 등록한 기본값으로 돌아간다.
 */
public final class IllagerHealthRules {
    private static final Map<Identifier, Double> TARGET_HEALTH = Map.ofEntries(
            target("minecraft", "pillager", 36.0),
            target("minecraft", "vindicator", 36.0),
            target("minecraft", "evoker", 36.0),
            target("minecraft", "illusioner", 48.0),
            target("minecraft", "witch", 40.0),
            target("minecraft", "ravager", 200.0),
            target("illagerinvasion", "alchemist", 60.0),
            target("illagerinvasion", "archivist", 60.0),
            target("illagerinvasion", "marauder", 60.0),
            target("illagerinvasion", "provoker", 60.0),
            target("illagerinvasion", "basher", 80.0),
            target("illagerinvasion", "firecaller", 80.0),
            target("illagerinvasion", "sorcerer", 80.0),
            target("illagerinvasion", "necromancer", 80.0),
            target("illagerinvasion", "inquisitor", 200.0),
            target("illagerinvasion", "invoker", 800.0));

    private IllagerHealthRules() { }

    public static void register() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, level) -> apply(entity));
    }

    private static void apply(Entity entity) {
        if (!(entity instanceof LivingEntity living)) return;
        Double raisedHealth = TARGET_HEALTH.get(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()));
        if (raisedHealth == null) return;
        AttributeInstance maxHealth = living.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) return;
        double targetHealth = ServerUtilities.config().illagerHealth() ? raisedHealth : registeredHealth(living);
        living.setHealth(BossHealthRules.applyHealth(maxHealth, living.getHealth(), targetHealth, false));
    }

    /** 바닐라·모드가 등록한 기본 최대 체력. 설정을 끄면 이 값으로 되돌린다. */
    private static double registeredHealth(LivingEntity living) {
        @SuppressWarnings("unchecked")
        EntityType<? extends LivingEntity> type = (EntityType<? extends LivingEntity>) living.getType();
        return DefaultAttributes.getSupplier(type).getBaseValue(Attributes.MAX_HEALTH);
    }

    private static Map.Entry<Identifier, Double> target(String namespace, String path, double health) {
        return Map.entry(Identifier.fromNamespaceAndPath(namespace, path), health);
    }
}
