package com.autovw.advancednetherite.mixin.client;

import com.autovw.advancednetherite.client.gui.BackpackScreenPosition;
import com.autovw.advancednetherite.client.gui.PetPanelLayout;
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
    @Shadow @Final @Mutable protected int imageHeight;
    @Shadow protected int leftPos;
    @Shadow protected int topPos;

    @Inject(method = "<init>(Lnet/minecraft/world/inventory/AbstractContainerMenu;Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/network/chat/Component;II)V", at = @At("TAIL"))
    private void advancednetherite$width(AbstractContainerMenu menu, Inventory inventory, Component title,
                                       int width, int height, CallbackInfo ci)
    {
        if ((Object) this instanceof InventoryScreen)
        {
            this.imageWidth = BackpackInventory.PANEL_LEFT + BackpackInventory.PANEL_WIDTH;
            // 펫 줄만큼 화면이 높다고 알려 두면 바닐라가 인벤토리를 위로 올려 다시 가운데 맞춘다.
            // inventoryLabelY는 이 위에서 원래 높이로 이미 계산되었으므로 글자는 제자리에 남는다.
            this.imageHeight = PetPanelLayout.INVENTORY_HEIGHT + PetPanelLayout.extraHeight();
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
