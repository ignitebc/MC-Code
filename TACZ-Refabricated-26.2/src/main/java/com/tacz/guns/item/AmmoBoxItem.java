package com.tacz.guns.item;

import cn.sh1rocu.tacz.api.extension.IItem;
import cn.sh1rocu.tacz.compat.fabric.BuiltinItemRendererRegistry;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.api.item.nbt.AmmoBoxItemDataAccessor;
import com.tacz.guns.config.sync.SyncConfig;
import com.tacz.guns.init.ModItems;
import com.tacz.guns.inventory.tooltip.AmmoBoxTooltip;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.TooltipDisplay;
import java.util.Optional;
import java.util.function.Consumer;

public class AmmoBoxItem extends Item implements AmmoBoxItemDataAccessor, IItem {

    public static final int IRON_LEVEL = 0;
    public static final int GOLD_LEVEL = 1;
    public static final int DIAMOND_LEVEL = 2;

    public AmmoBoxItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack pOther, Slot slot, ClickAction action, Player player, SlotAccess access) {
        return super.overrideOtherStackedOnMe(stack, pOther, slot, action, player, access);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack ammoBox, Slot slot, ClickAction action, Player player) {
        // 우클릭
        if (action == ClickAction.SECONDARY) {
            // 클릭한 칸
            ItemStack slotItem = slot.getItem();
            Identifier boxAmmoId = this.getAmmoId(ammoBox);

            // 칸이 비었으면 아이템을 꺼내는 것이다
            if (slotItem.isEmpty()) {
                // 아무것도 없으면 꺼낼 수 없다
                if (boxAmmoId.equals(DefaultAssets.EMPTY_AMMO_ID)) {
                    return false;
                }
                // 수량이 맞지 않으면 꺼낼 수 없다
                int boxAmmoCount = this.getAmmoCount(ammoBox);
                if (boxAmmoCount <= 0) {
                    return false;
                }
                return TimelessAPI.getCommonAmmoIndex(boxAmmoId).map(index -> {
                    int takeCount = Math.min(index.getStackSize(), boxAmmoCount);
                    ItemStack takeAmmo = AmmoItemBuilder.create().setId(boxAmmoId).setCount(takeCount).build();
                    ItemStack remainingAmmo = slot.safeInsert(takeAmmo);
                    int insertedCount = takeCount - remainingAmmo.getCount();
                    if (insertedCount <= 0) {
                        return false;
                    }

                    int remainCount = boxAmmoCount - insertedCount;
                    this.setAmmoCount(ammoBox, remainCount);
                    if (remainCount <= 0) {
                        this.setAmmoId(ammoBox, DefaultAssets.EMPTY_AMMO_ID);
                    }
                    this.playRemoveOneSound(player);
                    return true;
                }).orElse(false);
            }

            // 탄환이면
            if (slotItem.getItem() instanceof IAmmo iAmmo) {
                Identifier slotAmmoId = iAmmo.getAmmoId(slotItem);
                // 칸 안 탄환 ID가 맞지 않으면 넣을 수 없다
                if (slotAmmoId.equals(DefaultAssets.EMPTY_AMMO_ID)) {
                    return false;
                }
                // 상자의 탄환 ID가 비었으면 지금 클릭한 종류로 바꾼다
                if (boxAmmoId.equals(DefaultAssets.EMPTY_AMMO_ID)) {
                    this.setAmmoId(ammoBox, slotAmmoId);
                } else if (!slotAmmoId.equals(boxAmmoId)) {
                    return false;
                }
                TimelessAPI.getCommonAmmoIndex(slotAmmoId).ifPresent(index -> {
                    int boxAmmoCount = this.getAmmoCount(ammoBox);
                    int boxLevelMultiplier = this.getAmmoLevel(ammoBox) + 1;
                    int maxSize = index.getStackSize() * SyncConfig.AMMO_BOX_STACK_SIZE.get() * boxLevelMultiplier;
                    int needCount = maxSize - boxAmmoCount;
                    ItemStack takeItem = slot.safeTake(slotItem.getCount(), needCount, player);
                    this.setAmmoCount(ammoBox, boxAmmoCount + takeItem.getCount());
                });
                // 꺼내는 소리 재생
                this.playInsertSound(player);
                return true;
            }
        }
        return false;
    }

    private void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return !this.getAmmoId(stack).equals(DefaultAssets.EMPTY_AMMO_ID) && this.getAmmoCount(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        Identifier ammoId = this.getAmmoId(stack);
        int ammoCount = this.getAmmoCount(stack);
        int boxLevelMultiplier = this.getAmmoLevel(stack) + 1;
        double widthPercent = TimelessAPI.getCommonAmmoIndex(ammoId).map(index -> {
            double totalCount = index.getStackSize() * SyncConfig.AMMO_BOX_STACK_SIZE.get() * boxLevelMultiplier;
            return ammoCount / totalCount;
        }).orElse(0d);
        return (int) Math.min(1 + 12 * widthPercent, 13);
    }

    @Override
    public Component getName(ItemStack stack) {
        int ammoLevel = getAmmoLevel(stack);
        switch (ammoLevel) {
            case GOLD_LEVEL -> {
                return Component.translatable("item.tacz.ammo_box.gold").withStyle(style -> style.withColor(0xFFFF55));
            }
            case DIAMOND_LEVEL -> {
                return Component.translatable("item.tacz.ammo_box.diamond").withStyle(style -> style.withColor(0x55FFFF));
            }
            default -> {
                return Component.translatable("item.tacz.ammo_box.iron");
            }
        }
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return Mth.hsvToRgb(1 / 3f, 1.0F, 1.0F);
    }

    public static void fillItemCategory(CreativeModeTab.Output output) {
        ItemStack ammoBox = ModItems.AMMO_BOX.getDefaultInstance();
        if (ammoBox.getItem() instanceof IAmmoBox iAmmoBox) {
            // 일반판 탄약 상자 추가
            output.accept(iAmmoBox.setAmmoLevel(ammoBox.copy(), IRON_LEVEL));
            output.accept(iAmmoBox.setAmmoLevel(ammoBox.copy(), GOLD_LEVEL));
            output.accept(iAmmoBox.setAmmoLevel(ammoBox.copy(), DIAMOND_LEVEL));
        }
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        if (!(stack.getItem() instanceof IAmmoBox iAmmoBox)) {
            return Optional.empty();
        }
        Identifier ammoId = iAmmoBox.getAmmoId(stack);
        if (ammoId.equals(DefaultAssets.EMPTY_AMMO_ID)) {
            return Optional.empty();
        }
        int ammoCount = iAmmoBox.getAmmoCount(stack);
        if (ammoCount <= 0) {
            return Optional.empty();
        }
        ItemStack ammoStack = AmmoItemBuilder.create().setId(ammoId).build();
        return Optional.of(new AmmoBoxTooltip(stack, ammoStack, ammoCount));
    }

    /**
     * 탄약 상자는 사용자 정의 렌더러를 <b>쓰지 않는다</b> — 바닐라 모델 렌더링을 탄다.
     *
     * <p>외형 변형은 {@code assets/tacz/items/ammo_box.json}의
     * {@code minecraft:select} + {@code tacz:ammo_statue} 속성
     * ({@code AmmoBoxStatueProperty} 참고)이 6개의
     * {@code models/item/ammo_box/*.json} 사이에서 바꾸며, 염색은 모델의
     * {@code minecraft:dye} tint가 맡는다. 이는 원본 1.21.1의 방식과 같다 —
     * 원본에도 탄약 상자 렌더러는 없고 {@code ItemProperties.register} + overrides만 있다.
     *
     * <p>이전에는 여기서 {@code AmmoBoxItemRenderer}를 돌려줬는데, 그것은 128×128
     * <b>3D 모델 UV 전개도</b>를 평면 아이콘으로 16×16 사각형에 붙여
     * 인벤토리와 모델 텍스처가 모두 뒤죽박죽 색 덩어리가 되었다. 그 클래스는 이번 수정에서 지웠다.
     */
    @Override
    @Environment(EnvType.CLIENT)
    public BuiltinItemRendererRegistry.DynamicItemRenderer getCustomRenderer() {
        return null;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> adder, TooltipFlag isAdvanced) {
        adder.accept(Component.translatable("tooltip.tacz.ammo_box.usage.deposit").withStyle(style -> style.withColor(0xAAAAAA)));
        adder.accept(Component.translatable("tooltip.tacz.ammo_box.usage.remove").withStyle(style -> style.withColor(0xAAAAAA)));
    }
}
