package com.autovw.advancednetherite.common.item;

import com.autovw.advancednetherite.common.randombox.RandomBoxConfig;
import com.autovw.advancednetherite.common.randombox.RandomBoxConfigManager;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RandomBoxItem extends AdvancedItem {

    private static final Logger LOGGER = LogUtils.getLogger();

    private final Identifier configId;

    public RandomBoxItem(Properties properties, Identifier configId) {
        super(properties);
        this.configId = configId;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack boxStack = player.getItemInHand(hand);

        Component boxNameComponent = boxStack.getHoverName();

        // 클라에서는 성공 반환(입력/애니메이션 흐름 유지)
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        MinecraftServer server = level.getServer();
        if (server == null) {
            return InteractionResult.FAIL;
        }

        RandomBoxConfig config = RandomBoxConfigManager.get(server, configId);
        if (config == null) {
            player.sendOverlayMessage(Component.literal("상자 설정(JSON)을 찾지 못했습니다: " + configId));
            return InteractionResult.FAIL;
        }

        int consumeBox = (config.consume != null && config.consume.box > 0) ? config.consume.box : 1;
        int consumeKey = (config.consume != null && config.consume.key > 0) ? config.consume.key : 1;

        if (config.required_key == null) {
            player.sendOverlayMessage(Component.literal("required_key가 설정되지 않았습니다: " + configId));
            return InteractionResult.FAIL;
        }

        Item keyItem = getItemOrNull(config.required_key);
        if (keyItem == null) {
            player.sendOverlayMessage(Component.literal("열쇠 아이템을 찾지 못했습니다: " + config.required_key));
            return InteractionResult.FAIL;
        }

        Inventory inv = player.getInventory();

        int keyHave = countItem(inv, keyItem);
        if (keyHave < consumeKey) {
            player.sendOverlayMessage(Component.literal("열쇠가 부족합니다."));
            return InteractionResult.FAIL;
        }

        if (boxStack.getCount() < consumeBox) {
            player.sendOverlayMessage(Component.literal("상자 수량이 부족합니다."));
            return InteractionResult.FAIL;
        }

        // =========================
        // 1) 보상 설정 검증 (소모 전에 확인해서 설정 오류로 상자·열쇠만 사라지는 일을 막는다)
        // =========================
        List<RandomBoxConfig.Reward> rewards = config.rewards;
        if (rewards == null || rewards.isEmpty()) {
            player.sendOverlayMessage(Component.literal("보상 항목이 없습니다: " + configId));
            return InteractionResult.FAIL;
        }

        boolean hasValidReward = false;
        for (RandomBoxConfig.Reward reward : rewards) {
            if (getItemOrNull(reward.item) != null) {
                hasValidReward = true;
                break;
            }
        }
        if (!hasValidReward) {
            player.sendOverlayMessage(Component.literal("유효한 보상 아이템이 없습니다: " + configId));
            return InteractionResult.FAIL;
        }

        // =========================
        // 2) 소모 처리
        // =========================
        removeItem(inv, keyItem, consumeKey);
        boxStack.shrink(consumeBox);

        // 상자가 0개가 되면 즉시 손 슬롯 비우기(경계 상황 씹힘 완화)
        if (boxStack.isEmpty()) {
            player.setItemInHand(hand, ItemStack.EMPTY);
        }

        // 즉시 동기화
        inv.setChanged();
        player.containerMenu.broadcastChanges();

        RandomSource rnd = player.getRandom();
        List<ItemStack> givenRewardsForBroadcast = new ArrayList<>();

        if (config.roll_mode == RandomBoxConfig.RollMode.SINGLE) {
            List<RandomBoxConfig.Reward> pool = new ArrayList<>(rewards);

            while (!pool.isEmpty()) {
                RandomBoxConfig.Reward chosen = pickOneRewardWeighted(pool, rnd);
                if (chosen == null) break;

                Item chosenItem = getItemOrNull(chosen.item);
                if (chosenItem == null) {
                    pool.remove(chosen);
                    continue;
                }

                List<ItemStack> given = giveRewardSplit(player, chosen);
                if (!given.isEmpty()) givenRewardsForBroadcast.addAll(given);
                break;
            }
        } else {
            for (RandomBoxConfig.Reward r : rewards) {
                double prob = normalizeChanceToProbability(r.chance);
                double roll = rnd.nextDouble();

                if (roll <= prob) {
                    List<ItemStack> given = giveRewardSplit(player, r);
                    if (!given.isEmpty()) givenRewardsForBroadcast.addAll(given);
                }
            }
        }

        // 인벤토리에 들어간 보상이 바로 보이도록 다시 동기화한다.
        inv.setChanged();
        player.containerMenu.broadcastChanges();

        // ✅ 여기서 "동일 아이템"을 합산해서 방송 문구를 통합 표시
        MutableComponent rewardComponent = buildRewardComponent(givenRewardsForBroadcast);

        MutableComponent broadcast = Component.literal(player.getName().getString() + "님이 ")
                .append(boxNameComponent)
                .append(Component.literal(" 오픈!!! 보상은 ★"))
                .append(rewardComponent)
                .append(Component.literal("★ 입니다"))
                .withStyle(ChatFormatting.RED);

        server.getPlayerList().broadcastSystemMessage(broadcast, false);

        return InteractionResult.CONSUME;
    }

    private static Item getItemOrNull(Identifier id) {
        if (id == null) return null;

        Item item = BuiltInRegistries.ITEM.get(id).map(Holder.Reference::value).orElse(null);
        if (item == null) {
            LOGGER.warn("RandomBox reward item not found in registry, skipping: {}", id);
        }
        return item;
    }

    private static RandomBoxConfig.Reward pickOneRewardWeighted(List<RandomBoxConfig.Reward> candidates, RandomSource rnd) {
        if (candidates == null || candidates.isEmpty()) return null;

        double total = 0.0;
        for (RandomBoxConfig.Reward r : candidates) total += Math.max(r.chance, 0.0);

        if (total <= 0.0) return candidates.get(rnd.nextInt(candidates.size()));

        double roll = rnd.nextDouble() * total;
        double acc = 0.0;

        for (RandomBoxConfig.Reward r : candidates) {
            acc += Math.max(r.chance, 0.0);
            if (roll <= acc) return r;
        }
        return candidates.get(candidates.size() - 1);
    }

    /**
     * chance는 항상 % 단위로 해석한다. (0.5 = 0.5%, 50 = 50%)
     * 과거에는 1 이하 값을 0~1 확률로 해석했지만, 0.5%를 50%로 오독하는 사고를 막기 위해 통일했다.
     */
    private static double normalizeChanceToProbability(double chance) {
        if (Double.isNaN(chance) || Double.isInfinite(chance) || chance <= 0.0) return 0.0;
        double prob = chance / 100.0;
        if (prob > 1.0) return 1.0;
        return prob;
    }

    /**
     * 보상은 인벤토리에 먼저 넣고, 자리가 없어 들어가지 못한 수량만 바닥에 떨어뜨린다.
     * - maxStack 기준으로 쪼개서 지급한다.
     * - 방송 표기용으로는 쪼개진 스택 리스트를 반환.
     */
    private static List<ItemStack> giveRewardSplit(Player player, RandomBoxConfig.Reward r) {
        int totalCount = Math.max(r.count, 1);

        Item rewardItem = getItemOrNull(r.item);
        if (rewardItem == null) {
            return List.of();
        }

        int maxStack = Math.max(1, new ItemStack(rewardItem).getMaxStackSize());

        List<ItemStack> given = new ArrayList<>();
        int left = totalCount;

        while (left > 0) {
            int give = Math.min(left, maxStack);
            ItemStack stack = new ItemStack(rewardItem, give);

            // Inventory.add가 넣은 만큼 스택을 줄여 주므로, 남은 수량만 드롭된다.
            player.getInventory().add(stack);
            if (!stack.isEmpty()) {
                player.drop(stack, false);
            }

            // 방송용 표기 리스트
            given.add(new ItemStack(rewardItem, give));
            left -= give;
        }

        return given;
    }

    /**
     * ✅ 동일 아이템이 여러 스택으로 들어와도 (ex: 토템 x1, x1, x1)
     *    방송 문구에서는 (토템 x3)으로 통합해서 보여준다.
     */
    private static MutableComponent buildRewardComponent(List<ItemStack> givenRewards) {
        if (givenRewards == null || givenRewards.isEmpty()) return Component.literal("없음");

        // insertion-order 유지 + 아이템별 합산
        Map<Identifier, Integer> merged = new LinkedHashMap<>();

        for (ItemStack s : givenRewards) {
            if (s == null || s.isEmpty()) continue;

            Identifier id = BuiltInRegistries.ITEM.getKey(s.getItem());
            if (id == null) continue;

            merged.merge(id, s.getCount(), Integer::sum);
        }

        if (merged.isEmpty()) return Component.literal("없음");

        MutableComponent result = Component.empty();
        boolean first = true;

        for (Map.Entry<Identifier, Integer> e : merged.entrySet()) {
            Item item = getItemOrNull(e.getKey());
            if (item == null) continue;

            int totalCount = e.getValue();
            MutableComponent part = Component.empty()
                    .append(new ItemStack(item).getHoverName())
                    .append(Component.literal(" x" + totalCount));

            if (!first) result.append(Component.literal(", "));
            result.append(part);
            first = false;
        }

        return first ? Component.literal("없음") : result;
    }

    private static int countItem(Inventory inv, Item item) {
        int total = 0;
        int size = inv.getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.getItem() == item) total += stack.getCount();
        }
        return total;
    }

    private static void removeItem(Inventory inv, Item item, int count) {
        int left = count;
        int size = inv.getContainerSize();

        for (int i = 0; i < size && left > 0; i++) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;

            if (stack.getItem() == item) {
                int take = Math.min(stack.getCount(), left);
                stack.shrink(take);
                left -= take;
                if (stack.getCount() <= 0) inv.setItem(i, ItemStack.EMPTY);
            }
        }
    }
}
