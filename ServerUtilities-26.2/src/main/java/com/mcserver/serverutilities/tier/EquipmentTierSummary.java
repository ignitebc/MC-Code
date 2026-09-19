package com.mcserver.serverutilities.tier;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Tool;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 등급이 아이템 수치를 얼마나 바꿨는지 알려 주는 툴팁 줄을 만든다.
 *
 * <p>바닐라 툴팁은 등급이 붙인 수정자를 "-1.12 방어"처럼 따로 적을 뿐이고, 내구도와 채굴 속도는
 * 아예 적지 않는다. 그래서 원래 값이 얼마였고 지금 얼마가 됐는지 알 수 없다. 여기서는 기본값과
 * 적용값을 화살표로 이어 붙인 줄을 만들어 등급 바로 아래에 넣는다.
 *
 * <p>어떤 항목을 적을지는 {@link EquipmentTierRules}가 실제로 바꾸는 항목과 같게 맞춘다.
 */
public final class EquipmentTierSummary {
    /** 소수점은 둘째 자리까지만 적는다. 지역 설정과 무관하게 같은 글자가 나오도록 고정한다. */
    private static final DecimalFormat VALUE_FORMAT =
            new DecimalFormat("#,##0.##", DecimalFormatSymbols.getInstance(Locale.ROOT));

    private EquipmentTierSummary() { }

    /**
     * 등급이 바꾼 수치를 한 줄씩 담아 돌려준다.
     *
     * @return 표시할 줄 목록. 바뀌는 항목이 없으면 빈 목록
     */
    public static List<Component> describe(ItemStack stack, EquipmentTier tier) {
        DataComponentMap defaults = stack.getItem().components();
        double multiplier = tier.performanceMultiplier();
        boolean diggingTool = EquipmentTierRules.isDiggingTool(stack);
        List<Component> lines = new ArrayList<>();

        Integer baseMaxDamage = defaults.get(DataComponents.MAX_DAMAGE);
        if (baseMaxDamage != null && baseMaxDamage > 0) {
            lines.add(line("내구도", baseMaxDamage, tier.scaleDurability(baseMaxDamage),
                    tier.durabilityPercent()));
        }

        if (EquipmentTierRules.isHumanoidArmor(stack)) {
            double baseArmor = baseAmount(defaults, Attributes.ARMOR);
            if (baseArmor > 0.0D) {
                lines.add(line("방어", baseArmor, baseArmor * multiplier, tier.performancePercent()));
            }
        }

        if (!diggingTool) {
            double baseAttack = baseAmount(defaults, Attributes.ATTACK_DAMAGE);
            if (baseAttack > 0.0D) {
                // 바닐라 툴팁의 공격 피해는 플레이어 기본 공격력을 더한 값이다. 등급도 그 값에 걸린다.
                double total = baseAttack + EquipmentTierRules.PLAYER_BASE_ATTACK_DAMAGE;
                lines.add(line("공격 피해", total, total * multiplier, tier.performancePercent()));
            }
        }

        if (diggingTool) {
            double baseSpeed = baseMiningSpeed(defaults);
            if (baseSpeed > 0.0D) {
                lines.add(line("채굴 속도", baseSpeed, baseSpeed * multiplier, tier.performancePercent()));
            }
        }

        return lines;
    }

    /**
     * 항목 한 줄. 등급이 S라 값이 그대로면 화살표 없이 값만 적는다.
     *
     * @param percent 기본값에 곱한 백분율. 실제 값에서 역산하면 반올림 때문에 기준표와 어긋난다.
     */
    private static Component line(String label, double base, double scaled, int percent) {
        String text = " " + label + " " + format(base);
        if (percent != 100) {
            text += " → " + format(scaled) + " (" + (percent - 100) + "%)";
        }
        return Component.literal(text).withStyle(ChatFormatting.GRAY);
    }

    private static String format(double value) {
        return VALUE_FORMAT.format(value);
    }

    /** 아이템 기본 수정자 중 해당 속성에 더해지는 값의 합 */
    private static double baseAmount(DataComponentMap defaults, Holder<Attribute> attribute) {
        ItemAttributeModifiers modifiers =
                defaults.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);

        double total = 0.0D;
        for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            if (entry.modifier().operation() != AttributeModifier.Operation.ADD_VALUE) continue;
            if (!attribute.equals(entry.attribute())) continue;
            total += entry.modifier().amount();
        }
        return total;
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
