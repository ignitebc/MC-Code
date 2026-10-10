package moe.caramel.chat.driver;

/**
 * 현재 키보드 상태를 가져온다.
 *
 * @param language 현재 IME 언어
 * @param useNative 모국어 표시를 쓸지 여부
 */
public record KeyboardStatus(Language language, boolean useNative) {

    @Override
    public Language language() {
        return useNative() ? language : Language.ENGLISH;
    }

    /**
     * 언어 전환 알림에 쓸 표시를 가져온다.
     *
     * @return 표시
     */
    public String display() {
        return language().display;
    }

    /**
     * 표시기 X 오프셋을 가져온다.
     *
     * @return X 오프셋
     */
    public float offset() {
        return language().offset;
    }

    /**
     * 표시 목록
     */
    public enum Language {

        ENGLISH("ENG", 0.5f),
        KOREAN("한", 0.0f),
        JAPANESE("あ", 0.5f),
        CHINESE_SIMPLIFIED("中", 0.5f),
        CHINESE_TRADITIONAL("中", 0.5f),
        OTHER("Native", 0.5f);

        private final String display;
        private final float offset;

        Language(final String display, final float offset) {
            this.display = display;
            this.offset = offset;
        }
    }
}
