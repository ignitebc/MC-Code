package com.tacz.guns.resource.pojo.data.gun;

import com.google.gson.annotations.SerializedName;

public class ExplosionData {
    @SerializedName("explode")
    private boolean explode;

    @SerializedName("radius")
    private float radius;

    @SerializedName("damage")
    private float damage;

    @SerializedName("knockback")
    private boolean knockback;

    @SerializedName("destroy_block")
    private boolean destroyBlock;

    /**
     * 엔티티나 블록에 닿았는지와 관계없이 기본으로 30초 뒤 폭발한다
     */
    @SerializedName("delay")
    private float delay;

    public ExplosionData(boolean explode, float radius, float damage, boolean knockback, float delay, boolean destroyBlock) {
        this.explode = explode;
        this.radius = radius;
        this.damage = damage;
        this.knockback = knockback;
        this.delay = delay;
        this.destroyBlock = destroyBlock;
    }

    public boolean isExplode() {
        return explode;
    }

    public float getRadius() {
        return radius;
    }

    public float getDamage() {
        return damage;
    }

    public boolean isKnockback() {
        return knockback;
    }

    public boolean isDestroyBlock() {
        return destroyBlock;
    }

    public float getDelay() {
        return delay;
    }
}
