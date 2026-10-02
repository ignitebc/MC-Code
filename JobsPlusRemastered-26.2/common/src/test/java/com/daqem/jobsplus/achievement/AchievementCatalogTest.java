package com.daqem.jobsplus.achievement;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AchievementCatalogTest
{
    @Test
    void allSixCategoriesMatchTheRequestedQuestAndRewardTotals()
    {
        Map<String, Integer> counts = Map.of("A", 20, "B", 30, "C", 20, "D", 10, "E", 12, "F", 8);
        Map<String, Integer> rewards = Map.of("A", 272, "B", 295, "C", 294, "D", 115, "E", 161, "F", 119);
        for (String category : counts.keySet())
        {
            var definitions = AchievementCatalog.all().stream()
                    .filter(definition -> definition.id().startsWith(category)).toList();
            assertEquals(counts.get(category).intValue(), definitions.size());
            assertEquals(rewards.get(category).intValue(), definitions.stream().mapToInt(AchievementDefinition::diamonds).sum());
        }
        assertEquals(100, AchievementCatalog.all().size());
        assertEquals(1256, AchievementCatalog.all().stream().mapToInt(AchievementDefinition::diamonds).sum());
    }

    @Test
    void laterMiningTargetsReuseTheSeasonTotal()
    {
        AchievementProgress progress = new AchievementProgress();
        progress.add("ores", 500);
        assertTrue(AchievementCatalog.get("B01").hasMetObjectives(progress));
        assertFalse(AchievementCatalog.get("B02").isUnlocked(progress));
        progress.completed().add("B01");
        progress.add("ores", 1500);
        assertTrue(AchievementCatalog.get("B02").isUnlocked(progress));
        assertTrue(AchievementCatalog.get("B02").hasMetObjectives(progress));
        assertEquals(2000, progress.value("ores"));
    }

    @Test
    void twoMasterQuestsUnlockA14ButBothE05ParentsAreRequired()
    {
        AchievementProgress progress = new AchievementProgress();
        progress.completed().add("A06");
        assertFalse(AchievementCatalog.get("A14").isUnlocked(progress));
        progress.completed().add("A13");
        assertTrue(AchievementCatalog.get("A14").isUnlocked(progress));
        progress.completed().add("E03");
        assertFalse(AchievementCatalog.get("E05").isUnlocked(progress));
        progress.completed().add("E04");
        assertTrue(AchievementCatalog.get("E05").isUnlocked(progress));
    }

    @Test
    void compositeCropObjectivesRequireEveryCrop()
    {
        AchievementProgress progress = new AchievementProgress();
        progress.set("harvest:wheat", 1000);
        progress.set("harvest:carrot", 1000);
        progress.set("harvest:potato", 1000);
        progress.set("harvest:beetroot", 999);
        assertFalse(AchievementCatalog.get("B10").hasMetObjectives(progress));
        progress.add("harvest:beetroot", 1);
        assertTrue(AchievementCatalog.get("B10").hasMetObjectives(progress));
    }
}
