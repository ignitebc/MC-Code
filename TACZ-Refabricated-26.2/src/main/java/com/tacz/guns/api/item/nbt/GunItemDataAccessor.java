package com.tacz.guns.api.item.nbt;

import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.builder.AttachmentItemBuilder;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.index.ClientAttachmentIndex;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.util.GunIdAliases;
import com.tacz.guns.util.GunLevelManager;
import com.tacz.guns.util.ItemNbtUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public interface GunItemDataAccessor extends IGun {
    String GUN_ID_TAG = "GunId";
    String GUN_FIRE_MODE_TAG = "GunFireMode";
    String GUN_HAS_BULLET_IN_BARREL = "HasBulletInBarrel";
    String GUN_CURRENT_AMMO_COUNT_TAG = "GunCurrentAmmoCount";
    String GUN_ATTACHMENT_BASE = "Attachment";
    /**
     * 16차: 26.2의 ItemStack NBT 구조는 {@code {id, count, components:{...}}}다.
     * 장착한 부착물 자신의 데이터는 components 아래 minecraft:custom_data에 있다.
     * 예전 코드는 1.20.x의 {@code "tag"} 하위 키를 그대로 써서 26.2에서는 항상 찾지 못했다.
     */
    String COMPONENTS_TAG = "components";
    /** {@code DataComponents.CUSTOM_DATA.toString()}의 값. 실제로 확인했다. */
    String CUSTOM_DATA_KEY = "minecraft:custom_data";
    String GUN_EXP_TAG = "GunLevelExp";
    String GUN_DUMMY_AMMO = "DummyAmmo";
    String GUN_MAX_DUMMY_AMMO = "MaxDummyAmmo";
    String GUN_ATTACHMENT_LOCK = "AttachmentLock";
    String GUN_DISPLAY_ID_TAG = "GunDisplayId";
    String LASER_COLOR_TAG = "LaserColor";
    String GUN_OVERHEAT_TAG = "HeatAmount";
    String GUN_OVERHEAT_LOCK_TAG = "OverHeated";

    @Override
    default boolean useDummyAmmo(ItemStack gun) {
        CompoundTag nbt = ItemNbtUtils.getTag(gun);
        return nbt.contains(GUN_DUMMY_AMMO);
    }

    @Override
    default int getDummyAmmoAmount(ItemStack gun) {
        CompoundTag nbt = ItemNbtUtils.getTag(gun);
        return Math.max(0, nbt.getIntOr(GUN_DUMMY_AMMO, 0));
    }

    @Override
    default void setDummyAmmoAmount(ItemStack gun, int amount) {
        ItemNbtUtils.updateTag(gun, nbt -> nbt.putInt(GUN_DUMMY_AMMO, Math.max(amount, 0)));
    }

    @Override
    default void addDummyAmmoAmount(ItemStack gun, int amount) {
        if (!useDummyAmmo(gun)) {
            return;
        }
        int maxDummyAmmo = Integer.MAX_VALUE;
        if (hasMaxDummyAmmo(gun)) {
            maxDummyAmmo = getMaxDummyAmmoAmount(gun);
        }
        int finalMax = maxDummyAmmo;
        ItemNbtUtils.updateTag(gun, nbt -> {
            int newAmount = Math.min(nbt.getIntOr(GUN_DUMMY_AMMO, 0) + amount, finalMax);
            nbt.putInt(GUN_DUMMY_AMMO, Math.max(newAmount, 0));
        });
    }

    @Override
    default boolean hasMaxDummyAmmo(ItemStack gun) {
        CompoundTag nbt = ItemNbtUtils.getTag(gun);
        return nbt.contains(GUN_MAX_DUMMY_AMMO);
    }

    @Override
    default int getMaxDummyAmmoAmount(ItemStack gun) {
        CompoundTag nbt = ItemNbtUtils.getTag(gun);
        return Math.max(0, nbt.getIntOr(GUN_MAX_DUMMY_AMMO, 0));
    }

    @Override
    default void setMaxDummyAmmoAmount(ItemStack gun, int amount) {
        ItemNbtUtils.updateTag(gun, nbt -> nbt.putInt(GUN_MAX_DUMMY_AMMO, Math.max(amount, 0)));
    }

    @Override
    default boolean hasAttachmentLock(ItemStack gun) {
        CompoundTag nbt = ItemNbtUtils.getTag(gun);
        if (nbt.contains(GUN_ATTACHMENT_LOCK)) {
            return nbt.getBooleanOr(GUN_ATTACHMENT_LOCK, false);
        }
        return false;
    }

    @Override
    default void setAttachmentLock(ItemStack gun, boolean lock) {
        ItemNbtUtils.updateTag(gun, nbt -> nbt.putBoolean(GUN_ATTACHMENT_LOCK, lock));
    }

    @Override
    @Nonnull
    default Identifier getGunId(ItemStack gun) {
        CompoundTag nbt = ItemNbtUtils.getTag(gun);
        if (nbt.contains(GUN_ID_TAG)) {
            Identifier gunId = Identifier.tryParse(nbt.getStringOr(GUN_ID_TAG, ""));
            return Objects.requireNonNullElse(GunIdAliases.canonicalGunId(gunId), DefaultAssets.EMPTY_GUN_ID);
        }
        return DefaultAssets.EMPTY_GUN_ID;
    }

    @Override
    default void setGunId(ItemStack gun, @Nullable Identifier gunId) {
        Identifier canonicalId = GunIdAliases.canonicalGunId(gunId);
        ItemNbtUtils.updateTag(gun, nbt -> {
            if (canonicalId != null) {
                nbt.putString(GUN_ID_TAG, canonicalId.toString());
            }
        });
    }

    @Override
    @NotNull
    default Identifier getGunDisplayId(ItemStack gun) {
        CompoundTag nbt = ItemNbtUtils.getTag(gun);
        if (nbt.contains(GUN_DISPLAY_ID_TAG)) {
            Identifier gunDisplayId = Identifier.tryParse(nbt.getStringOr(GUN_DISPLAY_ID_TAG, ""));
            return Objects.requireNonNullElse(GunIdAliases.canonicalDisplayId(gunDisplayId), DefaultAssets.DEFAULT_GUN_DISPLAY_ID);
        }
        return DefaultAssets.DEFAULT_GUN_DISPLAY_ID;
    }

    @Override
    default void setGunDisplayId(ItemStack gun, Identifier displayId) {
        Identifier canonicalId = GunIdAliases.canonicalDisplayId(displayId);
        ItemNbtUtils.updateTag(gun, nbt -> {
            if (canonicalId != null) {
                nbt.putString(GUN_DISPLAY_ID_TAG, canonicalId.toString());
            }
        });
    }

    @Override
    default int getLevel(ItemStack gun) {
        CompoundTag nbt = ItemNbtUtils.getTag(gun);
        if (nbt.contains(GUN_EXP_TAG)) {
            return getLevel(nbt.getIntOr(GUN_EXP_TAG, 0));
        }
        return getLevel(0);
    }

    @Override
    default int getExp(ItemStack gun) {
        CompoundTag nbt = ItemNbtUtils.getTag(gun);
        if (nbt.contains(GUN_EXP_TAG)) {
            return GunLevelManager.clampExp(nbt.getIntOr(GUN_EXP_TAG, 0));
        }
        return 0;
    }

    @Override
    default int getExpToNextLevel(ItemStack gun) {
        int exp = getExp(gun);
        int level = getLevel(exp);
        if (level >= getMaxLevel()) {
            return 0;
        }
        int nextLevelExp = getExp(level + 1);
        return nextLevelExp - exp;
    }

    @Override
    default int getExpCurrentLevel(ItemStack gun) {
        int exp = getExp(gun);
        int level = getLevel(exp);
        if (level >= getMaxLevel()) {
            return 0;
        }
        return exp - getExp(level);
    }

    @Override
    default FireMode getFireMode(ItemStack gun) {
        CompoundTag nbt = ItemNbtUtils.getTag(gun);
        if (nbt.contains(GUN_FIRE_MODE_TAG)) {
            return FireMode.valueOf(nbt.getStringOr(GUN_FIRE_MODE_TAG, "UNKNOWN"));
        }
        return FireMode.UNKNOWN;
    }

    @Override
    default void setFireMode(ItemStack gun, @Nullable FireMode fireMode) {
        ItemNbtUtils.updateTag(gun, nbt -> {
            if (fireMode != null) {
                nbt.putString(GUN_FIRE_MODE_TAG, fireMode.name());
            } else {
                nbt.putString(GUN_FIRE_MODE_TAG, FireMode.UNKNOWN.name());
            }
        });
    }

    @Override
    default int getCurrentAmmoCount(ItemStack gun) {
        CompoundTag nbt = ItemNbtUtils.getTag(gun);
        if (nbt.contains(GUN_CURRENT_AMMO_COUNT_TAG)) {
            return nbt.getIntOr(GUN_CURRENT_AMMO_COUNT_TAG, 0);
        }
        return 0;
    }

    @Override
    default void setCurrentAmmoCount(ItemStack gun, int ammoCount) {
        ItemNbtUtils.updateTag(gun, nbt -> nbt.putInt(GUN_CURRENT_AMMO_COUNT_TAG, Math.max(ammoCount, 0)));
    }

    @Override
    default void reduceCurrentAmmoCount(ItemStack gun) {
        // 인벤토리 급탄을 쓰지 않을 때만 AmmoCount를 줄인다
        if (!useInventoryAmmo(gun)) {
            setCurrentAmmoCount(gun, getCurrentAmmoCount(gun) - 1);
        }
    }

    @Override
    /**
     * 장착한 부착물 자신의 custom_data 태그를 읽는다.
     *
     * <h2>16차 수정: {@code "tag"}는 1.20.x의 예전 NBT 구조다</h2>
     *
     * 원래 구현은 {@code allItemStackTag.contains("tag")}를 찾았는데, 이것은 <b>아이템 컴포넌트화 이전</b>
     * (1.20.4 이하)의 ItemStack NBT 구조 {@code {id, Count, tag:{...}}}다.
     *
     * <p>26.2의 {@code ItemStack} 직렬화 결과는 {@code {id, count, components:{...}}}이며,
     * <b>최상위에 "tag" 키가 아예 없다</b>(실측: 최상위 키는 {@code [count, id]}).
     * 그래서 이 메서드는 <b>항상 null을 돌려줬고</b> → {@link #getAttachmentId}는 항상
     * {@code EMPTY_ATTACHMENT_ID}를 돌려줘 → <b>장착한 모든 부착물이 "미장착"으로 판정됐다</b>.
     *
     * <p>이 버그 하나가 16차에서 보고된 여러 현상을 한꺼번에 설명한다:
     * <ul>
     *   <li>조준경을 달아도 인정되지 않음({@code FirstPersonRenderGunEvent}가 scopeId를 비었다고 판정 → 가늠쇠 조준)</li>
     *   <li>확장 탄창이 전혀 적용되지 않음({@code getMagExtendLevel}이 ID를 못 얻어 → 항상 0단계)</li>
     *   <li>각종 특수 탄창 부착물(소이탄 등)이 적용되지 않음(위와 같음)</li>
     * </ul>
     * 레이저가 "적용되는 것처럼 보였던" 이유는 이 메서드에 기대지 않는 별도의 모델 렌더링 경로를 쓰기 때문이다.
     *
     * <p>올바른 구조(원본 1.21.1과 같음):
     * {@code <부착물 칸 키> -> "components" -> "minecraft:custom_data"}.
     */
    @Nullable
    default CompoundTag getAttachmentTag(ItemStack gun, AttachmentType type) {
        if (!allowAttachmentType(gun, type)) {
            return null;
        }
        CompoundTag nbt = ItemNbtUtils.getTag(gun);
        String key = GUN_ATTACHMENT_BASE + type.name();
        if (!nbt.contains(key)) {
            return null;
        }
        CompoundTag stackTag = nbt.getCompoundOrEmpty(key);
        if (!stackTag.contains(COMPONENTS_TAG)) {
            return null;
        }
        CompoundTag components = stackTag.getCompoundOrEmpty(COMPONENTS_TAG);
        if (!components.contains(CUSTOM_DATA_KEY)) {
            return null;
        }
        return components.getCompoundOrEmpty(CUSTOM_DATA_KEY);
    }

    /**
     * 장착한 부착물 자신의 custom_data 태그를 다시 쓴다(조준경 배율 전환 등에 쓴다).
     *
     * <p>16차 추가: 이 메서드는 이식할 때 <b>통째로 빠졌다</b>(원본 1.21.1에는 있다).
     * 그래서 "장착한 부착물" 자신의 데이터를 바꿔야 하는 기능이 모두 저장되지 않았고,
     * 대표 증상이 조준경 배율을 바꿔도 적용되지 않거나 총을 바꾸면 되돌아가는 것이었다.
     */
    @Override
    default void setAttachmentTag(ItemStack gun, AttachmentType type, CompoundTag attachmentTag) {
        if (!allowAttachmentType(gun, type)) {
            return;
        }
        ItemNbtUtils.updateTag(gun, nbt -> {
            String key = GUN_ATTACHMENT_BASE + type.name();
            if (!nbt.contains(key)) {
                return;
            }
            CompoundTag stackTag = nbt.getCompoundOrEmpty(key);
            if (!stackTag.contains(COMPONENTS_TAG)) {
                return;
            }
            CompoundTag components = stackTag.getCompoundOrEmpty(COMPONENTS_TAG);
            if (!components.contains(CUSTOM_DATA_KEY)) {
                return;
            }
            components.put(CUSTOM_DATA_KEY, attachmentTag);
            stackTag.put(COMPONENTS_TAG, components);
            nbt.put(key, stackTag);
        });
    }

    @Override
    @NotNull
    default ItemStack getBuiltinAttachment(ItemStack gun, AttachmentType type) {
        IGun iGun = IGun.getIGunOrNull(gun);
        if (iGun == null) {
            return ItemStack.EMPTY;
        }
        CommonGunIndex index = TimelessAPI.getCommonGunIndex(iGun.getGunId(gun)).orElse(null);
        if (index != null) {
            var builtin = index.getGunData().getBuiltInAttachments();
            if (builtin.containsKey(type)) {
                return AttachmentItemBuilder.create().setId(builtin.get(type)).build();
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    @Nonnull
    default ItemStack getAttachment(ItemStack gun, AttachmentType type) {
        if (!allowAttachmentType(gun, type)) {
            return ItemStack.EMPTY;
        }
        CompoundTag nbt = ItemNbtUtils.getTag(gun);
        String key = GUN_ATTACHMENT_BASE + type.name();
        if (nbt.contains(key)) {
            return ItemNbtUtils.loadItemStack(nbt.getCompoundOrEmpty(key));
        }
        return ItemStack.EMPTY;
    }

    @Override
    @NotNull
    default Identifier getBuiltInAttachmentId(ItemStack gun, AttachmentType type) {
        IGun iGun = IGun.getIGunOrNull(gun);
        if (iGun == null) {
            return DefaultAssets.EMPTY_ATTACHMENT_ID;
        }
        CommonGunIndex index = TimelessAPI.getCommonGunIndex(iGun.getGunId(gun)).orElse(null);
        if (index != null) {
            var builtin = index.getGunData().getBuiltInAttachments();
            if (builtin.containsKey(type)) {
                return builtin.get(type);
            }
        }
        return DefaultAssets.EMPTY_ATTACHMENT_ID;
    }

    @Override
    @Nonnull
    default Identifier getAttachmentId(ItemStack gun, AttachmentType type) {
        CompoundTag attachmentTag = this.getAttachmentTag(gun, type);
        if (attachmentTag != null) {
            return AttachmentItemDataAccessor.getAttachmentIdFromTag(attachmentTag);
        }
        return DefaultAssets.EMPTY_ATTACHMENT_ID;
    }

    @Override
    default void installAttachment(@Nonnull ItemStack gun, @Nonnull ItemStack attachment) {
        if (!allowAttachment(gun, attachment)) {
            return;
        }
        IAttachment iAttachment = IAttachment.getIAttachmentOrNull(attachment);
        if (iAttachment == null) {
            return;
        }
        String key = GUN_ATTACHMENT_BASE + iAttachment.getType(attachment).name();
        CompoundTag attachmentTag = ItemNbtUtils.saveItemStack(attachment);
        ItemNbtUtils.updateTag(gun, nbt -> nbt.put(key, attachmentTag));
    }

    @Override
    default void unloadAttachment(@Nonnull ItemStack gun, AttachmentType type) {
        if (!allowAttachmentType(gun, type)) {
            return;
        }
        String key = GUN_ATTACHMENT_BASE + type.name();
        CompoundTag attachmentTag = ItemNbtUtils.saveItemStack(ItemStack.EMPTY);
        ItemNbtUtils.updateTag(gun, nbt -> nbt.put(key, attachmentTag));
    }

    @Override
    default float getAimingZoom(ItemStack gunItem) {
        float zoom = 1;
        Identifier scopeId = this.getAttachmentId(gunItem, AttachmentType.SCOPE);
        boolean builtin = false;
        if (scopeId.equals(DefaultAssets.EMPTY_ATTACHMENT_ID)) {
            scopeId = getBuiltInAttachmentId(gunItem, AttachmentType.SCOPE);
            builtin = true;
        }
        if (!DefaultAssets.isEmptyAttachmentId(scopeId)) {
            CompoundTag attachmentTag = this.getAttachmentTag(gunItem, AttachmentType.SCOPE);
            int zoomNumber = builtin ? 0 : AttachmentItemDataAccessor.getZoomNumberFromTag(attachmentTag);
            float[] zooms = TimelessAPI.getClientAttachmentIndex(scopeId).map(ClientAttachmentIndex::getZoom).orElse(null);
            if (zooms != null) {
                zoom = zooms[zoomNumber % zooms.length];
            }
        } else {
            zoom = TimelessAPI.getGunDisplay(gunItem).map(GunDisplayInstance::getIronZoom).orElse(1f);
        }
        return zoom;
    }

    @Override
    default boolean hasBulletInBarrel(ItemStack gun) {
        CompoundTag nbt = ItemNbtUtils.getTag(gun);
        if (nbt.contains(GUN_HAS_BULLET_IN_BARREL)) {
            return nbt.getBooleanOr(GUN_HAS_BULLET_IN_BARREL, false);
        }
        return false;
    }

    @Override
    default void setBulletInBarrel(ItemStack gun, boolean bulletInBarrel) {
        ItemNbtUtils.updateTag(gun, nbt -> nbt.putBoolean(GUN_HAS_BULLET_IN_BARREL, bulletInBarrel));
    }

    @Override
    default boolean hasCustomLaserColor(ItemStack gun) {
        CompoundTag nbt = ItemNbtUtils.getTag(gun);
        return nbt.contains(LASER_COLOR_TAG);
    }

    @Override
    default int getLaserColor(ItemStack gun) {
        CompoundTag nbt = ItemNbtUtils.getTag(gun);
        if (!hasCustomLaserColor(gun)) {
            return 0xFF0000;
        }
        return nbt.getIntOr(LASER_COLOR_TAG, 0xFF0000);
    }

    @Override
    default void setLaserColor(ItemStack gun, int color) {
        ItemNbtUtils.updateTag(gun, nbt -> nbt.putInt(LASER_COLOR_TAG, color));
    }

    /**
     * 열량 데이터
     */
    @Override
    default boolean hasHeatData(ItemStack gun) {
        return ItemNbtUtils.getTag(gun).contains(GUN_OVERHEAT_TAG);
    }

    @Override
    default boolean isOverheatLocked(ItemStack gun) {
        return ItemNbtUtils.getTag(gun).getBooleanOr(GUN_OVERHEAT_LOCK_TAG, false);
    }

    @Override
    default void setOverheatLocked(ItemStack gun, boolean locked) {
        ItemNbtUtils.updateTag(gun, nbt -> nbt.putBoolean(GUN_OVERHEAT_LOCK_TAG, locked));
    }

    @Override
    default float getHeatAmount(ItemStack gun) {
        if (hasHeatData(gun)) return ItemNbtUtils.getTag(gun).getFloatOr(GUN_OVERHEAT_TAG, 0f);
        return 0f;
    }

    @Override
    default void setHeatAmount(ItemStack gun, float amount) {
        ItemNbtUtils.updateTag(gun, nbt -> nbt.putFloat(GUN_OVERHEAT_TAG, Math.max(amount, 0f)));
    }

    @Override
    default float lerpRPM(ItemStack gun) {
        return TimelessAPI.getCommonGunIndex(getGunId(gun))
                .map(index -> index.getGunData().getHeatData())
                .map(heatData -> {
                    float heatPercentage = (getHeatAmount(gun) / heatData.getHeatMax());
                    return Mth.lerp(heatPercentage, heatData.getMinRpmMod(), heatData.getMaxRpmMod());
                }).orElse(1f);
    }

    @Override
    default float lerpInaccuracy(ItemStack gun) {
        return TimelessAPI.getCommonGunIndex(getGunId(gun))
                .map(index -> index.getGunData().getHeatData())
                .map(heatData -> {
                    float heatPercentage = (getHeatAmount(gun) / heatData.getHeatMax());
                    return Mth.lerp(heatPercentage, heatData.getMinInaccuracy(), heatData.getMaxInaccuracy());
                }).orElse(1f);
    }
}
