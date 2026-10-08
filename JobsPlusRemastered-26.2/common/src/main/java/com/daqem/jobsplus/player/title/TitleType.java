package com.daqem.jobsplus.player.title;

import com.daqem.jobsplus.JobsPlus;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
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
    SEAL_BREAKER("seal_breaker", "봉인해제", "서버에서 처음으로 하이퍼스킬 개방", '', 1152),
    LAST_STRIKE("last_strike", "마지막일격", "서버에서 처음으로 엔더 드래곤 처치 (마지막 일격)", '', 1152),
    TEN_THOUSAND_SOULS("ten_thousand_souls", "만개의영혼", "서버에서 처음으로 적대 몹 1만 마리 처치", '', 1152),
    SKY_RULER("sky_ruler", "하늘의지배자", "서버에서 처음으로 겉날개 획득", '\uE104', 1152),
    ETERNAL_PEAK("eternal_peak", "영원한정점", "서버에서 처음으로 서리빛 방어구 4부위 +10강 착용", '\uE105', 1152);

    /** 목록은 원본 픽셀, 이름·채팅은 같은 그림을 종횡비 그대로 축소한 bitmap 글리프를 쓴다. */
    public static final int TEXTURE_HEIGHT = 256;
    /** title_badge.json의 height 18에 위아래 여백을 확보한다. */
    public static final int BADGE_LINE_HEIGHT = 20;
    /** ascent 12인 배지의 위쪽 돌출분(12 - 바닐라 기준선 7)을 보정한다. */
    public static final int BADGE_TEXT_OFFSET = 5;
    /** 채팅 전용 배지는 height 14 / ascent 10으로 표시한다. */
    public static final int CHAT_BADGE_LINE_HEIGHT = 16;
    public static final int CHAT_BADGE_TEXT_OFFSET = 3;

    private static final FontDescription BADGE_FONT = new FontDescription.Resource(JobsPlus.getId("title_badge"));
    private static final FontDescription CHAT_BADGE_FONT = new FontDescription.Resource(JobsPlus.getId("title_badge_chat"));
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
                !(BADGE_FONT.equals(style.getFont()) || CHAT_BADGE_FONT.equals(style.getFont()))
                        || !isBadgeGlyph(codePoint));
    }

    /** 칭호를 추가할 때 글자 범위를 따로 고치지 않도록 등록된 칭호의 글자와 직접 비교한다. */
    private static boolean isBadgeGlyph(int codePoint)
    {
        for (TitleType type : values())
        {
            if (type.glyph == codePoint)
            {
                return true;
            }
        }
        return false;
    }

    /** 줄바꿈 전에 글꼴을 바꿔 채팅의 폭 계산과 클릭 영역에도 축소된 크기를 적용한다. */
    public static FormattedText forChat(FormattedText text)
    {
        List<FormattedText> parts = new ArrayList<>();
        text.visit((style, content) -> {
            Style chatStyle = BADGE_FONT.equals(style.getFont()) ? style.withFont(CHAT_BADGE_FONT) : style;
            parts.add(FormattedText.of(content, chatStyle));
            return Optional.empty();
        }, Style.EMPTY);
        return FormattedText.composite(parts);
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
