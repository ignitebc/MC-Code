package com.daqem.arc.mixin;

import com.daqem.arc.api.block.ArcHopperFedContainer;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlockEntity.class)
public class MixinHopperBlockEntity
{

    /**
     * 호퍼와 발사기가 보관함에 아이템을 넣을 때 지나가는 공통 경로다.
     * 화로나 양조대라면 자동 공급 표시를 남겨 두었다가 보상 지급 시점에 걸러낸다.
     */
    @Inject(at = @At("HEAD"),
            method = "addItem(Lnet/minecraft/world/Container;Lnet/minecraft/world/Container;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/core/Direction;)Lnet/minecraft/world/item/ItemStack;")
    private static void arc$markHopperFed(@Nullable Container source, Container destination, ItemStack stack,
                                          @Nullable Direction direction, CallbackInfoReturnable<ItemStack> cir)
    {
        if (destination instanceof ArcHopperFedContainer hopperFedContainer)
        {
            hopperFedContainer.arc$setHopperFed(true);
        }
    }
}
