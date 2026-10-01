package com.daqem.jobsplus.client.gui.jobs.components;

import com.autovw.advancednetherite.client.ClientPetData;
import com.autovw.advancednetherite.common.entity.DialgaPetEntity;
import com.autovw.advancednetherite.common.pet.PetManager;
import com.autovw.advancednetherite.common.pet.PetNames;
import com.autovw.advancednetherite.common.pet.PetRarity;
import com.autovw.advancednetherite.common.pet.PetStats;
import com.autovw.advancednetherite.network.PetStatusEntry;
import com.daqem.jobsplus.client.gui.jobs.widgets.ActionScrollWidget;
import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.uilib.gui.component.EmptyComponent;
import com.daqem.uilib.gui.widget.CustomButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

/**
 * 펫도감 탭. 왼쪽에서 펫 종류를 고르면 오른쪽에 3D 모델과 보유 상태·능력치를 표시한다.
 *
 * <p>같은 종류를 여러 마리 보유한 경우 레벨이 가장 높고, 같은 레벨이면 경험치가 가장 높은
 * 개체를 대표로 사용한다. 미보유 펫은 1레벨 기본 공격력과 체력을 보여준다.
 */
public class PetCareComponent extends EmptyComponent
{
    private static final Component LEFT_TITLE = Component.literal("펫 종류");
    private static final Component DETAIL_TITLE = Component.literal("펫 상세");

    private static final int COLUMN_GAP = 8;
    private static final int INNER_GAP = 6;
    private static final int TITLE_HEIGHT = 12;
    private static final int ROW_HEIGHT = 18;
    private static final int ROW_GAP = 2;
    private static final int MIN_CODEX_WIDTH = 96;
    private static final int MAX_CODEX_WIDTH = 174;
    private static final int MIN_PREVIEW_SCALE = 24;
    private static final int MAX_PREVIEW_SCALE = 180;
    private static final int PREVIEW_ENTITY_ID = -1;

    private final List<CatalogEntry> catalog;
    private final int codexWidth;
    private final int rightX;
    private final int detailWidth;
    private final int modelWidth;
    private final int infoX;
    private final int infoWidth;
    private final ActionScrollWidget codexScroll;
    private final PetManagementComponent petManagement;
    private final int previewHeight;

    private List<PetStatusEntry> renderedPets;
    private String selectedTypeId;
    private String previewTypeId;
    private DialgaPetEntity previewPet;

    public PetCareComponent(int width, int height)
    {
        super(0, 0, width, height);
        this.catalog = buildCatalog();

        int availableWidth = Math.max(1, width - COLUMN_GAP);
        int maximumCodexWidth = Math.min(MAX_CODEX_WIDTH, availableWidth);
        if (maximumCodexWidth < MIN_CODEX_WIDTH)
        {
            this.codexWidth = maximumCodexWidth;
        }
        else
        {
            this.codexWidth = Mth.clamp(availableWidth / 3, MIN_CODEX_WIDTH, maximumCodexWidth);
        }
        this.rightX = this.codexWidth + COLUMN_GAP;
        this.detailWidth = Math.max(1, width - this.rightX);

        int requestedInfoWidth = Math.max(82, this.detailWidth * 40 / 100);
        this.modelWidth = Math.max(1, this.detailWidth - INNER_GAP - requestedInfoWidth);
        this.infoX = this.rightX + this.modelWidth + INNER_GAP;
        this.infoWidth = Math.max(1, width - this.infoX);

        int columnHeight = Math.max(1, height - TITLE_HEIGHT);
        this.codexScroll = new ActionScrollWidget(this.codexWidth, columnHeight);
        this.codexScroll.setY(TITLE_HEIGHT);
        addWidget(this.codexScroll);

        int managementHeight = Math.min(104, Math.max(72, columnHeight / 2));
        this.previewHeight = Math.max(1, columnHeight - managementHeight - INNER_GAP);
        this.petManagement = new PetManagementComponent(this.rightX,
                TITLE_HEIGHT + this.previewHeight + INNER_GAP, this.modelWidth, managementHeight);
        addComponent(this.petManagement);

        refresh();
    }

    private static List<CatalogEntry> buildCatalog()
    {
        List<CatalogEntry> entries = new ArrayList<>();
        for (PetRarity rarity : PetRarity.values())
        {
            for (String typeId : rarity.petTypeIds())
            {
                entries.add(new CatalogEntry(typeId, rarity));
            }
        }
        return List.copyOf(entries);
    }

    private void refresh()
    {
        List<PetStatusEntry> pets = ClientPetData.getPets();
        ensureSelection(pets);

        Map<String, Integer> owned = new HashMap<>();
        for (PetStatusEntry entry : pets)
        {
            owned.merge(entry.petTypeId(), 1, Integer::sum);
        }

        double scrolled = this.codexScroll.scrollAmount();
        this.codexScroll.clearComponents();
        this.codexScroll.addComponent(buildCodex(owned));
        this.codexScroll.setScrollAmount(scrolled);
        this.renderedPets = pets;
    }

    private void ensureSelection(List<PetStatusEntry> pets)
    {
        if (findCatalogEntry(this.selectedTypeId) != null)
        {
            return;
        }

        for (CatalogEntry entry : this.catalog)
        {
            if (containsType(pets, entry.typeId()))
            {
                selectType(entry.typeId());
                return;
            }
        }

        if (!this.catalog.isEmpty())
        {
            selectType(this.catalog.getFirst().typeId());
        }
    }

    private EmptyComponent buildCodex(Map<String, Integer> owned)
    {
        int width = Math.max(1, this.codexWidth - 10);
        EmptyComponent content = new EmptyComponent(0, 0, width, 0);
        int y = 2;
        for (CatalogEntry entry : this.catalog)
        {
            int ownedCount = owned.getOrDefault(entry.typeId(), 0);
            content.addWidget(new CodexButton(y, width, entry, PetNames.typeName(entry.typeId()), ownedCount,
                    () -> entry.typeId().equals(this.selectedTypeId), () -> selectType(entry.typeId())));
            y += ROW_HEIGHT + ROW_GAP;
        }
        content.setHeight(y);
        return content;
    }

    private void selectType(String typeId)
    {
        if (typeId != null && typeId.equals(this.selectedTypeId))
        {
            return;
        }
        this.selectedTypeId = typeId;
        this.petManagement.selectType(typeId);
        this.previewTypeId = null;
        this.previewPet = null;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                   float partialTick, int parentWidth, int parentHeight)
    {
        if (ClientPetData.getPets() != this.renderedPets)
        {
            refresh();
            updateParentPosition(getParentX(), getParentY(), parentWidth, parentHeight);
        }

        int x = getTotalX();
        int y = getTotalY();
        JobsTheme.text(graphics, LEFT_TITLE, x, y, this.codexWidth, JobsTheme.CYAN);
        JobsTheme.text(graphics, DETAIL_TITLE, x + this.rightX, y, this.detailWidth, JobsTheme.CYAN);

        int bodyY = y + TITLE_HEIGHT;
        int bodyHeight = Math.max(1, getHeight() - TITLE_HEIGHT);
        int modelX = x + this.rightX;
        int infoX = x + this.infoX;
        JobsTheme.texture(graphics, JobsTheme.Skin.INSET, modelX, bodyY, this.modelWidth, this.previewHeight);
        JobsTheme.texture(graphics, JobsTheme.Skin.INSET, infoX, bodyY, this.infoWidth, bodyHeight);

        drawPreview(graphics, mouseX, mouseY, modelX, bodyY, this.modelWidth, this.previewHeight);
        drawDetails(graphics, infoX, bodyY, this.infoWidth, bodyHeight);
    }

    private void drawPreview(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                             int x, int y, int width, int height)
    {
        DialgaPetEntity pet = getPreviewPet();
        if (pet == null)
        {
            JobsTheme.label(graphics, Component.literal("모델을 불러올 수 없습니다."),
                    x + 4, y + 4, Math.max(1, width - 8), Math.max(1, height - 8), JobsTheme.DISABLED);
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null)
        {
            pet.tickCount = minecraft.player.tickCount;
        }

        int padding = 5;
        int left = x + padding;
        int top = y + padding;
        int right = x + width - padding;
        int bottom = y + height - padding;
        int scale = calculatePreviewScale(pet.getType(), right - left, bottom - top);
        InventoryScreen.extractEntityInInventoryFollowsMouse(graphics,
                left, top, right, bottom, scale, 0.0F, mouseX, mouseY, pet);
    }

    private DialgaPetEntity getPreviewPet()
    {
        if (this.selectedTypeId == null)
        {
            return null;
        }
        if (this.previewPet != null && this.selectedTypeId.equals(this.previewTypeId))
        {
            return this.previewPet;
        }

        Minecraft minecraft = Minecraft.getInstance();
        EntityType<DialgaPetEntity> petType = PetManager.getPetType(this.selectedTypeId);
        if (minecraft.level == null || petType == null)
        {
            this.previewTypeId = null;
            this.previewPet = null;
            return null;
        }

        DialgaPetEntity created = petType.create(minecraft.level, EntitySpawnReason.LOAD);
        if (created == null)
        {
            this.previewTypeId = null;
            this.previewPet = null;
            return null;
        }
        // 클라이언트에서 만든 미리보기는 ID가 0이라 렌더링할 수 없다. 월드에 등록하지 않는 전용 ID를 준다.
        created.setId(PREVIEW_ENTITY_ID);
        created.setNoAi(true);
        created.setSilent(true);
        created.setCustomNameVisible(false);
        this.previewTypeId = this.selectedTypeId;
        this.previewPet = created;
        return this.previewPet;
    }

    private static int calculatePreviewScale(EntityType<?> type, int width, int height)
    {
        float entityWidth = Math.max(0.5F, type.getWidth());
        float entityHeight = Math.max(0.5F, type.getHeight());
        float horizontalScale = width * 0.72F / entityWidth;
        float verticalScale = height * 0.72F / entityHeight;
        int scale = Mth.floor(Math.min(horizontalScale, verticalScale));
        return Mth.clamp(scale, MIN_PREVIEW_SCALE, MAX_PREVIEW_SCALE);
    }

    private void drawDetails(GuiGraphicsExtractor graphics, int x, int y, int width, int height)
    {
        CatalogEntry catalogEntry = findCatalogEntry(this.selectedTypeId);
        if (catalogEntry == null)
        {
            JobsTheme.label(graphics, Component.literal("펫을 선택해 주세요."), x, y, width, height,
                    JobsTheme.DISABLED);
            return;
        }

        PetStatusEntry representative = findRepresentative(this.renderedPets, catalogEntry.typeId());
        int ownedCount = countOwned(this.renderedPets, catalogEntry.typeId());
        int color = rarityColor(catalogEntry.rarity());
        int textX = x + 6;
        int textWidth = Math.max(1, width - 12);
        int currentY = y + 6;

        JobsTheme.text(graphics, PetNames.typeName(catalogEntry.typeId()), textX, currentY, textWidth, color);
        currentY += 14;
        Component identity = Component.literal(catalogEntry.rarity().label());
        JobsTheme.text(graphics, identity, textX, currentY, textWidth, JobsTheme.MUTED);
        currentY += 15;
        graphics.fill(textX, currentY, x + width - 6, currentY + 1, JobsTheme.DIVIDER);
        currentY += 8;

        Component ownership;
        int ownershipColor;
        if (ownedCount > 0)
        {
            ownership = Component.literal("보유 " + ownedCount + "마리");
            ownershipColor = JobsTheme.SUCCESS;
        }
        else
        {
            ownership = Component.literal("미보유");
            ownershipColor = JobsTheme.DISABLED;
        }
        JobsTheme.text(graphics, ownership, textX, currentY, textWidth, ownershipColor);
        currentY += 18;

        if (representative == null)
        {
            drawStat(graphics, "레벨", "-", textX, currentY, textWidth);
            currentY += 16;
            drawStat(graphics, "경험치", "-", textX, currentY, textWidth);
            currentY += 16;
            drawStat(graphics, "공격력", formatNumber(PetStats.attackDamage(catalogEntry.rarity(), 1)),
                    textX, currentY, textWidth);
            currentY += 16;
            drawStat(graphics, "체력", formatNumber(PetStats.maxHealth(catalogEntry.rarity(), 1)),
                    textX, currentY, textWidth);
            return;
        }

        drawStat(graphics, "레벨", Integer.toString(representative.level()), textX, currentY, textWidth);
        currentY += 16;
        drawStat(graphics, "경험치", formatExperience(representative), textX, currentY, textWidth);
        currentY += 16;
        drawStat(graphics, "공격력",
                formatNumber(PetStats.attackDamage(catalogEntry.rarity(), representative.level())),
                textX, currentY, textWidth);
        currentY += 16;
        drawStat(graphics, "체력", formatHealth(representative, catalogEntry.rarity()),
                textX, currentY, textWidth);

        if (ownedCount > 1 && currentY + 24 < y + height)
        {
            JobsTheme.text(graphics, Component.literal("최고 레벨 개체 기준"), textX, currentY + 19,
                    textWidth, JobsTheme.MUTED);
        }
    }

    private static void drawStat(GuiGraphicsExtractor graphics, String label, String value,
                                 int x, int y, int width)
    {
        JobsTheme.text(graphics, Component.literal(label), x, y, width / 2, JobsTheme.MUTED);
        JobsTheme.textRight(graphics, Component.literal(value), x + width, y, width / 2, JobsTheme.TEXT);
    }

    private static PetStatusEntry findRepresentative(List<PetStatusEntry> pets, String typeId)
    {
        PetStatusEntry best = null;
        for (PetStatusEntry pet : pets)
        {
            if (!typeId.equals(pet.petTypeId()))
            {
                continue;
            }
            if (best == null)
            {
                best = pet;
                continue;
            }

            boolean hasHigherLevel = pet.level() > best.level();
            boolean hasSameLevelAndMoreExperience = pet.level() == best.level() && pet.exp() > best.exp();
            if (hasHigherLevel || hasSameLevelAndMoreExperience)
            {
                best = pet;
            }
        }
        return best;
    }

    private static int countOwned(List<PetStatusEntry> pets, String typeId)
    {
        int count = 0;
        for (PetStatusEntry pet : pets)
        {
            if (typeId.equals(pet.petTypeId()))
            {
                count++;
            }
        }
        return count;
    }

    private static boolean containsType(List<PetStatusEntry> pets, String typeId)
    {
        for (PetStatusEntry pet : pets)
        {
            if (typeId.equals(pet.petTypeId()))
            {
                return true;
            }
        }
        return false;
    }

    private CatalogEntry findCatalogEntry(String typeId)
    {
        if (typeId == null)
        {
            return null;
        }
        for (CatalogEntry entry : this.catalog)
        {
            if (typeId.equals(entry.typeId()))
            {
                return entry;
            }
        }
        return null;
    }

    private static String formatExperience(PetStatusEntry entry)
    {
        if (entry.level() >= PetStats.MAX_LEVEL)
        {
            return "MAX";
        }
        return entry.exp() + "/" + PetStats.expToLevelUp(entry.level());
    }

    private static String formatHealth(PetStatusEntry entry, PetRarity rarity)
    {
        int currentHealth = (int) Math.ceil(entry.health());
        String maxHealth = formatNumber(PetStats.maxHealth(rarity, entry.level()));
        return currentHealth + "/" + maxHealth;
    }

    private static String formatNumber(double value)
    {
        double rounded = Math.round(value * 10.0) / 10.0;
        if (rounded == Math.floor(rounded))
        {
            return Integer.toString((int) rounded);
        }
        return Double.toString(rounded);
    }

    private static int rarityColor(PetRarity rarity)
    {
        return switch (rarity)
        {
            case NORMAL -> JobsTheme.TEXT;
            case RARE -> JobsTheme.PRIMARY;
            case LEGEND -> JobsTheme.WARNING;
        };
    }

    private record CatalogEntry(String typeId, PetRarity rarity)
    {
    }

    /** 펫 종류 한 줄. 클릭하면 오른쪽 상세 모델과 수치가 바뀐다. */
    private static class CodexButton extends CustomButtonWidget
    {
        private final CatalogEntry entry;
        private final Component name;
        private final int owned;
        private final BooleanSupplier selected;

        CodexButton(int y, int width, CatalogEntry entry, Component name, int owned,
                    BooleanSupplier selected, Runnable action)
        {
            super(0, y, width, ROW_HEIGHT, Component.empty(), null, button -> action.run());
            this.entry = entry;
            this.name = name;
            this.owned = owned;
            this.selected = selected;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
        {
            boolean isSelected = this.selected.getAsBoolean();
            JobsTheme.button(graphics, getX(), getY(), getWidth(), getHeight(), active,
                    isHoveredOrFocused(), isSelected, false);

            int color = rarityColor(this.entry.rarity());
            JobsTheme.text(graphics, this.name, getX() + 6, getY() + 5,
                    Math.max(1, getWidth() - 54), color);

            Component ownership;
            int ownershipColor;
            if (this.owned > 0)
            {
                ownership = Component.literal("보유 " + this.owned);
                ownershipColor = JobsTheme.CYAN;
            }
            else
            {
                ownership = Component.literal("미보유");
                ownershipColor = JobsTheme.DISABLED;
            }
            JobsTheme.textRight(graphics, ownership, getRight() - 4, getY() + 5, 40, ownershipColor);
        }
    }
}
