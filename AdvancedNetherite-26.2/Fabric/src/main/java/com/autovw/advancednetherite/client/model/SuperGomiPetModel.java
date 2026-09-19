package com.autovw.advancednetherite.client.model;

import com.autovw.advancednetherite.AdvancedNetherite;
import com.autovw.advancednetherite.client.model.mesh.SuperGomiPetMesh;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * 슈퍼꼬미 모델. 꼬미와 같은 몸에 파란 슈트, 가슴 발바닥 엠블럼, 빨간 망토, 깃털 날개를 입혔다.
 * <p>
 * 형태와 텍스처는 design/pets/dogs.py 에서 복셀로 조각해 {@link SuperGomiPetMesh} 로 생성한다.
 * 망토와 날개의 기본 각도는 메시의 기본 포즈에 들어 있고, 여기서는 그 위에 흔들림만 더한다.
 */
public class SuperGomiPetModel extends EntityModel<LivingEntityRenderState>
{
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "super_gomi_pet"),
            "main");

    private final ModelPart head;
    private final ModelPart frontLeftLeg;
    private final ModelPart frontRightLeg;
    private final ModelPart backLeftLeg;
    private final ModelPart backRightLeg;
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
        this.leftWing = body.getChild("left_wing");
        this.rightWing = body.getChild("right_wing");
        this.capeTop = body.getChild("cape_top");
        this.capeMiddle = this.capeTop.getChild("cape_middle");
        this.capeLeftTip = this.capeMiddle.getChild("cape_left_tip");
        this.capeRightTip = this.capeMiddle.getChild("cape_right_tip");
    }

    public static LayerDefinition createBodyLayer()
    {
        return SuperGomiPetMesh.create();
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

        // 날갯짓은 날개 루트만 움직인다. 달릴수록 크게 퍼덕인다.
        float flap = Mth.cos(renderState.ageInTicks * 0.35F) * (0.15F + walkSpeed * 0.2F);
        this.leftWing.zRot += -flap;
        this.rightWing.zRot += flap;

        // 망토 윗단은 등에 얹혀 있어 조금만 들리고, 아랫단일수록 크게 나부낀다
        float capeSway = walkSpeed * 0.45F + Mth.sin(renderState.ageInTicks * 0.1F) * 0.05F;
        this.capeTop.xRot += capeSway * 0.25F;
        this.capeMiddle.xRot += capeSway;
        this.capeLeftTip.xRot += capeSway * 0.5F;
        this.capeRightTip.xRot += capeSway * 0.5F;
    }
}
