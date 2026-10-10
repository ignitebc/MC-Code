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
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;

/**
 * "도구 관련" 조건을 표현하는 감싸기 조건.
 *
 * <p>예전부터 일부 데이터팩은 다음과 같은 구조를 쓴다:</p>
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
 * <p>이 조건은 안쪽 조건에 그대로 넘긴다. 안쪽 조건은
 * {@link com.daqem.arc.api.action.data.type.ActionDataType#ITEM_STACK}에서 올바른 도구를 읽거나,
 * 없으면 플레이어의 주 손을 확인해야 한다.</p>
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
        public ToolCondition fromJson(Identifier location, JsonObject jsonObject, boolean inverted) {
            JsonObject toolObj = GsonHelper.getAsJsonObject(jsonObject, "tool");
            Identifier type = getResourceLocation(toolObj, "type");

            IConditionSerializer<ICondition> conditionSerializer = (IConditionSerializer<ICondition>) ArcRegistry.CONDITION
                    .getOptional(type)
                    .map(IConditionType::getSerializer)
                    .orElseThrow(() -> new JsonParseException("Unknown condition type: " + type));

            return new ToolCondition(inverted, conditionSerializer.fromJson(location, toolObj));
        }

        @Override
        public ToolCondition fromNetwork(Identifier location, RegistryFriendlyByteBuf friendlyByteBuf, boolean inverted) {
            ICondition nested = IConditionSerializer.fromNetwork(friendlyByteBuf);
            return new ToolCondition(inverted, nested);
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, ToolCondition type) {
            IConditionSerializer.super.toNetwork(friendlyByteBuf, type);
            if (type.toolCondition == null) {
                // 자식이 없는 가짜 NOT 조건을 인코딩하려면 등록된 직렬화기가 필요하므로,
                // 실제 안쪽 조건이 있을 때만 그것을 인코딩한다.
                // 올바른 데이터팩에서는 null이 나오지 않는다.
                throw new IllegalStateException("ToolCondition.toolCondition is null");
            }
            // 안쪽 조건은 조건 종류 ID를 "location" 구분자로 쓴다.
            IConditionSerializer.toNetwork(type.toolCondition, friendlyByteBuf, type.toolCondition.getType().getLocation());
        }
    }
}
