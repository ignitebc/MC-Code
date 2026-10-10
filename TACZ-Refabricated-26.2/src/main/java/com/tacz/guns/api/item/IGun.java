package com.tacz.guns.api.item;

import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.GunProperty;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * 총기 로직은 없고 총기 NBT 접근만 담는다.<br>
 * 총기 로직은 {@link AbstractGunItem}에서 볼 수 있다
 */
public interface IGun {
    /**
     * @return 아이템 종류가 IGun이면 명시적으로 형변환한 인스턴스, 아니면 null
     */
    @Nullable
    static IGun getIGunOrNull(@Nullable ItemStack stack) {
        if (stack == null) {
            return null;
        }
        if (stack.getItem() instanceof IGun iGun) {
            return iGun;
        }
        return null;
    }

    /**
     * 주 손에 총을 들고 있는지
     */
    @Deprecated
    static boolean mainhandHoldGun(LivingEntity livingEntity) {
        return livingEntity.getMainHandItem().getItem() instanceof IGun;
    }

    /**
     * 주 손에 총을 들고 있는지
     */
    static boolean mainHandHoldGun(LivingEntity livingEntity) {
        return livingEntity.getMainHandItem().getItem() instanceof IGun;
    }

    /**
     * 주 손 총기의 발사 모드를 얻는다
     */
    @Deprecated
    static FireMode getMainhandFireMode(LivingEntity livingEntity) {
        ItemStack mainHandItem = livingEntity.getMainHandItem();
        if (mainHandItem.getItem() instanceof IGun iGun) {
            return iGun.getFireMode(mainHandItem);
        }
        return FireMode.UNKNOWN;
    }

    /**
     * 주 손 총기의 발사 모드를 얻는다
     */
    static FireMode getMainHandFireMode(LivingEntity livingEntity) {
        ItemStack mainHandItem = livingEntity.getMainHandItem();
        if (mainHandItem.getItem() instanceof IGun iGun) {
            return iGun.getFireMode(mainHandItem);
        }
        return FireMode.UNKNOWN;
    }

    /**
     * 조준 확대 배율을 얻는다
     */
    float getAimingZoom(ItemStack gunItem);

    /**
     * 재장전할 때 인벤토리의 실제 탄약 대신 "가상 예비 탄약"을 쓰는지
     */
    boolean useDummyAmmo(ItemStack gun);

    /**
     * 총기의 현재 "가상 예비 탄약" 수를 얻는다
     */
    int getDummyAmmoAmount(ItemStack gun);

    /**
     * 총기의 현재 "가상 예비 탄약" 수를 설정한다
     */
    void setDummyAmmoAmount(ItemStack gun, int amount);

    /**
     * 총기의 현재 "가상 예비 탄약" 수를 늘린다
     */
    void addDummyAmmoAmount(ItemStack gun, int amount);

    /**
     * "가상 예비 탄약" 최대 수가 설정되어 있는지 확인한다
     */
    boolean hasMaxDummyAmmo(ItemStack gun);

    /**
     * 총기의 현재 "가상 예비 탄약" 최대 수를 얻는다
     */
    int getMaxDummyAmmoAmount(ItemStack gun);

    /**
     * 총기의 현재 "가상 예비 탄약" 최대 수를 설정한다
     */
    void setMaxDummyAmmoAmount(ItemStack gun, int amount);

    /**
     * 총기의 "부착물 잠금" 상태를 얻는다
     */
    boolean hasAttachmentLock(ItemStack gun);

    /**
     * 총기의 "부착물 잠금"을 설정한다
     */
    void setAttachmentLock(ItemStack gun, boolean locked);

    /**
     * 총기 ID를 얻는다
     */
    @NotNull
    Identifier getGunId(ItemStack gun);

    /**
     * 총기 ID를 설정한다
     */
    void setGunId(ItemStack gun, @Nullable Identifier gunId);

    /**
     * 총기 클라이언트 표시 ID를 얻는다. 기본 스킨이면 {@link DefaultAssets#DEFAULT_GUN_DISPLAY_ID}를 돌려준다<br/>
     * 올바른 클라이언트 표시는 {@link com.tacz.guns.api.TimelessAPI#getGunDisplay(ItemStack)}로 얻어야 한다
     */
    @NotNull
    Identifier getGunDisplayId(ItemStack gun);

    /**
     * 총기 클라이언트 표시 ID를 설정한다
     */
    void setGunDisplayId(ItemStack gun, @Nullable Identifier displayId);

    /**
     * 입력한 경험치에 해당하는 레벨을 얻는다.
     *
     * @param exp 경험치
     * @return 해당 레벨
     */
    int getLevel(int exp);

    /**
     * 입력한 레벨에 필요한 최소 경험치를 얻는다.
     *
     * @param level 레벨
     * @return 최소로 필요한 경험치
     */
    int getExp(int level);

    /**
     * 허용하는 최대 레벨을 돌려준다.
     *
     * @return 최대 레벨
     */
    int getMaxLevel();

    /**
     * 총기의 현재 레벨을 얻는다
     */
    int getLevel(ItemStack gun);

    /**
     * 쌓인 전체 경험치를 얻는다.
     *
     * @param gun 입력 아이템
     * @return 전체 경험치
     */
    int getExp(ItemStack gun);

    /**
     * 다음 레벨까지 필요한 경험치를 얻는다.
     *
     * @param gun 입력 아이템
     * @return 다음 레벨까지 필요한 경험치. 이미 최대 레벨이면 0
     */
    int getExpToNextLevel(ItemStack gun);

    /**
     * 현재 레벨에서 쌓인 경험치를 얻는다.
     *
     * @param gun 입력 아이템
     * @return 현재 레벨에서 쌓인 경험치
     */
    int getExpCurrentLevel(ItemStack gun);

    /**
     * 발사 모드를 얻는다
     *
     * @param gun 총
     * @return 발사 모드
     */
    FireMode getFireMode(ItemStack gun);

    /**
     * 발사 모드를 설정한다
     */
    void setFireMode(ItemStack gun, @Nullable FireMode fireMode);

    /**
     * 현재 총기 탄약 수를 얻는다
     */
    int getCurrentAmmoCount(ItemStack gun);

    /**
     * 현재 총기 탄약 수를 설정한다
     */
    void setCurrentAmmoCount(ItemStack gun, int ammoCount);

    /**
     * 현재 총기 탄약 수를 하나 줄인다
     */
    void reduceCurrentAmmoCount(ItemStack gun);

    /**
     * 총기 속성을 동적으로 바꾼다.
     * 주의: 일부 복잡한 속성은 {@code GunProperty}의 타입과 값의 타입이 다를 수 있다.
     * 예를 들어 피해량이나 정확도 같은 복잡한 속성은 GunProperty 타입이 복잡한 자료 구조지만, 넣고 돌려받는 값은 단순한 실수다.
     *
     * @param dataHolder 상태 데이터
     * @param gunItem    총기 아이템
     * @param shooter    사격자
     * @param id         속성 ID. {@link com.tacz.guns.api.GunProperties} 참고
     * @param type       속성의 데이터 타입
     * @param original   속성의 원래 값
     * @param <T>        속성의 데이터 타입
     * @return 스크립트나 하위 클래스가 바꾼 속성
     * @author ChloePrime
     * @since 1.1.7
     */
    default <T> T modifyProperty(ShooterDataHolder dataHolder, ItemStack gunItem, LivingEntity shooter,
                                 GunProperty<?> id, Class<T> type, T original) {
        return modifyProperty(dataHolder, gunItem, shooter, id.name(), type, original);
    }

    /**
     * 총기 속성을 동적으로 바꾼다
     *
     * @param dataHolder 상태 데이터
     * @param gunItem    총기 아이템
     * @param shooter    사격자
     * @param id         속성 ID. {@link com.tacz.guns.api.GunProperties} 참고
     * @param type       속성의 데이터 타입
     * @param original   속성의 원래 값
     * @param <T>        속성의 데이터 타입
     * @return 스크립트나 하위 클래스가 바꾼 속성
     * @author ChloePrime
     * @since 1.1.7
     */
    default <T> T modifyProperty(ShooterDataHolder dataHolder, ItemStack gunItem, LivingEntity shooter,
                                 String id, Class<T> type, T original) {
        return modifyProperty(dataHolder, gunItem, shooter, "modify_property", id, type, original);
    }

    /**
     * 총기 속성을 동적으로 바꾼다.
     * 바꿀 때 쓸 lua 함수 이름을 지정할 수 있다
     *
     * @param dataHolder    상태 데이터
     * @param gunItem       총기 아이템
     * @param shooter       사격자
     * @param luaMethodName 속성을 바꿀 lua 함수 이름
     * @param id            속성 ID. {@link com.tacz.guns.api.GunProperties} 참고
     * @param type          속성의 데이터 타입
     * @param original      속성의 원래 값
     * @param <T>           속성의 데이터 타입
     * @return 스크립트나 하위 클래스가 바꾼 속성
     * @author ChloePrime
     * @since 1.1.7
     */
    default <T> T modifyProperty(ShooterDataHolder dataHolder, ItemStack gunItem, LivingEntity shooter,
                                 String luaMethodName, String id, Class<T> type, T original) {
        return original;
    }

    /**
     * 총 안의 탄을 모두 뺀다. 플레이어 전용 메서드로, 기본적으로 탄약을 뺄 때 쓴다
     */
    void dropAllAmmo(Player player, ItemStack gun);

    /**
     * 현재 총기에서 지정한 종류의 부착물을 얻는다
     */
    @Nonnull
    ItemStack getAttachment(ItemStack gun, AttachmentType type);

    @Nonnull
    ItemStack getBuiltinAttachment(ItemStack gun, AttachmentType type);

    /**
     * 현재 총기에서 지정한 종류의 부착물 NBT 데이터를 얻는다
     *
     * @return 비어 있으면 부착물 데이터가 없다
     */
    @Nullable
    CompoundTag getAttachmentTag(ItemStack gun, AttachmentType type);

    /**
     * 장착한 부착물 자신의 custom_data 태그를 다시 쓴다.
     *
     * <p>18차: 인터페이스에 이 선언을 다시 넣었다(원본 IGun 314번째 줄에 있었는데 이식할 때 빠졌다).
     * 구현은 {@code GunItemDataAccessor#setAttachmentTag}(16차에 복구)에 있다.
     * 주로 배율 가변 조준경의 배율을 바꾼 뒤 ZoomNumber를 저장하는 데 쓴다.
     */
    void setAttachmentTag(ItemStack gun, AttachmentType type, CompoundTag attachmentTag);

    @Nonnull
    Identifier getBuiltInAttachmentId(ItemStack gun, AttachmentType type);

    /**
     * 총기의 부착물 ID를 얻는다
     * <p>
     * 없으면 {@link DefaultAssets#EMPTY_ATTACHMENT_ID}를 돌려준다
     */
    @Nonnull
    Identifier getAttachmentId(ItemStack gun, AttachmentType type);

    /**
     * 부착물을 장착한다
     */
    void installAttachment(@Nonnull ItemStack gun, @Nonnull ItemStack attachment);

    /**
     * 부착물을 뗀다
     */
    void unloadAttachment(@Nonnull ItemStack gun, AttachmentType type);

    /**
     * 이 총기에 이 부착물을 장착할 수 있는지
     */
    boolean allowAttachment(ItemStack gun, ItemStack attachmentItem);

    /**
     * 이 총기에 이 종류의 부착물을 허용하는지
     */
    boolean allowAttachmentType(ItemStack gun, AttachmentType type);

    /**
     * 약실에 탄이 있는지. 클로즈드 볼트 방식 총기에 쓴다
     */
    boolean hasBulletInBarrel(ItemStack gun);

    /**
     * 약실의 탄 유무를 설정한다. 클로즈드 볼트 방식 총기에 쓴다
     */
    void setBulletInBarrel(ItemStack gun, boolean bulletInBarrel);

    /**
     * 인벤토리 탄약을 바로 쓰는 총기인지
     */
    boolean useInventoryAmmo(ItemStack gun);

    /**
     * 예비 탄약이 있는지 얻는다(인벤토리 급탄 방식에만 쓴다)
     */
    boolean hasInventoryAmmo(LivingEntity shooter, ItemStack gun, boolean needCheckAmmo);

    /**
     * RPM을 얻는다
     */
    int getRPM(ItemStack gun);

    /**
     * 엎드릴 수 있는지 얻는다
     */
    boolean isCanCrawl(ItemStack gun);

    boolean hasCustomLaserColor(ItemStack gun);

    int getLaserColor(ItemStack gun);

    void setLaserColor(ItemStack gun, int color);

    /**
     * 열량 데이터
     */
    boolean hasHeatData(ItemStack gun);

    /**
     * 완전히 과열되었는지
     */
    boolean isOverheatLocked(ItemStack gun);

    void setOverheatLocked(ItemStack gun, boolean locked);

    /**
     * 현재 과열 값을 설정한다
     */
    void setHeatAmount(ItemStack gun, float amount);

    float lerpRPM(ItemStack gun);

    float lerpInaccuracy(ItemStack gun);

    float getHeatAmount(ItemStack gun);
}