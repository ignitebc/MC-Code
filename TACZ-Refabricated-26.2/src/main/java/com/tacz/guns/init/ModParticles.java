package com.tacz.guns.init;

import com.mojang.serialization.MapCodec;
import com.tacz.guns.GunMod;
import com.tacz.guns.particles.BulletHoleOption;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public class ModParticles {
    public static void init() {

    }

    public static final ParticleType<BulletHoleOption> BULLET_HOLE = register("bullet_hole", new ModParticleType<>(false, BulletHoleOption.CODEC, BulletHoleOption.STREAM_CODEC));
    /** 저격 몬스터가 조준하는 동안 머리 앞에 뜨는 스코프 반짝임. 멀리서도 경고가 보여야 해 거리 제한을 풀어 둔다. */
    public static final SimpleParticleType SCOPE_GLINT = Registry.register(BuiltInRegistries.PARTICLE_TYPE,
            Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "scope_glint"), FabricParticleTypes.simple(true));

    private static <T extends ParticleOptions> ParticleType<T> register(String name, ParticleType<T> type) {
        return Registry.register(BuiltInRegistries.PARTICLE_TYPE, Identifier.fromNamespaceAndPath(GunMod.MOD_ID, name), type);
    }

    private static class ModParticleType<T extends ParticleOptions> extends ParticleType<T> {
        private final MapCodec<T> codec;
        private final StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec;

        public ModParticleType(boolean overrideLimiter, MapCodec<T> codec, StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
            super(overrideLimiter);
            this.codec = codec;
            this.streamCodec = streamCodec;
        }

        @Override
        public MapCodec<T> codec() {
            return this.codec;
        }

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() {
            return this.streamCodec;
        }
    }
}
