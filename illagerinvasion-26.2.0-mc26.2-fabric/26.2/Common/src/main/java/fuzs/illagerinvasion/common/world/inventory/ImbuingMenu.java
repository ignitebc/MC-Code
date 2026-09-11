package fuzs.illagerinvasion.common.world.inventory;

import fuzs.illagerinvasion.common.init.ModRegistry;
import fuzs.illagerinvasion.common.init.ModSoundEvents;
import fuzs.illagerinvasion.common.world.item.enhancement.EnhancementHelper;
import fuzs.puzzleslib.common.api.container.v1.QuickMoveRuleSet;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class ImbuingMenu extends AbstractContainerMenu {

    public static final int ENHANCE_BUTTON_ID = 0;

    public static final int EQUIPMENT_SLOT = 0;
    public static final int ENHANCEMENT_GEM_SLOT = 1;
    public static final int SUCCESS_SCROLL_SLOT = 2;
    public static final int PROTECTION_SCROLL_SLOT = 3;

    private static final int INPUT_SLOT_COUNT = 4;
    private static final int INPUT_SLOT_Y = 38;
    private static final int[] INPUT_SLOT_X = {26, 62, 98, 134};

    private final Container input;
    private final ContainerLevelAccess access;
    private final DataSlot successChance = DataSlot.standalone();
    private final DataSlot destroyChance = DataSlot.standalone();
    private final DataSlot enhanceState = DataSlot.standalone();
    private final DataSlot enhanceResult = DataSlot.standalone();
    private final DataSlot enhanceResultLevel = DataSlot.standalone();
    private final DataSlot enhanceResultSequence = DataSlot.standalone();

    public ImbuingMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, ContainerLevelAccess.NULL);
    }

    public ImbuingMenu(int containerId, Inventory inventory, final ContainerLevelAccess access) {
        super(ModRegistry.IMBUING_MENU_TYPE.value(), containerId);
        this.access = access;
        this.addDataSlot(this.successChance);
        this.addDataSlot(this.destroyChance);
        this.addDataSlot(this.enhanceState);
        this.addDataSlot(this.enhanceResult);
        this.addDataSlot(this.enhanceResultLevel);
        this.addDataSlot(this.enhanceResultSequence);
        this.input = new SimpleContainer(INPUT_SLOT_COUNT) {

            @Override
            public void setChanged() {
                super.setChanged();
                ImbuingMenu.this.slotsChanged(this);
            }
        };

        this.addSlot(new Slot(this.input, EQUIPMENT_SLOT, INPUT_SLOT_X[0], INPUT_SLOT_Y) {

            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return EnhancementHelper.isEnhanceableEquipment(itemStack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
        this.addSlot(new Slot(this.input, ENHANCEMENT_GEM_SLOT, INPUT_SLOT_X[1], INPUT_SLOT_Y) {

            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return EnhancementHelper.isEnhancementGem(itemStack);
            }
        });
        this.addSlot(new Slot(this.input, SUCCESS_SCROLL_SLOT, INPUT_SLOT_X[2], INPUT_SLOT_Y) {

            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return EnhancementHelper.isSuccessScroll(itemStack);
            }
        });
        this.addSlot(new Slot(this.input, PROTECTION_SCROLL_SLOT, INPUT_SLOT_X[3], INPUT_SLOT_Y) {

            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return EnhancementHelper.isProtectionScroll(itemStack);
            }
        });

        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                this.addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 103 + row * 18));
            }
        }
        for (int column = 0; column < 9; ++column) {
            this.addSlot(new Slot(inventory, column, 8 + column * 18, 161));
        }

        this.updateEnhanceInfo();
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModRegistry.IMBUING_TABLE_BLOCK.value());
    }

    @Override
    public void slotsChanged(Container container) {
        if (container == this.input) {
            this.updateEnhanceInfo();
        }
        super.slotsChanged(container);
    }

    /**
     * 재료 구성이 바뀔 때마다 화면에 표시할 성공률과 파괴 확률을 다시 계산한다.
     */
    protected void updateEnhanceInfo() {
        ItemStack equipment = this.input.getItem(EQUIPMENT_SLOT);
        ItemStack enhancementGem = this.input.getItem(ENHANCEMENT_GEM_SLOT);
        ItemStack successScroll = this.input.getItem(SUCCESS_SCROLL_SLOT);
        ItemStack protectionScroll = this.input.getItem(PROTECTION_SCROLL_SLOT);

        EnhanceState state = this.selectEnhanceState(equipment, enhancementGem);
        this.enhanceState.set(state.ordinal());

        if (state != EnhanceState.READY) {
            this.successChance.set(0);
            this.destroyChance.set(0);
            return;
        }

        int attemptLevel = EnhancementHelper.getEnhancementLevel(equipment) + 1;
        boolean protectionPresent = EnhancementHelper.isProtectionScroll(protectionScroll);
        this.successChance.set(EnhancementHelper.getSuccessChance(attemptLevel,
                EnhancementHelper.getSuccessScrollBonus(successScroll)));
        this.destroyChance.set(EnhancementHelper.getEffectiveDestroyChance(attemptLevel, protectionPresent));
    }

    protected EnhanceState selectEnhanceState(ItemStack equipment, ItemStack enhancementGem) {
        if (equipment.isEmpty()) {
            return EnhanceState.EQUIPMENT_MISSING;
        } else if (!EnhancementHelper.isEnhanceableEquipment(equipment)) {
            return EnhanceState.EQUIPMENT_NOT_SUPPORTED;
        } else if (EnhancementHelper.getEnhancementLevel(equipment) >= EnhancementHelper.MAX_ENHANCEMENT_LEVEL) {
            return EnhanceState.MAX_LEVEL_REACHED;
        } else if (enhancementGem.getCount() < EnhancementHelper.ENHANCEMENT_GEM_COST) {
            return EnhanceState.ENHANCEMENT_GEM_MISSING;
        } else {
            return EnhanceState.READY;
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId != ENHANCE_BUTTON_ID || this.getEnhanceState() != EnhanceState.READY) {
            return false;
        }
        this.access.execute((Level level, BlockPos blockPos) -> this.enhanceEquipment(player, level, blockPos));
        return true;
    }

    /**
     * 성공하면 한 단계 오르고, 실패하면 한 단계 내려간다. 파괴 판정이 나와도 파괴 방지권이 있으면
     * 방지권을 소모해 실패로 대체한다.
     */
    private void enhanceEquipment(Player player, Level level, BlockPos blockPos) {
        ItemStack equipment = this.input.getItem(EQUIPMENT_SLOT);
        ItemStack successScroll = this.input.getItem(SUCCESS_SCROLL_SLOT);
        ItemStack protectionScroll = this.input.getItem(PROTECTION_SCROLL_SLOT);
        Component equipmentName = this.getEquipmentName(equipment);

        int currentLevel = EnhancementHelper.getEnhancementLevel(equipment);
        int attemptLevel = currentLevel + 1;
        int successChance = EnhancementHelper.getSuccessChance(attemptLevel,
                EnhancementHelper.getSuccessScrollBonus(successScroll));
        int destroyChance = EnhancementHelper.getBaseDestroyChance(attemptLevel);
        boolean protectionPresent = EnhancementHelper.isProtectionScroll(protectionScroll);

        this.input.removeItem(ENHANCEMENT_GEM_SLOT, EnhancementHelper.ENHANCEMENT_GEM_COST);
        if (!successScroll.isEmpty()) {
            this.input.removeItem(SUCCESS_SCROLL_SLOT, 1);
        }

        // 성공 판정을 먼저 하고, 실패했을 때만 파괴 판정을 따로 굴린다.
        // 한 번의 난수를 구간으로 나누면 파괴가 성공 확률까지 잠식해 실제 파괴율이 표시값보다 높아진다.
        if (level.getRandom().nextInt(100) < successChance) {
            EnhancementHelper.setEnhancementLevel(equipment, attemptLevel);
            this.input.setItem(EQUIPMENT_SLOT, equipment);
            this.setEnhanceResult(EnhanceResult.SUCCESS, attemptLevel);
            this.broadcastEnhanceResult(player, level, equipmentName, EnhanceResult.SUCCESS, attemptLevel);
            level.playSound(null, blockPos, ModSoundEvents.SORCERER_COMPLETE_CAST_SOUND_EVENT.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
            this.updateEnhanceInfo();
            this.broadcastChanges();
            return;
        }

        boolean destroyRolled = level.getRandom().nextInt(100) < destroyChance;
        if (destroyRolled && !protectionPresent) {
            this.input.setItem(EQUIPMENT_SLOT, ItemStack.EMPTY);
            this.setEnhanceResult(EnhanceResult.DESTROYED, 0);
            this.broadcastEnhanceResult(player, level, equipmentName, EnhanceResult.DESTROYED, 0);
            level.playSound(null, blockPos, SoundEvents.ITEM_BREAK.value(), SoundSource.BLOCKS, 1.0f, 1.0f);
        } else {
            if (destroyRolled) {
                this.input.removeItem(PROTECTION_SCROLL_SLOT, 1);
            }
            int failedLevel = Math.max(0, currentLevel - 1);
            EnhancementHelper.setEnhancementLevel(equipment, failedLevel);
            this.input.setItem(EQUIPMENT_SLOT, equipment);
            this.setEnhanceResult(EnhanceResult.FAILURE, failedLevel);
            if (destroyRolled) {
                this.broadcastProtectionResult(player, level, equipmentName, failedLevel);
            } else {
                this.broadcastEnhanceResult(player, level, equipmentName, EnhanceResult.FAILURE, failedLevel);
            }
            level.playSound(null, blockPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1.0f, 1.0f);
        }

        this.updateEnhanceInfo();
        this.broadcastChanges();
    }

    private Component getEquipmentName(ItemStack equipment) {
        Component customName = equipment.getCustomName();
        if (customName != null) {
            return customName;
        }
        return equipment.getItemName();
    }

    private void broadcastEnhanceResult(Player player,
                                        Level level,
                                        Component equipmentName,
                                        EnhanceResult result,
                                        int enhancementLevel) {
        MinecraftServer server = level.getServer();
        if (server == null) {
            return;
        }

        Component message;
        if (result == EnhanceResult.SUCCESS) {
            message = Component.translatable("container.imbue.broadcast.success",
                    player.getDisplayName(),
                    equipmentName,
                    enhancementLevel);
        } else if (result == EnhanceResult.FAILURE) {
            message = Component.translatable("container.imbue.broadcast.failure",
                    player.getDisplayName(),
                    equipmentName,
                    enhancementLevel);
        } else if (result == EnhanceResult.DESTROYED) {
            message = Component.translatable("container.imbue.broadcast.destroyed",
                            player.getDisplayName(),
                            equipmentName)
                    .withStyle(ChatFormatting.RED);
        } else {
            return;
        }

        server.getPlayerList().broadcastSystemMessage(message, false);
    }

    private void broadcastProtectionResult(Player player,
                                           Level level,
                                           Component equipmentName,
                                           int enhancementLevel) {
        MinecraftServer server = level.getServer();
        if (server == null) {
            return;
        }

        Component message = Component.translatable("container.imbue.broadcast.protected",
                        player.getDisplayName(),
                        equipmentName,
                        enhancementLevel)
                .withStyle(ChatFormatting.YELLOW);
        server.getPlayerList().broadcastSystemMessage(message, false);
    }

    private void setEnhanceResult(EnhanceResult result, int enhancementLevel) {
        this.enhanceResult.set(result.ordinal());
        this.enhanceResultLevel.set(enhancementLevel);
        this.enhanceResultSequence.set(this.enhanceResultSequence.get() + 1);
    }

    public EnhanceState getEnhanceState() {
        return EnhanceState.values()[this.enhanceState.get()];
    }

    public int getSuccessChance() {
        return this.successChance.get();
    }

    public int getDestroyChance() {
        return this.destroyChance.get();
    }

    public EnhanceResult getEnhanceResult() {
        return EnhanceResult.values()[this.enhanceResult.get()];
    }

    public int getEnhanceResultLevel() {
        return this.enhanceResultLevel.get();
    }

    public int getEnhanceResultSequence() {
        return this.enhanceResultSequence.get();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return QuickMoveRuleSet.of(this, this::moveItemStackTo)
                .addContainerSlotRule(0, INPUT_SLOT_COUNT - 1, 1)
                .addInventoryRules()
                .addInventoryCompartmentRules()
                .quickMoveStack(player, index);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((Level level, BlockPos blockPos) -> this.clearContainer(player, this.input));
    }

    public enum EnhanceState {
        READY(null),
        EQUIPMENT_MISSING("container.imbue.equipmentMissing"),
        EQUIPMENT_NOT_SUPPORTED("container.imbue.equipmentNotSupported"),
        ENHANCEMENT_GEM_MISSING("container.imbue.enhancementGemMissing"),
        MAX_LEVEL_REACHED("container.imbue.maxLevelReached");

        @Nullable
        final String translationKey;

        EnhanceState(@Nullable String translationKey) {
            this.translationKey = translationKey;
        }

        public Component getComponent() {
            return this.translationKey != null ? Component.translatable(this.translationKey) : CommonComponents.EMPTY;
        }

        public boolean canEnhance() {
            return this == READY;
        }
    }

    public enum EnhanceResult {
        NONE,
        SUCCESS,
        FAILURE,
        DESTROYED
    }
}
