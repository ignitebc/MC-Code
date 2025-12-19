package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.widgets.ShopOfferEntryWidget;
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

        // 상품 리스트(아래로 계속 추가되는 구조)
        int startY = 46;   // 기존 힌트가 있던 영역을 비우고 그대로 시작
        int rowH = 24;
        int maxRows = (this.getHeight() - startY) / rowH;

        for (int i = 0; i < Math.min(OFFERS.size(), maxRows); i++)
        {
            ShopOffer offer = OFFERS.get(i);
            ShopOfferEntryWidget row = new ShopOfferEntryWidget(9, startY + (i * rowH), state, offer);
            this.addWidget(row);
        }
    }

    private static List<ShopOffer> createDefaultOffers()
    {
        List<ShopOffer> offers = new ArrayList<>();
        
        offers.add(new ShopOffer(ResourceLocation.parse("advancednetherite:bitcoin"), 2, ResourceLocation.parse("minecraft:diamond"), 8));
        offers.add(new ShopOffer(ResourceLocation.parse("advancednetherite:bitcoin"), 3, ResourceLocation.parse("minecraft:ancient_debris"), 1));
        offers.add(new ShopOffer(ResourceLocation.parse("advancednetherite:bitcoin"), 25, ResourceLocation.parse("minecraft:elytra"), 1));

        return offers;
    }
}
