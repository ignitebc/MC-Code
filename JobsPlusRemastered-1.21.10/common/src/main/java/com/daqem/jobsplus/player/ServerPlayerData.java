package com.daqem.jobsplus.player;

import com.daqem.jobsplus.player.job.Job;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/**
 * 플레이어 저장 데이터
 * - jobs: 보유 직업 목록
 * - coins: 직업 코인
 * - extraJobSlots: 전역 설정(maxJobs) 외에, 아이템 등으로 얻는 추가 직업 슬롯
 */
public record ServerPlayerData(List<Job> jobs, int coins, int extraJobSlots) {

        public static final Codec<ServerPlayerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        Job.CODEC.listOf().fieldOf("jobs").forGetter(ServerPlayerData::jobs),
                        Codec.INT.fieldOf("coins").forGetter(ServerPlayerData::coins),
                        Codec.INT.optionalFieldOf("extra_job_slots", 0).forGetter(ServerPlayerData::extraJobSlots))
                        .apply(instance, ServerPlayerData::new));
}
