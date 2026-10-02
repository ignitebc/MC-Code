package com.daqem.jobsplus.achievement;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AchievementRulesTest
{
    @Test
    void gainingExperienceAtAnAlreadyReachedLevelDoesNotGrantPastMilestones()
    {
        assertEquals(List.of(), AchievementRules.crossedEquipmentMilestones(2400, 2401));
        assertEquals(List.of(), AchievementRules.crossedEquipmentMilestones(4900, 4901));
        assertEquals(List.of(), AchievementRules.crossedEquipmentMilestones(9900, 9900));
    }

    @Test
    void onlyTheMilestoneCrossedByThePlayersExperienceIsGranted()
    {
        assertEquals(List.of(25), AchievementRules.crossedEquipmentMilestones(2399, 2400));
        assertEquals(List.of(50), AchievementRules.crossedEquipmentMilestones(4899, 4900));
        assertEquals(List.of(100), AchievementRules.crossedEquipmentMilestones(9899, 9900));
        assertEquals(List.of(25, 50, 100), AchievementRules.crossedEquipmentMilestones(0, 9900));
        assertEquals(List.of(), AchievementRules.crossedEquipmentMilestones(4900, 2400));
    }
}
