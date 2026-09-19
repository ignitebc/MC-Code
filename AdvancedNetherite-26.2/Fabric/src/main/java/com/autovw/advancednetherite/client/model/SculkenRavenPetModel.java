package com.autovw.advancednetherite.client.model;

import com.autovw.advancednetherite.AdvancedNetherite;
import com.autovw.advancednetherite.client.model.mesh.SculkenRavenPetMesh;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * 레전더리 펫: 스컬큰 레이븐.
 * 검푸른 깃털에 스컬크가 번진 큰까마귀. 눈·정수리 촉수·칼깃 끝이 청록으로 빛난다.
 * <p>
 * 형태와 텍스처는 design/pets/legendaries.py 에서 복셀로 조각해 {@link SculkenRavenPetMesh} 로 생성한다.
 * 날개는 처음부터 몸 옆에 접힌 모양으로 조각돼 있어, 여기서는 들썩임만 더한다.
 */
public class SculkenRavenPetModel extends EntityModel<LivingEntityRenderState>
{
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "sculken_raven_pet"),
            "main");

    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart beak;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart tail;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public SculkenRavenPetModel(ModelPart root)
    {
        super(root);

        ModelPart body = root.getChild("body");
        this.neck = body.getChild("neck");
        this.head = this.neck.getChild("head");
        this.beak = this.head.getChild("beak");
        this.leftWing = body.getChild("left_wing");
        this.rightWing = body.getChild("right_wing");
        this.tail = body.getChild("tail");
        this.leftLeg = body.getChild("left_leg");
        this.rightLeg = body.getChild("right_leg");
    }

    public static LayerDefinition createBodyLayer()
    {
        return SculkenRavenPetMesh.create();
    }

    @Override
    public void setupAnim(LivingEntityRenderState renderState)
    {
        super.setupAnim(renderState);

        float time = renderState.ageInTicks;
        float walkPosition = renderState.walkAnimationPos * 0.6662F;
        float walkSpeed = renderState.walkAnimationSpeed;

        // 시선을 따라 목과 머리가 움직인다.
        this.neck.yRot += renderState.yRot * Mth.DEG_TO_RAD * 0.4F;
        this.head.xRot += renderState.xRot * Mth.DEG_TO_RAD * 0.6F;
        this.head.yRot += renderState.yRot * Mth.DEG_TO_RAD * 0.6F;

        // 두 다리를 번갈아 내딛고, 까마귀처럼 걸음에 맞춰 고개를 끄덕인다.
        float swing = Mth.cos(walkPosition) * 0.7F * walkSpeed;
        this.leftLeg.xRot += swing;
        this.rightLeg.xRot -= swing;
        this.neck.xRot += Mth.cos(walkPosition * 2.0F) * 0.1F * walkSpeed;

        // 접힌 날개는 걸을 때 들썩이고, 가만히 있을 때는 이따금 기지개를 켜듯 살짝 펼친다.
        float shuffle = Math.abs(Mth.sin(walkPosition)) * 0.12F * walkSpeed;
        float stretch = Math.max(0.0F, Mth.sin(time * 0.045F) - 0.8F) * 2.2F;
        this.leftWing.zRot -= shuffle + stretch;
        this.rightWing.zRot += shuffle + stretch;

        // 대기 연출: 꼬리를 부채질하고 이따금씩 부리를 딱딱거린다.
        this.tail.xRot += Mth.sin(time * 0.08F) * 0.05F;
        this.tail.yRot += Mth.sin(time * 0.05F) * 0.08F;
        float peck = Math.max(0.0F, Mth.sin(time * 0.06F) - 0.92F) * 3.0F;
        this.beak.xRot += peck;
    }
}
