package com.daqem.itemrestrictions.mixin.client;

import com.daqem.itemrestrictions.ItemRestrictions;
import com.daqem.itemrestrictions.client.screen.ItemRestrictionsScreen;
import com.daqem.itemrestrictions.data.RestrictionType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class MixinScreen extends AbstractContainerEventHandler implements Renderable, ItemRestrictionsScreen {

    @Shadow
    public abstract Font getFont();
    @Shadow
    public int width;
    @Shadow
    public int height;
    @Unique
    private RestrictionType itemrestrictions$restrictionType;

    @Override
    public void itemrestrictions$cantCraft(RestrictionType restrictionType) {
        this.itemrestrictions$restrictionType = restrictionType;
    }

    @Inject(at = @At("HEAD"), method = "render")
    private void render(GuiGraphics guiGraphics, int i, int j, float f, CallbackInfo ci) {
        if (itemrestrictions$restrictionType != null && itemrestrictions$restrictionType != RestrictionType.NONE) {
            renderCantCraftMessage(guiGraphics, getFont(), width, height, 166, itemrestrictions$restrictionType);
        }
    }
}
