package com.tacz.guns.entity.shooter;

import com.tacz.guns.config.common.GunConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class LivingEntityAmmoCheck {
    private final LivingEntity shooter;

    public LivingEntityAmmoCheck(LivingEntity shooter) {
        this.shooter = shooter;
    }

    /**
     * 재장전할 때 탄약 아이템이 있는지 확인하고 소모할지.
     * 몬스터는 탄약 아이템을 갖지 않으므로 확인하지 않는다. 그래서 재장전하면 탄창이 그냥 가득 찬다.
     */
    public boolean needCheckAmmo() {
        if (shooter instanceof Player player) {
            return !player.isCreative();
        }
        return !MonsterGunController.isMonster(shooter);
    }

    /**
     * 쏠 때 탄창의 탄을 줄일지.
     * 몬스터도 줄인다. 탄창을 다 쓰면 {@link MonsterGunController}가 재장전을 걸고 장전 시간만큼 사격을 멈춘다.
     * 재장전 횟수에는 제한이 없다.
     */
    public boolean consumesAmmoOrNot() {
        if (shooter instanceof Player player) {
            return !player.isCreative() || GunConfig.CREATIVE_PLAYER_CONSUME_AMMO.get();
        }
        return true;
    }
}
