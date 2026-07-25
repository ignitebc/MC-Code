package com.daqem.arc.data.condition.item;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.condition.AbstractCondition;
import com.daqem.arc.api.condition.ICondition;
import com.daqem.arc.api.condition.serializer.IConditionSerializer;
import com.daqem.arc.api.condition.type.ConditionType;
import com.daqem.arc.api.condition.type.IConditionType;
import com.daqem.arc.registry.ArcRegistry;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

/**
 * Wrapper condition used to express "tool-related" conditions.
 *
 * <p>Historically some datapacks use a structure like:</p>
 * <pre>
 * {
 *   "type": "arc:tool",
 *   "tool": {
 *     "type": "arc:not",
 *     "conditions": [ { "type": "arc:enchantments", ... } ]
 *   }
 * }
 * </pre>
 *
 * <p>This condition simply delegates to the nested condition. The nested condition is expected to
 * inspect the correct tool from {@link com.daqem.arc.api.action.data.type.ActionDataType#ITEM_STACK}
 * or fallback to the player's main hand.</p>
 */
public class ToolCondition extends AbstractCondition {

    private final ICondition toolCondition;

    public ToolCondition(boolean inverted, ICondition toolCondition) {
        super(inverted);
        this.toolCondition = toolCondition;
    }

    @Override
    public boolean isMet(ActionData actionData) {
        if (toolCondition == null) {
            return true;
        }
        return toolCondition.isMet(actionData);
    }

    @Override
    public IConditionType<?> getType() {
        return ConditionType.TOOL;
    }

    public ICondition getToolCondition() {
        return toolCondition;
    }

    public static class Serializer implements IConditionSerializer<ToolCondition> {

        @Override
        @SuppressWarnings("unchecked")
        public ToolCondition fromJson(ResourceLocation location, JsonObject jsonObject, boolean inverted) {
            JsonObject toolObj = GsonHelper.getAsJsonObject(jsonObject, "tool");
            ResourceLocation type = getResourceLocation(toolObj, "type");

            IConditionSerializer<ICondition> conditionSerializer = (IConditionSerializer<ICondition>) ArcRegistry.CONDITION
                    .getOptional(type)
                    .map(IConditionType::getSerializer)
                    .orElseThrow(() -> new JsonParseException("Unknown condition type: " + type));

            return new ToolCondition(inverted, conditionSerializer.fromJson(location, toolObj));
        }

        @Override
        public ToolCondition fromNetwork(ResourceLocation location, RegistryFriendlyByteBuf friendlyByteBuf, boolean inverted) {
            ICondition nested = IConditionSerializer.fromNetwork(friendlyByteBuf);
            return new ToolCondition(inverted, nested);
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, ToolCondition type) {
            IConditionSerializer.super.toNetwork(friendlyByteBuf, type);
            if (type.toolCondition == null) {
                // Encode a dummy NOT condition with zero children would require a registered serializer;
                // instead, we encode the actual nested condition only when present.
                // Null should not normally occur in valid datapacks.
                throw new IllegalStateException("ToolCondition.toolCondition is null");
            }
            // For nested conditions, Arc uses the condition type id as the "location" discriminator.
            IConditionSerializer.toNetwork(type.toolCondition, friendlyByteBuf, type.toolCondition.getType().getLocation());
        }
    }
}
