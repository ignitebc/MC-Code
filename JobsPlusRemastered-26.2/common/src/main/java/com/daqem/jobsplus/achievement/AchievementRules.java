package com.daqem.jobsplus.achievement;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
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
            default -> "other";
        };
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
        if (id.startsWith("A"))
        {
            return "일반 스킬은 모든 구매 단계를 확인합니다. 구매 후 비활성화해도 인정하며 하이퍼는 제외합니다.";
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
}
