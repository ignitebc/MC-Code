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
 * 산소 보너스 속성을 올려 잠수 가능 시간을 늘린다.
 * <p>
 * 바닐라는 보너스 k에 대해 {@code 1/(k+1)} 확률로만 숨을 줄이므로, 기대 잠수 시간이 (k+1)배가 된다.
 * 기본 잠수 시간 15초 기준으로 k=1이면 30초, k=3이면 1분이 된다.
 * <p>
 * 실제 속성 반영은 {@link com.daqem.arc.player.AquaticAttributeSync}가 매 틱 처리한다.
 */
public class OxygenBonusAttributeModifierReward extends AbstractReward
{

    private final double bonus;

    public OxygenBonusAttributeModifierReward(double chance, int priority, double bonus)
    {
        super(chance, priority);
        this.bonus = bonus;
    }

    public double getBonus()
    {
        return bonus;
    }

    public static Identifier computeModifierId(Identifier holderId, Identifier actionId)
    {
        String holderKey = sanitizeForPath(holderId.getNamespace() + "_" + holderId.getPath());
        String actionKey = sanitizeForPath(actionId.getNamespace() + "_" + actionId.getPath());
        return Identifier.fromNamespaceAndPath("arc", "oxygen_bonus/" + holderKey + "/" + actionKey);
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
        return RewardType.OXYGEN_BONUS_ATTRIBUTE_MODIFIER;
    }

    @Override
    public ActionResult apply(ActionData actionData)
    {
        return new ActionResult();
    }

    @Override
    public Component getName()
    {
        return Component.literal("산소 보너스 보정");
    }

    @Override
    public Component getDescription(Object... args)
    {
        return Component.literal("잠수 가능 시간을 " + (bonus + 1) + "배로 늘립니다");
    }

    public static class Serializer implements IRewardSerializer<OxygenBonusAttributeModifierReward>
    {

        @Override
        public OxygenBonusAttributeModifierReward fromJson(JsonObject jsonObject, double chance, int priority)
        {
            return new OxygenBonusAttributeModifierReward(
                    chance,
                    priority,
                    GsonHelper.getAsDouble(jsonObject, "bonus"));
        }

        @Override
        public OxygenBonusAttributeModifierReward fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, double chance,
                                                              int priority)
        {
            return new OxygenBonusAttributeModifierReward(chance, priority, friendlyByteBuf.readDouble());
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, OxygenBonusAttributeModifierReward type)
        {
            IRewardSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeDouble(type.bonus);
        }
    }
}
