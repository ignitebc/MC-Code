package com.daqem.jobsplus.mixin.client;

import com.daqem.jobsplus.player.title.TitleType;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import java.util.List;

/** 큰 칭호 배지가 있는 채팅 기록은 그리기와 클릭 판정에 같은 줄 간격을 사용한다. */
@Mixin(ChatComponent.class)
public abstract class TitleChatLayoutMixin
{
    @Shadow @Final
    private List<GuiMessage.Line> trimmedMessages;

    @Unique
    private boolean jobsplus$hasTitleBadge()
    {
        for (GuiMessage.Line line : this.trimmedMessages)
        {
            if (TitleType.containsBadge(line.content()))
            {
                return true;
            }
        }
        return false;
    }

    @ModifyConstant(method = "getLineHeight", constant = @Constant(doubleValue = 9.0))
    private double jobsplus$lineHeight(double original)
    {
        return this.jobsplus$hasTitleBadge() ? TitleType.BADGE_LINE_HEIGHT : original;
    }

    // 이 내부 메서드는 화면 그리기와 클릭 가능한 글자 수집 양쪽에서 호출된다.
    @ModifyConstant(method = "extractRenderState(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;)V",
            constant = @Constant(intValue = 9))
    private int jobsplus$renderLineHeight(int original)
    {
        return this.jobsplus$hasTitleBadge() ? TitleType.BADGE_LINE_HEIGHT : original;
    }

    @ModifyConstant(method = "extractRenderState(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;)V",
            constant = @Constant(doubleValue = 8.0))
    private double jobsplus$textBaseline(double original)
    {
        // height 18 / ascent 12인 배지의 아래쪽이 채팅 한 줄의 바닥을 넘지 않게 한다.
        return this.jobsplus$hasTitleBadge() ? original + TitleType.BADGE_TEXT_OFFSET : original;
    }

    @ModifyArg(method = "extractRenderState(Lnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;IILnet/minecraft/client/gui/components/ChatComponent$DisplayMode;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ChatComponent$1;<init>(Lnet/minecraft/client/gui/components/ChatComponent;IIILnet/minecraft/client/gui/components/ChatComponent$ChatGraphicsAccess;FI)V"), index = 6)
    private int jobsplus$tagMessageHeight(int original)
    {
        // 메시지 신뢰도 아이콘은 확대된 행 높이가 아니라 일반 글자 높이에 맞춘다.
        return this.jobsplus$hasTitleBadge() ? 9 : original;
    }
}
