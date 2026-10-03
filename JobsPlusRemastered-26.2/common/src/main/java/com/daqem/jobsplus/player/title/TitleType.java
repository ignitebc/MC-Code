package com.daqem.jobsplus.player.title;

import com.daqem.jobsplus.JobsPlus;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

import java.util.Optional;

/**
 * 서버 전체에서 한 명만 가질 수 있는 칭호.
 *
 * <p>배지는 {@code assets/jobsplus/font/title_badge.json}에 글자 하나로 등록한 그림이다.
 * 글자로 넣어야 채팅, 머리 위 이름, Tab 목록이 바닐라 경로 그대로 배지를 보여 준다.
 */
public enum TitleType
{
    NETHER_STAR("nether_star", "네더의별", "서버에서 처음으로 위더 처치", '', 1152),
    SEAL_BREAKER("seal_breaker", "봉인해제", "서버에서 처음으로 하이퍼 스킬 개방", '', 1152),
    LAST_STRIKE("last_strike", "마지막일격", "서버에서 처음으로 엔더 드래곤 처치 (마지막 일격)", '', 1152),
    TEN_THOUSAND_SOULS("ten_thousand_souls", "만개의영혼", "서버에서 처음으로 적대 몹 1만 마리 처치", '', 1152);

    /** 목록은 원본 픽셀, 이름·채팅은 같은 그림을 종횡비 그대로 축소한 bitmap 글리프를 쓴다. */
    public static final int TEXTURE_HEIGHT = 256;
    /** title_badge.json의 height 18에 위아래 여백을 확보한다. */
    public static final int BADGE_LINE_HEIGHT = 20;
    /** ascent 12인 배지의 위쪽 돌출분(12 - 바닐라 기준선 7)을 보정한다. */
    public static final int BADGE_TEXT_OFFSET = 5;

    private static final FontDescription BADGE_FONT = new FontDescription.Resource(JobsPlus.getId("title_badge"));
    /** 그림 색을 그대로 내도록 흰색으로 고정한다. 팀 색이나 채팅 색이 덧칠되지 않게 한다. */
    private static final int BADGE_COLOR = 0xFFFFFF;
    /** 채팅 글자 그림자가 배지 뒤에 어두운 사본으로 한 번 더 찍히지 않도록 투명하게 둔다. */
    private static final int NO_SHADOW = 0;

    private final String id;
    private final String displayName;
    private final String condition;
    private final char glyph;
    private final int textureWidth;

    TitleType(String id, String displayName, String condition, char glyph, int textureWidth)
    {
        this.id = id;
        this.displayName = displayName;
        this.condition = condition;
        this.glyph = glyph;
        this.textureWidth = textureWidth;
    }

    public String getId()
    {
        return this.id;
    }

    public String getDisplayName()
    {
        return this.displayName;
    }

    public String getCondition()
    {
        return this.condition;
    }

    public int getTextureWidth()
    {
        return this.textureWidth;
    }

    public Identifier getTexture()
    {
        return JobsPlus.getId("textures/gui/title_badge/" + this.id + ".png");
    }

    public MutableComponent getBadge()
    {
        Style style = Style.EMPTY.withFont(BADGE_FONT).withColor(BADGE_COLOR).withShadowColor(NO_SHADOW)
                .withBold(false).withItalic(false).withUnderlined(false).withStrikethrough(false).withObfuscated(false);
        return Component.literal(String.valueOf(this.glyph)).withStyle(style);
    }

    /** 실제 칭호 글꼴과 글리프가 있는 줄에만 큰 배지용 간격을 적용한다. */
    public static boolean containsBadge(FormattedCharSequence text)
    {
        return !text.accept((index, style, codePoint) ->
                !BADGE_FONT.equals(style.getFont()) || codePoint < '\uE100' || codePoint > '\uE103');
    }

    public static Optional<TitleType> byId(String id)
    {
        for (TitleType type : values())
        {
            if (type.id.equals(id))
            {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }
}
