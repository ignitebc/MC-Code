package com.daqem.arc.mixin;

import com.daqem.arc.api.IArcAbstractCookingRecipe;
import com.daqem.arc.api.IArcIngredient;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.SingleItemRecipe;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractCookingRecipe.class)
public abstract class MixinAbstractCookingRecipe extends SingleItemRecipe implements IArcAbstractCookingRecipe {

    public MixinAbstractCookingRecipe(String string, Ingredient ingredient, ItemStack itemStack) {
        super(string, ingredient, itemStack);
    }

    @Override
    public ItemStack arc$getResult() {
        return this.result();
    }

    @Override
    public IArcIngredient arc$getIngredient() {
        Ingredient ingredient = this.input();
        if ((Object) ingredient instanceof IArcIngredient arcIngredient) {
            return arcIngredient;
        }
        return null;
    }
}
