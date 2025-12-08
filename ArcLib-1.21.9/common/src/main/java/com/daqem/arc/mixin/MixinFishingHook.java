package com.daqem.arc.mixin;

import com.daqem.arc.api.player.ArcPlayer;
import com.daqem.arc.event.triggers.PlayerEvents;
import com.daqem.arc.api.player.ArcServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(FishingHook.class)
public abstract class MixinFishingHook {

    @Inject(at = @At("HEAD"), method = "retrieve(Lnet/minecraft/world/item/ItemStack;)I")
    private void retrieve(ItemStack itemStack, CallbackInfoReturnable<Integer> info) {
        Player player = ((FishingHook) (Object) this).getPlayerOwner();
        if (player instanceof ArcPlayer serverPlayer) {
            PlayerEvents.onRodReelIn(serverPlayer, ((FishingHook) (Object) this));
        }
    }
}
