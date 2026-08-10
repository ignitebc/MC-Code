package com.daqem.jobsplus.integration.arc.reward.rewards.job;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.player.ArcPlayer;
import com.daqem.arc.api.reward.AbstractReward;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.api.reward.type.IRewardType;
import com.daqem.jobsplus.integration.arc.holder.holders.powerup.PowerupInstance;
import com.daqem.jobsplus.integration.arc.reward.type.JobsPlusRewardType;
import com.daqem.jobsplus.player.JobsPlayer;
import com.daqem.arc.player.SkillActivationNotifier;
import com.daqem.jobsplus.player.PlayerItemDelivery;
import com.daqem.jobsplus.player.job.powerup.Powerup;
import com.daqem.jobsplus.player.job.powerup.PowerupState;
import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

import java.util.Optional;

/**
 * 확률에 당첨되면 아이템을 지급하고, 지정한 보너스 계열을 보유했다면 그 자리에서 개수를 두 배로 올린다.
 *
 * 보상은 서로 독립적으로 확률을 굴리기 때문에 arc:item 을 두 개 나열하면
 * "지급된 아이템이 두 배가 된다"가 아니라 "같은 아이템을 따로 한 번 더 굴린다"가 된다.
 * 지급 여부를 먼저 판정한 뒤 그 결과에만 배수를 적용하기 위해 별도 보상으로 분리했다.
 */
public class SpecialItemReward extends AbstractReward
{

    /** 로마 숫자 단계 접미사. 뒤쪽이 상위 단계다. */
    private static final String[] TIER_SUFFIXES = {
            "i", "ii", "iii", "iv", "v", "vi", "vii", "viii", "viiii", "x"
    };

    private final ItemStackTemplate itemTemplate;
    private final int amount;
    private final Identifier bonusPowerupLine;
    private final double bonusChancePerTier;

    public SpecialItemReward(double chance, int priority, ItemStackTemplate itemTemplate, int amount,
                             Identifier bonusPowerupLine, double bonusChancePerTier)
    {
        super(chance, priority);
        this.itemTemplate = itemTemplate;
        this.amount = amount;
        this.bonusPowerupLine = bonusPowerupLine;
        this.bonusChancePerTier = bonusChancePerTier;
    }

    @Override
    public IRewardType<?> getType()
    {
        return JobsPlusRewardType.SPECIAL_ITEM;
    }

    @Override
    public Component getDescription()
    {
        return getDescription(this.amount, this.getItemStack().getHoverName());
    }

    @Override
    public ActionResult apply(ActionData actionData)
    {
        ArcPlayer arcPlayer = actionData.getPlayer();
        if (!(arcPlayer instanceof JobsPlayer jobsPlayer))
        {
            return new ActionResult();
        }

        int grantedAmount = this.amount;
        Component bonusSkillName = null;
        if (this.shouldDouble(jobsPlayer, actionData))
        {
            grantedAmount = grantedAmount * 2;
            bonusSkillName = getBonusPowerupName(jobsPlayer);
        }

        ItemStack reward = this.itemTemplate.create();
        reward.setCount(grantedAmount);
        giveToPlayer(arcPlayer, reward);

        ItemStack baseNotificationStack = reward.copyWithCount(this.amount);
        SkillActivationNotifier.notifyExtraDrop(actionData, baseNotificationStack);

        // 기본 특수 아이템 획득과 두 배 보너스는 서로 다른 스킬 발동이므로 각각 알린다.
        if (bonusSkillName != null && arcPlayer.arc$getPlayer() instanceof ServerPlayer serverPlayer)
        {
            ItemStack bonusNotificationStack = reward.copyWithCount(grantedAmount - this.amount);
            SkillActivationNotifier.notifyExtraDrop(serverPlayer, bonusSkillName, bonusNotificationStack);
        }

        return new ActionResult();
    }

    /** 보유한 보너스 계열 최고 단계 파워업의 표시 이름을 돌려준다. 없으면 null이다. */
    private Component getBonusPowerupName(JobsPlayer jobsPlayer)
    {
        int tier = getHighestActiveTier(jobsPlayer, this.bonusPowerupLine);
        if (tier <= 0)
        {
            return null;
        }

        Identifier powerupLocation = Identifier.fromNamespaceAndPath(
                this.bonusPowerupLine.getNamespace(),
                this.bonusPowerupLine.getPath() + "_" + TIER_SUFFIXES[tier - 1]);
        PowerupInstance powerupInstance = PowerupInstance.of(powerupLocation);
        if (powerupInstance == null)
        {
            return null;
        }
        return powerupInstance.getName();
    }

    private boolean shouldDouble(JobsPlayer jobsPlayer, ActionData actionData)
    {
        if (this.bonusPowerupLine == null || this.bonusChancePerTier <= 0)
        {
            return false;
        }

        int tier = getHighestActiveTier(jobsPlayer, this.bonusPowerupLine);
        if (tier <= 0)
        {
            return false;
        }

        double bonusChance = tier * this.bonusChancePerTier;
        return actionData.getPlayer().arc$nextRandomDouble() * 100 <= bonusChance;
    }

    /** 보유한 단계 중 가장 높은 것을 반환한다. 하나도 없으면 0이다. */
    private static int getHighestActiveTier(JobsPlayer jobsPlayer, Identifier powerupLine)
    {
        for (int index = TIER_SUFFIXES.length - 1; index >= 0; index--)
        {
            Identifier powerupLocation = Identifier.fromNamespaceAndPath(
                    powerupLine.getNamespace(),
                    powerupLine.getPath() + "_" + TIER_SUFFIXES[index]);

            if (isPowerupActive(jobsPlayer, powerupLocation))
            {
                return index + 1;
            }
        }
        return 0;
    }

    private static boolean isPowerupActive(JobsPlayer jobsPlayer, Identifier powerupLocation)
    {
        Optional<Powerup> powerup = jobsPlayer.jobsplus$getJobs().stream()
                .map(job -> job.getPowerupManager().getPowerup(powerupLocation))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();

        return powerup.isPresent() && powerup.get().getState() == PowerupState.ACTIVE;
    }

    private static void giveToPlayer(ArcPlayer arcPlayer, ItemStack reward)
    {
        if (arcPlayer.arc$getPlayer() instanceof ServerPlayer serverPlayer)
        {
            PlayerItemDelivery.giveOrDrop(serverPlayer, reward);
        }
    }

    public ItemStack getItemStack()
    {
        return this.itemTemplate.create();
    }

    public int getAmount()
    {
        return this.amount;
    }

    public static class Serializer implements IRewardSerializer<SpecialItemReward>
    {

        @Override
        public SpecialItemReward fromJson(JsonObject jsonObject, double chance, int priority)
        {
            int amount = GsonHelper.getAsInt(jsonObject, "amount", 1);
            ItemStackTemplate itemTemplate = getItemStackTemplate(jsonObject.get("item")).withCount(amount);

            Identifier bonusPowerupLine = null;
            if (jsonObject.has("bonus_powerup"))
            {
                bonusPowerupLine = Identifier.parse(GsonHelper.getAsString(jsonObject, "bonus_powerup"));
            }
            double bonusChancePerTier = GsonHelper.getAsDouble(jsonObject, "bonus_chance_per_tier", 0D);

            return new SpecialItemReward(chance, priority, itemTemplate, amount, bonusPowerupLine, bonusChancePerTier);
        }

        @Override
        public SpecialItemReward fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, double chance, int priority)
        {
            ItemStackTemplate itemTemplate = ItemStackTemplate.STREAM_CODEC.decode(friendlyByteBuf);
            int amount = friendlyByteBuf.readInt();
            Identifier bonusPowerupLine = null;
            if (friendlyByteBuf.readBoolean())
            {
                bonusPowerupLine = friendlyByteBuf.readIdentifier();
            }
            double bonusChancePerTier = friendlyByteBuf.readDouble();
            return new SpecialItemReward(chance, priority, itemTemplate, amount, bonusPowerupLine, bonusChancePerTier);
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, SpecialItemReward type)
        {
            IRewardSerializer.super.toNetwork(friendlyByteBuf, type);
            ItemStackTemplate.STREAM_CODEC.encode(friendlyByteBuf, type.itemTemplate);
            friendlyByteBuf.writeInt(type.amount);
            friendlyByteBuf.writeBoolean(type.bonusPowerupLine != null);
            if (type.bonusPowerupLine != null)
            {
                friendlyByteBuf.writeIdentifier(type.bonusPowerupLine);
            }
            friendlyByteBuf.writeDouble(type.bonusChancePerTier);
        }
    }
}
