package com.daqem.arc.api.player;

import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.arc.api.condition.ICondition;
import com.daqem.arc.player.BlockPosCache;
import com.daqem.arc.player.stat.StatData;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

public interface ArcServerPlayer extends ArcPlayer {

    ServerPlayer arc$getServerPlayer();

    NonNullList<StatData> arc$getStatData();

    void arc$addStatData(StatData statData);

    void arc$setSwimmingDistanceInCm(int swimmingDistanceInCm);

    void arc$setElytraFlyingDistanceInCm(float flyingDistanceInCm);

    int arc$getLastDistanceInCm(ICondition distanceCondition);
    void arc$setLastDistanceInCm(ICondition distanceCondition, int lastDistanceInCm);
    int arc$getLastRemainderInCm(ICondition distanceCondition);
    void arc$setLastRemainderInCm(ICondition distanceCondition, int lastRemainderInCm);

    Map<ResourceLocation, IActionHolder> arc$getActionHoldersMap();
    Map<ICondition, Integer> arc$getLastDistancesInCm();
    Map<ICondition, Integer> arc$getLastRemaindersInCm();
    boolean arc$isSwimming();
    int arc$getSwimmingDistanceInCm();
    boolean arc$isWalking();
    float arc$getWalkingDistance();
    boolean arc$isSprinting();
    float arc$getSprintingDistance();
    boolean arc$isCrouching();
    float arc$getCrouchingDistance();
    boolean arc$isElytraFlying();
    float arc$getElytraFlyingDistance();
    boolean arc$isGrinding();
    boolean arc$isHorseRiding();

    void arc$syncActionHoldersWithClient();

    BlockPosCache arc$getBlockPosCache();
}
