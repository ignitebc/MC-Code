package cn.sh1rocu.tacz.compat.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.IdentityHashMap;
import java.util.Map;

/**
 * 26.2에서 제거된 Fabric API의 BuiltinItemRendererRegistry를 대신하는 로컬 구현.
 * 단순한 Item → DynamicItemRenderer 레지스트리를 제공한다.
 */
public class BuiltinItemRendererRegistry {
    public static final BuiltinItemRendererRegistry INSTANCE = new BuiltinItemRendererRegistry();

    private final Map<Item, DynamicItemRenderer> renderers = new IdentityHashMap<>();

    private BuiltinItemRendererRegistry() {
    }

    public void register(Item item, DynamicItemRenderer renderer) {
        renderers.put(item, renderer);
    }

    public DynamicItemRenderer get(Item item) {
        return renderers.get(item);
    }

    /**
     * 26.2용 사용자 정의 아이템 렌더러 인터페이스.
     * 예전 BlockEntityWithoutLevelRenderer + DynamicItemRenderer 방식을 대신한다.
     */
    public interface DynamicItemRenderer {
        void render(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack, SubmitNodeCollector collector, int light, int overlay);
    }
}
