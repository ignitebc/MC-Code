package com.daqem.jobsplus.client.gui.jobs.components;

import com.autovw.advancednetherite.client.ClientPetData;
import com.autovw.advancednetherite.common.pet.PetNames;
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
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BooleanSupplier;

/** 선택한 종류의 보유 펫을 개체별로 켜고 끄거나 이름을 바꾼다. */
final class PetManagementComponent extends EmptyComponent
{
    private static final int TITLE_HEIGHT = 12;
    private static final int RENAME_HEIGHT = 22;
    private static final int ROW_HEIGHT = 34;
    private static final int BUTTON_WIDTH = 28;

    private final ActionScrollWidget petScroll;
    private final JobsEditBox nameInput;
    private final ActionButton saveButton;
    private final ActionButton cancelButton;
    private List<PetStatusEntry> renderedPets;
    private String selectedTypeId;
    private UUID renaming;

    PetManagementComponent(int x, int y, int width, int height)
    {
        super(x, y, width, height);
        this.petScroll = new ActionScrollWidget(width, Math.max(1, height - TITLE_HEIGHT - RENAME_HEIGHT));
        this.petScroll.setY(TITLE_HEIGHT);
        addWidget(this.petScroll);

        int renameY = height - RENAME_HEIGHT + 3;
        int cancelX = width - 3 - BUTTON_WIDTH;
        int saveX = cancelX - 3 - BUTTON_WIDTH;
        this.nameInput = new JobsEditBox(Minecraft.getInstance().font, 3, renameY,
                Math.max(1, saveX - 6), JobsTheme.BUTTON_HEIGHT, Component.literal("펫 이름"));
        this.nameInput.setMaxLength(PetNames.MAX_LENGTH);
        this.nameInput.setHint(Component.literal("새 이름"));
        addWidget(this.nameInput);
        this.saveButton = new ActionButton(saveX, renameY, BUTTON_WIDTH, Component.literal("저장"),
                () -> false, this::saveName);
        this.cancelButton = new ActionButton(cancelX, renameY, BUTTON_WIDTH, Component.literal("취소"),
                () -> false, this::cancelRename);
        addWidget(this.saveButton);
        addWidget(this.cancelButton);
        setRenameVisible(false);
    }

    void selectType(String typeId)
    {
        if (Objects.equals(this.selectedTypeId, typeId))
        {
            return;
        }
        this.selectedTypeId = typeId;
        cancelRename();
        this.petScroll.setScrollAmount(0);
        refresh();
    }

    private void refresh()
    {
        List<PetStatusEntry> pets = ClientPetData.getPets();
        if (this.renaming != null && find(this.renaming) == null)
        {
            cancelRename();
        }
        double scrolled = this.petScroll.scrollAmount();
        this.petScroll.clearComponents();
        this.petScroll.addComponent(buildPetList(pets));
        this.petScroll.setScrollAmount(scrolled);
        this.renderedPets = pets;
        positionUpdated();
    }

    private EmptyComponent buildPetList(List<PetStatusEntry> pets)
    {
        int width = Math.max(1, getWidth() - 10);
        EmptyComponent content = new EmptyComponent(0, 0, width, 0);
        // 이름 뒤의 개체 번호는 전체 보유 목록과 같은 순서를 유지한다.
        List<Component> labels = PetNames.labels(pets, PetStatusEntry::petTypeId, PetStatusEntry::name);
        int buttonWidth = Math.max(1, (width - 9) / 2);
        int y = 2;
        for (int i = 0; i < pets.size(); i++)
        {
            PetStatusEntry entry = pets.get(i);
            if (!entry.petTypeId().equals(this.selectedTypeId))
            {
                continue;
            }
            UUID recordId = entry.recordId();
            content.addComponent(new TextLine(y, width,
                    labels.get(i).copy().append(Component.literal(" LV" + entry.level()))));
            content.addWidget(new ToggleButton(3, y + 12, buttonWidth, recordId));
            content.addWidget(new ActionButton(6 + buttonWidth, y + 12, buttonWidth,
                    Component.literal("이름 변경"), () -> recordId.equals(this.renaming),
                    () -> startRename(recordId)));
            y += ROW_HEIGHT;
        }
        if (y == 2)
        {
            content.addComponent(new TextLine(y, width, Component.literal("보유한 펫이 없습니다.")));
            y += 14;
        }
        content.setHeight(y);
        return content;
    }

    private static PetStatusEntry find(UUID recordId)
    {
        for (PetStatusEntry entry : ClientPetData.getPets())
        {
            if (entry.recordId().equals(recordId))
            {
                return entry;
            }
        }
        return null;
    }

    private static void toggle(UUID recordId)
    {
        PetStatusEntry entry = find(recordId);
        if (entry == null || ClientPetData.reviveSecondsLeft(entry) > 0)
        {
            return;
        }
        // 표시 상태는 서버 응답으로 갱신해 연타 제한에 걸려도 실제 상태와 일치시킨다.
        send(new PetTogglePayload(recordId));
    }

    /** Advanced Netherite가 등록한 페이로드를 기존 바닐라 패킷 경로로 보낸다. */
    private static void send(CustomPacketPayload payload)
    {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (connection != null)
        {
            connection.send(new ServerboundCustomPayloadPacket(payload));
        }
    }

    private void startRename(UUID recordId)
    {
        PetStatusEntry entry = find(recordId);
        if (entry == null)
        {
            return;
        }
        this.renaming = recordId;
        this.nameInput.setValue(entry.name());
        setRenameVisible(true);
    }

    private void saveName()
    {
        if (this.renaming == null || find(this.renaming) == null)
        {
            return;
        }
        String name = PetNames.sanitize(this.nameInput.getValue());
        send(new PetRenamePayload(this.renaming, name));
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
        this.nameInput.active = visible;
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
        }
        JobsTheme.texture(graphics, JobsTheme.Skin.INSET, getTotalX(), getTotalY(), getWidth(), getHeight());
        JobsTheme.text(graphics, Component.literal("내 펫 관리"), getTotalX() + 3, getTotalY() + 2,
                Math.max(1, getWidth() - 6), JobsTheme.CYAN);
        if (this.renaming == null)
        {
            JobsTheme.text(graphics, Component.literal("이름은 " + PetNames.MAX_LENGTH + "자까지 · 비우면 초기화"),
                    getTotalX() + 3, getTotalY() + getHeight() - RENAME_HEIGHT + 6,
                    Math.max(1, getWidth() - 6), JobsTheme.MUTED);
        }
    }

    private static class TextLine extends EmptyComponent
    {
        private final Component text;

        TextLine(int y, int width, Component text)
        {
            super(0, y, width, 12);
            this.text = text;
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY,
                                       float partialTick, int parentWidth, int parentHeight)
        {
            JobsTheme.text(graphics, this.text, getTotalX() + 3, getTotalY() + 1,
                    Math.max(1, getWidth() - 6), JobsTheme.TEXT);
        }
    }

    private static class ToggleButton extends CustomButtonWidget
    {
        private final UUID recordId;

        ToggleButton(int x, int y, int width, UUID recordId)
        {
            super(x, y, width, JobsTheme.BUTTON_HEIGHT, Component.empty(), null, button -> toggle(recordId));
            this.recordId = recordId;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
        {
            PetStatusEntry entry = find(this.recordId);
            int secondsLeft = entry == null ? 0 : ClientPetData.reviveSecondsLeft(entry);
            this.active = entry != null && secondsLeft <= 0;
            boolean on = entry != null && entry.enabled();
            Component title = Component.literal(on ? "ON" : "OFF");
            int color = on ? JobsTheme.SUCCESS : JobsTheme.ERROR;
            if (secondsLeft > 0)
            {
                title = Component.literal("부활 " + secondsLeft + "초");
                color = JobsTheme.DISABLED;
            }
            setMessage(title);
            JobsTheme.button(graphics, getX(), getY(), getWidth(), getHeight(), active,
                    isHoveredOrFocused(), on, false);
            JobsTheme.label(graphics, title, getX(), getY(), getWidth(), getHeight(), color);
        }
    }

    private static class ActionButton extends CustomButtonWidget
    {
        private final BooleanSupplier selected;

        ActionButton(int x, int y, int width, Component title, BooleanSupplier selected, Runnable action)
        {
            super(x, y, width, JobsTheme.BUTTON_HEIGHT, title, null, button -> action.run());
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
