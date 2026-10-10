package com.tacz.guns.client.renderer.item;

import cn.sh1rocu.tacz.compat.fabric.BuiltinItemRendererRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tacz.guns.GunMod;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemModels;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 26.2 아이템 모델 다리의 컴파일 수준 원형.
 *
 * <p>제거된 BuiltinItemRendererRegistry 연동을 사용자 정의 ItemModel 종류로 대신한다.
 * ItemModel.update에는 아직 ItemDisplayContext가 있으므로, 지연된 SpecialModelRenderer
 * 제출 단계 전에 스택과 문맥을 함께 변하지 않는 인자로 고정할 수 있다.</p>
 *
 * <p>이 클래스를 렌더링 이전이 끝난 것으로 보지 않는다. 기존 렌더러는 폐기되어 아무것도 하지 않는
 * BedrockModel.render 경로 호출을 그만두고, 지연 제출 전에 변할 수 있는 Bedrock 모델 상태를 스냅숏해야 한다.</p>
 */
public final class TaczDynamicItemModel implements ItemModel {
    public static final Identifier TYPE_ID = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "dynamic_item");

    private static final TaczSpecialRenderer SPECIAL_RENDERER = new TaczSpecialRenderer();

    /**
     * 모델 경계 상자의 꼭짓점. {@code ItemStackRenderState#visitExtents}가
     * {@code getModelBoundingBox()}를 계산하는 데 쓴다.
     *
     * <p><b>1.5가 아니라 0.5인 이유</b></p>
     *
     * <p>26.2의 GUI 아이템 렌더링에는 경로가 두 개 있고, {@code GuiItemRenderState}를 만들 때 정해진다:</p>
     * <pre>
     * oversizedItemBounds = itemStackRenderState.isOversizedInGui()
     *         ? calculateOversizedItemBounds() : null;
     * </pre>
     * 그리고 {@code calculateOversizedItemBounds()}의 판정은 다음과 같다:
     * <pre>
     * AABB aabb = itemStackRenderState.getModelBoundingBox();   // visitExtents에서 옴
     * int actualXSize = Mth.ceil(aabb.getXsize() * 16.0);
     * int actualYSize = Mth.ceil(aabb.getYsize() * 16.0);
     * if (actualXSize &lt;= 16 &amp;&amp; actualYSize &lt;= 16) return null;  // 일반 GuiItemAtlas로 감
     * else ... // OversizedItemRenderer(PIP, 화면 밖 RT)로 감
     * </pre>
     *
     * <p>원래는 ±1.5로 고정해 → 경계 상자 한 변이 3.0 → {@code 3.0 * 16 = 48 px} ≫ 16이었다.
     * 그래서 모든 TACZ 아이템이 "oversized"로 판정되어 {@code OversizedItemRenderer}라는
     * 그림 속 그림 화면 밖 렌더링 경로로 강제로 갔다. 그 경로는 48px 경계 상자로 배치하고 잘라내는데,
     * TACZ slot 텍스처는 실제로 1칸(16px)뿐이라 결국 16×16 칸 안에서
     * 보이지 않는 위치로 줄거나 밀렸다 — 그래서 <b>작업대 화면과 인벤토리의 아이콘이 모두 비어</b> 보였다.
     *
     * <p>±0.5(한 변 1.0 → 정확히 16 px)로 바꾸면 oversized가 아니라고 판정되어
     * 바닐라 아이템과 같은 {@code GuiItemAtlas} 경로를 탄다. 이는 TACZ 자체의
     * {@code renderSlotTexture} 의미와도 맞는다: 그것이 그리는 것이 바로 1×1칸 사각형이다.</p>
     *
     * <p>{@code items/*.json}의 {@code "oversized_in_gui": true}는 틀 밖으로 그리는 것을 허용할 뿐이고,
     * 실제로 어느 경로로 갈지는 여기의 경계 상자 크기가 정한다는 점에 주의한다.</p>
     *
     * <h2>Y가 대칭인 [-0.5, +0.5]가 아니라 [0, 1]인 이유</h2>
     *
     * <p>여기서는 <b>서로 부딪히는</b> 26.2 제약 두 개를 동시에 만족해야 한다:</p>
     *
     * <p><b>제약 1(GUI)</b>: 위에서 말했듯 {@code calculateOversizedItemBounds}는
     * {@code ceil(getXsize()*16) &gt; 16 || ceil(getYsize()*16) &gt; 16}으로 oversized를 판정한다.
     * 그래서 경계 상자의 모든 <b>변 길이</b>가 1.0 이하여야 한다.</p>
     *
     * <p><b>제약 2(떨어진 아이템)</b>: {@code ItemEntityRenderer#submit}에는 다음이 있다
     * <pre>
     * AABB aabb = state.item.getModelBoundingBox();
     * poseStack.translate(0, -aabb.minY() * ... + 0.0625F, 0);
     * </pre>
     * 즉 떨어진 아이템을 띄우는 높이가 <b>{@code minY}로 바로 정해진다</b>:
     * 바닐라는 이것으로 모델 밑면을 땅 위 1/16칸에 올린다.</p>
     *
     * <p>원래 Y가 ±0.5였을 때는 {@code minY = -0.5}라서 모든 TACZ 떨어진 아이템이
     * <b>모델 실제 크기와 관계없이 0.5칸(8픽셀)씩 더 떠 있었다</b> —
     * 땅 위의 총, 탄약 상자 등이 모두 너무 높이 떠 보였다. 1.21.1의
     * {@code ItemEntityRenderer}에는 이런 경계 상자 기반 보정이 없었으므로(고정 상수
     * {@code 0.25F} 등을 썼다), 이것은 숫자를 잘못 쓴 것이 아니라 버전 간 동작 변화 때문에 생긴
     * 순수한 회귀였다.</p>
     *
     * <p>Y를 {@code [0, 1]}로 바꾸면: 변 길이는 여전히 1.0이고(제약 1 만족, GUI 그대로),
     * {@code minY = 0}이라 띄우는 양이 바닐라의 {@code 0.0625}로 돌아간다(제약 2 만족).
     * XZ는 ±0.5 그대로 둔다 — 제약 1에만 관여하고, 회전할 때 모델 원점을 중심으로 하므로
     * 대칭 범위가 맞다.</p>
     */
    private static final Supplier<Vector3fc[]> EXTENTS = () -> new Vector3fc[]{
            new Vector3f(-0.5F, 0.0F, -0.5F),
            new Vector3f(-0.5F, 0.0F, 0.5F),
            new Vector3f(-0.5F, 1.0F, -0.5F),
            new Vector3f(-0.5F, 1.0F, 0.5F),
            new Vector3f(0.5F, 0.0F, -0.5F),
            new Vector3f(0.5F, 0.0F, 0.5F),
            new Vector3f(0.5F, 1.0F, -0.5F),
            new Vector3f(0.5F, 1.0F, 0.5F)
    };

    private final ModelRenderProperties properties;
    private final Matrix4fc transformation;

    private TaczDynamicItemModel(ModelRenderProperties properties, Matrix4fc transformation) {
        this.properties = properties;
        this.transformation = new Matrix4f(transformation);
    }

    /** 클라이언트 초기화 중, 클라이언트 아이템 JSON 파일을 해석하기 전에 실행해야 한다. */
    public static void registerType() {
        ItemModels.ID_MAPPER.put(TYPE_ID, Unbaked.MAP_CODEC);
    }

    @Override
    public void update(ItemStackRenderState state,
                       ItemStack stack,
                       ItemModelResolver resolver,
                       ItemDisplayContext displayContext,
                       ClientLevel level,
                       ItemOwner owner,
                       int seed) {
        state.appendModelIdentityElement(this);

        ItemStackRenderState.LayerRenderState layer = state.newLayer();
        if (stack.hasFoil()) {
            layer.setFoilType(ItemStackRenderState.FoilType.STANDARD);
            state.setAnimated();
        }

        RenderArgument argument = new RenderArgument(stack.copy(), displayContext);
        layer.setExtents(EXTENTS);
        layer.setLocalTransform(this.transformation);
        layer.setupSpecialModel(SPECIAL_RENDERER, argument);
        this.properties.applyToLayer(layer, displayContext);

        // 26.2 수정: 인벤토리 아이콘이 비던 근본 원인.
        //
        // GuiItemAtlas#getOrUpdate는 TrackingItemStackRenderState#getModelIdentity()(곧
        // modelIdentityElements라는 List)를 키로 DynamicAtlasAllocator에서 아이콘 칸을 할당·재사용하며,
        // List.equals -> 요소별 equals에 기댄다.
        //
        // 원래는 여기서 RenderArgument를 identity에 바로 넣었다. record의 equals는 필드별로 비교하는데,
        // ItemStack은 26.2에서 <b>equals/hashCode를 재정의하지 않아</b>(javap 확인, 정적
        // ItemStack.matches만 있음) 객체 동일성으로 비교한다. 게다가 위에서 stack.copy()를 해 매 프레임 새 객체였다.
        // 그 결과 identity가 매 프레임 달라 atlas가 완전히 새 아이템으로 보고 칸을 계속 다시 할당하며
        // clear/다시 그리기를 반복해 금세 바닥나거나 흔들렸고, 결국 <b>인벤토리 아이콘이 텅 비어</b> 보였다.
        //
        // 올바른 방법은 "실제로 외형에 영향을 주는" 값 의미를 가진 것만 identity에 넣는 것이다:
        //   - 아이템 자체(Item은 싱글턴이라 안전하게 비교할 수 있음)
        //   - display 문맥
        //   - TACZ 외형을 정하는 gun/attachment/ammo ID와 핵심 컴포넌트
        // 그러면 같은 총이 이웃한 프레임에 같은 칸을 찾아 아이콘이 정상으로 그려진다.
        state.appendModelIdentityElement(identityKeyOf(stack, displayContext));
    }

    /**
     * <b>값 의미</b>를 가진 identity 키를 만든다(List.equals에 안전하게 참여할 수 있음).
     *
     * <p>아이콘 외형에 영향을 주는 정보만 담는다. ItemStack 자체(equals 없음)와 탄약 수처럼
     * 자주 바뀌는 필드는 일부러 <b>넣지 않는다</b> — 후자는 GUI 아이콘에 영향이 없고(GUI는 slot 텍스처를 씀), 넣으면 다시 매 프레임 무효가 된다.</p>
     */
    private static Object identityKeyOf(ItemStack stack, ItemDisplayContext displayContext) {
        Identifier contentId = null;
        var item = stack.getItem();
        if (item instanceof com.tacz.guns.api.item.IGun iGun) {
            contentId = iGun.getGunId(stack);
        } else if (item instanceof com.tacz.guns.api.item.IAttachment iAttachment) {
            contentId = iAttachment.getAttachmentId(stack);
        } else if (item instanceof com.tacz.guns.api.item.IAmmo iAmmo) {
            contentId = iAmmo.getAmmoId(stack);
        } else if (item instanceof com.tacz.guns.api.item.nbt.BlockItemDataAccessor accessor) {
            contentId = accessor.getBlockId(stack);
        }
        return java.util.List.of(
                item,
                displayContext,
                contentId == null ? "" : contentId
        );
    }

    public record RenderArgument(ItemStack stack, ItemDisplayContext displayContext) {
    }

    private static final class TaczSpecialRenderer implements SpecialModelRenderer<RenderArgument> {
        @Override
        public void submit(RenderArgument argument,
                           PoseStack poseStack,
                           SubmitNodeCollector collector,
                           int light,
                           int overlay,
                           boolean hasFoil,
                           int outlineColor) {
            BuiltinItemRendererRegistry.DynamicItemRenderer renderer =
                    BuiltinItemRendererRegistry.INSTANCE.get(argument.stack().getItem());
            if (renderer != null) {
                renderer.render(argument.stack(), argument.displayContext(), poseStack, collector, light, overlay);
            }
        }

        @Override
        public void getExtents(Consumer<Vector3fc> output) {
            for (Vector3fc extent : EXTENTS.get()) {
                output.accept(extent);
            }
        }

        @Override
        public RenderArgument extractArgument(ItemStack stack) {
            // 사용자 정의 ItemModel은 setupSpecialModel로 실제 display 문맥을 넘긴다.
            return new RenderArgument(stack.copy(), ItemDisplayContext.NONE);
        }
    }

    public record Unbaked(Identifier base, Optional<Transformation> transformation) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Identifier.CODEC.fieldOf("base").forGetter(Unbaked::base),
                Transformation.EXTENDED_CODEC.optionalFieldOf("transformation").forGetter(Unbaked::transformation)
        ).apply(instance, Unbaked::new));

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(this.base);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc inheritedTransform) {
            Matrix4fc composedTransform = Transformation.compose(inheritedTransform, this.transformation);
            ModelBaker baker = context.blockModelBaker();
            ResolvedModel resolved = baker.getModel(this.base);
            TextureSlots slots = resolved.getTopTextureSlots();
            ModelRenderProperties properties = ModelRenderProperties.fromResolvedModel(baker, resolved, slots);
            return new TaczDynamicItemModel(properties, composedTransform);
        }

        @Override
        public MapCodec<? extends ItemModel.Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
