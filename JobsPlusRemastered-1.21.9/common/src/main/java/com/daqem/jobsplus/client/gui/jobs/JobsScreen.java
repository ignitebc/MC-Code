package com.daqem.jobsplus.client.gui.jobs;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.jobs.components.JobsComponent;
import com.daqem.jobsplus.client.gui.jobs.widgets.ShopTooltipState;
import com.daqem.uilib.gui.AbstractScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class JobsScreen extends AbstractScreen
{
    private final JobsScreenState state;
    private final Screen previousScreen;

    public JobsScreen(JobsScreenState state, Screen previousScreen)
    {
        super(JobsPlus.translatable("gui.title.jobs"));
        this.state = state;
        this.previousScreen = previousScreen;
    }

    @Override
    protected void init()
    {
        JobsComponent jobsComponent = new JobsComponent(this.state);
        jobsComponent.center();

        this.addComponent(jobsComponent);

        super.init();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta)
    {
        // 프레임 시작 시 hover 초기화
        ShopTooltipState.clear();

        // 기존 UI 전부 렌더 (스크롤/Scissor 포함)
        super.render(graphics, mouseX, mouseY, delta);

        // 렌더가 다 끝난 후(=Scissor 작업이 끝난 후) 이름만 작은 박스로 그린다
        Component name = ShopTooltipState.getHoveredName();
        if (name != null)
        {
            renderNameBox(graphics, name, ShopTooltipState.getMouseX(), ShopTooltipState.getMouseY());
        }
    }

    private void renderNameBox(GuiGraphics g, Component name, int mouseX, int mouseY)
    {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        String text = name.getString();
        int textW = font.width(text);

        // 박스 최소화: padding만
        int paddingX = 4;
        int paddingY = 3;

        int boxW = textW + paddingX * 2;
        int boxH = font.lineHeight + paddingY * 2;

        int x = mouseX + 10;
        int y = mouseY - 12;

        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();

        // 화면 밖 보정
        if (x + boxW > screenW) x = screenW - boxW - 2;
        if (y + boxH > screenH) y = screenH - boxH - 2;
        if (x < 2) x = 2;
        if (y < 2) y = 2;

        // 바닐라풍 최소 박스(이름만)
        int bg = 0xF0100010;
        int border = 0xA0000000;

        g.fill(x - 1, y - 1, x + boxW + 1, y + boxH + 1, border);
        g.fill(x, y, x + boxW, y + boxH, bg);
        g.drawString(font, text, x + paddingX, y + paddingY, 0xFFFFFFFF, false);
    }

    @Override
    public void onClose()
    {
        assert this.minecraft != null;
        this.minecraft.setScreen(previousScreen);
    }

    public Screen getPreviousScreen()
    {
        return this.previousScreen;
    }

    /** 기존 상태 보존(Shop 탭 유지)용 Getter */
    public JobsScreenState getState()
    {
        return this.state;
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }
}
