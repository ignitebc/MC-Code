package com.daqem.jobsplus.achievement;

import java.util.List;

/** 목표와 선행 조건은 서버와 화면이 같은 정의를 사용한다. */
public record AchievementDefinition(String id, String name, int difficulty, int diamonds,
                                    List<String> parents, int requiredParents, List<Objective> objectives,
                                    String details)
{
    public AchievementDefinition
    {
        parents = List.copyOf(parents);
        objectives = List.copyOf(objectives);
        if (diamonds < 1 || diamonds > 30 || difficulty < 1 || difficulty > 4 || objectives.isEmpty())
        {
            throw new IllegalArgumentException("Invalid achievement: " + id);
        }
        if (requiredParents < 0 || requiredParents > parents.size())
        {
            throw new IllegalArgumentException("Invalid achievement parents: " + id);
        }
    }

    public boolean isUnlocked(AchievementProgress progress)
    {
        int completedParents = 0;
        for (String parent : parents)
        {
            if (progress.completed().contains(parent))
            {
                completedParents++;
            }
        }
        if (completedParents >= requiredParents)
        {
            return true;
        }
        return false;
    }

    public boolean hasMetObjectives(AchievementProgress progress)
    {
        for (Objective objective : objectives)
        {
            if (progress.value(objective.key()) < objective.target())
            {
                return false;
            }
        }
        return true;
    }

    public record Objective(String key, long target, String label)
    {
        public Objective
        {
            if (target <= 0)
            {
                throw new IllegalArgumentException("Invalid achievement target: " + key);
            }
        }
    }
}
