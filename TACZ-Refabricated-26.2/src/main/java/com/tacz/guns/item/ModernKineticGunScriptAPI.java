package com.tacz.guns.item;

import cn.sh1rocu.tacz.api.LogicalSide;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.GunProperties;
import com.tacz.guns.api.GunProperty;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.event.common.GunFireEvent;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.api.util.LuaEntityAccessor;
import com.tacz.guns.api.util.LuaNbtAccessor;
import com.tacz.guns.client.animation.statemachine.GunAnimationStateContext;
import com.tacz.guns.config.common.AmmoConfig;
import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.entity.shooter.MonsterGunAmmo;
import com.tacz.guns.entity.ai.GunfireAlert;
import com.tacz.guns.entity.shooter.MonsterGunController;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import com.tacz.guns.network.NetworkHandler;
import com.tacz.guns.network.message.event.ServerMessageGunFire;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import com.tacz.guns.resource.modifier.custom.SilenceModifier;
import com.tacz.guns.resource.pojo.data.gun.*;
import com.tacz.guns.sound.SoundManager;
import com.tacz.guns.util.AttachmentDataUtils;
import com.tacz.guns.util.CycleTaskHelper;
import com.tacz.guns.util.GunLevelManager;
import com.tacz.guns.util.GunShotContext;
import com.tacz.guns.util.ItemNbtUtils;
import com.tacz.guns.util.ShooterMagazineBonus;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class ModernKineticGunScriptAPI {
    public static String MARKER = "ScriptAPI";
    /**
     * 스크립트 상태를 모아 두는 총기 커스텀 데이터 하위 태그 이름
     */
    private static final String SCRIPT_STATE_TAG = "ScriptState";
    /**
     * 소음기 없는 총성을 주변 플레이어에게 보내는 거리 배율. 설정 거리(기본 64)의 2배인 128블록까지 들린다.
     * <p>
     * 소음기 보정은 설정 거리에서 블록 수를 빼는 방식이라 설정값 자체를 올리면 소음기 효과가 약해진다.
     * 그래서 설정은 그대로 두고 소음기가 없는 사격에만 배율을 곱한다.
     */
    private static final int UNSUPPRESSED_SHOT_DISTANCE_MULTIPLIER = 2;
    /** 소음기 없는 총성의 기본 음량. 마인크래프트가 1을 넘는 음량을 잘라 내므로 최대값이다. */
    private static final float UNSUPPRESSED_SHOT_VOLUME = 1.0f;
    /** 소음기 총성의 기본 음량. 소음기 효과를 유지하도록 기존 값을 그대로 쓴다. */
    private static final float SUPPRESSED_SHOT_VOLUME = 0.8f;

    private LivingEntity shooter;

    private ShooterDataHolder dataHolder;

    private ItemStack itemStack;

    private AbstractGunItem abstractGunItem;

    private CommonGunIndex gunIndex;

    private Identifier gunId;

    private Identifier gunDisplayId;

    private Supplier<Float> pitchSupplier;

    private Supplier<Float> yawSupplier;

    private LuaNbtAccessor nbtUtil;

    private LuaEntityAccessor entityAccessor;

    private float shotDamageMultiplier = 1f;

    private float projectileSpeedMultiplier = 1f;

    /**
     * 플레이어 속성 캐시에 저장된 값을 가져온다.
     * 복잡한 데이터 구조를 가진 속성 값은 가져오지 않는다. 그 동작은 정의되지 않았다.
     *
     * @param id 속성 id. {@link GunProperties} 참고
     * @return 속성 값
     * @author ChloePrime
     * @see com.tacz.guns.api.CacheModifiableByScript
     * @since 1.1.7
     */
    public LuaValue getCachedProperty(String id) {
        GunProperty<?> property = GunProperties.all().get(id);
        if (property == null) {
            throw new LuaError("unknown gun property: " + id);
        }
        AttachmentCacheProperty cache = IGunOperator.fromLivingEntity(shooter).getCacheProperty();
        if (cache == null) {
            return LuaValue.NIL;
        }
        return CoerceJavaToLua.coerce(cache.getCache(id));
    }

    /**
     * 완전한 사격 로직을 한 번 실행한다. 플레이어 상태(조준 중, 이동 중, 포복 중 등), 부착물 수치 영향, 다중 산탄 퍼짐, 점사를 고려하고 발사 효과음을 재생한다.
     *
     * @param consumeAmmo 이번 사격에서 탄약을 소모할지 여부
     */
    public void shootOnce(boolean consumeAmmo) {
        GunData gunData = gunIndex.getGunData();
        BulletData bulletData = gunIndex.getBulletData();
        IGunOperator gunOperator = IGunOperator.fromLivingEntity(shooter);
        final float shotDamageMultiplier = this.shotDamageMultiplier;
        final float projectileSpeedMultiplier = this.projectileSpeedMultiplier;

        // 부착물 데이터 캐시 가져오기
        AttachmentCacheProperty cacheProperty = gunOperator.getCacheProperty();
        if (cacheProperty == null) {
            return;
        }

        // 열량 데이터 처리
        float heatInaccuracy = 1f;
        if (hasHeatData()) {
            GunHeatData heatData = Objects.requireNonNull(gunIndex.getGunData().getHeatData());
            float heatMax = modifyProperty(GunProperties.RuntimeOnly.MAX_HEAT, Float.class, heatData.getHeatMax());
            float heatPercentage = (getHeatAmount() / heatMax);
            heatInaccuracy *= Mth.lerp(heatPercentage, heatData.getMinInaccuracy(), heatData.getMaxInaccuracy());
        }

        // 탄 퍼짐 영향
        InaccuracyType inaccuracyType = InaccuracyType.getInaccuracyType(shooter);
        final float unmodifiedInaccuracy = cacheProperty.getCache(GunProperties.INACCURACY).get(inaccuracyType) * heatInaccuracy;
        final float inaccuracy = Math.max(0, modifyProperty(GunProperties.INACCURACY, Float.class, unmodifiedInaccuracy));

        // 소음기 영향
        // 소음 옵션은 사수 입장에서 클라이언트에서 처리하므로 스크립트가 바꿔도 소용이 없어 아예 바꾸지 못하게 했다
        Pair<Integer, Boolean> silence = cacheProperty.getCache(SilenceModifier.ID);
        final int soundDistance = modifyProperty(GunProperties.RuntimeOnly.SOUND_DISTANCE, Integer.class, silence.left());
        final boolean useSilenceSound = silence.right();
        // 멀리 있는 몹·플레이어의 총성도 위치를 짐작할 수 있도록 소음기 없는 사격만 거리와 음량을 키운다.
        final int shotSoundDistance = useSilenceSound ? soundDistance : soundDistance * UNSUPPRESSED_SHOT_DISTANCE_MULTIPLIER;
        final float shotSoundVolume = useSilenceSound ? SUPPRESSED_SHOT_VOLUME : UNSUPPRESSED_SHOT_VOLUME;

        // 탄환 비행 속도
        float speed = modifyProperty(GunProperties.AMMO_SPEED, Float.class, cacheProperty.getCache(GunProperties.AMMO_SPEED));
        speed *= AmmoConfig.GLOBAL_BULLET_SPEED_MODIFIER.get();
        float processedSpeed = Mth.clamp((speed / 20) * projectileSpeedMultiplier, 0, Float.MAX_VALUE);
        // 산탄 수
        int bulletAmount = modifyProperty(GunProperties.RuntimeOnly.BULLET_AMOUNT, Integer.class, Math.max(bulletData.getBulletAmount(), 1));

        // 점사 수
        FireMode fireMode = abstractGunItem.getFireMode(itemStack);
        int cycles = modifyProperty(GunProperties.RuntimeOnly.BURST_COUNT, Integer.class, fireMode == FireMode.BURST ? gunData.getBurstData().getCount() : 1);
        // 점사 간격
        long period = modifyProperty(GunProperties.RuntimeOnly.BURST_SHOOT_INTERVAL, Long.class, fireMode == FireMode.BURST ? gunData.getBurstShootInterval() : 1);

        CycleTaskHelper.addCycleTask(() -> {
            // 사수가 죽으면 사격을 취소한다
            if (shooter.isDeadOrDying()) {
                return false;
            }
            // 무기가 바뀌면 사격을 취소한다
            if (!shooter.getMainHandItem().equals(itemStack) || shooter.getMainHandItem().isEmpty()) {
                return false;
            }
            // 격발 이벤트 발생
            GunFireEvent gunFireEvent = new GunFireEvent(shooter, itemStack, LogicalSide.SERVER);
            GunFireEvent.CALLBACK.invoker().post(gunFireEvent);
            boolean fire = !gunFireEvent.isCanceled();
            if (fire) {
                boolean monster = MonsterGunController.isMonster(shooter);
                if (monster && !MonsterGunAmmo.hasAmmo(itemStack)) {
                    return false;
                }
                NetworkHandler.sendToTrackingEntity(new ServerMessageGunFire(shooter.getId(), itemStack), shooter);
                // 탄약 차감
                if (consumeAmmo) {
                    if (!this.reduceAmmoOnce()) {
                        return false;
                    }
                }
                // SUCCESS는 점사 전체의 요청 결과다. 실제 탄약마다 제한해야 점사로 우회할 수 없다.
                if (monster && !MonsterGunAmmo.consumeShot(itemStack)) {
                    return false;
                }
                // 열량 데이터 처리
                if (gunIndex.getGunData().hasHeatData()) {
                    Optional.ofNullable(gunIndex.getScript())
                            .map(script -> checkFunction(script.get("handle_shoot_heat")))
                            .ifPresentOrElse(
                                    func -> func.call(CoerceJavaToLua.coerce(this)),
                                    this::handleShootHeat
                            );
                }
                // 사격 방향 가져오기(pitch와 yaw)
                float pitch = pitchSupplier != null ? pitchSupplier.get() : shooter.getXRot();
                float yaw = yawSupplier != null ? yawSupplier.get() : shooter.getYRot();
                // 탄환 생성
                Level world = shooter.level();
                Identifier ammoId = gunData.getAmmoId();
                boolean experienceAllowed = consumeAmmo;
                if (gunData.getReloadData().isInfinite() || abstractGunItem.useDummyAmmo(itemStack)) {
                    experienceAllowed = false;
                }
                // 점사에서는 매 탄약마다 새 정보를 만들고, 한 발의 산탄은 같은 정보를 공유한다.
                GunShotContext shotContext = GunLevelManager.createShotContext(itemStack, shooter, experienceAllowed);
                for (int i = 0; i < bulletAmount; i++) {
                    boolean isTracer = bulletData.hasTracerAmmo() && gunOperator.nextBulletIsTracer(bulletData.getTracerCountInterval());
                    EntityKineticBullet bullet = new EntityKineticBullet(world, shooter, itemStack, ammoId, gunId,
                            gunDisplayId, isTracer, gunData, bulletData);
                    bullet.applyShotgunDamageSpread(bulletAmount);
                    bullet.setShotDamageMultiplier(shotDamageMultiplier);
                    bullet.setShotContext(shotContext);
                    // 명중·처치 경보도 총소리 경보와 같은 기준으로 소음기 사격인지 판단한다.
                    bullet.setSilenced(useSilenceSound);
                    abstractGunItem.doBulletSpread(dataHolder, itemStack, shooter, bullet, i, processedSpeed,
                            inaccuracy, pitch, yaw);
                    world.addFreshEntity(bullet);
                }
                // 총소리 재생
                if (soundDistance > 0) {
                    String soundId = useSilenceSound ? SoundManager.SILENCE_3P_SOUND : SoundManager.SHOOT_3P_SOUND;
                    SoundManager.sendSoundToNearby(shooter, shotSoundDistance, gunId, gunDisplayId, soundId, shotSoundVolume, 0.9f + shooter.getRandom().nextFloat() * 0.125f);
                    // 플레이어의 총성을 들은 주변 몬스터가 쏜 쪽을 살피러 간다. 소음기를 달면 바로 옆에서만 듣는다.
                    if (shooter instanceof Player playerShooter) {
                        GunfireAlert.onGunshot(playerShooter, useSilenceSound);
                    }
                }
            }
            return true;
        }, period, cycles);
    }

    private <T> T modifyProperty(GunProperty<?> property, Class<T> type, T value) {
        return abstractGunItem.modifyProperty(dataHolder, itemStack, shooter, property, type, value);
    }

    private <T> T modifyProperty(String id, Class<T> type, T value) {
        return abstractGunItem.modifyProperty(dataHolder, itemStack, shooter, id, type, value);
    }

    /**
     * 사격 한 번의 과열 변화를 처리한다
     */
    public void handleShootHeat() {
        GunHeatData heatData = gunIndex.getGunData().getHeatData();
        if (heatData == null) {
            return;
        }
        float newHeat = Math.min(abstractGunItem.getHeatAmount(itemStack) + heatData.getHeatPerShot(), heatData.getHeatMax());
        abstractGunItem.setHeatAmount(itemStack, newHeat);
        if (newHeat >= heatData.getHeatMax()) {
            abstractGunItem.setOverheatLocked(itemStack, true);
        }
    }

    /**
     * 총기 안 탄환을 한 발 줄인다. 볼트 액션, 폐쇄 노리쇠 대기, 개방 노리쇠 대기 규칙을 따라 약실 탄환이나 탄창 탄환을 소모한다.
     * 소모할 탄환이 없으면 false를 돌려준다. 예를 들어 볼트 액션 소총은 탄창에 탄환이 있어도 bolt 전에는 약실에 탄환이 없으므로 false를 돌려준다.
     *
     * @return 탄환을 줄이는 데 성공했는지 여부.
     */
    public boolean reduceAmmoOnce() {
        Bolt boltType = TimelessAPI.getCommonGunIndex(abstractGunItem.getGunId(itemStack))
                .map(index -> index.getGunData().getBolt())
                .orElse(null);
        // 약실에 탄이 있는지
        boolean hasAmmoInBarrel = abstractGunItem.hasBulletInBarrel(itemStack) && boltType != Bolt.OPEN_BOLT;
        // 인벤토리에 탄이 남아 있는지(크리에이티브에서 인벤토리 예비 탄약을 소모하는지)
        boolean hasInventoryAmmo = abstractGunItem.hasInventoryAmmo(shooter, itemStack, isReloadingNeedConsumeAmmo());
        // 탄이 없다고 볼 조건(인벤토리 급탄이면서 인벤토리에 탄 없음 / 인벤토리 급탄이 아니면서 탄창 탄 수 < 1)
        boolean noAmmo = useInventoryAmmo() && !hasInventoryAmmo ||
                !useInventoryAmmo() && abstractGunItem.getCurrentAmmoCount(itemStack) < 1;
        if (boltType == null) {
            return false;
        }
        // 볼트 액션 로직
        if (boltType == Bolt.MANUAL_ACTION) {
            // 약실 탄환이 없으면 쏠 수 없다
            if (!hasAmmoInBarrel) {
                return false;
            }
            // 탄창 탄환이 없으면 약실 탄환을 소모한다
            abstractGunItem.setBulletInBarrel(itemStack, false);
            return true;
        }
        // 폐쇄 노리쇠 로직
        if (boltType == Bolt.CLOSED_BOLT) {
            // 탄창 탄환이 있으면 탄창 탄환을 먼저 소모한다
            if (!noAmmo) {
                // 인벤토리 직접 장전이면 사격 후 인벤토리 탄약 - 1
                if (useInventoryAmmo()) {
                    return consumeAmmoFromPlayer(1) == 1;
                }
                // 인벤토리 직접 장전이 아니면 탄창 탄환 - 1
                abstractGunItem.reduceCurrentAmmoCount(itemStack);
                return true;
            }
            // 약실 탄환이 없으면 쏠 수 없다
            if (!hasAmmoInBarrel) {
                return false;
            }
            // 탄창 탄환이 없으면 약실 탄환을 소모한다
            abstractGunItem.setBulletInBarrel(itemStack, false);
            return true;
        }
        // 개방 노리쇠 로직
        if (boltType == Bolt.OPEN_BOLT) {
            // 탄환이 없으면 쏠 수 없다
            if (noAmmo) {
                return false;
            }
            // 인벤토리 직접 장전이면 사격 후 인벤토리 탄약 - 1
            if (useInventoryAmmo()) {
                return consumeAmmoFromPlayer(1) == 1;
            }
            // 인벤토리 직접 장전이 아니면 탄창 탄환 - 1
            abstractGunItem.reduceCurrentAmmoCount(itemStack);
            return true;
        }
        // 알려진 세 가지 Bolt 종류가 아니면(지금은 생기지 않음) 기본으로 false를 돌려준다
        return false;
    }

    /**
     * 재장전을 시작한 뒤 지난 시간을 가져온다. 단위는 ms다
     *
     * @return 재장전을 시작한 뒤 지난 시간(ms)
     */
    public long getReloadTime() {
        if (dataHolder.reloadTimestamp == -1) {
            return 0;
        }
        return System.currentTimeMillis() - dataHolder.reloadTimestamp;
    }

    /**
     * 노리쇠 당기기를 시작한 뒤 지난 시간을 가져온다. 단위는 ms다
     *
     * @return 노리쇠 당기기를 시작한 뒤 지난 시간(ms)
     */
    public long getBoltTime() {
        if (!dataHolder.isBolting) {
            return 0;
        }
        return System.currentTimeMillis() - dataHolder.boltTimestamp;
    }

    /**
     * 총기의 사격 간격을 가져온다(단위: 밀리초)
     *
     * @return 사격 간격
     */
    public long getShootInterval() {
        FireMode fireMode = abstractGunItem.getFireMode(itemStack);
        if (fireMode == FireMode.BURST) {
            long coolDown = (long) (gunIndex.getGunData().getBurstData().getMinInterval() * 1000f);
            // 지연을 상쇄하려고 5 ms 여유 시간을 준다
            coolDown = coolDown - 5;
            return Math.max(coolDown, 0L);
        }
        long coolDown = gunIndex.getGunData().getShootInterval(this.shooter, fireMode, itemStack);
        // 지연을 상쇄하려고 5 ms 여유 시간을 준다
        coolDown = coolDown - 5;
        return Math.max(coolDown, 0L);
    }

    /**
     * 마지막으로 사격한 timestamp(시스템 시각, 밀리초)를 돌려준다. 총기를 바꾸면 -1로 초기화된다.
     *
     * @return 마지막 사격 timestamp. 총기를 바꾸면 -1로 초기화된다.
     */
    public long getLastShootTimestamp() {
        return dataHolder.lastShootTimestamp + dataHolder.baseTimestamp;
    }

    /**
     * 사격 간격을 조정한다.
     * 사격 간격은 특수해서 클라이언트와 서버에서 따로 계산한다. 그래서 상태 기계 스크립트에서도 이 작업을 한 번 더 해야 한다.
     *
     * @param alpha 더하거나 뺄 사격 간격(밀리초). 양수이면 사격 간격이 늘고, 음수이면 준다.
     * @see GunAnimationStateContext#adjustClientShootInterval
     */
    public void adjustShootInterval(long alpha) {
        dataHolder.shootTimestamp += alpha;
    }

    /**
     * 재장전 시간을 조정한다
     *
     * @param alpha 더하거나 뺄 재장전 시간(밀리초). 양수이면 재장전 시간을 늘리고(재장전 진행이 빨라짐), 음수이면 줄인다(재장전 진행이 느려짐).
     */
    public void adjustReloadTime(long alpha) {
        dataHolder.reloadTimestamp -= alpha;
    }

    /**
     * 노리쇠 당기기 시간을 조정한다
     *
     * @param alpha 더하거나 뺄 노리쇠 당기기 시간(밀리초). 양수이면 노리쇠 당기기 시간을 늘리고(진행이 빨라짐), 음수이면 줄인다(진행이 느려짐).
     */
    public void adjustBoltTime(long alpha) {
        dataHolder.boltTimestamp -= alpha;
    }

    /**
     * 조준 진행도를 가져온다.
     *
     * @return 범위 0~1. 0은 조준하지 않음, 1은 조준 완료를 뜻한다.
     */
    public float getAimingProgress() {
        return dataHolder.aimingProgress;
    }

    // 충전 관련 메서드
    /**
     * 이번 사격의 충전 진행도. 이 문맥은 사격 과정에서만 쓸 수 있으며, 사격 중이 아닐 때 호출하면 의미 없는 값을 돌려준다
     * @return 충전 진행도
     */
    public float getChargeProgress() {
        return dataHolder.chargeProgress;
    }

    /**
     * 현재 발사 방식에서 총기에 충전 설정이 있는지 여부
     * @return 충전 진행도
     */
    public boolean hasChargeData() {
        return getChargeData() != null;
    }

    /**
     * 현재 발사 방식에서 충전 최대 진행도
     */
    public float getMaxCharge() {
        ChargeData chargeData = getChargeData();
        return chargeData == null ? 0f : chargeData.getMaxCharge();
    }

    /**
     * 현재 발사 방식에서 충전 발사 문턱값
     */
    public float getFireThreshold() {
        ChargeData chargeData = getChargeData();
        return chargeData == null ? 0f : chargeData.getFireThreshold();
    }

    /**
     * 이번 사격의 충전 진행도를 계산한다. 이 문맥은 사격 과정에서만 쓸 수 있으며, 사격 중이 아닐 때 호출하면 의미 없는 값을 돌려준다
     */
    public float getChargeRatio() {
        float maxCharge = getMaxCharge();
        if (maxCharge <= 0f) {
            return 0f;
        }
        return Math.max(0f, Math.min(getChargeProgress() / maxCharge, 1f));
    }

    /**
     * 이번 사격의 추가 피해 배율(0-256)을 설정한다. 이 메서드는 사격 과정에서만 쓸 수 있으며, 사격 중이 아닐 때 호출하면 아무 의미가 없다
     */
    public void setShotDamageMultiplier(float multiplier) {
        this.shotDamageMultiplier = clampMultiplier(multiplier);
    }

    /**
     * 이번 사격의 추가 피해 배율을 가져온다. 이 메서드는 사격 과정에서만 쓸 수 있으며, 사격 중이 아닐 때 호출하면 아무 의미가 없다
     */
    public float getShotDamageMultiplier() {
        return shotDamageMultiplier;
    }

    /**
     * 이번 사격의 추가 탄속 배율(0-256)을 설정한다. 이 메서드는 사격 과정에서만 쓸 수 있으며, 사격 중이 아닐 때 호출하면 아무 의미가 없다
     */
    public void setProjectileSpeedMultiplier(float multiplier) {
        this.projectileSpeedMultiplier = clampMultiplier(multiplier);
    }

    /**
     * 이번 사격의 추가 탄속 배율을 가져온다. 이 메서드는 사격 과정에서만 쓸 수 있으며, 사격 중이 아닐 때 호출하면 아무 의미가 없다
     */
    public float getProjectileSpeedMultiplier() {
        return projectileSpeedMultiplier;
    }

    private float clampMultiplier(float multiplier) {
        return Mth.clamp(multiplier, 0f, 256f);
    }

    /**
     * 플레이어의 현재 재장전 상태를 가져온다.
     *
     * @return 플레이어의 현재 재장전 상태(서수)
     */
    public int getReloadStateType() {
        return dataHolder.reloadStateType.ordinal();
    }

    /**
     * 총기의 현재 발사 방식(자동, 반자동, 점사 등)을 가져온다.
     *
     * @return 발사 방식(서수)
     */
    public int getFireMode() {
        return abstractGunItem.getFireMode(itemStack).ordinal();
    }

    /**
     * 현재 플레이어의 사격이 탄약을 소모하는지 가져온다. 설정에 따라 크리에이티브 모드 플레이어는 탄약을 소모하지 않고 쏠 수 있다.
     *
     * @return 사격이 탄약을 소모하는지 여부
     */
    public boolean isShootingNeedConsumeAmmo() {
        return IGunOperator.fromLivingEntity(shooter).consumesAmmoOrNot();
    }

    /**
     * 현재 플레이어의 재장전이 탄약을 소모하는지 가져온다. 보통 크리에이티브 모드에서는 탄약을 소모하지 않는다.
     *
     * @return 재장전이 탄약을 소모하는지 여부
     */
    public boolean isReloadingNeedConsumeAmmo() {
        return IGunOperator.fromLivingEntity(shooter).needCheckAmmo();
    }

    /**
     * 현재 총기에 필요한 탄약 수를 가져온다.
     *
     * @return 현재 총기에 필요한 탄약 수
     */
    public int getNeededAmmoAmount() {
        int maxAmmoCount = ShooterMagazineBonus.maxAmmoCount(shooter, itemStack, gunIndex);
        int currentAmmoCount = abstractGunItem.getCurrentAmmoCount(itemStack);
        return maxAmmoCount - currentAmmoCount;
    }

    /**
     * 탄창 안의 탄약 수를 얻는다.
     *
     * @return 탄창 안의 탄약 수. 약실 안의 탄은 세지 않는다.
     */
    public int getAmmoAmount() {
        return abstractGunItem.getCurrentAmmoCount(itemStack);
    }

    /**
     * 총기 탄창의 최대 탄약 수를 얻는다.
     *
     * @return 총기 탄창의 최대 탄약 수. 약실 안의 탄은 세지 않는다.
     */
    public int getMaxAmmoCount() {
        return ShooterMagazineBonus.maxAmmoCount(shooter, itemStack, gunIndex);
    }

    /**
     * 총기 확장 탄창 단계를 얻는다.
     *
     * @return 확장 단계(0~3). 0은 확장 탄창 없음, 1~3은 해당 단계의 확장 탄창 장착
     */
    public int getMagExtentLevel() {
        return AttachmentDataUtils.getMagExtendLevel(itemStack, gunIndex.getGunData());
    }

    /**
     * 플레이어에게서(또는 가상 예비 탄약에서) 탄약을 가능한 한 많이 소모하고, 소모한 수를 돌려준다
     *
     * @param neededAmount 필요한 탄약 수
     * @return 실제로 소모한 탄약 수
     */
    public int consumeAmmoFromPlayer(int neededAmount) {
        // 인벤토리 직접 장전이면서 크리에이티브 모드라 소모하지 않는 경우
        if (useInventoryAmmo() && !isReloadingNeedConsumeAmmo()) {
            return neededAmount;
        }
        if (abstractGunItem.useDummyAmmo(itemStack)) {
            return abstractGunItem.findAndExtractDummyAmmo(itemStack, neededAmount);
        } else {
            return shooter.tacz$getItemHandler(null)
                    .map(cap -> abstractGunItem.findAndExtractInventoryAmmo(cap, itemStack, neededAmount))
                    .orElse(0);
        }
    }

    /**
     * 플레이어(또는 가상 예비 탄약)에게 소모할 탄약이 있는지 확인한다. 보통 반복 재장전을 끊을 때 쓴다.
     * 크리에이티브 플레이어는 바로 true를 돌려준다
     *
     * @return 플레이어(또는 가상 예비 탄약)에게 소모할 탄약이 있는지
     */
    public boolean hasAmmoToConsume() {
        if (!isReloadingNeedConsumeAmmo()) {
            return true;
        }
        if (abstractGunItem.useDummyAmmo(itemStack)) {
            return abstractGunItem.getDummyAmmoAmount(itemStack) > 0;
        }
        return shooter.tacz$getItemHandler(null).map(cap -> {
            // 인벤토리 확인
            for (int i = 0; i < cap.getSlots(); i++) {
                ItemStack checkAmmoStack = cap.getStackInSlot(i);
                if (checkAmmoStack.getItem() instanceof IAmmo iAmmo && iAmmo.isAmmoOfGun(itemStack, checkAmmoStack)) {
                    return true;
                }
                if (checkAmmoStack.getItem() instanceof IAmmoBox iAmmoBox && iAmmoBox.isAmmoBoxOfGun(itemStack, checkAmmoStack)) {
                    return true;
                }
            }
            return false;
        }).orElse(false);
    }

    /**
     * 탄환을 탄창에 밀어 넣는다.
     *
     * @param amount 밀어 넣을 탄환 수
     * @return 남는 탄환
     */
    public int putAmmoInMagazine(int amount) {
        if (amount < 0) {
            return 0;
        }
        int maxAmmoCount = ShooterMagazineBonus.maxAmmoCount(shooter, itemStack, gunIndex);
        int currentAmmoCount = abstractGunItem.getCurrentAmmoCount(itemStack);
        int newAmmoCount = currentAmmoCount + amount;
        if (maxAmmoCount < newAmmoCount) {
            abstractGunItem.setCurrentAmmoCount(itemStack, maxAmmoCount);
            return newAmmoCount - maxAmmoCount;
        } else {
            abstractGunItem.setCurrentAmmoCount(itemStack, newAmmoCount);
            return 0;
        }
    }

    /**
     * 탄창에서 탄환을 뺀다.
     *
     * @param amount 뺄 수량
     * @return 실제로 뺀 수량
     */
    public int removeAmmoFromMagazine(int amount) {
        if (amount < 0) {
            return 0;
        }
        int currentAmmoCount = abstractGunItem.getCurrentAmmoCount(itemStack);
        if (currentAmmoCount < amount) {
            abstractGunItem.setCurrentAmmoCount(itemStack, 0);
            return currentAmmoCount;
        } else {
            abstractGunItem.setCurrentAmmoCount(itemStack, currentAmmoCount - amount);
            return amount;
        }
    }

    /**
     * 탄창 안 탄환 수를 가져온다.
     *
     * @return 탄창 안 탄환 수
     */
    public int getAmmoCountInMagazine() {
        return abstractGunItem.getCurrentAmmoCount(itemStack);
    }

    /**
     * 약실에 탄환이 있는지 가져온다.
     *
     * @return 약실에 탄환이 있는지 여부. 개방 노리쇠 대기 총기이면 이 메서드는 false를 돌려준다.
     */
    public boolean hasAmmoInBarrel() {
        Bolt boltType = gunIndex.getGunData().getBolt();
        return boltType != Bolt.OPEN_BOLT && abstractGunItem.hasBulletInBarrel(itemStack);
    }

    /**
     * 약실에 탄환이 있는지 설정한다
     */
    public void setAmmoInBarrel(boolean ammoInBarrel) {
        abstractGunItem.setBulletInBarrel(itemStack, ammoInBarrel);
    }

    /**
     * 임의의 lua 객체 데이터를 플레이어 데이터에 캐시한다. 스크립트에서 비동기로 데이터를 넘기거나 메서드 사이에 데이터를 넘길 때 쓴다.
     *
     * @param luaValue 캐시할 lua 객체
     */
    public void cacheScriptData(LuaValue luaValue) {
        this.dataHolder.scriptData = luaValue;
    }

    /**
     * 플레이어 데이터에 캐시한 lua 객체를 꺼낸다.
     *
     * @return 캐시한 lua 객체
     */
    public LuaValue getCachedScriptData() {
        return dataHolder.scriptData;
    }

    /**
     * 총기 data에 선언한 스크립트 매개변수를 가져온다
     *
     * @return 스크립트 매개변수 표
     */
    public LuaTable getScriptParams() {
        LuaTable param = gunIndex.getScriptParam();
        return param == null ? new LuaTable() : param;
    }

    /**
     * 지연 반복 작업을 맡긴다. 메인 스레드에서 실행되어 스레드에 안전하지만 시간은 엄밀하지 않고, 정밀도는 TPS에 따라 다르다.
     *
     * @param value    boolean을 돌려주는 LuaFunction이어야 한다. false를 돌려주면 반복을 끝낸다.
     * @param delayMs  실행을 늦출 시간.
     * @param periodMs 반복 실행 간격.
     * @param cycles   최대 반복 횟수. -1이면 무한이다.
     */
    public void safeAsyncTask(LuaValue value, long delayMs, long periodMs, int cycles) {
        LuaFunction func = value.checkfunction();
        CycleTaskHelper.addCycleTask(() -> func.call().checkboolean(), delayMs, periodMs, cycles);
    }

    /**
     * 현재 시스템 시각(밀리초)을 얻는다.
     *
     * @return 현재 시스템 시각
     */
    public long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 총기의 부착물 ID를 얻는다
     *
     * @return 부착물 ID. 종류가 틀렸거나 해당 부착물이 없으면 빈 부착물 ID 'tacz:empty'
     */
    public String getAttachment(String type) {
        try {
            AttachmentType t = AttachmentType.valueOf(type);
            return abstractGunItem.getAttachmentId(itemStack, t).toString();
        } catch (IllegalArgumentException e) {
            return DefaultAssets.EMPTY_ATTACHMENT_ID.toString();
        }
    }

    /**
     * 현재 총기 아이템의 NBT 접근자를 돌려준다. 영구 데이터를 저장할 때가 아니면 이 메서드를 자주 호출하지 않는다.<br/>
     * {@link LuaNbtAccessor} 참고
     *
     * @return NBT 접근자
     */
    public LuaNbtAccessor getNbt() {
        return nbtUtil;
    }

    /**
     * 스크립트가 총기 아이템에 남겨 둔 정수 상태를 읽는다. 값이 없으면 0 이다.<br/>
     * {@link #getNbt()} 는 복사본이라 거기에 쓴 값은 아이템에 남지 않고, {@link #cacheScriptData} 는
     * 재장전·무기 교체 때 지워진다. 그 뒤에도 유지되어야 하는 상태는 이 메서드 쌍으로 다룬다.
     *
     * @param key 상태 이름
     * @return 저장된 값
     */
    public int getScriptStateInt(String key) {
        if (itemStack == null) {
            return 0;
        }
        return ItemNbtUtils.getTag(itemStack).getCompoundOrEmpty(SCRIPT_STATE_TAG).getIntOr(key, 0);
    }

    /**
     * 스크립트 상태를 총기 아이템에 저장한다. 참고: {@link #getScriptStateInt(String)}
     *
     * @param key   상태 이름
     * @param value 저장할 값
     */
    public void setScriptStateInt(String key, int value) {
        if (itemStack == null) {
            return;
        }
        ItemNbtUtils.updateTag(itemStack, nbt -> {
            CompoundTag state = nbt.getCompoundOrEmpty(SCRIPT_STATE_TAG);
            state.putInt(key, value);
            nbt.put(SCRIPT_STATE_TAG, state);
        });
    }

    /**
     * 현재 사격 중인 엔티티에 관한 도구를 돌려준다. 이 접근자는 시스템 메시지 보내기, ActionBar 보내기, 글자 컴포넌트 만들기 같은 자주 쓰는 메서드를 제공한다.<br/>
     * {@link LuaEntityAccessor} 참고
     *
     * @return 엔티티 접근자
     */
    public LuaEntityAccessor getEntityUtil() {
        if (entityAccessor == null) {
            entityAccessor = new LuaEntityAccessor(shooter);
        }
        return entityAccessor;
    }

    public void setShooter(LivingEntity shooter) {
        this.shooter = shooter;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
        initGunItem();
    }

    public void setPitchSupplier(Supplier<Float> pitchSupplier) {
        this.pitchSupplier = pitchSupplier;
    }

    public void setYawSupplier(Supplier<Float> yawSupplier) {
        this.yawSupplier = yawSupplier;
    }

    public LivingEntity getShooter() {
        return shooter;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public AbstractGunItem getAbstractGunItem() {
        return abstractGunItem;
    }

    public CommonGunIndex getGunIndex() {
        return gunIndex;
    }

    public void setHeatAmount(float amount) {
        abstractGunItem.setHeatAmount(itemStack, amount);
    }

    public float getHeatAmount() {
        return abstractGunItem.getHeatAmount(itemStack);
    }

    public boolean hasHeatData() {
        return gunIndex.getGunData().getHeatData() != null;
    }

    public float getHeatMinRpm() {
        if (hasHeatData()) return gunIndex.getGunData().getHeatData().getMinRpmMod();
        return 0f;
    }

    public float getHeatMaxRpm() {
        if (hasHeatData()) return gunIndex.getGunData().getHeatData().getMaxRpmMod();
        return 0f;
    }

    public float getHeatMinInaccuracy() {
        if (hasHeatData()) return gunIndex.getGunData().getHeatData().getMinInaccuracy();
        return 0f;
    }

    public float getHeatMaxInaccuracy() {
        if (hasHeatData()) return gunIndex.getGunData().getHeatData().getMaxInaccuracy();
        return 0f;
    }

    public float getHeatMax() {
        if (hasHeatData()) return gunIndex.getGunData().getHeatData().getHeatMax();
        return 0f;
    }

    public float getHeatPerShot() {
        if (hasHeatData()) return gunIndex.getGunData().getHeatData().getHeatPerShot();
        return 0f;
    }

    public boolean isOverheatLocked() {
        return abstractGunItem.isOverheatLocked(itemStack);
    }

    public void setOverheatLocked(boolean locked) {
        abstractGunItem.setOverheatLocked(itemStack, locked);
    }

    public long getOverheatTime() {
        if (hasHeatData()) return gunIndex.getGunData().getHeatData().getOverHeatTime();
        return 0;
    }

    public long getCoolingDelay() {
        if (hasHeatData()) return gunIndex.getGunData().getHeatData().getCoolingDelay();
        return 0;
    }

    public float calcHeatReduction(long heatTimestamp) {
        GunHeatData heatData = gunIndex.getGunData().getHeatData();
        if (heatData != null) {
            return ((float) (System.currentTimeMillis() - heatTimestamp) / 10000f)
                    * heatData.getCoolingMultiplier();
        }
        return 0f;
    }

    // TODO: enum 값을 lua에서 바로 호출할 수 있는지 시험해, 이 기능을 아래 메서드로 단순화할 수 있는지 확인한다
    public int getBoltByInt() {
        Bolt bolt = gunIndex.getGunData().getBolt();
        if (bolt == Bolt.MANUAL_ACTION) {
            return 1;
        }
        if (bolt == Bolt.CLOSED_BOLT) {
            return 2;
        }
        if (bolt == Bolt.OPEN_BOLT) {
            return 3;
        }
        return 0;
    }

    public Bolt getBolt() {
        return gunIndex.getGunData().getBolt();
    }

    private ChargeData getChargeData() {
        FireMode fireMode = abstractGunItem.getFireMode(itemStack);
        return gunIndex.getGunData().getChargeData(fireMode);
    }

    public void setDataHolder(ShooterDataHolder dataHolder) {
        this.dataHolder = dataHolder;
    }

    public boolean useInventoryAmmo() {
        return abstractGunItem.useInventoryAmmo(itemStack);
    }

    ShooterDataHolder getDataHolder() {
        return this.dataHolder;
    }

    private void initGunItem() {
        if (itemStack == null || !(itemStack.getItem() instanceof AbstractGunItem gunItem)) {
            gunIndex = null;
            abstractGunItem = null;
            return;
        }
        gunId = gunItem.getGunId(itemStack);
        gunDisplayId = gunItem.getGunDisplayId(itemStack);
        Optional<CommonGunIndex> gunIndexOptional = TimelessAPI.getCommonGunIndex(gunId);
        gunIndex = gunIndexOptional.orElse(null);
        abstractGunItem = gunItem;
        CustomData customData = itemStack.get(DataComponents.CUSTOM_DATA);
        if (customData != null && !customData.isEmpty()) {
            nbtUtil = new LuaNbtAccessor(customData.copyTag());
        }
    }


    private LuaFunction checkFunction(LuaValue luaValue) {
        if (luaValue.isfunction()) {
            return (LuaFunction) luaValue;
        } else if (luaValue.isnil()) {
            return null;
        } else {
            throw new LuaError("bad argument: function or nil expected, got " + luaValue.typename());
        }
    }
}
