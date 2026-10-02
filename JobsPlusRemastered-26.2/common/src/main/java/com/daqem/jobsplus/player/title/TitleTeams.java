package com.daqem.jobsplus.player.title;

import com.daqem.jobsplus.JobsPlus;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.PlayerTeam;
import org.jetbrains.annotations.Nullable;

/**
 * 장착한 칭호 배지를 스코어보드 팀 접두사로 붙인다.
 *
 * <p>바닐라는 채팅 보낸 사람 이름, 머리 위 이름, Tab 목록을 모두 팀 접두사와 함께 그린다.
 * 그래서 칭호마다 팀을 하나 두고 장착한 사람만 넣으면 별도 클라이언트 코드 없이 세 곳에 함께 표시된다.
 * 칭호는 서버에 한 명만 가지므로 한 팀에 두 사람이 들어가 아군 판정이 생기는 일은 없다.
 */
public final class TitleTeams
{
    private static final String TEAM_PREFIX = "jobsplus_title_";

    private TitleTeams()
    {
    }

    /**
     * 플레이어를 장착 칭호의 팀으로 옮긴다.
     *
     * <p>운영자가 {@code /team}으로 넣어 둔 팀은 칭호보다 우선하므로 건드리지 않는다.
     * 이 경우 배지는 머리 위·채팅에 붙지 않고 칭호 탭에서만 보인다.
     *
     * @param scoreboardName 플레이어 이름. 접속하지 않은 보유자도 이름으로 팀에서 뺄 수 있다.
     * @param equipped       {@code null}이면 칭호 팀에서 뺀다.
     */
    public static void apply(MinecraftServer server, String scoreboardName, @Nullable TitleType equipped)
    {
        ServerScoreboard scoreboard = server.getScoreboard();
        PlayerTeam currentTeam = scoreboard.getPlayersTeam(scoreboardName);
        boolean inOtherTeam = currentTeam != null && !isTitleTeam(currentTeam);
        if (inOtherTeam)
        {
            JobsPlus.LOGGER.info("Title badge for {} is not shown because the player is in team {}.",
                    scoreboardName, currentTeam.getName());
            return;
        }

        PlayerTeam targetTeam = null;
        if (equipped != null)
        {
            targetTeam = getOrCreateTeam(scoreboard, equipped);
        }
        if (currentTeam == targetTeam)
        {
            return;
        }
        if (currentTeam != null)
        {
            scoreboard.removePlayerFromTeam(scoreboardName, currentTeam);
        }
        if (targetTeam != null)
        {
            scoreboard.addPlayerToTeam(scoreboardName, targetTeam);
        }
    }

    private static PlayerTeam getOrCreateTeam(ServerScoreboard scoreboard, TitleType type)
    {
        String teamName = TEAM_PREFIX + type.getId();
        PlayerTeam team = scoreboard.getPlayerTeam(teamName);
        if (team == null)
        {
            team = scoreboard.addPlayerTeam(teamName);
            team.setDisplayName(Component.literal(type.getDisplayName()));
        }

        // 배지 그림이나 글자 위치를 바꾼 업데이트 뒤에도 기존 팀이 새 접두사를 쓰게 한다.
        Component prefix = Component.empty().append(type.getBadge()).append(Component.literal(" "));
        boolean prefixChanged = !prefix.equals(team.getPlayerPrefix());
        if (prefixChanged)
        {
            team.setPlayerPrefix(prefix);
        }
        return team;
    }

    private static boolean isTitleTeam(PlayerTeam team)
    {
        return team.getName().startsWith(TEAM_PREFIX);
    }
}
