package com.daqem.arc.api.action.holder.serializer;

import com.daqem.arc.api.action.IAction;
import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.arc.api.action.serializer.IActionSerializer;
import com.daqem.arc.data.serializer.ArcSerializer;
import com.daqem.arc.registry.ArcRegistry;
import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;

import java.util.List;

public interface IActionHolderSerializer<T extends IActionHolder> extends ArcSerializer {

    T fromJson(JsonObject jsonObject, Identifier location);

    T fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, Identifier location);

    static IActionHolder fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf) {
        Identifier resourceLocation = friendlyByteBuf.readResourceLocation();
        Identifier resourceLocation2 = friendlyByteBuf.readResourceLocation();
        return ArcRegistry.ACTION_HOLDER.getOptional(resourceLocation).orElseThrow(
                () -> new IllegalArgumentException("Unknown action holder serializer " + resourceLocation)
        ).getSerializer().fromNetwork(resourceLocation2, friendlyByteBuf);
    }

    static <T extends IActionHolder> void toNetwork(T actionHolder, RegistryFriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeResourceLocation(ArcRegistry.ACTION_HOLDER.getKey(actionHolder.getType()));
        friendlyByteBuf.writeResourceLocation(actionHolder.getLocation());
        ((IActionHolderSerializer<T>)actionHolder.getSerializer()).toNetwork(friendlyByteBuf, actionHolder);

    }

    default T fromJson(Identifier location, JsonObject jsonObject) {
        return fromJson(jsonObject, location);
    }

    default T fromNetwork(Identifier location, RegistryFriendlyByteBuf friendlyByteBuf) {
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
