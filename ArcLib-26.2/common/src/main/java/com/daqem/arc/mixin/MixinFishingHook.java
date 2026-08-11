package com.daqem.arc.mixin;

import com.daqem.arc.api.player.ArcPlayer;
import com.daqem.arc.event.triggers.PlayerEvents;
import com.daqem.arc.api.player.ArcServerPlayer;
import com.daqem.arc.player.FishingWaitTimeMultiplierResolver;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(FishingHook.class)
public abstract class MixinFishingHook {

    @Shadow
    private int timeUntilLured;

    @Unique
    private boolean arc$lureTimerJustReset;

    @Inject(at = @At("HEAD"), method = "retrieve(Lnet/minecraft/world/item/ItemStack;)I")
    private void retrieve(ItemStack itemStack, CallbackInfoReturnable<Integer> info) {
        Player player = ((FishingHook) (Object) this).getPlayerOwner();
        if (player instanceof ArcPlayer serverPlayer) {
            PlayerEvents.onRodReelIn(serverPlayer, ((FishingHook) (Object) this), itemStack);
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;nextInt(Lnet/minecraft/util/RandomSource;II)I"),
            method = "catchingFish(Lnet/minecraft/core/BlockPos;)V")
    private void arc$markLureTimerReset(BlockPos blockPos, CallbackInfo ci) {
        this.arc$lureTimerJustReset = true;
    }

    /**
     * 입질 대기시간이 새로 뽑힌 틱에만, 바닐라가 미끼 보정까지 끝낸 값에 스킬 배율을 곱한다.
     * 대기시간 감소 중이거나 입질 진행 중인 틱에는 다시 곱해지지 않도록 플래그와
     * 남은 시간 검사로 걸러낸다. 접근 시간과 입질 반응 시간은 건드리지 않는다.
     */
    @Inject(at = @At("TAIL"), method = "catchingFish(Lnet/minecraft/core/BlockPos;)V")
    private void arc$applyFishingWaitTimeMultiplier(BlockPos blockPos, CallbackInfo ci) {
        if (!this.arc$lureTimerJustReset) {
            return;
        }
        this.arc$lureTimerJustReset = false;

        if (this.timeUntilLured <= 0) {
            return;
        }
        if (((FishingHook) (Object) this).getPlayerOwner() instanceof ArcPlayer arcPlayer) {
            float multiplier = FishingWaitTimeMultiplierResolver.getMultiplier(arcPlayer);
            if (multiplier < 1.0F) {
                this.timeUntilLured = Math.max(1, Math.round(this.timeUntilLured * multiplier));
            }
        }
    }
}
