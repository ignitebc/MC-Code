package com.tacz.guns.entity.shooter;

import com.tacz.guns.util.ItemNbtUtils;
import net.minecraft.world.item.ItemStack;

/** 몬스터 총기의 실제 발사량을 제한한다. 점사의 각 탄약도 한 발씩 소비한다. */
public final class MonsterGunAmmo {
    public static final int MAGAZINE_COUNT = 3;
    private static final String REMAINING_AMMO_TAG = "MonsterGunRemainingAmmo";
    private static final String AMMO_VERSION_TAG = "MonsterGunAmmoVersion";
    private static final int AMMO_VERSION = 1;

    private MonsterGunAmmo() {
    }

    /** 총기에 기록하므로 무기를 다시 꺼내거나 청크를 로드해도 소모한 탄약이 복구되지 않는다. */
    public static void initialize(ItemStack stack, int magazineCapacity) {
        var tag = ItemNbtUtils.getTag(stack);
        if (tag.contains(REMAINING_AMMO_TAG)) {
            if (tag.getIntOr(AMMO_VERSION_TAG, 0) >= AMMO_VERSION) return;
            // 버전이 없는 잔탄은 기존 4탄창 예산이다. 이미 쏜 탄약을 보존하며 한 번만 줄인다.
            int removedAmmo = totalAmmo(magazineCapacity, 4) - totalAmmo(magazineCapacity, MAGAZINE_COUNT);
            int remainingAmmo = Math.max(0, tag.getIntOr(REMAINING_AMMO_TAG, 0));
            int migratedAmmo = Math.max(0, remainingAmmo - removedAmmo);
            ItemNbtUtils.updateTag(stack, nbt -> {
                nbt.putInt(REMAINING_AMMO_TAG, migratedAmmo);
                nbt.putInt(AMMO_VERSION_TAG, AMMO_VERSION);
            });
            return;
        }
        ItemNbtUtils.updateTag(stack, nbt -> {
            nbt.putInt(REMAINING_AMMO_TAG, totalAmmo(magazineCapacity, MAGAZINE_COUNT));
            nbt.putInt(AMMO_VERSION_TAG, AMMO_VERSION);
        });
    }

    private static int totalAmmo(int magazineCapacity, int magazines) {
        return (int) Math.min(Integer.MAX_VALUE, (long) Math.max(0, magazineCapacity) * magazines);
    }

    public static boolean hasAmmo(ItemStack stack) {
        return ItemNbtUtils.getTag(stack).getIntOr(REMAINING_AMMO_TAG, 0) > 0;
    }

    /** 탄약 감소에 성공한 뒤, 탄환을 생성하기 전에 호출한다. 산탄의 파편 수는 세지 않는다. */
    public static boolean consumeShot(ItemStack stack) {
        int remainingAmmo = ItemNbtUtils.getTag(stack).getIntOr(REMAINING_AMMO_TAG, 0);
        if (remainingAmmo <= 0) {
            return false;
        }
        ItemNbtUtils.updateTag(stack, nbt -> nbt.putInt(REMAINING_AMMO_TAG, remainingAmmo - 1));
        return true;
    }
}
