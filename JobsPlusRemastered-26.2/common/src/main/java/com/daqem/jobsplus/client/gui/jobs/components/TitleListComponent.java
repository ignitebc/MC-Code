package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.client.gui.jobs.widgets.ActionScrollWidget;
import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.jobsplus.client.title.ClientTitles;
import com.daqem.jobsplus.networking.c2s.ServerboundEquipTitlePacket;
import com.daqem.jobsplus.player.title.TitleType;
import com.daqem.uilib.gui.component.EmptyComponent;
import com.daqem.uilib.gui.widget.CustomButtonWidget;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

import java.util.Optional;

/** 칭호 탭. 칭호마다 배지·획득 조건·보유자를 보여 주고, 내 칭호는 장착하거나 해제할 수 있다. */
public class TitleListComponent extends EmptyComponent
{
    private static final int ROW_HEIGHT = 30;
    private static final int ROW_GAP = 3;
    /** 스크롤 막대가 들어갈 오른쪽 여백 */
    private static final int SCROLL_BAR_SPACE = 10;
    private static final int PADDING = 6;
    /** 배지 그림(높이 36px)을 절반으로 그린다. 4배 밀도 그림이라 GUI 배율 2에서 원본 픽셀과 1:1이 된다. */
    private static final int BADGE_HEIGHT = 18;
    /** 가장 긴 배지 폭에 맞춰 모든 줄의 글자 시작점을 같게 둔다. */
    private static final int BADGE_COLUMN_WIDTH = 80;
    private static final int BUTTON_WIDTH = 40;
    private static final int CONDITION_Y = 6;
    private static final int HOLDER_Y = 17;

    private final ActionScrollWidget scroll;
    private int renderedRevision = -1;

    public TitleListComponent(int width, int height)
    {
        super(0, 0, width, height);
        this.scroll = new ActionScrollWidget(width, height);
        this.addWidget(this.scroll);
        this.rebuild();
    }

    /** 서버에서 새 칭호 정보를 받으면 줄을 다시 만든다. 스크롤 위치는 유지한다. */
    private void rebuild()
    {
        double scrollAmount = this.scroll.scrollAmount();
        this.scroll.clearComponents();

        int contentWidth = getWidth() - SCROLL_BAR_SPACE;
        EmptyComponent content = new EmptyComponent(0, 0, contentWidth, 0);
        int rowY = 0;
        for (TitleType type : TitleType.values())
        {
            content.addComponent(new TitleRow(rowY, contentWidth, type));
            rowY += ROW_HEIGHT + ROW_GAP;
        }
        content.setHeight(rowY);

        this.scroll.addComponent(content);
        this.scroll.setScrollAmount(scrollAmount);
        this.renderedRevision = ClientTitles.getRevision();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                   float partialTick, int parentWidth, int parentHeight)
    {
        if (this.renderedRevision != ClientTitles.getRevision())
        {
            this.rebuild();
            this.updateParentPosition(getParentX(), getParentY(), parentWidth, parentHeight);
        }
    }

    private static class TitleRow extends EmptyComponent
    {
        private final TitleType type;
        private final Component holderLine;
        private final int holderColor;
        private final int textRight;

        TitleRow(int y, int width, TitleType type)
        {
            super(0, y, width, ROW_HEIGHT);
            this.type = type;

            Optional<ClientTitles.Entry> entry = ClientTitles.find(type);
            boolean hasHolder = entry.filter(ClientTitles.Entry::hasHolder).isPresent();
            boolean mine = entry.filter(ClientTitles.Entry::mine).isPresent();
            boolean equipped = ClientTitles.isEquipped(type);

            this.holderLine = createHolderLine(entry, hasHolder, mine && equipped);
            this.holderColor = hasHolder ? JobsTheme.CYAN : JobsTheme.MUTED;

            int textRight = width - PADDING;
            if (mine)
            {
                int buttonX = width - PADDING - BUTTON_WIDTH;
                int buttonY = (ROW_HEIGHT - JobsTheme.BUTTON_HEIGHT) / 2;
                this.addWidget(new EquipButton(buttonX, buttonY, type, equipped));
                textRight = buttonX - PADDING;
            }
            this.textRight = textRight;
        }

        private static Component createHolderLine(Optional<ClientTitles.Entry> entry, boolean hasHolder,
                                                  boolean equippedByMe)
        {
            if (!hasHolder)
            {
                return Component.literal("보유자 없음");
            }
            String line = "보유자: " + entry.get().holderName();
            if (equippedByMe)
            {
                line += " (장착 중)";
            }
            return Component.literal(line);
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                       float partialTick, int parentWidth, int parentHeight)
        {
            int x = getTotalX();
            int y = getTotalY();
            JobsTheme.texture(graphics, JobsTheme.Skin.INSET, x, y, getWidth(), getHeight());

            int textureWidth = this.type.getTextureWidth();
            int badgeWidth = textureWidth * BADGE_HEIGHT / TitleType.TEXTURE_HEIGHT;
            int badgeY = y + (getHeight() - BADGE_HEIGHT) / 2;
            graphics.blit(RenderPipelines.GUI_TEXTURED, this.type.getTexture(), x + PADDING, badgeY,
                    0.0F, 0.0F, badgeWidth, BADGE_HEIGHT,
                    textureWidth, TitleType.TEXTURE_HEIGHT, textureWidth, TitleType.TEXTURE_HEIGHT);

            int textX = x + PADDING + BADGE_COLUMN_WIDTH + PADDING;
            int textWidth = Math.max(1, x + this.textRight - textX);
            JobsTheme.text(graphics, Component.literal(this.type.getCondition()), textX, y + CONDITION_Y,
                    textWidth, JobsTheme.TEXT);
            JobsTheme.text(graphics, this.holderLine, textX, y + HOLDER_Y, textWidth, this.holderColor);
        }
    }

    private static class EquipButton extends CustomButtonWidget
    {
        private final boolean equipped;

        /** 장착 중이면 해제 단추, 아니면 장착 단추가 된다. 빈 ID를 보내면 서버가 장착을 푼다. */
        EquipButton(int x, int y, TitleType type, boolean equipped)
        {
            super(x, y, BUTTON_WIDTH, JobsTheme.BUTTON_HEIGHT, Component.literal(equipped ? "해제" : "장착"), null,
                    button -> NetworkManager.sendToServer(
                            new ServerboundEquipTitlePacket(equipped ? "" : type.getId())));
            this.equipped = equipped;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
        {
            boolean primary = !this.equipped;
            JobsTheme.button(graphics, getX(), getY(), getWidth(), getHeight(), this.active,
                    isHoveredOrFocused(), false, primary);
            JobsTheme.label(graphics, getMessage(), getX(), getY(), getWidth(), getHeight(), JobsTheme.TEXT);
        }
    }
}
