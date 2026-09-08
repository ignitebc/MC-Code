package com.autovw.advancednetherite.mixin.client;

import com.autovw.advancednetherite.common.backpack.BackpackInventory;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AbstractRecipeBookScreen.class)
public abstract class BackpackRecipeBookMixin
{
    @ModifyConstant(method = "init", constant = @Constant(intValue = 379))
    private int advancednetherite$recipeBookWidth(int width)
    {
        return (Object) this instanceof InventoryScreen ? width + BackpackInventory.PANEL_WIDTH : width;
    }
}
