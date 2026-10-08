package com.autovw.advancednetherite.common.entity;

import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;

/**
 * 공격 대상이 생긴 그 틱에 바로 달려가는 근접 공격.
 *
 * <p>바닐라 MeleeAttackGoal은 시작 여부를 1초에 한 번만 검사한다. 그 사이에는 다음 순위인 주인 따라가기가 먼저 움직여,
 * 사냥감을 고른 펫이 주인 쪽으로 몸을 돌려 걷다가 다시 사냥감에게 달려가는 모습이 나온다.
 * 여기서는 대상이 있으면 곧바로 시작해 주인 따라가기가 끼어들 틈을 없앤다.
 *
 * <p>닿을 수 없는 대상이면 걸어서 갈 수 있는 가장 가까운 곳까지 다가간다.
 * 그래도 10초 동안 한 대도 못 때리면 펫이 추격을 놓고 주인에게 돌아간다.
 */
public class PetMeleeAttackGoal extends MeleeAttackGoal
{
    private final DialgaPetEntity pet;
    private final double speedModifier;

    public PetMeleeAttackGoal(DialgaPetEntity pet, double speedModifier)
    {
        super(pet, speedModifier, true);
        this.pet = pet;
        this.speedModifier = speedModifier;
    }

    @Override
    public boolean canUse()
    {
        LivingEntity target = this.pet.getTarget();
        if (target == null || !target.isAlive())
        {
            return false;
        }
        // 크리에이티브·관전자는 공격을 이어 가지 못해 곧바로 중단된다. 시작과 중단이 매번 되풀이되지 않게 처음부터 고르지 않는다.
        return EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(target);
    }

    @Override
    public void start()
    {
        super.start();
        // 부모는 시작 검사 때 찾아 둔 길로 출발하는데, 여기서는 검사 때 길을 찾지 않으므로 대상에게 바로 길을 낸다.
        this.pet.getNavigation().moveTo(this.pet.getTarget(), this.speedModifier);
    }
}
