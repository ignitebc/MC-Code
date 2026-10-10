package cn.sh1rocu.tacz;

import cn.sh1rocu.tacz.api.event.*;
import cn.sh1rocu.tacz.util.forge.EnumArgument;
import cn.sh1rocu.tacz.util.forge.PartialNBTIngredient;
import cn.sh1rocu.tacz.util.forge.StrictNBTIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import com.tacz.guns.GunMod;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.api.event.common.EntityKillByGunEvent;
import com.tacz.guns.api.event.server.AmmoHitBlockEvent;
import com.tacz.guns.entity.ai.GunfireAlert;
import com.tacz.guns.config.ClientConfig;
import com.tacz.guns.config.CommonConfig;
import com.tacz.guns.config.PreLoadConfig;
import com.tacz.guns.config.ServerConfig;
import com.tacz.guns.event.*;
import com.tacz.guns.event.ammo.BellRing;
import com.tacz.guns.event.ammo.DestroyGlassBlock;
import com.tacz.guns.init.CapabilityRegistry;
import com.tacz.guns.init.CommandRegistry;
import com.tacz.guns.init.CommonRegistry;
import com.tacz.guns.resource.CommonAssetsManager;
import fuzs.forgeconfigapiport.fabric.api.v5.ConfigRegistry;
import fuzs.forgeconfigapiport.fabric.api.v5.ModConfigEvents;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.item.v1.EnchantmentEvents;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.config.ModConfig;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public class TaCZFabric implements ModInitializer {
    public static final Identifier HIGHEST = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "event_highest_priority");
    public static final Identifier HIGH = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "event_high_priority");
    public static final Identifier LOW = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "event_low_priority");
    public static final Identifier LOWEST = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "event_lowest_priority");

    @Nullable
    private static WeakReference<MinecraftServer> server;

    @Nullable
    public static MinecraftServer getServer() {
        if (server == null) {
            return null;
        }
        return server.get();
    }

    @Override
    public void onInitialize() {
        // 설정 파일을 먼저 불러온다. 이 단계는 표준 Forge 설정 파일 로드보다 이르다
        PreLoadConfig.init();

        ConfigRegistry.INSTANCE.register(GunMod.MOD_ID, ModConfig.Type.COMMON, CommonConfig.init());
        ConfigRegistry.INSTANCE.register(GunMod.MOD_ID, ModConfig.Type.SERVER, ServerConfig.init());
        ConfigRegistry.INSTANCE.register(GunMod.MOD_ID, ModConfig.Type.CLIENT, ClientConfig.init());

        GunMod.setup();
        CommandRegistry.onServerStaring();
        // Forge 시절의 사용자 정의 Ingredient 종류를 등록한다.
        //
        // 이 두 클래스는 계속 util/forge 아래 있었지만 [한 번도 등록된 적이 없었다] — 원본 1.21.1은 이 메서드의
        // 같은 위치에서 CustomIngredientSerializer.register(NBTIngredient.Serializer.INSTANCE)를 호출했는데,
        // 이식할 때 빠졌다. 등록하지 않으면 오류가 나는 대신 Fabric의 CustomIngredientImpl.CODEC이
        // REGISTERED_SERIALIZERS에서 해당 ID를 찾지 못하고
        // "Unknown custom ingredient serializer"를 돌려줘 제작법 전체 해석이 실패한다.
        //
        // 실제 증상: 외부 팩에서 forge:partial_nbt로 쓴 제작법 2개
        // ("마이카의 권총" = 콜트 M1892 2자루, "m1887_hc" = m1887 + 도끼)의
        // 재료 칸이 비어 제작할 수 없었다.
        CustomIngredientSerializer.register(PartialNBTIngredient.Serializer.INSTANCE);
        CustomIngredientSerializer.register(StrictNBTIngredient.Serializer.INSTANCE);

        Class<? extends EnumArgument<?>> enumArgumentClass = (Class<? extends EnumArgument<?>>) (Class) EnumArgument.class;
        ArgumentTypeRegistry.registerArgumentType(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "enum_argument"), enumArgumentClass,
                EnumArgument.Info.INSTANCE);
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
            CommonLoadPack.loadGunPack();
        }
        ServerLifecycleEvents.SERVER_STARTING.register((server) -> TaCZFabric.server = new WeakReference<>(server));

        subscribeEvents();
    }

    private void subscribeEvents() {
        CapabilityRegistry.init();

        AddReloadListenerEvent.CALLBACK.register(CommonAssetsManager::onReload);
        CommonLifecycleEvents.TAGS_LOADED.register(CommonAssetsManager::onReload);
        ServerLifecycleEvents.SERVER_STOPPED.register(CommonAssetsManager::onServerStopped);
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(CommonAssetsManager::OnDatapackSync);

        ServerLifecycleEvents.SERVER_STARTED.register(server -> CommonRegistry.onLoadComplete());

        AmmoHitBlockEvent.CALLBACK.register(BellRing::onAmmoHitBlock);

        AmmoHitBlockEvent.CALLBACK.register(DestroyGlassBlock::onAmmoHitBlock);

        // 플레이어가 총으로 몬스터를 맞히거나 죽이면 주변 몬스터에게 알린다.
        EntityHurtByGunEvent.POST.register(GunfireAlert::onHurtByGun);
        EntityKillByGunEvent.CALLBACK.register(GunfireAlert::onKillByGun);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> GunfireAlert.clear());

        LivingHurtEvent.CALLBACK.register(LOW, EntityDamageEvent::onLivingHurt);

        EnchantmentEvents.MODIFY_WITH_LOOKUP.register(BulletProjectileProtection::onModifyEnchantment);

        PlayerTickEvent.END.register(HitboxHelperEvent::onPlayerTick);
        PlayerEvent.LOGGED_OUT.register(HitboxHelperEvent::onPlayerLoggedOut);

        LivingKnockBackEvent.CALLBACK.register(KnockbackChange::onKnockback);

        ModConfigEvents.loading(GunMod.MOD_ID).register(LoadingConfigEvent::onLoadingConfig);
        ModConfigEvents.reloading(GunMod.MOD_ID).register(LoadingConfigEvent::onReloadingConfig);

        ServerPlayerEvents.AFTER_RESPAWN.register(PlayerRespawnEvent::onPlayerRespawn);

        AttackBlockCallback.EVENT.register(PreventGunClick::onLeftClickBlock);

        ServerTickEvents.START_SERVER_TICK.register(ServerTickEvent::onServerTick);
        ServerTickEvents.END_SERVER_TICK.register(ServerTickEvent::onServerTick);

        EntityJoinLevelEvent.CALLBACK.register(SyncBaseTimestamp::onPlayerJoinWorld);

        EntityTrackingEvents.START_TRACKING.register(SyncedEntityDataEvent::onStartTracking);
        EntityJoinLevelEvent.CALLBACK.register(SyncedEntityDataEvent::onPlayerJoinWorld);
        ServerPlayerEvents.COPY_FROM.register(SyncedEntityDataEvent::onPlayerClone);
        ServerTickEvents.END_SERVER_TICK.register(SyncedEntityDataEvent::onServerTick);

        ServerEntityLevelChangeEvents.AFTER_ENTITY_CHANGE_LEVEL.register(TravelToDimensionEvent::onTravelToDimension);
    }
}
