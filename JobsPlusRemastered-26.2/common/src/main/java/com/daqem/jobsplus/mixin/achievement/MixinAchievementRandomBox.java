package com.daqem.jobsplus.mixin.achievement;

import com.autovw.advancednetherite.common.item.RandomBoxItem;
import com.daqem.jobsplus.achievement.AchievementManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 랜덤 상자를 열어 보상을 받은 경우만 센다. 열쇠 부족·인벤토리 부족으로 되돌린 경우는 실패를 돌려준다.
 * 클라이언트는 입력 흐름을 위해 항상 성공을 돌려주므로 서버에서만 확인한다.
 */
@Mixin(RandomBoxItem.class)
public abstract class MixinAchievementRandomBox
{
    @Inject(method = "use", at = @At("RETURN"))
    private void jobsplus$recordRandomBox(Level level, Player player, InteractionHand hand,
                                          CallbackInfoReturnable<InteractionResult> cir)
    {
        if (!level.isClientSide() && cir.getReturnValue() instanceof InteractionResult.Success
                && player instanceof ServerPlayer serverPlayer)
        {
            AchievementManager.recordRandomBox(serverPlayer, BuiltInRegistries.ITEM.getKey((Item) (Object) this));
        }
    }
}
