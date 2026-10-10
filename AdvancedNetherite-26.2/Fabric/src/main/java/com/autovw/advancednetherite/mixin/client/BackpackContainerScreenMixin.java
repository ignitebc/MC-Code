package com.autovw.advancednetherite.mixin.client;

import com.autovw.advancednetherite.client.BackpackPanelRenderer;
import com.autovw.advancednetherite.common.backpack.BackpackInventory;
import com.autovw.advancednetherite.common.backpack.BackpackPanel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractFurnaceScreen;
import net.minecraft.client.gui.screens.inventory.BrewingStandScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.inventory.DispenserScreen;
import net.minecraft.client.gui.screens.inventory.HopperScreen;
import net.minecraft.client.gui.screens.inventory.ShulkerBoxScreen;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * BackpackPanelMenuMixin이 패널 칸을 붙인 메뉴의 화면에 E키 화면과 같은 가방 패널을 그린다.
 *
 * <p>화면 폭은 BackpackScreenGeometryMixin이 패널만큼 늘린다. 이 화면들은 바닐라 배경 그림을
 * 화면 폭 그대로 그리므로, 늘어난 폭으로 그리면 그림 오른쪽의 빈 영역까지 찍힌다.</p>
 */
@Mixin({ContainerScreen.class, ShulkerBoxScreen.class, CraftingScreen.class, AbstractFurnaceScreen.class,
        HopperScreen.class, DispenserScreen.class, BrewingStandScreen.class})
public abstract class BackpackContainerScreenMixin
{
    @ModifyArg(method = "extractBackground", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;blit(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/Identifier;IIFFIIII)V"), index = 6)
    private int advancednetherite$vanillaTextureWidth(int width)
    {
        if (!BackpackPanel.hasPanel(advancednetherite$menu()))
        {
            return width;
        }
        return Math.min(width, BackpackInventory.PANEL_LEFT);
    }

    @Inject(method = "extractBackground", at = @At("TAIL"))
    private void advancednetherite$panel(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, CallbackInfo ci)
    {
        AbstractContainerMenu menu = advancednetherite$menu();
        if (!BackpackPanel.hasPanel(menu))
        {
            return;
        }
        BackpackScreenAccessor screen = (BackpackScreenAccessor) this;
        BackpackPanelRenderer.extract(graphics, Minecraft.getInstance().font, menu,
                screen.advancednetherite$getLeftPos(), screen.advancednetherite$getTopPos());
    }

    @Unique
    private AbstractContainerMenu advancednetherite$menu()
    {
        return ((AbstractContainerScreen<?>) (Object) this).getMenu();
    }
}
