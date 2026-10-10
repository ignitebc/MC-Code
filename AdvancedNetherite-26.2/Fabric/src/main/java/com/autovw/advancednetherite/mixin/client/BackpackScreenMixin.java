package com.autovw.advancednetherite.mixin.client;

import com.autovw.advancednetherite.client.BackpackPanelRenderer;
import com.autovw.advancednetherite.common.backpack.BackpackInventory;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class BackpackScreenMixin extends AbstractContainerScreen<InventoryMenu>
{
    protected BackpackScreenMixin(InventoryMenu menu, Inventory inventory, Component title)
    {
        super(menu, inventory, title);
    }

    @ModifyArg(method = "extractBackground", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V"), index = 6)
    private int advancednetherite$vanillaTextureWidth(int width)
    {
        return BackpackInventory.PANEL_LEFT;
    }

    @ModifyArg(method = "extractBackground", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V"), index = 7)
    private int advancednetherite$vanillaTextureHeight(int height)
    {
        // 바닐라 인벤토리 배경 높이. 가방 패널은 폭만 늘리고 높이는 그대로 둔다.
        return 166;
    }

    @Inject(method = "extractBackground", at = @At("TAIL"))
    private void advancednetherite$panel(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, CallbackInfo ci)
    {
        BackpackPanelRenderer.extract(graphics, this.font, this.menu, this.leftPos, this.topPos);
    }
}
