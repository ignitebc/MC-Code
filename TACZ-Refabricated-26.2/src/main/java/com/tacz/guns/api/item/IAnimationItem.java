package com.tacz.guns.api.item;

import net.minecraft.world.item.ItemStack;

public interface IAnimationItem {
    /**
     * 상태 기계나 속성을 다시 초기화해야 하는 아이템인지 돌려준다
     *
     * @param stack1 아이템1
     * @param stack2 아이템2
     * @return 다시 초기화해야 하는지
     */
    boolean isSame(ItemStack stack1, ItemStack stack2);

    static boolean matchesIgnoreCount(ItemStack pStack, ItemStack pOther) {
        if (pStack == pOther) {
            return true;
        } else {
            return ItemStack.isSameItemSameComponents(pStack, pOther);
        }
    }
}
