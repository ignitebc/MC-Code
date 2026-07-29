package com.daqem.arc.api.player.holder;

import com.daqem.arc.api.action.holder.AbstractActionHolder;
import com.daqem.arc.api.action.holder.serializer.IActionHolderSerializer;
import com.daqem.arc.api.action.holder.type.ActionHolderType;
import com.daqem.arc.api.action.holder.type.IActionHolderType;
import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;

public class PlayerActionHolder extends AbstractActionHolder {

    public PlayerActionHolder(Identifier location) {
        super(location);
    }

    @Override
    public IActionHolderType<?> getType() {
        return ActionHolderType.PLAYER_ACTION_TYPE;
    }

    public static class Serializer implements IActionHolderSerializer<PlayerActionHolder> {

        @Override
        public PlayerActionHolder fromJson(JsonObject jsonObject, Identifier location) {
            return new PlayerActionHolder(location);
        }

        @Override
        public PlayerActionHolder fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, Identifier location) {
            return new PlayerActionHolder(location);
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, PlayerActionHolder type) {
            IActionHolderSerializer.super.toNetwork(friendlyByteBuf, type);
        }
    }
}
