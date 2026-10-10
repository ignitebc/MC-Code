package com.mcserver.serverutilities.death;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 엔드(엔드 시티가 있는 바깥 섬과 드래곤 섬 모두)에서 공허로 떨어져 죽으면 소지품을 모두 공허로 잃는다.
 * <p>
 * 유품 상자는 공허 사망 때 월드 최소 높이에 놓여 섬 아래 허공에 뜨므로 엔드에서는 만들지 않는다.
 * 공허 피해는 바닐라에서도 불사의 토템이 통하지 않는다. 아이템 보존권은 다른 사망과 같이 먼저 소모되어 소지품을 지킨다.
 * <p>
 * 손실 루프가 있는 {@link DeathRules#beforeDrops}의 삭제 호출은 Advanced Netherite가 가로채므로, 비우는 일은 이 클래스에서 따로 한다.
 */
public final class EndVoidDeath {
    public static final Component LOST_MESSAGE = Component.literal("모든 소지품이 공허로 사라집니다.");

    private EndVoidDeath() { }

    /** 엔드에서 공허 피해로 죽었는지. {@code /kill}은 공허 추락이 아니므로 다른 사망과 같이 처리한다. */
    public static boolean isEndVoidDeath(ServerPlayer player, DamageSource source) {
        boolean inEnd = Level.END.equals(player.level().dimension());
        return inEnd && source.is(DamageTypes.FELL_OUT_OF_WORLD);
    }

    /** 배낭 칸을 포함한 인벤토리 전체와 커서에 든 아이템, 2×2 제작 칸을 비운다. 아무것도 떨어뜨리지 않는다. */
    public static void discardAll(ServerPlayer player) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            inventory.removeItemNoUpdate(slot);
        }
        inventory.setChanged();
        // 메뉴가 닫힐 때 커서와 제작 칸의 아이템은 사망 위치에 떨어지므로 미리 지운다.
        player.containerMenu.setCarried(ItemStack.EMPTY);
        player.inventoryMenu.getCraftSlots().clearContent();
    }
}
