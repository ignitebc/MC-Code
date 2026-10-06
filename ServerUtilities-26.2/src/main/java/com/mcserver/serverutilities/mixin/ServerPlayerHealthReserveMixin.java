package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.tier.EquipmentHealthReserve;
import com.mcserver.serverutilities.tier.HealthReservePlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
abstract class ServerPlayerHealthReserveMixin implements HealthReservePlayer {
    @Unique private static final String SERVERUTILITIES_HEALTH_RESERVE_KEY = "ServerUtilitiesHealthReserve";
    /** 바닐라가 체력을 저장하는 키 */
    @Unique private static final String SERVERUTILITIES_VANILLA_HEALTH_KEY = "Health";

    @Unique private float serverutilities$healthReserve;
    @Unique private boolean serverutilities$respawnHealPending;

    @Override
    public float serverutilities$getHealthReserve() { return serverutilities$healthReserve; }

    @Override
    public void serverutilities$setHealthReserve(float reserve) { serverutilities$healthReserve = reserve; }

    @Override
    public boolean serverutilities$isRespawnHealPending() { return serverutilities$respawnHealPending; }

    @Override
    public void serverutilities$setRespawnHealPending(boolean pending) { serverutilities$respawnHealPending = pending; }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void serverutilities$saveHealthReserve(ValueOutput output, CallbackInfo ci) {
        if (serverutilities$healthReserve > 0.0F) {
            output.putFloat(SERVERUTILITIES_HEALTH_RESERVE_KEY, serverutilities$healthReserve);
        }
    }

    // 불러올 때는 장비 수정자가 아직 붙지 않아 저장된 체력이 장비 없는 최대 체력으로 잘린다.
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void serverutilities$readHealthReserve(ValueInput input, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        serverutilities$healthReserve = input.getFloatOr(SERVERUTILITIES_HEALTH_RESERVE_KEY, 0.0F);
        float savedHealth = input.getFloatOr(SERVERUTILITIES_VANILLA_HEALTH_KEY, player.getHealth());
        EquipmentHealthReserve.keepHealthClippedWithoutEquipment(player, savedHealth);
    }

    // 엔드에서 나올 때는 새 플레이어가 장비 없는 최대 체력으로 이전 체력을 받는다.
    // 사망 후 부활 때는 장비 없는 최대 체력으로 채워지므로 장비가 붙은 뒤 다시 채운다.
    @Inject(method = "restoreFrom(Lnet/minecraft/server/level/ServerPlayer;Z)V", at = @At("TAIL"))
    private void serverutilities$restoreHealthReserve(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
        if (!alive) {
            serverutilities$healthReserve = 0.0F;
            serverutilities$respawnHealPending = true;
            return;
        }

        ServerPlayer player = (ServerPlayer) (Object) this;
        serverutilities$healthReserve = ((HealthReservePlayer) oldPlayer).serverutilities$getHealthReserve();
        EquipmentHealthReserve.keepHealthClippedWithoutEquipment(player, oldPlayer.getHealth());
    }
}
