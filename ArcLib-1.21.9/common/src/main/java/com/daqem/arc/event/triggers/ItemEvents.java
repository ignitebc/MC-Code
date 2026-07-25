package com.daqem.arc.event.triggers;

import com.daqem.arc.api.action.type.ActionType;
import com.daqem.arc.api.player.ArcServerPlayer;
import com.daqem.arc.api.action.data.ActionDataBuilder;
import com.daqem.arc.api.action.data.type.ActionDataType;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;

public class ItemEvents {

    public static void registerEvents() {
        PlayerEvent.DROP_ITEM.register((player, itemStack) -> {
            if (player instanceof ArcServerPlayer arcServerPlayer) {
                new ActionDataBuilder(arcServerPlayer, ActionType.DROP_ITEM)
                        .withData(ActionDataType.ITEM, itemStack.getItem().getItem())
                        .withData(ActionDataType.ITEM_STACK, itemStack.getItem())
                        .build()
                        .sendToAction();
            }
            return EventResult.pass();
        });
    }

    /**
     * Called when a player uses an item.
     *
     * @param player   - The player that used the item.
     * @param usedItem - The item that was used.
     */
    public static void onUseItem(ArcServerPlayer player, Item usedItem) {
        new ActionDataBuilder(player, ActionType.USE_ITEM)
                .withData(ActionDataType.ITEM, usedItem)
                .build()
                .sendToAction();
    }

    public static void onThrowItem(ArcServerPlayer player, ThrowableItemProjectile thrownItemEntity) {
        new ActionDataBuilder(player, ActionType.THROW_ITEM)
                .withData(ActionDataType.ITEM_STACK, thrownItemEntity.getItem())
                .withData(ActionDataType.ENTITY, thrownItemEntity)
                .build()
                .sendToAction();
    }
}
