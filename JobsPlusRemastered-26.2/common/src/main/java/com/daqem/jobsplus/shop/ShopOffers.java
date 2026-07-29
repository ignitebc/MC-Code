package com.daqem.jobsplus.shop;

import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * 상점에서 취급하는 거래 목록.
 *
 * <p>거래 내용은 클라이언트가 보낸 패킷을 그대로 믿을 수 없으므로 서버에서도 대조해야 한다.
 * 그래서 목록을 GUI 클래스가 아니라 공용 패키지에 두고 화면과 서버가 같은 정의를 참조한다.
 * 상품을 추가할 때는 {@link #createDefaultOffers()}에만 넣으면 양쪽에 함께 반영된다.
 */
public final class ShopOffers
{
    private static final List<ShopOffer> OFFERS = List.copyOf(createDefaultOffers());

    private ShopOffers()
    {
    }

    public static List<ShopOffer> getOffers()
    {
        return OFFERS;
    }

    /**
     * 입력과 출력이 모두 일치하는 거래가 목록에 있는지 확인한다.
     * 수량까지 비교해야 같은 아이템의 다른 수량 거래를 위조할 수 없다.
     */
    public static boolean isValidOffer(Identifier inputItemId,
                                       int inputAmount,
                                       Identifier outputItemId,
                                       int outputAmount)
    {
        for (ShopOffer offer : OFFERS)
        {
            boolean sameInput = offer.inputItemId().equals(inputItemId) && offer.inputAmount() == inputAmount;
            boolean sameOutput = offer.outputItemId().equals(outputItemId) && offer.outputAmount() == outputAmount;
            if (sameInput && sameOutput)
            {
                return true;
            }
        }
        return false;
    }

    private static List<ShopOffer> createDefaultOffers()
    {
        List<ShopOffer> offers = new ArrayList<>();
        // 상점 물품 추가
        offers.add(new ShopOffer(Identifier.parse("advancednetherite:bitcoin"), 4, Identifier.parse("minecraft:diamond"), 8));
        offers.add(new ShopOffer(Identifier.parse("advancednetherite:bitcoin"), 50, Identifier.parse("minecraft:ancient_debris"), 1));
        offers.add(new ShopOffer(Identifier.parse("advancednetherite:bitcoin"), 350, Identifier.parse("minecraft:elytra"), 1));
        offers.add(new ShopOffer(Identifier.parse("advancednetherite:bitcoin"), 1, Identifier.parse("advancednetherite:random_box_i"), 1));
        offers.add(new ShopOffer(Identifier.parse("advancednetherite:bitcoin"), 1, Identifier.parse("advancednetherite:random_box_ii"), 1));
        offers.add(new ShopOffer(Identifier.parse("advancednetherite:bitcoin"), 1, Identifier.parse("advancednetherite:random_box_iii"), 1));
        offers.add(new ShopOffer(Identifier.parse("advancednetherite:bitcoin"), 1, Identifier.parse("advancednetherite:random_box_iv"), 1));
        offers.add(new ShopOffer(Identifier.parse("advancednetherite:bitcoin"), 10, Identifier.parse("advancednetherite:reward_key_i"), 1));
        offers.add(new ShopOffer(Identifier.parse("advancednetherite:bitcoin"), 30, Identifier.parse("advancednetherite:reward_key_ii"), 1));
        offers.add(new ShopOffer(Identifier.parse("advancednetherite:bitcoin"), 50, Identifier.parse("advancednetherite:reward_key_iii"), 1));
        offers.add(new ShopOffer(Identifier.parse("advancednetherite:bitcoin"), 100, Identifier.parse("advancednetherite:reward_key_iv"), 1));
        offers.add(new ShopOffer(Identifier.parse("advancednetherite:enhancement_shard"), 3, Identifier.parse("advancednetherite:enhancement_gem"), 1));
        offers.add(new ShopOffer(Identifier.parse("advancednetherite:bitcoin"), 50, Identifier.parse("advancednetherite:chunk_claim_map"), 1));

        offers.add(new ShopOffer(Identifier.parse("minecraft:cooked_chicken"), 60, Identifier.parse("minecraft:emerald"), 6));
        offers.add(new ShopOffer(Identifier.parse("minecraft:dirt"), 192, Identifier.parse("minecraft:emerald"), 1));
        offers.add(new ShopOffer(Identifier.parse("minecraft:netherrack"), 320, Identifier.parse("minecraft:emerald"), 1));
        return offers;
    }
}
