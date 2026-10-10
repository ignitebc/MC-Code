package com.tacz.guns.api.event.common;

import cn.sh1rocu.tacz.api.LogicalSide;
import cn.sh1rocu.tacz.api.event.BaseEvent;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * 생물이 총기 탄환에 죽을 때 발생하는 이벤트
 */
public class EntityKillByGunEvent extends BaseEvent {
    private final Entity bullet;
    private final @Nullable LivingEntity killedEntity;
    private final @Nullable LivingEntity attacker;
    private final Identifier gunId;
    private final Identifier gunDisplayId;
    private final float baseDamage;
    private final DamageSource nonApPartDamageSource;
    private final DamageSource apPartDamageSource;
    private final boolean isHeadShot;
    private final float headshotMultiplier;
    private final LogicalSide logicalSide;

    public static final Event<Callback> CALLBACK = EventFactory.createArrayBacked(Callback.class, callbacks -> event -> {
        for (Callback callback : callbacks) {
            callback.post(event);
        }
    });

    public interface Callback {
        void post(EntityKillByGunEvent event);
    }

    public EntityKillByGunEvent(Entity bullet, @Nullable LivingEntity hurtEntity, @Nullable LivingEntity attacker,
                                Identifier gunId, Identifier gunDisplayId, float baseDamage, @Nullable Pair<DamageSource, DamageSource> sources,
                                boolean isHeadShot, float headshotMultiplier, LogicalSide logicalSide) {
        this.bullet = bullet;
        this.killedEntity = hurtEntity;
        this.attacker = attacker;
        this.gunId = gunId;
        this.gunDisplayId = gunDisplayId;
        this.baseDamage = baseDamage;
        this.nonApPartDamageSource = Optional.ofNullable(sources).map(Pair::getLeft).orElse(null);
        this.apPartDamageSource = Optional.ofNullable(sources).map(Pair::getRight).orElse(null);
        this.isHeadShot = isHeadShot;
        this.headshotMultiplier = headshotMultiplier;
        this.logicalSide = logicalSide;
    }

    /**
     * 논리 클라이언트에서는 쓸 수 있다고 보장하지 않는다
     */
    public Entity getBullet() {
        return bullet;
    }

    @Nullable
    public LivingEntity getKilledEntity() {
        return killedEntity;
    }

    @Nullable
    public LivingEntity getAttacker() {
        return attacker;
    }

    public Identifier getGunId() {
        return gunId;
    }

    public float getBaseDamage() {
        return baseDamage;
    }

    public DamageSource getDamageSource(GunDamageSourcePart part) {
        if (logicalSide.isClient()) {
            throw new UnsupportedOperationException("DamageSource about gun hit is not available on client side!");
        }
        return part == GunDamageSourcePart.ARMOR_PIERCING ? apPartDamageSource : nonApPartDamageSource;
    }

    public boolean isHeadShot() {
        return isHeadShot;
    }

    public float getHeadshotMultiplier() {
        return headshotMultiplier;
    }

    public LogicalSide getLogicalSide() {
        return logicalSide;
    }

    public Identifier getGunDisplayId() {
        return gunDisplayId;
    }
}