package com.daqem.jobsplus.client.gui.jobs.widgets;

import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.uilib.api.component.IComponent;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class ActionScrollWidget extends AbstractScrollWidget
{

    public ActionScrollWidget(int width, int height)
    {
        super(width, height, 8);
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        super.extractWidgetRenderState(guiGraphics, mouseX, mouseY, partialTick);
        if (!getComponents().isEmpty())
        {
            IComponent firstComponent = getComponents().getFirst();
            if (firstComponent.getHeight() > getHeight() && scrollAmount() < maxScrollAmount())
            {
                guiGraphics.fillGradient(this.getX(), this.getY() + getHeight() - 8, this.getX() + firstComponent.getWidth(), this.getY() + this.getHeight(), 0x0008191F, JobsTheme.BACKGROUND);
            }
            if (firstComponent.getHeight() > getHeight() && scrollAmount() > 0)
            {
                guiGraphics.fillGradient(this.getX(), this.getY(), this.getX() + firstComponent.getWidth(), this.getY() + 8, JobsTheme.BACKGROUND, 0x0008191F);
            }
        }
    }
}
