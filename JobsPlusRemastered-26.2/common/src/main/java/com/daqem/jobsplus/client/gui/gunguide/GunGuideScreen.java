package com.daqem.jobsplus.client.gui.gunguide;

import com.daqem.jobsplus.client.gui.jobs.components.GunGuideComponent;
import com.daqem.jobsplus.client.gui.jobs.tab.RightTab;
import com.daqem.jobsplus.client.gui.theme.JobsCloseButton;
import com.daqem.jobsplus.client.gui.theme.JobsLayout;
import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.uilib.gui.AbstractScreen;
import com.daqem.uilib.gui.component.AbstractComponent;
import com.daqem.uilib.gui.widget.CustomButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * 총기 도감만 담은 화면. TACZ 총기 작업대의 "총기 도감" 탭이 연다.
 *
 * <p>직업 화면(J키)의 총기 도감 탭과 같은 컴포넌트를 같은 크기·같은 틀에 담으므로 보이는 모습과 쓰는 법이 같다.
 * 직업 화면은 서버가 직업 목록을 보내 줘야 열리지만, 도감은 클라이언트에 로드된 TACZ 팩만 읽으므로
 * 서버를 거치지 않고 바로 연다. 닫으면 열었던 화면(총기 작업대)으로 돌아간다.
 */
public class GunGuideScreen extends AbstractScreen
{
    private static final Component TITLE = RightTab.GUN_GUIDE.getName();

    private final @Nullable Screen previousScreen;

    public GunGuideScreen(@Nullable Screen previousScreen)
    {
        super(TITLE);
        this.previousScreen = previousScreen;
    }

    @Override
    protected void init()
    {
        Frame frame = new Frame(JobsLayout.forScreen(this.width, this.height));
        frame.center();
        this.addComponent(frame);
        super.init();
    }

    @Override
    public void onClose()
    {
        assert this.minecraft != null;
        this.minecraft.gui.setScreen(this.previousScreen);
    }

    @Override
    public boolean isPauseScreen()
    {
        return false;
    }

    /** 직업 화면에서 전체 폭 탭을 그릴 때와 같은 틀 */
    private static class Frame extends AbstractComponent
    {
        private static final int BACK_WIDTH = 70;

        private final JobsLayout layout;

        Frame(JobsLayout layout)
        {
            super(0, 0, layout.width(), layout.height());
            this.layout = layout;

            // 직업 화면의 오른쪽 내용 칸과 같은 자리, 같은 크기
            GunGuideComponent guide = new GunGuideComponent(
                    layout.pageWidth(RightTab.GUN_GUIDE) - 14, layout.bodyHeight() - 26);
            guide.setX(layout.pageX(RightTab.GUN_GUIDE) + 7);
            guide.setY(layout.bodyY() + 20);
            this.addComponent(guide);

            this.addWidget(new BackButton(8, 7, BACK_WIDTH));
            this.addWidget(new JobsCloseButton(getWidth() - 22, 7));
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick,
                                       int parentWidth, int parentHeight)
        {
            int x = getTotalX();
            int y = getTotalY();
            int pageX = this.layout.pageX(RightTab.GUN_GUIDE);
            int pageWidth = this.layout.pageWidth(RightTab.GUN_GUIDE);
            JobsTheme.panel(graphics, x, y, getWidth(), getHeight());
            JobsTheme.texture(graphics, JobsTheme.Skin.HEADER, x + 2, y + 2, getWidth() - 4, 25);
            graphics.fill(x + 8, y + 27, x + getWidth() - 8, y + 28, JobsTheme.DIVIDER);

            JobsTheme.panel(graphics, x + pageX, y + this.layout.bodyY(), pageWidth, this.layout.bodyHeight());
            JobsTheme.texture(graphics, JobsTheme.Skin.HEADER, x + 9, y + this.layout.bodyY() + 1, pageWidth - 2, 18);
            JobsTheme.text(graphics, TITLE, x + 16, y + this.layout.bodyY() + 7, pageWidth - 16, JobsTheme.CYAN);

            graphics.fill(x + 8, y + getHeight() - 20, x + getWidth() - 8, y + getHeight() - 19, JobsTheme.DIVIDER);
            JobsTheme.text(graphics, Component.literal("ESC(닫기)"), x + 10, y + getHeight() - 14, 60, JobsTheme.MUTED);
        }
    }

    /** 머리글 왼쪽의 돌아가기 단추. 닫기와 같은 경로로 열었던 화면에 돌아간다. */
    private static class BackButton extends CustomButtonWidget
    {
        BackButton(int x, int y, int width)
        {
            super(x, y, width, JobsTheme.BUTTON_HEIGHT, Component.literal("◀ 총기 작업대"), null, button -> {
                Screen screen = Minecraft.getInstance().gui.screen();
                if (screen != null)
                {
                    screen.onClose();
                }
            });
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
        {
            JobsTheme.button(graphics, getX(), getY(), getWidth(), getHeight(), active, isHoveredOrFocused(), false, false);
            JobsTheme.label(graphics, getMessage(), getX(), getY(), getWidth(), getHeight(), JobsTheme.TEXT);
        }
    }
}
