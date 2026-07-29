package fuzs.illagerinvasion.client.gui.screens.inventory;

import fuzs.illagerinvasion.IllagerInvasion;
import fuzs.illagerinvasion.world.inventory.ImbuingMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ImbuingScreen extends AbstractContainerScreen<ImbuingMenu> {

    private static final ResourceLocation TEXTURE_LOCATION = IllagerInvasion.id(
            "textures/gui/container/imbuing_table.png");

    private static final int CHANCE_TEXT_Y = 20;
    private static final int SLOT_LABEL_Y = 30;
    private static final int SEPARATOR_Y = 42;
    private static final int BUTTON_X = 52;
    private static final int BUTTON_Y = 62;
    private static final int BUTTON_WIDTH = 72;
    private static final int BUTTON_HEIGHT = 18;
    private static final int LABEL_COLOR = 0xFF404040;
    private static final int SUCCESS_COLOR = 0xFF1B7A2F;
    private static final int DESTROY_COLOR = 0xFFB22222;

    /** 네 슬롯의 가로 중심 좌표 */
    private static final int[] SLOT_CENTER_X = {34, 70, 106, 142};
    private static final String[] SLOT_LABEL_KEYS = {"container.imbue.slot.equipment",
            "container.imbue.slot.enhancementGem",
            "container.imbue.slot.successScroll",
            "container.imbue.slot.protectionScroll"};
    /** 필수 재료는 +로, 선택 재료는 /로 구분한다. */
    private static final int[] SEPARATOR_X = {52, 88, 124};
    private static final String[] SEPARATOR_TEXT = {"+", "/", "/"};

    private Button enhanceButton;
    private int handledEnhanceResultSequence;

    public ImbuingScreen(ImbuingMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 186;
        this.inventoryLabelY = 91;
    }

    @Override
    protected void init() {
        super.init();
        this.enhanceButton = Button.builder(Component.translatable("container.imbue.enhance"),
                        (Button button) -> this.onEnhanceButtonPressed())
                .bounds(this.leftPos + BUTTON_X, this.topPos + BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build();
        this.addRenderableWidget(this.enhanceButton);
    }

    private void onEnhanceButtonPressed() {
        if (this.minecraft == null) {
            return;
        }
        this.minecraft.setScreen(new ConfirmScreen(this::onEnhanceConfirmation,
                Component.translatable("container.imbue.confirm.title"),
                Component.translatable("container.imbue.confirm.message"),
                Component.translatable("container.imbue.confirm.enhance"),
                Component.translatable("container.imbue.confirm.no")));
    }

    private void onEnhanceConfirmation(boolean confirmed) {
        if (this.minecraft == null) {
            return;
        }

        this.minecraft.setScreen(this);
        if (!confirmed || this.minecraft.gameMode == null) {
            return;
        }

        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, ImbuingMenu.ENHANCE_BUTTON_ID);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.enhanceButton.active = this.menu.getEnhanceState().canEnhance();
        this.showEnhanceResult();
    }

    private void showEnhanceResult() {
        int resultSequence = this.menu.getEnhanceResultSequence();
        if (resultSequence == this.handledEnhanceResultSequence) {
            return;
        }

        this.handledEnhanceResultSequence = resultSequence;
        Component resultMessage = this.createEnhanceResultMessage();
        if (resultMessage == null || this.minecraft == null) {
            return;
        }

        this.minecraft.setScreen(new AlertScreen(() -> this.minecraft.setScreen(this),
                Component.translatable("container.imbue.result.title"),
                resultMessage,
                Component.translatable("container.imbue.result.confirm"),
                false));
    }

    private Component createEnhanceResultMessage() {
        ImbuingMenu.EnhanceResult result = this.menu.getEnhanceResult();
        int enhancementLevel = this.menu.getEnhanceResultLevel();

        if (result == ImbuingMenu.EnhanceResult.SUCCESS) {
            return Component.translatable("container.imbue.result.success", enhancementLevel);
        }
        if (result == ImbuingMenu.EnhanceResult.FAILURE) {
            return Component.translatable("container.imbue.result.failure", enhancementLevel);
        }
        if (result == ImbuingMenu.EnhanceResult.DESTROYED) {
            return Component.translatable("container.imbue.result.destroyed").withStyle(ChatFormatting.RED);
        }
        return null;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);

        ImbuingMenu.EnhanceState state = this.menu.getEnhanceState();
        boolean hoveringButton = this.enhanceButton.isMouseOver(mouseX, mouseY);
        if (!state.canEnhance() && hoveringButton) {
            guiGraphics.setTooltipForNextFrame(state.getComponent(), mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED,
                TEXTURE_LOCATION,
                this.leftPos,
                this.topPos,
                0,
                0,
                this.imageWidth,
                this.imageHeight,
                256,
                256);
        for (int index = 0; index < SEPARATOR_X.length; ++index) {
            this.drawCentered(guiGraphics,
                    Component.literal(SEPARATOR_TEXT[index]),
                    SEPARATOR_X[index],
                    SEPARATOR_Y,
                    LABEL_COLOR);
        }
        for (int index = 0; index < SLOT_CENTER_X.length; ++index) {
            this.drawCentered(guiGraphics,
                    Component.translatable(SLOT_LABEL_KEYS[index]),
                    SLOT_CENTER_X[index],
                    SLOT_LABEL_Y,
                    LABEL_COLOR);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);

        Component successText = Component.translatable("container.imbue.successChance", this.menu.getSuccessChance());
        Component destroyText = Component.translatable("container.imbue.destroyChance", this.menu.getDestroyChance());
        Component separator = Component.literal(" , ");

        int totalWidth = this.font.width(successText) + this.font.width(separator) + this.font.width(destroyText);
        int textX = (this.imageWidth - totalWidth) / 2;

        guiGraphics.drawString(this.font, successText, textX, CHANCE_TEXT_Y, SUCCESS_COLOR, false);
        textX += this.font.width(successText);
        guiGraphics.drawString(this.font, separator, textX, CHANCE_TEXT_Y, LABEL_COLOR, false);
        textX += this.font.width(separator);
        guiGraphics.drawString(this.font, destroyText, textX, CHANCE_TEXT_Y, DESTROY_COLOR, false);
    }

    /**
     * renderBg는 화면 원점을 기준으로 그리므로 GUI 좌상단 좌표를 더해 준다.
     */
    private void drawCentered(GuiGraphics guiGraphics, Component text, int centerX, int y, int color) {
        int textX = this.leftPos + centerX - this.font.width(text) / 2;
        guiGraphics.drawString(this.font, text, textX, this.topPos + y, color, false);
    }
}
