package com.autovw.advancednetherite.client;

import com.autovw.advancednetherite.network.PetStatusEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.UnaryOperator;

/**
 * 서버가 동기화해 준 내 펫 목록의 클라이언트 캐시.
 * 직업 화면(J키)의 펫관리 탭이 이 목록을 읽는다. 목록 객체는 바뀔 때마다 새로 만들어지므로
 * 화면은 참조가 달라졌는지만 보고 다시 그리면 된다.
 */
public final class ClientPetData
{
    private static volatile List<PetStatusEntry> pets = List.of();

    private ClientPetData()
    {
    }

    public static List<PetStatusEntry> getPets()
    {
        return pets;
    }

    public static void setPets(List<PetStatusEntry> newPets)
    {
        pets = List.copyOf(newPets);
    }

    public static void clear()
    {
        pets = List.of();
    }

    /**
     * 버튼을 누른 즉시 화면에 반영하기 위한 낙관적 갱신. 서버 동기화가 오면 덮어써진다.
     * 갱신된 상태를 돌려준다.
     */
    public static boolean toggleLocally(UUID recordId)
    {
        boolean[] newEnabled = new boolean[1];
        update(recordId, entry -> {
            newEnabled[0] = !entry.enabled();
            return entry.withEnabled(newEnabled[0]);
        });
        return newEnabled[0];
    }

    /** 이름 변경을 서버 응답 전에 먼저 반영한다. 서버가 다듬은 이름으로 곧 덮어쓴다. */
    public static void renameLocally(UUID recordId, String name)
    {
        update(recordId, entry -> entry.withName(name));
    }

    private static void update(UUID recordId, UnaryOperator<PetStatusEntry> change)
    {
        List<PetStatusEntry> updated = new ArrayList<>(pets);
        for (int i = 0; i < updated.size(); i++)
        {
            if (updated.get(i).recordId().equals(recordId))
            {
                updated.set(i, change.apply(updated.get(i)));
                break;
            }
        }
        pets = List.copyOf(updated);
    }
}
