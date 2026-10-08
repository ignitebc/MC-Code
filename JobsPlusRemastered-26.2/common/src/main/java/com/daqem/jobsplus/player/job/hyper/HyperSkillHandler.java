package com.daqem.jobsplus.player.job.hyper;

import com.daqem.arc.player.SkillActivationNotifier;
import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.accessor.HyperPlayerAccess;
import com.daqem.jobsplus.networking.c2s.ServerboundHyperLeapPacket;
import com.daqem.jobsplus.networking.s2c.ClientboundHyperLeapPacket;
import com.daqem.jobsplus.networking.s2c.ClientboundHyperStatusPacket;
import com.mcserver.serverutilities.combat.CombatRules;
import dev.architectury.networking.NetworkManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

public final class HyperSkillHandler
{
    private HyperSkillHandler() {}

    public static HyperPlayerState state(ServerPlayer player)
    {
        return ((HyperPlayerAccess) player).jobsplus$getHyperState();
    }

    public static long now(ServerPlayer player)
    {
        return player.level().getServer().overworld().getGameTime();
    }

    public static void tick(ServerPlayer player)
    {
        HyperPlayerState state = state(player);
        long now = now(player);
        // 바닐라 흡수 효과는 관전자·크리에이티브에서도 시간이 흐르므로 몫도 같은 조건으로 줄인다.
        if (player.isAlive())
        {
            FoodAbsorptionStack.tick(player, state);
        }
        if (!player.isAlive() || player.isSpectator() || player.isCreative())
        {
            state.clearTransient();
            ((HyperPlayerAccess) player).jobsplus$setShieldVisual((byte) 0);
            sync(player);
            return;
        }
        int smithLevel = HyperSkillRules.getActiveLevel(player, HyperSkillRules.SMITH);
        if (smithLevel > 0 && state.shieldReadyAt < 0)
        {
            state.shieldReadyAt = now + HyperSkillRules.getShieldCooldownTicks(smithLevel);
        }
        if (state.restoreFallProtection)
        {
            state.restoreFallProtection = false;
            state.leapProtected = !player.onGround();
            state.leftGround = state.leapProtected;
            state.leapStartedAt = now;
            // 재접속·차원 전환 뒤에는 추진을 다시 주지 않고 착지 보호만 복원한다.
            state.leapMotionTicks = 0;
        }
        if (state.chargeStartedAt >= 0)
        {
            // START는 같은 틱의 바닐라 웅크리기 입력보다 먼저 도착할 수 있다.
            if (!canCharge(player) || (now > state.chargeStartedAt && !player.isShiftKeyDown()))
            {
                state.chargeStartedAt = -1;
            }
            else if (now - state.chargeStartedAt >= HyperSkillRules.LEAP_CHARGE_TICKS)
            {
                state.chargeStartedAt = -1;
                launchLeap(player, state, now);
            }
        }
        if (state.leapProtected)
        {
            state.suppressMovementUntil = now + 2;
            player.resetFallDistance();
            if (CombatRules.isFlightRestricted(player))
            {
                state.clearTransient();
                CombatRules.punishFlight(player);
                sync(player);
                return;
            }
            // 서버의 예측 이동은 매 틱 원위치로 돌아가므로 실제 수신 위치의 지면도 확인한다.
            if (!player.onGround() && player.level().noCollision(player,
                    player.getBoundingBox().move(0, -0.08D, 0))) state.leftGround = true;
            if (player.isInWater() || player.isInLava() || player.isPassenger()
                    || player.isFallFlying() || player.isSleeping())
            {
                finishLeap(player, false);
            }
            else if (isGrounded(player) && (state.leftGround || now - state.leapStartedAt > 5))
            {
                finishLeap(player, state.leftGround);
            }
        }
        byte shieldVisual = 0;
        if (state.shieldUntil > now) shieldVisual = 2;
        else if (smithLevel > 0 && state.shieldReadyAt >= 0 && state.shieldReadyAt <= now) shieldVisual = 1;
        ((HyperPlayerAccess) player).jobsplus$setShieldVisual(shieldVisual);
        if (player.tickCount % 5 == 0) sync(player);
    }

    private static void finishLeap(ServerPlayer player, boolean landed)
    {
        HyperPlayerState state = state(player);
        state.leapProtected = false;
        state.leapMotionTicks = 0;
        state.leapMotionStopped = true;
        state.suppressMovementUntil = now(player) + 2;
        if (landed) state.landingUntil = now(player) + HyperSkillRules.LANDING_PROTECTION_TICKS;
        player.resetFallDistance();
        player.setDeltaMovement(0, player.getDeltaMovement().y, 0);
        player.hurtMarked = true;
        sync(player);
    }

    public static boolean blocksDamage(ServerPlayer player, DamageSource source, float amount)
    {
        if (amount <= 0 || !player.isAlive() || source.is(DamageTypes.FELL_OUT_OF_WORLD)
                || source.is(DamageTypes.GENERIC_KILL)) return false;
        HyperPlayerState state = state(player);
        long now = now(player);
        if (state.leapProtected && (source.is(DamageTypeTags.IS_FALL)
                || source.is(DamageTypes.FLY_INTO_WALL) || source.is(DamageTypes.IN_WALL))) return true;
        if (state.shieldUntil > now || state.landingUntil > now) return true;
        int level = HyperSkillRules.getActiveLevel(player, HyperSkillRules.SMITH);
        if (level == 0 || state.shieldReadyAt < 0 || state.shieldReadyAt > now
                || player.isInvulnerableTo(player.level(), source)) return false;
        state.shieldUntil = now + HyperSkillRules.SHIELD_DURATION_TICKS;
        state.shieldReadyAt = state.shieldUntil + HyperSkillRules.getShieldCooldownTicks(level);
        ((HyperPlayerAccess) player).jobsplus$setShieldVisual((byte) 2);
        SkillActivationNotifier.notifySkillActivated(player, JobsPlus.translatable("hyper.smith.activated"));
        sync(player);
        return true;
    }

    public static void onDamageDealt(LivingEntity target, DamageSource source, float healthBefore)
    {
        if (!(source.getEntity() instanceof ServerPlayer player) || target.getHealth() >= healthBefore
                || target instanceof TamableAnimal || target.getType().getCategory() != MobCategory.MONSTER
                || source.is(DamageTypeTags.IS_FIRE)) return;
        if (!isOwnAttack(player, source)) return;
        int level = HyperSkillRules.getActiveLevel(player, HyperSkillRules.HUNTER);
        if (level == 0 || player.getHealth() >= player.getMaxHealth()) return;
        HyperPlayerState state = state(player);
        long now = now(player);
        // 총알의 일반·관통 피해, 산탄·휩쓸기 등 같은 틱의 다중 피해는 한 번만 추첨한다.
        if (state.leechReadyAt > now || state.lastLeechAttempt == now) return;
        state.lastLeechAttempt = now;
        if (player.getRandom().nextDouble() * 100.0D >= HyperSkillRules.getHunterChance(level)) return;
        state.leechReadyAt = now + HyperSkillRules.LEECH_COOLDOWN_TICKS;
        player.heal(HyperSkillRules.LEECH_HEALTH);
        SkillActivationNotifier.notifySkillActivated(player, JobsPlus.translatable("hyper.hunter.activated"));
    }

    /**
     * 플레이어 본인의 공격인지. 근접 공격과, 본인이 쏘거나 던진 모든 투사체(화살·삼지창·총탄·로켓 등)를 포함한다.
     * <p>
     * 피해 종류 태그로 고르지 않고 직접 맞힌 엔티티로 고른다. TACZ 총탄은 바닐라 투사체 태그가 없고, 몹에 따라
     * 총탄 피해·마법 피해·근접 피해로 바뀌며, 로켓·유탄은 폭발 피해로 들어오기 때문이다.
     * 가시 마법부여 반사 피해는 직접 맞힌 엔티티가 플레이어라 근접처럼 보이므로 따로 뺀다.
     */
    private static boolean isOwnAttack(ServerPlayer player, DamageSource source)
    {
        Entity direct = source.getDirectEntity();
        boolean melee = direct == player && !source.is(DamageTypes.THORNS);
        boolean ownProjectile = direct instanceof Projectile projectile && projectile.getOwner() == player;
        return melee || ownProjectile;
    }

    private static boolean canCharge(ServerPlayer player)
    {
        return HyperSkillRules.getActiveLevel(player, HyperSkillRules.ADVENTURER) > 0
                && isGrounded(player) && !player.isInWater() && !player.isInLava()
                && !player.isPassenger() && !player.isFallFlying() && !player.isSleeping()
                && player.containerMenu == player.inventoryMenu;
    }

    private static boolean isGrounded(ServerPlayer player)
    {
        // 이동 패킷의 onGround 플래그만으로 공중 충전·가짜 착지를 허용하지 않는다.
        return player.onGround() && !player.level().noCollision(player,
                player.getBoundingBox().move(0, -0.08D, 0));
    }

    public static void handleLeapInput(ServerPlayer player, ServerboundHyperLeapPacket packet)
    {
        HyperPlayerState state = state(player);
        long now = now(player);
        if (packet.sequence() < 1) return;
        if (packet.action() == ServerboundHyperLeapPacket.Action.START)
        {
            if (state.chargeStartedAt >= 0 || state.lastChargeRequestAt == now
                    || packet.sequence() <= state.chargeSequence || state.leapProtected
                    || state.leapReadyAt > now || !canCharge(player)) return;
            state.lastChargeRequestAt = now;
            state.chargeSequence = packet.sequence();
            state.chargeStartedAt = now;
            return;
        }
        if (packet.sequence() != state.chargeSequence || state.chargeStartedAt < 0) return;
        // 키를 놓으면 취소한다. 완충 발동 시점은 서버 틱에서 결정한다.
        state.chargeStartedAt = -1;
    }

    private static void launchLeap(ServerPlayer player, HyperPlayerState state, long now)
    {
        if (state.leapReadyAt > now || state.leapProtected) return;
        if (CombatRules.isFlightRestricted(player))
        {
            CombatRules.punishFlight(player);
            return;
        }
        int level = HyperSkillRules.getActiveLevel(player, HyperSkillRules.ADVENTURER);
        double distance = HyperSkillRules.getLeapDistance(level);
        double yaw = Math.toRadians(player.getYRot());
        Vec3 direction = new Vec3(-Math.sin(yaw), 0, Math.cos(yaw));
        HyperLeapMovement.start(player, direction, distance);
        state.leapStartedAt = now;
        state.leapReadyAt = now + HyperSkillRules.getLeapCooldownTicks(level);
        state.suppressMovementUntil = now + 2;
        NetworkManager.sendToPlayer(player, new ClientboundHyperLeapPacket(direction.x, direction.z, distance));
        sync(player);
    }

    public static void sync(ServerPlayer player)
    {
        HyperPlayerState state = state(player);
        long now = now(player);
        var packet = new ClientboundHyperStatusPacket(
                HyperSkillRules.getActiveLevel(player, HyperSkillRules.SMITH),
                HyperSkillRules.getActiveLevel(player, HyperSkillRules.ADVENTURER),
                remaining(state.shieldUntil, now), remaining(state.shieldReadyAt, now),
                remaining(state.leapReadyAt, now), state.leapProtected);
        // 차원 전환으로 클라이언트 플레이어만 교체된 경우에도 HUD를 다시 채운다.
        if (packet.hashCode() != state.lastSyncedHash || player.tickCount % 20 == 0)
        {
            state.lastSyncedHash = packet.hashCode();
            NetworkManager.sendToPlayer(player, packet);
        }
    }

    private static int remaining(long until, long now)
    {
        return (int) Math.clamp(until - now, 0, Integer.MAX_VALUE);
    }
}
