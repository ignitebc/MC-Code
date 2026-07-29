package com.daqem.jobsplus.client.gui.jobs;

import com.daqem.jobsplus.client.gui.jobs.tab.RightTab;
import com.daqem.jobsplus.config.JobsPlusConfig;
import com.daqem.jobsplus.shop.ShopOffer;
import com.daqem.jobsplus.player.job.Job;
import com.daqem.jobsplus.player.stock.StockAccount;
import com.daqem.jobsplus.player.stock.StockPosition;
import com.daqem.jobsplus.player.stock.StockPositionSide;
import com.daqem.jobsplus.client.gui.jobs.stock.StockPanelMode;
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

    private @Nullable ShopOffer selectedShopOffer;
    private StockAccount stockAccount;
    private String selectedStockId;
    private @Nullable String selectedHoldingStockId;
    private StockPanelMode stockPanelMode;
    private StockPositionSide selectedStockPositionSide;
    private int selectedStockLeverage;

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
        this(jobs, coins, maxJobs, selectedJob, selectedRightTab, StockAccount.EMPTY);
    }

    public JobsScreenState(List<Job> jobs, int coins, int maxJobs, Job selectedJob, RightTab selectedRightTab,
                           StockAccount stockAccount) {
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
        this.selectedShopOffer = null;
        this.stockAccount = stockAccount;
        this.selectedStockId = "AAPL";
        this.selectedHoldingStockId = null;
        this.stockPanelMode = StockPanelMode.BUY;
        this.selectedStockPositionSide = StockPositionSide.LONG;
        this.selectedStockLeverage = StockPosition.DEFAULT_LEVERAGE;
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

    public @Nullable ShopOffer getSelectedShopOffer() {
        return selectedShopOffer;
    }

    public void setSelectedShopOffer(@Nullable ShopOffer selectedShopOffer) {
        this.selectedShopOffer = selectedShopOffer;
    }

    public StockAccount getStockAccount() {
        return stockAccount;
    }

    public void setStockAccount(StockAccount stockAccount) {
        this.stockAccount = stockAccount;
    }

    public String getSelectedStockId() {
        return selectedStockId;
    }

    public void setSelectedStockId(String selectedStockId) {
        this.selectedStockId = selectedStockId;
    }

    public @Nullable String getSelectedHoldingStockId() {
        return selectedHoldingStockId;
    }

    public void setSelectedHoldingStockId(@Nullable String selectedHoldingStockId) {
        this.selectedHoldingStockId = selectedHoldingStockId;
    }

    public StockPanelMode getStockPanelMode() {
        return stockPanelMode;
    }

    public void setStockPanelMode(StockPanelMode stockPanelMode) {
        this.stockPanelMode = stockPanelMode;
    }

    public StockPositionSide getSelectedStockPositionSide() {
        return selectedStockPositionSide;
    }

    public void setSelectedStockPositionSide(StockPositionSide selectedStockPositionSide) {
        this.selectedStockPositionSide = selectedStockPositionSide;
    }

    public int getSelectedStockLeverage() {
        return selectedStockLeverage;
    }

    public void setSelectedStockLeverage(int selectedStockLeverage) {
        this.selectedStockLeverage = StockPosition.normalizeLeverage(selectedStockLeverage);
    }

    public int getActiveJobCount() {
        return (int) jobs.stream().filter(job -> job.getLevel() > 0).count();
    }
}
