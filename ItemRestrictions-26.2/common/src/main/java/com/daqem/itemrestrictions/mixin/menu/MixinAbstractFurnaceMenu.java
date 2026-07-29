package com.daqem.itemrestrictions.mixin.menu;

import com.daqem.itemrestrictions.level.menu.ItemRestrictionsAbstractFurnaceMenu;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractFurnaceMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.RecipeBookMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractFurnaceMenu.class)
public abstract class MixinAbstractFurnaceMenu extends RecipeBookMenu implements ItemRestrictionsAbstractFurnaceMenu {

    @Shadow
    @Final
    Container container;

    public MixinAbstractFurnaceMenu(MenuType<?> menuType, int i) {
        super(menuType, i);
    }

    @Override
    public Container itemrestrictions$getContainer() {
        return this.container;
    }
}
