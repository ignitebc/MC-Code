package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.monster.MonsterEquipmentAccess;
import com.mcserver.serverutilities.monster.MonsterEquipmentRules;
import com.mcserver.serverutilities.monster.MonsterLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class MonsterEquipmentMixin implements MonsterEquipmentAccess {
    /** 레벨 기능 이전에 저장된 개체를 구분하기 위한 값 */
    @Unique private static final int SERVERUTILITIES_LEVEL_NOT_SAVED = -1;

    @Unique private boolean serverutilities$equipmentRolled;
    @Unique private boolean serverutilities$equipmentPending;
    @Unique private boolean serverutilities$randomArmor;
    @Unique private boolean serverutilities$randomWeapon;
    @Unique private int serverutilities$monsterLevel;

    @Override
    public boolean serverutilities$equipmentRolled() { return serverutilities$equipmentRolled; }

    @Override
    public boolean serverutilities$equipmentPending() { return serverutilities$equipmentPending; }

    @Override
    public void serverutilities$finishEquipmentRoll(boolean armorEquipped, boolean weaponEquipped, int level) {
        serverutilities$equipmentRolled = true;
        serverutilities$equipmentPending = false;
        serverutilities$randomArmor = armorEquipped;
        serverutilities$randomWeapon = weaponEquipped;
        serverutilities$monsterLevel = level;
    }

    @Override
    public int serverutilities$monsterLevel() { return serverutilities$monsterLevel; }

    @Override
    public void serverutilities$setMonsterLevel(int level) { serverutilities$monsterLevel = level; }

    @Inject(method = "finalizeSpawn", at = @At("TAIL"))
    private void serverutilities$prepareEquipment(ServerLevelAccessor level, DifficultyInstance difficulty,
            EntitySpawnReason reason, SpawnGroupData groupData, CallbackInfoReturnable<SpawnGroupData> cir) {
        // 하위 몬스터의 기본 장비 배정이 끝난 뒤 월드에 추가되는 시점에 지급한다.
        if (!serverutilities$equipmentRolled && MonsterEquipmentRules.isMonster((Mob) (Object) this)) {
            serverutilities$equipmentPending = true;
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void serverutilities$saveEquipment(ValueOutput output, CallbackInfo ci) {
        if (!MonsterEquipmentRules.isMonster((Mob) (Object) this)) return;
        output.putBoolean("ServerUtilitiesEquipmentRolled", serverutilities$equipmentRolled);
        output.putBoolean("ServerUtilitiesEquipmentPending", serverutilities$equipmentPending);
        output.putBoolean("ServerUtilitiesRandomArmor", serverutilities$randomArmor);
        output.putBoolean("ServerUtilitiesRandomWeapon", serverutilities$randomWeapon);
        output.putInt("ServerUtilitiesMonsterLevel", serverutilities$monsterLevel);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void serverutilities$loadEquipment(ValueInput input, CallbackInfo ci) {
        serverutilities$equipmentRolled = input.getBooleanOr("ServerUtilitiesEquipmentRolled", false);
        serverutilities$equipmentPending = input.getBooleanOr("ServerUtilitiesEquipmentPending", false);
        // 방어구와 무기를 함께 추첨하던 시절의 기록은 양쪽 모두 지급한 것으로 읽는다.
        boolean legacyEquipment = input.getBooleanOr("ServerUtilitiesRandomEquipment", false);
        serverutilities$randomArmor = input.getBooleanOr("ServerUtilitiesRandomArmor", legacyEquipment);
        serverutilities$randomWeapon = input.getBooleanOr("ServerUtilitiesRandomWeapon", legacyEquipment);
        if (serverutilities$randomArmor || serverutilities$randomWeapon) {
            serverutilities$equipmentRolled = true;
            serverutilities$equipmentPending = false;
            MonsterEquipmentRules.preventEquipmentDrops((Mob) (Object) this,
                    serverutilities$randomArmor, serverutilities$randomWeapon);
        }
        serverutilities$monsterLevel = serverutilities$loadLevel(input);
    }

    /** 저장된 레벨. 레벨 기능 이전에 추첨을 마친 개체는 지급 기록과 현재 장비로 다시 계산한다. */
    @Unique
    private int serverutilities$loadLevel(ValueInput input) {
        int savedLevel = input.getIntOr("ServerUtilitiesMonsterLevel", SERVERUTILITIES_LEVEL_NOT_SAVED);
        if (savedLevel != SERVERUTILITIES_LEVEL_NOT_SAVED) return savedLevel;
        if (!serverutilities$equipmentRolled) return MonsterLevel.NONE;
        return MonsterEquipmentRules.calculateLevel((Mob) (Object) this,
                serverutilities$randomArmor, serverutilities$randomWeapon);
    }

    @Inject(method = "dropCustomDeathLoot", at = @At("HEAD"))
    private void serverutilities$noEquipmentDrops(ServerLevel level, DamageSource source,
            boolean killedByPlayer, CallbackInfo ci) {
        MonsterEquipmentRules.preventEquipmentDrops((Mob) (Object) this,
                serverutilities$randomArmor, serverutilities$randomWeapon);
    }
}
