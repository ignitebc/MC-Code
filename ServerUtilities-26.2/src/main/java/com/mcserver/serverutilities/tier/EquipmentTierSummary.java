package com.mcserver.serverutilities.tier;

import com.mcserver.serverutilities.level.ToolLevelRules;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraft.world.item.component.Tool;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/**
 * 장비 등급과 도구 LV을 툴팁에 한 줄씩 적는다.
 *
 * <p>바닐라 툴팁은 등급이 붙인 수정자를 "-1.12 방어"처럼 따로 적을 뿐이고, 내구도와 채굴 속도는
 * 아예 적지 않는다. 그래서 원래 값이 얼마였고 지금 얼마가 됐는지 알 수 없다. 여기서는 LV, 내구도,
 * 성능, 체력 순서로 한 줄씩, 앞에는 항목과 등급을 적고 뒤에는 기본값과 적용값을 화살표로 이어 적는다.
 *
 * <p>어떤 항목을 적을지는 {@link EquipmentTierRules}가 실제로 바꾸는 항목과 같게 맞춘다.
 */
public final class EquipmentTierSummary {
    /** 소수점은 둘째 자리까지만 적는다. 지역 설정과 무관하게 같은 글자가 나오도록 고정한다. */
    private static final DecimalFormat VALUE_FORMAT =
            new DecimalFormat("#,##0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));

    /** 최대 충전한 활의 화살이 바로 앞 대상에 맞을 때의 바닐라 피해. 속도 3.0 × 기본 피해 2를 올린 값이다. */
    private static final double BOW_ARROW_DAMAGE = 6.0D;
    /** 쇠뇌 화살이 바로 앞 대상에 맞을 때의 바닐라 피해. 속도 3.15 × 기본 피해 2를 올린 값이다. */
    private static final double CROSSBOW_ARROW_DAMAGE = 7.0D;
    /** 체력 단위를 하트 칸으로 바꿀 때 나누는 값 */
    private static final double HEALTH_PER_HEART = 2.0D;
    private static final double FULL_PERCENT = 100.0D;

    /**
     * 행을 툴팁 줄로 만드는 방식. 서버에는 글꼴이 없으므로 공백 한 칸으로 잇고,
     * 클라이언트는 시작할 때 글꼴 폭으로 수치 열을 맞추는 방식으로 바꾼다.
     */
    private static Function<List<Row>, List<Component>> rowLayout = EquipmentTierSummary::joinWithSpace;

    private EquipmentTierSummary() { }

    /**
     * 툴팁 한 줄의 두 칸.
     *
     * @param label  항목과 등급. 예: "내구도 B"
     * @param detail 수치. 예: "2,031 → 1,810 (-10.9%)"
     */
    public record Row(Component label, Component detail) { }

    /** 행을 툴팁 줄로 만드는 방식을 바꾼다. 클라이언트 초기화 때 한 번 호출한다. */
    public static void setRowLayout(Function<List<Row>, List<Component>> layout) {
        rowLayout = layout;
    }

    /**
     * 툴팁에 넣을 줄을 LV, 내구도, 성능, 체력 순서로 돌려준다.
     *
     * <p>장비가 실제로 쓰는 항목만 적는다. LV은 성장 도구에만, 성능은 성능 등급이 쓰이는 장비에만,
     * 체력은 체력 등급을 받은 장비에만 적는다.
     *
     * @param tiers 기록된 등급 묶음. 등급이 없는 LV 도구면 null
     * @return 표시할 줄 목록. 적을 항목이 없으면 빈 목록
     */
    public static List<Component> describe(ItemStack stack, EquipmentTierSet tiers) {
        List<Row> rows = new ArrayList<>();
        if (ToolLevelRules.isLevelable(stack)) {
            rows.add(levelRow(stack));
        }

        Row durabilityRow = durabilityRow(stack, tiers);
        if (durabilityRow != null) rows.add(durabilityRow);

        Row performanceRow = performanceRow(stack, tiers);
        if (performanceRow != null) rows.add(performanceRow);

        Row healthRow = healthRow(stack, tiers);
        if (healthRow != null) rows.add(healthRow);

        if (rows.isEmpty()) return List.of();
        return rowLayout.apply(rows);
    }

    /** 글꼴 폭을 알 수 없을 때의 기본 방식. 항목과 수치를 공백 한 칸으로 잇는다. */
    public static List<Component> joinWithSpace(List<Row> rows) {
        List<Component> lines = new ArrayList<>(rows.size());
        for (Row row : rows) {
            lines.add(Component.empty().append(row.label()).append(" ").append(row.detail()));
        }
        return lines;
    }

    /** 예: "LV 37 · EXP 42/100" + "(채굴 속도 +3.6%, 내구도 +3.6%)" */
    private static Row levelRow(ItemStack stack) {
        int level = ToolLevelRules.level(stack);
        String progress = "EXP MAX";
        if (level != ToolLevelRules.MAX_LEVEL) {
            progress = "EXP " + ToolLevelRules.experienceInLevel(stack) + "/" + ToolLevelRules.EXPERIENCE_PER_LEVEL;
        }

        String bonus = "내구도 +" + formatBonus(ToolLevelRules.durabilityMultiplier(stack)) + "%";
        if (ToolLevelRules.isDiggingTool(stack)) {
            bonus = "채굴 속도 +" + formatBonus(ToolLevelRules.miningSpeedMultiplier(stack)) + "%, " + bonus;
        }
        return row("LV " + level + " · " + progress, "(" + bonus + ")", ChatFormatting.DARK_AQUA);
    }

    /** 예: "내구도 S+" + "2,031 → 2,092 (+3%)". 성장 도구는 LV 배율까지 곱한 값이다. */
    private static Row durabilityRow(ItemStack stack, EquipmentTierSet tiers) {
        Integer baseMaxDamage = stack.getItem().components().get(DataComponents.MAX_DAMAGE);
        if (baseMaxDamage == null || baseMaxDamage <= 0) return null;

        String label = "내구도";
        double percent = FULL_PERCENT;
        if (tiers != null) {
            label += " " + tiers.durability().label();
            percent = tiers.durability().durabilityPercent();
        }
        percent *= ToolLevelRules.durabilityMultiplier(stack);
        return row(label, change(baseMaxDamage, stack.getMaxDamage(), percent), ChatFormatting.GRAY);
    }

    /** 예: "효율 A" + "9 → 8.64 (-4%)", "공격 B" + "9 → 8.28 (-8%) · 투척 8 → 7.36", "방어 F" + "8 → 6.08 (-24%)" */
    private static Row performanceRow(ItemStack stack, EquipmentTierSet tiers) {
        String name = performanceName(stack);
        if (name == null) return null;
        String value = performanceValue(stack, tiers);
        if (value == null) return null;

        String label = name;
        if (tiers != null) label += " " + tiers.performance().label();
        return row(label, value, ChatFormatting.GRAY);
    }

    /**
     * 성능 등급이 바꾸는 항목의 이름.
     *
     * @return 항목 이름. 성능 등급이 쓰이지 않는 장비면 null
     */
    private static String performanceName(ItemStack stack) {
        if (EquipmentTierRules.isDiggingTool(stack)) return "효율";
        if (EquipmentTierRules.isHumanoidArmor(stack)) return "방어";
        if (EquipmentTierRules.isMeleeWeapon(stack)) return "공격";
        if (EquipmentTierRules.isRangedWeapon(stack)) return "공격";
        return null;
    }

    /**
     * 성능 줄의 수치 부분. 부류는 {@link #performanceName}과 같은 순서로 고른다.
     *
     * @return 수치 문자열. 기본값이 없어 적을 수 없으면 null
     */
    private static String performanceValue(ItemStack stack, EquipmentTierSet tiers) {
        DataComponentMap defaults = stack.getItem().components();
        double multiplier = 1.0D;
        double percent = FULL_PERCENT;
        if (tiers != null) {
            multiplier = tiers.performance().performanceMultiplier();
            percent = tiers.performance().performancePercent();
        }

        if (EquipmentTierRules.isDiggingTool(stack)) {
            double baseSpeed = baseMiningSpeed(defaults);
            if (baseSpeed <= 0.0D) return null;
            return change(baseSpeed, baseMiningSpeed(stack.getComponents()),
                    percent * ToolLevelRules.miningSpeedMultiplier(stack));
        }

        if (EquipmentTierRules.isHumanoidArmor(stack)) {
            double baseArmor = EquipmentTierRules.baseAmount(defaults, Attributes.ARMOR);
            if (baseArmor <= 0.0D) return null;
            return change(baseArmor, baseArmor * multiplier, percent);
        }

        if (EquipmentTierRules.isMeleeWeapon(stack)) {
            // 바닐라 툴팁의 공격 피해는 플레이어 기본 공격력을 더한 값이다. 등급도 그 값에 걸린다.
            double baseAttack = EquipmentTierRules.baseAmount(defaults, Attributes.ATTACK_DAMAGE)
                    + EquipmentTierRules.PLAYER_BASE_ATTACK_DAMAGE;
            String value = change(baseAttack, baseAttack * multiplier, percent);
            if (stack.getItem() instanceof TridentItem) {
                double thrownDamage = EquipmentTierRules.TRIDENT_THROWN_DAMAGE;
                value += " · 투척 " + arrowed(thrownDamage, thrownDamage * multiplier);
            }
            return value;
        }

        double arrowDamage = BOW_ARROW_DAMAGE;
        if (stack.getItem() instanceof CrossbowItem) arrowDamage = CROSSBOW_ARROW_DAMAGE;
        return "화살 " + change(arrowDamage, arrowDamage * multiplier, percent);
    }

    /** 예: "체력 A" + "+1칸 (주로 사용하는 손에 들 때)". 체력 등급을 받기 전의 예전 장비는 적지 않는다. */
    private static Row healthRow(ItemStack stack, EquipmentTierSet tiers) {
        if (tiers == null || tiers.health() == null) return null;
        if (!EquipmentTierRules.hasHealthTier(stack)) return null;

        double hearts = tiers.health().healthPoints() / HEALTH_PER_HEART;
        String value = format(hearts);
        if (hearts > 0.0D) value = "+" + value;
        value += "칸";
        if (!EquipmentTierRules.isHumanoidArmor(stack)) {
            value += " (주로 사용하는 손에 들 때)";
        }
        return row("체력 " + tiers.health().label(), value, ChatFormatting.GRAY);
    }

    /** 앞칸은 등급과 같은 빨간 글씨, 뒤칸은 수치 색으로 적은 한 행 */
    private static Row row(String label, String detail, ChatFormatting detailColor) {
        return new Row(Component.literal(label).withStyle(ChatFormatting.RED),
                Component.literal(detail).withStyle(detailColor));
    }

    /**
     * 기본값과 적용값, 바뀐 비율. 등급이 S라 값이 그대로면 화살표 없이 값만 적는다.
     *
     * @param percent 기본값에 곱한 백분율. 실제 값에서 역산하면 반올림 때문에 기준표와 어긋난다.
     */
    private static String change(double base, double scaled, double percent) {
        if (percent == FULL_PERCENT && base == scaled) return format(base);

        String difference = format(percent - FULL_PERCENT);
        if (percent > FULL_PERCENT) difference = "+" + difference;
        return format(base) + " → " + format(scaled) + " (" + difference + "%)";
    }

    /** 비율 없이 기본값과 적용값만 화살표로 잇는다. 같은 줄에 비율을 이미 적은 두 번째 수치에 쓴다. */
    private static String arrowed(double base, double scaled) {
        if (base == scaled) return format(base);
        return format(base) + " → " + format(scaled);
    }

    private static String format(double value) {
        return VALUE_FORMAT.format(value);
    }

    private static String formatBonus(double multiplier) {
        return String.format(Locale.ROOT, "%.1f", (multiplier - 1.0D) * FULL_PERCENT);
    }

    /** 적합한 블록을 캘 때의 속도. 규칙이 여럿이면 가장 빠른 값을 대표로 쓴다. */
    private static double baseMiningSpeed(DataComponentMap defaults) {
        Tool tool = defaults.get(DataComponents.TOOL);
        if (tool == null) return 0.0D;

        double fastest = 0.0D;
        for (Tool.Rule rule : tool.rules()) {
            if (rule.speed().isPresent()) {
                fastest = Math.max(fastest, rule.speed().get());
            }
        }
        return fastest;
    }
}
