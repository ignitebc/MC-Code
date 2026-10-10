package com.tacz.guns.client.gui.components.refit;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.util.ShooterMagazineBonus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

public final class GunPropertyDiagrams {
    public static int getHidePropertyButtonYOffset() {
        int[] startYOffset = new int[]{49};
        AttachmentPropertyManager.getModifiers().forEach((key, value) -> {
            startYOffset[0] += value.getDiagramsDataSize() * 10;
        });
        return startYOffset[0];
    }

    public static void draw(GuiGraphicsExtractor graphics, Font font, int x, int y) {
        graphics.fill(x, y, x + 288, y + getHidePropertyButtonYOffset() - 11, 0xAF222222);

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        ItemStack gunItem = player.getMainHandItem();
        IGun iGun = IGun.getIGunOrNull(gunItem);
        if (iGun == null) {
            return;
        }
        AttachmentCacheProperty cacheProperty = IGunOperator.fromLivingEntity(player).getCacheProperty();
        if (cacheProperty == null) {
            return;
        }
        Identifier gunId = iGun.getGunId(gunItem);
        TimelessAPI.getCommonGunIndex(gunId).ifPresent(index -> {
            GunData gunData = index.getGunData();
            FireMode fireMode = iGun.getFireMode(gunItem);

            int barStartX = x + 83;
            int barMaxWidth = 120;
            int barEndX = barStartX + barMaxWidth;

            int barBackgroundColor = 0xFF000000;
            int barBaseColor = 0xFFFFFFFF;
            int barPositivelyColor = 0xFF_55FF55;
            int barNegativeColor = 0xFF_FF5555;

            // [반드시 알파 포함] 26.2 GuiGraphicsExtractor#text의 첫 줄은
            //     if (ARGB.alpha(color) == 0) return;
            // 이다(바이트코드 오프셋 0-8 확인). 알파가 0인 글자는 [통째로 조용히 버려진다].
            //
            // 1.21.1의 GuiGraphics#drawString에는 이런 조기 반환이 없어 0xCCCCCC를
            // 불투명한 연회색으로 그대로 그렸기 때문에 원본은 여섯 자리 색으로도 됐다. 이식할 때 그대로 옮겨서
            // 개조 화면 "그래프"의 글자(탄창 용량/피해량/연사 속도…)가 모두 사라졌고,
            // 같은 메서드에서 graphics.fill()로 그린 진행 막대는 0xFF000000 /
            // 0xFFFFFFFF처럼 알파가 있는 값을 써서 정상 표시됐다 — 그래서
            // "빈 진행 막대만 줄지어 있고 글자는 하나도 없는" 현상이 나타났다.
            int fontColor = 0xFFCCCCCC;
            int nameTextStartX = x + 5;
            int valueTextStartX = x + 210;

            int[] yOffset = new int[]{y + 5};

            // 사격 모드
            MutableComponent fireModeText = Component.translatable("gui.tacz.gun_refit.property_diagrams.fire_mode");
            if (fireMode == FireMode.AUTO) {
                fireModeText.append(Component.translatable("gui.tacz.gun_refit.property_diagrams.auto"));
            } else if (fireMode == FireMode.SEMI) {
                fireModeText.append(Component.translatable("gui.tacz.gun_refit.property_diagrams.semi"));
            } else if (fireMode == FireMode.BURST) {
                fireModeText.append(Component.translatable("gui.tacz.gun_refit.property_diagrams.burst"));
            } else {
                fireModeText.append(Component.translatable("gui.tacz.gun_refit.property_diagrams.unknown"));
            }

            graphics.text(font, fireModeText, nameTextStartX + 12, yOffset[0], fontColor, false);

            yOffset[0] += 10;


            // 탄창 용량
            if (iGun.useInventoryAmmo(gunItem)) {
                // 인벤토리 급탄이면 막대를 가득 채우고 INV 표시를 한다
                graphics.text(font, Component.translatable("gui.tacz.gun_refit.property_diagrams.ammo_capacity"), nameTextStartX, yOffset[0], fontColor, false);
                graphics.fill(barStartX, yOffset[0] + 2, barEndX, yOffset[0] + 6, barBackgroundColor);
                graphics.fill(barStartX, yOffset[0] + 2, barStartX + barMaxWidth, yOffset[0] + 6, barBaseColor);
                graphics.text(font, Component.literal("INV"), valueTextStartX, yOffset[0], fontColor, false);
            } else {
                int barrelBulletAmount = (iGun.hasBulletInBarrel(gunItem) && index.getGunData().getBolt() != Bolt.OPEN_BOLT) ? 1 : 0;
                int ammoAmount = gunData.getAmmoAmount() + barrelBulletAmount;
                double ammoAmountPercent = Math.min(ammoAmount / 100.0, 1);
                int ammoLength = (int) (barStartX + barMaxWidth * ammoAmountPercent);
                int maxAmmoCount = ShooterMagazineBonus.maxAmmoCount(Minecraft.getInstance().player, gunItem, index) + barrelBulletAmount;
                int addAmmoCount = Math.max(maxAmmoCount - ammoAmount, 0);
                int addAmmoCountLength = (int) (barMaxWidth * addAmmoCount / 100.0);

                graphics.text(font, Component.translatable("gui.tacz.gun_refit.property_diagrams.ammo_capacity"), nameTextStartX, yOffset[0], fontColor, false);
                graphics.fill(barStartX, yOffset[0] + 2, barEndX, yOffset[0] + 6, barBackgroundColor);
                graphics.fill(barStartX, yOffset[0] + 2, ammoLength, yOffset[0] + 6, barBaseColor);
                if (addAmmoCount > 0) {
                    int barRight = Math.min(ammoLength + addAmmoCountLength, barEndX);
                    graphics.fill(ammoLength, yOffset[0] + 2, barRight, yOffset[0] + 6, barPositivelyColor);
                    graphics.text(font, String.format("%d §a(+%d)", ammoAmount, addAmmoCount), valueTextStartX, yOffset[0], fontColor, false);
                } else {
                    graphics.text(font, String.format("%d", ammoAmount), valueTextStartX, yOffset[0], fontColor, false);
                }
            }

            yOffset[0] += 10;


            // 달리다 쏘기 지연
            float sprintTime = gunData.getSprintTime();
            double sprintTimePercent = Mth.clamp(sprintTime / 0.5, 0, 1);
            int sprintLength = (int) (barStartX + barMaxWidth * sprintTimePercent);
            String sprintValueText = String.format("%.2fs", sprintTime);

            graphics.text(font, Component.translatable("gui.tacz.gun_refit.property_diagrams.sprint_time"), nameTextStartX, yOffset[0], fontColor, false);
            graphics.fill(barStartX, yOffset[0] + 2, barEndX, yOffset[0] + 6, barBackgroundColor);
            graphics.fill(barStartX, yOffset[0] + 2, sprintLength, yOffset[0] + 6, barBaseColor);
            graphics.text(font, sprintValueText, valueTextStartX, yOffset[0], fontColor, false);

            yOffset[0] += 10;

            AttachmentPropertyManager.getModifiers().forEach((key, value) -> value.getPropertyDiagramsData(gunItem, gunData, cacheProperty).forEach(data -> {
                double defaultPercent = data.defaultPercent();
                double modifierPercent = data.modifierPercent();
                double modifier = data.modifier().doubleValue();
                String titleKey = data.titleKey();
                String positivelyString = data.positivelyString();
                String negativeString = data.negativeString();
                String defaultString = data.defaultString();
                boolean positivelyBetter = data.positivelyBetter();

                defaultPercent = Mth.clamp(defaultPercent, 0, 1);
                int defaultLength = (int) (barStartX + barMaxWidth * defaultPercent);
                int modifierLength = Mth.clamp(defaultLength + (int) (barMaxWidth * modifierPercent), barStartX, barEndX);

                graphics.text(font, Component.translatable(titleKey), nameTextStartX, yOffset[0], fontColor, false);
                graphics.fill(barStartX, yOffset[0] + 2, barEndX, yOffset[0] + 6, barBackgroundColor);
                graphics.fill(barStartX, yOffset[0] + 2, defaultLength, yOffset[0] + 6, barBaseColor);
                if (modifier > 0) {
                    int barColor = positivelyBetter ? barPositivelyColor : barNegativeColor;
                    graphics.fill(defaultLength, yOffset[0] + 2, modifierLength, yOffset[0] + 6, barColor);
                    graphics.text(font, positivelyString, valueTextStartX, yOffset[0], fontColor, false);
                } else if (modifier < 0) {
                    int barColor = positivelyBetter ? barNegativeColor : barPositivelyColor;
                    graphics.fill(modifierLength, yOffset[0] + 2, defaultLength, yOffset[0] + 6, barColor);
                    graphics.text(font, negativeString, valueTextStartX, yOffset[0], fontColor, false);
                } else {
                    graphics.text(font, defaultString, valueTextStartX, yOffset[0], fontColor, false);
                }
                yOffset[0] += 10;
            }));
        });
    }
}
