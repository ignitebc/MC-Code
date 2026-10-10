package com.tacz.guns.util;

import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.api.modifier.JsonProperty;
import com.tacz.guns.config.sync.SyncConfig;
import com.tacz.guns.resource.index.CommonAttachmentIndex;
import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import com.tacz.guns.resource.modifier.custom.*;
import com.tacz.guns.resource.pojo.data.attachment.AttachmentData;
import com.tacz.guns.resource.pojo.data.attachment.Modifier;
import com.tacz.guns.resource.pojo.data.gun.BulletData;
import com.tacz.guns.resource.pojo.data.gun.ExplosionData;
import com.tacz.guns.resource.pojo.data.gun.ExtraDamage;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.resource.pojo.data.gun.GunFireModeAdjustData;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 부착물 데이터 도구 클래스. 아이템 속성을 오프라인으로 계산하는 데 쓴다<br>
 * 자주 호출하지 말고 가능하면 엔티티 캐시를 호출한다<br>
 * {@link AttachmentCacheProperty} 참고
 */
public final class AttachmentDataUtils {
    public static void getAllAttachmentData(ItemStack gunItem, GunData gunData, Consumer<AttachmentData> dataConsumer) {
        IGun iGun = IGun.getIGunOrNull(gunItem);
        if (iGun == null) {
            return;
        }
        for (AttachmentType type : AttachmentType.values()) {
            if (type == AttachmentType.NONE) {
                continue;
            }
            Identifier attachmentId = iGun.getAttachmentId(gunItem, type);
            if (DefaultAssets.isEmptyAttachmentId(attachmentId)) {
                continue;
            }
            AttachmentData attachmentData = gunData.getExclusiveAttachments().get(attachmentId);
            if (attachmentData != null) {
                dataConsumer.accept(attachmentData);
            } else {
                TimelessAPI.getCommonAttachmentIndex(attachmentId).ifPresent(index -> dataConsumer.accept(index.getData()));
            }
        }
    }

    public static int getMagExtendLevel(ItemStack gunItem, GunData gunData) {
        IGun iGun = IGun.getIGunOrNull(gunItem);
        if (iGun == null) {
            return 0;
        }
        Identifier attachmentId = iGun.getAttachmentId(gunItem, AttachmentType.EXTENDED_MAG);
        if (DefaultAssets.isEmptyAttachmentId(attachmentId)) {
            return 0;
        }
        AttachmentData attachmentData = gunData.getExclusiveAttachments().get(attachmentId);
        if (attachmentData != null) {
            int level = attachmentData.getExtendedMagLevel();
            if (level <= 0) {
                return 0;
            } else return Math.min(level, 3);
        } else {
            return TimelessAPI.getCommonAttachmentIndex(attachmentId).map(index -> {
                int level = index.getData().getExtendedMagLevel();
                if (level <= 0) {
                    return 0;
                } else return Math.min(level, 3);
            }).orElse(0);
        }
    }

    public static int getAmmoCountWithAttachment(ItemStack gunItem, GunData gunData) {
        int[] extendedMagAmmoAmount = gunData.getExtendedMagAmmoAmount();
        if (extendedMagAmmoAmount == null) {
            return gunData.getAmmoAmount();
        }
        int level = getMagExtendLevel(gunItem, gunData);
        if (level == 0) {
            return gunData.getAmmoAmount();
        }
        return extendedMagAmmoAmount[level - 1];
    }

    public static double getWightWithAttachment(ItemStack gunItem, GunData gunData) {
        IGun iGun = IGun.getIGunOrNull(gunItem);
        if (iGun == null) {
            return gunData.getWeight();
        }

        List<Modifier> modifiers = new ArrayList<>();
        for (AttachmentType type : AttachmentType.values()) {
            Identifier id = iGun.getAttachmentId(gunItem, type);
            AttachmentData attachmentData = gunData.getExclusiveAttachments().get(id);
            if (attachmentData != null) {
                var m = attachmentData.getModifier().get(WeightModifier.ID);
                if (m != null && m.getValue() instanceof Modifier modifier) {
                    modifiers.add(modifier);
                } else {
                    Modifier modifier = new Modifier();
                    modifier.setAddend(attachmentData.getWeight());
                    modifiers.add(modifier);
                }
            } else {
                TimelessAPI.getCommonAttachmentIndex(id).ifPresent(index -> {
                    var m = index.getData().getModifier().get(WeightModifier.ID);
                    if (m != null && m.getValue() instanceof Modifier modifier) {
                        modifiers.add(modifier);
                    } else {
                        Modifier modifier = new Modifier();
                        modifier.setAddend(index.getData().getWeight());
                        modifiers.add(modifier);
                    }
                });
            }
        }
        return AttachmentPropertyManager.eval(modifiers, gunData.getWeight());
    }

    public static boolean isExplodeEnabled(ItemStack gunItem, GunData gunData) {
        IGun iGun = IGun.getIGunOrNull(gunItem);
        if (iGun == null) {
            if (gunData.getBulletData().getExplosionData() != null) {
                return gunData.getBulletData().getExplosionData().isExplode();
            } else {
                return false;
            }
        }
        return calcBooleanValue(gunItem, gunData, ExplosionModifier.ID, ExplosionModifier.ExplosionModifierValue.class,
                ExplosionModifier.ExplosionModifierValue::isExplode);
    }

    /**
     * 부품 보정까지 반영한 폭발 피해를 계산한다.
     * <p>
     * 총기에 폭발 수치가 없으면 {@link ExplosionModifier#DEFAULT_EXPLOSION_DATA} 의 기본값을 쓴다.
     * 폭발이 켜져 있지 않은 총기에도 값을 돌려주므로, 표시 여부는 {@link #isExplodeEnabled} 로 따로 판단한다.
     */
    public static double getExplosionDamageWithAttachment(ItemStack gunItem, GunData gunData) {
        ExplosionData explosionData = gunData.getBulletData().getExplosionData();
        float base = explosionData != null ? explosionData.getDamage() : ExplosionModifier.DEFAULT_EXPLOSION_DATA.getDamage();

        List<Modifier> modifiers = new ArrayList<>();
        getAllAttachmentData(gunItem, gunData, data -> {
            var property = data.getModifier().get(ExplosionModifier.ID);
            if (property != null && property.getValue() instanceof ExplosionModifier.ExplosionModifierValue value) {
                modifiers.add(value.getDamage());
            }
        });
        return AttachmentPropertyManager.eval(modifiers, base) * SyncConfig.DAMAGE_BASE_MULTIPLIER.get();
    }

    public static double getArmorIgnoreWithAttachment(ItemStack gunItem, GunData gunData) {
        IGun iGun = IGun.getIGunOrNull(gunItem);
        if (iGun == null) {
            return 0;
        }
        FireMode fireMode = iGun.getFireMode(gunItem);
        BulletData bulletData = gunData.getBulletData();
        GunFireModeAdjustData fireModeAdjustData = gunData.getFireModeAdjustData(fireMode);
        // 추가 피해
        ExtraDamage extraDamage = bulletData.getExtraDamage();
        // 발사 방식 조정
        // 최종 base
        float finalBase = extraDamage != null ? extraDamage.getArmorIgnore() : 0f;
        finalBase = fireModeAdjustData != null ? finalBase + fireModeAdjustData.getArmorIgnore() : finalBase;
        finalBase *= SyncConfig.ARMOR_IGNORE_BASE_MULTIPLIER.get();

        List<Modifier> modifiers = getModifiers(gunItem, gunData, ArmorIgnoreModifier.ID);
        return AttachmentPropertyManager.eval(modifiers, finalBase);
    }

    public static double getHeadshotMultiplier(ItemStack gunItem, GunData gunData) {
        IGun iGun = IGun.getIGunOrNull(gunItem);
        if (iGun == null) {
            return 0;
        }
        FireMode fireMode = iGun.getFireMode(gunItem);
        BulletData bulletData = gunData.getBulletData();
        GunFireModeAdjustData fireModeAdjustData = gunData.getFireModeAdjustData(fireMode);
        // 추가 피해
        ExtraDamage extraDamage = bulletData.getExtraDamage();
        // 발사 방식 조정
        // 최종 base
        float finalBase = extraDamage != null ? extraDamage.getHeadShotMultiplier() : 0f;
        finalBase = fireModeAdjustData != null ? finalBase + fireModeAdjustData.getHeadShotMultiplier() : finalBase;
        finalBase *= SyncConfig.HEAD_SHOT_BASE_MULTIPLIER.get();

        List<Modifier> modifiers = getModifiers(gunItem, gunData, HeadShotModifier.ID);
        return AttachmentPropertyManager.eval(modifiers, finalBase);
    }

    public static double getDamageWithAttachment(ItemStack gunItem, GunData gunData) {
        IGun iGun = IGun.getIGunOrNull(gunItem);
        if (iGun == null) {
            return 0;
        }
        FireMode fireMode = iGun.getFireMode(gunItem);
        BulletData bulletData = gunData.getBulletData();
        GunFireModeAdjustData fireModeAdjustData = gunData.getFireModeAdjustData(fireMode);
        // 추가 피해
        ExtraDamage extraDamage = bulletData.getExtraDamage();
        float rawDamage = bulletData.getDamageAmount();
        // 발사 방식 조정
        // 최종 base 피해
        float finalBase = fireModeAdjustData != null ? fireModeAdjustData.getDamageAmount() : 0f;
        if (extraDamage != null && extraDamage.getDamageAdjust() != null) {
            finalBase += extraDamage.getDamageAdjust().get(0).getDamage();
        } else {
            finalBase += rawDamage;
        }
        finalBase *= SyncConfig.DAMAGE_BASE_MULTIPLIER.get();

        List<Modifier> modifiers = getModifiers(gunItem, gunData, DamageModifier.ID);
        double damage = AttachmentPropertyManager.eval(modifiers, finalBase);
        return damage * GunLevelManager.getDamageMultiplier(gunItem);
    }

    /**
     * 지정한 id로 총기 아이템의 modifier 목록을 가져온다
     *
     * @param gunItem
     * @param gunData
     * @param id
     * @return
     */
    private static List<Modifier> getModifiers(ItemStack gunItem, GunData gunData, String id) {
        IGun iGun = IGun.getIGunOrNull(gunItem);
        if (iGun == null) {
            return new ArrayList<>();
        }
        List<Modifier> modifiers = new ArrayList<>();
        for (AttachmentType type : AttachmentType.values()) {
            Identifier attachmentId = iGun.getAttachmentId(gunItem, type);
            if (DefaultAssets.isEmptyAttachmentId(attachmentId)) {
                continue;
            }
            AttachmentData attachmentData = gunData.getExclusiveAttachments().get(attachmentId);
            if (attachmentData != null) {
                var m = attachmentData.getModifier().get(id);
                if (m != null && m.getValue() instanceof Modifier modifier) {
                    modifiers.add(modifier);
                }
            } else {
                CommonAttachmentIndex index = TimelessAPI.getCommonAttachmentIndex(attachmentId).orElse(null);
                if (index != null) {
                    var m = index.getData().getModifier().get(id);
                    if (m != null && m.getValue() instanceof Modifier modifier) {
                        modifiers.add(modifier);
                    }
                }
            }
        }
        return modifiers;
    }

    /**
     * 불리언 값을 계산한다. 논리합을 취한다
     *
     * @param gunItem  총기 아이템
     * @param gunData  총기 원본 데이터
     * @param id       modifier id
     * @param clazz    data 데이터 구조 클래스
     * @param resolver 불리언 값을 얻는 메서드
     * @param <T>      data 데이터 구조 제네릭
     * @return 계산 결과
     */
    private static <T> boolean calcBooleanValue(ItemStack gunItem, GunData gunData, String id, Class<T> clazz, BooleanResolver<T> resolver) {
        IGun iGun = IGun.getIGunOrNull(gunItem);
        if (iGun == null) {
            return false;
        }
        for (AttachmentType type : AttachmentType.values()) {
            Identifier attachmentId = iGun.getAttachmentId(gunItem, type);
            if (DefaultAssets.isEmptyAttachmentId(attachmentId)) {
                continue;
            }
            AttachmentData attachmentData = gunData.getExclusiveAttachments().get(attachmentId);
            if (attachmentData != null) {
                var m = attachmentData.getModifier().get(id);
                boolean value = resolve(m, resolver, clazz);
                if (value) {
                    return true;
                }
            } else {
                CommonAttachmentIndex index = TimelessAPI.getCommonAttachmentIndex(attachmentId).orElse(null);
                if (index != null) {
                    var m = index.getData().getModifier().get(id);
                    boolean value = resolve(m, resolver, clazz);
                    if (value) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static <T> boolean resolve(JsonProperty<?> raw, BooleanResolver<T> data, Class<T> type) {
        if (raw != null && raw.getValue() != null && raw.getValue().getClass().equals(type)) {
            return data.apply((T) raw.getValue());
        }
        return false;
    }

    @FunctionalInterface
    public interface BooleanResolver<T> {
        boolean apply(T data);
    }
}
