package com.autovw.advancednetherite.mixin.client;

import com.autovw.advancednetherite.common.backpack.BackpackInventory;
import com.autovw.advancednetherite.common.backpack.BackpackSlot;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
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

    @Inject(method = "extractBackground", at = @At("TAIL"))
    private void advancednetherite$panel(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, CallbackInfo ci)
    {
        int left = this.leftPos + BackpackInventory.PANEL_LEFT;
        int right = left + BackpackInventory.PANEL_WIDTH;
        graphics.fill(left, this.topPos, right, this.topPos + 166, 0xFF373737);
        graphics.fill(left, this.topPos + 1, right - 1, this.topPos + 165, 0xFFC6C6C6);
        graphics.fill(left, this.topPos + 1, right - 1, this.topPos + 3, 0xFFFFFFFF);
        graphics.fill(right - 3, this.topPos + 3, right - 1, this.topPos + 165, 0xFF555555);
        graphics.text(this.font, Component.literal("가방"), left + 8, this.topPos + 8, 0xFF404040, false);

        int capacity = BackpackInventory.get(this.minecraft.player.getInventory()).capacity();
        graphics.text(this.font, Component.literal("+" + capacity + "칸"), left + 29, this.topPos + 49, 0xFF404040, false);
        graphics.text(this.font, Component.literal("추가 인벤토리"), left + 8, this.topPos + 70, 0xFF404040, false);
        for (Slot slot : this.menu.slots)
        {
            if (!(slot instanceof BackpackSlot)) continue;
            int x = this.leftPos + slot.x;
            int y = this.topPos + slot.y;
            if (slot.isActive())
            {
                graphics.fill(x - 1, y - 1, x + 17, y + 17, 0xFFFFFFFF);
                graphics.fill(x - 1, y - 1, x + 16, y + 16, 0xFF373737);
                graphics.fill(x, y, x + 16, y + 16, 0xFF8B8B8B);
            }
        }
    }
}
