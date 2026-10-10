package com.tacz.guns.api.item.gun;

import cn.sh1rocu.tacz.api.extension.IItem;
import cn.sh1rocu.tacz.util.itemhandler.IItemHandler;
import cn.sh1rocu.tacz.util.itemhandler.ItemHandlerHelper;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.ReloadState;
import com.tacz.guns.api.item.*;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.api.item.builder.GunItemBuilder;
import com.tacz.guns.client.renderer.item.GunItemRendererWrapper;
import com.tacz.guns.client.resource.index.ClientGunIndex;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import com.tacz.guns.inventory.tooltip.GunTooltip;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.FeedType;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.util.AllowAttachmentTagMatcher;
import com.tacz.guns.util.ShooterMagazineBonus;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import cn.sh1rocu.tacz.compat.fabric.BuiltinItemRendererRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Supplier;

public abstract class AbstractGunItem extends Item implements IGun, IAnimationItem, IItem {
    protected AbstractGunItem(Properties pProperties) {
        super(pProperties);
    }

    private static Comparator<Map.Entry<Identifier, CommonGunIndex>> idNameSort() {
        return Comparator.comparingInt(m -> m.getValue().getSort());
    }

    /**
     * 노리쇠를 당기기 시작할 때 호출되며 bolt 상태를 돌려준다
     *
     * @return bolt 상태. true면 bolt를 시작하고, false면 시작하지 않는다.
     */
    public abstract boolean startBolt(ShooterDataHolder dataHolder, ItemStack gunItem, LivingEntity shooter);

    /**
     * 노리쇠 당기기 틱마다 호출되며 아직 bolt 상태인지 돌려준다
     *
     * @return 아직 bolt 상태인지
     */
    public abstract boolean tickBolt(ShooterDataHolder dataHolder, ItemStack gunItem, LivingEntity shooter);

    /**
     * 사격할 때 호출된다
     */
    public abstract void shoot(ShooterDataHolder dataHolder, ItemStack gunItem, Supplier<Float> pitch, Supplier<Float> yaw, LivingEntity shooter);

    /**
     * 재장전을 시작할 때 호출된다
     */
    public abstract boolean startReload(ShooterDataHolder dataHolder, ItemStack gunItem, LivingEntity shooter);

    /**
     * 재장전 중 매 틱 호출된다
     *
     * @return 돌려준 종류가 NOT_RELOADING이면 다음 틱부터 호출하지 않는다
     */
    public abstract ReloadState tickReload(ShooterDataHolder dataHolder, ItemStack gunItem, LivingEntity shooter);

    /**
     * 재장전을 끊으려 할 때 호출된다
     */
    public abstract void interruptReload(ShooterDataHolder dataHolder, ItemStack gunItem, LivingEntity shooter);

    /**
     * 발사 모드를 바꿀 때 호출된다
     */
    public abstract void fireSelect(ShooterDataHolder dataHolder, ItemStack gunItem);

    /**
     * 근접 공격할 때 호출된다
     */
    public abstract void melee(ShooterDataHolder dataHolder, LivingEntity user, ItemStack gunItem);

    /**
     * 과열 틱 처리<br/>
     * 기본적으로 아무것도 하지 않는다
     */
    public void tickHeat(ShooterDataHolder dataHolder, ItemStack gunItem, LivingEntity shooter) {
    }

    ;

    /**
     * 탄환의 각도와 속도를 초기화한다
     *
     * @param dataHolder     상태 데이터
     * @param gunItem        총기 아이템
     * @param shooter        사격자
     * @param projectile     탄환
     * @param bulletCnt      다탄두 탄환의 순번
     * @param processedSpeed 보정한 탄환 초속
     * @param inaccuracy     보정한 탄환 부정확도
     * @param pitch          사격 방향
     * @param yaw            사격 방향
     */
    public void doBulletSpread(ShooterDataHolder dataHolder, ItemStack gunItem, LivingEntity shooter, Projectile projectile,
                               int bulletCnt, float processedSpeed, float inaccuracy, float pitch, float yaw) {
        projectile.shootFromRotation(shooter, pitch, yaw, 0.0F, processedSpeed, inaccuracy);
    }

    /**
     * 재장전 전 검사. 총 안 탄약이 가득 찼는지, 플레이어 인벤토리에 쓸 탄약이 있는지, 인벤토리 급탄인지 확인한다.
     *
     * @param shooter 재장전하려는 엔티티
     * @param gunItem 총기 아이템
     * @return 재장전 조건을 만족하는지
     */
    public boolean canReload(LivingEntity shooter, ItemStack gunItem) {
        Identifier gunId = this.getGunId(gunItem);
        CommonGunIndex gunIndex = TimelessAPI.getCommonGunIndex(gunId).orElse(null);
        if (gunIndex == null) {
            return false;
        }

        int currentAmmoCount = getCurrentAmmoCount(gunItem);
        int maxAmmoCount = ShooterMagazineBonus.maxAmmoCount(shooter, gunItem, gunIndex);
        if (currentAmmoCount >= maxAmmoCount) {
            return false;
        }
        // 인벤토리 급탄이면 재장전하지 않는다
        if (useInventoryAmmo(gunItem)) {
            return false;
        }
        // 무한 예비 탄약은 실제 탄을 소모하지 않는다
        if (gunIndex.getGunData().getReloadData().isInfinite()) {
            return true;
        }
        // 가상 예비 탄약 처리
        if (useDummyAmmo(gunItem)) {
            return getDummyAmmoAmount(gunItem) > 0;
        }
        // 인벤토리의 탄약 수를 확인한다
        return shooter.tacz$getItemHandler(null).map(cap -> {
            // 인벤토리 확인
            for (int i = 0; i < cap.getSlots(); i++) {
                ItemStack checkAmmoStack = cap.getStackInSlot(i);
                if (checkAmmoStack.getItem() instanceof IAmmo iAmmo && iAmmo.isAmmoOfGun(gunItem, checkAmmoStack)) {
                    return true;
                }
                if (checkAmmoStack.getItem() instanceof IAmmoBox iAmmoBox && iAmmoBox.isAmmoBoxOfGun(gunItem, checkAmmoStack)) {
                    return true;
                }
            }
            return false;
        }).orElse(false);
    }

    /**
     * 총 안의 탄약을 모두 인벤토리로 돌려준다(인벤토리가 가득 차면 땅에 떨어뜨린다). 약실 안의 탄은 빼지 않는다.
     * 지금은 탄창 부착물을 바꿀 때만 호출한다.
     *
     * @param player  플레이어
     * @param gunItem 총기 아이템
     */
    @Override
    public void dropAllAmmo(Player player, ItemStack gunItem) {
        // 인벤토리 급탄이면 탄약을 빼지 않는다
        if (useInventoryAmmo(gunItem)) {
            return;
        }
        // 비플레이어 엔티티를 지원하도록 대상을 Player에서 LivingEntity로 바꿨다
        // 또한 이제 약실 안의 탄도 처리한다
        int ammoCount = getCurrentAmmoCount(gunItem);
        if (ammoCount <= 0) {
            return;
        }
        Identifier gunId = getGunId(gunItem);
        TimelessAPI.getCommonGunIndex(gunId).ifPresent(index -> {
            // 가상 예비 탄약을 쓰면 가상 예비 탄약으로 돌려준다
            if (useDummyAmmo(gunItem)) {
                setCurrentAmmoCount(gunItem, 0);
                // 연료통 방식 재장전은 돌려주지 않는다
                if (index.getGunData().getReloadData().getType().equals(FeedType.FUEL)) {
                    return;
                }
                addDummyAmmoAmount(gunItem, ammoCount);
                return;
            }

            Identifier ammoId = index.getGunData().getAmmoId();
            // 크리에이티브 방식 재장전은 탄 총수만 채우고 탄약을 빼는 로직은 실행하지 않는다
            if (player.isCreative()) {
                int maxAmmCount = ShooterMagazineBonus.maxAmmoCount(player, gunItem, index);
                setCurrentAmmoCount(gunItem, maxAmmCount);
                return;
            }
            // 연료통 방식은 비우기만 하고 돌려주지 않는다
            if (index.getGunData().getReloadData().getType().equals(FeedType.FUEL)) {
                setCurrentAmmoCount(gunItem, 0);
                return;
            }
            TimelessAPI.getCommonAmmoIndex(ammoId).ifPresent(ammoIndex -> {
                int stackSize = ammoIndex.getStackSize();
                int tmpAmmoCount = ammoCount;
                int roundCount = tmpAmmoCount / (stackSize + 1);
                for (int i = 0; i <= roundCount; i++) {
                    int count = Math.min(tmpAmmoCount, stackSize);
                    ItemStack ammoItem = AmmoItemBuilder.create().setId(ammoId).setCount(count).build();
                    ItemHandlerHelper.giveItemToPlayer(player, ammoItem);
                    tmpAmmoCount -= stackSize;
                }
                setCurrentAmmoCount(gunItem, 0);
            });
        });
    }

    /**
     * 총기의 탄약 찾기와 인벤토리 탄약 차감 로직
     *
     * @param itemHandler   대상 엔티티의 인벤토리
     * @param gunItem       총기 아이템
     * @param needAmmoCount 필요한 탄약(아이템) 수
     * @return 찾은 탄약(아이템) 수
     */
    @Deprecated
    public int findAndExtractInventoryAmmos(IItemHandler itemHandler, ItemStack gunItem, int needAmmoCount) {
        return findAndExtractInventoryAmmo(itemHandler, gunItem, needAmmoCount);
    }

    /**
     * 총기의 탄약 찾기와 인벤토리 탄약 차감 로직
     *
     * @param itemHandler   대상 엔티티의 인벤토리
     * @param gunItem       총기 아이템
     * @param needAmmoCount 필요한 탄약(아이템) 수
     * @return 찾은 탄약(아이템) 수
     */
    public int findAndExtractInventoryAmmo(IItemHandler itemHandler, ItemStack gunItem, int needAmmoCount) {
        int cnt = needAmmoCount;
        // 인벤토리 확인
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack checkAmmoStack = itemHandler.getStackInSlot(i);
            if (checkAmmoStack.getItem() instanceof IAmmo iAmmo && iAmmo.isAmmoOfGun(gunItem, checkAmmoStack)) {
                ItemStack extractItem = itemHandler.extractItem(i, cnt, false);
                cnt = cnt - extractItem.getCount();
                if (cnt <= 0) {
                    break;
                }
            }
            if (checkAmmoStack.getItem() instanceof IAmmoBox iAmmoBox && iAmmoBox.isAmmoBoxOfGun(gunItem, checkAmmoStack)) {
                int boxAmmoCount = iAmmoBox.getAmmoCount(checkAmmoStack);
                int extractCount = Math.min(boxAmmoCount, cnt);
                int remainCount = boxAmmoCount - extractCount;
                iAmmoBox.setAmmoCount(checkAmmoStack, remainCount);
                if (remainCount <= 0) {
                    iAmmoBox.setAmmoId(checkAmmoStack, DefaultAssets.EMPTY_AMMO_ID);
                }
                cnt = cnt - extractCount;
                if (cnt <= 0) {
                    break;
                }
            }
        }
        return needAmmoCount - cnt;
    }

    /**
     * 가상 탄약 차감 로직. 공통 구현이라 여기에 둔다
     *
     * @param gunItem       총기 아이템
     * @param needAmmoCount 필요한 탄약(아이템) 수
     * @return 찾은 탄약(아이템) 수
     */
    public int findAndExtractDummyAmmo(ItemStack gunItem, int needAmmoCount) {
        int dummyAmmoCount = getDummyAmmoAmount(gunItem);
        int extractCount = Math.min(dummyAmmoCount, needAmmoCount);
        addDummyAmmoAmount(gunItem, -extractCount);
        return extractCount;
    }

    /**
     * 총기에 지정한 아이템을 부착물로 장착할 수 있는지 확인한다
     */
    @Override
    public boolean allowAttachment(ItemStack gun, ItemStack attachmentItem) {
        IAttachment iAttachment = IAttachment.getIAttachmentOrNull(attachmentItem);
        IGun iGun = IGun.getIGunOrNull(gun);
        if (iGun != null && iAttachment != null) {
            Identifier gunId = iGun.getGunId(gun);
            Identifier attachmentId = iAttachment.getAttachmentId(attachmentItem);
            return AllowAttachmentTagMatcher.match(gunId, attachmentId);
        }
        return false;
    }

    /**
     * 총기에 특정 종류의 부착물을 장착할 수 있는지 확인한다
     */
    @Override
    public boolean allowAttachmentType(ItemStack gun, AttachmentType type) {
        IGun iGun = IGun.getIGunOrNull(gun);
        if (iGun != null) {
            return TimelessAPI.getCommonGunIndex(iGun.getGunId(gun)).map(gunIndex -> {
                List<AttachmentType> allowAttachments = gunIndex.getGunData().getAllowAttachments();
                if (allowAttachments == null) {
                    return false;
                }
                return allowAttachments.contains(type);
            }).orElse(false);
        } else {
            return false;
        }
    }

    /**
     * 총기의 표시 이름을 얻는다
     */
    @Override
    @Nonnull
    @Environment(EnvType.CLIENT)
    public Component getName(@Nonnull ItemStack stack) {
        Identifier gunId = this.getGunId(stack);
        Optional<ClientGunIndex> gunIndex = TimelessAPI.getClientGunIndex(gunId);
        if (gunIndex.isPresent()) {
            return Component.translatable(gunIndex.get().getName());
        }
        return super.getName(stack);
    }

    /**
     * 특정 TabType의 모든 총기 아이템 인스턴스를 얻는다. 크리에이티브 인벤토리와 총기 제작대를 채우는 데 쓴다.
     */
    public static NonNullList<ItemStack> fillItemCategory(GunTabType type) {
        NonNullList<ItemStack> stacks = NonNullList.create();
        TimelessAPI.getAllCommonGunIndex().stream().sorted(idNameSort()).forEach(entry -> {
            CommonGunIndex index = entry.getValue();
            GunData gunData = index.getGunData();
            String key = type.name().toLowerCase(Locale.US);
            String indexType = index.getType();
            if (key.equals(indexType)) {
                ItemStack itemStack = GunItemBuilder.create()
                        .setId(entry.getKey())
                        .setFireMode(gunData.getFireModeSet().get(0))
                        .setAmmoCount(gunData.getAmmoAmount())
                        .setHeatData(gunData.hasHeatData())
                        .setAmmoInBarrel(true)
                        .build();
                stacks.add(itemStack);
            }
        });
        return stacks;
    }

    /**
     * 플레이어 팔 휘두르기를 막는다
     */
    @Override
    public boolean tacz$onEntitySwing(ItemStack stack, LivingEntity entity) {
        return true;
    }

    @Override
    @Environment(EnvType.CLIENT)
    public BuiltinItemRendererRegistry.DynamicItemRenderer getCustomRenderer() {
        return GunItemRendererWrapper.INSTANCE.get();
    }

    /**
     * 툴팁에 그릴 그림을 얻는다
     */
    @Override
    @Nonnull
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        if (stack.getItem() instanceof IGun iGun) {
            Optional<CommonGunIndex> optional = TimelessAPI.getCommonGunIndex(this.getGunId(stack));
            if (optional.isPresent()) {
                CommonGunIndex gunIndex = optional.get();
                Identifier ammoId = gunIndex.getGunData().getAmmoId();
                return Optional.of(new GunTooltip(stack, iGun, ammoId, gunIndex));
            }
        }
        return Optional.empty();
    }

    /**
     * 인벤토리 탄약을 바로 쓰는지 얻는다
     *
     * @param gun 총기
     * @return 인벤토리 탄약을 바로 쓰는지
     */
    @Override
    public boolean useInventoryAmmo(ItemStack gun) {
        if (gun.getItem() instanceof IGun) {
            Optional<CommonGunIndex> gunIndexOptional = TimelessAPI.getCommonGunIndex(this.getGunId(gun));
            if (gunIndexOptional.isEmpty()) {
                return false;
            }
            CommonGunIndex gunIndex = gunIndexOptional.get();
            // 인벤토리 급탄인지
            return gunIndex.getGunData().getReloadData().getType().equals(FeedType.INVENTORY);
        }
        return false;
    }

    /**
     * 인벤토리 급탄에 쓸 탄약이 있는지 얻는다
     *
     * @param gun 총기
     * @return 인벤토리 급탄에 쓸 탄약이 있는지
     */
    @Override
    public boolean hasInventoryAmmo(LivingEntity shooter, ItemStack gun, boolean needCheckAmmo) {
        // 인벤토리 급탄이 아니면 바로 false를 돌려준다
        if (!useInventoryAmmo(gun)) {
            return false;
        }
        // 탄을 확인할 필요가 없으면 바로 true를 돌려준다
        if (!needCheckAmmo) {
            return true;
        }
        // 가상 예비 탄약 처리
        if (useDummyAmmo(gun)) {
            return getDummyAmmoAmount(gun) > 0;
        }
        // 인벤토리의 탄약 수를 확인한다
        return shooter.tacz$getItemHandler(null).map(cap -> {
            // 인벤토리 확인
            for (int i = 0; i < cap.getSlots(); i++) {
                ItemStack checkAmmoStack = cap.getStackInSlot(i);
                if (checkAmmoStack.getItem() instanceof IAmmo iAmmo && iAmmo.isAmmoOfGun(gun, checkAmmoStack)) {
                    return true;
                }
                if (checkAmmoStack.getItem() instanceof IAmmoBox iAmmoBox && iAmmoBox.isAmmoBoxOfGun(gun, checkAmmoStack)) {
                    return true;
                }
            }
            return false;
        }).orElse(false);
    }

    /**
     * RPM을 얻는다
     *
     * @param gun 총기
     * @return RPM 값
     */
    public int getRPM(ItemStack gun) {
        if (gun.getItem() instanceof IGun iGun) {
            return TimelessAPI.getCommonGunIndex(this.getGunId(gun))
                    .map(CommonGunIndex::getGunData)
                    .map(gunData -> {
                        FireMode fireMode = getFireMode(gun);
                        int rpm = gunData.getRoundsPerMinute(fireMode);
                        if (iGun.hasHeatData(gun)) {
                            rpm *= (int) iGun.lerpRPM(gun);
                        }
                        return rpm;
                    }).orElse(300);
        }
        return 300;
    }

    /**
     * 엎드려 사격할 수 있는지 얻는다
     *
     * @param gun 총기
     * @return 엎드려 사격할 수 있는지
     */
    public boolean isCanCrawl(ItemStack gun) {
        if (gun.getItem() instanceof IGun) {
            return TimelessAPI.getCommonGunIndex(this.getGunId(gun))
                    .map(CommonGunIndex::getGunData)
                    .map(GunData::isCanCrawl)
                    .orElse(false);
        }
        return false;
    }

    @Override
    public boolean isSame(ItemStack i, ItemStack j) {
        IGun iGun1 = IGun.getIGunOrNull(i);
        IGun iGun2 = IGun.getIGunOrNull(j);
        if (iGun1 != null && iGun2 != null) {
            return iGun1.getGunId(i).equals(iGun2.getGunId(j)) && iGun1.getGunDisplayId(i).equals(iGun2.getGunDisplayId(j));
        }
        if (i.isEmpty() || j.isEmpty()) {
            return i.isEmpty() && j.isEmpty();
        }
        return ItemStack.matches(i, j);
    }
}
