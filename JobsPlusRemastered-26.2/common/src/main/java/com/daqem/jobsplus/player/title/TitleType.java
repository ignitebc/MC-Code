package com.daqem.jobsplus.player.title;

import com.daqem.jobsplus.JobsPlus;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;

import java.util.Optional;

/**
 * 서버 전체에서 한 명만 가질 수 있는 칭호.
 *
 * <p>배지는 {@code assets/jobsplus/font/title_badge.json}에 글자 하나로 등록한 그림이다.
 * 글자로 넣어야 채팅, 머리 위 이름, Tab 목록이 바닐라 경로 그대로 배지를 보여 준다.
 */
public enum TitleType
{
    NETHER_STAR("nether_star", "네더의별", "서버에서 처음으로 위더 처치", '', 140),
    SEAL_BREAKER("seal_breaker", "봉인해제", "서버에서 처음으로 하이퍼 스킬 개방", '', 140),
    LAST_STRIKE("last_strike", "마지막일격", "서버에서 처음으로 엔더 드래곤 처치 (마지막 일격)", '', 160),
    TEN_THOUSAND_SOULS("ten_thousand_souls", "만개의영혼", "서버에서 처음으로 적대 몹 1만 마리 처치", '', 160);

    /** 배지 그림의 세로 픽셀 수. 게임 안 9px 한 줄을 4배 밀도로 그렸다. */
    public static final int TEXTURE_HEIGHT = 36;

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
        return JobsPlus.getId("textures/font/title_badge/" + this.id + ".png");
    }

    public MutableComponent getBadge()
    {
        Style style = Style.EMPTY.withFont(BADGE_FONT).withColor(BADGE_COLOR).withShadowColor(NO_SHADOW);
        return Component.literal(String.valueOf(this.glyph)).withStyle(style);
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
