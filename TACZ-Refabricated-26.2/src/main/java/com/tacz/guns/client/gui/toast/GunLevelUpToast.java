package com.tacz.guns.client.gui.toast;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

@Environment(EnvType.CLIENT)
public class GunLevelUpToast implements Toast {
    private final Component title;
    private final @Nullable Component subTitle;
    private final ItemStack icon;
    private long visibleTime = -1;

    public GunLevelUpToast(ItemStack icon, Component titleComponent, @Nullable Component subtitle) {
        this.icon = icon.copy();
        this.title = titleComponent;
        this.subTitle = subtitle;
    }

    @Override
    public Visibility getWantedVisibility() {
        if (this.visibleTime < 0) {
            return Visibility.SHOW;
        }
        if (System.currentTimeMillis() - this.visibleTime >= 5000L) {
            return Visibility.HIDE;
        }
        return Visibility.SHOW;
    }

    @Override
    public int width() {
        Font font = Minecraft.getInstance().font;
        int textWidth = font.width(this.title);
        if (this.subTitle != null) {
            textWidth = Math.max(textWidth, font.width(this.subTitle));
        }
        return Math.max(160, textWidth + 38);
    }

    @Override
    public void update(ToastManager toastManager, long timeSinceLastVisible) {
        if (this.visibleTime < 0) {
            this.visibleTime = System.currentTimeMillis();
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor gui, Font font, long timeSinceLastVisible) {
        gui.fill(0, 0, width(), height(), 0xDD1F1F1F);
        gui.outline(0, 0, width(), height(), 0xFFFFCC55);
        if (!icon.isEmpty()) {
            gui.item(icon, 8, 8);
        }
        int titleY = 12;
        if (subTitle != null) {
            titleY = 7;
        }
        gui.text(font, title, 30, titleY, 0xFFFFCC55, false);
        if (subTitle != null) {
            gui.text(font, subTitle, 30, 19, 0xFFFFFFFF, false);
        }
    }
}
