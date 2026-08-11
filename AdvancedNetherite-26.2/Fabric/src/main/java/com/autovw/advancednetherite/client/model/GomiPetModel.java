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
 * 일반 꼬미 모델. 참고 이미지의 SD 비율(머리가 몸보다 큰 복셀 피규어)에 맞춰
 * 기존 네발동물 비율을 대폭 수정했다.
 * <p>
 * 핵심 실루엣: 몸보다 넓은 머리(폭 10.5 vs 몸 7.8), 짧고 통통한 몸(길이 8.5),
 * 짧고 굵은 다리, 넓은 볼과 돌출 주둥이, 계단형 귀, 등 위로 크게 말린 3단 꼬리,
 * 흰 줄무늬가 지오메트리로 살아 있는 남색 조끼.
 * <p>
 * 텍스처는 256x256을 64px 격자 팔레트로 사용한다. 좌표는 Renderer 스케일 1.0
 * 기준으로 잡았고 최종 크기는 Renderer 의 scale 에서만 조정한다.
 */
public class GomiPetModel extends EntityModel<LivingEntityRenderState>
{
    private static final int TEXTURE_SIZE = 256;

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "gomi_pet"),
            "main");

    // 64px 격자 팔레트 좌표 (gomi_pet.png 생성 스크립트와 일치해야 함)
    private static final int TAN_U = 0, TAN_V = 0;
    private static final int LIGHT_TAN_U = 64, LIGHT_TAN_V = 0;
    private static final int CREAM_U = 128, CREAM_V = 0;
    private static final int DARK_TAN_U = 192, DARK_TAN_V = 0;
    private static final int PALE_CREAM_U = 0, PALE_CREAM_V = 64;
    private static final int NAVY_U = 64, NAVY_V = 64;
    private static final int STRIPE_U = 192, STRIPE_V = 64;
    private static final int BLACK_U = 0, BLACK_V = 128;
    private static final int WHITE_U = 64, WHITE_V = 128;
    private static final int PINK_U = 128, PINK_V = 128;
    private static final int MOUTH_U = 192, MOUTH_V = 128;

    private final ModelPart head;
    private final ModelPart frontLeftLeg;
    private final ModelPart frontRightLeg;
    private final ModelPart backLeftLeg;
    private final ModelPart backRightLeg;
    private final ModelPart tailBase;
    private final ModelPart tailCurve;
    private final ModelPart tailTip;

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
        this.tailCurve = this.tailBase.getChild("tail_curve");
        this.tailTip = this.tailCurve.getChild("tail_tip");
    }

    public static LayerDefinition createBodyLayer()
    {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        // 짧고 통통한 몸통. 다리 바닥이 정확히 지면(y=24)에 닿도록 y 오프셋을 맞췄다.
        PartDefinition body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(CREAM_U, CREAM_V).addBox(-3.9F, -3.5F, -4.25F, 7.8F, 7.0F, 8.5F),
                PartPose.offset(0.0F, 15.4F, 0.5F));

        addVest(body);
        addHead(body);
        addLeg(body, "front_left_leg", 2.2F, -2.6F);
        addLeg(body, "front_right_leg", -2.2F, -2.6F);
        addLeg(body, "back_left_leg", 2.2F, 2.6F);
        addLeg(body, "back_right_leg", -2.2F, 2.6F);
        addCurledTail(body);

        return LayerDefinition.create(meshDefinition, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    private static void addVest(PartDefinition body)
    {
        // 몸 전체를 덮는 외피가 아니라 앞가슴과 몸통 앞쪽 절반을 감싸는 조끼 형태
        body.addOrReplaceChild(
                "vest",
                CubeListBuilder.create()
                        .texOffs(NAVY_U, NAVY_V).addBox(-4.05F, -3.0F, -4.55F, 8.1F, 5.4F, 0.9F)
                        .texOffs(NAVY_U, NAVY_V).addBox(3.85F, -3.2F, -4.3F, 0.9F, 5.6F, 5.6F)
                        .texOffs(NAVY_U, NAVY_V).addBox(-4.75F, -3.2F, -4.3F, 0.9F, 5.6F, 5.6F)
                        .texOffs(NAVY_U, NAVY_V).addBox(-4.0F, -3.85F, -4.3F, 8.0F, 0.9F, 5.6F)
                        .texOffs(NAVY_U, NAVY_V).addBox(-4.0F, 2.9F, -4.3F, 8.0F, 0.9F, 5.6F),
                PartPose.ZERO);

        // 흰 줄무늬는 텍스처가 아니라 얇은 큐브로 만들어 측면에서도 두께가 보이게 유지
        body.addOrReplaceChild(
                "vest_stripes",
                CubeListBuilder.create()
                        .texOffs(STRIPE_U, STRIPE_V).addBox(-4.15F, -0.9F, -4.75F, 8.3F, 1.3F, 0.5F)
                        .texOffs(STRIPE_U, STRIPE_V).addBox(-0.7F, -2.9F, -4.8F, 1.4F, 5.4F, 0.5F)
                        .texOffs(STRIPE_U, STRIPE_V).addBox(-0.7F, -4.0F, -3.8F, 1.4F, 0.45F, 4.8F),
                PartPose.ZERO);
        body.addOrReplaceChild(
                "left_vest_stripe",
                CubeListBuilder.create().texOffs(STRIPE_U, STRIPE_V)
                        .addBox(-0.3F, -0.65F, -2.8F, 0.6F, 1.3F, 5.6F),
                PartPose.offsetAndRotation(4.55F, -0.9F, -1.4F, 0.45F, 0.0F, 0.0F));
        body.addOrReplaceChild(
                "right_vest_stripe",
                CubeListBuilder.create().texOffs(STRIPE_U, STRIPE_V)
                        .addBox(-0.3F, -0.65F, -2.8F, 0.6F, 1.3F, 5.6F),
                PartPose.offsetAndRotation(-4.55F, -0.9F, -1.4F, 0.45F, 0.0F, 0.0F));
    }

    private static void addHead(PartDefinition body)
    {
        // 머리가 몸보다 넓은 것이 핵심 실루엣 (머리 폭 10.5 vs 몸 폭 7.8)
        PartDefinition head = body.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(TAN_U, TAN_V).addBox(-5.25F, -7.0F, -6.9F, 10.5F, 8.5F, 7.5F)
                        .texOffs(CREAM_U, CREAM_V).addBox(-4.5F, -2.0F, -7.2F, 9.0F, 3.3F, 3.5F),
                PartPose.offset(0.0F, -3.0F, -3.4F));

        addEar(head, "left_ear", 3.3F, 0.08F);
        addEar(head, "right_ear", -3.3F, -0.08F);

        head.addOrReplaceChild(
                "left_cheek",
                CubeListBuilder.create().texOffs(CREAM_U, CREAM_V)
                        .addBox(-2.3F, -2.1F, -1.0F, 4.6F, 4.2F, 2.0F),
                PartPose.offset(3.5F, -0.6F, -6.6F));
        head.addOrReplaceChild(
                "right_cheek",
                CubeListBuilder.create().texOffs(CREAM_U, CREAM_V)
                        .addBox(-2.3F, -2.1F, -1.0F, 4.6F, 4.2F, 2.0F),
                PartPose.offset(-3.5F, -0.6F, -6.6F));

        head.addOrReplaceChild(
                "muzzle",
                CubeListBuilder.create().texOffs(CREAM_U, CREAM_V)
                        .addBox(-2.35F, -1.5F, -2.9F, 4.7F, 3.1F, 2.9F),
                PartPose.offset(0.0F, 0.0F, -6.9F));
        head.addOrReplaceChild(
                "nose",
                CubeListBuilder.create().texOffs(BLACK_U, BLACK_V)
                        .addBox(-0.9F, -0.6F, -0.65F, 1.8F, 1.2F, 1.1F),
                PartPose.offset(0.0F, -1.1F, -9.7F));
        head.addOrReplaceChild(
                "mouth",
                CubeListBuilder.create().texOffs(MOUTH_U, MOUTH_V)
                        .addBox(-1.5F, -0.6F, -0.45F, 3.0F, 1.3F, 0.8F),
                PartPose.offset(0.0F, 1.3F, -9.6F));
        head.addOrReplaceChild(
                "tongue",
                CubeListBuilder.create().texOffs(PINK_U, PINK_V)
                        .addBox(-1.05F, -0.4F, -0.55F, 2.1F, 2.1F, 1.0F),
                PartPose.offset(0.0F, 2.2F, -9.5F));

        head.addOrReplaceChild(
                "left_eye",
                CubeListBuilder.create()
                        .texOffs(BLACK_U, BLACK_V).addBox(-0.9F, -0.9F, -0.45F, 1.8F, 1.8F, 0.8F)
                        .texOffs(WHITE_U, WHITE_V).addBox(0.12F, -0.62F, -0.62F, 0.5F, 0.5F, 0.3F),
                PartPose.offset(2.6F, -3.2F, -7.0F));
        head.addOrReplaceChild(
                "right_eye",
                CubeListBuilder.create()
                        .texOffs(BLACK_U, BLACK_V).addBox(-0.9F, -0.9F, -0.45F, 1.8F, 1.8F, 0.8F)
                        .texOffs(WHITE_U, WHITE_V).addBox(-0.62F, -0.62F, -0.62F, 0.5F, 0.5F, 0.3F),
                PartPose.offset(-2.6F, -3.2F, -7.0F));
    }

    private static void addEar(PartDefinition head, String name, float x, float tilt)
    {
        // 굵은 밑단 위에 좁은 윗단을 얹은 사각 계단형 귀 + 안쪽 진한 색
        head.addOrReplaceChild(
                name,
                CubeListBuilder.create()
                        .texOffs(TAN_U, TAN_V).addBox(-1.5F, -2.0F, -1.0F, 3.0F, 2.0F, 2.0F)
                        .texOffs(TAN_U, TAN_V).addBox(-1.0F, -3.8F, -1.0F, 2.0F, 2.0F, 2.0F)
                        .texOffs(DARK_TAN_U, DARK_TAN_V).addBox(-0.8F, -1.8F, -1.15F, 1.6F, 1.5F, 0.4F),
                PartPose.offsetAndRotation(x, -6.9F, -3.0F, 0.0F, 0.0F, tilt));
    }

    private static void addLeg(PartDefinition body, String name, float x, float z)
    {
        // 짧고 굵은 다리 + 네모난 발
        body.addOrReplaceChild(
                name,
                CubeListBuilder.create()
                        .texOffs(CREAM_U, CREAM_V).addBox(-1.6F, 0.0F, -1.6F, 3.2F, 4.3F, 3.2F)
                        .texOffs(PALE_CREAM_U, PALE_CREAM_V).addBox(-1.9F, 4.3F, -2.2F, 3.8F, 1.8F, 4.0F),
                PartPose.offset(x, 2.5F, z));
    }

    private static void addCurledTail(PartDefinition body)
    {
        // 등 위에서 크게 한 바퀴 말린 3단 꼬리. 끝으로 갈수록 크고 밝아진다.
        PartDefinition tailBase = body.addOrReplaceChild(
                "tail_base",
                CubeListBuilder.create().texOffs(TAN_U, TAN_V)
                        .addBox(-1.6F, -1.5F, -0.4F, 3.2F, 3.0F, 3.0F),
                PartPose.offset(0.0F, -3.2F, 3.6F));
        PartDefinition tailCurve = tailBase.addOrReplaceChild(
                "tail_curve",
                CubeListBuilder.create().texOffs(LIGHT_TAN_U, LIGHT_TAN_V)
                        .addBox(-2.0F, -2.4F, -1.2F, 4.0F, 3.2F, 3.6F),
                PartPose.offset(0.0F, -2.6F, 1.8F));
        tailCurve.addOrReplaceChild(
                "tail_tip",
                CubeListBuilder.create().texOffs(PALE_CREAM_U, PALE_CREAM_V)
                        .addBox(-2.3F, -2.0F, -3.2F, 4.6F, 3.8F, 3.6F),
                PartPose.offset(0.0F, -1.8F, -1.4F));
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

        // 3단 꼬리는 끝으로 갈수록 흔들림을 줄여 말린 덩어리가 함께 흔들리는 느낌을 낸다
        float wag = Mth.sin(renderState.ageInTicks * 0.25F) * 0.2F;
        this.tailBase.yRot = wag;
        this.tailCurve.yRot = wag * 0.5F;
        this.tailTip.yRot = wag * 0.3F;
    }
}
