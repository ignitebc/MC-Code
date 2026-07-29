package com.daqem.arc.networking;

import com.daqem.arc.Arc;

import com.daqem.arc.client.networking.*;
import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface ArcNetworking {

    CustomPacketPayload.Type<ClientboundUpdateActionsPacket> CLIENTBOUND_UPDATE_ACTIONS = new CustomPacketPayload.Type<>(Arc.getId("clientbound_update_actions"));
    CustomPacketPayload.Type<ClientboundUpdateActionHoldersPacket> CLIENTBOUND_UPDATE_ACTION_HOLDERS = new CustomPacketPayload.Type<>(Arc.getId("clientbound_update_action_holders"));
    CustomPacketPayload.Type<ClientboundActionScreenPacket> CLIENTBOUND_ACTION_SCREEN = new CustomPacketPayload.Type<>(Arc.getId("clientbound_action_screen"));
    CustomPacketPayload.Type<ClientboundSyncPlayerActionHoldersPacket> CLIENTBOUND_SYNC_PLAYER_ACTION_HOLDERS = new CustomPacketPayload.Type<>(Arc.getId("clientbound_sync_player_action_holders"));
    CustomPacketPayload.Type<ClientboundActionHoldersScreenPacket> CLIENTBOUND_ACTION_HOLDERS_SCREEN_PACKET = new CustomPacketPayload.Type<>(Arc.getId("clientbound_action_holders_screen_packet"));

    static void initClient() {
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, CLIENTBOUND_UPDATE_ACTIONS, ClientboundUpdateActionsPacket.STREAM_CODEC, ClientboundUpdateActionsPacketHandler::handleClientSide);
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, CLIENTBOUND_UPDATE_ACTION_HOLDERS, ClientboundUpdateActionHoldersPacket.STREAM_CODEC, ClientboundUpdateActionHoldersPacketHandler::handleClientSide);
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, CLIENTBOUND_ACTION_SCREEN, ClientboundActionScreenPacket.STREAM_CODEC, ClientboundActionScreenPacketHandler::handleClientSide);
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, CLIENTBOUND_SYNC_PLAYER_ACTION_HOLDERS, ClientboundSyncPlayerActionHoldersPacket.STREAM_CODEC, ClientboundSyncPlayerActionHoldersPacketHandler::handleClientSide);
        NetworkManager.registerReceiver(NetworkManager.Side.S2C, CLIENTBOUND_ACTION_HOLDERS_SCREEN_PACKET, ClientboundActionHoldersScreenPacket.STREAM_CODEC, ClientboundActionHoldersScreenPacketHandler::handleClientSide);
    }

    static void initServer() {
        NetworkManager.registerS2CPayloadType(CLIENTBOUND_UPDATE_ACTIONS, ClientboundUpdateActionsPacket.STREAM_CODEC);
        NetworkManager.registerS2CPayloadType(CLIENTBOUND_UPDATE_ACTION_HOLDERS, ClientboundUpdateActionHoldersPacket.STREAM_CODEC);
        NetworkManager.registerS2CPayloadType(CLIENTBOUND_ACTION_SCREEN, ClientboundActionScreenPacket.STREAM_CODEC);
        NetworkManager.registerS2CPayloadType(CLIENTBOUND_SYNC_PLAYER_ACTION_HOLDERS, ClientboundSyncPlayerActionHoldersPacket.STREAM_CODEC);
        NetworkManager.registerS2CPayloadType(CLIENTBOUND_ACTION_HOLDERS_SCREEN_PACKET, ClientboundActionHoldersScreenPacket.STREAM_CODEC);
    }

    static void init() {
        EnvExecutor.runInEnv(Env.CLIENT, () -> ArcNetworking::initClient);
        EnvExecutor.runInEnv(Env.SERVER, () -> ArcNetworking::initServer);
    }
}
