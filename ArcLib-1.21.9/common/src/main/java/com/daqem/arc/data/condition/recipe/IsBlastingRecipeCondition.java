package com.daqem.arc.data.condition.recipe;

import com.daqem.arc.api.IArcAbstractCookingRecipe;
import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.condition.serializer.IConditionSerializer;
import com.daqem.arc.api.condition.type.ConditionType;
import com.daqem.arc.api.condition.type.IConditionType;
import com.google.gson.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.BlastingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.SmeltingRecipe;

public class IsBlastingRecipeCondition extends IsRecipeCondition<BlastingRecipe> {

    public IsBlastingRecipeCondition(boolean inverted) {
        super(inverted);
    }

    @Override
    public boolean isMet(ActionData actionData) {
        Recipe<?> recipe = actionData.getData(ActionDataType.RECIPE);
        if (recipe != null) {
            if (recipe instanceof BlastingRecipe) {
                return true;
            }
            if (recipe instanceof IArcAbstractCookingRecipe smeltingRecipe) {
                return isSmeltingRecipeVersion(actionData, smeltingRecipe, BlastingRecipe.class);
            }
        }
        return false;
    }

    @Override
    public IConditionType<?> getType() {
        return ConditionType.IS_BLASTING_RECIPE;
    }

    public static class Serializer implements IConditionSerializer<IsBlastingRecipeCondition> {

        @Override
        public IsBlastingRecipeCondition fromJson(ResourceLocation location, JsonObject jsonObject, boolean inverted) {
            return new IsBlastingRecipeCondition(inverted);
        }

        @Override
        public IsBlastingRecipeCondition fromNetwork(ResourceLocation location, RegistryFriendlyByteBuf friendlyByteBuf, boolean inverted) {
            return new IsBlastingRecipeCondition(inverted);
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, IsBlastingRecipeCondition type) {
            IConditionSerializer.super.toNetwork(friendlyByteBuf, type);
        }
    }
}
