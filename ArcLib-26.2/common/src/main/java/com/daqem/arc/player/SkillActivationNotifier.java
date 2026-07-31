package com.daqem.arc.player;

import com.daqem.arc.api.player.ArcPlayer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class SkillActivationNotifier {

    private SkillActivationNotifier() {
    }

    public static void notifyExtraDrop(ArcPlayer player, ItemStack itemStack) {
        if (player.arc$getPlayer() instanceof ServerPlayer serverPlayer) {
            notifyExtraDrop(serverPlayer, List.of(itemStack));
        }
    }

    public static void notifyExtraDrop(ArcPlayer player, List<ItemStack> itemStacks) {
        if (player.arc$getPlayer() instanceof ServerPlayer serverPlayer) {
            notifyExtraDrop(serverPlayer, itemStacks);
        }
    }

    public static void notifyExtraDrop(ServerPlayer player, ItemStack itemStack) {
        notifyExtraDrop(player, List.of(itemStack));
    }

    public static void notifyExtraDrop(ServerPlayer player, List<ItemStack> itemStacks) {
        List<ItemStack> mergedStacks = mergeStacks(itemStacks);
        if (mergedStacks.isEmpty()) {
            return;
        }

        MutableComponent items = Component.empty();
        for (int index = 0; index < mergedStacks.size(); index++) {
            ItemStack stack = mergedStacks.get(index);
            if (index > 0) {
                items.append(Component.literal(", "));
            }
            items.append(stack.getHoverName())
                    .append(Component.literal(" x" + stack.getCount()));
        }

        player.sendSystemMessage(Component.translatable("arc.skill.extra_drop", items)
                .withStyle(ChatFormatting.GOLD));
    }

    private static List<ItemStack> mergeStacks(List<ItemStack> itemStacks) {
        List<ItemStack> merged = new ArrayList<>();
        for (ItemStack itemStack : itemStacks) {
            if (itemStack == null || itemStack.isEmpty()) {
                continue;
            }

            ItemStack existing = merged.stream()
                    .filter(stack -> ItemStack.isSameItemSameComponents(stack, itemStack))
                    .findFirst()
                    .orElse(null);
            if (existing == null) {
                merged.add(itemStack.copy());
            } else {
                existing.grow(itemStack.getCount());
            }
        }
        return merged;
    }
}
