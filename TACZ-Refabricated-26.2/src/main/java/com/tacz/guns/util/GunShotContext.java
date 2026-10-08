package com.tacz.guns.util;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import javax.annotation.Nullable;
import java.util.UUID;

/** 탄약 한 발의 레벨 배율, 경험치 지급 여부, 폭발 여부. 산탄과 관통 대상 사이에 공유된다. */
public final class GunShotContext {
    private final float damageMultiplier;
    private final @Nullable ServerPlayer player;
    private final @Nullable UUID gunInstanceId;
    private final @Nullable Identifier gunId;
    private boolean experienceAwarded;
    private boolean explosionTriggered;

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

    /**
     * 이 사격에서 처음 일어나는 폭발이면 true. 고폭탄 산탄은 알마다 폭발하지 않고 처음 맞은 알만 폭발한다.
     * <p>
     * 고폭탄 산탄총의 폭발 피해 값은 한 발 전체 피해와 같게 잡혀 있어, 알마다 터지면 한 발에 그 값이 알 수만큼 들어간다.
     */
    public boolean claimExplosion() {
        if (this.explosionTriggered) {
            return false;
        }
        this.explosionTriggered = true;
        return true;
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
