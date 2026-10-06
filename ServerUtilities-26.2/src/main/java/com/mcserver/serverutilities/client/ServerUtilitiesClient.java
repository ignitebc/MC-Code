package com.mcserver.serverutilities.client;

import com.mcserver.serverutilities.death.DeathChestBlockEntity;
import com.mcserver.serverutilities.death.DeathChests;
import com.mcserver.serverutilities.monster.MonsterLevelPayload;
import com.mcserver.serverutilities.tier.EquipmentTierSummary;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.renderer.blockentity.ChestRenderer;

public final class ServerUtilitiesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // 유품 상자는 ChestBlockEntity 하위 타입이라 바닐라 상자 렌더러가 일반 상자 재질로 그린다.
        // Fabric 등록기는 접근 확장기 대체 안내로 deprecated 표시만 있고 26.2에서 그대로 동작한다.
        registerChestRenderer();
        // 몬스터 머리 위 레벨은 서버가 알려 준 값을 몬스터에 기록해 두고 렌더러 Mixin이 그린다.
        ClientPlayNetworking.registerGlobalReceiver(MonsterLevelPayload.TYPE, MonsterLevelLabel::receive);
        // 장비 등급 툴팁은 서버에 글꼴이 없어 공백 한 칸으로 잇고, 클라이언트에서만 글꼴 폭으로 수치 칸을 맞춘다.
        EquipmentTierSummary.setRowLayout(TooltipColumnLayout::align);
    }

    @SuppressWarnings("deprecation")
    private static void registerChestRenderer() {
        BlockEntityRendererRegistry.register(DeathChests.BLOCK_ENTITY, ChestRenderer<DeathChestBlockEntity>::new);
    }
}
