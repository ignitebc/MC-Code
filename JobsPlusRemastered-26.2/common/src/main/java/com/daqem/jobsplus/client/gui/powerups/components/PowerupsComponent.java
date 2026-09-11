package com.daqem.jobsplus.client.gui.powerups.components;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.powerups.PowerupsScreenState;
import com.daqem.jobsplus.client.gui.powerups.skilltree.PowerupsSkillTree;
import com.daqem.jobsplus.client.gui.powerups.skilltree.PowerupsSkillTreeItem;
import com.daqem.jobsplus.client.gui.powerups.tab.PowerupTab;
import com.daqem.jobsplus.client.gui.powerups.widgets.PowerupTabWidget;
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
    private static final int TAB_ROW_Y = 31;
    private static final int TAB_GAP = 1;
    private static final int MIN_TAB_WIDTH = 76;
    private static final int CONTENT_Y = TAB_ROW_Y + JobsTheme.TAB_HEIGHT + 3;
    private static final int CONTENT_BOTTOM_MARGIN = 21;

    private final PowerupsScreenState state;
    private final Map<Identifier, PowerupsSkillTreeItem> powerupItems = new LinkedHashMap<>();
    private final SkillTreeComponent skillTreeComponent;
    private final PowerupDetailsComponent detailsComponent;
    private final HyperPowerupsComponent hyperComponent;
    private PowerupTab cachedTab;

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
        int contentHeight = getContentHeight();
        this.skillTreeComponent = new SkillTreeComponent(
                10,
                CONTENT_Y + 2,
                getWidth() - 20 - (layout.wide() ? 158 : 0),
                contentHeight - 4,
                powerupsSkillTree
        );
        if (layout.wide()) {
            this.detailsComponent = new PowerupDetailsComponent(state, getWidth() - 158, CONTENT_Y, 150, contentHeight);
        } else {
            this.detailsComponent = null;
        }
        this.hyperComponent = new HyperPowerupsComponent(10, CONTENT_Y + 2, getWidth() - 20, contentHeight - 4);
        this.addTabWidgets();
        this.cachedTab = state.getSelectedTab();
        this.applySelectedTab();
    }

    private void addTabWidgets()
    {
        int tabX = 8;
        for (PowerupTab tab : PowerupTab.values())
        {
            int tabWidth = getTabWidth(tab);
            this.addWidget(new PowerupTabWidget(this.state, tab, tabX, TAB_ROW_Y, tabWidth));
            tabX += tabWidth + TAB_GAP;
        }
    }

    private static int getTabWidth(PowerupTab tab)
    {
        int labelWidth = (int) Math.ceil(Minecraft.getInstance().font.width(tab.getName()) * JobsTheme.LABEL_SCALE) + 20;
        return Math.max(MIN_TAB_WIDTH, labelWidth);
    }

    private int getContentHeight()
    {
        return getHeight() - CONTENT_Y - CONTENT_BOTTOM_MARGIN;
    }

    /** 선택한 탭의 내용만 자식으로 유지한다. 스킬 트리는 재생성하지 않아 스크롤과 선택이 보존된다. */
    private void applySelectedTab()
    {
        if (this.cachedTab == PowerupTab.NORMAL)
        {
            this.removeComponent(this.hyperComponent);
            this.addComponent(this.skillTreeComponent);
            if (this.detailsComponent != null)
            {
                this.addComponent(this.detailsComponent);
            }
            return;
        }

        this.removeComponent(this.skillTreeComponent);
        if (this.detailsComponent != null)
        {
            this.removeComponent(this.detailsComponent);
        }
        this.addComponent(this.hyperComponent);
    }

    public void refreshPowerups()
    {
        Map<Identifier, Powerup> owned = this.state.getJob().getPowerupManager().getAllPowerups().stream()
                .collect(Collectors.toMap(Powerup::getPowerupLocation, powerup -> powerup));
        // 위젯이 참조하는 객체를 유지해야 선택·스크롤·포커스가 초기화되지 않는다.
        for (PowerupsSkillTreeItem item : this.powerupItems.values())
        {
            Powerup powerup = item.getPowerup();
            if (powerup == null) continue;
            Powerup updated = owned.get(powerup.getPowerupLocation());
            powerup.setState(updated == null ? PowerupState.LOCKED : updated.getState());
        }
        for (PowerupsSkillTreeItem item : this.powerupItems.values())
        {
            Powerup powerup = item.getPowerup();
            if (powerup == null) continue;
            Identifier parentId = powerup.getPowerupInstance().getParentLocation();
            PowerupsSkillTreeItem parent = parentId == null ? null : this.powerupItems.get(parentId);
            if ((parentId == null || parent != null) && canUnlockPowerup(this.state, item, parent))
            {
                powerup.setState(PowerupState.NOT_OWNED);
            }
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                   float partialTick, int parentWidth, int parentHeight)
    {
        if (this.cachedTab != this.state.getSelectedTab())
        {
            this.cachedTab = this.state.getSelectedTab();
            this.applySelectedTab();
            this.updateParentPosition(getParentX(), getParentY(), parentWidth, parentHeight);
        }
        super.extractRenderState(graphics, mouseX, mouseY, partialTick, parentWidth, parentHeight);
        JobsTheme.texture(graphics, JobsTheme.Skin.HEADER, getTotalX() + 2, getTotalY() + 2,
                getWidth() - 4, 25);
        JobsTheme.text(graphics, this.state.getJob().getJobInstance().getName().copy()
                        .append(Component.literal("  /  스킬  ·  Lv. " + this.state.getJob().getLevel())),
                getTotalX() + 12, getTotalY() + 11, getWidth() - 48, JobsTheme.CYAN);
        graphics.fill(getTotalX() + 8, getTotalY() + 27, getTotalX() + getWidth() - 8,
                getTotalY() + 28, JobsTheme.DIVIDER);
        JobsTheme.cutBox(graphics, getTotalX() + 8, getTotalY() + CONTENT_Y,
                getWidth() - 16, getContentHeight(), JobsTheme.INSET, JobsTheme.BORDER);
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
