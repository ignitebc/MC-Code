package com.autovw.advancednetherite;

import com.autovw.advancednetherite.common.ModLootTableModifiers;
import com.autovw.advancednetherite.common.randombox.RandomBoxConfigManager;
import com.autovw.advancednetherite.network.PetNetworking;
import com.autovw.advancednetherite.config.ConfigHelper;
import com.autovw.advancednetherite.config.TempConfig;
import com.autovw.advancednetherite.core.registry.ModBlockRegistry;
import com.autovw.advancednetherite.core.registry.ModEntityRegistry;
import com.autovw.advancednetherite.core.registry.ModItemRegistry;
import com.autovw.advancednetherite.registry.FabricRegistryHelper;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;

/**
 * @author Autovw
 */
public class AdvancedNetheriteFabric implements ModInitializer
{
    public static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize()
    {
        AdvancedNetherite.init(FabricPlatformHelper.getInstance());
        AdvancedNetherite.setRegistryHelper(new FabricRegistryHelper());

        ModBlockRegistry.registerBlocks();
        ModEntityRegistry.registerEntityTypes();
        ModItemRegistry.registerItems();

        AdvancedNetheriteTab.registerTab();

        ModLootTableModifiers.modifyTables();

        PetNetworking.register();

        ConfigHelper.registerClientConfig(() -> TempConfig.CLIENT);
        ConfigHelper.registerCommonConfig(() -> TempConfig.COMMON);
        ConfigHelper.registerServerConfig(() -> TempConfig.SERVER);

        // /reload 또는 데이터팩 변경 시 이미 읽어 둔 랜덤박스 설정이 남지 않도록 캐시를 비운다.
        ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(
                (server, resourceManager, success) -> RandomBoxConfigManager.clearCache());
        // 싱글플레이에서 다른 월드(다른 데이터팩)로 이동해도 이전 설정이 남지 않도록 서버 종료 시에도 비운다.
        ServerLifecycleEvents.SERVER_STOPPED.register(
                server -> RandomBoxConfigManager.clearCache());
    }
}
