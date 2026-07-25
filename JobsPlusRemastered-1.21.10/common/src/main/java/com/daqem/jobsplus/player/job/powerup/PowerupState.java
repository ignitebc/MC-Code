package com.daqem.jobsplus.player.job.powerup;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * 게임에서 파워업의 상태를 나타내는 열거형입니다.
 * 각 상태는 파워업이 가질 수 있는 다른 조건을 나타냅니다.
 */
public enum PowerupState implements StringRepresentable {
    /**
     * 파워업이 현재 활성화되어 사용 중입니다.
     */
    ACTIVE,

    /**
     * 플레이어가 파워업을 비활성화했습니다.
     */
    INACTIVE,

    /**
     * 파워업을 구매할 수 있지만 아직 구매하지 않은 상태입니다.
     */
    NOT_OWNED,

    /**
     * 파워업을 아직 구매할 수 없는 상태입니다.
     */
    LOCKED;

    public static final StringRepresentable.EnumCodec<PowerupState> CODEC = StringRepresentable
            .fromEnum(PowerupState::values);

    @Override
    public @NotNull String getSerializedName() {
        return this.name();
    }
}
