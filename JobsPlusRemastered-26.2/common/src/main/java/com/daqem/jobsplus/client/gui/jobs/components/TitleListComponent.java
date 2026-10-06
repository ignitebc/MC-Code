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
    /** 한 화면에 칭호 6개(2열 × 3줄)가 보이도록 칸 높이를 목록 높이에 맞춘다. */
    private static final int VISIBLE_ROWS = 3;
    /** 넓은 화면에서도 칸이 지나치게 커지지 않게 막는 높이 */
    private static final int MAX_ROW_HEIGHT = 60;
    /** 배지를 알아볼 수 있는 가장 낮은 칸 높이. 이보다 낮은 창에서는 스크롤한다. */
    private static final int MIN_ROW_HEIGHT = 50;
    private static final int ROW_GAP = 3;
    private static final int COLUMN_COUNT = 2;
    private static final int COLUMN_GAP = 6;
    /** 스크롤 막대가 들어갈 오른쪽 여백 */
    private static final int SCROLL_BAR_SPACE = 10;
    private static final int PADDING = 6;
    private static final int BUTTON_WIDTH = 40;
    /** 배지 위 여백 */
    private static final int BADGE_TOP = 4;
    /** 배지 아래에서 조건 줄까지의 간격 */
    private static final int CONDITION_GAP = 6;
    /** 조건 줄에서 보유자 줄까지의 간격 */
    private static final int LINE_HEIGHT = 13;
    /**
     * 칸 높이에서 배지를 뺀 나머지. 배지 위 여백 4, 조건 줄까지 6, 보유자 줄까지 13,
     * 보유자 글자 9, 아래 여백 4를 더한 값이다. 배지 아래에 조건·보유자를 두어 좁은 패널에서도
     * 배지를 칸 폭만큼 크게 그린다.
     */
    private static final int TEXT_BLOCK_HEIGHT = 36;

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

        int contentWidth = Math.max(COLUMN_COUNT + COLUMN_GAP, getWidth() - SCROLL_BAR_SPACE);
        int columnWidth = (contentWidth - COLUMN_GAP) / COLUMN_COUNT;
        int rowHeight = getRowHeight();
        int badgeHeight = rowHeight - TEXT_BLOCK_HEIGHT;
        EmptyComponent content = new EmptyComponent(0, 0, contentWidth, 0);
        TitleType[] types = TitleType.values();
        for (int index = 0; index < types.length; index++)
        {
            int column = index % COLUMN_COUNT;
            int row = index / COLUMN_COUNT;
            int columnX = column * (columnWidth + COLUMN_GAP);
            int width = column == COLUMN_COUNT - 1 ? contentWidth - columnX : columnWidth;
            int rowY = row * (rowHeight + ROW_GAP);
            content.addComponent(new TitleRow(columnX, rowY, width, rowHeight, badgeHeight, types[index]));
        }
        int rowCount = (types.length + COLUMN_COUNT - 1) / COLUMN_COUNT;
        content.setHeight(Math.max(0, rowCount * (rowHeight + ROW_GAP) - ROW_GAP));

        this.scroll.addComponent(content);
        this.scroll.setScrollAmount(scrollAmount);
        this.renderedRevision = ClientTitles.getRevision();
    }

    /** 목록 높이에 세 줄이 들어가는 칸 높이. 너무 크거나 작아지지 않게 상한과 하한으로 자른다. */
    private int getRowHeight()
    {
        int fittedHeight = (getHeight() - (VISIBLE_ROWS - 1) * ROW_GAP) / VISIBLE_ROWS;
        return Math.clamp(fittedHeight, MIN_ROW_HEIGHT, MAX_ROW_HEIGHT);
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
        private final int badgeHeight;
        private final int conditionY;
        private final int holderY;

        TitleRow(int x, int y, int width, int rowHeight, int badgeHeight, TitleType type)
        {
            super(x, y, width, rowHeight);
            this.type = type;
            this.badgeHeight = badgeHeight;
            this.conditionY = BADGE_TOP + badgeHeight + CONDITION_GAP;
            this.holderY = this.conditionY + LINE_HEIGHT;

            Optional<ClientTitles.Entry> entry = ClientTitles.find(type);
            boolean hasHolder = entry.filter(ClientTitles.Entry::hasHolder).isPresent();
            boolean mine = entry.filter(ClientTitles.Entry::mine).isPresent();
            boolean equipped = ClientTitles.isEquipped(type);

            this.holderLine = createHolderLine(entry, hasHolder, mine && equipped);
            this.holderColor = hasHolder ? JobsTheme.CYAN : JobsTheme.MUTED;

            this.textRight = width - PADDING;
            if (mine)
            {
                int buttonX = width - PADDING - BUTTON_WIDTH;
                int buttonY = (BADGE_TOP * 2 + badgeHeight - JobsTheme.BUTTON_HEIGHT) / 2;
                this.addWidget(new EquipButton(buttonX, buttonY, type, equipped));
            }
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

            // 배지 칸 폭은 그림 비율대로 칸 높이에서 정하되, 장착 단추 자리를 침범하지 않게 줄인다.
            int textureWidth = this.type.getTextureWidth();
            int fittedBadgeWidth = this.badgeHeight * textureWidth / TitleType.TEXTURE_HEIGHT;
            int badgeWidth = Math.max(1, Math.min(fittedBadgeWidth, getWidth() - PADDING * 3 - BUTTON_WIDTH));
            int drawnBadgeHeight = Math.max(1, badgeWidth * TitleType.TEXTURE_HEIGHT / textureWidth);
            int badgeY = y + BADGE_TOP + (this.badgeHeight - drawnBadgeHeight) / 2;
            graphics.blit(RenderPipelines.GUI_TEXTURED, this.type.getTexture(), x + PADDING, badgeY,
                    0.0F, 0.0F, badgeWidth, drawnBadgeHeight,
                    textureWidth, TitleType.TEXTURE_HEIGHT, textureWidth, TitleType.TEXTURE_HEIGHT);

            int textX = x + PADDING;
            int textWidth = Math.max(1, x + this.textRight - textX);
            JobsTheme.text(graphics, Component.literal(this.type.getCondition()), textX, y + this.conditionY,
                    textWidth, JobsTheme.TEXT);
            JobsTheme.text(graphics, this.holderLine, textX, y + this.holderY, textWidth, this.holderColor);
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
