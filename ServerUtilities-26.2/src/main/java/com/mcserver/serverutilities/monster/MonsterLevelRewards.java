package com.mcserver.serverutilities.monster;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * 몬스터 위험 단계에 따라 처치 보상 재료를 지급한다.
 *
 * <p>장비 몬스터는 단계마다 정한 개수만큼 재료를 따로 뽑으므로 같은 재료가 겹칠 수 있다.
 * 크리퍼는 이전 규칙 그대로 LV3~LV7에서 재료 1개를 준다.
 */
public final class MonsterLevelRewards {
    /** 장비 몬스터의 1~7단계 재료 개수 */
    private static final int[] STAGE_REWARD_COUNTS = {0, 1, 2, 2, 3, 3, 4};
    private static final int CREEPER_MIN_REWARD_LEVEL = 3;
    private static final int CREEPER_REWARD_COUNT = 1;
    /** 한 번 뽑을 때 떨어뜨리는 개수 */
    private static final int REWARD_STACK_SIZE = 1;

    private static final List<Item> REWARD_ITEMS = List.of(
            Items.IRON_INGOT,
            Items.COPPER_INGOT,
            Items.GOLD_INGOT,
            Items.COAL,
            Items.GLASS,
            Items.EMERALD,
            Items.LAPIS_LAZULI,
            Items.GUNPOWDER
    );

    private MonsterLevelRewards() { }

    /** 플레이어가 처치한 몬스터에게서 단계에 맞는 개수만큼 재료를 하나씩 뽑아 드롭한다. */
    public static void dropMaterialReward(ServerLevel level, Mob mob, int monsterLevel,
            boolean killedByPlayer) {
        if (!killedByPlayer) return;

        int rewardCount = rewardCount(monsterLevel, mob instanceof Creeper);
        for (int i = 0; i < rewardCount; i++) {
            dropRandomMaterial(level, mob);
        }
    }

    /**
     * 처치 한 번에 떨어뜨리는 재료 수.
     *
     * @param creeper 크리퍼인지. 크리퍼는 단계표 대신 이전 규칙(LV3~LV7에서 1개)을 쓴다.
     */
    public static int rewardCount(int monsterLevel, boolean creeper) {
        if (creeper) {
            boolean creeperRewardLevel = monsterLevel >= CREEPER_MIN_REWARD_LEVEL
                    && monsterLevel <= CreeperLevel.LEVEL_COUNT;
            if (!creeperRewardLevel) return 0;
            return CREEPER_REWARD_COUNT;
        }

        int stage = MonsterLevel.stage(monsterLevel, false);
        if (stage == MonsterLevel.NONE) return 0;
        return STAGE_REWARD_COUNTS[stage - MonsterLevel.MIN_STAGE];
    }

    private static void dropRandomMaterial(ServerLevel level, Mob mob) {
        int rewardIndex = mob.getRandom().nextInt(REWARD_ITEMS.size());
        Item rewardItem = REWARD_ITEMS.get(rewardIndex);
        ItemStack rewardStack = new ItemStack(rewardItem, REWARD_STACK_SIZE);

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
}
