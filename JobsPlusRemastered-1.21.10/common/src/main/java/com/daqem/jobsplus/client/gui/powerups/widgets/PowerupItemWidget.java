package com.daqem.jobsplus.client.gui.powerups.widgets;

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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
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

    private final ISkillTreeItem skillTreeItem;
    private final PowerupsScreenState state;
    private final Powerup powerup;

    public PowerupItemWidget(ISkillTreeItem skillTreeItem, PowerupsScreenState state, Powerup powerup) {
        super(0, 0, 26, 26, powerup != null ? powerup.getPowerupInstance().getName() : state.getJob().getJobInstance().getName(), null, btn -> {
            if (btn instanceof PowerupItemWidget button && button.isActive()) {
                Powerup powerUp = button.getPowerup();
                PowerupInstance powerupInstance = powerUp.getPowerupInstance();
                ResourceLocation location = button.getState().getJob().getJobInstance().getLocation();
                if (powerUp.getState() == PowerupState.ACTIVE || powerUp.getState() == PowerupState.INACTIVE) {
                    NetworkManager.sendToServer(new ServerboundTogglePowerUpPacket(location, powerupInstance.getLocation()));
                    if (Minecraft.getInstance().screen instanceof PowerupsScreen powerupsScreen) {
                        button.getPowerup().setState(powerUp.getState() == PowerupState.ACTIVE ? PowerupState.INACTIVE : PowerupState.ACTIVE);
                    }
                } else if (powerUp.getState() == PowerupState.NOT_OWNED) {
                    Minecraft.getInstance().setScreen(new ConfirmationScreen(Minecraft.getInstance().screen, new ConfirmationScreenState(JobsPlus.translatable("gui.confirmation.purchase_powerup", powerupInstance.getName(), powerupInstance.getPrice()), () -> {
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
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.blitSlot(guiGraphics);
    }

    public PowerupsScreenState getState() {
        return state;
    }

    public Powerup getPowerup() {
        return powerup;
    }

    private ResourceLocation getSprite() {
        ResourceLocation defaultSprite = JobsPlus.getId("powerups/slot_active");
        ResourceLocation lockedSprite = JobsPlus.getId("powerups/slot_locked");
        ResourceLocation notOwnedSprite = JobsPlus.getId("powerups/slot_not_owned");
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

    private void blitSlot(GuiGraphics guiGraphics) {
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, this.getSprite(), this.getX(), this.getY(), this.getWidth(), this.getHeight());

        ItemStack icon = this.powerup != null ? this.powerup.getPowerupInstance().getIcon() : state.getJob().getJobInstance().getIconItem();
        guiGraphics.renderFakeItem(icon, this.getX() + 5, this.getY() + 5);
        guiGraphics.renderItemDecorations(Minecraft.getInstance().font, icon, this.getX() + 5, this.getY() + 5);
    }

    @Override
    public void renderTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // 레벨 조건과 상관없이 마우스 오버 시 항상 툴팁 표시
        if (this.isMouseOver(mouseX, mouseY)) {
            Component title = this.powerup != null ? this.powerup.getPowerupInstance().getName() : state.getJob().getJobInstance().getName();
            Component description = this.powerup != null ? this.powerup.getPowerupInstance().getDescription() : state.getJob().getJobInstance().getDescription();

            // 제목 최소 폭
            int titleWidth = Math.max(50, Minecraft.getInstance().font.width(title));

            // ① 설명 부분 가로폭만 넓힘
            //    기존: titleWidth + getWidth() + 10
            //    → 직업/스킬 설명이 너무 세로로 길어져서, 여유 있게 +60 정도 여유를 준다.
            int descriptionWidth = titleWidth + getWidth() + 60;

            // ② 툴팁 전체 박스 폭 (좌우 여백 14px 유지)
            int tooltipWidth = descriptionWidth + 14;

            MultiLineTextComponent descriptionComponent =
                    new MultiLineTextComponent(0, 0, descriptionWidth, description, 0xFF1E1410);

            // 오른쪽으로 툴팁을 띄웠을 때 화면 밖으로 나가는지 체크
            int rightBgX = this.getX() - 6;
            int guiWidth = guiGraphics.guiWidth();

            // 파워업 레벨 요구치/가격은 둘 다 브랜치에서 공통 사용
            int requiredLevel = this.powerup != null ? this.powerup.getPowerupInstance().getRequiredLevel() : 0;
            int extraHeight = (this.powerup != null && requiredLevel > 0 ? 25 : 13);

            if (rightBgX + tooltipWidth > guiWidth) {
                // ───── 왼쪽으로 툴팁 표시 (슬롯 기준 오른쪽 끝 + 6px 위치에 맞춰서 정렬)
                int leftBgX = this.getX() + this.getWidth() + 6 - tooltipWidth;

                // 배경 및 상단 바
                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                        JobsPlus.getId("powerups/text_background"),
                        leftBgX,
                        this.getY() + 7,
                        tooltipWidth,
                        20 + descriptionComponent.getHeight() + 6 + extraHeight);

                guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED,
                        JobsPlus.getId("powerups/bar"),
                        leftBgX,
                        this.getY() + 3,
                        tooltipWidth,
                        20);

                // 제목
                guiGraphics.drawString(
                        Minecraft.getInstance().font,
                        title,
                        leftBgX + 12, // 기존 오프셋(배경 기준 +12) 유지
                        this.getY() + 9,
                        0xFF1E1410,
                        false
                );

                if (this.powerup != null) {
                    // 구분선
                    guiGraphics.blitSprite(
                            RenderPipelines.GUI_TEXTURED,
                            JobsPlus.getId("powerups/line"),
                            leftBgX + 6,
                            this.getY() + 29 + descriptionComponent.getHeight() + 1,
                            30,
                            1
                    );

                    if (requiredLevel > 0) {
                        guiGraphics.drawString(
                                Minecraft.getInstance().font,
                                JobsPlus.translatable("gui.powerups.required_level", requiredLevel),
                                leftBgX + 7,
                                this.getY() + 29 + descriptionComponent.getHeight() + 4,
                                0xFF1E1410,
                                false
                        );
                    }

                    MutableComponent price = JobsPlus.translatable("gui.powerups.price", this.powerup.getPowerupInstance().getPrice());

                    guiGraphics.drawString(
                            Minecraft.getInstance().font,
                            price,
                            leftBgX + 7,
                            this.getY() + 29 + descriptionComponent.getHeight() + (requiredLevel > 0 ? 15 : 4),
                            0xFF1E1410,
                            false
                    );

                    guiGraphics.blitSprite(
                            RenderPipelines.GUI_TEXTURED,
                            JobsPlus.getId("jobs/coins"),
                            leftBgX + 7 + Minecraft.getInstance().font.width(price) + 2,
                            this.getY() + 29 + descriptionComponent.getHeight() + (requiredLevel > 0 ? 15 : 4),
                            7,
                            8
                    );
                }

                // 설명 텍스트 위치 (배경 기준 +7px)
                descriptionComponent.setX(leftBgX + 7);
                descriptionComponent.setY(this.getY() + 29);
                descriptionComponent.renderBase(guiGraphics, mouseX, mouseY, 0, 0, 0);

            } else {
                // ───── 오른쪽으로 툴팁 표시 (기존 위치 유지하되 폭만 확대)
                guiGraphics.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        JobsPlus.getId("powerups/text_background"),
                        rightBgX,
                        this.getY() + 7,
                        tooltipWidth,
                        20 + descriptionComponent.getHeight() + 6 + extraHeight
                );

                guiGraphics.blitSprite(
                        RenderPipelines.GUI_TEXTURED,
                        JobsPlus.getId("powerups/bar"),
                        rightBgX,
                        this.getY() + 3,
                        tooltipWidth,
                        20
                );

                // 제목 (기존: this.getX() + this.getWidth() + 8)
                guiGraphics.drawString(
                        Minecraft.getInstance().font,
                        title,
                        rightBgX + this.getWidth() + 14, // rightBgX(-6) 기준 + (슬롯폭+14) = 기존과 같은 비율
                        this.getY() + 9,
                        0xFF1E1410,
                        false
                );

                if (this.powerup != null) {
                    // 구분선
                    guiGraphics.blitSprite(
                            RenderPipelines.GUI_TEXTURED,
                            JobsPlus.getId("powerups/line"),
                            rightBgX + 6,
                            this.getY() + 29 + descriptionComponent.getHeight() + 1,
                            30,
                            1
                    );

                    if (requiredLevel > 0) {
                        guiGraphics.drawString(
                                Minecraft.getInstance().font,
                                JobsPlus.translatable("gui.powerups.required_level", requiredLevel),
                                rightBgX + 7,
                                this.getY() + 29 + descriptionComponent.getHeight() + 4,
                                0xFF1E1410,
                                false
                        );
                    }

                    MutableComponent price = JobsPlus.translatable("gui.powerups.price", this.powerup.getPowerupInstance().getPrice());

                    guiGraphics.drawString(
                            Minecraft.getInstance().font,
                            price,
                            rightBgX + 7,
                            this.getY() + 29 + descriptionComponent.getHeight() + (requiredLevel > 0 ? 15 : 4),
                            0xFF1E1410,
                            false
                    );

                    guiGraphics.blitSprite(
                            RenderPipelines.GUI_TEXTURED,
                            JobsPlus.getId("jobs/coins"),
                            rightBgX + 7 + Minecraft.getInstance().font.width(price) + 2,
                            this.getY() + 29 + descriptionComponent.getHeight() + (requiredLevel > 0 ? 15 : 4),
                            7,
                            8
                    );
                }

                // 설명 텍스트 위치 (배경 기준 +1px, 기존과 동일한 상대 위치)
                descriptionComponent.setX(rightBgX + 1);
                descriptionComponent.setY(this.getY() + 29);
                descriptionComponent.renderBase(guiGraphics, mouseX, mouseY, 0, 0, 0);
            }

            this.blitSlot(guiGraphics);
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
