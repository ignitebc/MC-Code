package com.tacz.guns.client.init;

import com.tacz.guns.GunMod;
import com.tacz.guns.api.client.other.ThirdPersonManager;
import com.tacz.guns.client.gui.overlay.GunHudOverlay;
import com.tacz.guns.client.gui.overlay.HeatBarOverlay;
import com.tacz.guns.client.gui.overlay.KillAmountOverlay;
import com.tacz.guns.client.input.*;
import com.tacz.guns.client.resource.ClientAssetsManager;
import com.tacz.guns.client.tooltip.ClientAmmoBoxTooltip;
import com.tacz.guns.client.tooltip.ClientAttachmentItemTooltip;
import com.tacz.guns.client.tooltip.ClientBlockItemTooltip;
import com.tacz.guns.client.tooltip.ClientGunTooltip;
import com.tacz.guns.inventory.tooltip.AmmoBoxTooltip;
import com.tacz.guns.inventory.tooltip.AttachmentItemTooltip;
import com.tacz.guns.inventory.tooltip.BlockItemTooltip;
import com.tacz.guns.inventory.tooltip.GunTooltip;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;

@Environment(EnvType.CLIENT)
public class ClientSetupEvent {
    public static void init() {
        registerKeyMappings();
        registerClientTooltips();
        registerGuiOverlays();
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> onClientSetup());
        onClientResourceReload();
    }

    public static void registerKeyMappings() {
        // 키 등록(26.2: MKB를 쓸 수 없어 KeyMapping을 직접 등록한다)
        KeyMappingHelper.registerKeyMapping(InspectKey.INSPECT_KEY);
        KeyMappingHelper.registerKeyMapping(ReloadKey.RELOAD_KEY);
        KeyMappingHelper.registerKeyMapping(ShootKey.SHOOT_KEY);
        KeyMappingHelper.registerKeyMapping(FireSelectKey.FIRE_SELECT_KEY);
        KeyMappingHelper.registerKeyMapping(AimKey.AIM_KEY);
        KeyMappingHelper.registerKeyMapping(CrawlKey.CRAWL_KEY);
        KeyMappingHelper.registerKeyMapping(RefitKey.REFIT_KEY);
        KeyMappingHelper.registerKeyMapping(ZoomKey.ZOOM_KEY);
        KeyMappingHelper.registerKeyMapping(MeleeKey.MELEE_KEY);
    }

    public static void registerClientTooltips() {
        // 안내 문구 등록(26.2: TooltipComponentCallback → ClientTooltipComponentCallback)
        ClientTooltipComponentCallback.EVENT.register(tooltip -> {
            if (tooltip instanceof GunTooltip gunTooltip) {
                return new ClientGunTooltip(gunTooltip);
            }
            if (tooltip instanceof AmmoBoxTooltip ammoBoxTooltip) {
                return new ClientAmmoBoxTooltip(ammoBoxTooltip);
            }
            if (tooltip instanceof AttachmentItemTooltip attachmentItemTooltip) {
                return new ClientAttachmentItemTooltip(attachmentItemTooltip);
            }
            if (tooltip instanceof BlockItemTooltip blockItemTooltip) {
                return new ClientBlockItemTooltip(blockItemTooltip);
            }
            return null;
        });
    }

    public static void registerGuiOverlays() {
        HudElementRegistry.addLast(id("gun_hud"), (graphics, deltaTracker) -> GunHudOverlay.render(graphics, deltaTracker.getRealtimeDeltaTicks()));
        HudElementRegistry.addLast(id("heat_bar"), (graphics, deltaTracker) -> HeatBarOverlay.render(graphics, deltaTracker.getRealtimeDeltaTicks()));
        HudElementRegistry.addLast(id("kill_amount"), (graphics, deltaTracker) -> KillAmountOverlay.render(graphics, deltaTracker.getRealtimeDeltaTicks()));
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(GunMod.MOD_ID, path);
    }

    public static void onClientSetup() {
        // 직접 만든 고정 3인칭 애니메이션 등록
        ThirdPersonManager.registerDefault();

        // 26.2에서 해결: ColorProviderRegistry.ITEM과 ItemProperties는 모두 제거되었다.
        // 탄약 상자 염색은 items/ammo_box.json 모델의 minecraft:dye tint가 맡고,
        // 변형 선택은 minecraft:select + tacz:ammo_statue 속성이 맡는다
        // (속성 구현은 AmmoBoxStatueProperty, 등록은 TaCZFabricClient에 있다).

        // 자체 총기 팩 내려받기 관리자 초기화





    }

    public static void onClientResourceReload() {
        ClientAssetsManager.INSTANCE.reloadAndRegister(ResourceManagerHelper.get(PackType.CLIENT_RESOURCES)::registerReloadListener);
    }
}
