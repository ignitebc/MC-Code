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

        // 실제 지급된 보상을 수집 (전체 방송 메시지에 사용)
        List<ItemStack> givenRewards = new ArrayList<>();

        if (config.roll_mode == RandomBoxConfig.RollMode.SINGLE) {
            // SINGLE은 "확률"이 아니라 "가중치" 개념으로 1개를 뽑는 방식이므로
            // chance 값을 그대로 weight로 사용합니다. (예: 70/30)
            RandomBoxConfig.Reward chosen = pickOneRewardWeighted(rewards, rnd);
            if (chosen != null) {
                ItemStack given = giveReward(player, chosen);
                if (given != null && !given.isEmpty()) {
                    givenRewards.add(given);
                }
            }
        } else {
            // MULTI는 각 보상별 확률 판정이므로
            // chance를 항상 "퍼센트"로 해석: 0.03 -> 0.03%, 80 -> 80%
            for (RandomBoxConfig.Reward r : rewards) {
                if (r == null || r.item == null) {
                    continue;
                }

                double prob = normalizeChancePercent(r.chance); // 0~1 확률값으로 변환
                if (rnd.nextDouble() <= prob) {
                    ItemStack given = giveReward(player, r);
                    if (given != null && !given.isEmpty()) {
                        givenRewards.add(given);
                    }
                }
            }
        }

        // 전체 방송 메시지
        String openerName = player.getName().getString();
        String boxName = boxStack.getHoverName().getString(); // 로컬라이징된 아이템명 사용
        String rewardText = buildRewardText(givenRewards);

        Component broadcast = Component.literal(
                openerName + "님이 " + boxName + " 오픈!!! 축하합니다! 보상은 " + rewardText + " 입니다");

        // 서버 전체 채팅으로 방송
        server.getPlayerList().broadcastSystemMessage(broadcast, false);

        return InteractionResult.CONSUME;
    }

    /**
     * 당신 환경: BuiltInRegistries.ITEM.get(ResourceKey) ->
     * Optional<Holder.Reference<Item>>
     * 따라서 Optional/Holder.Reference에서 Item을 꺼내는 방식으로 구현.
     *
     * 주의: BuiltInRegistries.ITEM.get(id) 형태는 사용하지 않습니다.
     */
    private static Item getItemOrNull(ResourceLocation id) {
        if (id == null) {
            return null;
        }

        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);

        // 여기서 반환 타입이 Optional<Holder.Reference<Item>> 로 잡혀야 타입 불일치가 발생하지 않습니다.
        Optional<Holder.Reference<Item>> opt = BuiltInRegistries.ITEM.get(key);

        Item item = opt.map(Holder.Reference::value).orElse(Items.AIR);
        return (item == Items.AIR) ? null : item;
    }

    private static RandomBoxConfig.Reward pickOneRewardWeighted(List<RandomBoxConfig.Reward> rewards,
            RandomSource rnd) {
        List<RandomBoxConfig.Reward> candidates = new ArrayList<>();
        double total = 0.0;

        for (RandomBoxConfig.Reward r : rewards) {
            if (r == null || r.item == null) {
                continue;
            }

            double w = r.chance;
            if (Double.isNaN(w) || Double.isInfinite(w) || w <= 0.0) {
                continue;
            }

            candidates.add(r);
            total += w;
        }

        if (candidates.isEmpty() || total <= 0.0) {
            return null;
        }

        double roll = rnd.nextDouble() * total;
        double acc = 0.0;

        for (RandomBoxConfig.Reward r : candidates) {
            acc += r.chance;
            if (roll < acc) {
                return r;
            }
        }

        return candidates.get(candidates.size() - 1);
    }

    /**
     * 보상을 지급하고, 실제 지급된 ItemStack(표시용)을 반환
     */
    private static ItemStack giveReward(Player player, RandomBoxConfig.Reward r) {
        int count = Math.max(r.count, 1);

        Item rewardItem = getItemOrNull(r.item);
        if (rewardItem == null) {
            return ItemStack.EMPTY;
        }

        ItemStack rewardStack = new ItemStack(rewardItem, count);

        if (!player.getInventory().add(rewardStack)) {
            player.drop(rewardStack, false);
        }

        // 표시용으로 복제해서 반환
        return rewardStack.copy();
    }

    private static String buildRewardText(List<ItemStack> givenRewards) {
        if (givenRewards == null || givenRewards.isEmpty()) {
            return "없음";
        }

        // 같은 아이템이 여러 번 들어올 수 있어 합산
        List<ItemStack> merged = new ArrayList<>();
        outer: for (ItemStack s : givenRewards) {
            if (s == null || s.isEmpty()) {
                continue;
            }

            for (ItemStack m : merged) {
                if (ItemStack.isSameItemSameComponents(m, s)) {
                    m.grow(s.getCount());
                    continue outer;
                }
            }
            merged.add(s.copy());
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < merged.size(); i++) {
            ItemStack s = merged.get(i);
            String name = s.getHoverName().getString();
            sb.append(name).append(" x").append(s.getCount());
            if (i < merged.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    /**
     * 요구사항 반영:
     * chance는 항상 "퍼센트"로 입력됨.
     *
     * 예:
     * - 0.03 -> 0.03% -> 0.0003
     * - 80 -> 80% -> 0.8
     */
    private static double normalizeChancePercent(double percent) {
        if (Double.isNaN(percent) || Double.isInfinite(percent)) {
            return 0.0;
        }
        if (percent <= 0.0) {
            return 0.0;
        }

        double prob = percent / 100.0;

        if (prob < 0.0) {
            return 0.0;
        }
        if (prob > 1.0) {
            return 1.0;
        }
        return prob;
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
            if (s.isEmpty() || !s.is(item)) {
                continue;
            }

            int dec = Math.min(s.getCount(), remain);
            s.shrink(dec);
            remain -= dec;

            if (remain <= 0) {
                return;
            }
        }
    }
}
