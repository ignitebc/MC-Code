package com.tacz.guns.api.item;

import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public interface IBlock {
    /**
     * 블록 ID를 얻는다
     *
     * @param block 입력 아이템
     * @return 블록 ID
     */
    Identifier getBlockId(ItemStack block);

    /**
     * 블록 ID를 설정한다
     */
    void setBlockId(ItemStack block, @Nullable Identifier blockId);
}
