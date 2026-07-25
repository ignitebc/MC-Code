package com.daqem.arc.api.player.holder;

import com.daqem.arc.api.action.holder.AbstractActionHolder;
import com.daqem.arc.api.action.holder.serializer.IActionHolderSerializer;
import com.daqem.arc.api.action.holder.type.ActionHolderType;
import com.daqem.arc.api.action.holder.type.IActionHolderType;
import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class PlayerActionHolder extends AbstractActionHolder {

    public PlayerActionHolder(ResourceLocation location) {
        super(location);
    }

    @Override
    public IActionHolderType<?> getType() {
        return ActionHolderType.PLAYER_ACTION_TYPE;
    }

    public static class Serializer implements IActionHolderSerializer<PlayerActionHolder> {

        @Override
        public PlayerActionHolder fromJson(JsonObject jsonObject, ResourceLocation location) {
            return new PlayerActionHolder(location);
        }

        @Override
        public PlayerActionHolder fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, ResourceLocation location) {
            return new PlayerActionHolder(location);
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, PlayerActionHolder type) {
            IActionHolderSerializer.super.toNetwork(friendlyByteBuf, type);
        }
    }
}
