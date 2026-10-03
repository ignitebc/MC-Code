package com.daqem.jobsplus.player.job.hyper;

import com.daqem.arc.player.SkillActivationNotifier;
import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.accessor.HyperPlayerAccess;
import com.daqem.jobsplus.networking.c2s.ServerboundHyperLeapPacket;
import com.daqem.jobsplus.networking.s2c.ClientboundHyperLeapPacket;
import com.daqem.jobsplus.networking.s2c.ClientboundHyperStatusPacket;
import com.mcserver.serverutilities.combat.CombatRules;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
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
    private static final Identifier TACZ_BULLET = Identifier.fromNamespaceAndPath("tacz", "kinetic_bullet");

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
        if (state.chargeStartedAt >= 0 && !canCharge(player))
        {
            state.chargeStartedAt = -1;
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
                || source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_EXPLOSION)
                || source.is(DamageTypes.THORNS)) return;
        Entity direct = source.getDirectEntity();
        // TACZ 총알은 바닐라 is_projectile 태그가 없고 일부 몹에는 마법 피해를 사용한다.
        boolean playerProjectile = direct instanceof Projectile && (source.is(DamageTypeTags.IS_PROJECTILE)
                || TACZ_BULLET.equals(BuiltInRegistries.ENTITY_TYPE.getKey(direct.getType())));
        if (direct != player && !playerProjectile) return;
        int level = HyperSkillRules.getActiveLevel(player, HyperSkillRules.HUNTER);
        if (level == 0 || player.getHealth() >= player.getMaxHealth()) return;
        HyperPlayerState state = state(player);
        long now = now(player);
        // 총알의 일반·관통 피해, 산탄·휩쓸기 등 같은 틱의 다중 피해는 한 번만 추첨한다.
        if (state.leechReadyAt > now || state.lastLeechAttempt == now) return;
        state.lastLeechAttempt = now;
        if (player.getRandom().nextInt(100) >= HyperSkillRules.getHunterChance(level)) return;
        state.leechReadyAt = now + HyperSkillRules.LEECH_COOLDOWN_TICKS;
        player.heal(HyperSkillRules.LEECH_HEALTH);
        SkillActivationNotifier.notifySkillActivated(player, JobsPlus.translatable("hyper.hunter.activated"));
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
        long charged = now - state.chargeStartedAt;
        state.chargeStartedAt = -1;
        if (packet.action() == ServerboundHyperLeapPacket.Action.CANCEL
                || charged < HyperSkillRules.LEAP_MIN_CHARGE_TICKS || !canCharge(player)
                || state.leapReadyAt > now || state.leapProtected) return;
        if (CombatRules.isFlightRestricted(player))
        {
            CombatRules.punishFlight(player);
            return;
        }
        int level = HyperSkillRules.getActiveLevel(player, HyperSkillRules.ADVENTURER);
        double fraction = Math.min(charged, HyperSkillRules.LEAP_CHARGE_TICKS)
                / (double) HyperSkillRules.LEAP_CHARGE_TICKS;
        double distance = HyperSkillRules.getLeapDistance(level) * fraction;
        double yaw = Math.toRadians(player.getYRot());
        Vec3 direction = new Vec3(-Math.sin(yaw), 0, Math.cos(yaw));
        HyperLeapMovement.start(player, direction, distance);
        state.leapStartedAt = now;
        state.leapReadyAt = now + HyperSkillRules.LEAP_COOLDOWN_TICKS;
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
