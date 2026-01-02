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
import net.minecraft.resources.ResourceLocation;
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
import java.util.List;

public class RandomBoxItem extends AdvancedItem {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final ResourceLocation configId;

    public RandomBoxItem(Properties properties, ResourceLocation configId) {
        super(properties);
        this.configId = configId;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack boxStack = player.getItemInHand(hand);

        Component boxNameComponent = boxStack.getHoverName();
        ResourceLocation boxItemId = BuiltInRegistries.ITEM.getKey(boxStack.getItem());

        LOGGER.info("[RandomBox] START player={} uuid={} hand={} boxItem={} boxCount={} configId={} boxNameKeyOrText={}",
                player.getName().getString(), player.getUUID(), hand, boxItemId, boxStack.getCount(), configId, boxNameComponent.getString());

        // 클라에서는 성공 반환(입력/애니메이션 흐름 유지)
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        MinecraftServer server = level.getServer();
        if (server == null) {
            LOGGER.warn("[RandomBox] Server is null. FAIL");
            return InteractionResult.FAIL;
        }

        RandomBoxConfig config = RandomBoxConfigManager.get(server, configId);
        LOGGER.info("[RandomBox] Config lookup done. found={}", (config != null));

        if (config == null) {
            player.displayClientMessage(Component.literal("상자 설정(JSON)을 찾지 못했습니다: " + configId), true);
            return InteractionResult.FAIL;
        }

        int consumeBox = (config.consume != null && config.consume.box > 0) ? config.consume.box : 1;
        int consumeKey = (config.consume != null && config.consume.key > 0) ? config.consume.key : 1;

        LOGGER.info("[RandomBox] consumeBox={} consumeKey={} requiredKey={} rollMode={} rewardsCount={}",
                consumeBox, consumeKey, config.required_key, config.roll_mode, (config.rewards == null ? 0 : config.rewards.size()));

        if (config.required_key == null) {
            player.displayClientMessage(Component.literal("required_key가 설정되지 않았습니다: " + configId), true);
            LOGGER.warn("[RandomBox] required_key is null. configId={}", configId);
            return InteractionResult.FAIL;
        }

        Item keyItem = getItemOrNull(config.required_key);
        if (keyItem == null) {
            player.displayClientMessage(Component.literal("열쇠 아이템을 찾지 못했습니다: " + config.required_key), true);
            LOGGER.warn("[RandomBox] key item not found. required_key={} configId={}", config.required_key, configId);
            return InteractionResult.FAIL;
        }

        Inventory inv = player.getInventory();

        int keyHave = countItem(inv, keyItem);
        LOGGER.info("[RandomBox] Key check: have={} need={}", keyHave, consumeKey);
        if (keyHave < consumeKey) {
            player.displayClientMessage(Component.literal("열쇠가 부족합니다."), true);
            return InteractionResult.FAIL;
        }

        LOGGER.info("[RandomBox] Box check: have={} need={}", boxStack.getCount(), consumeBox);
        if (boxStack.getCount() < consumeBox) {
            player.displayClientMessage(Component.literal("상자 수량이 부족합니다."), true);
            return InteractionResult.FAIL;
        }

        // =========================
        // 1) 소모 처리
        // =========================
        removeItem(inv, keyItem, consumeKey);
        boxStack.shrink(consumeBox);

        // 상자가 0개가 되면 즉시 손 슬롯 비우기(경계 상황 씹힘 완화)
        if (boxStack.isEmpty()) {
            player.setItemInHand(hand, ItemStack.EMPTY);
            LOGGER.info("[RandomBox] Consumed to empty -> cleared hand slot BEFORE drop rewards");
        }

        // 즉시 동기화
        inv.setChanged();
        player.containerMenu.broadcastChanges();

        LOGGER.info("[RandomBox] Consumed. handNow={} keyRemaining={}",
                player.getItemInHand(hand).getCount(), countItem(inv, keyItem));

        List<RandomBoxConfig.Reward> rewards = config.rewards;
        if (rewards == null || rewards.isEmpty()) {
            player.displayClientMessage(Component.literal("보상 항목이 없습니다."), true);
            return InteractionResult.CONSUME;
        }

        RandomSource rnd = player.getRandom();
        List<ItemStack> droppedRewardsForBroadcast = new ArrayList<>();

        if (config.roll_mode == RandomBoxConfig.RollMode.SINGLE) {
            List<RandomBoxConfig.Reward> pool = new ArrayList<>(rewards);

            while (!pool.isEmpty()) {
                RandomBoxConfig.Reward chosen = pickOneRewardWeighted(pool, rnd);
                if (chosen == null) break;

                Item chosenItem = getItemOrNull(chosen.item);
                LOGGER.info("[RandomBox] SINGLE pick: chosenItem={} count={} weight(chance)={} valid={}",
                        chosen.item, chosen.count, chosen.chance, (chosenItem != null));

                if (chosenItem == null) {
                    LOGGER.warn("[RandomBox] SINGLE chosen item not found. remove from pool. itemId={}", chosen.item);
                    pool.remove(chosen);
                    continue;
                }

                List<ItemStack> dropped = dropRewardSplit(player, chosen);
                if (!dropped.isEmpty()) droppedRewardsForBroadcast.addAll(dropped);
                break;
            }
        } else {
            for (RandomBoxConfig.Reward r : rewards) {
                double prob = normalizeChanceToProbability(r.chance);
                double roll = rnd.nextDouble();

                LOGGER.info("[RandomBox] MULTI roll: item={} count={} chanceRaw={} prob={} roll={}",
                        r.item, r.count, r.chance, prob, roll);

                if (roll <= prob) {
                    List<ItemStack> dropped = dropRewardSplit(player, r);
                    if (!dropped.isEmpty()) droppedRewardsForBroadcast.addAll(dropped);
                }
            }
        }

        MutableComponent rewardComponent = buildRewardComponent(droppedRewardsForBroadcast);

        MutableComponent broadcast = Component.literal(player.getName().getString() + "님이 ")
                .append(boxNameComponent)
                .append(Component.literal(" 오픈!!! 보상은 ★"))
                .append(rewardComponent)
                .append(Component.literal("★ 입니다"))
                .withStyle(ChatFormatting.RED);

        LOGGER.info("[RandomBox] BROADCAST opener={} rewardsCount={}", player.getName().getString(), droppedRewardsForBroadcast.size());
        server.getPlayerList().broadcastSystemMessage(broadcast, false);

        return InteractionResult.CONSUME;
    }

    private static Item getItemOrNull(ResourceLocation id) {
        if (id == null) return null;
        return BuiltInRegistries.ITEM.get(id).map(Holder.Reference::value).orElse(null);
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

    private static double normalizeChanceToProbability(double chance) {
        if (Double.isNaN(chance) || Double.isInfinite(chance) || chance <= 0.0) return 0.0;
        double prob = (chance <= 1.0) ? chance : (chance / 100.0);
        if (prob < 0.0) return 0.0;
        if (prob > 1.0) return 1.0;
        return prob;
    }

    /**
     * 보상 지급을 인벤이 아니라 "무조건 드랍"으로 고정.
     * - 인벤/슬롯/동기화 이슈로 인한 '같이 지워짐'을 구조적으로 차단.
     * - maxStack 기준으로 쪼개서 여러 개 엔티티로 드랍.
     */
    private static List<ItemStack> dropRewardSplit(Player player, RandomBoxConfig.Reward r) {
        int totalCount = Math.max(r.count, 1);

        Item rewardItem = getItemOrNull(r.item);
        if (rewardItem == null) {
            LOGGER.warn("[RandomBox] dropRewardSplit: item not found. itemId={} totalCount={}", r.item, totalCount);
            return List.of();
        }

        int maxStack = Math.max(1, new ItemStack(rewardItem).getMaxStackSize());

        LOGGER.info("[RandomBox] dropRewardSplit: item={} totalCount={} maxStack={}",
                BuiltInRegistries.ITEM.getKey(rewardItem), totalCount, maxStack);

        List<ItemStack> dropped = new ArrayList<>();
        int left = totalCount;

        while (left > 0) {
            int give = Math.min(left, maxStack);
            ItemStack stack = new ItemStack(rewardItem, give);

            // false = 무작위 던지기(방향), true/false는 프로젝트 상황에 따라 다를 수 있음.
            // 여기서는 "바닥에 생성" 목적이므로 false로 둠.
            player.drop(stack, false);

            LOGGER.info("[RandomBox] dropRewardSplit: dropped item={} count={}",
                    BuiltInRegistries.ITEM.getKey(rewardItem), give);

            // 방송용 표기 리스트
            dropped.add(new ItemStack(rewardItem, give));
            left -= give;
        }

        return dropped;
    }

    private static MutableComponent buildRewardComponent(List<ItemStack> givenRewards) {
        if (givenRewards == null || givenRewards.isEmpty()) return Component.literal("없음");

        MutableComponent result = Component.empty();
        boolean first = true;

        for (ItemStack s : givenRewards) {
            if (s == null || s.isEmpty()) continue;

            MutableComponent part = Component.empty()
                    .append(s.getHoverName())
                    .append(Component.literal(" x" + s.getCount()));

            if (!first) result.append(Component.literal(", "));
            result.append(part);
            first = false;
        }

        if (first) return Component.literal("없음");
        return result;
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
