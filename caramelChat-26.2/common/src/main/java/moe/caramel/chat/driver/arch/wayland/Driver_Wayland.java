package moe.caramel.chat.driver.arch.wayland;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.WString;

/**
 * caramelChat Wayland 드라이버
 */
public interface Driver_Wayland extends Library {

    /**
     * caramelChat Wayland 드라이버를 초기화한다.
     *
     * @param wlDisplay Wayland 디스플레이
     * @param preEdit 조합 중 글자 콜백
     * @param preEditNull 조합 중 글자 비움 콜백
     * @param done 완료 콜백
     * @param rect 사각형 콜백
     * @param log 정보 로그 콜백
     * @param error 오류 로그 콜백
     * @param debug 디버그 로그 콜백
     */
    void initialize(
        final long wlDisplay,
        final PreeditCallback preEdit,
        final PreeditNullCallback preEditNull,
        final DoneCallback done,
        final RectCallback rect,
        final LogInfoCallback log,
        final LogErrorCallback error,
        final LogDebugCallback debug
    );

    /**
     * 포커스 여부를 설정한다.
     *
     * @param flag 포커스
     */
    void setFocus(final boolean flag);

    // ================================

    interface PreeditCallback extends Callback {
        void invoke(final WString string);
    }

    interface PreeditNullCallback extends Callback {
        void invoke();
    }

    interface DoneCallback extends Callback {
        void invoke(final WString string);
    }

    interface RectCallback extends Callback {
        int invoke(final Pointer pointer);
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
