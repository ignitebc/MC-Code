package com.daqem.jobsplus.client.gui.powerups.tab;

import com.daqem.jobsplus.client.gui.jobs.tab.ITab;
import net.minecraft.network.chat.Component;

public enum PowerupTab implements ITab {
    NORMAL(Component.literal("일반스킬")),
    HYPER(Component.literal("하이퍼스킬"));

    private final Component name;

    PowerupTab(Component name)
    {
        this.name = name;
    }

    @Override
    public Component getName()
    {
        return name;
    }
}
