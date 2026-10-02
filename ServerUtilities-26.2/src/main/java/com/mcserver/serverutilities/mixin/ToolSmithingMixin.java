package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.level.ToolLevelRules;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SmithingTransformRecipe.class)
abstract class ToolSmithingMixin {
    @Inject(method = "assemble(Lnet/minecraft/world/item/crafting/SmithingRecipeInput;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN"))
    private void serverutilities$inheritUpgradedTool(SmithingRecipeInput input,
                                                    CallbackInfoReturnable<ItemStack> callback) {
        ToolLevelRules.inheritPrimaryTool(input.base(), callback.getReturnValue());
    }
}
