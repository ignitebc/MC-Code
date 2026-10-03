package com.daqem.jobsplus.networking;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.hyper.ClientHyperSkills;
import com.daqem.jobsplus.client.networking.ClientboundAlertPacketHandler;
import com.daqem.jobsplus.client.networking.ClientboundAchievementPacketHandler;
import com.daqem.jobsplus.client.networking.ClientboundLevelUpJobPacketHandler;
import com.daqem.jobsplus.client.networking.ClientboundOpenJobsScreenPacketHandler;
import com.daqem.jobsplus.client.networking.ClientboundOpenPowerupsScreenPacketHandler;
import com.daqem.jobsplus.client.networking.ClientboundSkillNotificationsPacketHandler;
import com.daqem.jobsplus.client.networking.ClientboundSyncActionHoldersPacketHandler;
import com.daqem.jobsplus.client.networking.ClientboundStockSnapshotPacketHandler;
import com.daqem.jobsplus.client.networking.ClientboundTitlesPacketHandler;
import com.daqem.jobsplus.client.networking.ClientboundUnlockItemRestrictionPacketHandler;
import com.daqem.jobsplus.networking.c2s.*;
import com.daqem.jobsplus.networking.s2c.*;
import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import dev.architectury.utils.EnvExecutor;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public interface JobsPlusNetworking
{
        CustomPacketPayload.Type<ServerboundHyperLeapPacket> SERVERBOUND_HYPER_LEAP = new CustomPacketPayload.Type<>(JobsPlus.getId("serverbound_hyper_leap"));
        CustomPacketPayload.Type<ClientboundHyperStatusPacket> CLIENTBOUND_HYPER_STATUS = new CustomPacketPayload.Type<>(JobsPlus.getId("clientbound_hyper_status"));
        CustomPacketPayload.Type<ClientboundHyperLeapPacket> CLIENTBOUND_HYPER_LEAP = new CustomPacketPayload.Type<>(JobsPlus.getId("clientbound_hyper_leap"));
        CustomPacketPayload.Type<ServerboundAchievementPacket> SERVERBOUND_ACHIEVEMENTS = new CustomPacketPayload.Type<>(JobsPlus.getId("serverbound_achievements_v1"));
        CustomPacketPayload.Type<ClientboundAchievementPacket> CLIENTBOUND_ACHIEVEMENTS = new CustomPacketPayload.Type<>(JobsPlus.getId("clientbound_achievements_v1"));
        CustomPacketPayload.Type<ServerboundTogglePowerUpPacket> SERVERBOUND_TOGGLE_POWERUP = new CustomPacketPayload.Type<>(JobsPlus.getId("serverbound_toggle_powerup"));
        CustomPacketPayload.Type<ServerboundStartJobPacket> SERVERBOUND_START_JOB = new CustomPacketPayload.Type<>(JobsPlus.getId("serverbound_start_job"));
        CustomPacketPayload.Type<ServerboundStartPowerupPacket> SERVERBOUND_START_POWERUP = new CustomPacketPayload.Type<>(JobsPlus.getId("serverbound_start_powerup"));
        CustomPacketPayload.Type<ServerboundStartAllPowerupsPacket> SERVERBOUND_START_ALL_POWERUPS = new CustomPacketPayload.Type<>(JobsPlus.getId("serverbound_start_all_powerups"));
        CustomPacketPayload.Type<ServerboundHyperSkillPacket> SERVERBOUND_HYPER_SKILL = new CustomPacketPayload.Type<>(JobsPlus.getId("serverbound_hyper_skill"));
        CustomPacketPayload.Type<ServerboundOpenJobsScreenPacket> SERVERBOUND_OPEN_JOBS_SCREEN = new CustomPacketPayload.Type<>(JobsPlus.getId("serverbound_open_jobs_screen"));
        CustomPacketPayload.Type<ServerboundOpenPowerupsScreenPacket> SERVERBOUND_OPEN_POWERUPS_SCREEN = new CustomPacketPayload.Type<>(JobsPlus.getId("serverbound_open_powerups_screen"));
        CustomPacketPayload.Type<ServerboundStockActionPacket> SERVERBOUND_STOCK_ACTION = new CustomPacketPayload.Type<>(JobsPlus.getId("serverbound_stock_action"));
        // 주식 탭 시청자를 추적한다. 포지션이나 예약 주문이 있으면 시청자가 없어도 갱신을 유지한다.
        CustomPacketPayload.Type<ServerboundStockViewStatePacket> SERVERBOUND_STOCK_VIEW_STATE = new CustomPacketPayload.Type<>(JobsPlus.getId("serverbound_stock_view_state"));

        CustomPacketPayload.Type<ClientboundUnlockItemRestrictionPacket> CLIENTBOUND_UNLOCK_ITEM_RESTRICTION = new CustomPacketPayload.Type<>(JobsPlus.getId("clientbound_unlock_item_restriction"));
        // Job.Serializer에 하이퍼 스킬 상태가 추가되어 구형 화면 패킷과 구분한다.
        CustomPacketPayload.Type<ClientboundOpenJobsScreenPacket> CLIENTBOUND_OPEN_JOBS_SCREEN = new CustomPacketPayload.Type<>(JobsPlus.getId("clientbound_open_jobs_screen_v2"));
        CustomPacketPayload.Type<ClientboundLevelUpJobPacket> CLIENTBOUND_LEVEL_UP_JOB = new CustomPacketPayload.Type<>(JobsPlus.getId("clientbound_level_up_job"));
        CustomPacketPayload.Type<ClientboundOpenPowerupsScreenPacket> CLIENTBOUND_OPEN_POWERUPS_SCREEN = new CustomPacketPayload.Type<>(JobsPlus.getId("clientbound_open_powerups_screen_v2"));
        CustomPacketPayload.Type<ClientboundAlertPacket> CLIENTBOUND_ALERT = new CustomPacketPayload.Type<>(JobsPlus.getId("clientbound_alert"));

        // 서버가 확정한 1분 시세 스냅샷 전달용
        CustomPacketPayload.Type<ClientboundStockSnapshotPacket> CLIENTBOUND_STOCK_SNAPSHOT = new CustomPacketPayload.Type<>(JobsPlus.getId("clientbound_stock_snapshot"));

        // 신규: 서버 -> 클라 활성 홀더 동기화
        CustomPacketPayload.Type<ClientboundSyncActionHoldersPacket> CLIENTBOUND_SYNC_ACTION_HOLDERS = new CustomPacketPayload.Type<>(JobsPlus.getId("clientbound_sync_action_holders"));

        // 251217 jjh, 상점(아이템 판매) - C2S 패킷
        CustomPacketPayload.Type<ServerboundSellItemPacket> SERVERBOUND_SELL_ITEM = new CustomPacketPayload.Type<>(JobsPlus.getId("serverbound_sell_item"));

        // 스킬 화면 버튼으로 스킬 발동 채팅 알림을 끄고 켠다.
        CustomPacketPayload.Type<ServerboundSetSkillNotificationsPacket> SERVERBOUND_SET_SKILL_NOTIFICATIONS = new CustomPacketPayload.Type<>(JobsPlus.getId("serverbound_set_skill_notifications"));
        CustomPacketPayload.Type<ClientboundSkillNotificationsPacket> CLIENTBOUND_SKILL_NOTIFICATIONS = new CustomPacketPayload.Type<>(JobsPlus.getId("clientbound_skill_notifications"));

        // 칭호 탭의 보유자 목록과 장착 칭호를 주고받는다.
        CustomPacketPayload.Type<ServerboundEquipTitlePacket> SERVERBOUND_EQUIP_TITLE = new CustomPacketPayload.Type<>(JobsPlus.getId("serverbound_equip_title"));
        CustomPacketPayload.Type<ClientboundTitlesPacket> CLIENTBOUND_TITLES = new CustomPacketPayload.Type<>(JobsPlus.getId("clientbound_titles"));

        static void initClient()
        {
                NetworkManager.registerReceiver(NetworkManager.Side.S2C, CLIENTBOUND_HYPER_STATUS, ClientboundHyperStatusPacket.STREAM_CODEC, ClientHyperSkills::receiveStatus);
                NetworkManager.registerReceiver(NetworkManager.Side.S2C, CLIENTBOUND_HYPER_LEAP, ClientboundHyperLeapPacket.STREAM_CODEC, ClientHyperSkills::receiveLeap);
                NetworkManager.registerReceiver(NetworkManager.Side.S2C, CLIENTBOUND_ACHIEVEMENTS, ClientboundAchievementPacket.STREAM_CODEC, ClientboundAchievementPacketHandler::handleClientSide);
                NetworkManager.registerReceiver(NetworkManager.Side.S2C, CLIENTBOUND_OPEN_JOBS_SCREEN, ClientboundOpenJobsScreenPacket.STREAM_CODEC, ClientboundOpenJobsScreenPacketHandler::handleClientSide);
                NetworkManager.registerReceiver(NetworkManager.Side.S2C, CLIENTBOUND_LEVEL_UP_JOB, ClientboundLevelUpJobPacket.STREAM_CODEC, ClientboundLevelUpJobPacketHandler::handleClientSide);
                NetworkManager.registerReceiver(NetworkManager.Side.S2C, CLIENTBOUND_UNLOCK_ITEM_RESTRICTION, ClientboundUnlockItemRestrictionPacket.STREAM_CODEC, ClientboundUnlockItemRestrictionPacketHandler::handleClientSide);
                NetworkManager.registerReceiver(NetworkManager.Side.S2C, CLIENTBOUND_OPEN_POWERUPS_SCREEN, ClientboundOpenPowerupsScreenPacket.STREAM_CODEC, ClientboundOpenPowerupsScreenPacketHandler::handleClientSide);
                NetworkManager.registerReceiver(NetworkManager.Side.S2C, CLIENTBOUND_ALERT, ClientboundAlertPacket.STREAM_CODEC, ClientboundAlertPacketHandler::handleClientSide);
                NetworkManager.registerReceiver(NetworkManager.Side.S2C, CLIENTBOUND_STOCK_SNAPSHOT, ClientboundStockSnapshotPacket.STREAM_CODEC, ClientboundStockSnapshotPacketHandler::handleClientSide);

                // 신규 등록
                NetworkManager.registerReceiver(NetworkManager.Side.S2C, CLIENTBOUND_SYNC_ACTION_HOLDERS, ClientboundSyncActionHoldersPacket.STREAM_CODEC, ClientboundSyncActionHoldersPacketHandler::handleClientSide);
                NetworkManager.registerReceiver(NetworkManager.Side.S2C, CLIENTBOUND_SKILL_NOTIFICATIONS, ClientboundSkillNotificationsPacket.STREAM_CODEC, ClientboundSkillNotificationsPacketHandler::handleClientSide);
                NetworkManager.registerReceiver(NetworkManager.Side.S2C, CLIENTBOUND_TITLES, ClientboundTitlesPacket.STREAM_CODEC, ClientboundTitlesPacketHandler::handleClientSide);
        }

        static void initCommon()
        {
                NetworkManager.registerReceiver(NetworkManager.Side.C2S, SERVERBOUND_HYPER_LEAP, ServerboundHyperLeapPacket.STREAM_CODEC, ServerboundHyperLeapPacket::handleServerSide);
                NetworkManager.registerReceiver(NetworkManager.Side.C2S, SERVERBOUND_ACHIEVEMENTS, ServerboundAchievementPacket.STREAM_CODEC, ServerboundAchievementPacket::handleServerSide);
                NetworkManager.registerReceiver(NetworkManager.Side.C2S, SERVERBOUND_TOGGLE_POWERUP, ServerboundTogglePowerUpPacket.STREAM_CODEC, ServerboundTogglePowerUpPacket::handleServerSide);
                NetworkManager.registerReceiver(NetworkManager.Side.C2S, SERVERBOUND_START_JOB, ServerboundStartJobPacket.STREAM_CODEC, ServerboundStartJobPacket::handleServerSide);
                NetworkManager.registerReceiver(NetworkManager.Side.C2S, SERVERBOUND_START_POWERUP, ServerboundStartPowerupPacket.STREAM_CODEC, ServerboundStartPowerupPacket::handleServerSide);
                NetworkManager.registerReceiver(NetworkManager.Side.C2S, SERVERBOUND_START_ALL_POWERUPS, ServerboundStartAllPowerupsPacket.STREAM_CODEC, ServerboundStartAllPowerupsPacket::handleServerSide);
                NetworkManager.registerReceiver(NetworkManager.Side.C2S, SERVERBOUND_HYPER_SKILL, ServerboundHyperSkillPacket.STREAM_CODEC, ServerboundHyperSkillPacket::handleServerSide);
                NetworkManager.registerReceiver(NetworkManager.Side.C2S, SERVERBOUND_OPEN_JOBS_SCREEN, ServerboundOpenJobsScreenPacket.STREAM_CODEC, ServerboundOpenJobsScreenPacket::handleServerSide);
                NetworkManager.registerReceiver(NetworkManager.Side.C2S, SERVERBOUND_OPEN_POWERUPS_SCREEN, ServerboundOpenPowerupsScreenPacket.STREAM_CODEC, ServerboundOpenPowerupsScreenPacket::handleServerSide);
                NetworkManager.registerReceiver(NetworkManager.Side.C2S, SERVERBOUND_STOCK_ACTION, ServerboundStockActionPacket.STREAM_CODEC, ServerboundStockActionPacket::handleServerSide);
                NetworkManager.registerReceiver(NetworkManager.Side.C2S, SERVERBOUND_STOCK_VIEW_STATE, ServerboundStockViewStatePacket.STREAM_CODEC, ServerboundStockViewStatePacket::handleServerSide);

                // 251217 jjh, 상점(아이템 판매) - C2S 리시버 등록
                NetworkManager.registerReceiver(NetworkManager.Side.C2S, SERVERBOUND_SELL_ITEM, ServerboundSellItemPacket.STREAM_CODEC, ServerboundSellItemPacket::handleServerSide);
                NetworkManager.registerReceiver(NetworkManager.Side.C2S, SERVERBOUND_SET_SKILL_NOTIFICATIONS, ServerboundSetSkillNotificationsPacket.STREAM_CODEC, ServerboundSetSkillNotificationsPacket::handleServerSide);
                NetworkManager.registerReceiver(NetworkManager.Side.C2S, SERVERBOUND_EQUIP_TITLE, ServerboundEquipTitlePacket.STREAM_CODEC, ServerboundEquipTitlePacket::handleServerSide);
        }

        static void initServer()
        {
                NetworkManager.registerS2CPayloadType(CLIENTBOUND_HYPER_STATUS, ClientboundHyperStatusPacket.STREAM_CODEC);
                NetworkManager.registerS2CPayloadType(CLIENTBOUND_HYPER_LEAP, ClientboundHyperLeapPacket.STREAM_CODEC);
                NetworkManager.registerS2CPayloadType(CLIENTBOUND_ACHIEVEMENTS, ClientboundAchievementPacket.STREAM_CODEC);
                NetworkManager.registerS2CPayloadType(CLIENTBOUND_OPEN_JOBS_SCREEN, ClientboundOpenJobsScreenPacket.STREAM_CODEC);
                NetworkManager.registerS2CPayloadType(CLIENTBOUND_LEVEL_UP_JOB, ClientboundLevelUpJobPacket.STREAM_CODEC);
                NetworkManager.registerS2CPayloadType(CLIENTBOUND_UNLOCK_ITEM_RESTRICTION, ClientboundUnlockItemRestrictionPacket.STREAM_CODEC);
                NetworkManager.registerS2CPayloadType(CLIENTBOUND_OPEN_POWERUPS_SCREEN, ClientboundOpenPowerupsScreenPacket.STREAM_CODEC);
                NetworkManager.registerS2CPayloadType(CLIENTBOUND_ALERT, ClientboundAlertPacket.STREAM_CODEC);
                NetworkManager.registerS2CPayloadType(CLIENTBOUND_STOCK_SNAPSHOT, ClientboundStockSnapshotPacket.STREAM_CODEC);

                // 신규 타입 등록
                NetworkManager.registerS2CPayloadType(CLIENTBOUND_SYNC_ACTION_HOLDERS, ClientboundSyncActionHoldersPacket.STREAM_CODEC);
                NetworkManager.registerS2CPayloadType(CLIENTBOUND_SKILL_NOTIFICATIONS, ClientboundSkillNotificationsPacket.STREAM_CODEC);
                NetworkManager.registerS2CPayloadType(CLIENTBOUND_TITLES, ClientboundTitlesPacket.STREAM_CODEC);
        }

        static void init()
        {
                EnvExecutor.runInEnv(Env.CLIENT, () -> JobsPlusNetworking::initClient);
                EnvExecutor.runInEnv(Env.SERVER, () -> JobsPlusNetworking::initServer);
                initCommon();
        }
}
