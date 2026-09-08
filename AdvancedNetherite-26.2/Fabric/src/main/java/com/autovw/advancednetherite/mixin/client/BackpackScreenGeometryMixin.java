package com.autovw.advancednetherite.mixin.client;

import com.autovw.advancednetherite.client.gui.BackpackScreenPosition;
import com.autovw.advancednetherite.common.backpack.BackpackInventory;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
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

@Mixin(AbstractContainerScreen.class)
public abstract class BackpackScreenGeometryMixin implements BackpackScreenPosition
{
    @Shadow @Final @Mutable protected int imageWidth;
    @Shadow protected int leftPos;
    @Shadow protected int topPos;

    @Inject(method = "<init>(Lnet/minecraft/world/inventory/AbstractContainerMenu;Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/network/chat/Component;II)V", at = @At("TAIL"))
    private void advancednetherite$width(AbstractContainerMenu menu, Inventory inventory, Component title,
                                       int width, int height, CallbackInfo ci)
    {
        if ((Object) this instanceof InventoryScreen)
        {
            this.imageWidth = BackpackInventory.PANEL_LEFT + BackpackInventory.PANEL_WIDTH;
        }
    }

    @Override
    public int advancednetherite$getLeftPos()
    {
        return this.leftPos;
    }

    @Override
    public int advancednetherite$getTopPos()
    {
        return this.topPos;
    }
}
