package com.tacz.guns.entity;

import cn.sh1rocu.tacz.api.LogicalSide;
import cn.sh1rocu.tacz.api.extension.IEntityAdditionalSpawnData;
import cn.sh1rocu.tacz.api.extension.IEntityPersistentData;
import com.google.common.collect.Lists;
import com.tacz.guns.GunMod;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.GunProperties;
import com.tacz.guns.api.GunProperty;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ITargetEntity;
import com.tacz.guns.api.entity.KnockBackModifier;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.api.event.common.EntityKillByGunEvent;
import com.tacz.guns.api.event.server.AmmoHitBlockEvent;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.client.particle.AmmoParticleSpawner;
import com.tacz.guns.config.common.AmmoConfig;
import com.tacz.guns.config.sync.SyncConfig;
import com.tacz.guns.entity.ai.BulletSuppression;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import com.tacz.guns.init.ModDamageTypes;
import com.tacz.guns.network.NetworkHandler;
import com.tacz.guns.network.message.event.ServerMessageGunHurt;
import com.tacz.guns.network.message.event.ServerMessageGunKill;
import com.tacz.guns.particles.BulletHoleOption;
import com.tacz.guns.resource.modifier.AttachmentCacheProperty;
import com.tacz.guns.resource.modifier.custom.DamageModifier;
import com.tacz.guns.resource.modifier.custom.ExplosionModifier;
import com.tacz.guns.resource.modifier.custom.IgniteModifier;
import com.tacz.guns.resource.pojo.data.gun.BulletData;
import com.tacz.guns.resource.pojo.data.gun.ExplosionData;
import com.tacz.guns.resource.pojo.data.gun.ExtraDamage.DistanceDamagePair;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.resource.pojo.data.gun.Ignite;
import com.tacz.guns.util.EntityUtil;
import com.tacz.guns.util.ExplodeUtil;
import com.tacz.guns.util.GunLevelManager;
import com.tacz.guns.util.GunShotContext;
import com.tacz.guns.util.TacHitResult;
import com.tacz.guns.util.block.BlockRayTrace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.boss.enderdragon.EnderDragonPart;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2d;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.*;

import static com.tacz.guns.api.GunProperties.RuntimeOnly.*;
import static com.tacz.guns.api.event.common.GunDamageSourcePart.ARMOR_PIERCING;
import static com.tacz.guns.api.event.common.GunDamageSourcePart.NON_ARMOR_PIERCING;

/**
 * 운동 에너지 무기가 쏜 탄환 엔티티.
 */
public class EntityKineticBullet extends Projectile implements IEntityAdditionalSpawnData {
    public static final EntityType<EntityKineticBullet> TYPE = EntityType.Builder
            .<EntityKineticBullet>of(EntityKineticBullet::new, MobCategory.MISC)
            .noSummon().noSave().fireImmune()
            .sized(0.0625F, 0.0625F)
            // 밤에 먼 곳의 예광탄도 보이도록 8청크(128칸)까지 전송한다. RPG-7 로켓이 날아가는 최대 거리(7청크)도 들어온다.
            .clientTrackingRange(8).updateInterval(5)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath("tacz", "kinetic_bullet")));
    public static final TagKey<EntityType<?>> USE_MAGIC_DAMAGE_ON = TagKey.create(Registries.ENTITY_TYPE, Identifier.parse("tacz:use_magic_damage_on"));
    public static final TagKey<EntityType<?>> USE_VOID_DAMAGE_ON = TagKey.create(Registries.ENTITY_TYPE, Identifier.parse("tacz:use_void_damage_on"));
    public static final TagKey<EntityType<?>> PRETEND_MELEE_DAMAGE_ON = TagKey.create(Registries.ENTITY_TYPE, Identifier.parse("tacz:pretend_melee_damage_on"));

    /**
     * 다른 mod가 persistent data(영구 데이터)로 예광탄의 색과 굵기를 조절할 수 있게 한다.<p>
     * 영구 데이터를 쓰면 나중에 이 클래스가 크게 바뀌어도 이 기능을 쓰는 다른 mod가 충돌하지 않는다.<p>
     * 아래 두 필드는 persistent data의 key다.<p>
     * 이 필드 값의 타입은 int[4]다.<p>
     * <p>
     * 사용 예:
     * <pre>{@code
     *     bullet.getPersistentData().putIntArray(TRACER_COLOR_OVERRIDER_KEY, new int[]{255, 255, 255, 255});
     * }</pre>
     */
    public static final String TRACER_COLOR_OVERRIDER_KEY = GunMod.MOD_ID + ":tracer_override";

    /**
     * 이 필드 값의 타입은 float다.
     * 1은 기본 크기, 0은 굵기 0배(표시하지 않음)를 뜻한다
     */
    public static final String TRACER_SIZE_OVERRIDER_KEY = GunMod.MOD_ID + ":tracer_size";

    private static final ExplosionData DEFAULT_EXPLOSION_DATA = new ExplosionData(false, 0, 0, false, 30, false);

    private Identifier ammoId = DefaultAssets.EMPTY_AMMO_ID;
    private int life = 200;
    @Deprecated
    private float speed = 1;
    private float gravity = 0;
    private float friction = 0.01F;
    private LinkedList<DistanceDamagePair> damageAmount = Lists.newLinkedList();
    private float distanceAmount = 0;
    private float knockback = 0;
    private boolean explosion = false;
    private boolean igniteEntity = false;
    private boolean igniteBlock = false;
    private int igniteEntityTime = 2;
    private float explosionDamage = 3;
    private float explosionRadius = 3;
    private int explosionDelayCount = Integer.MAX_VALUE;
    private boolean explosionKnockback = false;
    private boolean explosionDestroyBlock = false;
    private float damageModifier = 1;
    // 관통 수
    private int pierce = 1;
    // 초기 위치
    private Vec3 startPos;
    // 예광탄
    private boolean isTracerAmmo;
    // 아래 몇 개는 클라이언트에서만 쓰는 예광탄 데이터다
    private float cameraXRot;
    private float cameraYRot;
    // 1인칭 예광탄을 총구에서 출발한 것처럼 옮겨 그리는 양. 월드 축 기준이다.
    private Vector3f firstPersonRenderOffset;
    // 1인칭 예광탄을 처음 그릴 때의 카메라 위치와, 옮긴 양이 0이 되어 실제 탄도와 합쳐지는 탄착점
    private Vec3 firstPersonOrigin;
    private Vec3 firstPersonImpact;
    // 발사한 총기 ID
    private Identifier gunId = DefaultAssets.EMPTY_GUN_ID;
    // 총기 display ID
    private Identifier gunDisplayId = DefaultAssets.DEFAULT_GUN_DISPLAY_ID;
    private float armorIgnore;
    private float headShot;
    private float shotDamageMultiplier = 1f;
    private float gunLevelDamageMultiplier = 1f;
    // 총기 레벨의 폭발 피해 배율. 폭발은 서버에서만 계산하므로 클라이언트에는 보내지 않는다.
    private float gunLevelExplosionMultiplier = 1f;
    private @Nullable GunShotContext shotContext;
    // 소음기를 단 총에서 쏜 탄. 맞히거나 죽였을 때 주변 몬스터에게 경보가 퍼지는 범위를 줄인다.
    private boolean silenced;

    public EntityKineticBullet(EntityType<? extends Projectile> type, Level worldIn) {
        super(type, worldIn);
    }

    public EntityKineticBullet(EntityType<? extends Projectile> type, double x, double y, double z, Level worldIn) {
        this(type, worldIn);
        this.setPos(x, y, z);
    }

    public EntityKineticBullet(Level worldIn, LivingEntity throwerIn, ItemStack gunItem, Identifier ammoId, Identifier gunId,
                               Identifier gunDisplayId, boolean isTracerAmmo, GunData gunData, BulletData bulletData) {
        this(TYPE, worldIn, throwerIn, gunItem, ammoId, gunId, gunDisplayId, isTracerAmmo, gunData, bulletData);
    }

    public EntityKineticBullet(Level worldIn, LivingEntity throwerIn, ItemStack gunItem, Identifier ammoId, Identifier gunId, boolean isTracerAmmo, GunData gunData, BulletData bulletData) {
        this(TYPE, worldIn, throwerIn, gunItem, ammoId, gunId, DefaultAssets.DEFAULT_GUN_DISPLAY_ID, isTracerAmmo, gunData, bulletData);
    }

    protected EntityKineticBullet(EntityType<? extends Projectile> type, Level worldIn, LivingEntity throwerIn, ItemStack gunItem,
                                  Identifier ammoId, Identifier gunId, Identifier gunDisplayId,
                                  boolean isTracerAmmo, GunData gunData, BulletData bulletData) {
        this(type, throwerIn.getX(), throwerIn.getEyeY() - (double) 0.1F, throwerIn.getZ(), worldIn);
        this.setOwner(throwerIn);
        // modifyProperty가 생성자 안에서 실행될 수 있도록 gunId를 먼저 대입한다
        this.gunId = gunId;
        this.gunLevelDamageMultiplier = (float) GunLevelManager.getDamageMultiplier(gunItem);
        this.gunLevelExplosionMultiplier = (float) GunLevelManager.getExplosionDamageMultiplier(gunItem);
        AttachmentCacheProperty cacheProperty = Objects.requireNonNull(IGunOperator.fromLivingEntity(throwerIn).getCacheProperty());
        float armorIgnore = modifyProperty(GunProperties.ARMOR_IGNORE, Float.class, cacheProperty.getCache(GunProperties.ARMOR_IGNORE));
        float headshot = modifyProperty(GunProperties.HEADSHOT_MULTIPLIER, Float.class, cacheProperty.getCache(GunProperties.HEADSHOT_MULTIPLIER));
        float knockback = modifyProperty(GunProperties.KNOCKBACK, Float.class, cacheProperty.getCache(GunProperties.KNOCKBACK));
        this.armorIgnore = Mth.clamp(armorIgnore, 0f, 1f);
        this.headShot = Math.max(headshot, 0f);
        this.knockback = Math.max(knockback, 0f);
        this.ammoId = ammoId;
        float lifeSecond = modifyProperty(BULLET_LIFE, Float.class, bulletData.getLifeSecond());
        this.life = Mth.clamp((int) (lifeSecond * 20), 1, Integer.MAX_VALUE);
        // speed 필드는 쓰이지 않는다. 실제로 적용되는 속도는 shootOnce에서 doBulletSpread에 넘기는 속도다
        this.gravity = Mth.clamp(modifyProperty(BULLET_GRAVITY, Float.class, bulletData.getGravity()), 0f, Float.MAX_VALUE);
        this.friction = Mth.clamp(modifyProperty(BULLET_FRICTION, Float.class, bulletData.getFriction()), 0f, Float.MAX_VALUE);
        // 점화
        Ignite ignite = cacheProperty.getCache(IgniteModifier.ID);
        this.igniteEntity = modifyProperty(IGNITE_ENTITY, Boolean.class, bulletData.getIgnite().isIgniteEntity() || ignite.isIgniteEntity());
        this.igniteEntityTime = Math.max(modifyProperty(IGNITE_ENTITY_TIME, Integer.class, bulletData.getIgniteEntityTime()), 0);
        this.igniteBlock = modifyProperty(IGNITE_BLOCK, Boolean.class, bulletData.getIgnite().isIgniteBlock() || ignite.isIgniteBlock());
        this.damageAmount = cacheProperty.getCache(DamageModifier.ID);
        this.distanceAmount = modifyProperty(GunProperties.EFFECTIVE_RANGE, Float.class, cacheProperty.getCache(GunProperties.EFFECTIVE_RANGE));
        int pierce = modifyProperty(GunProperties.PIERCE, Integer.class, cacheProperty.getCache(GunProperties.PIERCE));
        this.pierce = Mth.clamp(pierce, 1, Integer.MAX_VALUE);
        ExplosionData explosionData = Objects.requireNonNullElse(cacheProperty.getCache(ExplosionModifier.ID), DEFAULT_EXPLOSION_DATA);
        this.explosion = modifyProperty(EXPLODE_ENABLED, Boolean.class, explosionData.isExplode());
        if (this.explosion) {
            var explosionDamage = modifyProperty(EXPLOSION_DAMAGE, Float.class, explosionData.getDamage());
            var explosionRadius = modifyProperty(EXPLOSION_RADIUS, Float.class, explosionData.getRadius());
            this.explosionDamage = (float) Mth.clamp(explosionDamage * SyncConfig.DAMAGE_BASE_MULTIPLIER.get(), 0, Float.MAX_VALUE);
            this.explosionRadius = Mth.clamp(explosionRadius, 0, Float.MAX_VALUE);
            this.explosionKnockback = modifyProperty(EXPLOSION_KNOCKBACK, Boolean.class, explosionData.isKnockback());
            // 범위를 벗어나지 않도록 미리 판정한다
            int delayTickCount = (int) (modifyProperty(EXPLOSION_DELAY, Float.class, explosionData.getDelay()) * 20);
            if (delayTickCount < 0) {
                delayTickCount = Integer.MAX_VALUE;
            }
            // 설정 파일에서 폭발을 껐으면 스크립트가 바꾼 폭발 블록 파괴 여부를 무시한다
            this.explosionDestroyBlock = AmmoConfig.EXPLOSIVE_AMMO_DESTROYS_BLOCK.get() && modifyProperty(EXPLOSION_DESTROYS_BLOCK, Boolean.class, explosionData.isDestroyBlock());
            this.explosionDelayCount = Math.max(delayTickCount, 1);
        }
        // 탄환 초기 위치 초기화
        double posX = throwerIn.xOld + (throwerIn.getX() - throwerIn.xOld) / 2.0;
        double posY = throwerIn.yOld + (throwerIn.getY() - throwerIn.yOld) / 2.0 + throwerIn.getEyeHeight();
        double posZ = throwerIn.zOld + (throwerIn.getZ() - throwerIn.zOld) / 2.0;
        this.setPos(posX, posY, posZ);
        this.startPos = this.position();
        this.isTracerAmmo = isTracerAmmo;
        this.gunDisplayId = gunDisplayId;
    }

    @ApiStatus.Internal
    public void applyShotgunDamageSpread(int bulletCount) {
        // 산탄일 때는 피해마다 깎아야 한다
        if (bulletCount > 1) {
            this.damageModifier = 1f / bulletCount;
        }
    }

    @ApiStatus.Internal
    public void setShotDamageMultiplier(float multiplier) {
        this.shotDamageMultiplier = Math.max(multiplier, 0f);
    }

    @ApiStatus.Internal
    public void setShotContext(GunShotContext shotContext) {
        this.shotContext = shotContext;
        this.gunLevelDamageMultiplier = shotContext.getDamageMultiplier();
    }

    @ApiStatus.Internal
    public void setSilenced(boolean silenced) {
        this.silenced = silenced;
    }

    /** 소음기를 단 총에서 쏜 탄인지. 서버에서 발사할 때 정하며 클라이언트에는 보내지 않는다. */
    public boolean isSilenced() {
        return this.silenced;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        // TaC 탄환 서버 이벤트 호출
        this.onBulletTick();
        // 입자 효과
        if (this.level().isClientSide()) {
            AmmoParticleSpawner.addParticle(this);
        }
        // 탄환 모델의 회전과 포물선
        Vec3 movement = this.getDeltaMovement();
        double x = movement.x;
        double y = movement.y;
        double z = movement.z;
        double distance = movement.horizontalDistance();
        this.setYRot((float) Math.toDegrees(Mth.atan2(x, z)));
        this.setXRot((float) Math.toDegrees(Mth.atan2(y, distance)));
        // 탄환 초기 방향 설정
        if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        }
        // 탄환 이동 중 회전(자전 제외)
        this.setXRot(lerpRotation(this.xRotO, this.getXRot()));
        this.setYRot(lerpRotation(this.yRotO, this.getYRot()));
        // 탄환 위치 갱신
        double nextPosX = this.getX() + x;
        double nextPosY = this.getY() + y;
        double nextPosZ = this.getZ() + z;
        this.setPos(nextPosX, nextPosY, nextPosZ);
        float friction = this.friction;
        float gravity = this.gravity;
        // 탄환이 물에 들어간 뒤의 보정
        if (this.isInWater()) {
            for (int i = 0; i < 4; i++) {
                this.level().addParticle(ParticleTypes.BUBBLE, nextPosX - x * 0.25F, nextPosY - y * 0.25F, nextPosZ - z * 0.25F, x, y, z);
            }
            // 물속 저항
            friction = 0.4F;
            gravity *= 0.6F;
        }
        // 중력과 저항으로 속도 상태 갱신
        this.setDeltaMovement(this.getDeltaMovement().scale(1 - friction));
        this.setDeltaMovement(this.getDeltaMovement().add(0, -gravity, 0));
        // 탄환 수명 종료
        if (this.tickCount >= this.life - 1) {
            this.discard();
        }
    }

    // 탄환 로직 처리
    protected void onBulletTick() {
        // 서버 측 탄환 로직
        if (!this.level().isClientSide()) {
            // 지연 폭발 판정
            if (this.explosion) {
                if (this.explosionDelayCount > 0) {
                    this.explosionDelayCount--;
                } else {
                    // 같은 사격의 다른 산탄이 이미 폭발했으면 터지지 않고 사라진다.
                    tryExplode(this.position());
                    // 폭발하면 탄흔을 남기지 않고 바로 끝내며 이후 로직을 처리하지 않는다
                    this.discard();
                    return;
                }
            }
            // tick 시작 시 탄환 위치
            Vec3 startVec = this.position();
            // tick 끝 시 탄환 위치
            Vec3 endVec = startVec.add(this.getDeltaMovement());
            // 탄환 충돌 검사
            HitResult result = BlockRayTrace.rayTraceBlocks(this.level(), new ClipContext(startVec, endVec, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            BlockHitResult resultB = (BlockHitResult) result;
            if (resultB.getType() != HitResult.Type.MISS) {
                // 탄환이 블록에 맞으면 맞은 블록 위치를 탄환의 끝 위치로 설정한다
                endVec = resultB.getLocation();
            }
            // 플레이어의 탄이 몬스터 가까이를 스치면 제압한다. 블록에 막힌 탄은 막힌 지점까지만 본다.
            BulletSuppression.onBulletPath(this, startVec, endVec);

            List<EntityResult> hitEntities = null;
            // 탄환 명중 검사. 관통이 1이거나 폭발형 탄약이면 엔티티 하나만 관통 판정한다
            if (this.pierce <= 1 || this.explosion) {
                EntityResult entityResult = EntityUtil.findEntityOnPath(this, startVec, endVec);
                // 단일 명중 엔티티를 요소 하나짜리 list로 만든다
                if (entityResult != null) {
                    hitEntities = Collections.singletonList(entityResult);
                }
            } else {
                hitEntities = EntityUtil.findEntitiesOnPath(this, startVec, endVec);
            }
            // 탄환이 엔티티에 맞으면 맞은 엔티티를 읽는다
            if (hitEntities != null && !hitEntities.isEmpty()) {
                EntityResult[] hitEntityResult = hitEntities.toArray(new EntityResult[0]);
                // 맞은 엔티티를 탄환 발사 위치에서 가까운 순으로 오름차순 정렬한다
                for (int i = 0; (i < this.pierce || i < 1) && i < (hitEntityResult.length - 1); i++) {
                    int k = i;
                    for (int j = i + 1; j < hitEntityResult.length; j++) {
                        if (hitEntityResult[j].hitVec.distanceTo(startVec) < hitEntityResult[k].hitVec.distanceTo(startVec)) {
                            k = j;
                        }
                    }
                    EntityResult t = hitEntityResult[i];
                    hitEntityResult[i] = hitEntityResult[k];
                    hitEntityResult[k] = t;
                }
                for (EntityResult entityResult : hitEntityResult) {
                    result = new TacHitResult(entityResult);
                    this.onHitEntity((TacHitResult) result, startVec, endVec);
                    this.pierce--;
                    if (this.pierce < 1 || this.explosion) {
                        // 탄환이 모든 엔티티를 관통했으면 비행을 끝낸다
                        this.discard();
                        return;
                    }
                }
            }
            this.onHitBlock(resultB, startVec, endVec);
        }
    }

    public void shoot(double pitch, double yaw, float pVelocity, Vector2d vector2d) {
        Vector3d left = new Vector3d(vector2d.x, vector2d.y, 8);

        left.rotateX(pitch * Mth.DEG_TO_RAD);
        left.rotateY(-yaw * Mth.DEG_TO_RAD);

        Vec3 vec3 = new Vec3(left.x, left.y, left.z).normalize().scale(pVelocity);

        this.setDeltaMovement(vec3.x, vec3.y, vec3.z);
        double d0 = vec3.horizontalDistance();
        this.setYRot((float) (Mth.atan2(vec3.x, vec3.z) * (double) (180F / (float) Math.PI)));
        this.setXRot((float) (Mth.atan2(vec3.y, d0) * (double) (180F / (float) Math.PI)));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    public void shootFromRotation(Entity pShooter, float pX, float pY, float pZ, float pVelocity, Vector2d vector2d) {
        this.shoot(pX, pY, pVelocity, vector2d);
        Vec3 vec3 = pShooter.getDeltaMovement();
        this.setDeltaMovement(this.getDeltaMovement().add(vec3.x, pShooter.onGround() ? 0.0D : vec3.y, vec3.z));
    }

    public record MaybeMultipartEntity(
            Entity hitPart,
            Entity core
    ) {
        public static MaybeMultipartEntity of(Entity hitPart) {
            if (hitPart instanceof EnderDragonPart part) {
                return new MaybeMultipartEntity(hitPart, part.parentMob);
            }
            return new MaybeMultipartEntity(hitPart, hitPart);
        }
    }

    protected void onHitEntity(TacHitResult result, Vec3 startVec, Vec3 endVec) {
        if (result.getEntity() instanceof ITargetEntity targetEntity) {
            DamageSource source = this.damageSources().thrown(this, this.getOwner());
            targetEntity.onProjectileHit(this, result, source, this.getDamage(result.getLocation()));
            // 표적이면 바로 돌아간다
            return;
        }
        // Pre 이벤트에 필요한 정보 가져오기
        Entity entity = result.getEntity();
        @Nullable Entity owner = this.getOwner();
        // 공격자
        LivingEntity attacker = owner instanceof LivingEntity ? (LivingEntity) owner : null;
        var sources = createDamageSources(MaybeMultipartEntity.of(entity));
        boolean headshot = result.isHeadshot();
        float damage = this.getDamage(result.getLocation());
        float headShotMultiplier = Math.max(this.headShot, 0);
        // Pre 이벤트 발행
        var preEvent = new EntityHurtByGunEvent.Pre(this, entity, attacker, this.gunId, this.gunDisplayId, damage, sources, headshot, headShotMultiplier, LogicalSide.SERVER);
        EntityHurtByGunEvent.PRE.invoker().post(preEvent);
        if (preEvent.isCanceled()) {
            return;
        }
        // Pre 이벤트가 바꾼 매개변수 새로 고치기
        entity = preEvent.getHurtEntity();
        attacker = preEvent.getAttacker();
        var newGunId = preEvent.getGunId();
        damage = preEvent.getBaseAmount();
        sources = Pair.of(preEvent.getDamageSource(NON_ARMOR_PIERCING), preEvent.getDamageSource(ARMOR_PIERCING));
        headshot = preEvent.isHeadShot();
        headShotMultiplier = preEvent.getHeadshotMultiplier();
        if (entity == null) {
            return;
        }
        // 맞은 대상
        var parts = MaybeMultipartEntity.of(entity);
        // 점화
        if (this.igniteEntity && AmmoConfig.IGNITE_ENTITY.get()) {
            entity.igniteForSeconds(this.igniteEntityTime);
            // 입자 효과 주기
            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.LAVA, entity.getX(), entity.getY() + entity.getEyeHeight(), entity.getZ(), 1, 0, 0, 0, 0);
            }
        }
        // TODO 치명타 판정(헤드샷 아님) 내부 로직. 치명타 여부 flag를 출력해야 한다
        if (headshot) {
            // 기본 헤드샷 피해는 1배
            damage *= headShotMultiplier;
        }
        // LivingEntity의 넉백 세기를 사용자 정의한다
        if (parts.core() instanceof LivingEntity livingCore) {
            float healthBefore = livingCore.getHealth();
            float absorptionBefore = livingCore.getAbsorptionAmount();
            // 넉백 효과를 취소하고 자체 넉백 세기를 설정한다
            KnockBackModifier modifier = KnockBackModifier.fromLivingEntity(livingCore);
            modifier.setKnockBackStrength(this.knockback);
            // 피해 생성
            tacAttackEntity(parts, damage, sources);
            // 원래대로 되돌리기
            modifier.resetKnockBackStrength();
            // 폭발 전에 직접 타격으로 감소한 체력만 확인한다.
            awardShotExperience(livingCore, healthBefore, absorptionBefore);
        } else {
            // 피해 생성
            tacAttackEntity(parts, damage, sources);
        }
        // 폭발 로직
        if (this.explosion) {
            // 무적 시간 취소
            parts.core().invulnerableTime = 0;
            tryExplode(result.getLocation());
        }
        // LivingEntity에만 처치 판정을 한다
        if (parts.core() instanceof LivingEntity livingCore) {
            // 이벤트 동기화. 서버에서 클라이언트로
            if (!level().isClientSide()) {
                int attackerId = attacker == null ? 0 : attacker.getId();
                // 생물이 죽었으면
                if (livingCore.isDeadOrDying()) {
                    EntityKillByGunEvent killByGunEvent = new EntityKillByGunEvent(this, livingCore, attacker, newGunId, gunDisplayId, damage, sources, headshot, headShotMultiplier, LogicalSide.SERVER);
                    EntityKillByGunEvent.CALLBACK.invoker().post(killByGunEvent);
                    NetworkHandler.sendToDimension(new ServerMessageGunKill(getId(), livingCore.getId(), attackerId, newGunId, gunDisplayId, damage, headshot, headShotMultiplier), livingCore);
                } else {
                    EntityHurtByGunEvent.Post hurtByGunEvent = new EntityHurtByGunEvent.Post(this, livingCore, attacker, newGunId, gunDisplayId, damage, sources, headshot, headShotMultiplier, LogicalSide.SERVER);
                    EntityHurtByGunEvent.POST.invoker().post(hurtByGunEvent);
                    NetworkHandler.sendToDimension(new ServerMessageGunHurt(getId(), livingCore.getId(), attackerId, newGunId, gunDisplayId, damage, headshot, headShotMultiplier), livingCore);
                }
            }
        }
    }

    /**
     * 폭발탄이면 이 자리에서 폭발한다. 한 발의 산탄은 사격 정보를 공유하므로 처음 폭발한 알만 터진다.
     *
     * @return 폭발했으면 true
     */
    private boolean tryExplode(Vec3 position) {
        if (!this.explosion) {
            return false;
        }
        boolean alreadyExploded = this.shotContext != null && !this.shotContext.claimExplosion();
        if (alreadyExploded) {
            return false;
        }
        ExplodeUtil.createExplosion(this.getOwner(), this, this.explosionDamage, this.gunLevelExplosionMultiplier,
                this.explosionRadius, this.explosionKnockback, this.explosionDestroyBlock, position, this.shotContext);
        return true;
    }

    private void awardShotExperience(LivingEntity target, float healthBefore, float absorptionBefore) {
        if (this.shotContext == null || healthBefore <= 0) {
            return;
        }
        boolean tookDamage = false;
        if (target.getHealth() < healthBefore) {
            tookDamage = true;
        }
        if (target.getAbsorptionAmount() < absorptionBefore) {
            tookDamage = true;
        }
        if (tookDamage) {
            this.shotContext.awardExperience(target);
        }
    }

    protected void onHitBlock(BlockHitResult result, Vec3 startVec, Vec3 endVec) {
        if (result.getType() == HitResult.Type.MISS) {
            return;
        }
        BlockPos pos = result.getBlockPos();
        Vec3 hitVec = result.getLocation();
        // 이벤트 발생
        // 이벤트가 바닐라 명중 동작(예: 종 치기, 표적 쓰러뜨리기)을 취소할 수 있게 이벤트를 먼저 발생시킨다
        AmmoHitBlockEvent ammoHitBlockEvent = new AmmoHitBlockEvent(this.level(), result, this.level().getBlockState(pos), this);
        AmmoHitBlockEvent.CALLBACK.invoker().post(ammoHitBlockEvent);
        if (ammoHitBlockEvent.isCanceled()) {
            return;
        }
        super.onHitBlock(result);
        // 폭발. 같은 사격의 다른 산탄이 이미 폭발했으면 일반 탄처럼 탄흔을 남긴다.
        if (tryExplode(hitVec)) {
            // 폭발하면 탄흔을 남기지 않고 바로 끝내며 이후 로직을 처리하지 않는다
            this.discard();
            return;
        }
        // 탄흔과 점화 효과
        if (this.level() instanceof ServerLevel serverLevel) {
            BulletHoleOption bulletHoleOption = new BulletHoleOption(result.getDirection(), result.getBlockPos(), this.ammoId.toString(), this.gunId.toString(), this.gunDisplayId.toString());
            serverLevel.sendParticles(bulletHoleOption, hitVec.x, hitVec.y, hitVec.z, 1, 0, 0, 0, 0);
            if (this.igniteBlock) {
                serverLevel.sendParticles(ParticleTypes.LAVA, hitVec.x, hitVec.y, hitVec.z, 1, 0, 0, 0, 0);
            }
        }
        if (this.igniteBlock && AmmoConfig.IGNITE_BLOCK.get()) {
            BlockPos offsetPos = pos.relative(result.getDirection());
            if (BaseFireBlock.canBePlacedAt(this.level(), offsetPos, result.getDirection())) {
                BlockState fireState = BaseFireBlock.getState(this.level(), offsetPos);
                this.level().setBlock(offsetPos, fireState, Block.UPDATE_ALL_IMMEDIATE);
                ((ServerLevel) this.level()).sendParticles(ParticleTypes.LAVA, hitVec.x - 1.0 + this.getRandom().nextDouble() * 2.0, hitVec.y, hitVec.z - 1.0 + this.getRandom().nextDouble() * 2.0, 4, 0, 0, 0, 0);
            }
        }
        this.discard();
    }

    // 거리에 따른 피해 감소 설계
    public float getDamage(Vec3 hitVec) {
        // 최댓값을 빠뜨렸으면 피해를 0으로 본다
        float base = 0;
        // 하나씩 돌며 판단한다
        double playerDistance = hitVec.distanceTo(this.startPos);
        for (DistanceDamagePair pair : this.damageAmount) {
            float effectiveDistance = this.damageAmount.get(0).getDistance() == pair.getDistance() ? this.distanceAmount : pair.getDistance();
            if (playerDistance < effectiveDistance) {
                float damage = pair.getDamage();
                base = Math.max(damage * this.damageModifier, 0F);
                break;
            }
        }
        // 스크립트가 총기 피해를 바꾸게 한다
        float modifiedDamage = modifyProperty(GunProperties.DAMAGE, Float.class, base);
        float finalDamage = modifiedDamage * this.shotDamageMultiplier * this.gunLevelDamageMultiplier;
        return Math.max(finalDamage, 0F);
    }

    /**
     * @since 1.1.7
     */
    private <T> T modifyProperty(GunProperty<?> prop, Class<T> type, T original) {
        return modifyProperty(prop.name(), type, original);
    }

    /**
     * @since 1.1.7
     */
    private <T> T modifyProperty(String id, Class<T> type, T original) {
        if (getOwner() instanceof LivingEntity shooter) {
            ItemStack gun = shooter.getMainHandItem();
            if (gun.getItem() instanceof AbstractGunItem gunInterface && Objects.equals(this.gunId, gunInterface.getGunId(gun))) {
                ShooterDataHolder dataHolder = IGunOperator.fromLivingEntity(shooter).getDataHolder();
                return gunInterface.modifyProperty(dataHolder, gun, shooter, id, type, original);
            }
        }
        return original;
    }

    /**
     * @return Pair<비관통 피해 원인, 방어 관통 피해 원인>
     */
    private Pair<DamageSource, DamageSource> createDamageSources(MaybeMultipartEntity parts) {
        DamageSource source1, source2;
        var hitPartType = parts.hitPart().getType();
        var directCause = hitPartType.builtInRegistryHolder().is(PRETEND_MELEE_DAMAGE_ON) ? this.getOwner() : this;
        // 엔더맨에게 피해를 준다
        if (hitPartType.builtInRegistryHolder().is(USE_MAGIC_DAMAGE_ON)) {
            source1 = source2 = this.damageSources().indirectMagic(this, getOwner());
        } else if (hitPartType.builtInRegistryHolder().is(USE_VOID_DAMAGE_ON)) {
            source1 = ModDamageTypes.Sources.bulletVoid(this.level().registryAccess(), directCause, this.getOwner(), false, this.gunId);
            source2 = ModDamageTypes.Sources.bulletVoid(this.level().registryAccess(), directCause, this.getOwner(), true, this.gunId);
        } else {
            source1 = ModDamageTypes.Sources.bullet(this.level().registryAccess(), directCause, this.getOwner(), false, this.gunId);
            source2 = ModDamageTypes.Sources.bullet(this.level().registryAccess(), directCause, this.getOwner(), true, this.gunId);
        }
        return Pair.of(source1, source2);
    }

    private void tacAttackEntity(MaybeMultipartEntity parts, float damage, Pair<DamageSource, DamageSource> sources) {
        var source1 = sources.getLeft();
        var source2 = sources.getRight();
        // 방어 관통 피해와 일반 피해의 비율 계산
        float armorDamagePercent = Mth.clamp(this.armorIgnore, 0.0F, 1.0F);
        float normalDamagePercent = 1 - armorDamagePercent;
        // 무적 시간 취소
        parts.core().invulnerableTime = 0;
        // 일반 피해
        parts.hitPart().hurt(source1, damage * normalDamagePercent);
        // 무적 시간 취소
        parts.core().invulnerableTime = 0;
        // 방어 관통 피해
        parts.hitPart().hurt(source2, damage * armorDamagePercent);
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
        return IEntityAdditionalSpawnData.getEntitySpawningPacket(this);
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeFloat(getXRot());
        buffer.writeFloat(getYRot());
        buffer.writeDouble(getDeltaMovement().x);
        buffer.writeDouble(getDeltaMovement().y);
        buffer.writeDouble(getDeltaMovement().z);
        Entity entity = getOwner();
        buffer.writeInt(entity != null ? entity.getId() : 0);
        buffer.writeIdentifier(ammoId);
        buffer.writeFloat(this.gravity);
        buffer.writeBoolean(this.explosion);
        buffer.writeBoolean(this.igniteEntity);
        buffer.writeBoolean(this.igniteBlock);
        buffer.writeFloat(this.explosionRadius);
        buffer.writeFloat(this.explosionDamage);
        buffer.writeInt(this.life);
        buffer.writeFloat(this.speed);
        buffer.writeFloat(this.friction);
        buffer.writeInt(this.pierce);
        buffer.writeBoolean(this.isTracerAmmo);
        buffer.writeIdentifier(this.gunId);
        buffer.writeIdentifier(this.gunDisplayId);
        buffer.writeFloat(this.gunLevelDamageMultiplier);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        setXRot(additionalData.readFloat());
        setYRot(additionalData.readFloat());
        setDeltaMovement(additionalData.readDouble(), additionalData.readDouble(), additionalData.readDouble());
        Entity entity = this.level().getEntity(additionalData.readInt());
        if (entity != null) {
            this.setOwner(entity);
        }
        this.ammoId = additionalData.readIdentifier();
        this.gravity = additionalData.readFloat();
        this.explosion = additionalData.readBoolean();
        this.igniteEntity = additionalData.readBoolean();
        this.igniteBlock = additionalData.readBoolean();
        this.explosionRadius = additionalData.readFloat();
        this.explosionDamage = additionalData.readFloat();
        this.life = additionalData.readInt();
        this.speed = additionalData.readFloat();
        this.friction = additionalData.readFloat();
        this.pierce = additionalData.readInt();
        this.isTracerAmmo = additionalData.readBoolean();
        this.gunId = additionalData.readIdentifier();
        this.gunDisplayId = additionalData.readIdentifier();
        this.gunLevelDamageMultiplier = additionalData.readFloat();
    }

    public Identifier getAmmoId() {
        return ammoId;
    }

    public Identifier getGunId() {
        return gunId;
    }

    public Identifier getGunDisplayId() {
        return gunDisplayId;
    }

    public boolean isTracerAmmo() {
        return isTracerAmmo;
    }

    public RandomSource getRandom() {
        return this.random;
    }

    public float getCameraYRot() {
        return cameraYRot;
    }

    public void setCameraYRot(float cameraYRot) {
        this.cameraYRot = cameraYRot;
    }

    public float getCameraXRot() {
        return cameraXRot;
    }

    public void setCameraXRot(float cameraXRot) {
        this.cameraXRot = cameraXRot;
    }

    public Vector3f getFirstPersonRenderOffset() {
        return firstPersonRenderOffset;
    }

    public void setFirstPersonRenderOffset(Vector3f originRenderOffset) {
        this.firstPersonRenderOffset = originRenderOffset;
    }

    public Vec3 getFirstPersonOrigin() {
        return firstPersonOrigin;
    }

    public Vec3 getFirstPersonImpact() {
        return firstPersonImpact;
    }

    public void setFirstPersonPath(Vec3 origin, Vec3 impact) {
        this.firstPersonOrigin = origin;
        this.firstPersonImpact = impact;
    }

    public Optional<float[]> getTracerColorOverride() {
        var pd = ((IEntityPersistentData) this).tacz$getPersistentData();
        var optInts = pd.getIntArray(TRACER_COLOR_OVERRIDER_KEY);
        if (optInts.isEmpty()) {
            return Optional.empty();
        }
        var ints = optInts.get();
            // 값이 1개나 2개인 배열은 쓰지 않는다.
            // 여기서 값 1~2개 분기는 충돌 대신 예외 상황을 깔끔하게 처리하려는 조치일 뿐이다 :(
            switch (ints.length) {
                case 0:
                    return Optional.empty();
                case 1: {
                    var albedo = ints[0] / 255F;
                    return Optional.of(new float[]{albedo, albedo, albedo, 1});
                }
                case 2: {
                    var albedo = ints[0] / 255F;
                    var alpha = ints[1] / 255F;
                    return Optional.of(new float[]{albedo, albedo, albedo, alpha});
                }
                case 3: {
                    var r = ints[0] / 255F;
                    var g = ints[1] / 255F;
                    var b = ints[2] / 255F;
                    return Optional.of(new float[]{r, g, b, 1});
                }
                default: {
                    var r = ints[0] / 255F;
                    var g = ints[1] / 255F;
                    var b = ints[2] / 255F;
                    var a = ints[3] / 255F;
                    return Optional.of(new float[]{r, g, b, a});
                }
            }
    }

    public float getTracerSizeOverride() {
        var pd = ((IEntityPersistentData) this).tacz$getPersistentData();
        return pd.contains(TRACER_SIZE_OVERRIDER_KEY) ? pd.getFloatOr(TRACER_SIZE_OVERRIDER_KEY, 1f) : 1;
    }

    @Override
    public boolean ownedBy(@Nullable Entity entity) {
        if (entity == null) {
            return false;
        }
        return super.ownedBy(entity);
    }

    public static class EntityResult {
        private final Entity entity;
        private final Vec3 hitVec;
        private final boolean headshot;

        public EntityResult(Entity entity, Vec3 hitVec, boolean headshot) {
            this.entity = entity;
            this.hitVec = hitVec;
            this.headshot = headshot;
        }

        // 탄환이 맞힌 엔티티
        public Entity getEntity() {
            return this.entity;
        }

        // 탄환이 맞힌 위치
        public Vec3 getHitPos() {
            return this.hitVec;
        }

        // 헤드샷 여부
        public boolean isHeadshot() {
            return this.headshot;
        }
    }
}
