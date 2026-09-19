package com.tacz.guns.client.gui.components.smith;

import com.tacz.guns.GunMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Locale;

/**
 * 총기 작업대 화면의 스킨. Jobs+ 직업 화면(J키)과 같은 PNG 스프라이트와 색을 쓴다.
 * 두 모드는 서로 의존하지 않으므로 스프라이트는 textures/gui/sprites/smith 에 사본을 둔다.
 */
public final class SmithTheme {
    public static final int DIVIDER = 0xFF294651;
    public static final int TEXT = 0xFFE8F4F6;
    public static final int MUTED = 0xFF93ADB6;
    public static final int DISABLED_TEXT = 0xFF637880;
    public static final int CYAN = 0xFF00C7E8;
    public static final int ERROR = 0xFFD65D68;
    public static final int BUTTON_HEIGHT = 14;
    public static final int TAB_HEIGHT = 16;
    public static final float LABEL_SCALE = 0.85f;

    private SmithTheme() {
    }

    public enum Skin {
        PANEL, INSET, HEADER,
        SECONDARY, SECONDARY_HOVER, PRIMARY, PRIMARY_HOVER,
        DISABLED, TAB, TAB_HOVER, TAB_SELECTED;

        private final Identifier id = Identifier.fromNamespaceAndPath(GunMod.MOD_ID,
                "smith/" + name().toLowerCase(Locale.ROOT));
    }

    public static void texture(GuiGraphicsExtractor g, Skin skin, int x, int y, int width, int height) {
        if (width > 0 && height > 0) {
            g.blitSprite(RenderPipelines.GUI_TEXTURED, skin.id, x, y, width, height);
        }
    }

    public static void button(GuiGraphicsExtractor g, int x, int y, int width, int height,
                              boolean active, boolean hovered, boolean highlighted) {
        Skin skin;
        if (!active) {
            skin = Skin.DISABLED;
        } else if (highlighted) {
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

    /** 빨간 닫기 단추처럼 중립 스프라이트에 색을 입혀 그린다. */
    public static void tinted(GuiGraphicsExtractor g, int x, int y, int width, int height, int color) {
        if (width > 0 && height > 0) {
            g.blitSprite(RenderPipelines.GUI_TEXTURED, Skin.DISABLED.id, x, y, width, height, color);
        }
    }

    /** 영역 가운데에 글자를 놓는다. 폭이 모자라면 글자를 줄인다. */
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

    /** 왼쪽 정렬 글자. 폭이 모자라면 글자를 줄인다. */
    public static void text(GuiGraphicsExtractor g, Component text, int x, int y, int maxWidth, int color) {
        text(g, text, x, y, maxWidth, color, LABEL_SCALE);
    }

    public static void text(GuiGraphicsExtractor g, Component text, int x, int y, int maxWidth, int color,
                            float baseScale) {
        int textWidth = Minecraft.getInstance().font.width(text);
        float scale = Math.min(baseScale, Math.max(1, maxWidth) / (float) Math.max(1, textWidth));
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(scale, scale);
        g.text(Minecraft.getInstance().font, text, 0, 0, color, false);
        g.pose().popMatrix();
    }
}
