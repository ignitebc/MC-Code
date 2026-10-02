package com.daqem.jobsplus.client.gui.jobs.tab;

import net.minecraft.network.chat.Component;

/** 업적 탭 안에서 업적 목록과 칭호를 나누는 하위 탭. */
public enum AchievementTab implements ITab
{
    ACHIEVEMENTS(Component.literal("업적")),
    TITLES(Component.literal("칭호"));

    private final Component name;

    AchievementTab(Component name)
    {
        this.name = name;
    }

    @Override
    public Component getName()
    {
        return this.name;
    }
}
