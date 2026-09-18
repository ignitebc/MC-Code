package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.spawn.PersonalSpawnAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class PersonalSpawnMixin implements PersonalSpawnAccess {
    @Unique private static final String SERVERUTILITIES_PERSONAL_SPAWN_KEY = "ServerUtilitiesPersonalSpawn";
    @Unique private BlockPos serverutilities$personalSpawn;

    @Override
    public BlockPos serverutilities$personalSpawn() { return serverutilities$personalSpawn; }

    @Override
    public void serverutilities$setPersonalSpawn(BlockPos position) { serverutilities$personalSpawn = position; }

    // 사망 시 새 ServerPlayer가 만들어지므로 배정 기록을 직접 옮긴다.
    @Inject(method = "restoreFrom(Lnet/minecraft/server/level/ServerPlayer;Z)V", at = @At("TAIL"))
    private void serverutilities$copyPersonalSpawn(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
        serverutilities$personalSpawn = ((PersonalSpawnAccess) oldPlayer).serverutilities$personalSpawn();
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void serverutilities$savePersonalSpawn(ValueOutput output, CallbackInfo ci) {
        if (serverutilities$personalSpawn != null) {
            output.store(SERVERUTILITIES_PERSONAL_SPAWN_KEY, BlockPos.CODEC, serverutilities$personalSpawn);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void serverutilities$readPersonalSpawn(ValueInput input, CallbackInfo ci) {
        serverutilities$personalSpawn = input.read(SERVERUTILITIES_PERSONAL_SPAWN_KEY, BlockPos.CODEC).orElse(null);
    }
}
