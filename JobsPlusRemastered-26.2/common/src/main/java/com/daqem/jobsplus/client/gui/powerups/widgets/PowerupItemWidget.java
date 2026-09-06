package com.daqem.jobsplus.client.gui.powerups.widgets;

import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.confimation.ConfirmationScreen;
import com.daqem.jobsplus.client.gui.confimation.ConfirmationScreenState;
import com.daqem.jobsplus.client.gui.powerups.PowerupsScreen;
import com.daqem.jobsplus.client.gui.powerups.PowerupsScreenState;
import com.daqem.jobsplus.integration.arc.holder.holders.powerup.PowerupInstance;
import com.daqem.jobsplus.networking.c2s.ServerboundOpenPowerupsScreenPacket;
import com.daqem.jobsplus.networking.c2s.ServerboundStartPowerupPacket;
import com.daqem.jobsplus.networking.c2s.ServerboundTogglePowerUpPacket;
import com.daqem.jobsplus.player.job.Job;
import com.daqem.jobsplus.player.job.powerup.Powerup;
import com.daqem.jobsplus.player.job.powerup.PowerupState;
import com.daqem.uilib.api.skilltree.ISkillTreeItem;
import com.daqem.uilib.api.widget.skilltree.ISkillTreeItemWidget;
import com.daqem.uilib.gui.component.text.multiline.MultiLineTextComponent;
import com.daqem.uilib.gui.widget.CustomButtonWidget;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

// powerup 추가시 그냥 위젯이 알아서 위치에 맞게 선을 그어줌(ui상) , 따로 건들일 필요 없고, json의 parent만 연결을 잘해주면 됨
// powerup 스킬 추가, 삭제시 json parent 재배치 필요
// {
//   "location": "jobsplus:miner/skill2",
//   "job": "jobsplus:miner",
//   "parent": "jobsplus:miner/skill1",  // 부모 스킬 지정 → 자동으로 연결선 생성
//   "required_level": 5,  // 레벨 5 필요 → 자동으로 레벨 체크
//   "price": 10,
//   "icon": {...},
//   "type": "basic"
// }

public class PowerupItemWidget extends CustomButtonWidget implements ISkillTreeItemWidget {

    private static final float TEXT_SCALE = 0.65f;
    private static final int BASE_LINE_HEIGHT = 9;

    private final ISkillTreeItem skillTreeItem;
    private final PowerupsScreenState state;
    private final Powerup powerup;

    public PowerupItemWidget(ISkillTreeItem skillTreeItem, PowerupsScreenState state, Powerup powerup) {
        super(0, 0, 24, 24, powerup != null ? powerup.getPowerupInstance().getName() : state.getJob().getJobInstance().getName(), null, btn -> {
            if (btn instanceof PowerupItemWidget button && button.isActive()) {
                Powerup powerUp = button.getPowerup();
                PowerupInstance powerupInstance = powerUp.getPowerupInstance();
                Identifier location = button.getState().getJob().getJobInstance().getLocation();
                if (powerUp.getState() == PowerupState.ACTIVE || powerUp.getState() == PowerupState.INACTIVE) {
                    NetworkManager.sendToServer(new ServerboundTogglePowerUpPacket(location, powerupInstance.getLocation()));
                    if (Minecraft.getInstance().gui.screen() instanceof PowerupsScreen powerupsScreen) {
                        button.getPowerup().setState(powerUp.getState() == PowerupState.ACTIVE ? PowerupState.INACTIVE : PowerupState.ACTIVE);
                    }
                } else if (powerUp.getState() == PowerupState.NOT_OWNED) {
                    Minecraft.getInstance().gui.setScreen(new ConfirmationScreen(Minecraft.getInstance().gui.screen(), new ConfirmationScreenState(JobsPlus.translatable("gui.confirmation.purchase_powerup", powerupInstance.getName(), powerupInstance.getPrice()), () -> {
                        NetworkManager.sendToServer(new ServerboundStartPowerupPacket(location, powerupInstance.getLocation()));
                        NetworkManager.sendToServer(new ServerboundOpenPowerupsScreenPacket(location));
                    })));
                }
            }
        });
        this.skillTreeItem = skillTreeItem;
        this.state = state;
        this.powerup = powerup;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.blitSlot(guiGraphics);
    }

    public PowerupsScreenState getState() {
        return state;
    }

    public Powerup getPowerup() {
        return powerup;
    }

    private Identifier getSprite() {
        Identifier defaultSprite = JobsPlus.getId("powerups/slot_active");
        Identifier lockedSprite = JobsPlus.getId("powerups/slot_locked");
        Identifier notOwnedSprite = JobsPlus.getId("powerups/slot_not_owned");
        if (this.powerup == null) {
            Job job = state.getJob();
            if (job.getLevel() > 0) {
                return defaultSprite;
            }
            if (state.getCoins() >= job.getJobInstance().getPrice()) {
                return notOwnedSprite;
            }
            return lockedSprite;
        }
        if (!hasPowerup() && (!hasEnoughCoins() || !hasRequiredLevel())) {
            return lockedSprite;
        }
        return switch (this.powerup.getState()) {
            case ACTIVE -> defaultSprite;
            case INACTIVE -> JobsPlus.getId("powerups/slot_inactive");
            case NOT_OWNED -> notOwnedSprite;
            case LOCKED -> lockedSprite;
        };
    }

    private void blitSlot(GuiGraphicsExtractor guiGraphics) {
        JobsTheme.sprite(guiGraphics, this.getSprite(), this.getX(), this.getY(), this.getWidth(), this.getHeight());

        ItemStack icon;
        String countText = null;
        if (this.powerup != null) {
            PowerupInstance powerupInstance = this.powerup.getPowerupInstance();
            icon = powerupInstance.getIcon();
            if (powerupInstance.getIconCount() > 1) {
                countText = Integer.toString(powerupInstance.getIconCount());
            }
        } else {
            icon = state.getJob().getJobInstance().getIconItem();
        }
        guiGraphics.fakeItem(icon, this.getX() + 4, this.getY() + 4);
        guiGraphics.itemDecorations(Minecraft.getInstance().font, icon, this.getX() + 4, this.getY() + 4, countText);
    }

    @Override
    public void extractTooltips(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        // 레벨 조건과 상관없이 마우스 오버 시 항상 툴팁 표시
        if (this.isMouseOver(mouseX, mouseY)) {
            Component title = this.powerup != null ? this.powerup.getPowerupInstance().getName() : state.getJob().getJobInstance().getName();
            Component description = this.powerup != null ? this.powerup.getPowerupInstance().getDescription() : state.getJob().getJobInstance().getDescription();

            // 제목 최소 폭
            int titleWidth = Math.max(25, getScaledTextWidth(title));

            // ① 설명 부분 가로폭만 넓힘
            //    기존: titleWidth + getWidth() + 10
            //    → 직업/스킬 설명이 너무 세로로 길어져서, 여유 있게 +60 정도 여유를 준다.
            int descriptionWidth = Math.min(guiGraphics.guiWidth() - 30, Math.min(210, titleWidth + getWidth() + 60));

            // ② 툴팁 전체 박스 폭 (좌우 여백 14px 유지)
            int tooltipWidth = descriptionWidth + 14;

            int descriptionWrapWidth = Math.max(1, (int) Math.ceil(descriptionWidth / TEXT_SCALE));
            ScaledMultiLineTextComponent descriptionComponent =
                    new ScaledMultiLineTextComponent(0, 0, descriptionWrapWidth, description, JobsTheme.TEXT);

            int rightBgX = this.getX() - 6;
            int leftBgX = this.getX() + this.getWidth() + 6 - tooltipWidth;

            // 파워업 레벨 요구치/가격은 둘 다 브랜치에서 공통 사용
            int requiredLevel = this.powerup != null ? this.powerup.getPowerupInstance().getRequiredLevel() : 0;
            int extraHeight = (this.powerup != null && requiredLevel > 0 ? 25 : 13);
            int tooltipHeight = 20 + descriptionComponent.getScaledHeight() + 6 + extraHeight;
            int tooltipY = this.getY();
            int skillTreeBottom = guiGraphics.guiHeight() - 8;

            if (tooltipY + 7 + tooltipHeight > skillTreeBottom) {
                tooltipY = this.getY() - tooltipHeight - 10;
            }

            tooltipY = Math.max(0, Math.min(tooltipY, guiGraphics.guiHeight() - tooltipHeight - 10));
            rightBgX = Math.max(0, Math.min(rightBgX, guiGraphics.guiWidth() - tooltipWidth));

            if (leftBgX >= 0) {
                // ───── 왼쪽 공간이 확보되면 스킬 왼쪽으로 툴팁 표시
                // 배경 및 상단 바
                JobsTheme.sprite(guiGraphics, JobsPlus.getId("powerups/text_background"),
                        leftBgX,
                        tooltipY + 7,
                        tooltipWidth,
                        tooltipHeight);

                JobsTheme.sprite(guiGraphics, JobsPlus.getId("powerups/bar"),
                        leftBgX,
                        tooltipY + 3,
                        tooltipWidth,
                        20);

                // 제목
                drawScaledString(guiGraphics, title, leftBgX + 12, tooltipY + 9);

                if (this.powerup != null) {
                    // 구분선
                    JobsTheme.sprite(guiGraphics, JobsPlus.getId("powerups/line"),
                            leftBgX + 6,
                            tooltipY + 29 + descriptionComponent.getScaledHeight() + 1,
                            30,
                            1
                    );

                    if (requiredLevel > 0) {
                        drawScaledString(
                                guiGraphics,
                                JobsPlus.translatable("gui.powerups.required_level", requiredLevel),
                                leftBgX + 7,
                                tooltipY + 29 + descriptionComponent.getScaledHeight() + 4
                        );
                    }

                    MutableComponent price = JobsPlus.translatable("gui.powerups.price", this.powerup.getPowerupInstance().getPrice());

                    drawScaledString(
                            guiGraphics,
                            price,
                            leftBgX + 7,
                            tooltipY + 29 + descriptionComponent.getScaledHeight() + (requiredLevel > 0 ? 15 : 4)
                    );

                    JobsTheme.sprite(guiGraphics, JobsPlus.getId("jobs/coins"),
                            leftBgX + 7 + getScaledTextWidth(price) + 2,
                            tooltipY + 29 + descriptionComponent.getScaledHeight() + (requiredLevel > 0 ? 15 : 4),
                            7,
                            8
                    );
                }

                // 설명 텍스트 위치 (배경 기준 +7px)
                descriptionComponent.setX(leftBgX + 7);
                descriptionComponent.setY(tooltipY + 29);
                descriptionComponent.extractRenderStateBase(guiGraphics, mouseX, mouseY, 0, 0, 0);

            } else {
                // ───── 오른쪽으로 툴팁 표시 (기존 위치 유지하되 폭만 확대)
                JobsTheme.sprite(guiGraphics, JobsPlus.getId("powerups/text_background"),
                        rightBgX,
                        tooltipY + 7,
                        tooltipWidth,
                        tooltipHeight
                );

                JobsTheme.sprite(guiGraphics, JobsPlus.getId("powerups/bar"),
                        rightBgX,
                        tooltipY + 3,
                        tooltipWidth,
                        20
                );

                // 제목 (기존: this.getX() + this.getWidth() + 8)
                drawScaledString(
                        guiGraphics,
                        title,
                        rightBgX + 12,
                        tooltipY + 9
                );

                if (this.powerup != null) {
                    // 구분선
                    JobsTheme.sprite(guiGraphics, JobsPlus.getId("powerups/line"),
                            rightBgX + 6,
                            tooltipY + 29 + descriptionComponent.getScaledHeight() + 1,
                            30,
                            1
                    );

                    if (requiredLevel > 0) {
                        drawScaledString(
                                guiGraphics,
                                JobsPlus.translatable("gui.powerups.required_level", requiredLevel),
                                rightBgX + 7,
                                tooltipY + 29 + descriptionComponent.getScaledHeight() + 4
                        );
                    }

                    MutableComponent price = JobsPlus.translatable("gui.powerups.price", this.powerup.getPowerupInstance().getPrice());

                    drawScaledString(
                            guiGraphics,
                            price,
                            rightBgX + 7,
                            tooltipY + 29 + descriptionComponent.getScaledHeight() + (requiredLevel > 0 ? 15 : 4)
                    );

                    JobsTheme.sprite(guiGraphics, JobsPlus.getId("jobs/coins"),
                            rightBgX + 7 + getScaledTextWidth(price) + 2,
                            tooltipY + 29 + descriptionComponent.getScaledHeight() + (requiredLevel > 0 ? 15 : 4),
                            7,
                            8
                    );
                }

                // 설명 텍스트 위치 (좌우 배치 모두 배경 기준 +7px)
                descriptionComponent.setX(rightBgX + 7);
                descriptionComponent.setY(tooltipY + 29);
                descriptionComponent.extractRenderStateBase(guiGraphics, mouseX, mouseY, 0, 0, 0);
            }

            this.blitSlot(guiGraphics);
        }
    }

    private int getScaledTextWidth(Component text) {
        return (int) Math.ceil(Minecraft.getInstance().font.width(text) * TEXT_SCALE);
    }

    private void drawScaledString(GuiGraphicsExtractor guiGraphics, Component text, int x, int y) {
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(x, y);
        guiGraphics.pose().scale(TEXT_SCALE, TEXT_SCALE);
        guiGraphics.text(Minecraft.getInstance().font, text, 0, 0, JobsTheme.TEXT, false);
        guiGraphics.pose().popMatrix();
    }

    private static final class ScaledMultiLineTextComponent extends MultiLineTextComponent {

        // 축소 배율(0.5)이 적용되므로 화면상 실제 행간은 절반이 된다.
        private static final int LINE_SPACING = 2;

        public ScaledMultiLineTextComponent(int x, int y, int maxWidth, Component text, int color) {
            super(x, y, maxWidth, text, color);
        }

        public int getScaledHeight() {
            int rawHeight = getLines().size() * BASE_LINE_HEIGHT
                    + Math.max(0, getLines().size() - 1) * LINE_SPACING;
            return (int) Math.ceil(rawHeight * TEXT_SCALE);
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick, int parentWidth,
                           int parentHeight) {
            guiGraphics.pose().pushMatrix();
            guiGraphics.pose().translate(getTotalX(), getTotalY());
            guiGraphics.pose().scale(TEXT_SCALE, TEXT_SCALE);

            for (int i = 0; i < getLines().size(); i++) {
                guiGraphics.text(
                        getFont(),
                        getLines().get(i),
                        0,
                        i * (BASE_LINE_HEIGHT + LINE_SPACING),
                        getColor(),
                        isDrawShadow()
                );
            }

            guiGraphics.pose().popMatrix();
        }
    }

    @Override
    public ISkillTreeItem getSkillTreeItem() {
        return this.skillTreeItem;
    }

    @Override
    protected boolean isValidClickButton(MouseButtonInfo mouseButtonInfo) {
        if (hasPowerup())
            return true;

        boolean isCorrectPowerupState = this.powerup != null && this.powerup.getState() != PowerupState.LOCKED;
        return isCorrectPowerupState && hasEnoughCoins() && hasRequiredLevel();
    }

    @Override
    public boolean isActive() {
        return isValidClickButton(new MouseButtonInfo(0, 0));
    }

    private boolean hasEnoughCoins() {
        return this.powerup != null && this.state.getCoins() >= this.powerup.getPowerupInstance().getPrice();
    }

    private boolean hasRequiredLevel() {
        return this.powerup != null && this.state.getJob().getLevel() >= 1 && this.state.getJob().getLevel() >= this.powerup.getPowerupInstance().getRequiredLevel();
    }

    private boolean hasPowerup() {
        return this.powerup != null && (this.powerup.getState() == PowerupState.ACTIVE || this.powerup.getState() == PowerupState.INACTIVE);
    }

    // 25.12.06 jjh 신규 매서드 추가 (toolTip 항상 표시, UILib 수정 불가해서 추가)
    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        // 스킬이 잠겨 있든, 활성화 되었든
        // 화면에 보이는 슬롯 영역 안에 마우스가 들어오면 항상 true
        return mouseX >= this.getX()
                && mouseX <= this.getX() + this.getWidth()
                && mouseY >= this.getY()
                && mouseY <= this.getY() + this.getHeight();
    }
}
