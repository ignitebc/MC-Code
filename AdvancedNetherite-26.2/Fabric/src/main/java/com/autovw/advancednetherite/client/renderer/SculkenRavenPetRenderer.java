package com.autovw.advancednetherite.client.renderer;

import com.autovw.advancednetherite.AdvancedNetherite;
import com.autovw.advancednetherite.client.model.SculkenRavenPetModel;
import com.autovw.advancednetherite.common.entity.DialgaPetEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class SculkenRavenPetRenderer extends MobRenderer<DialgaPetEntity, LivingEntityRenderState, SculkenRavenPetModel>
{
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            AdvancedNetherite.MOD_ID,
            "textures/entity/sculken_raven_pet.png");

    // 원본이 약 66픽셀(4.1블록) 높이라서 펫 크기(약 1.2블록)로 줄인다.
    private static final float SCALE = 0.28F;

    public SculkenRavenPetRenderer(EntityRendererProvider.Context context)
    {
        super(context, new SculkenRavenPetModel(context.bakeLayer(SculkenRavenPetModel.LAYER_LOCATION)), 0.6F);
    }

    @Override
    public LivingEntityRenderState createRenderState()
    {
        return new LivingEntityRenderState();
    }

    @Override
    protected void scale(LivingEntityRenderState renderState, PoseStack poseStack)
    {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState renderState)
    {
        return TEXTURE;
    }
}
