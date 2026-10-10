package moe.caramel.chat.driver.arch.x11;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.WString;

/**
 * CocoaInput X11 드라이버
 */
public interface Driver_X11 extends Library {

    /**
     * CocoaInput X11 드라이버를 초기화한다.
     *
     * @param windowId 창 Id
     * @param xWindowId X11 창 Id
     * @param draw 그리기 콜백
     * @param done 완료 콜백
     * @param log 정보 로그 콜백
     * @param error 오류 로그 콜백
     * @param debug 디버그 로그 콜백
     */
    void initialize(
        final long windowId,
        final long xWindowId,
        final DrawCallback draw,
        final DoneCallback done,
        final LogInfoCallback log,
        final LogErrorCallback error,
        final LogDebugCallback debug
    );

    /**
     * 포커스 여부를 설정한다.
     *
     * @param flag 포커스
     */
    void set_focus(final int flag);

    // ================================

    interface DrawCallback extends Callback {
        Pointer invoke(
            final int caret, final int chg_first, final int chg_length, final short length,
            final boolean iswstring, final String rawstring, final WString rawwstring,
            final int primary, final int secondary, final int tertiary
        );
    }

    interface DoneCallback extends Callback {
        void invoke();
    }

    interface LogInfoCallback extends Callback {
        void invoke(final String log);
    }

    interface LogErrorCallback extends Callback {
        void invoke(final String log);
    }

    interface LogDebugCallback extends Callback {
        void invoke(final String log);
    }
}
