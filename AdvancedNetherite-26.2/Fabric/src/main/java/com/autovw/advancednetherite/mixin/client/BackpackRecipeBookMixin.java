package com.autovw.advancednetherite.mixin.client;

import com.autovw.advancednetherite.common.backpack.BackpackInventory;
import com.autovw.advancednetherite.common.backpack.BackpackPanel;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(AbstractRecipeBookScreen.class)
public abstract class BackpackRecipeBookMixin
{
    /** 레시피 책을 옆에 펼칠 최소 화면 폭. 가방 패널을 단 화면은 패널 폭만큼 더 넓어야 한다. */
    @ModifyConstant(method = "init", constant = @Constant(intValue = 379))
    private int advancednetherite$recipeBookWidth(int width)
    {
        boolean panelScreen = BackpackPanel.hasPanel(((AbstractContainerScreen<?>) (Object) this).getMenu());
        return panelScreen ? width + BackpackInventory.PANEL_WIDTH : width;
    }
}
