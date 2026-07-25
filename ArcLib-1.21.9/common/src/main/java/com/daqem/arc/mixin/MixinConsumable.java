package com.daqem.arc.mixin;

import com.daqem.arc.api.player.ArcServerPlayer;
import com.daqem.arc.event.triggers.PlayerEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Consumable.class)
public abstract class MixinConsumable {

    @Shadow public abstract ItemUseAnimation animation();

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;consume(ILnet/minecraft/world/entity/LivingEntity;)V"), method = "onConsume")
    private void onConsume(Level level, LivingEntity livingEntity, ItemStack itemStack, CallbackInfoReturnable<ItemStack> cir) {
        if (livingEntity instanceof ArcServerPlayer serverPlayer) {
            if (this.animation() != ItemUseAnimation.DRINK) {
                PlayerEvents.onPlayerEat(serverPlayer, itemStack);
            }
        }
    }
}
