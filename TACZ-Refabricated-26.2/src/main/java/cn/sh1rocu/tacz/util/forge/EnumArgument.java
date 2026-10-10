package cn.sh1rocu.tacz.util.forge;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 26.2 재구성판 - {@link ArgumentTypeInfo} API 대응
 * <p>
 * 26.1+ Mojang 재구성:
 * <ul>
 *   <li>{@code ArgumentTypeInfo.Template}이 독립 제네릭이 되었다. 예전에는 {@code ArgumentTypeInfo<T>.Template} 내부 클래스였다</li>
 *   <li>새 시그니처: {@code ArgumentTypeInfo<A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>>}</li>
 *   <li>Template은 이제 {@code ArgumentTypeInfo.Template<A>}이며 {@code instantiate(CommandBuildContext)}를 제공한다</li>
 * </ul>
 */
public class EnumArgument<T extends Enum<T>> implements ArgumentType<T> {
    private static final Dynamic2CommandExceptionType INVALID_ENUM = new Dynamic2CommandExceptionType(
            (found, constants) -> Component.translatable("commands.tacz.arguments.enum.invalid", constants, found));
    private final Class<T> enumClass;

    public static <R extends Enum<R>> EnumArgument<R> enumArgument(Class<R> enumClass) {
        return new EnumArgument<>(enumClass);
    }

    private EnumArgument(final Class<T> enumClass) {
        this.enumClass = enumClass;
    }

    public Class<T> getEnumClass() {
        return enumClass;
    }

    @Override
    public T parse(final StringReader reader) throws CommandSyntaxException {
        String name = reader.readUnquotedString();
        try {
            return Enum.valueOf(enumClass, name);
        } catch (IllegalArgumentException e) {
            throw INVALID_ENUM.createWithContext(reader, name,
                    Arrays.toString(Arrays.stream(enumClass.getEnumConstants()).map(Enum::name).toArray()));
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context, final SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggest(Stream.of(enumClass.getEnumConstants()).map(Enum::name), builder);
    }

    @Override
    public Collection<String> getExamples() {
        return Stream.of(enumClass.getEnumConstants()).map(Enum::name).collect(Collectors.toList());
    }

    /**
     * 26.2 ArgumentTypeInfo 구현
     * <p>
     * 시그니처: {@code ArgumentTypeInfo<A, T>}, 여기서:
     * <ul>
     *   <li>A = EnumArgument (구체 클래스지만 제네릭 소거 때문에 실제 사용 시 형변환)</li>
     *   <li>T = Info.Template (구체 내부 클래스)</li>
     * </ul>
     */
    public static class Info implements ArgumentTypeInfo<EnumArgument<?>, Info.Template> {
        public static final Info INSTANCE = new Info();

        @Override
        public void serializeToNetwork(Template template, FriendlyByteBuf buffer) {
            buffer.writeUtf(template.enumClass.getName());
        }

        @SuppressWarnings("unchecked")
        @Override
        public Template deserializeFromNetwork(FriendlyByteBuf buffer) {
            try {
                String name = buffer.readUtf();
                return new Template((Class<? extends Enum<?>>) Class.forName(name));
            } catch (ClassNotFoundException e) {
                return null;
            }
        }

        @Override
        public void serializeToJson(Template template, JsonObject json) {
            json.addProperty("enum", template.enumClass.getName());
        }

        @SuppressWarnings("unchecked")
        @Override
        public Template unpack(EnumArgument<?> argument) {
            return new Template(argument.getEnumClass());
        }

        /**
         * 26.2 Template 클래스 - 독립 제네릭, {@code ArgumentTypeInfo.Template<EnumArgument<?>>} 구현
         */
        public static class Template implements ArgumentTypeInfo.Template<EnumArgument<?>> {
            final Class<? extends Enum<?>> enumClass;

            Template(Class<? extends Enum<?>> enumClass) {
                this.enumClass = enumClass;
            }

            @SuppressWarnings({"unchecked", "rawtypes"})
            @Override
            public EnumArgument<?> instantiate(CommandBuildContext pStructure) {
                return new EnumArgument(enumClass);
            }

            @Override
            public ArgumentTypeInfo<EnumArgument<?>, ?> type() {
                return INSTANCE;
            }
        }
    }
}
