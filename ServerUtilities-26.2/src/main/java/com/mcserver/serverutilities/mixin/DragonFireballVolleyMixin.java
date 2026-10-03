package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.boss.DragonFireballVolley;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.phases.DragonStrafePlayerPhase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(DragonStrafePlayerPhase.class)
public abstract class DragonFireballVolleyMixin {
    // 발사 단계의 생성 한 곳만 바꿔 차지 시간·효과음·다음 페이즈 전환은 한 번씩 유지한다.
    @Redirect(method = "doServerTick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"),
            require = 1, allow = 1)
    private boolean serverutilities$spawnVolley(ServerLevel level, Entity fireball) {
        return DragonFireballVolley.spawn(level, fireball);
    }
}
