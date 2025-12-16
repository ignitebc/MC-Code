package com.daqem.itemrestrictions;

import com.daqem.arc.registry.ArcRegistry;
import com.daqem.itemrestrictions.config.ItemRestrictionsConfig;
import com.daqem.itemrestrictions.data.ItemRestrictionManager;
import com.daqem.itemrestrictions.event.ArcEvents;
import com.daqem.itemrestrictions.networking.ItemRestrictionsNetworking;
import com.mojang.logging.LogUtils;
import dev.architectury.registry.ReloadListenerRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;

public class ItemRestrictions {
    public static final String MOD_ID = "itemrestrictions";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        ItemRestrictionsConfig.init();
        registerEvents();
        initRegistry();
        initNetworking();
        ReloadListenerRegistry.register(PackType.SERVER_DATA, new ItemRestrictionManager(), getId(MOD_ID));
    }

    private static void initNetworking() {
        ItemRestrictionsNetworking.init();
    }

    private static void initRegistry() {
        ArcRegistry.init();
    }

    private static void registerEvents() {
        ArcEvents.registerEvents();
    }

    public static ResourceLocation getId(String id) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, id);
    }

    public static MutableComponent translatable(String str) {
        return Component.translatable(MOD_ID + "." + str);
    }

    public static MutableComponent translatable(String str, Object... objects) {
        return Component.translatable(MOD_ID + "." + str, objects);
    }

    @SuppressWarnings("unused")
    public static MutableComponent literal(String str) {
        return Component.literal(str);
    }

    public static boolean isDebugEnvironment() {
        return ItemRestrictionsConfig.isDebug.get();
    }
}
