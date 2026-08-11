package com.autovw.advancednetherite.client.renderer;

import com.autovw.advancednetherite.AdvancedNetherite;
import com.autovw.advancednetherite.client.model.SuperGomiPetModel;
import com.autovw.advancednetherite.common.entity.DialgaPetEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class SuperGomiPetRenderer extends MobRenderer<DialgaPetEntity, LivingEntityRenderState, SuperGomiPetModel>
{
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            AdvancedNetherite.MOD_ID,
            "textures/entity/super_gomi_pet.png");

    public SuperGomiPetRenderer(EntityRendererProvider.Context context)
    {
        super(context, new SuperGomiPetModel(context.bakeLayer(SuperGomiPetModel.LAYER_LOCATION)), 0.45F);
    }

    @Override
    public LivingEntityRenderState createRenderState()
    {
        return new LivingEntityRenderState();
    }

    @Override
    protected void scale(LivingEntityRenderState renderState, PoseStack poseStack)
    {
        // 모델 좌표는 1.0 기준으로 잡았으므로 최종 크기는 여기서만 조정한다
        poseStack.scale(0.75F, 0.75F, 0.75F);
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState renderState)
    {
        return TEXTURE;
    }
}
