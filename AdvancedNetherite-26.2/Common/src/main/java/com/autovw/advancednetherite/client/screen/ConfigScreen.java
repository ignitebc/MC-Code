package com.autovw.advancednetherite.client.screen;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.*;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Util;

import java.net.URI;
import java.util.List;

/**
 * Configured 모드가 없을 때 표시하는 설정 화면 클래스
 * @author Autovw
 */
public class ConfigScreen extends Screen
{
    private final Component modTitle;
    private final Screen parent;
    private final MutableComponent configured = Component.literal("Configured").withStyle(ChatFormatting.YELLOW);

    /**
     * @param title 화면을 추가하는 모드의 제목
     * @param parent 부모 화면
     */
    public ConfigScreen(Component title, Screen parent)
    {
        super(Component.translatable("fml.menu.mods.config").append(" / ").append(title));
        this.modTitle = title;
        this.parent = parent;
    }

    @Override
    protected void init()
    {
        // Configured 버튼
        addRenderableWidget(Button.builder(Component.translatable("config.advancednetherite.screen.button.install_configured", this.configured), onPress -> {
            Util.getPlatform().openUri(URI.create("https://www.curseforge.com/minecraft/mc-mods/configured"));
        }).pos(width / 2 - 155, height / 2 + 12).size(150, 20).build());

        // 안내 버튼
        Button instructionsButton = Button.builder(Component.translatable("config.advancednetherite.screen.button.instructions"), onPress -> {
            if (getInstructionsUrl() != null)
            {
                Util.getPlatform().openUri(getInstructionsUrl());
            }
        }).pos(width / 2 + 5, height / 2 + 12).size(150, 20).build();

        if (getInstructionsUrl() == null)
            instructionsButton.active = false;

        addRenderableWidget(instructionsButton);

        // 뒤로 버튼
        addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, onPress -> {
            this.minecraft.gui.setScreen(this.parent);
        }).pos(width / 2 - 75, height - 29).size(150, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float ticks)
    {
        this.extractBackground(graphics, mouseX, mouseY, ticks);
        super.extractRenderState(graphics, mouseX, mouseY, ticks);
        graphics.centeredText(this.font, this.title, this.width / 2, 7, 0xFFFFFF);
        drawCenteredSplitString(graphics, this.font, this.font.split(getDescriptionTop(), this.width), this.width / 2, 55, 0xFFFFFF);
        drawCenteredSplitString(graphics, this.font, this.font.split(getDescriptionBottom(), this.width), this.width / 2, 90, 0xFFFFFF);
    }

    /**
     * <code>drawCenteredString</code> 대신 {@link Font#split(FormattedText, int)}로 나눈 글자를 그려, 글자가 화면 밖으로 사라질 걱정을 없앤다.
     * @param graphics 그리기 도구
     * @param font 사용할 글꼴
     * @param charSequenceList {@link Font#split(FormattedText, int)}가 돌려준 목록
     * @param x 글자를 그릴 X 좌표
     * @param y 글자를 그릴 Y 좌표
     * @param color 글자 색
     */
    public static void drawCenteredSplitString(GuiGraphicsExtractor graphics, Font font, List<FormattedCharSequence> charSequenceList, int x, int y, int color)
    {
        for (FormattedCharSequence sequence : charSequenceList)
        {
            graphics.centeredText(font, sequence, x, y, color);
            y += font.lineHeight;
        }
    }

    /**
     * 위쪽 설명 컴포넌트. 바꾸려면 {@link Override}한다.
     * @return 위쪽 설명 컴포넌트
     */
    public Component getDescriptionTop()
    {
        return Component.translatable("config.advancednetherite.screen.description.top", this.configured, this.modTitle);
    }

    /**
     * 아래쪽 설명 컴포넌트. 바꾸려면 {@link Override}한다.
     * @return 아래쪽 설명 컴포넌트
     */
    public Component getDescriptionBottom()
    {
        return Component.translatable("config.advancednetherite.screen.description.bottom");
    }

    /**
     * 안내 버튼이 여는 URL. null을 돌려주면 버튼을 끈다.
     * @return 안내 URL
     */
    public URI getInstructionsUrl()
    {
        return URI.create("https://github.com/Autovw/AdvancedNetherite/wiki/Configuration");
    }
}
