package com.daqem.itemrestrictions.networking.clientbound;

import com.daqem.itemrestrictions.ItemRestrictions;
import com.daqem.itemrestrictions.client.screen.ItemRestrictionsScreen;
import com.daqem.itemrestrictions.data.ItemRestrictionManager;
import com.daqem.itemrestrictions.data.RestrictionType;
import com.daqem.itemrestrictions.networking.ItemRestrictionsNetworking;
import dev.architectury.networking.NetworkManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public class ClientboundRestrictionPacket implements CustomPacketPayload {

    private final RestrictionType restrictionType;

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundRestrictionPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull ClientboundRestrictionPacket decode(RegistryFriendlyByteBuf buf) {
            return new ClientboundRestrictionPacket(buf);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ClientboundRestrictionPacket packet) {
            buf.writeEnum(packet.restrictionType);
        }
    };

    public ClientboundRestrictionPacket(RestrictionType restrictionType) {
        this.restrictionType = restrictionType;
    }

    public ClientboundRestrictionPacket(FriendlyByteBuf friendlyByteBuf) {
        this.restrictionType = friendlyByteBuf.readEnum(RestrictionType.class);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ItemRestrictionsNetworking.CLIENTBOUND_RESTRICTION_TYPE;
    }

    public RestrictionType getRestrictionType() {
        return restrictionType;
    }
}
