package com.tacz.guns.mixin.common;

import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.builder.GunItemBuilder;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.util.GunGrades;
import com.tacz.guns.util.RegisteredGuns;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;

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
        ItemStack stack = tacz$createGun(guns.get(choice - meleeWeapons.size()));
        // 로딩 중 사라진 총기는 기본 무기로 대체한다.
        if (!stack.isEmpty()) cir.setReturnValue(stack);
    }

    /** 네더 피글린 계열의 총기 추첨. 등록된 총기의 각 ID를 동일한 확률로 고른다. */
    @Inject(method = "createNetherGun", at = @At("HEAD"), cancellable = true, remap = false)
    private static void tacz$createNetherGun(Mob mob, CallbackInfoReturnable<ItemStack> cir) {
        var guns = RegisteredGuns.sortedById();
        if (guns.isEmpty()) return;
        ItemStack stack = tacz$createGun(guns.get(mob.getRandom().nextInt(guns.size())));
        // 로딩 중 사라진 총기는 지급하지 않아 원래 무기를 그대로 쓴다.
        if (!stack.isEmpty()) cir.setReturnValue(stack);
    }

    /** 몬스터 레벨 계산에서 총기는 근접 무기 고정 점수 대신 총기 등급 점수를 쓴다. */
    @Inject(method = "weaponScore", at = @At("HEAD"), cancellable = true, remap = false)
    private static void tacz$scoreGun(ItemStack weapon, CallbackInfoReturnable<Integer> cir) {
        IGun iGun = IGun.getIGunOrNull(weapon);
        if (iGun == null) return;
        cir.setReturnValue(GunGrades.score(iGun.getGunId(weapon)));
    }

    /** 탄창이 가득 찬 총기. 새 빌더의 빈 파츠 목록을 사용한다. */
    @Unique
    private static ItemStack tacz$createGun(Map.Entry<Identifier, CommonGunIndex> gun) {
        var data = gun.getValue().getGunData();
        return GunItemBuilder.create().setId(gun.getKey())
                .setAmmoCount(data.getAmmoAmount()).setAmmoInBarrel(true)
                .setFireMode(data.getFireModeSet().getFirst()).build();
    }
}
