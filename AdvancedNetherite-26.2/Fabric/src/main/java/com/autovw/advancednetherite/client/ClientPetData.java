package com.autovw.advancednetherite.client;

import com.autovw.advancednetherite.network.PetStatusEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 서버가 동기화해 준 내 펫 목록의 클라이언트 캐시.
 * 인벤토리 화면의 ON/OFF 버튼이 이 목록을 읽는다.
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
        List<PetStatusEntry> updated = new ArrayList<>(pets);
        boolean newEnabled = false;
        for (int i = 0; i < updated.size(); i++)
        {
            PetStatusEntry entry = updated.get(i);
            if (entry.recordId().equals(recordId))
            {
                newEnabled = !entry.enabled();
                updated.set(i, new PetStatusEntry(entry.recordId(), entry.petTypeId(), newEnabled));
                break;
            }
        }
        pets = List.copyOf(updated);
        return newEnabled;
    }
}
