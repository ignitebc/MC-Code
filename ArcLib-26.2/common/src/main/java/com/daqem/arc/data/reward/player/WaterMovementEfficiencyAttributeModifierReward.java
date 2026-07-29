package com.daqem.arc.data.reward.player;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.reward.AbstractReward;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.api.reward.type.IRewardType;
import com.daqem.arc.api.reward.type.RewardType;
import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;

/**
 * 수중 이동 효율 속성을 올려 물속에서 느려지는 정도를 줄인다.
 * <p>
 * 심층 잠수부 마법이 사용하는 것과 같은 속성이며 0~1 범위다. 1.0이 심층 잠수부 III에 해당하고
 * 마법이 부여된 장화와는 합산된다. 바닥을 딛고 있을 때는 그대로, 헤엄치는 중에는 절반만 적용된다.
 * <p>
 * 실제 속성 반영은 {@link com.daqem.arc.player.AquaticAttributeSync}가 매 틱 처리한다.
 */
public class WaterMovementEfficiencyAttributeModifierReward extends AbstractReward
{

    private final double efficiency;

    public WaterMovementEfficiencyAttributeModifierReward(double chance, int priority, double efficiency)
    {
        super(chance, priority);
        this.efficiency = efficiency;
    }

    public double getEfficiency()
    {
        return efficiency;
    }

    public static Identifier computeModifierId(Identifier holderId, Identifier actionId)
    {
        String holderKey = sanitizeForPath(holderId.getNamespace() + "_" + holderId.getPath());
        String actionKey = sanitizeForPath(actionId.getNamespace() + "_" + actionId.getPath());
        return Identifier.fromNamespaceAndPath("arc",
                "water_movement_efficiency/" + holderKey + "/" + actionKey);
    }

    private static String sanitizeForPath(String value)
    {
        return value.toLowerCase()
                .replace(':', '_')
                .replace('/', '_')
                .replace('\\', '_')
                .replace(' ', '_');
    }

    @Override
    public IRewardType<?> getType()
    {
        return RewardType.WATER_MOVEMENT_EFFICIENCY_ATTRIBUTE_MODIFIER;
    }

    @Override
    public ActionResult apply(ActionData actionData)
    {
        return new ActionResult();
    }

    @Override
    public Component getName()
    {
        return Component.literal("수중 이동 효율 보정");
    }

    @Override
    public Component getDescription(Object... args)
    {
        return Component.literal("수중 이동 효율을 " + efficiency + " 증가시킵니다");
    }

    public static class Serializer implements IRewardSerializer<WaterMovementEfficiencyAttributeModifierReward>
    {

        @Override
        public WaterMovementEfficiencyAttributeModifierReward fromJson(JsonObject jsonObject, double chance,
                                                                      int priority)
        {
            return new WaterMovementEfficiencyAttributeModifierReward(
                    chance,
                    priority,
                    GsonHelper.getAsDouble(jsonObject, "efficiency"));
        }

        @Override
        public WaterMovementEfficiencyAttributeModifierReward fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf,
                                                                         double chance, int priority)
        {
            return new WaterMovementEfficiencyAttributeModifierReward(chance, priority, friendlyByteBuf.readDouble());
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf,
                              WaterMovementEfficiencyAttributeModifierReward type)
        {
            IRewardSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeDouble(type.efficiency);
        }
    }
}
