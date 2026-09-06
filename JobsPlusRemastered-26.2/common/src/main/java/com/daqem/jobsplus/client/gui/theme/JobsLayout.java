package com.daqem.jobsplus.client.gui.theme;

/** GUI-space bounds shared by drawing and widget construction. */
public record JobsLayout(int width, int height) {
    public static JobsLayout forScreen(int screenWidth, int screenHeight) {
        return new JobsLayout(Math.max(1, Math.min(480, screenWidth - 16)),
                Math.max(1, Math.min(260, screenHeight - 16)));
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
        return 110;
    }

    public int detailX() {
        return wide() ? 128 : 8;
    }

    public int detailWidth() {
        return wide() ? 106 : leftWidth();
    }

    public int contentX() {
        return wide() ? 244 : 128;
    }

    public int contentWidth() {
        return width - contentX() - 8;
    }

    public int jobsY() {
        return bodyY() + (wide() ? 18 : 78);
    }

    public int jobsHeight() {
        return bodyHeight() - (wide() ? 24 : 84);
    }

    public int actionY() {
        return wide() ? bodyY() + bodyHeight() - 14 - 8 : bodyY() + 46;
    }

    public int stockTradingX() {
        return width - 164;
    }
}
