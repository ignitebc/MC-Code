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

/**
 * 희귀 펫: 가젤(gazelle.bbmodel).
 * 좌표는 Blockbench Y-up 좌표를 Y축 반전으로 변환한 값이며,
 * 원본의 면별 UV 텍스처는 픽셀 그대로 box UV 아틀라스(128x64, 4배 해상도)로 재배치했다.
 */
public class GazellePetModel extends EntityModel<LivingEntityRenderState>
{
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "gazelle_pet"),
            "main");

    private final ModelPart head;
    private final ModelPart rightEar;
    private final ModelPart leftEar;
    private final ModelPart tail;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart rightBackLeg;
    private final ModelPart leftBackLeg;

    public GazellePetModel(ModelPart root)
    {
        super(root);

        ModelPart gazelle = root.getChild("gazelle");
        this.head = gazelle.getChild("head");
        this.rightEar = this.head.getChild("rightEar");
        this.leftEar = this.head.getChild("leftEar");
        this.tail = gazelle.getChild("tail");
        this.rightFrontLeg = gazelle.getChild("rightFronLeg");
        this.leftFrontLeg = gazelle.getChild("leftFronLeg");
        this.rightBackLeg = gazelle.getChild("rightBackLeg");
        this.leftBackLeg = gazelle.getChild("leftBackLeg");
    }

    public static LayerDefinition createBodyLayer()
    {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        PartDefinition gazelle = root.addOrReplaceChild(
                "gazelle",
                CubeListBuilder.create()
                        .texOffs(35, 0).addBox(-3.00F, -8.00F, -5.00F, 6.00F, 5.00F, 10.00F)
                        .texOffs(0, 0).addBox(-3.30F, -8.20F, -5.20F, 6.60F, 6.40F, 10.40F),
                PartPose.offset(0.00F, 22.25F, 0.00F));

        PartDefinition head = gazelle.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(56, 18).addBox(-1.94167F, 1.175F, -5.80F, 2.80F, 0.50F, 2.90F)
                        .texOffs(27, 18).addBox(-2.04167F, -0.575F, -5.90F, 3.00F, 1.75F, 3.00F)
                        .texOffs(68, 0).addBox(-4.04167F, -5.075F, -3.90F, 7.00F, 7.00F, 7.00F),
                PartPose.offset(0.54167F, -9.425F, -5.60F));

        head.addOrReplaceChild(
                "rightEar",
                CubeListBuilder.create()
                        .texOffs(69, 18).addBox(-0.25F, -2.25F, -0.25F, 3.50F, 2.50F, 1.00F),
                PartPose.offsetAndRotation(2.85833F, -3.325F, -0.40F, 0.00F, 0.00F, -0.17453F));

        head.addOrReplaceChild(
                "leftEar",
                CubeListBuilder.create()
                        .texOffs(79, 18).addBox(-3.25F, -1.25F, -0.25F, 3.50F, 2.50F, 1.00F),
                PartPose.offsetAndRotation(-3.94167F, -4.325F, -0.40F, 0.00F, 0.00F, 0.17453F));

        PartDefinition rightEye = head.addOrReplaceChild(
                "rightEye",
                CubeListBuilder.create()
                        .texOffs(98, 18).addBox(-1.00F, -0.50F, -0.05F, 1.50F, 1.50F, 0.00F),
                PartPose.offset(2.20833F, -2.075F, -3.95F));

        rightEye.addOrReplaceChild(
                "rightBrow",
                CubeListBuilder.create()
                        .texOffs(112, 18).addBox(-1.25F, -0.375F, 0.00F, 2.00F, 0.75F, 0.00F),
                PartPose.offset(0.00F, -0.875F, -0.15F));

        PartDefinition leftEye = head.addOrReplaceChild(
                "leftEye",
                CubeListBuilder.create()
                        .texOffs(102, 18).addBox(-0.50F, -0.50F, -0.05F, 1.50F, 1.50F, 0.00F),
                PartPose.offset(-3.29167F, -2.075F, -3.95F));

        leftEye.addOrReplaceChild(
                "leftBrow",
                CubeListBuilder.create()
                        .texOffs(117, 18).addBox(-0.75F, -0.375F, 0.00F, 2.00F, 0.75F, 0.00F),
                PartPose.offset(0.00F, -0.875F, -0.15F));

        PartDefinition rightHorn = head.addOrReplaceChild(
                "rightHorn",
                CubeListBuilder.create()
                        .texOffs(97, 0).addBox(-1.00F, -5.03822F, -3.32319F, 2.00F, 6.25F, 2.00F),
                PartPose.offsetAndRotation(1.45833F, -5.82886F, 2.48578F, -0.1309F, 0.00F, 0.00F));

        rightHorn.addOrReplaceChild(
                "rightHorn_r1",
                CubeListBuilder.create()
                        .texOffs(40, 18).addBox(-0.80F, -3.00F, -0.80F, 1.60F, 3.00F, 1.60F),
                PartPose.offsetAndRotation(0.00F, -4.4484F, -2.53272F, -0.47997F, 0.00F, 0.00F));

        PartDefinition leftHorn = head.addOrReplaceChild(
                "leftHorn",
                CubeListBuilder.create()
                        .texOffs(106, 0).addBox(-1.00F, -5.03822F, -3.32319F, 2.00F, 6.25F, 2.00F),
                PartPose.offsetAndRotation(-2.54167F, -5.82886F, 2.48578F, -0.1309F, 0.00F, 0.00F));

        leftHorn.addOrReplaceChild(
                "leftHorn_r1",
                CubeListBuilder.create()
                        .texOffs(48, 18).addBox(-0.80F, -3.00F, -0.80F, 1.60F, 3.00F, 1.60F),
                PartPose.offsetAndRotation(0.00F, -4.4484F, -2.53272F, -0.47997F, 0.00F, 0.00F));

        head.addOrReplaceChild(
                "nose",
                CubeListBuilder.create()
                        .texOffs(106, 18).addBox(-0.75F, 0.25F, -1.75F, 1.50F, 0.75F, 1.00F),
                PartPose.offset(-0.54167F, -0.575F, -4.40F));

        gazelle.addOrReplaceChild(
                "rightFronLeg",
                CubeListBuilder.create()
                        .texOffs(115, 0).addBox(-1.00F, 0.00F, -1.00F, 2.00F, 5.00F, 2.00F),
                PartPose.offset(1.75F, -3.25F, -3.75F));

        gazelle.addOrReplaceChild(
                "leftFronLeg",
                CubeListBuilder.create()
                        .texOffs(0, 18).addBox(-1.00F, 0.00F, -1.00F, 2.00F, 5.00F, 2.00F),
                PartPose.offset(-1.75F, -3.25F, -3.75F));

        gazelle.addOrReplaceChild(
                "tail",
                CubeListBuilder.create()
                        .texOffs(89, 18).addBox(-1.00F, -1.00F, 0.40F, 2.00F, 2.00F, 2.00F),
                PartPose.offset(0.00F, -5.00F, 4.50F));

        gazelle.addOrReplaceChild(
                "rightBackLeg",
                CubeListBuilder.create()
                        .texOffs(9, 18).addBox(-1.00F, 0.00F, -1.00F, 2.00F, 5.00F, 2.00F),
                PartPose.offset(1.75F, -3.25F, 3.75F));

        gazelle.addOrReplaceChild(
                "leftBackLeg",
                CubeListBuilder.create()
                        .texOffs(18, 18).addBox(-1.00F, 0.00F, -1.00F, 2.00F, 5.00F, 2.00F),
                PartPose.offset(-1.75F, -3.25F, 3.75F));

        return LayerDefinition.create(meshDefinition, 128, 64);
    }

    @Override
    public void setupAnim(LivingEntityRenderState renderState)
    {
        super.setupAnim(renderState);

        float time = renderState.ageInTicks;
        float walkPosition = renderState.walkAnimationPos * 0.6662F;
        float walkSpeed = renderState.walkAnimationSpeed;

        // 시선을 따라 고개를 돌린다.
        this.head.xRot += renderState.xRot * Mth.DEG_TO_RAD * 0.7F;
        this.head.yRot += renderState.yRot * Mth.DEG_TO_RAD * 0.7F;

        // 대각 보행: 오른앞-왼뒤, 왼앞-오른뒤 다리가 짝을 이룬다.
        float swingA = Mth.cos(walkPosition) * 1.0F * walkSpeed;
        float swingB = Mth.cos(walkPosition + Mth.PI) * 1.0F * walkSpeed;
        this.rightFrontLeg.xRot += swingA;
        this.leftBackLeg.xRot += swingA;
        this.leftFrontLeg.xRot += swingB;
        this.rightBackLeg.xRot += swingB;

        // 꼬리를 살랑살랑 흔들고 귀를 이따금씩 털어낸다.
        this.tail.yRot += Mth.sin(time * 0.15F) * 0.25F;
        float earFlick = Math.max(0.0F, Mth.sin(time * 0.05F) - 0.9F) * 3.0F;
        this.rightEar.zRot -= earFlick;
        this.leftEar.zRot += earFlick;
    }
}
