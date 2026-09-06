package com.daqem.jobsplus.client.gui.theme;

import com.daqem.jobsplus.client.gui.jobs.tab.RightTab;

/** GUI-space bounds shared by drawing and widget construction. */
public record JobsLayout(int width, int height) {
    public static JobsLayout forScreen(int screenWidth, int screenHeight) {
        return new JobsLayout(Math.max(1, Math.min(560, screenWidth - 16)),
                Math.max(1, Math.min(304, screenHeight - 16)));
    }

    public boolean wide() {
        return width >= 420;
    }

    public int bodyY() {
        return 32;
    }

    public int bodyHeight() {
        return height - bodyY() - 21;
    }

    public int leftWidth() {
        return wide() ? (width - 44) * 25 / 100 : 110;
    }

    public int detailX() {
        return wide() ? 18 + leftWidth() : 8;
    }

    public int detailWidth() {
        return wide() ? (width - 44) * 27 / 100 : leftWidth();
    }

    public int contentX() {
        return wide() ? detailX() + detailWidth() + 10 : 128;
    }

    public int contentWidth() {
        return width - contentX() - 8;
    }

    public int jobsY() {
        return bodyY() + (wide() ? 22 : 78);
    }

    public int jobsHeight() {
        return bodyHeight() - (wide() ? 28 : 84);
    }

    public int actionY() {
        return wide() ? bodyY() + bodyHeight() - 14 - 8 : bodyY() + 46;
    }

    public boolean expandedPage(RightTab tab) {
        return wide() && (tab == RightTab.RECIPES || tab == RightTab.SHOP);
    }

    public int pageX(RightTab tab) {
        return expandedPage(tab) ? detailX() : contentX();
    }

    public int pageWidth(RightTab tab) {
        if (wide() && tab == RightTab.SHOP) {
            return width - detailX() - 168;
        }
        return width - pageX(tab) - 8;
    }

    public int stockTradingWidth() {
        return wide() ? (width - 26) * 43 / 100 : 156;
    }

    public int stockTradingX() {
        return width - stockTradingWidth() - 8;
    }
}
