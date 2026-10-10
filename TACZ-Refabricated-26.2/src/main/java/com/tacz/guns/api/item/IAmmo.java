package com.tacz.guns.api.item;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public interface IAmmo {
    /**
     * @return 아이템 종류가 IAttachment면 명시적으로 형변환한 인스턴스, 아니면 null
     */
    @Nullable
    static IAmmo getIAmmoOrNull(@Nullable ItemStack stack) {
        if (stack == null) {
            return null;
        }
        if (stack.getItem() instanceof IAmmo iAmmo) {
            return iAmmo;
        }
        return null;
    }

    /**
     * 탄약 ID를 얻는다
     *
     * @param ammo 입력 아이템
     * @return 탄약 ID
     */
    Identifier getAmmoId(ItemStack ammo);

    /**
     * 탄약 ID를 설정한다
     */
    void setAmmoId(ItemStack ammo, @Nullable Identifier ammoId);

    /**
     * 탄약이 이 총의 것인지
     *
     * @param gun  확인할 총기 아이템
     * @param ammo 확인할 탄약 아이템
     * @return 이 총의 것인지
     */
    boolean isAmmoOfGun(ItemStack gun, ItemStack ammo);
}
