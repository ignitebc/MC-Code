package com.mcserver.serverutilities.monster;

public interface MonsterEquipmentAccess {
    boolean serverutilities$equipmentRolled();
    boolean serverutilities$equipmentPending();
    void serverutilities$finishEquipmentRoll(boolean armorEquipped, boolean weaponEquipped, int level);

    /** 방어구와 무기를 모두 벗긴 뒤 부른다. 추첨 장비 기록을 지우고, 레벨을 표시하는 개체는 장비 없는 레벨로 다시 계산한다. */
    void serverutilities$clearRandomEquipment();

    /** 머리 위에 표시할 레벨. 표시하지 않는 개체는 {@link MonsterLevel#NONE} */
    int serverutilities$monsterLevel();

    /** 클라이언트가 서버에서 받은 레벨을 기록한다. */
    void serverutilities$setMonsterLevel(int level);
}
