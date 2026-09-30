package com.autovw.advancednetherite.client.renderer;

import com.autovw.advancednetherite.common.entity.DialgaPetEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;

/**
 * 모든 펫 렌더러의 공통 부모. 머리 위 이름표를 바닐라보다 작게 그려 펫끼리 구별만 되게 하고,
 * 이름표 아래 줄에 체력 막대를 그린다. 종류별 모델·크기·텍스처는 각 하위 렌더러가 정한다.
 */
public abstract class PetRenderer<M extends EntityModel<LivingEntityRenderState>>
        extends MobRenderer<DialgaPetEntity, LivingEntityRenderState, M>
{
    /** 바닐라 이름표 크기에 곱하는 비율 */
    private static final float NAME_TAG_SCALE = 0.6F;

    protected PetRenderer(EntityRendererProvider.Context context, M model, float shadowRadius)
    {
        super(context, model, shadowRadius);
    }

    /** 체력 막대를 그리려고 모든 펫이 체력을 담는 상태를 쓴다. */
    @Override
    public final LivingEntityRenderState createRenderState()
    {
        return new PetRenderState();
    }

    @Override
    public void extractRenderState(DialgaPetEntity pet, LivingEntityRenderState renderState, float partialTick)
    {
        super.extractRenderState(pet, renderState, partialTick);
        if (renderState instanceof PetRenderState petState)
        {
            petState.health = pet.getHealth();
            petState.maxHealth = pet.getMaxHealth();
        }
    }

    @Override
    protected void submitNameDisplay(LivingEntityRenderState renderState, PoseStack poseStack,
                                     SubmitNodeCollector collector, CameraRenderState camera)
    {
        Vec3 attachment = renderState.nameTagAttachment;
        if (attachment == null)
        {
            super.submitNameDisplay(renderState, poseStack, collector, camera);
            return;
        }
        // 이름표가 붙는 점을 중심으로 줄여야 글자만 작아지고 위치는 그대로 남는다.
        poseStack.pushPose();
        poseStack.translate(attachment.x, attachment.y, attachment.z);
        poseStack.scale(NAME_TAG_SCALE, NAME_TAG_SCALE, NAME_TAG_SCALE);
        poseStack.translate(-attachment.x, -attachment.y, -attachment.z);
        if (renderState instanceof PetRenderState petState && PetHealthBar.isVisible(petState))
        {
            // 바닐라가 점수 줄을 그리는 방식처럼 기준점 줄에 체력을 두고 이름은 한 줄 위로 올린다.
            // 체력을 이름 아래로 내리면 펫 머리와 겹친다.
            PetHealthBar.submit(petState, attachment, poseStack, collector, camera);
            poseStack.translate(0.0F, PetHealthBar.LINE_HEIGHT, 0.0F);
        }
        super.submitNameDisplay(renderState, poseStack, collector, camera);
        poseStack.popPose();
    }
}
