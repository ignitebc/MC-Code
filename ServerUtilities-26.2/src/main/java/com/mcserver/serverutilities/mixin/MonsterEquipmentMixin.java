package com.mcserver.serverutilities.mixin;

import com.mcserver.serverutilities.monster.MonsterEquipmentAccess;
import com.mcserver.serverutilities.monster.MonsterEquipmentRules;
import com.mcserver.serverutilities.monster.MonsterLevel;
import com.mcserver.serverutilities.monster.MonsterLevelRewards;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
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

    /**
     * 불러온 개체의 레벨.
     * <p>
     * 크리퍼 레벨은 추첨값이라 장비로 다시 만들 수 없으므로 저장값을 쓴다. 장비 몬스터는 계산 기준이 바뀌어도
     * 새 기준을 따르도록 저장값 대신 지급 기록과 현재 장비로 다시 계산한다. 생성 때 레벨 대상이었는지는 저장된
     * 레벨로 판단하므로 차원을 옮긴 개체도 레벨을 유지한다. 레벨 기능 이전에 저장된 개체는 현재 차원으로 판단한다.
     */
    @Unique
    private int serverutilities$loadLevel(ValueInput input) {
        Mob mob = (Mob) (Object) this;
        int savedLevel = input.getIntOr("ServerUtilitiesMonsterLevel", SERVERUTILITIES_LEVEL_NOT_SAVED);
        if (mob instanceof Creeper) {
            if (savedLevel == SERVERUTILITIES_LEVEL_NOT_SAVED) return MonsterLevel.NONE;
            return savedLevel;
        }
        if (!serverutilities$equipmentRolled) return MonsterLevel.NONE;
        if (savedLevel == SERVERUTILITIES_LEVEL_NOT_SAVED) {
            return MonsterEquipmentRules.calculateLevel(mob, serverutilities$randomArmor, serverutilities$randomWeapon);
        }
        if (savedLevel == MonsterLevel.NONE) return MonsterLevel.NONE;
        return MonsterEquipmentRules.equipmentLevel(mob, serverutilities$randomArmor, serverutilities$randomWeapon);
    }

    /** 추첨으로 지급한 칸은 주운 아이템으로 바꾸지 않는다. 줍기를 막지 않는 피글린이 대상이다. */
    @Inject(method = "equipItemIfPossible", at = @At("HEAD"), cancellable = true)
    private void serverutilities$keepRandomEquipment(ServerLevel level, ItemStack stack,
            CallbackInfoReturnable<ItemStack> cir) {
        EquipmentSlot slot = ((Mob) (Object) this).getEquipmentSlotForItem(stack);
        boolean randomSlot = MonsterEquipmentRules.isRandomEquipmentSlot(slot,
                serverutilities$randomArmor, serverutilities$randomWeapon);
        if (randomSlot) cir.setReturnValue(ItemStack.EMPTY);
    }

    @Inject(method = "dropCustomDeathLoot", at = @At("HEAD"))
    private void serverutilities$prepareDeathDrops(ServerLevel level, DamageSource source,
            boolean killedByPlayer, CallbackInfo ci) {
        MonsterEquipmentRules.preventEquipmentDrops((Mob) (Object) this,
                serverutilities$randomArmor, serverutilities$randomWeapon);
        // 기존 전리품 처리는 취소하지 않고 레벨 재료 보상만 별도로 추가한다.
        MonsterLevelRewards.dropMaterialReward(level, (Mob) (Object) this,
                serverutilities$monsterLevel, killedByPlayer);
    }
}
