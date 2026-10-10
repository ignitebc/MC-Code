package com.daqem.arc.event.triggers;

import com.daqem.arc.api.action.type.ActionType;
import com.daqem.arc.api.player.ArcServerPlayer;
import com.daqem.arc.api.action.data.ActionDataBuilder;
import com.daqem.arc.api.action.data.type.ActionDataType;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
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
     * 플레이어가 아이템을 사용할 때 호출된다.
     *
     * @param player   - 아이템을 사용한 플레이어
     * @param usedItem - 사용한 아이템
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
