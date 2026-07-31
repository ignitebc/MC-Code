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
 * 일반 펫: 커비(kirby.bbmodel).
 * 원본은 구체 메시 모델이라 구 단면을 따라 쌓은 슬랩 큐브로 옮겼고,
 * 텍스처는 각 면에서 구 중심 방향으로 레이캐스트해 원본 픽셀을 투영해 구웠다.
 */
public class KirbyPetModel extends EntityModel<LivingEntityRenderState>
{
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "kirby_pet"),
            "main");

    private final ModelPart kirby;
    private final ModelPart body;
    private final ModelPart handLeft;
    private final ModelPart handRight;
    private final ModelPart footLeft;
    private final ModelPart footRight;

    public KirbyPetModel(ModelPart root)
    {
        super(root);

        this.kirby = root.getChild("kirby");
        this.body = this.kirby.getChild("body");
        this.handLeft = this.kirby.getChild("hand_left");
        this.handRight = this.kirby.getChild("hand_right");
        this.footLeft = this.kirby.getChild("foot_left");
        this.footRight = this.kirby.getChild("foot_right");
    }

    public static LayerDefinition createBodyLayer()
    {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        PartDefinition kirby = root.addOrReplaceChild(
                "kirby",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(-0.04029F, 15.66252F, -0.04F, 0.00F, 1.5708F, 0.00F));

        kirby.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(74, 60).addBox(-4.50F, 6.80F, -4.50F, 9.00F, 1.70F, 9.00F)
                        .texOffs(0, 22).addBox(-7.50F, 4.20F, -7.50F, 15.00F, 2.60F, 15.00F)
                        .texOffs(61, 22).addBox(-7.50F, 1.40F, -7.50F, 15.00F, 2.80F, 15.00F)
                        .texOffs(0, 0).addBox(-9.00F, -1.40F, -9.00F, 18.00F, 2.80F, 18.00F)
                        .texOffs(0, 41).addBox(-7.50F, -4.20F, -7.50F, 15.00F, 2.80F, 15.00F)
                        .texOffs(61, 41).addBox(-7.50F, -6.80F, -7.50F, 15.00F, 2.60F, 15.00F)
                        .texOffs(0, 73).addBox(-4.50F, -8.50F, -4.50F, 9.00F, 1.70F, 9.00F),
                PartPose.offset(0.04029F, -2.66252F, 0.04F));

        kirby.addOrReplaceChild(
                "hand_left",
                CubeListBuilder.create()
                        .texOffs(33, 85).addBox(-2.00F, 2.60F, -2.00F, 4.00F, 1.80F, 4.00F)
                        .texOffs(37, 73).addBox(-2.50F, -2.60F, -2.50F, 5.00F, 5.20F, 5.00F)
                        .texOffs(50, 85).addBox(-2.00F, -4.40F, -2.00F, 4.00F, 1.80F, 4.00F),
                PartPose.offsetAndRotation(0.04029F, -1.76252F, 8.34F, -2.11429F, 0.18077F, -0.16531F));

        kirby.addOrReplaceChild(
                "hand_right",
                CubeListBuilder.create()
                        .texOffs(67, 85).addBox(-2.00F, 2.60F, -2.00F, 4.00F, 1.80F, 4.00F)
                        .texOffs(58, 73).addBox(-2.50F, -2.60F, -2.50F, 5.00F, 5.20F, 5.00F)
                        .texOffs(84, 85).addBox(-2.00F, -4.40F, -2.00F, 4.00F, 1.80F, 4.00F),
                PartPose.offsetAndRotation(0.04029F, -5.06252F, -7.46F, -2.46091F, 0.00F, -0.24435F));

        kirby.addOrReplaceChild(
                "foot_left",
                CubeListBuilder.create()
                        .texOffs(0, 60).addBox(-4.50F, 2.00F, -4.50F, 9.00F, 2.60F, 9.00F)
                        .texOffs(79, 73).addBox(-4.00F, -0.5F, -4.00F, 8.00F, 2.50F, 8.00F),
                PartPose.offsetAndRotation(2.14029F, 3.34016F, 2.84F, 0.00F, 0.00F, -0.20944F));

        kirby.addOrReplaceChild(
                "foot_right",
                CubeListBuilder.create()
                        .texOffs(37, 60).addBox(-4.50F, 2.00F, -4.50F, 9.00F, 2.60F, 9.00F)
                        .texOffs(0, 85).addBox(-4.00F, -0.5F, -4.00F, 8.00F, 2.50F, 8.00F),
                PartPose.offsetAndRotation(-2.25971F, 3.34016F, -3.76F, 0.00F, 0.00F, 0.61087F));

        return LayerDefinition.create(meshDefinition, 128, 128);
    }

    @Override
    public void setupAnim(LivingEntityRenderState renderState)
    {
        super.setupAnim(renderState);

        float time = renderState.ageInTicks;
        float walkPosition = renderState.walkAnimationPos * 0.6662F;
        float walkSpeed = renderState.walkAnimationSpeed;

        // 몸 전체가 시선을 따라 살짝 돈다. (머리가 따로 없는 몸통형 캐릭터)
        this.kirby.yRot += renderState.yRot * Mth.DEG_TO_RAD * 0.3F;
        this.body.xRot += renderState.xRot * Mth.DEG_TO_RAD * 0.3F;

        // 통통 튀는 걸음: 발을 번갈아 내딛고 몸이 크게 바운스한다.
        float swing = Mth.cos(walkPosition) * 0.8F * walkSpeed;
        this.footLeft.xRot += swing;
        this.footRight.xRot -= swing;
        this.handLeft.xRot += swing * 0.4F;
        this.handRight.xRot -= swing * 0.4F;
        this.kirby.y -= Math.abs(Mth.sin(walkPosition)) * 1.5F * walkSpeed;
        this.kirby.zRot += Mth.cos(walkPosition) * 0.08F * walkSpeed;

        // 대기 중에는 풍선처럼 천천히 숨을 쉬듯 흔들린다.
        this.kirby.y += Mth.sin(time * 0.1F) * 0.4F;
        this.handLeft.zRot += Mth.sin(time * 0.08F) * 0.05F;
        this.handRight.zRot -= Mth.sin(time * 0.08F) * 0.05F;
    }
}
