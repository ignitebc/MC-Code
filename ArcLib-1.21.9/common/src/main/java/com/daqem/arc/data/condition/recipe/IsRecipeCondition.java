package com.daqem.arc.data.condition.recipe;

import com.daqem.arc.api.IArcAbstractCookingRecipe;
import com.daqem.arc.api.IArcIngredient;
import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.condition.AbstractCondition;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;

import java.util.List;

public abstract class IsRecipeCondition<T extends Recipe<?>> extends AbstractCondition {

    public IsRecipeCondition(boolean inverted) {
        super(inverted);
    }

    protected boolean isSmeltingRecipeVersion(ActionData actionData, IArcAbstractCookingRecipe smeltingRecipe, Class<? extends Recipe<?>> recipeClass) {
        MinecraftServer server = actionData.getPlayer().arc$getPlayer().level().getServer();
        if (server != null) {
            List<IArcAbstractCookingRecipe> recipes = server.getRecipeManager().getRecipes().stream()
                    .filter(recipeClass::isInstance)
                    .map(r -> (IArcAbstractCookingRecipe) r.value())
                    .toList();
            for (IArcAbstractCookingRecipe r : recipes) {
                if (r.arc$getResult().getItem().equals(smeltingRecipe.arc$getResult().getItem())) {
                    IArcIngredient ingredient = r.arc$getIngredient();
                    if (ingredient == null) {
                        return false;
                    }
                    for (int i = 0; i < ingredient.arc$getItems().size(); i++) {
                        Item item = ingredient.arc$getItems().get(i);
                        IArcIngredient ingredient1 = smeltingRecipe.arc$getIngredient();
                        if (ingredient1 == null) {
                            return false;
                        }
                        Item item1 = smeltingRecipe.arc$getIngredient().arc$getItems().get(i);
                        if (item != item1) {
                            return false;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }
}
