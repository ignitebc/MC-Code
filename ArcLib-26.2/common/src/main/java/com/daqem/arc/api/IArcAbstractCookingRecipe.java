package com.daqem.arc.api;

import net.minecraft.world.item.ItemStack;

public interface IArcAbstractCookingRecipe {

    ItemStack arc$getResult();
    IArcIngredient arc$getIngredient();

}
