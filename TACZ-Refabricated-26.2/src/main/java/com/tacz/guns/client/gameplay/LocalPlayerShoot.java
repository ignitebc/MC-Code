package com.tacz.guns.client.gameplay;

import cn.sh1rocu.tacz.api.LogicalSide;
import cn.sh1rocu.tacz.mixin.accessor.BlockableEventLoopAccessor;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.animation.statemachine.AnimationStateMachine;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ShootResult;
import com.tacz.guns.api.event.common.GunFireEvent;
import com.tacz.guns.api.event.common.GunShootEvent;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.client.animation.statemachine.GunAnimationConstant;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.index.ClientGunIndex;
import com.tacz.guns.client.sound.SoundPlayManager;
import com.tacz.guns.network.message.ClientMessagePlayerShoot;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import com.tacz.guns.resource.modifier.custom.SilenceModifier;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import com.tacz.guns.resource.pojo.data.gun.ChargeData;
import com.tacz.guns.resource.pojo.data.gun.ChargeType;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.sound.SoundManager;
import it.unimi.dsi.fastutil.Pair;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

public class LocalPlayerShoot {
    private static final Predicate<IGunOperator> SHOOT_LOCKED_CONDITION = operator -> operator.getSynShootCoolDown() > 0;
    private final LocalPlayerDataHolder data;
    private final LocalPlayer player;

    public LocalPlayerShoot(LocalPlayerDataHolder data, LocalPlayer player) {
        this.data = data;
        this.player = player;
    }

    public boolean chargeShoot(boolean isCharging) {
        // 발사 대기 검사에 특별히 만든 방법을 쓰므로, 상태 잠금을 보지 않고 재장전·총기 교체 여부를 직접 확인한다
        IGunOperator gunOperator = IGunOperator.fromLivingEntity(player);
        ItemStack mainHandItem = player.getMainHandItem();
        // 우선 주 손만 쏠 수 있다
        if (!(mainHandItem.getItem() instanceof IGun iGun)) {
            data.chargeProgress = 0f;
            return false;
        }
        Identifier gunId = iGun.getGunId(mainHandItem);
        Optional<ClientGunIndex> gunIndexOptional = TimelessAPI.getClientGunIndex(gunId);
        GunDisplayInstance display = TimelessAPI.getGunDisplay(mainHandItem).orElse(null);
        if (gunIndexOptional.isEmpty() || display == null) {
            return false;
        }
        ClientGunIndex gunIndex = gunIndexOptional.get();
        GunData gunData = gunIndex.getGunData();
        FireMode fireMode = iGun.getFireMode(mainHandItem);

        ChargeData chargeData = gunData.getChargeData(fireMode);
        if (chargeData == null) {
            return isCharging;
        }

        boolean canChargeDuringCooldown = chargeData.isChargeDuringCooldown() || getCoolDown(iGun, mainHandItem, gunData) < 50;
        boolean canCharge = canChargeDuringCooldown && preCheck(iGun, gunOperator, gunIndex, mainHandItem, display, gunData, isCharging) == null;
        float chargeProgress = data.chargeProgress;
        ChargeType type = chargeData.getChargeType();

        if (type == ChargeType.AUTO) {
            if (isCharging && canCharge) {
                data.isCharging = true;
                data.chargeProgress = Math.min(chargeProgress + chargeData.getIncreasePerTick(), chargeData.getMaxCharge());
                return data.chargeProgress >= chargeData.getMaxCharge();
            } else {
                data.isCharging = false;
                data.chargeProgress = Math.max(chargeProgress - chargeData.getDecreasePerTick(), 0f);
            }
        } else if (type == ChargeType.HOLD) {
            if (isCharging && canCharge) {
                data.isCharging = true;
                data.chargeProgress = Math.min(chargeProgress + chargeData.getIncreasePerTick(), chargeData.getMaxCharge());
            } else {
                if (canChargeDuringCooldown && chargeProgress >= chargeData.getFireThreshold()) {
                    return true;
                }
                data.isCharging = false;
                data.chargeProgress = Math.max(chargeProgress - chargeData.getDecreasePerTick(), 0f);
            }
        } else if (type == ChargeType.DELAY) {
            if ((isCharging || chargeProgress > 0) && canCharge) {
                data.isCharging = true;
                data.chargeProgress = Math.min(chargeProgress + chargeData.getIncreasePerTick(), chargeData.getMaxCharge());
                return data.chargeProgress >= chargeData.getMaxCharge();
            } else {
                data.isCharging = false;
                data.chargeProgress = Math.max(chargeProgress - chargeData.getDecreasePerTick(), 0f);
            }
        }
        return false;
    }

    public ShootResult shoot() {
        // 발사 대기 검사에 특별히 만든 방법을 쓰므로, 상태 잠금을 보지 않고 재장전·총기 교체 여부를 직접 확인한다
        IGunOperator gunOperator = IGunOperator.fromLivingEntity(player);
        ItemStack mainHandItem = player.getMainHandItem();
        // 우선 주 손만 쏠 수 있다
        if (!(mainHandItem.getItem() instanceof IGun iGun)) {
            return ShootResult.NOT_GUN;
        }
        Identifier gunId = iGun.getGunId(mainHandItem);
        Optional<ClientGunIndex> gunIndexOptional = TimelessAPI.getClientGunIndex(gunId);
        GunDisplayInstance display = TimelessAPI.getGunDisplay(mainHandItem).orElse(null);
        if (gunIndexOptional.isEmpty() || display == null) {
            return ShootResult.ID_NOT_EXIST;
        }
        ClientGunIndex gunIndex = gunIndexOptional.get();
        GunData gunData = gunIndex.getGunData();
        long coolDown = this.getCoolDown(iGun, mainHandItem, gunData);

        // 직전 비동기 발사 효과가 아직 실행되지 않았으면 바로 돌아가 실행을 기다린다
        if (!data.isShootRecorded) {
            return ShootResult.COOL_DOWN;
        }
        // 상태 잠금이 잠길 준비 중이고 발사용 잠금이 아니면 발사를 막는다(주로 총기 교체 뒤 발사 동작이 교체 동작을 덮어쓰는 것을 막는다)
        if (data.clientStateLock && data.lockedCondition != SHOOT_LOCKED_CONDITION && data.lockedCondition != null) {
            data.isShootRecorded = true;
            // 이 부분의 주목적이 총기 교체 뒤 발사 동작이 교체 동작을 덮어쓰지 않게 하는 것이므로 IS_DRAWING을 돌려준다
            return ShootResult.IS_DRAWING;
        }

        // 사격 대기 시간이 1틱(50ms) 이상이면 발사를 막는다
        if (coolDown >= 50) {
            return ShootResult.COOL_DOWN;
        }

        // 기본 확인
        ShootResult result = preCheck(iGun, gunOperator, gunIndex, mainHandItem, display, gunData, true);
        if (result != null) {
            return result;
        }

        // 달리는 중인지 확인
        if (gunOperator.getSynSprintTime() > 0) {
            return ShootResult.IS_SPRINTING;
        }
        // 발사 이벤트 발생
        var event = new GunShootEvent(player, mainHandItem, LogicalSide.CLIENT);
        GunShootEvent.CALLBACK.invoker().post(event);
        if (event.isCanceled()) {
            return ShootResult.FORGE_EVENT_CANCEL;
        }
        // 상태 잠금을 바꿔 재장전·살펴보기 등을 막는다.
        data.lockState(SHOOT_LOCKED_CONDITION);
        data.isShootRecorded = false;
        // 발사 로직 호출
        float finalChargeProgress = data.chargeProgress;
        this.doShoot(display, iGun, mainHandItem, gunData, coolDown, finalChargeProgress);

        FireMode fireMode = iGun.getFireMode(mainHandItem);
        ChargeData chargeData = gunData.getChargeData(fireMode);
        if (chargeData != null) {
            if (chargeData.getChargeType() == ChargeType.DELAY) {
                data.chargeProgress = 0f;
            } else {
                data.chargeProgress = Math.max(0f, data.chargeProgress - chargeData.getDecreaseOnFire());
            }
        }

        return ShootResult.SUCCESS;
    }

    private @Nullable ShootResult preCheck(IGun iGun, IGunOperator gunOperator, ClientGunIndex gunIndex, ItemStack mainHandItem,
                                           GunDisplayInstance display, GunData gunData, boolean playDrySound) {
        // 버튼 대기 시간이 지나지 않음. 버튼을 누른 뒤 실수로 발사되는 것을 막는다
        // 기본값은 50ms
        if (System.currentTimeMillis() - LocalPlayerDataHolder.clientClickButtonTimestamp < 50) {
            return ShootResult.COOL_DOWN;
        }

        // 재장전 중인지 확인
        if (gunOperator.getSynReloadState().getStateType().isReloading()) {
            return ShootResult.IS_RELOADING;
        }
        // 총기 교체 중인지 확인
        if (gunOperator.getSynDrawCoolDown() != 0) {
            return ShootResult.IS_DRAWING;
        }
        // 노리쇠 당기는 중인지 확인
        if (gunOperator.getSynIsBolting()) {
            return ShootResult.IS_BOLTING;
        }
        // 근접 공격 대기 시간 중인지 판단한다
        if (gunOperator.getSynMeleeCoolDown() != 0) {
            return ShootResult.IS_MELEE;
        }
        // 탄 수 판단
        Bolt boltType = gunIndex.getGunData().getBolt();
        // 인벤토리 급탄인지
        boolean useInventoryAmmo = iGun.useInventoryAmmo(mainHandItem);
        // 약실에 탄이 있는지
        boolean hasAmmoInBarrel = iGun.hasBulletInBarrel(mainHandItem) && boltType != Bolt.OPEN_BOLT;
        // 탄이 남아 있는지(크리에이티브에서 인벤토리 예비 탄약을 소모하는지)
        boolean hasInventoryAmmo = iGun.hasInventoryAmmo(player, mainHandItem, gunOperator.needCheckAmmo()) || hasAmmoInBarrel;
        int ammoCount = iGun.getCurrentAmmoCount(mainHandItem) + (hasAmmoInBarrel ? 1 : 0);
        // 탄이 없다고 볼 조건(인벤토리 급탄이면서 인벤토리에 탄 없음 / 인벤토리 급탄이 아니면서 전체 탄 수 < 1)
        boolean noAmmo = useInventoryAmmo && !hasInventoryAmmo ||
                !useInventoryAmmo && ammoCount < 1;
        if (noAmmo) {
            if (playDrySound) {
                SoundPlayManager.playDryFireSound(player, display);
            }
            return ShootResult.NO_AMMO;
        }
        // 열량 데이터 처리
        if (gunData.hasHeatData()) {
            if (iGun.isOverheatLocked(mainHandItem)) {
                if (playDrySound) {
                    SoundPlayManager.playDryFireSound(player, display);
                }
                return ShootResult.OVERHEATED;
            }
        }
        // 약실 탄 확인
        if (boltType == Bolt.MANUAL_ACTION && !hasAmmoInBarrel) {
            IClientPlayerGunOperator.fromLocalPlayer(player).bolt();
            return ShootResult.NEED_BOLT;
        }
        return null;
    }

    private void doShoot(GunDisplayInstance display, IGun iGun, ItemStack mainHandItem, GunData gunData, long delay, float chargeProgress) {
        FireMode fireMode = iGun.getFireMode(mainHandItem);
        Bolt boltType = gunData.getBolt();
        // 남은 탄 수 얻기
        boolean consumeAmmo = IGunOperator.fromLivingEntity(player).consumesAmmoOrNot();
        boolean hasAmmoInBarrel = iGun.hasBulletInBarrel(mainHandItem) && boltType != Bolt.OPEN_BOLT;
        int ammoCount = consumeAmmo ? iGun.getCurrentAmmoCount(mainHandItem) + (hasAmmoInBarrel ? 1 : 0) : Integer.MAX_VALUE;
        // 연발 사격 간격
        long period = fireMode == FireMode.BURST ? gunData.getBurstShootInterval() : 1;
        // 최대 연발 수
        final int maxCount = Math.min(ammoCount, fireMode == FireMode.BURST ? gunData.getBurstData().getCount() : 1);
        // 연발 카운터
        AtomicInteger count = new AtomicInteger(0);
        // 연발 작업은 스스로를 취소해야 한다. 실행 중인 Thread 는 ScheduledFuture 가 아니므로 형변환하면
        // ClassCastException 으로 작업이 끝난다. 예약 반환값을 보관해 두고 그것을 취소한다.
        // 지연이 0 이면 반환값을 저장하기 전에 첫 실행이 올 수 있어, 중단 요청 플래그를 함께 둔다.
        AtomicReference<ScheduledFuture<?>> shootTask = new AtomicReference<>();
        AtomicBoolean stopRequested = new AtomicBoolean(false);

        ScheduledFuture<?> scheduledTask = LocalPlayerDataHolder.SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(() -> {
            if (stopRequested.get()) {
                return;
            }
            if (count.get() == 0) {
                // isRecord 상태를 바꿔 다음 틱의 발사 검사를 허용한다.
                data.isShootRecorded = true;
            }
            // 열량 데이터 처리
            if (gunData.hasHeatData()) {
                if (iGun.isOverheatLocked(mainHandItem)) {
                    cancelShootTask(shootTask, stopRequested);
                    return;
                }
            }
            // 최대 연발 수에 도달했거나 플레이어가 죽었으면 작업을 취소한다
            if (count.get() >= maxCount || player.isDeadOrDying()) {
                cancelShootTask(shootTask, stopRequested);
                return;
            }

            // 아래 로직은 한 번만 실행하면 된다
            if (count.get() == 0) {
                // 상태 잠금이 잠길 준비 중이고 발사용 잠금이 아니면 발사를 막는다(주로 총기 교체 뒤 발사 동작이 교체 동작을 덮어쓰는 것을 막는다)
                if (data.clientStateLock && data.lockedCondition != SHOOT_LOCKED_CONDITION && data.lockedCondition != null) {
                    return;
                }
                // 새 발사 시각 기록
                data.clientLastShootTimestamp = data.clientShootTimestamp;
                data.clientShootTimestamp = System.currentTimeMillis();
                // 발사 패킷을 보내 서버에 알린다
                // 쏜 순간의 조준을 함께 보낸다. 서버의 회전은 마지막 이동 패킷 값이라 틱 사이에 쏜 탄이 조준점에서 벗어난다.
                long shootTimestamp = data.clientShootTimestamp - data.clientBaseTimestamp;
                ClientPlayNetworking.send(new ClientMessagePlayerShoot(shootTimestamp, chargeProgress,
                        player.getXRot(), player.getYRot()));
            }

            // TODO 확인 필요
            // 소리 재생과 상태 기계 호출은 비동기 스레드에서 메인 스레드로 넘겨 실행해야 한다. 아니면 CME가 난다
            ((BlockableEventLoopAccessor) Minecraft.getInstance()).tacz$submitAsync(() -> {
                // 격발 이벤트 발생
                var event = new GunFireEvent(player, mainHandItem, LogicalSide.CLIENT);
                GunFireEvent.CALLBACK.invoker().post(event);
                boolean fire = !event.isCanceled();
                if (fire) {
                    // 애니메이션과 소리 반복 재생
                    AnimationStateMachine<?> animationStateMachine = display.getAnimationStateMachine();
                    if (animationStateMachine != null) {
                        animationStateMachine.trigger(GunAnimationConstant.INPUT_SHOOT);
                    }
                    // 소음 여부 얻기
                    final boolean useSilenceSound = this.useSilenceSound();
                    // 발사하면 살펴보기를 끊어야 한다
                    SoundPlayManager.stopPlayGunSound(display, SoundManager.INSPECT_SOUND);
                    if (useSilenceSound) {
                        SoundPlayManager.playSilenceSound(player, display, gunData);
                    } else {
                        SoundPlayManager.playShootSound(player, display, gunData);
                    }
                }
            });

            count.getAndIncrement();
        }, delay, period, TimeUnit.MILLISECONDS);
        shootTask.set(scheduledTask);
        // 반환값을 저장하기 전에 작업이 중단을 요청했으면 여기서 취소한다
        if (stopRequested.get()) {
            scheduledTask.cancel(false);
        }
    }

    private static void cancelShootTask(AtomicReference<ScheduledFuture<?>> shootTask, AtomicBoolean stopRequested) {
        stopRequested.set(true);
        ScheduledFuture<?> future = shootTask.get();
        if (future != null) {
            future.cancel(false);
        }
    }

    private boolean useSilenceSound() {
        AttachmentCacheProperty cacheProperty = IGunOperator.fromLivingEntity(player).getCacheProperty();
        if (cacheProperty != null) {
            Pair<Integer, Boolean> silence = cacheProperty.getCache(SilenceModifier.ID);
            return silence.right();
        }
        return false;
    }

    private long getCoolDown(IGun iGun, ItemStack mainHandItem, GunData gunData) {
        FireMode fireMode = iGun.getFireMode(mainHandItem);
        long coolDown;
        if (fireMode == FireMode.BURST) {
            coolDown = (long) (gunData.getBurstData().getMinInterval() * 1000f) - (System.currentTimeMillis() - data.clientShootTimestamp);
        } else {
            coolDown = gunData.getShootInterval(this.player, fireMode, mainHandItem) - (System.currentTimeMillis() - data.clientShootTimestamp);
        }
        return Math.max(coolDown, 0);
    }

    public long getClientShootCoolDown() {
        ItemStack mainHandItem = player.getMainHandItem();
        IGun iGun = IGun.getIGunOrNull(mainHandItem);
        if (iGun == null) {
            return -1;
        }
        Identifier gunId = iGun.getGunId(mainHandItem);
        Optional<CommonGunIndex> gunIndexOptional = TimelessAPI.getCommonGunIndex(gunId);
        return gunIndexOptional.map(commonGunIndex -> getCoolDown(iGun, mainHandItem, commonGunIndex.getGunData())).orElse(-1L);
    }
}
