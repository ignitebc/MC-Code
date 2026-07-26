package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.widgets.ActionScrollWidget;
import com.daqem.uilib.gui.component.EmptyComponent;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jetbrains.annotations.NotNull;

public class ActionScrollComponent extends EmptyComponent
{

    private final ActionScrollWidget actionScrollWidget;

    public ActionScrollComponent(JobsScreenState state, int width, int height)
    {
        super(0, 38, width, height - 38);

        this.actionScrollWidget = new ActionScrollWidget(getWidth(), getHeight());
        ExperienceScrollContentComponent contentComponent =
                new ExperienceScrollContentComponent(state, Math.max(1, getWidth() - 16));
        this.actionScrollWidget.addComponent(contentComponent);
        this.addWidget(this.actionScrollWidget);
    }

    @Override
    public @NotNull ScreenRectangle getRectangle()
    {
        return new ScreenRectangle(this.getTotalX(), this.getTotalY(), getWidth(), getHeight());
    }
}
