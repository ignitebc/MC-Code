package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.tier.EquipmentTier;
import com.mcserver.serverutilities.tier.EquipmentTierRules;
import net.minecraft.ChatFormatting;
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
    /** 아이템 이름 바로 아래, 능력치 줄보다 위에 등급을 넣는다. */
    private static final int TIER_LINE_INDEX = 1;

    @Inject(method = "getTooltipLines", at = @At("RETURN"), cancellable = true)
    private void serverutilities$appendTier(Item.TooltipContext context, Player player, TooltipFlag flag,
                                           CallbackInfoReturnable<List<Component>> callback) {
        ItemStack itemStack = (ItemStack) (Object) this;
        EquipmentTier tier = EquipmentTierRules.readTier(itemStack);
        if (tier == null) {
            return;
        }

        // 반환된 목록이 수정 가능하다고 보장되지 않으므로 새 목록에 담아 돌려준다.
        List<Component> lines = new ArrayList<>(callback.getReturnValue());
        Component tierLine = Component.literal(tier.level() + "티어").withStyle(ChatFormatting.YELLOW);
        lines.add(Math.min(TIER_LINE_INDEX, lines.size()), tierLine);
        callback.setReturnValue(lines);
    }
}
