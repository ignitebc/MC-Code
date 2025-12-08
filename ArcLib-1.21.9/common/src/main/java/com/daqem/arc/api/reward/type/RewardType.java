package com.daqem.arc.api.reward.type;

import com.daqem.arc.Arc;
import com.daqem.arc.api.action.IAction;
import com.daqem.arc.api.action.type.ActionType;
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
import com.daqem.arc.data.reward.entity.MultipleArrowsReward;
import com.daqem.arc.data.reward.experience.ExpMultiplierReward;
import com.daqem.arc.data.reward.experience.ExpReward;
import com.daqem.arc.data.reward.item.ItemReward;
import com.daqem.arc.data.reward.player.AttackSpeedMultiplierReward;
import com.daqem.arc.data.reward.player.MoveToEntityReward;
import com.daqem.arc.data.reward.server.CommandReward;
import com.daqem.arc.data.reward.world.DropItemReward;
import com.daqem.arc.event.events.RegistryEvent;
import com.daqem.arc.registry.ArcRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

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
    IRewardType<BlockDropMultiplierReward> BLOCK_DROP_MULTIPLIER = register(Arc.getId("block_drop_multiplier"), new BlockDropMultiplierReward.Serializer());
    IRewardType<MoveToEntityReward> MOVE_TO_ENTITY = register(Arc.getId("move_to_entity"), new MoveToEntityReward.Serializer());
    IRewardType<ExpMultiplierReward> EXP_MULTIPLIER = register(Arc.getId("exp_multiplier"), new ExpMultiplierReward.Serializer());
    IRewardType<DamageMultiplierReward> DAMAGE_MULTIPLIER = register(Arc.getId("damage_multiplier"), new DamageMultiplierReward.Serializer());
    IRewardType<CommandReward> COMMAND = register(Arc.getId("command"), new CommandReward.Serializer());

    static <T extends IReward> IRewardType<T> register(final ResourceLocation location, final IRewardSerializer<T> serializer) {
        return Registry.register(ArcRegistry.REWARD, location, new RewardType<T>(){

            @Override
            public ResourceLocation getLocation() {
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
