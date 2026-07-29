package com.daqem.arc.registry;

import com.daqem.arc.Arc;
import com.daqem.arc.api.action.holder.type.ActionHolderType;
import com.daqem.arc.api.action.holder.type.IActionHolderType;
import com.daqem.arc.api.action.serializer.IActionSerializer;
import com.daqem.arc.api.action.type.ActionType;
import com.daqem.arc.api.action.type.IActionType;
import com.daqem.arc.api.condition.serializer.IConditionSerializer;
import com.daqem.arc.api.condition.type.ConditionType;
import com.daqem.arc.api.condition.type.IConditionType;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.api.reward.type.IRewardType;
import com.daqem.arc.api.reward.type.RewardType;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;

public class ArcRegistry {

    public static final ResourceKey<Registry<IActionType<?>>> ACTION_KEY = ResourceKey.createRegistryKey(Arc.getId("action"));
    public static final ResourceKey<Registry<IRewardType<?>>> REWARD_KEY = ResourceKey.createRegistryKey(Arc.getId("reward"));
    public static final ResourceKey<Registry<IConditionType<?>>> CONDITION_KEY = ResourceKey.createRegistryKey(Arc.getId("condition"));
    public static final ResourceKey<Registry<IActionHolderType<?>>> ACTION_HOLDER_KEY = ResourceKey.createRegistryKey(Arc.getId("action_holder"));

    public static final ResourceKey<Registry<IActionSerializer<?>>> ACTION_SERIALIZER_KEY = ResourceKey.createRegistryKey(Arc.getId("action_serializer"));
    public static final ResourceKey<Registry<IRewardSerializer<?>>> REWARD_SERIALIZER_KEY = ResourceKey.createRegistryKey(Arc.getId("reward_serializer"));
    public static final ResourceKey<Registry<IConditionSerializer<?>>> CONDITION_SERIALIZER_KEY = ResourceKey.createRegistryKey(Arc.getId("condition_serializer"));


    public static final Registry<IActionType<?>> ACTION = new MappedRegistry<>(ArcRegistry.ACTION_KEY, Lifecycle.experimental(), false);
    public static final Registry<IRewardType<?>> REWARD = new MappedRegistry<>(ArcRegistry.REWARD_KEY, Lifecycle.experimental(), false);
    public static final Registry<IConditionType<?>> CONDITION = new MappedRegistry<>(ArcRegistry.CONDITION_KEY, Lifecycle.experimental(), false);
    public static final Registry<IActionHolderType<?>> ACTION_HOLDER = new MappedRegistry<>(ArcRegistry.ACTION_HOLDER_KEY, Lifecycle.experimental(), false);

    public static void init() {
        ActionType.init();
        RewardType.init();
        ConditionType.init();
        ActionHolderType.init();
    }
}
