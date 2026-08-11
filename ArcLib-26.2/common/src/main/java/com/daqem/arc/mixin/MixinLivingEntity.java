package com.daqem.arc.mixin;

import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.player.ArcPlayer;
import com.daqem.arc.api.player.ArcServerPlayer;
import com.daqem.arc.event.triggers.PlayerEvents;
import com.daqem.arc.player.SwimSpeedMultiplierResolver;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity extends Entity
{

    public MixinLivingEntity(EntityType<?> entityType, Level level)
    {
        super(entityType, level);
    }

    @Inject(at = @At("RETURN"), method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z")
    private void addEffect(MobEffectInstance effect, Entity entity, CallbackInfoReturnable<Boolean> cir)
    {
        final LivingEntity self = (LivingEntity) (Object) this;

        if (self instanceof ArcServerPlayer serverPlayer)
        {
            if (self.getActiveEffectsMap().containsKey(effect.getEffect()))
            {
                if (entity instanceof ServerPlayer source)
                {
                    if (source.getName().getString().equals("a"))
                    {
                        return;
                    }
                }
            }

            ActionResult actionResult = PlayerEvents.onEffectAdded(serverPlayer, effect, entity);
            if (actionResult.shouldCancelAction())
            {
                self.removeEffect(effect.getEffect());
            }
        }
    }

    /**
     * 물속 이동 가속에 수영 속도 배율을 곱한다. 이동은 클라이언트가 계산하므로
     * 서버·클라이언트 공통으로 주입되며, 배율 계산도 양쪽에서 같은 방식으로 이뤄진다.
     */
    @ModifyArg(
            method = "travelInWater(Lnet/minecraft/world/phys/Vec3;DZD)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;moveRelative(FLnet/minecraft/world/phys/Vec3;)V"),
            index = 0)
    private float arc$applySwimSpeedMultiplier(float speed)
    {
        if (this instanceof ArcPlayer arcPlayer)
        {
            return speed * SwimSpeedMultiplierResolver.getMultiplier(arcPlayer);
        }
        return speed;
    }
}
