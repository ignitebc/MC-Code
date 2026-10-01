package com.autovw.advancednetherite.client.model;

import com.autovw.advancednetherite.AdvancedNetherite;
import com.autovw.advancednetherite.client.model.mesh.GazellePetMesh;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * 가젤. 황갈색 털, 얼굴 줄무늬, 고리뿔과 가는 다리를 복셀로 조각한다.
 * 형태와 텍스처는 design/pets/companions.py 에서 함께 생성한다.
 */
public class GazellePetModel extends EntityModel<LivingEntityRenderState>
{
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "gazelle_pet"),
            "main");

    private final ModelPart head;
    private final ModelPart rightEar;
    private final ModelPart leftEar;
    private final ModelPart tail;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftFrontLeg;
    private final ModelPart rightBackLeg;
    private final ModelPart leftBackLeg;

    public GazellePetModel(ModelPart root)
    {
        super(root);

        ModelPart gazelle = root.getChild("gazelle");
        this.head = gazelle.getChild("head");
        this.rightEar = this.head.getChild("rightEar");
        this.leftEar = this.head.getChild("leftEar");
        this.tail = gazelle.getChild("tail");
        this.rightFrontLeg = gazelle.getChild("rightFronLeg");
        this.leftFrontLeg = gazelle.getChild("leftFronLeg");
        this.rightBackLeg = gazelle.getChild("rightBackLeg");
        this.leftBackLeg = gazelle.getChild("leftBackLeg");
    }

    public static LayerDefinition createBodyLayer()
    {
        return GazellePetMesh.create();
    }

    @Override
    public void setupAnim(LivingEntityRenderState renderState)
    {
        super.setupAnim(renderState);

        float time = renderState.ageInTicks;
        float walkPosition = renderState.walkAnimationPos * 0.6662F;
        float walkSpeed = renderState.walkAnimationSpeed;

        // 시선을 따라 고개를 돌린다.
        this.head.xRot += renderState.xRot * Mth.DEG_TO_RAD * 0.7F;
        this.head.yRot += renderState.yRot * Mth.DEG_TO_RAD * 0.7F;

        // 대각 보행: 오른앞-왼뒤, 왼앞-오른뒤 다리가 짝을 이룬다.
        float swingA = Mth.cos(walkPosition) * 1.0F * walkSpeed;
        float swingB = Mth.cos(walkPosition + Mth.PI) * 1.0F * walkSpeed;
        this.rightFrontLeg.xRot += swingA;
        this.leftBackLeg.xRot += swingA;
        this.leftFrontLeg.xRot += swingB;
        this.rightBackLeg.xRot += swingB;

        // 꼬리를 살랑살랑 흔들고 귀를 이따금씩 털어낸다.
        this.tail.yRot += Mth.sin(time * 0.15F) * 0.25F;
        float earFlick = Math.max(0.0F, Mth.sin(time * 0.05F) - 0.9F) * 3.0F;
        this.rightEar.zRot -= earFlick;
        this.leftEar.zRot += earFlick;
    }
}
