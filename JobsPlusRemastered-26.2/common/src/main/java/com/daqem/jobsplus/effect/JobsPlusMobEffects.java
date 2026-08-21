package com.daqem.jobsplus.effect;

import com.daqem.jobsplus.JobsPlus;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 직업 보상 2배 쿠폰의 HUD 표시용 상태 효과.
 * 실제 EXP/BTC 배율 판정은 RewardCouponLedger에서 수행하며 이 효과 자체는 능력치를 변경하지 않는다.
 */
public final class JobsPlusMobEffects
{
    public static final Holder<MobEffect> EXPERIENCE_DOUBLE = register(
            "experience_double",
            0x4CD8FF,
            "직업 경험치 2배"
    );
    public static final Holder<MobEffect> BITCOIN_DOUBLE = register(
            "bitcoin_double",
            0xF4B942,
            "직업 비트코인 2배"
    );

    private JobsPlusMobEffects()
    {
    }

    public static void init()
    {
        // 정적 필드 초기화로 등록된다.
    }

    private static Holder<MobEffect> register(String id, int color, String displayName)
    {
        return Registry.registerForHolder(
                BuiltInRegistries.MOB_EFFECT,
                JobsPlus.getId(id),
                new RewardCouponMobEffect(color, displayName)
        );
    }

    private static final class RewardCouponMobEffect extends MobEffect
    {
        private final Component displayName;

        private RewardCouponMobEffect(int color, String displayName)
        {
            super(MobEffectCategory.BENEFICIAL, color);
            this.displayName = Component.literal(displayName);
        }

        @Override
        public Component getDisplayName()
        {
            return this.displayName;
        }
    }
}
