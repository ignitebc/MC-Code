package com.daqem.arc.api;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Predicate;

public interface IArcIngredient {

    List<Item> arc$getItems();
}
