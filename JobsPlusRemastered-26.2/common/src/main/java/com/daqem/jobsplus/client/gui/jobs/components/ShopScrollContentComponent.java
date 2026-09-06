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

    private static final int ROW_HEIGHT = 29;

    public ShopScrollContentComponent(JobsScreenState state, int width)
    {
        // The parent reserves a separate strip for the scrollbar.
        super(0, 0, width, 0);

        List<ShopOffer> offers = ShopComponent.getOffers();

        int gap = 6;
        int columnWidth = (getWidth() - gap) / 2;
        for (int i = 0; i < offers.size(); i++)
        {
            ShopOffer offer = offers.get(i);
            this.addWidget(new ShopOfferEntryWidget((i % 2) * (columnWidth + gap), (i / 2) * ROW_HEIGHT, columnWidth, state, offer));
        }

        // 전체 컨텐츠 높이 = row 수 * row 높이
        this.setHeight(((offers.size() + 1) / 2) * ROW_HEIGHT);
    }
}
