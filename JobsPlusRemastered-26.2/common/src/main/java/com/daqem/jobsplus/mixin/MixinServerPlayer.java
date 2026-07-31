package com.daqem.jobsplus.mixin;

import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.arc.api.player.ArcPlayer;
import com.daqem.arc.api.player.ArcServerPlayer;
import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.integration.arc.holder.holders.job.JobInstance;
import com.daqem.jobsplus.integration.arc.holder.holders.job.JobManager;
import com.daqem.jobsplus.integration.arc.holder.holders.powerup.PowerupInstance;
import com.daqem.jobsplus.player.JobHealthSync;
import com.daqem.jobsplus.player.JobsServerPlayer;
import com.daqem.jobsplus.player.ServerPlayerData;
import com.daqem.jobsplus.player.job.Job;
import com.daqem.jobsplus.player.job.exp.ExpCollector;
import com.daqem.jobsplus.player.job.powerup.Powerup;
import com.daqem.jobsplus.player.job.powerup.PowerupState;
import com.daqem.jobsplus.player.stock.StockAccount;
import com.daqem.jobsplus.player.stock.StockPositionLedger;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer extends Player implements JobsServerPlayer {
    @Unique
    private List<Job> jobsplus$jobs = new ArrayList<>();
    @Unique
    private int jobsplus$coins = 0;
    @Unique
    private boolean jobsplus$deathItemProtected;

    /**
     * 직업추가권 등으로 얻는 추가 슬롯 (상한 없음)
     */
    @Unique
    private int jobsplus$extraJobSlots = 0;

    public MixinServerPlayer(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Override
    public List<Job> jobsplus$getJobs() {
        return jobsplus$jobs;
    }

    @Override
    public List<JobInstance> jobsplus$getJobInstances() {
        return jobsplus$jobs.stream().map(Job::getJobInstance).toList();
    }

    @Override
    public List<Job> jobsplus$getInactiveJobs() {
        return JobManager.getInstance().getJobs().values().stream()
                .filter(jobInstance -> !jobsplus$getJobInstances().contains(jobInstance))
                .map(jobInstance -> new Job(this, jobInstance))
                .toList();
    }

    @Override
    public @Nullable Job jobsplus$addNewJob(@NotNull JobInstance jobInstance) {
        if (jobInstance.getLocation() == null)
            return null;

        // 핵심: 전역 maxJobs가 아니라 "유효 최대 직업 수"로 비교
        if (jobsplus$jobs.size() >= jobsplus$getEffectiveMaxJobs()) {
            return null;
        }

        Job job = jobsplus$getJob(jobInstance);
        if (job == null) {
            job = new Job(this, jobInstance, 1, 0);
            jobsplus$jobs.add(job);
            jobsplus$updateJob(job);
            JobHealthSync.sync(this);
            return job;
        }
        return null;
    }

    @Override
    public void jobsplus$removeJob(JobInstance jobInstance) {
        Job job = jobsplus$getJob(jobInstance);
        if (job != null) {
            jobsplus$jobs.remove(job);
            jobsplus$removeActionHolders(job);
            JobHealthSync.sync(this);
        }
    }

    @Override
    public void jobsplus$removeActionHolders(Job job) {
        if (jobsplus$getServerPlayer() instanceof ArcPlayer arcPlayer) {
            arcPlayer.arc$removeActionHolder(job.getJobInstance());
            job.getPowerupManager().getAllPowerups()
                    .stream()
                    .map(Powerup::getPowerupInstance)
                    .filter(Objects::nonNull)
                    .forEach(arcPlayer::arc$removeActionHolder);
        }
    }

    @Override
    public @Nullable Job jobsplus$getJob(@Nullable JobInstance jobLocation) {
        if (jobLocation == null)
            return null;
        return this.jobsplus$jobs.stream()
                .filter(job -> job.getJobInstance().getLocation().equals(jobLocation.getLocation()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Job jobsplus$getJob(Identifier jobLocation) {
        return this.jobsplus$jobs.stream()
                .filter(job -> job.getJobInstance().getLocation().equals(jobLocation))
                .findFirst()
                .orElse(null);
    }

    @Override
    public @Nullable Powerup jobsplus$getPowerup(PowerupInstance powerupInstance) {
        return jobsplus$getJobs().stream()
                .map(Job::getPowerupManager)
                .flatMap(powerupManager -> powerupManager.getAllPowerups().stream())
                .filter(powerup -> powerup.getPowerupLocation().equals(powerupInstance.getLocation()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public int jobsplus$getCoins() {
        return jobsplus$coins;
    }

    @Override
    public void jobsplus$addCoins(int coins) {
        this.jobsplus$setCoins(Mth.clamp(this.jobsplus$coins + coins, 0, Integer.MAX_VALUE));
    }

    @Override
    public void jobsplus$setCoins(int coins) {
        this.jobsplus$coins = coins;
    }

    /**
     * 주식 계좌는 예약 주문과 같은 월드 저장 데이터({@link StockPositionLedger})에 보관한다.
     * 계좌 차감과 예약 기록이 하나의 저장 객체에 함께 담겨, 강제 종료 시 한쪽만 저장되는 일이 없다.
     */
    @Override
    public StockAccount jobsplus$getStockAccount() {
        MinecraftServer server = this.level().getServer();
        if (server == null) {
            return StockAccount.EMPTY;
        }
        return StockPositionLedger.get(server).getAccount(this.getUUID());
    }

    @Override
    public void jobsplus$setStockAccount(StockAccount stockAccount) {
        MinecraftServer server = this.level().getServer();
        if (server == null) {
            return;
        }
        StockPositionLedger.get(server).setAccount(this.getUUID(), stockAccount);
    }

    @Override
    public int jobsplus$getExtraJobSlots() 
    {
        return jobsplus$extraJobSlots;
    }

    @Override
    public void jobsplus$addExtraJobSlots(int delta) 
    {
        long next = (long) jobsplus$extraJobSlots + (long) delta;
        if (next < 0) 
        {
            next = 0;
        }

        int base = Math.max(0, com.daqem.jobsplus.config.JobsPlusConfig.amountOfFreeJobs.get());
        int cap = Math.max(0, com.daqem.jobsplus.config.JobsPlusConfig.maxJobs.get());
        int extraCap = Math.max(0, cap - base);

        if (next > (long) extraCap) {
            next = extraCap;
        }

        jobsplus$extraJobSlots = (int) Math.min(next, (long) Integer.MAX_VALUE);
    }

    @Override
    public boolean jobsplus$isDeathItemProtected()
    {
        return this.jobsplus$deathItemProtected;
    }

    @Override
    public void jobsplus$setDeathItemProtected(boolean deathItemProtected)
    {
        this.jobsplus$deathItemProtected = deathItemProtected;
    }

    @Override
    public List<IActionHolder> jobsplus$getActionHolders() {
        List<IActionHolder> actionHolders = new ArrayList<>(jobsplus$getJobInstances());
        actionHolders.addAll(
                jobsplus$getJobs().stream()
                        .map(Job::getPowerupManager)
                        .flatMap(powerupManager -> powerupManager.getAllPowerups().stream()
                                .filter(powerup -> powerup.getState() == PowerupState.ACTIVE))
                        .map(Powerup::getPowerupInstance)
                        .filter(Objects::nonNull)
                        .toList());
        return actionHolders;
    }

    @Override
    public ServerPlayer jobsplus$getServerPlayer() {
        // noinspection DataFlowIssue
        return (ServerPlayer) (Object) this;
    }

    @Override
    public String jobsplus$getName() {
        return super.getName().getString();
    }

    @Override
    public void jobsplus$updateJob(Job job) {
        this.jobsplus$updateActionHolders(job);
    }

    @Override
    public void jobsplus$updateActionHolders(Job job)
    {
        if (jobsplus$getServerPlayer() instanceof ArcPlayer arcPlayer)
        {
            arcPlayer.arc$removeActionHolder(job.getJobInstance());
            job.getPowerupManager().getAllPowerups().stream()
                    .map(Powerup::getPowerupInstance)
                    .filter(Objects::nonNull)
                    .forEach(arcPlayer::arc$removeActionHolder);
            arcPlayer.arc$addActionHolder(job.getJobInstance());
            job.getPowerupManager().getAllPowerups().stream()
                    .filter(powerup -> powerup.getState() == PowerupState.ACTIVE)
                    .map(Powerup::getPowerupInstance)
                    .filter(Objects::nonNull)
                    .forEach(arcPlayer::arc$addActionHolder);
        }

        // ---- 추가: 클라이언트에도 활성 홀더 목록 동기화 ----
        syncActionHoldersToClient();
    }
    
    private void syncActionHoldersToClient()
    {
        ServerPlayer self = jobsplus$getServerPlayer();
        // 활성 홀더들의 location만 전달
        java.util.List<net.minecraft.resources.Identifier> ids = jobsplus$getActionHolders().stream().map(com.daqem.arc.api.action.holder.IActionHolder::getLocation).toList();

        dev.architectury.networking.NetworkManager.sendToPlayer(self, new com.daqem.jobsplus.networking.s2c.ClientboundSyncActionHoldersPacket(ids));
    }
    
    @Override
    public Player jobsplus$getPlayer() {
        return jobsplus$getServerPlayer();
    }

    @Inject(at = @At("TAIL"), method = "restoreFrom(Lnet/minecraft/server/level/ServerPlayer;Z)V")
    public void restoreFrom(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
        if (oldPlayer instanceof JobsServerPlayer oldJobsServerPlayer) {
            this.jobsplus$jobs = oldJobsServerPlayer.jobsplus$getJobs();
            this.jobsplus$coins = oldJobsServerPlayer.jobsplus$getCoins();
            this.jobsplus$extraJobSlots = oldJobsServerPlayer.jobsplus$getExtraJobSlots();
            // 주식 계좌는 월드 저장 데이터에 UUID 기준으로 보관되므로 복사할 필요가 없다.

            this.jobsplus$jobs.forEach(job -> job.setPlayer(this));
            if (oldJobsServerPlayer.jobsplus$isDeathItemProtected())
            {
                this.getInventory().replaceWith(oldPlayer.getInventory());
                this.jobsplus$deathItemProtected = false;
                oldJobsServerPlayer.jobsplus$setDeathItemProtected(false);
            }
            JobHealthSync.sync(this);
        }
    }

    @Inject(at = @At("TAIL"), method = "addAdditionalSaveData")
    public void addAdditionalSaveData(ValueOutput valueOutput, CallbackInfo ci) {
        // 주식 계좌는 월드 저장 데이터에 보관하므로 플레이어 NBT에는 더 이상 저장하지 않는다.
        valueOutput.store("JobsPlus", ServerPlayerData.CODEC,
                new ServerPlayerData(this.jobsplus$jobs, this.jobsplus$coins, this.jobsplus$extraJobSlots,
                        StockAccount.EMPTY));

        // 사망 보존 플래그는 리스폰 시 인벤토리 복원의 유일한 근거이므로,
        // 사망 화면에서 접속을 끊거나 서버가 재시작돼도 유지되도록 함께 저장한다.
        valueOutput.store("JobsPlusDeathItemProtected", Codec.BOOL, this.jobsplus$deathItemProtected);
    }

    @Inject(at = @At("TAIL"), method = "readAdditionalSaveData")
    public void readAdditionalSaveData(ValueInput valueInput, CallbackInfo ci) {
        valueInput.read("JobsPlus", ServerPlayerData.CODEC).ifPresent(serverPlayerData -> {
            this.jobsplus$jobs = serverPlayerData.jobs().stream()
                    .filter(job -> job.getJobInstance() != null)
                    .peek(job -> job.setPlayer(this))
                    .collect(Collectors.toCollection(ArrayList::new));

            this.jobsplus$coins = serverPlayerData.coins();
            this.jobsplus$extraJobSlots = Math.max(0, serverPlayerData.extraJobSlots());
            jobsplus$migrateLegacyStockAccount(serverPlayerData.stockAccount());

            if (jobsplus$getServerPlayer() instanceof ArcServerPlayer arcServerPlayer) {
                List<IActionHolder> iActionHolders = this.jobsplus$getActionHolders();
                arcServerPlayer.arc$addActionHolders(new ArrayList<>(iActionHolders));
            }
            JobHealthSync.sync(this);
        });

        valueInput.read("JobsPlusDeathItemProtected", Codec.BOOL)
                .ifPresent(protectedFlag -> this.jobsplus$deathItemProtected = protectedFlag);
    }

    @Inject(at = @At("TAIL"), method = "tick()V")
    public void tickTail(CallbackInfo ci) {
        jobsplus$jobs.forEach((job) -> {
            ExpCollector expCollector = job.getExpCollector();
            double exp = expCollector.getExp();
            double roundedExp = Math.round(exp * 10.0D) / 10.0D;
            if (roundedExp > 0.0D) {
                JobInstance jobInstance = job.getJobInstance();
                MutableComponent component = JobsPlus
                        .translatable("job.exp.gain", jobsplus$formatExp(roundedExp), jobInstance.getName().getString())
                        .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(jobInstance.getColorDecimal())))
                        .withStyle(ChatFormatting.BOLD);
                jobsplus$getServerPlayer().sendSystemMessage(component, true);
            }
            expCollector.clear();
        });
    }

    /**
     * 예전 버전이 플레이어 NBT에 저장해 둔 주식 계좌를 월드 저장 데이터로 1회 이관한다.
     * 월드 저장 데이터에 이미 계좌가 있으면 그것을 우선한다.
     */
    @Unique
    private void jobsplus$migrateLegacyStockAccount(StockAccount legacyAccount) {
        if (legacyAccount == null || StockAccount.EMPTY.equals(legacyAccount)) {
            return;
        }
        MinecraftServer server = this.level().getServer();
        if (server == null) {
            return;
        }
        StockPositionLedger ledger = StockPositionLedger.get(server);
        if (ledger.hasAccount(this.getUUID())) {
            return;
        }
        ledger.setAccount(this.getUUID(), legacyAccount);
    }

    @Unique
    private static String jobsplus$formatExp(double exp) {
        boolean isWholeNumber = exp == Math.rint(exp);
        if (isWholeNumber) {
            return String.valueOf((long) exp);
        }
        return String.format(Locale.ROOT, "%.1f", exp);
    }
}
