package cn.sh1rocu.tacz.util.forge;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.text.DecimalFormat;

/**
 * 정해진 범위 안의 값을 선택적인 간격으로 입력받는 슬라이더 위젯.
 */
public class ForgeSlider extends AbstractSliderButton {
    protected Component prefix;
    protected Component suffix;

    protected double minValue;
    protected double maxValue;

    /**
     * 정해진 간격의 불연속 값을 입력받는다
     */
    protected double stepSize;

    protected boolean drawString;

    private final DecimalFormat format;

    /**
     * @param x            왼쪽 위 모서리의 x 위치
     * @param y            왼쪽 위 모서리의 y 위치
     * @param width        위젯 너비
     * @param height       위젯 높이
     * @param prefix       값 글자 앞에 표시할 {@link Component}
     * @param suffix       값 글자 뒤에 표시할 {@link Component}
     * @param minValue     슬라이더 최솟값(왼쪽)
     * @param maxValue     슬라이더 최댓값(오른쪽)
     * @param currentValue 위젯을 처음 표시할 때의 값
     * @param stepSize     간격 크기. 0이 아니면 이 값으로 정밀도를 자동 계산한다.
     * @param precision    {@code stepSize}가 0일 때만 쓴다. 최대 4(포함)까지.
     * @param drawString   위젯에 글자를 표시할지
     */
    public ForgeSlider(int x, int y, int width, int height, Component prefix, Component suffix, double minValue, double maxValue, double currentValue, double stepSize, int precision, boolean drawString) {
        super(x, y, width, height, Component.empty(), 0D);
        this.prefix = prefix;
        this.suffix = suffix;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.stepSize = Math.abs(stepSize);
        this.value = this.snapToNearest((currentValue - minValue) / (maxValue - minValue));
        this.drawString = drawString;

        if (stepSize == 0D) {
            precision = Math.min(precision, 4);

            StringBuilder builder = new StringBuilder("0");

            if (precision > 0)
                builder.append('.');

            while (precision-- > 0)
                builder.append('0');

            this.format = new DecimalFormat(builder.toString());
        } else if (Mth.equal(this.stepSize, Math.floor(this.stepSize))) {
            this.format = new DecimalFormat("0");
        } else {
            this.format = new DecimalFormat(Double.toString(this.stepSize).replaceAll("\\d", "0"));
        }

        this.updateMessage();
    }

    /**
     * {@code stepSize}를 1로 둔 오버로드. 정수 값 슬라이더에 쓴다.
     */
    public ForgeSlider(int x, int y, int width, int height, Component prefix, Component suffix, double minValue, double maxValue, double currentValue, boolean drawString) {
        this(x, y, width, height, prefix, suffix, minValue, maxValue, currentValue, 1D, 0, drawString);
    }

    /**
     * @return 현재 슬라이더 값(double)
     */
    public double getValue() {
        return this.value * (maxValue - minValue) + minValue;
    }

    /**
     * @return 현재 슬라이더 값(long)
     */
    public long getValueLong() {
        return Math.round(this.getValue());
    }

    /**
     * @return 현재 슬라이더 값(int)
     */
    public int getValueInt() {
        return (int) this.getValueLong();
    }

    /**
     * <b>실제 값</b>({@code minValue}~{@code maxValue} 구간)으로 슬라이더를 설정한다.
     *
     * <h2>setValue가 아니라 setValueReal인 이유</h2>
     * 26.2의 {@code AbstractSliderButton}에는 <b>이름과 시그니처가 같은</b> 메서드
     * {@code setValue(double)}가 있지만 의미가 완전히 다르다 — 그쪽은
     * <b>0~1 비율</b>을 받고, 안에서 중요한 일을 두 가지 한다(바이트코드 확인):
     * <pre>
     * this.value = Mth.clamp(value, 0.0, 1.0);
     * if (d != this.value) { this.applyValue(); }   // 값이 바뀌었을 때만 콜백
     * this.updateMessage();
     * </pre>
     *
     * <p>이 클래스는 원래 이 메서드를 {@code setValue}라고 불러 부모 메서드를 <b>의도치 않게 재정의</b>했고,
     * 재정의한 쪽은 입력을 "실제 값"으로 해석하면서 <b>{@code applyValue()}를 한 번도 호출하지 않았다</b>.
     * 그 결과가 사용자가 실제로 겪은 이상한 현상이다:
     * <ul>
     *   <li><b>슬라이더를 끌면 적용되지 않음</b> — 드래그는 바닐라
     *       {@code AbstractSliderButton#onDrag → setValueFromMouse(event) → setValue(double)}를 거치는데,
     *       여기서 다형 호출이 이 클래스의 재정의로 들어가 값만 바꾸고
     *       {@code applyValue()}를 부르지 않아 레이저 색이 갱신되지 않았다.</li>
     *   <li><b>슬라이더의 다른 위치를 클릭하면 적용됨</b> — 클릭은 이 클래스 자체의
     *       {@code onClick → setValueFromMouse(double) → setSliderValue}를 거치고,
     *       그 경로는 {@code applyValue()}를 명시적으로 불러 색도 바뀌고 저장도 됐다.</li>
     * </ul>
     * "클릭은 되는데 드래그는 안 되는" 조합이 바로 이 호출 충돌의 지문이었다.
     *
     * <p>이름을 바꾼 뒤로는 부모 메서드를 재정의하지 않으므로 바닐라 {@code setValue(double)}가 원래 의미
     * ({@code applyValue()} 콜백 포함)를 되찾고, 드래그 경로도 자연히 동작한다.
     * 이 클래스의 실제 값 의미는 이 메서드가 맡아 키보드 좌우 키 조정에 쓴다.
     *
     * @param value 새 슬라이더 값(비율이 아닌 실제 값)
     */
    public void setValueReal(double value) {
        this.value = this.snapToNearest((value - this.minValue) / (this.maxValue - this.minValue));
        this.updateMessage();
    }

    public String getValueString() {
        return this.format.format(this.getValue());
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean b) {
        this.setValueFromMouse(event.x());
    }

    /**
     * 드래그할 때 슬라이더 값을 갱신한다.
     *
     * <p><b>일부러 {@code super.onDrag}를 호출하지 않는다</b>: 바닐라
     * {@code AbstractSliderButton#onDrag}는 안에서
     * {@code setValueFromMouse(event) → setValue(double)}를 부르는데,
     * 그 경로는 {@code Mth.clamp(0,1)}만 하고 <b>{@link #snapToNearest} 간격 맞춤을 하지 않는다</b>.
     * super를 먼저 부르고 이 클래스의 {@code setValueFromMouse}를 다시 부르면, 같은 드래그에서
     * "맞춤 없이" 한 번, "맞춤 있게" 한 번 값을 쓰게 된다 — 두 번 모두
     * {@code applyValue()}를 부를 수 있어 쓸데없는 중복 콜백이 생기고 간격 동작도 불확실해진다.
     *
     * <p>그래서 이 클래스의 {@code setValueFromMouse(double)}로 바로 가며,
     * 그 경로는 결국 {@link #setSliderValue}에 이르러 먼저 {@code snapToNearest}를 하고,
     * <b>값이 실제로 바뀌었을 때만</b> {@code applyValue()}를 부른다 —
     * 바닐라 {@code setValue}의 "바뀌었을 때만 콜백"과 같은 의미를 지키면서
     * 이 클래스의 간격 기능도 유지한다.
     *
     * <p>부모 {@code onDrag}에는 그 밖의 부작용이 없다(바이트코드 확인:
     * {@code setValueFromMouse}와 빈 {@code WithInactiveMessage#onDrag}뿐이다). 따라서 건너뛰어도 안전하다.
     */
    @Override
    protected void onDrag(MouseButtonEvent event, double dragX, double dragY) {
        this.setValueFromMouse(event.x());
    }

    public boolean handleKeyEvent(KeyEvent event) {
        boolean flag = event.key() == GLFW.GLFW_KEY_LEFT;
        if (flag || event.key() == GLFW.GLFW_KEY_RIGHT) {
            if (this.minValue > this.maxValue)
                flag = !flag;
            float f = flag ? -1F : 1F;
            if (stepSize <= 0D)
                this.setSliderValue(this.value + (f / (this.width - 8)));
            else
                this.setValueReal(this.getValue() + f * this.stepSize);
        }

        return false;
    }

    private void setValueFromMouse(double mouseX) {
        this.setSliderValue((mouseX - (this.getX() + 4)) / (this.width - 8));
    }

    /**
     * @param value 슬라이더 범위에 대한 비율
     */
    private void setSliderValue(double value) {
        double oldValue = this.value;
        this.value = this.snapToNearest(value);
        if (!Mth.equal(oldValue, this.value))
            this.applyValue();

        this.updateMessage();
    }

    /**
     * 표시 값이 {@code stepSize}의 가장 가까운 배수가 되도록 값을 맞춘다.
     * {@code stepSize}가 0이면 맞추지 않는다.
     */
    private double snapToNearest(double value) {
        if (stepSize <= 0D)
            return Mth.clamp(value, 0D, 1D);

        value = Mth.lerp(Mth.clamp(value, 0D, 1D), this.minValue, this.maxValue);

        value = (stepSize * Math.round(value / stepSize));

        if (this.minValue > this.maxValue) {
            value = Mth.clamp(value, this.maxValue, this.minValue);
        } else {
            value = Mth.clamp(value, this.minValue, this.maxValue);
        }

        return Mth.map(value, this.minValue, this.maxValue, 0D, 1D);
    }

    @Override
    protected void updateMessage() {
        if (this.drawString) {
            this.setMessage(Component.literal("").append(prefix).append(this.getValueString()).append(suffix));
        } else {
            this.setMessage(Component.empty());
        }
    }

    @Override
    protected void applyValue() {
    }
}
