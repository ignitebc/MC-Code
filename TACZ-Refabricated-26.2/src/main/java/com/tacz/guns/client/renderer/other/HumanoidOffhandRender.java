package com.tacz.guns.client.renderer.other;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.resource.pojo.display.gun.LayerGunShow;
import com.tacz.guns.util.math.MathUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * "몸에 멘" 총을 그린다: 보조 손 총 + 손에 들지 않은 단축바의 총.
 *
 * <p><b>26.2 이전 설명(디컴파일 소스와 맞춤)</b></p>
 *
 * <p>1.21.1 구현은 {@code ItemRenderer#renderStatic(stack, ctx, light, overlay, poseStack,
 * MultiBufferSource, level, seed)}를 썼다. 26.2에는 그 메서드가 없다: 엔티티 층 렌더링은 "먼저
 * {@link ItemStackRenderState}를 extract하고, 그다음 {@link SubmitNodeCollector}에 submit하는" 두 단계 방식이 되었다.</p>
 *
 * <p>같은 효과의 경로(모두 javap / 디컴파일로 확인):</p>
 * <ul>
 *   <li>{@code Minecraft#getItemModelResolver()} → {@link ItemModelResolver}</li>
 *   <li>{@code ItemModelResolver#updateForTopItem(ItemStackRenderState, ItemStack,
 *       ItemDisplayContext, Level, ItemOwner, int)} — render state를 채운다.
 *       안에서 {@code output.clear()}를 하고 {@code displayContext}를 쓴다</li>
 *   <li>{@code ItemStackRenderState#submit(PoseStack, SubmitNodeCollector, int, int, int)}
 *       — 바닐라 {@code ItemInHandLayer#submitArmWithItem} 끝에서 부르는 것과 같은 메서드</li>
 * </ul>
 *
 * <p>여기서는 일부러 {@code updateForLiving}을 <b>쓰지 않는다</b>: 그쪽의 seed는
 * {@code entity.getId() + displayContext.ordinal()}이라 한 엔티티의 총 여러 자루(보조 손 + 여러 단축바 칸)가
 * 같은 seed를 얻는다. {@code updateForTopItem}을 쓰고 칸 번호를 seed에 섞어 총마다 독립되게 한다.</p>
 *
 * <p>좌표 변환은 1.21.1과 줄마다 같고(translate → scale(-x,-y,z) → 오일러 각을 사원수로),
 * 렌더링 제출 방식만 바꾸며 형상 의미는 바꾸지 않는다.</p>
 */
public class HumanoidOffhandRender {
    /** 보조 손 seed 오프셋. 0..8 단축바 칸 번호를 피한다. */
    private static final int OFFHAND_SEED_OFFSET = 100;

    /**
     * {@code ItemInHandLayerMixin}이 {@code ItemInHandLayer#submit}의 TAIL에서 호출한다.
     *
     * <p>26.2의 엔티티 층은 엔티티 자체가 아니라 render state를 받으므로
     * {@code state.id}로 엔티티를 다시 찾아야 한다. GUI/진열대 등에서는 실제 엔티티가 없을 수 있으며, 이때는 바로 건너뛴다.</p>
     */
    public static void renderGun(ArmedEntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, int packedLight) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        // ArmedEntityRenderState 자체에는 id 필드가 없고(javap 확인), id는 AvatarRenderState에 정의되어 있다.
        // 여기서는 render state의 실제 타입으로 엔티티를 되찾는다.
        LivingEntity entity = resolveEntity(state);
        if (entity == null) {
            return;
        }
        renderOffhandGun(entity, poseStack, collector, packedLight);
        renderHotbarGun(entity, poseStack, collector, packedLight);
    }

    private static LivingEntity resolveEntity(ArmedEntityRenderState state) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return null;
        }
        if (state instanceof net.minecraft.client.renderer.entity.state.AvatarRenderState avatarState) {
            if (minecraft.level.getEntity(avatarState.id) instanceof LivingEntity livingEntity) {
                return livingEntity;
            }
        }
        return null;
    }

    private static void renderOffhandGun(LivingEntity entity, PoseStack poseStack, SubmitNodeCollector collector, int packedLight) {
        ItemStack itemStack = entity.getOffhandItem();
        if (itemStack.isEmpty()) {
            return;
        }
        if (IGun.getIGunOrNull(itemStack) == null) {
            return;
        }
        TimelessAPI.getGunDisplay(itemStack).ifPresent(index -> {
            LayerGunShow offhandShow = index.getOffhandShow();
            if (offhandShow == null) {
                return;
            }
            renderGunItem(entity, poseStack, collector, packedLight, itemStack, offhandShow, OFFHAND_SEED_OFFSET);
        });
    }

    private static void renderHotbarGun(LivingEntity entity, PoseStack poseStack, SubmitNodeCollector collector, int packedLight) {
        if (!(entity instanceof Player player)) {
            return;
        }
        Inventory inventory = player.getInventory();
        // 26.2: Inventory#selected 필드는 getSelectedSlot() 접근자로 바뀌었다.
        int selected = inventory.getSelectedSlot();
        for (int i = 0; i < 9; i++) {
            if (i == selected) {
                continue;
            }
            renderHotbarGun(entity, poseStack, collector, packedLight, inventory.getItem(i), i);
        }
    }

    private static void renderHotbarGun(LivingEntity entity, PoseStack poseStack, SubmitNodeCollector collector, int packedLight,
                                        ItemStack itemStack, int inventoryIndex) {
        if (itemStack.isEmpty()) {
            return;
        }
        if (IGun.getIGunOrNull(itemStack) == null) {
            return;
        }
        TimelessAPI.getGunDisplay(itemStack).ifPresent(display -> {
            Int2ObjectArrayMap<LayerGunShow> hotbarShow = display.getHotbarShow();
            if (hotbarShow == null || hotbarShow.isEmpty()) {
                return;
            }
            if (!hotbarShow.containsKey(inventoryIndex)) {
                return;
            }
            renderGunItem(entity, poseStack, collector, packedLight, itemStack, hotbarShow.get(inventoryIndex), inventoryIndex);
        });
    }

    /**
     * 변환 부분은 1.21.1과 줄마다 같고, 제출 부분은 26.2의 extract → submit 두 단계로 바꿨다.
     */
    private static void renderGunItem(LivingEntity entity, PoseStack poseStack, SubmitNodeCollector collector, int packedLight,
                                      ItemStack itemStack, LayerGunShow gunShow, int seedSalt) {
        Minecraft minecraft = Minecraft.getInstance();
        ItemModelResolver resolver = minecraft.getItemModelResolver();
        if (resolver == null) {
            return;
        }

        Vector3f pos = gunShow.getPos();
        Vector3f rotate = gunShow.getRotate();
        Vector3f scale = gunShow.getScale();

        poseStack.pushPose();
        poseStack.translate(-pos.x() / 16f, 1.5 - pos.y() / 16f, pos.z() / 16f);
        poseStack.scale(-scale.x(), -scale.y(), scale.z());
        Quaternionf rotation = new Quaternionf();
        MathUtil.toQuaternion((float) Math.toRadians(rotate.x), (float) Math.toRadians(rotate.y), (float) Math.toRadians(rotate.z), rotation);
        poseStack.mulPose(rotation);

        // 26.2에서 예전 ItemRenderer#renderStatic(..., ItemDisplayContext.FIXED, ...)에 해당한다.
        // seed에 seedSalt를 섞어 한 엔티티의 총 여러 자루가 seed를 함께 쓰지 않게 한다.
        ItemStackRenderState renderState = new ItemStackRenderState();
        resolver.updateForTopItem(renderState, itemStack, ItemDisplayContext.FIXED, entity.level(), entity,
                entity.getId() + seedSalt * 31);
        renderState.submit(poseStack, collector, packedLight, OverlayTexture.NO_OVERLAY, 0);

        poseStack.popPose();
    }
}
