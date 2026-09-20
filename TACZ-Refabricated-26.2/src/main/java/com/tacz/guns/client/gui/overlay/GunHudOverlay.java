package com.tacz.guns.client.gui.overlay;

import com.tacz.guns.GunMod;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.index.ClientGunIndex;
import com.tacz.guns.client.resource.pojo.display.gun.AmmoCountStyle;
import com.tacz.guns.config.client.RenderConfig;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.util.AttachmentDataUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;

import java.text.DecimalFormat;

/**
 * 26.2 HUD implementation using Fabric HudElementRegistry + GuiGraphicsExtractor.
 */
public class GunHudOverlay {
    private static final Identifier FIRE_MODE_SEMI =
            Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "textures/hud/fire_mode_semi.png");
    private static final Identifier FIRE_MODE_AUTO =
            Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "textures/hud/fire_mode_auto.png");
    private static final Identifier FIRE_MODE_BURST =
            Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "textures/hud/fire_mode_burst.png");

    private static final DecimalFormat CURRENT_AMMO_FORMAT = new DecimalFormat("000");
    private static final DecimalFormat CURRENT_AMMO_FORMAT_PERCENT = new DecimalFormat("000%");
    private static final DecimalFormat INVENTORY_AMMO_FORMAT = new DecimalFormat("0000");
    private static long checkAmmoTimestamp = -1L;
    private static int cacheMaxAmmoCount = 0;
    private static int cacheInventoryAmmoCount = 0;

    private static final int MAX_AMMO_COUNT = 9999;

    /** HUD 위쪽 여백 */
    private static final int TOP_MARGIN = 6;
    /** 바닐라 상태 효과 아이콘 한 줄의 높이. 윗줄은 이로운 효과, 아랫줄은 해로운 효과다. */
    private static final int EFFECT_ROW_HEIGHT = 26;

    public static void render(GuiGraphicsExtractor graphics, float partialTick) {
        if (!RenderConfig.GUN_HUD_ENABLE.get()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (!(player instanceof IClientPlayerGunOperator)) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof IGun iGun)) {
            return;
        }
        Identifier gunId = iGun.getGunId(stack);

        GunData gunData = TimelessAPI.getClientGunIndex(gunId).map(ClientGunIndex::getGunData).orElse(null);
        GunDisplayInstance display = TimelessAPI.getGunDisplay(stack).orElse(null);
        if (gunData == null) {
            return;
        }

        boolean useInventoryAmmo = iGun.useInventoryAmmo(stack);
        boolean useDummyAmmo = iGun.useDummyAmmo(stack);
        boolean overheatLocked = gunData.hasHeatData() && iGun.isOverheatLocked(stack);
        handleCacheCount(player, stack, gunData, iGun, useInventoryAmmo);

        int ammoCount = useInventoryAmmo ? cacheInventoryAmmoCount + (iGun.hasBulletInBarrel(stack) && gunData.getBolt() != Bolt.OPEN_BOLT ? 1 : 0) :
                iGun.getCurrentAmmoCount(stack) + (iGun.hasBulletInBarrel(stack) && gunData.getBolt() != Bolt.OPEN_BOLT ? 1 : 0);
        ammoCount = Math.min(ammoCount, MAX_AMMO_COUNT);

        // 【顺序】先算缓存, 再画 —— 上游是先画后算, 导致首帧用的是上一帧的
        // cacheMaxAmmoCount, 百分比模式下切枪瞬间会闪一下错误数字。这里修正。
        handleCacheCount(player, stack, gunData, iGun, useInventoryAmmo);

        int width = graphics.guiWidth();
        // 오른쪽 하단은 소리 자막과 겹치므로 오른쪽 상단에 붙인다. 가로 배치는 상류 1.21.1 과 같다.
        int top = hudTop(player);

        // 弹药数颜色: 余弹告急 / 过热 -> 红; 背包直读+虚拟备弹 -> 青;
        // 仅背包直读 -> 黄; 其余 -> 白
        int ammoCountColor;
        if (ammoCount < (cacheMaxAmmoCount * 0.25) && ammoCount < 10 || overheatLocked) {
            ammoCountColor = 0xFFFF5555;
        } else {
            ammoCountColor = useInventoryAmmo && useDummyAmmo ? 0xFF55FFFF
                    : useInventoryAmmo ? 0xFFFFFF55 : 0xFFFFFFFF;
        }
        // 备弹颜色
        int inventoryAmmoCountColor = (!useInventoryAmmo && useDummyAmmo) ? 0xFF55FFFF : 0xFFAAAAAA;

        // 当前弹药数文本
        String currentAmmoCountText;
        if (display != null && display.getAmmoCountStyle() == AmmoCountStyle.PERCENT) {
            currentAmmoCountText = CURRENT_AMMO_FORMAT_PERCENT.format(
                    (float) ammoCount / (cacheMaxAmmoCount == 0 ? 1f : cacheMaxAmmoCount));
        } else {
            currentAmmoCountText = CURRENT_AMMO_FORMAT.format(ammoCount);
        }

        // 备弹文本: 背包直读模式不显示备弹; 无限备弹显示 ∞
        String inventoryAmmoCountText = useInventoryAmmo ? ""
                : INVENTORY_AMMO_FORMAT.format(Math.min(cacheInventoryAmmoCount, MAX_AMMO_COUNT));
        if (!useInventoryAmmo && gunData.getReloadData().isInfinite()) {
            inventoryAmmoCountText = "\u221e";
        }

        Font font = mc.font;
        Matrix3x2fStack poseStack = graphics.pose();

        // 竖线分隔符
        graphics.fill(width - 75, top, width - 74, top + 14, 0xFFFFFFFF);

        // 当前弹药数 (1.5 倍字号)
        poseStack.pushMatrix();
        poseStack.scale(1.5f, 1.5f);
        graphics.text(font, currentAmmoCountText,
                (int) ((width - 70) / 1.5f), (int) (top / 1.5f), ammoCountColor, false);
        poseStack.popMatrix();

        // 备弹数 (0.8 倍字号, 紧跟在当前弹药数右侧)
        poseStack.pushMatrix();
        poseStack.scale(0.8f, 0.8f);
        graphics.text(font, inventoryAmmoCountText,
                (int) ((width - 68 + font.width(currentAmmoCountText) * 1.5f) / 0.8f),
                (int) (top / 0.8f), inventoryAmmoCountColor, false);
        poseStack.popMatrix();

        // 枪械图标。弹尽/过热时若有专用空仓图标就换图, 否则染红。
        if (display != null) {
            Identifier hudTexture = display.getHUDTexture();
            Identifier hudEmptyTexture = display.getHudEmptyTexture();
            int hudTint = 0xFFFFFFFF;
            if (ammoCount <= 0 || overheatLocked) {
                if (hudEmptyTexture == null) {
                    // 上游用 RenderSystem.setShaderColor(1,0.3,0.3,1); 26.2 该 API 已移除,
                    // 改用 blit 的 tint 参数 —— 等价且不依赖全局状态。
                    hudTint = 0xFFFF4D4D;
                } else {
                    hudTexture = hudEmptyTexture;
                }
            }
            if (hudTexture != null) {
                graphics.blit(RenderPipelines.GUI_TEXTURED, hudTexture,
                        width - 117, top - 1, 0.0F, 0.0F, 39, 13, 39, 13, hudTint);
            }
        }

        // 开火模式图标
        Identifier fireModeTexture = switch (iGun.getFireMode(stack)) {
            case AUTO -> FIRE_MODE_AUTO;
            case BURST -> FIRE_MODE_BURST;
            default -> FIRE_MODE_SEMI;
        };
        graphics.blit(RenderPipelines.GUI_TEXTURED, fireModeTexture,
                (int) (width - 68.5 + font.width(currentAmmoCountText) * 1.5), top + 5,
                0.0F, 0.0F, 10, 10, 10, 10);
    }

    /**
     * HUD 맨 위의 y 좌표. 바닐라가 오른쪽 상단에 그리는 상태 효과 아이콘을 피해 그 아래로 내려간다.
     * 과열 바도 이 값을 기준으로 HUD 바로 아래에 붙는다.
     */
    public static int hudTop(LocalPlayer player) {
        boolean hasIcon = false;
        boolean hasHarmfulIcon = false;
        for (MobEffectInstance effect : player.getActiveEffects()) {
            if (!effect.showIcon()) {
                continue;
            }
            hasIcon = true;
            if (!effect.getEffect().value().isBeneficial()) {
                hasHarmfulIcon = true;
            }
        }
        int effectRows = hasHarmfulIcon ? 2 : hasIcon ? 1 : 0;
        return TOP_MARGIN + effectRows * EFFECT_ROW_HEIGHT;
    }

    private static void handleCacheCount(LocalPlayer player, ItemStack stack, GunData gunData, IGun iGun, boolean useInventoryAmmo) {
        if ((System.currentTimeMillis() - checkAmmoTimestamp) > 50) {
            checkAmmoTimestamp = System.currentTimeMillis();
            cacheMaxAmmoCount = AttachmentDataUtils.getAmmoCountWithAttachment(stack, gunData);
            if (IGunOperator.fromLivingEntity(player).needCheckAmmo()) {
                if (iGun.useDummyAmmo(stack)) {
                    cacheInventoryAmmoCount = iGun.getDummyAmmoAmount(stack);
                } else {
                    handleInventoryAmmo(stack, player.getInventory());
                }
            } else {
                cacheInventoryAmmoCount = MAX_AMMO_COUNT;
            }
            if (useInventoryAmmo) {
                iGun.setCurrentAmmoCount(stack, cacheInventoryAmmoCount);
            }
        }
    }

    private static void handleInventoryAmmo(ItemStack stack, Inventory inventory) {
        cacheInventoryAmmoCount = 0;
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack inventoryItem = inventory.getItem(i);
            if (inventoryItem.getItem() instanceof IAmmo iAmmo && iAmmo.isAmmoOfGun(stack, inventoryItem)) {
                cacheInventoryAmmoCount += inventoryItem.getCount();
            }
            if (inventoryItem.getItem() instanceof IAmmoBox iAmmoBox && iAmmoBox.isAmmoBoxOfGun(stack, inventoryItem)) {
                cacheInventoryAmmoCount += iAmmoBox.getAmmoCount(inventoryItem);
            }
        }
    }
}
