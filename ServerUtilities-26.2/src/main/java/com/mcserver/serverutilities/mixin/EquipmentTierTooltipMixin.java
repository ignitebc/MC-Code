package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.level.ToolLevelRules;
import com.mcserver.serverutilities.tier.EquipmentTier;
import com.mcserver.serverutilities.tier.EquipmentTierRules;
import com.mcserver.serverutilities.tier.EquipmentTierSummary;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Mixin(ItemStack.class)
abstract class EquipmentTierTooltipMixin {
    /** 아이템 이름 바로 아래, 능력치 줄보다 위에 등급과 그 등급이 바꾼 수치를 넣는다. */
    private static final int TIER_LINE_INDEX = 1;

    @Inject(method = "getTooltipLines", at = @At("RETURN"), cancellable = true)
    private void serverutilities$appendTier(Item.TooltipContext context, Player player, TooltipFlag flag,
                                           CallbackInfoReturnable<List<Component>> callback) {
        ItemStack itemStack = (ItemStack) (Object) this;
        EquipmentTier tier = EquipmentTierRules.readTier(itemStack);
        boolean levelable = ToolLevelRules.isLevelable(itemStack);
        if (tier == null && !levelable) {
            return;
        }

        // 반환된 목록이 수정 가능하다고 보장되지 않으므로 새 목록에 담아 돌려준다.
        List<Component> lines = new ArrayList<>(callback.getReturnValue());
        int tierLineIndex = Math.min(TIER_LINE_INDEX, lines.size());
        String title = "";
        if (tier != null) title = tier.label() + "티어";
        if (levelable) {
            if (!title.isEmpty()) title += " · ";
            title += "LV " + ToolLevelRules.level(itemStack);
            if (ToolLevelRules.level(itemStack) == ToolLevelRules.MAX_LEVEL) {
                title += " · EXP MAX";
            } else {
                title += " · EXP " + ToolLevelRules.experienceInLevel(itemStack)
                        + "/" + ToolLevelRules.EXPERIENCE_PER_LEVEL;
            }
        }
        Component tierLine = Component.literal(title).withStyle(ChatFormatting.RED);
        lines.add(tierLineIndex, tierLine);
        List<Component> summary = new ArrayList<>(EquipmentTierSummary.describe(itemStack, tier));
        if (levelable) {
            String bonus = " LV 보너스: ";
            if (ToolLevelRules.isDiggingTool(itemStack)) {
                bonus += "채굴 속도 +" + serverutilities$formatBonus(
                        ToolLevelRules.miningSpeedMultiplier(itemStack)) + "%, ";
            }
            bonus += "내구도 +" + serverutilities$formatBonus(
                    ToolLevelRules.durabilityMultiplier(itemStack)) + "%";
            summary.add(Component.literal(bonus).withStyle(ChatFormatting.DARK_AQUA));
        }
        lines.addAll(tierLineIndex + 1, summary);
        callback.setReturnValue(lines);
    }

    @Unique
    private static String serverutilities$formatBonus(double multiplier) {
        return String.format(Locale.ROOT, "%.1f", (multiplier - 1.0D) * 100.0D);
    }
}
