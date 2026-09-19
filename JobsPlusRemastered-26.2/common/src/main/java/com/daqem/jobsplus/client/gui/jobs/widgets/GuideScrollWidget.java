package com.daqem.jobsplus.client.gui.jobs.widgets;

/**
 * 게임 안내 본문을 담는 스크롤 영역.
 *
 * <p>내용은 쪽을 넘길 때마다 바깥에서 새로 채우므로 여기서는 빈 상태로 만든다.
 */
public class GuideScrollWidget extends AbstractScrollWidget
{

    public GuideScrollWidget(int width, int height)
    {
        super(width, height, 25);
    }
}
