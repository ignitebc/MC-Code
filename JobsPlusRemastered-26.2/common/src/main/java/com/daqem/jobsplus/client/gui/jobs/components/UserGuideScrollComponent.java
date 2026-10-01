package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.client.gui.jobs.JobsScreenState;
import com.daqem.jobsplus.client.gui.jobs.tab.UserGuideTab;
import com.daqem.jobsplus.client.gui.jobs.widgets.GuideScrollWidget;
import com.daqem.jobsplus.client.gui.jobs.widgets.UserGuideTabWidget;
import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.uilib.gui.component.EmptyComponent;
import com.daqem.uilib.gui.component.text.multiline.MultiLineTextComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.Map;

/** 주제별 탭마다 일반 안내와 중요 안내를 좌우 두 칸으로 보여 준다. */
public class UserGuideScrollComponent extends EmptyComponent
{
    private static final Component GENERAL_TITLE = Component.literal("일반 안내");
    private static final Component IMPORTANT_TITLE = Component.literal("중요 안내");

    private static final int TAB_GAP = 1;
    private static final int COLUMN_GAP = 8;
    private static final int COLUMN_TITLE_Y = JobsTheme.TAB_HEIGHT + 4;
    private static final int TITLE_HEIGHT = 12;
    private static final int CONTENT_Y = COLUMN_TITLE_Y + TITLE_HEIGHT;

    private final JobsScreenState state;
    private final int columnWidth;
    private final Map<UserGuideTab, GuideColumns> columns = new EnumMap<>(UserGuideTab.class);
    private UserGuideTab cachedTab;

    public UserGuideScrollComponent(JobsScreenState state, int width, int height)
    {
        super(0, 0, width, height);
        this.state = state;
        this.columnWidth = (getWidth() - COLUMN_GAP) / 2;
        this.addTabWidgets();
        this.addGuideColumns();

        this.cachedTab = state.getSelectedUserGuideTab();
        this.updateColumnVisibility();
    }

    /** 화면 너비를 남김없이 나누어 모든 안내 탭을 한 줄에 배치한다. */
    private void addTabWidgets()
    {
        UserGuideTab[] tabs = UserGuideTab.values();
        int totalGap = TAB_GAP * (tabs.length - 1);
        int availableWidth = Math.max(1, getWidth() - totalGap);
        int baseTabWidth = availableWidth / tabs.length;
        int remainingWidth = availableWidth % tabs.length;
        int tabX = 0;

        for (int index = 0; index < tabs.length; index++)
        {
            int tabWidth = baseTabWidth;
            if (index < remainingWidth)
            {
                tabWidth++;
            }

            UserGuideTab tab = tabs[index];
            this.addWidget(new UserGuideTabWidget(this.state, tab, tabX, tabWidth));
            tabX += tabWidth + TAB_GAP;
        }
    }

    /** 모든 탭의 두 스크롤을 한 번만 만들고 표시 여부만 바꾸어 탭별 스크롤 위치를 보존한다. */
    private void addGuideColumns()
    {
        int columnHeight = Math.max(1, getHeight() - CONTENT_Y);
        for (UserGuideTab tab : UserGuideTab.values())
        {
            UserGuidePages.GuidePage page = UserGuidePages.get(tab);
            GuideScrollWidget general = this.addColumn(0, columnHeight, page.general(), false);
            GuideScrollWidget important = this.addColumn(
                    this.columnWidth + COLUMN_GAP, columnHeight, page.important(), true);
            this.columns.put(tab, new GuideColumns(general, important));
        }
    }

    /** 안내 한 칸을 따로 굴러가는 스크롤 영역으로 만들어 붙인다. */
    private GuideScrollWidget addColumn(int x, int height, String guide, boolean important)
    {
        GuideScrollWidget scrollWidget = new GuideScrollWidget(this.columnWidth, height);
        scrollWidget.setX(x);
        scrollWidget.setY(CONTENT_Y);

        int textWidth = Math.max(1, this.columnWidth - 10);
        float textScale = 0.70F;

        /*
         * 화면에 실제로 표시되는 너비는 wrapWidth × textScale이므로
         * 스케일만큼 역보정한다.
         */
        int wrapWidth = Math.max(1, (int) Math.ceil(textWidth / textScale));
        int textColor = important ? JobsTheme.ERROR : JobsTheme.TEXT;
        ScaledMultiLineTextComponent guideText = new ScaledMultiLineTextComponent(
                0,
                0,
                wrapWidth,
                createGuideComponent(guide, important),
                textColor,
                textScale
        );

        EmptyComponent guideContainer = new EmptyComponent(0, 0, textWidth, 0);
        guideContainer.addComponent(guideText);
        guideContainer.setHeight(guideText.getScaledHeight());

        scrollWidget.addComponent(guideContainer);
        this.addWidget(scrollWidget);
        return scrollWidget;
    }

    private void updateColumnVisibility()
    {
        for (Map.Entry<UserGuideTab, GuideColumns> entry : this.columns.entrySet())
        {
            boolean selected = entry.getKey() == this.cachedTab;
            GuideColumns guideColumns = entry.getValue();
            guideColumns.general().visible = selected;
            guideColumns.general().active = selected;
            guideColumns.important().visible = selected;
            guideColumns.important().active = selected;
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY,
                                   float partialTick, int parentWidth, int parentHeight)
    {
        if (this.cachedTab != this.state.getSelectedUserGuideTab())
        {
            this.cachedTab = this.state.getSelectedUserGuideTab();
            this.updateColumnVisibility();
        }

        int totalX = getTotalX();
        int totalY = getTotalY();
        JobsTheme.text(guiGraphics, GENERAL_TITLE, totalX, totalY + COLUMN_TITLE_Y,
                this.columnWidth, JobsTheme.CYAN);
        JobsTheme.text(guiGraphics, IMPORTANT_TITLE,
                totalX + this.columnWidth + COLUMN_GAP, totalY + COLUMN_TITLE_Y,
                this.columnWidth, JobsTheme.ERROR);

        int dividerX = totalX + this.columnWidth + COLUMN_GAP / 2;
        guiGraphics.fill(dividerX, totalY + COLUMN_TITLE_Y,
                dividerX + 1, totalY + getHeight(), JobsTheme.DIVIDER);
    }

    private static Component createGuideComponent(String page, boolean important)
    {
        String guide = page.strip();
        if (important)
        {
            return Component.literal(guide).withStyle(ChatFormatting.RED);
        }
        return styleSections(guide);
    }

    private static Component styleSections(String text)
    {
        MutableComponent result = Component.empty();
        String[] lines = text.split("\n", -1);
        for (int index = 0; index < lines.length; index++)
        {
            MutableComponent line = Component.literal(lines[index]);
            String strippedLine = lines[index].stripLeading();
            if (strippedLine.startsWith("■") || strippedLine.startsWith("★"))
            {
                // 축소된 한글은 굵게 표시하면 획이 겹쳐 보여 색상만으로 강조한다.
                line.withStyle(ChatFormatting.AQUA);
            }
            result.append(line);
            if (index < lines.length - 1)
            {
                result.append("\n");
            }
        }
        return result;
    }

    private record GuideColumns(GuideScrollWidget general, GuideScrollWidget important)
    {
    }

    /** MultiLineTextComponent에 출력 배율을 적용하기 위한 컴포넌트. */
    private static final class ScaledMultiLineTextComponent extends MultiLineTextComponent
    {
        private static final int BASE_LINE_HEIGHT = 9;
        private static final int LINE_SPACING = 2;

        private final float scale;

        public ScaledMultiLineTextComponent(int x, int y, int maxWidth, Component text, int color, float scale)
        {
            super(x, y, maxWidth, text, color);
            this.scale = scale <= 0.0F ? 1.0F : scale;
        }

        public int getScaledHeight()
        {
            int rawHeight = getLines().size() * BASE_LINE_HEIGHT
                    + Math.max(0, getLines().size() - 1) * LINE_SPACING;
            return (int) Math.ceil(rawHeight * this.scale);
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                       float delta, int x, int y)
        {
            Object pose = graphics.pose();
            boolean pushed = invokeNoArg(pose, "pushPose")
                    || invokeNoArg(pose, "push")
                    || invokeNoArg(pose, "pushMatrix");

            if (!pushed)
            {
                super.extractRenderState(graphics, mouseX, mouseY, delta, x, y);
                return;
            }

            float totalX = (float) getTotalX();
            float totalY = (float) getTotalY();
            if (!invoke2f(pose, "translate", totalX, totalY))
            {
                invoke3f(pose, "translate", totalX, totalY, 0.0F);
            }

            if (!invoke2f(pose, "scale", this.scale, this.scale))
            {
                invoke3f(pose, "scale", this.scale, this.scale, 1.0F);
            }

            for (int index = 0; index < getLines().size(); index++)
            {
                graphics.text(
                        getFont(),
                        getLines().get(index),
                        0,
                        index * (BASE_LINE_HEIGHT + LINE_SPACING),
                        getColor(),
                        isDrawShadow()
                );
            }

            if (!invokeNoArg(pose, "popPose") && !invokeNoArg(pose, "pop"))
            {
                invokeNoArg(pose, "popMatrix");
            }
        }

        private static boolean invokeNoArg(Object target, String methodName)
        {
            try
            {
                Method method = target.getClass().getMethod(methodName);
                method.invoke(target);
                return true;
            }
            catch (ReflectiveOperationException ignored)
            {
                return false;
            }
        }

        private static boolean invoke2f(Object target, String methodName, float first, float second)
        {
            try
            {
                Method method = target.getClass().getMethod(methodName, float.class, float.class);
                method.invoke(target, first, second);
                return true;
            }
            catch (ReflectiveOperationException ignored)
            {
                return false;
            }
        }

        private static boolean invoke3f(Object target, String methodName,
                                        float first, float second, float third)
        {
            try
            {
                Method method = target.getClass().getMethod(
                        methodName,
                        float.class,
                        float.class,
                        float.class
                );
                method.invoke(target, first, second, third);
                return true;
            }
            catch (ReflectiveOperationException ignored)
            {
                return false;
            }
        }
    }
}
