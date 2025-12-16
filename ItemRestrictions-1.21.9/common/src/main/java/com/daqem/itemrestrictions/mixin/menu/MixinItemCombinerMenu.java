package com.daqem.itemrestrictions.mixin.menu;

import com.daqem.itemrestrictions.data.RestrictionType;
import com.daqem.itemrestrictions.networking.clientbound.ClientboundRestrictionPacket;
import dev.architectury.networking.NetworkManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemCombinerMenu.class)
public abstract class MixinItemCombinerMenu extends AbstractContainerMenu {

    @Shadow
    @Final
    protected Player player;

    protected MixinItemCombinerMenu(@Nullable MenuType<?> menuType, int i) {
        super(menuType, i);
    }

    @SuppressWarnings("DataFlowIssue")
    @Unique
    private ItemCombinerMenu itemrestrictions$getMenu() {
        return (ItemCombinerMenu) (Object) this;
    }

    @Inject(at = @At("HEAD"), method = "slotsChanged(Lnet/minecraft/world/Container;)V")
    private void slotsChanged(CallbackInfo info) {
        if (itemrestrictions$getMenu() instanceof AnvilMenu || itemrestrictions$getMenu() instanceof SmithingMenu) {
            if (this.player instanceof ServerPlayer serverPlayer) {
                if (!(!getSlot(0).getItem().isEmpty() && !getSlot(1).getItem().isEmpty())) {
                    NetworkManager.sendToPlayer(serverPlayer, new ClientboundRestrictionPacket(RestrictionType.NONE));
                }
            }
        }
    }
}
