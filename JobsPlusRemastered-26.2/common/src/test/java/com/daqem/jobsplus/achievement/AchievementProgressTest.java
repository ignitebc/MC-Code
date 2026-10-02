package com.daqem.jobsplus.achievement;

import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AchievementProgressTest
{
    @Test
    void countersSaturateInsteadOfOverflowingAndIgnoreInvalidIncrements()
    {
        AchievementProgress progress = new AchievementProgress();
        progress.set("ores", Long.MAX_VALUE - 1);
        progress.add("ores", 100);
        assertEquals(Long.MAX_VALUE, progress.value("ores"));
        assertFalse(progress.add("ores", -1));
        assertFalse(progress.add("ores", 0));
        assertEquals(Long.MAX_VALUE, progress.value("ores"));
    }

    @Test
    void savingAndLoadingPreservesCompletionsAndIndividualGearMilestones()
    {
        AchievementProgress progress = new AchievementProgress();
        String itemId = "gun:61f57c65-d57e-493b-8cde-f2c2ab2aef91";
        progress.set("gear_milestone:" + itemId + ":100", 1);
        progress.equipment().put(itemId, new AchievementProgress.GearProgress("gun", "tacz:ak47", 1, 100));
        progress.completed().add("E01");
        var encoded = AchievementProgress.CODEC.encodeStart(JsonOps.INSTANCE, progress).getOrThrow();
        AchievementProgress loaded = AchievementProgress.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow();
        assertEquals(progress, loaded);
        assertEquals(0, loaded.value("gear_milestone:" + itemId + ":50"));
    }
}
