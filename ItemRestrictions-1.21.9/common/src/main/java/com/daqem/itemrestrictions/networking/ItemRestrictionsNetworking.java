package com.daqem.itemrestrictions.networking;

import com.daqem.itemrestrictions.ItemRestrictions;
import com.daqem.itemrestrictions.client.networking.ClientboundRestrictionPacketHandler;
import com.daqem.itemrestrictions.client.networking.ClientboundUpdateItemRestrictionsPacketHandler;
import com.daqem.itemrestrictions.networking.clientbound.ClientboundRestrictionPacket;
import com.daqem.itemrestrictions.networking.clientbound.ClientboundUpdateItemRestrictionsPacket;
import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface ItemRestrictionsNetworking {

    CustomPacketPayload.Type<ClientboundRestrictionPacket> CLIENTBOUND_RESTRICTION_TYPE = new CustomPacketPayload.Type<>(ItemRestrictions.getId("clientbound_restriction"));
    CustomPacketPayload.Type<ClientboundUpdateItemRestrictionsPacket> CLIENTBOUND_UPDATE_ITEM_RESTRICTIONS_PACKET = new CustomPacketPayload.Type<>(ItemRestrictions.getId("clientbound_update_item_restrictions"));

    static void initClient() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, CLIENTBOUND_RESTRICTION_TYPE, ClientboundRestrictionPacket.STREAM_CODEC, ClientboundRestrictionPacketHandler::handleClientSide);
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, CLIENTBOUND_UPDATE_ITEM_RESTRICTIONS_PACKET, ClientboundUpdateItemRestrictionsPacket.STREAM_CODEC, ClientboundUpdateItemRestrictionsPacketHandler::handleClientSide);
    }

    static void initServer() {
        NetworkManager.registerS2CPayloadType(CLIENTBOUND_RESTRICTION_TYPE, ClientboundRestrictionPacket.STREAM_CODEC);
        NetworkManager.registerS2CPayloadType(CLIENTBOUND_UPDATE_ITEM_RESTRICTIONS_PACKET, ClientboundUpdateItemRestrictionsPacket.STREAM_CODEC);
    }

    static void init() {
        EnvExecutor.runInEnv(Env.CLIENT, () -> ItemRestrictionsNetworking::initClient);
        EnvExecutor.runInEnv(Env.SERVER, () -> ItemRestrictionsNetworking::initServer);
    }
}
