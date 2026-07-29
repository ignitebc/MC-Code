package com.daqem.arc.mixin;

import com.daqem.arc.api.player.ArcServerPlayer;
import com.daqem.arc.event.triggers.PlayerEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public abstract class MixinAnvilMenu extends ItemCombinerMenu {

    public MixinAnvilMenu(@Nullable MenuType<?> menuType, int i, Inventory inventory, ContainerLevelAccess containerLevelAccess, ItemCombinerMenuSlotDefinition itemCombinerMenuSlotDefinition) {
        super(menuType, i, inventory, containerLevelAccess, itemCombinerMenuSlotDefinition);
    }

    @Inject(method = "onTake", at = @At("HEAD"))
    public void mixinOnTake(Player player, ItemStack itemStack, CallbackInfo ci) {
        if (player instanceof ArcServerPlayer arcServerPlayer) {
            PlayerEvents.onUseAnvil(arcServerPlayer, this.inputSlots.getItem(0));
        }
    }
}
