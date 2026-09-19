package com.autovw.advancednetherite.client.model;

import com.autovw.advancednetherite.AdvancedNetherite;
import com.autovw.advancednetherite.client.model.mesh.GomiPetMesh;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * 일반 꼬미 모델. 참고 이미지의 복셀 피규어(머리가 몸보다 큰 SD 비율)를 그대로 옮겼다.
 * <p>
 * 형태와 텍스처는 design/pets/dogs.py 에서 복셀로 조각해 {@link GomiPetMesh} 로 생성한다.
 * 이 클래스는 파트를 찾아 움직이는 일만 맡는다. 좌표는 Renderer 스케일 1.0 기준이고
 * 최종 크기는 Renderer 의 scale 에서만 조정한다.
 */
public class GomiPetModel extends EntityModel<LivingEntityRenderState>
{
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "gomi_pet"),
            "main");

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
        return GomiPetMesh.create();
    }

    @Override
    public void setupAnim(LivingEntityRenderState renderState)
    {
        super.setupAnim(renderState);

        this.head.xRot += renderState.xRot * Mth.DEG_TO_RAD;
        this.head.yRot += renderState.yRot * Mth.DEG_TO_RAD;

        float walkPosition = renderState.walkAnimationPos * 0.6662F;
        float walkSpeed = renderState.walkAnimationSpeed;
        this.frontLeftLeg.xRot += Mth.cos(walkPosition) * 1.1F * walkSpeed;
        this.frontRightLeg.xRot += Mth.cos(walkPosition + Mth.PI) * 1.1F * walkSpeed;
        this.backLeftLeg.xRot += Mth.cos(walkPosition + Mth.PI) * 1.1F * walkSpeed;
        this.backRightLeg.xRot += Mth.cos(walkPosition) * 1.1F * walkSpeed;

        // 3단 꼬리는 끝으로 갈수록 흔들림을 줄여 말린 덩어리가 함께 흔들리는 느낌을 낸다
        float wag = Mth.sin(renderState.ageInTicks * 0.25F) * 0.2F;
        this.tailBase.yRot += wag;
        this.tailCurve.yRot += wag * 0.5F;
        this.tailTip.yRot += wag * 0.3F;
    }
}
