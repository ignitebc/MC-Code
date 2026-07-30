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
 * 레전더리 펫: 암흑드래곤.
 * 흑색 비늘 + 백색 두개골 장갑판·뿔·날개 가시, 보라 날개막(너덜너덜한 뒷단),
 * 보라 발광 눈·입, 꼬리 하단 금색 콘셉트. 텍스처는 256x256 스크립트 생성.
 */
public class DarkDragonPetModel extends EntityModel<LivingEntityRenderState>
{
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "dark_dragon_pet"),
            "main");

    private final ModelPart head;
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
        this.head = body.getChild("neck").getChild("head");
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
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        PartDefinition body = root.addOrReplaceChild(
                "body",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-10.0F, -8.0F, -20.0F, 20.0F, 16.0F, 40.0F)
                        .texOffs(121, 0).addBox(-3.0F, -11.0F, -18.0F, 6.0F, 4.0F, 7.0F)
                        .texOffs(121, 0).addBox(-3.0F, -11.0F, -9.0F, 6.0F, 4.0F, 7.0F)
                        .texOffs(121, 0).addBox(-3.0F, -11.0F, 0.0F, 6.0F, 4.0F, 7.0F)
                        .texOffs(121, 0).addBox(-3.0F, -11.0F, 9.0F, 6.0F, 4.0F, 7.0F),
                PartPose.offset(0.0F, 4.0F, 0.0F));

        PartDefinition neck = body.addOrReplaceChild(
                "neck",
                CubeListBuilder.create()
                        .texOffs(148, 0).addBox(-5.0F, -7.0F, -16.0F, 10.0F, 10.0F, 16.0F)
                        .texOffs(201, 0).addBox(-3.0F, -9.0F, -14.0F, 6.0F, 3.0F, 11.0F),
                PartPose.offsetAndRotation(0.0F, -4.0F, -18.0F, -0.4F, 0.0F, 0.0F));

        PartDefinition head = neck.addOrReplaceChild(
                "head",
                CubeListBuilder.create()
                        .texOffs(0, 57).addBox(-6.0F, -6.0F, -12.0F, 12.0F, 10.0F, 12.0F)
                        .texOffs(49, 57).addBox(1.0F, -7.5F, -12.5F, 5.0F, 3.0F, 6.0F)
                        .texOffs(49, 57).addBox(-6.0F, -7.5F, -12.5F, 5.0F, 3.0F, 6.0F)
                        .texOffs(72, 57).addBox(-4.0F, -4.0F, -21.0F, 8.0F, 6.0F, 9.0F)
                        .texOffs(107, 57).addBox(-2.0F, -5.5F, -20.0F, 4.0F, 2.0F, 8.0F),
                PartPose.offsetAndRotation(0.0F, -3.0F, -15.0F, 0.4F, 0.0F, 0.0F));
        head.addOrReplaceChild(
                "jaw",
                CubeListBuilder.create()
                        .texOffs(132, 57).addBox(-3.5F, 0.0F, -9.0F, 7.0F, 3.0F, 9.0F),
                PartPose.offsetAndRotation(0.0F, 3.0F, -11.0F, 0.3F, 0.0F, 0.0F));
        head.addOrReplaceChild(
                "horn_left",
                CubeListBuilder.create()
                        .texOffs(165, 57).addBox(-1.0F, -12.0F, -1.0F, 2.0F, 12.0F, 2.0F),
                PartPose.offsetAndRotation(3.5F, -5.0F, -2.0F, 0.9F, 0.0F, 0.2F));
        head.addOrReplaceChild(
                "horn_right",
                CubeListBuilder.create()
                        .texOffs(165, 57).addBox(-1.0F, -12.0F, -1.0F, 2.0F, 12.0F, 2.0F),
                PartPose.offsetAndRotation(-3.5F, -5.0F, -2.0F, 0.9F, 0.0F, -0.2F));
        head.addOrReplaceChild(
                "horn_left_short",
                CubeListBuilder.create()
                        .texOffs(174, 57).addBox(-1.0F, -7.0F, -1.0F, 2.0F, 7.0F, 2.0F),
                PartPose.offsetAndRotation(5.5F, -3.0F, -5.0F, 0.8F, 0.0F, 0.5F));
        head.addOrReplaceChild(
                "horn_right_short",
                CubeListBuilder.create()
                        .texOffs(174, 57).addBox(-1.0F, -7.0F, -1.0F, 2.0F, 7.0F, 2.0F),
                PartPose.offsetAndRotation(-5.5F, -3.0F, -5.0F, 0.8F, 0.0F, -0.5F));

        PartDefinition wingLeft = body.addOrReplaceChild(
                "wing_left",
                CubeListBuilder.create()
                        .texOffs(183, 57).addBox(0.0F, -2.0F, -2.0F, 28.0F, 4.0F, 4.0F)
                        .texOffs(0, 80).addBox(0.0F, 0.0F, -2.0F, 28.0F, 0.0F, 22.0F)
                        .texOffs(101, 80).addBox(6.0F, -7.0F, -2.0F, 2.0F, 6.0F, 2.0F)
                        .texOffs(174, 57).addBox(14.0F, -8.0F, -2.0F, 2.0F, 7.0F, 2.0F)
                        .texOffs(101, 80).addBox(22.0F, -7.0F, -2.0F, 2.0F, 6.0F, 2.0F),
                PartPose.offset(10.0F, -6.0F, -8.0F));
        wingLeft.addOrReplaceChild(
                "wing_left_outer",
                CubeListBuilder.create()
                        .texOffs(110, 80).addBox(0.0F, -2.0F, -2.0F, 20.0F, 4.0F, 4.0F)
                        .texOffs(159, 80).addBox(0.0F, 0.0F, -2.0F, 20.0F, 0.0F, 18.0F)
                        .texOffs(101, 80).addBox(5.0F, -7.0F, -2.0F, 2.0F, 6.0F, 2.0F)
                        .texOffs(236, 80).addBox(12.0F, -6.0F, -2.0F, 2.0F, 5.0F, 2.0F)
                        .texOffs(0, 103).addBox(19.0F, -5.0F, -2.0F, 3.0F, 4.0F, 3.0F),
                PartPose.offset(28.0F, 0.0F, 0.0F));

        PartDefinition wingRight = body.addOrReplaceChild(
                "wing_right",
                CubeListBuilder.create()
                        .texOffs(183, 57).addBox(-28.0F, -2.0F, -2.0F, 28.0F, 4.0F, 4.0F)
                        .texOffs(0, 80).addBox(-28.0F, 0.0F, -2.0F, 28.0F, 0.0F, 22.0F)
                        .texOffs(101, 80).addBox(-8.0F, -7.0F, -2.0F, 2.0F, 6.0F, 2.0F)
                        .texOffs(174, 57).addBox(-16.0F, -8.0F, -2.0F, 2.0F, 7.0F, 2.0F)
                        .texOffs(101, 80).addBox(-24.0F, -7.0F, -2.0F, 2.0F, 6.0F, 2.0F),
                PartPose.offset(-10.0F, -6.0F, -8.0F));
        wingRight.addOrReplaceChild(
                "wing_right_outer",
                CubeListBuilder.create()
                        .texOffs(110, 80).addBox(-20.0F, -2.0F, -2.0F, 20.0F, 4.0F, 4.0F)
                        .texOffs(159, 80).addBox(-20.0F, 0.0F, -2.0F, 20.0F, 0.0F, 18.0F)
                        .texOffs(101, 80).addBox(-7.0F, -7.0F, -2.0F, 2.0F, 6.0F, 2.0F)
                        .texOffs(236, 80).addBox(-14.0F, -6.0F, -2.0F, 2.0F, 5.0F, 2.0F)
                        .texOffs(0, 103).addBox(-22.0F, -5.0F, -2.0F, 3.0F, 4.0F, 3.0F),
                PartPose.offset(-28.0F, 0.0F, 0.0F));

        addLeg(body, "front_left_leg", 8.0F, -12.0F);
        addLeg(body, "front_right_leg", -8.0F, -12.0F);
        addLeg(body, "hind_left_leg", 8.0F, 12.0F);
        addLeg(body, "hind_right_leg", -8.0F, 12.0F);

        PartDefinition tail1 = body.addOrReplaceChild(
                "tail_1",
                CubeListBuilder.create()
                        .texOffs(73, 103).addBox(-6.0F, -4.0F, 0.0F, 12.0F, 8.0F, 16.0F)
                        .texOffs(130, 103).addBox(-1.0F, -7.0F, 5.0F, 2.0F, 3.0F, 2.0F)
                        .texOffs(130, 103).addBox(-1.0F, -7.0F, 11.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(0.0F, -2.0F, 20.0F));
        PartDefinition tail2 = tail1.addOrReplaceChild(
                "tail_2",
                CubeListBuilder.create()
                        .texOffs(139, 103).addBox(-4.0F, -3.0F, 0.0F, 8.0F, 6.0F, 16.0F)
                        .texOffs(130, 103).addBox(-1.0F, -6.0F, 5.0F, 2.0F, 3.0F, 2.0F)
                        .texOffs(130, 103).addBox(-1.0F, -6.0F, 11.0F, 2.0F, 3.0F, 2.0F),
                PartPose.offset(0.0F, 0.0F, 16.0F));
        tail2.addOrReplaceChild(
                "tail_3",
                CubeListBuilder.create()
                        .texOffs(188, 103).addBox(-3.0F, -2.0F, 0.0F, 6.0F, 4.0F, 16.0F)
                        .texOffs(0, 128).addBox(-8.0F, -1.0F, 10.0F, 16.0F, 0.0F, 10.0F),
                PartPose.offset(0.0F, 0.0F, 16.0F));

        return LayerDefinition.create(meshDefinition, 256, 256);
    }

    private static void addLeg(PartDefinition body, String name, float x, float z)
    {
        body.addOrReplaceChild(
                name,
                CubeListBuilder.create()
                        .texOffs(13, 103).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 12.0F, 6.0F)
                        .texOffs(38, 103).addBox(-4.0F, 12.0F, -6.0F, 8.0F, 6.0F, 9.0F),
                PartPose.offset(x, 6.0F, z));
    }

    @Override
    public void setupAnim(LivingEntityRenderState renderState)
    {
        super.setupAnim(renderState);

        this.head.xRot += renderState.xRot * Mth.DEG_TO_RAD;
        this.head.yRot += renderState.yRot * Mth.DEG_TO_RAD;

        // 날개는 항상 천천히 퍼덕이고, 이동 중에는 더 크게 퍼덕인다.
        float flap = Mth.cos(renderState.ageInTicks * 0.25F)
                * (0.25F + renderState.walkAnimationSpeed * 0.35F);
        this.wingLeft.zRot += -0.2F - flap;
        this.wingRight.zRot += 0.2F + flap;
        this.wingLeftOuter.zRot += 0.15F + flap * 0.6F;
        this.wingRightOuter.zRot += -0.15F - flap * 0.6F;

        float tailSway = Mth.sin(renderState.ageInTicks * 0.1F) * 0.12F;
        this.tail1.yRot += tailSway;
        this.tail2.yRot += tailSway * 1.5F;
        this.tail3.yRot += tailSway * 2.0F;

        float walkPosition = renderState.walkAnimationPos * 0.6662F;
        float walkSpeed = renderState.walkAnimationSpeed;
        this.frontLeftLeg.xRot += Mth.cos(walkPosition) * 1.0F * walkSpeed;
        this.frontRightLeg.xRot += Mth.cos(walkPosition + Mth.PI) * 1.0F * walkSpeed;
        this.hindLeftLeg.xRot += Mth.cos(walkPosition + Mth.PI) * 1.0F * walkSpeed;
        this.hindRightLeg.xRot += Mth.cos(walkPosition) * 1.0F * walkSpeed;
    }
}
