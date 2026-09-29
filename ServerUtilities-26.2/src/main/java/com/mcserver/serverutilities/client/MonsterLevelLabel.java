package com.mcserver.serverutilities.client;

import com.mcserver.serverutilities.monster.MonsterEquipmentAccess;
import com.mcserver.serverutilities.monster.MonsterLevel;
import com.mcserver.serverutilities.monster.MonsterLevelPayload;
import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.phys.Vec3;

/**
 * 몬스터 머리 위의 레벨 표시.
 *
 * <p>바닐라 이름표는 벽 너머로 비쳐 보이거나, 가려지는 대신 반투명하게만 그려진다. 레벨은 동굴 속
 * 몬스터의 위치를 드러내지 않으면서 또렷해야 하므로 이름표와 같은 위치·크기로 불투명하게 따로 그린다.
 * 바닐라 이름표나 점수 줄이 있으면 그 위 줄에 놓는다.
 */
public final class MonsterLevelLabel {
    /** 레벨을 보여 주는 최대 거리(블록) */
    private static final double MAX_DISTANCE = 32.0D;
    /** 이름표 한 줄의 높이(월드 단위). 바닐라가 점수 줄 위로 이름을 올릴 때 쓰는 값과 같다. */
    private static final float LINE_HEIGHT = 9.0F * 1.15F * 0.025F;
    /** 바닐라 이름표와 같은 글자 크기와 기준점 높이 */
    private static final float TEXT_SCALE = 0.025F;
    private static final double ATTACHMENT_OFFSET_Y = 0.5D;
    /** 바닐라 이름표처럼 접근성의 글자 배경 불투명도 설정을 따른다. */
    private static final float DEFAULT_BACKGROUND_OPACITY = 0.25F;
    private static final int BACKGROUND_BLACK = 0xFF000000;
    /** 바닐라 이름표가 가려지지 않은 글자에 더하는 발광 세기 */
    private static final int TEXT_EMISSION = 2;
    private static final int TEXT_WHITE = 0xFFFFFFFF;
    private static final int NO_OUTLINE = 0;
    /** LV1~LV7 색. 총기·부착물 이름의 F~S 등급 색과 같은 순서다. */
    private static final ChatFormatting[] LEVEL_COLORS = {
            ChatFormatting.GREEN, ChatFormatting.AQUA, ChatFormatting.BLUE, ChatFormatting.DARK_PURPLE,
            ChatFormatting.YELLOW, ChatFormatting.RED, ChatFormatting.GOLD
    };

    private MonsterLevelLabel() { }

    /** 서버가 보낸 레벨을 해당 몬스터에 기록한다. 렌더 스레드에서 불린다. */
    public static void receive(MonsterLevelPayload payload, ClientPlayNetworking.Context context) {
        ClientLevel level = context.client().level;
        if (level == null) return;
        if (level.getEntity(payload.entityId()) instanceof MonsterEquipmentAccess monster) {
            monster.serverutilities$setMonsterLevel(payload.level());
        }
    }

    /** 이번 프레임에 그릴 레벨과 위치를 렌더 상태에 담는다. 상태 객체가 재사용될 수 있어 매번 덮어쓴다. */
    public static void extract(Entity entity, EntityRenderState state, float partialTick) {
        MonsterLevelRenderState labelState = (MonsterLevelRenderState) state;
        int level = visibleLevel(entity, state);
        Vec3 attachment = null;
        if (level != MonsterLevel.NONE) {
            attachment = entity.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, entity.getYRot(partialTick));
        }
        if (attachment == null) {
            level = MonsterLevel.NONE;
        }
        labelState.serverutilities$setLabel(level, attachment);
    }

    /** 렌더 상태에 담긴 레벨을 그린다. 엔티티 위치로 옮겨진 좌표계에서 불린다. */
    public static void submit(EntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                              CameraRenderState camera) {
        MonsterLevelRenderState labelState = (MonsterLevelRenderState) state;
        int level = labelState.serverutilities$labelLevel();
        Vec3 attachment = labelState.serverutilities$labelAttachment();
        if (level == MonsterLevel.NONE || attachment == null) return;

        Minecraft minecraft = Minecraft.getInstance();
        Component text = text(level);
        float x = -minecraft.font.width(text) / 2.0F;
        float backgroundOpacity = minecraft.options.getBackgroundOpacity(DEFAULT_BACKGROUND_OPACITY);
        int backgroundColor = ARGB.color(backgroundOpacity, BACKGROUND_BLACK);
        int light = LightCoordsUtil.lightCoordsWithEmission(state.lightCoords, TEXT_EMISSION);

        poseStack.pushPose();
        // 바닐라처럼 아래 줄 수만큼 월드 위쪽으로 먼저 올린 뒤 이름표 기준점에서 카메라를 향하게 한다.
        poseStack.translate(0.0F, LINE_HEIGHT * linesBelow(state), 0.0F);
        poseStack.translate(attachment.x, attachment.y + ATTACHMENT_OFFSET_Y, attachment.z);
        poseStack.mulPose(camera.orientation);
        poseStack.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);
        // NORMAL은 깊이 검사를 하므로 벽에 가려진다.
        collector.submitText(poseStack, x, 0.0F, text.getVisualOrderText(), false, Font.DisplayMode.NORMAL,
                light, TEXT_WHITE, backgroundColor, NO_OUTLINE);
        poseStack.popPose();
    }

    private static int visibleLevel(Entity entity, EntityRenderState state) {
        if (!(entity instanceof MonsterEquipmentAccess monster)) return MonsterLevel.NONE;
        int level = monster.serverutilities$monsterLevel();
        if (!MonsterLevel.isVisible(level)) return MonsterLevel.NONE;

        Minecraft minecraft = Minecraft.getInstance();
        boolean hudHidden = minecraft.gui.hud.isHidden();
        boolean tooFar = state.distanceToCameraSq >= MAX_DISTANCE * MAX_DISTANCE;
        boolean invisibleToPlayer = minecraft.player != null && entity.isInvisibleTo(minecraft.player);
        if (hudHidden || tooFar || invisibleToPlayer) return MonsterLevel.NONE;
        return level;
    }

    /** 레벨 아래에 놓이는 바닐라 줄 수. 점수 줄과 이름표가 각각 한 줄씩 차지한다. */
    private static int linesBelow(EntityRenderState state) {
        int lines = 0;
        if (state.scoreText != null) lines++;
        if (state.nameTag != null) lines++;
        return lines;
    }

    private static Component text(int level) {
        ChatFormatting color = LEVEL_COLORS[level - MonsterLevel.MIN_SCORE];
        return Component.literal("LV" + level).withStyle(color);
    }
}
