package moe.caramel.chat.driver;

import moe.caramel.chat.driver.arch.darwin.DarwinController;
import moe.caramel.chat.driver.arch.unknown.UnknownController;
import moe.caramel.chat.driver.arch.wayland.WaylandController;
import moe.caramel.chat.driver.arch.win.WinController;
import moe.caramel.chat.driver.arch.x11.X11Controller;
import moe.caramel.chat.util.ModLogger;
import moe.caramel.chat.wrapper.AbstractIMEWrapper;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

/**
 * 컨트롤러 인터페이스
 */
public interface IController {

    /**
     * IME 오퍼레이터를 만든다.
     *
     * @param wrapper IME 래퍼
     * @return IME 오퍼레이터
     */
    IOperator createOperator(final AbstractIMEWrapper wrapper);

    /**
     * 현재 포커스된 화면으로 바꾼다.
     *
     * @param screen 포커스된 화면
     */
    void changeFocusedScreen(final Screen screen);

    /**
     * 포커스 여부를 설정한다. (드라이버)
     *
     * @param focus 포커스
     */
    void setFocus(final boolean focus);

    /**
     * 현재 키보드 상태를 가져온다.
     *
     * @return 키보드 상태({@code null}이면 지원하지 않는 OS)
     */
    @Nullable
    default KeyboardStatus getKeyboardStatus() {
        return null;
    }

    /**
     * 컨트롤러를 가져온다.
     *
     * @return 컨트롤러
     */
    static IController getController() {
        try {
            return switch (GLFW.glfwGetPlatform()) {
                // Windows
                case GLFW.GLFW_PLATFORM_WIN32 -> new WinController();
                // macOS
                case GLFW.GLFW_PLATFORM_COCOA -> new DarwinController();
                // Linux (X11)
                case GLFW.GLFW_PLATFORM_X11 -> new X11Controller();
                // Linux (Wayland)
                case GLFW.GLFW_PLATFORM_WAYLAND -> new WaylandController();
                // 알 수 없는 OS
                default -> throw new UnsupportedOperationException();
            };
        } catch (final UnsupportedOperationException ignored) {
            ModLogger.error("This platform is not supported by CocoaInput Driver.");
        } catch (final Exception exception) {
            ModLogger.error("Error while loading the CocoaInput Driver.", exception);
        }
        return UnknownController.INSTANCE;
    }
}
