package com.autovw.advancednetherite.client.renderer;

import com.autovw.advancednetherite.AdvancedNetherite;
import com.autovw.advancednetherite.client.model.UnicornPetModel;
import com.autovw.advancednetherite.common.entity.DialgaPetEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class UnicornPetRenderer extends MobRenderer<DialgaPetEntity, LivingEntityRenderState, UnicornPetModel>
{
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            AdvancedNetherite.MOD_ID,
            "textures/entity/unicorn_pet.png");

    // 원본 모델이 약 90픽셀(5.6블록) 높이라서 펫 크기(약 1.5블록)로 줄인다.
    private static final float SCALE = 0.27F;

    public UnicornPetRenderer(EntityRendererProvider.Context context)
    {
        super(context, new UnicornPetModel(context.bakeLayer(UnicornPetModel.LAYER_LOCATION)), 0.5F);
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
