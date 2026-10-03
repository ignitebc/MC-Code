package com.daqem.jobsplus.client.hyper;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;

/** 플레이어 외곽을 감싸는 얇은 반투명 타원막. 텍스처·파티클을 매 틱 생성하지 않는다. */
public final class HyperShieldRenderer
{
    private static final int SEGMENTS = 24;
    private static final int BANDS = 12;

    private HyperShieldRenderer() {}

    public static void submit(EntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector)
    {
        byte visual = ((HyperShieldRenderState) state).jobsplus$getShieldVisual();
        if (visual == 0 || state.isInvisible) return;
        float pulse = (float) Math.sin(state.ageInTicks * 0.16F);
        float radius = state.boundingBoxWidth * 0.5F + 0.17F + pulse * 0.008F;
        float height = state.boundingBoxHeight + 0.2F;
        int alpha = visual == 2 ? 39 : 16;
        int color = alpha << 24 | (visual == 2 ? 0x8AE9FF : 0x46CDBD);
        collector.submitCustomGeometry(poseStack, RenderTypes.debugQuads(), (pose, buffer) -> {
            for (int band = 0; band < BANDS; band++)
            {
                double latitude0 = -Math.PI / 2 + Math.PI * band / BANDS;
                double latitude1 = -Math.PI / 2 + Math.PI * (band + 1) / BANDS;
                for (int segment = 0; segment < SEGMENTS; segment++)
                {
                    double longitude0 = Math.PI * 2 * segment / SEGMENTS;
                    double longitude1 = Math.PI * 2 * (segment + 1) / SEGMENTS;
                    vertex(buffer, pose, radius, height, latitude0, longitude0, color);
                    vertex(buffer, pose, radius, height, latitude1, longitude0, color);
                    vertex(buffer, pose, radius, height, latitude1, longitude1, color);
                    vertex(buffer, pose, radius, height, latitude0, longitude1, color);
                }
            }
        });
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float radius, float height,
                                double latitude, double longitude, int color)
    {
        float x = (float) (radius * Math.cos(latitude) * Math.cos(longitude));
        float z = (float) (radius * Math.cos(latitude) * Math.sin(longitude));
        float y = (float) (height * 0.5 * (1 + Math.sin(latitude))) - 0.1F;
        buffer.addVertex(pose, x, y, z).setColor(color);
    }
}
