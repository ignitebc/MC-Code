package com.daqem.arc.api.action;

import com.daqem.arc.Arc;
import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.holder.ActionHolderManager;
import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.arc.api.action.holder.type.IActionHolderType;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.action.serializer.IActionSerializer;
import com.daqem.arc.api.condition.ICondition;
import com.daqem.arc.api.reward.IReward;
import com.daqem.arc.data.ActionManager;
import com.daqem.arc.event.events.ActionEvent;
import dev.architectury.event.EventResult;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractAction implements IAction {

    private final ResourceLocation location;
    private final ResourceLocation actionHolderLocation;
    private final IActionHolderType<?> actionHolderType;
    private final boolean performOnClient;
    private final List<IReward> rewards;
    private final List<ICondition> conditions;

    public AbstractAction(ResourceLocation location, ResourceLocation actionHolderLocation, IActionHolderType<?> actionHolderType, boolean performOnClient, List<IReward> rewards, List<ICondition> conditions) {
        this.location = location;
        this.actionHolderLocation = actionHolderLocation;
        this.actionHolderType = actionHolderType;
        this.performOnClient = performOnClient;
        this.rewards = rewards;
        this.conditions = conditions;
    }

    @Override
    public ResourceLocation getLocation() {
        return location;
    }

    @Override
    public IActionHolderType<?> getActionHolderType() {
        return actionHolderType;
    }

    @Override
    public ResourceLocation getActionHolderLocation() {
        return actionHolderLocation;
    }

    @Override
    public boolean shouldPerformOnClient() {
        return performOnClient;
    }

    @Override
    public IActionSerializer<?> getSerializer() {
        return getType().getSerializer();
    }

    @Override
    public Component getName() {
        return Arc.translatable("action." + this.getType().getLocation().getPath());
    }

    @Override
    public Component getDescription() {
        return Arc.translatable("action.description." + this.getType().getLocation().getPath());
    }

    @Override
    public Component getShortDescription() {
        return Arc.translatable("action.short_description." + this.getType().getLocation().getPath());
    }

    @Override
    public List<IReward> getRewards() {
        return rewards;
    }

    @Override
    public List<ICondition> getConditions() {
        return conditions;
    }

    public ActionResult perform(ActionData actionData) {
        IActionHolder sourceActionHolder = actionData.getPlayer().arc$getActionHolders().stream()
        .filter(actionHolder -> actionHolder.getType() == this.getActionHolderType()
                && actionHolder.getLocation().equals(this.getActionHolderLocation()))
        .findFirst()
        .orElseThrow(() -> new IllegalStateException("Action holder not found for action " + this.getType().getLocation() + " and action holder " + this.getActionHolderLocation()));

        actionData.setSourceActionHolder(sourceActionHolder);

        ActionResult result = new ActionResult();

        //noinspection resource
        if (actionData.getPlayer().arc$getLevel().isClientSide() && !performOnClient) {
            return result;
        }

        EventResult beforeConditionsEventResult = ActionEvent.BEFORE_CONDITIONS.invoker().registerBeforeConditions(actionData);
        if (beforeConditionsEventResult == EventResult.interrupt(true)) {
            return result;
        }

        if (!actionData.getSourceActionHolder().passedHolderCondition(actionData)) {
            return result;
        }

        if (metConditions(actionData)) {
            EventResult beforeRewardsEventResult = ActionEvent.BEFORE_REWARDS.invoker().registerBeforeRewards(actionData);
            if (beforeRewardsEventResult == EventResult.interrupt(true)) {
                return result;
            }
            if (Arc.isDebugEnvironment()) {
                Arc.LOGGER.info("Action {} passed conditions for action holder {}", this.getType().getLocation(), this.actionHolderLocation);
            }
            result = applyRewards(actionData);
        }

        return result;
    }

    public boolean metConditions(ActionData actionData) {
        return conditions.stream().allMatch(condition -> condition.isMet(actionData));
    }


    public ActionResult applyRewards(ActionData actionData) {
        return rewards.stream()
                .sorted((reward1, reward2) -> Integer.compare(reward2.getPriority(), reward1.getPriority()))
                .map(reward -> applyReward(actionData, reward))
                .reduce(new ActionResult(), ActionResult::merge);
    }

    private static ActionResult applyReward(ActionData actionData, IReward reward) {
        if (reward.passedChance(actionData)) {
            return reward.apply(actionData);
        }

        return new ActionResult();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof IAction action && action.getLocation().equals(this.getLocation());
    }
}
