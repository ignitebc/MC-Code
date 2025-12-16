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
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GrindstoneMenu.class)
public abstract class MixinGrindStoneMenu extends AbstractContainerMenu {

    @Shadow
    @Final
    Container repairSlots;
    @Shadow
    @Final
    private Container resultSlots;
    @Unique
    private ServerPlayer itemrestrictions$player;

    protected MixinGrindStoneMenu(@Nullable MenuType<?> menuType, int i) {
        super(menuType, i);
    }

    @Inject(at = @At("TAIL"), method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V")
    private void init(int i, Inventory inventory, ContainerLevelAccess containerLevelAccess, CallbackInfo ci) {
        if (inventory.player instanceof ServerPlayer serverPlayer) {
            this.itemrestrictions$player = serverPlayer;
        }
    }

    @Inject(at = @At("HEAD"), method = "slotsChanged(Lnet/minecraft/world/Container;)V")
    private void slotsChanged(Container container, CallbackInfo ci) {
        if (this.itemrestrictions$player != null) {
            if (this.repairSlots.getItem(0).isEmpty() || this.repairSlots.getItem(1).isEmpty()) {
                NetworkManager.sendToPlayer(this.itemrestrictions$player, new ClientboundRestrictionPacket(RestrictionType.NONE));
            }
        }
    }

    @Inject(at = @At("TAIL"), method = "createResult()V")
    private void createResult(CallbackInfo ci) {
        if (this.itemrestrictions$player != null) {
            if (!this.repairSlots.getItem(0).isEmpty() && !this.repairSlots.getItem(1).isEmpty()) {
                ItemStack resultSlotItem = this.resultSlots.getItem(0);
                if (!resultSlotItem.isEmpty()) {
                    if (this.itemrestrictions$player instanceof ItemRestrictionsServerPlayer itemRestrictionsPlayer) {
                        if (this.itemrestrictions$player instanceof ArcPlayer arcPlayer) {
                            RestrictionResult result = itemRestrictionsPlayer.itemrestrictions$isRestricted(
                                    new ActionDataBuilder(arcPlayer, null)
                                            .withData(ActionDataType.ITEM_STACK, resultSlotItem)
                                            .build());
                            if (result.isRestricted(RestrictionType.REPAIR)) {
                                this.getSlot(2).set(ItemStack.EMPTY);
                                NetworkManager.sendToPlayer(this.itemrestrictions$player, new ClientboundRestrictionPacket(RestrictionType.REPAIR));
                            }
                        }
                    }
                }
            }
        }
    }
}
