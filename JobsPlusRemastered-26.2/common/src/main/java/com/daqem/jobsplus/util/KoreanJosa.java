package com.daqem.jobsplus.util;

public class KoreanJosa
{

    private static final char HANGUL_FIRST = 0xAC00;
    private static final char HANGUL_LAST = 0xD7A3;
    private static final int JONGSEONG_COUNT = 28;

    private KoreanJosa()
    {
    }

    /**
     * 단어의 마지막 글자 받침 유무에 따라 '을' 또는 '를'을 돌려준다.
     * 한글 음절이 아닌 글자로 끝나면 '를'을 사용한다.
     */
    public static String eulReul(String word)
    {
        if (word == null || word.isEmpty())
        {
            return "를";
        }

        char lastChar = word.charAt(word.length() - 1);
        boolean isHangulSyllable = lastChar >= HANGUL_FIRST && lastChar <= HANGUL_LAST;
        if (!isHangulSyllable)
        {
            return "를";
        }

        boolean hasBatchim = (lastChar - HANGUL_FIRST) % JONGSEONG_COUNT != 0;
        if (hasBatchim)
        {
            return "을";
        }
        return "를";
    }
}
