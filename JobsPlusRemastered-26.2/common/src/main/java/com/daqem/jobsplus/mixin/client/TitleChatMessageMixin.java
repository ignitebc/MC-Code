package com.daqem.jobsplus.mixin.client;

import com.daqem.jobsplus.player.title.TitleType;
import net.minecraft.client.multiplayer.chat.GuiMessage;
import net.minecraft.network.chat.FormattedText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/** 채팅 표시용 복사본만 축소해 팀 접두사와 원본 메시지를 유지한다. */
@Mixin(GuiMessage.class)
public abstract class TitleChatMessageMixin
{
    @ModifyArg(method = "splitLines",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/ComponentRenderUtils;wrapComponents(Lnet/minecraft/network/chat/FormattedText;ILnet/minecraft/client/gui/Font;)Ljava/util/List;"),
            index = 0)
    private FormattedText jobsplus$chatBadgeSize(FormattedText text)
    {
        return TitleType.forChat(text);
    }
}
