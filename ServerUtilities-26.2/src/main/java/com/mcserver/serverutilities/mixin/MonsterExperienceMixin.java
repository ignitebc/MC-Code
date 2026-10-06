package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.monster.MonsterEquipmentAccess;
import com.mcserver.serverutilities.monster.MonsterExperience;
import com.mcserver.serverutilities.monster.MonsterLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MonsterExperienceMixin {
    @Unique private static final long SERVERUTILITIES_NEVER_HURT_BY_PLAYER = Long.MIN_VALUE;

    @Shadow protected int lastHurtByPlayerMemoryTime;

    /** 바닐라가 플레이어의 공격을 기록한 게임 시각. 바닐라 기억 시간(5초)이 지난 뒤의 구슬 판정에 쓴다. */
    @Unique private long serverutilities$lastPlayerHurtGameTime = SERVERUTILITIES_NEVER_HURT_BY_PLAYER;
    /** 지금 구슬을 떨어뜨리는 죽음이 공격자 없는 피해로 일어났는지 */
    @Unique private boolean serverutilities$diedWithoutAttacker;

    @Inject(method = "setLastHurtByPlayer(Lnet/minecraft/world/entity/EntityReference;I)V", at = @At("TAIL"))
    private void serverutilities$rememberPlayerHurt(EntityReference<Player> player, int memoryTime, CallbackInfo ci) {
        serverutilities$lastPlayerHurtGameTime = ((LivingEntity) (Object) this).level().getGameTime();
    }

    @Inject(method = "dropExperience", at = @At("HEAD"))
    private void serverutilities$rememberKiller(ServerLevel level, Entity killer, CallbackInfo ci) {
        serverutilities$diedWithoutAttacker = killer == null;
    }

    /**
     * 불·독처럼 공격자 없는 피해로 죽었을 때, 플레이어가 20초 안에 공격했으면 바닐라 기억 시간이 지나도
     * 구슬을 떨어뜨린다. 다른 생물이 죽였으면 바닐라 판정 그대로다.
     */
    @Redirect(method = "dropExperience", at = @At(value = "FIELD",
            target = "Lnet/minecraft/world/entity/LivingEntity;lastHurtByPlayerMemoryTime:I", opcode = Opcodes.GETFIELD))
    private int serverutilities$extendPlayerHurtMemory(LivingEntity self) {
        boolean vanillaCredit = lastHurtByPlayerMemoryTime > 0;
        if (vanillaCredit || !serverutilities$diedWithoutAttacker) return lastHurtByPlayerMemoryTime;

        boolean everHurtByPlayer = serverutilities$lastPlayerHurtGameTime != SERVERUTILITIES_NEVER_HURT_BY_PLAYER;
        boolean hurtByPlayerRecently = everHurtByPlayer && MonsterExperience.withinPlayerHurtCredit(
                serverutilities$lastPlayerHurtGameTime, self.level().getGameTime());
        // 바닐라 조건은 기억 시간이 0보다 큰지만 보므로 1을 돌려주면 플레이어 처치로 인정된다.
        return hurtByPlayerRecently ? 1 : lastHurtByPlayerMemoryTime;
    }

    /** 떨어뜨리는 구슬에 몬스터 위험 단계 배율을 곱한다. 레벨이 없는 몹은 바닐라 그대로다. */
    @ModifyArg(method = "dropExperience", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/ExperienceOrb;award(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/phys/Vec3;I)V"),
            index = 2)
    private int serverutilities$scaleByMonsterLevel(int amount) {
        if (!((Object) this instanceof MonsterEquipmentAccess monster)) return amount;
        boolean creeper = (Object) this instanceof Creeper;
        int stage = MonsterLevel.stage(monster.serverutilities$monsterLevel(), creeper);
        return MonsterExperience.scale(amount, stage);
    }
}
