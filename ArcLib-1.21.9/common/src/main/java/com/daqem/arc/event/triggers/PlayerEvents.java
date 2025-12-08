package com.daqem.arc.event.triggers;

import com.daqem.arc.api.IArcAbstractArrow;
import com.daqem.arc.api.action.type.ActionType;
import com.daqem.arc.api.player.ArcPlayer;
import com.daqem.arc.api.player.ArcServerPlayer;
import com.daqem.arc.api.action.data.ActionDataBuilder;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.action.data.type.ActionDataType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;

public class PlayerEvents {

    public static void onPlayerEat(ArcServerPlayer player, ItemStack stack) {
        new ActionDataBuilder(player, ActionType.EAT)
                .withData(ActionDataType.ITEM_STACK, stack)
                .build()
                .sendToAction();
    }

    public static void onPlayerDrink(ArcServerPlayer player, ItemStack stack) {
        new ActionDataBuilder(player, ActionType.DRINK)
                .withData(ActionDataType.ITEM_STACK, stack)
                .build()
                .sendToAction();
    }

    public static void onShootProjectile(ArcServerPlayer player, IArcAbstractArrow shotArrowEntity) {
        new ActionDataBuilder(player, ActionType.SHOOT_PROJECTILE)
                .withData(ActionDataType.ITEM_STACK, shotArrowEntity.arc$getPickupItem())
                .withData(ActionDataType.ENTITY, (AbstractArrow) shotArrowEntity)
                .build()
                .sendToAction();
    }

    public static void onBrewPotion(ArcServerPlayer player, ItemStack stack, BlockPos pos, Level level) {
        new ActionDataBuilder(player, ActionType.BREW_POTION)
                .withData(ActionDataType.ITEM_STACK, stack)
                .withData(ActionDataType.BLOCK_POSITION, pos)
                .withData(ActionDataType.BLOCK_STATE, level.getBlockState(pos))
                .withData(ActionDataType.WORLD, level)
                .build()
                .sendToAction();
    }

    public static ActionResult onEffectAdded(ArcServerPlayer player, MobEffectInstance effect, Entity source) {
        return new ActionDataBuilder(player, ActionType.EFFECT_ADDED)
                .withData(ActionDataType.MOB_EFFECT_INSTANCE, effect)
                .withData(ActionDataType.ENTITY, source)
                .build()
                .sendToAction();

    }

    public static void onSmeltItem(ArcServerPlayer player, Recipe<?> recipe, ItemStack stack, BlockPos furnacePos, Level level) {
        new ActionDataBuilder(player, ActionType.SMELT_ITEM)
                .withData(ActionDataType.ITEM_STACK, stack)
                .withData(ActionDataType.BLOCK_POSITION, furnacePos)
                .withData(ActionDataType.BLOCK_STATE, level.getBlockState(furnacePos))
                .withData(ActionDataType.WORLD, level)
                .withData(ActionDataType.RECIPE, recipe)
                .build()
                .sendToAction();
    }

    public static void onCraftItem(ArcServerPlayer player, Recipe<?> recipe, ItemStack stack, Level level) {
        new ActionDataBuilder(player, ActionType.CRAFT_ITEM)
                .withData(ActionDataType.ITEM_STACK, stack)
                .withData(ActionDataType.WORLD, level)
                .withData(ActionDataType.RECIPE, recipe)
                .build()
                .sendToAction();
    }

    public static void onEnchantItem(ArcServerPlayer player, ItemStack stack, int level) {
        new ActionDataBuilder(player, ActionType.ENCHANT_ITEM)
                .withData(ActionDataType.ITEM_STACK, stack)
                .withData(ActionDataType.EXP_LEVEL, level)
                .build()
                .sendToAction();
    }

    public static void onFishedUpItem(ArcServerPlayer player, ItemStack stack) {
        new ActionDataBuilder(player, ActionType.FISHED_UP_ITEM)
                .withData(ActionDataType.ITEM_STACK, stack)
                .withData(ActionDataType.ITEM, stack.getItem())
                .build()
                .sendToAction();
    }

    public static void onStripLog(ArcServerPlayer player, BlockPos pos, Level level) {
        new ActionDataBuilder(player, ActionType.STRIP_LOG)
                .withData(ActionDataType.BLOCK_STATE, level.getBlockState(pos))
                .withData(ActionDataType.BLOCK_POSITION, pos)
                .withData(ActionDataType.WORLD, level)
                .build()
                .sendToAction();
    }

    public static void onGrindItem(ArcServerPlayer player) {
        new ActionDataBuilder(player, ActionType.GRIND_ITEM)
                .build()
                .sendToAction();
    }

    public static void onUseAnvil(ArcServerPlayer player, ItemStack stack) {
        new ActionDataBuilder(player, ActionType.USE_ANVIL)
                .withData(ActionDataType.ITEM_STACK, stack)
                .withData(ActionDataType.ITEM, stack.getItem())
                .build()
                .sendToAction();
    }

    public static ActionResult onPlayerHurtItem(ArcServerPlayer player, ItemStack itemStack) {
        return new ActionDataBuilder(player, ActionType.HURT_ITEM)
                .withData(ActionDataType.ITEM_STACK, itemStack)
                .withData(ActionDataType.ITEM, itemStack.getItem())
                .build()
                .sendToAction();
    }

    public static void onRodReelIn(ArcPlayer serverPlayer, FishingHook fishingHook) {
        new ActionDataBuilder(serverPlayer, ActionType.ROD_REEL_IN)
                .withData(ActionDataType.ENTITY, fishingHook)
                .withData(ActionDataType.BLOCK_POSITION, fishingHook.blockPosition())
                .withData(ActionDataType.WORLD, fishingHook.level())
                .build()
                .sendToAction();
    }
}
