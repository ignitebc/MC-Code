package com.mcserver.serverutilities.monster;

import com.mcserver.serverutilities.ServerUtilities;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;

/** 크리퍼 폭발에 적용할 레벨 배율을 정한다. 피해 보정과 블록 파괴 범위 보정이 같은 값을 쓴다. */
public final class CreeperExplosionRules {
    private CreeperExplosionRules() { }

    /**
     * 폭발을 일으킨 개체의 배율. 크리퍼가 아니거나 설정이 꺼져 있으면 바닐라 배율이다.
     *
     * @param source 폭발을 일으킨 개체. 없으면 null
     */
    public static float multiplier(Entity source) {
        if (!ServerUtilities.config().creeperLevels()) return CreeperLevel.VANILLA_MULTIPLIER;
        if (!(source instanceof Creeper creeper)) return CreeperLevel.VANILLA_MULTIPLIER;
        int level = ((MonsterEquipmentAccess) creeper).serverutilities$monsterLevel();
        return CreeperLevel.explosionMultiplier(level);
    }
}
