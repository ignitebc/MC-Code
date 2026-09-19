package com.tacz.guns.resource.modifier;

import com.tacz.guns.resource.pojo.data.attachment.Modifier;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 부착물 툴팁의 효과 줄 뒤에 실제 수치를 괄호로 붙인다. 예: "+ 조준 속도 (+0.02s)", "- 수직 반동 (x0.85)".
 * 수정자는 더하기·퍼센트·곱하기·수식을 함께 가질 수 있으므로 0이 아닌 항목만 골라 적는다.
 */
public final class ModifierText {
    private ModifierText() {
    }

    public static Component line(String translationKey, @Nullable Modifier modifier, String unit, int color) {
        return line(translationKey, amount(modifier, unit), color);
    }

    public static Component line(String translationKey, String amount, int color) {
        Component text = amount.isEmpty()
                ? Component.translatable(translationKey)
                : Component.translatable(translationKey).append(" (" + amount + ")");
        return text.copy().withStyle(style -> style.withColor(color));
    }

    /** 수정자의 수치 표기. 표시할 값이 없으면 빈 문자열. */
    public static String amount(@Nullable Modifier modifier, String unit) {
        if (modifier == null) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        if (modifier.getAddend() != 0) {
            parts.add(signed(modifier.getAddend()) + unit);
        }
        if (modifier.getPercent() != 0) {
            parts.add(percent(modifier.getPercent()));
        }
        if (modifier.getMultiplier() != 1) {
            parts.add("x" + number(modifier.getMultiplier()));
        }
        if (StringUtils.isNotEmpty(modifier.getFunction())) {
            parts.add("f(x)");
        }
        return String.join(", ", parts);
    }

    /** 0.05 → "+5%" */
    public static String percent(double fraction) {
        return signed(fraction * 100) + "%";
    }

    private static String signed(double value) {
        return (value > 0 ? "+" : "") + number(value);
    }

    private static String number(double value) {
        String text = String.format(Locale.ROOT, "%.3f", value);
        text = text.replaceAll("0+$", "").replaceAll("\\.$", "");
        return text.equals("-0") ? "0" : text;
    }
}
