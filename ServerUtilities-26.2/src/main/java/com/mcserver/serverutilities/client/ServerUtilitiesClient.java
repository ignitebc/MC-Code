package com.mcserver.serverutilities.client;

import com.mcserver.serverutilities.death.DeathChestBlockEntity;
import com.mcserver.serverutilities.death.DeathChests;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.renderer.blockentity.ChestRenderer;

public final class ServerUtilitiesClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // 유품 상자는 ChestBlockEntity 하위 타입이라 바닐라 상자 렌더러가 일반 상자 재질로 그린다.
        // Fabric 등록기는 접근 확장기 대체 안내로 deprecated 표시만 있고 26.2에서 그대로 동작한다.
        registerChestRenderer();
    }

    @SuppressWarnings("deprecation")
    private static void registerChestRenderer() {
        BlockEntityRendererRegistry.register(DeathChests.BLOCK_ENTITY, ChestRenderer<DeathChestBlockEntity>::new);
    }
}
