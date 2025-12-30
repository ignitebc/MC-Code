package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.shop.ShopOffer;
import com.daqem.uilib.gui.component.EmptyComponent;
import com.daqem.uilib.gui.component.sprite.SpriteComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/**
 * 직업 GUI - SHOP 탭
 *
 * - 상품은 아래로 계속 추가될 예정이므로, ShopOffer 목록 기반으로 유지보수 가능하게 구성
 * - 힌트 텍스트 관련 로직은 전부 제거됨
 */
public class ShopComponent extends EmptyComponent
{
    // 상품 목록(여기에 계속 추가하면 됨)
    private static final List<ShopOffer> OFFERS = createDefaultOffers();

    /**
     * 다른 컴포넌트(스크롤 컨텐츠)에서 동일 목록을 사용하기 위한 접근자
     */
    public static List<ShopOffer> getOffers()
    {
        return OFFERS;
    }

    public ShopComponent(JobsScreenState state)
    {
        super(0, 0, 117, 167);

        // 배너
        SpriteComponent bannerComponent = new SpriteComponent(9, 0, 98, 33, JobsPlus.getId("jobs/shop_banner"));
        this.addComponent(bannerComponent);

        // 선택값이 없으면 첫 상품을 기본 선택
        if (state.getSelectedShopOffer() == null && !OFFERS.isEmpty())
        {
            state.setSelectedShopOffer(OFFERS.getFirst());
        }

        // 상품 리스트는 계속 늘어나므로 스크롤 컨테이너로 표시
        int startY = 46; // 기존 힌트가 있던 영역을 비우고 그대로 시작
        this.addComponent(new ShopScrollComponent(0, startY, getWidth(), getHeight() - startY, state));
    }

    private static List<ShopOffer> createDefaultOffers()
    {
        List<ShopOffer> offers = new ArrayList<>();
        // 상점 물품 추가
        offers.add(new ShopOffer(ResourceLocation.parse("advancednetherite:bitcoin"), 2, ResourceLocation.parse("minecraft:diamond"), 8));
        offers.add(new ShopOffer(ResourceLocation.parse("advancednetherite:bitcoin"), 5, ResourceLocation.parse("minecraft:ancient_debris"), 1));
        offers.add(new ShopOffer(ResourceLocation.parse("advancednetherite:bitcoin"), 30, ResourceLocation.parse("minecraft:elytra"), 1));
        offers.add(new ShopOffer(ResourceLocation.parse("advancednetherite:bitcoin"), 1, ResourceLocation.parse("advancednetherite:random_box_i"), 1));
        offers.add(new ShopOffer(ResourceLocation.parse("advancednetherite:bitcoin"), 2, ResourceLocation.parse("advancednetherite:random_box_ii"), 1));
        offers.add(new ShopOffer(ResourceLocation.parse("advancednetherite:bitcoin"), 3, ResourceLocation.parse("advancednetherite:random_box_iii"), 1));
        offers.add(new ShopOffer(ResourceLocation.parse("advancednetherite:bitcoin"), 4, ResourceLocation.parse("advancednetherite:random_box_iv"), 1));
        offers.add(new ShopOffer(ResourceLocation.parse("advancednetherite:bitcoin"), 10, ResourceLocation.parse("advancednetherite:reward_key_i"), 1));
        offers.add(new ShopOffer(ResourceLocation.parse("advancednetherite:bitcoin"), 30, ResourceLocation.parse("advancednetherite:reward_key_ii"), 1));
        offers.add(new ShopOffer(ResourceLocation.parse("advancednetherite:bitcoin"), 50, ResourceLocation.parse("advancednetherite:reward_key_iii"), 1));
        offers.add(new ShopOffer(ResourceLocation.parse("advancednetherite:bitcoin"), 100, ResourceLocation.parse("advancednetherite:reward_key_iv"), 1));
        offers.add(new ShopOffer(ResourceLocation.parse("advancednetherite:enhancement_shard"), 3, ResourceLocation.parse("advancednetherite:enhancement_gem"), 1));
        
        offers.add(new ShopOffer(ResourceLocation.parse("minecraft:cooked_chicken"), 10, ResourceLocation.parse("minecraft:emerald"), 1));
        offers.add(new ShopOffer(ResourceLocation.parse("minecraft:dirt"), 192, ResourceLocation.parse("minecraft:emerald"), 1));
        offers.add(new ShopOffer(ResourceLocation.parse("minecraft:netherrack"), 320, ResourceLocation.parse("minecraft:emerald"), 1));
        return offers;
    }
}
