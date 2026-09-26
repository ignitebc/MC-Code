package com.daqem.jobsplus.client.gui.gunguide;

import com.daqem.jobsplus.client.gui.jobs.components.GunGuideComponent;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * TACZ 총기 작업대 화면 안에 끼우는 총기 도감 칸. 작업대의 "총기 도감" 탭을 고르면 본문 자리에 들어간다.
 *
 * <p>작업대는 UILib 화면이 아니어서 UILib 컴포넌트를 그대로 받지 못한다. 그래서 바닐라 화면이 다룰 수 있는
 * 위젯 묶음으로 감싸 그리기와 클릭·휠·글자 입력을 안쪽 위젯에 넘긴다. 안에 든 것은 직업 화면(J키)의 도감 탭과
 * 같은 {@link GunGuideComponent}라 보이는 모습과 쓰는 법이 같다.
 *
 * <p>칸의 틀과 머리글 제목은 작업대가 그리고, 여기서는 도감 내용만 그린다.
 */
public class GunGuidePanel extends AbstractContainerEventHandler implements Renderable, NarratableEntry
{
    /** 머리글 줄 높이. 검색창이 이 줄 오른쪽에 들어간다. 직업 화면의 도감 탭과 같은 값이다. */
    private static final int HEADER = 20;
    private static final int SIDE_PADDING = 7;
    private static final int BOTTOM_PADDING = 6;

    private final ScreenRectangle area;
    private final GunGuideComponent guide;

    /** @param area 도감이 들어갈 자리. 머리글 줄을 포함한 작업대 본문 전체다. */
    public GunGuidePanel(ScreenRectangle area)
    {
        this.area = area;
        this.guide = new GunGuideComponent(area.width() - SIDE_PADDING * 2,
                area.height() - HEADER - BOTTOM_PADDING);
        this.guide.setX(area.left() + SIDE_PADDING);
        this.guide.setY(area.top() + HEADER);
        // UILib 화면이 init에서 해 주는 위치 전달을 여기서 한 번 한다. 스크롤 칸 안쪽 카드까지 자리가 정해진다.
        this.guide.updateParentPosition(0, 0, area.width(), area.height());
    }

    @Override
    public @NotNull List<? extends GuiEventListener> children()
    {
        return this.guide.getAllWidgets();
    }

    /** 검색창이 도감 위쪽 머리글 줄에 있으므로 판정 범위는 도감이 아니라 받은 자리 전체다. */
    @Override
    public boolean isMouseOver(double mouseX, double mouseY)
    {
        return this.area.containsPoint((int) mouseX, (int) mouseY);
    }

    @Override
    public @NotNull ScreenRectangle getRectangle()
    {
        return this.area;
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
    {
        this.guide.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public @NotNull NarrationPriority narrationPriority()
    {
        return NarrationPriority.NONE;
    }

    @Override
    public void updateNarration(@NotNull NarrationElementOutput output)
    {
    }
}
