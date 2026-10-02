package com.daqem.jobsplus.mixin.achievement;

import com.daqem.jobsplus.achievement.AchievementManager;
import com.daqem.jobsplus.achievement.AchievementRules;
import com.mcserver.serverutilities.level.ToolLevelRules;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.Deque;

@Mixin(value = ToolLevelRules.class, remap = false)
public abstract class MixinAchievementTool
{
    @Unique private static final ThreadLocal<Deque<Integer>> jobsplus$toolExperience = ThreadLocal.withInitial(ArrayDeque::new);

    @Inject(method = "addExperience", at = @At("HEAD"), remap = false)
    private static void jobsplus$beforeExperience(ServerPlayer player, ItemStack stack, CallbackInfo ci)
    {
        jobsplus$toolExperience.get().push(ToolLevelRules.totalExperience(stack));
    }

    @Inject(method = "addExperience", at = @At("RETURN"), remap = false)
    private static void jobsplus$afterExperience(ServerPlayer player, ItemStack stack, CallbackInfo ci)
    {
        Deque<Integer> experiences = jobsplus$toolExperience.get();
        int before = experiences.pop();
        if (experiences.isEmpty())
        {
            jobsplus$toolExperience.remove();
        }
        int after = ToolLevelRules.totalExperience(stack);
        if (after > before)
        {
            AchievementManager.recordEquipment(player, AchievementManager.equipmentId(stack), "tool",
                    AchievementRules.toolType(stack), before, after);
            player.getInventory().setChanged();
        }
    }
}
