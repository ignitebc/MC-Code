package com.daqem.jobsplus.client.gui.jobs.widgets;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.confimation.ConfirmationScreen;
import com.daqem.jobsplus.client.gui.confimation.ConfirmationScreenState;
import com.daqem.jobsplus.client.gui.confimation.PendingJobSelectionAlert;
import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.integration.arc.holder.holders.job.JobInstance;
import com.daqem.jobsplus.networking.c2s.ServerboundStartJobPacket;
import com.daqem.jobsplus.player.job.Job;
import com.daqem.jobsplus.util.KoreanJosa;
import com.daqem.uilib.gui.widget.CustomButtonWidget;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

public class StartJobButtonWidget extends CustomButtonWidget {
    private static final Component MESSAGE = JobsPlus.translatable("gui.jobs.start_job");

    private final JobsScreenState state;

    public StartJobButtonWidget(JobsScreenState state) {
        super(31, 192, Minecraft.getInstance().font.width(MESSAGE) + 20, 18, MESSAGE, null,
                button -> {
                    Job selectedJob = state.getSelectedJob();
                    if (selectedJob == null) {
                        return;
                    }

                    int activeJobCount = state.getActiveJobCount();

                    // maxJobs는 서버가 내려준 "유효 최대 직업 수" (무료 2 + 티켓 누적, 단 상한 8)
                    if (activeJobCount >= state.getMaxJobs()) {
                        Minecraft.getInstance().gui.setScreen(
                                new ConfirmationScreen(
                                        Minecraft.getInstance().gui.screen(),
                                        new ConfirmationScreenState(
                                                JobsPlus.translatable("gui.jobs.max_jobs", state.getMaxJobs()),
                                                () -> {
                                                })));
                        return;
                    }

                    JobInstance jobInstance = selectedJob.getJobInstance();
                    String jobName = jobInstance.getName().getString();

                    Component confirmMessage = JobsPlus.translatable(
                            "gui.confirmation.select_job",
                            jobName,
                            KoreanJosa.eulReul(jobName));

                    if (selectedJob.getLevel() == 0) {
                        Minecraft.getInstance().gui.setScreen(
                                new ConfirmationScreen(
                                        Minecraft.getInstance().gui.screen(),
                                        new ConfirmationScreenState(
                                                confirmMessage,
                                                JobsPlus.translatable("gui.confirmation.select"),
                                                JobsPlus.translatable("gui.confirmation.cancel"),
                                                () -> {
                                                    // 완료 알림은 서버가 화면을 갱신한 뒤에 띄운다
                                                    // (여기서 바로 띄우면 갱신 패킷이 알림 화면을 덮어씀)
                                                    PendingJobSelectionAlert.set(jobName);
                                                    NetworkManager.sendToServer(
                                                            new ServerboundStartJobPacket(
                                                                    jobInstance.getLocation()));
                                                })));
                    }
                });

        this.state = state;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                JobsPlus.getId("jobs/tab_bottom"),
                this.getX(),
                this.getY(),
                this.getWidth(),
                this.getHeight(),
                ARGB.white(this.alpha));

        guiGraphics.text(
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
