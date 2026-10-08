package com.daqem.arc.player;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.data.ActionDataBuilder;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.action.type.ActionType;
import com.daqem.arc.api.player.ArcServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import org.jetbrains.annotations.Nullable;

/**
 * 상태 효과 스킬(농축 정제·오래 끓인 약 등)이 바꿀 단계와 시간을 효과를 실제로 넣지 않고 계산한다.
 * <p>
 * 효과 보상은 평소 계산한 효과를 플레이어에게 바로 다시 넣는다. 음식 흡수 중첩처럼 효과를 직접 관리하는 쪽은
 * 다시 넣는 효과가 계산을 흐트러뜨리므로, 이 클래스로 결과만 받아 원하는 방식으로 적용한다. 발동 알림은 평소처럼 보낸다.
 */
public final class EffectRewardPreview {
    private static final ThreadLocal<Boolean> PREVIEWING = ThreadLocal.withInitial(() -> false);

    private EffectRewardPreview() {
    }

    /** 효과 보상이 플레이어에게 효과를 다시 넣지 말아야 하는 계산 중인지. */
    public static boolean isPreviewing() {
        return PREVIEWING.get();
    }

    /**
     * 효과를 받았을 때와 같은 스킬을 적용한 결과를 돌려준다.
     *
     * @return 스킬이 바꾼 효과. 바뀐 것이 없으면 받은 효과 그대로, 효과 추가가 취소되면 null
     */
    public static @Nullable MobEffectInstance resolve(ArcServerPlayer player, MobEffectInstance effect) {
        boolean previous = PREVIEWING.get();
        PREVIEWING.set(true);
        try {
            ActionData actionData = new ActionDataBuilder(player, ActionType.EFFECT_ADDED)
                    .withData(ActionDataType.MOB_EFFECT_INSTANCE, effect)
                    .build();
            ActionResult result = actionData.sendToAction();
            if (result.shouldCancelAction()) {
                return null;
            }
            MobEffectInstance resolved = actionData.getData(ActionDataType.MOB_EFFECT_INSTANCE);
            if (resolved == null) {
                return effect;
            }
            return resolved;
        } finally {
            PREVIEWING.set(previous);
        }
    }
}
