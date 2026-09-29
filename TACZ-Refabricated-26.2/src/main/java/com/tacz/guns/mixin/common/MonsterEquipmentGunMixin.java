package com.tacz.guns.mixin.common;

import com.tacz.guns.api.item.builder.GunItemBuilder;
import com.tacz.guns.util.RegisteredGuns;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

/** Server Utilities 없이도 TACZ를 로드할 수 있도록 장비 추첨만 선택적으로 연결한다. */
@Pseudo
@Mixin(targets = "com.mcserver.serverutilities.monster.MonsterEquipmentRules", remap = false)
public abstract class MonsterEquipmentGunMixin {
    @Inject(method = "createWeapon", at = @At("HEAD"), cancellable = true, remap = false)
    private static void tacz$includeGuns(Mob mob, List<Item> meleeWeapons, CallbackInfoReturnable<ItemStack> cir) {
        var guns = RegisteredGuns.sortedById();
        if (guns.isEmpty()) return;
        // 근접 무기와 등록된 총기의 각 ID를 동일한 확률로 추첨한다.
        int choice = mob.getRandom().nextInt(meleeWeapons.size() + guns.size());
        if (choice < meleeWeapons.size()) {
            cir.setReturnValue(new ItemStack(meleeWeapons.get(choice)));
            return;
        }
        var gun = guns.get(choice - meleeWeapons.size());
        var data = gun.getValue().getGunData();
        ItemStack stack = GunItemBuilder.create().setId(gun.getKey())
                .setAmmoCount(data.getAmmoAmount()).setAmmoInBarrel(true)
                .setFireMode(data.getFireModeSet().getFirst()).build();
        // 새 빌더의 빈 파츠 목록을 사용한다. 로딩 중 사라진 총기는 기본 무기로 대체한다.
        if (!stack.isEmpty()) cir.setReturnValue(stack);
    }
}
