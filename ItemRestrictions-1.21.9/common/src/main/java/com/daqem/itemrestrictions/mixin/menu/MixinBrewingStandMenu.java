package com.daqem.itemrestrictions.mixin.menu;

import com.daqem.itemrestrictions.level.menu.ItemRestrictionsBrewingStandMenu;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.BrewingStandMenu;
import net.minecraft.world.inventory.MenuType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BrewingStandMenu.class)
public abstract class MixinBrewingStandMenu extends AbstractContainerMenu implements ItemRestrictionsBrewingStandMenu {

    @Shadow @Final public Container brewingStand;

    protected MixinBrewingStandMenu(@Nullable MenuType<?> menuType, int i) {
        super(menuType, i);
    }

    @Override
    public Container itemrestrictions$getBrewingStand() {
        return brewingStand;
    }
}
