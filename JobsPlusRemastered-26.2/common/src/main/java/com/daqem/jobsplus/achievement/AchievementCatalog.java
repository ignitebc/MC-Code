package com.daqem.jobsplus.achievement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.daqem.jobsplus.achievement.AchievementDefinition.Objective;

/**
 * 시즌 퀘스트 100종. 수량은 추가 수량이 아니라 시즌 누적 목표다.
 * <p>
 * 보상은 난이도 구간 안에서 정한다. ★1 1~2개, ★2 3~5개, ★3 6~12개, ★4 13~20개, ★5 21~30개.
 */
public final class AchievementCatalog
{
    public static final List<String> COMBAT_MOBS = List.of(
            "zombie", "husk", "drowned", "skeleton", "stray", "bogged", "wither_skeleton",
            "creeper", "spider", "cave_spider", "enderman", "witch", "slime", "magma_cube",
            "blaze", "ghast", "piglin_brute", "hoglin", "zoglin", "phantom");
    public static final List<String> NETHER_BIOMES = List.of(
            "nether_wastes", "soul_sand_valley", "crimson_forest", "warped_forest", "basalt_deltas");
    public static final List<String> JOBS = List.of(
            "miner", "digger", "farmer", "fisherman", "hunter", "smith", "alchemist", "adventurer");
    public static final List<String> CROPS = List.of("wheat", "carrot", "potato", "beetroot");
    public static final List<String> FISH = List.of("cod", "salmon", "tropical_fish", "pufferfish");
    private static final Map<String, AchievementDefinition> DEFINITIONS = createDefinitions();
    private static final List<AchievementDefinition> ALL = List.copyOf(DEFINITIONS.values());

    private AchievementCatalog()
    {
    }

    public static List<AchievementDefinition> all()
    {
        return ALL;
    }

    public static AchievementDefinition get(String id)
    {
        return DEFINITIONS.get(id);
    }

    private static Map<String, AchievementDefinition> createDefinitions()
    {
        Map<String, AchievementDefinition> definitions = new LinkedHashMap<>();
        add(definitions, "A01", "숙련공", 1, 2, "", o("job_max_level", 50, "최고 직업 레벨"));
        add(definitions, "A02", "노련한 전문가", 2, 4, "A01", o("job_max_level", 75, "최고 직업 레벨"));
        add(definitions, "A03", "달인", 2, 5, "A02", o("job_max_level", 100, "최고 직업 레벨"));
        add(definitions, "A04", "거장", 4, 17, "A03", o("job_max_level", 150, "최고 직업 레벨"));
        add(definitions, "A05", "경지의 끝", 5, 30, "A04", o("job_max_level", 200, "최고 직업 레벨"));
        String[] masters = {"광맥의 장인", "대지의 장인", "풍요의 장인", "물결의 장인",
                "추적의 장인", "불꽃의 장인", "비약의 장인", "길 위의 장인"};
        List<String> masterIds = new ArrayList<>();
        for (int index = 0; index < JOBS.size(); index++)
        {
            String id = String.format(java.util.Locale.ROOT, "A%02d", index + 6);
            add(definitions, id, masters[index], 3, 9, "", o("job_master:" + JOBS.get(index), 1, "일반 스킬 전체 구매"));
            masterIds.add(id);
        }
        definitions.put("A14", new AchievementDefinition("A14", "두 길의 장인", 4, 17, masterIds, 2,
                List.of(o("job_master_count", 2, "완성한 직업")), "A06~A13 중 2개 완료. 비활성화한 구매 스킬도 인정하며 하이퍼는 제외합니다."));
        add(definitions, "A15", "세 길의 장인", 5, 23, "A14", o("job_master_count", 3, "완성한 직업"));
        add(definitions, "A16", "다재다능", 3, 10, "", o("jobs_level50", 4, "Lv.50 직업"));
        add(definitions, "A17", "팔방미인", 3, 9, "", o("jobs_level20", 8, "Lv.20 직업"));
        add(definitions, "A18", "잠든 힘의 문", 3, 8, "A03", o("hyper_max_level", 1, "광부·굴착가 하이퍼 레벨"));
        add(definitions, "A19", "깨어나는 힘", 3, 11, "A18", o("hyper_max_level", 5, "최고 하이퍼 레벨"));
        add(definitions, "A20", "극한 각성", 5, 30, "A19", o("hyper_max_level", 10, "최고 하이퍼 레벨"));

        add(definitions, "B01", "광맥의 흔적", 1, 1, "", o("ores", 500, "자연 광석"));
        add(definitions, "B02", "광맥 추적자", 2, 4, "B01", o("ores", 2000, "자연 광석"));
        add(definitions, "B03", "지하의 왕", 4, 17, "B02", o("ores", 8000, "자연 광석"));
        add(definitions, "B04", "고대의 잔향", 3, 8, "", o("ancient_debris", 64, "자연 고대 잔해"));
        add(definitions, "B05", "대지를 여는 삽", 1, 2, "", o("excavation", 5000, "자연 굴착 블록"));
        add(definitions, "B06", "삽의 달인", 2, 4, "B05", o("excavation", 15000, "자연 굴착 블록"));
        add(definitions, "B07", "지형 조각가", 4, 17, "B06", o("excavation", 60000, "자연 굴착 블록"));
        add(definitions, "B08", "풍요의 씨앗", 1, 2, "", o("harvests", 1000, "성숙 작물 수확"));
        add(definitions, "B09", "성실한 농부", 2, 4, "B08", o("harvests", 5000, "성숙 작물 수확"));
        add(definitions, "B10", "네 가지 결실", 2, 5, "B08", cropObjectives("harvest:", 1000));
        add(definitions, "B11", "대농장주", 4, 17, "B09", o("harvests", 20000, "성숙 작물 수확"));
        add(definitions, "B12", "물결을 읽는 자", 1, 2, "", o("fishing", 250, "낚시 성공 회수"));
        add(definitions, "B13", "낚시 애호가", 2, 4, "B12", o("fishing", 1000, "낚시 성공 회수"));
        add(definitions, "B14", "네 바다의 식탁", 2, 5, "B12",
                o("fish:cod", 10, "대구"), o("fish:salmon", 10, "연어"),
                o("fish:tropical_fish", 10, "열대어"), o("fish:pufferfish", 10, "복어"));
        add(definitions, "B15", "강태공", 4, 17, "B13", o("fishing", 5000, "낚시 성공 회수"));
        add(definitions, "B16", "사냥 숙련", 2, 4, "", o("hostile_kills", 1000, "적대몹 처치"));
        add(definitions, "B17", "어둠을 걷어내는 자", 4, 17, "B16", o("hostile_kills", 5000, "적대몹 처치"));
        add(definitions, "B18", "끝없는 토벌", 5, 30, "B17", o("hostile_kills", 10000, "적대몹 처치"));
        add(definitions, "B19", "비약의 기초", 1, 2, "", o("potions", 100, "효과 물약 첫 완성"));
        add(definitions, "B20", "물약 제조사", 2, 4, "B19", o("potions", 500, "효과 물약 첫 완성"));
        add(definitions, "B21", "비약의 대가", 3, 11, "B20", o("potions", 3000, "효과 물약 첫 완성"));
        add(definitions, "B22", "꺼지지 않는 불", 1, 2, "", o("smelted", 1000, "본인 제련품 회수"));
        add(definitions, "B23", "용광로 지기", 2, 5, "B22", o("smelted", 3000, "본인 제련품 회수"));
        add(definitions, "B24", "마력을 새기는 손", 2, 4, "", o("enchants", 100, "마법부여대 사용"));
        add(definitions, "B25", "마법 대장장이", 3, 9, "B24", o("enchants", 300, "마법부여대 사용"));
        add(definitions, "B26", "긴 여정", 1, 2, "", o("walk_cm", 5000000, "걷기·달리기(cm)"));
        add(definitions, "B27", "대륙 횡단", 2, 5, "B26", o("walk_cm", 15000000, "걷기·달리기(cm)"));
        add(definitions, "B28", "지평선 너머", 4, 17, "B27", o("walk_cm", 45000000, "걷기·달리기(cm)"));
        add(definitions, "B29", "바람을 타고", 2, 5, "", o("elytra_cm", 10000000, "겉날개 비행(cm)"));
        add(definitions, "B30", "하늘길의 주인", 3, 9, "B29", o("elytra_cm", 30000000, "겉날개 비행(cm)"));

        add(definitions, "C01", "끝의 시작", 2, 5, "", o("structure:stronghold", 1, "근거지 방문"), o("dimension:end", 1, "엔드 진입"));
        add(definitions, "C02", "요새의 불꽃", 2, 4, "", o("structure:fortress", 1, "네더 요새 방문"), o("kill:minecraft:blaze", 50, "블레이즈 처치"));
        add(definitions, "C03", "보루 돌파", 2, 5, "", o("structure:bastion_remnant", 1, "보루 잔해 방문"), o("kill:minecraft:piglin_brute", 10, "피글린 야수 처치"));
        add(definitions, "C04", "네더의 발자취", 2, 5, "", o("nether_biomes", 5, "네더 바닐라 바이옴"));
        add(definitions, "C05", "미지의 지도", 2, 4, "", o("overworld_biomes", 20, "오버월드 바이옴"));
        add(definitions, "C06", "모험의 시간", 5, 30, "C05", o("adventuring_time", 1, "바닐라 모험의 시간 완료"));
        add(definitions, "C07", "마을의 영웅", 2, 5, "", o("raids", 1, "습격 승리 공로"));
        add(definitions, "C08", "습격 진압대", 4, 17, "C07", o("raids", 5, "습격 승리 공로"));
        add(definitions, "C09", "용을 꺾은 자", 3, 10, "C01", o("dragons", 1, "드래곤 처치 기여"));
        add(definitions, "C10", "되풀이되는 종말", 4, 20, "C09", o("dragons", 3, "드래곤 처치 기여"));
        add(definitions, "C11", "심연의 정적", 4, 17, "", o("kill:minecraft:warden", 1, "워든 처치"));
        add(definitions, "C12", "소환술의 종언", 3, 9, "", o("kill:illagerinvasion:invoker", 1, "인보커 처치"));
        add(definitions, "C13", "바다의 수호자", 3, 9, "", o("kill:minecraft:elder_guardian", 3, "엘더 가디언 처치"));
        add(definitions, "C14", "해저 정복자", 4, 17, "C13", o("kill:minecraft:elder_guardian", 9, "엘더 가디언 처치"));
        add(definitions, "C15", "위더 사냥꾼", 3, 11, "", o("kill:minecraft:wither", 3, "위더 처치"));
        add(definitions, "C16", "재앙의 종결자", 5, 23, "C15", o("kill:minecraft:wither", 10, "위더 처치"));
        add(definitions, "C17", "엔드 정찰대", 3, 8, "C01", o("structure:end_city", 1, "엔드 도시 방문"), o("kill:minecraft:shulker", 30, "셜커 처치"));
        add(definitions, "C18", "네더의 대공포", 2, 5, "", o("kill:minecraft:ghast", 30, "가스트 처치"));
        add(definitions, "C19", "잠들지 않는 파수꾼", 2, 4, "", o("kill:minecraft:phantom", 30, "팬텀 처치"));
        List<Objective> combatObjectives = new ArrayList<>();
        for (String mob : COMBAT_MOBS)
        {
            combatObjectives.add(o("kill:minecraft:" + mob, 10, mob));
        }
        add(definitions, "C20", "전장의 기록", 3, 9, "", combatObjectives.toArray(Objective[]::new));

        add(definitions, "D01", "땀으로 번 코인", 1, 2, "", o("job_btc", 50, "직업 지급 BTC"));
        add(definitions, "D02", "비트코인 채굴자", 2, 5, "D01", o("job_btc", 150, "직업 지급 BTC"));
        add(definitions, "D03", "코인 부자", 4, 17, "D02", o("job_btc", 400, "직업 지급 BTC"));
        add(definitions, "D04", "비트코인 고래", 5, 23, "D03", o("job_btc", 800, "직업 지급 BTC"));
        add(definitions, "D05", "내 땅 마련", 1, 1, "", o("claimed_chunks", 1, "동시 소유 청크"));
        add(definitions, "D06", "작은 영지", 2, 4, "D05", o("claimed_chunks", 4, "동시 소유 청크"));
        add(definitions, "D07", "하늘을 산 사람", 3, 12, "", o("shop_elytra", 1, "500 BTC 겉날개 구매"));
        add(definitions, "D08", "시장을 읽는 눈", 2, 4, "", o("stock_round_trips", 3, "유효 매수·매도 종목"));
        add(definitions, "D09", "넓어진 투자 지도", 3, 9, "D08", o("stock_round_trips", 10, "유효 매수·매도 종목"));
        add(definitions, "D10", "농산물 납품 책임자", 2, 5, "", cropObjectives("sold:", 1000));

        add(definitions, "E01", "익숙해진 방아쇠", 1, 2, "", o("gun_level25", 25, "직접 Lv.25 도달"));
        add(definitions, "E02", "손에 익은 총", 2, 5, "E01", o("gun_level50", 50, "직접 Lv.50 도달"));
        add(definitions, "E03", "전설의 총잡이", 4, 17, "E02", o("gun_level100", 100, "직접 Lv.100 도달"));
        add(definitions, "E04", "다루지 못할 총은 없다", 3, 9, "E02", o("gun_models_level50", 3, "직접 Lv.50 도달 모델"));
        add(definitions, "E05", "완성된 무기고", 5, 23, "E03,E04", o("gun_models_level100", 3, "직접 Lv.100 도달 모델"));
        add(definitions, "E06", "손때 묻은 도구", 1, 2, "", o("tool_level25", 25, "직접 Lv.25 도달"));
        add(definitions, "E07", "장인의 손길", 2, 4, "E06", o("tool_level50", 50, "직접 Lv.50 도달"));
        add(definitions, "E08", "명품 도구", 3, 11, "E07", o("tool_level100", 100, "직접 Lv.100 도달"));
        add(definitions, "E09", "장인의 작업대", 4, 20, "E08", o("tool_types_level100", 3, "직접 Lv.100 도달 종류"));
        add(definitions, "E10", "세 번의 담금질", 1, 2, "", o("enhancement_max", 3, "본인 강화 성공 단계"));
        add(definitions, "E11", "일곱 번의 불꽃", 3, 11, "E10", o("enhancement_max", 7, "본인 강화 성공 단계"));
        add(definitions, "E12", "강화의 끝", 5, 30, "E11", o("enhancement_max", 10, "본인 강화 성공 단계"));

        add(definitions, "F01", "함께 자라는 사이", 1, 2, "", o("pet_max_level", 20, "본인 펫 최고 레벨"));
        add(definitions, "F02", "믿음직한 동행", 2, 5, "F01", o("pet_max_level", 50, "본인 펫 최고 레벨"));
        add(definitions, "F03", "전장을 함께한 벗", 3, 11, "F02", o("pet_max_level", 75, "본인 펫 최고 레벨"));
        add(definitions, "F04", "든든한 동료", 4, 17, "F03", o("pet_max_level", 100, "본인 펫 최고 레벨"));
        add(definitions, "F05", "특별한 인연", 2, 5, "", o("pet_rare", 1, "희귀 펫 획득"));
        add(definitions, "F06", "전설과의 만남", 3, 9, "", o("pet_legend", 1, "전설 펫 획득"));
        add(definitions, "F07", "세 친구의 발자취", 3, 8, "F01", o("pet_types_level20", 3, "Lv.20 펫 종류"));
        add(definitions, "F08", "전설의 완성", 5, 23, "F04,F06", o("legend_pet_max_level", 100, "전설 펫 최고 레벨"));
        validate(definitions);
        return java.util.Collections.unmodifiableMap(definitions);
    }

    private static Objective o(String key, long target, String label)
    {
        return new Objective(key, target, label);
    }

    private static Objective[] cropObjectives(String prefix, long target)
    {
        return new Objective[]{o(prefix + "wheat", target, "밀"), o(prefix + "carrot", target, "당근"),
                o(prefix + "potato", target, "감자"), o(prefix + "beetroot", target, "비트")};
    }

    private static void add(Map<String, AchievementDefinition> definitions, String id, String name,
                            int difficulty, int diamonds, String parentIds, Objective... objectives)
    {
        List<String> parents = List.of();
        if (!parentIds.isEmpty())
        {
            parents = List.of(parentIds.split(","));
        }
        String details = AchievementRules.detailsFor(id);
        AchievementDefinition previous = definitions.put(id, new AchievementDefinition(id, name, difficulty,
                diamonds, parents, parents.size(), List.of(objectives), details));
        if (previous != null)
        {
            throw new IllegalArgumentException("Duplicate achievement: " + id);
        }
    }

    private static void validate(Map<String, AchievementDefinition> definitions)
    {
        int rewards = 0;
        Set<String> preceding = new HashSet<>();
        for (AchievementDefinition definition : definitions.values())
        {
            for (String parent : definition.parents())
            {
                if (!preceding.contains(parent))
                {
                    throw new IllegalArgumentException("Unknown or cyclic achievement parent: " + parent);
                }
            }
            preceding.add(definition.id());
            rewards += definition.diamonds();
            // 같은 별 수의 업적이 비슷한 보상을 받도록 난이도마다 보상 구간을 고정한다.
            int[] band = switch (definition.difficulty())
            {
                case 1 -> new int[]{1, 2};
                case 2 -> new int[]{3, 5};
                case 3 -> new int[]{6, 12};
                case 4 -> new int[]{13, 20};
                default -> new int[]{21, 30};
            };
            if (definition.diamonds() < band[0] || definition.diamonds() > band[1])
            {
                throw new IllegalStateException("Achievement reward is outside its difficulty band: " + definition.id());
            }
        }
        if (definitions.size() != 100 || rewards != 988)
        {
            throw new IllegalStateException("Achievement catalog must contain 100 quests and 988 diamonds");
        }
    }
}
