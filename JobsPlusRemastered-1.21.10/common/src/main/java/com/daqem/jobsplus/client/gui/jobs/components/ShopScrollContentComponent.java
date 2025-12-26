package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.widgets.ShopOfferEntryWidget;
import com.daqem.jobsplus.shop.ShopOffer;
import com.daqem.uilib.gui.component.EmptyComponent;

import java.util.List;

/**
 * ShopScrollWidget 안에서 실제 상품 목록을 그리는 컨텐츠 컴포넌트
 */
public class ShopScrollContentComponent extends EmptyComponent
{

    private static final int START_X = 9;
    private static final int START_Y = 0;
    private static final int ROW_HEIGHT = 24;

    public ShopScrollContentComponent(JobsScreenState state)
    {
        // 너비는 실제 row(98) + 좌우 여백(START_X) 기준으로 설정한다.
        // ScrollWidget은 전체 width(117)를 가지며 스크롤바 영역까지 포함한다.
        super(0, 0, 117, 0);

        List<ShopOffer> offers = ShopComponent.getOffers();

        for (int i = 0; i < offers.size(); i++)
        {
            ShopOffer offer = offers.get(i);
            this.addWidget(new ShopOfferEntryWidget(START_X, START_Y + (i * ROW_HEIGHT), state, offer));
        }

        // 전체 컨텐츠 높이 = row 수 * row 높이
        this.setHeight(offers.size() * ROW_HEIGHT);
    }
}
