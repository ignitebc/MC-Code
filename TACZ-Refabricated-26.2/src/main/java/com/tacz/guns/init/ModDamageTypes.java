package com.tacz.guns.init;

import com.tacz.guns.GunMod;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.resource.index.CommonGunIndex;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;

public class ModDamageTypes {
    public static final ResourceKey<DamageType> BULLET = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "bullet"));
    public static final ResourceKey<DamageType> BULLET_IGNORE_ARMOR = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "bullet_ignore_armor"));
    public static final ResourceKey<DamageType> BULLET_VOID = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "bullet_void"));
    public static final ResourceKey<DamageType> BULLET_VOID_IGNORE_ARMOR = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "bullet_void_ignore_armor"));

    public static final TagKey<DamageType> BULLETS_TAG = TagKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "bullets"));

    public static class Sources {
        private static Holder.Reference<DamageType> getHolder(RegistryAccess access, ResourceKey<DamageType> damageTypeKey) {
            return access.lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(damageTypeKey);
        }

        public static DamageSource bullet(RegistryAccess access, Entity bullet, Entity shooter, boolean ignoreArmor, @Nullable Identifier gunId) {
            return new BulletDamageSource(getHolder(access, ignoreArmor ? BULLET_IGNORE_ARMOR : BULLET), bullet, shooter, gunId);
        }

        public static DamageSource bulletVoid(RegistryAccess access, Entity bullet, Entity shooter, boolean ignoreArmor, @Nullable Identifier gunId) {
            return new BulletDamageSource(getHolder(access, ignoreArmor ? BULLET_VOID_IGNORE_ARMOR : BULLET_VOID), bullet, shooter, gunId);
        }
    }

    /**
     * 어떤 총에 맞았는지 기억하는 피해원. 사망 문구에 총기 종류를 넣는 데 쓴다.
     * 예: "Steve이(가) 스켈레톤 돌격소총에 사살되었습니다"
     */
    public static class BulletDamageSource extends DamageSource {
        private static final String DEATH_KEY = "death.attack.tacz.bullet.gun";
        private static final String GUN_TYPE_KEY_PREFIX = "death.attack.tacz.gun_type.";

        @Nullable
        private final Identifier gunId;

        public BulletDamageSource(Holder<DamageType> type, @Nullable Entity bullet, @Nullable Entity shooter, @Nullable Identifier gunId) {
            super(type, bullet, shooter);
            this.gunId = gunId;
        }

        @Nullable
        public Identifier getGunId() {
            return gunId;
        }

        @Override
        public Component getLocalizedDeathMessage(LivingEntity victim) {
            Entity shooter = this.getEntity();
            String gunType = gunId == null ? null : TimelessAPI.getCommonGunIndex(gunId).map(CommonGunIndex::getType).orElse(null);
            // 사수나 총기 종류를 알 수 없으면 기존 문구로 돌아간다
            if (shooter == null || gunType == null) {
                return super.getLocalizedDeathMessage(victim);
            }
            // 팩이 새 종류를 추가해 번역 키가 없으면 종류 ID를 그대로 보여준다
            Component gunTypeName = Component.translatableWithFallback(GUN_TYPE_KEY_PREFIX + gunType, gunType);
            return Component.translatable(DEATH_KEY, victim.getDisplayName(), shooter.getDisplayName(), gunTypeName);
        }
    }
}
