package com.daqem.arc.mixin;

import com.daqem.arc.api.IArcAbstractCookingRecipe;
import com.daqem.arc.api.player.ArcServerPlayer;
import com.daqem.arc.event.triggers.PlayerEvents;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class MixinAbstractFurnaceBlockEntity {


    @Shadow
    @Final
    private Reference2IntOpenHashMap<ResourceKey<Recipe<?>>> recipesUsed;

    @Inject(at = @At("HEAD"), method = "awardUsedRecipesAndPopExperience")
    private void awardUsedRecipesAndPopExperience(ServerPlayer serverPlayer, CallbackInfo ci) {
        if (serverPlayer instanceof ArcServerPlayer arcServerPlayer) {
            ServerLevel serverLevel = serverPlayer.level();
            this.recipesUsed.forEach((recipeId, recipeCount) -> serverLevel.recipeAccess().byKey(recipeId).ifPresent((recipe) -> {
                if (recipe.value() instanceof IArcAbstractCookingRecipe cookingRecipe) {
                    for (int i = 0; i < recipeCount; i++) {
                        PlayerEvents.onSmeltItem(arcServerPlayer, recipe.value(), cookingRecipe.arc$getResult(),
                                ((AbstractFurnaceBlockEntity) (Object) this).getBlockPos(), serverLevel);
                    }
                }
            }));
        }
    }
}
