package com.daqem.arc.mixin;

import com.daqem.arc.event.triggers.PlayerEvents;
import com.daqem.arc.api.player.ArcServerPlayer;
import com.daqem.arc.api.action.result.ActionResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {

    @Shadow
    public abstract Item getItem();

    @Shadow public abstract ItemUseAnimation getUseAnimation();

    @Inject(at = @At("HEAD"), method = "finishUsingItem(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;)Lnet/minecraft/world/item/ItemStack;")
    private void finishUsingItem(Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
        if (entity instanceof ArcServerPlayer player) {
            if (this.getUseAnimation() == ItemUseAnimation.DRINK) {
                PlayerEvents.onPlayerDrink(player, arc$getItemStack());
            }
        }
    }

    @Inject(at = @At(value = "HEAD"), method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerPlayer;Ljava/util/function/Consumer;)V", cancellable = true)
    private void hurtAndBreak(int i, ServerLevel serverLevel, ServerPlayer serverPlayer, Consumer<Item> consumer, CallbackInfo ci) {
        if (serverPlayer instanceof ArcServerPlayer player) {
            ItemStack copy = arc$getItemStack().copy();
            if (copy.isDamageableItem()) {
                if (!serverPlayer.hasInfiniteMaterials()) {
                    if (i > 0) {
                        i = EnchantmentHelper.processDurabilityChange(serverLevel, copy, i);
                        if (i <= 0) {
                            return;
                        }
                    }

                    int j = copy.getDamageValue() + i;
                    copy.setDamageValue(j);
                    if (j >= copy.getMaxDamage()) {
                        ActionResult actionResult = PlayerEvents.onPlayerHurtItem(player, arc$getItemStack());
                        if (actionResult.shouldCancelAction()) {
                            ci.cancel();
                        }
                    }
                }
            }
        }
    }

    @Unique
    private ItemStack arc$getItemStack() {
        return (ItemStack) (Object) this;
    }
}
