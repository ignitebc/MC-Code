package com.daqem.itemrestrictions.mixin.menu;

import com.daqem.arc.api.action.data.ActionDataBuilder;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.player.ArcPlayer;
import com.daqem.itemrestrictions.data.RestrictionResult;
import com.daqem.itemrestrictions.data.RestrictionType;
import com.daqem.itemrestrictions.level.player.ItemRestrictionsServerPlayer;
import com.daqem.itemrestrictions.networking.clientbound.ClientboundRestrictionPacket;
import dev.architectury.networking.NetworkManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SmithingMenu.class)
public abstract class MixingSmithingMenu extends ItemCombinerMenu {

    public MixingSmithingMenu(@Nullable MenuType<?> menuType, int i, Inventory inventory, ContainerLevelAccess containerLevelAccess, ItemCombinerMenuSlotDefinition itemCombinerMenuSlotDefinition) {
        super(menuType, i, inventory, containerLevelAccess, itemCombinerMenuSlotDefinition);
    }

    @Inject(at = @At("TAIL"), method = "createResult()V", cancellable = true)
    private void createResult(CallbackInfo ci) {
        if (this.player instanceof ServerPlayer serverPlayer) {
            if (serverPlayer instanceof ItemRestrictionsServerPlayer itemRestrictionsPlayer) {
                if (serverPlayer instanceof ArcPlayer arcPlayer) {
                    if (!this.inputSlots.getItem(0).isEmpty() && !this.inputSlots.getItem(1).isEmpty()) {
                        RestrictionResult restrictionResult = itemRestrictionsPlayer.itemrestrictions$isRestricted(new ActionDataBuilder(arcPlayer, null)
                                .withData(ActionDataType.ITEM_STACK, this.resultSlots.getItem(0))
                                .build());
                        if (restrictionResult.isRestricted(RestrictionType.CRAFT)) {
                            this.resultSlots.setItem(0, ItemStack.EMPTY);
                            NetworkManager.sendToPlayer(serverPlayer, new ClientboundRestrictionPacket(RestrictionType.CRAFT));
                            ci.cancel();
                        }
                    }
                }
            }
        }
    }
}
