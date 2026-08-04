package com.autovw.advancednetherite.network;

import com.autovw.advancednetherite.common.pet.PetManager;
import com.autovw.advancednetherite.common.pet.PetRecord;
import com.autovw.advancednetherite.common.pet.PetStorage;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * 펫 시스템의 서버 측 네트워킹·수명주기 등록.
 */
public final class PetNetworking
{
    private PetNetworking()
    {
    }

    public static void register()
    {
        PayloadTypeRegistry.playS2C().register(PetListSyncPayload.TYPE, PetListSyncPayload.STREAM_CODEC);
        PayloadTypeRegistry.playC2S().register(PetTogglePayload.TYPE, PetTogglePayload.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(PetTogglePayload.TYPE,
                (payload, context) -> PetManager.togglePet(context.player(), payload.recordId()));

        PetManager.setSyncHandler(PetNetworking::sendPetList);

        ServerLifecycleEvents.SERVER_STARTING.register(PetStorage::load);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            PetStorage.save();
            PetManager.clearRuntimeState();
        });
        ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) -> PetManager.handlePlayerJoin(handler.getPlayer()));
        ServerPlayConnectionEvents.DISCONNECT.register(
                (handler, server) -> PetManager.handlePlayerQuit(handler.getPlayer()));
    }

    private static void sendPetList(ServerPlayer player)
    {
        List<PetStatusEntry> entries = new ArrayList<>();
        for (PetRecord record : PetStorage.getPets(player.getUUID()))
        {
            entries.add(new PetStatusEntry(record.id(), record.petTypeId(), record.enabled()));
        }
        ServerPlayNetworking.send(player, new PetListSyncPayload(entries));
    }
}
