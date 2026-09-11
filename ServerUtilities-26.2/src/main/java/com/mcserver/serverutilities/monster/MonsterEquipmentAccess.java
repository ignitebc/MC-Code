package com.mcserver.serverutilities.monster;

public interface MonsterEquipmentAccess {
    boolean serverutilities$equipmentRolled();
    boolean serverutilities$equipmentPending();
    void serverutilities$finishEquipmentRoll(boolean armorEquipped, boolean weaponEquipped);
}
