package com.autovw.advancednetherite.client.gui;

import com.autovw.advancednetherite.client.ClientPetData;
import com.autovw.advancednetherite.common.entity.DialgaPetEntity;
import com.autovw.advancednetherite.common.pet.PetManager;
import com.autovw.advancednetherite.network.PetStatusEntry;
import com.autovw.advancednetherite.network.PetTogglePayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 인벤토리 화면 아래의 펫 ON/OFF 판.
 *
 * <p>판 전체를 하나의 위젯으로 다룬다. 단추를 하나씩 화면에 붙이면 레시피 책을 여닫을 때
 * 바닐라가 init을 다시 돌리지 않고 leftPos만 바꾸기 때문에 단추가 제자리에 남아 어긋난다.
 * 여기서는 그릴 때마다 화면 위치를 다시 읽어 자리를 맞추므로 어느 경우에도 따라간다.
 *
 * <p>켜진 펫은 연한 파랑, 꺼진 펫은 연한 빨강으로 구분하고, 두 줄을 넘는 펫은 휠로 굴려서 본다.
 */
public class PetPanelWidget extends AbstractWidget
{
    private static final int FRAME = 0xFF373737;
    private static final int FACE = 0xFFC6C6C6;
    private static final int HIGHLIGHT = 0xFFFFFFFF;
    private static final int SHADOW = 0xFF555555;

    private static final int ENABLED_FACE = 0xFF7FB2E5;
    private static final int ENABLED_FACE_HOVER = 0xFF9FC8EF;
    private static final int DISABLED_FACE = 0xFFE59A9A;
    private static final int DISABLED_FACE_HOVER = 0xFFEFB6B6;
    private static final int BUTTON_BORDER = 0xFF3F3F3F;
    private static final int BUTTON_LABEL = 0xFF1A1A1A;

    private static final int SCROLL_TRACK = 0xFF8B8B8B;
    private static final int SCROLL_THUMB = 0xFF4A4A4A;
    private static final int SCROLL_BAR_WIDTH = 3;

    /** 글자가 단추를 넘치면 이 비율까지 줄인다. */
    private static final float MIN_LABEL_SCALE = 0.55f;

    private final BackpackScreenPosition screen;
    private int scrollRow;

    public PetPanelWidget(BackpackScreenPosition screen)
    {
        super(0, 0, PetPanelLayout.width(), Math.max(1, PetPanelLayout.panelHeight()), Component.empty());
        this.screen = screen;
    }

    /** 화면이 움직였을 수 있으므로 쓰기 직전에 자리를 다시 읽는다. */
    private void syncBounds()
    {
        setX(screen.advancednetherite$getLeftPos());
        setY(screen.advancednetherite$getTopPos() + PetPanelLayout.panelTop());
        setSize(PetPanelLayout.width(), Math.max(1, PetPanelLayout.panelHeight()));
        this.scrollRow = Math.clamp(this.scrollRow, 0, PetPanelLayout.maxScrollRow());
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
    {
        syncBounds();
        List<PetStatusEntry> pets = ClientPetData.getPets();
        if (pets.isEmpty())
        {
            return;
        }

        int left = getX();
        int top = getY();
        int right = left + getWidth();
        int bottom = top + getHeight();

        // 가방 패널과 같은 테두리로 맞춘다.
        graphics.fill(left, top, right, bottom, FRAME);
        graphics.fill(left, top + 1, right - 1, bottom - 1, FACE);
        graphics.fill(left, top + 1, right - 1, top + 3, HIGHLIGHT);
        graphics.fill(right - 3, top + 3, right - 1, bottom - 1, SHADOW);

        List<Component> labels = buildLabels(pets);
        int firstVisible = this.scrollRow * PetPanelLayout.columns();
        int lastVisible = firstVisible + PetPanelLayout.VISIBLE_ROWS * PetPanelLayout.columns();
        int hovered = indexAt(mouseX, mouseY);

        for (int i = firstVisible; i < Math.min(lastVisible, pets.size()); i++)
        {
            int buttonX = left + PetPanelLayout.buttonX(i);
            int buttonY = top + PetPanelLayout.buttonY(i, this.scrollRow);
            drawButton(graphics, buttonX, buttonY, labels.get(i), pets.get(i).enabled(), i == hovered);
        }

        drawScrollBar(graphics, right, top, bottom);

        if (hovered >= 0)
        {
            graphics.setTooltipForNextFrame(Minecraft.getInstance().font,
                    List.of(labels.get(hovered),
                            Component.literal(pets.get(hovered).enabled() ? "활성" : "비활성")),
                    java.util.Optional.empty(), mouseX, mouseY);
        }
    }

    private void drawButton(GuiGraphicsExtractor graphics, int x, int y, Component label,
                            boolean enabled, boolean hovered)
    {
        int width = PetPanelLayout.BUTTON_WIDTH;
        int height = PetPanelLayout.BUTTON_HEIGHT;
        int face = enabled
                ? (hovered ? ENABLED_FACE_HOVER : ENABLED_FACE)
                : (hovered ? DISABLED_FACE_HOVER : DISABLED_FACE);

        graphics.fill(x, y, x + width, y + height, BUTTON_BORDER);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, face);

        // 펫 이름이 길면 단추 안에 들어가도록 줄여서 가운데에 놓는다.
        var font = Minecraft.getInstance().font;
        int textWidth = font.width(label);
        float scale = Math.max(MIN_LABEL_SCALE,
                Math.min(1.0f, (width - 6) / (float) Math.max(1, textWidth)));
        graphics.pose().pushMatrix();
        graphics.pose().translate(x + (width - textWidth * scale) / 2.0f,
                y + (height - font.lineHeight * scale) / 2.0f);
        graphics.pose().scale(scale, scale);
        graphics.text(font, label, 0, 0, BUTTON_LABEL, false);
        graphics.pose().popMatrix();
    }

    private void drawScrollBar(GuiGraphicsExtractor graphics, int right, int top, int bottom)
    {
        int maxScrollRow = PetPanelLayout.maxScrollRow();
        if (maxScrollRow <= 0)
        {
            return;
        }

        int trackTop = top + PetPanelLayout.PADDING;
        int trackBottom = bottom - PetPanelLayout.PADDING;
        int barLeft = right - 3 - SCROLL_BAR_WIDTH;
        graphics.fill(barLeft, trackTop, barLeft + SCROLL_BAR_WIDTH, trackBottom, SCROLL_TRACK);

        int totalRows = PetPanelLayout.totalRows();
        int trackHeight = trackBottom - trackTop;
        int thumbHeight = Math.max(6, trackHeight * PetPanelLayout.VISIBLE_ROWS / totalRows);
        int thumbTop = trackTop + (trackHeight - thumbHeight) * this.scrollRow / maxScrollRow;
        graphics.fill(barLeft, thumbTop, barLeft + SCROLL_BAR_WIDTH, thumbTop + thumbHeight, SCROLL_THUMB);
    }

    /**
     * 화면 좌표 아래에 있는 펫의 전체 목록 기준 번호.
     *
     * @return 단추 위가 아니면 -1
     */
    private int indexAt(double mouseX, double mouseY)
    {
        List<PetStatusEntry> pets = ClientPetData.getPets();
        int firstVisible = this.scrollRow * PetPanelLayout.columns();
        int lastVisible = Math.min(firstVisible + PetPanelLayout.VISIBLE_ROWS * PetPanelLayout.columns(),
                pets.size());

        for (int i = firstVisible; i < lastVisible; i++)
        {
            int buttonX = getX() + PetPanelLayout.buttonX(i);
            int buttonY = getY() + PetPanelLayout.buttonY(i, this.scrollRow);
            if (mouseX >= buttonX && mouseX < buttonX + PetPanelLayout.BUTTON_WIDTH
                    && mouseY >= buttonY && mouseY < buttonY + PetPanelLayout.BUTTON_HEIGHT)
            {
                return i;
            }
        }
        return -1;
    }

    /**
     * 단추에 적을 이름. 같은 종류가 둘 이상이면 뒤에 번호를 붙여 구분한다.
     */
    private static List<Component> buildLabels(List<PetStatusEntry> pets)
    {
        Map<String, Integer> totals = new HashMap<>();
        for (PetStatusEntry entry : pets)
        {
            totals.merge(entry.petTypeId(), 1, Integer::sum);
        }

        Map<String, Integer> seen = new HashMap<>();
        List<Component> labels = new java.util.ArrayList<>(pets.size());
        for (PetStatusEntry entry : pets)
        {
            Component name = petName(entry);
            int order = seen.merge(entry.petTypeId(), 1, Integer::sum);
            labels.add(totals.get(entry.petTypeId()) > 1
                    ? name.copy().append(Component.literal(" " + order))
                    : name);
        }
        return labels;
    }

    private static Component petName(PetStatusEntry entry)
    {
        EntityType<DialgaPetEntity> petType = PetManager.getPetType(entry.petTypeId());
        return petType == null ? Component.literal(entry.petTypeId()) : petType.getDescription();
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY)
    {
        syncBounds();
        return !ClientPetData.getPets().isEmpty() && super.isMouseOver(mouseX, mouseY);
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick)
    {
        syncBounds();
        int index = indexAt(event.x(), event.y());
        if (index < 0)
        {
            return;
        }

        PetStatusEntry entry = ClientPetData.getPets().get(index);
        // 서버 응답을 기다리지 않고 색이 바로 바뀌도록 먼저 뒤집는다.
        ClientPetData.toggleLocally(entry.recordId());
        ClientPlayNetworking.send(new PetTogglePayload(entry.recordId()));
        playButtonClickSound(Minecraft.getInstance().getSoundManager());
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY)
    {
        syncBounds();
        int maxScrollRow = PetPanelLayout.maxScrollRow();
        if (maxScrollRow <= 0 || !isMouseOver(mouseX, mouseY))
        {
            return false;
        }

        this.scrollRow = Math.clamp(this.scrollRow - (int) Math.signum(scrollY), 0, maxScrollRow);
        return true;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output)
    {
        this.defaultButtonNarrationText(output);
    }
}
