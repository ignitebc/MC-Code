package com.daqem.arc.event.triggers;

import com.daqem.arc.Arc;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.action.type.ActionType;
import com.daqem.arc.api.player.ArcServerPlayer;
import com.daqem.arc.api.action.data.ActionDataBuilder;
import com.daqem.arc.api.action.data.type.ActionDataType;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.InteractionEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.Animal;

import javax.sound.midi.MidiFileFormat;

public class EntityEvents {

    public static void registerEvents() {
        EntityEvent.LIVING_DEATH.register((entity, source) -> {
            if (entity instanceof ArcServerPlayer arcServerPlayer) {
                new ActionDataBuilder(arcServerPlayer, ActionType.DEATH)
                        .withData(ActionDataType.DAMAGE_SOURCE, source)
                        .build()
                        .sendToAction();
            } else if (source.getEntity() instanceof ArcServerPlayer arcServerPlayer) {
                new ActionDataBuilder(arcServerPlayer, ActionType.KILL_ENTITY)
                        .withData(ActionDataType.ENTITY, entity)
                        .withData(ActionDataType.BLOCK_POSITION, entity.blockPosition())
                        .withData(ActionDataType.WORLD, entity.level())
                        .withData(ActionDataType.EXP_DROP, entity.getExperienceReward((ServerLevel) entity.level(), entity))
                        .build()
                        .sendToAction();
            }
            return EventResult.pass();
        });

        EntityEvent.ANIMAL_TAME.register((animal, player) -> {
            if (player instanceof ArcServerPlayer arcServerPlayer) {
                ActionResult actionResult = new ActionDataBuilder(arcServerPlayer, ActionType.TAME_ANIMAL)
                        .withData(ActionDataType.ENTITY, animal)
                        .build()
                        .sendToAction();

                if (actionResult.shouldCancelAction()) {
                    return EventResult.interruptFalse();
                }
            }
            return EventResult.pass();
        });

        InteractionEvent.INTERACT_ENTITY.register((player, entity, hand) -> {
            if (player instanceof ArcServerPlayer arcServerPlayer) {
                ActionResult actionResult = new ActionDataBuilder(arcServerPlayer, ActionType.INTERACT_ENTITY)
                        .withData(ActionDataType.ITEM_STACK, player.getItemInHand(hand))
                        .withData(ActionDataType.ITEM, player.getItemInHand(hand).getItem())
                        .withData(ActionDataType.ENTITY, entity)
                        .build()
                        .sendToAction();

                if (actionResult.shouldCancelAction()) {
                    return EventResult.interruptFalse();
                }
            }
            return EventResult.pass();
        });

        EntityEvent.LIVING_HURT.register((entity, source, amount) -> {
            if (source.getEntity() instanceof ArcServerPlayer arcServerPlayer) {
                ActionResult actionResult = new ActionDataBuilder(arcServerPlayer, ActionType.HURT_ENTITY)
                        .withData(ActionDataType.ENTITY, entity)
                        .withData(ActionDataType.DAMAGE_SOURCE, source)
                        .withData(ActionDataType.DAMAGE_AMOUNT, amount)
                        .build()
                        .sendToAction();

                if (actionResult.shouldCancelAction()) {
                    return EventResult.interruptFalse();
                }
            }
            return EventResult.pass();
        });
    }

    public static ActionResult onBreedAnimal(ArcServerPlayer player, Animal animal) {
        return new ActionDataBuilder(player, ActionType.BREED_ANIMAL)
                .withData(ActionDataType.ENTITY, animal)
                .build()
                .sendToAction();
    }
}
