package com.daqem.jobsplus.client.gui.jobs;

import com.daqem.arc.api.action.IAction;
import com.daqem.jobsplus.client.gui.jobs.tab.RightTab;
import com.daqem.jobsplus.config.JobsPlusConfig;
import com.daqem.jobsplus.shop.ShopOffer;
import com.daqem.jobsplus.player.job.Job;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public class JobsScreenState {
    private final List<Job> jobs;
    private final List<Job> preformingJobs;
    private final List<Job> notPreformingJobs;
    private int coins;

    /**
     * 서버에서 전달받는 "유효 최대 직업 수"
     * (전역 기본 + 플레이어 추가 슬롯)
     */
    private final int maxJobs;

    private Job selectedJob;
    private RightTab selectedRightTab;
    private @Nullable IAction activeAction;

    private @Nullable ShopOffer selectedShopOffer;

    // 호환: 기존 시그니처 유지(서버가 maxJobs를 보내지 않는 경우)
    public JobsScreenState(List<Job> jobs, int coins) {
        this(jobs, coins, JobsPlusConfig.maxJobs.get(), null, RightTab.EXPERIENCE);
    }

    public JobsScreenState(List<Job> jobs, int coins, Job selectedJob, RightTab selectedRightTab) {
        this(jobs, coins, JobsPlusConfig.maxJobs.get(), selectedJob, selectedRightTab);
    }

    // 신규: maxJobs 포함
    public JobsScreenState(List<Job> jobs, int coins, int maxJobs) {
        this(jobs, coins, maxJobs, null, RightTab.EXPERIENCE);
    }

    public JobsScreenState(List<Job> jobs, int coins, int maxJobs, Job selectedJob, RightTab selectedRightTab) {
        this.jobs = jobs.stream()
                .sorted(Comparator.comparing(Job::getLevel).reversed()
                        .thenComparingInt(job -> -job.getExperience())
                        .thenComparing(job -> job.getJobInstance().getName().getString()))
                .toList();
        this.preformingJobs = this.jobs.stream().filter(job -> job.getLevel() > 0).toList();
        this.notPreformingJobs = this.jobs.stream().filter(job -> job.getLevel() <= 0).toList();
        this.coins = coins;
        this.maxJobs = Math.max(0, maxJobs);

        this.selectedJob = selectedJob != null ? selectedJob : (this.jobs.isEmpty() ? null : this.jobs.getFirst());
        this.selectedRightTab = selectedRightTab;
        this.activeAction = null;
        this.selectedShopOffer = null;
    }

    public List<Job> getJobs() {
        return jobs;
    }

    public List<Job> getPreformingJobs() {
        return preformingJobs;
    }

    public List<Job> getNotPreformingJobs() {
        return notPreformingJobs;
    }

    public int getCoins() {
        return coins;
    }

    public int getMaxJobs() {
        return maxJobs;
    }

    public Job getSelectedJob() {
        return selectedJob;
    }

    public RightTab getSelectedRightTab() {
        return selectedRightTab;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public void setSelectedJob(Job selectedJob) {
        this.selectedJob = selectedJob;
    }

    public void setSelectedRightTab(RightTab selectedRightTab) {
        this.selectedRightTab = selectedRightTab;
    }

    public @Nullable IAction getActiveAction() {
        return activeAction;
    }

    public void setActiveAction(@Nullable IAction activeAction) {
        this.activeAction = activeAction;
    }

    public @Nullable ShopOffer getSelectedShopOffer() {
        return selectedShopOffer;
    }

    public void setSelectedShopOffer(@Nullable ShopOffer selectedShopOffer) {
        this.selectedShopOffer = selectedShopOffer;
    }

    public int getActiveJobCount() {
        return (int) jobs.stream().filter(job -> job.getLevel() > 0).count();
    }
}
