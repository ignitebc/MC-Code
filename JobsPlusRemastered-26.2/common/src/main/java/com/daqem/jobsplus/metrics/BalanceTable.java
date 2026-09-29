package com.daqem.jobsplus.metrics;

import com.daqem.arc.api.action.IAction;
import com.daqem.arc.api.reward.IReward;
import com.daqem.jobsplus.integration.arc.holder.holders.job.JobInstance;
import com.daqem.jobsplus.integration.arc.holder.holders.job.JobManager;
import com.daqem.jobsplus.integration.arc.holder.holders.powerup.PowerupInstance;
import com.daqem.jobsplus.integration.arc.holder.holders.powerup.PowerupManager;
import com.daqem.jobsplus.integration.arc.reward.rewards.job.JobBitcoinReward;
import com.daqem.jobsplus.integration.arc.reward.rewards.job.JobCoinReward;
import com.daqem.jobsplus.integration.arc.reward.rewards.job.JobExpMultiplierReward;
import com.daqem.jobsplus.integration.arc.reward.rewards.job.JobExpReward;
import net.minecraft.resources.Identifier;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;

/**
 * 서버에 실제로 적용된 직업 보상표와 스킬 가격표를 꺼내고, 그 내용의 해시를 밸런스 버전으로 쓴다.
 * 커밋 시각과 배포 시각이 달라도 로그 행의 balance_version으로 어떤 수치 아래에서 나온 기록인지 구분할 수 있다.
 */
final class BalanceTable
{
    static final String REWARDS_HEADER = "balance_version,holder_type,holder_id,job_id,action_id,trigger,reward_index,"
            + "reward_type,chance,priority,exp_min,exp_max,btc_amount,exp_multiplier,coin_amount";
    static final String POWERUPS_HEADER = "balance_version,powerup_id,job_id,price,required_level,parent_id,powerup_type";

    private final String version;
    private final List<String> rewardRows;
    private final List<String> powerupRows;
    private final int jobCount;
    private final int powerupCount;

    private BalanceTable(String version, List<String> rewardRows, List<String> powerupRows, int jobCount, int powerupCount)
    {
        this.version = version;
        this.rewardRows = rewardRows;
        this.powerupRows = powerupRows;
        this.jobCount = jobCount;
        this.powerupCount = powerupCount;
    }

    static BalanceTable capture()
    {
        List<String> rewardRows = new ArrayList<>();
        List<String> powerupRows = new ArrayList<>();

        List<JobInstance> jobs = new ArrayList<>(JobManager.getInstance().getJobs().values());
        for (JobInstance job : jobs)
        {
            addRewardRows(rewardRows, "job", job.getLocation(), job.getLocation(), job.getActions());
        }

        List<PowerupInstance> powerups = new ArrayList<>(PowerupManager.getInstance().getAllPowerups().values());
        for (PowerupInstance powerup : powerups)
        {
            addRewardRows(rewardRows, "powerup", powerup.getLocation(), powerup.getJobLocation(), powerup.getActions());
            powerupRows.add(MetricsCsv.text(powerup.getLocation().toString()) + ","
                    + MetricsCsv.text(powerup.getJobLocation().toString()) + ","
                    + powerup.getPrice() + ","
                    + powerup.getRequiredLevel() + ","
                    + MetricsCsv.text(powerup.getParentLocation() == null ? "" : powerup.getParentLocation().toString()) + ","
                    + MetricsCsv.text(String.valueOf(powerup.getPowerupType())));
        }

        // 맵 순회 순서와 무관하게 같은 수치면 같은 버전이 나오도록 정렬한 뒤 해시한다.
        Collections.sort(rewardRows);
        Collections.sort(powerupRows);
        String version = hash(rewardRows, powerupRows);
        return new BalanceTable(version, rewardRows, powerupRows, jobs.size(), powerups.size());
    }

    private static void addRewardRows(List<String> rows, String holderType, Identifier holderId, Identifier jobId, List<IAction> actions)
    {
        List<IAction> sortedActions = new ArrayList<>(actions);
        sortedActions.sort(Comparator.comparing(action -> action.getLocation().toString()));
        for (IAction action : sortedActions)
        {
            List<IReward> rewards = action.getRewards();
            for (int index = 0; index < rewards.size(); index++)
            {
                IReward reward = rewards.get(index);
                String expMin = "";
                String expMax = "";
                String bitcoinAmount = "";
                String expMultiplier = "";
                String coinAmount = "";
                if (reward instanceof JobExpReward expReward)
                {
                    expMin = MetricsCsv.number(expReward.getMin());
                    expMax = MetricsCsv.number(expReward.getMax());
                }
                else if (reward instanceof JobBitcoinReward bitcoinReward)
                {
                    bitcoinAmount = Integer.toString(bitcoinReward.getAmount());
                }
                else if (reward instanceof JobExpMultiplierReward multiplierReward)
                {
                    expMultiplier = MetricsCsv.number(multiplierReward.getMultiplier());
                }
                else if (reward instanceof JobCoinReward coinReward)
                {
                    coinAmount = Integer.toString(coinReward.getAmount());
                }

                rows.add(holderType + ","
                        + MetricsCsv.text(holderId.toString()) + ","
                        + MetricsCsv.text(jobId.toString()) + ","
                        + MetricsCsv.text(action.getLocation().toString()) + ","
                        + MetricsCsv.text(action.getType().getLocation().toString()) + ","
                        + index + ","
                        + MetricsCsv.text(reward.getType().getLocation().toString()) + ","
                        + MetricsCsv.number(reward.getChance()) + ","
                        + reward.getPriority() + ","
                        + expMin + ","
                        + expMax + ","
                        + bitcoinAmount + ","
                        + expMultiplier + ","
                        + coinAmount);
            }
        }
    }

    private static String hash(List<String> rewardRows, List<String> powerupRows)
    {
        try
        {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (String row : rewardRows)
            {
                digest.update(row.getBytes(StandardCharsets.UTF_8));
                digest.update((byte) '\n');
            }
            digest.update((byte) '#');
            for (String row : powerupRows)
            {
                digest.update(row.getBytes(StandardCharsets.UTF_8));
                digest.update((byte) '\n');
            }
            return HexFormat.of().formatHex(digest.digest()).substring(0, 12);
        }
        catch (NoSuchAlgorithmException exception)
        {
            throw new IllegalStateException("SHA-256 is not available.", exception);
        }
    }

    String version()
    {
        return version;
    }

    int jobCount()
    {
        return jobCount;
    }

    int powerupCount()
    {
        return powerupCount;
    }

    int rewardRowCount()
    {
        return rewardRows.size();
    }

    List<String> versionedRewardRows()
    {
        return rewardRows.stream().map(row -> version + "," + row).toList();
    }

    List<String> versionedPowerupRows()
    {
        return powerupRows.stream().map(row -> version + "," + row).toList();
    }
}
