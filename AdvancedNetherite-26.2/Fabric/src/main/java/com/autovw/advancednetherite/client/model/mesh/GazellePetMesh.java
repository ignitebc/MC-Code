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
 * design/pets/companions.py 가 생성한 메시. 직접 고치지 말고 스크립트를 고친 뒤 다시 생성한다.
 * 복셀 한 칸은 0.5 유닛이고, 텍스처는 복셀당 4px 로 구워져 있다.
 * 다른 박스에 완전히 가려지는 면은 faces 집합에서 빼서 그리지 않는다.
 */
public final class GazellePetMesh
{
    private static final Set<Direction> FACES_DES = EnumSet.of(Direction.DOWN, Direction.EAST, Direction.SOUTH);
    private static final Set<Direction> FACES_DNE = EnumSet.of(Direction.DOWN, Direction.NORTH, Direction.EAST);
    private static final Set<Direction> FACES_DNES = EnumSet.of(Direction.DOWN, Direction.NORTH, Direction.EAST, Direction.SOUTH);
    private static final Set<Direction> FACES_DUES = EnumSet.of(Direction.DOWN, Direction.UP, Direction.EAST, Direction.SOUTH);
    private static final Set<Direction> FACES_DUNE = EnumSet.of(Direction.DOWN, Direction.UP, Direction.NORTH, Direction.EAST);
    private static final Set<Direction> FACES_DUNES = EnumSet.of(Direction.DOWN, Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH);
    private static final Set<Direction> FACES_DUWES = EnumSet.of(Direction.DOWN, Direction.UP, Direction.WEST, Direction.EAST, Direction.SOUTH);
    private static final Set<Direction> FACES_DUWN = EnumSet.of(Direction.DOWN, Direction.UP, Direction.WEST, Direction.NORTH);
    private static final Set<Direction> FACES_DUWNE = EnumSet.of(Direction.DOWN, Direction.UP, Direction.WEST, Direction.NORTH, Direction.EAST);
    private static final Set<Direction> FACES_DUWNES = EnumSet.of(Direction.DOWN, Direction.UP, Direction.WEST, Direction.NORTH, Direction.EAST, Direction.SOUTH);
    private static final Set<Direction> FACES_DUWNS = EnumSet.of(Direction.DOWN, Direction.UP, Direction.WEST, Direction.NORTH, Direction.SOUTH);
    private static final Set<Direction> FACES_DUWS = EnumSet.of(Direction.DOWN, Direction.UP, Direction.WEST, Direction.SOUTH);
    private static final Set<Direction> FACES_DWES = EnumSet.of(Direction.DOWN, Direction.WEST, Direction.EAST, Direction.SOUTH);
    private static final Set<Direction> FACES_DWN = EnumSet.of(Direction.DOWN, Direction.WEST, Direction.NORTH);
    private static final Set<Direction> FACES_DWNE = EnumSet.of(Direction.DOWN, Direction.WEST, Direction.NORTH, Direction.EAST);
    private static final Set<Direction> FACES_DWNES = EnumSet.of(Direction.DOWN, Direction.WEST, Direction.NORTH, Direction.EAST, Direction.SOUTH);
    private static final Set<Direction> FACES_DWNS = EnumSet.of(Direction.DOWN, Direction.WEST, Direction.NORTH, Direction.SOUTH);
    private static final Set<Direction> FACES_DWS = EnumSet.of(Direction.DOWN, Direction.WEST, Direction.SOUTH);
    private static final Set<Direction> FACES_UES = EnumSet.of(Direction.UP, Direction.EAST, Direction.SOUTH);
    private static final Set<Direction> FACES_UNE = EnumSet.of(Direction.UP, Direction.NORTH, Direction.EAST);
    private static final Set<Direction> FACES_UNES = EnumSet.of(Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH);
    private static final Set<Direction> FACES_UWES = EnumSet.of(Direction.UP, Direction.WEST, Direction.EAST, Direction.SOUTH);
    private static final Set<Direction> FACES_UWN = EnumSet.of(Direction.UP, Direction.WEST, Direction.NORTH);
    private static final Set<Direction> FACES_UWNE = EnumSet.of(Direction.UP, Direction.WEST, Direction.NORTH, Direction.EAST);
    private static final Set<Direction> FACES_UWNES = EnumSet.of(Direction.UP, Direction.WEST, Direction.NORTH, Direction.EAST, Direction.SOUTH);
    private static final Set<Direction> FACES_UWNS = EnumSet.of(Direction.UP, Direction.WEST, Direction.NORTH, Direction.SOUTH);
    private static final Set<Direction> FACES_UWS = EnumSet.of(Direction.UP, Direction.WEST, Direction.SOUTH);
    private static final Set<Direction> FACES_WNES = EnumSet.of(Direction.WEST, Direction.NORTH, Direction.EAST, Direction.SOUTH);

    private GazellePetMesh()
    {
    }

    public static LayerDefinition create()
    {
        MeshDefinition meshDefinition = new MeshDefinition();
        PartDefinition root = meshDefinition.getRoot();

        PartDefinition gazelle = root.addOrReplaceChild("gazelle", gazelleCubes(), PartPose.offset(0.0F, 15.0F, 0.0F));
        PartDefinition head = gazelle.addOrReplaceChild("head", headCubes(), PartPose.offset(0.05F, -5.0F, -5.1F));
        head.addOrReplaceChild("rightEar", rightEarCubes(), PartPose.offsetAndRotation(2.5F, -3.0F, 0.1F, 0.0F, 0.0F, -0.25F));
        head.addOrReplaceChild("leftEar", leftEarCubes(), PartPose.offsetAndRotation(-2.5F, -3.0F, 0.1F, 0.0F, 0.0F, 0.25F));
        head.addOrReplaceChild("rightHorn", rightHornCubes(), PartPose.offset(1.8F, -4.0F, 0.1F));
        head.addOrReplaceChild("leftHorn", leftHornCubes(), PartPose.offset(-1.8F, -4.0F, 0.1F));
        gazelle.addOrReplaceChild("rightFronLeg", rightFronLegCubes(), PartPose.offset(2.4F, 2.5F, -4.5F));
        gazelle.addOrReplaceChild("leftFronLeg", leftFronLegCubes(), PartPose.offset(-2.4F, 2.5F, -4.5F));
        gazelle.addOrReplaceChild("rightBackLeg", rightBackLegCubes(), PartPose.offset(2.4F, 2.5F, 4.5F));
        gazelle.addOrReplaceChild("leftBackLeg", leftBackLegCubes(), PartPose.offset(-2.4F, 2.5F, 4.5F));
        gazelle.addOrReplaceChild("tail", tailCubes(), PartPose.offset(0.05F, -1.1F, 6.1F));

        return LayerDefinition.create(meshDefinition, 128, 56);
    }

    private static CubeListBuilder gazelleCubes()
    {
        return CubeListBuilder.create()
                .texOffs(0, 0).addBox(-2.5F, -2.5F, -3.5F, 5.0F, 5.0F, 8.0F, FACES_DUWNES)
                .texOffs(78, 0).addBox(-2.0F, -6.5F, -5.5F, 4.0F, 7.5F, 2.0F, FACES_DUWNES)
                .texOffs(106, 21).addBox(-2.0F, -2.0F, 4.5F, 4.0F, 4.0F, 1.5F, FACES_DUWES)
                .texOffs(90, 0).addBox(-1.5F, -3.5F, -3.5F, 3.0F, 1.0F, 7.5F, FACES_DWES)
                .texOffs(0, 13).addBox(-1.5F, 2.5F, -3.0F, 3.0F, 1.0F, 7.0F, FACES_UWNES)
                .texOffs(62, 13).addBox(-1.5F, -6.5F, -6.5F, 3.0F, 6.0F, 1.0F, FACES_DUWNE)
                .texOffs(26, 0).addBox(-3.0F, -2.0F, -3.5F, 0.5F, 4.0F, 8.0F, FACES_DUWNS)
                .texOffs(43, 0).addBox(2.5F, -2.0F, -3.5F, 0.5F, 4.0F, 8.0F, FACES_DUNES)
                .texOffs(27, 34).addBox(-1.5F, -6.5F, -3.5F, 3.0F, 3.0F, 1.0F, FACES_DWES)
                .texOffs(20, 13).addBox(-3.5F, -1.5F, -2.0F, 0.5F, 3.0F, 5.0F, FACES_DUWNS)
                .texOffs(31, 13).addBox(3.0F, -1.5F, -2.0F, 0.5F, 3.0F, 5.0F, FACES_DUNES)
                .texOffs(93, 39).addBox(-1.5F, 1.0F, -5.0F, 3.0F, 1.5F, 1.5F, FACES_UWNE)
                .texOffs(11, 34).addBox(-1.5F, -2.0F, 6.0F, 3.0F, 4.0F, 0.5F, FACES_DUWES)
                .texOffs(102, 39).addBox(-1.5F, -7.5F, -5.5F, 3.0F, 1.0F, 2.0F, FACES_DWNES)
                .texOffs(18, 34).addBox(-1.0F, -5.5F, -7.0F, 2.0F, 4.0F, 0.5F, FACES_DUWNE)
                .texOffs(102, 13).addBox(-2.5F, -3.0F, -2.5F, 1.0F, 0.5F, 6.0F, FACES_DWNS)
                .texOffs(0, 21).addBox(-2.5F, 2.5F, -2.5F, 1.0F, 0.5F, 6.0F, FACES_UWNS)
                .texOffs(14, 21).addBox(1.5F, -3.0F, -2.5F, 1.0F, 0.5F, 6.0F, FACES_DNES)
                .texOffs(28, 21).addBox(1.5F, 2.5F, -2.5F, 1.0F, 0.5F, 6.0F, FACES_UNES)
                .texOffs(43, 46).addBox(-1.5F, -2.5F, 4.5F, 3.0F, 0.5F, 1.5F, FACES_DWES)
                .texOffs(52, 46).addBox(-1.5F, 2.0F, 4.5F, 3.0F, 0.5F, 1.5F, FACES_UWES)
                .texOffs(22, 43).addBox(-1.0F, -5.5F, -2.5F, 2.0F, 2.0F, 0.5F, FACES_DWES)
                .texOffs(27, 43).addBox(-1.0F, -1.0F, 6.5F, 2.0F, 2.0F, 0.5F, FACES_DUWES)
                .texOffs(32, 43).addBox(-1.0F, -8.0F, -5.5F, 2.0F, 0.5F, 2.0F, FACES_DWNES)
                .texOffs(44, 28).addBox(-2.5F, -2.0F, -4.5F, 0.5F, 4.0F, 1.0F, FACES_DUWN)
                .texOffs(47, 28).addBox(-2.5F, -2.0F, 4.5F, 0.5F, 4.0F, 1.0F, FACES_DUWS)
                .texOffs(50, 28).addBox(2.0F, -2.0F, -4.5F, 0.5F, 4.0F, 1.0F, FACES_DUNE)
                .texOffs(53, 28).addBox(2.0F, -2.0F, 4.5F, 0.5F, 4.0F, 1.0F, FACES_DUES)
                .texOffs(115, 46).addBox(-1.5F, -0.5F, -6.0F, 3.0F, 1.0F, 0.5F, FACES_UWNE)
                .texOffs(0, 49).addBox(-1.5F, 1.0F, -5.5F, 3.0F, 1.0F, 0.5F, FACES_UWNE)
                .texOffs(7, 49).addBox(-1.5F, -3.0F, 4.0F, 3.0F, 0.5F, 1.0F, FACES_DWES)
                .texOffs(15, 49).addBox(-1.5F, 2.5F, -4.0F, 3.0F, 0.5F, 1.0F, FACES_UWNE)
                .texOffs(23, 49).addBox(-1.5F, 2.5F, 4.0F, 3.0F, 0.5F, 1.0F, FACES_UWES)
                .texOffs(42, 21).addBox(-3.0F, -2.5F, -2.5F, 0.5F, 0.5F, 6.0F, FACES_DWNS)
                .texOffs(55, 21).addBox(-3.0F, 2.0F, -2.5F, 0.5F, 0.5F, 6.0F, FACES_UWNS)
                .texOffs(68, 21).addBox(2.5F, -2.5F, -2.5F, 0.5F, 0.5F, 6.0F, FACES_DNES)
                .texOffs(81, 21).addBox(2.5F, 2.0F, -2.5F, 0.5F, 0.5F, 6.0F, FACES_UNES)
                .texOffs(117, 21).addBox(-2.0F, -6.0F, -6.0F, 0.5F, 5.0F, 0.5F, FACES_DUWN)
                .texOffs(119, 21).addBox(1.5F, -6.0F, -6.0F, 0.5F, 5.0F, 0.5F, FACES_DUNE)
                .texOffs(0, 28).addBox(-2.0F, -3.5F, -2.0F, 0.5F, 0.5F, 5.0F, FACES_DWNS)
                .texOffs(11, 28).addBox(-2.0F, 3.0F, -2.0F, 0.5F, 0.5F, 5.0F, FACES_UWNS)
                .texOffs(22, 28).addBox(1.5F, -3.5F, -2.0F, 0.5F, 0.5F, 5.0F, FACES_DNES)
                .texOffs(33, 28).addBox(1.5F, 3.0F, -2.0F, 0.5F, 0.5F, 5.0F, FACES_UNES)
                .texOffs(31, 49).addBox(-1.0F, -7.5F, -6.0F, 2.0F, 1.0F, 0.5F, FACES_DWNE)
                .texOffs(36, 49).addBox(-1.0F, -7.5F, -3.5F, 2.0F, 1.0F, 0.5F, FACES_DWES)
                .texOffs(112, 39).addBox(-2.0F, -4.5F, -3.5F, 0.5F, 2.0F, 1.0F, FACES_DWS)
                .texOffs(115, 39).addBox(1.5F, -4.5F, -3.5F, 0.5F, 2.0F, 1.0F, FACES_DES)
                .texOffs(17, 39).addBox(-3.0F, -1.5F, -4.0F, 0.5F, 3.0F, 0.5F, FACES_DUWN)
                .texOffs(19, 39).addBox(-3.0F, -1.5F, 4.5F, 0.5F, 3.0F, 0.5F, FACES_DUWS)
                .texOffs(21, 39).addBox(2.5F, -1.5F, -4.0F, 0.5F, 3.0F, 0.5F, FACES_DUNE)
                .texOffs(23, 39).addBox(2.5F, -1.5F, 4.5F, 0.5F, 3.0F, 0.5F, FACES_DUES)
                .texOffs(40, 43).addBox(-2.0F, 1.0F, -4.5F, 0.5F, 1.5F, 1.0F, FACES_UWN)
                .texOffs(43, 43).addBox(1.5F, 1.0F, -4.5F, 0.5F, 1.5F, 1.0F, FACES_UNE)
                .texOffs(122, 51).addBox(-1.0F, -7.0F, -6.5F, 2.0F, 0.5F, 0.5F, FACES_DWNE)
                .texOffs(0, 53).addBox(-1.0F, -7.0F, -3.0F, 2.0F, 0.5F, 0.5F, FACES_DWES)
                .texOffs(5, 53).addBox(-1.0F, -3.0F, 5.0F, 2.0F, 0.5F, 0.5F, FACES_DWES)
                .texOffs(10, 53).addBox(-1.0F, -0.5F, -6.5F, 2.0F, 0.5F, 0.5F, FACES_UWNE)
                .texOffs(15, 53).addBox(-1.0F, 0.5F, -6.0F, 2.0F, 0.5F, 0.5F, FACES_UWNE)
                .texOffs(20, 53).addBox(-1.0F, 2.5F, -4.5F, 2.0F, 0.5F, 0.5F, FACES_UWNE)
                .texOffs(25, 53).addBox(-1.0F, 2.5F, 5.0F, 2.0F, 0.5F, 0.5F, FACES_UWES)
                .texOffs(46, 43).addBox(-3.5F, -1.0F, -2.5F, 0.5F, 2.0F, 0.5F, FACES_DUWN)
                .texOffs(48, 43).addBox(-3.5F, -1.0F, 3.0F, 0.5F, 2.0F, 0.5F, FACES_DUWS)
                .texOffs(50, 43).addBox(-2.5F, -1.0F, -5.0F, 0.5F, 2.0F, 0.5F, FACES_DUWN)
                .texOffs(52, 43).addBox(-2.5F, -1.0F, 5.5F, 0.5F, 2.0F, 0.5F, FACES_DUWS)
                .texOffs(54, 43).addBox(-2.0F, -4.5F, -6.5F, 0.5F, 2.0F, 0.5F, FACES_DUWN)
                .texOffs(56, 43).addBox(-2.0F, -1.0F, 6.0F, 0.5F, 2.0F, 0.5F, FACES_DUWS)
                .texOffs(58, 43).addBox(1.5F, -4.5F, -6.5F, 0.5F, 2.0F, 0.5F, FACES_DUNE)
                .texOffs(60, 43).addBox(1.5F, -1.0F, 6.0F, 0.5F, 2.0F, 0.5F, FACES_DUES)
                .texOffs(62, 43).addBox(2.0F, -1.0F, -5.0F, 0.5F, 2.0F, 0.5F, FACES_DUNE)
                .texOffs(64, 43).addBox(2.0F, -1.0F, 5.5F, 0.5F, 2.0F, 0.5F, FACES_DUES)
                .texOffs(66, 43).addBox(3.0F, -1.0F, -2.5F, 0.5F, 2.0F, 0.5F, FACES_DUNE)
                .texOffs(68, 43).addBox(3.0F, -1.0F, 3.0F, 0.5F, 2.0F, 0.5F, FACES_DUES)
                .texOffs(61, 46).addBox(-2.0F, -6.0F, -3.5F, 0.5F, 1.5F, 0.5F, FACES_DWS)
                .texOffs(63, 46).addBox(1.5F, -6.0F, -3.5F, 0.5F, 1.5F, 0.5F, FACES_DES)
                .texOffs(30, 53).addBox(-0.5F, -6.0F, -7.0F, 1.0F, 0.5F, 0.5F, FACES_DWNE)
                .texOffs(33, 53).addBox(-0.5F, -6.0F, -2.5F, 1.0F, 0.5F, 0.5F, FACES_DWES)
                .texOffs(36, 53).addBox(-0.5F, -3.5F, 4.0F, 1.0F, 0.5F, 0.5F, FACES_DWES)
                .texOffs(39, 53).addBox(-0.5F, -1.5F, -7.0F, 1.0F, 0.5F, 0.5F, FACES_UWNE)
                .texOffs(42, 53).addBox(-0.5F, -1.5F, 6.5F, 1.0F, 0.5F, 0.5F, FACES_DWES)
                .texOffs(45, 53).addBox(-0.5F, 1.0F, -6.0F, 1.0F, 0.5F, 0.5F, FACES_UWNE)
                .texOffs(48, 53).addBox(-0.5F, 1.0F, 6.5F, 1.0F, 0.5F, 0.5F, FACES_UWES)
                .texOffs(51, 53).addBox(-0.5F, 3.0F, -3.5F, 1.0F, 0.5F, 0.5F, FACES_UWNE)
                .texOffs(54, 53).addBox(-0.5F, 3.0F, 4.0F, 1.0F, 0.5F, 0.5F, FACES_UWES)
                .texOffs(41, 49).addBox(-2.0F, 1.0F, -5.0F, 0.5F, 1.0F, 0.5F, FACES_UWN)
                .texOffs(43, 49).addBox(-1.5F, -0.5F, 6.5F, 0.5F, 1.0F, 0.5F, FACES_DUWS)
                .texOffs(45, 49).addBox(1.0F, -0.5F, 6.5F, 0.5F, 1.0F, 0.5F, FACES_DUES)
                .texOffs(47, 49).addBox(1.5F, 1.0F, -5.0F, 0.5F, 1.0F, 0.5F, FACES_UNE)
                .texOffs(49, 49).addBox(-3.5F, -2.0F, 0.0F, 0.5F, 0.5F, 1.0F, FACES_DWNS)
                .texOffs(52, 49).addBox(-3.5F, 1.5F, 0.0F, 0.5F, 0.5F, 1.0F, FACES_UWNS)
                .texOffs(55, 49).addBox(-2.0F, -3.0F, 3.5F, 0.5F, 0.5F, 1.0F, FACES_DWS)
                .texOffs(58, 49).addBox(-2.0F, -2.5F, 4.5F, 0.5F, 0.5F, 1.0F, FACES_DWS)
                .texOffs(61, 49).addBox(-2.0F, 2.0F, 4.5F, 0.5F, 0.5F, 1.0F, FACES_UWS)
                .texOffs(64, 49).addBox(-2.0F, 2.5F, -3.5F, 0.5F, 0.5F, 1.0F, FACES_UWN)
                .texOffs(67, 49).addBox(-2.0F, 2.5F, 3.5F, 0.5F, 0.5F, 1.0F, FACES_UWS)
                .texOffs(70, 49).addBox(1.5F, -3.0F, 3.5F, 0.5F, 0.5F, 1.0F, FACES_DES)
                .texOffs(73, 49).addBox(1.5F, -2.5F, 4.5F, 0.5F, 0.5F, 1.0F, FACES_DES)
                .texOffs(76, 49).addBox(1.5F, 2.0F, 4.5F, 0.5F, 0.5F, 1.0F, FACES_UES)
                .texOffs(79, 49).addBox(1.5F, 2.5F, -3.5F, 0.5F, 0.5F, 1.0F, FACES_UNE)
                .texOffs(82, 49).addBox(1.5F, 2.5F, 3.5F, 0.5F, 0.5F, 1.0F, FACES_UES)
                .texOffs(85, 49).addBox(3.0F, -2.0F, 0.0F, 0.5F, 0.5F, 1.0F, FACES_DNES)
                .texOffs(88, 49).addBox(3.0F, 1.5F, 0.0F, 0.5F, 0.5F, 1.0F, FACES_UNES)
                .texOffs(57, 53).addBox(-1.5F, -7.0F, -6.0F, 0.5F, 0.5F, 0.5F, FACES_DWN)
                .texOffs(59, 53).addBox(-1.5F, -7.0F, -3.5F, 0.5F, 0.5F, 0.5F, FACES_DWS)
                .texOffs(61, 53).addBox(1.0F, -7.0F, -6.0F, 0.5F, 0.5F, 0.5F, FACES_DNE)
                .texOffs(63, 53).addBox(1.0F, -7.0F, -3.5F, 0.5F, 0.5F, 0.5F, FACES_DES);
    }

    private static CubeListBuilder headCubes()
    {
        return CubeListBuilder.create()
                .texOffs(60, 0).addBox(-2.0F, -4.5F, -3.0F, 4.0F, 6.0F, 5.0F, FACES_DUWNES)
                .texOffs(94, 21).addBox(-1.5F, -1.0F, -6.0F, 3.0F, 3.0F, 3.0F, FACES_DUWNE)
                .texOffs(42, 13).addBox(-3.0F, -3.5F, -2.5F, 1.0F, 4.0F, 4.0F, FACES_DUWNS)
                .texOffs(52, 13).addBox(2.0F, -3.5F, -2.5F, 1.0F, 4.0F, 4.0F, FACES_DUNES)
                .texOffs(35, 34).addBox(-1.5F, -3.0F, 2.0F, 3.0F, 3.0F, 1.0F, FACES_DUWES)
                .texOffs(56, 28).addBox(-1.5F, 1.5F, -3.0F, 3.0F, 0.5F, 4.5F, FACES_UWES)
                .texOffs(25, 39).addBox(-2.0F, -5.0F, -2.0F, 4.0F, 0.5F, 3.0F, FACES_DWNES)
                .texOffs(118, 39).addBox(-1.5F, -3.0F, -4.0F, 3.0F, 2.0F, 1.0F, FACES_DWNE)
                .texOffs(70, 43).addBox(-1.5F, -5.5F, -1.5F, 3.0F, 0.5F, 2.0F, FACES_DWNES)
                .texOffs(80, 43).addBox(-1.5F, 2.0F, -1.5F, 3.0F, 0.5F, 2.0F, FACES_UWNES)
                .texOffs(71, 28).addBox(-2.0F, -1.0F, -5.0F, 0.5F, 3.0F, 2.0F, FACES_DUWNS)
                .texOffs(76, 28).addBox(1.5F, -1.0F, -5.0F, 0.5F, 3.0F, 2.0F, FACES_DUNES)
                .texOffs(90, 43).addBox(-1.0F, -0.5F, -6.5F, 2.0F, 2.0F, 0.5F, FACES_DUWNE)
                .texOffs(91, 49).addBox(-1.5F, -4.0F, -3.5F, 3.0F, 1.0F, 0.5F, FACES_DWNE)
                .texOffs(98, 49).addBox(-1.5F, -4.0F, 2.0F, 3.0F, 1.0F, 0.5F, FACES_DWES)
                .texOffs(105, 49).addBox(-1.5F, 0.0F, 2.0F, 3.0F, 1.0F, 0.5F, FACES_UWES)
                .texOffs(43, 34).addBox(-2.5F, -4.5F, -2.0F, 0.5F, 1.0F, 3.0F, FACES_DWNS)
                .texOffs(50, 34).addBox(-2.5F, 0.5F, -2.0F, 0.5F, 1.0F, 3.0F, FACES_UWNS)
                .texOffs(57, 34).addBox(2.0F, -4.5F, -2.0F, 0.5F, 1.0F, 3.0F, FACES_DNES)
                .texOffs(64, 34).addBox(2.0F, 0.5F, -2.0F, 0.5F, 1.0F, 3.0F, FACES_UNES)
                .texOffs(112, 49).addBox(-1.0F, -5.0F, -3.0F, 2.0F, 0.5F, 1.0F, FACES_DWNE)
                .texOffs(118, 49).addBox(-1.0F, -5.0F, 1.0F, 2.0F, 0.5F, 1.0F, FACES_DWES)
                .texOffs(95, 43).addBox(-3.0F, -3.0F, -3.0F, 1.0F, 2.0F, 0.5F, FACES_DUWN)
                .texOffs(98, 43).addBox(-2.5F, -2.5F, 2.0F, 1.0F, 2.0F, 0.5F, FACES_DUWS)
                .texOffs(101, 43).addBox(1.5F, -2.5F, 2.0F, 1.0F, 2.0F, 0.5F, FACES_DUES)
                .texOffs(104, 43).addBox(2.0F, -3.0F, -3.0F, 1.0F, 2.0F, 0.5F, FACES_DUNE)
                .texOffs(23, 34).addBox(-2.5F, -3.5F, 1.5F, 0.5F, 4.0F, 0.5F, FACES_DUWS)
                .texOffs(25, 34).addBox(2.0F, -3.5F, 1.5F, 0.5F, 4.0F, 0.5F, FACES_DUES)
                .texOffs(0, 43).addBox(-2.0F, -0.5F, -6.0F, 0.5F, 2.0F, 1.0F, FACES_DUWN)
                .texOffs(3, 43).addBox(1.5F, -0.5F, -6.0F, 0.5F, 2.0F, 1.0F, FACES_DUNE)
                .texOffs(65, 46).addBox(-2.5F, -2.5F, -3.5F, 1.0F, 1.5F, 0.5F, FACES_DWN)
                .texOffs(68, 46).addBox(1.5F, -2.5F, -3.5F, 1.0F, 1.5F, 0.5F, FACES_DNE)
                .texOffs(39, 39).addBox(-2.0F, 1.5F, -2.0F, 0.5F, 0.5F, 3.0F, FACES_UWNS)
                .texOffs(46, 39).addBox(1.5F, 1.5F, -2.0F, 0.5F, 0.5F, 3.0F, FACES_UNES)
                .texOffs(65, 53).addBox(-1.0F, -5.5F, -2.0F, 2.0F, 0.5F, 0.5F, FACES_DWNE)
                .texOffs(70, 53).addBox(-1.0F, -5.5F, 0.5F, 2.0F, 0.5F, 0.5F, FACES_DWES)
                .texOffs(75, 53).addBox(-1.0F, -4.5F, -3.5F, 2.0F, 0.5F, 0.5F, FACES_DWNE)
                .texOffs(80, 53).addBox(-1.0F, -4.5F, 2.0F, 2.0F, 0.5F, 0.5F, FACES_DWES)
                .texOffs(85, 53).addBox(-1.0F, 1.0F, 2.0F, 2.0F, 0.5F, 0.5F, FACES_UWES)
                .texOffs(90, 53).addBox(-1.0F, 1.5F, 1.5F, 2.0F, 0.5F, 0.5F, FACES_UWES)
                .texOffs(95, 53).addBox(-1.0F, 2.0F, -2.0F, 2.0F, 0.5F, 0.5F, FACES_UWNE)
                .texOffs(100, 53).addBox(-1.0F, 2.0F, 0.5F, 2.0F, 0.5F, 0.5F, FACES_UWES)
                .texOffs(107, 43).addBox(-3.0F, -4.0F, -1.5F, 0.5F, 0.5F, 2.0F, FACES_DWNS)
                .texOffs(112, 43).addBox(-3.0F, 0.5F, -1.5F, 0.5F, 0.5F, 2.0F, FACES_UWNS)
                .texOffs(117, 43).addBox(2.5F, -4.0F, -1.5F, 0.5F, 0.5F, 2.0F, FACES_DNES)
                .texOffs(122, 43).addBox(2.5F, 0.5F, -1.5F, 0.5F, 0.5F, 2.0F, FACES_UNES)
                .texOffs(71, 46).addBox(-2.5F, -1.0F, -3.0F, 0.5F, 1.5F, 0.5F, FACES_UWN)
                .texOffs(73, 46).addBox(2.0F, -1.0F, -3.0F, 0.5F, 1.5F, 0.5F, FACES_UNE)
                .texOffs(105, 53).addBox(-0.5F, -3.5F, -4.0F, 1.0F, 0.5F, 0.5F, FACES_DWNE)
                .texOffs(108, 53).addBox(-0.5F, -3.5F, 2.5F, 1.0F, 0.5F, 0.5F, FACES_DWES)
                .texOffs(111, 53).addBox(-0.5F, 0.0F, 2.5F, 1.0F, 0.5F, 0.5F, FACES_UWES)
                .texOffs(114, 53).addBox(-0.5F, 1.5F, -6.5F, 1.0F, 0.5F, 0.5F, FACES_UWNE)
                .texOffs(124, 49).addBox(-2.0F, -3.5F, -3.5F, 0.5F, 1.0F, 0.5F, FACES_DWN)
                .texOffs(126, 49).addBox(-2.0F, -3.5F, 2.0F, 0.5F, 1.0F, 0.5F, FACES_DWS)
                .texOffs(0, 51).addBox(-2.0F, -0.5F, 2.0F, 0.5F, 1.0F, 0.5F, FACES_UWS)
                .texOffs(2, 51).addBox(-1.5F, 0.0F, -6.5F, 0.5F, 1.0F, 0.5F, FACES_DUWN)
                .texOffs(4, 51).addBox(1.0F, 0.0F, -6.5F, 0.5F, 1.0F, 0.5F, FACES_DUNE)
                .texOffs(6, 51).addBox(1.5F, -3.5F, -3.5F, 0.5F, 1.0F, 0.5F, FACES_DNE)
                .texOffs(8, 51).addBox(1.5F, -3.5F, 2.0F, 0.5F, 1.0F, 0.5F, FACES_DES)
                .texOffs(10, 51).addBox(1.5F, -0.5F, 2.0F, 0.5F, 1.0F, 0.5F, FACES_UES)
                .texOffs(117, 53).addBox(-2.5F, -4.0F, -2.5F, 0.5F, 0.5F, 0.5F, FACES_DWN)
                .texOffs(119, 53).addBox(-2.5F, -4.0F, 1.0F, 0.5F, 0.5F, 0.5F, FACES_DWS)
                .texOffs(121, 53).addBox(-2.5F, -3.5F, -3.0F, 0.5F, 0.5F, 0.5F, FACES_DWN)
                .texOffs(123, 53).addBox(-2.5F, -1.0F, -3.5F, 0.5F, 0.5F, 0.5F, FACES_UWN)
                .texOffs(125, 53).addBox(-2.5F, 0.5F, -2.5F, 0.5F, 0.5F, 0.5F, FACES_UWN)
                .texOffs(0, 54).addBox(-2.5F, 0.5F, 1.0F, 0.5F, 0.5F, 0.5F, FACES_UWS)
                .texOffs(2, 54).addBox(-1.5F, -5.0F, -2.5F, 0.5F, 0.5F, 0.5F, FACES_DWN)
                .texOffs(4, 54).addBox(-1.5F, -5.0F, 1.0F, 0.5F, 0.5F, 0.5F, FACES_DWS)
                .texOffs(6, 54).addBox(1.0F, -5.0F, -2.5F, 0.5F, 0.5F, 0.5F, FACES_DNE)
                .texOffs(8, 54).addBox(1.0F, -5.0F, 1.0F, 0.5F, 0.5F, 0.5F, FACES_DES)
                .texOffs(10, 54).addBox(2.0F, -4.0F, -2.5F, 0.5F, 0.5F, 0.5F, FACES_DNE)
                .texOffs(12, 54).addBox(2.0F, -4.0F, 1.0F, 0.5F, 0.5F, 0.5F, FACES_DES)
                .texOffs(14, 54).addBox(2.0F, -3.5F, -3.0F, 0.5F, 0.5F, 0.5F, FACES_DNE)
                .texOffs(16, 54).addBox(2.0F, -1.0F, -3.5F, 0.5F, 0.5F, 0.5F, FACES_UNE)
                .texOffs(18, 54).addBox(2.0F, 0.5F, -2.5F, 0.5F, 0.5F, 0.5F, FACES_UNE)
                .texOffs(20, 54).addBox(2.0F, 0.5F, 1.0F, 0.5F, 0.5F, 0.5F, FACES_UES);
    }

    private static CubeListBuilder rightEarCubes()
    {
        return CubeListBuilder.create()
                .texOffs(71, 34).addBox(0.5F, -1.5F, -1.0F, 3.0F, 2.0F, 2.0F, FACES_DUWNES)
                .texOffs(6, 43).addBox(-0.5F, -1.5F, -0.5F, 1.0F, 2.0F, 1.0F, FACES_DUWNS)
                .texOffs(10, 43).addBox(3.5F, -1.5F, -0.5F, 1.0F, 2.0F, 1.0F, FACES_DUNES)
                .texOffs(12, 51).addBox(0.5F, -2.0F, -0.5F, 3.0F, 0.5F, 1.0F, FACES_DWNES)
                .texOffs(20, 51).addBox(0.5F, 0.5F, -0.5F, 3.0F, 0.5F, 1.0F, FACES_UWNES)
                .texOffs(75, 46).addBox(-1.0F, -1.0F, -0.5F, 0.5F, 1.0F, 1.0F, FACES_DUWNS)
                .texOffs(78, 46).addBox(4.5F, -1.0F, -0.5F, 0.5F, 1.0F, 1.0F, FACES_DUNES)
                .texOffs(28, 51).addBox(0.0F, -1.0F, -1.0F, 0.5F, 1.0F, 0.5F, FACES_DUWN)
                .texOffs(30, 51).addBox(0.0F, -1.0F, 0.5F, 0.5F, 1.0F, 0.5F, FACES_DUWS)
                .texOffs(32, 51).addBox(3.5F, -1.0F, -1.0F, 0.5F, 1.0F, 0.5F, FACES_DUNE)
                .texOffs(34, 51).addBox(3.5F, -1.0F, 0.5F, 0.5F, 1.0F, 0.5F, FACES_DUES);
    }

    private static CubeListBuilder leftEarCubes()
    {
        return CubeListBuilder.create()
                .texOffs(81, 34).addBox(-3.5F, -1.5F, -1.0F, 3.0F, 2.0F, 2.0F, FACES_DUWNES)
                .texOffs(14, 43).addBox(-4.5F, -1.5F, -0.5F, 1.0F, 2.0F, 1.0F, FACES_DUWNS)
                .texOffs(18, 43).addBox(-0.5F, -1.5F, -0.5F, 1.0F, 2.0F, 1.0F, FACES_DUNES)
                .texOffs(36, 51).addBox(-3.5F, -2.0F, -0.5F, 3.0F, 0.5F, 1.0F, FACES_DWNES)
                .texOffs(44, 51).addBox(-3.5F, 0.5F, -0.5F, 3.0F, 0.5F, 1.0F, FACES_UWNES)
                .texOffs(81, 46).addBox(-5.0F, -1.0F, -0.5F, 0.5F, 1.0F, 1.0F, FACES_DUWNS)
                .texOffs(84, 46).addBox(0.5F, -1.0F, -0.5F, 0.5F, 1.0F, 1.0F, FACES_DUNES)
                .texOffs(52, 51).addBox(-4.0F, -1.0F, -1.0F, 0.5F, 1.0F, 0.5F, FACES_DUWN)
                .texOffs(54, 51).addBox(-4.0F, -1.0F, 0.5F, 0.5F, 1.0F, 0.5F, FACES_DUWS)
                .texOffs(56, 51).addBox(-0.5F, -1.0F, -1.0F, 0.5F, 1.0F, 0.5F, FACES_DUNE)
                .texOffs(58, 51).addBox(-0.5F, -1.0F, 0.5F, 0.5F, 1.0F, 0.5F, FACES_DUES);
    }

    private static CubeListBuilder rightHornCubes()
    {
        return CubeListBuilder.create()
                .texOffs(91, 34).addBox(0.5F, -5.0F, 1.0F, 1.0F, 3.0F, 1.0F, FACES_DUWNES)
                .texOffs(0, 46).addBox(-0.5F, -1.5F, 0.0F, 1.5F, 1.5F, 1.0F, FACES_DUWNES)
                .texOffs(87, 46).addBox(-0.5F, -1.0F, -0.5F, 1.5F, 1.5F, 0.5F, FACES_DUWNE)
                .texOffs(60, 51).addBox(0.0F, -2.5F, 0.5F, 1.5F, 1.0F, 0.5F, FACES_DWNE)
                .texOffs(91, 46).addBox(0.0F, -7.0F, 0.5F, 1.0F, 1.5F, 0.5F, FACES_DUWNES)
                .texOffs(94, 46).addBox(0.0F, -2.0F, 1.0F, 1.0F, 1.5F, 0.5F, FACES_UWES)
                .texOffs(22, 54).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 0.5F, 0.5F, FACES_UWES)
                .texOffs(64, 51).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 0.5F, FACES_DUWNE)
                .texOffs(97, 46).addBox(0.0F, -7.5F, 0.0F, 0.5F, 1.5F, 0.5F, FACES_DUWNES)
                .texOffs(99, 46).addBox(0.5F, -6.5F, 1.0F, 0.5F, 1.5F, 0.5F, FACES_DWES)
                .texOffs(27, 54).addBox(-0.5F, 0.0F, 0.5F, 1.0F, 0.5F, 0.5F, FACES_UWES)
                .texOffs(30, 54).addBox(0.0F, -2.0F, 0.0F, 1.0F, 0.5F, 0.5F, FACES_DWNE)
                .texOffs(67, 51).addBox(-1.0F, -0.5F, -0.5F, 0.5F, 1.0F, 0.5F, FACES_DUWN)
                .texOffs(69, 51).addBox(0.0F, -3.0F, 1.0F, 0.5F, 1.0F, 0.5F, FACES_DWNS)
                .texOffs(33, 54).addBox(-1.0F, -0.5F, 0.0F, 0.5F, 0.5F, 0.5F, FACES_DWS)
                .texOffs(35, 54).addBox(0.5F, -5.5F, 0.5F, 0.5F, 0.5F, 0.5F, FACES_UWNE)
                .texOffs(37, 54).addBox(0.5F, -5.5F, 1.5F, 0.5F, 0.5F, 0.5F, FACES_DWES)
                .texOffs(39, 54).addBox(0.5F, -3.0F, 0.5F, 0.5F, 0.5F, 0.5F, FACES_DWNE)
                .texOffs(41, 54).addBox(0.5F, -2.0F, 1.5F, 0.5F, 0.5F, 0.5F, FACES_UWES)
                .texOffs(43, 54).addBox(1.0F, -5.5F, 1.0F, 0.5F, 0.5F, 0.5F, FACES_DNES)
                .texOffs(45, 54).addBox(1.0F, -2.0F, 1.0F, 0.5F, 0.5F, 0.5F, FACES_UES)
                .texOffs(47, 54).addBox(1.0F, -1.5F, 0.5F, 0.5F, 0.5F, 0.5F, FACES_UNES);
    }

    private static CubeListBuilder leftHornCubes()
    {
        return CubeListBuilder.create()
                .texOffs(95, 34).addBox(-1.5F, -5.0F, 1.0F, 1.0F, 3.0F, 1.0F, FACES_DUWNES)
                .texOffs(5, 46).addBox(-1.0F, -1.5F, 0.0F, 1.5F, 1.5F, 1.0F, FACES_DUWNES)
                .texOffs(101, 46).addBox(-1.0F, -1.0F, -0.5F, 1.5F, 1.5F, 0.5F, FACES_DUWNE)
                .texOffs(71, 51).addBox(-1.5F, -2.5F, 0.5F, 1.5F, 1.0F, 0.5F, FACES_DWNE)
                .texOffs(105, 46).addBox(-1.0F, -7.0F, 0.5F, 1.0F, 1.5F, 0.5F, FACES_DUWNES)
                .texOffs(108, 46).addBox(-1.0F, -2.0F, 1.0F, 1.0F, 1.5F, 0.5F, FACES_UWES)
                .texOffs(49, 54).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 0.5F, 0.5F, FACES_UWES)
                .texOffs(75, 51).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 0.5F, FACES_DUWNE)
                .texOffs(111, 46).addBox(-1.0F, -6.5F, 1.0F, 0.5F, 1.5F, 0.5F, FACES_DWES)
                .texOffs(113, 46).addBox(-0.5F, -7.5F, 0.0F, 0.5F, 1.5F, 0.5F, FACES_DUWNES)
                .texOffs(54, 54).addBox(-1.0F, -2.0F, 0.0F, 1.0F, 0.5F, 0.5F, FACES_DWNE)
                .texOffs(57, 54).addBox(-0.5F, 0.0F, 0.5F, 1.0F, 0.5F, 0.5F, FACES_UWES)
                .texOffs(78, 51).addBox(-0.5F, -3.0F, 1.0F, 0.5F, 1.0F, 0.5F, FACES_DNES)
                .texOffs(80, 51).addBox(0.5F, -0.5F, -0.5F, 0.5F, 1.0F, 0.5F, FACES_DUNE)
                .texOffs(60, 54).addBox(-1.5F, -5.5F, 1.0F, 0.5F, 0.5F, 0.5F, FACES_DWNS)
                .texOffs(62, 54).addBox(-1.5F, -2.0F, 1.0F, 0.5F, 0.5F, 0.5F, FACES_UWS)
                .texOffs(64, 54).addBox(-1.5F, -1.5F, 0.5F, 0.5F, 0.5F, 0.5F, FACES_UWNS)
                .texOffs(66, 54).addBox(-1.0F, -5.5F, 0.5F, 0.5F, 0.5F, 0.5F, FACES_UWNE)
                .texOffs(68, 54).addBox(-1.0F, -5.5F, 1.5F, 0.5F, 0.5F, 0.5F, FACES_DWES)
                .texOffs(70, 54).addBox(-1.0F, -3.0F, 0.5F, 0.5F, 0.5F, 0.5F, FACES_DWNE)
                .texOffs(72, 54).addBox(-1.0F, -2.0F, 1.5F, 0.5F, 0.5F, 0.5F, FACES_UWES)
                .texOffs(74, 54).addBox(0.5F, -0.5F, 0.0F, 0.5F, 0.5F, 0.5F, FACES_DES);
    }

    private static CubeListBuilder rightFronLegCubes()
    {
        return CubeListBuilder.create()
                .texOffs(70, 13).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 5.0F, 2.0F, FACES_DUWNES)
                .texOffs(99, 34).addBox(-1.0F, 5.0F, -1.5F, 2.0F, 1.5F, 2.5F, FACES_DUWNES)
                .texOffs(53, 39).addBox(-1.0F, -1.0F, -1.5F, 2.0F, 3.0F, 0.5F, FACES_DUWNE)
                .texOffs(58, 39).addBox(-1.0F, -1.0F, 1.0F, 2.0F, 3.0F, 0.5F, FACES_DUWES)
                .texOffs(81, 28).addBox(-1.5F, -1.0F, -1.0F, 0.5F, 3.0F, 2.0F, FACES_DUWNS)
                .texOffs(86, 28).addBox(1.0F, -1.0F, -1.0F, 0.5F, 3.0F, 2.0F, FACES_DUNES)
                .texOffs(10, 46).addBox(-0.5F, 3.5F, -0.5F, 1.0F, 1.5F, 1.0F, FACES_WNES)
                .texOffs(82, 51).addBox(-1.0F, -2.0F, -0.5F, 2.0F, 0.5F, 1.0F, FACES_DWNES)
                .texOffs(76, 54).addBox(-0.5F, -2.0F, -1.0F, 1.0F, 0.5F, 0.5F, FACES_DWNE)
                .texOffs(79, 54).addBox(-0.5F, -2.0F, 0.5F, 1.0F, 0.5F, 0.5F, FACES_DWES)
                .texOffs(82, 54).addBox(-0.5F, -1.5F, -1.5F, 1.0F, 0.5F, 0.5F, FACES_DWNE)
                .texOffs(85, 54).addBox(-0.5F, -1.5F, 1.0F, 1.0F, 0.5F, 0.5F, FACES_DWES)
                .texOffs(88, 54).addBox(-0.5F, 2.0F, -1.5F, 1.0F, 0.5F, 0.5F, FACES_UWNE)
                .texOffs(91, 54).addBox(-0.5F, 2.0F, 1.0F, 1.0F, 0.5F, 0.5F, FACES_UWES)
                .texOffs(88, 51).addBox(0.0F, 5.5F, -2.0F, 0.5F, 1.0F, 0.5F, FACES_DUWNE);
    }

    private static CubeListBuilder leftFronLegCubes()
    {
        return CubeListBuilder.create()
                .texOffs(78, 13).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 5.0F, 2.0F, FACES_DUWNES)
                .texOffs(108, 34).addBox(-1.0F, 5.0F, -1.5F, 2.0F, 1.5F, 2.5F, FACES_DUWNES)
                .texOffs(63, 39).addBox(-1.0F, -1.0F, -1.5F, 2.0F, 3.0F, 0.5F, FACES_DUWNE)
                .texOffs(68, 39).addBox(-1.0F, -1.0F, 1.0F, 2.0F, 3.0F, 0.5F, FACES_DUWES)
                .texOffs(91, 28).addBox(-1.5F, -1.0F, -1.0F, 0.5F, 3.0F, 2.0F, FACES_DUWNS)
                .texOffs(96, 28).addBox(1.0F, -1.0F, -1.0F, 0.5F, 3.0F, 2.0F, FACES_DUNES)
                .texOffs(14, 46).addBox(-0.5F, 3.5F, -0.5F, 1.0F, 1.5F, 1.0F, FACES_WNES)
                .texOffs(90, 51).addBox(-1.0F, -2.0F, -0.5F, 2.0F, 0.5F, 1.0F, FACES_DWNES)
                .texOffs(94, 54).addBox(-0.5F, -2.0F, -1.0F, 1.0F, 0.5F, 0.5F, FACES_DWNE)
                .texOffs(97, 54).addBox(-0.5F, -2.0F, 0.5F, 1.0F, 0.5F, 0.5F, FACES_DWES)
                .texOffs(100, 54).addBox(-0.5F, -1.5F, -1.5F, 1.0F, 0.5F, 0.5F, FACES_DWNE)
                .texOffs(103, 54).addBox(-0.5F, -1.5F, 1.0F, 1.0F, 0.5F, 0.5F, FACES_DWES)
                .texOffs(106, 54).addBox(-0.5F, 2.0F, -1.5F, 1.0F, 0.5F, 0.5F, FACES_UWNE)
                .texOffs(109, 54).addBox(-0.5F, 2.0F, 1.0F, 1.0F, 0.5F, 0.5F, FACES_UWES)
                .texOffs(96, 51).addBox(0.0F, 5.5F, -2.0F, 0.5F, 1.0F, 0.5F, FACES_DUWNE);
    }

    private static CubeListBuilder rightBackLegCubes()
    {
        return CubeListBuilder.create()
                .texOffs(86, 13).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 5.0F, 2.0F, FACES_DUWNES)
                .texOffs(117, 34).addBox(-1.0F, 5.0F, -1.5F, 2.0F, 1.5F, 2.5F, FACES_DUWNES)
                .texOffs(73, 39).addBox(-1.0F, -1.0F, -1.5F, 2.0F, 3.0F, 0.5F, FACES_DUWNE)
                .texOffs(78, 39).addBox(-1.0F, -1.0F, 1.0F, 2.0F, 3.0F, 0.5F, FACES_DUWES)
                .texOffs(101, 28).addBox(-1.5F, -1.0F, -1.0F, 0.5F, 3.0F, 2.0F, FACES_DUWNS)
                .texOffs(106, 28).addBox(1.0F, -1.0F, -1.0F, 0.5F, 3.0F, 2.0F, FACES_DUNES)
                .texOffs(18, 46).addBox(-0.5F, 3.5F, -0.5F, 1.0F, 1.5F, 1.0F, FACES_WNES)
                .texOffs(98, 51).addBox(-1.0F, -2.0F, -0.5F, 2.0F, 0.5F, 1.0F, FACES_DWNES)
                .texOffs(112, 54).addBox(-0.5F, -2.0F, -1.0F, 1.0F, 0.5F, 0.5F, FACES_DWNE)
                .texOffs(115, 54).addBox(-0.5F, -2.0F, 0.5F, 1.0F, 0.5F, 0.5F, FACES_DWES)
                .texOffs(118, 54).addBox(-0.5F, -1.5F, -1.5F, 1.0F, 0.5F, 0.5F, FACES_DWNE)
                .texOffs(121, 54).addBox(-0.5F, -1.5F, 1.0F, 1.0F, 0.5F, 0.5F, FACES_DWES)
                .texOffs(124, 54).addBox(-0.5F, 2.0F, -1.5F, 1.0F, 0.5F, 0.5F, FACES_UWNE)
                .texOffs(0, 55).addBox(-0.5F, 2.0F, 1.0F, 1.0F, 0.5F, 0.5F, FACES_UWES)
                .texOffs(104, 51).addBox(0.0F, 5.5F, -2.0F, 0.5F, 1.0F, 0.5F, FACES_DUWNE);
    }

    private static CubeListBuilder leftBackLegCubes()
    {
        return CubeListBuilder.create()
                .texOffs(94, 13).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 5.0F, 2.0F, FACES_DUWNES)
                .texOffs(0, 39).addBox(-1.0F, 5.0F, -1.5F, 2.0F, 1.5F, 2.5F, FACES_DUWNES)
                .texOffs(83, 39).addBox(-1.0F, -1.0F, -1.5F, 2.0F, 3.0F, 0.5F, FACES_DUWNE)
                .texOffs(88, 39).addBox(-1.0F, -1.0F, 1.0F, 2.0F, 3.0F, 0.5F, FACES_DUWES)
                .texOffs(111, 28).addBox(-1.5F, -1.0F, -1.0F, 0.5F, 3.0F, 2.0F, FACES_DUWNS)
                .texOffs(116, 28).addBox(1.0F, -1.0F, -1.0F, 0.5F, 3.0F, 2.0F, FACES_DUNES)
                .texOffs(22, 46).addBox(-0.5F, 3.5F, -0.5F, 1.0F, 1.5F, 1.0F, FACES_WNES)
                .texOffs(106, 51).addBox(-1.0F, -2.0F, -0.5F, 2.0F, 0.5F, 1.0F, FACES_DWNES)
                .texOffs(3, 55).addBox(-0.5F, -2.0F, -1.0F, 1.0F, 0.5F, 0.5F, FACES_DWNE)
                .texOffs(6, 55).addBox(-0.5F, -2.0F, 0.5F, 1.0F, 0.5F, 0.5F, FACES_DWES)
                .texOffs(9, 55).addBox(-0.5F, -1.5F, -1.5F, 1.0F, 0.5F, 0.5F, FACES_DWNE)
                .texOffs(12, 55).addBox(-0.5F, -1.5F, 1.0F, 1.0F, 0.5F, 0.5F, FACES_DWES)
                .texOffs(15, 55).addBox(-0.5F, 2.0F, -1.5F, 1.0F, 0.5F, 0.5F, FACES_UWNE)
                .texOffs(18, 55).addBox(-0.5F, 2.0F, 1.0F, 1.0F, 0.5F, 0.5F, FACES_UWES)
                .texOffs(112, 51).addBox(0.0F, 5.5F, -2.0F, 0.5F, 1.0F, 0.5F, FACES_DUWNE);
    }

    private static CubeListBuilder tailCubes()
    {
        return CubeListBuilder.create()
                .texOffs(9, 39).addBox(-1.0F, 1.0F, 2.5F, 2.0F, 2.0F, 2.0F, FACES_DUWNES)
                .texOffs(0, 34).addBox(-0.5F, 0.5F, 0.0F, 1.0F, 0.5F, 4.5F, FACES_DUWNES)
                .texOffs(26, 46).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.5F, FACES_DUWNES)
                .texOffs(31, 46).addBox(-0.5F, 1.0F, 2.0F, 1.0F, 2.0F, 0.5F, FACES_UWNE)
                .texOffs(34, 46).addBox(-0.5F, 1.0F, 4.5F, 1.0F, 2.0F, 0.5F, FACES_DUWES)
                .texOffs(37, 46).addBox(-0.5F, 3.0F, 2.5F, 1.0F, 0.5F, 2.0F, FACES_UWNES)
                .texOffs(114, 51).addBox(-0.5F, 0.0F, 1.0F, 1.0F, 0.5F, 1.0F, FACES_DWES)
                .texOffs(118, 51).addBox(-0.5F, 1.0F, 1.0F, 1.0F, 0.5F, 1.0F, FACES_UWNE);
    }
}
