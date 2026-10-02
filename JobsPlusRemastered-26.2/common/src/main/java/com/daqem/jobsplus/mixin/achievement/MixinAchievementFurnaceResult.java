package com.daqem.jobsplus.mixin.achievement;

import com.daqem.jobsplus.achievement.AchievementManager;
import com.daqem.jobsplus.achievement.ProductionContainer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FurnaceResultSlot.class)
public abstract class MixinAchievementFurnaceResult
{
    @Shadow @Final private Player player;
    @Shadow private int removeCount;

    @Inject(method = "checkTakeAchievements", at = @At("HEAD"))
    private void jobsplus$countTakenResults(ItemStack stack, CallbackInfo ci)
    {
        Slot slot = (Slot) (Object) this;
        if (!(player instanceof ServerPlayer serverPlayer) || removeCount <= 0
                || !(slot.container instanceof ProductionContainer production))
        {
            return;
        }
        int credited = production.jobsplus$getProductionTracker().take(2, removeCount, player.getUUID().toString());
        AchievementManager.add(serverPlayer, "smelted", credited);
        if (slot.container instanceof BlockEntity blockEntity)
        {
            blockEntity.setChanged();
        }
    }
}
