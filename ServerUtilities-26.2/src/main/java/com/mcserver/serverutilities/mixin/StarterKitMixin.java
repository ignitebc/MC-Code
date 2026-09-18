package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.starter.StarterKitAccess;
import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class StarterKitMixin implements StarterKitAccess {
    @Unique private static final String SERVERUTILITIES_STARTER_KIT_KEY = "ServerUtilitiesStarterKit";
    @Unique private boolean serverutilities$starterKitGiven;

    @Override
    public boolean serverutilities$starterKitGiven() { return serverutilities$starterKitGiven; }

    @Override
    public void serverutilities$setStarterKitGiven(boolean value) { serverutilities$starterKitGiven = value; }

    // 사망 시 새 ServerPlayer가 만들어지므로 지급 기록을 옮겨야 재지급되지 않는다.
    @Inject(method = "restoreFrom(Lnet/minecraft/server/level/ServerPlayer;Z)V", at = @At("TAIL"))
    private void serverutilities$copyStarterKit(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
        serverutilities$starterKitGiven = ((StarterKitAccess) oldPlayer).serverutilities$starterKitGiven();
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void serverutilities$saveStarterKit(ValueOutput output, CallbackInfo ci) {
        output.store(SERVERUTILITIES_STARTER_KIT_KEY, Codec.BOOL, serverutilities$starterKitGiven);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void serverutilities$readStarterKit(ValueInput input, CallbackInfo ci) {
        serverutilities$starterKitGiven = input.read(SERVERUTILITIES_STARTER_KIT_KEY, Codec.BOOL).orElse(false);
    }
}
