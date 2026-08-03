package com.daqem.arc.api;

import net.minecraft.world.item.ItemStack;

public interface IArcAbstractArrow {

    ItemStack arc$getPickupItem();

    int arc$getFireDurationTicks();

    void arc$setFireDurationTicks(int fireDurationTicks);

    int arc$getPoisonDurationTicks();

    void arc$setPoisonDurationTicks(int poisonDurationTicks);

    int arc$getPoisonAmplifier();

    void arc$setPoisonAmplifier(int poisonAmplifier);
}
