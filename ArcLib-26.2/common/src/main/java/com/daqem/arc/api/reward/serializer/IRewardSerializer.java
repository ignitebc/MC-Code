package com.daqem.arc.api.reward.serializer;

import com.daqem.arc.api.reward.IReward;
import com.daqem.arc.data.serializer.ArcSerializer;
import com.daqem.arc.registry.ArcRegistry;
import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;

public interface IRewardSerializer<T extends IReward> extends ArcSerializer {

    T fromJson(JsonObject jsonObject, double chance, int priority);

    T fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, double chance, int priority);

    static IReward fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf) {
        Identifier resourceLocation = friendlyByteBuf.readResourceLocation();
        Identifier resourceLocation2 = friendlyByteBuf.readResourceLocation();
        return ArcRegistry.REWARD.getOptional(resourceLocation).orElseThrow(
                () -> new IllegalArgumentException("Unknown reward serializer " + resourceLocation)
        ).getSerializer().fromNetwork(resourceLocation2, friendlyByteBuf);
    }

    static <T extends IReward> void toNetwork(T reward, RegistryFriendlyByteBuf friendlyByteBuf, Identifier location) {
        friendlyByteBuf.writeResourceLocation(ArcRegistry.REWARD.getKey(reward.getType()));
        friendlyByteBuf.writeResourceLocation(location);
        ((IRewardSerializer<T>)reward.getSerializer()).toNetwork(friendlyByteBuf, reward);

    }

    default T fromJson(Identifier location, JsonObject jsonObject) {
        return fromJson(jsonObject, GsonHelper.getAsDouble(jsonObject, "chance", 100D), GsonHelper.getAsInt(jsonObject, "priority", 1));
    }

    default T fromNetwork(Identifier location, RegistryFriendlyByteBuf friendlyByteBuf) {
        return fromNetwork(friendlyByteBuf, friendlyByteBuf.readDouble(), friendlyByteBuf.readInt());
    }

    default void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, T type) {
        friendlyByteBuf.writeDouble(type.getChance());
        friendlyByteBuf.writeInt(type.getPriority());
    }
}
