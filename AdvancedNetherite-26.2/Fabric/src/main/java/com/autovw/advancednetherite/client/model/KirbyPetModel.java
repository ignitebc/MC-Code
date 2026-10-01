package com.autovw.advancednetherite.client.model;

import com.autovw.advancednetherite.AdvancedNetherite;
import com.autovw.advancednetherite.client.model.mesh.KirbyPetMesh;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/**
 * 커비. 둥근 분홍 몸, 구면에 이어지는 표정, 붉은 발을 작은 복셀로 조각한다.
 * 형태와 텍스처는 design/pets/companions.py 에서 함께 생성한다.
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
        return KirbyPetMesh.create();
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
