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
 * 레전더리 펫: 스컬큰 레이븐(sculken-raven-geo.bbmodel).
 * 원본이 box UV라 텍스처(512x512)와 UV 오프셋을 그대로 사용한다.
 * 원본 기본 포즈는 날개를 편 상태라, 지상 펫에 맞게 setupAnim에서 날개를 접는다.
 */
public class SculkenRavenPetModel extends EntityModel<LivingEntityRenderState>
{
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "sculken_raven_pet"),
            "main");

    // 접힌 날개 기본 포즈. 어깨를 뒤로 젖히고 날개 끝을 앞으로 되접는다.
    private static final float WING_FOLD_YAW = 1.22F;
    private static final float WING_FOLD_DROOP = 0.30F;
    private static final float WING_TIP_FOLD_YAW = 2.62F;

    private final ModelPart body;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart beak;
    private final ModelPart rightWing;
    private final ModelPart rightWingTip;
    private final ModelPart leftWing;
    private final ModelPart leftWingTip;
    private final ModelPart tail;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public SculkenRavenPetModel(ModelPart root)
    {
        super(root);

        ModelPart raven = root.getChild("raven");
        this.body = raven.getChild("body");
        this.neck = this.body.getChild("neck");
        this.head = this.neck.getChild("head");
        this.beak = this.head.getChild("beak");
        this.rightWing = this.body.getChild("wing");
        this.rightWingTip = this.rightWing.getChild("wingtip");
        this.leftWing = this.body.getChild("wing1");
        this.leftWingTip = this.leftWing.getChild("wingtip1");
        this.tail = this.body.getChild("tail");
        this.rightLeg = this.body.getChild("rearleg");
        this.leftLeg = this.body.getChild("rearleg1");
    }

    public static LayerDefinition createBodyLayer()
    {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        PartDefinition raven = root.addOrReplaceChild(
                "raven",
                CubeListBuilder.create(),
                PartPose.offset(0.00F, -12.00F, -24.00F));

        PartDefinition body = raven.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(224, 136).addBox(-12.00F, 0.00F, -3.00F, 24.00F, 23.50F, 45.00F, new CubeDeformation(-0.10F))
                        .texOffs(114, 248).addBox(-1.00F, -6.00F, -10.00F, 2.00F, 6.00F, 0.00F),
                PartPose.offset(0.00F, -20.00F, 8.00F));

        body.addOrReplaceChild(
                "body_r1",
                CubeListBuilder.create()
                        .texOffs(0, 248).addBox(-12.00F, -53.00F, 5.00F, 25.00F, 21.50F, 32.00F),
                PartPose.offsetAndRotation(0.00F, 63.00F, 0.00F, 0.2618F, 0.00F, 0.00F));

        PartDefinition neck = body.addOrReplaceChild(
                "neck",
                CubeListBuilder.create(),
                PartPose.offset(0.00F, 10.78463F, -5.17038F));

        neck.addOrReplaceChild(
                "neck_r1",
                CubeListBuilder.create()
                        .texOffs(224, 204).addBox(-10.00F, -8.50F, -24.80F, 20.00F, 17.70F, 37.60F, new CubeDeformation(0.10F)),
                PartPose.offsetAndRotation(0.00F, 2.59708F, 5.34953F, -0.7854F, 0.00F, 0.00F));

        PartDefinition head = neck.addOrReplaceChild(
                "head",
                CubeListBuilder.create(),
                PartPose.offset(0.00F, -12.79848F, -8.51934F));

        head.addOrReplaceChild(
                "head_r1",
                CubeListBuilder.create()
                        .texOffs(114, 282).addBox(-8.00F, -7.00F, -14.30F, 16.00F, 9.30F, 22.30F),
                PartPose.offsetAndRotation(0.00F, 0.04974F, -1.56724F, 0.43633F, 0.00F, 0.00F));

        head.addOrReplaceChild(
                "head_r2",
                CubeListBuilder.create()
                        .texOffs(0, 301).addBox(-8.00F, -6.00F, -14.30F, 16.00F, 17.30F, 12.30F, new CubeDeformation(0.10F)),
                PartPose.offsetAndRotation(0.00F, -2.38417F, -2.22086F, 1.13446F, 0.00F, 0.00F));

        PartDefinition beak = head.addOrReplaceChild(
                "beak",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(-0.15251F, 5.28344F, -15.03928F, 0.69813F, 0.00F, 0.00F));

        beak.addOrReplaceChild(
                "beak_r1",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-6.00F, -1.50F, -5.60F, 12.00F, 3.00F, 11.90F),
                PartPose.offsetAndRotation(-0.24749F, 1.50F, -0.24749F, 0.00F, 0.7854F, 0.00F));

        beak.addOrReplaceChild(
                "beak_r2",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-6.00F, -1.50F, -5.60F, 12.00F, 3.00F, 11.90F),
                PartPose.offsetAndRotation(-0.24749F, -1.50F, -0.24749F, 0.00F, 0.7854F, 0.00F));

        PartDefinition wing = body.addOrReplaceChild(
                "wing",
                CubeListBuilder.create()
                        .texOffs(0, 192).mirror(true).addBox(0.00F, -1.00F, 2.00F, 56.00F, 1.00F, 56.00F, new CubeDeformation(0.01F))
                        .texOffs(242, 258).addBox(0.00F, -4.00F, -4.00F, 56.00F, 8.00F, 8.00F),
                PartPose.offsetAndRotation(12.00F, 1.00F, -6.00F, 0.00F, -0.17453F, -0.17453F));

        wing.addOrReplaceChild(
                "wingtip",
                CubeListBuilder.create()
                        .texOffs(224, 80).mirror(true).addBox(0.00F, -1.00F, 4.00F, 56.00F, 1.00F, 56.00F, new CubeDeformation(0.01F))
                        .texOffs(234, 274).addBox(0.00F, -2.00F, 0.00F, 56.00F, 4.00F, 4.00F),
                PartPose.offsetAndRotation(56.00F, 0.00F, -2.00F, 0.00F, 0.00F, 0.34907F));

        PartDefinition rearleg = body.addOrReplaceChild(
                "rearleg",
                CubeListBuilder.create()
                        .texOffs(56, 301).addBox(-6.00F, -0.54672F, -6.03319F, 12.00F, 22.00F, 12.00F),
                PartPose.offsetAndRotation(13.00F, 17.87298F, 37.49799F, 0.82903F, 0.00F, 0.00F));

        PartDefinition rearlegtip2 = rearleg.addOrReplaceChild(
                "rearlegtip2",
                CubeListBuilder.create()
                        .texOffs(278, 305).addBox(-3.00F, 0.82347F, -2.70926F, 6.00F, 16.00F, 6.00F),
                PartPose.offset(0.00F, 20.62981F, -1.32393F));

        rearlegtip2.addOrReplaceChild(
                "rearfoot2",
                CubeListBuilder.create()
                        .texOffs(302, 34).addBox(20.00F, -2.00F, -11.50F, 12.00F, 3.00F, 15.00F)
                        .texOffs(190, 282).addBox(18.00F, 0.00F, -19.50F, 16.00F, 0.00F, 23.00F),
                PartPose.offset(-26.00F, 16.17652F, -0.29074F));

        PartDefinition wing1 = body.addOrReplaceChild(
                "wing1",
                CubeListBuilder.create()
                        .texOffs(242, 258).addBox(-56.00F, -4.00F, -4.00F, 56.00F, 8.00F, 8.00F)
                        .texOffs(0, 192).addBox(-56.00F, -1.00F, 2.00F, 56.00F, 1.00F, 56.00F, new CubeDeformation(0.01F)),
                PartPose.offsetAndRotation(-12.00F, 1.00F, -6.00F, 0.00F, 0.17453F, 0.17453F));

        wing1.addOrReplaceChild(
                "wingtip1",
                CubeListBuilder.create()
                        .texOffs(234, 274).addBox(-56.00F, -2.00F, 0.00F, 56.00F, 4.00F, 4.00F)
                        .texOffs(224, 80).addBox(-56.00F, -1.00F, 4.00F, 56.00F, 1.00F, 56.00F, new CubeDeformation(0.01F)),
                PartPose.offsetAndRotation(-56.00F, 0.00F, -2.00F, 0.00F, 0.00F, -0.34907F));

        PartDefinition tail = body.addOrReplaceChild(
                "tail",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-36.00F, -2.00F, -14.00F, 71.00F, 0.00F, 80.00F),
                PartPose.offset(0.00F, 6.00F, 48.00F));

        tail.addOrReplaceChild(
                "tail_end",
                CubeListBuilder.create(),
                PartPose.offset(0.00F, 0.00F, 0.00F));

        PartDefinition rearleg1 = body.addOrReplaceChild(
                "rearleg1",
                CubeListBuilder.create()
                        .texOffs(302, 0).addBox(-6.00F, -0.54672F, -6.03319F, 12.00F, 22.00F, 12.00F),
                PartPose.offsetAndRotation(-13.00F, 17.87298F, 37.49799F, 0.82903F, 0.00F, 0.00F));

        PartDefinition rearlegtip3 = rearleg1.addOrReplaceChild(
                "rearlegtip3",
                CubeListBuilder.create()
                        .texOffs(302, 305).addBox(-3.00F, 0.82347F, -2.70926F, 6.00F, 16.00F, 6.00F),
                PartPose.offset(0.00F, 20.62981F, -1.32393F));

        rearlegtip3.addOrReplaceChild(
                "rearfoot3",
                CubeListBuilder.create()
                        .texOffs(302, 34).addBox(-6.00F, -2.00F, -11.50F, 12.00F, 3.00F, 15.00F)
                        .texOffs(190, 282).addBox(-8.00F, 0.00F, -19.50F, 16.00F, 0.00F, 23.00F),
                PartPose.offset(0.00F, 16.17652F, -0.29074F));

        return LayerDefinition.create(meshDefinition, 512, 512);
    }

    @Override
    public void setupAnim(LivingEntityRenderState renderState)
    {
        super.setupAnim(renderState);

        float time = renderState.ageInTicks;
        float walkPosition = renderState.walkAnimationPos * 0.6662F;
        float walkSpeed = renderState.walkAnimationSpeed;

        // 지상 펫이므로 날개를 몸통 옆으로 접는다.
        this.rightWing.yRot -= WING_FOLD_YAW;
        this.rightWing.zRot += WING_FOLD_DROOP;
        this.rightWingTip.yRot += WING_TIP_FOLD_YAW;
        this.leftWing.yRot += WING_FOLD_YAW;
        this.leftWing.zRot -= WING_FOLD_DROOP;
        this.leftWingTip.yRot -= WING_TIP_FOLD_YAW;

        // 시선을 따라 목과 머리가 움직인다.
        this.neck.yRot += renderState.yRot * Mth.DEG_TO_RAD * 0.4F;
        this.head.xRot += renderState.xRot * Mth.DEG_TO_RAD * 0.5F;
        this.head.yRot += renderState.yRot * Mth.DEG_TO_RAD * 0.3F;

        // 두 다리를 번갈아 내딛고, 까마귀처럼 걸음에 맞춰 고개를 끄덕인다.
        float swing = Mth.cos(walkPosition) * 0.6F * walkSpeed;
        this.rightLeg.xRot += swing;
        this.leftLeg.xRot -= swing;
        this.neck.xRot += Mth.cos(walkPosition * 2.0F) * 0.1F * walkSpeed;

        // 걷는 동안 접힌 날개가 가볍게 들썩인다.
        float shuffle = Math.abs(Mth.sin(walkPosition)) * 0.08F * walkSpeed;
        this.rightWing.zRot += shuffle;
        this.leftWing.zRot -= shuffle;

        // 대기 연출: 꼬리를 부채질하고 이따금씩 부리를 딱딱거린다.
        this.tail.xRot += Mth.sin(time * 0.08F) * 0.05F;
        this.tail.yRot += Mth.sin(time * 0.05F) * 0.08F;
        float peck = Math.max(0.0F, Mth.sin(time * 0.06F) - 0.92F) * 3.0F;
        this.beak.xRot += peck;
    }
}
