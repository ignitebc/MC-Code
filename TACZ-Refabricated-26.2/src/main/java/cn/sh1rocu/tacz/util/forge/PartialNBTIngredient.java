package cn.sh1rocu.tacz.util.forge;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 26.2: DataComponents 체계로 옮겼다.
 * 예전 NBT 태그 비교는 DataComponents.CUSTOM_DATA + CustomData.matchedBy()로 바꿨다.
 */
public class PartialNBTIngredient implements CustomIngredient {
    private final Set<Item> items;
    private final CompoundTag nbt;

    protected PartialNBTIngredient(Set<Item> items, CompoundTag nbt) {
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Cannot create a PartialNBTIngredient with no items");
        }
        this.items = Collections.unmodifiableSet(items);
        this.nbt = nbt;
    }

    /**
     * 목록의 아이템 중 하나이면서 주어진 NBT를 가진 아이템과 일치하는 재료를 만든다
     */
    public static PartialNBTIngredient of(CompoundTag nbt, ItemLike... items) {
        return new PartialNBTIngredient(Arrays.stream(items).map(ItemLike::asItem).collect(Collectors.toSet()), nbt);
    }

    /**
     * 주어진 아이템이면서 주어진 NBT를 가진 아이템과 일치하는 재료를 만든다
     */
    public static PartialNBTIngredient of(ItemLike item, CompoundTag nbt) {
        return new PartialNBTIngredient(Set.of(item.asItem()), nbt);
    }

    @Override
    public boolean test(@Nullable ItemStack input) {
        if (input == null)
            return false;
        if (!items.contains(input.getItem()))
            return false;
        // 26.2: CUSTOM_DATA 컴포넌트로 NBT를 비교한다
        CustomData customData = input.get(DataComponents.CUSTOM_DATA);
        return customData != null && customData.matchedBy(nbt);
    }

    @Override
    public Stream<Holder<Item>> items() {
        return items.stream().map(BuiltInRegistries.ITEM::wrapAsHolder);
    }

    /**
     * 재료 칸에 맨 기본 아이템이 아니라 <b>요구 NBT가 붙은</b> 아이템을 보여 준다.
     *
     * <p>재정의하지 않으면 부모 인터페이스의 기본 구현은 {@link #items()}의 맨 아이템만 그린다 —
     * TACZ라면 "빈 총 ID"를 가진 {@code tacz:modern_kinetic_gun}이 되어
     * 아이콘은 기본 모델이고 이름도 틀려, 플레이어는 무엇을 내야 하는지 알 수 없다.
     *
     * <p>여기서는 {@code nbt}를 {@code CUSTOM_DATA}에 넣어 표시층에 넘기므로,
     * 재료 칸에 "콜트 M1892" 자체가 제대로 그려진다.
     * 이것은 <b>표시</b>에만 영향을 주고, 일치 판정은 계속 {@link #test}가 맡아 서로 간섭하지 않는다.
     */
    @Override
    public SlotDisplay display() {
        return new SlotDisplay.Composite(items.stream()
                .map(item -> {
                    ItemStack stack = new ItemStack(item);
                    stack.set(DataComponents.CUSTOM_DATA, CustomData.of(nbt.copy()));
                    return (SlotDisplay) new SlotDisplay.ItemStackSlotDisplay(ItemStackTemplate.fromNonEmptyStack(stack));
                })
                .toList());
    }

    @Override
    public boolean requiresTesting() {
        return true;
    }

    @Override
    public CustomIngredientSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    public static final Identifier ID = Identifier.fromNamespaceAndPath("forge", "partial_nbt");

    public static class Serializer implements CustomIngredientSerializer<PartialNBTIngredient> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public Identifier getIdentifier() {
            return ID;
        }

        @Override
        public MapCodec<PartialNBTIngredient> getCodec() {
            return RecordCodecBuilder.mapCodec(codec -> codec.group(
                    BuiltInRegistries.ITEM.holderByNameCodec().listOf().fieldOf("items").forGetter(ing ->
                            ing.items.stream().map(BuiltInRegistries.ITEM::wrapAsHolder).toList()),
                    CustomData.COMPOUND_TAG_CODEC.fieldOf("nbt").forGetter(ing -> ing.nbt)
            ).apply(codec, (holders, tag) -> new PartialNBTIngredient(
                    holders.stream().map(Holder::value).collect(Collectors.toSet()), tag)));
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, PartialNBTIngredient> getStreamCodec() {
            return StreamCodec.composite(
                    ByteBufCodecs.holderRegistry(Registries.ITEM).apply(ByteBufCodecs.list()),
                    ing -> ing.items.stream().map(BuiltInRegistries.ITEM::wrapAsHolder).toList(),
                    ByteBufCodecs.TRUSTED_COMPOUND_TAG,
                    ing -> ing.nbt,
                    (holders, tag) -> new PartialNBTIngredient(
                            holders.stream().map(Holder::value).collect(Collectors.toSet()), tag)
            );
        }
    }
}
