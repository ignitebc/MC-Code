package com.daqem.arc.api.condition.serializer;

import com.daqem.arc.api.condition.ICondition;
import com.daqem.arc.data.serializer.ArcSerializer;
import com.daqem.arc.registry.ArcRegistry;
import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;

public interface IConditionSerializer<T extends ICondition> extends ArcSerializer {

    T fromJson(Identifier location, JsonObject jsonObject, boolean inverted);

    T fromNetwork(Identifier location, RegistryFriendlyByteBuf friendlyByteBuf, boolean inverted);

    static ICondition fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf) {
        Identifier resourceLocation = friendlyByteBuf.readResourceLocation();
        Identifier resourceLocation2 = friendlyByteBuf.readResourceLocation();
        return ArcRegistry.CONDITION.getOptional(resourceLocation).orElseThrow(
                () -> new IllegalArgumentException("Unknown condition serializer " + resourceLocation)
        ).getSerializer().fromNetwork(resourceLocation2, friendlyByteBuf);
    }

    static <T extends ICondition> void toNetwork(T condition, RegistryFriendlyByteBuf friendlyByteBuf, Identifier location) {
        friendlyByteBuf.writeResourceLocation(ArcRegistry.CONDITION.getKey(condition.getType()));
        friendlyByteBuf.writeResourceLocation(location);
        ((IConditionSerializer<T>)condition.getSerializer()).toNetwork(friendlyByteBuf, condition);

    }

    default T fromJson(Identifier location, JsonObject jsonObject) {
        return fromJson(location, jsonObject, GsonHelper.getAsBoolean(jsonObject, "inverted", false));
    }

    default T fromNetwork(Identifier location, RegistryFriendlyByteBuf friendlyByteBuf) {
        return fromNetwork(location, friendlyByteBuf, friendlyByteBuf.readBoolean());
    }

    default void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, T type) {
        friendlyByteBuf.writeBoolean(type.isInverted());
    }
}
