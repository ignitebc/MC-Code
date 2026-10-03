package com.daqem.arc.player;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;

/** 한 번의 효과 적용 안에서 외부 강화와 일반 강화의 중복 합산을 막는다. */
public final class EffectAmplifierScope implements AutoCloseable {
    private static final ThreadLocal<EffectAmplifierScope> CURRENT = new ThreadLocal<>();
    private final EffectAmplifierScope previous;
    private final ServerPlayer player;
    private final MobEffectInstance original;
    private final int minimumAddition;
    private final int extraAddition;
    private final int maximumAmplifier;

    public EffectAmplifierScope(ServerPlayer player, MobEffectInstance original,
                                int minimumAddition, int extraAddition, int maximumAmplifier) {
        this.previous = CURRENT.get();
        this.player = player;
        this.original = original;
        this.minimumAddition = minimumAddition;
        this.extraAddition = extraAddition;
        this.maximumAmplifier = maximumAmplifier;
        CURRENT.set(this);
    }

    public static int resolve(ServerPlayer player, MobEffectInstance effect, int addition) {
        EffectAmplifierScope scope = CURRENT.get();
        if (scope == null || scope.player != player || !scope.original.getEffect().equals(effect.getEffect())) {
            return effect.getAmplifier() + addition;
        }
        int amplifier = scope.original.getAmplifier()
                + Math.max(scope.minimumAddition, addition) + scope.extraAddition;
        // 이미 상한보다 높은 원본 효과를 약화시키지는 않는다.
        return Math.max(scope.original.getAmplifier(), Math.min(amplifier, scope.maximumAmplifier));
    }

    @Override
    public void close() {
        if (previous == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(previous);
        }
    }
}
