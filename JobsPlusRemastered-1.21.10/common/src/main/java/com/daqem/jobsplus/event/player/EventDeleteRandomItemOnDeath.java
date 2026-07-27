package com.daqem.jobsplus.event.player;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.player.JobsServerPlayer;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 플레이어가 죽을 때 사망 시 아이템 보존권이 있으면 1개를 소비하고 모든 소지품을 보호한다.
 *
 * 보존권이 없으면 소지품 한 칸을 무작위로 골라 완전히 삭제하고 서버 전체에 알린다.
 * 삭제 대상과 보존권 검색 대상에는 방어구와 보조 손 칸도 포함된다.
 */
public final class EventDeleteRandomItemOnDeath
{

    private static final ResourceLocation DEATH_ITEM_PROTECTION_SCROLL_ID =
            ResourceLocation.fromNamespaceAndPath("advancednetherite", "death_item_protection_scroll");

    private EventDeleteRandomItemOnDeath()
    {
    }

    public static void registerEvent()
    {
        EntityEvent.LIVING_DEATH.register((entity, damageSource) ->
        {
            if (entity instanceof ServerPlayer serverPlayer)
            {
                handleDeath(serverPlayer);
            }
            return EventResult.pass();
        });
    }

    private static void handleDeath(ServerPlayer player)
    {
        // 크리에이티브는 소지품을 잃지 않으므로 제외한다.
        if (player.isCreative() || player.isSpectator())
        {
            return;
        }

        Inventory inventory = player.getInventory();
        if (player instanceof JobsServerPlayer jobsServerPlayer
                && consumeDeathItemProtectionScroll(inventory))
        {
            jobsServerPlayer.jobsplus$setDeathItemProtected(true);
            broadcast(
                    player,
                    JobsPlus.translatable("death.items_protected", formatPlayerName(player))
            );
            return;
        }

        List<Integer> filledSlots = collectFilledSlots(inventory);
        if (filledSlots.isEmpty())
        {
            broadcast(player, JobsPlus.translatable("death.no_item_lost", formatPlayerName(player)));
            return;
        }

        RandomSource random = player.getRandom();
        int selectedSlot = filledSlots.get(random.nextInt(filledSlots.size()));

        ItemStack removed = inventory.removeItemNoUpdate(selectedSlot);
        if (removed.isEmpty())
        {
            broadcast(player, JobsPlus.translatable("death.no_item_lost", formatPlayerName(player)));
            return;
        }

        Component itemName = describeItem(removed).copy().withStyle(ChatFormatting.RED);
        broadcast(player, JobsPlus.translatable("death.item_lost", formatPlayerName(player), itemName));
    }

    private static boolean consumeDeathItemProtectionScroll(Inventory inventory)
    {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++)
        {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty()
                    || !DEATH_ITEM_PROTECTION_SCROLL_ID.equals(
                            BuiltInRegistries.ITEM.getKey(stack.getItem())))
            {
                continue;
            }

            stack.shrink(1);
            if (stack.isEmpty())
            {
                inventory.setItem(slot, ItemStack.EMPTY);
            }
            inventory.setChanged();
            return true;
        }
        return false;
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

    private static Component formatPlayerName(ServerPlayer player)
    {
        return player.getName().copy().withStyle(ChatFormatting.GOLD);
    }

    private static void broadcast(ServerPlayer player, Component message)
    {
        MinecraftServer server = player.level().getServer();
        if (server != null)
        {
            server.getPlayerList().broadcastSystemMessage(message, false);
        }
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
