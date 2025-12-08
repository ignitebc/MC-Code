package com.daqem.arc.networking;

import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.arc.api.action.holder.serializer.IActionHolderSerializer;
import com.daqem.arc.client.gui.holder.ActionHoldersScreen;
import dev.architectury.networking.NetworkManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ClientboundActionHoldersScreenPacket implements CustomPacketPayload {

    private final List<IActionHolder> actionHolders;

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundActionHoldersScreenPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull ClientboundActionHoldersScreenPacket decode(RegistryFriendlyByteBuf buf) {
            return new ClientboundActionHoldersScreenPacket(buf);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ClientboundActionHoldersScreenPacket packet) {
            buf.writeCollection(packet.actionHolders,
                    (friendlyByteBuf, action) -> IActionHolderSerializer.toNetwork(action, (RegistryFriendlyByteBuf) friendlyByteBuf));
        }
    };

    public ClientboundActionHoldersScreenPacket(List<IActionHolder> actionHolders) {
        this.actionHolders = actionHolders;
    }

    public ClientboundActionHoldersScreenPacket(RegistryFriendlyByteBuf buf) {
        this.actionHolders = buf.readList(friendlyByteBuf1 -> IActionHolderSerializer.fromNetwork((RegistryFriendlyByteBuf) friendlyByteBuf1));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ArcNetworking.CLIENTBOUND_ACTION_HOLDERS_SCREEN_PACKET;
    }

    public List<IActionHolder> getActionHolders() {
        return actionHolders;
    }
}
