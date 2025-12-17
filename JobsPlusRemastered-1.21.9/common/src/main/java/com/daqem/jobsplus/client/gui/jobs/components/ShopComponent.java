package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.widgets.SellItemButtonWidget;
import com.daqem.uilib.gui.component.EmptyComponent;
import com.daqem.uilib.gui.component.item.ItemComponent;
import com.daqem.uilib.gui.component.sprite.SpriteComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ShopComponent extends EmptyComponent
{

    private static final int REQUIRED_AMOUNT = 10; // 판매에 필요한 익힌 닭고기 개수
    private static final int REWARD_COINS = 100; // 지급될 코인 개수

    public ShopComponent(JobsScreenState state)
    {
        super(0, 0, 117, 167);

        // 배너 추가
        SpriteComponent bannerComponent = new SpriteComponent(9, 0, 98, 33, JobsPlus.getId("jobs/shop_banner"));
        this.addComponent(bannerComponent);

        // 익힌 닭고기 아이템 표시
        ItemStack cookedChickenStack = new ItemStack(Items.COOKED_CHICKEN, REQUIRED_AMOUNT);
        ItemComponent itemComponent = new ItemComponent(9, 40, cookedChickenStack, true);
        this.addComponent(itemComponent);

        // 판매 버튼 추가
        SellItemButtonWidget sellButton = new SellItemButtonWidget(9, 70, state, Items.COOKED_CHICKEN, REQUIRED_AMOUNT, REWARD_COINS);
        this.addWidget(sellButton);
    }
}

