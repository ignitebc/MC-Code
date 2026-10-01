package com.daqem.jobsplus.networking.c2s;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.integration.arc.holder.holders.powerup.PowerupInstance;
import com.daqem.jobsplus.metrics.MetricsEvent;
import com.daqem.jobsplus.networking.JobsPlusNetworking;
import com.daqem.jobsplus.networking.PowerupsScreenSync;
import com.daqem.jobsplus.networking.s2c.ClientboundAlertPacket;
import com.daqem.jobsplus.player.JobsServerPlayer;
import com.daqem.jobsplus.player.job.Job;
import com.daqem.jobsplus.player.job.powerup.PowerupAvailability;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/** 현재 레벨에서 해금된 모든 미습득 스킬을 한 번에 구매한다. */
public class ServerboundStartAllPowerupsPacket implements CustomPacketPayload
{
    private final Identifier jobLocation;

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundStartAllPowerupsPacket> STREAM_CODEC =
            new StreamCodec<>()
            {
                @Override
                public @NotNull ServerboundStartAllPowerupsPacket decode(RegistryFriendlyByteBuf buffer)
                {
                    return new ServerboundStartAllPowerupsPacket(buffer.readIdentifier());
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, ServerboundStartAllPowerupsPacket packet)
                {
                    buffer.writeIdentifier(packet.jobLocation);
                }
            };

    public ServerboundStartAllPowerupsPacket(Identifier jobLocation)
    {
        this.jobLocation = jobLocation;
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type()
    {
        return JobsPlusNetworking.SERVERBOUND_START_ALL_POWERUPS;
    }

    public static void handleServerSide(ServerboundStartAllPowerupsPacket packet,
                                        NetworkManager.PacketContext context)
    {
        if (!(context.getPlayer() instanceof JobsServerPlayer serverPlayer))
        {
            return;
        }

        Job job = serverPlayer.jobsplus$getJob(packet.jobLocation);
        if (job == null)
        {
            sendAlert(serverPlayer,
                    JobsPlus.translatable("error.job_not_found", packet.jobLocation.toString()));
            return;
        }

        List<PowerupInstance> powerups = PowerupAvailability.getBatchUnlockablePowerups(job);
        if (powerups.isEmpty())
        {
            sendAlert(serverPlayer, JobsPlus.translatable("gui.powerups.no_available_powerups"));
            return;
        }

        long totalPrice = PowerupAvailability.getTotalPrice(powerups);
        int coinsBefore = serverPlayer.jobsplus$getCoins();
        if (totalPrice > coinsBefore)
        {
            recordPurchaseFailure(serverPlayer, job, totalPrice);
            sendAlert(serverPlayer, JobsPlus.translatable("gui.powerups.not_enough_coins_for_all"));
            return;
        }

        int ownedBefore = job.getPowerupManager().getAllPowerups().size();
        if (!job.getPowerupManager().addPowerups(serverPlayer, job, powerups))
        {
            sendAlert(serverPlayer, JobsPlus.translatable("gui.powerups.could_not_purchase_all"));
            return;
        }

        int totalPriceAsInt = Math.toIntExact(totalPrice);
        serverPlayer.jobsplus$setCoins(coinsBefore - totalPriceAsInt);
        recordPurchases(serverPlayer, job, powerups, coinsBefore, ownedBefore);
        sendAlert(serverPlayer,
                JobsPlus.translatable("gui.powerups.all_powerups_purchased", powerups.size()));
        PowerupsScreenSync.send(serverPlayer, packet.jobLocation);
    }

    private static void recordPurchases(JobsServerPlayer serverPlayer, Job job,
                                        List<PowerupInstance> powerups, int coinsBefore, int ownedBefore)
    {
        int coins = coinsBefore;
        for (int index = 0; index < powerups.size(); index++)
        {
            PowerupInstance powerupInstance = powerups.get(index);
            int coinsAfter = coins - powerupInstance.getPrice();
            MetricsEvent.of("POWERUP_BUY")
                    .player(serverPlayer.jobsplus$getServerPlayer())
                    .job(job.getJobInstance().getLocation())
                    .target(powerupInstance.getLocation())
                    .value(powerupInstance.getPrice())
                    .coins(coins, coinsAfter)
                    .jobLevel(job.getLevel())
                    .detail("required_level", powerupInstance.getRequiredLevel())
                    .detail("parent", powerupInstance.getParentLocation() == null
                            ? "" : powerupInstance.getParentLocation())
                    .detail("owned_after", ownedBefore + index + 1)
                    .detail("batch", true)
                    .record();
            coins = coinsAfter;
        }
    }

    private static void recordPurchaseFailure(JobsServerPlayer serverPlayer, Job job, long totalPrice)
    {
        MetricsEvent.of("POWERUP_BUY_ALL_FAILED")
                .player(serverPlayer.jobsplus$getServerPlayer())
                .job(job.getJobInstance().getLocation())
                .value(totalPrice)
                .coins(serverPlayer.jobsplus$getCoins(), serverPlayer.jobsplus$getCoins())
                .jobLevel(job.getLevel())
                .detail("reason", "not_enough_coins")
                .record();
    }

    private static void sendAlert(JobsServerPlayer serverPlayer, Component message)
    {
        NetworkManager.sendToPlayer(
                serverPlayer.jobsplus$getServerPlayer(),
                new ClientboundAlertPacket(
                        message,
                        JobsPlus.translatable("gui.confirmation.ok")
                )
        );
    }
}
