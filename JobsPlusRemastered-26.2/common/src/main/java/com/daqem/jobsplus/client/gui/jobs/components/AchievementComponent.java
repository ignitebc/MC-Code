package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.tab.AchievementTab;
import com.daqem.jobsplus.client.gui.jobs.widgets.AchievementTabWidget;
import com.daqem.uilib.gui.component.EmptyComponent;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** 업적 탭. 위쪽 하위 탭으로 업적 목록과 칭호 화면을 전환한다. */
public class AchievementComponent extends EmptyComponent
{
    private static final int TAB_GAP = 1;
    private static final int MAX_TAB_WIDTH = 50;
    private static final int CONTENT_Y = AchievementTabWidget.HEIGHT + 4;

    private final JobsScreenState state;
    private AchievementTab renderedTab;

    public AchievementComponent(JobsScreenState state, int width, int height)
    {
        super(0, 0, width, height);
        this.state = state;
        this.addTabWidgets();
        this.showSelectedTab();
    }

    /** 하위 탭이 두 개뿐이라 화면 폭을 나누지 않고 상위 탭과 비슷한 폭으로 왼쪽부터 놓는다. */
    private void addTabWidgets()
    {
        AchievementTab[] tabs = AchievementTab.values();
        int evenWidth = (getWidth() - TAB_GAP * (tabs.length - 1)) / tabs.length;
        int tabWidth = Math.min(MAX_TAB_WIDTH, evenWidth);
        int tabX = 0;
        for (AchievementTab tab : tabs)
        {
            this.addWidget(new AchievementTabWidget(this.state, tab, tabX, tabWidth));
            tabX += tabWidth + TAB_GAP;
        }
    }

    /** 고른 하위 탭의 내용만 붙인다. 하위 탭 단추는 위젯이라 내용을 지워도 남는다. */
    private void showSelectedTab()
    {
        this.renderedTab = this.state.getSelectedAchievementTab();
        this.clearComponents();
        if (this.renderedTab == AchievementTab.TITLES)
        {
            TitleListComponent titleList = new TitleListComponent(getWidth(), getContentHeight());
            titleList.setY(CONTENT_Y);
            this.addComponent(titleList);
        }
        else if (this.renderedTab == AchievementTab.ACHIEVEMENTS)
        {
            AchievementListComponent achievementList = new AchievementListComponent(getWidth(), getContentHeight());
            achievementList.setY(CONTENT_Y);
            this.addComponent(achievementList);
        }
    }

    private int getContentHeight()
    {
        return Math.max(1, getHeight() - CONTENT_Y);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY,
                                   float partialTick, int parentWidth, int parentHeight)
    {
        if (this.renderedTab != this.state.getSelectedAchievementTab())
        {
            this.showSelectedTab();
            this.updateParentPosition(getParentX(), getParentY(), parentWidth, parentHeight);
        }
    }
}
