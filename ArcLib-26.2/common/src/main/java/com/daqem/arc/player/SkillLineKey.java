package com.daqem.arc.player;

import net.minecraft.resources.Identifier;

import java.util.regex.Pattern;

/**
 * 홀더 위치에서 로마 숫자 단계 접미사(_i~_x)를 제거해 스킬 줄 구분 키를 만든다.
 * 같은 키를 가진 홀더들은 한 스킬의 서로 다른 단계로 취급한다.
 */
public final class SkillLineKey
{

    // String#replaceFirst 는 호출할 때마다 정규식을 다시 컴파일하므로 한 번만 컴파일해 둔다.
    private static final Pattern TIER_SUFFIX_PATTERN =
            Pattern.compile("_(?:viiii|viii|vii|vi|iv|iii|ii|ix|x|v|i)$");

    private SkillLineKey()
    {
    }

    public static String of(Identifier location)
    {
        return TIER_SUFFIX_PATTERN.matcher(location.toString()).replaceFirst("");
    }
}
