package com.daqem.itemrestrictions.networking.clientbound;

import com.daqem.itemrestrictions.data.ItemRestriction;
import com.daqem.itemrestrictions.data.ItemRestrictionManager;
import com.daqem.itemrestrictions.networking.ItemRestrictionsNetworking;
import dev.architectury.networking.NetworkManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ClientboundUpdateItemRestrictionsPacket implements CustomPacketPayload {

    private final List<ItemRestriction> itemRestrictions;

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundUpdateItemRestrictionsPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull ClientboundUpdateItemRestrictionsPacket decode(RegistryFriendlyByteBuf buf) {
            return new ClientboundUpdateItemRestrictionsPacket(buf);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ClientboundUpdateItemRestrictionsPacket packet) {
            buf.writeCollection(packet.itemRestrictions, (buf1, value) ->
                    ItemRestriction.Serializer.toNetwork((RegistryFriendlyByteBuf) buf1, value));
        }
    };

    public ClientboundUpdateItemRestrictionsPacket(List<ItemRestriction> itemRestrictions) {
        this.itemRestrictions = itemRestrictions;
    }

    public ClientboundUpdateItemRestrictionsPacket(RegistryFriendlyByteBuf buf) {
        this.itemRestrictions = buf.readList(value -> ItemRestriction.Serializer.fromNetwork(buf));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ItemRestrictionsNetworking.CLIENTBOUND_UPDATE_ITEM_RESTRICTIONS_PACKET;
    }

    public List<ItemRestriction> getItemRestrictions() {
        return itemRestrictions;
    }
}
