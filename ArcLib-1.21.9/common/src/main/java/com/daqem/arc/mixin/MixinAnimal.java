package com.daqem.arc.mixin;

import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.event.triggers.EntityEvents;
import com.daqem.arc.api.player.ArcServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.animal.Animal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Animal.class)
public abstract class MixinAnimal {

    @Unique
    private Animal arc$getAnimal() {
        return (Animal) (Object) this;
    }

    @Inject(at = @At("TAIL"), method = "spawnChildFromBreeding(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/animal/Animal;)V", cancellable = true)
    private void onSpawnChildFromBreeding(ServerLevel serverLevel, Animal animal, CallbackInfo ci) {
        ServerPlayer serverPlayer = arc$getAnimal().getLoveCause();
        if (serverPlayer instanceof ArcServerPlayer arcServerPlayer) {
            ActionResult actionResult = EntityEvents.onBreedAnimal(arcServerPlayer, arc$getAnimal());
            if (actionResult.shouldCancelAction()) {
                ci.cancel();
            }
        }
    }
}
