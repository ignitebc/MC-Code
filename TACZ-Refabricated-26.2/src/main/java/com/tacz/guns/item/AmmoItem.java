package com.tacz.guns.item;

import cn.sh1rocu.tacz.api.extension.IItem;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.api.item.nbt.AmmoItemDataAccessor;
import com.tacz.guns.client.renderer.item.AmmoItemRenderer;
import com.tacz.guns.client.resource.index.ClientAmmoIndex;
import com.tacz.guns.resource.index.CommonAmmoIndex;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import cn.sh1rocu.tacz.compat.fabric.BuiltinItemRendererRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class AmmoItem extends Item implements AmmoItemDataAccessor, IItem {
    public AmmoItem(Properties properties) {
        // [34차] 더는 stacksTo(1)을 쓰지 않는다.
        //
        // 지난 차수에 MAX_STACK_SIZE 컴포넌트를 setAmmoId에서 쓰게 했고, 방식 자체는 맞았다
        // (26.2의 ItemInstance#getMaxStackSize는 DataComponents.MAX_STACK_SIZE를 읽는다.
        //  바이트코드 확인: getOrDefault(MAX_STACK_SIZE, 1)). 그래도 적용되지 않았는데, 이유는 두 가지다:
        //
        // 1. <b>아이템 등록 시점에 한도가 1로 고정되었다.</b> {@code Properties#stacksTo(n)}의 구현은
        //    바로 {@code component(MAX_STACK_SIZE, n)}이며(바이트코드 확인),
        //    아이템의 <b>기본 컴포넌트 집합(prototype)</b>에 쓴다. 그런데 {@code applyMaxStackSize}는
        //    {@code TimelessAPI.getCommonAmmoIndex(...)}로 총기 팩 데이터를 조회한다 —
        //    이 데이터는 자원 다시 불러오기에서 오므로 <b>클라이언트가 막 서버에 들어왔거나 아직 동기화되지 않았을 때</b> 비어 있을 수 있고,
        //    그때는 {@code ifPresent}가 실행되지 않아 stack이 prototype의 1을 그대로 유지한다.
        //    <b>한 번이라도</b> 빗나가면 그 탄환 묶음은 영원히 1에 머문다.
        //
        // 2. <b>patch가 동일성 판단에 참여한다</b>({@code PatchedDataComponentMap#equals}는
        //    prototype과 patch를 함께 비교한다. 바이트코드 확인). prototype=1일 때 컴포넌트를 쓴 탄환은
        //    {@code patch{MAX_STACK_SIZE=36}}을 갖고 쓰지 않은 탄환은 patch가 비어 있어,
        //    {@code isSameItemSameComponents}가 바로 false가 된다 —
        //    <b>똑같아 보이는</b> 탄환 두 묶음이 영원히 합쳐지지 않는다.
        //
        // 3. 저장 파일에 <b>이미 있는</b> 탄환은 setAmmoId를 다시 거치지 않으므로(아이템을 만들 때만 호출),
        //    예전 아이템은 영원히 1에 머문다. 아래 inventoryTick의 자가 복구와 함께 써야 한다.
        //
        // 그래서 아이템 단위 기본 한도를 99로 올린다: 총기 팩 데이터를 잠시 찾지 못해도 정상으로 쌓이고,
        // 탄종별 정확한 한도는 setAmmoId / inventoryTick이 따로 쓴다.
        super(properties.stacksTo(MAX_AMMO_STACK_SIZE));
    }

    /**
     * 아이템 단위 기본 겹치기 한도.
     *
     * <p>99로 둔다 — 바닐라 {@code Item.ABSOLUTE_MAX_STACK_SIZE}와 같다. 이것은 <b>안전망</b>일 뿐이며,
     * 탄종별 실제 한도는 {@link AmmoItemDataAccessor#applyMaxStackSize}가
     * {@code CommonAmmoIndex#getStackSize}에 따라 하나씩 쓴다. 1로 두지 않는 이유는
     * "컴포넌트가 없는 예전 아이템"도 1에 묶이지 않고 우선 쌓일 수 있게 하려는 것이다.</p>
     */
    public static final int MAX_AMMO_STACK_SIZE = 99;

    /**
     * [34차] 예전 저장 파일 속 탄환의 자가 복구.
     *
     * <p>{@code setAmmoId}는 탄약을 <b>만들 때만</b> 호출되므로, 이미 플레이어 인벤토리/상자에 있는 예전 탄환은
     * 그것을 다시 거치지 않아 올바른 {@code MAX_STACK_SIZE} 컴포넌트가 영원히 없다.
     * 여기서 아이템 tick 때 한 번 채워 쓴다. 비용은 아주 낮다(컴포넌트가 같으면 {@code set}이 아무것도 바꾸지 않음).</p>
     *
     * <p>26.2의 시그니처는 {@code (ItemStack, ServerLevel, Entity, EquipmentSlot)}이며
     * 바이트코드로 확인했다. 이 메서드는 <b>서버에서만</b> 호출되고({@code ItemStack#inventoryTick}에
     * {@code instanceof ServerLevel} 관문이 있음), 컴포넌트 변경은 아이템과 함께 클라이언트로 동기화된다.</p>
     */
    @Override
    public void inventoryTick(@Nonnull ItemStack stack, @Nonnull ServerLevel level,
                              @Nonnull Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        AmmoItemDataAccessor.applyMaxStackSize(stack);
    }

    // [39차] tacz$getMaxStackSize 재정의를 제거했다.
    // 이 확장 지점은 compat/tweakeroo/ItemMixin이 Item#getMaxStackSize(ItemStack)에 주입하는 데 기댔는데,
    // 26.2의 Item에는 그 오버로드가 아예 없어(인자 없는 getDefaultMaxStackSize만 있음)
    // mixin 등록 즉시 충돌해 한 번도 켜지지 않았다. 겹치기 한도는 34차부터
    // DataComponents.MAX_STACK_SIZE 컴포넌트가 맡고(생성자와 inventoryTick 참고),
    // 이 메서드를 호출하는 곳은 더 없다.

    @Override
    @Nonnull
    @Environment(EnvType.CLIENT)
    public Component getName(@Nonnull ItemStack stack) {
        Identifier ammoId = this.getAmmoId(stack);
        Optional<ClientAmmoIndex> ammoIndex = TimelessAPI.getClientAmmoIndex(ammoId);
        if (ammoIndex.isPresent()) {
            return Component.translatable(ammoIndex.get().getName());
        }
        return super.getName(stack);
    }

    private static Comparator<Map.Entry<Identifier, CommonAmmoIndex>> idNameSort() {
        return Comparator.comparingInt(m -> m.getValue().getSort());
    }

    public static NonNullList<ItemStack> fillItemCategory() {
        NonNullList<ItemStack> stacks = NonNullList.create();
        TimelessAPI.getAllCommonAmmoIndex().stream().sorted(idNameSort()).forEach(entry -> {
            ItemStack itemStack = AmmoItemBuilder.create().setId(entry.getKey()).build();
            stacks.add(itemStack);
        });
        return stacks;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public BuiltinItemRendererRegistry.DynamicItemRenderer getCustomRenderer() {
        return AmmoItemRenderer.INSTANCE.get();
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> adder, TooltipFlag isAdvanced) {
        Identifier ammoId = this.getAmmoId(stack);
        TimelessAPI.getClientAmmoIndex(ammoId).ifPresent(index -> {
            String tooltipKey = index.getTooltipKey();
            if (tooltipKey != null) {
                adder.accept(Component.translatable(tooltipKey).withStyle(style -> style.withColor(0xAAAAAA)));
            }
        });
    }
}
