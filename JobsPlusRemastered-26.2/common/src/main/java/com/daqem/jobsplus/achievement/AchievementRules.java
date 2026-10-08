package com.daqem.jobsplus.achievement;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Set;
import java.util.ArrayList;
import java.util.List;

/** 태그 변경으로 업적 대상이 늘어나지 않도록 채굴·굴착·수확 대상을 고정한다. */
public final class AchievementRules
{
    public static final Set<String> ORES = Set.of(
            "coal_ore", "deepslate_coal_ore", "copper_ore", "deepslate_copper_ore",
            "iron_ore", "deepslate_iron_ore", "gold_ore", "deepslate_gold_ore",
            "redstone_ore", "deepslate_redstone_ore", "emerald_ore", "deepslate_emerald_ore",
            "lapis_ore", "deepslate_lapis_ore", "diamond_ore", "deepslate_diamond_ore",
            "nether_gold_ore", "nether_quartz_ore", "ancient_debris");
    public static final Set<String> EXCAVATION = Set.of(
            "dirt", "grass_block", "coarse_dirt", "rooted_dirt", "podzol", "mycelium",
            "mud", "sand", "red_sand", "gravel");
    public static final Set<String> CROPS = Set.of("wheat", "carrots", "potatoes", "beetroots", "nether_wart", "cocoa", "sweet_berry_bush");

    private AchievementRules()
    {
    }

    public static String blockId(BlockState state)
    {
        return BuiltInRegistries.BLOCK.getKey(state.getBlock()).toString();
    }

    public static boolean isOre(BlockState state)
    {
        return containsBlock(ORES, state);
    }

    public static boolean isExcavation(BlockState state)
    {
        return containsBlock(EXCAVATION, state);
    }

    private static boolean containsBlock(Set<String> blocks, BlockState state)
    {
        String id = blockId(state);
        if (!id.startsWith("minecraft:"))
        {
            return false;
        }
        if (blocks.contains(id.substring("minecraft:".length())))
        {
            return true;
        }
        return false;
    }

    public static boolean isMatureCrop(BlockState state)
    {
        if (!containsBlock(CROPS, state))
        {
            return false;
        }
        if (state.getBlock() instanceof CropBlock crop)
        {
            return crop.isMaxAge(state);
        }
        if (state.getBlock() instanceof NetherWartBlock)
        {
            if (state.getValue(NetherWartBlock.AGE) == NetherWartBlock.MAX_AGE)
            {
                return true;
            }
        }
        if (state.getBlock() instanceof CocoaBlock)
        {
            if (state.getValue(CocoaBlock.AGE) == CocoaBlock.MAX_AGE)
            {
                return true;
            }
        }
        if (state.getBlock() instanceof SweetBerryBushBlock)
        {
            if (state.getValue(SweetBerryBushBlock.AGE) == 3)
            {
                return true;
            }
        }
        return false;
    }

    public static String cropType(BlockState state)
    {
        String id = blockId(state);
        return switch (id)
        {
            case "minecraft:wheat" -> "wheat";
            case "minecraft:carrots" -> "carrot";
            case "minecraft:potatoes" -> "potato";
            case "minecraft:beetroots" -> "beetroot";
            case "minecraft:nether_wart" -> "nether_wart";
            case "minecraft:cocoa" -> "cocoa";
            case "minecraft:sweet_berry_bush" -> "sweet_berry";
            default -> "other";
        };
    }

    /** 다이아몬드·에메랄드 광석은 심층암 광석도 같은 종류로 센다. */
    public static String oreType(BlockState state)
    {
        return switch (blockId(state))
        {
            case "minecraft:diamond_ore", "minecraft:deepslate_diamond_ore" -> "diamond";
            case "minecraft:emerald_ore", "minecraft:deepslate_emerald_ore" -> "emerald";
            default -> "";
        };
    }

    /**
     * 낚시 보물 전리품인지. 쓰레기 전리품의 낚싯대는 마법이 없으므로 마법이 붙은 낚싯대만 보물로 본다.
     * 활은 보물 전리품으로만 나온다.
     */
    public static boolean isFishingTreasure(ItemStack stack)
    {
        if (stack.is(Items.FISHING_ROD))
        {
            return stack.isEnchanted();
        }
        return stack.is(Items.ENCHANTED_BOOK) || stack.is(Items.NAME_TAG) || stack.is(Items.NAUTILUS_SHELL)
                || stack.is(Items.SADDLE) || stack.is(Items.BOW);
    }

    /** 대장장이 작업대에서 받은 장비의 네더라이트 단계. 주괴와 블록은 조합대에서 만들므로 제외한다. */
    public static String forgeTier(ItemStack stack)
    {
        Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!id.getNamespace().equals("advancednetherite") || id.getPath().endsWith("_ingot") || id.getPath().endsWith("_block"))
        {
            return "";
        }
        for (String tier : List.of("ash", "sunlight", "soul", "frost"))
        {
            if (id.getPath().startsWith(tier + "_"))
            {
                return tier;
            }
        }
        return "";
    }

    public static String toolType(ItemStack stack)
    {
        if (stack.is(ItemTags.PICKAXES))
        {
            return "pickaxe";
        }
        if (stack.is(ItemTags.SHOVELS))
        {
            return "shovel";
        }
        if (stack.is(ItemTags.AXES))
        {
            return "axe";
        }
        if (stack.is(ItemTags.HOES))
        {
            return "hoe";
        }
        if (stack.getItem() instanceof FishingRodItem)
        {
            return "fishing_rod";
        }
        return "";
    }

    /** EXP를 얻은 이번 변경에서 실제로 넘긴 목표만 반환한다. 지난 레벨은 소급하지 않는다. */
    public static List<Integer> crossedEquipmentMilestones(int oldExperience, int newExperience)
    {
        if (newExperience <= oldExperience)
        {
            return List.of();
        }
        int oldLevel = 1 + Math.clamp(oldExperience, 0, 9900) / 100;
        int newLevel = 1 + Math.clamp(newExperience, 0, 9900) / 100;
        List<Integer> crossed = new ArrayList<>();
        for (int threshold : List.of(25, 50, 100))
        {
            if (oldLevel < threshold && newLevel >= threshold)
            {
                crossed.add(threshold);
            }
        }
        return List.copyOf(crossed);
    }

    public static String detailsFor(String id)
    {
        // 후반 확장 업적은 분류 공통 문구보다 먼저 고른다.
        String expansion = expansionDetails(id);
        if (expansion != null)
        {
            return expansion;
        }
        if (id.startsWith("A"))
        {
            return "일반스킬은 모든 구매 단계를 확인합니다. 구매 후 비활성화해도 인정하며 하이퍼스킬은 제외합니다.";
        }
        if (Set.of("B01", "B02", "B03", "B04").contains(id))
        {
            return "설치 블록 제외. 대상: " + String.join(", ", ORES.stream().sorted().toList());
        }
        if (Set.of("B05", "B06", "B07").contains(id))
        {
            return "설치 블록 제외. 대상: " + String.join(", ", EXCAVATION.stream().sorted().toList());
        }
        if (Set.of("B08", "B09", "B10", "B11").contains(id))
        {
            return "성숙 블록당 1회. 추가 드롭 제외. 대상: " + String.join(", ", CROPS.stream().sorted().toList());
        }
        if (Set.of("B12", "B13", "B14", "B15").contains(id))
        {
            return "정상 낚시로 전리품을 회수한 캐스팅당 1회. 걸린 엔티티 회수·실패·자동 낚시·추가 드롭 제외.";
        }
        if (Set.of("B19", "B20", "B21").contains(id))
        {
            return "본인이 병과 재료를 직접 넣어 효과 없는 물약을 효과 물약으로 처음 완성한 병만 인정. 시간 연장·강화·투척형 전환·호퍼 투입 제외.";
        }
        if (Set.of("B22", "B23").contains(id))
        {
            return "본인이 투입한 재료로 제련한 완제품을 본인이 꺼낸 실제 수량. 호퍼 투입·회수 및 다른 사람의 완제품 제외.";
        }
        if (id.equals("C04"))
        {
            return "네더 바이옴: " + String.join(", ", AchievementCatalog.NETHER_BIOMES);
        }
        if (id.equals("C05"))
        {
            return "현재 서버의 오버월드 생성기가 사용하는 바이옴만 인정합니다. 서로 다른 ID를 시즌별 1회 기록합니다.";
        }
        if (Set.of("B16", "B17", "B18").contains(id))
        {
            return "서버의 Enemy 분류에 해당하는 적대몹만 집계합니다. 일반 처치는 기존 처치 공로를 사용하며, 드래곤은 별도 기여 조건을 만족해야 합니다.";
        }
        if (id.equals("C09") || id.equals("C10"))
        {
            return "개체 최대 체력의 5% 이상 실제 피해 + 사망 시 같은 차원 128블록 이내 생존. 개체당 1회.";
        }
        if (id.equals("C07") || id.equals("C08"))
        {
            return "바닐라 습격의 영웅 공로 목록에 등록되어 있고, 승리 전환 시 같은 차원에 생존해 있는 플레이어. 습격 ID당 1회.";
        }
        if (id.equals("C20"))
        {
            return "지정 목록의 20종 모두 각각 10마리: " + String.join(", ", AchievementCatalog.COMBAT_MOBS);
        }
        if (id.equals("D08") || id.equals("D09"))
        {
            return "종목별 실제 체결 원금 합계 1 BTC 이상을 매수하고 해당 투자 원금 전량을 직접 매도. 기존 보유분·예약·취소·실패·강제 청산 제외. 종목당 1회.";
        }
        if (id.startsWith("E"))
        {
            return "장비 개체별 본인 EXP 기여와 목표 레벨에 도달한 순간을 저장합니다. 소지만으로는 인정하지 않습니다. 도구 종류: 곡괭이·삽·도끼·괭이·낚싯대. 강화는 본인 성공 기록.";
        }
        return "시즌별 1회 수령. 모든 + 조건을 충족해야 하며 선행 업적 달성 후 해금됩니다.";
    }

    /** 후반 확장 업적(A21 이후와 G 분류)의 안내. 분류 공통 문구가 맞지 않는 업적만 다룬다. */
    private static String expansionDetails(String id)
    {
        return switch (id)
        {
            case "A21", "A22", "A23", "A24", "A25", "A26", "A27", "A28" ->
                    "해당 직업의 현재 레벨을 확인합니다. 비활성화한 직업도 인정합니다.";
            case "A29", "A30" -> "8개 직업의 현재 레벨을 확인합니다. 비활성화한 직업도 인정합니다.";
            case "A31" -> "각 직업의 장인 업적 중 4개를 완성한 상태여야 합니다. 비활성화한 일반스킬도 인정하며 하이퍼스킬은 제외합니다.";
            case "A32", "A33" -> "모든 직업에서 구매한 일반스킬 단계를 합산합니다. 비활성화한 일반스킬도 인정하며 하이퍼스킬은 제외합니다.";
            case "A34", "A35" -> "광부와 굴착공의 하이퍼스킬 레벨이 각각 목표 이상이어야 합니다.";
            case "A36" -> "보유한 직업 중 가장 높은 레벨을 확인합니다.";
            case "B31", "B32" -> "자연 생성된 광석만 집계하며 심층암 광석도 포함합니다. 직접 설치한 블록은 제외합니다.";
            case "B33", "B34" -> "설치 블록 제외. 대상: " + String.join(", ", ORES.stream().sorted().toList());
            case "B35" -> "설치 블록 제외. 대상: " + String.join(", ", EXCAVATION.stream().sorted().toList());
            case "B36" -> "성숙 블록당 1회. 추가 드롭 제외. 대상: " + String.join(", ", CROPS.stream().sorted().toList());
            case "B37", "B38", "B39" -> "완전히 자란 작물을 수확할 때 1회 집계합니다. 달콤한 열매는 우클릭 수확도 인정합니다.";
            case "B40" -> "정상 낚시로 전리품을 회수한 캐스팅당 1회. 걸린 엔티티 회수·실패·자동 낚시·추가 드롭 제외.";
            case "B41" -> "낚시로 얻은 마법이 부여된 책·이름표·앵무조개 껍데기·안장·활, 마법이 부여된 낚싯대를 1개씩 집계합니다.";
            case "B42" -> "본인이 병과 재료를 직접 넣어 효과 없는 물약을 효과 물약으로 처음 완성한 병만 인정. 시간 연장·강화·투척형 전환·호퍼 투입 제외.";
            case "B43", "B44" -> "본인이 투입한 재료로 제련한 완제품을 본인이 꺼낸 실제 수량. 호퍼 투입·회수 및 다른 사람의 완제품 제외.";
            case "B45" -> "마법부여대에서 마법을 부여한 횟수입니다.";
            case "B46" -> "겉날개로 활공한 거리입니다.";
            case "B47", "B48" -> "먹이로 번식시킨 새끼가 태어날 때 번식을 시킨 플레이어에게 1회 집계합니다.";
            case "B49", "B50" -> "주민과 떠돌이 상인과 거래를 1회 완료할 때마다 집계합니다.";
            case "C21" -> "네더의 바닐라 생물 군계를 한 곳이라도 방문하면 달성합니다.";
            case "C22", "C23", "C24", "C25", "C26", "C27", "C28" ->
                    "구조물 영역 안에 들어가면 시즌마다 1회 기록합니다. 난파선과 바다 폐허는 종류를 가리지 않습니다.";
            case "C29", "C30", "C31", "C32", "C33" -> "Illager Invasion 구조물 영역 안에 들어가면 시즌마다 1회 기록합니다.";
            case "C34" -> "현재 서버의 오버월드 생성기가 사용하는 바이옴만 인정합니다. 서로 다른 ID를 시즌별 1회 기록합니다.";
            case "C35" -> "근거지·네더 요새·보루 잔해·엔드 도시·고대 도시·시련의 회당·삼림 대저택·해저 유적·약탈자 전초기지·"
                    + "사막 피라미드·정글 사원·난파선·바다 폐허와 Illager Invasion 구조물 5종 중 서로 다른 구조물 수입니다.";
            case "C36" -> "셜커를 직접 처치한 수입니다.";
            case "D11", "D12" -> "직업 화면 상점에서 교환을 1회 완료할 때마다 집계합니다.";
            case "D13", "D14", "D15", "D16" -> "랜덤 상자 I~IV를 열어 보상을 받았을 때 집계합니다. 인벤토리가 부족해 열리지 않은 경우는 제외합니다.";
            case "D17" -> "종목별 실제 체결 원금 합계 1 BTC 이상을 매수하고 해당 투자 원금 전량을 직접 매도. 기존 보유분·예약·취소·실패·강제 청산 제외. 종목당 1회.";
            case "D18", "D19" -> "원금 1 BTC 이상을 한 번에 매도했을 때 수수료를 뺀 수익률로 판정합니다. 강제 청산은 제외합니다.";
            case "D20", "D21" -> "현재 동시에 소유한 청크 수입니다.";
            case "D22" -> "직업 경험치·비트코인 보상 쿠폰을 사용한 횟수입니다.";
            case "E13", "E14", "E15", "E16" -> "대장장이 작업대에서 해당 단계의 장비를 만들어 꺼냈을 때 달성합니다.";
            case "E17" -> "서리빛 투구·흉갑·레깅스·부츠를 동시에 착용하면 달성합니다.";
            case "E18" -> "본인이 +7 이상 강화에 성공한 서로 다른 장비 수입니다. 이후 실패로 단계가 내려가도 인정합니다.";
            case "E19" -> "강화 제작대에서 강화를 시도한 횟수입니다. 성공·실패·파괴를 모두 포함합니다.";
            case "E20" -> "장비 개체별 본인 EXP 기여와 목표 레벨에 도달한 순간을 저장합니다. 소지만으로는 인정하지 않습니다.";
            case "E21", "E22" -> "총기 탄환으로 적대몹을 처치한 수입니다. 폭발 피해는 제외합니다.";
            case "E23" -> "곡괭이·삽·도끼·괭이·낚싯대를 각각 본인 EXP로 Lv.100까지 올려야 합니다.";
            case "E24" -> "총기 작업대에서 총기를 만들면 달성합니다. 부착물과 탄약은 제외합니다.";
            case "F09", "F10", "F11", "F12", "F13" -> "보유한 펫의 종류를 확인합니다. OFF 상태로 보관한 펫도 인정합니다.";
            case "F14", "F15", "F16", "F17" -> "펫 종류별 최고 레벨을 확인합니다.";
            case "F18" -> "본인 펫이 마지막 공격으로 적대몹을 처치한 수입니다.";
            case "G01", "G02", "G03", "G04", "G05", "G06", "G07", "G08", "G09", "G10", "G11", "G12" ->
                    "직접 처치한 수입니다. 처치 공로는 기존 처치 판정을 따릅니다.";
            case "G13", "G14" -> "머리 위에 표시되는 몬스터 레벨 기준입니다. 크리퍼·피글린 계열도 포함합니다.";
            default -> null;
        };
    }
}
