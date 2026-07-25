package com.daqem.itemrestrictions.mixin.block;

import com.daqem.arc.api.action.data.ActionDataBuilder;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.player.ArcPlayer;
import com.daqem.itemrestrictions.data.RestrictionResult;
import com.daqem.itemrestrictions.data.RestrictionType;
import com.daqem.itemrestrictions.level.block.ItemRestrictionsFurnaceBlockEntity;
import com.daqem.itemrestrictions.level.menu.ItemRestrictionsAbstractFurnaceMenu;
import com.daqem.itemrestrictions.level.player.ItemRestrictionsServerPlayer;
import com.daqem.itemrestrictions.networking.clientbound.ClientboundRestrictionPacket;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.UUID;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class MixinAbstractFurnaceBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, RecipeCraftingHolder, StackedContentsCompatible, ItemRestrictionsFurnaceBlockEntity {

    @Unique
    @Nullable
    private UUID itemrestrictions$playerUUID;

    @Unique
    @Nullable
    private ServerPlayer itemrestrictions$player;

    @Unique
    private boolean itemrestrictions$isRestricted = false;

    @Shadow
    protected NonNullList<ItemStack> items;

    @Shadow
    int litTimeRemaining;

    protected MixinAbstractFurnaceBlockEntity(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Unique
    private RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> itemrestrictions$quickCheck;

    @Inject(at = @At("TAIL"), method = "<init>")
    private void init(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState, RecipeType<? extends AbstractCookingRecipe> recipeType, CallbackInfo ci) {
        this.itemrestrictions$quickCheck = RecipeManager.createCheck(recipeType);
    }

    @Inject(at = @At("TAIL"), method = "saveAdditional")
    private void saveAdditional(ValueOutput valueOutput, CallbackInfo ci) {
        ServerPlayer serverPlayer = itemrestrictions$getPlayer();
        if (serverPlayer != null) {
            valueOutput.putString("ItemRestrictionsServerPlayer", serverPlayer.getUUID().toString());
        } else {
            UUID uuid = itemrestrictions$getPlayerUUID();
            if (uuid != null) {
                valueOutput.putString("ItemRestrictionsServerPlayer", uuid.toString());
            }
        }
    }

    @Inject(at = @At("TAIL"), method = "loadAdditional")
    private void load(ValueInput valueInput, CallbackInfo ci) {
        valueInput.getString("ItemRestrictionsServerPlayer").ifPresent(uuid ->
                itemrestrictions$setPlayerUUID(UUID.fromString(uuid)));
    }

    @Inject(at = @At(value = "HEAD"), method = "serverTick", cancellable = true)
    private static void serverTickRecipe(ServerLevel serverLevel, BlockPos blockPos, BlockState blockState, AbstractFurnaceBlockEntity abstractFurnaceBlockEntity, CallbackInfo ci) {
        if (abstractFurnaceBlockEntity instanceof ItemRestrictionsFurnaceBlockEntity block) {
            if (block.itemrestrictions$getPlayer() == null && block.itemrestrictions$getPlayerUUID() != null) {
                ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(block.itemrestrictions$getPlayerUUID());
                block.itemrestrictions$setPlayer(player);

            }
            if (block.itemrestrictions$getPlayer() != null && !abstractFurnaceBlockEntity.getItem(0).isEmpty()) {
                if (!abstractFurnaceBlockEntity.getItem(1).isEmpty()) {

                    RecipeHolder<?> recipeHolder = block.itemrestrictions$getRecipe();
                    if (recipeHolder != null) {
                        Recipe<?> recipe = recipeHolder.value();
                        RestrictionResult result = new RestrictionResult();

                        if (block.itemrestrictions$getPlayer() instanceof ItemRestrictionsServerPlayer player) {
                            if (player instanceof ArcPlayer arcPlayer) {
                                result = player.itemrestrictions$isRestricted(
                                        new ActionDataBuilder(arcPlayer, null)
                                                .withData(ActionDataType.ITEM_STACK, recipe.assemble(null, ((ServerPlayer) player).level().getServer().registryAccess()))
                                                .build());
                            }
                        }

                        if (result.isRestricted(RestrictionType.SMELT)) {
                            if (block.itemrestrictions$isLit()) {
                                block.itemrestrictions$setLitTime(block.itemrestrictions$getLitTime() - 1);
                            }
                            blockState = blockState.setValue(AbstractFurnaceBlock.LIT, false);
                            serverLevel.setBlock(blockPos, blockState, 3);
                            setChanged(serverLevel, blockPos, blockState);
                            ci.cancel();

                            itemrestrictions$sendPacketCantCraft(RestrictionType.SMELT, block);
                            block.itemrestrictions$setRestricted(true);
                        }
                    }
                } else {
                    if (block.itemrestrictions$isRestricted()) {
                        itemrestrictions$sendPacketCantCraft(RestrictionType.NONE, block);
                        block.itemrestrictions$setRestricted(false);
                    }
                }
            } else if (block.itemrestrictions$getPlayer() != null) {
                if (block.itemrestrictions$isRestricted()) {
                    itemrestrictions$sendPacketCantCraft(RestrictionType.NONE, block);
                    block.itemrestrictions$setRestricted(false);
                }
            }
        }
    }

    @Unique
    private static void itemrestrictions$sendPacketCantCraft(RestrictionType type, ItemRestrictionsFurnaceBlockEntity block) {
        if (block.itemrestrictions$getPlayer().containerMenu instanceof ItemRestrictionsAbstractFurnaceMenu menu) {
            if (menu.itemrestrictions$getContainer().equals(block.itemrestrictions$getAbstractFurnaceBlockEntity())) {
                NetworkManager.sendToPlayer(block.itemrestrictions$getPlayer(), new ClientboundRestrictionPacket(type));
            }
        }
    }

    @Override
    public @Nullable ServerPlayer itemrestrictions$getPlayer() {
        return itemrestrictions$player;
    }

    @Override
    public void itemrestrictions$setPlayer(@Nullable ServerPlayer player) {
        this.itemrestrictions$player = player;
    }

    @Override
    public @Nullable UUID itemrestrictions$getPlayerUUID() {
        return itemrestrictions$playerUUID;
    }

    @Override
    public void itemrestrictions$setPlayerUUID(@Nullable UUID playerUUID) {
        this.itemrestrictions$playerUUID = playerUUID;
    }

    @Override
    public int itemrestrictions$getLitTime() {
        return litTimeRemaining;
    }

    @Override
    public void itemrestrictions$setLitTime(int litTime) {
        this.litTimeRemaining = litTime;
    }

    @Override
    public boolean itemrestrictions$isLit() {
        return itemrestrictions$getLitTime() > 0;
    }

    @Override
    @Nullable
    public RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> itemrestrictions$getQuickCheck() {
        return itemrestrictions$quickCheck;
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public AbstractFurnaceBlockEntity itemrestrictions$getAbstractFurnaceBlockEntity() {
        return (AbstractFurnaceBlockEntity) (Object) this;
    }

    @Override
    @Nullable
    public RecipeHolder<?> itemrestrictions$getRecipe() {
        if (getLevel() == null || getLevel().isClientSide()) return null;
        if (getItem(0).isEmpty()) return null;
        if (getItem(1).isEmpty()) return null;
        if (itemrestrictions$getQuickCheck() == null) return null;
        return Objects.requireNonNull(itemrestrictions$getQuickCheck()).getRecipeFor(new SingleRecipeInput(this.items.getFirst()), (ServerLevel) getLevel()).orElse(null);
    }

    @Override
    public void itemrestrictions$setRestricted(boolean restricted) {
        this.itemrestrictions$isRestricted = restricted;
    }

    @Override
    public boolean itemrestrictions$isRestricted() {
        return itemrestrictions$isRestricted;
    }
}
