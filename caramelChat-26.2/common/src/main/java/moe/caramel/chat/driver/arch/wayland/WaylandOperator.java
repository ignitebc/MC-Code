package moe.caramel.chat.driver.arch.wayland;

import moe.caramel.chat.driver.IController;
import moe.caramel.chat.driver.IOperator;
import moe.caramel.chat.util.ModLogger;
import moe.caramel.chat.wrapper.AbstractIMEWrapper;

/**
 * Wayland IME 오퍼레이터
 */
public final class WaylandOperator implements IOperator {

    private final WaylandController controller;
    private final AbstractIMEWrapper wrapper;
    private boolean nowFocused;

    public WaylandOperator(final WaylandController controller, final AbstractIMEWrapper wrapper) {
        this.controller = controller;
        this.wrapper = wrapper;
    }

    /**
     * IME 래퍼를 가져온다.
     *
     * @return IME 래퍼
     */
    public AbstractIMEWrapper getWrapper() {
        return wrapper;
    }

    @Override
    public IController getController() {
        return controller;
    }

    @Override
    public void setFocused(final boolean focus) {
        if (focus == this.nowFocused) {
            return;
        }

        ModLogger.debug("[Native|Java] Called setFocused: " + focus);
        this.nowFocused = focus;

        if (focus) {
            WaylandController.focused = this;
            this.controller.setFocus(true);
        } else if (WaylandController.focused == this) {
            this.wrapper.insertText("");
            WaylandController.focused = null;
            this.controller.setFocus(false);
        }
    }

    @Override
    public boolean isFocused() {
        return nowFocused;
    }
}
