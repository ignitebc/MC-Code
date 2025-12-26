package com.daqem.jobsplus.shop;

import net.minecraft.resources.ResourceLocation;

/**
 * 상점 거래 1건(입력 아이템 -> 출력 아이템).
 *
 * 유지보수 관점에서 GUI/패킷/서버 로직이 동일한 구조를 공유할 수 있도록
 * 레지스트리 ID(ResourceLocation) 기반으로만 정의한다.
 */
public record ShopOffer
(
        ResourceLocation inputItemId,
        int inputAmount,
        ResourceLocation outputItemId,
        int outputAmount
)
{
}
