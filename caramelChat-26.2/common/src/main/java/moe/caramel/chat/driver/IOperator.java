package moe.caramel.chat.driver;

/**
 * IME 오퍼레이터 인터페이스
 */
public interface IOperator {

    /**
     * IME 컨트롤러를 가져온다.
     *
     * @return 컨트롤러
     */
    IController getController();

    /**
     * 포커스 여부를 설정한다. (래퍼)
     *
     * @param focus 포커스
     */
    void setFocused(final boolean focus);

    /**
     * 포커스 여부를 가져온다. (래퍼)
     *
     * @return 포커스
     */
    boolean isFocused();
}
