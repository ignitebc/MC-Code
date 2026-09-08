package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.death.DeathProtectedPlayer;
import com.mcserver.serverutilities.death.DeathRules;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.CombatTracker;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerDeathMixin implements DeathProtectedPlayer {
    // 기존 월드 및 구버전 Jobs+로의 롤백을 위해 저장 키를 유지한다.
    @Unique private static final String SERVERUTILITIES_PROTECTION_KEY = "JobsPlusDeathItemProtected";
    @Unique private boolean serverutilities$deathProtected;
    @Unique private boolean serverutilities$deathHandled;

    @Override
    public boolean serverutilities$isDeathProtected() { return serverutilities$deathProtected; }

    @Override
    public void serverutilities$setDeathProtected(boolean value) { serverutilities$deathProtected = value; }

    @Redirect(method = "die", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/damagesource/CombatTracker;getDeathMessage()Lnet/minecraft/network/chat/Component;"))
    private Component serverutilities$pvpDeathMessage(CombatTracker tracker, DamageSource source) {
        ServerPlayer victim = (ServerPlayer) (Object) this;
        var attacker = source.getEntity();
        // 투사체의 소유자 및 전투 직후 낙사 등 바닐라가 인정하는 플레이어 처치를 포함한다.
        boolean killedByPlayer = attacker instanceof ServerPlayer && attacker != victim;
        boolean combatAccident = attacker == null && victim.getKillCredit() instanceof ServerPlayer killer && killer != victim;
        if (killedByPlayer || combatAccident) {
            return Component.literal(victim.getName().getString() + " 님이 누군가에게 살해당했습니다.");
        }
        return tracker.getDeathMessage();
    }

    // 취소 가능한 사망 허용 이벤트 대신, 실제 사망의 드롭 직전에 한 번만 처리한다.
    @Inject(method = "die", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;dropAllDeathLoot(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;)V"))
    private void serverutilities$beforeDeathDrops(DamageSource source, CallbackInfo ci) {
        if (!serverutilities$deathHandled) {
            serverutilities$deathHandled = true;
            DeathRules.beforeDrops((ServerPlayer) (Object) this);
        }
    }

    @Inject(method = "restoreFrom(Lnet/minecraft/server/level/ServerPlayer;Z)V", at = @At("TAIL"))
    private void serverutilities$restoreInventory(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
        DeathProtectedPlayer oldState = (DeathProtectedPlayer) oldPlayer;
        if (oldState.serverutilities$isDeathProtected()) {
            ((ServerPlayer) (Object) this).getInventory().replaceWith(oldPlayer.getInventory());
            oldState.serverutilities$setDeathProtected(false);
        }
        serverutilities$deathProtected = false;
        serverutilities$deathHandled = false;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void serverutilities$saveProtection(ValueOutput output, CallbackInfo ci) {
        output.store(SERVERUTILITIES_PROTECTION_KEY, Codec.BOOL, serverutilities$deathProtected);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void serverutilities$readProtection(ValueInput input, CallbackInfo ci) {
        serverutilities$deathProtected = input.read(SERVERUTILITIES_PROTECTION_KEY, Codec.BOOL).orElse(false);
    }
}
