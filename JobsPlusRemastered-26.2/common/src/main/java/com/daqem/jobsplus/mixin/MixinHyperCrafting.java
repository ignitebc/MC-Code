package com.daqem.jobsplus.mixin;

import com.daqem.jobsplus.player.job.hyper.HyperFarmerHandler;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractContainerMenu.class)
public abstract class MixinHyperCrafting
{
    @WrapMethod(method = "clicked")
    private void jobsplus$craftGoldenApple(int slot, int button, ContainerInput input, Player player,
                                           Operation<Void> original)
    {
        // 다른 클릭 추적기의 HEAD/RETURN 쌍이 반쪽만 실행되지 않도록 원본 전체를 감싼다.
        if (!HyperFarmerHandler.craft((AbstractContainerMenu) (Object) this, slot, button, input, player))
        {
            original.call(slot, button, input, player);
        }
    }
}
