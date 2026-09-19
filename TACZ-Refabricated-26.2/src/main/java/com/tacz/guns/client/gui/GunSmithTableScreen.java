package com.tacz.guns.client.gui;

import cn.sh1rocu.tacz.mixin.accessor.ScreenAccessor;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.gui.components.smith.SmithRowButton;
import com.tacz.guns.client.gui.components.smith.SmithTabButton;
import com.tacz.guns.client.gui.components.smith.SmithTextButton;
import com.tacz.guns.client.gui.components.smith.SmithTheme;
import com.tacz.guns.config.sync.SyncConfig;
import com.tacz.guns.crafting.GunSmithTableIngredient;
import com.tacz.guns.crafting.GunSmithTableRecipe;
import com.tacz.guns.inventory.GunSmithTableMenu;
import com.tacz.guns.network.message.ClientMessageCraft;
import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.filter.RecipeFilter;
import com.tacz.guns.resource.pojo.data.block.TabConfig;
import com.tacz.guns.resource.pojo.data.recipe.TableRecipe;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.display.SlotDisplayContext;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 총기 작업대 화면. 총기·부착물·탄약을 한 작업대에서 만들며, Jobs+ 직업 화면(J키)과 같은 스킨을 쓴다.
 *
 * <pre>
 * [ 총기 | 부착물 | 탄약 | 기타 ]                       [x]   ← 큰 분류 탭
 * ┌ 분류 ─────┐ ┌ 제작 목록 ───────────┐ ┌ 재료 ───────┐
 * │ 세부 분류  │ │ 만들 수 있는 물건      │ │ 재료와 보유량 │
 * │ (휠 스크롤)│ │ (휠 스크롤)           │ │ 개수 / 제작   │
 * └──────────┘ └─────────────────────┘ └────────────┘
 * </pre>
 *
 * 큰 분류는 팩 설정에 따로 적지 않고, 세부 분류에 든 첫 제작법의 결과물 종류로 정한다.
 * 그래서 다른 총기팩이 탭을 추가해도 알맞은 큰 분류 아래에 들어간다.
 */
public class GunSmithTableScreen extends AbstractContainerScreen<GunSmithTableMenu> {
    private static final int WIDTH = 420;
    private static final int HEIGHT = 236;
    private static final int BODY_Y = 32;
    private static final int BODY_HEIGHT = HEIGHT - BODY_Y - 21;
    private static final int PANEL_HEADER = 18;

    private static final int TYPE_X = 8;
    private static final int TYPE_WIDTH = 104;
    private static final int RECIPE_X = TYPE_X + TYPE_WIDTH + 4;
    private static final int RECIPE_WIDTH = 170;
    private static final int DETAIL_X = RECIPE_X + RECIPE_WIDTH + 4;
    private static final int DETAIL_WIDTH = WIDTH - 8 - DETAIL_X;

    private static final int LIST_Y = BODY_Y + PANEL_HEADER + 4;
    private static final int ROW_STEP = SmithRowButton.HEIGHT + 1;
    private static final int VISIBLE_ROWS = (BODY_Y + BODY_HEIGHT - 3 - LIST_Y) / ROW_STEP;
    private static final int GROUP_TAB_WIDTH = 64;
    private static final int INGREDIENT_ROW_HEIGHT = 18;
    private static final int MAX_INGREDIENTS = 12;

    /** 큰 분류. 선언 순서가 탭 순서다. */
    private enum Group {
        GUN("gui.tacz.gun_smith_table.group.gun"),
        ATTACHMENT("gui.tacz.gun_smith_table.group.attachment"),
        AMMO("gui.tacz.gun_smith_table.group.ammo"),
        MISC("gui.tacz.gun_smith_table.group.misc");

        private final String nameKey;

        Group(String nameKey) {
            this.nameKey = nameKey;
        }

        static Group of(ItemStack output) {
            Item item = output.getItem();
            if (item instanceof IGun) {
                return GUN;
            }
            if (item instanceof IAttachment) {
                return ATTACHMENT;
            }
            if (item instanceof IAmmo) {
                return AMMO;
            }
            return MISC;
        }
    }

    /** 세부 분류별 제작법. 제작법이 하나도 없는 분류는 담지 않는다. */
    private final Map<Identifier, List<Identifier>> recipes = Maps.newLinkedHashMap();
    private final Map<Identifier, TabConfig> tabs = Maps.newLinkedHashMap();
    private final Map<Group, List<Identifier>> groupTabs = new EnumMap<>(Group.class);

    private @Nullable Group selectedGroup;
    private @Nullable Identifier selectedType;
    private @Nullable GunSmithTableRecipe selectedRecipe;
    private @Nullable Int2IntArrayMap playerIngredientCount;
    private int typeScroll;
    private int recipeScroll;

    public GunSmithTableScreen(GunSmithTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, WIDTH, HEIGHT);
    }

    // ---- 제작법 분류 ------------------------------------------------------

    private void classifyRecipes() {
        this.recipes.clear();
        this.tabs.clear();
        this.groupTabs.clear();
        Identifier blockId = this.menu.getBlockId();
        if (blockId == null) {
            return;
        }
        boolean unrestricted = DefaultAssets.DEFAULT_BLOCK_ID.equals(blockId) && !SyncConfig.ENABLE_TABLE_FILTER.get();

        Map<Identifier, TabConfig> declaredTabs = Maps.newLinkedHashMap();
        TimelessAPI.getCommonBlockIndex(blockId).ifPresent(blockIndex -> {
            List<TabConfig> declared = unrestricted ? TabConfig.DEFAULT_TABS : blockIndex.getData().getTabs();
            for (TabConfig tab : declared) {
                declaredTabs.put(tab.id(), tab);
            }
        });

        // 26.2 클라이언트에는 전체 제작법 표가 없으므로 모드가 따로 동기화한 제작법을 읽는다.
        List<Pair<Identifier, Identifier>> candidates = Lists.newArrayList();
        for (Map.Entry<Identifier, TableRecipe> entry : CommonAssetsManager.get().getAllTableRecipes()) {
            TableRecipe pojo = entry.getValue();
            if (pojo == null || pojo.getResult() == null) {
                continue;
            }
            GunSmithTableRecipe recipe = new GunSmithTableRecipe(entry.getKey(), pojo);
            // init()을 거쳐야 결과물 ItemStack과 분류가 채워진다.
            recipe.init();
            Identifier tabId = recipe.getResult().getGroup();
            if (declaredTabs.containsKey(tabId)) {
                candidates.add(Pair.of(tabId, entry.getKey()));
            }
        }

        List<Pair<Identifier, Identifier>> allowed = candidates;
        if (!unrestricted) {
            RecipeFilter filter = TimelessAPI.getCommonBlockIndex(blockId)
                    .map(blockIndex -> blockIndex.getFilter()).orElse(null);
            if (filter != null) {
                allowed = filter.filter(candidates, Pair::value);
            }
        }

        Map<Identifier, List<Identifier>> byTab = Maps.newLinkedHashMap();
        for (Identifier tabId : declaredTabs.keySet()) {
            byTab.put(tabId, Lists.newArrayList());
        }
        for (Pair<Identifier, Identifier> entry : allowed) {
            byTab.get(entry.key()).add(entry.value());
        }

        for (Map.Entry<Identifier, List<Identifier>> entry : byTab.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            GunSmithTableRecipe first = loadRecipe(entry.getValue().get(0));
            Group group = first == null ? Group.MISC : Group.of(first.getOutput());
            this.recipes.put(entry.getKey(), entry.getValue());
            this.tabs.put(entry.getKey(), declaredTabs.get(entry.getKey()));
            this.groupTabs.computeIfAbsent(group, g -> new ArrayList<>()).add(entry.getKey());
        }
    }

    /** 선택 상태가 더는 없는 항목을 가리키면 첫 항목으로 되돌린다. */
    private void repairSelection() {
        if (this.selectedGroup == null || !this.groupTabs.containsKey(this.selectedGroup)) {
            this.selectedGroup = this.groupTabs.keySet().stream().findFirst().orElse(null);
            this.selectedType = null;
        }
        List<Identifier> types = visibleTypes();
        if (this.selectedType == null || !types.contains(this.selectedType)) {
            this.selectedType = types.isEmpty() ? null : types.get(0);
            this.selectedRecipe = null;
            this.typeScroll = 0;
            this.recipeScroll = 0;
        }
        List<Identifier> list = visibleRecipes();
        if (this.selectedRecipe == null || !list.contains(this.selectedRecipe.getId())) {
            this.selectedRecipe = list.isEmpty() ? null : loadRecipe(list.get(0));
        }
        this.typeScroll = clampScroll(this.typeScroll, types.size());
        this.recipeScroll = clampScroll(this.recipeScroll, list.size());
        countPlayerIngredients();
    }

    private List<Identifier> visibleTypes() {
        return this.selectedGroup == null ? List.of() : this.groupTabs.getOrDefault(this.selectedGroup, List.of());
    }

    private List<Identifier> visibleRecipes() {
        return this.selectedType == null ? List.of() : this.recipes.getOrDefault(this.selectedType, List.of());
    }

    private static int clampScroll(int scroll, int size) {
        return Math.max(0, Math.min(scroll, size - VISIBLE_ROWS));
    }

    @Nullable
    private GunSmithTableRecipe loadRecipe(@Nullable Identifier recipeId) {
        if (recipeId == null) {
            return null;
        }
        TableRecipe pojo = CommonAssetsManager.get().getTableRecipe(recipeId);
        if (pojo == null || pojo.getResult() == null) {
            return null;
        }
        GunSmithTableRecipe recipe = new GunSmithTableRecipe(recipeId, pojo);
        recipe.init();
        return recipe;
    }

    private void countPlayerIngredients() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || this.selectedRecipe == null) {
            this.playerIngredientCount = null;
            return;
        }
        List<GunSmithTableIngredient> ingredients = this.selectedRecipe.getInputs();
        Int2IntArrayMap counts = new Int2IntArrayMap(ingredients.size());
        for (int i = 0; i < ingredients.size(); i++) {
            // 재료는 늦게 해석되므로 아직 없을 수 있다. 그때는 하나도 없는 것으로 센다.
            Ingredient resolved = ingredients.get(i).getIngredient();
            int count = 0;
            if (resolved != null) {
                for (ItemStack stack : player.getInventory()) {
                    if (!stack.isEmpty() && resolved.test(stack)) {
                        count += stack.getCount();
                    }
                }
            }
            counts.put(i, count);
        }
        this.playerIngredientCount = counts;
    }

    /** 서버가 제작을 끝냈다고 알려 오면 보유량을 다시 센다. */
    public void updateIngredientCount() {
        this.init();
    }

    private boolean canCraft() {
        if (this.selectedRecipe == null || this.playerIngredientCount == null) {
            return false;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && player.isCreative()) {
            return true;
        }
        List<GunSmithTableIngredient> inputs = this.selectedRecipe.getInputs();
        for (int i = 0; i < inputs.size(); i++) {
            if (this.playerIngredientCount.get(i) < inputs.get(i).getCount()) {
                return false;
            }
        }
        return true;
    }

    // ---- 위젯 -------------------------------------------------------------

    @Override
    public void init() {
        super.init();
        this.classifyRecipes();
        this.repairSelection();
        this.clearWidgets();

        this.addGroupTabs();
        this.addTypeRows();
        this.addRecipeRows();
        this.addRenderableWidget(new SmithTextButton(leftPos + WIDTH - 8 - SmithTheme.BUTTON_HEIGHT, topPos + 7,
                SmithTheme.BUTTON_HEIGHT, SmithTheme.BUTTON_HEIGHT, Component.literal("×"),
                SmithTextButton.Style.CLOSE, b -> this.onClose()));

        SmithTextButton craft = new SmithTextButton(leftPos + DETAIL_X + 6,
                topPos + BODY_Y + BODY_HEIGHT - SmithTheme.BUTTON_HEIGHT - 6, DETAIL_WIDTH - 12,
                SmithTheme.BUTTON_HEIGHT, Component.translatable("gui.tacz.gun_smith_table.craft"),
                SmithTextButton.Style.PRIMARY, b -> this.craft());
        craft.active = this.canCraft();
        this.addRenderableWidget(craft);
    }

    private void craft() {
        if (this.selectedRecipe != null && this.canCraft()) {
            ClientPlayNetworking.send(new ClientMessageCraft(this.selectedRecipe.getId(), this.menu.containerId));
        }
    }

    private void addGroupTabs() {
        int x = leftPos + 8;
        for (Group group : this.groupTabs.keySet()) {
            this.addRenderableWidget(new SmithTabButton(x, topPos + 6, GROUP_TAB_WIDTH,
                    Component.translatable(group.nameKey), group == this.selectedGroup, b -> {
                this.selectedGroup = group;
                this.selectedType = null;
                this.init();
            }));
            x += GROUP_TAB_WIDTH + 2;
        }
    }

    private void addTypeRows() {
        List<Identifier> types = visibleTypes();
        for (int row = 0; row < VISIBLE_ROWS && row + this.typeScroll < types.size(); row++) {
            Identifier type = types.get(row + this.typeScroll);
            TabConfig tab = this.tabs.get(type);
            this.addRenderableWidget(new SmithRowButton(leftPos + TYPE_X + 3, topPos + LIST_Y + row * ROW_STEP,
                    TYPE_WIDTH - 9, tab.icon().get(), tab.getName(), type.equals(this.selectedType), false, b -> {
                this.selectedType = type;
                this.selectedRecipe = null;
                this.recipeScroll = 0;
                this.init();
            }));
        }
    }

    private void addRecipeRows() {
        List<Identifier> list = visibleRecipes();
        for (int row = 0; row < VISIBLE_ROWS && row + this.recipeScroll < list.size(); row++) {
            GunSmithTableRecipe recipe = loadRecipe(list.get(row + this.recipeScroll));
            if (recipe == null) {
                continue;
            }
            boolean selected = this.selectedRecipe != null && recipe.getId().equals(this.selectedRecipe.getId());
            this.addRenderableWidget(new SmithRowButton(leftPos + RECIPE_X + 3, topPos + LIST_Y + row * ROW_STEP,
                    RECIPE_WIDTH - 9, recipe.getOutput(), recipe.getOutput().getHoverName(), selected, true, b -> {
                this.selectedRecipe = recipe;
                this.init();
            }));
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int step = scrollY > 0 ? -1 : 1;
        if (isOver(mouseX, mouseY, TYPE_X, TYPE_WIDTH)) {
            this.typeScroll = clampScroll(this.typeScroll + step, visibleTypes().size());
            this.init();
            return true;
        }
        if (isOver(mouseX, mouseY, RECIPE_X, RECIPE_WIDTH)) {
            this.recipeScroll = clampScroll(this.recipeScroll + step, visibleRecipes().size());
            this.init();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private boolean isOver(double mouseX, double mouseY, int panelX, int panelWidth) {
        return mouseX >= leftPos + panelX && mouseX < leftPos + panelX + panelWidth
                && mouseY >= topPos + BODY_Y && mouseY < topPos + BODY_Y + BODY_HEIGHT;
    }

    // ---- 그리기 -----------------------------------------------------------

    /**
     * 26.2의 컨테이너 화면은 배경을 옮기지 않은 좌표계의 extractBackground에서 그린다.
     * extractContents 안은 이미 (leftPos, topPos)만큼 옮겨져 있어 거기서 또 더하면 두 배로 밀린다.
     */
    @Override
    public void extractBackground(@NotNull GuiGraphicsExtractor gui, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(gui, mouseX, mouseY, partialTick);
        int x = leftPos;
        int y = topPos;
        SmithTheme.texture(gui, SmithTheme.Skin.PANEL, x, y, WIDTH, HEIGHT);
        SmithTheme.texture(gui, SmithTheme.Skin.HEADER, x + 2, y + 2, WIDTH - 4, 25);
        gui.fill(x + 8, y + 27, x + WIDTH - 8, y + 28, SmithTheme.DIVIDER);

        drawPanel(gui, TYPE_X, TYPE_WIDTH);
        drawPanel(gui, RECIPE_X, RECIPE_WIDTH);
        drawPanel(gui, DETAIL_X, DETAIL_WIDTH);

        gui.fill(x + 8, y + HEIGHT - 20, x + WIDTH - 8, y + HEIGHT - 19, SmithTheme.DIVIDER);
    }

    private void drawPanel(GuiGraphicsExtractor gui, int panelX, int panelWidth) {
        SmithTheme.texture(gui, SmithTheme.Skin.PANEL, leftPos + panelX, topPos + BODY_Y, panelWidth, BODY_HEIGHT);
        SmithTheme.texture(gui, SmithTheme.Skin.HEADER, leftPos + panelX + 1, topPos + BODY_Y + 1,
                panelWidth - 2, PANEL_HEADER);
    }

    @Override
    public void extractRenderState(@NotNull GuiGraphicsExtractor gui, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(gui, mouseX, mouseY, partialTick);
        int headerY = topPos + BODY_Y + 6;
        SmithTheme.text(gui, Component.translatable("gui.tacz.gun_smith_table.category"),
                leftPos + TYPE_X + 8, headerY, TYPE_WIDTH - 16, SmithTheme.CYAN);
        TabConfig tab = this.selectedType == null ? null : this.tabs.get(this.selectedType);
        SmithTheme.text(gui, tab == null ? Component.translatable("gui.tacz.gun_smith_table.recipes") : tab.getName(),
                leftPos + RECIPE_X + 8, headerY, RECIPE_WIDTH - 16, SmithTheme.CYAN);
        SmithTheme.text(gui, Component.translatable("gui.tacz.gun_smith_table.ingredient"),
                leftPos + DETAIL_X + 8, headerY, DETAIL_WIDTH - 16, SmithTheme.CYAN);

        drawScrollBar(gui, TYPE_X + TYPE_WIDTH - 5, this.typeScroll, visibleTypes().size());
        drawScrollBar(gui, RECIPE_X + RECIPE_WIDTH - 5, this.recipeScroll, visibleRecipes().size());

        if (this.selectedRecipe == null) {
            SmithTheme.text(gui, Component.translatable("gui.tacz.gun_smith_table.empty"),
                    leftPos + RECIPE_X + 8, topPos + LIST_Y + 4, RECIPE_WIDTH - 16, SmithTheme.MUTED);
        } else {
            drawIngredients(gui);
            SmithTheme.text(gui, Component.translatable("gui.tacz.gun_smith_table.count",
                            this.selectedRecipe.getOutput().getCount()),
                    leftPos + DETAIL_X + 8, topPos + BODY_Y + BODY_HEIGHT - SmithTheme.BUTTON_HEIGHT - 18,
                    DETAIL_WIDTH - 16, SmithTheme.MUTED);
        }
        SmithTheme.text(gui, Component.translatable("gui.tacz.gun_smith_table.close_hint"),
                leftPos + 10, topPos + HEIGHT - 14, 80, SmithTheme.MUTED);

        for (var widget : ((ScreenAccessor) this).tacz$getRenderables()) {
            if (widget instanceof SmithRowButton row && !row.tooltipStack().isEmpty()) {
                gui.setTooltipForNextFrame(font, row.tooltipStack(), mouseX, mouseY);
            }
        }
    }

    /** 목록이 화면보다 길 때만 오른쪽 가장자리에 가는 스크롤 막대를 그린다. */
    private void drawScrollBar(GuiGraphicsExtractor gui, int barX, int scroll, int size) {
        if (size <= VISIBLE_ROWS) {
            return;
        }
        int trackTop = topPos + LIST_Y;
        int trackHeight = VISIBLE_ROWS * ROW_STEP - 1;
        int thumbHeight = Math.max(8, trackHeight * VISIBLE_ROWS / size);
        int thumbTop = trackTop + (trackHeight - thumbHeight) * scroll / (size - VISIBLE_ROWS);
        gui.fill(leftPos + barX, trackTop, leftPos + barX + 2, trackTop + trackHeight, SmithTheme.DIVIDER);
        gui.fill(leftPos + barX, thumbTop, leftPos + barX + 2, thumbTop + thumbHeight, SmithTheme.CYAN);
    }

    private void drawIngredients(GuiGraphicsExtractor gui) {
        if (this.selectedRecipe == null) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        boolean creative = player != null && player.isCreative();
        List<GunSmithTableIngredient> inputs = this.selectedRecipe.getInputs();
        int columnWidth = (DETAIL_WIDTH - 12) / 2;
        for (int index = 0; index < inputs.size() && index < MAX_INGREDIENTS; index++) {
            int x = leftPos + DETAIL_X + 6 + (index % 2) * columnWidth;
            int y = topPos + LIST_Y + (index / 2) * INGREDIENT_ROW_HEIGHT;
            GunSmithTableIngredient input = inputs.get(index);

            // 태그 재료는 해당하는 아이템을 1초마다 돌려 가며 보여 준다.
            Ingredient ingredient = input.getIngredient();
            List<ItemStack> choices = ingredient == null ? List.of() : ingredient.display()
                    .resolveForStacks(SlotDisplayContext.fromLevel(Minecraft.getInstance().level));
            ItemStack shown = choices.isEmpty() ? ItemStack.EMPTY
                    : choices.get((int) (System.currentTimeMillis() / 1_000 % choices.size()));
            SmithTheme.texture(gui, SmithTheme.Skin.INSET, x, y, columnWidth - 2, INGREDIENT_ROW_HEIGHT - 1);
            gui.fakeItem(shown, x + 1, y);

            int need = input.getCount();
            int have = this.playerIngredientCount == null ? 0 : this.playerIngredientCount.get(index);
            Component amount = Component.literal(creative ? need + "/∞" : need + "/" + have);
            SmithTheme.text(gui, amount, x + 19, y + 5, columnWidth - 23,
                    creative || have >= need ? SmithTheme.TEXT : SmithTheme.ERROR, 0.7f);
        }
    }

    @Override
    protected void extractLabels(@NotNull GuiGraphicsExtractor gui, int mouseX, int mouseY) {
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
