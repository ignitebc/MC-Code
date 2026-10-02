package com.daqem.jobsplus.achievement;

import java.util.Set;

/** 인벤토리와 같은 플레이어 저장 파일에 수령 기록을 보관한다. 키는 시즌/업적 ID다. */
public interface AchievementPlayer
{
    Set<String> jobsplus$getClaimedAchievements();
}
