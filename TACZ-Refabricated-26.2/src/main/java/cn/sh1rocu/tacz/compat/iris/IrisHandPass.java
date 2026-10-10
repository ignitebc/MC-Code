package cn.sh1rocu.tacz.compat.iris;

import com.tacz.guns.GunMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Iris 셰이더팩의 1인칭 손 렌더링 패스를 감지한다.
 *
 * <p>셰이더팩이 켜져 있으면 Iris는 {@code GameRenderer#renderItemInHand} 안의 손 그리기를 막고,
 * 월드 렌더링 도중 자체 {@code HandRenderer}로 손을 따로 그린다. 그 패스에서도 bobHurt/bobView가
 * 호출되고 스코프 몸체가 그려지므로, 손 패스에서만 동작하는 스코프 마스크와 손 흔들림 처리가
 * 이 패스를 같은 손 패스로 알아봐야 한다.</p>
 */
@Environment(EnvType.CLIENT)
public final class IrisHandPass {
    private static final String IRIS_MOD_ID = "iris";
    private static final String HAND_RENDERER_CLASS = "net.irisshaders.iris.pathways.HandRenderer";

    private static boolean initialized = false;
    @Nullable
    private static Object handRenderer;
    @Nullable
    private static Method isActiveMethod;

    private IrisHandPass() {
    }

    /**
     * @return Iris가 지금 1인칭 손을 그리는 중이면 true. Iris가 없거나 내부 구조가 바뀌었으면 항상 false
     */
    public static boolean isActive() {
        if (!initialized) {
            initialize();
        }
        if (isActiveMethod == null) {
            return false;
        }
        try {
            return (boolean) isActiveMethod.invoke(handRenderer);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            disable(exception);
            return false;
        }
    }

    private static void initialize() {
        initialized = true;
        if (!FabricLoader.getInstance().isModLoaded(IRIS_MOD_ID)) {
            return;
        }
        try {
            // 선택적 연동이므로 Iris를 빌드·실행 필수 의존성으로 추가하지 않는다.
            Class<?> rendererClass = Class.forName(HAND_RENDERER_CLASS);
            handRenderer = rendererClass.getField("INSTANCE").get(null);
            isActiveMethod = rendererClass.getMethod("isActive");
            GunMod.LOGGER.info("[TACZ Scope] Iris hand pass detection enabled.");
        } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            // Iris 내부 구조가 바뀌면 원인을 기록하고 바닐라 손 패스 기준으로만 동작한다.
            disable(exception);
        }
    }

    private static void disable(Throwable cause) {
        handRenderer = null;
        isActiveMethod = null;
        GunMod.LOGGER.warn("[TACZ Scope] Could not detect the Iris hand pass; scope mask uses the vanilla hand pass only.", cause);
    }
}
