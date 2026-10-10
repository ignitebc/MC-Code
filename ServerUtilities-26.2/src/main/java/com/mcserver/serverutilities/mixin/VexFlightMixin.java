package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.monster.VexFlightRules;
import com.mcserver.serverutilities.monster.VexPathChaseGoal;
import com.mcserver.serverutilities.monster.VexPathWanderGoal;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.level.Level;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 벡스와 그것을 물려받은 몹(항복한 자)이 블록을 통과하지 않고 뚫린 공중으로만 날게 한다.
 * <p>
 * 바닐라는 매 틱 충돌을 꺼 벽·나뭇잎·땅을 통과한다. 규칙이 켜져 있으면 충돌을 유지하고(중력은 바닐라대로 꺼져 있어 공중을 난다),
 * 비행 길찾기와 경로 추적·떠돌기 Goal을 붙이며, 벽 속에 들어가 있으면 빈 공간으로 옮긴다. 규칙을 끄면 바닐라 동작으로 돌아간다.
 */
@Mixin(Vex.class)
public abstract class VexFlightMixin extends Monster {
    protected VexFlightMixin(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    /** 바닐라 벡스는 길찾기를 쓰지 않는다. 벽을 돌아 뚫린 길로 오도록 벌·알레이와 같은 비행 길찾기를 쓴다. */
    @Override
    protected PathNavigation createNavigation(Level level) {
        FlyingPathNavigation navigation = new FlyingPathNavigation(this, level);
        navigation.setCanOpenDoors(false);
        navigation.setCanFloat(true);
        return navigation;
    }

    /** 바닐라 돌진·무작위 이동보다 먼저 이동을 잡는다. 바닐라 두 Goal은 규칙이 켜져 있으면 스스로 쉰다. */
    @Inject(method = "registerGoals", at = @At("TAIL"))
    private void serverutilities$addPathGoals(CallbackInfo ci) {
        Vex vex = (Vex) (Object) this;
        this.goalSelector.addGoal(3, new VexPathChaseGoal(vex));
        this.goalSelector.addGoal(7, new VexPathWanderGoal(vex));
    }

    /** 소환 직후나 블록이 놓여 벽 속에 있으면 질식하기 전에 빈 공간으로 옮긴다. 옮길 곳이 없어 없앴으면 이번 틱을 멈춘다. */
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void serverutilities$freeIfStuck(CallbackInfo ci) {
        boolean server = !this.level().isClientSide();
        if (server && VexFlightRules.enabled() && VexFlightRules.freeIfStuck((Vex) (Object) this)) {
            ci.cancel();
        }
    }

    /** 바닐라가 매 틱 충돌을 끄는 대입을 막는다. 몹 이동은 서버가 정하므로 서버에서만 충돌을 켠다. */
    @Redirect(method = "tick", at = @At(value = "FIELD",
            target = "Lnet/minecraft/world/entity/monster/Vex;noPhysics:Z", opcode = Opcodes.PUTFIELD, ordinal = 0))
    private void serverutilities$keepCollision(Vex vex, boolean noPhysics) {
        boolean collide = !vex.level().isClientSide() && VexFlightRules.enabled();
        vex.noPhysics = noPhysics && !collide;
    }
}
