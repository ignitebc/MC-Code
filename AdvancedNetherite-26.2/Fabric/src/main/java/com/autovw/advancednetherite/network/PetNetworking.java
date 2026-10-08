package com.autovw.advancednetherite.network;

import com.autovw.advancednetherite.common.pet.PetAttackMode;
import com.autovw.advancednetherite.common.pet.PetManager;
import com.autovw.advancednetherite.common.pet.PetRecord;
import com.autovw.advancednetherite.common.pet.PetStorage;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
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
    /** 밀린 펫 기록 변경을 파일에 반영하는 주기(30초) */
    private static final int SAVE_INTERVAL_TICKS = 600;

    private PetNetworking()
    {
    }

    public static void register()
    {
        PayloadTypeRegistry.clientboundPlay().register(PetListSyncPayload.TYPE, PetListSyncPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(PetTogglePayload.TYPE, PetTogglePayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(PetRenamePayload.TYPE, PetRenamePayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(PetAttackModePayload.TYPE, PetAttackModePayload.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(PetTogglePayload.TYPE,
                (payload, context) -> PetManager.togglePet(context.player(), payload.recordId()));
        ServerPlayNetworking.registerGlobalReceiver(PetRenamePayload.TYPE,
                (payload, context) -> PetManager.renamePet(context.player(), payload.recordId(), payload.name()));
        ServerPlayNetworking.registerGlobalReceiver(PetAttackModePayload.TYPE,
                (payload, context) -> PetManager.setAttackMode(context.player(), toAttackMode(payload.autoAttack())));

        PetManager.setSyncHandler(PetNetworking::sendPetList);

        ServerLifecycleEvents.SERVER_STARTING.register(PetStorage::load);
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            PetManager.tick(server);
            if (server.getTickCount() % SAVE_INTERVAL_TICKS == 0)
            {
                PetStorage.saveIfDirty();
            }
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            // 접속 종료 처리보다 먼저 불려 살아 있는 펫 목록이 비기 전이다. 여기서 체력을 남겨야 재시작 후에도 유지된다.
            PetManager.storeLiveHealth();
            PetStorage.save();
            PetManager.clearRuntimeState();
        });
        ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) -> PetManager.handlePlayerJoin(handler.getPlayer()));
        ServerPlayConnectionEvents.DISCONNECT.register(
                (handler, server) -> PetManager.handlePlayerQuit(handler.getPlayer()));
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            if (entity instanceof ServerPlayer player)
            {
                PetManager.handlePlayerDeath(player);
            }
        });
        ServerPlayerEvents.AFTER_RESPAWN.register(
                (oldPlayer, newPlayer, alive) -> PetManager.handlePlayerRespawn(newPlayer));
    }

    private static void sendPetList(ServerPlayer player)
    {
        long now = System.currentTimeMillis();
        List<PetStatusEntry> entries = new ArrayList<>();
        for (PetRecord record : PetStorage.getPets(player.getUUID()))
        {
            float health = PetManager.currentHealth(record, now);
            entries.add(new PetStatusEntry(record.id(), record.petTypeId(), record.enabled(), record.name(),
                    record.level(), record.exp(), health, Math.max(0L, record.reviveAtMillis() - now)));
        }
        boolean autoAttack = PetManager.getAttackMode(player) == PetAttackMode.AUTO;
        ServerPlayNetworking.send(player, new PetListSyncPayload(entries, autoAttack));
    }

    private static PetAttackMode toAttackMode(boolean autoAttack)
    {
        if (autoAttack)
        {
            return PetAttackMode.AUTO;
        }
        return PetAttackMode.NORMAL;
    }
}
