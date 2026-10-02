package com.tacz.guns.util;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.UUID;

/** 탄약 한 발의 레벨 배율과 경험치 지급 여부. 산탄과 관통 대상 사이에 공유된다. */
public final class GunShotContext {
    private final float damageMultiplier;
    private final @Nullable ServerPlayer player;
    private final @Nullable UUID gunInstanceId;
    private final @Nullable Identifier gunId;
    private boolean experienceAwarded;

    public GunShotContext(float damageMultiplier) {
        this(damageMultiplier, null, null, null);
    }

    public GunShotContext(float damageMultiplier, @Nullable ServerPlayer player,
                          @Nullable UUID gunInstanceId, @Nullable Identifier gunId) {
        this.damageMultiplier = damageMultiplier;
        this.player = player;
        this.gunInstanceId = gunInstanceId;
        this.gunId = gunId;
    }

    public float getDamageMultiplier() {
        return this.damageMultiplier;
    }

    /** 직접 타격으로 체력 또는 흡수 체력이 감소한 뒤, 서버에서만 호출한다. */
    public void awardExperience(LivingEntity target) {
        if (this.experienceAwarded || this.player == null || this.gunInstanceId == null || this.gunId == null) {
            return;
        }
        if (target.level().isClientSide() || this.player.isRemoved()
                || this.player.isCreative() || this.player.isSpectator()) {
            return;
        }
        if (!GunLevelManager.isExperienceTarget(this.player, target)) {
            return;
        }
        this.experienceAwarded = true;
        GunLevelManager.addExperience(this.player, this.gunInstanceId, this.gunId);
    }
}
