package com.tacz.guns.entity.shooter;

import cn.sh1rocu.tacz.api.LogicalSide;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ReloadState;
import com.tacz.guns.api.event.common.GunReloadEvent;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.network.NetworkHandler;
import com.tacz.guns.network.message.event.ServerMessageGunReload;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class LivingEntityReload {
    private final LivingEntity shooter;
    private final ShooterDataHolder data;
    private final LivingEntityDrawGun draw;
    private final LivingEntityShoot shoot;

    public LivingEntityReload(LivingEntity shooter, ShooterDataHolder data, LivingEntityDrawGun draw, LivingEntityShoot shoot) {
        this.shooter = shooter;
        this.data = data;
        this.draw = draw;
        this.shoot = shoot;
    }

    public void reload() {
        if (data.currentGunItem == null) {
            return;
        }
        ItemStack currentGunItem = data.currentGunItem.get();
        if (!(currentGunItem.getItem() instanceof AbstractGunItem gunItem)) {
            return;
        }
        Identifier gunId = gunItem.getGunId(currentGunItem);
        TimelessAPI.getCommonGunIndex(gunId).ifPresent(gunIndex -> {
            // 인벤토리 급탄인지 확인한다
            if (gunItem.useInventoryAmmo(currentGunItem)) {
                return;
            }
            // 재장전이 아직 끝나지 않았는지 확인한다
            if (data.reloadStateType.isReloading()) {
                return;
            }
            // 사격 대기 시간 중인지 확인한다
            if (shoot.getShootCoolDown() != 0) {
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
            // 탄약 확인
            if (IGunOperator.fromLivingEntity(shooter).needCheckAmmo() && !gunItem.canReload(shooter, currentGunItem)) {
                return;
            }
            // 장전 이벤트 발생
            GunReloadEvent gunReloadEvent = new GunReloadEvent(shooter, currentGunItem, LogicalSide.SERVER);
            GunReloadEvent.CALLBACK.invoker().post(gunReloadEvent);
            if (gunReloadEvent.isCanceled()) {
                return;
            }
            NetworkHandler.sendToTrackingEntity(new ServerMessageGunReload(shooter.getId(), currentGunItem), shooter);
            Bolt boltType = gunIndex.getGunData().getBolt();
            int ammoCount = gunItem.getCurrentAmmoCount(currentGunItem) + (gunItem.hasBulletInBarrel(currentGunItem) && boltType != Bolt.OPEN_BOLT ? 1 : 0);
            if (ammoCount <= 0) {
                // 빈 탄창 재장전 tick 상태 초기화
                data.reloadStateType = ReloadState.StateType.EMPTY_RELOAD_FEEDING;
            } else {
                // 전술 재장전 tick 상태 초기화
                data.reloadStateType = ReloadState.StateType.TACTICAL_RELOAD_FEEDING;
            }
            data.reloadTimestamp = System.currentTimeMillis();
            // 총기 로직 호출
            if (!gunItem.startReload(data, currentGunItem, shooter)) {
                data.reloadStateType = ReloadState.StateType.NOT_RELOADING;
                data.reloadTimestamp = -1;
            }
        });
    }

    public void cancelReload() {
        if (data.currentGunItem == null) {
            return;
        }
        ItemStack currentGunItem = data.currentGunItem.get();
        if (!(currentGunItem.getItem() instanceof AbstractGunItem gunItem)) {
            return;
        }
        // 재장전 중인지 확인한다
        if (!data.reloadStateType.isReloading()) {
            return;
        }
        gunItem.interruptReload(data, currentGunItem, shooter);
    }

    public ReloadState tickReloadState() {
        ReloadState result = new ReloadState();
        // 재장전 중이 아니면 바로 돌아간다
        if (data.reloadTimestamp == -1) {
            return result;
        }
        // 총기 로직 호출
        if (data.currentGunItem != null) {
            ItemStack currentGunItem = data.currentGunItem.get();
            if (currentGunItem != null && currentGunItem.getItem() instanceof AbstractGunItem abstractGunItem) {
                result = abstractGunItem.tickReload(data, currentGunItem, shooter);
            }
        }
        // tick 결과를 data holder에 저장한다
        data.reloadStateType = result.getStateType();
        if (!result.getStateType().isReloading()) {
            data.reloadTimestamp = -1;
        }
        return result;
    }
}
