package com.daqem.arc.event.triggers;

import com.daqem.arc.api.action.type.ActionType;
import com.daqem.arc.api.player.ArcServerPlayer;
import com.daqem.arc.api.action.data.ActionDataBuilder;
import com.daqem.arc.api.action.data.type.ActionDataType;

public class MovementEvents {

    public static void onSwim(ArcServerPlayer player, int swimmingDistanceInCm) {
        new ActionDataBuilder(player, ActionType.SWIM)
                .withData(ActionDataType.DISTANCE_IN_CM, swimmingDistanceInCm)
                .build()
                .sendToAction();
    }

    public static void onStartSwimming(ArcServerPlayer player) {
        new ActionDataBuilder(player, ActionType.SWIM_START)
                .build()
                .sendToAction();
    }

    public static void onStopSwimming(ArcServerPlayer player) {
        new ActionDataBuilder(player, ActionType.SWIM_STOP)
                .build()
                .sendToAction();
    }

    public static void onWalk(ArcServerPlayer player, int walkingDistanceInCm) {
        new ActionDataBuilder(player, ActionType.WALK)
                .withData(ActionDataType.DISTANCE_IN_CM, walkingDistanceInCm)
                .build()
                .sendToAction();
    }

    public static void onStopWalking(ArcServerPlayer player) {
        new ActionDataBuilder(player, ActionType.WALK_STOP)
                .build()
                .sendToAction();
    }

    public static void onStartWalking(ArcServerPlayer player) {
        new ActionDataBuilder(player, ActionType.WALK_START)
                .build()
                .sendToAction();
    }

    public static void onSprint(ArcServerPlayer player, int sprintingDistanceInCm) {
        new ActionDataBuilder(player, ActionType.SPRINT)
                .withData(ActionDataType.DISTANCE_IN_CM, sprintingDistanceInCm)
                .build()
                .sendToAction();
    }

    public static void onStopSprinting(ArcServerPlayer player) {
        new ActionDataBuilder(player, ActionType.SPRINT_STOP)
                .build()
                .sendToAction();
    }

    public static void onStartSprinting(ArcServerPlayer player) {
        new ActionDataBuilder(player, ActionType.SPRINT_START)
                .build()
                .sendToAction();
    }

    public static void onCrouch(ArcServerPlayer player, int crouchDistanceInCm) {
        new ActionDataBuilder(player, ActionType.CROUCH)
                .withData(ActionDataType.DISTANCE_IN_CM, crouchDistanceInCm)
                .build()
                .sendToAction();
    }

    public static void onStopCrouching(ArcServerPlayer player) {
        new ActionDataBuilder(player, ActionType.CROUCH_STOP)
                .build()
                .sendToAction();
    }

    public static void onStartCrouching(ArcServerPlayer player) {
        new ActionDataBuilder(player, ActionType.CROUCH_START)
                .build()
                .sendToAction();
    }

    public static void onElytraFly(ArcServerPlayer player, int flyingDistanceInCm) {
        new ActionDataBuilder(player, ActionType.ELYTRA_FLY)
                .withData(ActionDataType.DISTANCE_IN_CM, flyingDistanceInCm)
                .build()
                .sendToAction();
    }

    public static void onStopElytraFlying(ArcServerPlayer player) {
        new ActionDataBuilder(player, ActionType.ELYTRA_FLY_STOP)
                .build()
                .sendToAction();
    }

    public static void onStartElytraFlying(ArcServerPlayer player) {
        new ActionDataBuilder(player, ActionType.ELYTRA_FLY_START)
                .build()
                .sendToAction();
    }

    public static void onHorseRide(ArcServerPlayer player, int ridingDistanceInCm) {
        new ActionDataBuilder(player, ActionType.HORSE_RIDE)
                .withData(ActionDataType.DISTANCE_IN_CM, ridingDistanceInCm)
                .build()
                .sendToAction();
    }

    public static void onStopHorseRiding(ArcServerPlayer player) {
        new ActionDataBuilder(player, ActionType.HORSE_RIDE_STOP)
                .build()
                .sendToAction();
    }

    public static void onStartHorseRiding(ArcServerPlayer player) {
        new ActionDataBuilder(player, ActionType.HORSE_RIDE_START)
                .build()
                .sendToAction();
    }
}
