package com.tacz.guns.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tacz.guns.GunMod;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.crafting.result.GunSmithTableResult;
import com.tacz.guns.crafting.result.RawGunTableResult;
import com.tacz.guns.resource.pojo.data.recipe.GunResult;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** 예전 TACZ 총기 작업대 레시피 JSON 형식용 26.2 codec. */
public final class GunSmithTableSerializer {
    private static final Codec<GunSmithTableIngredient> INGREDIENT_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Ingredient.CODEC.fieldOf("item").forGetter(GunSmithTableIngredient::getIngredientOrThrow),
                    Codec.INT.optionalFieldOf("count", 1).forGetter(GunSmithTableIngredient::getCount)
            ).apply(instance, GunSmithTableIngredient::new)
    );

    private static final Codec<Map<String, Identifier>> ATTACHMENTS_CODEC =
            Codec.unboundedMap(Codec.STRING, Identifier.CODEC);

    /**
     * {@code result.group}의 코덱: <b>네임스페이스가 없으면 바닐라의 {@code minecraft:}가 아니라 {@code tacz:}를 붙인다</b>.
     *
     * <h2>{@code Identifier.CODEC}을 그대로 쓸 수 없는 이유</h2>
     * 총기 팩의 {@code group}은 관례상 <b>맨 이름</b>(예: {@code "shotgun_shells"})으로 쓴다 — 기본 총기 팩의 탄약 레시피 24개가
     * 모두 이렇게 적혀 있다. {@code Identifier.CODEC}은
     * {@code Codec.STRING.comapFlatMap(Identifier::read, ...)} →
     * {@code Identifier.parse} → {@code bySeparator(s, ':')}를 거치며, 바이트코드로 확인한 결과 문자열에 {@code ':'}가 없으면
     * {@code withDefaultNamespace}로 가는데, 이 메서드는 네임스페이스를 {@code "minecraft"}로 <b>고정</b>한다
     * (오프셋 4/6 두 곳의 상수 {@code 'minecraft'}).
     * 그래서 {@code "shotgun_shells"}는 {@code minecraft:shotgun_shells}로 해석되고,
     * 작업대 탭 id는 {@code tacz:shotgun_shells}라 — 둘은 영원히 같아지지 않는다.
     *
     * <h2>이것이 바로 "탄약에 레시피도 있고 재료도 충분한데 제작을 눌러도 아무 반응이 없음"의 근본 원인이다</h2>
     * {@link com.tacz.guns.inventory.GunSmithTableMenu#getRecipe}에는 검사가 하나 있다:
     * 레시피의 {@code getTab()}(곧 이 group)이 현재 블록의 탭 중 하나와 맞아야 하며, 아니면 {@code null}을 돌려준다
     * → {@code doCraft}가 바로 return하며 <b>오류도, 안내도, 재료 차감도 없다</b>.
     * 탄약 레시피의 group이 {@code minecraft:*}가 되면 반드시 빗나가므로 <b>탄 하나도 만들 수 없었다</b>.
     * 반면 총기/부착물 레시피에는 <b>group 필드가 아예 없어</b>(기본 팩의 총 53정 + 부착물 95개 모두 없음)
     * {@code init()}에서 아이템 인덱스로 올바른 {@code tacz:rifle} 등을 거꾸로 찾아내므로 평소대로 만들 수 있다 —
     * "탄약만 제작할 수 없음" 현상이 이로써 완전히 설명된다.
     *
     * <h2>원본과 비교</h2>
     * 원본 1.21.1의 {@code GunSmithTableResult#decode}는 이 정규화를 분명히 적어 두었다:
     * <pre>{@code Codec.STRING.optionalFieldOf("group") ... .map(raw -> raw.contains(":") ? raw : GunMod.MOD_ID + ":" + raw)}</pre>
     * 이 프로젝트가 {@code RecordCodecBuilder}로 이식하면서 이를 빠뜨렸으므로 <b>이식 회귀</b>다.
     * {@code tacz:}를 붙이는 로직은 계속 남아 있었다 — 그래서 두 경로가 갈라졌다:
     * <b>화면은 {@code tacz:} 기준으로 레시피를 보여 주고, 서버는 {@code minecraft:} 기준으로 검사해 "보이는데 눌러도 안 됨"이 되었다.</b>
     */
    private static final Codec<Identifier> GROUP_CODEC = Codec.STRING.xmap(
            raw -> Identifier.parse(raw.contains(":") ? raw : GunMod.MOD_ID + ":" + raw),
            Identifier::toString
    );

    private record ResultSpec(String type,
                              Identifier id,
                              int count,
                              int ammoCount,
                              Optional<Identifier> group,
                              Map<String, Identifier> attachments) {
        private static final Codec<ResultSpec> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("type").forGetter(ResultSpec::type),
                Identifier.CODEC.fieldOf("id").forGetter(ResultSpec::id),
                Codec.INT.optionalFieldOf("count", 1).forGetter(ResultSpec::count),
                Codec.INT.optionalFieldOf("ammo_count", 0).forGetter(ResultSpec::ammoCount),
                GROUP_CODEC.optionalFieldOf("group").forGetter(ResultSpec::group),
                ATTACHMENTS_CODEC.optionalFieldOf("attachments", Map.of()).forGetter(ResultSpec::attachments)
        ).apply(instance, ResultSpec::new));

        GunSmithTableResult toResult() {
            RawGunTableResult raw = new RawGunTableResult(type, id, Math.max(1, count));
            if (GunSmithTableResult.GUN.equals(type)) {
                EnumMap<AttachmentType, Identifier> parsedAttachments = new EnumMap<>(AttachmentType.class);
                attachments.forEach((name, attachmentId) -> {
                    try {
                        parsedAttachments.put(AttachmentType.valueOf(name.toUpperCase(java.util.Locale.ROOT)), attachmentId);
                    } catch (IllegalArgumentException ignored) {
                    }
                });
                raw.setExtraData(new GunResult(ammoCount, parsedAttachments));
            }
            return new GunSmithTableResult(raw, group.orElse(null));
        }

        static ResultSpec fromRecipe(GunSmithTableRecipe recipe) {
            ItemStack stack = recipe.getResult().getResult();
            String type = GunSmithTableResult.CUSTOM;
            Identifier id = Identifier.withDefaultNamespace("air");
            if (stack.getItem() instanceof IGun gun) {
                type = GunSmithTableResult.GUN;
                id = gun.getGunId(stack);
            } else if (stack.getItem() instanceof IAmmo ammo) {
                type = GunSmithTableResult.AMMO;
                id = ammo.getAmmoId(stack);
            } else if (stack.getItem() instanceof IAttachment attachment) {
                type = GunSmithTableResult.ATTACHMENT;
                id = attachment.getAttachmentId(stack);
            }
            return new ResultSpec(type, id, Math.max(1, stack.getCount()), 0,
                    Optional.ofNullable(recipe.getResult().getGroup()), Map.of());
        }
    }

    /**
     * 최신 Minecraft에서는 자원 id를 RecipeHolder가 가지며 MapCodec에 넘겨주지 않는다.
     * TACZ는 여전히 레시피 객체에 id가 있다고 기대하므로, 결과 id를 안정적인 대체값으로 쓴다.
     */
    public static final MapCodec<GunSmithTableRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ResultSpec.CODEC.fieldOf("result").forGetter(ResultSpec::fromRecipe),
                    INGREDIENT_CODEC.listOf().fieldOf("materials").forGetter(GunSmithTableRecipe::getInputs)
            ).apply(instance, (result, materials) ->
                    new GunSmithTableRecipe(result.id(), result.toResult(), materials))
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, GunSmithTableRecipe> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public GunSmithTableRecipe decode(RegistryFriendlyByteBuf buffer) {
                    Identifier recipeId = buffer.readIdentifier();
                    int size = buffer.readInt();
                    List<GunSmithTableIngredient> ingredients = new ArrayList<>();
                    for (int i = 0; i < size; i++) {
                        ingredients.add(new GunSmithTableIngredient(
                                Ingredient.CONTENTS_STREAM_CODEC.decode(buffer), buffer.readInt()));
                    }
                    ItemStack resultItem = ItemStack.STREAM_CODEC.decode(buffer);
                    Identifier group = buffer.readIdentifier();
                    return new GunSmithTableRecipe(recipeId, new GunSmithTableResult(resultItem, group), ingredients);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, GunSmithTableRecipe recipe) {
                    buffer.writeIdentifier(recipe.getId());
                    buffer.writeInt(recipe.getInputs().size());
                    for (GunSmithTableIngredient ingredient : recipe.getInputs()) {
                        Ingredient.CONTENTS_STREAM_CODEC.encode(buffer, ingredient.getIngredientOrThrow());
                        buffer.writeInt(ingredient.getCount());
                    }
                    ItemStack.STREAM_CODEC.encode(buffer, recipe.getResult().getResult());
                    buffer.writeIdentifier(recipe.getResult().getGroup());
                }
            };

    private GunSmithTableSerializer() {
    }

    public static RecipeSerializer<GunSmithTableRecipe> create() {
        return new RecipeSerializer<>(CODEC, STREAM_CODEC);
    }
}
