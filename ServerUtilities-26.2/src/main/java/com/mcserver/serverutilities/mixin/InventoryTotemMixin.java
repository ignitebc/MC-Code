package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.death.InventoryTotems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 플레이어는 손에 불사의 토템이 없어도 인벤토리·배낭에 있으면 죽음을 막는다.
 * <p>
 * 바닐라가 양손을 먼저 검사하고, 실패했을 때만 나머지 칸을 찾는다. 몬스터는 바닐라대로 손에 든 토템만 쓴다.
 * 공허 피해와 {@code /kill}은 바닐라처럼 토템이 통하지 않는다.
 */
@Mixin(LivingEntity.class)
public abstract class InventoryTotemMixin {
    @Inject(method = "checkTotemDeathProtection", at = @At("RETURN"), cancellable = true)
    private void serverutilities$useInventoryTotem(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) return;
        if (!((Object) this instanceof ServerPlayer player)) return;
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        if (InventoryTotems.use(player)) cir.setReturnValue(true);
    }
}
