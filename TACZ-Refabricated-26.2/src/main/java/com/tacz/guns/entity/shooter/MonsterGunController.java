package com.tacz.guns.entity.shooter;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ShootResult;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.resource.pojo.data.gun.ChargeType;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;

/** 기존 몬스터의 공격 대상을 향해 서버의 총기 발사·연사·볼트·과열 처리를 사용한다. */
public final class MonsterGunController {
    private final Mob mob;
    private ItemStack drawnStack = ItemStack.EMPTY;
    private Identifier drawnId;
    private float chargeProgress;

    public MonsterGunController(Mob mob) { this.mob = mob; }

    public static boolean isMonster(LivingEntity entity) {
        return entity instanceof Mob && (entity instanceof Enemy || entity.getType().getCategory() == MobCategory.MONSTER);
    }

    public void tick() {
        IGunOperator operator = IGunOperator.fromLivingEntity(this.mob);
        ItemStack stack = this.mob.getMainHandItem();
        if (!this.mob.isAlive() || this.mob.isNoAi() || !(stack.getItem() instanceof IGun gun)) {
            if (!this.drawnStack.isEmpty()) operator.aim(false);
            this.drawnStack = ItemStack.EMPTY;
            this.drawnId = null;
            this.chargeProgress = 0;
            return;
        }
        Identifier id = gun.getGunId(stack);
        var index = TimelessAPI.getCommonGunIndex(id);
        if (index.isEmpty()) return;
        var data = index.get().getGunData();
        if (this.drawnStack != stack || !id.equals(this.drawnId)) {
            operator.draw(this.mob::getMainHandItem);
            this.drawnStack = stack;
            this.drawnId = id;
            this.chargeProgress = 0;
            gun.setBulletInBarrel(stack, true);
        }
        var followRange = this.mob.getAttribute(Attributes.FOLLOW_RANGE);
        double range = followRange == null ? 32.0 : Math.clamp(followRange.getValue(), 16.0, 64.0);
        LivingEntity target = findTarget(range);
        boolean canShoot = target != null && target.isAlive() && target != this.mob
                && !(target instanceof Player player && (player.isCreative() || player.isSpectator()))
                && this.mob.distanceToSqr(target) <= range * range && this.mob.hasLineOfSight(target);
        operator.aim(canShoot);
        if (!canShoot) {
            this.chargeProgress = 0;
            return;
        }
        // 몬스터는 탄약 아이템 없이 장전하지만, 장전 시간은 플레이어와 똑같이 기다린다.
        if (operator.getDataHolder().reloadStateType.isReloading()) {
            this.chargeProgress = 0;
            return;
        }
        var charge = data.getChargeData(gun.getFireMode(stack));
        if (charge != null) {
            if (charge.isChargeDuringCooldown() || operator.getSynShootCoolDown() <= 0) {
                this.chargeProgress = Math.min(charge.getMaxCharge(),
                        this.chargeProgress + Math.max(0, charge.getIncreasePerTick()));
            }
            if (this.chargeProgress < Math.min(charge.getFireThreshold(), charge.getMaxCharge())) return;
        } else {
            this.chargeProgress = 0;
        }
        if (operator.getSynShootCoolDown() > 0 || operator.getSynDrawCoolDown() > 0 || operator.getSynIsBolting()) return;
        double dx = target.getX() - this.mob.getX();
        double dz = target.getZ() - this.mob.getZ();
        double dy = target.getY(0.5) - this.mob.getEyeY();
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
        this.mob.getLookControl().setLookAt(target, 30.0f, 30.0f);
        ShootResult result = operator.shoot(() -> pitch, () -> yaw,
                System.currentTimeMillis() - operator.getDataHolder().baseTimestamp, this.chargeProgress);
        if (result == ShootResult.NEED_BOLT) operator.bolt();
        if (result == ShootResult.NO_AMMO) operator.reload();
        if (result == ShootResult.SUCCESS && charge != null) {
            this.chargeProgress = charge.getChargeType() == ChargeType.DELAY ? 0
                    : Math.max(0, this.chargeProgress - charge.getDecreaseOnFire());
        }
    }

    private LivingEntity findTarget(double range) {
        LivingEntity target = this.mob.getTarget();
        if (target == null && this.mob.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
            target = this.mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        }
        // 드래곤은 일반 Mob의 target 필드 대신 전투 페이즈에서 플레이어를 선택한다.
        if (target == null && this.mob instanceof EnderDragon && this.mob.level() instanceof ServerLevel level) {
            target = level.players().stream().filter(player -> player.isAlive() && !player.isCreative() && !player.isSpectator())
                    .filter(player -> this.mob.distanceToSqr(player) <= range * range)
                    .min(Comparator.comparingDouble(this.mob::distanceToSqr)).orElse(null);
        }
        return target;
    }
}
