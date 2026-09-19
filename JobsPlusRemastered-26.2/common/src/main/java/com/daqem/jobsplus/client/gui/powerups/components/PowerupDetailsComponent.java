package com.daqem.jobsplus.client.gui.powerups.components;

import com.daqem.jobsplus.client.gui.powerups.PowerupsScreenState;
import com.daqem.jobsplus.client.gui.powerups.widgets.PowerupItemWidget;
import com.daqem.jobsplus.client.gui.jobs.widgets.ActionScrollWidget;
import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.jobsplus.player.job.powerup.Powerup;
import com.daqem.jobsplus.player.job.powerup.PowerupState;
import com.daqem.uilib.gui.component.EmptyComponent;
import com.daqem.uilib.gui.component.text.multiline.MultiLineTextComponent;
import com.daqem.uilib.gui.widget.CustomButtonWidget;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/** Client-only inspection panel. All actions delegate to the existing skill node. */
public class PowerupDetailsComponent extends EmptyComponent {
    /** 스킬 트리 칸과 같은 칸·아이템 크기 */
    private static final int ICON_SIZE = 24;
    private static final int ICON_Y = 27;
    /** 설명이 시작하는 줄 */
    private static final int DESCRIPTION_Y = ICON_Y + ICON_SIZE + 5;
    /** 단추 한 줄과 아래 여백이 쓰는 높이 */
    private static final int BUTTON_ROW_HEIGHT = 26;

    private final PowerupsScreenState state;
    private final ActionScrollWidget descriptionScroll;
    private MultiLineTextComponent description;
    private Powerup displayedPowerup;

    public PowerupDetailsComponent(PowerupsScreenState state, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.state = state;
        this.descriptionScroll = new ActionScrollWidget(width - 16,
                Math.max(20, height - DESCRIPTION_Y - BUTTON_ROW_HEIGHT));
        this.descriptionScroll.setX(8);
        this.descriptionScroll.setY(DESCRIPTION_Y);
        this.addWidget(descriptionScroll);

        // 구매와 활성 토글을 같은 크기로 한 줄에 놓는다.
        int buttonWidth = (width - 20) / 2;
        int buttonY = height - 20;
        this.addWidget(new PurchaseButton(state, 8, buttonY, buttonWidth));
        this.addWidget(new ToggleButton(state, 12 + buttonWidth, buttonY, buttonWidth));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                   float partialTick, int parentWidth, int parentHeight) {
        int x = getTotalX();
        int y = getTotalY();
        JobsTheme.panel(graphics, x, y, getWidth(), getHeight());
        JobsTheme.texture(graphics, JobsTheme.Skin.HEADER, x + 2, y + 2, getWidth() - 4, 18);
        JobsTheme.text(graphics, Component.literal("스킬상세정보"), x + 8, y + 7,
                getWidth() - 16, JobsTheme.TEXT);
        PowerupItemWidget preview = state.getPreviewWidget();
        Powerup powerup = preview == null ? null : preview.getPowerup();
        if (powerup == null) {
            return;
        }
        var instance = powerup.getPowerupInstance();

        // 필요 레벨과 가격은 제목과 같은 줄 오른쪽 끝에 붙인다.
        JobsTheme.textRight(graphics,
                Component.literal("필요레벨 " + instance.getRequiredLevel()
                        + "  /  직업코인 " + instance.getPrice()),
                x + getWidth() - 8, y + 7, getWidth() - 62, JobsTheme.MUTED);

        if (displayedPowerup != powerup) {
            if (description != null) {
                descriptionScroll.removeComponent(description);
            }
            description = new MultiLineTextComponent(0, 0, descriptionScroll.getWidth() - 10,
                    instance.getDescription(), JobsTheme.TEXT);
            description.setLineSpacing(2);
            descriptionScroll.addComponent(description);
            descriptionScroll.setScrollAmount(0);
            displayedPowerup = powerup;
            this.updateParentPosition(getParentX(), getParentY(), parentWidth, parentHeight);
        }
        JobsTheme.texture(graphics, JobsTheme.Skin.SLOT, x + 8, y + ICON_Y, ICON_SIZE, ICON_SIZE);
        graphics.fakeItem(instance.getIcon(), x + 12, y + ICON_Y + 4);
        JobsTheme.text(graphics, instance.getName(), x + 38, y + ICON_Y + 2, getWidth() - 46, JobsTheme.TEXT);
        String status = switch (powerup.getState()) {
            case ACTIVE -> "활성";
            case INACTIVE -> "비활성";
            case NOT_OWNED -> preview.isActive() ? "구매 가능" : "조건 미충족";
            default -> "잠김";
        };
        JobsTheme.text(graphics, Component.literal(status), x + 38, y + ICON_Y + 14,
                getWidth() - 46, JobsTheme.CYAN);
    }

    /** 미리보기로 잡힌 스킬 칸에 그대로 넘기는 단추. 눌리는 조건은 칸 쪽 규칙을 따른다. */
    private abstract static class DetailButton extends CustomButtonWidget {
        final PowerupsScreenState state;

        DetailButton(PowerupsScreenState state, int x, int y, int width) {
            super(x, y, width, JobsTheme.BUTTON_HEIGHT, Component.empty(), null, button -> {
                PowerupItemWidget preview = state.getPreviewWidget();
                if (preview != null && preview.getPowerup() != null) {
                    preview.activateFromDetails();
                }
            });
            this.state = state;
        }

        /** 지금 눌러도 되는 상태인지 */
        abstract boolean usable(Powerup powerup, PowerupItemWidget preview);

        /** 지금 상태에 맞는 글자 */
        abstract Component label(Powerup powerup);

        /** 파란 단추로 강조할지 */
        boolean primary() {
            return false;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            PowerupItemWidget preview = state.getPreviewWidget();
            Powerup powerup = preview == null ? null : preview.getPowerup();
            this.active = powerup != null && preview.isActive() && usable(powerup, preview);
            this.setMessage(label(powerup));
            JobsTheme.button(graphics, getX(), getY(), getWidth(), getHeight(), active,
                    isHoveredOrFocused(), false, primary());
            JobsTheme.label(graphics, getMessage(), getX(), getY(), getWidth(), getHeight(),
                    active ? JobsTheme.TEXT : JobsTheme.DISABLED);
        }
    }

    /** 아직 없는 스킬을 살 때만 눌린다. */
    private static final class PurchaseButton extends DetailButton {
        private static final Component LABEL = Component.literal("스킬 구매");

        PurchaseButton(PowerupsScreenState state, int x, int y, int width) {
            super(state, x, y, width);
        }

        @Override
        boolean usable(Powerup powerup, PowerupItemWidget preview) {
            return powerup.getState() == PowerupState.NOT_OWNED;
        }

        @Override
        Component label(Powerup powerup) {
            return LABEL;
        }

        @Override
        boolean primary() {
            return true;
        }
    }

    /** 가진 스킬의 활성과 비활성을 한 단추로 오간다. */
    private static final class ToggleButton extends DetailButton {
        private static final Component ACTIVATE = Component.literal("활성화");
        private static final Component DEACTIVATE = Component.literal("비활성화");

        ToggleButton(PowerupsScreenState state, int x, int y, int width) {
            super(state, x, y, width);
        }

        @Override
        boolean usable(Powerup powerup, PowerupItemWidget preview) {
            return powerup.getState() == PowerupState.ACTIVE || powerup.getState() == PowerupState.INACTIVE;
        }

        @Override
        Component label(Powerup powerup) {
            // 켜져 있으면 끄는 단추, 그 밖에는 켜는 단추로 보인다.
            return powerup != null && powerup.getState() == PowerupState.ACTIVE ? DEACTIVATE : ACTIVATE;
        }
    }
}
