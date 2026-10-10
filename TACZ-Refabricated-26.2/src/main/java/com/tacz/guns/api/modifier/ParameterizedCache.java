package com.tacz.guns.api.modifier;


import com.google.common.collect.ImmutableList;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import com.tacz.guns.resource.pojo.data.attachment.Modifier;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 매개변수가 있는 속성 캐시. 곱셈 구간마다 결과를 캐시에 저장해 빠르게 계산한다.
 * 반동처럼 초깃값을 바로 정할 수 없는 속성에 쓴다
 */
public class ParameterizedCache<T> {
    private final T defaultValue;
    private final List<String> scripts;
    private final double addend;
    private final double percent;
    private final double multiplier;

    public ParameterizedCache(List<Modifier> modifiers, T defaultValue) {
        double addend = 0;
        double percent = 1;
        double multiplier = 1;

        ImmutableList.Builder<String> builder = new ImmutableList.Builder<>();
        for (Modifier mod : modifiers) {
            addend += mod.getAddend();
            percent += mod.getPercent();
            multiplier *= Math.max(mod.getMultiplier(), 0f);
            if (StringUtils.isNotEmpty(mod.getFunction())) {
                builder.add(mod.getFunction());
            }
        }

        this.addend = addend;
        this.percent = percent;
        this.multiplier = multiplier;
        this.scripts = builder.build();
        this.defaultValue = defaultValue;
    }

    public T getDefaultValue() {
        return defaultValue;
    }

    public double eval(double input) {
        double percent = Math.max(this.percent, 0);
        double value = (input + addend) * percent * multiplier;
        for (String function : scripts) {
            if (StringUtils.isEmpty(function)) {
                continue;
            }
            value = AttachmentPropertyManager.functionEval(value, input, function);
        }
        return value;
    }

    public double eval(double input, double extraAddend, double extraPercent, double extraMultiplier) {
        double percent = Math.max(this.percent + extraPercent, 0);
        extraMultiplier = Math.max(extraMultiplier, 0);
        double value = (input + addend + extraAddend) * percent * multiplier * extraMultiplier;
        for (String function : scripts) {
            if (StringUtils.isEmpty(function)) {
                continue;
            }
            value = AttachmentPropertyManager.functionEval(value, input, function);
        }
        return value;
    }

    public static <T> ParameterizedCache<T> of(T defaultValue) {
        return new ParameterizedCache<>(List.of(), defaultValue);
    }

    public static <T> ParameterizedCache<T> of(List<Modifier> modifiers, T defaultValue) {
        return new ParameterizedCache<>(modifiers, defaultValue);
    }

}
