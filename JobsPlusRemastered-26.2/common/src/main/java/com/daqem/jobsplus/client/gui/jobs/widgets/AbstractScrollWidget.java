package com.daqem.jobsplus.client.gui.jobs.widgets;

import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.jobsplus.mixin.client.AbstractScrollAreaAccessor;
import com.daqem.uilib.gui.widget.ScrollContainerWidget;
import com.mojang.blaze3d.platform.cursor.CursorTypes;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.util.Mth;

public abstract class AbstractScrollWidget extends ScrollContainerWidget
{

    private final static int DEFAULT_SCROLL_HANDLE_WIDTH = 8;
    private final static int DEFAULT_SCROLL_TRACK_WIDTH = 4;
    private final static int SCROLL_HANDLE_HEIGHT = 13;

    private final int itemHeight;

    public AbstractScrollWidget(int width, int height, int itemHeight)
    {
        super(width, height, 0);
        this.itemHeight = itemHeight;
    }

    @Override
    protected int scrollerHeight()
    {
        return SCROLL_HANDLE_HEIGHT;
    }

    @Override
    public boolean updateScrolling(MouseButtonEvent event)
    {
        ((AbstractScrollAreaAccessor) this).jobsplus$setScrolling(this.scrollable() && this.isValidClickButton(event.buttonInfo()) && event.x() >= this.scrollBarX() && event.x() <= this.scrollBarX() + this.scrollHandleWidth() && event.y() >= this.getY() && event.y() < this.getBottom());
        return ((AbstractScrollAreaAccessor) this).jobsplus$getScrolling();
    }

    protected int scrollBarX()
    {
        return this.getRight() - this.scrollHandleWidth();
    }

    protected int scrollHandleWidth()
    {
        return DEFAULT_SCROLL_HANDLE_WIDTH;
    }

    protected int scrollTrackWidth()
    {
        return DEFAULT_SCROLL_TRACK_WIDTH;
    }

    @Override
    public int scrollBarY()
    {
        int availableHeight = this.height - this.scrollerHeight() - 8;
        int baseY = (int) (this.scrollAmount() * availableHeight / this.maxScrollAmount()) + this.getY() + 4;
        return Mth.clamp(baseY, this.getY() + 4, this.getBottom() - this.scrollerHeight() - 4);
    }

    @Override
    protected void extractScrollbar(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY)
    {
        if (this.scrollable())
        {
            int scrollBarX = this.scrollBarX();
            int scrollHandleWidth = this.scrollHandleWidth();
            int scrollTrackWidth = this.scrollTrackWidth();
            int scrollTrackX = scrollBarX + (scrollHandleWidth - scrollTrackWidth) / 2;
            int scrollerHeight = this.scrollerHeight();
            int scrollBarY = this.scrollBarY();
            JobsTheme.cutBox(guiGraphics, scrollTrackX, getY(), scrollTrackWidth, getHeight(), JobsTheme.INSET, JobsTheme.DIVIDER);
            boolean dragging = ((AbstractScrollAreaAccessor) this).jobsplus$getScrolling();
            int thumbColor = dragging || this.isOverScrollbar(mouseX, mouseY) ? JobsTheme.CYAN : JobsTheme.MUTED;
            JobsTheme.cutBox(guiGraphics, scrollBarX, scrollBarY, scrollHandleWidth, scrollerHeight,
                    thumbColor, dragging ? JobsTheme.TEXT : JobsTheme.BORDER);
            if (this.isOverScrollbar(mouseX, mouseY))
            {
                guiGraphics.requestCursor(((AbstractScrollAreaAccessor) this).jobsplus$getScrolling() ? CursorTypes.RESIZE_NS : CursorTypes.POINTING_HAND);
            }
        }
    }

    @Override
    protected double scrollRate()
    {
        return this.itemHeight;
    }
}
