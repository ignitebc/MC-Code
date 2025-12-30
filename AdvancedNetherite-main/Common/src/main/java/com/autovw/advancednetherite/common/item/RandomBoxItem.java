package com.autovw.advancednetherite.common.item;

import com.autovw.advancednetherite.common.randombox.RandomBoxConfig;
import com.autovw.advancednetherite.common.randombox.RandomBoxConfigManager;
import net.minecraft.ChatFormatting;
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

        // 소모 처리
        removeItem(inv, keyItem, consumeKey);
        boxStack.shrink(consumeBox);

        // 보상 목록
        List<RandomBoxConfig.Reward> rewards = config.rewards;
        if (rewards == null || rewards.isEmpty()) {
            player.displayClientMessage(Component.literal("보상 항목이 없습니다."), true);
            return InteractionResult.CONSUME;
        }

        RandomSource rnd = player.getRandom();
        List<ItemStack> givenRewards = new ArrayList<>();

        if (config.roll_mode == RandomBoxConfig.RollMode.SINGLE) {
            // SINGLE: chance를 "가중치(weight)"로 사용하여 반드시 1개를 뽑는다.
            // 단, 레지스트리에서 아이템을 못 찾는(EMPTY) 케이스가 있으면 후보에서 제거하고 재시도해서
            // 결과적으로 "없음" 방송이 뜨는 것을 방지한다.
            List<RandomBoxConfig.Reward> pool = new ArrayList<>(rewards);

            while (!pool.isEmpty()) {
                RandomBoxConfig.Reward chosen = pickOneRewardWeighted(pool, rnd);
                if (chosen == null) {
                    break;
                }

                ItemStack given = giveReward(player, chosen);
                if (given != null && !given.isEmpty()) {
                    givenRewards.add(given);
                    break; // SINGLE이므로 1개 지급 후 종료
                }

                // 지급 실패(대부분 아이템 레지스트리 누락/오타)면 후보에서 제외 후 재시도
                pool.remove(chosen);
            }

        } else {
            // INDEPENDENT: 각 보상을 독립 확률로 판정
            for (RandomBoxConfig.Reward r : rewards) {
                if (r == null || r.item == null) {
                    continue;
                }

                double prob = normalizeChanceToProbability(r.chance);
                if (rnd.nextDouble() <= prob) {
                    ItemStack given = giveReward(player, r);
                    if (given != null && !given.isEmpty()) {
                        givenRewards.add(given);
                    }
                }
            }
        }

        // 전체 방송 메시지 (빨간색)
        String openerName = player.getName().getString();
        String boxName = boxStack.getHoverName().getString();
        String rewardText = buildRewardText(givenRewards);

        Component broadcast = Component.literal(openerName + "님이 " + boxName + " 오픈!!! 보상은 ★" + rewardText + "★ 입니다").withStyle(ChatFormatting.RED);

        server.getPlayerList().broadcastSystemMessage(broadcast, false);

        return InteractionResult.CONSUME;
    }

    /**
     * BuiltInRegistries.ITEM.get(ResourceKey) -> Optional<Holder.Reference<Item>>
     */
    private static Item getItemOrNull(ResourceLocation id) {
        if (id == null) {
            return null;
        }

        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        Optional<Holder.Reference<Item>> opt = BuiltInRegistries.ITEM.get(key);

        Item item = opt.map(Holder.Reference::value).orElse(Items.AIR);
        return (item == Items.AIR) ? null : item;
    }

    /**
     * SINGLE 전용: 가중치 기반 1개 선택
     * - chance(가중치)는 정수/실수 모두 허용 (예: 1.5, 0.5)
     */
    private static RandomBoxConfig.Reward pickOneRewardWeighted(List<RandomBoxConfig.Reward> rewards, RandomSource rnd) {
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
            if (roll <= acc) {
                return r;
            }
        }

        // 부동소수 오차 대비: 마지막 반환
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

        // 인벤토리가 가득찼을경우, 바닥에 드롭
        if (!player.getInventory().add(rewardStack.copy())) {
            player.drop(rewardStack.copy(), false);
        }

        return rewardStack.copy();
    }

    private static String buildRewardText(List<ItemStack> givenRewards) {
        if (givenRewards == null || givenRewards.isEmpty()) {
            return "없음";
        }

        // 같은 아이템이 여러 번 들어올 수 있어 합산
        List<ItemStack> merged = new ArrayList<>();
        outer:
        for (ItemStack s : givenRewards) {
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
            sb.append(s.getHoverName().getString()).append(" x").append(s.getCount());
            if (i < merged.size() - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    /**
     * INDEPENDENT 확률 정규화:
     * - 0~1: 확률로 간주 (1.0 = 100%)
     * - 1 초과: 퍼센트로 간주 (80 = 80%)
     */
    private static double normalizeChanceToProbability(double chance) {
        if (Double.isNaN(chance) || Double.isInfinite(chance) || chance <= 0.0) {
            return 0.0;
        }

        double prob = (chance <= 1.0) ? chance : (chance / 100.0);

        if (prob < 0.0) return 0.0;
        if (prob > 1.0) return 1.0;
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
