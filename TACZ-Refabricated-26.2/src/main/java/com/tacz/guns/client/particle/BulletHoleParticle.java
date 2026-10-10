package com.tacz.guns.client.particle;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.config.client.RenderConfig;
import com.tacz.guns.init.ModBlocks;
import com.tacz.guns.particles.BulletHoleOption;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;

/**
 * 원작자: MrCrayfish에서 갈라져 나와 Timeless 개발진이 이어 감
 * 26.2: TextureSheetParticle → SingleQuadParticle, render → extract
 */
public class BulletHoleParticle extends SingleQuadParticle {
    private final Direction direction;
    private final BlockPos pos;
    private int uOffset;
    private int vOffset;
    private float textureDensity;

    public BulletHoleParticle(ClientLevel world, double x, double y, double z, Direction direction, BlockPos pos, String ammoId, String gunId, String gunDisplayId) {
        super(world, x, y, z, getSpriteForPos(pos));
        this.direction = direction;
        this.pos = pos;
        this.lifetime = this.getLifetimeFromConfig(world);
        this.hasPhysics = false;
        this.gravity = 0.0F;
        this.quadSize = 0.05F;

        BlockState state = world.getBlockState(pos);
        if (state.is(ModBlocks.TARGET) || shouldRemove()) {
            this.remove();
        }
        TimelessAPI.getGunDisplay(Identifier.parse(gunDisplayId), Identifier.parse(gunId)).ifPresent(gunIndex -> {
            float[] gunTracerColor = gunIndex.getTracerColor();
            if (gunTracerColor != null) {
                this.rCol = gunTracerColor[0];
                this.gCol = gunTracerColor[1];
                this.bCol = gunTracerColor[2];
            } else {
                TimelessAPI.getClientAmmoIndex(Identifier.parse(ammoId)).ifPresent(ammoIndex -> {
                    float[] ammoTracerColor = ammoIndex.getTracerColor();
                    this.rCol = ammoTracerColor[0];
                    this.gCol = ammoTracerColor[1];
                    this.bCol = ammoTracerColor[2];
                });
            }
        });
        this.alpha = 0.9F;
    }

    private static TextureAtlasSprite getSpriteForPos(BlockPos pos) {
        Minecraft minecraft = Minecraft.getInstance();
        Level world = minecraft.level;
        if (world != null) {
            BlockState state = world.getBlockState(pos);
            return minecraft.getModelManager().getBlockStateModelSet().getParticleMaterial(state).sprite();
        }
        // 대체: 보통은 일어나지 않는다
        return minecraft.getModelManager().getBlockStateModelSet().missingModel().particleMaterial().sprite();
    }

    private int getLifetimeFromConfig(ClientLevel world) {
        int configLife = RenderConfig.BULLET_HOLE_PARTICLE_LIFE.get();
        if (configLife <= 1) {
            return configLife;
        }
        return configLife + world.getRandom().nextInt(configLife / 2);
    }

    @Override
    protected void setSprite(TextureAtlasSprite sprite) {
        super.setSprite(sprite);
        this.uOffset = this.random.nextInt(16);
        this.vOffset = this.random.nextInt(16);
        // 텍스처는 모두 정사각형이어야 한다
        this.textureDensity = (sprite.getU1() - sprite.getU0()) / 16.0F;
    }

    @Override
    protected float getU0() {
        return this.sprite.getU0() + this.uOffset * this.textureDensity;
    }

    @Override
    protected float getV0() {
        return this.sprite.getV0() + this.vOffset * this.textureDensity;
    }

    @Override
    protected float getU1() {
        return this.getU0() + this.textureDensity;
    }

    @Override
    protected float getV1() {
        return this.getV0() + this.textureDensity;
    }

    @Override
    public void tick() {
        super.tick();
        if (shouldRemove()) {
            this.remove();
        }
    }

    /**
     * <b>8차 수정: 블록을 부술 때 하늘에 점점 커지는 이상한 사각 조각이 나타나던 문제.</b>
     *
     * <p>원래 코드는 다음과 같았다:</p>
     * <pre>
     * this.extractRotatedQuad(state, quaternion, red, green, blue, alphaFade);
     * </pre>
     *
     * <p>"회전 + 색"을 넘기는 것처럼 보이지만, 26.2의 {@code SingleQuadParticle}에는 색을 받는
     * 오버로드가 <b>없다</b>(디컴파일 확인, 다음 것들뿐이다):</p>
     * <pre>
     * extractRotatedQuad(QuadParticleRenderState, Camera, Quaternionf, float partialTick)
     * extractRotatedQuad(QuadParticleRenderState, Quaternionf, float x, float y, float z, float partialTick)
     * </pre>
     *
     * <p>그래서 이 호출은 두 번째 오버로드에 조용히 묶여 — <b>r/g/b를 x/y/z 좌표로</b>,
     * alpha를 partialTick으로 썼다. 색은 0~1 실수라 사각형이
     * "카메라 기준 (r, g, b)"라는 <b>고정 오프셋</b>(+x 동쪽, +z 남쪽, +y 위쪽)에 그려졌고,
     * 실제 탄흔 위치와 전혀 관계가 없었다 — 이것이 보고된 "서-남서 방향 화면 위쪽에 고정되어 나타남"이다.</p>
     *
     * <p>그리고 색은 총기/탄약의 <b>tracerColor</b>에서 오므로 <b>총마다 다른 위치에 나타났고</b>,
     * {@code colorPercent}가 0으로 줄면서 좌표도 카메라 원점에 가까워져
     * 보기에는 "점점 커지다 사라지는" 모습이었다. 수명은 약 60틱 ≈ 3초로 — 보고 내용과 하나하나 맞는다.</p>
     *
     * <p>올바른 방법: Camera를 받는 오버로드를 써서 부모 클래스가 카메라 상대 좌표를 직접 계산하게 하고,
     * 색은 {@code rCol/gCol/bCol/alpha} 필드로 넘긴다(부모 {@code extractRotatedQuad}는
     * 안에서 {@code ARGB.colorFromFloat(this.alpha, this.rCol, this.gCol, this.bCol)}로 값을 읽는다).</p>
     */
    @Override
    public void extract(QuadParticleRenderState state, Camera camera, float partialTicks) {
        // 0~30틱 동안 밝기 15에서 0으로
        int light = Math.max(15 - this.age / 2, 0);

        // 색은 점점 0 0 0, 곧 검은색으로 바뀐다
        float colorPercent = light / 15.0f;

        // 투명도는 점점 0, 곧 투명해진다
        double threshold = RenderConfig.BULLET_HOLE_PARTICLE_FADE_THRESHOLD.get() * this.lifetime;
        float fade = 1.0f - (float) (Math.max(this.age - threshold, 0) / (this.lifetime - threshold));

        // 기본 색을 백업해 두고, 렌더링할 때 부모 필드(부모가 이 필드에서 색을 읽음)에 잠시 쓴 뒤 렌더링 후 되돌린다.
        // 줄어든 색이 기본 색에 쌓이지 않게 하기 위해서다.
        float baseR = this.rCol;
        float baseG = this.gCol;
        float baseB = this.bCol;
        float baseA = this.alpha;
        this.rCol = baseR * colorPercent;
        this.gCol = baseG * colorPercent;
        this.bCol = baseB * colorPercent;
        this.alpha = baseA * fade;
        try {
            // 방향 사원수로 사각형을 돌리고, 위치는 Camera를 받는 오버로드가 계산하게 한다.
            Quaternionf quaternion = this.direction.getRotation();
            this.extractRotatedQuad(state, camera, quaternion, partialTicks);
        } finally {
            this.rCol = baseR;
            this.gCol = baseG;
            this.bCol = baseB;
            this.alpha = baseA;
        }
    }

    @Override
    public ParticleRenderType getGroup() {
        return ParticleRenderType.SINGLE_QUADS;
    }

    @Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT_TERRAIN;
    }

    private boolean shouldRemove() {
        final BlockState blockState = this.level.getBlockState(this.pos);
        if (blockState.isAir()) {
            return true;
        } else {
            // 블록에 제대로 붙어 있지 않으면 탄흔을 더 그리지 않는다
            VoxelShape shape = blockState.getCollisionShape(this.level, this.pos);
            if (shape.isEmpty()) {
                return true;
            }
            AABB baseBlockBoundingBox = shape.bounds();
            AABB blockBoundingBox = baseBlockBoundingBox.move(this.pos);
            boolean intersects = blockBoundingBox.intersects(
                    this.x - 0.1, this.y - 0.1, this.z - 0.1,
                    this.x + 0.1, this.y + 0.1, this.z + 0.1);
            return !intersects;
        }
    }

    @Environment(EnvType.CLIENT)
    public static class Provider implements ParticleProvider<BulletHoleOption> {
        public Provider() {
        }

        @Override
        public BulletHoleParticle createParticle(@NotNull BulletHoleOption option, @NotNull ClientLevel world, double x, double y, double z, double pXSpeed, double pYSpeed, double pZSpeed, RandomSource random) {
            return new BulletHoleParticle(world, x, y, z, option.getDirection(), option.getPos(), option.getAmmoId(), option.getGunId(), option.getGunDisplayId());
        }
    }
}
