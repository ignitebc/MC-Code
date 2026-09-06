package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.shop.ShopOffer;
import com.daqem.jobsplus.shop.ShopOffers;
import com.daqem.uilib.gui.component.EmptyComponent;

import java.util.List;

/**
 * 직업 GUI - SHOP 탭
 *
 * - 상품 목록은 서버 검증과 같은 정의를 써야 하므로 ShopOffers에서 가져온다.
 * - 힌트 텍스트 관련 로직은 전부 제거됨
 */
public class ShopComponent extends EmptyComponent
{
    /**
     * 다른 컴포넌트(스크롤 컨텐츠)에서 동일 목록을 사용하기 위한 접근자
     */
    public static List<ShopOffer> getOffers()
    {
        return ShopOffers.getOffers();
    }

    public ShopComponent(JobsScreenState state, int width, int height)
    {
        super(0, 0, width, height);

        List<ShopOffer> offers = ShopOffers.getOffers();

        // 선택값이 없으면 첫 상품을 기본 선택
        if (state.getSelectedShopOffer() == null && !offers.isEmpty())
        {
            state.setSelectedShopOffer(offers.getFirst());
        }

        // 상품 리스트는 계속 늘어나므로 스크롤 컨테이너로 표시
        int startY = 0;
        this.addComponent(new ShopScrollComponent(0, startY, getWidth(), getHeight() - startY, state));
    }
}
