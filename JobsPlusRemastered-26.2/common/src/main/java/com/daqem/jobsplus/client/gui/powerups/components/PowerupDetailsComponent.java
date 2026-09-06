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
    private final PowerupsScreenState state;
    private final ActionScrollWidget descriptionScroll;
    private MultiLineTextComponent description;
    private Powerup displayedPowerup;

    public PowerupDetailsComponent(PowerupsScreenState state, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.state = state;
        this.descriptionScroll = new ActionScrollWidget(width - 16, Math.max(20, height - 140));
        this.descriptionScroll.setX(8);
        this.descriptionScroll.setY(62);
        this.addWidget(descriptionScroll);
        this.addWidget(new DetailButton(state, 8, height - 43, width - 16,
                "스킬 구매", PowerupState.NOT_OWNED));
        this.addWidget(new DetailButton(state, 8, height - 23, (width - 20) / 2,
                "활성화", PowerupState.INACTIVE));
        this.addWidget(new DetailButton(state, 12 + (width - 20) / 2, height - 23, (width - 20) / 2,
                "비활성화", PowerupState.ACTIVE));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                   float partialTick, int parentWidth, int parentHeight) {
        int x = getTotalX();
        int y = getTotalY();
        JobsTheme.panel(graphics, x, y, getWidth(), getHeight());
        JobsTheme.texture(graphics, JobsTheme.Skin.HEADER, x + 2, y + 2, getWidth() - 4, 18);
        JobsTheme.text(graphics, Component.literal("스킬 상세 정보"), x + 8, y + 7,
                getWidth() - 16, JobsTheme.TEXT);
        PowerupItemWidget preview = state.getPreviewWidget();
        Powerup powerup = preview == null ? null : preview.getPowerup();
        if (powerup == null) {
            return;
        }
        var instance = powerup.getPowerupInstance();
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
        JobsTheme.texture(graphics, JobsTheme.Skin.SLOT, x + 8, y + 27, 28, 28);
        graphics.fakeItem(instance.getIcon(), x + 14, y + 33);
        JobsTheme.text(graphics, instance.getName(), x + 42, y + 29, getWidth() - 50, JobsTheme.TEXT);
        String status = switch (powerup.getState()) {
            case ACTIVE -> "활성";
            case INACTIVE -> "비활성";
            case NOT_OWNED -> preview.isActive() ? "구매 가능" : "조건 미충족";
            default -> "잠김";
        };
        JobsTheme.text(graphics, Component.literal(status), x + 42, y + 43,
                getWidth() - 50, JobsTheme.CYAN);
        JobsTheme.texture(graphics, JobsTheme.Skin.INSET, x + 6, y + getHeight() - 74, getWidth() - 12, 26);
        JobsTheme.text(graphics, Component.literal("필요 레벨  " + instance.getRequiredLevel()),
                x + 12, y + getHeight() - 69, getWidth() - 24, JobsTheme.MUTED);
        JobsTheme.text(graphics, Component.literal("가격  " + instance.getPrice() + " 직업 코인"),
                x + 12, y + getHeight() - 58, getWidth() - 24, JobsTheme.TEXT);
    }

    private static class DetailButton extends CustomButtonWidget {
        private final PowerupsScreenState state;
        private final PowerupState expectedState;

        DetailButton(PowerupsScreenState state, int x, int y, int width, String label, PowerupState expectedState) {
            super(x, y, width, JobsTheme.BUTTON_HEIGHT, Component.literal(label), null, button -> {
                PowerupItemWidget preview = state.getPreviewWidget();
                if (preview != null && preview.getPowerup() != null
                        && preview.getPowerup().getState() == expectedState) {
                    preview.activateFromDetails();
                }
            });
            this.state = state;
            this.expectedState = expectedState;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            PowerupItemWidget preview = state.getPreviewWidget();
            this.active = preview != null && preview.getPowerup() != null
                    && preview.getPowerup().getState() == expectedState && preview.isActive();
            JobsTheme.button(graphics, getX(), getY(), getWidth(), getHeight(), active,
                    isHoveredOrFocused(), false, expectedState == PowerupState.NOT_OWNED);
            JobsTheme.label(graphics, getMessage(), getX(), getY(), getWidth(), getHeight(),
                    active ? JobsTheme.TEXT : JobsTheme.DISABLED);
        }
    }
}
