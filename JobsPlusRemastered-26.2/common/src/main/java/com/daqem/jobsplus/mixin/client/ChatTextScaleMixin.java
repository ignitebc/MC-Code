package com.daqem.jobsplus.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.gui.components.ChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

/**
 * 채팅 글자, 칭호 배지, 줄 간격을 함께 한 단계 작게 그린다.
 *
 * <p>바닐라는 이 배율 하나로 그리기, 클릭 판정, 줄바꿈 폭을 모두 계산하므로 여기서 줄여야 셋이 어긋나지 않는다.
 */
@Mixin(ChatComponent.class)
public abstract class ChatTextScaleMixin
{
    /** 플레이어의 채팅 텍스트 크기 설정에 곱하므로 각자 고른 크기의 비율은 그대로 유지된다. */
    @Unique
    private static final double JOBSPLUS_CHAT_TEXT_SCALE = 0.85;

    @ModifyReturnValue(method = "getScale", at = @At("RETURN"))
    private double jobsplus$shrinkChatText(double original)
    {
        return original * JOBSPLUS_CHAT_TEXT_SCALE;
    }
}
