package com.daqem.arc.data.condition.entity;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.condition.AbstractCondition;
import com.daqem.arc.api.condition.ICondition;
import com.daqem.arc.api.condition.serializer.IConditionSerializer;
import com.daqem.arc.api.condition.type.ConditionType;
import com.daqem.arc.api.condition.type.IConditionType;
import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enemy;

/**
 * 대상이 적대적 생명체인지 검사한다.
 *
 * 바닐라가 적대적 몹에 붙이는 Enemy 마커 인터페이스를 그대로 사용하므로
 * 종류를 일일이 나열하지 않아도 되고, 다른 모드가 추가한 적대적 몹도 함께 인정된다.
 * 소, 닭, 주민 같은 중립·우호 생명체는 제외된다.
 */
public class HostileEntityCondition extends AbstractCondition
{

    public HostileEntityCondition(boolean inverted)
    {
        super(inverted);
    }

    @Override
    public boolean isMet(ActionData actionData)
    {
        Entity entity = actionData.getData(ActionDataType.ENTITY);
        boolean isHostile = entity instanceof Enemy;
        return isInverted() != isHostile;
    }

    @Override
    public IConditionType<? extends ICondition> getType()
    {
        return ConditionType.HOSTILE_ENTITY;
    }

    @Override
    public Component getDescription()
    {
        return getDescription(new Object[0]);
    }

    public static class Serializer implements IConditionSerializer<HostileEntityCondition>
    {

        @Override
        public HostileEntityCondition fromJson(Identifier location, JsonObject jsonObject, boolean inverted)
        {
            return new HostileEntityCondition(inverted);
        }

        @Override
        public HostileEntityCondition fromNetwork(Identifier location, RegistryFriendlyByteBuf friendlyByteBuf, boolean inverted)
        {
            return new HostileEntityCondition(inverted);
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, HostileEntityCondition type)
        {
            IConditionSerializer.super.toNetwork(friendlyByteBuf, type);
        }
    }
}
