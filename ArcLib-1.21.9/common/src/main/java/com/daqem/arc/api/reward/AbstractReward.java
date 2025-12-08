package com.daqem.arc.api.reward;

import com.daqem.arc.Arc;
import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.api.reward.type.IRewardType;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

public abstract class AbstractReward implements IReward {

    private final double chance;
    private final int priority;

    public AbstractReward(double chance, int priority) {
        this.chance = chance;
        this.priority = priority;
    }

    @Override
    public double getChance() {
        return chance;
    }

    public int getPriority() {
        return priority;
    }
    
    @Override
    public IRewardSerializer<? extends IReward> getSerializer() {
        return this.getType().getSerializer();
    }

    public abstract ActionResult apply(ActionData actionData);

    public boolean passedChance(ActionData actionData) {
        return chance == 100 || actionData.getPlayer().arc$nextRandomDouble() * 100 <= chance;
    }

    @Override
    public Component getName() {
        return Arc.translatable("reward." + this.getType().getLocation().getPath());
    }

    @Override
    public Component getDescription(Object... args) {
        return Arc.translatable("reward.description." + this.getType().getLocation().getPath(), args);
    }

    @Override
    public Component getDescription() {
        return getDescription(TranslatableContents.NO_ARGS);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IReward reward && reward.getType().equals(this.getType());
    }
}
