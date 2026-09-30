package com.autovw.advancednetherite.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;

/**
 * 펫 머리 위 이름표 아래 줄에 그리는 체력 막대와 "현재/최대" 숫자.
 *
 * <p>바닐라가 점수 줄을 그리는 자리(이름표 기준점)에 그리고, 이름은 그 위 줄로 올린다.
 * 막대는 바닐라 이름표 배경과 같은 렌더 타입으로 그려 벽에 가려지며, 숫자도 가려지는 글자로 그린다.
 */
public final class PetHealthBar
{
    /** 이름표 한 줄의 높이(월드 단위). 바닐라가 점수 줄 위로 이름을 올릴 때 쓰는 값과 같다. */
    public static final float LINE_HEIGHT = 9.0F * 1.15F * 0.025F;

    /** 체력을 보여 주는 최대 거리(블록). 몬스터 레벨 표시와 같다. */
    private static final double MAX_DISTANCE = 32.0D;
    /** 바닐라 이름표와 같은 글자 크기와 기준점 높이 */
    private static final float TEXT_SCALE = 0.025F;
    private static final double ATTACHMENT_OFFSET_Y = 0.5D;

    /** 막대 크기와 숫자와의 간격(글자 픽셀 단위) */
    private static final float BAR_WIDTH = 32.0F;
    private static final float BAR_HEIGHT = 4.0F;
    private static final float BAR_TEXT_GAP = 3.0F;

    /** 남은 체력 비율에 따른 막대 색. 바닐라 초록·노랑·빨강 글자색과 같다. */
    private static final float HIGH_HEALTH_RATIO = 0.5F;
    private static final float LOW_HEALTH_RATIO = 0.25F;
    private static final int HIGH_HEALTH_COLOR = 0xFF55FF55;
    private static final int MID_HEALTH_COLOR = 0xFFFFFF55;
    private static final int LOW_HEALTH_COLOR = 0xFFFF5555;
    private static final int EMPTY_COLOR = 0xC0202020;

    /** 바닐라 이름표처럼 접근성의 글자 배경 불투명도 설정을 따른다. */
    private static final float DEFAULT_BACKGROUND_OPACITY = 0.25F;
    private static final int BACKGROUND_BLACK = 0xFF000000;
    /** 바닐라 이름표가 가려지지 않은 글자에 더하는 발광 세기 */
    private static final int TEXT_EMISSION = 2;
    private static final int TEXT_WHITE = 0xFFFFFFFF;
    private static final int NO_OUTLINE = 0;

    private PetHealthBar()
    {
    }

    /** 가깝고 보이는 살아 있는 펫에만 그린다. 이름표가 없으면(F1 등) 호출되지 않는다. */
    public static boolean isVisible(PetRenderState state)
    {
        boolean hasHealth = state.health > 0.0F && state.maxHealth > 0.0F;
        boolean nearby = state.distanceToCameraSq < MAX_DISTANCE * MAX_DISTANCE;
        return hasHealth && nearby && !state.isInvisibleToPlayer;
    }

    /** 이름표 기준점 줄에 막대와 숫자를 가운데 맞춰 그린다. 엔티티 위치로 옮겨진 좌표계에서 불린다. */
    public static void submit(PetRenderState state, Vec3 attachment, PoseStack poseStack,
                              SubmitNodeCollector collector, CameraRenderState camera)
    {
        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        Component text = Component.literal(formatHealth(state));
        float lineWidth = BAR_WIDTH + BAR_TEXT_GAP + font.width(text);
        float barLeft = -lineWidth / 2.0F;
        float barRight = barLeft + BAR_WIDTH;
        float barTop = (font.lineHeight - BAR_HEIGHT) / 2.0F;
        float barBottom = barTop + BAR_HEIGHT;
        float fillRight = barLeft + BAR_WIDTH * healthRatio(state);
        int fillColor = fillColor(healthRatio(state));
        int light = LightCoordsUtil.lightCoordsWithEmission(state.lightCoords, TEXT_EMISSION);
        float backgroundOpacity = minecraft.options.getBackgroundOpacity(DEFAULT_BACKGROUND_OPACITY);
        int backgroundColor = ARGB.color(backgroundOpacity, BACKGROUND_BLACK);

        poseStack.pushPose();
        poseStack.translate(attachment.x, attachment.y + ATTACHMENT_OFFSET_Y, attachment.z);
        poseStack.mulPose(camera.orientation);
        poseStack.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);
        // 찬 부분과 빈 부분을 겹치지 않게 나란히 그려 같은 평면에서 깜빡이지 않게 한다.
        collector.submitCustomGeometry(poseStack, RenderTypes.textBackground(), (pose, consumer) -> {
            addQuad(pose, consumer, barLeft, barTop, fillRight, barBottom, fillColor, light);
            addQuad(pose, consumer, fillRight, barTop, barRight, barBottom, EMPTY_COLOR, light);
        });
        collector.submitText(poseStack, barRight + BAR_TEXT_GAP, 0.0F, text.getVisualOrderText(), false,
                Font.DisplayMode.NORMAL, light, TEXT_WHITE, backgroundColor, NO_OUTLINE);
        poseStack.popPose();
    }

    /** 1 미만으로 남은 체력이 0으로 보이지 않도록 현재 체력은 올림한다. */
    private static String formatHealth(PetRenderState state)
    {
        int current = (int) Math.ceil(state.health);
        int max = Math.round(state.maxHealth);
        return current + "/" + max;
    }

    private static float healthRatio(PetRenderState state)
    {
        float ratio = state.health / state.maxHealth;
        return Math.max(0.0F, Math.min(1.0F, ratio));
    }

    private static int fillColor(float ratio)
    {
        if (ratio >= HIGH_HEALTH_RATIO)
        {
            return HIGH_HEALTH_COLOR;
        }
        if (ratio >= LOW_HEALTH_RATIO)
        {
            return MID_HEALTH_COLOR;
        }
        return LOW_HEALTH_COLOR;
    }

    /** 바닐라 글자 배경과 같은 정점 순서로 사각형을 넣는다. 순서가 다르면 뒷면으로 잘려 보이지 않는다. */
    private static void addQuad(PoseStack.Pose pose, VertexConsumer consumer, float left, float top,
                                float right, float bottom, int color, int light)
    {
        if (right <= left)
        {
            return;
        }
        consumer.addVertex(pose, left, bottom, 0.0F).setColor(color).setLight(light);
        consumer.addVertex(pose, right, bottom, 0.0F).setColor(color).setLight(light);
        consumer.addVertex(pose, right, top, 0.0F).setColor(color).setLight(light);
        consumer.addVertex(pose, left, top, 0.0F).setColor(color).setLight(light);
    }
}
