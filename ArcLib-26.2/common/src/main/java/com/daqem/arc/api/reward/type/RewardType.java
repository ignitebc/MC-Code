package com.daqem.arc.api.reward.type;

import com.daqem.arc.Arc;
import com.daqem.arc.api.reward.IReward;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.data.reward.CancelActionReward;
import com.daqem.arc.data.reward.block.BlockDropMultiplierReward;
import com.daqem.arc.data.reward.block.DestroySpeedMultiplierReward;
import com.daqem.arc.data.reward.effect.EffectAmplifierAdditionReward;
import com.daqem.arc.data.reward.effect.EffectDurationMultiplierReward;
import com.daqem.arc.data.reward.effect.EffectReward;
import com.daqem.arc.data.reward.effect.RemoveEffectReward;
import com.daqem.arc.data.reward.entity.DamageMultiplierReward;
import com.daqem.arc.data.reward.entity.EntityOnFireReward;
import com.daqem.arc.data.reward.entity.EntityPoisonReward;
import com.daqem.arc.data.reward.entity.MultipleArrowsReward;
import com.daqem.arc.data.reward.experience.ExpMultiplierReward;
import com.daqem.arc.data.reward.experience.ExpReward;
import com.daqem.arc.data.reward.item.CaughtFishMultiplierReward;
import com.daqem.arc.data.reward.item.ItemReward;
import com.daqem.arc.data.reward.player.AttackSpeedMultiplierReward;
import com.daqem.arc.data.reward.player.BlockInteractionRangeAttributeModifierReward;
import com.daqem.arc.data.reward.player.FishingWaitTimeMultiplierReward;
import com.daqem.arc.data.reward.player.EntityInteractionRangeAttributeModifierReward;
import com.daqem.arc.data.reward.player.MaxHealthAttributeModifierReward;
import com.daqem.arc.data.reward.player.MoveToEntityReward;
import com.daqem.arc.data.reward.player.MovementSpeedAttributeModifierReward;
import com.daqem.arc.data.reward.player.OxygenBonusAttributeModifierReward;
import com.daqem.arc.data.reward.player.SafeFallDistanceAttributeModifierReward;
import com.daqem.arc.data.reward.player.SwimSpeedMultiplierReward;
import com.daqem.arc.data.reward.player.WaterMovementEfficiencyAttributeModifierReward;
import com.daqem.arc.data.reward.server.CommandReward;
import com.daqem.arc.data.reward.world.DropItemReward;
import com.daqem.arc.event.events.RegistryEvent;
import com.daqem.arc.registry.ArcRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;

public interface RewardType<T extends IReward> extends IRewardType<T> {

    IRewardType<ExpReward> EXP = register(Arc.getId("exp"), new ExpReward.Serializer());
    IRewardType<ItemReward> ITEM = register(Arc.getId("item"), new ItemReward.Serializer());
    IRewardType<EffectReward> EFFECT = register(Arc.getId("effect"), new EffectReward.Serializer());
    IRewardType<EffectDurationMultiplierReward> EFFECT_DURATION_MULTIPLIER = register(Arc.getId("effect_duration_multiplier"), new EffectDurationMultiplierReward.Serializer());
    IRewardType<EffectAmplifierAdditionReward> EFFECT_AMPLIFIER_ADDITION = register(Arc.getId("effect_amplifier_addition"), new EffectAmplifierAdditionReward.Serializer());
    IRewardType<RemoveEffectReward> REMOVE_EFFECT = register(Arc.getId("remove_effect"), new RemoveEffectReward.Serializer());
    IRewardType<CancelActionReward> CANCEL_ACTION = register(Arc.getId("cancel_action"), new CancelActionReward.Serializer());
    IRewardType<DestroySpeedMultiplierReward> DESTROY_SPEED_MULTIPLIER = register(Arc.getId("destroy_speed_multiplier"), new DestroySpeedMultiplierReward.Serializer());
    IRewardType<AttackSpeedMultiplierReward> ATTACK_SPEED_MULTIPLIER = register(Arc.getId("attack_speed_multiplier"), new AttackSpeedMultiplierReward.Serializer());
    IRewardType<DropItemReward> DROP_ITEM = register(Arc.getId("drop_item"), new DropItemReward.Serializer());
    IRewardType<MultipleArrowsReward> MULTIPLE_ARROWS = register(Arc.getId("multiple_arrows"), new MultipleArrowsReward.Serializer());
    IRewardType<EntityOnFireReward> ENTITY_ON_FIRE = register(Arc.getId("entity_on_fire"), new EntityOnFireReward.Serializer());
    IRewardType<EntityPoisonReward> ENTITY_POISON = register(Arc.getId("entity_poison"), new EntityPoisonReward.Serializer());
    IRewardType<BlockDropMultiplierReward> BLOCK_DROP_MULTIPLIER = register(Arc.getId("block_drop_multiplier"), new BlockDropMultiplierReward.Serializer());
    IRewardType<MoveToEntityReward> MOVE_TO_ENTITY = register(Arc.getId("move_to_entity"), new MoveToEntityReward.Serializer());
    IRewardType<ExpMultiplierReward> EXP_MULTIPLIER = register(Arc.getId("exp_multiplier"), new ExpMultiplierReward.Serializer());
    IRewardType<DamageMultiplierReward> DAMAGE_MULTIPLIER = register(Arc.getId("damage_multiplier"), new DamageMultiplierReward.Serializer());
    IRewardType<CommandReward> COMMAND = register(Arc.getId("command"), new CommandReward.Serializer());

    // AttributeModifier 기반 실제 이동속도 증가 (유지)
    IRewardType<MovementSpeedAttributeModifierReward> MOVEMENT_SPEED_ATTRIBUTE_MODIFIER =
            register(Arc.getId("movement_speed_modifier"), new MovementSpeedAttributeModifierReward.Serializer());
    IRewardType<BlockInteractionRangeAttributeModifierReward> BLOCK_INTERACTION_RANGE_ATTRIBUTE_MODIFIER =
            register(Arc.getId("block_interaction_range_modifier"), new BlockInteractionRangeAttributeModifierReward.Serializer());
    IRewardType<EntityInteractionRangeAttributeModifierReward> ENTITY_INTERACTION_RANGE_ATTRIBUTE_MODIFIER =
            register(Arc.getId("entity_interaction_range_modifier"), new EntityInteractionRangeAttributeModifierReward.Serializer());
    IRewardType<MaxHealthAttributeModifierReward> MAX_HEALTH_ATTRIBUTE_MODIFIER =
            register(Arc.getId("max_health_modifier"), new MaxHealthAttributeModifierReward.Serializer());
    IRewardType<SafeFallDistanceAttributeModifierReward> SAFE_FALL_DISTANCE_ATTRIBUTE_MODIFIER =
            register(Arc.getId("safe_fall_distance_modifier"), new SafeFallDistanceAttributeModifierReward.Serializer());
    IRewardType<OxygenBonusAttributeModifierReward> OXYGEN_BONUS_ATTRIBUTE_MODIFIER =
            register(Arc.getId("oxygen_bonus_modifier"), new OxygenBonusAttributeModifierReward.Serializer());
    IRewardType<WaterMovementEfficiencyAttributeModifierReward> WATER_MOVEMENT_EFFICIENCY_ATTRIBUTE_MODIFIER =
            register(Arc.getId("water_movement_efficiency_modifier"), new WaterMovementEfficiencyAttributeModifierReward.Serializer());
    IRewardType<SwimSpeedMultiplierReward> SWIM_SPEED_MULTIPLIER =
            register(Arc.getId("swim_speed_multiplier"), new SwimSpeedMultiplierReward.Serializer());
    IRewardType<FishingWaitTimeMultiplierReward> FISHING_WAIT_TIME_MULTIPLIER =
            register(Arc.getId("fishing_wait_time_multiplier"), new FishingWaitTimeMultiplierReward.Serializer());
    IRewardType<CaughtFishMultiplierReward> CAUGHT_FISH_MULTIPLIER =
            register(Arc.getId("caught_fish_multiplier"), new CaughtFishMultiplierReward.Serializer());

    static <T extends IReward> IRewardType<T> register(final Identifier location, final IRewardSerializer<T> serializer) {
        return Registry.register(ArcRegistry.REWARD, location, new RewardType<T>() {

            @Override
            public Identifier getLocation() {
                return location;
            }

            @Override
            public IRewardSerializer<T> getSerializer() {
                return serializer;
            }

            @Override
            public String toString() {
                return location.toString();
            }
        });
    }

    static void init() {
        RegistryEvent.REGISTER_REWARD_TYPE.invoker().registerRewardType();
    }
}
