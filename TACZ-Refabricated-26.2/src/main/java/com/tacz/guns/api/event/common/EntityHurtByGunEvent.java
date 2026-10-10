package com.tacz.guns.api.event.common;

import cn.sh1rocu.tacz.api.LogicalSide;
import cn.sh1rocu.tacz.api.event.BaseEvent;
import cn.sh1rocu.tacz.api.event.ICancellableEvent;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.ApiStatus.Obsolete;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * 생물이 총기 탄환에 피해를 입을 때 발생하는 이벤트
 */
public class EntityHurtByGunEvent extends BaseEvent {
    protected final Entity bullet;
    protected @Nullable Entity hurtEntity;
    protected @Nullable LivingEntity attacker;
    protected Identifier gunId;
    protected Identifier gunDisplayId;
    protected float baseAmount;
    protected DamageSource nonApPartDamageSource;
    protected DamageSource apPartDamageSource;
    protected boolean isHeadShot;
    protected float headshotMultiplier;
    protected final LogicalSide logicalSide;

    public static final Event<PreCallBack> PRE = EventFactory.createArrayBacked(PreCallBack.class, callbacks -> event -> {
        for (PreCallBack callback : callbacks) {
            callback.post(event);
        }
    });

    public static final Event<PostCallBack> POST = EventFactory.createArrayBacked(PostCallBack.class, callbacks -> event -> {
        for (PostCallBack callback : callbacks) {
            callback.post(event);
        }
    });

    public interface PreCallBack {
        void post(Pre event);
    }

    public interface PostCallBack {
        void post(Post event);
    }

    @ApiStatus.Internal
    protected EntityHurtByGunEvent(Entity bullet, @Nullable Entity hurtEntity, @Nullable LivingEntity attacker,
                                   Identifier gunId, Identifier gunDisplayId,
                                   float baseAmount, @Nullable Pair<DamageSource, DamageSource> sources, boolean isHeadShot,
                                   float headshotMultiplier, LogicalSide logicalSide) {
        this.bullet = bullet;
        this.hurtEntity = hurtEntity;
        this.attacker = attacker;
        this.gunId = gunId;
        this.baseAmount = baseAmount;
        this.nonApPartDamageSource = Optional.ofNullable(sources).map(Pair::getLeft).orElse(null);
        this.apPartDamageSource = Optional.ofNullable(sources).map(Pair::getRight).orElse(null);
        this.isHeadShot = isHeadShot;
        this.headshotMultiplier = headshotMultiplier;
        this.logicalSide = logicalSide;
    }

    /**
     * 엔티티가 총에 맞아 피해 판정을 하기 전에 발생한다. 총격 피해 속성을 설정할 수 있다
     */
    public static class Pre extends EntityHurtByGunEvent implements ICancellableEvent {
        @ApiStatus.Internal
        public Pre(Entity bullet, @Nullable Entity hurtEntity, @Nullable LivingEntity attacker,
                   Identifier gunId, Identifier gunDisplayId,
                   float amount, @Nullable Pair<DamageSource, DamageSource> sources,
                   boolean isHeadShot, float headshotMultiplier, LogicalSide logicalSide) {
            super(bullet, hurtEntity, attacker, gunId, gunDisplayId, amount, sources, isHeadShot, headshotMultiplier, logicalSide);
            this.headshotMultiplier = headshotMultiplier;
        }

        public final void setHurtEntity(@Nullable Entity hurtEntity) {
            this.hurtEntity = hurtEntity;
        }

        public final void setAttacker(@Nullable LivingEntity attacker) {
            this.attacker = attacker;
        }

        public final void setGunId(Identifier gunId) {
            this.gunId = gunId;
        }

        public final void setBaseAmount(float baseAmount) {
            this.baseAmount = baseAmount;
        }

        public final void setDamageSource(GunDamageSourcePart part, DamageSource value) {
            if (logicalSide.isClient()) {
                throw new UnsupportedOperationException("DamageSource about gun hit is not available on client side!");
            }
            if (part == GunDamageSourcePart.ARMOR_PIERCING) {
                apPartDamageSource = value;
            } else {
                nonApPartDamageSource = value;
            }
        }

        public final void setHeadshot(boolean headshot) {
            this.isHeadShot = headshot;
        }

        public final void setHeadshotMultiplier(float headshotMultiplier) {
            this.headshotMultiplier = headshotMultiplier;
        }
    }

    /**
     * 엔티티가 총에 맞아 피해 판정이 끝났지만 죽지 않았을 때 발생한다
     *
     * @see EntityKillByGunEvent 엔티티가 총격으로 죽을 때 발생하는 이벤트
     */
    public static class Post extends EntityHurtByGunEvent {
        @ApiStatus.Internal
        public Post(Entity bullet, @Nullable Entity hurtEntity, @Nullable LivingEntity attacker,
                    Identifier gunId, Identifier gunDisplayId,
                    float amount, @Nullable Pair<DamageSource, DamageSource> sources,
                    boolean isHeadShot, float headshotMultiplier, LogicalSide logicalSide) {
            super(bullet, hurtEntity, attacker, gunId, gunDisplayId, amount, sources, isHeadShot, headshotMultiplier, logicalSide);
        }
    }

    public Entity getBullet() {
        return bullet;
    }

    @Nullable
    public Entity getHurtEntity() {
        return hurtEntity;
    }

    @Nullable
    public LivingEntity getAttacker() {
        return attacker;
    }

    public Identifier getGunId() {
        return gunId;
    }

    public Identifier getGunDisplayId() {
        return gunDisplayId;
    }

    @Obsolete
    public float getAmount() {
        return baseAmount * headshotMultiplier;
    }

    public float getBaseAmount() {
        return baseAmount;
    }

    public DamageSource getDamageSource(GunDamageSourcePart part) {
        if (logicalSide.isClient()) {
            throw new UnsupportedOperationException("DamageSource about gun hit is not available on client side!");
        }
        return part == GunDamageSourcePart.ARMOR_PIERCING ? apPartDamageSource : nonApPartDamageSource;
    }

    public float getHeadshotMultiplier() {
        return headshotMultiplier;
    }

    public boolean isHeadShot() {
        return isHeadShot;
    }

    public LogicalSide getLogicalSide() {
        return logicalSide;
    }
}
