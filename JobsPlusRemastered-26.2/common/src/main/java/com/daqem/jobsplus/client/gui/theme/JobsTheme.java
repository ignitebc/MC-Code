package com.daqem.jobsplus.client.gui.theme;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** Shared, resolution-independent drawing for JobsPlus screens only. */
public final class JobsTheme {
    public static final int BACKGROUND = 0xF208191F;
    public static final int PANEL = 0xF510272F;
    public static final int INSET = 0xF5081D25;
    public static final int BORDER = 0xFF536C74;
    public static final int DIVIDER = 0xFF294651;
    public static final int TEXT = 0xFFE8F4F6;
    public static final int MUTED = 0xFF93ADB6;
    public static final int DISABLED = 0xFF637880;
    public static final int CYAN = 0xFF00C7E8;
    public static final int PRIMARY = 0xFF08A9EF;
    public static final int SELECTED = 0xD0124657;
    public static final int SUCCESS = 0xFF4DCC85;
    public static final int WARNING = 0xFFE2AE4C;
    public static final int ERROR = 0xFFD65D68;
    public static final int BUTTON_HEIGHT = 14;
    public static final int TAB_HEIGHT = 16;
    public static final float LABEL_SCALE = 0.85f;

    private JobsTheme() {
    }

    public static void panel(GuiGraphicsExtractor g, int x, int y, int width, int height) {
        cutBox(g, x, y, width, height, BACKGROUND, BORDER);
        // Restrained facets stay behind content and need no image scaling or atlas padding.
        int facet = Math.min(26, Math.min(width / 3, height / 3));
        for (int i = 0; i < facet; i++) {
            g.fill(x + width - facet * 2 + i, y + 2 + i,
                    x + width - facet + i, y + 3 + i, 0x101C6478);
        }
        corners(g, x, y, width, height, TEXT);
    }

    public static void cutBox(GuiGraphicsExtractor g, int x, int y, int width, int height,
                              int fill, int border) {
        if (width < 3 || height < 3) {
            return;
        }
        g.fill(x + 1, y + 1, x + width - 1, y + height - 1, fill);
        g.fill(x + 2, y, x + width - 2, y + 1, border);
        g.fill(x + 2, y + height - 1, x + width - 2, y + height, border);
        g.fill(x, y + 2, x + 1, y + height - 2, border);
        g.fill(x + width - 1, y + 2, x + width, y + height - 2, border);
        g.fill(x + 1, y + 1, x + 2, y + 2, border);
        g.fill(x + width - 2, y + 1, x + width - 1, y + 2, border);
        g.fill(x + 1, y + height - 2, x + 2, y + height - 1, border);
        g.fill(x + width - 2, y + height - 2, x + width - 1, y + height - 1, border);
    }

    public static void corners(GuiGraphicsExtractor g, int x, int y, int width, int height, int color) {
        int length = Math.min(4, Math.min(width, height) / 3);
        if (length < 1) {
            return;
        }
        g.fill(x, y, x + length, y + 1, color);
        g.fill(x, y, x + 1, y + length, color);
        g.fill(x + width - length, y, x + width, y + 1, color);
        g.fill(x + width - 1, y, x + width, y + length, color);
        g.fill(x, y + height - 1, x + length, y + height, color);
        g.fill(x, y + height - length, x + 1, y + height, color);
        g.fill(x + width - length, y + height - 1, x + width, y + height, color);
        g.fill(x + width - 1, y + height - length, x + width, y + height, color);
    }

    public static void button(GuiGraphicsExtractor g, int x, int y, int width, int height,
                              boolean active, boolean hovered, boolean selected, boolean primary) {
        int fill = PANEL;
        int edge = DIVIDER;
        if (active) {
            fill = selected || primary ? PRIMARY : INSET;
            edge = hovered || selected || primary ? CYAN : BORDER;
        }
        if (hovered && active) {
            fill = selected || primary ? 0xFF17BEEB : SELECTED;
        }
        cutBox(g, x, y, width, height, fill, edge);
        if (selected && active) {
            corners(g, x, y, width, height, CYAN);
        }
    }

    public static void tab(GuiGraphicsExtractor g, int x, int y, int width, int height,
                           boolean hovered, boolean selected) {
        int edge = selected || hovered ? CYAN : BORDER;
        int fill = PANEL;
        if (selected) {
            fill = PRIMARY;
        } else if (hovered) {
            fill = SELECTED;
        }
        // The slant is rendered in GUI coordinates, so the visual and hit box share one scale.
        int slant = Math.min(5, height / 3);
        for (int row = 0; row < height; row++) {
            int inset = slant * row / Math.max(1, height - 1);
            g.fill(x + inset, y + row, x + width - slant + inset, y + row + 1,
                    row == 0 || row == height - 1 ? edge : fill);
            g.fill(x + inset, y + row, x + inset + 1, y + row + 1, edge);
            g.fill(x + width - slant + inset - 1, y + row,
                    x + width - slant + inset, y + row + 1, edge);
        }
    }

    public static void label(GuiGraphicsExtractor g, Component text, int x, int y,
                             int width, int height, int color) {
        int textWidth = Minecraft.getInstance().font.width(text);
        float scale = Math.min(LABEL_SCALE, Math.max(1, width - 6) / (float) Math.max(1, textWidth));
        g.pose().pushMatrix();
        g.pose().translate(x + (width - textWidth * scale) / 2.0f,
                y + (height - Minecraft.getInstance().font.lineHeight * scale) / 2.0f);
        g.pose().scale(scale, scale);
        g.text(Minecraft.getInstance().font, text, 0, 0, color, false);
        g.pose().popMatrix();
    }

    public static void text(GuiGraphicsExtractor g, Component text, int x, int y, int maxWidth, int color) {
        int textWidth = Minecraft.getInstance().font.width(text);
        float scale = Math.min(LABEL_SCALE, Math.max(1, maxWidth) / (float) Math.max(1, textWidth));
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(scale, scale);
        g.text(Minecraft.getInstance().font, text, 0, 0, color, false);
        g.pose().popMatrix();
    }

    public static void progress(GuiGraphicsExtractor g, int x, int y, int width, int height, double percent) {
        cutBox(g, x, y, width, height, INSET, BORDER);
        int filled = (int) Math.round(Math.clamp(percent, 0.0, 100.0) * Math.max(0, width - 2) / 100.0);
        if (filled > 0 && height > 2) {
            g.fill(x + 1, y + 1, x + 1 + filled, y + height - 1, CYAN);
        }
    }

    public static void sprite(GuiGraphicsExtractor g, Identifier sprite, int x, int y, int width, int height) {
        String path = sprite.getPath();
        if (path.equals("jobs/coins")) {
            g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height);
        } else if (path.contains("separator") || path.equals("powerups/line")) {
            g.fill(x, y + height / 2, x + width, y + height / 2 + 1, DIVIDER);
        } else if (path.contains("slot")) {
            int edge = BORDER;
            if (path.endsWith("slot_active")) {
                edge = SUCCESS;
            } else if (path.endsWith("slot_not_owned")) {
                edge = CYAN;
            } else if (path.endsWith("slot_locked")) {
                edge = DISABLED;
            } else if (path.endsWith("slot_inactive")) {
                edge = WARNING;
            }
            cutBox(g, x, y, width, height, INSET, edge);
            corners(g, x, y, width, height, edge);
        } else if (path.equals("jobs/exp_bar")) {
            progress(g, x, y, width, height, 0);
        } else if (path.contains("pagination_arrow")) {
            button(g, x, y, width, height, true, path.contains("hovered"), false, false);
            label(g, Component.literal(path.contains("left") ? "‹" : "›"), x, y, width, height, TEXT);
        } else if (path.contains("pagination")) {
            cutBox(g, x, y, width, height, path.endsWith("inactive") ? PANEL : CYAN, BORDER);
        } else if (path.contains("button") || path.equals("jobs/tab_bottom")) {
            button(g, x, y, width, height, true, path.contains("hover"), false, false);
        } else if (path.contains("bar") || path.contains("coins_background") || path.equals("jobs/tab_coins")) {
            cutBox(g, x, y, width, height, PANEL, BORDER);
        } else {
            panel(g, x, y, width, height);
        }
    }
}
