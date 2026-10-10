package moe.caramel.chat.wrapper;

import moe.caramel.chat.Main;
import moe.caramel.chat.driver.IOperator;
import moe.caramel.chat.util.ModLogger;
import moe.caramel.chat.util.Rect;

/**
 * 추상 IME 래퍼
 */
public abstract class AbstractIMEWrapper {

    private final IOperator ime;
    private InputStatus status = InputStatus.NONE;
    private int firstEndPos = -1, secondStartPos = -1;
    protected String origin;

    protected AbstractIMEWrapper() {
        this("");
    }

    protected AbstractIMEWrapper(final String defValue) {
        this.ime = Main.getController().createOperator(this);
        this.origin = defValue;
    }

    /**
     * IME 오퍼레이터를 가져온다.
     *
     * @return IME 오퍼레이터
     */
    public IOperator getIme() {
        return ime;
    }

    // ================================

    /**
     * 입력 상태
     */
    public enum InputStatus {
        NONE, // 완료
        PREVIEW // 미리보기
    }

    /**
     * 현재 입력 상태를 가져온다.
     *
     * @return 현재 입력 상태
     */
    public final InputStatus getStatus() {
        return status;
    }

    /**
     * 현재 입력 상태를 없음으로 설정한다.
     */
    public final void setToNoneStatus() {
        this.status = InputStatus.NONE;
        this.setPreviewText(this.origin);
    }

    /**
     * 첫 번째 글자 부분의 끝 위치를 가져온다.
     *
     * @return 첫 번째 글자 부분의 끝 위치
     */
    public final int getFirstEndPos() {
        return firstEndPos;
    }

    /**
     * 두 번째 글자 부분의 시작 위치를 가져온다.
     *
     * @return 두 번째 글자 부분의 시작 위치
     */
    public final int getSecondStartPos() {
        return secondStartPos;
    }

    // ================================

    /**
     * IME를 켤지 끌지 바꾼다.
     *
     * @param focused IME를 켤지 여부
     */
    public final void setFocused(final boolean focused) {
        this.ime.setFocused(focused);
    }

    /**
     * 현재 최종 입력값을 가져온다.
     *
     * @return 현재 최종 입력값
     */
    public final String getOrigin() {
        return origin;
    }

    /**
     * 현재 입력값을 미리보기와 함께 저장한다.
     */
    public final void setOrigin() {
        this.setOrigin(this.getTextWithPreview());
    }

    /**
     * 현재 최종 입력값을 바꾼다.
     *
     * @param value 입력값
     */
    public final void setOrigin(final String value) {
        this.origin = value;
    }

    // ================================

    /**
     * (1) 현재 입력값에 미리보기 글자를 붙인다.
     *
     * @param typing 미리보기 글자
     */
    public final void appendPreviewText(final String typing) {
        if (!this.editable()) {
            return;
        }

        ModLogger.debug("[Preview] Current: ({}) / Preview: ({})", this.origin, typing);
        this.status = InputStatus.PREVIEW;

        final int start = Math.min(this.getCursorPos(), this.getHighlightPos());
        final int end = Math.max(this.getCursorPos(), this.getHighlightPos());
        final boolean samePos = (start == end);
        final int lastPos = origin.length();

        // 다른 위치
        if (lastPos != end && samePos) {
            final String first = this.origin.substring(0, end);
            final String second = this.origin.substring(end, lastPos);
            this.firstEndPos = first.length();
            this.secondStartPos = (this.firstEndPos + typing.length());
            this.setPreviewText(first + typing + second);
        }
        // 마지막 위치
        else if (samePos) {
            final String result = (this.origin + typing);
            this.firstEndPos = this.origin.length();
            this.secondStartPos = result.length();
            this.setPreviewText(result);
        }
        // 선택됨
        else {

            // 캐시
            final String first = this.origin.substring(0, start);
            final String second = this.origin.substring(end, lastPos);

            // 선택 부분 지우기 & 강제 갱신
            this.insert("");
            this.origin = this.getTextWithPreview();

            // 미리보기 추가
            this.firstEndPos = first.length();
            this.secondStartPos = (this.firstEndPos + typing.length());
            this.setPreviewText(first + typing + second);
        }
    }

    /**
     * (2) 완성된 글자를 최종 입력값에 넣는다.
     *
     * @param input 완성된 글자
     */
    public final void insertText(final String input) {
        if (this.blockTyping() || !this.editable()) {
            return;
        }

        ModLogger.debug("[Complete] Current: ({}) / Preview: ({})", this.origin, input);
        this.status = InputStatus.NONE;
        this.firstEndPos = -1;
        this.secondStartPos = -1;

        this.setPreviewText(this.origin);
        this.insert(input);
        this.origin = this.getTextWithPreview();
    }

    /**
     * (2-1) 입력 구성 요소에 글자 값을 넣는다.
     *
     * @param text 글자 값
     */
    protected abstract void insert(final String text);

    // ================================

    /**
     * 커서 위치를 가져온다.
     *
     * @return 커서 위치
     */
    protected abstract int getCursorPos();

    /**
     * 강조 커서 위치를 가져온다.
     *
     * @return 강조 커서 위치
     */
    protected abstract int getHighlightPos();

    /**
     * 편집할 수 있는지 가져온다.
     *
     * @return 편집 가능 여부
     */
    public boolean editable() {
        return true;
    }

    /**
     * 입력을 막을지 가져온다.
     *
     * @return 입력 차단 여부
     */
    public abstract boolean blockTyping();

    /**
     * 미리보기를 포함한 현재 입력값을 가져온다.
     *
     * @return 현재 입력값
     */
    protected abstract String getTextWithPreview();

    /**
     * 미리보기를 포함한 현재 입력값을 설정한다.
     *
     * @param text 현재 입력값
     */
    protected abstract void setPreviewText(final String text);

    /**
     * 사각형 구조를 가져온다.
     *
     * @return 사각형 구조
     */
    public abstract Rect getRect();
}
