package com.autovw.advancednetherite.client.renderer;

import com.autovw.advancednetherite.AdvancedNetherite;
import com.autovw.advancednetherite.client.model.DialgaPetModel;
import com.autovw.advancednetherite.common.entity.DialgaPetEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;

public class DialgaPetRenderer extends MobRenderer<DialgaPetEntity, LivingEntityRenderState, DialgaPetModel>
{
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            AdvancedNetherite.MOD_ID,
            "textures/entity/dialga_pet.png");

    public DialgaPetRenderer(EntityRendererProvider.Context context)
    {
        super(context, new DialgaPetModel(context.bakeLayer(DialgaPetModel.LAYER_LOCATION)), 0.35F);
    }

    @Override
    public LivingEntityRenderState createRenderState()
    {
        return new LivingEntityRenderState();
    }

    @Override
    protected void scale(LivingEntityRenderState renderState, PoseStack poseStack)
    {
        poseStack.scale(0.35F, 0.35F, 0.35F);
    }

    @Override
    public ResourceLocation getTextureLocation(LivingEntityRenderState renderState)
    {
        return TEXTURE;
    }
}
