package cn.sh1rocu.tacz.api.extension;


import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import cn.sh1rocu.tacz.compat.fabric.BuiltinItemRendererRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface IItem {
    // [39차] tacz$getMaxStackSize(ItemStack)를 제거했다.
    //
    // 1.21.1 시절 "탄약마다 다른 최대 묶음 수"를 위해 만든 확장 지점으로,
    // compat/tweakeroo/ItemMixin이 Item#getMaxStackSize(ItemStack)에 주입해 동작했다.
    // 하지만 26.2에서는 이 경로가 완전히 끊겼다:
    //   1. Item에 getMaxStackSize(ItemStack) 오버로드가 [없다](바이트코드 확인,
    //      인자 없는 getDefaultMaxStackSize()만 남음) — 그 mixin을 등록하면
    //      대상을 찾지 못해 크래시가 나며, 오랫동안 등록하지 않은 실제 이유이기도 하다.
    //   2. 실제 상한은 이제 DataComponents.MAX_STACK_SIZE 컴포넌트가 정한다
    //      (ItemInstance#getMaxStackSize 구현이 곧
    //       getOrDefault(MAX_STACK_SIZE, 1)이다). 34차에서
    //      AmmoItemDataAccessor#applyMaxStackSize와 AmmoItem#inventoryTick이
    //      이 컴포넌트를 쓰도록 바꿨으므로 mixin이 더는 필요 없다.
    //
    // ItemMixin을 지우면서 이 메서드는 유일한 호출처를 잃었고, 남겨 두면 아직 쓰이는 것처럼 오해하게 만든다.

    default boolean tacz$onEntitySwing(ItemStack stack, LivingEntity entity) {
        return false;
    }

    @Environment(EnvType.CLIENT)
    BuiltinItemRendererRegistry.DynamicItemRenderer getCustomRenderer();
}
