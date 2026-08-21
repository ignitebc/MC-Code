package com.daqem.jobsplus;

import com.daqem.arc.registry.ArcRegistry;
import com.daqem.jobsplus.config.JobsPlusConfig;
import com.daqem.jobsplus.effect.JobsPlusMobEffects;
import com.daqem.jobsplus.event.command.EventRegisterCommands;
import com.daqem.jobsplus.event.block.CropReplantManager;
import com.daqem.jobsplus.event.item.EventJobSelectTicketUse;
import com.daqem.jobsplus.event.item.EventRewardCouponUse;
import com.daqem.jobsplus.event.player.EventDeleteRandomItemOnDeath;
import com.daqem.jobsplus.event.player.EventKillElytraDuringRaidOrWither;
import com.daqem.jobsplus.event.player.EventRewardCouponEffectSync;
import com.daqem.jobsplus.event.stock.StockMarketTicker;
import com.daqem.jobsplus.integration.arc.holder.holders.job.JobManager;
import com.daqem.jobsplus.integration.arc.holder.holders.powerup.PowerupManager;
import com.daqem.jobsplus.integration.arc.holder.type.JobsPlusActionHolderType;
import com.daqem.jobsplus.integration.arc.action.type.JobsPlusActionType;
import com.daqem.jobsplus.integration.arc.condition.type.JobsPlusConditionType;
import com.daqem.jobsplus.integration.arc.reward.type.JobsPlusRewardType;
import com.daqem.jobsplus.metrics.JobsPlusMetrics;
import com.daqem.jobsplus.networking.JobsPlusNetworking;
import com.mojang.logging.LogUtils;
import dev.architectury.registry.ReloadListenerRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import org.slf4j.Logger;

public class JobsPlus
{

    public static final String MOD_ID = "jobsplus";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init()
    {
        JobsPlusConfig.init();
        JobsPlusNetworking.init();
        JobsPlusMobEffects.init();

        registerEvents();
        initRegistry();

        ReloadListenerRegistry.register(PackType.SERVER_DATA, new JobManager(), getId("jobs"));
        ReloadListenerRegistry.register(PackType.SERVER_DATA, new PowerupManager(), getId("powerups"));
    }

    private static void registerEvents()
    {
        EventRegisterCommands.registerEvent();
        EventJobSelectTicketUse.registerEvent(); // 추가
        EventRewardCouponUse.registerEvent();
        EventRewardCouponEffectSync.registerEvent();
        EventKillElytraDuringRaidOrWither.registerEvent(); // 레이드/위더 활성 시 겉날개 즉사
        CropReplantManager.registerEvent();
        StockMarketTicker.registerEvent(); // 주식 시청자·미결제 포지션·예약 주문이 있으면 매분 시세 갱신
        EventDeleteRandomItemOnDeath.registerEvent(); // 사망 시 소지품 한 칸을 무작위로 삭제
        JobsPlusMetrics.registerEvents(); // EXP/BTC/접속시간 분석용 경량 메트릭
    }

    private static void initRegistry()
    {
        ArcRegistry.init();

        JobsPlusActionType.init();
        JobsPlusRewardType.init();
        JobsPlusConditionType.init();
        JobsPlusActionHolderType.init();
    }

    public static Identifier getId(String id)
    {
        return Identifier.fromNamespaceAndPath(MOD_ID, id);
    }

    public static MutableComponent translatable(String str)
    {
        return Component.translatable(MOD_ID + "." + str);
    }

    public static MutableComponent translatable(String str, Object... objects)
    {
        return Component.translatable(MOD_ID + "." + str, objects);
    }

    public static MutableComponent literal(String str)
    {
        return Component.literal(str);
    }

    public static void debug(String s)
    {
        debug(s, new Object[0]);
    }

    public static void debug(String message, Object... objects)
    {
        if (JobsPlusConfig.isDebug.get())
        {
            LOGGER.warn("DEBUG MESSAGE: " + message, objects);
        }
    }

    public static boolean isDebugEnvironment()
    {
        return JobsPlusConfig.isDebug.get();
    }
}
