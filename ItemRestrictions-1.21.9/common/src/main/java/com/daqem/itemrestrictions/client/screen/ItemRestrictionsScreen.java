package com.daqem.itemrestrictions.client.screen;

import com.daqem.itemrestrictions.ItemRestrictions;
import com.daqem.itemrestrictions.data.RestrictionType;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.MutableComponent;

public interface ItemRestrictionsScreen {

    void itemrestrictions$cantCraft(RestrictionType restrictionType);

    default void renderCantCraftMessage(GuiGraphics guiGraphics, Font font, int width, int height, int imageHeight, RestrictionType restrictionType) {
        MutableComponent component = ItemRestrictions.translatable(restrictionType.getTranslationKey()).withStyle(ChatFormatting.RED);
        guiGraphics.drawString(font, component, (int) ((width / 2F) - (font.width(component) / 2F)), (int) ((height - imageHeight) / 4F), 0xFFFFFFFF);
    }
}
