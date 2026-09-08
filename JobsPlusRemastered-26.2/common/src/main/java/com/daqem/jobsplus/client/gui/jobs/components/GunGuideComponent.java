package com.daqem.jobsplus.client.gui.jobs.components;

import com.daqem.jobsplus.client.gunguide.TaczCatalog;
import com.daqem.jobsplus.client.gunguide.TaczCatalog.Entry;
import com.daqem.jobsplus.client.gunguide.TaczCatalog.Kind;
import com.daqem.jobsplus.client.gui.jobs.widgets.ActionScrollWidget;
import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.jobsplus.client.gui.theme.JobsEditBox;
import net.minecraft.util.FormattedCharSequence;
import java.util.Locale;
import java.text.Normalizer;
import com.daqem.uilib.gui.component.EmptyComponent;
import com.daqem.uilib.gui.widget.CustomButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;

import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

/** Browse only: selecting a card never crafts, equips or sends a packet. */
public class GunGuideComponent extends EmptyComponent {
    private final TaczCatalog.Snapshot catalog;
    private final Map<String, Entry> entries;
    private final int listWidth;
    private final int detailX;
    private final ActionScrollWidget listScroll;
    private final ActionScrollWidget detailScroll;
    private final JobsEditBox searchInput;
    private final ActionScrollWidget searchScroll;
    private String searchQuery = "";
    private String renderedQuery;
    private String revealEntry;
    private final Map<String, Integer> cardRows = new java.util.HashMap<>();
    private boolean searchOpen;
    private Kind category = Kind.GUN;
    private Kind renderedCategory;
    private Entry selected;
    private Entry renderedSelection;

    public GunGuideComponent(int width, int height) {
        super(0, 0, width, height);
        catalog = TaczCatalog.load();
        entries = catalog.entries().stream().collect(Collectors.toMap(Entry::key, entry -> entry));
        listWidth = (width - 8) * 53 / 100;
        detailX = listWidth + 8;
        int categoryWidth = (listWidth - 4) / 3;
        for (Kind kind : Kind.values()) {
            addWidget(new GuideButton(kind.ordinal() * (categoryWidth + 2), 0, categoryWidth, 16,
                    Component.literal(kind.label), ItemStack.EMPTY, () -> category == kind, () -> selectCategory(kind)));
        }
        listScroll = new ActionScrollWidget(listWidth, Math.max(1, height - 21));
        listScroll.setY(21);
        detailScroll = new ActionScrollWidget(width - detailX, height);
        detailScroll.setX(detailX);
        addWidget(listScroll);
        addWidget(detailScroll);
        searchScroll = new ActionScrollWidget(width - detailX, Math.min(height, 170));
        searchScroll.setX(detailX);
        searchScroll.visible = false;
        addWidget(searchScroll);
        int searchWidth = Math.min(155, width - detailX);
        searchInput = new JobsEditBox(Minecraft.getInstance().font, width - searchWidth, -17,
                searchWidth, 14, Component.literal("총기 도감 검색")) {
            @Override
            public void setFocused(boolean focused) {
                super.setFocused(focused);
                if (focused && !searchQuery.isEmpty()) {
                    searchOpen = true;
                }
            }
        };
        searchInput.setMaxLength(64);
        searchInput.setHint(Component.literal("총기 · 탄약 · 파츠 검색"));
        searchInput.setResponder(value -> {
            searchQuery = normalize(value);
            searchOpen = !searchQuery.isEmpty();
        });
        addWidget(searchInput);
        selected = catalog.entries().stream().filter(entry -> entry.kind() == Kind.GUN).findFirst()
                .orElse(catalog.entries().isEmpty() ? null : catalog.entries().getFirst());
        refresh();
    }

    private void selectCategory(Kind kind) {
        category = kind;
        searchOpen = false;
        searchInput.setFocused(false);
    }

    private void navigateTo(Entry entry) {
        selected = entry;
        selectCategory(entry.kind());
        revealEntry = entry.key();
    }

    private void refresh() {
        if (renderedCategory != category) {
            cardRows.clear();
            listScroll.clearComponents();
            listScroll.setScrollAmount(0);
            EmptyComponent content = new EmptyComponent(0, 0, listWidth - 10, 0);
            List<Entry> visible = catalog.entries().stream().filter(entry -> entry.kind() == category).toList();
            int columns = 3;
            int cardWidth = (content.getWidth() - (columns - 1) * 4) / columns;
            int rowY = 0;
            for (int first = 0; first < visible.size(); first += columns) {
                int rowHeight = 0;
                for (int i = first; i < Math.min(first + columns, visible.size()); i++) {
                    rowHeight = Math.max(rowHeight, 35 + wrappedHeight(visible.get(i).name(), cardWidth - 6, 0.65f));
                }
                for (int i = first; i < Math.min(first + columns, visible.size()); i++) {
                    Entry entry = visible.get(i);
                    cardRows.put(entry.key(), rowY);
                    content.addWidget(new GuideButton((i % columns) * (cardWidth + 4), rowY,
                            cardWidth, rowHeight, entry.name(), entry.icon(), () -> selected == entry,
                            () -> navigateTo(entry)));
                }
                rowY += rowHeight + 4;
            }
            content.setHeight(rowY);
            listScroll.addComponent(content);
            renderedCategory = category;
        }
        if (revealEntry != null) {
            listScroll.setScrollAmount(cardRows.getOrDefault(revealEntry, 0));
            revealEntry = null;
        }
        if (!searchQuery.equals(renderedQuery)) {
            searchScroll.clearComponents();
            searchScroll.setScrollAmount(0);
            EmptyComponent results = new EmptyComponent(0, 0, searchScroll.getWidth() - 10, 0);
            int resultY = 0;
            if (!searchQuery.isEmpty()) {
                for (Entry entry : catalog.entries()) {
                    if (!normalize(entry.name().getString() + " " + entry.id()).contains(searchQuery)) {
                        continue;
                    }
                    Component title = Component.literal(entry.kind().label + " · ").append(entry.name());
                    int rowHeight = Math.max(26, wrappedHeight(title, results.getWidth() - 26, 0.7f) + 8);
                    results.addWidget(new GuideButton(0, resultY, results.getWidth(), rowHeight,
                            title, entry.icon(), () -> false, () -> navigateTo(entry), false));
                    resultY += rowHeight + 3;
                }
                if (resultY == 0) {
                    results.addComponent(new LineComponent(8, results.getWidth(), "검색 결과가 없습니다.", JobsTheme.MUTED));
                    resultY = 25;
                }
            }
            results.setHeight(resultY);
            searchScroll.addComponent(results);
            renderedQuery = searchQuery;
        }
        if (renderedSelection != selected) {
            detailScroll.clearComponents();
            detailScroll.setScrollAmount(0);
            if (selected != null) {
                detailScroll.addComponent(details(selected, detailScroll.getWidth() - 10));
            }
            renderedSelection = selected;
        }
    }

    private EmptyComponent details(Entry entry, int width) {
        EmptyComponent content = new EmptyComponent(0, 0, width, 0);
        content.addComponent(new PreviewComponent(width, entry));
        int y = 73;
        for (String text : entry.description()) {
            LineComponent line = new LineComponent(y, width, text, JobsTheme.MUTED);
            content.addComponent(line);
            y += line.getHeight() + 3;
        }
        content.addComponent(new LineComponent(y + 3, width, "제작 재료", JobsTheme.CYAN));
        y += 20;
        if (entry.recipes().isEmpty()) {
            content.addComponent(new LineComponent(y, width, "등록된 작업대 제작식 없음", JobsTheme.MUTED));
            y += 16;
        }
        for (int recipeIndex = 0; recipeIndex < entry.recipes().size(); recipeIndex++) {
            TaczCatalog.Recipe recipe = entry.recipes().get(recipeIndex);
            if (recipeIndex > 0) {
                y += 8;
            }
            for (TaczCatalog.Material material : recipe.materials()) {
                content.addComponent(new MaterialComponent(y, width, material));
                y += 29;
            }
        }
        content.addComponent(new LineComponent(y + 3, width,
                entry.kind() == Kind.GUN ? "탄환 · 조준경 · 호환 파츠" : "호환 총기", JobsTheme.CYAN));
        y += 20;
        if (entry.links().isEmpty()) {
            content.addComponent(new LineComponent(y, width, "등록된 호환 항목 없음", JobsTheme.MUTED));
            y += 16;
        }
        for (TaczCatalog.Link link : entry.links()) {
            Entry target = entries.get(link.target());
            if (target == null) {
                content.addComponent(new LineComponent(y, width, link.label() + " · 정보 없음", JobsTheme.MUTED));
                y += 16;
                continue;
            }
            Component title = Component.literal(link.label() + " · ").append(target.name());
            int linkHeight = Math.max(25, wrappedHeight(title, width - 26, 0.7f) + 8);
            content.addWidget(new GuideButton(0, y, width, linkHeight, title, target.icon(), () -> false,
                    () -> navigateTo(target), false));
            y += linkHeight + 3;
        }
        content.setHeight(y + 4);
        return content;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                   float partialTick, int parentWidth, int parentHeight) {
        if (renderedCategory != category || renderedSelection != selected || revealEntry != null || !searchQuery.equals(renderedQuery)) {
            refresh();
            updateParentPosition(getParentX(), getParentY(), parentWidth, parentHeight);
        }
        detailScroll.visible = !searchOpen;
        searchScroll.visible = searchOpen;
        JobsTheme.texture(graphics, JobsTheme.Skin.INSET, getTotalX() + detailX - 3, getTotalY(),
                getWidth() - detailX + 3, getHeight());
        if (!catalog.message().isEmpty()) {
            JobsTheme.text(graphics, Component.literal(catalog.message()), getTotalX() + 3, getTotalY() + 30,
                    listWidth - 10, JobsTheme.MUTED);
        }
    }

    private static class GuideButton extends CustomButtonWidget {
        private final ItemStack icon;
        private final BooleanSupplier selected;
        private final boolean card;

        GuideButton(int x, int y, int width, int height, Component title, ItemStack icon,
                    BooleanSupplier selected, Runnable action) {
            this(x, y, width, height, title, icon, selected, action, height > 40);
        }

        GuideButton(int x, int y, int width, int height, Component title, ItemStack icon,
                    BooleanSupplier selected, Runnable action, boolean card) {
            super(x, y, width, height, title, null, button -> action.run());
            this.card = card;
            this.icon = icon;
            this.selected = selected;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            JobsTheme.button(graphics, getX(), getY(), getWidth(), getHeight(), active,
                    isHoveredOrFocused(), selected.getAsBoolean(), false);
            if (card) {
                drawItem(graphics, icon, getX() + (getWidth() - 26) / 2, getY() + 3, 26);
                drawWrapped(graphics, getMessage(), getX() + 3, getY() + 32, getWidth() - 6, 0.65f, JobsTheme.TEXT, true);
            } else if (!icon.isEmpty()) {
                graphics.fakeItem(icon, getX() + 3, getY() + 4);
                drawWrapped(graphics, getMessage(), getX() + 22, getY() + 4, getWidth() - 26, 0.7f, JobsTheme.TEXT, false);
            } else {
                JobsTheme.label(graphics, getMessage(), getX(), getY(), getWidth(), getHeight(), JobsTheme.TEXT);
            }
        }
    }

    private static class PreviewComponent extends EmptyComponent {
        private final Entry entry;
        PreviewComponent(int width, Entry entry) {
            super(0, 0, width, 70);
            this.entry = entry;
        }
        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                       float partialTick, int parentWidth, int parentHeight) {
            JobsTheme.label(graphics, entry.name(), getTotalX() + 3, getTotalY() + 2, getWidth() - 6, 14, JobsTheme.CYAN);
            drawItem(graphics, entry.icon(), getTotalX() + (getWidth() - 48) / 2, getTotalY() + 20, 48);
        }
    }

    private static class LineComponent extends EmptyComponent {
        private final Component text;
        private final int color;
        LineComponent(int y, int width, String text, int color) {
            super(3, y, width - 6, wrappedHeight(Component.literal(text), width - 6, 0.75f));
            this.text = Component.literal(text);
            this.color = color;
        }
        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                       float partialTick, int parentWidth, int parentHeight) {
            drawWrapped(graphics, text, getTotalX(), getTotalY(), getWidth(), 0.75f, color, false);
        }
    }

    private static class MaterialComponent extends EmptyComponent {
        private final TaczCatalog.Material material;
        private final List<ItemStack> alternatives;
        MaterialComponent(int y, int width, TaczCatalog.Material material) {
            super(0, y, width, 26);
            this.material = material;
            Minecraft client = Minecraft.getInstance();
            alternatives = material.ingredient() == null || client.level == null ? List.of()
                    : material.ingredient().display().resolveForStacks(SlotDisplayContext.fromLevel(client.level));
        }
        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                       float partialTick, int parentWidth, int parentHeight) {
            JobsTheme.texture(graphics, JobsTheme.Skin.INSET, getTotalX(), getTotalY(), getWidth(), getHeight());
            ItemStack icon = alternatives.isEmpty() ? ItemStack.EMPTY
                    : alternatives.get((int) ((System.currentTimeMillis() / 1000) % alternatives.size()));
            graphics.fakeItem(icon, getTotalX() + 3, getTotalY() + 5);
            Component title = icon.isEmpty() ? Component.literal("재료 정보 없음") : icon.getHoverName();
            JobsTheme.text(graphics, title, getTotalX() + 23, getTotalY() + 3, getWidth() - 27, JobsTheme.TEXT);
            int owned = 0;
            Minecraft client = Minecraft.getInstance();
            if (client.player != null && material.ingredient() != null) {
                for (ItemStack stack : client.player.getInventory()) {
                    if (material.ingredient().test(stack)) {
                        owned += stack.getCount();
                    }
                }
            }
            JobsTheme.text(graphics, Component.literal("보유 " + owned + " / 필요 " + material.count()),
                    getTotalX() + 23, getTotalY() + 15, getWidth() - 27,
                    owned >= material.count() ? JobsTheme.CYAN : JobsTheme.ERROR);
        }
    }

    private static String normalize(String text) {
        return Normalizer.normalize(text, Normalizer.Form.NFKC).toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
    }

    private static int wrappedHeight(Component text, int width, float scale) {
        return (int) Math.ceil(Minecraft.getInstance().font.split(text, Math.max(1, (int) (width / scale))).size() * 10 * scale);
    }

    private static void drawWrapped(GuiGraphicsExtractor graphics, Component text, int x, int y,
                                    int width, float scale, int color, boolean centered) {
        var font = Minecraft.getInstance().font;
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(scale, scale);
        int lineY = 0;
        for (FormattedCharSequence line : font.split(text, Math.max(1, (int) (width / scale)))) {
            int lineX = centered ? Math.max(0, (int) ((width / scale - font.width(line)) / 2)) : 0;
            graphics.text(font, line, lineX, lineY, color, false);
            lineY += 10;
        }
        graphics.pose().popMatrix();
    }

    private static void drawItem(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y, int size) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.pose().scale(size / 16.0f, size / 16.0f);
        graphics.fakeItem(stack, 0, 0);
        graphics.pose().popMatrix();
    }
}
