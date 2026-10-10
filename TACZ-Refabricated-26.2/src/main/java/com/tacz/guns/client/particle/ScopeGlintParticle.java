package com.tacz.guns.client.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteSet;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

/**
 * 저격 몬스터가 조준하는 동안 머리 앞에서 반짝이는 스코프 반사광.
 * <p>
 * 크기를 월드 단위로 고정하면 멀리 있는 저격수의 반짝임이 한 점보다 작아져 경고 역할을 못 한다.
 * 카메라와의 거리에 비례해 키워 화면에서는 거리와 관계없이 비슷한 크기로 보이게 하고, 어두운 곳에서도 보이도록 항상 가장 밝게 그린다.
 */
@Environment(EnvType.CLIENT)
public class ScopeGlintParticle extends SingleQuadParticle {
    /** 한 번 반짝이는 시간(틱). 서버가 이보다 조금 짧은 간격으로 계속 보내 조준하는 동안 끊기지 않는다. */
    private static final int LIFETIME_TICKS = 6;
    /** 가까이서도 너무 작아지지 않게 하는 최소 반지름(칸) */
    private static final float MIN_HALF_SIZE = 0.12F;
    /** 거리 1칸당 늘리는 반지름. 화면에서 지름 약 0.7도로 보인다. */
    private static final float HALF_SIZE_PER_BLOCK = 0.006F;
    /** 반짝일 때 가장 작아지는 크기 비율 */
    private static final float MIN_PULSE_SCALE = 0.6F;
    /** 블록·하늘 밝기 모두 15. 몬스터가 그늘에 있어도 반사광은 밝게 보인다. */
    private static final int FULL_BRIGHT = 0xF000F0;

    protected ScopeGlintParticle(ClientLevel level, double x, double y, double z, TextureAtlasSprite sprite) {
        super(level, x, y, z, sprite);
        this.lifetime = LIFETIME_TICKS;
        this.hasPhysics = false;
        this.gravity = 0.0F;
        // 렌즈 반사광처럼 살짝 따뜻한 흰색
        this.setColor(1.0F, 0.97F, 0.85F);
    }

    @Override
    public void extract(QuadParticleRenderState state, Camera camera, float partialTicks) {
        double distance = camera.position().distanceTo(new Vec3(this.x, this.y, this.z));
        float pulse = Mth.sin((this.age + partialTicks) / this.lifetime * Mth.PI);
        float halfSize = Math.max(MIN_HALF_SIZE, (float) distance * HALF_SIZE_PER_BLOCK);
        this.quadSize = halfSize * (MIN_PULSE_SCALE + (1.0F - MIN_PULSE_SCALE) * pulse);
        this.alpha = Math.max(0.0F, pulse);
        super.extract(state, camera, partialTicks);
    }

    @Override
    protected int getLightCoords(float partialTick) {
        return FULL_BRIGHT;
    }

    @Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

    @Environment(EnvType.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final FabricSpriteSet sprites;

        public Provider(FabricSpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public ScopeGlintParticle createParticle(@NotNull SimpleParticleType type, @NotNull ClientLevel level,
                                                 double x, double y, double z,
                                                 double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            return new ScopeGlintParticle(level, x, y, z, this.sprites.first());
        }
    }
}
