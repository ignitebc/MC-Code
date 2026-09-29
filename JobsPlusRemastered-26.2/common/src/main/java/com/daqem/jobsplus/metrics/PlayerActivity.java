package com.daqem.jobsplus.metrics;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 접속 중인 플레이어의 활동량을 1분마다 activity.csv 행으로 남긴다.
 * <p>
 * 접속시간만으로는 잠수 시간과 실제 플레이 시간을 구분할 수 없으므로, 입력 흔적이 마지막으로 있었던 뒤 지난 시간(idle_seconds)을 함께 기록한다.
 * 입력 흔적은 바닐라가 키 입력·공격·상호작용·컨테이너 클릭 때 갱신하는 마지막 행동 시각, 시점 회전, 열린 메뉴 변화, 직업 액션 실행이다.
 * 이동 거리는 물살·밀림으로도 늘어나므로 입력 흔적으로 보지 않는다.
 */
final class PlayerActivity
{
    static final String HEADER = "timestamp_ms,sample_seconds,player_uuid,player_name,dimension,x,y,z,game_mode,operator,in_vehicle,"
            + "moved_blocks,rotation_degrees,rotation_ticks,menu_changes,job_actions,teleports,idle_seconds,afk";

    /** 입력 흔적이 이 시간 이상 없으면 잠수로 본다. */
    static final long AFK_MILLIS = 5L * 60L * 1000L;
    static final int SAMPLE_INTERVAL_TICKS = 20 * 60;

    /** 한 틱에 이보다 멀리 움직이면 텔레포트·리스폰으로 보고 이동 거리에서 뺀다. */
    private static final double TELEPORT_DISTANCE = 16.0D;
    private static final float ROTATION_EPSILON = 0.01F;

    private final Map<UUID, State> states = new HashMap<>();
    private int ticksUntilSample = SAMPLE_INTERVAL_TICKS;

    void tick(MinecraftServer server, List<String> output)
    {
        long nowMonotonic = Util.getMillis();
        Set<UUID> online = new HashSet<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers())
        {
            online.add(player.getUUID());
            State state = states.computeIfAbsent(player.getUUID(), ignored -> new State(player, nowMonotonic));
            state.update(player, nowMonotonic);
        }
        states.keySet().retainAll(online);

        ticksUntilSample--;
        if (ticksUntilSample <= 0)
        {
            ticksUntilSample = SAMPLE_INTERVAL_TICKS;
            long now = System.currentTimeMillis();
            for (ServerPlayer player : server.getPlayerList().getPlayers())
            {
                State state = states.get(player.getUUID());
                if (state != null)
                {
                    output.add(state.sample(player, now, nowMonotonic));
                }
            }
        }
    }

    void recordJobAction(UUID playerUuid)
    {
        State state = states.get(playerUuid);
        if (state != null)
        {
            state.jobActions++;
            state.lastInputMillis = Util.getMillis();
        }
    }

    /** 접속 종료·서버 종료 때 마지막 1분 미만 구간을 남긴다. */
    void finish(ServerPlayer player, List<String> output)
    {
        State state = states.remove(player.getUUID());
        if (state != null)
        {
            output.add(state.sample(player, System.currentTimeMillis(), Util.getMillis()));
        }
    }

    List<String> finishAll(MinecraftServer server)
    {
        List<String> output = new ArrayList<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers())
        {
            finish(player, output);
        }
        states.clear();
        return output;
    }

    void clear()
    {
        states.clear();
        ticksUntilSample = SAMPLE_INTERVAL_TICKS;
    }

    private static final class State
    {
        private String dimension;
        private double x;
        private double y;
        private double z;
        private float yaw;
        private float pitch;
        private int containerId;
        private int containerStateId;
        private long lastInputMillis;
        private long sampleStartMillis;

        private double movedBlocks;
        private double rotationDegrees;
        private int rotationTicks;
        private int menuChanges;
        private int jobActions;
        private int teleports;

        private State(ServerPlayer player, long nowMonotonic)
        {
            capturePosition(player);
            this.containerId = player.containerMenu.containerId;
            this.containerStateId = player.containerMenu.getStateId();
            this.lastInputMillis = nowMonotonic;
            this.sampleStartMillis = System.currentTimeMillis();
        }

        private void update(ServerPlayer player, long nowMonotonic)
        {
            String currentDimension = player.level().dimension().identifier().toString();
            double dx = player.getX() - x;
            double dy = player.getY() - y;
            double dz = player.getZ() - z;
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (!currentDimension.equals(dimension) || distance > TELEPORT_DISTANCE)
            {
                teleports++;
            }
            else
            {
                movedBlocks += distance;
            }

            float rotation = Math.abs(Mth.wrapDegrees(player.getYRot() - yaw)) + Math.abs(player.getXRot() - pitch);
            if (rotation > ROTATION_EPSILON)
            {
                rotationDegrees += rotation;
                rotationTicks++;
                lastInputMillis = nowMonotonic;
            }

            int currentContainerId = player.containerMenu.containerId;
            int currentStateId = player.containerMenu.getStateId();
            if (currentContainerId != containerId || currentStateId != containerStateId)
            {
                menuChanges++;
                lastInputMillis = nowMonotonic;
                containerId = currentContainerId;
                containerStateId = currentStateId;
            }

            capturePosition(player);
        }

        private String sample(ServerPlayer player, long now, long nowMonotonic)
        {
            long lastInput = Math.max(lastInputMillis, player.getLastActionTime());
            long idleMillis = Math.max(0L, nowMonotonic - lastInput);
            long sampleSeconds = Math.max(0L, Math.round((now - sampleStartMillis) / 1000.0D));

            String line = now + ","
                    + sampleSeconds + ","
                    + player.getUUID() + ","
                    + MetricsCsv.text(player.getName().getString()) + ","
                    + MetricsCsv.text(player.level().dimension().identifier().toString()) + ","
                    + player.getBlockX() + ","
                    + player.getBlockY() + ","
                    + player.getBlockZ() + ","
                    + MetricsCsv.text(player.gameMode().getName()) + ","
                    + player.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER) + ","
                    + player.isPassenger() + ","
                    + MetricsCsv.number(movedBlocks) + ","
                    + MetricsCsv.number(rotationDegrees) + ","
                    + rotationTicks + ","
                    + menuChanges + ","
                    + jobActions + ","
                    + teleports + ","
                    + idleMillis / 1000L + ","
                    + (idleMillis >= AFK_MILLIS);

            movedBlocks = 0.0D;
            rotationDegrees = 0.0D;
            rotationTicks = 0;
            menuChanges = 0;
            jobActions = 0;
            teleports = 0;
            sampleStartMillis = now;
            return line;
        }

        private void capturePosition(ServerPlayer player)
        {
            dimension = player.level().dimension().identifier().toString();
            x = player.getX();
            y = player.getY();
            z = player.getZ();
            yaw = player.getYRot();
            pitch = player.getXRot();
        }
    }
}
