package com.tacz.guns.event;

import com.tacz.guns.init.ModDamageTypes;
import net.fabricmc.fabric.api.item.v1.EnchantmentSource;
import net.minecraft.advancements.predicates.DamageSourcePredicate;
import net.minecraft.advancements.predicates.TagPredicate;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.LevelBasedValue;
import net.minecraft.world.item.enchantment.effects.AddValue;
import net.minecraft.world.level.storage.loot.predicates.DamageSourceCondition;

/**
 * 발사체로부터의 보호가 화살처럼 총알 피해도 줄이게 한다.
 * <p>
 * 총알 피해를 {@code minecraft:is_projectile} 태그에 넣으면 방패 막기, 엔더맨·셜커 피격, 발전과제 판정까지
 * 바뀌므로, 인챈트에 총알 조건의 보호 효과만 하나 더 붙인다. 수치와 제외 조건은 바닐라 화살 조건과 같다.
 */
public class BulletProjectileProtection {
    /** 바닐라 발사체로부터의 보호와 같은 값. 레벨마다 보호 수치 2를 더한다. */
    private static final float PROTECTION_PER_LEVEL = 2.0F;

    public static void onModifyEnchantment(ResourceKey<Enchantment> key, Enchantment.Builder builder, EnchantmentSource source,
                                           RegistryOps.RegistryInfoLookup registries) {
        // 데이터팩이 인챈트를 직접 다시 정의했으면 그 정의를 그대로 따른다.
        boolean isVanillaProjectileProtection = source.isBuiltin() && key.equals(Enchantments.PROJECTILE_PROTECTION);
        if (!isVanillaProjectileProtection) {
            return;
        }
        // 무적 시간을 무시하는 피해는 바닐라 화살 조건과 같이 제외한다.
        DamageSourcePredicate.Builder bulletDamage = DamageSourcePredicate.Builder.damageType()
                .tag(TagPredicate.is(ModDamageTypes.BULLETS_TAG))
                .tag(TagPredicate.isNot(DamageTypeTags.BYPASSES_INVULNERABILITY));
        builder.withEffect(EnchantmentEffectComponents.DAMAGE_PROTECTION,
                new AddValue(LevelBasedValue.perLevel(PROTECTION_PER_LEVEL)),
                DamageSourceCondition.hasDamageSource(bulletDamage));
    }
}
