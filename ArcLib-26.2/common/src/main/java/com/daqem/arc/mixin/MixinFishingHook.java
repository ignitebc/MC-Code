package com.daqem.arc.mixin;

import com.daqem.arc.api.entity.ArcFishingHook;
import com.daqem.arc.api.player.ArcPlayer;
import com.daqem.arc.api.player.ArcServerPlayer;
import com.daqem.arc.event.triggers.PlayerEvents;
import com.daqem.arc.player.FishingWaitTimeMultiplierResolver;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingHook.class)
public abstract class MixinFishingHook implements ArcFishingHook {

    @Shadow
    private int timeUntilLured;

    @Unique
    private boolean arc$lureTimerJustReset;

    // --- 자동 낚시 판정 근거 ---
    @Unique
    private long arc$originInteractionCounter = 0L;
    @Unique
    private int arc$trackedTicks = 0;
    @Unique
    private boolean arc$originRecorded = false;
    @Unique
    private float arc$originYaw = 0.0F;
    @Unique
    private float arc$originPitch = 0.0F;
    @Unique
    private Vec3 arc$originPosition = Vec3.ZERO;
    @Unique
    private float arc$maxYawDelta = 0.0F;
    @Unique
    private float arc$maxPitchDelta = 0.0F;
    @Unique
    private double arc$maxPositionDelta = 0.0D;

    @Override
    public int arc$getBlockInteractionCount() {
        Player owner = ((FishingHook) (Object) this).getPlayerOwner();
        if (!(owner instanceof ArcServerPlayer arcServerPlayer)) {
            return 0;
        }
        long delta = arcServerPlayer.arc$getBlockInteractionCounter() - this.arc$originInteractionCounter;
        return (int) Math.max(0L, Math.min(Integer.MAX_VALUE, delta));
    }

    @Override
    public float arc$getMaxYawDelta() {
        return this.arc$maxYawDelta;
    }

    @Override
    public float arc$getMaxPitchDelta() {
        return this.arc$maxPitchDelta;
    }

    @Override
    public double arc$getMaxPositionDelta() {
        return this.arc$maxPositionDelta;
    }

    @Override
    public int arc$getTrackedTicks() {
        return this.arc$trackedTicks;
    }

    /**
     * 찌가 떠 있는 동안 소유자의 시선과 위치 변화를 누적한다.
     * 방치 낚시는 입력이 없어 세 값이 모두 0 에 가깝게 남는다.
     */
    @Inject(at = @At("TAIL"), method = "tick()V")
    private void arc$trackOwnerInput(CallbackInfo ci) {
        FishingHook self = (FishingHook) (Object) this;
        if (self.level().isClientSide()) {
            return;
        }
        Player owner = self.getPlayerOwner();
        if (owner == null) {
            return;
        }

        if (!this.arc$originRecorded) {
            this.arc$originRecorded = true;
            this.arc$originYaw = owner.getYRot();
            this.arc$originPitch = owner.getXRot();
            this.arc$originPosition = owner.position();
            if (owner instanceof ArcServerPlayer arcServerPlayer) {
                this.arc$originInteractionCounter = arcServerPlayer.arc$getBlockInteractionCounter();
            }
        }

        this.arc$trackedTicks++;
        float yawDelta = Math.abs(Mth.wrapDegrees(owner.getYRot() - this.arc$originYaw));
        float pitchDelta = Math.abs(Mth.wrapDegrees(owner.getXRot() - this.arc$originPitch));
        double positionDelta = owner.position().distanceTo(this.arc$originPosition);

        if (yawDelta > this.arc$maxYawDelta) {
            this.arc$maxYawDelta = yawDelta;
        }
        if (pitchDelta > this.arc$maxPitchDelta) {
            this.arc$maxPitchDelta = pitchDelta;
        }
        if (positionDelta > this.arc$maxPositionDelta) {
            this.arc$maxPositionDelta = positionDelta;
        }
    }

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
