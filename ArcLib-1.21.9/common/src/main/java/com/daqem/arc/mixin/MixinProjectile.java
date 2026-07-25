package com.daqem.arc.mixin;

import com.daqem.arc.api.IArcAbstractArrow;
import com.daqem.arc.event.triggers.ItemEvents;
import com.daqem.arc.event.triggers.PlayerEvents;
import com.daqem.arc.api.player.ArcServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Projectile.class)
public abstract class MixinProjectile extends Entity {

    public MixinProjectile(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(at = @At("HEAD"), method = "shootFromRotation(Lnet/minecraft/world/entity/Entity;FFFFF)V")
    private void shootFromRotation(Entity entity, float f, float g, float h, float i, float j, CallbackInfo ci) {
        if (entity instanceof ArcServerPlayer player) {
            if ((Projectile) (Object) this instanceof ThrowableItemProjectile throwableItemProjectile) {
                ItemEvents.onThrowItem(player, throwableItemProjectile);
            } else if ((Projectile) (Object) this instanceof IArcAbstractArrow abstractArrow) {
                PlayerEvents.onShootProjectile(player, abstractArrow);
            }
        }
    }
}
