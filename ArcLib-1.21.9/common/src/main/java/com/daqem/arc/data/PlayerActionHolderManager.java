package com.daqem.arc.data;

import com.daqem.arc.Arc;
import com.daqem.arc.api.action.holder.ActionHolderManager;
import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.arc.api.action.holder.type.ActionHolderType;
import com.daqem.arc.api.player.holder.PlayerActionHolder;
import com.google.common.collect.ImmutableMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PlayerActionHolderManager extends SimplePreparableReloadListener<IActionHolder> {

    private static PlayerActionHolderManager instance;
    private ImmutableMap<ResourceLocation, IActionHolder> playerActionHolders = ImmutableMap.of();

    public PlayerActionHolderManager() {
        instance = this;
    }

    @Override
    protected @NotNull IActionHolder prepare(ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        ResourceLocation location = Arc.getId("player");
        return new PlayerActionHolder(location);
    }

    @Override
    protected void apply(IActionHolder actionHolder, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        ActionHolderManager actionHolderManager = ActionHolderManager.getInstance();
        actionHolderManager.clearAllActionHoldersForType(ActionHolderType.PLAYER_ACTION_TYPE);


        this.playerActionHolders = ImmutableMap.of(actionHolder.getLocation(), actionHolder);

        actionHolderManager.registerActionHolders(List.of(actionHolder));
    }

    public static PlayerActionHolderManager getInstance() {
        return instance;
    }

    public List<IActionHolder> getPlayerActionHoldersList() {
        return new ArrayList<>(this.playerActionHolders.values());
    }
}
