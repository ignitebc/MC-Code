package moe.caramel.chat.driver.arch.win;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import moe.caramel.chat.driver.KeyboardStatus.Language;
import java.util.Map;

/**
 * CocoaInput Windows 드라이버
 */
public interface Driver_Win extends Library {

    int LAYOUT_CHINESE_TRADITIONAL = 0x0404;
    int LAYOUT_JAPANESE = 0x0411;
    int LAYOUT_KOREAN = 0x0412;
    int LAYOUT_CHINESE_SIMPLIFIED = 0x0804;

    Map<Integer, Language> LAYOUT_MAP = Map.of(
        LAYOUT_KOREAN, Language.KOREAN,
        LAYOUT_JAPANESE, Language.JAPANESE,
        LAYOUT_CHINESE_SIMPLIFIED, Language.CHINESE_SIMPLIFIED,
        LAYOUT_CHINESE_TRADITIONAL, Language.CHINESE_TRADITIONAL
    );

    // ================================

    /**
     * CocoaInput Windows 드라이버를 초기화한다.
     *
     * @param windowId 창 Id
     * @param preEdit 조합 중 글자 콜백
     * @param done 완료 콜백
     * @param rect 사각형 콜백
     * @param log 정보 로그 콜백
     * @param error 오류 로그 콜백
     * @param debug 디버그 로그 콜백
     */
    void initialize(
        final long windowId,
        final PreeditCallback preEdit,
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
    void set_focus(final int flag);

    /**
     * 현재 키보드 배열을 가져온다.
     *
     * @return 현재 키보드 배열
     */
    int getKeyboardLayout();

    /**
     * 현재 IME 상태를 가져온다.
     *
     * @return IME 상태(1이면 모국어 입력)
     */
    int getStatus();

    // ================================

    interface PreeditCallback extends Callback {
        void invoke(final WString string, final int cursor, final int length);
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
