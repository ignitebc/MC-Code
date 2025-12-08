package com.daqem.arc.mixin;

import com.daqem.arc.api.IArcIngredient;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.function.Predicate;

@Mixin(Ingredient.class)
public abstract class MixinIngredient implements StackedContents.IngredientInfo<Holder<Item>>, Predicate<ItemStack>, IArcIngredient {
    @Shadow @Final private HolderSet<Item> values;

    @Override
    public List<Item> arc$getItems() {
        return this.values.stream().map(Holder::value).toList();
    }
}
