package com.daqem.jobsplus.integration.arc.condition.conditions;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.condition.AbstractCondition;
import com.daqem.arc.api.condition.ICondition;
import com.daqem.arc.api.condition.serializer.IConditionSerializer;
import com.daqem.arc.api.condition.type.IConditionType;
import com.daqem.jobsplus.integration.arc.condition.type.JobsPlusConditionType;
import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;

/**
 * 정한 확률(%)로만 통과하는 조건.
 * <p>
 * 보상마다 붙는 확률은 보상끼리 따로 추첨한다. 맹독 불화살처럼 불과 독이 함께 걸려야 하는 효과는
 * 이 조건으로 한 번만 추첨하고, 통과하면 보상을 모두 지급한다.
 */
public class RandomChanceCondition extends AbstractCondition
{
    /** 통과 확률(%). 0이면 통과하지 않고 100이면 항상 통과한다. */
    private final double chance;

    public RandomChanceCondition(boolean inverted, double chance)
    {
        super(inverted);
        this.chance = chance;
    }

    @Override
    public boolean isMet(ActionData actionData)
    {
        if (this.chance >= 100)
        {
            return true;
        }
        return actionData.getPlayer().arc$nextRandomDouble() * 100 < this.chance;
    }

    @Override
    public IConditionType<? extends ICondition> getType()
    {
        return JobsPlusConditionType.RANDOM_CHANCE;
    }

    @Override
    public Component getDescription()
    {
        return getDescription(String.format("%.0f", this.chance));
    }

    public static class Serializer implements IConditionSerializer<RandomChanceCondition>
    {

        @Override
        public RandomChanceCondition fromJson(Identifier location, JsonObject jsonObject, boolean inverted)
        {
            return new RandomChanceCondition(inverted, GsonHelper.getAsDouble(jsonObject, "chance"));
        }

        @Override
        public RandomChanceCondition fromNetwork(Identifier location, RegistryFriendlyByteBuf friendlyByteBuf, boolean inverted)
        {
            return new RandomChanceCondition(inverted, friendlyByteBuf.readDouble());
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, RandomChanceCondition type)
        {
            IConditionSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeDouble(type.chance);
        }
    }
}
