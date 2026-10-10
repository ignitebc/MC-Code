package com.tacz.guns.client.gui;

import cn.sh1rocu.tacz.mixin.accessor.ScreenAccessor;
import com.tacz.guns.GunMod;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.client.animation.screen.RefitTransform;
import com.tacz.guns.client.gui.components.FlatColorButton;
import com.tacz.guns.client.gui.components.refit.*;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.index.ClientAttachmentIndex;
import com.tacz.guns.client.sound.SoundPlayManager;
import com.tacz.guns.network.message.ClientMessageLaserColor;
import com.tacz.guns.network.message.ClientMessageRefitGun;
import com.tacz.guns.network.message.ClientMessageUnloadAttachment;
import com.tacz.guns.sound.SoundManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

@Environment(EnvType.CLIENT)
public class GunRefitScreen extends Screen {
    public static final Identifier SLOT_TEXTURE = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "textures/gui/refit_slot.png");
    public static final Identifier TURN_PAGE_TEXTURE = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "textures/gui/refit_turn_page.png");
    public static final Identifier UNLOAD_TEXTURE = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "textures/gui/refit_unload.png");
    public static final Identifier ICONS_TEXTURE = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "textures/gui/refit_slot_icons.png");

    public static final int ICON_UV_SIZE = 32;
    public static final int SLOT_SIZE = 18;
    private static final int INVENTORY_ATTACHMENT_SLOT_COUNT = 8;
    private static boolean HIDE_GUN_PROPERTY_DIAGRAMS = true;

    private int currentPage = 0;

    public GunRefitScreen() {
        super(Component.literal("Gun Refit Screen"));
        RefitTransform.init();
    }

    public static int getSlotTextureXOffset(ItemStack gunItem, AttachmentType attachmentType) {
        IGun iGun = IGun.getIGunOrNull(gunItem);
        if (iGun == null) {
            return -1;
        }
        if (!iGun.allowAttachmentType(gunItem, attachmentType)) {
            return ICON_UV_SIZE * 6;
        }
        switch (attachmentType) {
            case GRIP -> {
                return 0;
            }
            case LASER -> {
                return ICON_UV_SIZE;
            }
            case MUZZLE -> {
                return ICON_UV_SIZE * 2;
            }
            case SCOPE -> {
                return ICON_UV_SIZE * 3;
            }
            case STOCK -> {
                return ICON_UV_SIZE * 4;
            }
            case EXTENDED_MAG -> {
                return ICON_UV_SIZE * 5;
            }
        }
        return -1;
    }

    public static int getSlotsTextureWidth() {
        return ICON_UV_SIZE * 7;
    }

    @Override
    public void init() {
        this.clearWidgets();
        // 부착물 칸 추가
        this.addAttachmentTypeButtons();
        // 고를 수 있는 부착물 목록 추가
        this.addInventoryAttachmentButtons();
        // 속성 그래프 숨기기 버튼 추가
        if (HIDE_GUN_PROPERTY_DIAGRAMS) {
            this.addRenderableWidget(new FlatColorButton(11, 11, 288, 16,
                    Component.translatable("gui.tacz.gun_refit.property_diagrams.show"), b -> switchHideButton()));
        } else {
            this.addRenderableWidget(new FlatColorButton(14, 14, 12, 12, Component.literal("S"), b -> {
                LocalPlayer player = Minecraft.getInstance().player;
                if (player == null || player.isSpectator()) return;
                if (IGun.mainHandHoldGun(player)) {
                    IClientPlayerGunOperator.fromLocalPlayer(player).fireSelect();
                    this.init();
                }
            }).setTooltips(Component.translatable("gui.tacz.gun_refit.property_diagrams.fire_mode.switch")));
            int buttonYOffset = GunPropertyDiagrams.getHidePropertyButtonYOffset();
            this.addRenderableWidget(new FlatColorButton(11, buttonYOffset, 288, 12,
                    Component.translatable("gui.tacz.gun_refit.property_diagrams.hide"), b -> switchHideButton()));
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float pPartialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, pPartialTick);

        if (!HIDE_GUN_PROPERTY_DIAGRAMS) {
            GunPropertyDiagrams.draw(graphics, font, 11, 11);
        }

        // 26.2 툴팁 API: 위젯이 기존 consumer 훅으로 툴팁 내용을 넘긴다.
        ((ScreenAccessor) this).tacz$getRenderables().stream().filter(w -> w instanceof IComponentTooltip).forEach(w -> {
            IComponentTooltip tooltipWidget = (IComponentTooltip) w;
            tooltipWidget.renderTooltip(lines -> graphics.setTooltipForNextFrame(font, lines, java.util.Optional.empty(), mouseX, mouseY));
        });
        ((ScreenAccessor) this).tacz$getRenderables().stream().filter(w -> w instanceof IStackTooltip).forEach(w -> {
            IStackTooltip tooltipWidget = (IStackTooltip) w;
            tooltipWidget.renderTooltip(stack -> {
                if (!stack.isEmpty()) {
                    graphics.setTooltipForNextFrame(font, Screen.getTooltipFromItem(Minecraft.getInstance(), stack), stack.getTooltipImage(), mouseX, mouseY);
                }
            });
        });
    }

    /**
     * 개조 화면에서는 전체 화면 흐림을 <b>쓰지 않는다</b> — 원본 1.21.1과 같은 동작이다.
     *
     * <p>원본 {@code GunRefitScreen}에는 내용이 빈
     * <pre>
     * &#64;Override protected void renderBlurredBackground(float partialTick) { }
     * </pre>
     * 이 있었는데, 이식할 때 빠져 바닐라 기본 구현을 타면서 개조 화면에 배경 흐림이 덮였다.
     *
     * <p>26.2에서 대응하는 메서드 이름은 {@code extractBlurredBackground(GuiGraphicsExtractor)}이며,
     * 호출 흐름은 다음과 같다(바이트코드 확인):
     * <pre>
     * Screen#extractBackground
     *   -> Screen#extractBlurredBackground
     *        -> if (options.getMenuBackgroundBlurriness() != 0)
     *               graphics.blurBeforeThisStratum();
     * </pre>
     * 빈 재정의로 원본의 "흐림 없음" 효과를 정확히 재현할 수 있다.
     *
     * <p>재정의하지 않는 것이 아니라 비워 둬야 한다: 플레이어는 총 모델을 보면서 부착물을 다는데,
     * 배경 흐림은 총까지 함께 흐리게 만든다(흐림은 해당 stratum 이전 전체 화면 후처리다).
     * 부착물 모양을 보기가 크게 어려워지며 — 원본이 일부러 끈 이유가 이것이다.
     */
    @Override
    protected void extractBlurredBackground(GuiGraphicsExtractor graphics) {
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void addInventoryAttachmentButtons() {
        LocalPlayer player = this.minecraft.player;
        if (RefitTransform.getCurrentTransformType() == AttachmentType.NONE || player == null) {
            return;
        }
        int startX = this.width - 30;
        int startY = 50;
        int pageStart = currentPage * INVENTORY_ATTACHMENT_SLOT_COUNT;
        int count = 0;
        int currentY = startY;
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack inventoryItem = inventory.getItem(i);
            IAttachment attachment = IAttachment.getIAttachmentOrNull(inventoryItem);
            IGun iGun = IGun.getIGunOrNull(player.getMainHandItem());
            if (attachment != null && iGun != null && attachment.getType(inventoryItem) == RefitTransform.getCurrentTransformType()) {
                if (!iGun.allowAttachment(player.getMainHandItem(), inventoryItem)) {
                    continue;
                }
                count++;
                if (count <= pageStart) {
                    continue;
                }
                if (count > pageStart + INVENTORY_ATTACHMENT_SLOT_COUNT) {
                    continue;
                }
                InventoryAttachmentSlot button = new InventoryAttachmentSlot(startX, currentY, i, inventory, b -> {
                    int slotIndex = ((InventoryAttachmentSlot) b).getSlotIndex();
                    SoundPlayManager.playerRefitSound(inventory.getItem(slotIndex), player, SoundManager.INSTALL_SOUND);
                    ClientMessageRefitGun message = new ClientMessageRefitGun(slotIndex, inventory.getSelectedSlot(), RefitTransform.getCurrentTransformType());
                    ClientPlayNetworking.send(message);
                });
                this.addRenderableWidget(button);
                currentY = currentY + SLOT_SIZE;
            }
        }
        int totalPage = (count - 1) / INVENTORY_ATTACHMENT_SLOT_COUNT;
        RefitTurnPageButton turnPageButtonUp = new RefitTurnPageButton(startX, startY - 10, true, b -> {
            if (currentPage > 0) {
                currentPage--;
                init();
            }
        });
        RefitTurnPageButton turnPageButtonDown = new RefitTurnPageButton(startX, startY + SLOT_SIZE * INVENTORY_ATTACHMENT_SLOT_COUNT + 2, false, b -> {
            if (currentPage < totalPage) {
                currentPage++;
                init();
            }
        });
        if (currentPage < totalPage) {
            this.addRenderableWidget(turnPageButtonDown);
        }
        if (currentPage > 0) {
            this.addRenderableWidget(turnPageButtonUp);
        }
    }

    private void addAttachmentTypeButtons() {
        LocalPlayer player = this.minecraft.player;
        if (player == null) {
            return;
        }
        IGun iGun = IGun.getIGunOrNull(player.getMainHandItem());
        if (iGun == null) {
            return;
        }
        int startX = this.width - 30;
        int startY = 10;
        Inventory inventory = player.getInventory();
        for (AttachmentType type : AttachmentType.values()) {
            if (type == AttachmentType.NONE) {
                if (RefitTransform.getCurrentTransformType() == AttachmentType.NONE) {
                    TimelessAPI.getGunDisplay(player.getMainHandItem())
                            .map(GunDisplayInstance::getLaserConfig)
                            .ifPresent(laserConfig -> {
                                if (laserConfig.canEdit()) {
                                    // 레이저 색 선택기 추가
                                    HSVSliderGroup hsvSliderGroup = new HSVSliderGroup(width - 140, height - 64, 120, 16, inventory, inventory.getSelectedSlot(), AttachmentType.NONE);
                                    this.addRenderableWidget(hsvSliderGroup.getHueSlider());
                                    this.addRenderableWidget(hsvSliderGroup.getSaturationSlider());
                                }
                            });
                }
                continue;
            }
            GunAttachmentSlot button = new GunAttachmentSlot(startX, startY, type, inventory.getSelectedSlot(), inventory, b -> {
                AttachmentType buttonType = ((GunAttachmentSlot) b).getType();
                // 이 칸에 부착물을 달 수 없으면 개요로 돌아가고 칸을 고르지 않는다.
                if (!((GunAttachmentSlot) b).isAllow()) {
                    if (RefitTransform.changeRefitScreenView(AttachmentType.NONE)) {
                        this.init();
                    }
                    return;
                }
                // 이미 고른 칸을 누르면 개요로 돌아간다
                if (RefitTransform.getCurrentTransformType() == buttonType && buttonType != AttachmentType.NONE) {
                    if (RefitTransform.changeRefitScreenView(AttachmentType.NONE)) {
                        this.init();
                    }
                    return;
                }
                // 고른 칸을 바꾼다.
                if (RefitTransform.changeRefitScreenView(buttonType)) {
                    this.init();
                }
            });
            if (RefitTransform.getCurrentTransformType() == type) {
                button.setSelected(true);
                // 부착물 떼기 버튼 추가
                RefitUnloadButton unloadButton = new RefitUnloadButton(startX + 5, startY + SLOT_SIZE + 2, b -> {
                    ItemStack attachmentItem = button.getAttachmentItem();
                    if (!attachmentItem.isEmpty()) {
                        int freeSlot = inventory.getFreeSlot();
                        if (freeSlot != -1) {
                            SoundPlayManager.playerRefitSound(attachmentItem, player, SoundManager.UNINSTALL_SOUND);
                            ClientMessageUnloadAttachment message = new ClientMessageUnloadAttachment(inventory.getSelectedSlot(), RefitTransform.getCurrentTransformType());
                            ClientPlayNetworking.send(message);
                        } else {
                            player.sendSystemMessage(Component.translatable("gui.tacz.gun_refit.unload.no_space"));
                        }
                    }
                });
                if (!button.getAttachmentItem().isEmpty()) {
                    this.addRenderableWidget(unloadButton);

                    if (button.getAttachmentItem().getItem() instanceof IAttachment iAttachment) {
                        TimelessAPI.getClientAttachmentIndex(iAttachment.getAttachmentId(button.getAttachmentItem()))
                                .map(ClientAttachmentIndex::getLaserConfig)
                                .ifPresent(laserConfig -> {
                                    if (laserConfig.canEdit()) {
                                        // 레이저 색 선택기 추가
                                        HSVSliderGroup hsvSliderGroup = new HSVSliderGroup(width - 140, height - 64, 120, 16, inventory, inventory.getSelectedSlot(), type);
                                        this.addRenderableWidget(hsvSliderGroup.getHueSlider());
                                        this.addRenderableWidget(hsvSliderGroup.getSaturationSlider());
                                    }
                                });
                    }
                }
            }
            this.addRenderableWidget(button);
            startX = startX - SLOT_SIZE;
        }
    }

    @Override
    public void onClose() {
        // 화면을 닫을 때 염색 데이터를 한꺼번에 올린다
        LocalPlayer player = this.minecraft.player;
        if (player != null) {
            ItemStack gun = player.getMainHandItem();
            if (player.getMainHandItem().getItem() instanceof IGun) {
                ClientMessageLaserColor message = new ClientMessageLaserColor(gun, player.getInventory().getSelectedSlot());
                ClientPlayNetworking.send(message);
            }
        }
        super.onClose();
    }

    private void switchHideButton() {
        HIDE_GUN_PROPERTY_DIAGRAMS = !HIDE_GUN_PROPERTY_DIAGRAMS;
        this.init();
    }
}
