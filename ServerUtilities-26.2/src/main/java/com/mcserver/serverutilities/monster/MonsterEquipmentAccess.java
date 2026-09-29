package com.mcserver.serverutilities.monster;

public interface MonsterEquipmentAccess {
    boolean serverutilities$equipmentRolled();
    boolean serverutilities$equipmentPending();
    void serverutilities$finishEquipmentRoll(boolean armorEquipped, boolean weaponEquipped, int level);

    /** 머리 위에 표시할 레벨. 표시하지 않는 개체는 {@link MonsterLevel#NONE} */
    int serverutilities$monsterLevel();

    /** 클라이언트가 서버에서 받은 레벨을 기록한다. */
    void serverutilities$setMonsterLevel(int level);
}
