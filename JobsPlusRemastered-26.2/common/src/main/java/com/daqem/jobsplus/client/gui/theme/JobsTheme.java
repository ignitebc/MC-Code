package com.daqem.jobsplus.client.gui.theme;

import com.daqem.jobsplus.JobsPlus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** PNG-backed JobsPlus skin; sprite metadata preserves borders at different GUI sizes. */
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

    public enum Skin {
        PANEL, INSET, TOOLTIP, HEADER,
        SECONDARY, SECONDARY_HOVER, PRIMARY, PRIMARY_HOVER,
        DISABLED, TAB, TAB_HOVER, TAB_SELECTED,
        SLOT, SLOT_ACTIVE, SLOT_INACTIVE, SLOT_LOCKED;

        private final Identifier id = JobsPlus.getId("theme/" + name().toLowerCase(java.util.Locale.ROOT));
    }

    public static void texture(GuiGraphicsExtractor g, Skin skin, int x, int y, int width, int height) {
        if (width > 0 && height > 0) {
            g.blitSprite(RenderPipelines.GUI_TEXTURED, skin.id, x, y, width, height);
        }
    }

    public static void panel(GuiGraphicsExtractor g, int x, int y, int width, int height) {
        texture(g, Skin.PANEL, x, y, width, height);
    }

    public static void cutBox(GuiGraphicsExtractor g, int x, int y, int width, int height,
                              int fill, int border) {
        Skin skin = Skin.INSET;
        if (border == SUCCESS) {
            skin = Skin.SLOT_ACTIVE;
        } else if (border == WARNING) {
            skin = Skin.SLOT_INACTIVE;
        } else if (border == DISABLED) {
            skin = Skin.SLOT_LOCKED;
        } else if (border == CYAN) {
            skin = fill == BACKGROUND ? Skin.TOOLTIP : Skin.SECONDARY_HOVER;
        }
        if (border == ERROR) {
            // Tint a neutral PNG so the close/error state also uses an image frame.
            if (width > 0 && height > 0) {
                g.blitSprite(RenderPipelines.GUI_TEXTURED, Skin.DISABLED.id, x, y, width, height,
                        fill == 0xFF823B49 ? 0xFFFF8E9B : 0xFFDB6575);
            }
        } else {
            texture(g, skin, x, y, width, height);
        }
    }

    public static void button(GuiGraphicsExtractor g, int x, int y, int width, int height,
                              boolean active, boolean hovered, boolean selected, boolean primary) {
        Skin skin;
        if (!active) {
            skin = Skin.DISABLED;
        } else if (selected || primary) {
            skin = hovered ? Skin.PRIMARY_HOVER : Skin.PRIMARY;
        } else {
            skin = hovered ? Skin.SECONDARY_HOVER : Skin.SECONDARY;
        }
        texture(g, skin, x, y, width, height);
    }

    public static void tab(GuiGraphicsExtractor g, int x, int y, int width, int height,
                           boolean hovered, boolean selected) {
        Skin skin = Skin.TAB;
        if (selected) {
            skin = Skin.TAB_SELECTED;
        } else if (hovered) {
            skin = Skin.TAB_HOVER;
        }
        texture(g, skin, x, y, width, height);
    }

    public static void inputFrame(GuiGraphicsExtractor g, int x, int y, int width, int height, int color) {
        if (width < 2 || height < 2) {
            return;
        }
        // Crop only the PNG border; leave native text, selection and caret unobscured.
        Identifier id = Skin.INSET.id;
        g.blitSprite(RenderPipelines.GUI_TEXTURED, id, width, height, 0, 0, x, y, width, 1, color);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, id, width, height, 0, height - 1,
                x, y + height - 1, width, 1, color);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, id, width, height, 0, 0, x, y, 1, height, color);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, id, width, height, width - 1, 0,
                x + width - 1, y, 1, height, color);
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
            texture(g, Skin.PRIMARY, x + 1, y + 1, filled, height - 2);
        }
    }

    public static void sprite(GuiGraphicsExtractor g, Identifier sprite, int x, int y, int width, int height) {
        String path = sprite.getPath();
        if (path.equals("jobs/coins")) {
            g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y, width, height);
        } else if (path.contains("separator") || path.equals("powerups/line")) {
            g.fill(x, y + height / 2, x + width, y + height / 2 + 1, DIVIDER);
        } else if (path.contains("slot")) {
            Skin skin = Skin.SLOT;
            if (path.endsWith("slot_active")) {
                skin = Skin.SLOT_ACTIVE;
            } else if (path.endsWith("slot_not_owned")) {
                skin = Skin.TOOLTIP;
            } else if (path.endsWith("slot_locked")) {
                skin = Skin.SLOT_LOCKED;
            } else if (path.endsWith("slot_inactive")) {
                skin = Skin.SLOT_INACTIVE;
            }
            texture(g, skin, x, y, width, height);
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
