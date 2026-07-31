package com.autovw.advancednetherite.client.renderer;

import com.autovw.advancednetherite.AdvancedNetherite;
import com.autovw.advancednetherite.client.model.KirbyPetModel;
import com.autovw.advancednetherite.common.entity.DialgaPetEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class KirbyPetRenderer extends MobRenderer<DialgaPetEntity, LivingEntityRenderState, KirbyPetModel>
{
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            AdvancedNetherite.MOD_ID,
            "textures/entity/kirby_pet.png");

    // 원본이 약 20픽셀(1.25블록) 높이라서 아담한 펫 크기(약 0.7블록)로 줄인다.
    private static final float SCALE = 0.55F;

    public KirbyPetRenderer(EntityRendererProvider.Context context)
    {
        super(context, new KirbyPetModel(context.bakeLayer(KirbyPetModel.LAYER_LOCATION)), 0.3F);
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
