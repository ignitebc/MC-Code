package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.level.ToolLevelRules;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GrindstoneMenu.class)
abstract class ToolGrindstoneMixin {
    @Inject(method = "computeResult", at = @At("RETURN"))
    private void serverutilities$inheritGrindstoneTool(ItemStack first, ItemStack second,
                                                      CallbackInfoReturnable<ItemStack> callback) {
        ItemStack primary = first;
        if (primary.isEmpty()) primary = second;
        ToolLevelRules.inheritPrimaryTool(primary, callback.getReturnValue());
    }
}
