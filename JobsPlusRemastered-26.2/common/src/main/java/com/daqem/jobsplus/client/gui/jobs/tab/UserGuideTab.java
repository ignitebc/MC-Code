package com.daqem.jobsplus.client.gui.jobs.tab;

import net.minecraft.network.chat.Component;

/** 게임안내 안에서 공통 규칙을 주제별로 나누는 하위 탭. */
public enum UserGuideTab implements ITab
{
    BASICS_AND_JOBS(Component.literal("기본·직업")),
    LIFE_AND_CONVENIENCE(Component.literal("생활·편의")),
    ECONOMY_AND_TRADE(Component.literal("경제·거래")),
    BOXES_AND_PETS(Component.literal("상자·펫")),
    EQUIPMENT_AND_GUNS(Component.literal("장비·총기")),
    ADVENTURE_AND_COMBAT(Component.literal("모험·전투"));

    private final Component name;

    UserGuideTab(Component name)
    {
        this.name = name;
    }

    @Override
    public Component getName()
    {
        return this.name;
    }
}
