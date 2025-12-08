package com.daqem.arc.api.action.data.type;

import com.daqem.arc.Arc;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface ActionDataType<T> extends IActionDataType<T> {

    IActionDataType<BlockState> BLOCK_STATE = register(Arc.getId("block_state"));
    IActionDataType<BlockPos> BLOCK_POSITION = register(Arc.getId("block_position"));
    IActionDataType<Integer> EXP_DROP = register(Arc.getId("exp_drop"));
    IActionDataType<Integer> EXP_LEVEL = register(Arc.getId("exp_level"));
    IActionDataType<Level> WORLD = register(Arc.getId("world"));
    IActionDataType<DamageSource> DAMAGE_SOURCE = register(Arc.getId("damage_source"));
    IActionDataType<Entity> ENTITY = register(Arc.getId("entity"));
    IActionDataType<Float> DAMAGE_AMOUNT = register(Arc.getId("damage_amount"));
    IActionDataType<Integer> DISTANCE_IN_CM = register(Arc.getId("distance_in_cm"));
    IActionDataType<ItemStack> ITEM_STACK = register(Arc.getId("item_stack"));
    IActionDataType<Item> ITEM = register(Arc.getId("item"));
    IActionDataType<Advancement> ADVANCEMENT = register(Arc.getId("advancement"));
    IActionDataType<MobEffectInstance> MOB_EFFECT_INSTANCE = register(Arc.getId("mob_effect_instance"));
    IActionDataType<Recipe<?>> RECIPE = register(Arc.getId("recipe"));

    static <T> IActionDataType<T> register(ResourceLocation location) {
        return () -> location;
    }
}
