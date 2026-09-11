package com.mcserver.serverutilities.mixin;

import net.minecraft.world.item.equipment.ArmorMaterials;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(ArmorMaterials.class)
public abstract class NetheriteKnockbackMixin {
    /**
     * 바닐라 네더라이트 방어구의 부위당 넉백 저항을 낮춘다.
     *
     * <p>넉백 저항은 부위마다 더해지고 합계 상한이 1.0이다. 강화 네더라이트 4종이 그 위에 쌓이므로
     * 기본값 0.1을 그대로 두면 상위 등급의 합계가 상한을 넘어 등급 간 차이가 사라진다.
     *
     * <p>네더라이트 생성 구간으로 한정해야 같은 생성자를 쓰는 다른 재질이 함께 바뀌지 않는다.
     * 대상이 없거나 둘 이상이면 Mixin 적용을 실패시켜 버전 변경으로 인한 오적용을 드러낸다.
     */
    @ModifyArg(
            method = "<clinit>",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/item/equipment/ArmorMaterial;<init>"
                            + "(ILjava/util/Map;ILnet/minecraft/core/Holder;FF"
                            + "Lnet/minecraft/tags/TagKey;Lnet/minecraft/resources/ResourceKey;)V"),
            slice = @Slice(
                    from = @At(value = "FIELD",
                            target = "Lnet/minecraft/sounds/SoundEvents;"
                                    + "ARMOR_EQUIP_NETHERITE:Lnet/minecraft/core/Holder;"),
                    to = @At(value = "FIELD",
                            target = "Lnet/minecraft/world/item/equipment/ArmorMaterials;"
                                    + "NETHERITE:Lnet/minecraft/world/item/equipment/ArmorMaterial;")),
            index = 5,
            require = 1,
            allow = 1)
    private static float serverutilities$lowerNetheriteKnockback(float originalResistance) {
        return 0.05F;
    }
}
