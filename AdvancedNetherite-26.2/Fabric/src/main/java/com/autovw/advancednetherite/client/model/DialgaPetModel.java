package com.autovw.advancednetherite.client.model;

import com.autovw.advancednetherite.AdvancedNetherite;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public class DialgaPetModel extends EntityModel<LivingEntityRenderState>
{
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "dialga_pet"),
            "main");

    private final ModelPart head;
    private final ModelPart tailBase;
    private final ModelPart tailMid;
    private final ModelPart tailTip;
    private final ModelPart frontUpperLeft;
    private final ModelPart frontUpperRight;
    private final ModelPart hindUpperLeft;
    private final ModelPart hindUpperRight;

    public DialgaPetModel(ModelPart root)
    {
        super(root);

        ModelPart body = root.getChild("body");
        ModelPart neck = body.getChild("neck");
        this.head = neck.getChild("head");
        this.tailBase = body.getChild("tail_base");
        this.tailMid = this.tailBase.getChild("tail_mid");
        this.tailTip = this.tailMid.getChild("tail_tip");
        this.frontUpperLeft = body.getChild("front_upper_left");
        this.frontUpperRight = body.getChild("front_upper_right");
        this.hindUpperLeft = body.getChild("hind_upper_left");
        this.hindUpperRight = body.getChild("hind_upper_right");
    }

    public static LayerDefinition createBodyLayer()
    {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();
        PartDefinition body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create().texOffs(156, 3).addBox(-14.0F, -8.0F, -7.0F, 28.0F, 16.0F, 14.0F),
                PartPose.ZERO);

        body.addOrReplaceChild(
                "chest_gem",
                CubeListBuilder.create().texOffs(50, 152).addBox(-4.0F, -4.0F, -9.0F, 8.0F, 8.0F, 2.0F),
                PartPose.ZERO);
        body.addOrReplaceChild(
                "back_plate",
                CubeListBuilder.create().texOffs(156, 152).addBox(-8.0F, -12.0F, -5.0F, 16.0F, 4.0F, 10.0F),
                PartPose.ZERO);
        body.addOrReplaceChild(
                "shoulder_blade_left",
                CubeListBuilder.create().texOffs(126, 152).addBox(-2.0F, -14.0F, -1.0F, 4.0F, 14.0F, 2.0F),
                PartPose.offset(10.0F, -4.0F, -5.0F));
        body.addOrReplaceChild(
                "shoulder_blade_right",
                CubeListBuilder.create().texOffs(141, 152).addBox(-2.0F, -14.0F, -1.0F, 4.0F, 14.0F, 2.0F),
                PartPose.offset(-10.0F, -4.0F, -5.0F));

        PartDefinition neck = body.addOrReplaceChild(
                "neck",
                CubeListBuilder.create().texOffs(113, 3).addBox(-5.0F, -12.0F, -5.0F, 10.0F, 12.0F, 10.0F),
                PartPose.offset(0.0F, -2.0F, -7.0F));
        neck.addOrReplaceChild(
                "neck_blade_left",
                CubeListBuilder.create().texOffs(96, 152).addBox(-2.0F, -12.0F, -1.0F, 4.0F, 12.0F, 2.0F),
                PartPose.offset(6.0F, -1.0F, 0.0F));
        neck.addOrReplaceChild(
                "neck_blade_right",
                CubeListBuilder.create().texOffs(111, 152).addBox(-2.0F, -12.0F, -1.0F, 4.0F, 12.0F, 2.0F),
                PartPose.offset(-6.0F, -1.0F, 0.0F));

        PartDefinition head = neck.addOrReplaceChild(
                "head",
                CubeListBuilder.create().texOffs(3, 3).addBox(-8.0F, -14.0F, -7.0F, 16.0F, 14.0F, 14.0F),
                PartPose.offset(0.0F, -10.0F, -3.0F));
        head.addOrReplaceChild(
                "snout",
                CubeListBuilder.create().texOffs(66, 3).addBox(-6.0F, -6.0F, -10.0F, 12.0F, 6.0F, 10.0F),
                PartPose.offset(0.0F, -2.0F, -7.0F));
        head.addOrReplaceChild(
                "head_crest",
                CubeListBuilder.create().texOffs(73, 152).addBox(-3.0F, -14.0F, -2.0F, 6.0F, 14.0F, 4.0F),
                PartPose.offset(0.0F, -13.0F, 0.0F));
        head.addOrReplaceChild(
                "jaw_plate",
                CubeListBuilder.create().texOffs(3, 191).addBox(-7.0F, 0.0F, -5.0F, 14.0F, 4.0F, 10.0F),
                PartPose.offset(0.0F, -1.0F, -4.0F));

        PartDefinition tailBase = body.addOrReplaceChild(
                "tail_base",
                CubeListBuilder.create().texOffs(3, 66).addBox(-8.0F, -5.0F, 0.0F, 16.0F, 10.0F, 10.0F),
                PartPose.offset(0.0F, 0.0F, 7.0F));
        PartDefinition tailMid = tailBase.addOrReplaceChild(
                "tail_mid",
                CubeListBuilder.create().texOffs(58, 66).addBox(-7.0F, -4.0F, 0.0F, 14.0F, 8.0F, 8.0F),
                PartPose.offset(0.0F, 0.0F, 9.0F));
        tailMid.addOrReplaceChild(
                "tail_tip",
                CubeListBuilder.create().texOffs(105, 66).addBox(-6.0F, -3.0F, 0.0F, 12.0F, 6.0F, 6.0F),
                PartPose.offset(0.0F, 0.0F, 7.0F));

        addFrontLeg(body, "front_upper_left", "front_lower_left", "front_foot_left", 144, 198, 3, 10.0F);
        addFrontLeg(body, "front_upper_right", "front_lower_right", "front_foot_right", 171, 221, 42, -10.0F);
        addHindLeg(body, "hind_upper_left", "hind_lower_left", "hind_foot_left", 81, 151, 205, 9.0F);
        addHindLeg(body, "hind_upper_right", "hind_lower_right", "hind_foot_right", 116, 178, 3, -9.0F);

        return LayerDefinition.create(meshDefinition, 256, 256);
    }

    private static void addFrontLeg(
            PartDefinition body,
            String upperName,
            String lowerName,
            String footName,
            int upperU,
            int lowerU,
            int footU,
            float x)
    {
        PartDefinition upper = body.addOrReplaceChild(
                upperName,
                CubeListBuilder.create().texOffs(upperU, 66).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 12.0F, 6.0F),
                PartPose.offset(x, -1.0F, -5.0F));
        PartDefinition lower = upper.addOrReplaceChild(
                lowerName,
                CubeListBuilder.create().texOffs(lowerU, 66).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 10.0F, 5.0F),
                PartPose.offset(0.0F, 10.0F, 0.0F));
        lower.addOrReplaceChild(
                footName,
                CubeListBuilder.create().texOffs(footU, 109).addBox(-4.0F, 0.0F, -7.0F, 8.0F, 4.0F, 10.0F),
                PartPose.offset(0.0F, 9.0F, -1.0F));
    }

    private static void addHindLeg(
            PartDefinition body,
            String upperName,
            String lowerName,
            String footName,
            int upperU,
            int lowerU,
            int footU,
            float x)
    {
        PartDefinition upper = body.addOrReplaceChild(
                upperName,
                CubeListBuilder.create().texOffs(upperU, 109).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 12.0F, 8.0F),
                PartPose.offset(x, -1.0F, 5.0F));
        PartDefinition lower = upper.addOrReplaceChild(
                lowerName,
                CubeListBuilder.create().texOffs(lowerU, 109).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 10.0F, 6.0F),
                PartPose.offset(0.0F, 10.0F, 0.0F));
        lower.addOrReplaceChild(
                footName,
                CubeListBuilder.create().texOffs(footU, footU == 3 ? 152 : 109).addBox(-5.0F, 0.0F, -8.0F, 10.0F, 5.0F, 12.0F),
                PartPose.offset(0.0F, 9.0F, -1.0F));
    }

    @Override
    public void setupAnim(LivingEntityRenderState renderState)
    {
        super.setupAnim(renderState);

        this.head.xRot = renderState.xRot * Mth.DEG_TO_RAD;
        this.head.yRot = renderState.yRot * Mth.DEG_TO_RAD;

        float walkPosition = renderState.walkAnimationPos * 0.6662F;
        float walkSpeed = renderState.walkAnimationSpeed;
        this.frontUpperLeft.xRot = Mth.cos(walkPosition) * 1.2F * walkSpeed;
        this.frontUpperRight.xRot = Mth.cos(walkPosition + Mth.PI) * 1.2F * walkSpeed;
        this.hindUpperLeft.xRot = Mth.cos(walkPosition + Mth.PI) * 1.2F * walkSpeed;
        this.hindUpperRight.xRot = Mth.cos(walkPosition) * 1.2F * walkSpeed;

        float tailMovement = Mth.sin(renderState.ageInTicks * 0.12F) * 0.12F;
        this.tailBase.yRot = tailMovement;
        this.tailMid.yRot = tailMovement * 1.5F;
        this.tailTip.yRot = tailMovement * 2.0F;
    }
}
