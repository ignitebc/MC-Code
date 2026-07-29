package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.widgets.ShopScrollWidget;
import com.daqem.uilib.gui.component.EmptyComponent;

/**
 * SHOP 탭 상품 리스트 스크롤 영역
 *
 * ShopOffer는 계속 늘어나는 구조라 고정 row 렌더링이 아니라
 * ScrollContainerWidget 기반으로 전체를 스크롤 가능하게 표시한다.
 */
public class ShopScrollComponent extends EmptyComponent
{

    public ShopScrollComponent(int x, int y, int width, int height, JobsScreenState state)
    {
        super(x, y, width, height);

        ShopScrollWidget shopScrollWidget = new ShopScrollWidget(getWidth(), getHeight(), state);
        this.addWidget(shopScrollWidget);
    }
}
