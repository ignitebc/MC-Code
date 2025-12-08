package com.daqem.arc.api.action.holder;

import com.daqem.arc.api.action.IAction;
import com.daqem.arc.api.action.holder.serializer.IActionHolderSerializer;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractActionHolder implements IActionHolder {

    protected final ResourceLocation location;
    // Made this a maps, so we don't get duplicate actions.
    protected final Map<ResourceLocation, IAction> actions = new HashMap<>();

    public AbstractActionHolder(ResourceLocation location) {
        this.location = location;
    }

    @Override
    public IActionHolderSerializer<?> getSerializer() {
        return getType().getSerializer();
    }

    @Override
    public ResourceLocation getLocation() {
        return location;
    }

    @Override
    public List<IAction> getActions() {
        return new ArrayList<>(actions.values());
    }

    @Override
    public void addAction(IAction action) {
        this.actions.put(action.getLocation(), action);
    }

    @Override
    public void addActions(List<IAction> actionHolderActions) {
        actionHolderActions.forEach(this::addAction);
    }

    @Override
    public void removeAction(IAction action) {
        this.actions.remove(action.getLocation());
    }

    @Override
    public void clearActions() {
        this.actions.clear();
    }
}
