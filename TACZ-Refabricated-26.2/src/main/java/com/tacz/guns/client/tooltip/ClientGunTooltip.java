package com.tacz.guns.client.tooltip;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.client.input.RefitKey;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.pojo.display.gun.AmmoCountStyle;
import com.tacz.guns.config.sync.SyncConfig;
import com.tacz.guns.inventory.tooltip.GunTooltip;
import com.tacz.guns.item.GunTooltipPart;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import com.tacz.guns.resource.pojo.data.gun.BulletData;
import com.tacz.guns.resource.pojo.data.gun.ExtraDamage;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.util.AllowAttachmentTagMatcher;
import com.tacz.guns.util.AttachmentDataUtils;
import com.tacz.guns.util.GunLevelManager;
import com.tacz.guns.util.ShooterMagazineBonus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

import static com.tacz.guns.item.ModernKineticGunItem.DefaultPropertyModification.SLUGS;

public class ClientGunTooltip implements ClientTooltipComponent {
    private static final DecimalFormat FORMAT = new DecimalFormat("#.##%");
    private static final DecimalFormat FORMAT_P_D1 = new DecimalFormat("#.#%");
    private static final DecimalFormat DAMAGE_FORMAT = new DecimalFormat("#.##");
    private static final DecimalFormat CURRENT_AMMO_FORMAT_PERCENT = new DecimalFormat("0%");

    private final ItemStack gun;
    private final IGun iGun;
    private final CommonGunIndex gunIndex;
    private final @Nullable GunDisplayInstance display;
    private final ItemStack ammo;
    private @Nullable List<FormattedCharSequence> desc;
    private Component ammoName;
    private MutableComponent ammoCountText;
    private @Nullable MutableComponent gunType;
    private MutableComponent damage;
    private MutableComponent armorIgnore;
    private MutableComponent headShotMultiplier;
    private MutableComponent weight;
    private MutableComponent tips;
    private MutableComponent levelInfo;
    private MutableComponent levelDamageBonus;
    private @Nullable MutableComponent pelletDamage;

    private int maxWidth;

    public ClientGunTooltip(GunTooltip tooltip) {
        this.gun = tooltip.getGun();
        this.iGun = tooltip.getIGun();
        Identifier ammoId = tooltip.getAmmoId();
        this.gunIndex = tooltip.getGunIndex();
        this.display = TimelessAPI.getGunDisplay(gun).orElse(null);
        this.ammo = AmmoItemBuilder.create().setId(ammoId).build();
        this.maxWidth = 0;
        this.getText();
    }

    @Override
    public int getHeight(Font font) {
        int height = 0;
        if (shouldShow(GunTooltipPart.DESCRIPTION) && this.desc != null) {
            height += 10 * this.desc.size() + 2;
        }
        if (shouldShow(GunTooltipPart.AMMO_INFO)) {
            height += 24;
        }
        if (shouldShow(GunTooltipPart.BASE_INFO)) {
            height += 44;
            if (this.pelletDamage != null) {
                height += 10;
            }
        }
        if (shouldShow(GunTooltipPart.EXTRA_DAMAGE_INFO)) {
            height += 34;
        }
        if (shouldShow(GunTooltipPart.UPGRADES_TIP)) {
            height += 14;
        }
        return height;
    }

    @Override
    public int getWidth(Font font) {
        return this.maxWidth;
    }

    private void getText() {
        Font font = Minecraft.getInstance().font;
        BulletData bulletData = gunIndex.getBulletData();
        GunData gunData = gunIndex.getGunData();

        if (shouldShow(GunTooltipPart.DESCRIPTION)) {
            @Nullable String tooltip = gunIndex.getPojo().getTooltip();
            if (tooltip != null) {
                List<FormattedCharSequence> split = font.split(Component.translatable(tooltip), 300);
                if (split.size() > 3) {
                    this.desc = split.subList(0, 3);
                } else {
                    this.desc = split;
                }
                for (FormattedCharSequence sequence : this.desc) {
                    this.maxWidth = Math.max(font.width(sequence), this.maxWidth);
                }
            }
        }


        if (shouldShow(GunTooltipPart.AMMO_INFO)) {
            this.ammoName = ammo.getHoverName();
            this.maxWidth = Math.max(font.width(this.ammoName) + 22, this.maxWidth);

            int barrelBulletAmount = (iGun.hasBulletInBarrel(gun) && gunIndex.getGunData().getBolt() != Bolt.OPEN_BOLT) ? 1 : 0;
            int maxAmmoCount = ShooterMagazineBonus.maxAmmoCount(Minecraft.getInstance().player, gun, gunIndex) + barrelBulletAmount;
            int currentAmmoCount = iGun.getCurrentAmmoCount(this.gun) + barrelBulletAmount;

            if (!iGun.useDummyAmmo(gun)) {
                if (display != null && display.getAmmoCountStyle() == AmmoCountStyle.PERCENT) {
                    this.ammoCountText = Component.literal(CURRENT_AMMO_FORMAT_PERCENT.format((float) currentAmmoCount / (maxAmmoCount == 0 ? 1f : maxAmmoCount)));
                } else {
                    this.ammoCountText = Component.literal("%d/%d".formatted(currentAmmoCount, maxAmmoCount));
                }
            } else {
                int dummyAmmoAmount = iGun.getDummyAmmoAmount(gun);
                if (display != null && display.getAmmoCountStyle() == AmmoCountStyle.PERCENT) {
                    String p = CURRENT_AMMO_FORMAT_PERCENT.format((float) currentAmmoCount / (maxAmmoCount == 0 ? 1f : maxAmmoCount));
                    this.ammoCountText = Component.literal("%s (%d)".formatted(p, dummyAmmoAmount));
                } else {
                    this.ammoCountText = Component.literal("%d/%d (%d)".formatted(currentAmmoCount, maxAmmoCount, dummyAmmoAmount));
                }

            }
            if (iGun.useInventoryAmmo(gun)) {
                this.ammoCountText = Component.translatable("tooltip.tacz.gun.inventory_mode").withStyle(style -> style.withColor(0xFFFF55));
            }
            this.maxWidth = Math.max(font.width(this.ammoCountText) + 22, this.maxWidth);
        }


        if (shouldShow(GunTooltipPart.BASE_INFO)) {
            int expCurrentLevel = iGun.getExpCurrentLevel(gun);
            int level = iGun.getLevel(gun);
            if (level >= iGun.getMaxLevel()) {
                this.levelInfo = Component.translatable("tooltip.tacz.gun.level_max", level)
                        .withStyle(style -> style.withColor(0xAA00AA));
            } else {
                this.levelInfo = Component.translatable("tooltip.tacz.gun.level_progress", level,
                        expCurrentLevel, GunLevelManager.EXP_PER_LEVEL)
                        .withStyle(style -> style.withColor(0xFFFF55));
            }
            this.maxWidth = Math.max(font.width(this.levelInfo), this.maxWidth);
            String bonusPercent = String.format(Locale.ROOT, "%.1f", GunLevelManager.getDamageBonusPercent(level));
            this.levelDamageBonus = Component.translatable("tooltip.tacz.gun.level_damage_bonus", bonusPercent)
                    .withStyle(style -> style.withColor(0x55FF55));
            this.maxWidth = Math.max(font.width(this.levelDamageBonus), this.maxWidth);

            String tabKey = "tacz.type." + gunIndex.getType() + ".name";
            this.gunType = Component.translatable("tooltip.tacz.gun.type").append(Component.translatable(tabKey).withStyle(style -> style.withColor(0x55FFFF)));
            this.maxWidth = Math.max(font.width(this.gunType), this.maxWidth);

            double damage = AttachmentDataUtils.getDamageWithAttachment(gun, gunData);
            boolean hasSlugInstalled = AllowAttachmentTagMatcher.matchTag(SLUGS, iGun.getAttachmentId(gun, AttachmentType.EXTENDED_MAG));
            int bulletAmount = Math.max(bulletData.getBulletAmount(), 1);
            if (hasSlugInstalled) {
                bulletAmount = 1;
            }
            String damageKey = "tooltip.tacz.gun.damage";
            if (bulletAmount > 1) {
                damageKey = "tooltip.tacz.gun.damage_total";
                this.pelletDamage = Component.translatable("tooltip.tacz.gun.pellet_damage",
                        DAMAGE_FORMAT.format(damage / bulletAmount), bulletAmount)
                        .withStyle(style -> style.withColor(0x55FFFF));
                this.maxWidth = Math.max(font.width(this.pelletDamage), this.maxWidth);
            }
            MutableComponent value = Component.literal(DAMAGE_FORMAT.format(damage))
                    .withStyle(style -> style.withColor(0x55FFFF));
            // 총기에 폭발 수치가 없어도 고폭탄 같은 부품이 폭발을 켤 수 있으므로, 폭발 여부만으로 판단한다
            boolean explodeEnabled = AttachmentDataUtils.isExplodeEnabled(gun, gunData)
                    || (bulletData.getExplosionData() != null && bulletData.getExplosionData().isExplode());
            if (explodeEnabled) {
                double explosionDamage = AttachmentDataUtils.getExplosionDamageWithAttachment(gun, gunData);
                value.append(" + ").append(DAMAGE_FORMAT.format(explosionDamage)).append(Component.translatable("tooltip.tacz.gun.explosion"));
            }
            this.damage = Component.translatable(damageKey).append(value);
            this.maxWidth = Math.max(font.width(this.damage), this.maxWidth);
        }


        if (shouldShow(GunTooltipPart.EXTRA_DAMAGE_INFO)) {
            @Nullable ExtraDamage extraDamage = bulletData.getExtraDamage();
            if (extraDamage != null) {
                double armorDamagePercent = AttachmentDataUtils.getArmorIgnoreWithAttachment(gun, gunData);
                double headShotMultiplierPercent = AttachmentDataUtils.getHeadshotMultiplier(gun, gunData);

                armorDamagePercent = Mth.clamp(armorDamagePercent, 0.0F, 1.0F);

                this.armorIgnore = Component.translatable("tooltip.tacz.gun.armor_ignore", FORMAT.format(armorDamagePercent));
                this.headShotMultiplier = Component.translatable("tooltip.tacz.gun.head_shot_multiplier", FORMAT.format(headShotMultiplierPercent));
            } else {
                this.armorIgnore = Component.translatable("tooltip.tacz.gun.armor_ignore", FORMAT.format(0));
                this.headShotMultiplier = Component.translatable("tooltip.tacz.gun.head_shot_multiplier", FORMAT.format(1));
            }

            double weightFactor = SyncConfig.WEIGHT_SPEED_MULTIPLIER.get();
            double weight = AttachmentDataUtils.getWightWithAttachment(gun, gunData);
            this.weight = Component.translatable("tooltip.tacz.gun.movement_speed", FORMAT_P_D1.format(-weightFactor * weight)).withStyle(style -> style.withColor(0xFF5555));

            this.maxWidth = Math.max(font.width(this.armorIgnore), this.maxWidth);
            this.maxWidth = Math.max(font.width(this.headShotMultiplier), this.maxWidth);
            this.maxWidth = Math.max(font.width(this.weight), this.maxWidth);
        }


        if (shouldShow(GunTooltipPart.UPGRADES_TIP)) {
            String keyName = Component.keybind(RefitKey.REFIT_KEY.getName()).getString().toUpperCase(Locale.ENGLISH);
            this.tips = Component.translatable("tooltip.tacz.gun.tips", keyName).withStyle(style -> style.withColor(0xFFFF55)).withStyle(style -> style.withItalic(true));
            this.maxWidth = Math.max(font.width(this.tips), this.maxWidth);
        }
    }

    @Override
    public void extractText(GuiGraphicsExtractor graphics, Font font, int pX, int pY) {
        int yOffset = pY;

        if (shouldShow(GunTooltipPart.DESCRIPTION) && this.desc != null) {
            yOffset += 2;
            for (FormattedCharSequence sequence : this.desc) {
                graphics.text(font, sequence, pX, yOffset, 0xFFaaaaaa);
                yOffset += 10;
            }
        }


        if (shouldShow(GunTooltipPart.AMMO_INFO)) {
            yOffset += 4;

            // 탄약 이름
            graphics.text(font, this.ammoName, pX + 20, yOffset, 0xFFffaa00);

            // 탄약 수
            graphics.text(font, this.ammoCountText, pX + 20, yOffset + 10, 0xFF777777);

            yOffset += 20;
        }


        if (shouldShow(GunTooltipPart.BASE_INFO)) {
            yOffset += 4;

            // 등급 정보
            graphics.text(font, this.levelInfo, pX, yOffset, 0xFF777777);
            yOffset += 10;

            graphics.text(font, this.levelDamageBonus, pX, yOffset, 0xFF777777);
            yOffset += 10;

            // 총기 종류
            if (this.gunType != null) {
                graphics.text(font, this.gunType, pX, yOffset, 0xFF777777);
                yOffset += 10;
            }

            // 피해
            graphics.text(font, this.damage, pX, yOffset, 0xFF777777);
            yOffset += 10;
            if (this.pelletDamage != null) {
                graphics.text(font, this.pelletDamage, pX, yOffset, 0xFF777777);
                yOffset += 10;
            }
        }


        if (shouldShow(GunTooltipPart.EXTRA_DAMAGE_INFO)) {
            yOffset += 4;

            // 방어 관통 피해
            graphics.text(font, this.armorIgnore, pX, yOffset, 0xFFffaa00);
            yOffset += 10;

            // 헤드샷 피해
            graphics.text(font, this.headShotMultiplier, pX, yOffset, 0xFFffaa00);
            yOffset += 10;

            graphics.text(font, this.weight, pX, yOffset, 0xFFffffff);
            yOffset += 10;
        }


        if (shouldShow(GunTooltipPart.UPGRADES_TIP)) {
            yOffset += 4;

            // Z 키 설명
            graphics.text(font, this.tips, pX, yOffset, 0xFFffffff);
            yOffset += 10;
        }
    }

    @Override
    public void extractImage(Font pFont, int pX, int pY, int width, int height, GuiGraphicsExtractor graphics) {
        IGun iGun = IGun.getIGunOrNull(this.gun);
        if (iGun == null) {
            return;
        }
        if (shouldShow(GunTooltipPart.AMMO_INFO)) {
            int yOffset = pY;
            if (shouldShow(GunTooltipPart.DESCRIPTION) && this.desc != null) {
                yOffset += this.desc.size() * 10 + 2;
            }
            graphics.item(ammo, pX, yOffset + 4);
        }
    }

    private boolean shouldShow(GunTooltipPart part) {
        return (GunTooltipPart.getHideFlags(this.gun) & part.getMask()) == 0;
    }
}
