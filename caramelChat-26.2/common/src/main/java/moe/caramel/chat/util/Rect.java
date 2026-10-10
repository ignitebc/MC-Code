package moe.caramel.chat.util;

/**
 * 사각형 생성자
 *
 * @param x 사각형의 x 좌표
 * @param y 사각형의 y 좌표
 * @param width 사각형의 너비
 * @param height 사각형의 높이
 */
public record Rect(float x, float y, float width, float height) {

    /**
     * 빈 사각형
     */
    public static final Rect EMPTY = new Rect(0, 0, 0, 0);

    /**
     * float 배열로 복사한다.
     *
     * @return float 배열
     */
    public float[] copy() {
        return new float[] { x, y, width, height };
    }
}
