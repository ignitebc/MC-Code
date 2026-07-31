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
 * 희귀 펫: 야생 유니콘(wild-unicorn.bbmodel).
 * 좌표는 Blockbench Y-up 좌표를 Y축 반전으로 변환한 값이며,
 * 원본의 면별 UV 텍스처는 픽셀 그대로 box UV 아틀라스(256x256)로 재배치했다.
 */
public class UnicornPetModel extends EntityModel<LivingEntityRenderState>
{
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "unicorn_pet"),
            "main");

    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart rightEar;
    private final ModelPart leftEar;
    private final ModelPart hair;
    private final ModelPart hair2;
    private final ModelPart hair3;
    private final ModelPart hair4;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart lowerArmRight;
    private final ModelPart lowerArmLeft;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart lowerLegRight;
    private final ModelPart lowerLegLeft;
    private final ModelPart[] tailParts;

    public UnicornPetModel(ModelPart root)
    {
        super(root);

        ModelPart unicorn = root.getChild("unicorn");
        this.body = unicorn.getChild("body");
        ModelPart chest = this.body.getChild("chest");
        ModelPart neck = chest.getChild("neck");
        ModelPart neckUpper = neck.getChild("neck_upper");
        this.head = neckUpper.getChild("head");
        this.rightEar = this.head.getChild("right_ear");
        this.leftEar = this.head.getChild("left_ear");
        this.hair = neck.getChild("hair");
        this.hair2 = neckUpper.getChild("hair2");
        this.hair3 = this.head.getChild("hair3");
        this.hair4 = this.head.getChild("hair4");
        this.rightArm = chest.getChild("right_arm");
        this.leftArm = chest.getChild("left_arm");
        this.lowerArmRight = this.rightArm.getChild("lower_arm_right");
        this.lowerArmLeft = this.leftArm.getChild("lower_arm_left");

        ModelPart hips = this.body.getChild("hips");
        this.rightLeg = hips.getChild("right_leg");
        this.leftLeg = hips.getChild("left_leg");
        this.lowerLegRight = this.rightLeg.getChild("lower_leg_right");
        this.lowerLegLeft = this.leftLeg.getChild("lower_leg_left");

        ModelPart tail = hips.getChild("tail");
        ModelPart tail2 = tail.getChild("tail_2");
        ModelPart tail3 = tail2.getChild("tail_3");
        ModelPart tail4 = tail3.getChild("tail_4");
        ModelPart tail5 = tail4.getChild("tail_5");
        this.tailParts = new ModelPart[] {tail, tail2, tail3, tail4, tail5};
    }

    public static LayerDefinition createBodyLayer()
    {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        PartDefinition unicorn = root.addOrReplaceChild(
                "unicorn",
                CubeListBuilder.create(),
                PartPose.offset(-0.10124F, 24.00F, -2.04811F));

        PartDefinition body = unicorn.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-9.19468F, -11.16992F, -15.70492F, 18.59184F, 21.89124F, 31.9868F),
                PartPose.offset(0.00F, -53.23473F, 0.00F));

        PartDefinition belly = body.addOrReplaceChild(
                "belly",
                CubeListBuilder.create(),
                PartPose.offset(0.00F, 9.82168F, -12.70612F));

        belly.addOrReplaceChild(
                "belly_r1",
                CubeListBuilder.create()
                        .texOffs(0, 55).addBox(-8.39664F, -20.9916F, -1.6988F, 16.79328F, 4.5976F, 35.8868F),
                PartPose.offsetAndRotation(0.10124F, 21.19136F, -0.70F, 0.13963F, 0.00F, 0.00F));

        PartDefinition chest = body.addOrReplaceChild(
                "chest",
                CubeListBuilder.create(),
                PartPose.offset(2.30529F, -1.06305F, -15.03288F));

        chest.addOrReplaceChild(
                "chest_r1",
                CubeListBuilder.create()
                        .texOffs(103, 0).addBox(-8.79628F, -24.59016F, 11.9952F, 17.59256F, 25.2886F, 17.9928F),
                PartPose.offsetAndRotation(-2.20405F, -0.51071F, -27.06148F, -0.56723F, 0.00F, 0.00F));

        PartDefinition neck = chest.addOrReplaceChild(
                "neck",
                CubeListBuilder.create(),
                PartPose.offset(-2.30529F, 0.79534F, -5.33975F));

        neck.addOrReplaceChild(
                "neck_r1",
                CubeListBuilder.create()
                        .texOffs(0, 97).addBox(-4.4982F, -29.988F, 15.29388F, 8.9964F, 15.29388F, 16.9952F),
                PartPose.offsetAndRotation(0.10124F, -7.30365F, -35.81609F, -0.6545F, 0.00F, 0.00F));

        neck.addOrReplaceChild(
                "neck_r2",
                CubeListBuilder.create()
                        .texOffs(62, 131).addBox(-3.89844F, -14.09436F, -8.19352F, 7.79688F, 7.19712F, 19.38944F),
                PartPose.offsetAndRotation(0.10124F, -0.4113F, -19.93731F, -1.10828F, 0.00F, 0.00F));

        PartDefinition hair = neck.addOrReplaceChild(
                "hair",
                CubeListBuilder.create(),
                PartPose.offset(0.00F, -17.30849F, 0.43477F));

        hair.addOrReplaceChild(
                "hair_r1",
                CubeListBuilder.create()
                        .texOffs(54, 161).addBox(0.00F, 1.95306F, -5.9476F, 0.00F, 2.29388F, 17.0952F),
                PartPose.offsetAndRotation(0.10124F, -3.50F, 2.20F, -0.6545F, 0.00F, 0.00F));

        PartDefinition neckUpper = neck.addOrReplaceChild(
                "neck_upper",
                CubeListBuilder.create(),
                PartPose.offset(0.00F, -15.72298F, -9.07214F));

        neckUpper.addOrReplaceChild(
                "neck_upper_r1",
                CubeListBuilder.create()
                        .texOffs(199, 97).addBox(-3.59856F, -29.68812F, 0.29988F, 7.19712F, 12.59496F, 16.29388F),
                PartPose.offsetAndRotation(0.10124F, 8.41933F, -26.74395F, -0.6545F, 0.00F, 0.00F));

        neckUpper.addOrReplaceChild(
                "neck_upper_r2",
                CubeListBuilder.create()
                        .texOffs(193, 161).addBox(-3.80156F, -14.09436F, -16.19352F, 7.00F, 6.69712F, 9.98944F),
                PartPose.offsetAndRotation(0.4028F, 15.31168F, -10.86517F, -1.10828F, 0.00F, 0.00F));

        neckUpper.addOrReplaceChild(
                "hair2",
                CubeListBuilder.create()
                        .texOffs(194, 131).addBox(0.10124F, -3.74748F, -8.14694F, 0.00F, 3.89496F, 16.29388F),
                PartPose.offsetAndRotation(0.00F, -10.0217F, -2.21029F, -0.6545F, 0.00F, 0.00F));

        PartDefinition head = neckUpper.addOrReplaceChild(
                "head",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(-0.01114F, -11.06144F, -9.8769F, -0.74176F, 0.00F, 0.00F));

        head.addOrReplaceChild(
                "head_r1",
                CubeListBuilder.create()
                        .texOffs(0, 161).addBox(-4.32727F, -32.62095F, -5.65874F, 8.65454F, 11.87047F, 8.98741F),
                PartPose.offsetAndRotation(0.11238F, 25.82655F, -17.1929F, -0.6545F, 0.00F, 0.00F));

        head.addOrReplaceChild(
                "head_r2",
                CubeListBuilder.create()
                        .texOffs(17, 183).addBox(-3.32867F, -20.68302F, -5.66474F, 6.65734F, 6.43453F, 5.10427F),
                PartPose.offsetAndRotation(0.11238F, 25.82655F, -17.1929F, -0.6545F, 0.00F, 0.00F));

        head.addOrReplaceChild(
                "head_r3",
                CubeListBuilder.create()
                        .texOffs(149, 197).addBox(-4.32727F, -32.62095F, -6.65707F, 8.65454F, 6.21201F, 0.99833F),
                PartPose.offsetAndRotation(0.11238F, 25.82655F, -17.1929F, -0.6545F, 0.00F, 0.00F));

        head.addOrReplaceChild(
                "head_r4",
                CubeListBuilder.create()
                        .texOffs(200, 197).addBox(-3.32867F, -20.68302F, -0.56047F, 6.65734F, 3.21727F, 2.66293F),
                PartPose.offsetAndRotation(0.11238F, 25.82655F, -17.1929F, -0.6545F, 0.00F, 0.00F));

        PartDefinition horn = head.addOrReplaceChild(
                "horn",
                CubeListBuilder.create(),
                PartPose.offset(0.11238F, -2.84429F, -8.28977F));

        horn.addOrReplaceChild(
                "horn_r1",
                CubeListBuilder.create()
                        .texOffs(170, 197).addBox(-0.99727F, 0.224F, -8.15817F, 1.99454F, 2.66001F, 4.99433F),
                PartPose.offsetAndRotation(0.00F, 0.00F, 0.00F, -0.67195F, 0.00F, 0.00F));

        horn.addOrReplaceChild(
                "horn_r2",
                CubeListBuilder.create()
                        .texOffs(185, 197).addBox(-0.88627F, 0.224F, -9.26817F, 1.77254F, 2.66F, 4.99434F),
                PartPose.offsetAndRotation(0.00F, -1.53162F, -3.18807F, -0.84648F, 0.00F, 0.00F));

        horn.addOrReplaceChild(
                "horn_r3",
                CubeListBuilder.create()
                        .texOffs(42, 183).addBox(-1.10827F, 0.224F, -2.49717F, 2.21654F, 2.66F, 7.21434F),
                PartPose.offsetAndRotation(0.00F, -1.11756F, -0.72468F, -0.47997F, 0.00F, 0.00F));

        head.addOrReplaceChild(
                "hair3",
                CubeListBuilder.create()
                        .texOffs(0, 183).addBox(0.11238F, -4.71588F, -3.57186F, 0.00F, 4.87747F, 7.8774F),
                PartPose.offsetAndRotation(0.00F, -2.35492F, -0.651F, -0.6545F, 0.00F, 0.00F));

        PartDefinition rightEar = head.addOrReplaceChild(
                "right_ear",
                CubeListBuilder.create(),
                PartPose.offset(2.9958F, -2.15053F, -3.15645F));

        rightEar.addOrReplaceChild(
                "right_ear_r1",
                CubeListBuilder.create()
                        .texOffs(62, 183).addBox(1.33147F, -0.9986F, -3.82797F, 3.32867F, 1.99721F, 7.65594F),
                PartPose.offsetAndRotation(-2.9958F, -2.553F, -1.332F, -0.95993F, 0.00F, 0.00F));

        PartDefinition leftEar = head.addOrReplaceChild(
                "left_ear",
                CubeListBuilder.create(),
                PartPose.offset(-2.77104F, -2.15053F, -3.15645F));

        leftEar.addOrReplaceChild(
                "left_ear_r1",
                CubeListBuilder.create()
                        .texOffs(85, 183).addBox(-4.66014F, -0.9986F, -3.82797F, 3.32867F, 1.99721F, 7.65594F),
                PartPose.offsetAndRotation(2.9958F, -2.553F, -1.332F, -0.95993F, 0.00F, 0.00F));

        PartDefinition hair4 = head.addOrReplaceChild(
                "hair4",
                CubeListBuilder.create(),
                PartPose.offset(0.00F, -3.96251F, -4.07137F));

        hair4.addOrReplaceChild(
                "hair4_r1",
                CubeListBuilder.create()
                        .texOffs(228, 161).addBox(-3.43927F, -32.62095F, -6.73407F, 6.87854F, 14.42601F, 0.00F),
                PartPose.offsetAndRotation(0.11238F, 29.78906F, -13.12153F, -0.6545F, 0.00F, 0.00F));

        PartDefinition jaw = head.addOrReplaceChild(
                "jaw",
                CubeListBuilder.create(),
                PartPose.offset(0.00F, 13.46411F, -8.78281F));

        jaw.addOrReplaceChild(
                "jaw_r1",
                CubeListBuilder.create()
                        .texOffs(220, 197).addBox(-2.9958F, -17.46575F, -0.56047F, 5.9916F, 2.8844F, 2.66293F),
                PartPose.offsetAndRotation(0.11238F, 12.36244F, -8.41009F, -0.6545F, 0.00F, 0.00F));

        jaw.addOrReplaceChild(
                "jaw_r2",
                CubeListBuilder.create()
                        .texOffs(108, 183).addBox(0.00F, -18.57575F, 1.65953F, 0.00F, 3.9944F, 5.99293F),
                PartPose.offsetAndRotation(0.11238F, 12.36244F, -8.41009F, -0.6545F, 0.00F, 0.00F));

        PartDefinition rightArm = chest.addOrReplaceChild(
                "right_arm",
                CubeListBuilder.create()
                        .texOffs(147, 97).addBox(-3.01754F, -3.17254F, -3.86937F, 5.9976F, 24.38944F, 6.1976F),
                PartPose.offset(3.51057F, 11.99441F, -4.64025F));

        PartDefinition lowerArmRight = rightArm.addOrReplaceChild(
                "lower_arm_right",
                CubeListBuilder.create()
                        .texOffs(90, 161).addBox(-2.39904F, -0.0393F, -0.39527F, 4.79808F, 14.89484F, 4.19832F)
                        .texOffs(118, 131).addBox(0.00F, -0.0393F, 0.60473F, 0.00F, 17.89484F, 7.19832F),
                PartPose.offset(-0.01874F, 21.2562F, -2.57446F));

        PartDefinition handRight = lowerArmRight.addOrReplaceChild(
                "hand_right",
                CubeListBuilder.create()
                        .texOffs(121, 183).addBox(-2.54898F, 4.94866F, -4.05626F, 5.39784F, 3.59856F, 6.29748F),
                PartPose.offset(-0.14994F, 12.49208F, 0.3543F));

        handRight.addOrReplaceChild(
                "hand_right_r1",
                CubeListBuilder.create()
                        .texOffs(25, 197).addBox(-2.09916F, -2.54898F, -2.39904F, 4.19832F, 5.09796F, 3.89844F),
                PartPose.offsetAndRotation(0.14994F, 2.0998F, 0.74182F, -0.41015F, 0.00F, 0.00F));

        handRight.addOrReplaceChild(
                "hand_right_r2",
                CubeListBuilder.create()
                        .texOffs(43, 197).addBox(-2.09916F, 0.7497F, -2.69892F, 4.79808F, 2.69892F, 5.39784F),
                PartPose.offsetAndRotation(-0.14994F, 3.67957F, 0.2707F, -0.58469F, 0.00F, 0.00F));

        PartDefinition leftArm = chest.addOrReplaceChild(
                "left_arm",
                CubeListBuilder.create()
                        .texOffs(173, 97).addBox(-2.98006F, -3.17254F, -3.86937F, 5.9976F, 24.38944F, 6.1976F),
                PartPose.offset(-7.91867F, 11.99441F, -4.64025F));

        PartDefinition lowerArmLeft = leftArm.addOrReplaceChild(
                "lower_arm_left",
                CubeListBuilder.create()
                        .texOffs(109, 161).addBox(-2.39904F, -0.0393F, -0.39527F, 4.79808F, 14.89484F, 4.19832F)
                        .texOffs(134, 131).addBox(0.00F, -0.0393F, 0.60473F, 0.00F, 17.89484F, 7.19832F),
                PartPose.offset(0.01874F, 21.2562F, -2.57446F));

        PartDefinition handLeft = lowerArmLeft.addOrReplaceChild(
                "hand_left",
                CubeListBuilder.create()
                        .texOffs(146, 183).addBox(-2.84886F, 4.94866F, -4.05626F, 5.39784F, 3.59856F, 6.29748F),
                PartPose.offset(0.14994F, 12.49208F, 0.3543F));

        handLeft.addOrReplaceChild(
                "hand_left_r1",
                CubeListBuilder.create()
                        .texOffs(65, 197).addBox(-2.09916F, -2.54898F, -2.39904F, 4.19832F, 5.09796F, 3.89844F),
                PartPose.offsetAndRotation(-0.14994F, 2.0998F, 0.74182F, -0.41015F, 0.00F, 0.00F));

        handLeft.addOrReplaceChild(
                "hand_left_r2",
                CubeListBuilder.create()
                        .texOffs(83, 197).addBox(-2.69892F, 0.7497F, -2.69892F, 4.79808F, 2.69892F, 5.39784F),
                PartPose.offsetAndRotation(0.14994F, 3.67957F, 0.2707F, -0.58469F, 0.00F, 0.00F));

        PartDefinition hips = body.addOrReplaceChild(
                "hips",
                CubeListBuilder.create(),
                PartPose.offset(1.81987F, -6.55696F, 12.92344F));

        hips.addOrReplaceChild(
                "hips_r1",
                CubeListBuilder.create()
                        .texOffs(107, 55).addBox(-8.69628F, -9.04502F, -7.9952F, 17.39256F, 18.79004F, 19.9904F),
                PartPose.offsetAndRotation(-1.71863F, 8.04692F, 5.92956F, 1.30027F, 0.00F, 0.00F));

        hips.addOrReplaceChild(
                "hips_r2",
                CubeListBuilder.create()
                        .texOffs(53, 97).addBox(-5.7988F, -13.2449F, -6.39628F, 9.3976F, 19.09316F, 13.19448F),
                PartPose.offsetAndRotation(4.17725F, 13.2299F, 9.05496F, -0.22689F, 0.00F, 0.00F));

        hips.addOrReplaceChild(
                "hips_r3",
                CubeListBuilder.create()
                        .texOffs(100, 97).addBox(-3.5988F, -13.2449F, -6.39628F, 9.3976F, 19.09316F, 13.19448F),
                PartPose.offsetAndRotation(-7.61451F, 13.2299F, 9.05496F, -0.22689F, 0.00F, 0.00F));

        PartDefinition rightLeg = hips.addOrReplaceChild(
                "right_leg",
                CubeListBuilder.create(),
                PartPose.offset(3.63975F, 17.02978F, 5.81624F));

        rightLeg.addOrReplaceChild(
                "right_leg_r1",
                CubeListBuilder.create()
                        .texOffs(0, 131).addBox(-3.99892F, 0.2503F, -6.89724F, 6.69784F, 20.89124F, 8.09676F),
                PartPose.offsetAndRotation(0.5375F, -3.79988F, 3.23872F, 0.57596F, 0.00F, 0.00F));

        PartDefinition lowerLegRight = rightLeg.addOrReplaceChild(
                "lower_leg_right",
                CubeListBuilder.create(),
                PartPose.offset(0.1875F, 14.26714F, 13.48007F));

        lowerLegRight.addOrReplaceChild(
                "lower_leg_right_r1",
                CubeListBuilder.create()
                        .texOffs(150, 131).addBox(-2.39904F, 17.2431F, 5.9976F, 4.79808F, 20.39184F, 5.39784F),
                PartPose.offsetAndRotation(-0.45F, -18.06702F, -10.24135F, 0.00873F, 0.00F, 0.00F));

        lowerLegRight.addOrReplaceChild(
                "lower_leg_right_r2",
                CubeListBuilder.create()
                        .texOffs(183, 55).addBox(0.00F, 17.2431F, 6.9976F, 0.00F, 26.39184F, 9.39784F),
                PartPose.offsetAndRotation(-0.45F, -18.06702F, -10.24135F, 0.00873F, 0.00F, 0.00F));

        PartDefinition footRight = lowerLegRight.addOrReplaceChild(
                "foot_right",
                CubeListBuilder.create()
                        .texOffs(189, 183).addBox(-3.29892F, 5.7867F, -4.52734F, 5.39784F, 3.59856F, 6.29748F),
                PartPose.offset(0.15F, 19.10164F, -2.66476F));

        footRight.addOrReplaceChild(
                "foot_right_r1",
                CubeListBuilder.create()
                        .texOffs(105, 197).addBox(-2.09916F, 0.7497F, -2.69892F, 4.79808F, 2.69892F, 5.39784F),
                PartPose.offsetAndRotation(-0.89988F, 4.51761F, -0.20038F, -0.58469F, 0.00F, 0.00F));

        footRight.addOrReplaceChild(
                "foot_right_r2",
                CubeListBuilder.create()
                        .texOffs(171, 183).addBox(-2.09916F, -3.44862F, -2.39904F, 4.19832F, 5.9976F, 3.89844F),
                PartPose.offsetAndRotation(-0.60F, 2.93784F, 0.27074F, -0.41015F, 0.00F, 0.00F));

        PartDefinition leftLeg = hips.addOrReplaceChild(
                "left_leg",
                CubeListBuilder.create(),
                PartPose.offset(-7.07701F, 17.02978F, 5.81624F));

        leftLeg.addOrReplaceChild(
                "left_leg_r1",
                CubeListBuilder.create()
                        .texOffs(31, 131).addBox(-2.69892F, 0.2503F, -6.89724F, 6.69784F, 20.89124F, 8.09676F),
                PartPose.offsetAndRotation(-0.5375F, -3.79988F, 3.23872F, 0.57596F, 0.00F, 0.00F));

        PartDefinition lowerLegLeft = leftLeg.addOrReplaceChild(
                "lower_leg_left",
                CubeListBuilder.create(),
                PartPose.offset(-0.1875F, 14.26714F, 13.48007F));

        lowerLegLeft.addOrReplaceChild(
                "lower_leg_left_r1",
                CubeListBuilder.create()
                        .texOffs(172, 131).addBox(-2.39904F, 17.2431F, 5.9976F, 4.79808F, 20.39184F, 5.39784F),
                PartPose.offsetAndRotation(0.45F, -18.06702F, -10.24135F, 0.00873F, 0.00F, 0.00F));

        lowerLegLeft.addOrReplaceChild(
                "lower_leg_left_r2",
                CubeListBuilder.create()
                        .texOffs(203, 55).addBox(0.00F, 17.2431F, 6.9976F, 0.00F, 26.39184F, 9.39784F),
                PartPose.offsetAndRotation(0.45F, -18.06702F, -10.24135F, 0.00873F, 0.00F, 0.00F));

        PartDefinition footLeft = lowerLegLeft.addOrReplaceChild(
                "foot_left",
                CubeListBuilder.create()
                        .texOffs(0, 197).addBox(-2.09892F, 5.7867F, -4.52734F, 5.39784F, 3.59856F, 6.29748F),
                PartPose.offset(-0.15F, 19.10164F, -2.66476F));

        footLeft.addOrReplaceChild(
                "foot_left_r1",
                CubeListBuilder.create()
                        .texOffs(127, 197).addBox(-2.69892F, 0.7497F, -2.69892F, 4.79808F, 2.69892F, 5.39784F),
                PartPose.offsetAndRotation(0.89988F, 4.51761F, -0.20038F, -0.58469F, 0.00F, 0.00F));

        footLeft.addOrReplaceChild(
                "foot_left_r2",
                CubeListBuilder.create()
                        .texOffs(214, 183).addBox(-2.09916F, -3.44862F, -2.39904F, 4.19832F, 5.9976F, 3.89844F),
                PartPose.offsetAndRotation(0.60F, 2.93784F, 0.27074F, -0.41015F, 0.00F, 0.00F));

        PartDefinition tail = hips.addOrReplaceChild(
                "tail",
                CubeListBuilder.create(),
                PartPose.offset(-1.81987F, 2.22897F, 17.84564F));

        tail.addOrReplaceChild(
                "tail_r1",
                CubeListBuilder.create()
                        .texOffs(128, 161).addBox(-1.79928F, -23.9904F, 1.20072F, 3.59856F, 14.9808F, 3.79688F),
                PartPose.offsetAndRotation(0.10124F, 22.24643F, -0.39404F, 0.06981F, 0.00F, 0.00F));

        PartDefinition tail2 = tail.addOrReplaceChild(
                "tail_2",
                CubeListBuilder.create(),
                PartPose.offset(0.00F, 13.24643F, 1.30596F));

        tail2.addOrReplaceChild(
                "tail_2_r1",
                CubeListBuilder.create()
                        .texOffs(144, 161).addBox(-1.49928F, -8.9904F, 1.20072F, 2.99856F, 14.9808F, 3.79688F),
                PartPose.offsetAndRotation(0.10124F, 9.00F, -1.70F, 0.06981F, 0.00F, 0.00F));

        PartDefinition tail3 = tail2.addOrReplaceChild(
                "tail_3",
                CubeListBuilder.create(),
                PartPose.offset(0.00F, 14.90F, 1.30F));

        tail3.addOrReplaceChild(
                "tail_3_r1",
                CubeListBuilder.create()
                        .texOffs(37, 161).addBox(0.00F, -8.9904F, -0.79928F, 0.00F, 12.9808F, 7.79688F),
                PartPose.offsetAndRotation(0.10124F, 9.10F, -3.00F, 0.06981F, 0.00F, 0.00F));

        PartDefinition tail4 = tail3.addOrReplaceChild(
                "tail_4",
                CubeListBuilder.create(),
                PartPose.offset(0.00F, 13.02867F, 0.57063F));

        tail4.addOrReplaceChild(
                "tail_4_r1",
                CubeListBuilder.create()
                        .texOffs(159, 161).addBox(0.00F, -10.9904F, 0.20072F, 0.00F, 9.9808F, 7.79688F),
                PartPose.offsetAndRotation(0.10124F, 11.07133F, -3.57063F, 0.06981F, 0.00F, 0.00F));

        PartDefinition tail5 = tail4.addOrReplaceChild(
                "tail_5",
                CubeListBuilder.create(),
                PartPose.offset(0.00F, 10.00F, 0.00F));

        tail5.addOrReplaceChild(
                "tail_5_r1",
                CubeListBuilder.create()
                        .texOffs(176, 161).addBox(0.00F, -10.9904F, 0.20072F, 0.00F, 9.9808F, 7.79688F),
                PartPose.offsetAndRotation(0.10124F, 11.07133F, -3.57063F, 0.06981F, 0.00F, 0.00F));

        return LayerDefinition.create(meshDefinition, 256, 256);
    }

    @Override
    public void setupAnim(LivingEntityRenderState renderState)
    {
        super.setupAnim(renderState);

        float time = renderState.ageInTicks;
        float walkPosition = renderState.walkAnimationPos * 0.6662F;
        float walkSpeed = renderState.walkAnimationSpeed;

        // 시선을 따라 머리가 움직인다. 목 체인이 있으므로 절반만 반영한다.
        this.head.xRot += renderState.xRot * Mth.DEG_TO_RAD * 0.5F;
        this.head.yRot += renderState.yRot * Mth.DEG_TO_RAD * 0.5F;

        // 대각 보행: 오른쪽 앞다리와 왼쪽 뒷다리가 같은 박자로 내딛는다.
        float swingA = Mth.cos(walkPosition) * 0.7F * walkSpeed;
        float swingB = Mth.cos(walkPosition + Mth.PI) * 0.7F * walkSpeed;
        this.rightArm.xRot += swingA;
        this.leftArm.xRot += swingB;
        this.rightLeg.xRot += swingB;
        this.leftLeg.xRot += swingA;

        // 무릎 아래는 반 박자 늦게 따라와 관절이 접히는 느낌을 준다.
        float followA = Mth.cos(walkPosition - Mth.HALF_PI) * 0.35F * walkSpeed;
        float followB = Mth.cos(walkPosition + Mth.PI - Mth.HALF_PI) * 0.35F * walkSpeed;
        this.lowerArmRight.xRot += Math.max(0.0F, followA);
        this.lowerArmLeft.xRot += Math.max(0.0F, followB);
        this.lowerLegRight.xRot += Math.max(0.0F, followB);
        this.lowerLegLeft.xRot += Math.max(0.0F, followA);

        // 걷는 동안 몸통이 가볍게 오르내린다.
        this.body.y += Mth.sin(walkPosition * 2.0F) * 0.8F * walkSpeed;

        // 갈기가 잔잔하게 흔들리는 대기 연출
        float maneSway = Mth.sin(time * 0.1F);
        this.hair.zRot += maneSway * 0.06F;
        this.hair2.zRot -= maneSway * 0.06F;
        this.hair3.zRot += maneSway * 0.09F;
        this.hair4.xRot += Mth.sin(time * 0.08F) * 0.05F;

        // 귀를 이따금씩 쫑긋거린다.
        float earFlick = Math.max(0.0F, Mth.sin(time * 0.04F) - 0.9F) * 2.5F;
        this.rightEar.xRot += earFlick;
        this.leftEar.xRot += earFlick;

        // 꼬리 다섯 마디가 뿌리부터 끝까지 파도치듯 흔들린다.
        for (int index = 0; index < this.tailParts.length; index++)
        {
            float phase = index * 0.6F;
            this.tailParts[index].xRot += Mth.sin(time * 0.12F - phase) * 0.04F;
            this.tailParts[index].yRot += Mth.sin(time * 0.09F - phase) * (0.03F + index * 0.02F);
        }
    }
}
