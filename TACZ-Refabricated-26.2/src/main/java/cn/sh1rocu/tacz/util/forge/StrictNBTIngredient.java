package cn.sh1rocu.tacz.util.forge;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

/**
 * 26.2: DataComponents 체계로 옮겼다.
 * 예전 NBT 태그 비교는 ItemStack.isSameItemSameComponents()로 바꿨다.
 */
public class StrictNBTIngredient implements CustomIngredient {
    private final ItemStack stack;

    protected StrictNBTIngredient(ItemStack stack) {
        this.stack = stack;
    }

    /**
     * 주어진 스택·컴포넌트와 일치하는 재료를 만든다
     */
    public static StrictNBTIngredient of(ItemStack stack) {
        return new StrictNBTIngredient(stack);
    }

    @Override
    public boolean test(@Nullable ItemStack input) {
        if (input == null)
            return false;
        return ItemStack.isSameItemSameComponents(this.stack, input);
    }

    @Override
    public Stream<Holder<Item>> items() {
        return Stream.of(stack.typeHolder());
    }

    @Override
    public boolean requiresTesting() {
        return true;
    }

    @Override
    public CustomIngredientSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    public static final Identifier ID = Identifier.fromNamespaceAndPath("forge", "nbt");

    public static class Serializer implements CustomIngredientSerializer<StrictNBTIngredient> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public Identifier getIdentifier() {
            return ID;
        }

        @Override
        public MapCodec<StrictNBTIngredient> getCodec() {
            return ItemStack.MAP_CODEC.xmap(StrictNBTIngredient::new, ing -> ing.stack);
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, StrictNBTIngredient> getStreamCodec() {
            return ItemStack.STREAM_CODEC.map(StrictNBTIngredient::new, ing -> ing.stack);
        }
    }
}
