package com.daqem.arc.mixin;

import com.daqem.arc.api.block.ArcBrewingStandOwnerTracker;
import com.daqem.arc.api.block.ArcHopperFedContainer;
import com.daqem.arc.event.triggers.PlayerEvents;
import com.daqem.arc.api.player.ArcServerPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(BrewingStandBlockEntity.class)
public abstract class MixinBrewingStandBlockEntity implements ArcBrewingStandOwnerTracker {

    @Unique
    private static final int arc$POTION_SLOT_COUNT = 3;

    @Unique
    private final UUID[] arc$potionSlotOwners = new UUID[arc$POTION_SLOT_COUNT];

    @Unique
    @Nullable
    private UUID arc$lastPlayerToInteract;

    @Override
    public void arc$setLastPlayerToInteract(UUID playerUuid) {
        this.arc$lastPlayerToInteract = playerUuid;
    }

    @Override
    @Nullable
    public UUID arc$getLastPlayerToInteract() {
        return this.arc$lastPlayerToInteract;
    }

    @Override
    @Nullable
    public UUID arc$getPotionSlotOwner(int slot) {
        if (slot < 0 || slot >= arc$POTION_SLOT_COUNT) {
            return null;
        }
        return this.arc$potionSlotOwners[slot];
    }

    @Override
    public void arc$setPotionSlotOwner(int slot, @Nullable UUID ownerUuid) {
        if (slot < 0 || slot >= arc$POTION_SLOT_COUNT) {
            return;
        }
        this.arc$potionSlotOwners[slot] = ownerUuid;
    }

    @Inject(at = @At("HEAD"), method = "serverTick(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/block/entity/BrewingStandBlockEntity;)V")
    private static void serverTick(Level level, BlockPos blockPos, BlockState blockState, BrewingStandBlockEntity brewingStandBlockEntity, CallbackInfo info) {
        ArcBrewingStandOwnerTracker tracker = (ArcBrewingStandOwnerTracker) brewingStandBlockEntity;
        for (int slot = 0; slot < arc$POTION_SLOT_COUNT; slot++) {
            if (brewingStandBlockEntity.getItem(slot).isEmpty()) {
                tracker.arc$setPotionSlotOwner(slot, null);
            } else if (tracker.arc$getPotionSlotOwner(slot) == null && tracker.arc$getLastPlayerToInteract() != null) {
                tracker.arc$setPotionSlotOwner(slot, tracker.arc$getLastPlayerToInteract());
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "doBrew(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/NonNullList;)V")
    private static void doBrew(Level level, BlockPos blockPos, NonNullList<ItemStack> nonNullList, CallbackInfo ci) {
        if (!(level.getBlockEntity(blockPos) instanceof BrewingStandBlockEntity brewingStand)) {
            return;
        }
        if (brewingStand instanceof ArcHopperFedContainer hopperFedContainer
                && hopperFedContainer.arc$consumeHopperFedItems(1) > 0) {
            return;
        }

        MinecraftServer server = level.getServer();
        if (server == null) {
            return;
        }

        ArcBrewingStandOwnerTracker tracker = (ArcBrewingStandOwnerTracker) brewingStand;
        for (int slot = 0; slot < arc$POTION_SLOT_COUNT; slot++) {
            if (!(nonNullList.get(slot).getItem() instanceof PotionItem)) {
                continue;
            }
            UUID ownerUuid = tracker.arc$getPotionSlotOwner(slot);
            if (ownerUuid == null) {
                continue;
            }
            if (server.getPlayerList().getPlayer(ownerUuid) instanceof ArcServerPlayer owner) {
                PlayerEvents.onBrewPotion(owner, nonNullList.get(slot), blockPos, level);
            }
        }
    }
}
