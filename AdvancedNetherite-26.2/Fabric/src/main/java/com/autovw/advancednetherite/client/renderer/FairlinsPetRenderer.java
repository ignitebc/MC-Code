package com.autovw.advancednetherite.client.renderer;

import com.autovw.advancednetherite.AdvancedNetherite;
import com.autovw.advancednetherite.client.model.FairlinsPetModel;
import com.autovw.advancednetherite.common.entity.DialgaPetEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

public class FairlinsPetRenderer extends MobRenderer<DialgaPetEntity, LivingEntityRenderState, FairlinsPetModel>
{
    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            AdvancedNetherite.MOD_ID,
            "textures/entity/fairlins_pet.png");

    private static final float SCALE = 1.0F;

    public FairlinsPetRenderer(EntityRendererProvider.Context context)
    {
        super(context, new FairlinsPetModel(context.bakeLayer(FairlinsPetModel.LAYER_LOCATION)), 0.5F);
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
