package com.daqem.arc.networking;

import com.daqem.arc.api.action.holder.ActionHolderManager;
import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.arc.api.player.ArcClientPlayer;
import dev.architectury.networking.NetworkManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ClientboundSyncPlayerActionHoldersPacket implements CustomPacketPayload {

    private final List<IActionHolder> actionHolders;

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSyncPlayerActionHoldersPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull ClientboundSyncPlayerActionHoldersPacket decode(RegistryFriendlyByteBuf buf) {
            return new ClientboundSyncPlayerActionHoldersPacket(buf);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ClientboundSyncPlayerActionHoldersPacket packet) {
            buf.writeInt(packet.actionHolders.size());
            for (IActionHolder actionHolder : packet.actionHolders) {
                buf.writeResourceLocation(actionHolder.getLocation());
            }
        }
    };

    public ClientboundSyncPlayerActionHoldersPacket(List<IActionHolder> actionHolders) {
        this.actionHolders = actionHolders;
    }

    public ClientboundSyncPlayerActionHoldersPacket(RegistryFriendlyByteBuf friendlyByteBuf) {
        List<ResourceLocation> actionHolderLocations = new ArrayList<>();
        int size = friendlyByteBuf.readInt();
        for (int i = 0; i < size; i++) {
            actionHolderLocations.add(friendlyByteBuf.readResourceLocation());
        }
        this.actionHolders = ActionHolderManager.getInstance().getActionHolders(actionHolderLocations);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ArcNetworking.CLIENTBOUND_SYNC_PLAYER_ACTION_HOLDERS;
    }

    public List<IActionHolder> getActionHolders() {
        return actionHolders;
    }
}
