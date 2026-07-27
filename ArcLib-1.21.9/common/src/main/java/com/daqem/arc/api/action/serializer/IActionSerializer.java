package com.daqem.arc.api.action.serializer;

import com.daqem.arc.api.action.IAction;
import com.daqem.arc.api.action.holder.type.IActionHolderType;
import com.daqem.arc.api.condition.ICondition;
import com.daqem.arc.api.condition.serializer.IConditionSerializer;
import com.daqem.arc.api.reward.IReward;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.data.serializer.ArcSerializer;
import com.daqem.arc.registry.ArcRegistry;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.ArrayList;
import java.util.List;

public interface IActionSerializer<T extends IAction> extends ArcSerializer {

    T fromJson(ResourceLocation location, JsonObject jsonObject, ResourceLocation actionHolderLocation, IActionHolderType<?> actionHolderType, boolean performOnClient, List<IReward> rewards, List<ICondition> conditions);

    T fromNetwork(ResourceLocation location, RegistryFriendlyByteBuf friendlyByteBuf, ResourceLocation actionHolderLocation, IActionHolderType<?> actionHolderType, boolean performOnClient, List<IReward> rewards, List<ICondition> conditions);

    static IAction fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf) {
        ResourceLocation resourceLocation = friendlyByteBuf.readResourceLocation();
        ResourceLocation resourceLocation2 = friendlyByteBuf.readResourceLocation();
        return ArcRegistry.ACTION.getOptional(resourceLocation).orElseThrow(
                () -> new IllegalArgumentException("Unknown action serializer " + resourceLocation)
        ).getSerializer().fromNetwork(resourceLocation2, friendlyByteBuf);
    }

    static <T extends IAction> void toNetwork(T action, RegistryFriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeResourceLocation(ArcRegistry.ACTION.getKey(action.getType()));
        friendlyByteBuf.writeResourceLocation(action.getLocation());
        ((IActionSerializer<T>)action.getSerializer()).toNetwork(friendlyByteBuf, action);

    }

    default T fromJson(ResourceLocation location, JsonObject jsonObject) {
        // 등록되지 않은 타입을 조용히 건너뛰면 조건이 사라진 액션이 모든 대상에서 발동한다.
        // ActionManager 가 JsonParseException 을 잡아 로그를 남기고 해당 액션만 제외한다.
        List<IReward> rewards = new ArrayList<>();
        if (jsonObject.has("rewards")) {
            jsonObject.getAsJsonArray("rewards").forEach(jsonElement -> {
                JsonObject rewardObject = jsonElement.getAsJsonObject();
                ResourceLocation rewardTypeLocation = getResourceLocation(rewardObject, "type");
                rewards.add(ArcRegistry.REWARD.getOptional(rewardTypeLocation)
                        .orElseThrow(() -> new JsonParseException("Unknown reward type: " + rewardTypeLocation))
                        .getSerializer().fromJson(location, rewardObject));
            });
        }

        List<ICondition> conditions = new ArrayList<>();
        if (jsonObject.has("conditions")) {
            jsonObject.getAsJsonArray("conditions").forEach(jsonElement -> {
                JsonObject conditionObject = jsonElement.getAsJsonObject();
                ResourceLocation conditionTypeLocation = getResourceLocation(conditionObject, "type");
                conditions.add(ArcRegistry.CONDITION.getOptional(conditionTypeLocation)
                        .orElseThrow(() -> new JsonParseException("Unknown condition type: " + conditionTypeLocation))
                        .getSerializer().fromJson(location, conditionObject));
            });
        }

        JsonObject holderObject = GsonHelper.getAsJsonObject(jsonObject, "holder");

        return fromJson(location, jsonObject,
                getResourceLocation(holderObject, "id"),
                getHolderType(holderObject, "type"),
                false,
                rewards, conditions);
    }

    default T fromNetwork(ResourceLocation location, RegistryFriendlyByteBuf friendlyByteBuf) {
        return fromNetwork(location, friendlyByteBuf,
                friendlyByteBuf.readResourceLocation(),
                ArcRegistry.ACTION_HOLDER.getOptional(friendlyByteBuf.readResourceLocation()).orElse(null),
                friendlyByteBuf.readBoolean(),
                friendlyByteBuf.readList(object -> IRewardSerializer.fromNetwork((RegistryFriendlyByteBuf) object)),
                friendlyByteBuf.readList(object -> IConditionSerializer.fromNetwork((RegistryFriendlyByteBuf) object)));
    }

    default void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, T type) {
        friendlyByteBuf.writeResourceLocation(type.getActionHolderLocation());
        friendlyByteBuf.writeResourceLocation(type.getActionHolderType().getLocation());
        friendlyByteBuf.writeBoolean(type.shouldPerformOnClient());
        friendlyByteBuf.writeCollection(type.getRewards(),
                (friendlyByteBuf1, reward) -> IRewardSerializer.toNetwork(reward, (RegistryFriendlyByteBuf) friendlyByteBuf1, type.getLocation()));
        friendlyByteBuf.writeCollection(type.getConditions(),
                (friendlyByteBuf1, condition) -> IConditionSerializer.toNetwork(condition, (RegistryFriendlyByteBuf) friendlyByteBuf1, type.getLocation()));
    }
}
