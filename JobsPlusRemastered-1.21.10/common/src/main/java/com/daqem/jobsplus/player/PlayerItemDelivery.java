package com.daqem.jobsplus.player;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * 플레이어 보상을 인벤토리에 넣고, 들어가지 않은 수량은 바닥에 떨어뜨린다.
 */
public final class PlayerItemDelivery
{

    private PlayerItemDelivery()
    {
    }

    public static void giveOrDrop(ServerPlayer player, ItemStack itemStack)
    {
        if (itemStack.isEmpty())
        {
            return;
        }

        player.getInventory().add(itemStack);
        while (!itemStack.isEmpty())
        {
            int maxStackSize = Math.max(1, itemStack.getMaxStackSize());
            int dropAmount = Math.min(itemStack.getCount(), maxStackSize);
            ItemStack droppedStack = itemStack.split(dropAmount);
            player.drop(droppedStack, false);
        }
    }
}
