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
 * 슈퍼꼬미 전용 모델. 일반 꼬미 모델을 공유하지 않고 참고 이미지 기준으로 새로 만들었다.
 * <p>
 * 핵심 실루엣: 몸보다 큰 머리(폭 11 vs 몸 9), 짧고 통통한 몸(길이 9), 짧은 다리,
 * 돌출된 주둥이와 볼, 계단식 깃털을 겹친 큰 날개, 아래로 갈수록 퍼지는 분절 망토.
 * 깃털 하나는 막대가 아니라 3개의 큐브를 계단으로 쌓아 만들고, 이를 한 날개에 4장 겹친다.
 * <p>
 * 텍스처는 256x256을 64px 격자 16칸으로 나눠 색상 팔레트로 사용한다.
 * 좌표는 Renderer 스케일 1.0 기준으로 잡고 최종 크기는 Renderer 에서만 조정한다.
 */
public class SuperGomiPetModel extends EntityModel<LivingEntityRenderState>
{
    private static final int TEXTURE_SIZE = 256;

    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "super_gomi_pet"),
            "main");

    // 64px 격자 팔레트 좌표 (super_gomi_pet.png 생성 스크립트와 일치해야 함)
    private static final int TAN_U = 0, TAN_V = 0;
    private static final int LIGHT_TAN_U = 64, LIGHT_TAN_V = 0;
    private static final int CREAM_U = 128, CREAM_V = 0;
    private static final int DARK_TAN_U = 192, DARK_TAN_V = 0;
    private static final int BLUE_U = 0, BLUE_V = 64;
    private static final int DARK_BLUE_U = 64, DARK_BLUE_V = 64;
    private static final int RED_U = 128, RED_V = 64;
    private static final int DARK_RED_U = 192, DARK_RED_V = 64;
    private static final int GOLD_U = 0, GOLD_V = 128;
    private static final int YELLOW_U = 64, YELLOW_V = 128;
    private static final int WHITE_U = 128, WHITE_V = 128;
    private static final int WING_SHADOW_U = 192, WING_SHADOW_V = 128;
    private static final int BLACK_U = 0, BLACK_V = 192;
    private static final int PINK_U = 64, PINK_V = 192;
    private static final int MOUTH_U = 128, MOUTH_V = 192;

    private final ModelPart head;
    private final ModelPart frontLeftLeg;
    private final ModelPart frontRightLeg;
    private final ModelPart backLeftLeg;
    private final ModelPart backRightLeg;
    private final ModelPart tailBase;
    private final ModelPart tailTip;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart capeTop;
    private final ModelPart capeMiddle;
    private final ModelPart capeLeftTip;
    private final ModelPart capeRightTip;

    public SuperGomiPetModel(ModelPart root)
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
        this.capeTop = body.getChild("cape_top");
        this.capeMiddle = this.capeTop.getChild("cape_middle");
        this.capeLeftTip = this.capeMiddle.getChild("cape_left_tip");
        this.capeRightTip = this.capeMiddle.getChild("cape_right_tip");
    }

    public static LayerDefinition createBodyLayer()
    {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        // 짧고 통통한 몸통. 다리 바닥이 정확히 지면(y=24)에 닿도록 y 오프셋을 맞췄다.
        PartDefinition body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(TAN_U, TAN_V).addBox(-4.5F, -4.0F, -4.5F, 9.0F, 8.0F, 9.0F),
                PartPose.offset(0.0F, 14.5F, 1.0F));

        addSuit(body);
        addHead(body);
        addLeg(body, "front_left_leg", 2.9F, -2.8F);
        addLeg(body, "front_right_leg", -2.9F, -2.8F);
        addLeg(body, "back_left_leg", 2.9F, 2.8F);
        addLeg(body, "back_right_leg", -2.9F, 2.8F);
        addCurledTail(body);
        addWing(body, "left_wing", false);
        addWing(body, "right_wing", true);
        addCape(body);

        return LayerDefinition.create(meshDefinition, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    private static void addSuit(PartDefinition body)
    {
        body.addOrReplaceChild(
                "outfit",
                CubeListBuilder.create()
                        .texOffs(BLUE_U, BLUE_V).addBox(-4.9F, -4.3F, -4.9F, 9.8F, 8.2F, 9.8F)
                        .texOffs(DARK_BLUE_U, DARK_BLUE_V).addBox(-5.0F, 1.6F, -5.0F, 10.0F, 1.4F, 10.0F)
                        .texOffs(YELLOW_U, YELLOW_V).addBox(-5.05F, 2.9F, -5.05F, 10.1F, 1.1F, 10.1F)
                        .texOffs(RED_U, RED_V).addBox(-5.1F, -4.6F, -5.1F, 10.2F, 1.4F, 10.2F),
                PartPose.ZERO);

        // 가슴 엠블럼: 빨간 방패 테두리 + 금색 방패 + 빨간 발바닥 무늬
        body.addOrReplaceChild(
                "emblem",
                CubeListBuilder.create()
                        .texOffs(RED_U, RED_V).addBox(-3.3F, -3.2F, -0.6F, 6.6F, 6.4F, 0.7F)
                        .texOffs(GOLD_U, GOLD_V).addBox(-2.6F, -2.5F, -1.0F, 5.2F, 5.0F, 0.7F)
                        .texOffs(RED_U, RED_V).addBox(-1.2F, 0.1F, -1.35F, 2.4F, 1.7F, 0.6F)
                        .texOffs(RED_U, RED_V).addBox(-1.7F, -1.1F, -1.35F, 0.9F, 0.9F, 0.6F)
                        .texOffs(RED_U, RED_V).addBox(-0.45F, -1.5F, -1.35F, 0.9F, 0.9F, 0.6F)
                        .texOffs(RED_U, RED_V).addBox(0.8F, -1.1F, -1.35F, 0.9F, 0.9F, 0.6F),
                // 뒷면이 슈트 앞면과 같은 평면에 놓이지 않도록 살짝 파묻는다
                PartPose.offset(0.0F, -0.6F, -4.95F));
    }

    private static void addHead(PartDefinition body)
    {
        // 머리가 몸보다 큰 것이 핵심 실루엣 (머리 폭 11 vs 몸 폭 9)
        PartDefinition head = body.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(TAN_U, TAN_V).addBox(-5.5F, -7.4F, -7.4F, 11.0F, 9.0F, 8.0F)
                        .texOffs(CREAM_U, CREAM_V).addBox(-4.7F, -1.8F, -7.7F, 9.4F, 3.5F, 3.4F),
                PartPose.offset(0.0F, -3.5F, -3.6F));

        addEar(head, "left_ear", 3.5F, 0.10F);
        addEar(head, "right_ear", -3.5F, -0.10F);

        head.addOrReplaceChild(
                "left_cheek",
                CubeListBuilder.create().texOffs(CREAM_U, CREAM_V)
                        .addBox(-2.4F, -2.0F, -1.1F, 4.8F, 4.0F, 2.2F),
                PartPose.offset(3.8F, -0.7F, -7.0F));
        head.addOrReplaceChild(
                "right_cheek",
                CubeListBuilder.create().texOffs(CREAM_U, CREAM_V)
                        .addBox(-2.4F, -2.0F, -1.1F, 4.8F, 4.0F, 2.2F),
                PartPose.offset(-3.8F, -0.7F, -7.0F));

        head.addOrReplaceChild(
                "muzzle",
                CubeListBuilder.create().texOffs(CREAM_U, CREAM_V)
                        .addBox(-2.6F, -1.7F, -3.1F, 5.2F, 3.4F, 3.1F),
                PartPose.offset(0.0F, 0.1F, -7.4F));
        head.addOrReplaceChild(
                "nose",
                CubeListBuilder.create().texOffs(BLACK_U, BLACK_V)
                        .addBox(-1.1F, -0.7F, -0.7F, 2.2F, 1.4F, 1.2F),
                PartPose.offset(0.0F, -1.2F, -10.4F));
        head.addOrReplaceChild(
                "mouth",
                CubeListBuilder.create().texOffs(MOUTH_U, MOUTH_V)
                        .addBox(-1.7F, -0.7F, -0.5F, 3.4F, 1.5F, 0.9F),
                PartPose.offset(0.0F, 1.5F, -10.3F));
        head.addOrReplaceChild(
                "tongue",
                CubeListBuilder.create().texOffs(PINK_U, PINK_V)
                        .addBox(-1.0F, -0.5F, -0.6F, 2.0F, 2.0F, 1.1F),
                PartPose.offset(0.0F, 2.6F, -10.2F));

        head.addOrReplaceChild(
                "left_eye",
                CubeListBuilder.create()
                        .texOffs(BLACK_U, BLACK_V).addBox(-1.0F, -1.0F, -0.45F, 2.0F, 2.0F, 0.8F)
                        .texOffs(WHITE_U, WHITE_V).addBox(0.1F, -0.75F, -0.65F, 0.6F, 0.6F, 0.35F),
                PartPose.offset(2.6F, -3.6F, -7.5F));
        head.addOrReplaceChild(
                "right_eye",
                CubeListBuilder.create()
                        .texOffs(BLACK_U, BLACK_V).addBox(-1.0F, -1.0F, -0.45F, 2.0F, 2.0F, 0.8F)
                        .texOffs(WHITE_U, WHITE_V).addBox(-0.7F, -0.75F, -0.65F, 0.6F, 0.6F, 0.35F),
                PartPose.offset(-2.6F, -3.6F, -7.5F));
    }

    private static void addEar(PartDefinition head, String name, float x, float tilt)
    {
        // 굵은 밑단 위에 좁은 윗단을 얹은 계단형 귀 + 안쪽 분홍
        head.addOrReplaceChild(
                name,
                CubeListBuilder.create()
                        .texOffs(TAN_U, TAN_V).addBox(-1.6F, -2.4F, -1.0F, 3.2F, 2.6F, 2.0F)
                        .texOffs(DARK_TAN_U, DARK_TAN_V).addBox(-1.0F, -3.9F, -1.0F, 2.0F, 1.7F, 2.0F)
                        .texOffs(PINK_U, PINK_V).addBox(-0.9F, -2.1F, -1.15F, 1.8F, 1.7F, 0.4F),
                PartPose.offsetAndRotation(x, -7.3F, -3.3F, 0.0F, 0.0F, tilt));
    }

    private static void addLeg(PartDefinition body, String name, float x, float z)
    {
        // 파란 슈트 다리 + 빨간 부츠단 + 크림색 발
        body.addOrReplaceChild(
                name,
                CubeListBuilder.create()
                        .texOffs(BLUE_U, BLUE_V).addBox(-1.7F, 0.0F, -1.7F, 3.4F, 3.6F, 3.4F)
                        .texOffs(RED_U, RED_V).addBox(-1.85F, 3.3F, -1.85F, 3.7F, 1.7F, 3.7F)
                        .texOffs(CREAM_U, CREAM_V).addBox(-1.95F, 4.8F, -2.4F, 3.9F, 1.7F, 4.3F),
                PartPose.offset(x, 3.0F, z));
    }

    private static void addCurledTail(PartDefinition body)
    {
        PartDefinition tailBase = body.addOrReplaceChild(
                "tail_base",
                CubeListBuilder.create()
                        .texOffs(TAN_U, TAN_V).addBox(-1.5F, -1.4F, -0.2F, 3.0F, 3.0F, 3.2F)
                        .texOffs(LIGHT_TAN_U, LIGHT_TAN_V).addBox(-1.8F, -4.6F, 1.4F, 3.6F, 3.6F, 3.0F),
                PartPose.offset(0.0F, -2.8F, 4.2F));
        tailBase.addOrReplaceChild(
                "tail_tip",
                CubeListBuilder.create().texOffs(CREAM_U, CREAM_V)
                        .addBox(-2.0F, -1.8F, -3.4F, 4.0F, 3.8F, 3.8F),
                PartPose.offset(0.0F, -4.4F, 1.6F));
    }

    private static void addWing(PartDefinition body, String name, boolean mirrored)
    {
        float side = mirrored ? -1.0F : 1.0F;
        PartDefinition wing = body.addOrReplaceChild(
                name,
                CubeListBuilder.create().texOffs(WHITE_U, WHITE_V)
                        .addBox(mirrored ? -3.5F : -0.5F, -1.6F, -1.4F, 4.0F, 3.2F, 2.8F),
                PartPose.offset(side * 4.2F, -3.4F, 2.2F));

        // 위쪽 깃털일수록 길고 가파르게 세워 부채꼴로 겹친다
        addFeather(wing, "feather_top", mirrored, 14.0F, 1.5F, -1.0F, 0.0F, 1.15F, WHITE_U, WHITE_V);
        addFeather(wing, "feather_upper", mirrored, 12.5F, 1.2F, -0.4F, 0.6F, 0.85F, WHITE_U, WHITE_V);
        addFeather(wing, "feather_lower", mirrored, 11.0F, 1.0F, 0.3F, 1.1F, 0.55F, WING_SHADOW_U, WING_SHADOW_V);
        addFeather(wing, "feather_bottom", mirrored, 9.0F, 0.8F, 1.0F, 1.5F, 0.30F, WING_SHADOW_U, WING_SHADOW_V);
    }

    private static void addFeather(
            PartDefinition wing,
            String name,
            boolean mirrored,
            float length,
            float pivotX,
            float pivotY,
            float pivotZ,
            float raiseAngle,
            int textureU,
            int textureV)
    {
        float side = mirrored ? -1.0F : 1.0F;
        float segmentALength = length * 0.58F;
        float segmentBStart = length * 0.32F;
        float segmentBLength = length * 0.48F;
        float segmentCStart = length * 0.60F;
        float segmentCLength = length * 0.40F;

        // 깃털 하나를 계단식 큐브 3개로 만들어 끝으로 갈수록 위로 올라가는 모양을 만든다
        CubeListBuilder featherBuilder = CubeListBuilder.create()
                .texOffs(textureU, textureV)
                .addBox(mirrored ? -segmentALength : 0.0F, -0.65F, -0.65F, segmentALength, 1.3F, 1.3F)
                .texOffs(textureU, textureV)
                .addBox(mirrored ? -segmentBStart - segmentBLength : segmentBStart, -1.85F, -0.65F,
                        segmentBLength, 1.3F, 1.3F)
                .texOffs(textureU, textureV)
                .addBox(mirrored ? -segmentCStart - segmentCLength : segmentCStart, -3.05F, -0.65F,
                        segmentCLength, 1.3F, 1.3F);

        wing.addOrReplaceChild(
                name,
                featherBuilder,
                PartPose.offsetAndRotation(side * pivotX, pivotY, pivotZ, 0.0F, 0.0F, -raiseAngle * side));
    }

    private static void addCape(PartDefinition body)
    {
        // 목에서 시작해 아래로 갈수록 퍼지고 끝이 두 갈래로 갈라지는 분절 망토
        PartDefinition capeTop = body.addOrReplaceChild(
                "cape_top",
                CubeListBuilder.create().texOffs(RED_U, RED_V)
                        .addBox(-5.0F, 0.0F, -0.65F, 10.0F, 4.6F, 1.3F),
                PartPose.offsetAndRotation(0.0F, -3.9F, 4.7F, 0.25F, 0.0F, 0.0F));

        PartDefinition capeMiddle = capeTop.addOrReplaceChild(
                "cape_middle",
                CubeListBuilder.create()
                        .texOffs(RED_U, RED_V).addBox(-4.6F, 0.0F, -0.65F, 9.2F, 4.4F, 1.3F)
                        .texOffs(GOLD_U, GOLD_V).addBox(-1.9F, 0.7F, 0.5F, 3.8F, 3.9F, 0.6F)
                        .texOffs(RED_U, RED_V).addBox(-0.8F, 2.5F, 0.9F, 1.6F, 1.2F, 0.5F)
                        .texOffs(RED_U, RED_V).addBox(-1.3F, 1.6F, 0.9F, 0.7F, 0.7F, 0.5F)
                        .texOffs(RED_U, RED_V).addBox(-0.35F, 1.3F, 0.9F, 0.7F, 0.7F, 0.5F)
                        .texOffs(RED_U, RED_V).addBox(0.6F, 1.6F, 0.9F, 0.7F, 0.7F, 0.5F),
                PartPose.offsetAndRotation(0.0F, 4.5F, 0.15F, 0.12F, 0.0F, 0.0F));

        capeMiddle.addOrReplaceChild(
                "cape_left_tip",
                CubeListBuilder.create().texOffs(DARK_RED_U, DARK_RED_V)
                        .addBox(-2.1F, 0.0F, -0.65F, 4.2F, 3.4F, 1.3F),
                PartPose.offsetAndRotation(-2.5F, 4.4F, 0.0F, 0.10F, 0.0F, 0.0F));
        capeMiddle.addOrReplaceChild(
                "cape_right_tip",
                CubeListBuilder.create().texOffs(DARK_RED_U, DARK_RED_V)
                        .addBox(-2.1F, 0.0F, -0.65F, 4.2F, 3.4F, 1.3F),
                PartPose.offsetAndRotation(2.5F, 4.4F, 0.0F, 0.10F, 0.0F, 0.0F));
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

        // 깃털은 날개 전체에 붙어 함께 회전하고, 날갯짓은 날개 루트만 움직인다
        float flap = Mth.cos(renderState.ageInTicks * 0.35F) * 0.15F;
        this.leftWing.zRot = -0.10F - flap;
        this.rightWing.zRot = 0.10F + flap;

        // 망토는 분절마다 다른 비율로 젖혀 걷거나 날 때 자연스럽게 흔들린다
        float capeSway = walkSpeed * 0.35F + Mth.sin(renderState.ageInTicks * 0.1F) * 0.04F;
        this.capeTop.xRot = 0.25F + capeSway;
        this.capeMiddle.xRot = 0.12F + capeSway * 0.5F;
        this.capeLeftTip.xRot = 0.10F + capeSway * 0.35F;
        this.capeRightTip.xRot = 0.10F + capeSway * 0.35F;
    }
}
