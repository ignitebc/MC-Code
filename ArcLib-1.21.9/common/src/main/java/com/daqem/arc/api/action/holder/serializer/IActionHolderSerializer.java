package com.daqem.arc.api.action.holder.serializer;

import com.daqem.arc.api.action.IAction;
import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.arc.api.action.serializer.IActionSerializer;
import com.daqem.arc.data.serializer.ArcSerializer;
import com.daqem.arc.registry.ArcRegistry;
import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public interface IActionHolderSerializer<T extends IActionHolder> extends ArcSerializer {

    T fromJson(JsonObject jsonObject, ResourceLocation location);

    T fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, ResourceLocation location);

    static IActionHolder fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf) {
        ResourceLocation resourceLocation = friendlyByteBuf.readResourceLocation();
        ResourceLocation resourceLocation2 = friendlyByteBuf.readResourceLocation();
        return ArcRegistry.ACTION_HOLDER.getOptional(resourceLocation).orElseThrow(
                () -> new IllegalArgumentException("Unknown action holder serializer " + resourceLocation)
        ).getSerializer().fromNetwork(resourceLocation2, friendlyByteBuf);
    }

    static <T extends IActionHolder> void toNetwork(T actionHolder, RegistryFriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeResourceLocation(ArcRegistry.ACTION_HOLDER.getKey(actionHolder.getType()));
        friendlyByteBuf.writeResourceLocation(actionHolder.getLocation());
        ((IActionHolderSerializer<T>)actionHolder.getSerializer()).toNetwork(friendlyByteBuf, actionHolder);

    }

    default T fromJson(ResourceLocation location, JsonObject jsonObject) {
        return fromJson(jsonObject, location);
    }

    default T fromNetwork(ResourceLocation location, RegistryFriendlyByteBuf friendlyByteBuf) {
        var actionHolder = fromNetwork(friendlyByteBuf, location);
        List<IAction> actions = friendlyByteBuf.readList(friendlyByteBuf1 ->
                IActionSerializer.fromNetwork((RegistryFriendlyByteBuf) friendlyByteBuf1));
        actionHolder.addActions(actions);
        return actionHolder;
    }

    default void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, T type) {
        List<IAction> actions = type.getActions();
        friendlyByteBuf.writeCollection(actions, (buf, action) ->
                IActionSerializer.toNetwork(action, (RegistryFriendlyByteBuf) buf));
    }
}
