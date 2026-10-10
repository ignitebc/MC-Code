package com.tacz.guns.event;

import com.tacz.guns.api.item.IGun;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class PreventGunClick {
    public static InteractionResult onLeftClickBlock(Player player, Level world, InteractionHand hand, BlockPos pos, Direction direction) {
        // 주 손에 총이 있으면 상호작용을 막는다
        ItemStack itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (itemInHand.getItem() instanceof IGun) {
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }
}
