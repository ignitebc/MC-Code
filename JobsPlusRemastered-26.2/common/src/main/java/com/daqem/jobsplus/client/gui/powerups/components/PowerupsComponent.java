package com.daqem.jobsplus.client.gui.powerups.components;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.powerups.PowerupsScreenState;
import com.daqem.jobsplus.client.gui.powerups.skilltree.PowerupsSkillTree;
import com.daqem.jobsplus.client.gui.powerups.skilltree.PowerupsSkillTreeItem;
import com.daqem.jobsplus.integration.arc.holder.holders.powerup.PowerupInstance;
import com.daqem.jobsplus.player.job.powerup.Powerup;
import com.daqem.jobsplus.player.job.powerup.PowerupState;
import com.daqem.uilib.gui.component.skilltree.SkillTreeComponent;
import com.daqem.jobsplus.client.gui.theme.JobsSpriteComponent;
import com.daqem.jobsplus.client.gui.theme.JobsCloseButton;
import com.daqem.jobsplus.client.gui.theme.JobsLayout;
import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.*;
import java.util.stream.Collectors;

public class PowerupsComponent extends JobsSpriteComponent
{
    private final PowerupsScreenState state;

    public PowerupsComponent(PowerupsScreenState state)
    {
        this(state, JobsLayout.forScreen(Minecraft.getInstance().getWindow().getGuiScaledWidth(),
                Minecraft.getInstance().getWindow().getGuiScaledHeight()));
    }

    private PowerupsComponent(PowerupsScreenState state, JobsLayout layout)
    {
        super(0, 0, layout.width(), layout.height(), JobsPlus.getId("powerups/background"));
        this.state = state;
        state.setDetailsPanelVisible(layout.wide());
        this.addWidget(new JobsCloseButton(getWidth() - 22, 7));
        CoinsComponent coinsComponent = new CoinsComponent(state);
        coinsComponent.setX(getWidth() - coinsComponent.getWidth() - 8);
        coinsComponent.setY(getHeight() - 17);
        this.addComponent(coinsComponent);

        Map<Identifier, Powerup> allPowerups = state.getJob().getPowerupManager().getAllPowerups().stream().collect(Collectors.toMap(Powerup::getPowerupLocation, powerup -> powerup));
        List<PowerupInstance> powerupInstances = state.getJob().getJobInstance().getPowerups();
        PowerupsSkillTreeItem rootItem = new PowerupsSkillTreeItem(state, null, true, new ArrayList<>());
        Map<Identifier, PowerupsSkillTreeItem> powerupItems = new HashMap<>();
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
            Identifier parentLocation = powerupItem.getPowerup().getPowerupInstance().getParentLocation();
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
                10,
                34,
                getWidth() - 20 - (layout.wide() ? 158 : 0),
                getHeight() - 57,
                powerupsSkillTree
        );
        this.addComponent(skillTreeComponent);
        if (layout.wide()) {
            this.addComponent(new PowerupDetailsComponent(state, getWidth() - 158, 32, 150, getHeight() - 53));
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                   float partialTick, int parentWidth, int parentHeight)
    {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick, parentWidth, parentHeight);
        JobsTheme.texture(graphics, JobsTheme.Skin.HEADER, getTotalX() + 2, getTotalY() + 2,
                getWidth() - 4, 25);
        JobsTheme.text(graphics, this.state.getJob().getJobInstance().getName().copy()
                        .append(Component.literal("  /  스킬  ·  Lv. " + this.state.getJob().getLevel())),
                getTotalX() + 12, getTotalY() + 11, getWidth() - 48, JobsTheme.CYAN);
        graphics.fill(getTotalX() + 8, getTotalY() + 27, getTotalX() + getWidth() - 8,
                getTotalY() + 28, JobsTheme.DIVIDER);
        JobsTheme.cutBox(graphics, getTotalX() + 8, getTotalY() + 32,
                getWidth() - 16, getHeight() - 53, JobsTheme.INSET, JobsTheme.BORDER);
        JobsTheme.text(graphics, Component.literal("드래그 이동  ·  스킬 선택  ·  ESC 돌아가기"),
                getTotalX() + 10, getTotalY() + getHeight() - 12, getWidth() - 100, JobsTheme.MUTED);
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
