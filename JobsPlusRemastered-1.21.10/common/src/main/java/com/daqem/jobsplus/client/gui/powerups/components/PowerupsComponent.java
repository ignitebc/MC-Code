package com.daqem.jobsplus.client.gui.powerups.components;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.powerups.PowerupsScreenState;
import com.daqem.jobsplus.client.gui.powerups.skilltree.PowerupsSkillTree;
import com.daqem.jobsplus.client.gui.powerups.skilltree.PowerupsSkillTreeItem;
import com.daqem.jobsplus.integration.arc.holder.holders.powerup.PowerupInstance;
import com.daqem.jobsplus.player.job.powerup.Powerup;
import com.daqem.jobsplus.player.job.powerup.PowerupState;
import com.daqem.uilib.gui.component.skilltree.SkillTreeComponent;
import com.daqem.uilib.gui.component.sprite.SpriteComponent;
import com.daqem.uilib.gui.component.text.TextComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.stream.Collectors;

public class PowerupsComponent extends SpriteComponent
{
    public static final int BACKGROUND_WIDTH = 326;
    public static final int BACKGROUND_HEIGHT = 240;
    public static final int SKILL_TREE_X = 23;
    public static final int SKILL_TREE_Y = 30;
    public static final int SKILL_TREE_WIDTH = 282;
    public static final int SKILL_TREE_HEIGHT = 192;

    public PowerupsComponent(PowerupsScreenState state)
    {
        super(0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT, JobsPlus.getId("powerups/background"));

        TextComponent title = new TextComponent(11, 5, state.getJob().getJobInstance().getName().withStyle(Style.EMPTY.withBold(true)).append(JobsPlus.literal(" • " + state.getJob().getLevel()).withStyle(Style.EMPTY.withBold(false))), 0xFFEAF0FF);
        this.addComponent(title);
        CoinsComponent coinsComponent = new CoinsComponent(state);
        coinsComponent.setX(-coinsComponent.getWidth() + 14);
        this.addComponent(coinsComponent);

        Map<ResourceLocation, Powerup> allPowerups = state.getJob().getPowerupManager().getAllPowerups().stream().collect(Collectors.toMap(Powerup::getPowerupLocation, powerup -> powerup));
        List<PowerupInstance> powerupInstances = state.getJob().getJobInstance().getPowerups();
        PowerupsSkillTreeItem rootItem = new PowerupsSkillTreeItem(state, null, true, new ArrayList<>());
        Map<ResourceLocation, PowerupsSkillTreeItem> powerupItems = new HashMap<>();
        for (PowerupInstance powerupInstance : powerupInstances)
        {
            Powerup powerup = allPowerups.get(powerupInstance.getLocation());
            if (powerup == null)
            {
                powerupItems.put(powerupInstance.getLocation(), new PowerupsSkillTreeItem(state, new Powerup(powerupInstance, PowerupState.LOCKED)));
            } else
            {
                powerupItems.put(powerupInstance.getLocation(), new PowerupsSkillTreeItem(state, powerup));
            }
        }
        for (PowerupsSkillTreeItem powerupItem : powerupItems.values())
        {
            ResourceLocation parentLocation = powerupItem.getPowerup().getPowerupInstance().getParentLocation();
            if (parentLocation == null)
            {
                rootItem.addChild(powerupItem);
                if (canUnlockPowerup(state, powerupItem, null))
                {
                    powerupItem.getPowerup().setState(PowerupState.NOT_OWNED);
                }
            } else
            {
                PowerupsSkillTreeItem parentItem = powerupItems.get(parentLocation);
                if (parentItem != null)
                {
                    parentItem.addChild(powerupItem);
                    if (canUnlockPowerup(state, powerupItem, parentItem))
                    {
                        powerupItem.getPowerup().setState(PowerupState.NOT_OWNED);
                    }
                }
            }
        }
        powerupItems.put(state.getJob().getJobInstance().getLocation(), rootItem);
        PowerupsSkillTree powerupsSkillTree = new PowerupsSkillTree(new ArrayList<>(powerupItems.values()));
        SkillTreeComponent skillTreeComponent = new SkillTreeComponent(
                SKILL_TREE_X,
                SKILL_TREE_Y,
                SKILL_TREE_WIDTH,
                SKILL_TREE_HEIGHT,
                powerupsSkillTree
        );
        this.addComponent(skillTreeComponent);
    }

    private static boolean canUnlockPowerup(PowerupsScreenState state, PowerupsSkillTreeItem powerupItem, PowerupsSkillTreeItem parentItem)
    {
        Powerup powerup = powerupItem.getPowerup();
        if (powerup == null || powerup.getState() != PowerupState.LOCKED)
        {
            return false;
        }

        if (state.getJob().getLevel() < powerup.getPowerupInstance().getRequiredLevel())
        {
            return false;
        }

        if (parentItem == null)
        {
            return true;
        }

        Powerup parentPowerup = parentItem.getPowerup();
        return parentPowerup != null && (parentPowerup.getState() == PowerupState.ACTIVE || parentPowerup.getState() == PowerupState.INACTIVE);
    }
}
