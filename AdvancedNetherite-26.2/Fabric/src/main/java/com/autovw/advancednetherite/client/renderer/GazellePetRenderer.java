package com.autovw.advancednetherite.client.renderer;

import com.autovw.advancednetherite.AdvancedNetherite;
import com.autovw.advancednetherite.client.model.GazellePetModel;
import com.autovw.advancednetherite.common.entity.DialgaPetEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class GazellePetRenderer extends MobRenderer<DialgaPetEntity, LivingEntityRenderState, GazellePetModel>
{
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            AdvancedNetherite.MOD_ID,
            "textures/entity/gazelle_pet.png");

    // 원본이 약 24.5픽셀(1.5블록) 높이라서 펫 크기(약 1.1블록)로 줄인다.
    private static final float SCALE = 0.75F;

    public GazellePetRenderer(EntityRendererProvider.Context context)
    {
        super(context, new GazellePetModel(context.bakeLayer(GazellePetModel.LAYER_LOCATION)), 0.4F);
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
