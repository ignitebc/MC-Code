package com.daqem.jobsplus.mixin;

import com.daqem.jobsplus.player.job.hyper.HyperFarmerHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Consumable.class)
public abstract class MixinHyperGoldenCarrot
{
    // 바닐라 섭취 효과를 모두 적용한 뒤, 먹은 아이템이 줄어들기 전이라 스택으로 황금당근인지 판별할 수 있다.
    @Inject(method = "onConsume", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;consume(ILnet/minecraft/world/entity/LivingEntity;)V"))
    private void jobsplus$goldenCarrotAbsorption(Level level, LivingEntity entity, ItemStack stack,
                                                 CallbackInfoReturnable<ItemStack> cir)
    {
        HyperFarmerHandler.eatGoldenCarrot(entity, stack);
    }
}
