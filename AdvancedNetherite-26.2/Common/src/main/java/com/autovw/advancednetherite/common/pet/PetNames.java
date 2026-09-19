package com.autovw.advancednetherite.common.pet;

import com.autovw.advancednetherite.common.entity.DialgaPetEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 펫을 부르는 이름 규칙. 서버의 머리 위 이름표와 클라이언트의 펫 관리 목록이 같은 규칙을 쓴다.
 *
 * <p>플레이어가 이름을 붙였으면 그 이름을, 아니면 종류 이름을 쓰되 같은 종류가 둘 이상이면
 * 목록 순서대로 번호를 붙여 구분한다.
 */
public final class PetNames
{
    /** 플레이어가 붙일 수 있는 이름의 최대 글자 수 */
    public static final int MAX_LENGTH = 16;

    private PetNames()
    {
    }

    /**
     * 플레이어 입력을 저장할 수 있는 이름으로 다듬는다.
     * 앞뒤 공백을 지우고 연속 공백을 하나로 줄이며, 제어 문자와 색 코드 기호를 뺀다.
     * 결과가 비면 이름을 지우는 것으로 본다.
     */
    public static String sanitize(String input)
    {
        if (input == null)
        {
            return "";
        }
        StringBuilder builder = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++)
        {
            char character = input.charAt(i);
            if (Character.isISOControl(character) || character == '§') continue;
            builder.append(character);
        }
        String cleaned = builder.toString().trim().replaceAll("\\s+", " ");
        if (cleaned.length() > MAX_LENGTH)
        {
            cleaned = cleaned.substring(0, MAX_LENGTH).trim();
        }
        return cleaned;
    }

    /** 종류 이름. 알 수 없는 타입이면 ID를 그대로 쓴다. */
    public static Component typeName(String petTypeId)
    {
        EntityType<DialgaPetEntity> petType = PetManager.getPetType(petTypeId);
        return petType == null ? Component.literal(petTypeId) : petType.getDescription();
    }

    /**
     * 목록의 모든 펫에 대해 표시 이름을 만든다. 목록 순서와 같은 순서로 돌려준다.
     *
     * @param pets       주인이 가진 펫 전체. 번호는 이 목록 순서로 매긴다.
     * @param typeId     펫 타입 ID를 꺼내는 함수
     * @param customName 플레이어가 붙인 이름을 꺼내는 함수. 없으면 빈 문자열
     */
    public static <T> List<Component> labels(List<T> pets, Function<T, String> typeId, Function<T, String> customName)
    {
        Map<String, Integer> totals = new HashMap<>();
        for (T pet : pets)
        {
            totals.merge(typeId.apply(pet), 1, Integer::sum);
        }

        Map<String, Integer> seen = new HashMap<>();
        List<Component> labels = new ArrayList<>(pets.size());
        for (T pet : pets)
        {
            String type = typeId.apply(pet);
            int order = seen.merge(type, 1, Integer::sum);
            String custom = customName.apply(pet);
            if (custom != null && !custom.isEmpty())
            {
                labels.add(Component.literal(custom));
                continue;
            }
            Component name = typeName(type);
            labels.add(totals.get(type) > 1 ? name.copy().append(Component.literal(" " + order)) : name);
        }
        return labels;
    }

    /** 기록 하나의 표시 이름. 목록에 없는 기록이면 종류 이름만 돌려준다. */
    public static Component displayName(List<PetRecord> pets, PetRecord record)
    {
        List<Component> labels = labels(pets, PetRecord::petTypeId, PetRecord::name);
        for (int i = 0; i < pets.size(); i++)
        {
            if (pets.get(i).id().equals(record.id()))
            {
                return labels.get(i);
            }
        }
        return record.name().isEmpty() ? typeName(record.petTypeId()) : Component.literal(record.name());
    }
}
