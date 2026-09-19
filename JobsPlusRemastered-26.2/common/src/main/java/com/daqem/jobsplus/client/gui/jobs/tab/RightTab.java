package com.daqem.jobsplus.client.gui.jobs.tab;

import net.minecraft.network.chat.Component;

public enum RightTab implements ITab {
    EXPERIENCE(Component.literal("직업정보")),
    UP_AND_DOWN(Component.literal("주식")),
    SHOP(Component.literal("상점")),
    GUN_GUIDE(Component.literal("총기 도감")),
    RECIPES(Component.literal("게임안내"));

    private final Component name;

    RightTab(Component name)
    {
        this.name = name;
    }

    @Override
    public Component getName()
    {
        return name;
    }
}
