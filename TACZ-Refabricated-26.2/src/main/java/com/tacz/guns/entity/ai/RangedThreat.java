package com.tacz.guns.entity.ai;

import com.tacz.guns.api.item.IGun;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;

/**
 * 공격 대상 플레이어가 원거리 무기를 들었는지 판단한다.
 * <p>
 * 플레이어는 무기를 자주 바꿔 들므로, 원거리 무기를 내려놓은 뒤에도 잠시 위협으로 본다.
 * 그렇지 않으면 몬스터가 엄폐와 돌진을 오가며 깜빡인다.
 */
public final class RangedThreat {
    /** 원거리 무기를 내려놓은 뒤에도 위협으로 보는 시간(틱). */
    private static final int MEMORY_TICKS = 60;

    private int lastTargetId = -1;
    private long lastSeenTick = Long.MIN_VALUE;

    /** 총, 활, 석궁. 총은 주 손에서만 쏠 수 있으므로 보조 손은 활과 석궁만 본다. */
    public static boolean holdsRangedWeapon(LivingEntity entity) {
        ItemStack mainHand = entity.getMainHandItem();
        return mainHand.getItem() instanceof IGun || mainHand.getItem() instanceof ProjectileWeaponItem
                || entity.getOffhandItem().getItem() instanceof ProjectileWeaponItem;
    }

    /** 대상이 지금 원거리 무기를 들었거나 조금 전까지 들고 있었는지 */
    public boolean isThreat(LivingEntity target, long gameTime) {
        if (!(target instanceof Player player) || !player.isAlive() || player.isCreative() || player.isSpectator()) {
            return false;
        }
        if (player.getId() != this.lastTargetId) {
            this.lastTargetId = player.getId();
            this.lastSeenTick = Long.MIN_VALUE;
        }
        if (holdsRangedWeapon(player)) {
            this.lastSeenTick = gameTime;
            return true;
        }
        return this.lastSeenTick != Long.MIN_VALUE && gameTime - this.lastSeenTick <= MEMORY_TICKS;
    }
}
