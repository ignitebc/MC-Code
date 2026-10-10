package moe.caramel.chat.util;

import moe.caramel.chat.Main;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 로그 도구
 */
public final class ModLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger("caramelChat");

    /**
     * 로그 메시지를 출력한다.
     *
     * @param msg 로그 형식
     * @param data 기타 데이터
     */
    public static void log(final String msg, final Object... data) {
        LOGGER.info(msg, data);
    }

    /**
     * 오류 메시지를 출력한다.
     *
     * @param msg 로그 형식
     * @param data 기타 데이터
     */
    public static void error(final String msg, final Object... data) {
        LOGGER.error(msg, data);
    }

    /**
     * 디버그 메시지를 출력한다.
     *
     * @param msg 로그 형식
     * @param args 기타 데이터
     */
    public static void debug(final String msg, final Object... args) {
        if (Main.DEBUG) {
            LOGGER.warn(msg, args);
        }
    }
}
