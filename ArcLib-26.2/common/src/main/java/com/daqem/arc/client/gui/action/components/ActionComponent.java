package com.daqem.arc.client.gui.action.components;

import com.daqem.arc.Arc;
import com.daqem.arc.api.action.IAction;
import com.daqem.uilib.gui.component.color.ColorComponent;
import com.daqem.uilib.gui.component.sprite.SpriteComponent;
import com.daqem.uilib.gui.component.text.TextAlign;
import com.daqem.uilib.gui.component.text.TextComponent;
import com.daqem.uilib.gui.component.text.TruncatedTextComponent;
import com.daqem.uilib.gui.component.text.multiline.MultiLineTextComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ActionComponent extends SpriteComponent {

    private final IAction action;
    private final ConditionsTabWidget conditionsTabComponent;
    private final RewardsTabWidget rewardsTabComponent;
    private final ScrollContainerComponent scrollContainerComponent;

    public ActionComponent(int index, IAction action) {
        super(0, 0, 326, 166, Arc.getId("action_background"));
        this.action = action;

        String indexString = String.valueOf(index + 1);
        Font font = Minecraft.getInstance().font;
        TruncatedTextComponent titleComponent = new TruncatedTextComponent(7, 13, 130 - font.width(indexString), action.getName().copy().withStyle(Style.EMPTY.withBold(true)), 0xFF333333);
        TextComponent indexComponent = new TextComponent(140, 13, Arc.literal(indexString), 0xFFAAAAAA);
        indexComponent.setTextAlign(TextAlign.RIGHT);
        MultiLineTextComponent descriptionComponent = new MultiLineTextComponent(7, 13 + 5 + font.lineHeight, 132, action.getDescription(), 0xFF555555);
        ColorComponent lineComponent = new ColorComponent(7, 13 + font.lineHeight + 1, 132, 1, 0xFFFFFFFF);
        this.conditionsTabComponent = new ConditionsTabWidget(156, -28, true, new SpriteComponent(0, 0, 18, 18, Arc.getId("conditions_icon")), this);
        this.rewardsTabComponent = new RewardsTabWidget(188, -28, false, new SpriteComponent(0, 0, 18, 18, Arc.getId("rewards_icon")), this);
        this.scrollContainerComponent = new ScrollContainerComponent(157, 15, 162, 140, getScrollItemComponents(conditionsTabComponent));

        this.addComponent(titleComponent);
        this.addComponent(indexComponent);
        this.addComponent(descriptionComponent);
        this.addComponent(lineComponent);
        this.addWidget(conditionsTabComponent);
        this.addWidget(rewardsTabComponent);
        this.addComponent(scrollContainerComponent);
    }

    public void selectTab(AbstractTabWidget tabComponent) {
        getTabComponents().forEach(t -> t.setSelected(false));
        tabComponent.setSelected(true);
        scrollContainerComponent.setItems(getScrollItemComponents(tabComponent));
        scrollContainerComponent.updateParentPosition(
                getTotalX(),
                getTotalY(),
                getWidth(),
                getHeight()
        );
    }

    public List<AbstractTabWidget> getTabComponents() {
        return Arrays.asList(conditionsTabComponent, rewardsTabComponent);
    }

    public List<ScrollItemComponent> getScrollItemComponents(AbstractTabWidget activeTabComponent) {
        if (action != null) {
            if (activeTabComponent == conditionsTabComponent) {
                return action.getConditions().stream()
                        .map(c -> new ScrollItemComponent(c.getName(), c.getDescription()))
                        .toList();
            } else if (activeTabComponent == rewardsTabComponent) {
                return action.getRewards().stream()
                        .map(c -> new ScrollItemComponent(c.getName(), c.getDescription()))
                        .toList();
            }
        }
        return new ArrayList<>();
    }

    public IAction getAction() {
        return action;
    }
}
