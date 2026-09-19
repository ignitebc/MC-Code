package com.autovw.advancednetherite.client.model.mesh;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.core.Direction;

import java.util.EnumSet;
import java.util.Set;

/**
 * design/pets/dogs.py 가 생성한 메시. 직접 고치지 말고 스크립트를 고친 뒤 다시 생성한다.
 * 복셀 한 칸은 0.5 유닛이고, 텍스처는 복셀당 4px 로 구워져 있다.
 * 다른 박스에 완전히 가려지는 면은 faces 집합에서 빼서 그리지 않는다.
 */
public final class GomiPetMesh
{
    private static final Set<Direction> FACES_DES = EnumSet.of(Direction.DOWN, Direction.EAST, Direction.SOUTH);
    private static final Set<Direction> FACES_DNE = EnumSet.of(Direction.DOWN, Direction.NORTH, Direction.EAST);
    private static final Set<Direction> FACES_DNES = EnumSet.of(Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH);
    private static final Set<Direction> FACES_DUE = EnumSet.of(Direction.DOWN, Direction.UP, Direction.EAST);
    private static final Set<Direction> FACES_DUES = EnumSet.of(Direction.DOWN, Direction.UP, Direction.EAST, Direction.SOUTH);
    private static final Set<Direction> FACES_DUNE = EnumSet.of(Direction.DOWN, Direction.UP, Direction.NORTH, Direction.EAST);
    private static final Set<Direction> FACES_DUNES = EnumSet.of(Direction.DOWN, Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH);
    private static final Set<Direction> FACES_DUW = EnumSet.of(Direction.DOWN, Direction.UP, Direction.WEST);
    private static final Set<Direction> FACES_DUWES = EnumSet.of(Direction.DOWN, Direction.UP, Direction.WEST, Direction.EAST, Direction.SOUTH);
    private static final Set<Direction> FACES_DUWN = EnumSet.of(Direction.DOWN, Direction.UP, Direction.WEST, Direction.NORTH);
    private static final Set<Direction> FACES_DUWNE = EnumSet.of(Direction.DOWN, Direction.UP, Direction.WEST, Direction.NORTH, Direction.EAST);
    private static final Set<Direction> FACES_DUWNES = EnumSet.of(Direction.DOWN, Direction.UP, Direction.WEST, Direction.NORTH, Direction.EAST, Direction.SOUTH);
    private static final Set<Direction> FACES_DUWNS = EnumSet.of(Direction.DOWN, Direction.UP, Direction.WEST, Direction.NORTH, Direction.SOUTH);
    private static final Set<Direction> FACES_DUWS = EnumSet.of(Direction.DOWN, Direction.UP, Direction.WEST, Direction.SOUTH);
    private static final Set<Direction> FACES_DWE = EnumSet.of(Direction.DOWN, Direction.WEST, Direction.EAST);
    private static final Set<Direction> FACES_DWES = EnumSet.of(Direction.DOWN, Direction.WEST, Direction.EAST, Direction.SOUTH);
    private static final Set<Direction> FACES_DWN = EnumSet.of(Direction.DOWN, Direction.WEST, Direction.NORTH);
    private static final Set<Direction> FACES_DWNE = EnumSet.of(Direction.DOWN, Direction.WEST, Direction.NORTH, Direction.EAST);
    private static final Set<Direction> FACES_DWNES = EnumSet.of(Direction.DOWN, Direction.WEST, Direction.NORTH, Direction.EAST, Direction.SOUTH);
    private static final Set<Direction> FACES_DWNS = EnumSet.of(Direction.DOWN, Direction.WEST, Direction.NORTH, Direction.SOUTH);
    private static final Set<Direction> FACES_DWS = EnumSet.of(Direction.DOWN, Direction.WEST, Direction.SOUTH);
    private static final Set<Direction> FACES_UES = EnumSet.of(Direction.UP, Direction.EAST, Direction.SOUTH);
    private static final Set<Direction> FACES_UNE = EnumSet.of(Direction.UP, Direction.NORTH, Direction.EAST);
    private static final Set<Direction> FACES_UNES = EnumSet.of(Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH);
    private static final Set<Direction> FACES_UWE = EnumSet.of(Direction.UP, Direction.WEST, Direction.EAST);
    private static final Set<Direction> FACES_UWES = EnumSet.of(Direction.UP, Direction.WEST, Direction.EAST, Direction.SOUTH);
    private static final Set<Direction> FACES_UWN = EnumSet.of(Direction.UP, Direction.WEST, Direction.NORTH);
    private static final Set<Direction> FACES_UWNE = EnumSet.of(Direction.UP, Direction.WEST, Direction.NORTH, Direction.EAST);
    private static final Set<Direction> FACES_UWNES = EnumSet.of(Direction.UP, Direction.WEST, Direction.NORTH, Direction.EAST, Direction.SOUTH);
    private static final Set<Direction> FACES_UWNS = EnumSet.of(Direction.UP, Direction.WEST, Direction.NORTH, Direction.SOUTH);
    private static final Set<Direction> FACES_UWS = EnumSet.of(Direction.UP, Direction.WEST, Direction.SOUTH);

    private GomiPetMesh()
    {
    }

    public static LayerDefinition create()
    {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        PartDefinition body = root.addOrReplaceChild("body", bodyCubes(), PartPose.offset(0.0F, 17.0F, 0.0F));
        body.addOrReplaceChild("head", headCubes(), PartPose.offset(0.05F, -3.1F, -4.1F));
        body.addOrReplaceChild("front_left_leg", frontLeftLegCubes(), PartPose.offset(2.4F, 3.0F, -3.09F));
        body.addOrReplaceChild("front_right_leg", frontRightLegCubes(), PartPose.offset(-2.4F, 3.0F, -3.09F));
        body.addOrReplaceChild("back_left_leg", backLeftLegCubes(), PartPose.offset(2.4F, 3.0F, 3.09F));
        body.addOrReplaceChild("back_right_leg", backRightLegCubes(), PartPose.offset(-2.4F, 3.0F, 3.09F));
        PartDefinition tailBase = body.addOrReplaceChild("tail_base", tailBaseCubes(), PartPose.offset(0.05F, -2.45F, 4.1F));
        PartDefinition tailCurve = tailBase.addOrReplaceChild("tail_curve", tailCurveCubes(), PartPose.offset(-0.05F, -1.55F, 1.55F));
        tailCurve.addOrReplaceChild("tail_tip", tailTipCubes(), PartPose.offset(0.05F, -1.45F, -1.05F));

        return LayerDefinition.create(meshDefinition, 128, 56);
    }

    private static CubeListBuilder bodyCubes()
    {
        return CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.5F, -3.0F, -4.0F, 7.0F, 6.0F, 7.0F, FACES_DUWNES)
                .texOffs(40, 13).addBox(-2.5F, -2.5F, -5.5F, 5.0F, 5.0F, 1.5F, FACES_DUWNE)
                .texOffs(53, 13).addBox(-2.5F, -2.5F, 3.0F, 5.0F, 5.0F, 1.5F, FACES_DUWES)
                .texOffs(58, 26).addBox(-3.0F, -4.0F, -3.0F, 6.0F, 1.0F, 3.5F, FACES_DWNES)
                .texOffs(77, 26).addBox(-3.0F, 3.0F, -3.0F, 6.0F, 1.0F, 3.5F, FACES_UWNES)
                .texOffs(80, 0).addBox(-4.5F, -2.5F, -3.0F, 1.0F, 5.0F, 3.5F, FACES_DUWNS)
                .texOffs(89, 0).addBox(3.5F, -2.5F, -3.0F, 1.0F, 5.0F, 3.5F, FACES_DUNES)
                .texOffs(105, 36).addBox(-2.5F, -3.5F, 0.5F, 5.0F, 0.5F, 2.5F, FACES_DWES)
                .texOffs(0, 40).addBox(-2.5F, 3.0F, 0.5F, 5.0F, 0.5F, 2.5F, FACES_UWES)
                .texOffs(98, 0).addBox(-4.0F, -2.5F, 0.5F, 0.5F, 5.0F, 2.0F, FACES_DUWS)
                .texOffs(103, 0).addBox(3.5F, -2.5F, 0.5F, 0.5F, 5.0F, 2.0F, FACES_DUES)
                .texOffs(78, 43).addBox(-2.0F, -4.0F, -4.0F, 4.0F, 1.0F, 1.0F, FACES_DWNE)
                .texOffs(88, 43).addBox(-2.0F, 3.0F, -4.0F, 4.0F, 1.0F, 1.0F, FACES_UWNE)
                .texOffs(87, 20).addBox(-3.5F, -2.0F, -5.0F, 1.0F, 4.0F, 1.0F, FACES_DUWN)
                .texOffs(91, 20).addBox(-3.5F, -2.0F, 3.0F, 1.0F, 4.0F, 1.0F, FACES_DUWS)
                .texOffs(95, 20).addBox(2.5F, -2.0F, -5.0F, 1.0F, 4.0F, 1.0F, FACES_DUNE)
                .texOffs(99, 20).addBox(2.5F, -2.0F, 3.0F, 1.0F, 4.0F, 1.0F, FACES_DUES)
                .texOffs(112, 31).addBox(-4.5F, -1.5F, -4.0F, 1.0F, 3.0F, 1.0F, FACES_DUWN)
                .texOffs(116, 31).addBox(3.5F, -1.5F, -4.0F, 1.0F, 3.0F, 1.0F, FACES_DUNE)
                .texOffs(14, 48).addBox(-2.5F, -3.5F, -4.5F, 5.0F, 1.0F, 0.5F, FACES_DWNE)
                .texOffs(25, 48).addBox(-2.5F, 2.5F, -4.5F, 5.0F, 1.0F, 0.5F, FACES_UWNE)
                .texOffs(36, 48).addBox(-2.5F, -3.0F, 3.0F, 5.0F, 0.5F, 1.0F, FACES_DWES)
                .texOffs(48, 48).addBox(-2.5F, 2.5F, 3.0F, 5.0F, 0.5F, 1.0F, FACES_UWES)
                .texOffs(120, 31).addBox(-4.0F, -1.5F, 2.5F, 0.5F, 3.0F, 1.0F, FACES_DUWS)
                .texOffs(123, 31).addBox(3.5F, -1.5F, 2.5F, 0.5F, 3.0F, 1.0F, FACES_DUES)
                .texOffs(78, 50).addBox(-2.5F, -3.0F, -5.0F, 5.0F, 0.5F, 0.5F, FACES_DWNE)
                .texOffs(89, 50).addBox(-2.5F, 2.5F, -5.0F, 5.0F, 0.5F, 0.5F, FACES_UWNE)
                .texOffs(100, 50).addBox(-2.0F, -3.5F, 3.0F, 4.0F, 0.5F, 0.5F, FACES_DWES)
                .texOffs(109, 50).addBox(-2.0F, 3.0F, 3.0F, 4.0F, 0.5F, 0.5F, FACES_UWES)
                .texOffs(96, 26).addBox(-4.0F, -2.0F, -4.5F, 0.5F, 4.0F, 0.5F, FACES_DUWN)
                .texOffs(98, 26).addBox(3.5F, -2.0F, -4.5F, 0.5F, 4.0F, 0.5F, FACES_DUNE)
                .texOffs(100, 26).addBox(-4.0F, -3.0F, -3.5F, 0.5F, 0.5F, 4.0F, FACES_DWNS)
                .texOffs(109, 26).addBox(-4.0F, 2.5F, -3.5F, 0.5F, 0.5F, 4.0F, FACES_UWNS)
                .texOffs(118, 26).addBox(-3.5F, -3.5F, -3.5F, 0.5F, 0.5F, 4.0F, FACES_DWNS)
                .texOffs(0, 31).addBox(-3.5F, 3.0F, -3.5F, 0.5F, 0.5F, 4.0F, FACES_UWNS)
                .texOffs(9, 31).addBox(3.0F, -3.5F, -3.5F, 0.5F, 0.5F, 4.0F, FACES_DNES)
                .texOffs(18, 31).addBox(3.0F, 3.0F, -3.5F, 0.5F, 0.5F, 4.0F, FACES_UNES)
                .texOffs(27, 31).addBox(3.5F, -3.0F, -3.5F, 0.5F, 0.5F, 4.0F, FACES_DNES)
                .texOffs(36, 31).addBox(3.5F, 2.5F, -3.5F, 0.5F, 0.5F, 4.0F, FACES_UNES)
                .texOffs(57, 36).addBox(-3.0F, -1.5F, -5.5F, 0.5F, 3.0F, 0.5F, FACES_DUWN)
                .texOffs(59, 36).addBox(-3.0F, -1.5F, 4.0F, 0.5F, 3.0F, 0.5F, FACES_DUWS)
                .texOffs(61, 36).addBox(2.5F, -1.5F, -5.5F, 0.5F, 3.0F, 0.5F, FACES_DUNE)
                .texOffs(63, 36).addBox(2.5F, -1.5F, 4.0F, 0.5F, 3.0F, 0.5F, FACES_DUES)
                .texOffs(60, 48).addBox(-3.0F, -3.5F, -4.0F, 1.0F, 0.5F, 1.0F, FACES_DWN)
                .texOffs(64, 48).addBox(-3.0F, 3.0F, -4.0F, 1.0F, 0.5F, 1.0F, FACES_UWN)
                .texOffs(68, 48).addBox(2.0F, -3.5F, -4.0F, 1.0F, 0.5F, 1.0F, FACES_DNE)
                .texOffs(72, 48).addBox(2.0F, 3.0F, -4.0F, 1.0F, 0.5F, 1.0F, FACES_UNE)
                .texOffs(98, 43).addBox(-4.0F, -2.5F, -4.0F, 0.5F, 1.0F, 1.0F, FACES_DWN)
                .texOffs(101, 43).addBox(-4.0F, 1.5F, -4.0F, 0.5F, 1.0F, 1.0F, FACES_UWN)
                .texOffs(104, 43).addBox(3.5F, -2.5F, -4.0F, 0.5F, 1.0F, 1.0F, FACES_DNE)
                .texOffs(107, 43).addBox(3.5F, 1.5F, -4.0F, 0.5F, 1.0F, 1.0F, FACES_UNE)
                .texOffs(13, 43).addBox(-3.0F, -3.5F, 0.5F, 0.5F, 0.5F, 2.0F, FACES_DWS)
                .texOffs(18, 43).addBox(-3.0F, 3.0F, 0.5F, 0.5F, 0.5F, 2.0F, FACES_UWS)
                .texOffs(23, 43).addBox(2.5F, -3.5F, 0.5F, 0.5F, 0.5F, 2.0F, FACES_DES)
                .texOffs(28, 43).addBox(2.5F, 3.0F, 0.5F, 0.5F, 0.5F, 2.0F, FACES_UES)
                .texOffs(118, 50).addBox(-3.5F, -2.5F, -4.5F, 1.0F, 0.5F, 0.5F, FACES_DWN)
                .texOffs(121, 50).addBox(-3.5F, -2.5F, 3.0F, 1.0F, 0.5F, 0.5F, FACES_DWS)
                .texOffs(124, 50).addBox(-3.5F, 2.0F, -4.5F, 1.0F, 0.5F, 0.5F, FACES_UWN)
                .texOffs(0, 52).addBox(-3.5F, 2.0F, 3.0F, 1.0F, 0.5F, 0.5F, FACES_UWS)
                .texOffs(3, 52).addBox(2.5F, -2.5F, -4.5F, 1.0F, 0.5F, 0.5F, FACES_DNE)
                .texOffs(6, 52).addBox(2.5F, -2.5F, 3.0F, 1.0F, 0.5F, 0.5F, FACES_DES)
                .texOffs(9, 52).addBox(2.5F, 2.0F, -4.5F, 1.0F, 0.5F, 0.5F, FACES_UNE)
                .texOffs(12, 52).addBox(2.5F, 2.0F, 3.0F, 1.0F, 0.5F, 0.5F, FACES_UES)
                .texOffs(15, 52).addBox(-4.5F, -2.0F, -3.5F, 0.5F, 0.5F, 0.5F, FACES_DWN)
                .texOffs(17, 52).addBox(-4.5F, 1.5F, -3.5F, 0.5F, 0.5F, 0.5F, FACES_UWN)
                .texOffs(19, 52).addBox(-4.0F, -2.0F, 2.5F, 0.5F, 0.5F, 0.5F, FACES_DWS)
                .texOffs(21, 52).addBox(-4.0F, 1.5F, 2.5F, 0.5F, 0.5F, 0.5F, FACES_UWS)
                .texOffs(23, 52).addBox(-3.0F, -3.0F, -4.5F, 0.5F, 0.5F, 0.5F, FACES_DWN)
                .texOffs(25, 52).addBox(-3.0F, -3.0F, 3.0F, 0.5F, 0.5F, 0.5F, FACES_DWS)
                .texOffs(27, 52).addBox(-3.0F, -2.5F, -5.0F, 0.5F, 0.5F, 0.5F, FACES_DWN)
                .texOffs(29, 52).addBox(-3.0F, -2.5F, 3.5F, 0.5F, 0.5F, 0.5F, FACES_DWS)
                .texOffs(31, 52).addBox(-3.0F, 2.0F, -5.0F, 0.5F, 0.5F, 0.5F, FACES_UWN)
                .texOffs(33, 52).addBox(-3.0F, 2.0F, 3.5F, 0.5F, 0.5F, 0.5F, FACES_UWS)
                .texOffs(35, 52).addBox(-3.0F, 2.5F, -4.5F, 0.5F, 0.5F, 0.5F, FACES_UWN)
                .texOffs(37, 52).addBox(-3.0F, 2.5F, 3.0F, 0.5F, 0.5F, 0.5F, FACES_UWS)
                .texOffs(39, 52).addBox(-2.5F, -4.0F, -3.5F, 0.5F, 0.5F, 0.5F, FACES_DWN)
                .texOffs(41, 52).addBox(-2.5F, 3.5F, -3.5F, 0.5F, 0.5F, 0.5F, FACES_UWN)
                .texOffs(43, 52).addBox(2.0F, -4.0F, -3.5F, 0.5F, 0.5F, 0.5F, FACES_DNE)
                .texOffs(45, 52).addBox(2.0F, 3.5F, -3.5F, 0.5F, 0.5F, 0.5F, FACES_UNE)
                .texOffs(47, 52).addBox(2.5F, -3.0F, -4.5F, 0.5F, 0.5F, 0.5F, FACES_DNE)
                .texOffs(49, 52).addBox(2.5F, -3.0F, 3.0F, 0.5F, 0.5F, 0.5F, FACES_DES)
                .texOffs(51, 52).addBox(2.5F, -2.5F, -5.0F, 0.5F, 0.5F, 0.5F, FACES_DNE)
                .texOffs(53, 52).addBox(2.5F, -2.5F, 3.5F, 0.5F, 0.5F, 0.5F, FACES_DES)
                .texOffs(55, 52).addBox(2.5F, 2.0F, -5.0F, 0.5F, 0.5F, 0.5F, FACES_UNE)
                .texOffs(57, 52).addBox(2.5F, 2.0F, 3.5F, 0.5F, 0.5F, 0.5F, FACES_UES)
                .texOffs(59, 52).addBox(2.5F, 2.5F, -4.5F, 0.5F, 0.5F, 0.5F, FACES_UNE)
                .texOffs(61, 52).addBox(2.5F, 2.5F, 3.0F, 0.5F, 0.5F, 0.5F, FACES_UES)
                .texOffs(63, 52).addBox(3.5F, -2.0F, 2.5F, 0.5F, 0.5F, 0.5F, FACES_DES)
                .texOffs(65, 52).addBox(3.5F, 1.5F, 2.5F, 0.5F, 0.5F, 0.5F, FACES_UES)
                .texOffs(67, 52).addBox(4.0F, -2.0F, -3.5F, 0.5F, 0.5F, 0.5F, FACES_DNE)
                .texOffs(69, 52).addBox(4.0F, 1.5F, -3.5F, 0.5F, 0.5F, 0.5F, FACES_UNE);
    }

    private static CubeListBuilder headCubes()
    {
        return CubeListBuilder.create()
                .texOffs(28, 0).addBox(-4.0F, -6.5F, -5.5F, 8.0F, 7.0F, 6.0F, FACES_DUWNES)
                .texOffs(2, 20).addBox(-3.5F, 0.5F, -5.0F, 7.0F, 1.0F, 4.5F, FACES_UWNES)
                .texOffs(56, 0).addBox(-5.0F, -5.5F, -5.0F, 1.0F, 6.0F, 5.0F, FACES_DUWNS)
                .texOffs(68, 0).addBox(4.0F, -5.5F, -5.0F, 1.0F, 6.0F, 5.0F, FACES_DUNES)
                .texOffs(66, 13).addBox(-2.5F, -5.5F, -6.5F, 5.0F, 5.5F, 1.0F, FACES_DUWNE)
                .texOffs(113, 13).addBox(-2.5F, -5.5F, 0.5F, 5.0F, 5.0F, 1.0F, FACES_DUWES)
                .texOffs(103, 20).addBox(-2.5F, -7.5F, -4.5F, 5.0F, 1.0F, 4.0F, FACES_DWNES)
                .texOffs(108, 0).addBox(-6.0F, -3.0F, -4.5F, 1.0F, 3.5F, 3.5F, FACES_DUWNS)
                .texOffs(117, 0).addBox(5.0F, -3.0F, -4.5F, 1.0F, 3.5F, 3.5F, FACES_DUNES)
                .texOffs(45, 31).addBox(-2.5F, 1.5F, -4.5F, 5.0F, 0.5F, 4.0F, FACES_UWNES)
                .texOffs(0, 36).addBox(-5.0F, -9.5F, -4.0F, 2.5F, 2.5F, 1.5F, FACES_DUWNS)
                .texOffs(8, 36).addBox(2.5F, -9.5F, -4.0F, 2.5F, 2.5F, 1.5F, FACES_DUNES)
                .texOffs(16, 36).addBox(-1.5F, -2.5F, -7.5F, 3.0F, 3.0F, 1.0F, FACES_DUWNE)
                .texOffs(78, 13).addBox(-4.0F, -5.5F, -6.0F, 1.5F, 6.0F, 0.5F, FACES_DUWN)
                .texOffs(82, 13).addBox(2.5F, -5.5F, -6.0F, 1.5F, 6.0F, 0.5F, FACES_DUNE)
                .texOffs(110, 43).addBox(-2.0F, 0.5F, -0.5F, 4.0F, 1.0F, 1.0F, FACES_UWES)
                .texOffs(25, 20).addBox(-4.0F, -5.5F, 0.5F, 1.5F, 5.0F, 0.5F, FACES_DUWS)
                .texOffs(29, 20).addBox(2.5F, -5.5F, 0.5F, 1.5F, 5.0F, 0.5F, FACES_DUES)
                .texOffs(0, 26).addBox(-5.0F, 0.5F, -5.0F, 1.5F, 0.5F, 4.5F, FACES_UWNS)
                .texOffs(12, 26).addBox(3.5F, 0.5F, -5.0F, 1.5F, 0.5F, 4.5F, FACES_UNES)
                .texOffs(24, 26).addBox(-6.5F, -2.5F, -4.0F, 0.5F, 2.5F, 2.5F, FACES_DUWNS)
                .texOffs(30, 26).addBox(6.0F, -2.5F, -4.0F, 0.5F, 2.5F, 2.5F, FACES_DUNES)
                .texOffs(76, 48).addBox(-3.0F, -6.5F, -6.0F, 6.0F, 1.0F, 0.5F, FACES_DWNE)
                .texOffs(89, 48).addBox(-3.0F, -6.5F, 0.5F, 6.0F, 1.0F, 0.5F, FACES_DWES)
                .texOffs(102, 48).addBox(-3.0F, -0.5F, 0.5F, 6.0F, 1.0F, 0.5F, FACES_UWES)
                .texOffs(0, 50).addBox(-3.0F, -7.0F, -5.5F, 6.0F, 0.5F, 1.0F, FACES_DWNE)
                .texOffs(14, 50).addBox(-3.0F, -7.0F, -0.5F, 6.0F, 0.5F, 1.0F, FACES_DWES)
                .texOffs(33, 43).addBox(-1.5F, -2.0F, -8.0F, 3.0F, 2.0F, 0.5F, FACES_DUWNE)
                .texOffs(63, 31).addBox(-4.0F, -7.0F, -4.5F, 1.5F, 0.5F, 4.0F, FACES_DWNS)
                .texOffs(74, 31).addBox(2.5F, -7.0F, -4.5F, 1.5F, 0.5F, 4.0F, FACES_DNES)
                .texOffs(28, 50).addBox(-2.5F, 0.0F, -6.0F, 5.0F, 1.0F, 0.5F, FACES_UWNE)
                .texOffs(71, 52).addBox(-4.0F, 0.5F, -5.5F, 8.0F, 0.5F, 0.5F, FACES_UWNE)
                .texOffs(120, 43).addBox(-1.0F, -2.5F, -9.0F, 2.0F, 1.0F, 1.0F, FACES_DUWNE)
                .texOffs(36, 26).addBox(-4.5F, -6.5F, -4.5F, 0.5F, 1.0F, 4.0F, FACES_DWNS)
                .texOffs(45, 26).addBox(4.0F, -6.5F, -4.5F, 0.5F, 1.0F, 4.0F, FACES_DNES)
                .texOffs(15, 40).addBox(-5.5F, -2.5F, -5.5F, 1.5F, 2.5F, 0.5F, FACES_DUWN)
                .texOffs(19, 40).addBox(4.0F, -2.5F, -5.5F, 1.5F, 2.5F, 0.5F, FACES_DUNE)
                .texOffs(0, 46).addBox(-4.5F, -10.0F, -4.0F, 2.0F, 0.5F, 1.5F, FACES_DWNES)
                .texOffs(7, 46).addBox(2.5F, -10.0F, -4.0F, 2.0F, 0.5F, 1.5F, FACES_DWNES)
                .texOffs(65, 36).addBox(-2.5F, -9.5F, -4.0F, 0.5F, 2.0F, 1.5F, FACES_DNES)
                .texOffs(69, 36).addBox(2.0F, -9.5F, -4.0F, 0.5F, 2.0F, 1.5F, FACES_DWNS)
                .texOffs(125, 13).addBox(-4.5F, -5.5F, 0.0F, 0.5F, 5.5F, 0.5F, FACES_DUWS)
                .texOffs(0, 20).addBox(4.0F, -5.5F, 0.0F, 0.5F, 5.5F, 0.5F, FACES_DUES)
                .texOffs(23, 40).addBox(-6.0F, -2.5F, -5.0F, 1.0F, 2.5F, 0.5F, FACES_DUWN)
                .texOffs(26, 40).addBox(-6.0F, -2.5F, -1.0F, 1.0F, 2.5F, 0.5F, FACES_DUWS)
                .texOffs(29, 40).addBox(5.0F, -2.5F, -5.0F, 1.0F, 2.5F, 0.5F, FACES_DUNE)
                .texOffs(32, 40).addBox(5.0F, -2.5F, -1.0F, 1.0F, 2.5F, 0.5F, FACES_DUES)
                .texOffs(35, 40).addBox(-4.5F, 1.0F, -4.0F, 1.0F, 0.5F, 2.5F, FACES_UWNS)
                .texOffs(42, 40).addBox(3.5F, 1.0F, -4.0F, 1.0F, 0.5F, 2.5F, FACES_UNES)
                .texOffs(54, 26).addBox(-3.0F, -5.0F, -6.5F, 0.5F, 4.5F, 0.5F, FACES_DUWN)
                .texOffs(56, 26).addBox(2.5F, -5.0F, -6.5F, 0.5F, 4.5F, 0.5F, FACES_DUNE)
                .texOffs(88, 52).addBox(-2.0F, 0.0F, -6.5F, 4.0F, 0.5F, 0.5F, FACES_UWE)
                .texOffs(97, 52).addBox(-2.0F, 1.0F, -5.5F, 4.0F, 0.5F, 0.5F, FACES_UWNE)
                .texOffs(106, 52).addBox(-2.0F, 1.5F, -5.0F, 4.0F, 0.5F, 0.5F, FACES_UWNE)
                .texOffs(115, 52).addBox(-2.0F, 1.5F, -0.5F, 4.0F, 0.5F, 0.5F, FACES_UWES)
                .texOffs(14, 46).addBox(-0.5F, 0.0F, -8.5F, 1.0F, 1.0F, 1.0F, FACES_UWNES)
                .texOffs(85, 31).addBox(-3.0F, -5.0F, 1.0F, 0.5F, 4.0F, 0.5F, FACES_DUWS)
                .texOffs(87, 31).addBox(2.5F, -5.0F, 1.0F, 0.5F, 4.0F, 0.5F, FACES_DUES)
                .texOffs(49, 40).addBox(-2.0F, -2.0F, -7.5F, 0.5F, 2.0F, 1.0F, FACES_DUWN)
                .texOffs(52, 40).addBox(1.5F, -2.0F, -7.5F, 0.5F, 2.0F, 1.0F, FACES_DUNE)
                .texOffs(73, 36).addBox(-4.5F, -5.5F, -5.5F, 0.5F, 3.0F, 0.5F, FACES_DWN)
                .texOffs(75, 36).addBox(4.0F, -5.5F, -5.5F, 0.5F, 3.0F, 0.5F, FACES_DNE)
                .texOffs(77, 36).addBox(-5.0F, -6.0F, -4.0F, 0.5F, 0.5F, 3.0F, FACES_DWNS)
                .texOffs(84, 36).addBox(-3.0F, 1.5F, -4.0F, 0.5F, 0.5F, 3.0F, FACES_UWNS)
                .texOffs(91, 36).addBox(2.5F, 1.5F, -4.0F, 0.5F, 0.5F, 3.0F, FACES_UNES)
                .texOffs(98, 36).addBox(4.5F, -6.0F, -4.0F, 0.5F, 0.5F, 3.0F, FACES_DNES)
                .texOffs(55, 40).addBox(-5.5F, -2.5F, -0.5F, 0.5F, 2.5F, 0.5F, FACES_DUWS)
                .texOffs(57, 40).addBox(-4.5F, -2.5F, -6.0F, 0.5F, 2.5F, 0.5F, FACES_DUWN)
                .texOffs(59, 40).addBox(4.0F, -2.5F, -6.0F, 0.5F, 2.5F, 0.5F, FACES_DUNE)
                .texOffs(61, 40).addBox(5.0F, -2.5F, -0.5F, 0.5F, 2.5F, 0.5F, FACES_DUES)
                .texOffs(63, 40).addBox(-5.5F, -3.5F, -4.0F, 0.5F, 0.5F, 2.5F, FACES_DWNS)
                .texOffs(69, 40).addBox(-5.5F, 0.5F, -4.0F, 0.5F, 0.5F, 2.5F, FACES_UWNS)
                .texOffs(75, 40).addBox(5.0F, -3.5F, -4.0F, 0.5F, 0.5F, 2.5F, FACES_DNES)
                .texOffs(81, 40).addBox(5.0F, 0.5F, -4.0F, 0.5F, 0.5F, 2.5F, FACES_UNES)
                .texOffs(0, 53).addBox(-4.0F, 0.5F, -0.5F, 2.0F, 0.5F, 0.5F, FACES_UWS)
                .texOffs(5, 53).addBox(-1.0F, -7.5F, -5.0F, 2.0F, 0.5F, 0.5F, FACES_DWNE)
                .texOffs(10, 53).addBox(-1.0F, -7.5F, -0.5F, 2.0F, 0.5F, 0.5F, FACES_DWES)
                .texOffs(15, 53).addBox(-1.0F, -2.5F, -8.0F, 2.0F, 0.5F, 0.5F, FACES_DWE)
                .texOffs(20, 53).addBox(2.0F, 0.5F, -0.5F, 2.0F, 0.5F, 0.5F, FACES_UES)
                .texOffs(18, 46).addBox(-3.0F, -7.5F, -2.5F, 0.5F, 0.5F, 1.5F, FACES_DWS)
                .texOffs(22, 46).addBox(2.5F, -7.5F, -2.5F, 0.5F, 0.5F, 1.5F, FACES_DES)
                .texOffs(25, 53).addBox(-5.0F, 0.0F, -5.5F, 1.0F, 0.5F, 0.5F, FACES_UWN)
                .texOffs(28, 53).addBox(-3.0F, 0.5F, 0.0F, 1.0F, 0.5F, 0.5F, FACES_UWS)
                .texOffs(31, 53).addBox(-3.0F, 1.0F, -0.5F, 1.0F, 0.5F, 0.5F, FACES_UWS)
                .texOffs(34, 53).addBox(-0.5F, -1.5F, -8.5F, 1.0F, 0.5F, 0.5F, FACES_UWNE)
                .texOffs(37, 53).addBox(-0.5F, -0.5F, -8.5F, 1.0F, 0.5F, 0.5F, FACES_DWNE)
                .texOffs(40, 53).addBox(2.0F, 0.5F, 0.0F, 1.0F, 0.5F, 0.5F, FACES_UES)
                .texOffs(43, 53).addBox(2.0F, 1.0F, -0.5F, 1.0F, 0.5F, 0.5F, FACES_UES)
                .texOffs(46, 53).addBox(4.0F, 0.0F, -5.5F, 1.0F, 0.5F, 0.5F, FACES_UNE)
                .texOffs(39, 50).addBox(-2.0F, -1.5F, -8.0F, 0.5F, 1.0F, 0.5F, FACES_DUWN)
                .texOffs(41, 50).addBox(1.5F, -1.5F, -8.0F, 0.5F, 1.0F, 0.5F, FACES_DUNE)
                .texOffs(43, 50).addBox(-3.5F, 1.5F, -3.0F, 0.5F, 0.5F, 1.0F, FACES_UWNS)
                .texOffs(46, 50).addBox(3.0F, 1.5F, -3.0F, 0.5F, 0.5F, 1.0F, FACES_UNES)
                .texOffs(49, 53).addBox(-6.5F, -1.5F, -4.5F, 0.5F, 0.5F, 0.5F, FACES_DUWN)
                .texOffs(51, 53).addBox(-6.5F, -1.5F, -1.5F, 0.5F, 0.5F, 0.5F, FACES_DUWS)
                .texOffs(53, 53).addBox(-5.5F, -3.0F, -5.0F, 0.5F, 0.5F, 0.5F, FACES_DWN)
                .texOffs(55, 53).addBox(-5.5F, -3.0F, -1.0F, 0.5F, 0.5F, 0.5F, FACES_DWS)
                .texOffs(57, 53).addBox(-5.5F, 0.0F, -5.0F, 0.5F, 0.5F, 0.5F, FACES_UWN)
                .texOffs(59, 53).addBox(-5.5F, 0.0F, -1.0F, 0.5F, 0.5F, 0.5F, FACES_UWS)
                .texOffs(61, 53).addBox(-5.0F, -3.0F, -5.5F, 0.5F, 0.5F, 0.5F, FACES_DWN)
                .texOffs(63, 53).addBox(-4.5F, -6.0F, -5.0F, 0.5F, 0.5F, 0.5F, FACES_DWN)
                .texOffs(65, 53).addBox(-4.5F, -6.0F, -0.5F, 0.5F, 0.5F, 0.5F, FACES_DWS)
                .texOffs(67, 53).addBox(-4.0F, 1.0F, -4.5F, 0.5F, 0.5F, 0.5F, FACES_UWN)
                .texOffs(69, 53).addBox(-4.0F, 1.0F, -1.5F, 0.5F, 0.5F, 0.5F, FACES_UWS)
                .texOffs(71, 53).addBox(-3.5F, -7.0F, -5.0F, 0.5F, 0.5F, 0.5F, FACES_DWN)
                .texOffs(73, 53).addBox(-3.5F, -7.0F, -0.5F, 0.5F, 0.5F, 0.5F, FACES_DWS)
                .texOffs(75, 53).addBox(-3.5F, -6.0F, -6.0F, 0.5F, 0.5F, 0.5F, FACES_DWN)
                .texOffs(77, 53).addBox(-3.5F, -6.0F, 0.5F, 0.5F, 0.5F, 0.5F, FACES_DWS)
                .texOffs(79, 53).addBox(-3.5F, -0.5F, 0.5F, 0.5F, 0.5F, 0.5F, FACES_UWS)
                .texOffs(81, 53).addBox(-2.0F, -2.5F, -7.0F, 0.5F, 0.5F, 0.5F, FACES_DWN)
                .texOffs(83, 53).addBox(-2.0F, 0.0F, -7.0F, 0.5F, 0.5F, 0.5F, FACES_UWN)
                .texOffs(85, 53).addBox(-1.0F, 0.0F, -8.0F, 0.5F, 0.5F, 0.5F, FACES_UWN)
                .texOffs(87, 53).addBox(0.5F, 0.0F, -8.0F, 0.5F, 0.5F, 0.5F, FACES_UNE)
                .texOffs(89, 53).addBox(1.5F, -2.5F, -7.0F, 0.5F, 0.5F, 0.5F, FACES_DNE)
                .texOffs(91, 53).addBox(1.5F, 0.0F, -7.0F, 0.5F, 0.5F, 0.5F, FACES_UNE)
                .texOffs(93, 53).addBox(3.0F, -7.0F, -5.0F, 0.5F, 0.5F, 0.5F, FACES_DNE)
                .texOffs(95, 53).addBox(3.0F, -7.0F, -0.5F, 0.5F, 0.5F, 0.5F, FACES_DES)
                .texOffs(97, 53).addBox(3.0F, -6.0F, -6.0F, 0.5F, 0.5F, 0.5F, FACES_DNE)
                .texOffs(99, 53).addBox(3.0F, -6.0F, 0.5F, 0.5F, 0.5F, 0.5F, FACES_DES)
                .texOffs(101, 53).addBox(3.0F, -0.5F, 0.5F, 0.5F, 0.5F, 0.5F, FACES_UES)
                .texOffs(103, 53).addBox(3.5F, 1.0F, -4.5F, 0.5F, 0.5F, 0.5F, FACES_UNE)
                .texOffs(105, 53).addBox(3.5F, 1.0F, -1.5F, 0.5F, 0.5F, 0.5F, FACES_UES)
                .texOffs(107, 53).addBox(4.0F, -6.0F, -5.0F, 0.5F, 0.5F, 0.5F, FACES_DNE)
                .texOffs(109, 53).addBox(4.0F, -6.0F, -0.5F, 0.5F, 0.5F, 0.5F, FACES_DES)
                .texOffs(111, 53).addBox(4.5F, -3.0F, -5.5F, 0.5F, 0.5F, 0.5F, FACES_DNE)
                .texOffs(113, 53).addBox(5.0F, -3.0F, -5.0F, 0.5F, 0.5F, 0.5F, FACES_DNE)
                .texOffs(115, 53).addBox(5.0F, -3.0F, -1.0F, 0.5F, 0.5F, 0.5F, FACES_DES)
                .texOffs(117, 53).addBox(5.0F, 0.0F, -5.0F, 0.5F, 0.5F, 0.5F, FACES_UNE)
                .texOffs(119, 53).addBox(5.0F, 0.0F, -1.0F, 0.5F, 0.5F, 0.5F, FACES_UES)
                .texOffs(121, 53).addBox(6.0F, -1.5F, -4.5F, 0.5F, 0.5F, 0.5F, FACES_DUNE)
                .texOffs(123, 53).addBox(6.0F, -1.5F, -1.5F, 0.5F, 0.5F, 0.5F, FACES_DUES);
    }

    private static CubeListBuilder frontLeftLegCubes()
    {
        return CubeListBuilder.create()
                .texOffs(0, 13).addBox(-1.5F, -1.0F, -1.0F, 3.0F, 5.0F, 2.0F, FACES_DUWNES)
                .texOffs(33, 20).addBox(-1.0F, -1.0F, -1.5F, 2.0F, 5.0F, 0.5F, FACES_DUWNE)
                .texOffs(38, 20).addBox(-1.0F, -1.0F, 1.0F, 2.0F, 5.0F, 0.5F, FACES_DUWES)
                .texOffs(26, 46).addBox(-1.5F, 2.5F, -2.0F, 3.0F, 1.5F, 0.5F, FACES_DUWNE)
                .texOffs(49, 50).addBox(-1.0F, 3.0F, -2.5F, 2.0F, 1.0F, 0.5F, FACES_DUWNE)
                .texOffs(33, 46).addBox(-1.5F, 2.5F, -1.5F, 0.5F, 1.5F, 0.5F, FACES_DUW)
                .texOffs(35, 46).addBox(-1.5F, 2.5F, 1.0F, 0.5F, 1.5F, 0.5F, FACES_DUWS)
                .texOffs(37, 46).addBox(1.0F, 2.5F, -1.5F, 0.5F, 1.5F, 0.5F, FACES_DUE)
                .texOffs(39, 46).addBox(1.0F, 2.5F, 1.0F, 0.5F, 1.5F, 0.5F, FACES_DUES);
    }

    private static CubeListBuilder frontRightLegCubes()
    {
        return CubeListBuilder.create()
                .texOffs(10, 13).addBox(-1.5F, -1.0F, -1.0F, 3.0F, 5.0F, 2.0F, FACES_DUWNES)
                .texOffs(43, 20).addBox(-1.0F, -1.0F, -1.5F, 2.0F, 5.0F, 0.5F, FACES_DUWNE)
                .texOffs(48, 20).addBox(-1.0F, -1.0F, 1.0F, 2.0F, 5.0F, 0.5F, FACES_DUWES)
                .texOffs(41, 46).addBox(-1.5F, 2.5F, -2.0F, 3.0F, 1.5F, 0.5F, FACES_DUWNE)
                .texOffs(54, 50).addBox(-1.0F, 3.0F, -2.5F, 2.0F, 1.0F, 0.5F, FACES_DUWNE)
                .texOffs(48, 46).addBox(-1.5F, 2.5F, -1.5F, 0.5F, 1.5F, 0.5F, FACES_DUW)
                .texOffs(50, 46).addBox(-1.5F, 2.5F, 1.0F, 0.5F, 1.5F, 0.5F, FACES_DUWS)
                .texOffs(52, 46).addBox(1.0F, 2.5F, -1.5F, 0.5F, 1.5F, 0.5F, FACES_DUE)
                .texOffs(54, 46).addBox(1.0F, 2.5F, 1.0F, 0.5F, 1.5F, 0.5F, FACES_DUES);
    }

    private static CubeListBuilder backLeftLegCubes()
    {
        return CubeListBuilder.create()
                .texOffs(20, 13).addBox(-1.5F, -1.0F, -1.0F, 3.0F, 5.0F, 2.0F, FACES_DUWNES)
                .texOffs(53, 20).addBox(-1.0F, -1.0F, -1.5F, 2.0F, 5.0F, 0.5F, FACES_DUWNE)
                .texOffs(58, 20).addBox(-1.0F, -1.0F, 1.0F, 2.0F, 5.0F, 0.5F, FACES_DUWES)
                .texOffs(56, 46).addBox(-1.5F, 2.5F, -2.0F, 3.0F, 1.5F, 0.5F, FACES_DUWNE)
                .texOffs(59, 50).addBox(-1.0F, 3.0F, -2.5F, 2.0F, 1.0F, 0.5F, FACES_DUWNE)
                .texOffs(63, 46).addBox(-1.5F, 2.5F, -1.5F, 0.5F, 1.5F, 0.5F, FACES_DUW)
                .texOffs(65, 46).addBox(-1.5F, 2.5F, 1.0F, 0.5F, 1.5F, 0.5F, FACES_DUWS)
                .texOffs(67, 46).addBox(1.0F, 2.5F, -1.5F, 0.5F, 1.5F, 0.5F, FACES_DUE)
                .texOffs(69, 46).addBox(1.0F, 2.5F, 1.0F, 0.5F, 1.5F, 0.5F, FACES_DUES);
    }

    private static CubeListBuilder backRightLegCubes()
    {
        return CubeListBuilder.create()
                .texOffs(30, 13).addBox(-1.5F, -1.0F, -1.0F, 3.0F, 5.0F, 2.0F, FACES_DUWNES)
                .texOffs(63, 20).addBox(-1.0F, -1.0F, -1.5F, 2.0F, 5.0F, 0.5F, FACES_DUWNE)
                .texOffs(68, 20).addBox(-1.0F, -1.0F, 1.0F, 2.0F, 5.0F, 0.5F, FACES_DUWES)
                .texOffs(71, 46).addBox(-1.5F, 2.5F, -2.0F, 3.0F, 1.5F, 0.5F, FACES_DUWNE)
                .texOffs(64, 50).addBox(-1.0F, 3.0F, -2.5F, 2.0F, 1.0F, 0.5F, FACES_DUWNE)
                .texOffs(78, 46).addBox(-1.5F, 2.5F, -1.5F, 0.5F, 1.5F, 0.5F, FACES_DUW)
                .texOffs(80, 46).addBox(-1.5F, 2.5F, 1.0F, 0.5F, 1.5F, 0.5F, FACES_DUWS)
                .texOffs(82, 46).addBox(1.0F, 2.5F, -1.5F, 0.5F, 1.5F, 0.5F, FACES_DUE)
                .texOffs(84, 46).addBox(1.0F, 2.5F, 1.0F, 0.5F, 1.5F, 0.5F, FACES_DUES);
    }

    private static CubeListBuilder tailBaseCubes()
    {
        return CubeListBuilder.create()
                .texOffs(89, 31).addBox(-1.5F, -1.5F, 0.0F, 3.0F, 2.0F, 2.5F, FACES_DUWNES)
                .texOffs(40, 43).addBox(-1.0F, -2.5F, 0.5F, 2.0F, 1.0F, 1.5F, FACES_DWNES)
                .texOffs(47, 43).addBox(-1.0F, 0.5F, 0.5F, 2.0F, 1.0F, 1.5F, FACES_UWNES)
                .texOffs(54, 43).addBox(-1.0F, -1.5F, -0.5F, 2.0F, 2.0F, 0.5F, FACES_DUWNE)
                .texOffs(59, 43).addBox(-1.0F, -1.5F, 2.5F, 2.0F, 2.0F, 0.5F, FACES_DUWES)
                .texOffs(0, 54).addBox(-1.0F, -2.0F, 0.0F, 2.0F, 0.5F, 0.5F, FACES_DWNE)
                .texOffs(5, 54).addBox(-1.0F, -2.0F, 2.0F, 2.0F, 0.5F, 0.5F, FACES_DWES)
                .texOffs(10, 54).addBox(-1.0F, 0.5F, 0.0F, 2.0F, 0.5F, 0.5F, FACES_UWNE)
                .texOffs(15, 54).addBox(-1.0F, 0.5F, 2.0F, 2.0F, 0.5F, 0.5F, FACES_UWES)
                .texOffs(86, 46).addBox(-1.5F, -2.0F, 0.5F, 0.5F, 0.5F, 1.5F, FACES_DWNS)
                .texOffs(90, 46).addBox(-1.5F, 0.5F, 0.5F, 0.5F, 0.5F, 1.5F, FACES_UWNS)
                .texOffs(94, 46).addBox(1.0F, -2.0F, 0.5F, 0.5F, 0.5F, 1.5F, FACES_DNES)
                .texOffs(98, 46).addBox(1.0F, 0.5F, 0.5F, 0.5F, 0.5F, 1.5F, FACES_UNES)
                .texOffs(20, 54).addBox(-0.5F, -2.5F, 0.0F, 1.0F, 0.5F, 0.5F, FACES_DWNE)
                .texOffs(23, 54).addBox(-0.5F, -2.5F, 2.0F, 1.0F, 0.5F, 0.5F, FACES_DWES)
                .texOffs(26, 54).addBox(-0.5F, 1.0F, 0.0F, 1.0F, 0.5F, 0.5F, FACES_UWNE)
                .texOffs(29, 54).addBox(-0.5F, 1.0F, 2.0F, 1.0F, 0.5F, 0.5F, FACES_UWES);
    }

    private static CubeListBuilder tailCurveCubes()
    {
        return CubeListBuilder.create()
                .texOffs(86, 13).addBox(-2.0F, -3.0F, -1.5F, 4.0F, 3.0F, 3.5F, FACES_DUWNES)
                .texOffs(24, 36).addBox(-1.5F, -3.5F, -1.5F, 3.0F, 0.5F, 3.5F, FACES_DWNES)
                .texOffs(37, 36).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 0.5F, 3.5F, FACES_UWNES)
                .texOffs(64, 43).addBox(-1.5F, -2.5F, -2.0F, 3.0F, 2.0F, 0.5F, FACES_DUWNE)
                .texOffs(71, 43).addBox(-1.5F, -2.5F, 2.0F, 3.0F, 2.0F, 0.5F, FACES_DUWES)
                .texOffs(87, 40).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 0.5F, 2.5F, FACES_DWNES)
                .texOffs(96, 40).addBox(-1.0F, 0.5F, -1.0F, 2.0F, 0.5F, 2.5F, FACES_UWNES)
                .texOffs(100, 31).addBox(-2.5F, -2.5F, -1.0F, 0.5F, 2.0F, 2.5F, FACES_DUWNS)
                .texOffs(106, 31).addBox(2.0F, -2.5F, -1.0F, 0.5F, 2.0F, 2.5F, FACES_DUNES)
                .texOffs(105, 40).addBox(-2.0F, -3.5F, -1.0F, 0.5F, 0.5F, 2.5F, FACES_DWNS)
                .texOffs(111, 40).addBox(-2.0F, 0.0F, -1.0F, 0.5F, 0.5F, 2.5F, FACES_UWNS)
                .texOffs(117, 40).addBox(1.5F, -3.5F, -1.0F, 0.5F, 0.5F, 2.5F, FACES_DNES)
                .texOffs(0, 43).addBox(1.5F, 0.0F, -1.0F, 0.5F, 0.5F, 2.5F, FACES_UNES)
                .texOffs(32, 54).addBox(-1.0F, -3.0F, -2.0F, 2.0F, 0.5F, 0.5F, FACES_DWNE)
                .texOffs(37, 54).addBox(-1.0F, -3.0F, 2.0F, 2.0F, 0.5F, 0.5F, FACES_DWES)
                .texOffs(42, 54).addBox(-1.0F, -0.5F, -2.0F, 2.0F, 0.5F, 0.5F, FACES_UWNE)
                .texOffs(47, 54).addBox(-1.0F, -0.5F, 2.0F, 2.0F, 0.5F, 0.5F, FACES_UWES)
                .texOffs(102, 46).addBox(-2.5F, -3.0F, -0.5F, 0.5F, 0.5F, 1.5F, FACES_DWNS)
                .texOffs(106, 46).addBox(-2.5F, -0.5F, -0.5F, 0.5F, 0.5F, 1.5F, FACES_UWNS)
                .texOffs(110, 46).addBox(-1.5F, -4.0F, -0.5F, 0.5F, 0.5F, 1.5F, FACES_DWNS)
                .texOffs(114, 46).addBox(-1.5F, 0.5F, -0.5F, 0.5F, 0.5F, 1.5F, FACES_UWNS)
                .texOffs(118, 46).addBox(1.0F, -4.0F, -0.5F, 0.5F, 0.5F, 1.5F, FACES_DNES)
                .texOffs(122, 46).addBox(1.0F, 0.5F, -0.5F, 0.5F, 0.5F, 1.5F, FACES_UNES)
                .texOffs(0, 48).addBox(2.0F, -3.0F, -0.5F, 0.5F, 0.5F, 1.5F, FACES_DNES)
                .texOffs(4, 48).addBox(2.0F, -0.5F, -0.5F, 0.5F, 0.5F, 1.5F, FACES_UNES);
    }

    private static CubeListBuilder tailTipCubes()
    {
        return CubeListBuilder.create()
                .texOffs(101, 13).addBox(-1.5F, -2.5F, -3.5F, 3.0F, 3.5F, 3.0F, FACES_DUWNES)
                .texOffs(50, 36).addBox(-1.5F, -2.0F, -4.0F, 3.0F, 3.5F, 0.5F, FACES_DUWNE)
                .texOffs(6, 43).addBox(-1.5F, -2.0F, -0.5F, 3.0F, 2.5F, 0.5F, FACES_DUWES)
                .texOffs(73, 20).addBox(-2.0F, -2.0F, -3.5F, 0.5F, 2.5F, 3.0F, FACES_DUWNS)
                .texOffs(80, 20).addBox(1.5F, -2.0F, -3.5F, 0.5F, 2.5F, 3.0F, FACES_DUNES)
                .texOffs(8, 48).addBox(-1.0F, 1.0F, -3.5F, 2.0F, 1.0F, 1.0F, FACES_UWES)
                .texOffs(69, 50).addBox(-1.0F, 0.5F, -4.5F, 2.0F, 1.0F, 0.5F, FACES_DUWNE)
                .texOffs(52, 54).addBox(-1.0F, 1.0F, -2.5F, 2.0F, 0.5F, 0.5F, FACES_UWES)
                .texOffs(57, 54).addBox(-1.0F, 1.5F, -4.0F, 2.0F, 0.5F, 0.5F, FACES_UWNE)
                .texOffs(62, 54).addBox(-0.5F, 0.0F, -4.5F, 1.0F, 0.5F, 0.5F, FACES_DWNE)
                .texOffs(65, 54).addBox(-0.5F, 1.5F, -4.5F, 1.0F, 0.5F, 0.5F, FACES_UWNE)
                .texOffs(68, 54).addBox(-0.5F, 1.5F, -2.5F, 1.0F, 0.5F, 0.5F, FACES_UWES)
                .texOffs(74, 50).addBox(-1.5F, 1.0F, -3.5F, 0.5F, 1.0F, 0.5F, FACES_UWNS)
                .texOffs(76, 50).addBox(1.0F, 1.0F, -3.5F, 0.5F, 1.0F, 0.5F, FACES_UNES)
                .texOffs(71, 54).addBox(-1.5F, 1.0F, -3.0F, 0.5F, 0.5F, 0.5F, FACES_UWS)
                .texOffs(73, 54).addBox(1.0F, 1.0F, -3.0F, 0.5F, 0.5F, 0.5F, FACES_UES);
    }
}
