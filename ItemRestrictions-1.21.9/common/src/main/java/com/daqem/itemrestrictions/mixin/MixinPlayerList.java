package com.daqem.itemrestrictions.mixin;

import com.daqem.itemrestrictions.ItemRestrictions;
import com.daqem.itemrestrictions.data.ItemRestrictionManager;
import com.daqem.itemrestrictions.networking.clientbound.ClientboundUpdateItemRestrictionsPacket;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PlayerList.class)
public abstract class MixinPlayerList {

    @Shadow @Final private List<ServerPlayer> players;

    @Inject(at = @At("TAIL"), method = "reloadResources")
    private void reloadResources(CallbackInfo ci) {
        for (ServerPlayer player : this.players) {
            if (ItemRestrictions.isDebugEnvironment()) {
                ItemRestrictions.LOGGER.info("Sending item restrictions to player {}", player.getName().getString());
            }
            NetworkManager.sendToPlayer(player, new ClientboundUpdateItemRestrictionsPacket(ItemRestrictionManager.getInstance().getItemRestrictions()));
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;sendPlayerPermissionLevel(Lnet/minecraft/server/level/ServerPlayer;)V", shift = At.Shift.BEFORE), method = "placeNewPlayer")
    private void placeNewPlayer(Connection connection, ServerPlayer serverPlayer, CommonListenerCookie commonListenerCookie, CallbackInfo ci) {
        NetworkManager.sendToPlayer(serverPlayer, new ClientboundUpdateItemRestrictionsPacket(ItemRestrictionManager.getInstance().getItemRestrictions()));
    }
}
