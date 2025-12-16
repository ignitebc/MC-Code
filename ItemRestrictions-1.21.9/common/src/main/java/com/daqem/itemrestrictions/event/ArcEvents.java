package com.daqem.itemrestrictions.event;

import com.daqem.arc.api.action.data.ActionDataBuilder;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.action.type.ActionType;
import com.daqem.arc.api.action.type.IActionType;
import com.daqem.arc.event.events.ActionEvent;
import com.daqem.itemrestrictions.ItemRestrictions;
import com.daqem.itemrestrictions.data.RestrictionResult;
import com.daqem.itemrestrictions.data.RestrictionType;
import com.daqem.itemrestrictions.level.player.ItemRestrictionsServerPlayer;
import dev.architectury.event.EventResult;
import net.minecraft.ChatFormatting;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class ArcEvents {

    public static void registerEvents() {
        ActionEvent.BEFORE_ACTION.register(actionData -> {
            IActionType<?> actionType = actionData.getActionType();

            if (actionData.getPlayer() instanceof ServerPlayer serverPlayer) {
                if (actionData.getPlayer() instanceof ItemRestrictionsServerPlayer itemRestrictionsPlayer) {
                    if (actionType == ActionType.BREAK_BLOCK) {
                        BlockState blockState = actionData.getData(ActionDataType.BLOCK_STATE);
                        if (blockState != null) {
                            RestrictionResult result = itemRestrictionsPlayer.itemrestrictions$isRestricted(
                                    new ActionDataBuilder(actionData.getPlayer(), null)
                                            .withData(ActionDataType.ITEM_STACK, blockState.getBlock().asItem().getDefaultInstance())
                                            .build());

                            if (result.isRestricted(RestrictionType.BREAK_BLOCK)) {
                                serverPlayer.sendSystemMessage(ItemRestrictions.translatable(RestrictionType.BREAK_BLOCK.getTranslationKey()).withStyle(ChatFormatting.RED), true);
                                return EventResult.interruptFalse();
                            }
                        }


                        ItemStack usedItemStack = serverPlayer.getMainHandItem();
                        RestrictionResult result = itemRestrictionsPlayer.itemrestrictions$isRestricted(
                                new ActionDataBuilder(actionData.getPlayer(), null)
                                        .withData(ActionDataType.ITEM_STACK, usedItemStack)
                                        .build());

                        if (result.isRestricted(RestrictionType.ITEM_BREAK_BLOCK)) {
                            serverPlayer.sendSystemMessage(ItemRestrictions.translatable(RestrictionType.ITEM_BREAK_BLOCK.getTranslationKey()).withStyle(ChatFormatting.RED), true);
                            return EventResult.interruptFalse();
                        }
                    } else if (actionType == ActionType.PLACE_BLOCK) {
                        BlockState blockState = actionData.getData(ActionDataType.BLOCK_STATE);
                        if (blockState != null) {
                            RestrictionResult result = itemRestrictionsPlayer.itemrestrictions$isRestricted(
                                    new ActionDataBuilder(actionData.getPlayer(), null)
                                            .withData(ActionDataType.ITEM_STACK, blockState.getBlock().asItem().getDefaultInstance())
                                            .build());

                            if (result.isRestricted(RestrictionType.PLACE_BLOCK)) {
                                serverPlayer.sendSystemMessage(ItemRestrictions.translatable(RestrictionType.PLACE_BLOCK.getTranslationKey()).withStyle(ChatFormatting.RED), true);
                                return EventResult.interruptFalse();
                            }
                        }
                    } else if (actionType == ActionType.HURT_ENTITY) {
                        ItemStack usedItemStack = serverPlayer.getMainHandItem();
                        RestrictionResult result = itemRestrictionsPlayer.itemrestrictions$isRestricted(
                                new ActionDataBuilder(actionData.getPlayer(), null)
                                        .withData(ActionDataType.ITEM_STACK, usedItemStack)
                                        .build());

                        if (result.isRestricted(RestrictionType.HURT_ENTITY)) {
                            serverPlayer.sendSystemMessage(ItemRestrictions.translatable(RestrictionType.HURT_ENTITY.getTranslationKey()).withStyle(ChatFormatting.RED), true);
                            return EventResult.interruptFalse();
                        }
                    }
                }
            }
            return EventResult.pass();
        });
    }
}
