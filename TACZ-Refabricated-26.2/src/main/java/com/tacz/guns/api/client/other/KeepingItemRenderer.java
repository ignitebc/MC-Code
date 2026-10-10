package com.tacz.guns.api.client.other;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

/**
 * 아이템을 집어넣을 때 잠시 동안 계속 그리도록 하는 인터페이스
 */
public interface KeepingItemRenderer {
    /**
     * 아이템을 계속 그릴 시간
     *
     * @param itemStack 계속 그릴 아이템
     * @param timeMs    시간(밀리초)
     */
    void keep(ItemStack itemStack, long timeMs);

    /**
     * 현재 주 손에서 그리고 있는 아이템을 얻는다
     */
    ItemStack getCurrentItem();

    /**
     * ItemInHandRenderer가 Mixin으로 이 인터페이스를 구현한다.
     *
     * @return ItemInHandRenderer 인스턴스
     */
    static KeepingItemRenderer getRenderer() {
        return (KeepingItemRenderer) Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer();
    }
}
