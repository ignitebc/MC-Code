package com.daqem.jobsplus.client.gui.jobs.components;

import com.autovw.advancednetherite.client.ClientPetData;
import com.autovw.advancednetherite.common.pet.PetNames;
import com.autovw.advancednetherite.common.pet.PetRarity;
import com.autovw.advancednetherite.network.PetRenamePayload;
import com.autovw.advancednetherite.network.PetStatusEntry;
import com.autovw.advancednetherite.network.PetTogglePayload;
import com.daqem.jobsplus.client.gui.jobs.widgets.ActionScrollWidget;
import com.daqem.jobsplus.client.gui.theme.JobsEditBox;
import com.daqem.jobsplus.client.gui.theme.JobsTheme;
import com.daqem.uilib.gui.component.EmptyComponent;
import com.daqem.uilib.gui.widget.CustomButtonWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BooleanSupplier;

/**
 * 펫관리 탭. 왼쪽은 등급별 펫 도감, 오른쪽은 내 펫의 ON/OFF와 이름 변경.
 *
 * <p>펫 목록은 Advanced Netherite가 서버에서 동기화해 준 클라이언트 캐시를 읽는다.
 * 캐시 객체는 바뀔 때마다 새로 만들어지므로 참조가 달라졌을 때만 목록을 다시 만든다.
 */
public class PetCareComponent extends EmptyComponent
{
    private static final Component LEFT_TITLE = Component.literal("펫도감");
    private static final Component RIGHT_TITLE = Component.literal("펫관리");

    private static final int COLUMN_GAP = 8;
    private static final int TITLE_HEIGHT = 12;
    private static final int ROW_HEIGHT = 18;
    private static final int ROW_GAP = 3;
    /** 오른쪽 칸 아래에 두는 이름 변경 영역의 높이 */
    private static final int RENAME_HEIGHT = 40;
    private static final int TOGGLE_WIDTH = 34;
    private static final int RENAME_WIDTH = 54;

    private final int columnWidth;
    private final int rightX;
    private final ActionScrollWidget codexScroll;
    private final ActionScrollWidget petScroll;
    private final JobsEditBox nameInput;
    private final ActionButton saveButton;
    private final ActionButton cancelButton;

    private List<PetStatusEntry> renderedPets;
    /** 이름을 바꾸는 중인 펫의 기록 ID. 없으면 null */
    private UUID renaming;

    public PetCareComponent(int width, int height)
    {
        super(0, 0, width, height);
        this.columnWidth = (width - COLUMN_GAP) / 2;
        this.rightX = this.columnWidth + COLUMN_GAP;
        int columnHeight = Math.max(1, height - TITLE_HEIGHT);

        this.codexScroll = new ActionScrollWidget(this.columnWidth, columnHeight);
        this.codexScroll.setY(TITLE_HEIGHT);
        addWidget(this.codexScroll);

        this.petScroll = new ActionScrollWidget(this.columnWidth, Math.max(1, columnHeight - RENAME_HEIGHT));
        this.petScroll.setX(this.rightX);
        this.petScroll.setY(TITLE_HEIGHT);
        addWidget(this.petScroll);

        int renameY = height - RENAME_HEIGHT + 4;
        this.nameInput = new JobsEditBox(Minecraft.getInstance().font, this.rightX + 3, renameY,
                this.columnWidth - 6, 14, Component.literal("펫 이름"));
        this.nameInput.setMaxLength(PetNames.MAX_LENGTH);
        this.nameInput.setHint(Component.literal("새 이름 (비우면 원래 이름으로)"));
        addWidget(this.nameInput);
        this.saveButton = new ActionButton(this.rightX + 3, renameY + 18, 60, JobsTheme.BUTTON_HEIGHT,
                Component.literal("저장"), () -> false, this::saveName);
        this.cancelButton = new ActionButton(this.rightX + 67, renameY + 18, 60, JobsTheme.BUTTON_HEIGHT,
                Component.literal("취소"), () -> false, this::cancelRename);
        addWidget(this.saveButton);
        addWidget(this.cancelButton);
        setRenameVisible(false);

        refresh();
    }

    private void refresh()
    {
        List<PetStatusEntry> pets = ClientPetData.getPets();
        if (this.renaming != null && pets.stream().noneMatch(entry -> entry.recordId().equals(this.renaming)))
        {
            cancelRename();
        }
        double codexScrolled = this.codexScroll.scrollAmount();
        double petScrolled = this.petScroll.scrollAmount();
        this.codexScroll.clearComponents();
        this.codexScroll.addComponent(buildCodex(pets));
        this.codexScroll.setScrollAmount(codexScrolled);
        this.petScroll.clearComponents();
        this.petScroll.addComponent(buildPetList(pets));
        this.petScroll.setScrollAmount(petScrolled);
        this.renderedPets = pets;
    }

    /** 등급 순서대로 펫 종류를 늘어놓고, 각 종류를 몇 마리 가졌는지 보여준다. */
    private EmptyComponent buildCodex(List<PetStatusEntry> pets)
    {
        Map<String, Integer> owned = new HashMap<>();
        for (PetStatusEntry entry : pets)
        {
            owned.merge(entry.petTypeId(), 1, Integer::sum);
        }

        int width = this.columnWidth - 10;
        EmptyComponent content = new EmptyComponent(0, 0, width, 0);
        int y = 2;
        for (PetRarity rarity : PetRarity.values())
        {
            List<String> typeIds = rarity.petTypeIds();
            content.addComponent(new TextLine(0, y, width,
                    Component.literal(rarity.label() + " 펫"), rarityColor(rarity), 0.85f));
            y += 11;
            content.addComponent(new TextLine(0, y, width,
                    Component.literal(rarity.label() + " 펫 상자 · 공격력 +" + formatNumber(rarity.attackDamage())
                            + " · 종류당 " + formatNumber(100.0 / typeIds.size()) + "%"), JobsTheme.MUTED, 0.7f));
            y += 11;
            for (String typeId : typeIds)
            {
                content.addComponent(new CodexRow(y, width, PetNames.typeName(typeId), owned.getOrDefault(typeId, 0)));
                y += 16;
            }
            y += 6;
        }
        content.setHeight(y);
        return content;
    }

    /** 내 펫 한 줄: 이름, ON/OFF 단추, 이름 변경 단추 */
    private EmptyComponent buildPetList(List<PetStatusEntry> pets)
    {
        int width = this.columnWidth - 10;
        EmptyComponent content = new EmptyComponent(0, 0, width, 0);
        if (pets.isEmpty())
        {
            content.addComponent(new TextLine(0, 6, width,
                    Component.literal("가진 펫이 없습니다. 펫 상자를 사용하면 여기에 나타납니다."), JobsTheme.MUTED, 0.75f));
            content.setHeight(30);
            return content;
        }

        List<Component> labels = PetNames.labels(pets, PetStatusEntry::petTypeId, PetStatusEntry::name);
        int y = 0;
        for (int i = 0; i < pets.size(); i++)
        {
            PetStatusEntry entry = pets.get(i);
            UUID recordId = entry.recordId();
            int nameWidth = width - TOGGLE_WIDTH - RENAME_WIDTH - 8;
            content.addComponent(new PetRow(y, nameWidth, labels.get(i),
                    entry.name().isEmpty() ? null : PetNames.typeName(entry.petTypeId()),
                    () -> this.renaming != null && this.renaming.equals(recordId)));
            content.addWidget(new ToggleButton(nameWidth + 4, y + 2, TOGGLE_WIDTH, JobsTheme.BUTTON_HEIGHT,
                    () -> isEnabled(recordId), () -> toggle(recordId)));
            content.addWidget(new ActionButton(width - RENAME_WIDTH, y + 2, RENAME_WIDTH, JobsTheme.BUTTON_HEIGHT,
                    Component.literal("이름 변경"), () -> this.renaming != null && this.renaming.equals(recordId),
                    () -> startRename(entry)));
            y += ROW_HEIGHT + ROW_GAP;
        }
        content.setHeight(y);
        return content;
    }

    private static boolean isEnabled(UUID recordId)
    {
        for (PetStatusEntry entry : ClientPetData.getPets())
        {
            if (entry.recordId().equals(recordId)) return entry.enabled();
        }
        return false;
    }

    private void toggle(UUID recordId)
    {
        // 서버 응답을 기다리지 않고 단추 색이 바로 바뀌도록 먼저 뒤집는다.
        ClientPetData.toggleLocally(recordId);
        send(new PetTogglePayload(recordId));
    }

    /** Advanced Netherite가 등록한 페이로드를 바닐라 경로로 보낸다. 다른 모드의 등록기를 거치지 않는다. */
    private static void send(CustomPacketPayload payload)
    {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null)
        {
            connection.send(new ServerboundCustomPayloadPacket(payload));
        }
    }

    private void startRename(PetStatusEntry entry)
    {
        this.renaming = entry.recordId();
        this.nameInput.setValue(entry.name());
        setRenameVisible(true);
    }

    private void saveName()
    {
        if (this.renaming == null)
        {
            return;
        }
        UUID recordId = this.renaming;
        String name = PetNames.sanitize(this.nameInput.getValue());
        ClientPetData.renameLocally(recordId, name);
        send(new PetRenamePayload(recordId, name));
        cancelRename();
    }

    private void cancelRename()
    {
        this.renaming = null;
        this.nameInput.setValue("");
        this.nameInput.setFocused(false);
        setRenameVisible(false);
    }

    private void setRenameVisible(boolean visible)
    {
        this.nameInput.visible = visible;
        this.saveButton.visible = visible;
        this.cancelButton.visible = visible;
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
        JobsTheme.text(graphics, LEFT_TITLE, getTotalX(), getTotalY(), this.columnWidth, JobsTheme.CYAN);
        JobsTheme.text(graphics, RIGHT_TITLE, getTotalX() + this.rightX, getTotalY(), this.columnWidth, JobsTheme.CYAN);

        int renameTop = getTotalY() + getHeight() - RENAME_HEIGHT;
        JobsTheme.texture(graphics, JobsTheme.Skin.INSET, getTotalX() + this.rightX, renameTop,
                this.columnWidth, RENAME_HEIGHT);
        if (this.renaming == null)
        {
            JobsTheme.text(graphics, Component.literal("이름 변경을 누르면 여기서 새 이름을 정합니다."),
                    getTotalX() + this.rightX + 4, renameTop + 8, this.columnWidth - 8, JobsTheme.MUTED);
            JobsTheme.text(graphics, Component.literal("이름은 " + PetNames.MAX_LENGTH + "자까지이며 펫 머리 위에 표시됩니다."),
                    getTotalX() + this.rightX + 4, renameTop + 22, this.columnWidth - 8, JobsTheme.MUTED);
        }
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

    private static String formatNumber(double value)
    {
        double rounded = Math.round(value * 10.0) / 10.0;
        return rounded == Math.floor(rounded) ? Integer.toString((int) rounded) : Double.toString(rounded);
    }

    /** 배율을 적용한 한 줄 글자 */
    private static class TextLine extends EmptyComponent
    {
        private final Component text;
        private final int color;
        private final float scale;

        TextLine(int x, int y, int width, Component text, int color, float scale)
        {
            super(x, y, width, (int) Math.ceil(10 * scale));
            this.text = text;
            this.color = color;
            this.scale = scale;
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                       float partialTick, int parentWidth, int parentHeight)
        {
            graphics.pose().pushMatrix();
            graphics.pose().translate(getTotalX() + 3, getTotalY());
            graphics.pose().scale(this.scale, this.scale);
            JobsTheme.text(graphics, this.text, 0, 0, (int) ((getWidth() - 6) / this.scale), this.color);
            graphics.pose().popMatrix();
        }
    }

    /** 도감 한 줄: 종류 이름과 보유 수 */
    private static class CodexRow extends EmptyComponent
    {
        private final Component name;
        private final int owned;

        CodexRow(int y, int width, Component name, int owned)
        {
            super(0, y, width, 14);
            this.name = name;
            this.owned = owned;
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                       float partialTick, int parentWidth, int parentHeight)
        {
            JobsTheme.texture(graphics, JobsTheme.Skin.INSET, getTotalX(), getTotalY(), getWidth(), getHeight());
            JobsTheme.text(graphics, this.name, getTotalX() + 5, getTotalY() + 3, getWidth() - 60,
                    this.owned > 0 ? JobsTheme.TEXT : JobsTheme.DISABLED);
            Component count = Component.literal(this.owned > 0 ? "보유 " + this.owned : "미보유");
            JobsTheme.textRight(graphics, count, getTotalX() + getWidth() - 5, getTotalY() + 3, 50,
                    this.owned > 0 ? JobsTheme.CYAN : JobsTheme.DISABLED);
        }
    }

    /** 내 펫 한 줄의 이름 부분. 이름을 붙인 펫은 종류 이름을 작게 덧붙인다. */
    private static class PetRow extends EmptyComponent
    {
        private final Component label;
        private final Component typeName;
        private final BooleanSupplier highlighted;

        PetRow(int y, int width, Component label, Component typeName, BooleanSupplier highlighted)
        {
            super(0, y, width, ROW_HEIGHT);
            this.label = label;
            this.typeName = typeName;
            this.highlighted = highlighted;
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                       float partialTick, int parentWidth, int parentHeight)
        {
            int color = this.highlighted.getAsBoolean() ? JobsTheme.CYAN : JobsTheme.TEXT;
            if (this.typeName == null)
            {
                JobsTheme.text(graphics, this.label, getTotalX() + 3, getTotalY() + 5, getWidth() - 6, color);
                return;
            }
            JobsTheme.text(graphics, this.label, getTotalX() + 3, getTotalY() + 1, getWidth() - 6, color);
            graphics.pose().pushMatrix();
            graphics.pose().translate(getTotalX() + 3, getTotalY() + 10);
            graphics.pose().scale(0.7f, 0.7f);
            JobsTheme.text(graphics, this.typeName, 0, 0, (int) ((getWidth() - 6) / 0.7f), JobsTheme.MUTED);
            graphics.pose().popMatrix();
        }
    }

    /** 켜짐은 초록 ON, 꺼짐은 빨강 OFF */
    private static class ToggleButton extends CustomButtonWidget
    {
        private final BooleanSupplier enabled;

        ToggleButton(int x, int y, int width, int height, BooleanSupplier enabled, Runnable action)
        {
            super(x, y, width, height, Component.empty(), null, button -> action.run());
            this.enabled = enabled;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
        {
            boolean on = this.enabled.getAsBoolean();
            JobsTheme.button(graphics, getX(), getY(), getWidth(), getHeight(), active, isHoveredOrFocused(), on, false);
            JobsTheme.label(graphics, Component.literal(on ? "ON" : "OFF"), getX(), getY(), getWidth(), getHeight(),
                    on ? JobsTheme.SUCCESS : JobsTheme.ERROR);
        }
    }

    private static class ActionButton extends CustomButtonWidget
    {
        private final BooleanSupplier selected;

        ActionButton(int x, int y, int width, int height, Component title, BooleanSupplier selected, Runnable action)
        {
            super(x, y, width, height, title, null, button -> action.run());
            this.selected = selected;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
        {
            JobsTheme.button(graphics, getX(), getY(), getWidth(), getHeight(), active, isHoveredOrFocused(),
                    this.selected.getAsBoolean(), false);
            JobsTheme.label(graphics, getMessage(), getX(), getY(), getWidth(), getHeight(), JobsTheme.TEXT);
        }
    }
}
