package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.level.ToolLevelRules;
import com.mcserver.serverutilities.tier.EquipmentTierRules;
import com.mcserver.serverutilities.tier.EquipmentTierSet;
import com.mcserver.serverutilities.tier.EquipmentTierSummary;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ItemStack.class)
abstract class EquipmentTierTooltipMixin {
    /** 아이템 이름 바로 아래, 능력치 줄보다 위에 LV과 등급 줄을 넣는다. */
    private static final int TIER_LINE_INDEX = 1;

    @Inject(method = "getTooltipLines", at = @At("RETURN"), cancellable = true)
    private void serverutilities$appendTier(Item.TooltipContext context, Player player, TooltipFlag flag,
                                           CallbackInfoReturnable<List<Component>> callback) {
        ItemStack itemStack = (ItemStack) (Object) this;
        EquipmentTierSet tiers = EquipmentTierRules.readKnownTiers(itemStack);
        boolean levelable = ToolLevelRules.isLevelable(itemStack);
        if (tiers == null && !levelable) {
            return;
        }

        // 반환된 목록이 수정 가능하다고 보장되지 않으므로 새 목록에 담아 돌려준다.
        List<Component> lines = new ArrayList<>(callback.getReturnValue());
        int tierLineIndex = Math.min(TIER_LINE_INDEX, lines.size());
        lines.addAll(tierLineIndex, EquipmentTierSummary.describe(itemStack, tiers));
        callback.setReturnValue(lines);
    }
}
