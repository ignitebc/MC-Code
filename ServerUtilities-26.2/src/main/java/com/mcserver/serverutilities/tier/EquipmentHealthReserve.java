package com.mcserver.serverutilities.tier;

import net.minecraft.server.level.ServerPlayer;

/**
 * 장비 때문에 잘린 체력을 보관했다가 장비로 최대 체력이 다시 늘면 돌려준다.
 *
 * <p>바닐라는 최대 체력이 줄면 현재 체력을 그만큼 잘라 내고, 다시 늘어도 채워 주지 않는다. 장비 등급에
 * 체력이 붙으면서 손에 든 도구를 바꾸거나 방어구를 갈아입기만 해도 체력이 사라지게 됐다. 그래서 장비
 * 변경으로 잘린 양만 기억했다가 장비 변경으로 최대 체력이 늘 때 늘어난 만큼까지만 돌려준다.
 * 돌려주는 양은 잘린 양을 넘지 않으므로 장비를 바꿔서 체력을 얻을 수는 없다.
 *
 * <p>장비 수정자는 저장되지 않아서 접속하거나 엔드에서 나올 때도 장비 없는 최대 체력으로 체력이
 * 잘린다. 이 양도 같은 보관분에 넣어 첫 장비 적용 때 돌려준다. 부활할 때는 바닐라가 장비 없는
 * 최대 체력으로 채우므로 첫 장비 적용 뒤에 한 번 더 가득 채운다.
 */
public final class EquipmentHealthReserve {
    private EquipmentHealthReserve() { }

    /**
     * 장비 수정자를 바꾼 직후에 호출한다. 바닐라가 같은 틱 뒤에서 체력을 잘라 내기 전이다.
     *
     * @param healthBefore    장비 수정자를 바꾸기 전 체력
     * @param maxHealthBefore 장비 수정자를 바꾸기 전 최대 체력
     */
    public static void afterEquipmentUpdate(ServerPlayer player, float healthBefore, float maxHealthBefore) {
        if (player.isDeadOrDying()) return;

        HealthReservePlayer reserve = (HealthReservePlayer) player;
        float maxHealthAfter = player.getMaxHealth();
        if (reserve.serverutilities$isRespawnHealPending()) {
            reserve.serverutilities$setRespawnHealPending(false);
            player.setHealth(maxHealthAfter);
            return;
        }

        if (maxHealthAfter < maxHealthBefore) {
            keepClippedHealth(reserve, healthBefore, maxHealthBefore, maxHealthAfter);
            return;
        }
        if (maxHealthAfter > maxHealthBefore) {
            restoreKeptHealth(player, reserve, maxHealthAfter - maxHealthBefore);
        }
    }

    /**
     * 장비 없는 최대 체력으로 체력이 잘린 양을 보관한다. 접속과 엔드 귀환 때 쓴다.
     *
     * @param healthBeforeClip 잘리기 전 체력
     */
    public static void keepHealthClippedWithoutEquipment(ServerPlayer player, float healthBeforeClip) {
        float clipped = healthBeforeClip - player.getHealth();
        if (clipped <= 0.0F) return;

        HealthReservePlayer reserve = (HealthReservePlayer) player;
        reserve.serverutilities$setHealthReserve(reserve.serverutilities$getHealthReserve() + clipped);
    }

    private static void keepClippedHealth(HealthReservePlayer reserve, float health,
                                          float maxHealthBefore, float maxHealthAfter) {
        // 다른 이유로 이미 잘릴 예정이던 양은 장비 때문이 아니므로 빼고 센다.
        float clippedBefore = Math.max(0.0F, health - maxHealthBefore);
        float clippedAfter = Math.max(0.0F, health - maxHealthAfter);
        float clippedByEquipment = clippedAfter - clippedBefore;
        if (clippedByEquipment <= 0.0F) return;

        reserve.serverutilities$setHealthReserve(reserve.serverutilities$getHealthReserve() + clippedByEquipment);
    }

    private static void restoreKeptHealth(ServerPlayer player, HealthReservePlayer reserve, float increase) {
        float kept = reserve.serverutilities$getHealthReserve();
        if (kept <= 0.0F) return;

        float room = player.getMaxHealth() - player.getHealth();
        float restored = Math.min(kept, Math.min(increase, room));
        if (restored <= 0.0F) return;

        player.setHealth(player.getHealth() + restored);
        reserve.serverutilities$setHealthReserve(kept - restored);
    }
}
