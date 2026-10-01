package com.autovw.advancednetherite.client.model;

import com.autovw.advancednetherite.AdvancedNetherite;
import com.autovw.advancednetherite.client.model.mesh.UnicornPetMesh;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * 유니콘. 진주빛 털, 나선 뿔, 여러 겹의 갈기와 관절 다리를 복셀로 조각한다.
 * 형태와 텍스처는 design/pets/companions.py 에서 함께 생성한다.
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
        return UnicornPetMesh.create();
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
