package com.tacz.guns.util;

import com.tacz.guns.api.entity.ITargetEntity;
import com.tacz.guns.config.common.OtherConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedList;
import java.util.WeakHashMap;

public final class HitboxHelper {
    // 玩家位置缓存表
    private static final WeakHashMap<Player, LinkedList<Vec3>> PLAYER_POSITION = new WeakHashMap<>();
    // 玩家命中箱缓存表
    private static final WeakHashMap<Player, LinkedList<AABB>> PLAYER_HITBOXES = new WeakHashMap<>();
    // 玩家速度缓存表
    private static final WeakHashMap<Player, LinkedList<Vec3>> PLAYER_VELOCITY = new WeakHashMap<>();
    // 命中箱缓存 Tick 上限
    private static final int SAVE_TICK = Mth.floor(OtherConfig.SERVER_HITBOX_LATENCY_MAX_SAVE_MS.get() / 1000 * 20 + 0.5);
    /**
     * 되감기의 기준 틱.
     * <p>
     * 사수 화면에 그려지는 대상의 위치는 보간 때문에 서버 위치보다 뒤처진다. 그만큼 명중 판정용 상자를
     * 뒤로 물려야 화면에서 맞은 사격이 실제로도 맞는다. 실제 되감기량은 여기서 {@code ServerHitboxOffset}
     * 을 뺀 값이며, 기본 설정에서 2틱이 된다.
     */
    private static final double BASE_REWIND_TICK = 5.0;
    /** 탈것에 탄 대상과 표적에 추가로 적용하는 되감기 틱. */
    private static final double RIDING_EXTRA_REWIND_TICK = 2.5;

    public static void onPlayerTick(Player player) {
        if (player.isSpectator()) {
            PLAYER_POSITION.remove(player);
            PLAYER_HITBOXES.remove(player);
            PLAYER_VELOCITY.remove(player);
            return;
        }
        LinkedList<Vec3> positions = PLAYER_POSITION.computeIfAbsent(player, p -> new LinkedList<>());
        LinkedList<AABB> boxes = PLAYER_HITBOXES.computeIfAbsent(player, p -> new LinkedList<>());
        LinkedList<Vec3> velocities = PLAYER_VELOCITY.computeIfAbsent(player, p -> new LinkedList<>());
        positions.addFirst(player.position());
        boxes.addFirst(player.getBoundingBox());
        velocities.addFirst(getPlayerVelocity(player));
        // Position 用于速度计算，所以只需要缓存 2 个位置
        if (positions.size() > 2) {
            positions.removeLast();
        }
        // 命中箱和速度缓存数量限制
        if (boxes.size() > SAVE_TICK) {
            boxes.removeLast();
            velocities.removeLast();
        }
    }

    public static void onPlayerLoggedOut(Player player) {
        PLAYER_POSITION.remove(player);
        PLAYER_HITBOXES.remove(player);
        PLAYER_VELOCITY.remove(player);
    }

    public static Vec3 getPlayerVelocity(Player entity) {
        LinkedList<Vec3> positions = PLAYER_POSITION.computeIfAbsent(entity, player -> new LinkedList<>());
        if (positions.size() > 1) {
            Vec3 currPos = positions.getFirst();
            Vec3 prevPos = positions.getLast();
            return new Vec3(currPos.x - prevPos.x, currPos.y - prevPos.y, currPos.z - prevPos.z);
        }
        return new Vec3(0, 0, 0);
    }

    public static AABB getBoundingBox(Player entity, int ping) {
        if (PLAYER_HITBOXES.containsKey(entity)) {
            LinkedList<AABB> boxes = PLAYER_HITBOXES.get(entity);
            int index = Mth.clamp(ping, 0, boxes.size() - 1);
            return boxes.get(index);
        }
        return entity.getBoundingBox();
    }

    public static Vec3 getVelocity(Player entity, int ping) {
        if (PLAYER_VELOCITY.containsKey(entity)) {
            LinkedList<Vec3> velocities = PLAYER_VELOCITY.get(entity);
            int index = Mth.clamp(ping, 0, velocities.size() - 1);
            return velocities.get(index);
        }
        return getPlayerVelocity(entity);
    }

    /**
     * 사수의 통신 지연을 틱으로 환산한다. 사수가 플레이어가 아니면 지연이 없으므로 0 이다.
     */
    public static int getPingTick(Entity owner) {
        if (owner instanceof ServerPlayer shooter) {
            int ping = Mth.floor((shooter.connection.latency() / 1000.0) * 20.0 + 0.5);
            return Mth.clamp(ping, 0, SAVE_TICK);
        }
        return 0;
    }

    public static AABB getFixedBoundingBox(Entity entity, Entity owner) {
        AABB boundingBox = entity.getBoundingBox();
        Vec3 velocity = new Vec3(entity.getX() - entity.xOld, entity.getY() - entity.yOld, entity.getZ() - entity.zOld);
        int pingTick = getPingTick(owner);
        // hitbox 延迟补偿。只有射击者是玩家（且被击中者也是玩家）才进行此类延迟补偿计算
        // 과거 히트박스를 직접 꺼내 쓴 경우에는 핑만큼의 되감기가 이미 끝난 상태이므로 아래에서 핑을 다시 더하지 않는다.
        boolean rewoundByHistory = false;
        if (OtherConfig.SERVER_HITBOX_LATENCY_FIX.get() && entity instanceof ServerPlayer player && owner instanceof ServerPlayer) {
            boundingBox = getBoundingBox(player, pingTick);
            velocity = getVelocity(player, pingTick);
            rewoundByHistory = true;
        }
        // 应用蹲伏导致的 hitbox 变形
        double expandHeight = entity instanceof Player && !entity.isCrouching() ? 0.0625 : 0.0;
        boundingBox = boundingBox.expandTowards(0, expandHeight, 0);
        // 根据速度一定程度地扩展 hitbox
        boundingBox = boundingBox.expandTowards(velocity.x, velocity.y, velocity.z);
        // 되감기량 계산.
        // 종전에는 앞으로 밀어 주는 보정이 플레이어에게만 적용되어, 몬스터만 5틱치를 그대로 뒤집어썼다.
        // 그래서 달리는 몬스터는 판정 상자가 모델보다 한 칸 가까이 뒤에 놓였다. 대상 종류와 무관하게 같은 양을 적용한다.
        double hitboxOffset = OtherConfig.SERVER_HITBOX_OFFSET.get();
        double rewindTick = BASE_REWIND_TICK - hitboxOffset;
        if (entity.getVehicle() != null || entity instanceof ITargetEntity) {
            rewindTick += RIDING_EXTRA_REWIND_TICK - hitboxOffset / 2;
        }
        // 히트박스 이력이 없는 대상(몬스터 등)은 사수의 핑만큼을 속도로 되감아 플레이어와 보정량을 맞춘다.
        if (!rewoundByHistory) {
            rewindTick += pingTick;
        }
        boundingBox = boundingBox.move(velocity.multiply(-rewindTick, -rewindTick, -rewindTick));
        return boundingBox;
    }
}
