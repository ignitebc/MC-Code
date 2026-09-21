package com.daqem.arc.mixin;

import com.daqem.arc.api.player.ArcServerPlayer;
import com.daqem.arc.event.triggers.PlayerEvents;
import com.daqem.arc.player.FishingAutomationGuard;
import net.minecraft.advancements.triggers.FishingRodHookedTrigger;
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
            // 우클릭 자동화로 돌아가는 낚시에는 직업 보상을 주지 않는다.
            // 바닐라 발전 과제는 그대로 두고 Arc 액션만 건너뛴다.
            if (!FishingAutomationGuard.shouldReward(serverPlayer, fishingHook)) {
                return;
            }
            for (ItemStack stack : collection) {
                PlayerEvents.onFishedUpItem(arcServerPlayer, stack);
            }
        }
    }
}
