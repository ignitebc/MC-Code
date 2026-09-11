package com.tacz.guns.entity.shooter;

import com.tacz.guns.GunMod;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ShootResult;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.resource.pojo.data.gun.ChargeType;
import com.tacz.guns.resource.pojo.data.gun.ExtraDamage;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.LinkedList;

/** 기존 몬스터의 공격 대상을 향해 서버의 총기 발사·연사·볼트·과열 처리를 사용한다. */
public final class MonsterGunController {
    /**
     * 거리별 피해표가 없어 유효사거리를 읽을 수 없는 총기(유탄발사기, 로켓)에 쓸 사격 거리.
     * 몬스터의 기본 추적 범위와 같은 값이라 이 총기들은 종전과 똑같이 행동한다.
     */
    private static final double FALLBACK_RANGE = 32.0;
    /**
     * 사격 거리의 하한.
     * <p>
     * 산탄총처럼 유효사거리가 아주 짧은 총기를 그대로 쓰면 몬스터가 근접 공격 거리까지 붙어야
     * 발사해 총을 든 의미가 사라진다. 상한은 두지 않는다.
     */
    private static final double MINIMUM_RANGE = 12.0;
    /** 거리별 피해표의 "infinite" 구간은 이 값으로 들어온다. 거리 제한이 없다는 뜻이다. */
    private static final float UNLIMITED_DISTANCE = Float.MAX_VALUE;
    private static final Identifier FOLLOW_RANGE_MODIFIER_ID =
            Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "monster_gun_follow_range");

    private final Mob mob;
    private ItemStack drawnStack = ItemStack.EMPTY;
    private Identifier drawnId;
    private float chargeProgress;
    private double effectiveRange = FALLBACK_RANGE;

    public MonsterGunController(Mob mob) { this.mob = mob; }

    public static boolean isMonster(LivingEntity entity) {
        return entity instanceof Mob && (entity instanceof Enemy || entity.getType().getCategory() == MobCategory.MONSTER);
    }

    public void tick() {
        IGunOperator operator = IGunOperator.fromLivingEntity(this.mob);
        ItemStack stack = this.mob.getMainHandItem();
        if (!this.mob.isAlive() || this.mob.isNoAi() || !(stack.getItem() instanceof IGun gun)) {
            // 총을 막 내려놓은 틱에만 정리한다. 이후 틱에는 되돌릴 것이 남아 있지 않다.
            if (!this.drawnStack.isEmpty()) {
                operator.aim(false);
                clearFollowRange();
            }
            this.drawnStack = ItemStack.EMPTY;
            this.drawnId = null;
            this.chargeProgress = 0;
            this.effectiveRange = FALLBACK_RANGE;
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
            // 총기를 바꿀 때만 다시 읽는다. 매 틱 계산할 값이 아니다.
            this.effectiveRange = getEffectiveRange(data);
            applyFollowRange(this.effectiveRange);
        }
        double range = this.effectiveRange;
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

    /**
     * 총기의 유효사거리를 칸 단위로 돌려준다.
     * <p>
     * 거리별 피해표의 첫 구간까지가 피해가 깎이지 않는 범위이고, TACZ 자신도 이 값을 유효사거리로
     * 쓴다. 표가 없거나 첫 구간부터 거리 제한이 없는 총기는 기준을 잡을 수 없어 기본값을 쓰고,
     * 그 값이 너무 짧은 총기는 {@link #MINIMUM_RANGE}까지 끌어올린다.
     *
     * @see com.tacz.guns.resource.modifier.custom.EffectiveRangeModifier
     */
    public static double getEffectiveRange(GunData gunData) {
        ExtraDamage extraDamage = gunData.getBulletData().getExtraDamage();
        if (extraDamage == null) {
            return FALLBACK_RANGE;
        }
        LinkedList<ExtraDamage.DistanceDamagePair> damageAdjust = extraDamage.getDamageAdjust();
        if (damageAdjust == null || damageAdjust.isEmpty()) {
            return FALLBACK_RANGE;
        }
        float firstDistance = damageAdjust.get(0).getDistance();
        if (firstDistance <= 0 || firstDistance >= UNLIMITED_DISTANCE) {
            return FALLBACK_RANGE;
        }
        return Math.max(MINIMUM_RANGE, firstDistance);
    }

    /**
     * 총기 사거리만큼 떨어진 상대도 공격 대상으로 잡도록 추적 범위를 넓힌다.
     * <p>
     * 몬스터는 대상을 잡은 뒤에만 발사하므로, 사거리만 늘리고 추적 범위를 그대로 두면
     * 먼 거리의 상대를 아예 인지하지 못해 총을 쏘지 않는다.
     */
    private void applyFollowRange(double range) {
        AttributeInstance followRange = this.mob.getAttributes().getInstance(Attributes.FOLLOW_RANGE);
        if (followRange == null) {
            return;
        }
        followRange.removeModifier(FOLLOW_RANGE_MODIFIER_ID);
        double naturalRange = followRange.getValue();
        // 사거리가 더 짧아도 추적 범위는 줄이지 않는다. 줄이면 근접 행동까지 망가진다.
        if (range <= naturalRange) {
            return;
        }
        followRange.addTransientModifier(new AttributeModifier(
                FOLLOW_RANGE_MODIFIER_ID, range - naturalRange, AttributeModifier.Operation.ADD_VALUE));
    }

    private void clearFollowRange() {
        AttributeInstance followRange = this.mob.getAttributes().getInstance(Attributes.FOLLOW_RANGE);
        if (followRange != null) {
            followRange.removeModifier(FOLLOW_RANGE_MODIFIER_ID);
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
