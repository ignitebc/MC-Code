package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.ServerUtilities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public abstract class CreeperExplosionDamageMixin {
    @ModifyVariable(method = "hurtServer", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private float serverutilities$scaleCreeperDamage(float amount, ServerLevel level, DamageSource source) {
        var config = ServerUtilities.config();
        if (config.creeperDamage() && source.is(DamageTypeTags.IS_EXPLOSION) && source.getEntity() instanceof Creeper) {
            return amount * config.creeperMultiplier();
        }
        return amount;
    }
}
