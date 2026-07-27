package com.daqem.jobsplus.event.player;

import com.daqem.jobsplus.JobsPlus;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 플레이어가 죽을 때 소지품 한 칸을 무작위로 골라 완전히 삭제하고 서버 전체에 알린다.
 *
 * 삭제 대상에는 방어구와 보조 손 칸도 포함된다. 사망 시 전리품이 떨어지기 전에 발동하므로
 * 선택된 칸은 바닥에 드롭되지 않고 그대로 사라진다.
 */
public final class EventDeleteRandomItemOnDeath
{

    private EventDeleteRandomItemOnDeath()
    {
    }

    public static void registerEvent()
    {
        EntityEvent.LIVING_DEATH.register((entity, damageSource) ->
        {
            if (entity instanceof ServerPlayer serverPlayer)
            {
                deleteRandomItem(serverPlayer);
            }
            return EventResult.pass();
        });
    }

    private static void deleteRandomItem(ServerPlayer player)
    {
        // 크리에이티브는 소지품을 잃지 않으므로 제외한다.
        if (player.isCreative() || player.isSpectator())
        {
            return;
        }

        Inventory inventory = player.getInventory();
        List<Integer> filledSlots = collectFilledSlots(inventory);
        if (filledSlots.isEmpty())
        {
            return;
        }

        RandomSource random = player.getRandom();
        int selectedSlot = filledSlots.get(random.nextInt(filledSlots.size()));

        ItemStack removed = inventory.removeItemNoUpdate(selectedSlot);
        if (removed.isEmpty())
        {
            return;
        }

        broadcastLoss(player, removed);
    }

    private static List<Integer> collectFilledSlots(Inventory inventory)
    {
        // getContainerSize 는 보관함 36칸에 방어구와 보조 손 칸까지 포함한다.
        List<Integer> filledSlots = new ArrayList<>();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++)
        {
            if (!inventory.getItem(slot).isEmpty())
            {
                filledSlots.add(slot);
            }
        }
        return filledSlots;
    }

    private static void broadcastLoss(ServerPlayer player, ItemStack removed)
    {
        MinecraftServer server = player.level().getServer();
        if (server == null)
        {
            return;
        }

        Component playerName = player.getName().copy().withStyle(ChatFormatting.GOLD);
        Component itemName = describeItem(removed).copy().withStyle(ChatFormatting.RED);

        server.getPlayerList().broadcastSystemMessage(
                JobsPlus.translatable("death.item_lost", playerName, itemName), false);
    }

    /** 여러 개가 든 칸이 사라졌을 때 손실 규모를 알 수 있도록 개수를 함께 표시한다. */
    private static Component describeItem(ItemStack stack)
    {
        if (stack.getCount() <= 1)
        {
            return stack.getHoverName();
        }
        return Component.empty()
                .append(stack.getHoverName())
                .append(Component.literal(" " + stack.getCount() + "개"));
    }
}
