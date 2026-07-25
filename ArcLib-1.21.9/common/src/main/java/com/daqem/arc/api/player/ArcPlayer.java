package com.daqem.arc.api.player;

import com.daqem.arc.api.action.holder.IActionHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.List;

public interface ArcPlayer {

    List<IActionHolder> arc$getActionHolders();

    void arc$addActionHolder(IActionHolder actionHolder);

    void arc$addActionHolders(List<IActionHolder> actionHolders);

    void arc$removeActionHolder(IActionHolder actionHolder);

    void arc$clearActionHolders();

    Level arc$getLevel();

    double arc$nextRandomDouble();

    Player arc$getPlayer();

    String arc$getName();
}
