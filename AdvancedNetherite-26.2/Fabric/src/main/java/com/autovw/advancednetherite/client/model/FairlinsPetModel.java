package com.autovw.advancednetherite.client.model;

import com.autovw.advancednetherite.AdvancedNetherite;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * 레전더리 펫: 요정 3인 세트(기본·대장장이·채집꾼, fairlins.bbmodel).
 * 텍스처는 원본 5장을 하나의 128x128 아틀라스로 합쳐 사용한다.
 */
public class FairlinsPetModel extends EntityModel<LivingEntityRenderState>
{
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "fairlins_pet"),
            "main");

    private final Fairy green;
    private final Fairy smith;
    private final Fairy forager;

    public FairlinsPetModel(ModelPart root)
    {
        super(root);

        this.green = new Fairy(root.getChild("fairy_green"), "fairy_green", 0.0F);
        this.smith = new Fairy(root.getChild("fairy_smith"), "fairy_smith", 2.1F);
        this.forager = new Fairy(root.getChild("fairy_forager"), "fairy_forager", 4.2F);
    }

    public static LayerDefinition createBodyLayer()
    {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        PartDefinition fairy_green = root.addOrReplaceChild(
                "fairy_green",
                CubeListBuilder.create()
                        .texOffs(0, 9).addBox(-2.00F, -3.00F, -1.00F, 4.00F, 3.00F, 2.00F)
                        .texOffs(2, 18).addBox(-2.00F, -1.00F, -1.00F, 4.00F, 1.00F, 2.00F, new CubeDeformation(0.20F))
                        .texOffs(0, 21).addBox(-2.00F, 0.00F, -1.00F, 4.00F, 1.00F, 2.00F)
                        .texOffs(0, 15).addBox(2.00F, -3.00F, -0.50F, 1.00F, 3.00F, 1.00F)
                        .texOffs(22, 0).addBox(-1.50F, -3.00F, 1.00F, 3.00F, 3.00F, 2.00F, new CubeDeformation(0.20F))
                        .texOffs(13, 9).addBox(-3.00F, -3.00F, -0.50F, 1.00F, 3.00F, 1.00F)
                        .texOffs(96, 0).addBox(-3.00F, 0.00F, -7.00F, 1.00F, 1.00F, 8.00F),
                PartPose.offset(0.00F, 22.00F, -4.00F));
        fairy_green.addOrReplaceChild(
                "fairy_green_leg_0",
                CubeListBuilder.create()
                        .texOffs(17, 0).addBox(-0.50F, 0.00F, -0.50F, 1.00F, 2.00F, 1.00F),
                PartPose.offset(-1.00F, 0.00F, 0.00F));
        fairy_green.addOrReplaceChild(
                "fairy_green_leg_1",
                CubeListBuilder.create()
                        .texOffs(17, 4).addBox(-0.50F, 0.00F, -0.50F, 1.00F, 2.00F, 1.00F),
                PartPose.offset(1.00F, 0.00F, 0.00F));
        PartDefinition fairy_green_head = fairy_green.addOrReplaceChild(
                "fairy_green_head",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.00F, -4.00F, -2.00F, 4.00F, 4.00F, 4.00F, new CubeDeformation(0.01F))
                        .texOffs(15, 14).addBox(-2.00F, -4.00F, -2.00F, 4.00F, 4.00F, 4.00F, new CubeDeformation(0.20F)),
                PartPose.offset(0.00F, -3.00F, 0.00F));
        fairy_green_head.addOrReplaceChild(
                "fairy_green_wing_0",
                CubeListBuilder.create()
                        .texOffs(5, 15).addBox(-3.00F, -1.00F, 0.00F, 3.00F, 2.00F, 0.00F),
                PartPose.offset(-2.00F, -2.00F, 0.00F));
        fairy_green_head.addOrReplaceChild(
                "fairy_green_wing_1",
                CubeListBuilder.create()
                        .texOffs(12, 15).addBox(0.00F, -1.00F, 0.00F, 3.00F, 2.00F, 0.00F),
                PartPose.offset(2.00F, -2.00F, 0.00F));

        PartDefinition fairy_smith = root.addOrReplaceChild(
                "fairy_smith",
                CubeListBuilder.create()
                        .texOffs(0, 51).addBox(-2.00F, -3.00F, -1.00F, 4.00F, 3.00F, 2.00F)
                        .texOffs(19, 32).addBox(-2.00F, -1.00F, -1.00F, 4.00F, 1.00F, 2.00F, new CubeDeformation(0.20F))
                        .texOffs(19, 36).addBox(-2.00F, 0.00F, -1.00F, 4.00F, 1.00F, 2.00F)
                        .texOffs(24, 51).addBox(2.00F, -3.00F, -0.50F, 1.00F, 3.00F, 1.00F)
                        .texOffs(13, 51).addBox(-1.50F, -3.00F, 1.00F, 3.00F, 3.00F, 2.00F, new CubeDeformation(0.20F))
                        .texOffs(7, 57).addBox(-3.00F, -3.00F, -0.50F, 1.00F, 3.00F, 1.00F)
                        .texOffs(6, 37).addBox(-2.50F, 0.00F, -2.00F, 0.00F, 1.00F, 3.00F)
                        .texOffs(0, 60).addBox(-3.50F, -1.00F, -4.00F, 2.00F, 3.00F, 2.00F),
                PartPose.offset(7.00F, 22.00F, 4.00F));
        fairy_smith.addOrReplaceChild(
                "fairy_smith_leg_0",
                CubeListBuilder.create()
                        .texOffs(12, 57).addBox(-0.50F, 0.00F, -0.50F, 1.00F, 2.00F, 1.00F),
                PartPose.offset(-1.00F, 0.00F, 0.00F));
        fairy_smith.addOrReplaceChild(
                "fairy_smith_leg_1",
                CubeListBuilder.create()
                        .texOffs(17, 57).addBox(-0.50F, 0.00F, -0.50F, 1.00F, 2.00F, 1.00F),
                PartPose.offset(1.00F, 0.00F, 0.00F));
        PartDefinition fairy_smith_head = fairy_smith.addOrReplaceChild(
                "fairy_smith_head",
                CubeListBuilder.create()
                        .texOffs(0, 42).addBox(-2.00F, -4.00F, -2.00F, 4.00F, 4.00F, 4.00F),
                PartPose.offset(0.00F, -3.00F, 0.00F));
        fairy_smith_head.addOrReplaceChild(
                "fairy_smith_wing_0",
                CubeListBuilder.create()
                        .texOffs(24, 56).addBox(-3.00F, -1.00F, 0.00F, 3.00F, 2.00F, 0.00F),
                PartPose.offset(-2.00F, -2.00F, 0.00F));
        fairy_smith_head.addOrReplaceChild(
                "fairy_smith_wing_1",
                CubeListBuilder.create()
                        .texOffs(0, 57).addBox(0.00F, -1.00F, 0.00F, 3.00F, 2.00F, 0.00F),
                PartPose.offset(2.00F, -2.00F, 0.00F));

        PartDefinition fairy_forager = root.addOrReplaceChild(
                "fairy_forager",
                CubeListBuilder.create()
                        .texOffs(32, 8).addBox(-2.00F, -3.00F, -1.00F, 4.00F, 3.00F, 2.00F)
                        .texOffs(44, 8).addBox(-2.00F, -1.00F, -1.00F, 4.00F, 1.00F, 2.00F, new CubeDeformation(0.20F))
                        .texOffs(44, 11).addBox(-2.00F, 0.00F, -1.00F, 4.00F, 1.00F, 2.00F)
                        .texOffs(32, 13).addBox(2.00F, -3.00F, -0.50F, 1.00F, 3.00F, 1.00F)
                        .texOffs(13, 51).addBox(-1.50F, -3.00F, 1.00F, 3.00F, 3.00F, 2.00F, new CubeDeformation(0.20F))
                        .texOffs(36, 15).addBox(-3.00F, -3.00F, -0.50F, 1.00F, 3.00F, 1.00F)
                        .texOffs(64, 7).addBox(-3.00F, -3.00F, -7.00F, 1.00F, 3.00F, 6.00F)
                        .texOffs(84, 0).addBox(-2.00F, -2.00F, -6.00F, 4.00F, 0.00F, 4.00F)
                        .texOffs(78, 7).addBox(-2.00F, -3.00F, -7.00F, 4.00F, 3.00F, 1.00F)
                        .texOffs(64, 0).addBox(-3.00F, 0.00F, -7.00F, 6.00F, 1.00F, 6.00F)
                        .texOffs(64, 7).addBox(2.00F, -3.00F, -7.00F, 1.00F, 3.00F, 6.00F)
                        .texOffs(78, 7).addBox(-2.00F, -3.00F, -2.00F, 4.00F, 3.00F, 1.00F),
                PartPose.offset(-7.00F, 22.00F, 1.00F));
        fairy_forager.addOrReplaceChild(
                "fairy_forager_leg_0",
                CubeListBuilder.create()
                        .texOffs(48, 0).addBox(-0.50F, 0.00F, -0.50F, 1.00F, 2.00F, 1.00F),
                PartPose.offset(-1.00F, 0.00F, 0.00F));
        fairy_forager.addOrReplaceChild(
                "fairy_forager_leg_1",
                CubeListBuilder.create()
                        .texOffs(48, 3).addBox(-0.50F, 0.00F, -0.50F, 1.00F, 2.00F, 1.00F),
                PartPose.offset(1.00F, 0.00F, 0.00F));
        PartDefinition fairy_forager_head = fairy_forager.addOrReplaceChild(
                "fairy_forager_head",
                CubeListBuilder.create()
                        .texOffs(32, 0).addBox(-2.00F, -4.00F, -2.00F, 4.00F, 4.00F, 4.00F),
                PartPose.offset(0.00F, -3.00F, 0.00F));
        fairy_forager_head.addOrReplaceChild(
                "fairy_forager_wing_0",
                CubeListBuilder.create()
                        .texOffs(36, 13).addBox(-3.00F, -1.00F, 0.00F, 3.00F, 2.00F, 0.00F),
                PartPose.offset(-2.00F, -2.00F, 0.00F));
        fairy_forager_head.addOrReplaceChild(
                "fairy_forager_wing_1",
                CubeListBuilder.create()
                        .texOffs(42, 14).addBox(0.00F, -1.00F, 0.00F, 3.00F, 2.00F, 0.00F),
                PartPose.offset(2.00F, -2.00F, 0.00F));

        return LayerDefinition.create(meshDefinition, 128, 128);
    }

    @Override
    public void setupAnim(LivingEntityRenderState renderState)
    {
        super.setupAnim(renderState);

        float walkPosition = renderState.walkAnimationPos * 0.6662F;
        float walkSpeed = renderState.walkAnimationSpeed;
        this.green.animate(renderState, walkPosition, walkSpeed);
        this.smith.animate(renderState, walkPosition, walkSpeed);
        this.forager.animate(renderState, walkPosition, walkSpeed);
    }

    /** 요정 한 마리의 파트 묶음. 위상(phase)을 달리해 세 마리가 제각각 움직이게 한다. */
    private static final class Fairy
    {
        private final ModelPart body;
        private final ModelPart head;
        private final ModelPart wingLeft;
        private final ModelPart wingRight;
        private final ModelPart legLeft;
        private final ModelPart legRight;
        private final float phase;

        private Fairy(ModelPart body, String prefix, float phase)
        {
            this.body = body;
            this.head = body.getChild(prefix + "_head");
            this.wingLeft = this.head.getChild(prefix + "_wing_0");
            this.wingRight = this.head.getChild(prefix + "_wing_1");
            this.legLeft = body.getChild(prefix + "_leg_0");
            this.legRight = body.getChild(prefix + "_leg_1");
            this.phase = phase;
        }

        private void animate(LivingEntityRenderState renderState, float walkPosition, float walkSpeed)
        {
            float time = renderState.ageInTicks + this.phase * 10.0F;

            // 부유: 세 마리가 서로 다른 박자로 오르내린다.
            this.body.y += Mth.sin(time * 0.15F) * 0.6F;

            this.head.xRot = renderState.xRot * Mth.DEG_TO_RAD * 0.5F;
            this.head.yRot = renderState.yRot * Mth.DEG_TO_RAD * 0.5F;

            float flap = Mth.sin(time * 1.1F) * 0.7F;
            this.wingLeft.yRot = -0.3F - flap;
            this.wingRight.yRot = 0.3F + flap;

            this.legLeft.xRot = Mth.cos(walkPosition + this.phase) * 0.9F * walkSpeed;
            this.legRight.xRot = Mth.cos(walkPosition + this.phase + Mth.PI) * 0.9F * walkSpeed;
        }
    }
}
