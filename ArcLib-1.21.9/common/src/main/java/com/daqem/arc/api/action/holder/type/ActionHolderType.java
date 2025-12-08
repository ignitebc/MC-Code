package com.daqem.arc.api.action.holder.type;

import com.daqem.arc.Arc;
import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.arc.api.action.holder.serializer.IActionHolderSerializer;
import com.daqem.arc.api.player.holder.PlayerActionHolder;
import com.daqem.arc.event.events.RegistryEvent;
import com.daqem.arc.registry.ArcRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public interface ActionHolderType<T extends IActionHolder> extends IActionHolderType<T> {

    ActionHolderType<PlayerActionHolder> PLAYER_ACTION_TYPE = ActionHolderType.register(Arc.getId("player"), new PlayerActionHolder.Serializer());

    static <T extends IActionHolder> ActionHolderType<T> register(final ResourceLocation location, final IActionHolderSerializer<T> serializer) {
        return Registry.register(ArcRegistry.ACTION_HOLDER, location, new ActionHolderType<T>() {

            @Override
            public ResourceLocation getLocation() {
                return location;
            }

            @Override
            public IActionHolderSerializer<T> getSerializer() {
                return serializer;
            }
        });
    }

    static void init() {
        RegistryEvent.REGISTER_ACTION_HOLDER_TYPE.invoker().registerActionHolderType();
    }
}
