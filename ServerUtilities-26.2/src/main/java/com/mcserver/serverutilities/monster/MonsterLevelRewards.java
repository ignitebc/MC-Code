package com.mcserver.serverutilities.monster;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/** 몬스터 레벨에 따라 처치 보상을 지급한다. */
public final class MonsterLevelRewards {
    private static final int MIN_REWARD_LEVEL = 3;
    private static final int REWARD_COUNT = 1;

    private static final List<Item> REWARD_ITEMS = List.of(
            Items.IRON_INGOT,
            Items.COPPER_INGOT,
            Items.GOLD_INGOT,
            Items.COAL,
            Items.GLASS,
            Items.EMERALD,
            Items.LAPIS_LAZULI
    );

    private MonsterLevelRewards() { }

    /** 플레이어가 처치한 LV3~LV7 몬스터에게서 재료 한 종류를 한 개 드롭한다. */
    public static void dropMaterialReward(ServerLevel level, Mob mob, int monsterLevel,
            boolean killedByPlayer) {
        if (!killedByPlayer) return;
        if (!isRewardLevel(monsterLevel)) return;

        int rewardIndex = mob.getRandom().nextInt(REWARD_ITEMS.size());
        Item rewardItem = REWARD_ITEMS.get(rewardIndex);
        ItemStack rewardStack = new ItemStack(rewardItem, REWARD_COUNT);

        ItemEntity rewardEntity = new ItemEntity(
                level,
                mob.getX(),
                mob.getY(),
                mob.getZ(),
                rewardStack
        );
        rewardEntity.setDefaultPickUpDelay();
        level.addFreshEntity(rewardEntity);
    }

    private static boolean isRewardLevel(int monsterLevel) {
        return monsterLevel >= MIN_REWARD_LEVEL && monsterLevel <= MonsterLevel.MAX_SCORE;
    }
}
