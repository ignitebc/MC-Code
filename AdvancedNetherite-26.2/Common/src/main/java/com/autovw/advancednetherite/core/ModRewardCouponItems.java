package com.autovw.advancednetherite.core;

import com.autovw.advancednetherite.AdvancedNetherite;
import com.autovw.advancednetherite.common.item.RewardCouponItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

/**
 * JobsPlus가 효과를 처리하는 직업 보상 쿠폰 아이템 정의.
 * AdvancedNetherite는 아이템 등록과 리소스만 담당한다.
 */
public final class ModRewardCouponItems
{
    public static final RewardCouponItem EXPERIENCE_DOUBLE_COUPON = new RewardCouponItem(
            new Item.Properties().setId(key("experience_double_coupon")),
            RewardCouponItem.CouponType.EXPERIENCE
    );
    public static final RewardCouponItem BITCOIN_DOUBLE_COUPON = new RewardCouponItem(
            new Item.Properties().setId(key("bitcoin_double_coupon")),
            RewardCouponItem.CouponType.BITCOIN
    );
    public static final RewardCouponItem BITCOIN_TRIPLE_COUPON = new RewardCouponItem(
            new Item.Properties().setId(key("bitcoin_triple_coupon")),
            RewardCouponItem.CouponType.BITCOIN,
            3
    );

    private ModRewardCouponItems()
    {
    }

    private static ResourceKey<Item> key(String name)
    {
        return ResourceKey.create(
                Registries.ITEM,
                Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, name)
        );
    }
}
