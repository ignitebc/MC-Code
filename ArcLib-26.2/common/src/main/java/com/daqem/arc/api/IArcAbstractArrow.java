package com.daqem.arc.api;

import net.minecraft.world.item.ItemStack;

public interface IArcAbstractArrow {

    ItemStack arc$getPickupItem();

    int arc$getFireDurationTicks();

    void arc$setFireDurationTicks(int fireDurationTicks);
}
