package com.daqem.jobsplus.networking.c2s;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.metrics.MetricsEvent;
import com.daqem.jobsplus.networking.JobsPlusNetworking;
import com.daqem.jobsplus.networking.PowerupsScreenSync;
import com.daqem.jobsplus.networking.s2c.ClientboundAlertPacket;
import com.daqem.jobsplus.player.JobsServerPlayer;
import com.daqem.jobsplus.player.job.Job;
import com.daqem.jobsplus.player.job.hyper.HyperSkillRules;
import com.daqem.jobsplus.player.job.hyper.HyperSkillState;
import com.daqem.jobsplus.player.title.TitleManager;
import dev.architectury.networking.NetworkManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

/** 비용과 단계는 서버에서 계산한다. revision으로 동일 요청의 재처리를 막는다. */
public record ServerboundHyperSkillPacket(Identifier jobLocation, Action action, int revision)
        implements CustomPacketPayload
{
    public enum Action { OPEN, UPGRADE, TOGGLE }

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundHyperSkillPacket> STREAM_CODEC =
            new StreamCodec<>()
            {
                @Override
                public @NotNull ServerboundHyperSkillPacket decode(RegistryFriendlyByteBuf buffer)
                {
                    return new ServerboundHyperSkillPacket(
                            buffer.readIdentifier(), buffer.readEnum(Action.class), buffer.readInt());
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, ServerboundHyperSkillPacket packet)
                {
                    buffer.writeIdentifier(packet.jobLocation());
                    buffer.writeEnum(packet.action());
                    buffer.writeInt(packet.revision());
                }
            };

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type()
    {
        return JobsPlusNetworking.SERVERBOUND_HYPER_SKILL;
    }

    public static void handleServerSide(ServerboundHyperSkillPacket packet, NetworkManager.PacketContext context)
    {
        if (context.getPlayer() instanceof ServerPlayer player)
        {
            player.level().getServer().execute(() -> handle(player, packet));
        }
    }

    private static void handle(ServerPlayer player, ServerboundHyperSkillPacket packet)
    {
        if (player.isRemoved() || !(player instanceof JobsServerPlayer jobsPlayer))
        {
            return;
        }
        if (!HyperSkillRules.supports(packet.jobLocation()))
        {
            return;
        }
        Job job = jobsPlayer.jobsplus$getJob(packet.jobLocation());
        if (job == null)
        {
            return;
        }

        HyperSkillState state = job.getHyperSkill();
        if (packet.revision() != state.revision())
        {
            // 성공·실패·전환을 포함해 처리된 요청은 비용을 다시 차감하지 않는다.
            PowerupsScreenSync.send(jobsPlayer, packet.jobLocation());
            return;
        }
        if (job.getLevel() < HyperSkillRules.REQUIRED_JOB_LEVEL)
        {
            finish(player, jobsPlayer, job, JobsPlus.translatable("hyper.requires_level"));
            return;
        }

        if (packet.action() == Action.TOGGLE)
        {
            if (state.level() > 0)
            {
                job.setHyperSkill(state.toggle());
            }
            PowerupsScreenSync.send(jobsPlayer, packet.jobLocation());
            return;
        }
        if (packet.action() == Action.OPEN)
        {
            open(player, jobsPlayer, job);
            return;
        }
        upgrade(player, jobsPlayer, job);
    }

    private static void open(ServerPlayer player, JobsServerPlayer jobsPlayer, Job job)
    {
        if (job.getHyperSkill().level() != 0)
        {
            finish(player, jobsPlayer, job, JobsPlus.translatable("hyper.already_open"));
            return;
        }
        Inventory inventory = player.getInventory();
        int coinsBefore = jobsPlayer.jobsplus$getCoins();
        if (coinsBefore < HyperSkillRules.OPEN_COIN_COST
                || HyperSkillRules.countItems(inventory, HyperSkillRules.GEM) < HyperSkillRules.OPEN_GEM_COST)
        {
            finish(player, jobsPlayer, job, JobsPlus.translatable("hyper.not_enough_open"));
            return;
        }

        HyperSkillRules.consumeItems(inventory, HyperSkillRules.GEM, HyperSkillRules.OPEN_GEM_COST);
        jobsPlayer.jobsplus$setCoins(coinsBefore - HyperSkillRules.OPEN_COIN_COST);
        job.setHyperSkill(job.getHyperSkill().withLevel(1));
        MetricsEvent.of("HYPER_OPEN").player(player).job(job.getJobInstance().getLocation())
                .before(0).after(1).coins(coinsBefore, jobsPlayer.jobsplus$getCoins())
                .jobLevel(job.getLevel()).detail("gems", HyperSkillRules.OPEN_GEM_COST).record();
        TitleManager.onHyperSkillOpened(player);
        finish(player, jobsPlayer, job,
                JobsPlus.translatable("hyper.opened", HyperSkillRules.getName(job.getJobInstance().getLocation())));
    }

    private static void upgrade(ServerPlayer player, JobsServerPlayer jobsPlayer, Job job)
    {
        HyperSkillState state = job.getHyperSkill();
        if (state.level() < 1 || state.level() >= HyperSkillRules.MAX_LEVEL)
        {
            finish(player, jobsPlayer, job, JobsPlus.translatable("hyper.cannot_upgrade"));
            return;
        }
        int targetLevel = state.level() + 1;
        int gemCost = HyperSkillRules.getGemCost(targetLevel);
        int bitcoinCost = HyperSkillRules.getBitcoinCost(targetLevel);
        int coinCost = HyperSkillRules.UPGRADE_COIN_COST;
        int coinsBefore = jobsPlayer.jobsplus$getCoins();
        int successChance = HyperSkillRules.getSuccessChance(targetLevel);
        Inventory inventory = player.getInventory();
        if (coinsBefore < coinCost
                || HyperSkillRules.countItems(inventory, HyperSkillRules.GEM) < gemCost
                || HyperSkillRules.countItems(inventory, HyperSkillRules.BITCOIN) < bitcoinCost)
        {
            finish(player, jobsPlayer, job,
                    JobsPlus.translatable("hyper.not_enough_upgrade", gemCost, bitcoinCost, coinCost));
            return;
        }

        HyperSkillRules.consumeItems(inventory, HyperSkillRules.GEM, gemCost);
        HyperSkillRules.consumeItems(inventory, HyperSkillRules.BITCOIN, bitcoinCost);
        jobsPlayer.jobsplus$setCoins(coinsBefore - coinCost);
        int nextLevel = state.level();
        boolean success = player.getRandom().nextInt(100) < successChance;
        Component result = JobsPlus.translatable("hyper.upgrade_failed", nextLevel);
        if (success)
        {
            nextLevel = targetLevel;
            result = JobsPlus.translatable("hyper.upgrade_success", nextLevel,
                    HyperSkillRules.getActivationChance(nextLevel));
        }
        // 실패해도 revision은 증가시켜 같은 확인 요청으로 재시도되지 않게 한다.
        job.setHyperSkill(state.withLevel(nextLevel));
        MetricsEvent.of("HYPER_UPGRADE").player(player).job(job.getJobInstance().getLocation())
                .before(state.level()).after(nextLevel).jobLevel(job.getLevel())
                .coins(coinsBefore, jobsPlayer.jobsplus$getCoins())
                .detail("success", success).detail("chance", successChance)
                .detail("gems", gemCost).detail("bitcoin", bitcoinCost).record();
        finish(player, jobsPlayer, job, result);
    }

    private static void finish(ServerPlayer player, JobsServerPlayer jobsPlayer, Job job, Component message)
    {
        player.inventoryMenu.broadcastChanges();
        if (player.containerMenu != player.inventoryMenu)
        {
            player.containerMenu.broadcastChanges();
        }
        NetworkManager.sendToPlayer(player, new ClientboundAlertPacket(message));
        PowerupsScreenSync.send(jobsPlayer, job.getJobInstance().getLocation());
    }
}
