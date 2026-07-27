package com.daqem.arc.mixin;

import com.daqem.arc.api.IArcAbstractCookingRecipe;
import com.daqem.arc.api.block.ArcHopperFedContainer;
import com.daqem.arc.api.player.ArcServerPlayer;
import com.daqem.arc.event.triggers.PlayerEvents;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class MixinAbstractFurnaceBlockEntity {

    @Unique
    private static final String arc$HOPPER_FED_RECIPES_USED_TAG = "ArcHopperFedRecipesUsed";

    @Unique
    private static final Codec<Map<ResourceKey<Recipe<?>>, Integer>> arc$HOPPER_FED_RECIPES_USED_CODEC =
            Codec.unboundedMap(Recipe.KEY_CODEC, Codec.INT);

    @Shadow
    @Final
    private Reference2IntOpenHashMap<ResourceKey<Recipe<?>>> recipesUsed;

    @Unique
    private final Reference2IntOpenHashMap<ResourceKey<Recipe<?>>> arc$hopperFedRecipesUsed =
            new Reference2IntOpenHashMap<>();

    @Inject(at = @At("HEAD"), method = "setRecipeUsed")
    private void arc$recordHopperFedRecipe(@Nullable RecipeHolder<?> recipeHolder, CallbackInfo ci) {
        if (recipeHolder != null && ((ArcHopperFedContainer) this).arc$consumeHopperFedItems(1) > 0) {
            this.arc$hopperFedRecipesUsed.addTo(recipeHolder.id(), 1);
        }
    }

    @Inject(at = @At("HEAD"), method = "awardUsedRecipesAndPopExperience")
    private void awardUsedRecipesAndPopExperience(ServerPlayer serverPlayer, CallbackInfo ci) {
        if (serverPlayer instanceof ArcServerPlayer arcServerPlayer) {
            ServerLevel serverLevel = serverPlayer.level();
            this.recipesUsed.forEach((recipeId, recipeCount) -> serverLevel.recipeAccess().byKey(recipeId).ifPresent((recipe) -> {
                int hopperFedRecipeCount = Math.min(recipeCount, this.arc$hopperFedRecipesUsed.getInt(recipeId));
                if (recipe.value() instanceof IArcAbstractCookingRecipe cookingRecipe) {
                    for (int i = hopperFedRecipeCount; i < recipeCount; i++) {
                        PlayerEvents.onSmeltItem(arcServerPlayer, recipe.value(), cookingRecipe.arc$getResult(),
                                ((AbstractFurnaceBlockEntity) (Object) this).getBlockPos(), serverLevel);
                    }
                }
            }));
        }
    }

    @Inject(at = @At("TAIL"), method = "awardUsedRecipesAndPopExperience")
    private void arc$clearHopperFedRecipesUsed(ServerPlayer serverPlayer, CallbackInfo ci) {
        this.arc$hopperFedRecipesUsed.clear();
    }

    @Inject(at = @At("TAIL"), method = "loadAdditional(Lnet/minecraft/world/level/storage/ValueInput;)V")
    private void arc$loadHopperFedRecipesUsed(ValueInput valueInput, CallbackInfo ci) {
        this.arc$hopperFedRecipesUsed.clear();
        this.arc$hopperFedRecipesUsed.putAll(
                valueInput.read(arc$HOPPER_FED_RECIPES_USED_TAG, arc$HOPPER_FED_RECIPES_USED_CODEC)
                        .orElse(Map.of()));
    }

    @Inject(at = @At("TAIL"), method = "saveAdditional(Lnet/minecraft/world/level/storage/ValueOutput;)V")
    private void arc$saveHopperFedRecipesUsed(ValueOutput valueOutput, CallbackInfo ci) {
        if (!this.arc$hopperFedRecipesUsed.isEmpty()) {
            valueOutput.store(
                    arc$HOPPER_FED_RECIPES_USED_TAG,
                    arc$HOPPER_FED_RECIPES_USED_CODEC,
                    this.arc$hopperFedRecipesUsed);
        }
    }
}
