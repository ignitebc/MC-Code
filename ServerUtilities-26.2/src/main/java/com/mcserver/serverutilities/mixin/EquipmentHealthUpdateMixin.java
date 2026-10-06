package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.tier.EquipmentHealthReserve;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
abstract class EquipmentHealthUpdateMixin {
    @Unique private float serverutilities$healthBeforeEquipment;
    @Unique private float serverutilities$maxHealthBeforeEquipment;

    // 장비 수정자 교체는 이 메서드 안에서 끝나고, 최대 체력에 맞춘 체력 자르기는 같은 틱 뒤에서 일어난다.
    // 그래서 앞뒤 최대 체력의 차이가 곧 장비 때문에 바뀐 양이다.
    @Inject(method = "detectEquipmentUpdates", at = @At("HEAD"))
    private void serverutilities$rememberHealth(CallbackInfo ci) {
        if (!((Object) this instanceof ServerPlayer player)) return;

        serverutilities$healthBeforeEquipment = player.getHealth();
        serverutilities$maxHealthBeforeEquipment = player.getMaxHealth();
    }

    @Inject(method = "detectEquipmentUpdates", at = @At("RETURN"))
    private void serverutilities$keepEquipmentHealth(CallbackInfo ci) {
        if (!((Object) this instanceof ServerPlayer player)) return;

        EquipmentHealthReserve.afterEquipmentUpdate(player,
                serverutilities$healthBeforeEquipment, serverutilities$maxHealthBeforeEquipment);
    }
}
