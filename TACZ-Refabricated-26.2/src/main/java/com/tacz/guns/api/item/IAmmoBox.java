package com.tacz.guns.api.item;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * 탄약 상자 인터페이스
 */
public interface IAmmoBox {
    /**
     * 탄약 상자 안 탄약의 ID를 얻는다
     *
     * @param ammoBox 탄약 상자
     * @return 탄약 상자 안 탄약의 ID
     */
    Identifier getAmmoId(ItemStack ammoBox);

    /**
     * 탄약 상자 안 탄약 수를 얻는다
     *
     * @param ammoBox 탄약 상자
     * @return 탄약 수
     */
    int getAmmoCount(ItemStack ammoBox);

    /**
     * 탄약 상자 안 탄약의 ID를 설정한다
     */
    void setAmmoId(ItemStack ammoBox, Identifier ammoId);

    /**
     * 탄약 상자 안 탄약 수를 설정한다
     */
    void setAmmoCount(ItemStack ammoBox, int count);

    /**
     * 탄약 상자 안 탄약이 이 총의 것인지
     *
     * @param gun     총
     * @param ammoBox 탄약 상자
     * @return 이 총의 것인지
     */
    boolean isAmmoBoxOfGun(ItemStack gun, ItemStack ammoBox);

    /**
     * 탄약 상자의 등급을 설정한다
     *
     * @param ammoBox   탄약 상자
     * @param ammoLevel 탄약 상자 등급
     * @return 바뀐 탄약 상자
     */
    ItemStack setAmmoLevel(ItemStack ammoBox, int ammoLevel);

    /**
     * 탄약 상자의 등급을 얻는다
     *
     * @param ammoBox 탄약 상자
     * @return 등급. 0부터 시작한다
     */
    int getAmmoLevel(ItemStack ammoBox);

}
