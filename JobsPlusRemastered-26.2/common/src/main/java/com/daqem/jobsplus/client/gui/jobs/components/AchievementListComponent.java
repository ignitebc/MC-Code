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
    private static final String[] CATEGORY_NAMES = {"직업", "생활", "탐험", "경제", "장비", "펫"};

    private final ActionScrollWidget list;
    private final ActionScrollWidget detail;
    private final ClaimButton claimButton;
    private final int listWidth;
    private final int detailX;
    private String renderedCategory;
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
        int categoryWidth = Math.max(1, (width - 5) / 6);
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
        if (!Objects.equals(this.renderedCategory, ClientAchievements.category))
        {
            if (!ClientAchievements.selectedId.startsWith(ClientAchievements.category))
            {
                ClientAchievements.selectedId = ClientAchievements.category + "01";
            }
            this.list.clearComponents();
            int rowWidth = Math.max(1, this.listWidth - 10);
            EmptyComponent content = new EmptyComponent(0, 0, rowWidth, 0);
            int y = 0;
            for (AchievementDefinition definition : AchievementCatalog.all())
            {
                if (definition.id().startsWith(ClientAchievements.category))
                {
                    content.addWidget(new AchievementRow(y, rowWidth, definition));
                    y += ROW_HEIGHT;
                }
            }
            content.setHeight(y);
            this.list.addComponent(content);
            this.list.setScrollAmount(0);
            this.renderedCategory = ClientAchievements.category;
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
        y = addText(content, y, width, "★".repeat(definition.difficulty()) + " · 다이아몬드 " + definition.diamonds() + "개", JobsTheme.TEXT);
        y = addText(content, y, width, status(definition, snapshot), statusColor(definition, snapshot));
        if (!definition.parents().isEmpty())
        {
            String parents = String.join(" · ", definition.parents().stream()
                    .map(AchievementDisplayText::achievementName).toList());
            if (definition.requiredParents() < definition.parents().size())
            {
                parents = parents + " 중 " + definition.requiredParents() + "개";
            }
            y = addText(content, y, width, "선행: " + parents, JobsTheme.MUTED);
        }
        y += 4;
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
            y = addText(content, y, width, label + ": " + quantity, color);
        }
        y += 5;
        y = addText(content, y, width, AchievementDisplayText.details(definition), JobsTheme.MUTED);
        if (definition.id().equals("C05"))
        {
            y += 4;
            y = addText(content, y, width, "현재 서버의 오버월드 방문 대상:", JobsTheme.CYAN);
            for (String biome : snapshot.overworldBiomes())
            {
                y = addText(content, y, width, "• " + AchievementDisplayText.resourceName("biome", biome, "추가 생물 군계"), JobsTheme.MUTED);
            }
        }
        if (definition.id().equals("C06"))
        {
            y += 4;
            y = addText(content, y, width, "현재 바닐라 발전 과제의 방문 목록:", JobsTheme.CYAN);
            for (String biome : snapshot.adventureBiomes())
            {
                y = addText(content, y, width, "• " + AchievementDisplayText.resourceName("biome", biome, "추가 생물 군계"), JobsTheme.MUTED);
            }
        }
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
