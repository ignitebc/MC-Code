package com.autovw.advancednetherite.client.model;

import com.autovw.advancednetherite.AdvancedNetherite;
import com.autovw.advancednetherite.client.model.mesh.DialgaPetMesh;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * 디아루가. 푸른 갑주, 은빛 칼날, 다이아몬드 가슴과 세 마디 꼬리를 복셀로 조각한다.
 * 형태와 텍스처는 design/pets/companions.py 에서 함께 생성한다.
 */
public class DialgaPetModel extends EntityModel<LivingEntityRenderState>
{
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "dialga_pet"),
            "main");

    private final ModelPart head;
    private final ModelPart tailBase;
    private final ModelPart tailMid;
    private final ModelPart tailTip;
    private final ModelPart frontUpperLeft;
    private final ModelPart frontUpperRight;
    private final ModelPart hindUpperLeft;
    private final ModelPart hindUpperRight;

    public DialgaPetModel(ModelPart root)
    {
        super(root);

        ModelPart body = root.getChild("body");
        ModelPart neck = body.getChild("neck");
        this.head = neck.getChild("head");
        this.tailBase = body.getChild("tail_base");
        this.tailMid = this.tailBase.getChild("tail_mid");
        this.tailTip = this.tailMid.getChild("tail_tip");
        this.frontUpperLeft = body.getChild("front_upper_left");
        this.frontUpperRight = body.getChild("front_upper_right");
        this.hindUpperLeft = body.getChild("hind_upper_left");
        this.hindUpperRight = body.getChild("hind_upper_right");
    }

    public static LayerDefinition createBodyLayer()
    {
        return DialgaPetMesh.create();
    }

    @Override
    public void setupAnim(LivingEntityRenderState renderState)
    {
        super.setupAnim(renderState);

        this.head.xRot += renderState.xRot * Mth.DEG_TO_RAD;
        this.head.yRot += renderState.yRot * Mth.DEG_TO_RAD;

        float walkPosition = renderState.walkAnimationPos * 0.6662F;
        float walkSpeed = renderState.walkAnimationSpeed;
        this.frontUpperLeft.xRot += Mth.cos(walkPosition) * 0.8F * walkSpeed;
        this.frontUpperRight.xRot += Mth.cos(walkPosition + Mth.PI) * 0.8F * walkSpeed;
        this.hindUpperLeft.xRot += Mth.cos(walkPosition + Mth.PI) * 0.8F * walkSpeed;
        this.hindUpperRight.xRot += Mth.cos(walkPosition) * 0.8F * walkSpeed;

        float tailMovement = Mth.sin(renderState.ageInTicks * 0.12F) * 0.12F;
        this.tailBase.yRot += tailMovement;
        this.tailMid.yRot += tailMovement * 1.5F;
        this.tailTip.yRot += tailMovement * 2.0F;
    }
}
