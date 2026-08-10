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

public class GomiPetModel extends EntityModel<LivingEntityRenderState>
{
    private static final int TEXTURE_SIZE = 512;

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "gomi_pet"),
            "main");
    public static final ModelLayerLocation SUPER_LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "super_gomi_pet"),
            "main");

    private final ModelPart head;
    private final ModelPart frontLeftLeg;
    private final ModelPart frontRightLeg;
    private final ModelPart backLeftLeg;
    private final ModelPart backRightLeg;
    private final ModelPart tailBase;
    private final ModelPart tailTip;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart cape;

    public GomiPetModel(ModelPart root)
    {
        super(root);
        ModelPart body = root.getChild("body");
        this.head = body.getChild("head");
        this.frontLeftLeg = body.getChild("front_left_leg");
        this.frontRightLeg = body.getChild("front_right_leg");
        this.backLeftLeg = body.getChild("back_left_leg");
        this.backRightLeg = body.getChild("back_right_leg");
        this.tailBase = body.getChild("tail_base");
        this.tailTip = this.tailBase.getChild("tail_tip");
        this.leftWing = body.getChild("left_wing");
        this.rightWing = body.getChild("right_wing");
        this.cape = body.getChild("cape");
    }

    public static LayerDefinition createBodyLayer()
    {
        return createLayer(false);
    }

    public static LayerDefinition createSuperBodyLayer()
    {
        return createLayer(true);
    }

    private static LayerDefinition createLayer(boolean superForm)
    {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        int tanU = 0;
        int tanV = 0;
        int lightTanU = 128;
        int lightTanV = 0;
        int creamU = 256;
        int creamV = 0;
        int darkTanU = 384;
        int darkTanV = 0;
        int blackU = 256;
        int blackV = superForm ? 256 : 128;
        int whiteU = 384;
        int whiteV = superForm ? 256 : 128;
        int pinkU = 0;
        int pinkV = superForm ? 384 : 256;

        PartDefinition body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(tanU, tanV).addBox(-4.5F, -5.0F, -5.5F, 9.0F, 8.5F, 11.0F)
                        .texOffs(lightTanU, lightTanV).addBox(-5.0F, -3.5F, 1.5F, 10.0F, 6.5F, 4.5F)
                        .texOffs(creamU, creamV).addBox(-3.5F, 1.5F, -5.8F, 7.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, 14.0F, 2.0F));

        if (superForm)
        {
            addSuperSuit(body);
        }
        else
        {
            addNormalHarness(body);
        }

        PartDefinition head = body.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(tanU, tanV).addBox(-4.8F, -5.8F, -4.0F, 9.6F, 7.5F, 7.0F)
                        .texOffs(lightTanU, lightTanV).addBox(-3.8F, -6.8F, -3.5F, 7.6F, 1.5F, 6.0F)
                        .texOffs(lightTanU, lightTanV).addBox(-5.5F, -3.9F, -3.5F, 1.5F, 4.3F, 5.5F)
                        .texOffs(lightTanU, lightTanV).addBox(4.0F, -3.9F, -3.5F, 1.5F, 4.3F, 5.5F)
                        .texOffs(creamU, creamV).addBox(-3.8F, 0.2F, -4.0F, 7.6F, 2.3F, 5.5F),
                PartPose.offset(0.0F, -5.0F, -5.4F));

        addFace(head, tanU, tanV, creamU, creamV, darkTanU, darkTanV, blackU, blackV, whiteU, whiteV, pinkU, pinkV);
        addLeg(body, "front_left_leg", 2.7F, -3.8F, superForm, tanU, tanV, creamU, creamV);
        addLeg(body, "front_right_leg", -2.7F, -3.8F, superForm, tanU, tanV, creamU, creamV);
        addLeg(body, "back_left_leg", 2.7F, 3.6F, superForm, tanU, tanV, creamU, creamV);
        addLeg(body, "back_right_leg", -2.7F, 3.6F, superForm, tanU, tanV, creamU, creamV);
        addCurledTail(body, tanU, tanV, lightTanU, lightTanV, creamU, creamV);
        addHeroParts(body, superForm);

        return LayerDefinition.create(meshDefinition, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    private static void addFace(
            PartDefinition head,
            int tanU,
            int tanV,
            int creamU,
            int creamV,
            int darkTanU,
            int darkTanV,
            int blackU,
            int blackV,
            int whiteU,
            int whiteV,
            int pinkU,
            int pinkV)
    {
        head.addOrReplaceChild(
                "left_ear",
                CubeListBuilder.create()
                        .texOffs(tanU, tanV).addBox(-1.3F, -3.0F, -0.9F, 2.6F, 3.0F, 2.0F)
                        .texOffs(darkTanU, darkTanV).addBox(-0.65F, -2.2F, -1.1F, 1.3F, 1.8F, 0.5F),
                PartPose.offsetAndRotation(3.2F, -6.3F, -0.3F, 0.0F, 0.0F, 0.08F));
        head.addOrReplaceChild(
                "right_ear",
                CubeListBuilder.create()
                        .texOffs(tanU, tanV).addBox(-1.3F, -3.0F, -0.9F, 2.6F, 3.0F, 2.0F)
                        .texOffs(darkTanU, darkTanV).addBox(-0.65F, -2.2F, -1.1F, 1.3F, 1.8F, 0.5F),
                PartPose.offsetAndRotation(-3.2F, -6.3F, -0.3F, 0.0F, 0.0F, -0.08F));

        head.addOrReplaceChild(
                "left_cheek",
                CubeListBuilder.create().texOffs(creamU, creamV)
                        .addBox(-1.9F, -1.8F, -0.8F, 3.8F, 3.6F, 1.6F),
                PartPose.offset(2.7F, -0.4F, -4.2F));
        head.addOrReplaceChild(
                "right_cheek",
                CubeListBuilder.create().texOffs(creamU, creamV)
                        .addBox(-1.9F, -1.8F, -0.8F, 3.8F, 3.6F, 1.6F),
                PartPose.offset(-2.7F, -0.4F, -4.2F));
        head.addOrReplaceChild(
                "muzzle",
                CubeListBuilder.create().texOffs(creamU, creamV)
                        .addBox(-2.1F, -1.4F, -1.6F, 4.2F, 2.8F, 2.5F),
                PartPose.offset(0.0F, 0.0F, -5.0F));

        head.addOrReplaceChild(
                "left_eye",
                CubeListBuilder.create()
                        .texOffs(blackU, blackV).addBox(-0.9F, -0.9F, -0.5F, 1.8F, 1.8F, 0.7F)
                        .texOffs(whiteU, whiteV).addBox(0.15F, -0.65F, -0.7F, 0.5F, 0.5F, 0.3F),
                PartPose.offset(2.25F, -2.75F, -4.0F));
        head.addOrReplaceChild(
                "right_eye",
                CubeListBuilder.create()
                        .texOffs(blackU, blackV).addBox(-0.9F, -0.9F, -0.5F, 1.8F, 1.8F, 0.7F)
                        .texOffs(whiteU, whiteV).addBox(-0.65F, -0.65F, -0.7F, 0.5F, 0.5F, 0.3F),
                PartPose.offset(-2.25F, -2.75F, -4.0F));
        head.addOrReplaceChild(
                "nose",
                CubeListBuilder.create().texOffs(blackU, blackV)
                        .addBox(-1.0F, -0.75F, -0.8F, 2.0F, 1.5F, 1.2F),
                PartPose.offset(0.0F, -0.4F, -6.5F));
        head.addOrReplaceChild(
                "mouth",
                CubeListBuilder.create().texOffs(blackU, blackV)
                        .addBox(-1.5F, -0.4F, -0.5F, 3.0F, 1.4F, 0.8F),
                PartPose.offset(0.0F, 1.4F, -5.7F));
        head.addOrReplaceChild(
                "tongue",
                CubeListBuilder.create().texOffs(pinkU, pinkV)
                        .addBox(-0.9F, 0.0F, -0.8F, 1.8F, 1.8F, 1.0F),
                PartPose.offset(0.0F, 2.2F, -5.7F));
    }

    private static void addNormalHarness(PartDefinition body)
    {
        int navyU = 0;
        int navyV = 128;
        int stripeU = 128;
        int stripeV = 128;

        body.addOrReplaceChild(
                "outfit",
                CubeListBuilder.create()
                        .texOffs(navyU, navyV).addBox(-4.5F, -4.5F, -6.1F, 9.0F, 6.3F, 1.2F)
                        .texOffs(navyU, navyV).addBox(-4.5F, -5.6F, -5.0F, 9.0F, 1.4F, 8.0F)
                        .texOffs(navyU, navyV).addBox(-4.6F, -4.5F, -5.0F, 1.2F, 6.5F, 8.0F)
                        .texOffs(navyU, navyV).addBox(3.4F, -4.5F, -5.0F, 1.2F, 6.5F, 8.0F)
                        .texOffs(stripeU, stripeV).addBox(-4.7F, -2.2F, -6.8F, 9.4F, 1.2F, 0.8F)
                        .texOffs(stripeU, stripeV).addBox(-0.7F, -4.5F, -6.9F, 1.4F, 6.0F, 0.8F),
                PartPose.ZERO);
        body.addOrReplaceChild(
                "left_harness_stripe",
                CubeListBuilder.create().texOffs(stripeU, stripeV)
                        .addBox(-0.35F, -0.6F, -3.0F, 0.7F, 1.2F, 6.0F),
                PartPose.offsetAndRotation(4.65F, -2.0F, -1.0F, 0.48F, 0.0F, 0.0F));
        body.addOrReplaceChild(
                "right_harness_stripe",
                CubeListBuilder.create().texOffs(stripeU, stripeV)
                        .addBox(-0.35F, -0.6F, -3.0F, 0.7F, 1.2F, 6.0F),
                PartPose.offsetAndRotation(-4.65F, -2.0F, -1.0F, 0.48F, 0.0F, 0.0F));
    }

    private static void addSuperSuit(PartDefinition body)
    {
        int blueU = 0;
        int blueV = 128;
        int darkBlueU = 128;
        int darkBlueV = 128;
        int redU = 256;
        int redV = 128;
        int goldU = 0;
        int goldV = 256;
        int yellowU = 128;
        int yellowV = 256;

        body.addOrReplaceChild(
                "outfit",
                CubeListBuilder.create()
                        .texOffs(blueU, blueV).addBox(-4.4F, -5.3F, -5.7F, 8.8F, 8.5F, 11.4F)
                        .texOffs(darkBlueU, darkBlueV).addBox(-4.5F, 1.0F, -5.8F, 9.0F, 2.0F, 11.6F)
                        .texOffs(redU, redV).addBox(-4.8F, -5.8F, -6.0F, 9.6F, 1.5F, 12.0F)
                        .texOffs(goldU, goldV).addBox(-4.7F, 1.8F, -5.9F, 9.4F, 1.0F, 11.8F),
                PartPose.ZERO);

        body.addOrReplaceChild(
                "emblem",
                CubeListBuilder.create()
                        .texOffs(goldU, goldV).addBox(-2.8F, -2.8F, -0.6F, 5.6F, 5.6F, 0.8F)
                        .texOffs(256, 128).addBox(-1.9F, -1.9F, -1.0F, 3.8F, 3.8F, 0.8F)
                        .texOffs(yellowU, yellowV).addBox(-0.7F, -1.3F, -1.4F, 1.4F, 2.6F, 0.8F)
                        .texOffs(yellowU, yellowV).addBox(-1.3F, -0.7F, -1.4F, 2.6F, 1.4F, 0.8F),
                PartPose.offset(0.0F, -0.2F, -5.8F));
    }

    private static void addLeg(
            PartDefinition body,
            String name,
            float x,
            float z,
            boolean superForm,
            int tanU,
            int tanV,
            int creamU,
            int creamV)
    {
        CubeListBuilder legBuilder = CubeListBuilder.create();
        if (superForm)
        {
            legBuilder
                    .texOffs(0, 128).addBox(-1.6F, 0.0F, -1.6F, 3.2F, 4.2F, 3.2F)
                    .texOffs(256, 128).addBox(-1.75F, 3.5F, -1.75F, 3.5F, 2.0F, 3.5F)
                    .texOffs(creamU, creamV).addBox(-1.9F, 5.0F, -2.3F, 3.8F, 2.2F, 4.2F);
        }
        else
        {
            legBuilder
                    .texOffs(tanU, tanV).addBox(-1.6F, 0.0F, -1.6F, 3.2F, 5.5F, 3.2F)
                    .texOffs(creamU, creamV).addBox(-1.9F, 5.0F, -2.3F, 3.8F, 2.2F, 4.2F);
        }
        body.addOrReplaceChild(name, legBuilder, PartPose.offset(x, 2.0F, z));
    }

    private static void addCurledTail(
            PartDefinition body,
            int tanU,
            int tanV,
            int lightTanU,
            int lightTanV,
            int creamU,
            int creamV)
    {
        PartDefinition tailBase = body.addOrReplaceChild(
                "tail_base",
                CubeListBuilder.create()
                        .texOffs(tanU, tanV).addBox(-1.6F, -1.6F, 0.0F, 3.2F, 3.2F, 4.2F)
                        .texOffs(lightTanU, lightTanV).addBox(-1.9F, -4.8F, 2.2F, 3.8F, 4.2F, 3.4F)
                        .texOffs(lightTanU, lightTanV).addBox(-2.2F, -6.0F, -0.2F, 4.4F, 3.2F, 4.2F),
                PartPose.offset(0.0F, -1.5F, 5.0F));
        tailBase.addOrReplaceChild(
                "tail_tip",
                CubeListBuilder.create()
                        .texOffs(creamU, creamV).addBox(-2.3F, -2.2F, -4.8F, 4.6F, 4.4F, 5.0F)
                        .texOffs(lightTanU, lightTanV).addBox(-1.8F, -1.7F, -6.2F, 3.6F, 3.4F, 2.4F),
                PartPose.offset(0.0F, -4.7F, 2.0F));
    }

    private static void addHeroParts(PartDefinition body, boolean superForm)
    {
        PartDefinition leftWing = body.addOrReplaceChild("left_wing", CubeListBuilder.create(), PartPose.offset(4.0F, -4.5F, 0.5F));
        PartDefinition rightWing = body.addOrReplaceChild("right_wing", CubeListBuilder.create(), PartPose.offset(-4.0F, -4.5F, 0.5F));
        CubeListBuilder capeBuilder = CubeListBuilder.create();

        if (superForm)
        {
            addWingFeathers(leftWing, false);
            addWingFeathers(rightWing, true);
            capeBuilder
                    .texOffs(256, 128).addBox(-5.2F, -0.5F, 0.0F, 10.4F, 1.2F, 4.0F)
                    .texOffs(256, 128).addBox(-4.6F, 0.2F, 3.0F, 9.2F, 1.2F, 5.0F)
                    .texOffs(384, 128).addBox(-4.2F, 1.0F, 7.0F, 3.8F, 1.2F, 5.0F)
                    .texOffs(384, 128).addBox(0.4F, 1.0F, 7.0F, 3.8F, 1.2F, 5.0F)
                    .texOffs(0, 256).addBox(-2.7F, -1.2F, 3.8F, 5.4F, 0.8F, 5.4F)
                    .texOffs(256, 128).addBox(-1.8F, -1.7F, 4.7F, 3.6F, 0.8F, 3.6F)
                    .texOffs(128, 256).addBox(-0.6F, -2.2F, 5.2F, 1.2F, 0.8F, 2.6F)
                    .texOffs(128, 256).addBox(-1.3F, -2.2F, 5.9F, 2.6F, 0.8F, 1.2F);
        }
        body.addOrReplaceChild("cape", capeBuilder, PartPose.offset(0.0F, -5.5F, 2.5F));
    }

    private static void addWingFeathers(PartDefinition wing, boolean mirrored)
    {
        float direction = mirrored ? -1.0F : 1.0F;
        int wingU = 256;
        int wingV = 384;
        int shadowU = 384;
        int shadowV = 384;

        wing.addOrReplaceChild(
                "upper_feather",
                CubeListBuilder.create()
                        .texOffs(wingU, wingV).addBox(mirrored ? -13.0F : 0.0F, -0.8F, -0.8F, 13.0F, 1.6F, 1.6F),
                PartPose.offsetAndRotation(0.0F, -0.5F, 0.0F, 0.0F, -0.08F * direction, -1.02F * direction));
        wing.addOrReplaceChild(
                "middle_feather",
                CubeListBuilder.create()
                        .texOffs(wingU, wingV).addBox(mirrored ? -11.5F : 0.0F, -0.9F, -0.9F, 11.5F, 1.8F, 1.8F),
                PartPose.offsetAndRotation(0.0F, 0.4F, 0.7F, 0.0F, -0.04F * direction, -0.82F * direction));
        wing.addOrReplaceChild(
                "lower_feather",
                CubeListBuilder.create()
                        .texOffs(shadowU, shadowV).addBox(mirrored ? -10.0F : 0.0F, -1.0F, -1.0F, 10.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(0.0F, 1.4F, 1.3F, 0.0F, 0.0F, -0.62F * direction));
        wing.addOrReplaceChild(
                "bottom_feather",
                CubeListBuilder.create()
                        .texOffs(shadowU, shadowV).addBox(mirrored ? -8.0F : 0.0F, -1.0F, -1.1F, 8.0F, 2.0F, 2.2F),
                PartPose.offsetAndRotation(0.0F, 2.4F, 1.8F, 0.0F, 0.0F, -0.42F * direction));
        wing.addOrReplaceChild(
                "wing_base",
                CubeListBuilder.create()
                        .texOffs(wingU, wingV).addBox(mirrored ? -5.0F : 0.0F, -1.8F, -1.5F, 5.0F, 4.0F, 3.0F),
                PartPose.ZERO);
    }

    @Override
    public void setupAnim(LivingEntityRenderState renderState)
    {
        super.setupAnim(renderState);

        this.head.xRot = renderState.xRot * Mth.DEG_TO_RAD;
        this.head.yRot = renderState.yRot * Mth.DEG_TO_RAD;

        float walkPosition = renderState.walkAnimationPos * 0.6662F;
        float walkSpeed = renderState.walkAnimationSpeed;
        this.frontLeftLeg.xRot = Mth.cos(walkPosition) * 1.1F * walkSpeed;
        this.frontRightLeg.xRot = Mth.cos(walkPosition + Mth.PI) * 1.1F * walkSpeed;
        this.backLeftLeg.xRot = Mth.cos(walkPosition + Mth.PI) * 1.1F * walkSpeed;
        this.backRightLeg.xRot = Mth.cos(walkPosition) * 1.1F * walkSpeed;

        float wag = Mth.sin(renderState.ageInTicks * 0.25F) * 0.2F;
        this.tailBase.yRot = wag;
        this.tailTip.yRot = wag * 0.6F;

        float flap = Mth.cos(renderState.ageInTicks * 0.35F) * 0.12F;
        this.leftWing.zRot = -0.08F - flap;
        this.rightWing.zRot = 0.08F + flap;
        this.cape.xRot = renderState.walkAnimationSpeed * 0.12F;
    }
}
