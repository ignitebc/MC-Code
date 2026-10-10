package moe.caramel.chat.driver.arch.win;

import static moe.caramel.chat.driver.arch.win.Driver_Win.LAYOUT_MAP;
import com.sun.jna.Native;
import moe.caramel.chat.Main;
import moe.caramel.chat.controller.ScreenController;
import moe.caramel.chat.driver.IController;
import moe.caramel.chat.driver.IOperator;
import moe.caramel.chat.driver.KeyboardStatus;
import moe.caramel.chat.driver.KeyboardStatus.Language;
import moe.caramel.chat.util.ModLogger;
import moe.caramel.chat.wrapper.AbstractIMEWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.lwjgl.glfw.GLFWNativeWin32;

/**
 * Windows 컨트롤러
 */
public final class WinController implements IController {

    static WinOperator focused;

    private final Driver_Win driver;

    /**
     * Windows 컨트롤러를 만든다
     */
    public WinController() {
        ModLogger.log("[Native] Load the Windows Controller.");
        this.driver = Native.load(Main.copyLibrary("libwincocoainput.dll"), Driver_Win.class);
        this.driver.initialize(
            // 창 Id
            GLFWNativeWin32.glfwGetWin32Window(Minecraft.getInstance().getWindow().handle()),
            // 조합 중 글자 콜백
            (str, cursor, length) -> {
                if (focused != null) {
                    ModLogger.debug("[Native|Java] Preedit Callback (" + str.toString() + ") (" + cursor + ") (" + length + ")");
                    focused.getWrapper().appendPreviewText(str.toString());
                }
            },
            // 완료 콜백
            (str) -> {
                if (focused != null) {
                    ModLogger.debug("[Native|Java] Done Callback (" + str.toString() + ")");
                    focused.getWrapper().insertText(str.toString());
                }
            },
            // 사각형 콜백
            (rect) -> {
                if (focused != null) {
                    ModLogger.debug("[Native|Java] Rect Callback");
                    final float[] buff = focused.getWrapper().getRect().copy();
                    final float factor = (float) Minecraft.getInstance().getWindow().getGuiScale();
                    buff[0] *= factor;
                    buff[1] *= factor;
                    buff[2] *= factor;
                    buff[3] *= factor;

                    rect.write(0, buff, 0, 4);
                    return 0;
                }
                return 1;
            },
            // 정보
            (log) -> ModLogger.log("[Native|C] " + log),
            // 오류
            (log) -> ModLogger.error("[Native|C] " + log),
            // 디버그
            (log) -> ModLogger.debug("[Native|C] " + log)
        );
    }

    @Override
    public IOperator createOperator(final AbstractIMEWrapper wrapper) {
        return new WinOperator(this, wrapper);
    }

    @Override
    public void changeFocusedScreen(final Screen screen) {
        if (screen instanceof ScreenController) {
            return;
        }

        if (WinController.focused != null) {
            WinController.focused.setFocused(false);
            WinController.focused = null;
        }
    }

    @Override
    public void setFocus(final boolean focus) {
        this.driver.set_focus(focus ? 1 : 0);
    }

    @Override
    public KeyboardStatus getKeyboardStatus() {
        return new KeyboardStatus(
            LAYOUT_MAP.getOrDefault(driver.getKeyboardLayout(), Language.OTHER),
            (driver.getStatus() != 0)
        );
    }
}
