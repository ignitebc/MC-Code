package com.daqem.jobsplus.achievement;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.daqem.jobsplus.achievement.AchievementDefinition.Objective;

/**
 * 시즌 퀘스트 200종. 수량은 추가 수량이 아니라 시즌 누적 목표다.
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
    /** G08 대상. 보스인 인보커는 C12에서 따로 다룬다. */
    public static final List<String> ILLAGER_INVASION_MOBS = List.of(
            "provoker", "basher", "marauder", "inquisitor", "archivist",
            "necromancer", "sorcerer", "firecaller", "alchemist", "surrendered");
    public static final List<String> NORMAL_PETS = List.of(
            "advancednetherite:dialga_pet", "advancednetherite:kirby_pet", "advancednetherite:gomi_pet");
    public static final List<String> RARE_PETS = List.of(
            "advancednetherite:unicorn_pet", "advancednetherite:gazelle_pet");
    public static final List<String> LEGEND_PETS = List.of(
            "advancednetherite:fairlins_pet", "advancednetherite:dark_dragon_pet",
            "advancednetherite:sculken_raven_pet", "advancednetherite:super_gomi_pet");
    /** 구조물 진행도 키와 그 키로 인정하는 구조물 ID. 생김새만 다른 변형은 한 키로 묶는다. */
    public static final Map<String, List<String>> STRUCTURES = createStructures();
    private static final String[] JOB_NAMES = {"광부", "굴착공", "농부", "낚시꾼", "사냥꾼", "대장장이", "연금술사", "모험가"};
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
        add(definitions, "D07", "하늘을 산 사람", 3, 12, "", o("shop_elytra", 1, "상점에서 겉날개 구매"));
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

        addSeasonExpansion(definitions);
        validate(definitions);
        return java.util.Collections.unmodifiableMap(definitions);
    }

    /** 후반 확장 100종(A21 이후와 G 분류). 선행 업적이 앞에 정의되도록 분류 순서대로 추가한다. */
    private static void addSeasonExpansion(Map<String, AchievementDefinition> definitions)
    {
        String[] jobTitles = {"심층의 광부", "지층을 읽는 자", "들판의 주인", "바다의 단골",
                "숲의 추적자", "모루의 주인", "현자의 제자", "끝없는 여행자"};
        for (int index = 0; index < JOBS.size(); index++)
        {
            String id = String.format(java.util.Locale.ROOT, "A%02d", index + 21);
            add(definitions, id, jobTitles[index], 2, 5, "",
                    o("job_level:" + JOBS.get(index), 100, JOB_NAMES[index] + " 레벨"));
        }
        add(definitions, "A29", "여덟 갈래의 길", 4, 17, "A16", o("jobs_level50", 8, "Lv.50 직업"));
        add(definitions, "A30", "세 개의 백", 4, 17, "A03", o("jobs_level100", 3, "Lv.100 직업"));
        add(definitions, "A31", "네 길의 장인", 5, 30, "A15", o("job_master_count", 4, "완성한 직업"));
        add(definitions, "A32", "스킬 수집가", 2, 4, "", o("skills_purchased", 30, "구매한 일반 스킬 단계"));
        add(definitions, "A33", "스킬 백과사전", 4, 15, "A32", o("skills_purchased", 150, "구매한 일반 스킬 단계"));
        add(definitions, "A34", "쌍둥이 각성", 3, 9, "A18",
                o("hyper_level:miner", 1, "광부 하이퍼 레벨"), o("hyper_level:digger", 1, "굴착공 하이퍼 레벨"));
        add(definitions, "A35", "두 개의 각성", 4, 20, "A34",
                o("hyper_level:miner", 7, "광부 하이퍼 레벨"), o("hyper_level:digger", 7, "굴착공 하이퍼 레벨"));
        add(definitions, "A36", "첫걸음", 1, 1, "", o("job_max_level", 10, "최고 직업 레벨"));

        add(definitions, "B31", "다이아몬드 광맥", 2, 5, "B01", o("ore:diamond", 100, "자연 다이아몬드 광석"));
        add(definitions, "B32", "푸른 행운", 2, 5, "B01", o("ore:emerald", 30, "자연 에메랄드 광석"));
        add(definitions, "B33", "고대의 보고", 4, 17, "B04", o("ancient_debris", 256, "자연 고대 잔해"));
        add(definitions, "B34", "땅속의 제왕", 5, 23, "B03", o("ores", 15000, "자연 광석"));
        add(definitions, "B35", "대륙을 깎는 삽", 5, 23, "B07", o("excavation", 150000, "자연 굴착 블록"));
        add(definitions, "B36", "곡창 지대", 5, 23, "B11", o("harvests", 50000, "성숙 작물 수확"));
        add(definitions, "B37", "지옥의 농부", 2, 4, "B08", o("harvest:nether_wart", 1000, "네더 사마귀"));
        add(definitions, "B38", "열대 농장", 2, 3, "B08", o("harvest:cocoa", 500, "코코아"));
        add(definitions, "B39", "가시덤불 수확", 1, 2, "", o("harvest:sweet_berry", 500, "달콤한 열매"));
        add(definitions, "B40", "바다의 전설", 5, 23, "B15", o("fishing", 10000, "낚시 성공 회수"));
        add(definitions, "B41", "보물 낚시꾼", 2, 5, "B13", o("fish_treasure", 20, "보물 전리품"));
        add(definitions, "B42", "비약의 현자", 4, 17, "B21", o("potions", 6000, "효과 물약 첫 완성"));
        add(definitions, "B43", "용광로 장인", 3, 9, "B23", o("smelted", 10000, "본인 제련품 회수"));
        add(definitions, "B44", "불멸의 화로", 4, 17, "B43", o("smelted", 30000, "본인 제련품 회수"));
        add(definitions, "B45", "마법의 정점", 4, 17, "B25", o("enchants", 1000, "마법부여대 사용"));
        add(definitions, "B46", "하늘의 방랑자", 4, 17, "B30", o("elytra_cm", 100000000, "겉날개 비행(cm)"));
        add(definitions, "B47", "목장 주인", 1, 2, "", o("animals_bred", 100, "동물 번식"));
        add(definitions, "B48", "대목장", 3, 8, "B47", o("animals_bred", 1000, "동물 번식"));
        add(definitions, "B49", "단골 손님", 1, 2, "", o("villager_trades", 100, "주민·떠돌이 상인 거래"));
        add(definitions, "B50", "마을 경제의 큰손", 3, 8, "B49", o("villager_trades", 1000, "주민·떠돌이 상인 거래"));

        add(definitions, "C21", "지옥문 너머", 1, 1, "", o("nether_biomes", 1, "네더 바이옴"));
        add(definitions, "C22", "고대 도시 탐사", 2, 5, "", o("structure:ancient_city", 1, "고대 도시 방문"));
        add(definitions, "C23", "시련의 문턱", 2, 4, "", o("structure:trial_chambers", 1, "시련의 회당 방문"));
        add(definitions, "C24", "숲속의 저택", 3, 8, "", o("structure:mansion", 1, "삼림 대저택 방문"));
        add(definitions, "C25", "해저 신전 발견", 2, 4, "", o("structure:monument", 1, "해저 유적 방문"));
        add(definitions, "C26", "전초기지 정찰", 1, 2, "", o("structure:pillager_outpost", 1, "약탈자 전초기지 방문"));
        add(definitions, "C27", "잊힌 사원", 2, 4, "",
                o("structure:desert_pyramid", 1, "사막 피라미드 방문"), o("structure:jungle_pyramid", 1, "정글 사원 방문"));
        add(definitions, "C28", "바다의 잔해", 1, 2, "",
                o("structure:shipwreck", 1, "난파선 방문"), o("structure:ocean_ruin", 1, "바다 폐허 방문"));
        add(definitions, "C29", "미궁의 입구", 2, 4, "", o("structure:illagerinvasion:labyrinth", 1, "미궁 방문"));
        add(definitions, "C30", "일리저 요새 침투", 2, 4, "", o("structure:illagerinvasion:illager_fort", 1, "일리저 요새 방문"));
        add(definitions, "C31", "환영술사의 탑", 2, 4, "", o("structure:illagerinvasion:illusioner_tower", 1, "환영술사 탑 방문"));
        add(definitions, "C32", "술사의 오두막들", 2, 4, "",
                o("structure:illagerinvasion:sorcerer_hut", 1, "주술사 오두막 방문"),
                o("structure:illagerinvasion:firecaller_hut", 1, "화염술사 오두막 방문"));
        add(definitions, "C33", "일리저 영토 정복", 3, 9, "C29,C30,C31,C32",
                o("structure:illagerinvasion:labyrinth", 1, "미궁 방문"),
                o("structure:illagerinvasion:illager_fort", 1, "일리저 요새 방문"),
                o("structure:illagerinvasion:illusioner_tower", 1, "환영술사 탑 방문"),
                o("structure:illagerinvasion:sorcerer_hut", 1, "주술사 오두막 방문"),
                o("structure:illagerinvasion:firecaller_hut", 1, "화염술사 오두막 방문"));
        add(definitions, "C34", "세계 지도 제작자", 3, 9, "C05", o("overworld_biomes", 35, "오버월드 바이옴"));
        add(definitions, "C35", "구조물 수집가", 4, 17, "", o("structures_visited", 12, "방문한 구조물 종류"));
        add(definitions, "C36", "끝섬의 보물", 3, 8, "C17", o("kill:minecraft:shulker", 100, "셜커 처치"));

        add(definitions, "D11", "상점 단골", 1, 1, "", o("shop_trades", 100, "상점 교환"));
        add(definitions, "D12", "상점 큰손", 3, 8, "D11", o("shop_trades", 1000, "상점 교환"));
        add(definitions, "D13", "첫 번째 행운", 1, 1, "", o("random_boxes", 1, "랜덤 상자 개봉"));
        add(definitions, "D14", "상자 수집가", 2, 4, "D13", o("random_boxes", 10, "랜덤 상자 개봉"));
        add(definitions, "D15", "최고급 상자", 3, 8, "D13", o("random_box:iv", 1, "랜덤 상자 IV 개봉"));
        add(definitions, "D16", "확률의 지배자", 4, 17, "D14", o("random_boxes", 30, "랜덤 상자 개봉"));
        add(definitions, "D17", "분산 투자", 4, 17, "D09", o("stock_round_trips", 20, "유효 매수·매도 종목"));
        add(definitions, "D18", "첫 수익 실현", 2, 3, "D08", o("stock_profitable_sells", 1, "원금 1 BTC 이상 수익 매도"));
        add(definitions, "D19", "고수익 실현", 4, 17, "D18", o("stock_best_return", 100, "원금 1 BTC 이상 매도 최고 수익률(%)"));
        add(definitions, "D20", "영지 확장", 3, 8, "D06", o("claimed_chunks", 9, "동시 소유 청크"));
        add(definitions, "D21", "대영주", 4, 17, "D20", o("claimed_chunks", 16, "동시 소유 청크"));
        add(definitions, "D22", "시간을 사는 사람", 2, 4, "", o("coupons_used", 10, "보상 쿠폰 사용"));

        add(definitions, "E13", "잿빛 단조", 1, 2, "", o("forge:ash", 1, "잿빛 장비 제작"));
        add(definitions, "E14", "태양빛 단조", 2, 4, "E13", o("forge:sunlight", 1, "태양빛 장비 제작"));
        add(definitions, "E15", "영혼빛 단조", 3, 8, "E14", o("forge:soul", 1, "영혼빛 장비 제작"));
        add(definitions, "E16", "서리빛 단조", 3, 9, "E15", o("forge:frost", 1, "서리빛 장비 제작"));
        add(definitions, "E17", "서리빛 완전 무장", 4, 17, "E16", o("frost_armor_set", 1, "서리빛 방어구 4부위 동시 착용"));
        add(definitions, "E18", "세 개의 명품", 4, 17, "E11", o("enhanced7_items", 3, "+7 이상 강화 장비"));
        add(definitions, "E19", "끈기의 대장장이", 3, 8, "E10", o("enhance_attempts", 100, "강화 시도"));
        add(definitions, "E20", "무기고 확장", 4, 17, "E04", o("gun_models_level50", 6, "직접 Lv.50 도달 모델"));
        add(definitions, "E21", "총잡이의 기록", 3, 8, "E01", o("gun_kills", 500, "총기로 적대몹 처치"));
        add(definitions, "E22", "명사수", 4, 17, "E21", o("gun_kills", 3000, "총기로 적대몹 처치"));
        add(definitions, "E23", "도구 장인", 5, 23, "E09", o("tool_types_level100", 5, "직접 Lv.100 도달 종류"));
        add(definitions, "E24", "첫 총기 제작", 1, 1, "", o("guns_crafted", 1, "총기 작업대에서 총기 제작"));

        add(definitions, "F09", "첫 동료", 1, 1, "", o("pet_max_level", 1, "본인 펫 보유"));
        add(definitions, "F10", "일반 펫 도감", 2, 5, "F09", petObjectives(NORMAL_PETS));
        add(definitions, "F11", "희귀 펫 도감", 3, 9, "F05", petObjectives(RARE_PETS));
        add(definitions, "F12", "전설 펫 도감", 4, 20, "F06", petObjectives(LEGEND_PETS));
        List<String> allPets = new ArrayList<>(NORMAL_PETS);
        allPets.addAll(RARE_PETS);
        allPets.addAll(LEGEND_PETS);
        add(definitions, "F13", "펫 도감 완성", 5, 23, "F10,F11,F12", petObjectives(allPets));
        add(definitions, "F14", "함께 걷는 셋", 3, 9, "F07", o("pet_types_level50", 3, "Lv.50 펫 종류"));
        add(definitions, "F15", "여섯 동료", 4, 17, "F07", o("pet_types_level20", 6, "Lv.20 펫 종류"));
        add(definitions, "F16", "희귀한 성장", 4, 17, "F04", o("rare_pet_max_level", 100, "희귀 펫 최고 레벨"));
        add(definitions, "F17", "두 전설의 완성", 5, 23, "F08", o("legend_pets_level100", 2, "Lv.100 전설 펫 종류"));
        add(definitions, "F18", "펫과 함께한 전투", 3, 8, "F02", o("pet_kills", 500, "펫이 처치한 적대몹"));

        add(definitions, "G01", "약탈자 소탕", 1, 2, "", o("kill:minecraft:pillager", 50, "약탈자 처치"));
        add(definitions, "G02", "습격의 선봉 저지", 2, 5, "G01",
                o("kill:minecraft:vindicator", 30, "변명자 처치"), o("kill:minecraft:evoker", 5, "소환사 처치"));
        add(definitions, "G03", "파괴수 사냥", 3, 8, "G02", o("kill:minecraft:ravager", 10, "파괴수 처치"));
        add(definitions, "G04", "변방의 일리저", 2, 5, "",
                illagerKill("provoker", 10), illagerKill("basher", 10), illagerKill("marauder", 10));
        add(definitions, "G05", "심문과 기록", 3, 8, "G04", illagerKill("inquisitor", 5), illagerKill("archivist", 5));
        add(definitions, "G06", "어둠의 술사들", 3, 9, "G04",
                illagerKill("necromancer", 3), illagerKill("sorcerer", 3), illagerKill("firecaller", 3));
        add(definitions, "G07", "비약 도둑", 2, 4, "G04", illagerKill("alchemist", 5), illagerKill("surrendered", 20));
        List<Objective> illagerObjectives = new ArrayList<>();
        for (String mob : ILLAGER_INVASION_MOBS)
        {
            illagerObjectives.add(illagerKill(mob, 10));
        }
        add(definitions, "G08", "일리저 대토벌", 4, 17, "G05,G06,G07", illagerObjectives.toArray(Objective[]::new));
        add(definitions, "G09", "바람의 시련", 2, 4, "", o("kill:minecraft:breeze", 30, "브리즈 처치"));
        add(definitions, "G10", "시련의 정복자", 3, 9, "G09", o("kill:minecraft:breeze", 150, "브리즈 처치"));
        add(definitions, "G11", "공허의 사냥꾼", 3, 8, "", o("kill:minecraft:enderman", 300, "엔더맨 처치"));
        add(definitions, "G12", "검은 해골의 천적", 3, 9, "", o("kill:minecraft:wither_skeleton", 200, "위더 스켈레톤 처치"));
        // 기록 이름은 이어 쓰는 처치 수를 위해 예전 기준 그대로다. 실제 기준은 위험 6단계 이상과 7단계다.
        add(definitions, "G13", "강적 사냥", 3, 8, "B16", o("monster_kills_level5", 200, "LV12+ 몬스터 처치 (크리퍼 LV6+)"));
        add(definitions, "G14", "정점의 포식자", 4, 17, "G13", o("monster_kills_level7", 100, "LV15+ 몬스터 처치 (크리퍼 LV7)"));
    }

    private static Map<String, List<String>> createStructures()
    {
        Map<String, List<String>> structures = new LinkedHashMap<>();
        for (String id : List.of("stronghold", "fortress", "bastion_remnant", "end_city", "ancient_city",
                "trial_chambers", "mansion", "monument", "pillager_outpost", "desert_pyramid", "jungle_pyramid"))
        {
            structures.put(id, List.of("minecraft:" + id));
        }
        structures.put("shipwreck", List.of("minecraft:shipwreck", "minecraft:shipwreck_beached"));
        structures.put("ocean_ruin", List.of("minecraft:ocean_ruin_cold", "minecraft:ocean_ruin_warm"));
        for (String id : List.of("labyrinth", "illager_fort", "illusioner_tower", "sorcerer_hut", "firecaller_hut"))
        {
            structures.put("illagerinvasion:" + id, List.of("illagerinvasion:" + id));
        }
        return java.util.Collections.unmodifiableMap(structures);
    }

    private static Objective[] petObjectives(List<String> petTypes)
    {
        Objective[] objectives = new Objective[petTypes.size()];
        for (int index = 0; index < petTypes.size(); index++)
        {
            objectives[index] = o("pet_type:" + petTypes.get(index), 1, petName(petTypes.get(index)) + " 보유");
        }
        return objectives;
    }

    /** 펫 엔티티 이름. 화면이 레지스트리 번역을 찾지 못해도 게임 안내와 같은 이름을 보여 준다. */
    public static String petName(String petType)
    {
        return switch (petType)
        {
            case "advancednetherite:dialga_pet" -> "디아루가";
            case "advancednetherite:kirby_pet" -> "커비";
            case "advancednetherite:gomi_pet" -> "꼬미";
            case "advancednetherite:unicorn_pet" -> "유니콘";
            case "advancednetherite:gazelle_pet" -> "가젤";
            case "advancednetherite:fairlins_pet" -> "페어린";
            case "advancednetherite:dark_dragon_pet" -> "암흑드래곤";
            case "advancednetherite:sculken_raven_pet" -> "스컬큰 레이븐";
            case "advancednetherite:super_gomi_pet" -> "슈퍼 꼬미";
            default -> "펫";
        };
    }

    /** Illager Invasion은 한글 번역이 없어 C12의 인보커처럼 음역한 이름을 쓴다. */
    public static String illagerName(String mob)
    {
        return switch (mob)
        {
            case "provoker" -> "프로보커";
            case "basher" -> "배셔";
            case "marauder" -> "머로더";
            case "inquisitor" -> "인퀴지터";
            case "archivist" -> "아키비스트";
            case "necromancer" -> "네크로맨서";
            case "sorcerer" -> "소서러";
            case "firecaller" -> "파이어콜러";
            case "alchemist" -> "알케미스트";
            case "surrendered" -> "서렌더드";
            default -> "일리저";
        };
    }

    private static Objective illagerKill(String mob, long target)
    {
        return o("kill:illagerinvasion:" + mob, target, illagerName(mob) + " 처치");
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
        if (definitions.size() != 200 || rewards != 1941)
        {
            throw new IllegalStateException("Achievement catalog must contain 200 quests and 1,941 diamonds");
        }
    }
}
