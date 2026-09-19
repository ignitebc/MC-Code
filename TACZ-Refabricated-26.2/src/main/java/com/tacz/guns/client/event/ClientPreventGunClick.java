package com.tacz.guns.client.event;

import cn.sh1rocu.tacz.api.event.InputEvent;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.block.StatueBlock;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * 총을 든 동안에는 블록·엔티티와 상호작용하지 않는다. 우클릭은 조준에만 쓴다.
 * 문을 열거나 상자를 열려면 총이 아닌 것을 들어야 한다.
 */
@Environment(EnvType.CLIENT)
public class ClientPreventGunClick {
    public static void onClickInput(InputEvent.InteractionKeyMappingTriggered event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        ItemStack itemInHand = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!(itemInHand.getItem() instanceof IGun)) {
            return;
        }
        // 총을 걸어 두는 곳만은 예외다. 총을 들고 있어야 쓸 수 있는 기능이기 때문이다.
        HitResult hitResult = Minecraft.getInstance().hitResult;
        if (hitResult instanceof EntityHitResult entityHitResult && entityHitResult.getEntity() instanceof ItemFrame) {
            return;
        }
        if (hitResult instanceof BlockHitResult blockHitResult
                && player.level().getBlockState(blockHitResult.getBlockPos()).getBlock() instanceof StatueBlock) {
            return;
        }
        // false로 두면 클라이언트의 팔 휘두름과 입자도 함께 막힌다.
        event.setSwingHand(false);
        event.setCanceled(true);
    }
}
