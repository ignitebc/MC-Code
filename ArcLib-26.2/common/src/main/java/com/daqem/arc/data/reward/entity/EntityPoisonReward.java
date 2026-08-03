package com.daqem.arc.data.reward.entity;

import com.daqem.arc.api.IArcAbstractArrow;
import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.reward.AbstractReward;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.api.reward.type.IRewardType;
import com.daqem.arc.api.reward.type.RewardType;
import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.Entity;

/**
 * 발사한 화살에 독을 실어, 명중한 대상에게 독 효과를 건다.
 * 실제 부여는 화살이 대상을 맞힌 시점에 MixinAbstractArrow가 처리한다.
 */
public class EntityPoisonReward extends AbstractReward {

    private final int poisonTicks;
    private final int amplifier;

    public EntityPoisonReward(double chance, int priority, int poisonTicks, int amplifier) {
        super(chance, priority);
        this.poisonTicks = poisonTicks;
        this.amplifier = amplifier;
    }

    @Override
    public Component getDescription() {
        return getDescription(amplifier + 1, String.format("%.1f", poisonTicks / 20F));
    }

    @Override
    public IRewardType<?> getType() {
        return RewardType.ENTITY_POISON;
    }

    @Override
    public ActionResult apply(ActionData actionData) {
        Entity entity = actionData.getData(ActionDataType.ENTITY);
        if (entity instanceof IArcAbstractArrow abstractArrow) {
            abstractArrow.arc$setPoisonDurationTicks(poisonTicks);
            abstractArrow.arc$setPoisonAmplifier(amplifier);
        }
        return new ActionResult();
    }

    public int getPoisonTicks() {
        return poisonTicks;
    }

    public int getAmplifier() {
        return amplifier;
    }

    public static class Serializer implements IRewardSerializer<EntityPoisonReward> {

        @Override
        public EntityPoisonReward fromJson(JsonObject jsonObject, double chance, int priority) {
            return new EntityPoisonReward(
                    chance,
                    priority,
                    GsonHelper.getAsInt(jsonObject, "ticks"),
                    GsonHelper.getAsInt(jsonObject, "amplifier", 0));
        }

        @Override
        public EntityPoisonReward fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, double chance, int priority) {
            return new EntityPoisonReward(
                    chance,
                    priority,
                    friendlyByteBuf.readInt(),
                    friendlyByteBuf.readInt());
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, EntityPoisonReward type) {
            IRewardSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeInt(type.poisonTicks);
            friendlyByteBuf.writeInt(type.amplifier);
        }
    }
}
