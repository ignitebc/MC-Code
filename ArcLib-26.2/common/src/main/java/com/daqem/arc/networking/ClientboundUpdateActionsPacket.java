package com.daqem.arc.networking;

import com.daqem.arc.api.action.IAction;
import com.daqem.arc.api.action.holder.ActionHolderManager;
import com.daqem.arc.api.action.serializer.IActionSerializer;
import dev.architectury.networking.NetworkManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ClientboundUpdateActionsPacket implements CustomPacketPayload {

    private final List<IAction> actions;

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundUpdateActionsPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull ClientboundUpdateActionsPacket decode(RegistryFriendlyByteBuf buf) {
            return new ClientboundUpdateActionsPacket(buf);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ClientboundUpdateActionsPacket packet) {
            buf.writeCollection(packet.actions,
                    (friendlyByteBuf, action) -> IActionSerializer.toNetwork(action, (RegistryFriendlyByteBuf) friendlyByteBuf));
        }
    };

    public ClientboundUpdateActionsPacket(List<IAction> actions) {
        this.actions = actions;
    }

    public ClientboundUpdateActionsPacket(RegistryFriendlyByteBuf friendlyByteBuf) {
        this.actions = friendlyByteBuf.readList(object -> IActionSerializer.fromNetwork((RegistryFriendlyByteBuf) object));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ArcNetworking.CLIENTBOUND_UPDATE_ACTIONS;
    }

    public List<IAction> getActions() {
        return actions;
    }
}
