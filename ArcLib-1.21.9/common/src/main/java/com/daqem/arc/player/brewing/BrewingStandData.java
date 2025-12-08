package com.daqem.arc.player.brewing;

import com.daqem.arc.api.player.ArcServerPlayer;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;

import java.util.HashMap;
import java.util.Map;

public class BrewingStandData {

    private final BrewingStandBlockEntity brewingStandBlockEntity;
    private final Map<Integer, ArcServerPlayer> brewingStandItemOwners = new HashMap<>();
    private ArcServerPlayer lastPlayerToInteract;

    public BrewingStandData(BrewingStandBlockEntity brewingStandBlockEntity) {
        this.brewingStandBlockEntity = brewingStandBlockEntity;
    }

    public BrewingStandData(BrewingStandBlockEntity brewingStandBlockEntity, ArcServerPlayer lastPlayerToInteract) {
        this(brewingStandBlockEntity);
        this.lastPlayerToInteract = lastPlayerToInteract;
    }

    public ArcServerPlayer getBrewingStandItemOwner(int slot) {
        if (!brewingStandItemOwners.containsKey(slot)) {
            return null;
        }
        return brewingStandItemOwners.get(slot);
    }

    public Map<Integer, ArcServerPlayer> getBrewingStandItemOwners() {
        return brewingStandItemOwners;
    }

    public void removeBrewingStandItemOwner(int slot) {
        brewingStandItemOwners.remove(slot);
    }

    public void setLastPlayerToInteract(ArcServerPlayer arcServerPlayer) {
        this.lastPlayerToInteract = arcServerPlayer;
    }

    public ArcServerPlayer getLastPlayerToInteract() {
        return lastPlayerToInteract;
    }

    public void addBrewingStandItemOwner(int slot, ArcServerPlayer owner) {
        if (brewingStandItemOwners.containsKey(slot)) {
            brewingStandItemOwners.replace(slot, owner);
        } else {
            brewingStandItemOwners.put(slot, owner);
        }
    }
}
