package com.autovw.advancednetherite.mixin.client;

import com.autovw.advancednetherite.common.backpack.BackpackInventory;
import com.autovw.advancednetherite.common.backpack.BackpackPanel;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class BackpackScreenGeometryMixin
{
    @Shadow @Final @Mutable protected int imageWidth;

    @Inject(method = "<init>(Lnet/minecraft/world/inventory/AbstractContainerMenu;Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/network/chat/Component;II)V", at = @At("TAIL"))
    private void advancednetherite$width(AbstractContainerMenu menu, Inventory inventory, Component title,
                                       int width, int height, CallbackInfo ci)
    {
        if (BackpackPanel.hasPanel(menu))
        {
            this.imageWidth = BackpackInventory.PANEL_LEFT + BackpackInventory.PANEL_WIDTH;
        }
    }

    /**
     * 호퍼처럼 바닐라 화면이 패널보다 낮으면 패널 위쪽이 화면 영역 밖으로 나온다.
     * 그 부분을 누르면 들고 있던 물건을 버리는 바깥 클릭으로 처리되므로 패널 안쪽은 화면 안으로 본다.
     */
    @Inject(method = "hasClickedOutside", at = @At("RETURN"), cancellable = true)
    private void advancednetherite$panelClick(double mouseX, double mouseY, int left, int top,
                                             CallbackInfoReturnable<Boolean> cir)
    {
        if (!cir.getReturnValue())
        {
            return;
        }
        AbstractContainerMenu menu = ((AbstractContainerScreen<?>) (Object) this).getMenu();
        if (BackpackPanel.hasPanel(menu) && BackpackPanel.contains(menu, mouseX - left, mouseY - top))
        {
            cir.setReturnValue(false);
        }
    }
}
