package com.daqem.itemrestrictions.client.screen;

import com.daqem.itemrestrictions.ItemRestrictions;
import com.daqem.itemrestrictions.data.RestrictionType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.MutableComponent;

public interface ItemRestrictionsScreen {

    void itemrestrictions$cantCraft(RestrictionType restrictionType);

    default void renderCantCraftMessage(GuiGraphicsExtractor guiGraphics, Font font, int width, int height, int imageHeight, RestrictionType restrictionType) {
        MutableComponent component = ItemRestrictions.translatable(restrictionType.getTranslationKey()).withStyle(ChatFormatting.RED);
        guiGraphics.text(font, component, (int) ((width / 2F) - (font.width(component) / 2F)), (int) ((height - imageHeight) / 4F), 0xFFFFFFFF);
    }
}
