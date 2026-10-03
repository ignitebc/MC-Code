package com.daqem.jobsplus.client.achievement;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.achievement.AchievementCatalog;
import com.daqem.jobsplus.achievement.AchievementDefinition;
import com.daqem.jobsplus.achievement.AchievementRules;
import com.google.gson.JsonParseException;
import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 판정용 ID를 바꾸지 않고 업적 화면의 대상 이름과 설명만 한글로 표시한다. */
public final class AchievementDisplayText
{
    private static final Map<String, String> KOREAN_NAMES = new HashMap<>();
    private static Language cachedLanguage;

    private AchievementDisplayText()
    {
    }

    public static String achievementName(String id)
    {
        AchievementDefinition definition = AchievementCatalog.get(id);
        return definition == null ? "선행 업적" : definition.name();
    }

    public static String resourceName(String type, String id, String fallback)
    {
        Identifier identifier = Identifier.tryParse(id);
        if (identifier == null)
        {
            return fallback;
        }
        loadKoreanNames();
        String key = type + "." + identifier.getNamespace() + "." + identifier.getPath().replace('/', '.');
        String koreanName = KOREAN_NAMES.get(key);
        if (koreanName != null)
        {
            return koreanName;
        }
        return fallback;
    }

    /** 게임 언어와 관계없이 한글 이름을 사용하며 리소스 재로드 때 캐시도 갱신한다. */
    private static void loadKoreanNames()
    {
        Language currentLanguage = Language.getInstance();
        if (cachedLanguage == currentLanguage)
        {
            return;
        }
        KOREAN_NAMES.clear();
        var resourceManager = Minecraft.getInstance().getResourceManager();
        for (String namespace : resourceManager.getNamespaces())
        {
            Identifier languageId = Identifier.fromNamespaceAndPath(namespace, "lang/ko_kr.json");
            for (Resource resource : resourceManager.getResourceStack(languageId))
            {
                try (InputStream stream = resource.open())
                {
                    Language.loadFromJson(stream, KOREAN_NAMES::put);
                }
                catch (IOException | JsonParseException | IllegalStateException exception)
                {
                    JobsPlus.LOGGER.warn("Could not load Korean achievement display names from {}", languageId, exception);
                }
            }
        }
        cachedLanguage = currentLanguage;
    }

    public static String details(AchievementDefinition definition)
    {
        return switch (definition.id())
        {
            case "A14" -> "각 직업의 장인 업적 중 2개를 달성해야 합니다.\n구매 후 비활성화한 일반 스킬도 인정하며 하이퍼스킬은 제외합니다.";
            case "B01", "B02", "B03" -> "직접 설치한 블록은 집계하지 않습니다.\n대상 광석:\n"
                    + names("block", AchievementRules.ORES.stream().sorted().toList());
            case "B04" -> "자연 생성된 고대 잔해만 집계합니다.\n직접 설치한 블록은 제외합니다.";
            case "B05", "B06", "B07" -> "직접 설치한 블록은 집계하지 않습니다.\n대상 블록:\n"
                    + names("block", AchievementRules.EXCAVATION.stream().sorted().toList());
            case "B08", "B09", "B10", "B11" -> "완전히 자란 작물 블록을 수확할 때 1회 집계합니다.\n추가 드롭은 제외합니다.\n대상 작물:\n"
                    + names("block", AchievementRules.CROPS.stream().sorted().toList());
            case "B16", "B17", "B18" -> "서버에서 적대 몬스터로 분류된 대상만 집계합니다.\n일반 처치는 기존 처치 공로를 사용하며, 드래곤은 별도 기여 조건을 만족해야 합니다.";
            case "C04" -> "다음 네더 생물 군계를 모두 방문해야 합니다:\n"
                    + names("biome", AchievementCatalog.NETHER_BIOMES);
            case "C05" -> "현재 서버에서 생성되는 오버월드 생물 군계만 인정합니다.\n서로 다른 생물 군계를 방문할 때 시즌마다 1회 기록합니다.";
            case "C07", "C08" -> "습격의 영웅 공로 목록에 등록되어 있어야 합니다.\n승리 시 같은 차원에 생존한 플레이어만 인정하며 습격마다 1회 집계합니다.";
            case "C20" -> "다음 몬스터 20종을 각각 10마리씩 처치해야 합니다:\n"
                    + names("entity", AchievementCatalog.COMBAT_MOBS);
            default -> definition.details();
        };
    }

    private static String names(String type, List<String> ids)
    {
        return "• " + String.join("\n• ", ids.stream()
                .map(id -> resourceName(type, id, "추가 대상")).toList());
    }
}
