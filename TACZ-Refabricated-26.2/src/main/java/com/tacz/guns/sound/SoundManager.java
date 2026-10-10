package com.tacz.guns.sound;

import com.tacz.guns.network.NetworkHandler;
import com.tacz.guns.network.message.ServerMessageSound;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;

public class SoundManager {
    /**
     * 사격 효과음. 자신이 듣는다
     */
    public static String SHOOT_SOUND = "shoot";
    /**
     * 다른 플레이어가 듣는 총소리
     */
    public static String SHOOT_3P_SOUND = "shoot_3p";
    /**
     * 소음기 효과음
     */
    public static String SILENCE_SOUND = "silence";
    /**
     * 다른 플레이어가 듣는 소음기 총소리
     */
    public static String SILENCE_3P_SOUND = "silence_3p";
    /**
     * 근접 총검 효과음
     */
    public static String MELEE_BAYONET = "melee_bayonet";
    /**
     * 근접 밀치기 효과음
     */
    public static String MELEE_PUSH = "melee_push";
    /**
     * 근접 개머리판 치기 효과음
     */
    public static String MELEE_STOCK = "melee_stock";
    /**
     * 탄환이 없을 때 빈 격발 소리
     */
    public static String DRY_FIRE_SOUND = "dry_fire";
    /**
     * 빈 탄창 재장전 소리
     */
    public static String RELOAD_EMPTY_SOUND = "reload_empty";
    /**
     * 전술 재장전 소리
     */
    public static String RELOAD_TACTICAL_SOUND = "reload_tactical";
    /**
     * 빈 탄창 점검 소리
     */
    public static String INSPECT_EMPTY_SOUND = "inspect_empty";
    /**
     * 일반 점검 소리
     */
    public static String INSPECT_SOUND = "inspect";
    /**
     * 총 꺼내기 소리
     */
    public static String DRAW_SOUND = "draw";
    /**
     * 총 집어넣기 소리
     */
    public static String PUT_AWAY_SOUND = "put_away";
    /**
     * 노리쇠 당기기 소리
     */
    public static String BOLT_SOUND = "bolt";
    /**
     * 발사 방식 전환 소리
     */
    public static String FIRE_SELECT = "fire_select";
    /**
     * 헤드샷 명중 소리
     */
    public static String HEAD_HIT_SOUND = "head_hit";
    /**
     * 일반 명중 소리
     */
    public static String FLESH_HIT_SOUND = "flesh_hit";
    /**
     * 처치 소리
     */
    public static String KILL_SOUND = "kill";
    /**
     * 부착물을 떼어 내는 소리. 부착물에 쓴다
     */
    public static String UNINSTALL_SOUND = "uninstall";
    /**
     * 부착물을 다는 소리. 부착물에 쓴다
     */
    public static String INSTALL_SOUND = "install";

    public static void sendSoundToNearby(LivingEntity sourceEntity, int distance, Identifier gunId, Identifier gunDisplayId, String soundName, float volume, float pitch) {
        if (sourceEntity.level() instanceof ServerLevel serverLevel) {
            BlockPos pos = sourceEntity.blockPosition();
            ServerMessageSound soundMessage = new ServerMessageSound(sourceEntity.getId(), gunId, gunDisplayId, soundName, volume, pitch, distance);
            serverLevel.getChunkSource().chunkMap.getPlayers(new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4), false).stream()
                    .filter(p -> p.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) < distance * distance)
                    .filter(p -> p.getId() != sourceEntity.getId())
                    .forEach(p -> NetworkHandler.sendToClientPlayer(soundMessage, p));
        }
    }
}
