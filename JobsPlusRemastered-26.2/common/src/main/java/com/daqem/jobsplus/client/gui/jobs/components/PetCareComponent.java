package com.daqem.jobsplus.client.gui.jobs.components;

import com.autovw.advancednetherite.client.ClientPetData;
import com.autovw.advancednetherite.common.pet.PetNames;
import com.autovw.advancednetherite.common.pet.PetRarity;
import com.autovw.advancednetherite.common.pet.PetStats;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;

/**
 * 펫관리 탭. 왼쪽은 등급별 펫 도감, 오른쪽은 등급별로 묶은 내 펫의 ON/OFF와 이름 변경.
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
    /** 오른쪽 칸 아래에 두는 이름 변경 영역의 높이. 입력칸과 저장·취소 단추가 한 줄에 들어간다. */
    private static final int RENAME_HEIGHT = 22;
    private static final int RENAME_BUTTON_WIDTH = 34;
    private static final float RENAME_HINT_SCALE = 0.7f;
    /** "부활 100초"까지 들어가는 너비 */
    private static final int TOGGLE_WIDTH = 46;
    private static final int RENAME_WIDTH = 54;
    private static final int SECTION_HEIGHT = 12;

    /** 왼쪽 도감 칸과 오른쪽 펫관리 칸의 너비. 도감은 절반의 2/3만 쓰고 나머지를 펫관리가 가진다. */
    private final int codexWidth;
    private final int petWidth;
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
        this.codexWidth = (width - COLUMN_GAP) / 2 * 2 / 3;
        this.petWidth = width - COLUMN_GAP - this.codexWidth;
        this.rightX = this.codexWidth + COLUMN_GAP;
        int columnHeight = Math.max(1, height - TITLE_HEIGHT);

        this.codexScroll = new ActionScrollWidget(this.codexWidth, columnHeight);
        this.codexScroll.setY(TITLE_HEIGHT);
        addWidget(this.codexScroll);

        this.petScroll = new ActionScrollWidget(this.petWidth, Math.max(1, columnHeight - RENAME_HEIGHT));
        this.petScroll.setX(this.rightX);
        this.petScroll.setY(TITLE_HEIGHT);
        addWidget(this.petScroll);

        // 한 줄 배치: [입력칸][저장][취소]
        int renameY = height - RENAME_HEIGHT + (RENAME_HEIGHT - JobsTheme.BUTTON_HEIGHT) / 2;
        int cancelX = this.rightX + this.petWidth - 3 - RENAME_BUTTON_WIDTH;
        int saveX = cancelX - 3 - RENAME_BUTTON_WIDTH;
        this.nameInput = new JobsEditBox(Minecraft.getInstance().font, this.rightX + 3, renameY,
                saveX - 3 - (this.rightX + 3), JobsTheme.BUTTON_HEIGHT, Component.literal("펫 이름"));
        this.nameInput.setMaxLength(PetNames.MAX_LENGTH);
        this.nameInput.setHint(Component.literal("새 이름 (비우면 원래 이름으로)"));
        addWidget(this.nameInput);
        this.saveButton = new ActionButton(saveX, renameY, RENAME_BUTTON_WIDTH, JobsTheme.BUTTON_HEIGHT,
                Component.literal("저장"), () -> false, this::saveName);
        this.cancelButton = new ActionButton(cancelX, renameY, RENAME_BUTTON_WIDTH, JobsTheme.BUTTON_HEIGHT,
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

        int width = this.codexWidth - 10;
        EmptyComponent content = new EmptyComponent(0, 0, width, 0);
        int y = 2;
        for (PetRarity rarity : PetRarity.values())
        {
            content.addComponent(new TextLine(0, y, width,
                    Component.literal(rarity.label() + "펫 (공격력+" + formatNumber(rarity.attackDamage()) + ")"),
                    rarityColor(rarity), 0.85f));
            y += SECTION_HEIGHT;
            for (String typeId : rarity.petTypeIds())
            {
                content.addComponent(new CodexRow(y, width, PetNames.typeName(typeId), owned.getOrDefault(typeId, 0)));
                y += 16;
            }
            y += 6;
        }
        content.setHeight(y);
        return content;
    }

    /** 등급별 구역으로 나눈 내 펫 목록. 한 줄은 이름·레벨, ON/OFF 단추, 이름 변경 단추 */
    private EmptyComponent buildPetList(List<PetStatusEntry> pets)
    {
        int width = this.petWidth - 10;
        EmptyComponent content = new EmptyComponent(0, 0, width, 0);
        if (pets.isEmpty())
        {
            content.addComponent(new TextLine(0, 6, width,
                    Component.literal("가진 펫이 없습니다. 펫 상자를 사용하면 여기에 나타납니다."), JobsTheme.MUTED, 0.75f));
            content.setHeight(30);
            return content;
        }

        // 같은 종류가 여럿일 때 붙는 번호는 전체 목록 순서로 매기므로 구역을 나누기 전에 이름부터 만든다.
        List<Component> labels = PetNames.labels(pets, PetStatusEntry::petTypeId, PetStatusEntry::name);
        int nameWidth = width - TOGGLE_WIDTH - RENAME_WIDTH - 8;
        int y = 2;
        for (PetRarity rarity : PetRarity.values())
        {
            List<Integer> members = new ArrayList<>();
            for (int i = 0; i < pets.size(); i++)
            {
                if (rarityOf(pets.get(i)) == rarity) members.add(i);
            }
            if (members.isEmpty())
            {
                continue;
            }

            content.addComponent(new TextLine(0, y, width,
                    Component.literal(rarity.label() + "펫 " + members.size() + "마리"), rarityColor(rarity), 0.85f));
            y += SECTION_HEIGHT;
            for (int index : members)
            {
                PetStatusEntry entry = pets.get(index);
                UUID recordId = entry.recordId();
                content.addComponent(new PetRow(y, nameWidth,
                        labels.get(index).copy().append(Component.literal(" LV" + entry.level())),
                        describe(entry, rarity), rarityColor(rarity),
                        () -> this.renaming != null && this.renaming.equals(recordId)));
                content.addWidget(new ToggleButton(nameWidth + 4, y + 2, TOGGLE_WIDTH, JobsTheme.BUTTON_HEIGHT,
                        () -> isEnabled(recordId), () -> reviveSecondsLeft(recordId), () -> toggle(recordId)));
                content.addWidget(new ActionButton(width - RENAME_WIDTH, y + 2, RENAME_WIDTH, JobsTheme.BUTTON_HEIGHT,
                        Component.literal("이름 변경"), () -> this.renaming != null && this.renaming.equals(recordId),
                        () -> startRename(entry)));
                y += ROW_HEIGHT + ROW_GAP;
            }
            y += 4;
        }
        content.setHeight(y);
        return content;
    }

    /** 이름 아래 작은 글씨. 이름을 붙인 펫은 종류 이름을 앞에 두고, 경험치와 현재 능력치를 잇는다. */
    private static Component describe(PetStatusEntry entry, PetRarity rarity)
    {
        String exp = entry.level() >= PetStats.MAX_LEVEL
                ? "EXP MAX"
                : "EXP " + entry.exp() + "/" + PetStats.expToLevelUp(entry.level());
        String stats = exp + " · 공격력 " + formatNumber(PetStats.attackDamage(rarity, entry.level()))
                + " · 체력 " + formatNumber(PetStats.maxHealth(rarity, entry.level()));
        if (entry.name().isEmpty())
        {
            return Component.literal(stats);
        }
        return PetNames.typeName(entry.petTypeId()).copy().append(Component.literal(" · " + stats));
    }

    /** 알 수 없는 종류는 일반으로 묶는다. */
    private static PetRarity rarityOf(PetStatusEntry entry)
    {
        PetRarity rarity = PetRarity.of(entry.petTypeId());
        return rarity == null ? PetRarity.NORMAL : rarity;
    }

    private static PetStatusEntry find(UUID recordId)
    {
        for (PetStatusEntry entry : ClientPetData.getPets())
        {
            if (entry.recordId().equals(recordId)) return entry;
        }
        return null;
    }

    private static boolean isEnabled(UUID recordId)
    {
        PetStatusEntry entry = find(recordId);
        return entry != null && entry.enabled();
    }

    /** 죽은 펫이 부활하기까지 남은 시간(초). 살아 있으면 0 */
    private static int reviveSecondsLeft(UUID recordId)
    {
        PetStatusEntry entry = find(recordId);
        return entry == null ? 0 : ClientPetData.reviveSecondsLeft(entry);
    }

    private void toggle(UUID recordId)
    {
        if (reviveSecondsLeft(recordId) > 0)
        {
            return;
        }
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
        JobsTheme.text(graphics, LEFT_TITLE, getTotalX(), getTotalY(), this.codexWidth, JobsTheme.CYAN);
        JobsTheme.text(graphics, RIGHT_TITLE, getTotalX() + this.rightX, getTotalY(), this.petWidth, JobsTheme.CYAN);

        int renameTop = getTotalY() + getHeight() - RENAME_HEIGHT;
        JobsTheme.texture(graphics, JobsTheme.Skin.INSET, getTotalX() + this.rightX, renameTop,
                this.petWidth, RENAME_HEIGHT);
        if (this.renaming == null)
        {
            // 안내는 작은 글자 한 줄로, 영역의 세로 가운데에 둔다.
            float textHeight = Minecraft.getInstance().font.lineHeight * RENAME_HINT_SCALE * JobsTheme.LABEL_SCALE;
            graphics.pose().pushMatrix();
            graphics.pose().translate(getTotalX() + this.rightX + 5, renameTop + (RENAME_HEIGHT - textHeight) / 2.0f);
            graphics.pose().scale(RENAME_HINT_SCALE, RENAME_HINT_SCALE);
            JobsTheme.text(graphics,
                    Component.literal("이름 변경을 누르면 여기서 새 이름을 정합니다. ("
                            + PetNames.MAX_LENGTH + "자까지, 펫 머리 위에 표시)"),
                    0, 0, (int) ((this.petWidth - 10) / RENAME_HINT_SCALE), JobsTheme.MUTED);
            graphics.pose().popMatrix();
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
            JobsTheme.text(graphics, this.name, getTotalX() + 5, getTotalY() + 3, getWidth() - 46,
                    this.owned > 0 ? JobsTheme.TEXT : JobsTheme.DISABLED);
            Component count = Component.literal(this.owned > 0 ? "보유 " + this.owned : "미보유");
            JobsTheme.textRight(graphics, count, getTotalX() + getWidth() - 5, getTotalY() + 3, 38,
                    this.owned > 0 ? JobsTheme.CYAN : JobsTheme.DISABLED);
        }
    }

    /** 내 펫 한 줄의 이름 부분. 이름과 레벨은 등급 색으로, 그 아래에 경험치와 능력치를 작게 적는다. */
    private static class PetRow extends EmptyComponent
    {
        private final Component label;
        private final Component detail;
        private final int color;
        private final BooleanSupplier highlighted;

        PetRow(int y, int width, Component label, Component detail, int color, BooleanSupplier highlighted)
        {
            super(0, y, width, ROW_HEIGHT);
            this.label = label;
            this.detail = detail;
            this.color = color;
            this.highlighted = highlighted;
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                       float partialTick, int parentWidth, int parentHeight)
        {
            int nameColor = this.highlighted.getAsBoolean() ? JobsTheme.CYAN : this.color;
            JobsTheme.text(graphics, this.label, getTotalX() + 3, getTotalY() + 1, getWidth() - 6, nameColor);
            graphics.pose().pushMatrix();
            graphics.pose().translate(getTotalX() + 3, getTotalY() + 10);
            graphics.pose().scale(0.7f, 0.7f);
            JobsTheme.text(graphics, this.detail, 0, 0, (int) ((getWidth() - 6) / 0.7f), JobsTheme.MUTED);
            graphics.pose().popMatrix();
        }
    }

    /** 켜짐은 초록 ON, 꺼짐은 빨강 OFF. 죽은 펫은 부활까지 남은 시간을 보여주고 누를 수 없다. */
    private static class ToggleButton extends CustomButtonWidget
    {
        private final BooleanSupplier enabled;
        private final IntSupplier reviveSecondsLeft;

        ToggleButton(int x, int y, int width, int height, BooleanSupplier enabled, IntSupplier reviveSecondsLeft,
                     Runnable action)
        {
            super(x, y, width, height, Component.empty(), null, button -> action.run());
            this.enabled = enabled;
            this.reviveSecondsLeft = reviveSecondsLeft;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
        {
            int secondsLeft = this.reviveSecondsLeft.getAsInt();
            this.active = secondsLeft <= 0;
            if (secondsLeft > 0)
            {
                JobsTheme.button(graphics, getX(), getY(), getWidth(), getHeight(), false, false, false, false);
                JobsTheme.label(graphics, Component.literal("부활 " + secondsLeft + "초"), getX(), getY(), getWidth(),
                        getHeight(), JobsTheme.DISABLED);
                return;
            }
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
