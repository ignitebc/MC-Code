package com.daqem.arc.mixin;

import com.daqem.arc.api.player.ArcServerPlayer;
import com.daqem.arc.event.triggers.PlayerEvents;
import net.minecraft.advancements.critereon.FishingRodHookedTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(FishingRodHookedTrigger.class)
public class MixinFishingRodHookedTrigger {

    @Inject(method = "trigger", at = @At("HEAD"))
    private void trigger(ServerPlayer serverPlayer, ItemStack itemStack, FishingHook fishingHook, Collection<ItemStack> collection, CallbackInfo ci) {
        if (serverPlayer instanceof ArcServerPlayer arcServerPlayer) {
            for (ItemStack stack : collection) {
                PlayerEvents.onFishedUpItem(arcServerPlayer, stack);
            }
        }
    }
}
