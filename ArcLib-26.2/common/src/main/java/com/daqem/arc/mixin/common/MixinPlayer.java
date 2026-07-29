package com.daqem.arc.mixin.common;

import com.daqem.arc.api.action.data.ActionDataBuilder;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.action.type.ActionType;
import com.daqem.arc.api.player.ArcPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class MixinPlayer extends LivingEntity {

    protected MixinPlayer(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(at = @At("RETURN"), method = "getCurrentItemAttackStrengthDelay()F", cancellable = true)
    private void getCurrentItemAttackStrengthDelay(CallbackInfoReturnable<Float> cir) {
        if (this instanceof ArcPlayer arcPlayer) {
            cir.setReturnValue(
                    cir.getReturnValue()
                            * new ActionDataBuilder(arcPlayer, ActionType.GET_ATTACK_SPEED)
                            .withData(ActionDataType.ITEM_STACK, this.getMainHandItem())
                            .withData(ActionDataType.ITEM, this.getMainHandItem().getItem())
                            .build()
                            .sendToAction()
                            .getAttackSpeedModifier()
            );
        }
    }
}
