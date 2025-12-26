package com.daqem.jobsplus.player;

import com.daqem.jobsplus.player.job.Job;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 * 플레이어 저장 데이터
 * - jobs: 보유 직업 목록
 * - coins: 코인
 * - extra_job_slots: 직업추가권 등으로 증가한 추가 슬롯(상한 없음)
 */
public record ServerPlayerData(List<Job> jobs, int coins, int extraJobSlots) {
        public static final Codec<ServerPlayerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        Job.CODEC.listOf().fieldOf("jobs").forGetter(ServerPlayerData::jobs),
                        Codec.INT.fieldOf("coins").forGetter(ServerPlayerData::coins),
                        Codec.INT.optionalFieldOf("extra_job_slots", 0).forGetter(ServerPlayerData::extraJobSlots))
                        .apply(instance, ServerPlayerData::new));
}
