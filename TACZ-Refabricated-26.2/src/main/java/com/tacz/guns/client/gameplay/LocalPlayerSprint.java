package com.tacz.guns.client.gameplay;

import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ReloadState;
import net.minecraft.client.player.LocalPlayer;

public class LocalPlayerSprint {
    private final LocalPlayerDataHolder data;
    private final LocalPlayer player;

    public static boolean stopSprint = false;

    public LocalPlayerSprint(LocalPlayerDataHolder data, LocalPlayer player) {
        this.data = data;
        this.player = player;
    }

    /**
     * 상황에 따라 플레이어가 있어야 할 질주 상태를 돌려준다. 플레이어가 질주 상태를 바꿀 때 호출된다.
     * 이 로직은 서버와 정확히 대응해야 하며, 다르면 클라이언트 표시와 서버 상태가 어긋난다.
     * (예: 클라이언트에서는 질주하는 것처럼 보이지만 서버에서는 실제로 질주하지 않음)
     *
     * @see com.tacz.guns.entity.shooter.LivingEntitySprint#getProcessedSprintStatus
     */
    public boolean getProcessedSprintStatus(boolean sprinting) {
        // 이 로직은 서버와 정확히 대응해야 하며, 다르면 클라이언트 표시와 서버 상태가 어긋난다.
        // (예: 클라이언트에서는 질주하는 것처럼 보이지만 서버에서는 실제로 질주하지 않음)
        IGunOperator gunOperator = IGunOperator.fromLivingEntity(player);
        ReloadState.StateType reloadStateType = gunOperator.getSynReloadState().getStateType();
        if (gunOperator.getSynIsAiming() || (reloadStateType.isReloading() && !reloadStateType.isReloadFinishing()) || stopSprint) {
            return false;
        } else {
            return sprinting;
        }
    }
}
