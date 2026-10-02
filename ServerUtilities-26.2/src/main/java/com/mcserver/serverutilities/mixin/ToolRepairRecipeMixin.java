package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.level.ToolLevelRules;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RepairItemRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RepairItemRecipe.class)
abstract class ToolRepairRecipeMixin {
    @Inject(method = "assemble(Lnet/minecraft/world/item/crafting/CraftingInput;)Lnet/minecraft/world/item/ItemStack;",
            at = @At("RETURN"))
    private void serverutilities$inheritRepairedTool(CraftingInput input,
                                                    CallbackInfoReturnable<ItemStack> callback) {
        ItemStack result = callback.getReturnValue();
        if (!ToolLevelRules.isLevelable(result)) return;
        // 제작 칸을 앞에서부터 확인했을 때 첫 번째 도구가 주 재료다.
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack primary = input.getItem(slot);
            if (primary.isEmpty()) continue;
            ToolLevelRules.inheritPrimaryTool(primary, result);
            return;
        }
    }
}
