package moe.caramel.chat.plugin;

import org.jetbrains.annotations.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 호환성 데이터 등록부
 */
public final class Compatibilities {

    private static final Map<String, Data> REGISTRY = new HashMap<>();

    public static final Data EMI = register(
        "EMI", "dev.emi.emi.screen.widget.EmiSearchWidget",
        Set.of(
            "moe.caramel.chat.mixin.emi.MixinPluginEmiPort",
            "moe.caramel.chat.mixin.emi.MixinPluginEmiSearchWidget"
        )
    );

    public static final Data XAERO_MINIMAP = register(
        "Xaero's Minimap", "xaero.common.gui.GuiAddWaypoint",
        Set.of("moe.caramel.chat.mixin.xaeromap.MixinPluginXaeroMapWayPoint")
    );

    // ================================

    /**
     * 호환성 데이터를 가져온다.
     *
     * @param mixinClass Mixin 클래스 이름
     * @return 호환성 데이터
     */
    @Nullable
    public static Data getData(final String mixinClass) {
        return Compatibilities.REGISTRY.get(mixinClass);
    }

    /**
     * 호환성 데이터.
     *
     * @param name 모드 이름
     * @param targetClassName 감지할 대상 클래스 이름
     * @param classes Mixin 클래스
     */
    public record Data(String name, String targetClassName, Set<String> classes) {}

    /**
     * 호환성 데이터를 등록한다.
     *
     * @param name 모드 이름
     * @param targetClassName 감지할 대상 클래스 이름
     * @param classes Mixin 클래스
     */
    private static Data register(final String name, final String targetClassName, final Set<String> classes) {
        final Data data = new Data(name, targetClassName, classes);
        for (final String clazz : classes) {
            Compatibilities.REGISTRY.put(clazz, data);
        }
        return data;
    }
}
