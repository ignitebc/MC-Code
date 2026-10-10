package com.tacz.guns.client.gui.components.refit;

import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public interface IStackTooltip {
    /**
     * 이 인터페이스를 붙이면 이 메서드로 안내 문구를 그린다
     *
     * @param consumer 안내 문구를 그릴 아이템
     */
    void renderTooltip(Consumer<ItemStack> consumer);
}
