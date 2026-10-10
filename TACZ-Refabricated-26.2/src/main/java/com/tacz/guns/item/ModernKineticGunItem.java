package com.tacz.guns.item;

import com.google.common.base.Suppliers;
import com.tacz.guns.GunMod;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.GunProperties;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.ReloadState;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.api.item.nbt.GunItemDataAccessor;
import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.attachment.EffectData;
import com.tacz.guns.resource.pojo.data.attachment.MeleeData;
import com.tacz.guns.resource.pojo.data.gun.*;
import com.tacz.guns.util.AllowAttachmentTagMatcher;
import com.tacz.guns.util.EntityUtil;
import com.tacz.guns.util.GunLevelManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2d;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;
import org.slf4j.MarkerFactory;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.DoubleFunction;
import java.util.function.Supplier;

/**
 * 현대식 총의 로직 구현
 */
public class ModernKineticGunItem extends AbstractGunItem implements GunItemDataAccessor {
    public static final String TYPE_NAME = "modern_kinetic";

    private static final DoubleFunction<AttributeModifier> AM_FACTORY = amount -> new AttributeModifier(
            Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "melee_damage"),
            amount, AttributeModifier.Operation.ADD_VALUE
    );

    public ModernKineticGunItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public boolean startBolt(ShooterDataHolder dataHolder, ItemStack gunItem, LivingEntity shooter) {
        ModernKineticGunScriptAPI api = new ModernKineticGunScriptAPI();
        api.setItemStack(gunItem);
        api.setShooter(shooter);
        api.setDataHolder(dataHolder);

        CommonGunIndex gunIndex = api.getGunIndex();
        if (gunIndex == null) {
            return false;
        }
        return Optional.ofNullable(gunIndex.getScript())
                .map(script -> checkFunction(script.get("start_bolt")))
                .map(func -> func.call(CoerceJavaToLua.coerce(api)).checkboolean())
                .orElse(true);
    }

    @Override
    public boolean tickBolt(ShooterDataHolder dataHolder, ItemStack gunItem, LivingEntity shooter) {
        ModernKineticGunScriptAPI api = new ModernKineticGunScriptAPI();
        api.setItemStack(gunItem);
        api.setShooter(shooter);
        api.setDataHolder(dataHolder);

        CommonGunIndex gunIndex = api.getGunIndex();
        if (gunIndex == null) {
            return false;
        }
        return Optional.ofNullable(gunIndex.getScript())
                .map(script -> checkFunction(script.get("tick_bolt")))
                .map(func -> func.call(CoerceJavaToLua.coerce(api)).checkboolean())
                .orElseGet(() -> defaultTickBolt(api));
    }

    @Override
    public void shoot(ShooterDataHolder dataHolder, ItemStack gunItem, Supplier<Float> pitch, Supplier<Float> yaw, LivingEntity shooter) {
        ModernKineticGunScriptAPI api = new ModernKineticGunScriptAPI();
        api.setItemStack(gunItem);
        api.setShooter(shooter);
        api.setDataHolder(dataHolder);
        api.setPitchSupplier(pitch);
        api.setYawSupplier(yaw);

        CommonGunIndex gunIndex = api.getGunIndex();
        if (gunIndex == null) {
            return;
        }

        Optional.ofNullable(gunIndex.getScript())
                .map(script -> checkFunction(script.get("shoot")))
                .ifPresentOrElse(
                        func -> func.call(CoerceJavaToLua.coerce(api)),
                        () -> api.shootOnce(api.isShootingNeedConsumeAmmo()));
    }

    @Override
    public boolean startReload(ShooterDataHolder dataHolder, ItemStack gunItem, LivingEntity shooter) {
        ModernKineticGunScriptAPI api = new ModernKineticGunScriptAPI();
        api.setItemStack(gunItem);
        api.setShooter(shooter);
        api.setDataHolder(dataHolder);

        CommonGunIndex gunIndex = api.getGunIndex();
        if (gunIndex == null) {
            return false;
        }
        return Optional.ofNullable(gunIndex.getScript())
                .map(script -> checkFunction(script.get("start_reload")))
                .map(func -> func.call(CoerceJavaToLua.coerce(api)).checkboolean())
                .orElse(true);
    }

    @Override
    public ReloadState tickReload(ShooterDataHolder dataHolder, ItemStack gunItem, LivingEntity shooter) {
        ModernKineticGunScriptAPI api = new ModernKineticGunScriptAPI();
        api.setItemStack(gunItem);
        api.setShooter(shooter);
        api.setDataHolder(dataHolder);

        CommonGunIndex gunIndex = api.getGunIndex();
        if (gunIndex == null) {
            return new ReloadState();
        }
        return Optional.ofNullable(gunIndex.getScript())
                .map(script -> checkFunction(script.get("tick_reload")))
                .map(func -> {
                    ReloadState reloadState = new ReloadState();
                    Varargs varargs = func.invoke(CoerceJavaToLua.coerce(api));
                    int typeOrdinary = varargs.arg(1).checkint();
                    long countDown = varargs.arg(2).checklong();
                    reloadState.setStateType(ReloadState.StateType.values()[typeOrdinary]);
                    reloadState.setCountDown(countDown);
                    return reloadState;
                })
                .orElseGet(() -> defaultTickReload(api));
    }

    @Override
    public void interruptReload(ShooterDataHolder dataHolder, ItemStack gunItem, LivingEntity shooter) {
        ModernKineticGunScriptAPI api = new ModernKineticGunScriptAPI();
        api.setItemStack(gunItem);
        api.setShooter(shooter);
        api.setDataHolder(dataHolder);

        CommonGunIndex gunIndex = api.getGunIndex();
        if (gunIndex == null) {
            return;
        }
        Optional.ofNullable(gunIndex.getScript())
                .map(script -> checkFunction(script.get("interrupt_reload")))
                .ifPresent(func -> func.call(CoerceJavaToLua.coerce(api)));
    }


    @Override
    public void melee(ShooterDataHolder dataHolder, LivingEntity user, ItemStack gunItem) {
        Identifier gunId = this.getGunId(gunItem);
        TimelessAPI.getCommonGunIndex(gunId).ifPresent(gunIndex -> {
            GunMeleeData meleeData = gunIndex.getGunData().getMeleeData();
            float distance = meleeData.getDistance();

            Identifier muzzleId = this.getAttachmentId(gunItem, AttachmentType.MUZZLE);
            MeleeData muzzleData = getMeleeData(muzzleId);
            if (muzzleData != null) {
                doMelee(user, distance, muzzleData.getDistance(), muzzleData.getRangeAngle(), muzzleData.getKnockback(), muzzleData.getDamage(), muzzleData.getEffects());
                return;
            }

            Identifier stockId = this.getAttachmentId(gunItem, AttachmentType.STOCK);
            MeleeData stockData = getMeleeData(stockId);
            if (stockData != null) {
                doMelee(user, distance, stockData.getDistance(), stockData.getRangeAngle(), stockData.getKnockback(), stockData.getDamage(), stockData.getEffects());
                return;
            }

            GunDefaultMeleeData defaultData = meleeData.getDefaultMeleeData();
            if (defaultData == null) {
                return;
            }
            doMelee(user, distance, defaultData.getDistance(), defaultData.getRangeAngle(), defaultData.getKnockback(), defaultData.getDamage(), Collections.emptyList());
        });
    }

    @Override
    public void tickHeat(ShooterDataHolder dataHolder, ItemStack gunItem, LivingEntity shooter) {
        ModernKineticGunScriptAPI api = new ModernKineticGunScriptAPI();
        api.setItemStack(gunItem);
        api.setShooter(shooter);
        api.setDataHolder(dataHolder);

        long heatTimestamp = dataHolder.heatTimestamp;
        CommonGunIndex gunIndex = api.getGunIndex();
        if (gunIndex == null) {
            return;
        }
        Optional.ofNullable(gunIndex.getScript())
                .map(script -> checkFunction(script.get("tick_heat")))
                .ifPresentOrElse(
                        func -> func.call(CoerceJavaToLua.coerce(api), LuaValue.valueOf(heatTimestamp)),
                        () -> defaultTickHeat(heatTimestamp, gunItem)
                );
    }

    private void defaultTickHeat(long heatTimestamp, ItemStack gunItem) {
        var iGun = IGun.getIGunOrNull(gunItem);
        if (iGun == null) return;
        TimelessAPI.getCommonGunIndex(iGun.getGunId(gunItem))
                .map(index -> index.getGunData().getHeatData())
                .ifPresent(heatData -> {
                    if (iGun.getHeatAmount(gunItem) <= 0) return;
                    if (iGun.isOverheatLocked(gunItem)) {
                        tickLocked(iGun, gunItem, heatData, heatTimestamp);
                    } else {
                        tickNormal(iGun, gunItem, heatData, heatTimestamp);
                    }
                });
    }

    public void tickLocked(IGun iGun, ItemStack gunStack, GunHeatData heatData, long heatTimestamp) {
        if (System.currentTimeMillis() - heatTimestamp >= heatData.getOverHeatTime()) {
            float heatAmount = iGun.getHeatAmount(gunStack)
                    - ((float) (System.currentTimeMillis() - heatTimestamp) / 10000f)
                    * heatData.getCoolingMultiplier();

            iGun.setHeatAmount(gunStack, heatAmount);
            if (heatAmount <= 0) {
                iGun.setOverheatLocked(gunStack, false);
            }
        }
    }

    public void tickNormal(IGun iGun, ItemStack gunStack, GunHeatData heatData, long heatTimestamp) {
        if (System.currentTimeMillis() - heatTimestamp >= heatData.getCoolingDelay()) {
            float heatAmount = iGun.getHeatAmount(gunStack)
                    - ((float) (System.currentTimeMillis() - heatTimestamp) / 10000f)
                    * heatData.getCoolingMultiplier();

            iGun.setHeatAmount(gunStack, heatAmount);
        }
    }

    public <T> T modifyProperty(ShooterDataHolder dataHolder, ItemStack gunItem, LivingEntity shooter,
                                String luaMethodName, String id, Class<T> type, T original) {
        ModernKineticGunScriptAPI api = new ModernKineticGunScriptAPI();
        api.setItemStack(gunItem);
        api.setShooter(shooter);
        api.setDataHolder(dataHolder);

        CommonGunIndex gunIndex = api.getGunIndex();
        if (gunIndex == null) {
            return original;
        }

        var afterDefaultModification = defaultPropertyModification.modify(gunItem, shooter, gunIndex, id, original);

        try {
            return Optional.ofNullable(gunIndex.getScript())
                    .map(script -> checkFunction(script.get(luaMethodName)))
                    .map(func -> func.call(CoerceJavaToLua.coerce(api), LuaValue.valueOf(id), CoerceJavaToLua.coerce(afterDefaultModification)))
                    .map(luaValue -> type.cast(CoerceLuaToJava.coerce(luaValue, type)))
                    .orElse(afterDefaultModification);
        } catch (Exception exception) {
            GunMod.LOGGER.warn(MarkerFactory.getMarker("Gun Script"), "Failed to modify gun property {}", id, exception);
            return afterDefaultModification;
        }
    }

    public final DefaultPropertyModification defaultPropertyModification = new DefaultPropertyModification();

    public class DefaultPropertyModification {
        public static final Identifier SLUGS = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "intrinsic/slug");

        @SuppressWarnings("unchecked")
        public <T> T modify(ItemStack gunItem, LivingEntity shooter, CommonGunIndex gunIndex,
                            String id, T original) {
            if (GunProperties.RuntimeOnly.BULLET_AMOUNT.equals(id)) {
                if (AllowAttachmentTagMatcher.matchTag(SLUGS, getAttachmentId(gunItem, AttachmentType.EXTENDED_MAG))) {
                    return (T) Integer.valueOf(1);
                }
            }
            return original;
        }
    }

    @Override
    public void doBulletSpread(ShooterDataHolder dataHolder, ItemStack gunItem, LivingEntity shooter, Projectile projectile,
                               int bulletCnt, float processedSpeed, float inaccuracy, float pitch, float yaw) {
        if (!(projectile instanceof EntityKineticBullet bullet)) {
            return;
        }
        ModernKineticGunScriptAPI api = new ModernKineticGunScriptAPI();
        api.setItemStack(gunItem);
        api.setShooter(shooter);
        api.setDataHolder(dataHolder);

        CommonGunIndex gunIndex = api.getGunIndex();
        if (gunIndex == null) {
            return;
        }
        Optional.ofNullable(gunIndex.getScript())
                .map(script -> checkFunction(script.get("calcSpread")))
                .map(func -> func.call(CoerceJavaToLua.coerce(api), LuaValue.valueOf(bulletCnt), LuaValue.valueOf(inaccuracy)))
                .map(luaValue -> {
                    if (luaValue.istable()) {
                        LuaTable table = luaValue.checktable();
                        return new Vector2d(table.get(1).checkdouble(), table.get(2).checkdouble());
                    }
                    return null;
                }).ifPresentOrElse(vector2d -> {
                    bullet.shootFromRotation(shooter, pitch, yaw, 0.0F, processedSpeed, vector2d);
                }, () -> {
                    bullet.shootFromRotation(shooter, pitch, yaw, 0.0F, processedSpeed, inaccuracy);
                });
    }

    private boolean defaultTickBolt(ModernKineticGunScriptAPI api) {
        GunData gunData = api.getGunIndex().getGunData();
        long boltActionTime = (long) (gunData.getBoltActionTime() * 1000);
        float rawBoltFeedTime = gunData.getBoltFeedTime();
        long boltFeedTime = rawBoltFeedTime == -1 ? boltActionTime : (long) (gunData.getBoltFeedTime() * 1000);
        if (api.getBoltTime() < boltFeedTime) {
            return true;
        }
        if (!api.hasAmmoInBarrel()) {
            // 인벤토리 직접 장전 방식이면 인벤토리 탄약 소모를 확인한다
            if (api.useInventoryAmmo()) {
                if (api.consumeAmmoFromPlayer(1) == 1) {
                    api.setAmmoInBarrel(true);
                }
            } else if (api.removeAmmoFromMagazine(1) != 0) {
                api.setAmmoInBarrel(true);
            }
        }
        return api.getBoltTime() < boltActionTime;
    }

    private ReloadState defaultTickReload(ModernKineticGunScriptAPI api) {
        CommonGunIndex gunIndex = api.getGunIndex();
        // ReloadData 가져오기
        GunData gunData = gunIndex.getGunData();
        GunReloadData reloadData = gunData.getReloadData();
        // 새 stateType과 countDown 계산
        long countDown;
        ReloadState.StateType stateType;
        ReloadState.StateType oldStateType = ReloadState.StateType.values()[api.getReloadStateType()];
        long progressTime = api.getReloadTime();
        if (oldStateType.isReloadingEmpty()) {
            long feedTime = (long) (reloadData.getFeed().getEmptyTime() * 1000);
            long finishingTime = (long) (reloadData.getCooldown().getEmptyTime() * 1000);
            if (progressTime < feedTime) {
                stateType = ReloadState.StateType.EMPTY_RELOAD_FEEDING;
                countDown = feedTime - progressTime;
            } else if (progressTime < finishingTime) {
                stateType = ReloadState.StateType.EMPTY_RELOAD_FINISHING;
                countDown = finishingTime - progressTime;
            } else {
                stateType = ReloadState.StateType.NOT_RELOADING;
                countDown = ReloadState.NOT_RELOADING_COUNTDOWN;
            }
        } else if (oldStateType.isReloadingTactical()) {
            long feedTime = (long) (reloadData.getFeed().getTacticalTime() * 1000);
            long finishingTime = (long) (reloadData.getCooldown().getTacticalTime() * 1000);
            if (progressTime < feedTime) {
                stateType = ReloadState.StateType.TACTICAL_RELOAD_FEEDING;
                countDown = feedTime - progressTime;
            } else if (progressTime < finishingTime) {
                stateType = ReloadState.StateType.TACTICAL_RELOAD_FINISHING;
                countDown = finishingTime - progressTime;
            } else {
                stateType = ReloadState.StateType.NOT_RELOADING;
                countDown = ReloadState.NOT_RELOADING_COUNTDOWN;
            }
        } else {
            stateType = ReloadState.StateType.NOT_RELOADING;
            countDown = ReloadState.NOT_RELOADING_COUNTDOWN;
        }
        // 재장전 상태가 장전 -> 마무리로 바뀌면 탄 보충을 호출해야 한다
        if (oldStateType == ReloadState.StateType.EMPTY_RELOAD_FEEDING && oldStateType != stateType) {
            this.defaultReloadFinishing(api, false);
        }
        if (oldStateType == ReloadState.StateType.TACTICAL_RELOAD_FEEDING && oldStateType != stateType) {
            this.defaultReloadFinishing(api, true);
        }
        // tick 결과 돌려주기
        ReloadState reloadState = new ReloadState();
        reloadState.setStateType(stateType);
        reloadState.setCountDown(countDown);
        return reloadState;
    }

    private void defaultReloadFinishing(ModernKineticGunScriptAPI api, boolean isTactical) {
        GunData data = api.getGunIndex().getGunData();
        int needAmmoCount = api.getNeededAmmoAmount();
        boolean needConsumeAmmo = api.isReloadingNeedConsumeAmmo();
        boolean infinite = data.getReloadData().isInfinite();
        needConsumeAmmo = needConsumeAmmo && !infinite;
        switch (data.getReloadData().getType()) {
            case MAGAZINE -> {
                if (needConsumeAmmo) {
                    int consumedAmount = api.consumeAmmoFromPlayer(needAmmoCount);
                    api.putAmmoInMagazine(consumedAmount);
                } else {
                    api.putAmmoInMagazine(needAmmoCount);
                }
            }
            case FUEL -> {
                if (needConsumeAmmo) {
                    int consumedAmount = api.consumeAmmoFromPlayer(1);
                    api.putAmmoInMagazine(needAmmoCount * consumedAmount);
                } else {
                    api.putAmmoInMagazine(needAmmoCount);
                }
            }
            case INVENTORY -> {
                // 인벤토리 직접 장전: 재장전할 때 인벤토리에서 탄약을 소모해 탄창에 채운다(MAGAZINE 동작과 같음)
                if (needConsumeAmmo) {
                    int consumedAmount = api.consumeAmmoFromPlayer(needAmmoCount);
                    api.putAmmoInMagazine(consumedAmount);
                } else {
                    api.putAmmoInMagazine(needAmmoCount);
                }
            }
            default -> {
                // 알 수 없는 종류는 MAGAZINE 로직으로 처리한다
                if (needConsumeAmmo) {
                    int consumedAmount = api.consumeAmmoFromPlayer(needAmmoCount);
                    api.putAmmoInMagazine(consumedAmount);
                } else {
                    api.putAmmoInMagazine(needAmmoCount);
                }
            }
        }
        // 전술 재장전이 아니면 탄창의 탄환 한 발을 약실에 넣어야 한다
        Bolt boltType = api.getGunIndex().getGunData().getBolt();
        if (!isTactical && (boltType == Bolt.MANUAL_ACTION || boltType == Bolt.CLOSED_BOLT)) {
            int i = api.removeAmmoFromMagazine(1);
            if (i != 0) {
                api.setAmmoInBarrel(true);
            }
        }
    }

    private void doMelee(LivingEntity user, float gunDistance, float meleeDistance, float rangeAngle, float knockback, float damage, List<EffectData> effects) {
        // 총 길이 + 총검 길이 = 전체 길이
        double distance = gunDistance + meleeDistance;
        float xRot = (float) Math.toRadians(-user.getXRot());
        float yRot = (float) Math.toRadians(-user.getYRot());
        // 시선 벡터
        Vec3 eyeVec = new Vec3(0, 0, 1).xRot(xRot).yRot(yRot).normalize().scale(distance);
        // 구 중심 좌표
        Vec3 centrePos = user.getEyePosition().subtract(eyeVec);
        // 먼저 범위 안 모든 엔티티를 가져온다
        List<LivingEntity> entityList = user.level().getEntitiesOfClass(LivingEntity.class, user.getBoundingBox().inflate(distance));
        Supplier<Float> realDamage = Suppliers.memoize(() -> {
            var instance = user.getAttribute(Attributes.ATTACK_DAMAGE);
            if (instance == null) {
                return damage;
            }
            var oldBase = instance.getBaseValue();
            var modifier = AM_FACTORY.apply(damage);
            try {
                instance.setBaseValue(0);
                instance.addTransientModifier(modifier);
                return (float) instance.getValue();
            } finally {
                instance.setBaseValue(oldBase);
                instance.removeModifier(modifier);
            }
        });
        // 그다음 원뿔 범위 안에 있는지 확인한다
        for (LivingEntity living : entityList) {
            // 주인의 펫은 총검·개머리판 공격을 통과한다. 밀려나거나 총검 효과를 받지 않는다.
            if (EntityUtil.isShootersPet(living, user)) {
                continue;
            }
            // 먼저 구 중심->대상 벡터를 계산한다
            Vec3 targetVec = living.getEyePosition().subtract(centrePos);
            // 대상과 구 중심 사이 거리
            double targetLength = targetVec.length();
            // 거리가 1배 거리 안이면 플레이어 뒤쪽이므로 피해를 주지 않는다
            if (targetLength < distance) {
                continue;
            }
            // 벡터 사이 각 계산
            double degree = Math.toDegrees(Math.acos(targetVec.dot(eyeVec) / (targetLength * distance)));
            // 벡터 사이 각이 범위 안이어야 피해를 줄 수 있다
            if (degree < (rangeAngle / 2)) {
                // 엔티티와 플레이어 사이에 가로막는 것이 있는지 판단한다
                if (user.hasLineOfSight(living)) {
                    doPerLivingHurt(user, living, knockback, realDamage.get(), effects);
                }
            }
        }

        // 플레이어 허기 차감
        if (user instanceof Player player) {
            player.causeFoodExhaustion(0.1F);
        }
    }

    private static void doPerLivingHurt(LivingEntity user, LivingEntity target, float knockback, float damage, List<EffectData> effects) {
        if (target.equals(user)) {
            return;
        }
        target.push(-(float) Math.sin(Math.toRadians(user.getYRot())) * knockback, 0, (float) Math.cos(Math.toRadians(user.getYRot())) * knockback);
        if (user instanceof Player player) {
            target.hurt(user.damageSources().playerAttack(player), damage);
        } else {
            target.hurt(user.damageSources().mobAttack(user), damage);
        }
        // 근접 총기가 Apotheosis 접사/보석을 발동하지 않던 버그 수정(doEnchantDamageEffects는 새 버전에서 제거됨)

        if (!target.isAlive()) {
            return;
        }
        for (EffectData data : effects) {
            var effectHolder = BuiltInRegistries.MOB_EFFECT.get(data.getEffectId());
            if (effectHolder.isEmpty()) {
                continue;
            }
            int time = Math.max(0, data.getTime() * 20);
            int amplifier = Math.max(0, data.getAmplifier());
            MobEffectInstance effectInstance = new MobEffectInstance(effectHolder.get(), time, amplifier, false, data.isHideParticles());
            target.addEffect(effectInstance);
        }
        if (user.level() instanceof ServerLevel serverLevel) {
            int count = (int) (damage * 0.5);
            serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR, target.getX(), target.getY(0.5), target.getZ(), count, 0.1, 0, 0.1, 0.2);
        }
    }

    @Nullable
    private MeleeData getMeleeData(Identifier attachmentId) {
        if (DefaultAssets.isEmptyAttachmentId(attachmentId)) {
            return null;
        }
        return TimelessAPI.getCommonAttachmentIndex(attachmentId).map(index -> index.getData().getMeleeData()).orElse(null);
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

    @Override
    public void fireSelect(ShooterDataHolder dataHolder, ItemStack gunItem) {
        Identifier gunId = this.getGunId(gunItem);
        TimelessAPI.getCommonGunIndex(gunId).map(gunIndex -> {
            FireMode fireMode = this.getFireMode(gunItem);
            List<FireMode> fireModeSet = gunIndex.getGunData().getFireModeSet();
            // 플레이어가 없는 FireMode를 들고 있어도 여기서 정상 상태로 바꿀 수 있다
            int nextIndex = (fireModeSet.indexOf(fireMode) + 1) % fireModeSet.size();
            FireMode nextFireMode = fireModeSet.get(nextIndex);
            this.setFireMode(gunItem, nextFireMode);
            return nextFireMode;
        });
    }

    @Override
    public int getLevel(int exp) {
        return GunLevelManager.getLevel(exp);
    }

    @Override
    public int getExp(int level) {
        return GunLevelManager.getExp(level);
    }

    @Override
    public int getMaxLevel() {
        return GunLevelManager.MAX_LEVEL;
    }
}
