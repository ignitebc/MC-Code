package cn.sh1rocu.tacz.client;

import cn.sh1rocu.simplebedrockmodel.api.event.ViewportEvent;
import cn.sh1rocu.simplebedrockmodel.api.event.RenderTickEvent;
import cn.sh1rocu.tacz.api.event.*;
import cn.sh1rocu.tacz.api.extension.IItem;
import com.tacz.guns.api.client.event.BeforeRenderHandEvent;
import com.tacz.guns.api.client.event.RenderItemInHandBobEvent;
import com.tacz.guns.api.client.event.SwapItemWithOffHand;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.api.event.common.EntityKillByGunEvent;
import com.tacz.guns.api.event.common.GunFireEvent;
import com.tacz.guns.client.animation.screen.RefitTransform;
import com.tacz.guns.client.event.*;
import com.tacz.guns.client.init.ClientSetupEvent;
import com.tacz.guns.client.init.ModContainerScreen;
import com.tacz.guns.client.init.ModEntitiesRender;
import com.tacz.guns.client.init.ParticleFactories;
import com.tacz.guns.client.renderer.block.GunSmithTableRenderer;
import com.tacz.guns.client.renderer.block.StatueRenderer;
import com.tacz.guns.client.renderer.block.TargetRenderer;
import com.tacz.guns.client.renderer.feature.TaczFeatureRenderers;
import com.tacz.guns.client.renderer.item.AmmoBoxStatueProperty;
import com.tacz.guns.client.renderer.item.TaczDynamicItemModel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperties;
import com.tacz.guns.client.input.*;
import com.tacz.guns.client.sound.SoundPlayManager;
import com.tacz.guns.init.CommonRegistry;
import com.tacz.guns.init.ModBlocks;
import com.tacz.guns.network.NetworkHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import cn.sh1rocu.tacz.compat.fabric.BuiltinItemRendererRegistry;
import net.minecraft.core.registries.BuiltInRegistries;

public class TaCZFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // 26.2 클라이언트 아이템 JSON은 사용자 정의 ItemModel 종류인 tacz:dynamic_item을 쓴다.
        TaczDynamicItemModel.registerType();
        // 탄약 상자 외형 변형 속성(tacz:ammo_statue). items/ammo_box.json의 select가 쓴다.
        // 클라이언트 아이템 JSON을 해석하기 전에 등록해야 한다. 그렇지 않으면 select가 속성 종류를 찾지 못해 오류가 난다.
        SelectItemModelProperties.ID_MAPPER.put(AmmoBoxStatueProperty.ID, AmmoBoxStatueProperty.TYPE);
        NetworkHandler.registerS2CPackets();
        ClientSetupEvent.init();
        ModContainerScreen.registerScreens();
        ModEntitiesRender.registerEntityRenderers();
        ParticleFactories.registerParticles();
        // 세 블록 모두 RenderShape.INVISIBLE을 돌려주므로 등록이 꼭 필요하다:
        // 이 렌더러가 없으면 설치한 작업대·과녁·조각상이 동작은 해도 보이지 않는다.
        BlockEntityRendererRegistry.register(ModBlocks.GUN_SMITH_TABLE_BE, GunSmithTableRenderer::new);
        BlockEntityRendererRegistry.register(ModBlocks.TARGET_BE, TargetRenderer::new);
        BlockEntityRendererRegistry.register(ModBlocks.STATUE_BE, StatueRenderer::new);
        // getCustomRenderer()는 null을 돌려줄 수 있다 — "이 아이템은 바닐라 모델로 렌더링"한다는 뜻이다.
        // 탄약 상자가 그런 경우다(items/ammo_box.json의 select + 변형 모델 9개를 쓴다).
        // null을 레지스트리에 넣으면 TaczSpecialRenderer가 null 렌더러를 받아 아무것도 그리지 않는다.
        BuiltInRegistries.ITEM.stream().filter(item -> item instanceof IItem).forEach(clientEx -> {
            BuiltinItemRendererRegistry.DynamicItemRenderer renderer = ((IItem) clientEx).getCustomRenderer();
            if (renderer != null) {
                BuiltinItemRendererRegistry.INSTANCE.register(clientEx, renderer);
            }
        });
        // 26.2 Feature Rendering: TACZ 사용자 정의 FeatureRenderer 등록
        TaczFeatureRenderers.register();
        subscribeEvents();
    }

    private void subscribeEvents() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> CommonRegistry.onLoadComplete());

        RenderTickEvent.EVENT.register(RefitTransform::tickInterpolation);

        ViewportEvent.CAMERA.register(CameraSetupEvent::applyLevelCameraAnimation);
        BeforeRenderHandEvent.CALLBACK.register(CameraSetupEvent::applyItemInHandCameraAnimation);
        ViewportEvent.FOV.register(CameraSetupEvent::applyScopeMagnification);
        ViewportEvent.FOV.register(CameraSetupEvent::applyGunModelFovModifying);
        GunFireEvent.CALLBACK.register(CameraSetupEvent::initialCameraRecoil);
        ViewportEvent.CAMERA.register(CameraSetupEvent::applyCameraRecoil);
        ComputeFovModifierEvent.CALLBACK.register(CameraSetupEvent::onComputeMovementFov);

        EntityHurtByGunEvent.POST.register(ClientHitMark::onEntityHurt);
        EntityKillByGunEvent.CALLBACK.register(ClientHitMark::onEntityKill);

        InputEvent.InteractionKeyMappingTriggered.EVENT.register(ClientPreventGunClick::onClickInput);

        ClientPlayConnectionEvents.DISCONNECT.register(CommonNetworkCacheEvent::onClientPlayerLoggingIn);

        // 26.2: 1인칭 진입점은 더 이상 SimpleBedrockModel의 RenderHandEvent를 거치지 않는다.
        // 이제 클라이언트 ItemModel(tacz:dynamic_item) -> AnimateGeoItemRenderer#render의
        // mode.firstPerson() 분기로 들어오며, 다른 display context와 완전히 같다.
        // 예전에 주석 처리돼 있던 FirstPersonRenderEvent 등록과 RenderHandEvent 대체 구현은 모두 지웠다.
        // 다시 1인칭 경로로 착각해 조사하는 일이 없게 하기 위해서다.

        RenderItemInHandBobEvent.VIEW.register(FirstPersonRenderGunEvent::cancelItemInHandViewBobbing);
        GunFireEvent.CALLBACK.register(FirstPersonRenderGunEvent::onGunFire);

        ClientTickEvents.START_CLIENT_TICK.register(client -> InventoryEvent.onPlayerChangeSelect(client, false));
        ClientTickEvents.END_CLIENT_TICK.register(client -> InventoryEvent.onPlayerChangeSelect(client, true));
        SwapItemWithOffHand.CALLBACK.register(InventoryEvent::onPlayerSwapMainHand);
        ClientPlayerNetworkEvent.LOGGING_OUT.register(InventoryEvent::onPlayerLoggedOut);

        PlayerEvent.LOGGED_IN.register(PlayerEnterWorld::onPlayerEnterWorld);

        EntityHurtByGunEvent.POST.register(PlayerHurtByGunEvent::onPlayerHurtByGun);

        // [r42] 원래 여기서 ClientPlayerNetworkEvent.CLONE ->
        // RefreshClonePlayerDataEvent::onClientPlayerClone도 등록했지만, 이 이벤트는 26.2에서 절대 발생하지 않는다
        // (유일한 발생 지점인 ClientHooks#firePlayerRespawn이 의존하던 ClientPacketListenerMixin의
        //   주입 지점 ClientLevel#addPlayer가 사라졌다).
        // 부활·차원 이동 뒤 부착물 캐시 새로 고침은 아래 틱 콜백이 플레이어 인스턴스 변화를 감지해 처리한다.
        ClientTickEvents.START_CLIENT_TICK.register(RefreshClonePlayerDataEvent::onClientTick);

        TextureStitchEvent.POST.register(ReloadResourceEvent::onTextureStitchEventPost);

        RenderTickEvent.EVENT.register(RenderCrosshairEvent::onRenderTick);

        ClientTickEvents.START_CLIENT_TICK.register(TickAnimationEvent::tickAnimation);
        ClientTickEvents.END_CLIENT_TICK.register(TickAnimationEvent::tickAnimation);
        RenderTickEvent.EVENT.register(TickAnimationEvent::tickAnimation);

        ItemTooltipCallback.EVENT.register((stack, tooltipContext, flag, lines) -> TooltipEvent.onTooltip(stack, flag, lines));

        InputEvent.MouseButton.Post.EVENT.register(AimKey::onAimPress);
        InputEvent.MouseButton.Post.EVENT.register(ShootKey::onShootMousePress);
        InputEvent.Key.EVENT.register(ShootKey::onShootKeyPress);
        ClientTickEvents.END_CLIENT_TICK.register(AimKey::cancelAim);
        ClientTickEvents.START_CLIENT_TICK.register(AimKey::onAimHoldingPreInput);
        ClientTickEvents.END_CLIENT_TICK.register(AimKey::onAimHoldingPreInput);

        InputEvent.Key.EVENT.register(CrawlKey::onCrawlPress);

        InputEvent.Key.EVENT.register(FireSelectKey::onFireSelectKeyPress);
        InputEvent.MouseButton.Post.EVENT.register(FireSelectKey::onFireSelectMousePress);

        InputEvent.Key.EVENT.register(InspectKey::onInspectPress);


        InputEvent.Key.EVENT.register(MeleeKey::onMeleeKeyPress);
        InputEvent.MouseButton.Post.EVENT.register(MeleeKey::onMeleeMousePress);

        InputEvent.Key.EVENT.register(RefitKey::onRefitPress);

        InputEvent.Key.EVENT.register(ReloadKey::onReloadPress);
        PlayerTickEvent.START.register(ReloadKey::autoReload);

        ClientTickEvents.START_CLIENT_TICK.register(mc -> ShootKey.autoShoot(mc, false));
        ClientTickEvents.END_CLIENT_TICK.register(mc -> ShootKey.autoShoot(mc, true));

        InputEvent.Key.EVENT.register(ZoomKey::onZoomKeyPress);
        InputEvent.MouseButton.Post.EVENT.register(ZoomKey::onZoomMousePress);

        ClientTickEvents.END_CLIENT_TICK.register(SoundPlayManager::onClientTick);
    }
}
