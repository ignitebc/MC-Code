package com.daqem.jobsplus.mixin.client;

import com.daqem.jobsplus.player.title.TitleType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** 팀 접두사의 같은 배지를 Tab에서도 이웃 행과 겹치지 않게 표시한다. */
@Mixin(PlayerTabOverlay.class)
public abstract class TitleTabLayoutMixin
{
    @Shadow @Final
    private Minecraft minecraft;

    @Shadow
    private Component header;

    @Shadow
    private Component footer;

    @Shadow
    public abstract Component getNameForDisplay(PlayerInfo player);

    @Unique
    private boolean jobsplus$hasTitleBadge;

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void jobsplus$updateLayout(CallbackInfo ci)
    {
        this.jobsplus$hasTitleBadge = false;
        var connection = this.minecraft.getConnection();
        if (connection == null)
        {
            return;
        }
        for (PlayerInfo player : connection.getListedOnlinePlayers())
        {
            if (TitleType.containsBadge(this.getNameForDisplay(player).getVisualOrderText()))
            {
                this.jobsplus$hasTitleBadge = true;
                return;
            }
        }
    }

    // 순서: 목록 배경 높이, 행 간격, footer 시작 위치. 머리 아이콘 폭과 header/footer 글꼴은 제외한다.
    @ModifyConstant(method = "extractRenderState", constant = {
            @Constant(intValue = 9, ordinal = 3),
            @Constant(intValue = 9, ordinal = 4),
            @Constant(intValue = 9, ordinal = 6)
    }, require = 3, allow = 3)
    private int jobsplus$rowHeight(int original)
    {
        return this.jobsplus$hasTitleBadge ? TitleType.BADGE_LINE_HEIGHT : original;
    }

    @ModifyConstant(method = "extractRenderState", constant = @Constant(intValue = 8, ordinal = 0))
    private int jobsplus$rowBackgroundHeight(int original)
    {
        return this.jobsplus$hasTitleBadge ? TitleType.BADGE_LINE_HEIGHT - 1 : original;
    }

    @ModifyConstant(method = "extractRenderState", constant = @Constant(intValue = 20))
    private int jobsplus$rowsPerColumn(int original)
    {
        if (!this.jobsplus$hasTitleBadge)
        {
            return original;
        }
        // GUI 배율이 클 때 20행을 그대로 두면 확대된 배지가 화면 아래로 잘린다.
        int textWidth = Math.max(1, this.minecraft.getWindow().getGuiScaledWidth() - 50);
        int availableHeight = this.minecraft.getWindow().getGuiScaledHeight() - 40;
        for (Component decoration : new Component[]{this.header, this.footer})
        {
            if (decoration != null)
            {
                availableHeight -= this.minecraft.font.split(decoration, textWidth).size() * 9 + 1;
            }
        }
        return Math.max(1, Math.min(original, availableHeight / TitleType.BADGE_LINE_HEIGHT));
    }

    @ModifyArg(method = "extractRenderState", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/GuiGraphicsExtractor;text(Lnet/minecraft/client/gui/Font;Lnet/minecraft/network/chat/Component;III)V"), index = 3)
    private int jobsplus$nameY(int original)
    {
        return this.jobsplus$centerY(original);
    }

    @ModifyArg(method = "extractRenderState", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/PlayerFaceExtractor;extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/resources/Identifier;IIIZZI)V"), index = 3)
    private int jobsplus$faceY(int original)
    {
        return this.jobsplus$centerY(original);
    }

    @ModifyArg(method = "extractRenderState", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;extractPingIcon(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIILnet/minecraft/client/multiplayer/PlayerInfo;)V"), index = 3)
    private int jobsplus$pingY(int original)
    {
        return this.jobsplus$centerY(original);
    }

    @ModifyArg(method = "extractRenderState", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;extractTablistScore(Lnet/minecraft/world/scores/Objective;ILnet/minecraft/client/gui/components/PlayerTabOverlay$ScoreDisplayEntry;IILjava/util/UUID;Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V"), index = 1)
    private int jobsplus$scoreY(int original)
    {
        return this.jobsplus$centerY(original);
    }

    @Unique
    private int jobsplus$centerY(int original)
    {
        return this.jobsplus$hasTitleBadge ? original + TitleType.BADGE_TEXT_OFFSET : original;
    }
}
