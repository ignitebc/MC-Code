package com.daqem.arc.api.condition.type;

import com.daqem.arc.Arc;
import com.daqem.arc.api.condition.ICondition;
import com.daqem.arc.api.condition.serializer.IConditionSerializer;
import com.daqem.arc.data.condition.NotCondition;
import com.daqem.arc.data.condition.OrCondition;
import com.daqem.arc.data.condition.block.BlockCondition;
import com.daqem.arc.data.condition.block.BlocksCondition;
import com.daqem.arc.data.condition.block.NotInBlockPosCacheCondition;
import com.daqem.arc.data.condition.block.crop.CropAgeCondition;
import com.daqem.arc.data.condition.block.crop.CropFullyGrownCondition;
import com.daqem.arc.data.condition.block.ore.IsOreCondition;
import com.daqem.arc.data.condition.block.properties.BlockHardnessCondition;
import com.daqem.arc.data.condition.effect.EffectCategoryCondition;
import com.daqem.arc.data.condition.effect.EffectCondition;
import com.daqem.arc.data.condition.entity.*;
import com.daqem.arc.data.condition.experience.ExpDropCondition;
import com.daqem.arc.data.condition.experience.ExpLevelCondition;
import com.daqem.arc.data.condition.item.*;
import com.daqem.arc.data.condition.movement.DistanceCondition;
import com.daqem.arc.data.condition.recipe.IsBlastingRecipeCondition;
import com.daqem.arc.data.condition.recipe.IsSmokingRecipeCondition;
import com.daqem.arc.data.condition.scoreboard.ScoreboardCondition;
import com.daqem.arc.data.condition.team.TeamCondition;
import com.daqem.arc.data.condition.world.DimensionCondition;
import com.daqem.arc.event.events.RegistryEvent;
import com.daqem.arc.registry.ArcRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public interface ConditionType<T extends ICondition> extends IConditionType<T> {

    ConditionType<OrCondition> OR = register(Arc.getId("or"), new OrCondition.Serializer());
    ConditionType<NotCondition> NOT = register(Arc.getId("not"), new NotCondition.Serializer());

    ConditionType<CropFullyGrownCondition> CROP_FULLY_GROWN = register(Arc.getId("crop_fully_grown"), new CropFullyGrownCondition.Serializer());
    ConditionType<CropAgeCondition> CROP_AGE = register(Arc.getId("crop_age"), new CropAgeCondition.Serializer());
    ConditionType<BlockCondition> BLOCK = register(Arc.getId("block"), new BlockCondition.Serializer());
    ConditionType<BlocksCondition> BLOCKS = register(Arc.getId("blocks"), new BlocksCondition.Serializer());
    ConditionType<DistanceCondition> DISTANCE = register(Arc.getId("distance"), new DistanceCondition.Serializer());
    ConditionType<EntityTypeCondition> ENTITY_TYPE = register(Arc.getId("entity_type"), new EntityTypeCondition.Serializer());
    ConditionType<EntityTypesCondition> ENTITY_TYPES = register(Arc.getId("entity_types"), new EntityTypesCondition.Serializer());
    ConditionType<DimensionCondition> DIMENSION = register(Arc.getId("dimension"), new DimensionCondition.Serializer());
    ConditionType<ScoreboardCondition> SCOREBOARD = register(Arc.getId("scoreboard"), new ScoreboardCondition.Serializer());
    ConditionType<TeamCondition> TEAM = register(Arc.getId("team"), new TeamCondition.Serializer());
    ConditionType<ItemInHandCondition> ITEM_IN_HAND = register(Arc.getId("item_in_hand"), new ItemInHandCondition.Serializer());
    ConditionType<ItemInInventoryCondition> ITEM_IN_INVENTORY = register(Arc.getId("item_in_inventory"), new ItemInInventoryCondition.Serializer());
    ConditionType<ItemEquippedCondition> ITEM_EQUIPPED = register(Arc.getId("item_equipped"), new ItemEquippedCondition.Serializer());
    ConditionType<ItemCondition> ITEM = register(Arc.getId("item"), new ItemCondition.Serializer());
    ConditionType<ItemsCondition> ITEMS = register(Arc.getId("items"), new ItemsCondition.Serializer());
    ConditionType<ExpDropCondition> EXP_DROP = register(Arc.getId("exp_drop"), new ExpDropCondition.Serializer());
    ConditionType<ExpLevelCondition> EXP_LEVEL = register(Arc.getId("exp_level"), new ExpLevelCondition.Serializer());
    ConditionType<ReadyForShearingCondition> READY_FOR_SHEARING = register(Arc.getId("ready_for_shearing"), new ReadyForShearingCondition.Serializer());
    ConditionType<IsBlastingRecipeCondition> IS_BLASTING_RECIPE = register(Arc.getId("is_blasting_recipe"), new IsBlastingRecipeCondition.Serializer());
    ConditionType<IsSmokingRecipeCondition> IS_SMOKING_RECIPE = register(Arc.getId("is_smoking_recipe"), new IsSmokingRecipeCondition.Serializer());
    ConditionType<IsOreCondition> IS_ORE = register(Arc.getId("is_ore"), new IsOreCondition.Serializer());
    ConditionType<EffectCategoryCondition> EFFECT_CATEGORY = register(Arc.getId("effect_category"), new EffectCategoryCondition.Serializer());
    ConditionType<EffectCondition> EFFECT = register(Arc.getId("effect"), new EffectCondition.Serializer());
    ConditionType<BlockHardnessCondition> BLOCK_HARDNESS = register(Arc.getId("block_hardness"), new BlockHardnessCondition.Serializer());
    IConditionType<EntityInBlockCondition> ENTITY_IN_BLOCK = register(Arc.getId("entity_in_block"), new EntityInBlockCondition.Serializer());
    IConditionType<DamageSourceCondition> DAMAGE_SOURCE = register(Arc.getId("damage_source"), new DamageSourceCondition.Serializer());
    IConditionType<NotInBlockPosCacheCondition> NOT_IN_BLOCK_POS_CACHE = register(Arc.getId("not_in_block_pos_cache"), new NotInBlockPosCacheCondition.Serializer());

    static <T extends ICondition> ConditionType<T> register(final ResourceLocation location, final IConditionSerializer<T> serializer) {
        return Registry.register(ArcRegistry.CONDITION, location, new ConditionType<T>(){

            @Override
            public ResourceLocation getLocation() {
                return location;
            }

            @Override
            public IConditionSerializer<T> getSerializer() {
                return serializer;
            }

            @Override
            public String toString() {
                return location.toString();
            }
        });
    }

    static void init() {
        RegistryEvent.REGISTER_CONDITION_TYPE.invoker().registerConditionType();
    }
}
