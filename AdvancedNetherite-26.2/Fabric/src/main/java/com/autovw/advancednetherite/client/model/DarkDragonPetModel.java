package com.autovw.advancednetherite.client.model;

import com.autovw.advancednetherite.AdvancedNetherite;
import com.autovw.advancednetherite.client.model.mesh.DarkDragonPetMesh;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * 레전더리 펫: 암흑드래곤.
 * 흑색 비늘 + 백색 두개골 장갑판·뿔·날개 가시, 보라 날개막(너덜너덜한 뒷단),
 * 보라 발광 눈·입, 꼬리 하단 금색 콘셉트.
 * <p>
 * 형태와 텍스처는 design/pets/legendaries.py 에서 복셀로 조각해 {@link DarkDragonPetMesh} 로 생성한다.
 * 목·턱·날개의 기본 각도는 메시의 기본 포즈에 들어 있고, 여기서는 그 위에 움직임만 더한다.
 */
public class DarkDragonPetModel extends EntityModel<LivingEntityRenderState>
{
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "dark_dragon_pet"),
            "main");

    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart wingLeft;
    private final ModelPart wingRight;
    private final ModelPart wingLeftOuter;
    private final ModelPart wingRightOuter;
    private final ModelPart tail1;
    private final ModelPart tail2;
    private final ModelPart tail3;
    private final ModelPart frontLeftLeg;
    private final ModelPart frontRightLeg;
    private final ModelPart hindLeftLeg;
    private final ModelPart hindRightLeg;

    public DarkDragonPetModel(ModelPart root)
    {
        super(root);

        ModelPart body = root.getChild("body");
        this.neck = body.getChild("neck");
        this.head = this.neck.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.wingLeft = body.getChild("wing_left");
        this.wingLeftOuter = this.wingLeft.getChild("wing_left_outer");
        this.wingRight = body.getChild("wing_right");
        this.wingRightOuter = this.wingRight.getChild("wing_right_outer");
        this.tail1 = body.getChild("tail_1");
        this.tail2 = this.tail1.getChild("tail_2");
        this.tail3 = this.tail2.getChild("tail_3");
        this.frontLeftLeg = body.getChild("front_left_leg");
        this.frontRightLeg = body.getChild("front_right_leg");
        this.hindLeftLeg = body.getChild("hind_left_leg");
        this.hindRightLeg = body.getChild("hind_right_leg");
    }

    public static LayerDefinition createBodyLayer()
    {
        return DarkDragonPetMesh.create();
    }

    @Override
    public void setupAnim(LivingEntityRenderState renderState)
    {
        super.setupAnim(renderState);

        float time = renderState.ageInTicks;
        float walkPosition = renderState.walkAnimationPos * 0.6662F;
        float walkSpeed = renderState.walkAnimationSpeed;

        // 시선은 목과 머리가 나눠서 따라간다. 숨쉬듯 목이 오르내리고 턱이 살짝 벌어진다.
        this.neck.yRot += renderState.yRot * Mth.DEG_TO_RAD * 0.4F;
        this.neck.xRot += Mth.sin(time * 0.06F) * 0.04F;
        this.head.xRot += renderState.xRot * Mth.DEG_TO_RAD;
        this.head.yRot += renderState.yRot * Mth.DEG_TO_RAD * 0.6F;
        this.jaw.xRot += (Mth.sin(time * 0.06F) + 1.0F) * 0.06F;

        // 날개는 항상 천천히 퍼덕이고, 이동 중에는 더 크게 퍼덕인다.
        float flap = Mth.cos(time * 0.25F) * (0.25F + walkSpeed * 0.35F);
        this.wingLeft.zRot += -flap;
        this.wingRight.zRot += flap;
        this.wingLeftOuter.zRot += flap * 0.6F;
        this.wingRightOuter.zRot += -flap * 0.6F;

        float tailSway = Mth.sin(time * 0.1F) * 0.12F;
        this.tail1.yRot += tailSway;
        this.tail2.yRot += tailSway * 1.5F;
        this.tail3.yRot += tailSway * 2.0F;

        this.frontLeftLeg.xRot += Mth.cos(walkPosition) * 1.0F * walkSpeed;
        this.frontRightLeg.xRot += Mth.cos(walkPosition + Mth.PI) * 1.0F * walkSpeed;
        this.hindLeftLeg.xRot += Mth.cos(walkPosition + Mth.PI) * 1.0F * walkSpeed;
        this.hindRightLeg.xRot += Mth.cos(walkPosition) * 1.0F * walkSpeed;
    }
}
