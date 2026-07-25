package com.daqem.jobsplus.client.gui.jobs.widgets;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.confimation.ConfirmationScreen;
import com.daqem.jobsplus.client.gui.confimation.ConfirmationScreenState;
import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.integration.arc.holder.holders.job.JobInstance;
import com.daqem.jobsplus.networking.c2s.ServerboundOpenJobsScreenPacket;
import com.daqem.jobsplus.networking.c2s.ServerboundStartJobPacket;
import com.daqem.jobsplus.player.job.Job;
import com.daqem.uilib.gui.widget.CustomButtonWidget;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

public class StartJobButtonWidget extends CustomButtonWidget {
    private static final Component MESSAGE = JobsPlus.translatable("gui.jobs.start_job");

    private final JobsScreenState state;

    public StartJobButtonWidget(JobsScreenState state) {
        super(26, 168, Minecraft.getInstance().font.width(MESSAGE) + 20, 18, MESSAGE, null,
                button -> {
                    Job selectedJob = state.getSelectedJob();
                    if (selectedJob == null) {
                        return;
                    }

                    int activeJobCount = state.getActiveJobCount();

                    // maxJobs는 서버가 내려준 "유효 최대 직업 수" (무료 1 + 티켓 누적, 단 상한 7)
                    if (activeJobCount >= state.getMaxJobs()) {
                        Minecraft.getInstance().setScreen(
                                new ConfirmationScreen(
                                        Minecraft.getInstance().screen,
                                        new ConfirmationScreenState(
                                                JobsPlus.translatable("gui.jobs.max_jobs", state.getMaxJobs()),
                                                () -> {
                                                })));
                        return;
                    }

                    JobInstance jobInstance = selectedJob.getJobInstance();

                    // 본 서버 정책: 티켓으로 늘린 슬롯까지 "무료 선택"으로 취급
                    // -> 클라이언트 확인창도 유료/무료를 amount_of_free_jobs(=1)로 판단하면 안 됨
                    int jobAmount = activeJobCount;

                    Component freeJobMessage = JobsPlus.translatable(
                            "gui.confirmation.purchase_job.free",
                            jobInstance.getName());

                    Component paidJobMessage = JobsPlus.translatable(
                            "gui.confirmation.purchase_job.paid",
                            jobInstance.getName(),
                            jobInstance.getPrice());

                    if (selectedJob.getLevel() == 0) {
                        Minecraft.getInstance().setScreen(
                                new ConfirmationScreen(
                                        Minecraft.getInstance().screen,
                                        new ConfirmationScreenState(
                                                // 수정: 티켓 슬롯도 무료이므로, 유료 판단을 state.getMaxJobs() 기준으로
                                                jobAmount >= state.getMaxJobs()
                                                        ? paidJobMessage
                                                        : freeJobMessage,
                                                () -> {
                                                    NetworkManager.sendToServer(
                                                            new ServerboundStartJobPacket(
                                                                    selectedJob.getJobInstance().getLocation()));
                                                    NetworkManager.sendToServer(
                                                            new ServerboundOpenJobsScreenPacket());
                                                })));
                    }
                });

        this.state = state;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                JobsPlus.getId("jobs/tab_bottom"),
                this.getX(),
                this.getY(),
                this.getWidth(),
                this.getHeight(),
                ARGB.white(this.alpha));

        guiGraphics.drawString(
                Minecraft.getInstance().font,
                this.getMessage(),
                this.getX() + 10,
                this.getY() + 6,
                ARGB.color(
                        this.alpha,
                        isHoveredOrFocused()
                                ? this.state.getSelectedJob().getJobInstance().getColorDecimal()
                                : 0x1E1410),
                false);
    }
}
