package com.mcserver.serverutilities.monster;

import com.mcserver.serverutilities.ServerUtilities;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

import java.util.ArrayList;
import java.util.List;

/**
 * 플레이어가 처치한 크리퍼의 전리품 화약을 크리퍼 레벨 배수만큼 늘린다.
 *
 * <p>전리품 표가 굴린 결과(약탈 마법부여 포함)에 곱하므로 늘어난 화약은 확률 없이 그대로 나온다.
 * 직업 드롭 배수는 아이템을 떨어뜨리는 단계에서 따로 곱하므로 이 배수 위에 겹친다.
 * 자동 농장처럼 플레이어가 처치하지 않은 크리퍼는 바닐라 그대로다.
 */
public final class CreeperGunpowderDrops {
    private CreeperGunpowderDrops() { }

    public static void register() {
        LootTableEvents.MODIFY_DROPS.register(CreeperGunpowderDrops::multiplyGunpowder);
    }

    private static void multiplyGunpowder(Holder<LootTable> table, LootContext context, List<ItemStack> drops) {
        int multiplier = gunpowderMultiplier(context);
        if (multiplier <= CreeperLevel.VANILLA_GUNPOWDER_MULTIPLIER) return;

        // 한 묶음 최대 개수를 넘는 몫은 새 묶음으로 나눠 붙인다. 반복 중에 목록을 바꾸지 않도록 따로 모은다.
        List<ItemStack> overflowStacks = new ArrayList<>();
        for (ItemStack stack : drops) {
            if (stack.getItem() != Items.GUNPOWDER) continue;

            int totalCount = stack.getCount() * multiplier;
            int maxStackSize = stack.getMaxStackSize();
            stack.setCount(Math.min(totalCount, maxStackSize));

            int remainingCount = totalCount - stack.getCount();
            while (remainingCount > 0) {
                int splitCount = Math.min(remainingCount, maxStackSize);
                overflowStacks.add(stack.copyWithCount(splitCount));
                remainingCount -= splitCount;
            }
        }
        drops.addAll(overflowStacks);
    }

    /** 플레이어가 처치한 크리퍼의 사망 전리품이면 레벨 배수, 아니면 바닐라 배수 */
    private static int gunpowderMultiplier(LootContext context) {
        if (!ServerUtilities.config().creeperLevels()) return CreeperLevel.VANILLA_GUNPOWDER_MULTIPLIER;

        Entity entity = context.getOptionalParameter(LootContextParams.THIS_ENTITY);
        if (!(entity instanceof Creeper creeper)) return CreeperLevel.VANILLA_GUNPOWDER_MULTIPLIER;

        // 바닐라는 플레이어가 처치했을 때만 사망 전리품 문맥에 이 값을 넣는다.
        boolean killedByPlayer = context.hasParameter(LootContextParams.LAST_DAMAGE_PLAYER);
        if (!killedByPlayer) return CreeperLevel.VANILLA_GUNPOWDER_MULTIPLIER;

        int level = ((MonsterEquipmentAccess) creeper).serverutilities$monsterLevel();
        return CreeperLevel.gunpowderMultiplier(level);
    }
}
