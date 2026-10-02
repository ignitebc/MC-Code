package com.daqem.jobsplus.client.title;

import com.daqem.jobsplus.player.title.TitleType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * 서버가 보낸 칭호 보유자 목록과 내 장착 칭호를 보관하는 클라이언트 전용 저장소.
 * <p>
 * 칭호 탭 표시에만 쓴다. 보유·장착 판단은 서버가 한다.
 */
public final class ClientTitles
{
    /** @param holderName 보유자가 없으면 빈 문자열 */
    public record Entry(TitleType type, String holderName, boolean mine)
    {
        public boolean hasHolder()
        {
            return !this.holderName.isEmpty();
        }
    }

    private static volatile List<Entry> entries = List.of();
    private static volatile @Nullable TitleType equipped;
    /** 칭호 탭이 다시 그려야 하는지 판단하는 값. 서버 정보를 받을 때마다 오른다. */
    private static volatile int revision;

    private ClientTitles()
    {
    }

    public static void update(List<Entry> newEntries, @Nullable TitleType newEquipped)
    {
        entries = List.copyOf(newEntries);
        equipped = newEquipped;
        revision++;
    }

    public static Optional<Entry> find(TitleType type)
    {
        for (Entry entry : entries)
        {
            if (entry.type() == type)
            {
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }

    public static boolean isEquipped(TitleType type)
    {
        return equipped == type;
    }

    public static int getRevision()
    {
        return revision;
    }
}
