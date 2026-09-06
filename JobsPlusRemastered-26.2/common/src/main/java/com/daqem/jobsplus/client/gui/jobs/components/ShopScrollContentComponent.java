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

    private static final int START_X = 0;
    private static final int START_Y = 0;
    private static final int ROW_HEIGHT = 32;

    public ShopScrollContentComponent(JobsScreenState state, int width)
    {
        // The parent reserves a separate strip for the scrollbar.
        super(0, 0, width, 0);

        List<ShopOffer> offers = ShopComponent.getOffers();

        for (int i = 0; i < offers.size(); i++)
        {
            ShopOffer offer = offers.get(i);
            this.addWidget(new ShopOfferEntryWidget(START_X, START_Y + (i * ROW_HEIGHT), getWidth(), state, offer));
        }

        // 전체 컨텐츠 높이 = row 수 * row 높이
        this.setHeight(offers.size() * ROW_HEIGHT);
    }
}
