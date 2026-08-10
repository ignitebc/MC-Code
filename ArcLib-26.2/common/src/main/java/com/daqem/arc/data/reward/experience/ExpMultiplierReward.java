package com.daqem.arc.data.reward.experience;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.reward.AbstractReward;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.api.reward.type.IRewardType;
import com.daqem.arc.api.reward.type.RewardType;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.Level;

public class ExpMultiplierReward extends AbstractReward {

    private final double multiplier;

    public ExpMultiplierReward(double chance, int priority, double multiplier) {
        super(chance, priority);
        this.multiplier = multiplier;
    }

    @Override
    public Component getDescription() {
        return getDescription(multiplier);
    }

    @Override
    public ActionResult apply(ActionData actionData) {
        Level level = actionData.getData(ActionDataType.WORLD);
        if (level == null) {
            level = actionData.getPlayer().arc$getLevel();
        }
        if (level != null) {
            Integer exp = actionData.getData(ActionDataType.EXP_DROP);
            if (exp != null) {
                int bonusExp = (int) Math.round(exp * multiplier) - exp;
                BlockPos blockPos = actionData.getData(ActionDataType.BLOCK_POSITION);
                if (bonusExp > 0 && blockPos != null) {
                    level.addFreshEntity(
                            new ExperienceOrb(level, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, bonusExp));
                }
            }
        }
        return new ActionResult();
    }

    @Override
    public IRewardType<?> getType() {
        return RewardType.EXP_MULTIPLIER;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public static class Serializer implements IRewardSerializer<ExpMultiplierReward> {

        @Override
        public ExpMultiplierReward fromJson(JsonObject jsonObject, double chance, int priority) {
            return new ExpMultiplierReward(
                    chance,
                    priority,
                    GsonHelper.getAsDouble(jsonObject, "multiplier"));
        }

        @Override
        public ExpMultiplierReward fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, double chance, int priority) {
            return new ExpMultiplierReward(
                    chance,
                    priority,
                    friendlyByteBuf.readDouble());
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, ExpMultiplierReward type) {
            IRewardSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeDouble(type.multiplier);
        }
    }
}
