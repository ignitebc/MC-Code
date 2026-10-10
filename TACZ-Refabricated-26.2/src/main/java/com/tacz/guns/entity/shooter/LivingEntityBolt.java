package com.tacz.guns.entity.shooter;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public class LivingEntityBolt {
    private final ShooterDataHolder data;
    private final LivingEntityDrawGun draw;
    private final LivingEntityShoot shoot;
    private final LivingEntity shooter;

    public LivingEntityBolt(ShooterDataHolder data, LivingEntity shooter, LivingEntityDrawGun draw, LivingEntityShoot shoot) {
        this.data = data;
        this.draw = draw;
        this.shoot = shoot;
        this.shooter = shooter;
    }

    public void bolt() {
        if (data.currentGunItem == null) {
            return;
        }
        ItemStack currentGunItem = data.currentGunItem.get();
        if (!(currentGunItem.getItem() instanceof AbstractGunItem iGun)) {
            return;
        }
        Identifier gunId = iGun.getGunId(currentGunItem);
        TimelessAPI.getCommonGunIndex(gunId).ifPresent(gunIndex -> {
            // 사격 대기 시간 중인지 판단한다
            if (shoot.getShootCoolDown() != 0) {
                return;
            }
            // 재장전 중인지 확인
            if (data.reloadStateType.isReloading()) {
                return;
            }
            // 총을 바꾸는 중인지 확인한다
            if (draw.getDrawCoolDown() != 0) {
                return;
            }
            // 노리쇠를 당기는 중인지 확인한다
            if (data.isBolting) {
                return;
            }
            IGunOperator gunOperator = IGunOperator.fromLivingEntity(shooter);
            // bolt 종류가 수동 장전(manual action)인지 확인한다
            Bolt boltType = gunIndex.getGunData().getBolt();
            // 인벤토리 급탄인지
            boolean useInventoryAmmo = iGun.useInventoryAmmo(currentGunItem);
            // 약실에 탄이 있는지
            boolean hasAmmoInBarrel = iGun.hasBulletInBarrel(currentGunItem) && boltType != Bolt.OPEN_BOLT;
            // 인벤토리에 탄이 남아 있는지(크리에이티브에서 인벤토리 예비 탄약을 소모하는지)
            boolean hasInventoryAmmo = iGun.hasInventoryAmmo(shooter, currentGunItem, gunOperator.needCheckAmmo());
            // 탄이 없다고 볼 조건(인벤토리 급탄이면서 인벤토리에 탄 없음 / 인벤토리 급탄이 아니면서 탄창 탄 수 < 1)
            boolean noAmmo = useInventoryAmmo && !hasInventoryAmmo ||
                    !useInventoryAmmo && iGun.getCurrentAmmoCount(currentGunItem) < 1;
            if (boltType != Bolt.MANUAL_ACTION) {
                return;
            }
            // 약실에 탄약이 있는지 확인한다
            if (hasAmmoInBarrel) {
                return;
            }
            // 탄창에 탄이 있는지 확인한다
            if (noAmmo) {
                return;
            }
            data.boltTimestamp = System.currentTimeMillis();
            data.isBolting = iGun.startBolt(data, currentGunItem, shooter);
        });
    }

    public void tickBolt() {
        // bolt cool down이 -1이면 노리쇠 당기기 로직이 시작되지 않은 것이라 tick이 필요 없다
        if (!data.isBolting) {
            return;
        }
        if (data.currentGunItem == null) {
            data.isBolting = false;
            return;
        }
        ItemStack currentGunItem = data.currentGunItem.get();
        if (!(currentGunItem.getItem() instanceof AbstractGunItem iGun)) {
            data.isBolting = false;
            return;
        }
        Identifier gunId = iGun.getGunId(currentGunItem);
        Optional<CommonGunIndex> gunIndex = TimelessAPI.getCommonGunIndex(gunId);
        data.isBolting = gunIndex.map(index -> iGun.tickBolt(data, currentGunItem, shooter)).orElse(false);
    }
}
