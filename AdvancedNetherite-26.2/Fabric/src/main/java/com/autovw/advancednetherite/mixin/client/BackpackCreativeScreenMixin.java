package com.autovw.advancednetherite.mixin.client;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * 크리에이티브의 별도 슬롯 패킷은 바닐라 0~45만 허용한다.
 * 생존용 추가 슬롯이 핫바와 겹치거나 휴지통에서 클라이언트만 삭제되는 것을 막는다.
 */
@Mixin(CreativeModeInventoryScreen.class)
public abstract class BackpackCreativeScreenMixin
{
    @Redirect(method = {"selectTab", "slotClicked"}, at = @At(value = "INVOKE",
            target = "Lnet/minecraft/core/NonNullList;size()I"))
    private int advancednetherite$vanillaSlots(NonNullList<?> slots)
    {
        return Math.min(46, slots.size());
    }
}
