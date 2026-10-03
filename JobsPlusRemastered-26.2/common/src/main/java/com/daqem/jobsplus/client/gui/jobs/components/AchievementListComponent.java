package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.achievement.AchievementCatalog;
import com.daqem.jobsplus.achievement.AchievementDefinition;
import com.daqem.jobsplus.client.achievement.ClientAchievements;
import com.daqem.jobsplus.client.achievement.AchievementDisplayText;
import com.daqem.jobsplus.client.gui.jobs.widgets.ActionScrollWidget;
import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.jobsplus.networking.s2c.ClientboundAchievementPacket;
import com.daqem.uilib.gui.component.EmptyComponent;
import com.daqem.uilib.gui.widget.CustomButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** 분류별 목록, 복합 목표의 개별 진행도, 판정 대상 목록과 완료 버튼을 보여 준다. */
public final class AchievementListComponent extends EmptyComponent
{
    private static final int CATEGORY_HEIGHT = 16;
    private static final int BODY_Y = CATEGORY_HEIGHT + 4;
    private static final int ROW_HEIGHT = 28;
    private static final int FOOTER_HEIGHT = 23;
    private static final int SECTION_GAP = 4;
    private static final int SUMMARY_COLUMN_MIN_WIDTH = 88;
    private static final String[] CATEGORY_NAMES = {"직업", "생활", "탐험", "경제", "장비", "펫", "전투"};

    private final ActionScrollWidget list;
    private final ActionScrollWidget detail;
    private final ClaimButton claimButton;
    private final int listWidth;
    private final int detailX;
    private String renderedCategory;
    private List<String> renderedOrder = List.of();
    private String renderedId;
    private ClientboundAchievementPacket renderedSnapshot;

    public AchievementListComponent(int width, int height)
    {
        super(0, 0, width, height);
        this.listWidth = Math.max(1, Math.min(180, width * 42 / 100));
        this.detailX = this.listWidth + 4;
        int bodyHeight = Math.max(1, height - BODY_Y);
        int detailWidth = Math.max(1, width - this.detailX);
        this.list = new ActionScrollWidget(this.listWidth, bodyHeight);
        this.list.setY(BODY_Y);
        addWidget(this.list);
        this.detail = new ActionScrollWidget(detailWidth, Math.max(1, bodyHeight - FOOTER_HEIGHT));
        this.detail.setX(this.detailX);
        this.detail.setY(BODY_Y);
        addWidget(this.detail);
        this.claimButton = new ClaimButton(this.detailX + 3, Math.max(BODY_Y, height - FOOTER_HEIGHT + 3),
                Math.max(1, detailWidth - 6));
        addWidget(this.claimButton);
        // 탭 사이 1픽셀 간격을 빼고 분류 수만큼 나눈다.
        int categoryWidth = Math.max(1, (width - (CATEGORY_NAMES.length - 1)) / CATEGORY_NAMES.length);
        for (int index = 0; index < CATEGORY_NAMES.length; index++)
        {
            String category = Character.toString((char) ('A' + index));
            addWidget(new CategoryButton(index * (categoryWidth + 1), 0, categoryWidth,
                    category, Component.literal(CATEGORY_NAMES[index])));
        }
        refresh();
        ClientAchievements.poll();
    }

    private void refresh()
    {
        ClientboundAchievementPacket snapshot = ClientAchievements.getSnapshot();
        boolean categoryChanged = !Objects.equals(this.renderedCategory, ClientAchievements.category);
        List<AchievementDefinition> rows = sortedRows(ClientAchievements.category, snapshot);
        List<String> order = rows.stream().map(AchievementDefinition::id).toList();
        // 진행 상황은 1초마다 새로 받으므로, 순서가 실제로 바뀐 때만 목록을 다시 만든다.
        if (categoryChanged || !order.equals(this.renderedOrder))
        {
            // 분류를 바꾸면 맨 위 업적을 고른다. 보상 수령 대기 업적이 있으면 그 업적이 선택된다.
            if (categoryChanged && !ClientAchievements.selectedId.startsWith(ClientAchievements.category))
            {
                ClientAchievements.selectedId = order.isEmpty() ? ClientAchievements.category + "01" : order.getFirst();
            }
            double previousScroll = categoryChanged ? 0 : this.list.scrollAmount();
            this.list.clearComponents();
            int rowWidth = Math.max(1, this.listWidth - 10);
            EmptyComponent content = new EmptyComponent(0, 0, rowWidth, 0);
            int y = 0;
            for (AchievementDefinition definition : rows)
            {
                content.addWidget(new AchievementRow(y, rowWidth, definition));
                y += ROW_HEIGHT;
            }
            content.setHeight(y);
            this.list.addComponent(content);
            this.list.setScrollAmount(previousScroll);
            this.renderedCategory = ClientAchievements.category;
            this.renderedOrder = order;
        }
        if (!Objects.equals(this.renderedId, ClientAchievements.selectedId) || this.renderedSnapshot != snapshot)
        {
            double previousScroll = this.detail.scrollAmount();
            if (!Objects.equals(this.renderedId, ClientAchievements.selectedId))
            {
                previousScroll = 0;
            }
            this.detail.clearComponents();
            this.detail.addComponent(buildDetail(snapshot));
            this.detail.setScrollAmount(previousScroll);
            this.renderedId = ClientAchievements.selectedId;
            this.renderedSnapshot = snapshot;
        }
        positionUpdated();
    }

    private EmptyComponent buildDetail(ClientboundAchievementPacket snapshot)
    {
        int width = Math.max(1, this.detail.getWidth() - 10);
        EmptyComponent content = new EmptyComponent(0, 0, width, 0);
        AchievementDefinition definition = AchievementCatalog.get(ClientAchievements.selectedId);
        if (definition == null)
        {
            return content;
        }
        int y = 3;
        y = addText(content, y, width, definition.name(), JobsTheme.CYAN);
        y = addText(content, y, width, status(definition, snapshot), statusColor(definition, snapshot));
        y += SECTION_GAP;

        // 폭이 충분할 때만 요약을 나란히 배치해 작은 화면에서도 보상 문구를 읽을 수 있게 한다.
        boolean twoColumns = width >= SUMMARY_COLUMN_MIN_WIDTH * 2 + SECTION_GAP;
        int summaryWidth = twoColumns ? (width - SECTION_GAP) / 2 : width;
        DetailSection difficulty = new DetailSection(0, y, summaryWidth, "난이도");
        difficulty.addLine("★".repeat(definition.difficulty()), JobsTheme.WARNING);
        content.addComponent(difficulty);

        int rewardX = twoColumns ? summaryWidth + SECTION_GAP : 0;
        int rewardY = twoColumns ? y : y + difficulty.getHeight() + SECTION_GAP;
        int rewardWidth = twoColumns ? width - rewardX : width;
        DetailSection reward = new DetailSection(rewardX, rewardY, rewardWidth, "보상");
        reward.addLine("다이아몬드 " + definition.diamonds() + "개", JobsTheme.TEXT);
        content.addComponent(reward);
        y = Math.max(y + difficulty.getHeight(), rewardY + reward.getHeight()) + SECTION_GAP;

        DetailSection conditions = new DetailSection(0, y, width, "달성 조건");
        if (!definition.parents().isEmpty())
        {
            String parents = String.join(" · ", definition.parents().stream()
                    .map(AchievementDisplayText::achievementName).toList());
            if (definition.requiredParents() < definition.parents().size())
            {
                parents = parents + " 중 " + definition.requiredParents() + "개";
            }
            conditions.addLine("선행 업적: " + parents, JobsTheme.MUTED);
        }
        for (AchievementDefinition.Objective objective : definition.objectives())
        {
            long current = snapshot.values().getOrDefault(objective.key(), 0L);
            int color = JobsTheme.TEXT;
            if (current >= objective.target())
            {
                color = JobsTheme.SUCCESS;
            }
            String label = objective.label();
            if (objective.key().startsWith("kill:"))
            {
                String entityId = objective.key().substring("kill:".length());
                String fallback = label.replace(" 처치", "");
                if (fallback.equals(entityId.substring(entityId.indexOf(':') + 1)))
                {
                    fallback = "적대 몬스터";
                }
                label = AchievementDisplayText.resourceName("entity", entityId, fallback) + " 처치";
            }
            String quantity = current + " / " + objective.target();
            if (objective.key().equals("walk_cm") || objective.key().equals("elytra_cm"))
            {
                quantity = String.format(Locale.ROOT, "%.2f / %.0f km", current / 100000.0D, objective.target() / 100000.0D);
                label = label.replace("(cm)", "");
            }
            conditions.addLine(label + ": " + quantity, color);
        }
        content.addComponent(conditions);
        y += conditions.getHeight() + SECTION_GAP;

        DetailSection guidance = new DetailSection(0, y, width, "상세 안내");
        guidance.addLine(AchievementDisplayText.details(definition), JobsTheme.MUTED);
        if (definition.id().equals("C05"))
        {
            guidance.addLine("현재 서버의 오버월드 방문 대상:", JobsTheme.CYAN);
            for (String biome : snapshot.overworldBiomes())
            {
                guidance.addLine("• " + AchievementDisplayText.resourceName("biome", biome, "추가 생물 군계"), JobsTheme.MUTED);
            }
        }
        if (definition.id().equals("C06"))
        {
            guidance.addLine("현재 바닐라 발전 과제의 방문 목록:", JobsTheme.CYAN);
            for (String biome : snapshot.adventureBiomes())
            {
                guidance.addLine("• " + AchievementDisplayText.resourceName("biome", biome, "추가 생물 군계"), JobsTheme.MUTED);
            }
        }
        content.addComponent(guidance);
        y += guidance.getHeight();
        content.setHeight(y + 4);
        return content;
    }

    private static int addText(EmptyComponent content, int y, int width, String text, int color)
    {
        for (String line : wrap(text, Math.max(1, width - 8)))
        {
            content.addComponent(new TextLine(y, width, Component.literal(line), color));
            y += 11;
        }
        return y;
    }

    /** 한글 설명과 대상 목록이 잘리지 않도록 실제 글꼴 폭으로 줄을 나눈다. */
    private static List<String> wrap(String text, int width)
    {
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (int codePoint : text.codePoints().toArray())
        {
            String character = new String(Character.toChars(codePoint));
            if (codePoint == '\n')
            {
                lines.add(line.toString());
                line.setLength(0);
                continue;
            }
            if (!line.isEmpty() && Minecraft.getInstance().font.width(line + character) > width)
            {
                lines.add(line.toString());
                line.setLength(0);
            }
            line.append(character);
        }
        if (!line.isEmpty())
        {
            lines.add(line.toString());
        }
        return lines;
    }

    /**
     * 분류의 업적을 보상 수령 대기, 진행 중·잠김, 보상 수령 완료 순서로 돌려준다.
     * 같은 상태끼리는 카탈로그 순서를 지킨다.
     */
    private static List<AchievementDefinition> sortedRows(String category, ClientboundAchievementPacket snapshot)
    {
        List<AchievementDefinition> rows = new ArrayList<>();
        for (AchievementDefinition definition : AchievementCatalog.all())
        {
            if (definition.id().startsWith(category))
            {
                rows.add(definition);
            }
        }
        // List.sort는 안정 정렬이므로 같은 순위 안의 카탈로그 순서가 유지된다.
        rows.sort(Comparator.comparingInt(definition -> listRank(definition, snapshot)));
        return rows;
    }

    /** 상태 문구와 같은 기준이다. 보상을 받으면 달성도 끝난 것이므로 수령 여부를 먼저 본다. */
    private static int listRank(AchievementDefinition definition, ClientboundAchievementPacket snapshot)
    {
        if (snapshot.claimed().contains(definition.id()))
        {
            return 2;
        }
        if (snapshot.completed().contains(definition.id()))
        {
            return 0;
        }
        return 1;
    }

    private static boolean unlocked(AchievementDefinition definition, ClientboundAchievementPacket snapshot)
    {
        int parents = 0;
        for (String parent : definition.parents())
        {
            if (snapshot.completed().contains(parent))
            {
                parents++;
            }
        }
        if (parents >= definition.requiredParents())
        {
            return true;
        }
        return false;
    }

    private static String status(AchievementDefinition definition, ClientboundAchievementPacket snapshot)
    {
        if (snapshot.claimed().contains(definition.id()))
        {
            return "보상 수령 완료";
        }
        if (snapshot.completed().contains(definition.id()))
        {
            return "달성 · 보상 수령 대기";
        }
        if (!unlocked(definition, snapshot))
        {
            return "선행 업적 달성 후 해금";
        }
        return "진행 중";
    }

    private static int statusColor(AchievementDefinition definition, ClientboundAchievementPacket snapshot)
    {
        if (snapshot.completed().contains(definition.id()))
        {
            return JobsTheme.SUCCESS;
        }
        if (!unlocked(definition, snapshot))
        {
            return JobsTheme.DISABLED;
        }
        return JobsTheme.MUTED;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                   float partialTick, int parentWidth, int parentHeight)
    {
        ClientAchievements.poll();
        ClientboundAchievementPacket snapshot = ClientAchievements.getSnapshot();
        if (!Objects.equals(this.renderedCategory, ClientAchievements.category)
                || !Objects.equals(this.renderedId, ClientAchievements.selectedId) || this.renderedSnapshot != snapshot)
        {
            refresh();
        }
        JobsTheme.texture(graphics, JobsTheme.Skin.INSET, getTotalX(), getTotalY() + BODY_Y,
                this.listWidth, Math.max(1, getHeight() - BODY_Y));
        JobsTheme.texture(graphics, JobsTheme.Skin.INSET, getTotalX() + this.detailX, getTotalY() + BODY_Y,
                Math.max(1, getWidth() - this.detailX), Math.max(1, getHeight() - BODY_Y));
    }

    private static class DetailSection extends EmptyComponent
    {
        private final int dividerY;
        private int nextLineY;

        DetailSection(int x, int y, int width, String title)
        {
            super(x, y, width, 0);
            this.dividerY = addText(this, 4, width, title, JobsTheme.CYAN) + 1;
            this.nextLineY = this.dividerY + 4;
            setHeight(this.nextLineY + 4);
        }

        void addLine(String text, int color)
        {
            if (text.isBlank())
            {
                return;
            }
            this.nextLineY = addText(this, this.nextLineY, getWidth(), text, color);
            setHeight(this.nextLineY + 4);
            this.nextLineY += 2;
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                       float partialTick, int parentWidth, int parentHeight)
        {
            JobsTheme.texture(graphics, JobsTheme.Skin.INSET, getTotalX(), getTotalY(), getWidth(), getHeight());
            if (getWidth() > 8)
            {
                graphics.fill(getTotalX() + 4, getTotalY() + this.dividerY,
                        getTotalX() + getWidth() - 4, getTotalY() + this.dividerY + 1, JobsTheme.DIVIDER);
            }
        }
    }

    private static class TextLine extends EmptyComponent
    {
        private final Component text;
        private final int color;

        TextLine(int y, int width, Component text, int color)
        {
            super(0, y, width, 11);
            this.text = text;
            this.color = color;
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                       float partialTick, int parentWidth, int parentHeight)
        {
            JobsTheme.text(graphics, text, getTotalX() + 4, getTotalY() + 1, Math.max(1, getWidth() - 8), color);
        }
    }

    private static class CategoryButton extends CustomButtonWidget
    {
        private final String category;

        CategoryButton(int x, int y, int width, String category, Component title)
        {
            super(x, y, width, CATEGORY_HEIGHT, title, null, button -> ClientAchievements.category = category);
            this.category = category;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
        {
            JobsTheme.tab(graphics, getX(), getY(), getWidth(), getHeight(), isHoveredOrFocused(),
                    this.category.equals(ClientAchievements.category));
            JobsTheme.label(graphics, getMessage(), getX(), getY(), getWidth(), getHeight(), JobsTheme.TEXT);
        }
    }

    private static class AchievementRow extends CustomButtonWidget
    {
        private final AchievementDefinition definition;

        AchievementRow(int y, int width, AchievementDefinition definition)
        {
            super(0, y, width, ROW_HEIGHT - 2, Component.literal(definition.name()), null,
                    button -> ClientAchievements.selectedId = definition.id());
            this.definition = definition;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
        {
            ClientboundAchievementPacket snapshot = ClientAchievements.getSnapshot();
            JobsTheme.button(graphics, getX(), getY(), getWidth(), getHeight(), true, isHoveredOrFocused(),
                    definition.id().equals(ClientAchievements.selectedId), false);
            JobsTheme.text(graphics, Component.literal(definition.name()),
                    getX() + 4, getY() + 4, Math.max(1, getWidth() - 8), JobsTheme.TEXT);
            JobsTheme.text(graphics, Component.literal(status(definition, snapshot)),
                    getX() + 4, getY() + 15, Math.max(1, getWidth() - 8), statusColor(definition, snapshot));
        }
    }

    private static class ClaimButton extends CustomButtonWidget
    {
        ClaimButton(int x, int y, int width)
        {
            super(x, y, width, JobsTheme.BUTTON_HEIGHT, Component.literal("완료 · 보상 수령"), null,
                    button -> ClientAchievements.claim(ClientAchievements.selectedId));
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
        {
            ClientboundAchievementPacket snapshot = ClientAchievements.getSnapshot();
            this.active = snapshot.completed().contains(ClientAchievements.selectedId)
                    && !snapshot.claimed().contains(ClientAchievements.selectedId);
            JobsTheme.button(graphics, getX(), getY(), getWidth(), getHeight(), active, isHoveredOrFocused(), false, true);
            JobsTheme.label(graphics, getMessage(), getX(), getY(), getWidth(), getHeight(), JobsTheme.TEXT);
        }
    }
}
