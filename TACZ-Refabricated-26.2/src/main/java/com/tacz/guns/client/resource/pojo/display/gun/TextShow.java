package com.tacz.guns.client.resource.pojo.display.gun;

import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.StringUtils;

public class TextShow {
    @SerializedName("scale")
    private float scale = 1.0f;

    @SerializedName("align")
    private Align align = Align.CENTER;

    @SerializedName("shadow")
    private boolean shadow = false;

    @SerializedName("color")
    private String colorText = "#FFFFFF";

    @SerializedName("light")
    private int textLight = 15;

    @SerializedName("text")
    private String textKey = StringUtils.EMPTY;

    /** 기본은 흰색. <b>alpha가 반드시 있어야 한다</b>. 이유는 {@link #setColorInt(int)} 참고. */
    private volatile int colorInt = 0xFFFFFFFF;

    public float getScale() {
        return scale;
    }

    public Align getAlign() {
        return align;
    }

    public boolean isShadow() {
        return shadow;
    }

    public String getTextKey() {
        return textKey;
    }

    public String getColorText() {
        return colorText;
    }

    public int getTextLight() {
        return textLight;
    }

    public int getColorInt() {
        return colorInt;
    }

    /**
     * 글자 색을 설정한다. <b>불투명 alpha를 강제로 채운다.</b>
     *
     * <p>총기 팩 display json에는 {@code "color": "#FFFFFF"} 같은 <b>여섯 자리</b> 색 값이 적혀 있어,
     * {@code ColorHex.colorTextToRbgInt}로 해석하면 당연히 RGB만 있고 alpha는 0이다.
     * 1.21.1의 {@code Font#drawInBatch}는 이를 너그럽게 넘겼지만, 26.2의 글자 렌더링
     * ({@code SubmitNodeCollector#submitText}. {@code GuiGraphicsExtractor#text}와
     * 같은 판정)은 alpha == 0을 만나면 <b>글자 전체를 바로 버린다</b>.
     *
     * <p>채우지 않으면 총몸의 글자 표시(예: 8배율 조준경의 탄약 수 {@code ammo_count_text})가
     * 모두 보이지 않는다. {@code ColorHex}를 고치지 않고 여기서 채우는 이유는, 그쪽이
     * {@code colorTextToRbgFloatArray}에서도 쓰이는데 그 경로는 RGB 성분을 스스로 나누므로
     * alpha를 채우는 것이 의미가 없고 오히려 헷갈리게 할 수 있기 때문이다.
     */
    public void setColorInt(int colorInt) {
        this.colorInt = 0xFF000000 | colorInt;
    }
}
