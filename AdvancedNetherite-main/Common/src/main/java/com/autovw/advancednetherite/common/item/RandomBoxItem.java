package com.autovw.advancednetherite.common.item;

import com.autovw.advancednetherite.common.randombox.RandomBoxConfig;
import com.autovw.advancednetherite.common.randombox.RandomBoxConfigManager;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RandomBoxItem extends AdvancedItem {

    private final ResourceLocation configId;

    public RandomBoxItem(Properties properties, ResourceLocation configId) {
        super(properties);
        this.configId = configId;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack boxStack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        MinecraftServer server = level.getServer();
        if (server == null) {
            return InteractionResult.FAIL;
        }

        RandomBoxConfig config = RandomBoxConfigManager.get(server, configId);
        if (config == null) {
            player.displayClientMessage(Component.literal("상자 설정(JSON)을 찾지 못했습니다: " + configId), true);
            return InteractionResult.FAIL;
        }

        int consumeBox = (config.consume != null && config.consume.box > 0) ? config.consume.box : 1;
        int consumeKey = (config.consume != null && config.consume.key > 0) ? config.consume.key : 1;

        if (config.required_key == null) {
            player.displayClientMessage(Component.literal("required_key가 설정되지 않았습니다: " + configId), true);
            return InteractionResult.FAIL;
        }

        Item keyItem = getItemOrNull(config.required_key);
        if (keyItem == null) {
            player.displayClientMessage(Component.literal("열쇠 아이템을 찾지 못했습니다: " + config.required_key), true);
            return InteractionResult.FAIL;
        }

        Inventory inv = player.getInventory();

        if (countItem(inv, keyItem) < consumeKey) {
            player.displayClientMessage(Component.literal("열쇠가 부족합니다."), true);
            return InteractionResult.FAIL;
        }

        if (boxStack.getCount() < consumeBox) {
            player.displayClientMessage(Component.literal("상자 수량이 부족합니다."), true);
            return InteractionResult.FAIL;
        }

        // 소모
        removeItem(inv, keyItem, consumeKey);
        boxStack.shrink(consumeBox);

        // 보상 지급
        List<RandomBoxConfig.Reward> rewards = config.rewards;
        if (rewards == null || rewards.isEmpty()) {
            player.displayClientMessage(Component.literal("보상 항목이 없습니다."), true);
            return InteractionResult.CONSUME;
        }

        RandomSource rnd = player.getRandom();

        if (config.roll_mode == RandomBoxConfig.RollMode.SINGLE) {
            RandomBoxConfig.Reward chosen = pickOneRewardWeighted(rewards, rnd);
            if (chosen != null) {
                giveReward(player, chosen);
            }
        } else {
            for (RandomBoxConfig.Reward r : rewards) {
                if (r == null || r.item == null)
                    continue;

                double chance = clamp01(r.chance);
                if (rnd.nextDouble() <= chance) {
                    giveReward(player, r);
                }
            }
        }

        player.displayClientMessage(Component.literal("상자를 열었습니다."), true);
        return InteractionResult.CONSUME;
    }

    /**
     * 당신 환경: BuiltInRegistries.ITEM.get(key) -> Optional<Holder.Reference<Item>>
     * 따라서 Optional/Holder에서 Item을 꺼내는 방식으로 구현.
     */
    private static Item getItemOrNull(ResourceLocation id) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);

        Optional<? extends Holder<Item>> opt = BuiltInRegistries.ITEM.get(key);

        Item item = opt
                .map(h -> (Item) h.value())
                .orElse(Items.AIR);

        return (item == Items.AIR) ? null : item;
    }

    private static RandomBoxConfig.Reward pickOneRewardWeighted(List<RandomBoxConfig.Reward> rewards,
            RandomSource rnd) {
        List<RandomBoxConfig.Reward> candidates = new ArrayList<>();
        double total = 0.0;

        for (RandomBoxConfig.Reward r : rewards) {
            if (r == null || r.item == null)
                continue;

            double w = r.chance;
            if (Double.isNaN(w) || Double.isInfinite(w) || w <= 0.0)
                continue;

            candidates.add(r);
            total += w;
        }

        if (candidates.isEmpty() || total <= 0.0)
            return null;

        double roll = rnd.nextDouble() * total;
        double acc = 0.0;

        for (RandomBoxConfig.Reward r : candidates) {
            acc += r.chance;
            if (roll < acc)
                return r;
        }

        return candidates.get(candidates.size() - 1);
    }

    private static void giveReward(Player player, RandomBoxConfig.Reward r) {
        int count = Math.max(r.count, 1);

        Item rewardItem = getItemOrNull(r.item);
        if (rewardItem == null)
            return;

        ItemStack rewardStack = new ItemStack(rewardItem, count);

        if (!player.getInventory().add(rewardStack)) {
            player.drop(rewardStack, false);
        }
    }

    private static double clamp01(double v) {
        if (Double.isNaN(v) || Double.isInfinite(v))
            return 0.0;
        if (v < 0.0)
            return 0.0;
        if (v > 1.0)
            return 1.0;
        return v;
    }

    private static int countItem(Inventory inv, Item item) {
        int total = 0;
        int size = inv.getContainerSize();

        for (int i = 0; i < size; i++) {
            ItemStack s = inv.getItem(i);
            if (!s.isEmpty() && s.is(item)) {
                total += s.getCount();
            }
        }
        return total;
    }

    private static void removeItem(Inventory inv, Item item, int toRemove) {
        int remain = toRemove;
        int size = inv.getContainerSize();

        for (int i = 0; i < size; i++) {
            ItemStack s = inv.getItem(i);
            if (s.isEmpty() || !s.is(item))
                continue;

            int dec = Math.min(s.getCount(), remain);
            s.shrink(dec);
            remain -= dec;

            if (remain <= 0)
                return;
        }
    }
}
