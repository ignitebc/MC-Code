package com.tacz.guns.entity.shooter;

import cn.sh1rocu.tacz.api.LogicalSide;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ShootResult;
import com.tacz.guns.api.event.common.GunShootEvent;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.config.sync.SyncConfig;
import com.tacz.guns.network.NetworkHandler;
import com.tacz.guns.network.message.ServerMessageSyncBaseTimestamp;
import com.tacz.guns.network.message.event.ServerMessageGunShoot;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import com.tacz.guns.resource.pojo.data.gun.ChargeData;
import com.tacz.guns.resource.pojo.data.gun.ChargeType;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.function.Supplier;

public class LivingEntityShoot {
    private final LivingEntity shooter;
    private final ShooterDataHolder data;
    private final LivingEntityDrawGun draw;

    public LivingEntityShoot(LivingEntity shooter, ShooterDataHolder data, LivingEntityDrawGun draw) {
        this.shooter = shooter;
        this.data = data;
        this.draw = draw;
    }

    public ShootResult shoot(Supplier<Float> pitch, Supplier<Float> yaw, long timestamp) {
        return shoot(pitch, yaw, timestamp, 0f, false);
    }

    public ShootResult shoot(Supplier<Float> pitch, Supplier<Float> yaw, long timestamp, float chargeProgress) {
        return shoot(pitch, yaw, timestamp, chargeProgress, true);
    }

    private ShootResult shoot(Supplier<Float> pitch, Supplier<Float> yaw, long timestamp, float chargeProgress, boolean hasChargeContext) {
        if (data.currentGunItem == null) {
            return ShootResult.NOT_DRAW;
        }
        ItemStack currentGunItem = data.currentGunItem.get();
        if (!(currentGunItem.getItem() instanceof IGun iGun)) {
            return ShootResult.NOT_GUN;
        }
        Identifier gunId = iGun.getGunId(currentGunItem);
        Optional<CommonGunIndex> gunIndexOptional = TimelessAPI.getCommonGunIndex(gunId);
        if (gunIndexOptional.isEmpty()) {
            return ShootResult.ID_NOT_EXIST;
        }
        CommonGunIndex gunIndex = gunIndexOptional.get();
        if (SyncConfig.SERVER_SHOOT_COOLDOWN_V.get()) {
            // 사격 대기 시간 중인지 판단한다
            long coolDown = getShootCoolDown(timestamp);
            if (coolDown == -1) {
                // 보통은 -1이 될 일이 거의 없는데, 원인은 알 수 없다
                return ShootResult.UNKNOWN_FAIL;
            }
            if (coolDown > 0) {
                return ShootResult.COOL_DOWN;
            }
        }
        if (SyncConfig.SERVER_SHOOT_NETWORK_V.get()) {
            // tick time과 허용하는 네트워크 지연 변동으로 타임스탬프 수용 범위를 계산한다
            MinecraftServer server = shooter.level().getServer();
            if (server == null) {
                return ShootResult.NETWORK_FAIL;
            }
            // 26.2: MinecraftServer.tickTimes가 제거되어 고정 50ms tick time을 쓴다
            double tickTime = 50;
            long alpha = System.currentTimeMillis() - data.baseTimestamp - timestamp;
            if (alpha < -300 || alpha > 300 + tickTime * 2) { // +-300ms 네트워크 변동을 허용하고, 범위 하한을 tick time 2개만큼 더 넓힌다(최악의 경우 사격이 2 tick 늦어진다)
                if (shooter instanceof ServerPlayer player) {
                    NetworkHandler.sendToClientPlayer(new ServerMessageSyncBaseTimestamp(), player);
                }
                return ShootResult.NETWORK_FAIL;
            }
        }
        // 재장전 중인지 확인
        if (data.reloadStateType.isReloading()) {
            return ShootResult.IS_RELOADING;
        }
        // 총을 바꾸는 중인지 확인한다
        if (draw.getDrawCoolDown() != 0) {
            return ShootResult.IS_DRAWING;
        }
        // 노리쇠를 당기는 중인지 확인한다
        if (data.isBolting) {
            return ShootResult.IS_BOLTING;
        }
        // 달리는 중인지 확인한다
        if (data.sprintTimeS > 0) {
            return ShootResult.IS_SPRINTING;
        }
        ChargeData chargeData = gunIndex.getGunData().getChargeData(iGun.getFireMode(currentGunItem));
        if (hasChargeContext && !isChargeProgressReasonable(chargeData, chargeProgress)) {
            return ShootResult.UNKNOWN_FAIL;
        }
        IGunOperator gunOperator = IGunOperator.fromLivingEntity(shooter);
        // 탄 수 판단
        Bolt boltType = gunIndex.getGunData().getBolt();
        // 인벤토리 급탄인지
        boolean useInventoryAmmo = iGun.useInventoryAmmo(currentGunItem);
        // 약실에 탄이 있는지
        boolean hasAmmoInBarrel = iGun.hasBulletInBarrel(currentGunItem) && boltType != Bolt.OPEN_BOLT;
        // 탄이 남아 있는지(크리에이티브에서 인벤토리 예비 탄약을 소모하는지)
        boolean hasInventoryAmmo = iGun.hasInventoryAmmo(shooter, currentGunItem, gunOperator.needCheckAmmo()) || hasAmmoInBarrel;
        int ammoCount = iGun.getCurrentAmmoCount(currentGunItem) + (hasAmmoInBarrel ? 1 : 0);
        // 탄이 없다고 볼 조건(인벤토리 급탄이면서 인벤토리에 탄 없음 / 인벤토리 급탄이 아니면서 전체 탄 수 < 1)
        boolean noAmmo = useInventoryAmmo && !hasInventoryAmmo ||
                !useInventoryAmmo && ammoCount < 1;
        if (noAmmo) {
            return ShootResult.NO_AMMO;
        }
        // 열량 데이터 처리
        if (gunIndex.getGunData().hasHeatData()) {
            if (iGun.isOverheatLocked(currentGunItem)) {
                return ShootResult.OVERHEATED;
            }
        }
        // 약실 탄 확인
        if (boltType == Bolt.MANUAL_ACTION && !hasAmmoInBarrel) {
            return ShootResult.NEED_BOLT;
        }
        // 폐쇄 노리쇠 약실 확인 로직
        if (boltType == Bolt.CLOSED_BOLT && !hasAmmoInBarrel) {
            // 서로 다른 두 가지 장전 상황
            if (useInventoryAmmo) {
                consumeAmmoFromPlayer(1, currentGunItem, gunOperator.needCheckAmmo());
            } else {
                iGun.reduceCurrentAmmoCount(currentGunItem);
            }
            iGun.setBulletInBarrel(currentGunItem, true);
        }
        // 사격 이벤트 발생
        GunShootEvent gunShootEvent = new GunShootEvent(shooter, currentGunItem, LogicalSide.SERVER);
        GunShootEvent.CALLBACK.invoker().post(gunShootEvent);
        if (gunShootEvent.isCanceled()) {
            return ShootResult.FORGE_EVENT_CANCEL;
        }

        NetworkHandler.sendToTrackingEntity(new ServerMessageGunShoot(shooter.getId(), currentGunItem), shooter);
        data.lastShootTimestamp = data.shootTimestamp;
        data.heatTimestamp = System.currentTimeMillis();
        data.shootTimestamp = timestamp;
        data.chargeProgress = validateChargeProgress(chargeData, chargeProgress, hasChargeContext);
        // 총기 사격 로직 실행
        if (iGun instanceof AbstractGunItem logicGun) {
            logicGun.shoot(data, currentGunItem, pitch, yaw, shooter);
        }
        MuzzleFlashBroadcaster.broadcast(shooter);
        return ShootResult.SUCCESS;
    }

    // 간단한 검증. 서버는 방아쇠를 누르고 있는 상태를 추적하지 않으므로 "클라이언트가 계속 눌러 충전"할 때 이론상 닿을 수 있는 최대 진행도를 넘는 것만 거부한다.
    private boolean isChargeProgressReasonable(ChargeData chargeData, float chargeProgress) {
        final float tolerance = 0.001f;
        if (!Float.isFinite(chargeProgress)) {
            return false;
        }
        if (chargeData == null) {
            return Math.abs(chargeProgress) <= tolerance;
        }
        if (chargeProgress < -tolerance) {
            return false;
        }
        float minimumProgress = Math.min(chargeData.getFireThreshold(), chargeData.getMaxCharge());
        if (chargeProgress + tolerance < minimumProgress) {
            return false;
        }
        if (chargeProgress > getMaxReasonableChargeProgress(chargeData) + tolerance) {
            return false;
        }
        return true;
    }

    private float getMaxReasonableChargeProgress(ChargeData chargeData) {
        // 네트워크 흔들림과 클라이언트/서버 스케줄 차이를 견디도록 tick 여유를 조금 남긴다.
        final float extraTicks = 4f;
        float startProgress = getChargeProgressAfterLastFire(chargeData);
        float elapsedTicks = Math.max(getChargeElapsedMillis() / 50f, 0f) + extraTicks;
        float maxProgress = startProgress + elapsedTicks * Math.max(chargeData.getIncreasePerTick(), 0f);
        return Math.min(maxProgress, chargeData.getMaxCharge());
    }

    private float getChargeProgressAfterLastFire(ChargeData chargeData) {
        if (data.shootTimestamp < 0) {
            return 0f;
        }
        // delay 충전 방식은 클라이언트가 발사한 뒤 항상 초기화된다.
        if (chargeData.getChargeType() == ChargeType.DELAY) {
            return 0f;
        }
        return Math.max(0f, data.chargeProgress - chargeData.getDecreaseOnFire());
    }

    private long getChargeElapsedMillis() {
        if (data.shootTimestamp >= 0) {
            long startTimestamp = data.baseTimestamp + data.shootTimestamp;
            return System.currentTimeMillis() - startTimestamp;
        }
        if (data.drawTimestamp >= 0) {
            return System.currentTimeMillis() - data.drawTimestamp;
        }
        return 0L;
    }

    private float validateChargeProgress(ChargeData chargeData, float chargeProgress, boolean hasChargeContext) {
        if (!hasChargeContext || !Float.isFinite(chargeProgress)) {
            return 0f;
        }
        if (chargeData == null) {
            return 0f;
        }
        return Math.max(0f, Math.min(chargeProgress, chargeData.getMaxCharge()));
    }

    /**
     * 현재 타임스탬프로 사격 대기 시간을 조회한다. 돌려주는 값은 보통 총기의 사격 간격을 넘지 않는다
     *
     * @return 사격 대기 시간
     */
    public long getShootCoolDown() {
        return getShootCoolDown(System.currentTimeMillis() - data.baseTimestamp);
    }

    /**
     * 지정한 timestamp 기준 사격 대기 시간을 조회한다. 상황에 따라 총기의 사격 간격을 넘을 수 있다.
     *
     * @param timestamp 지정한 timestamp. 오프셋 타임스탬프다(base timestamp 기준 상대 타임스탬프)
     * @return 사격 대기 시간
     */
    public long getShootCoolDown(long timestamp) {
        if (data.currentGunItem == null) {
            return 0;
        }
        ItemStack currentGunItem = data.currentGunItem.get();
        if (!(currentGunItem.getItem() instanceof IGun iGun)) {
            return 0;
        }
        Identifier gunId = iGun.getGunId(currentGunItem);
        Optional<CommonGunIndex> gunIndex = TimelessAPI.getCommonGunIndex(gunId);
        FireMode fireMode = iGun.getFireMode(currentGunItem);
        long interval = timestamp - data.shootTimestamp;
        if (fireMode == FireMode.BURST) {
            return gunIndex.map(index -> {
                long coolDown = (long) (index.getGunData().getBurstData().getMinInterval() * 1000f) - interval;
                // 지연을 상쇄하려고 5 ms 여유 시간을 준다
                coolDown = coolDown - 5;
                return Math.max(coolDown, 0L);
            }).orElse(-1L);
        }
        return gunIndex.map(index -> {
            long coolDown = index.getGunData().getShootInterval(this.shooter, fireMode, currentGunItem) - interval;
            // 지연을 상쇄하려고 5 ms 여유 시간을 준다
            coolDown = coolDown - 5;
            return Math.max(coolDown, 0L);
        }).orElse(-1L);
    }

    /**
     * 예비 탄약을 소모한다. TODO: 인벤토리 안 탄약을 더 간단히 소모하는 방법이 있는지 확인해야 한다(이 부분은 논리 기계 API에서 그대로 복사해 왔다)
     */
    public void consumeAmmoFromPlayer(int neededAmount, ItemStack itemStack, boolean needCheckAmmo) {
        if (!(itemStack.getItem() instanceof AbstractGunItem abstractGunItem)) {
            return;
        }
        // 크리에이티브 모드라 소모하지 않는 경우
        if (!needCheckAmmo) {
            return;
        }
        if (abstractGunItem.useDummyAmmo(itemStack)) {
            abstractGunItem.findAndExtractDummyAmmo(itemStack, neededAmount);
        } else {
            shooter.tacz$getItemHandler(null)
                    .map(cap -> abstractGunItem.findAndExtractInventoryAmmo(cap, itemStack, neededAmount));
        }
    }
}
