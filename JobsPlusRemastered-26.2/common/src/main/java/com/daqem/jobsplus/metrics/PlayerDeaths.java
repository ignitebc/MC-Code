package com.daqem.jobsplus.metrics;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/**
 * 플레이어 사망을 events.csv의 DEATH 행으로 만든다.
 * <p>
 * 실측 직업 효율이 시뮬레이션보다 낮을 때 전투 난이도 때문인지 가리려고 사망 원인과 가해 몹의 무장 상태를 남긴다.
 * 사망이 어느 직업 작업 중에 일어났는지는 분석 스크립트가 activity.csv의 시간 배분으로 붙인다.
 */
final class PlayerDeaths
{
    /** TACZ 총기 아이템이 총기 ID를 담는 CUSTOM_DATA 키 */
    private static final String GUN_ID_TAG = "GunId";

    private PlayerDeaths()
    {
    }

    /**
     * target_id는 피해 종류, value는 처치 기여 생물 종류다. 가해 생물이 있으면 주 손 무기·총기 ID·흉갑을 detail에 붙인다.
     */
    static MetricsEvent event(ServerPlayer player, DamageSource source)
    {
        BlockPos position = player.blockPosition();
        Entity attacker = source.getEntity();
        MetricsEvent event = MetricsEvent.of("DEATH")
                .player(player)
                .target(damageTypeId(source))
                .value(entityTypeId(attacker))
                .detail("direct", entityTypeId(source.getDirectEntity()))
                .detail("dimension", player.level().dimension().identifier())
                .detail("x", position.getX())
                .detail("y", position.getY())
                .detail("z", position.getZ());
        if (attacker instanceof LivingEntity livingAttacker)
        {
            ItemStack weapon = livingAttacker.getMainHandItem();
            event.detail("attacker_weapon", itemId(weapon))
                    .detail("attacker_gun", gunId(weapon))
                    .detail("attacker_armor", itemId(livingAttacker.getItemBySlot(EquipmentSlot.CHEST)));
        }
        return event;
    }

    private static String damageTypeId(DamageSource source)
    {
        return source.typeHolder().unwrapKey().map(key -> key.identifier().toString()).orElse("");
    }

    private static String entityTypeId(Entity entity)
    {
        if (entity == null)
        {
            return "";
        }
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
    }

    private static String itemId(ItemStack stack)
    {
        if (stack.isEmpty())
        {
            return "";
        }
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    /** TACZ 총기면 총기 ID(예: tacz:ak47), 아니면 빈 값 */
    private static String gunId(ItemStack stack)
    {
        CustomData data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return data.copyTag().getStringOr(GUN_ID_TAG, "");
    }
}
