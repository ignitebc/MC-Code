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
 * 희귀 펫: 드라군(starcraft-dragoon.bbmodel).
 * 좌표는 Blockbench Y-up 좌표를 Y축 반전으로 변환한 값이다.
 */
public class DragoonPetModel extends EntityModel<LivingEntityRenderState>
{
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "dragoon_pet"),
            "main");

    private final ModelPart laserCap;
    private final ModelPart legFix0;
    private final ModelPart legFix1;
    private final ModelPart legFix2;
    private final ModelPart legFix3;

    public DragoonPetModel(ModelPart root)
    {
        super(root);

        ModelPart body = root.getChild("body");
        this.laserCap = body.getChild("laser_cap");
        this.legFix0 = body.getChild("leg_0_fix");
        this.legFix1 = body.getChild("leg_1_fix");
        this.legFix2 = body.getChild("leg_2_fix");
        this.legFix3 = body.getChild("leg_3_fix");
    }

    public static LayerDefinition createBodyLayer()
    {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        PartDefinition body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-16.0F, -2.0F, -16.0F, 32.0F, 13.0F, 32.0F),
                PartPose.offsetAndRotation(0.0F, 7.0F, 0.0F, 0.0F, 0.7854F, 0.0F));
        body.addOrReplaceChild(
                "body_r1",
                CubeListBuilder.create()
                        .texOffs(0, 45).addBox(-13.0F, -2.5F, -13.0F, 26.0F, 5.0F, 26.0F),
                PartPose.offsetAndRotation(0.0F, -4.5F, 0.0F, 0.0F, -0.7854F, 0.0F));
        body.addOrReplaceChild(
                "body_r2",
                CubeListBuilder.create()
                        .texOffs(165, 57).addBox(-0.5F, -6.0F, -10.0F, 9.0F, 10.0F, 19.0F),
                PartPose.offsetAndRotation(0.5F, -10.5F, 0.0F, 0.0F, 2.3562F, 0.0F));

        PartDefinition generator = body.addOrReplaceChild(
                "generator",
                CubeListBuilder.create()
                        .texOffs(126, 91).addBox(-2.0F, -6.5F, -1.0F, 4.0F, 3.0F, 4.0F)
                        .texOffs(128, 19).addBox(-16.0F, -4.5F, -1.0F, 16.0F, 3.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, 17.5F, 0.0F, 0.0F, -0.7854F, 0.0F));
        generator.addOrReplaceChild(
                "generator_r1",
                CubeListBuilder.create()
                        .texOffs(128, 19).addBox(-16.0F, -1.5F, -2.0F, 16.0F, 3.0F, 4.0F),
                PartPose.offsetAndRotation(0.0F, -3.0F, 1.0F, 0.0F, -2.0944F, 0.0F));
        generator.addOrReplaceChild(
                "generator_r2",
                CubeListBuilder.create()
                        .texOffs(128, 19).addBox(-16.0F, -1.5F, -2.0F, 16.0F, 3.0F, 4.0F),
                PartPose.offsetAndRotation(1.0F, -3.0F, 2.0F, 0.0F, 2.0944F, 0.0F));

        body.addOrReplaceChild(
                "gun",
                CubeListBuilder.create()
                        .texOffs(122, 67).addBox(-4.0F, -2.0F, -2.0F, 8.0F, 4.0F, 4.0F),
                PartPose.offsetAndRotation(16.0F, 8.5F, 16.0F, 0.0F, 2.3562F, 0.0F));
        body.addOrReplaceChild(
                "laser",
                CubeListBuilder.create()
                        .texOffs(126, 75).addBox(-2.0F, -6.0F, -2.0F, 4.0F, 12.0F, 4.0F),
                PartPose.offset(0.0F, -2.5F, 0.0F));

        PartDefinition laserCap = body.addOrReplaceChild(
                "laser_cap",
                CubeListBuilder.create(),
                PartPose.offsetAndRotation(0.0F, -6.5F, 0.0F, 0.0F, 0.7854F, 0.0F));
        laserCap.addOrReplaceChild(
                "laser_cap_right",
                CubeListBuilder.create()
                        .texOffs(168, 18).addBox(-6.0F, -4.5F, -8.0F, 9.0F, 2.0F, 16.0F),
                PartPose.offset(6.0F, 2.0F, 0.0F));
        laserCap.addOrReplaceChild(
                "laser_cap_left",
                CubeListBuilder.create()
                        .texOffs(168, 0).addBox(-3.0F, -4.5F, -8.0F, 9.0F, 2.0F, 16.0F),
                PartPose.offset(-6.0F, 2.0F, 0.0F));

        addLeg(body, 0, PartPose.offset(0.0F, 3.5F, 0.0F),
                PartPose.offset(-15.0F, 0.0F, -1.0F));
        addLeg(body, 1, PartPose.offsetAndRotation(-15.0F, 3.5F, 15.0F, 0.0F, 1.5708F, 0.0F),
                PartPose.offset(0.0F, 0.0F, 15.0F));
        addLeg(body, 2, PartPose.offsetAndRotation(30.0F, 3.5F, -29.0F, 0.0F, -3.1416F, 0.0F),
                PartPose.offset(15.0F, 0.0F, -29.0F));
        addLeg(body, 3, PartPose.offsetAndRotation(-15.0F, 3.5F, -15.0F, 0.0F, -1.5708F, 0.0F),
                PartPose.offset(0.0F, 0.0F, -15.0F));

        return LayerDefinition.create(meshDefinition, 256, 128);
    }

    private static void addLeg(PartDefinition body, int index, PartPose fixPose, PartPose innerPose)
    {
        PartDefinition fix = body.addOrReplaceChild(
                "leg_" + index + "_fix",
                CubeListBuilder.create(),
                fixPose);
        PartDefinition inner = fix.addOrReplaceChild(
                "leg_" + index + "_inner",
                CubeListBuilder.create()
                        .texOffs(17, 80).addBox(-20.0F, -7.5F, -5.0F, 21.0F, 12.0F, 10.0F)
                        .texOffs(170, 36).addBox(-8.5F, -4.5F, -5.5F, 12.0F, 10.0F, 11.0F),
                innerPose);
        PartDefinition middle = inner.addOrReplaceChild(
                "leg_" + index + "_middle",
                CubeListBuilder.create()
                        .texOffs(5, 102).addBox(-29.0F, -8.5F, -5.5F, 32.0F, 10.0F, 11.0F)
                        .texOffs(128, 0).addBox(-5.5F, -4.5F, -5.5F, 9.0F, 8.0F, 11.0F)
                        .texOffs(104, 45).addBox(-7.0F, -9.5F, -6.0F, 19.0F, 4.0F, 12.0F),
                PartPose.offset(-15.0F, -1.0F, 0.0F));
        PartDefinition outer = middle.addOrReplaceChild(
                "leg_" + index + "_outer",
                CubeListBuilder.create()
                        .texOffs(128, 0).addBox(-5.5F, -4.5F, -5.5F, 9.0F, 8.0F, 11.0F)
                        .texOffs(104, 45).addBox(-7.0F, -8.0F, -6.0F, 19.0F, 4.0F, 12.0F),
                PartPose.offset(-27.0F, -4.0F, 0.0F));
        outer.addOrReplaceChild(
                "leg_" + index + "_claw",
                CubeListBuilder.create()
                        .texOffs(172, 86).addBox(-16.0F, 0.0F, -4.0F, 11.0F, 4.0F, 9.0F),
                PartPose.offsetAndRotation(-1.0F, -5.5F, -0.5F, 0.0F, 0.0F, -0.3491F));
    }

    @Override
    public void setupAnim(LivingEntityRenderState renderState)
    {
        super.setupAnim(renderState);

        // 레이저 캡이 천천히 회전하는 대기 연출
        this.laserCap.yRot += renderState.ageInTicks * 0.06F;

        // 대각선 다리 쌍이 번갈아 내딛는 보행
        float walkPosition = renderState.walkAnimationPos * 0.6662F;
        float walkSpeed = renderState.walkAnimationSpeed;
        float swingA = Mth.cos(walkPosition) * 0.25F * walkSpeed;
        float swingB = Mth.cos(walkPosition + Mth.PI) * 0.25F * walkSpeed;
        this.legFix0.yRot += swingA;
        this.legFix2.yRot += swingA;
        this.legFix1.yRot += swingB;
        this.legFix3.yRot += swingB;
    }
}
