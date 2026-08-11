package com.daqem.arc.player;

import net.minecraft.resources.Identifier;

/**
 * 홀더 위치에서 로마 숫자 단계 접미사(_i~_x)를 제거해 스킬 줄 구분 키를 만든다.
 * 같은 키를 가진 홀더들은 한 스킬의 서로 다른 단계로 취급한다.
 */
public final class SkillLineKey
{

    private SkillLineKey()
    {
    }

    public static String of(Identifier location)
    {
        return location.toString().replaceFirst("_(?:viiii|viii|vii|vi|iv|iii|ii|ix|x|v|i)$", "");
    }
}
