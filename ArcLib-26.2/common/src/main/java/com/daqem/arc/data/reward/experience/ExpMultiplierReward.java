package com.daqem.arc.data.reward.experience;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.player.ArcPlayer;
import com.daqem.arc.api.reward.AbstractReward;
import com.daqem.arc.player.SkillActivationNotifier;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.api.reward.type.IRewardType;
import com.daqem.arc.api.reward.type.RewardType;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ExpMultiplierReward extends AbstractReward {

    // 소수 보너스를 반올림하면 원본 경험치가 작을 때 배율이 장기적으로 맞지 않는다.
    // (예: 1 경험치 +5%는 항상 0, +50%는 항상 +1) 플레이어별로 소수 보너스를 누적해
    // 1 이상이 될 때만 지급하여 장기 기대값이 배율과 일치하도록 한다.
    private static final Map<UUID, Double> BONUS_EXP_REMAINDERS = new HashMap<>();

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
            BlockPos blockPos = actionData.getData(ActionDataType.BLOCK_POSITION);
            if (exp != null && exp > 0 && blockPos != null) {
                int bonusExp = collectWholeBonusExp(actionData.getPlayer(), exp);
                if (bonusExp > 0) {
                    boolean spawned = level.addFreshEntity(
                            new ExperienceOrb(level, blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, bonusExp));
                    if (spawned && actionData.getPlayer().arc$getPlayer() instanceof ServerPlayer serverPlayer) {
                        SkillActivationNotifier.notifySkillActivated(serverPlayer,
                                Component.translatable("arc.skill.extra_exp",
                                        SkillActivationNotifier.resolveSkillName(actionData), bonusExp));
                    }
                }
            }
        }
        return new ActionResult();
    }

    private int collectWholeBonusExp(ArcPlayer player, int exp) {
        if (multiplier <= 1.0D) {
            return 0;
        }

        UUID playerUUID = player.arc$getPlayer().getUUID();
        double accumulatedBonus = BONUS_EXP_REMAINDERS.getOrDefault(playerUUID, 0.0D) + exp * (multiplier - 1.0D);
        int wholeBonus = (int) Math.floor(accumulatedBonus + 1.0E-9D);
        BONUS_EXP_REMAINDERS.put(playerUUID, accumulatedBonus - wholeBonus);
        return wholeBonus;
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
