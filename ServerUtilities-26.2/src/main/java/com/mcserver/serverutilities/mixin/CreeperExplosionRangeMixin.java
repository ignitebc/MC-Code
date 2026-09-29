package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.monster.CreeperExplosionRules;
import net.minecraft.world.level.ServerExplosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerExplosion.class)
abstract class CreeperExplosionRangeMixin {
    /**
     * 블록을 부수는 광선의 세기에만 크리퍼 레벨 배율을 곱한다.
     * <p>
     * 이 메서드는 폭발 반경을 광선 세기 계산에 한 번만 읽는다. 반경은 final 필드라 쓰기가 없으므로 읽기만 맞는다.
     * 엔티티 피해와 넉백은 hurtEntities가 따로 읽으므로 바닐라 범위 그대로다.
     * 대상이 없거나 둘 이상이면 Mixin 적용을 실패시켜 버전 변경으로 인한 오적용을 드러낸다.
     */
    @Redirect(
            method = "calculateExplodedPositions",
            at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/ServerExplosion;radius:F"),
            require = 1,
            allow = 1)
    private float serverutilities$scaleCreeperBlockRange(ServerExplosion explosion) {
        return explosion.radius() * CreeperExplosionRules.multiplier(explosion.getDirectSourceEntity());
    }
}
