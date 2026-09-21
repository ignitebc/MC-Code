package com.daqem.arc.mixin;

import com.daqem.arc.api.action.data.ActionDataBuilder;
import com.daqem.arc.api.action.data.type.ActionDataType;
import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.action.type.ActionType;
import com.daqem.arc.api.action.type.IActionType;
import com.daqem.arc.api.condition.ICondition;
import com.daqem.arc.api.player.ArcPlayer;
import com.daqem.arc.data.PlayerActionHolderManager;
import com.daqem.arc.event.triggers.MovementEvents;
import com.daqem.arc.event.triggers.PlayerEvents;
import com.daqem.arc.event.triggers.StatEvents;
import com.daqem.arc.api.player.ArcServerPlayer;
import com.daqem.arc.networking.ClientboundSyncPlayerActionHoldersPacket;
import com.daqem.arc.player.BlockPosCache;
import com.daqem.arc.player.MovementCreditTracker;
import com.daqem.arc.player.CachedBlockPos;
import com.daqem.arc.player.PlayerActionCache;
import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.stats.Stat;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.AirItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer extends Player implements ArcServerPlayer {

    @Shadow public ServerGamePacketListenerImpl connection;

    // 바닐라 moveDist 는 실제 수평 이동 거리에 0.6 을 곱해 누적한다(Entity#applyMovementEmissionAndPlaySound).
    // 거리 조건이 실제 블록 수를 기준으로 동작하도록 이벤트로 넘기기 전에 되돌린다.
    @Unique
    private static final float ARC_MOVE_DIST_SCALE = 0.6F;

    @Unique
    private Map<Identifier, IActionHolder> arc$actionHolders = new HashMap<>();
    @Unique
    private final PlayerActionCache arc$actionCache = new PlayerActionCache();
    @Unique
    private Map<ICondition, Integer> arc$lastDistanceInCm = new HashMap<>();
    @Unique
    private Map<ICondition, Integer> arc$lastRemainderInCm = new HashMap<>();
    @Unique
    private boolean arc$isSwimming = false;
    @Unique
    private int arc$swimmingDistanceInCm = 0;
    @Unique
    private boolean arc$isWalking = false;
    @Unique
    private float arc$walkingDistance = 0;
    @Unique
    private boolean arc$isSprinting = false;
    @Unique
    private float arc$sprintingDistance = 0;
    @Unique
    private boolean arc$isCrouching = false;
    @Unique
    private float arc$crouchingDistance = 0;
    @Unique
    private boolean arc$isElytraFlying = false;
    @Unique
    private float arc$elytraFlyingDistance = 0;
    @Unique
    private boolean arc$isGrinding = false;
    @Unique
    public boolean arc$isHorseRiding = false;
    @Unique
    public float arc$horseRidingDistance = 0;
    @Unique
    public BlockPosCache arc$blockPosCache = new BlockPosCache();
    @Unique
    private final MovementCreditTracker arc$movementCreditTracker = new MovementCreditTracker();
    // 이벤트로 넘기는 값은 원본 누적기가 아니라 "인정된 이동"만 더한 누적기다.
    @Unique
    private double arc$creditedSwimCm = 0.0D;
    @Unique
    private double arc$creditedWalkCm = 0.0D;
    @Unique
    private double arc$creditedSprintCm = 0.0D;
    @Unique
    private double arc$creditedCrouchCm = 0.0D;
    @Unique
    private double arc$creditedRideCm = 0.0D;
    @Unique
    private double arc$creditedElytraCm = 0.0D;
    @Unique
    private double arc$rawSwimCm = 0.0D;
    @Unique
    private double arc$rawWalkCm = 0.0D;
    @Unique
    private double arc$rawSprintCm = 0.0D;
    @Unique
    private double arc$rawCrouchCm = 0.0D;
    @Unique
    private double arc$rawRideCm = 0.0D;
    @Unique
    private double arc$rawElytraCm = 0.0D;
    @Unique
    private long arc$blockInteractionCounter = 0L;
    @Unique
    private int arc$automatedFishingStreak = 0;
    @Unique
    private long arc$lastFishingNoticeTick = Long.MIN_VALUE;
    @Unique
    private static final String arc$BLOCK_POS_CACHE_TAG = "ArcBlockPosCacheByDimension";
    @Unique
    private static final String arc$LEGACY_BLOCK_POS_CACHE_TAG = "ArcBlockPosCache";
    @Unique
    private static final Codec<List<CachedBlockPos>> arc$BLOCK_POS_CACHE_CODEC = CachedBlockPos.CODEC.listOf();
    @Unique
    private static final Codec<List<Long>> arc$LEGACY_BLOCK_POS_CACHE_CODEC = Codec.LONG.listOf();

    public MixinServerPlayer(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Override
    public List<IActionHolder> arc$getActionHolders() {
        return new ArrayList<>(arc$actionHolders.values());
    }

    @Override
    public List<PlayerActionCache.ActionEntry> arc$getActionsOfType(IActionType<?> actionType) {
        arc$ensureActionCacheUpToDate();
        return this.arc$actionCache.getActionsOfType(actionType);
    }

    @Override
    public float arc$getSwimSpeedMultiplier() {
        arc$ensureActionCacheUpToDate();
        return this.arc$actionCache.getSwimSpeedMultiplier();
    }

    @Unique
    private void arc$ensureActionCacheUpToDate() {
        if (this.arc$actionCache.isUpToDate()) {
            return;
        }
        // 데이터팩 reload 는 액션과 condition 객체를 새로 파싱하므로, 이전 condition 이
        // key 인 거리 캐시는 다시 조회될 수 없는 항목만 남는다. 남겨두면 누수가 된다.
        if (this.arc$actionCache.isDataGenerationOutdated()) {
            this.arc$lastDistanceInCm.clear();
            this.arc$lastRemainderInCm.clear();
        }
        this.arc$actionCache.rebuild(this.arc$actionHolders.values());
    }

    @Override
    public void arc$addActionHolder(IActionHolder actionHolder) {
        if (actionHolder == null) return;
        this.arc$actionHolders.put(actionHolder.getLocation(), actionHolder);
        this.arc$actionCache.invalidate();
        arc$syncActionHoldersWithClient();
    }

    @Override
    public void arc$addActionHolders(List<IActionHolder> actionHolders) {
        if (actionHolders == null) return;
        for (IActionHolder actionHolder : actionHolders) {
            arc$addActionHolder(actionHolder);
        }
    }

    @Override
    public void arc$removeActionHolder(IActionHolder actionHolder) {
        this.arc$actionHolders.remove(actionHolder.getLocation());
        this.arc$actionCache.invalidate();
    }

    @Override
    public void arc$clearActionHolders() {
        this.arc$actionHolders.clear();
        this.arc$actionCache.invalidate();
    }

    @Override
    public ServerPlayer arc$getServerPlayer() {
        return (ServerPlayer) (Object) this;
    }

    @Override
    public void arc$setSwimmingDistanceInCm(int swimmingDistanceInCm) {
        this.arc$swimmingDistanceInCm = swimmingDistanceInCm;
    }

    @Override
    public void arc$setElytraFlyingDistanceInCm(float flyingDistanceInCm) {
        this.arc$elytraFlyingDistance = flyingDistanceInCm;
    }

    @Override
    public int arc$getLastDistanceInCm(ICondition distanceCondition) {
        Integer lastDistanceInCm = arc$lastDistanceInCm.get(distanceCondition);
        return lastDistanceInCm == null ? 0 : lastDistanceInCm;
    }

    @Override
    public void arc$setLastDistanceInCm(ICondition distanceCondition, int lastDistanceInCm) {
        arc$lastDistanceInCm.put(distanceCondition, lastDistanceInCm);
    }

    @Override
    public int arc$getLastRemainderInCm(ICondition distanceCondition) {
        Integer lastRemainderInCm = arc$lastRemainderInCm.get(distanceCondition);
        return lastRemainderInCm == null ? 0 : lastRemainderInCm;
    }

    @Override
    public void arc$setLastRemainderInCm(ICondition distanceCondition, int lastRemainderInCm) {
        arc$lastRemainderInCm.put(distanceCondition, lastRemainderInCm);
    }

    @Override
    public Map<Identifier, IActionHolder> arc$getActionHoldersMap() {
        return this.arc$actionHolders;
    }

    @Override
    public Map<ICondition, Integer> arc$getLastDistancesInCm() {
        return this.arc$lastDistanceInCm;
    }

    @Override
    public Map<ICondition, Integer> arc$getLastRemaindersInCm() {
        return this.arc$lastRemainderInCm;
    }

    @Override
    public boolean arc$isSwimming() {
        return this.arc$isSwimming;
    }

    @Override
    public int arc$getSwimmingDistanceInCm() {
        return this.arc$swimmingDistanceInCm;
    }

    @Override
    public boolean arc$isWalking() {
        return this.arc$isWalking;
    }

    @Override
    public float arc$getWalkingDistance() {
        return this.arc$walkingDistance;
    }

    @Override
    public boolean arc$isSprinting() {
        return this.arc$isSprinting;
    }

    @Override
    public float arc$getSprintingDistance() {
        return this.arc$sprintingDistance;
    }

    @Override
    public boolean arc$isCrouching() {
        return this.arc$isCrouching;
    }

    @Override
    public float arc$getCrouchingDistance() {
        return this.arc$crouchingDistance;
    }

    @Override
    public boolean arc$isElytraFlying() {
        return this.arc$isElytraFlying;
    }

    @Override
    public float arc$getElytraFlyingDistance() {
        return this.arc$elytraFlyingDistance;
    }

    @Override
    public boolean arc$isGrinding() {
        return this.arc$isGrinding;
    }

    @Override
    public boolean arc$isHorseRiding() {
        return this.arc$isHorseRiding;
    }

    @Override
    public void arc$syncActionHoldersWithClient() {
        if (this.connection == null) return;
        NetworkManager.sendToPlayer(arc$getServerPlayer(), new ClientboundSyncPlayerActionHoldersPacket(arc$getActionHolders()));
    }

    @Override
    public BlockPosCache arc$getBlockPosCache() {
        return this.arc$blockPosCache;
    }

    @Override
    public MovementCreditTracker arc$getMovementCreditTracker() {
        return this.arc$movementCreditTracker;
    }

    @Override
    public long arc$getBlockInteractionCounter() {
        return this.arc$blockInteractionCounter;
    }

    @Override
    public void arc$incrementBlockInteractionCounter() {
        this.arc$blockInteractionCounter++;
    }

    @Override
    public int arc$getAutomatedFishingStreak() {
        return this.arc$automatedFishingStreak;
    }

    @Override
    public void arc$setAutomatedFishingStreak(int streak) {
        this.arc$automatedFishingStreak = Math.max(0, streak);
    }

    @Override
    public long arc$getLastFishingNoticeTick() {
        return this.arc$lastFishingNoticeTick;
    }

    @Override
    public void arc$setLastFishingNoticeTick(long gameTime) {
        this.arc$lastFishingNoticeTick = gameTime;
    }

    /**
     * 원본 누적기(cm)의 이번 틱 증가분에 인정 비율을 곱해 인정 누적기에 더하고,
     * 이벤트로 넘길 값을 돌려준다. 인정 비율이 0 이면 누적기가 그대로 멈춘다.
     */
    @Unique
    private double arc$advanceCredited(double rawAbsoluteCm, double previousRawCm,
                                       double creditedCm, double factor) {
        double delta = Math.max(0.0D, rawAbsoluteCm - previousRawCm);
        return creditedCm + delta * factor;
    }

    @Override
    public double arc$nextRandomDouble() {
        return this.arc$getServerPlayer().getRandom().nextDouble();
    }

    @Override
    public @NotNull Level arc$getLevel() {
        return super.level();
    }

    @Override
    public String arc$getName() {
        return super.getName().getString();
    }

    @Override
    public Player arc$getPlayer() {
        return arc$getServerPlayer();
    }

    @Inject(at = @At("TAIL"), method = "tick()V")
    public void tick(CallbackInfo ci) {
        // 이동 보상 인정 비율은 틱당 한 번만 계산한다. 제자리 왕복이면 0 이 나온다.
        final double arc$creditFactor = this.arc$movementCreditTracker.creditFactor(arc$getServerPlayer());

        if (this.arc$isSwimming && this.isSwimming()) {
            this.arc$creditedSwimCm = arc$advanceCredited(this.arc$swimmingDistanceInCm,
                    this.arc$rawSwimCm, this.arc$creditedSwimCm, arc$creditFactor);
            this.arc$rawSwimCm = this.arc$swimmingDistanceInCm;
            MovementEvents.onSwim(this, (int) this.arc$creditedSwimCm);
        } else {
            if (this.arc$isSwimming) {
                this.arc$isSwimming = false;
                MovementEvents.onStopSwimming(this);
            } else {
                if (this.isSwimming()) {
                    this.arc$isSwimming = true;
                    MovementEvents.onStartSwimming(this);
                }
            }
        }

        boolean isCurrentlyWalking = this.moveDist > this.arc$walkingDistance;
        float distance = this.moveDist - this.arc$walkingDistance;
        if (this.arc$isWalking && isCurrentlyWalking) {
            this.arc$walkingDistance = this.moveDist;
            double arc$rawWalk = this.arc$walkingDistance / ARC_MOVE_DIST_SCALE * 100;
            this.arc$creditedWalkCm = arc$advanceCredited(arc$rawWalk, this.arc$rawWalkCm,
                    this.arc$creditedWalkCm, arc$creditFactor);
            this.arc$rawWalkCm = arc$rawWalk;
            MovementEvents.onWalk(this, (int) this.arc$creditedWalkCm);
        } else {
            if (this.arc$isWalking) {
                this.arc$isWalking = false;
                MovementEvents.onStopWalking(this);
            } else if (isCurrentlyWalking) {
                this.arc$isWalking = true;
                MovementEvents.onStartWalking(this);
            }
        }

        if (this.arc$isSprinting && this.isSprinting()) {
            this.arc$sprintingDistance += distance;
            double arc$rawSprint = this.arc$sprintingDistance / ARC_MOVE_DIST_SCALE * 100;
            this.arc$creditedSprintCm = arc$advanceCredited(arc$rawSprint, this.arc$rawSprintCm,
                    this.arc$creditedSprintCm, arc$creditFactor);
            this.arc$rawSprintCm = arc$rawSprint;
            MovementEvents.onSprint(this, (int) this.arc$creditedSprintCm);
        } else {
            if (this.arc$isSprinting) {
                this.arc$isSprinting = false;
                MovementEvents.onStopSprinting(this);
            } else if (this.isSprinting()) {
                this.arc$isSprinting = true;
                MovementEvents.onStartSprinting(this);
            }
        }

        if (this.arc$isHorseRiding && this.getRootVehicle() instanceof Horse horse && horse.isSaddled() && horse.isTamed()) {
            boolean isCurrentlyRiding = horse.moveDist > this.arc$horseRidingDistance;
            float horseRidingDistance = horse.moveDist - this.arc$horseRidingDistance;
            if (isCurrentlyRiding) {
                this.arc$horseRidingDistance += horseRidingDistance;
                double arc$rawRide = this.arc$horseRidingDistance / ARC_MOVE_DIST_SCALE * 100;
                this.arc$creditedRideCm = arc$advanceCredited(arc$rawRide, this.arc$rawRideCm,
                        this.arc$creditedRideCm, arc$creditFactor);
                this.arc$rawRideCm = arc$rawRide;
                MovementEvents.onHorseRide(this, (int) this.arc$creditedRideCm);
            }
        } else {
            if (this.arc$isHorseRiding) {
                this.arc$isHorseRiding = false;
                MovementEvents.onStopHorseRiding(this);
            } else {
                if (this.getRootVehicle() instanceof Horse horse && horse.isSaddled() && horse.isTamed()) {
                    this.arc$isHorseRiding = true;
                    this.arc$horseRidingDistance = 0;
                    this.arc$rawRideCm = 0.0D;
                    horse.moveDist = 0;
                    MovementEvents.onStartHorseRiding(this);
                }
            }
        }

        if (this.arc$isCrouching && this.isCrouching()) {
            this.arc$crouchingDistance += distance;
            double arc$rawCrouch = this.arc$crouchingDistance / ARC_MOVE_DIST_SCALE * 100;
            this.arc$creditedCrouchCm = arc$advanceCredited(arc$rawCrouch, this.arc$rawCrouchCm,
                    this.arc$creditedCrouchCm, arc$creditFactor);
            this.arc$rawCrouchCm = arc$rawCrouch;
            MovementEvents.onCrouch(this, (int) this.arc$creditedCrouchCm);
        } else {
            if (this.arc$isCrouching) {
                this.arc$isCrouching = false;
                MovementEvents.onStopCrouching(this);
            } else if (this.isCrouching()) {
                this.arc$isCrouching = true;
                MovementEvents.onStartCrouching(this);
            }
        }

        if (this.arc$isElytraFlying && this.isFallFlying()) {
            this.arc$creditedElytraCm = arc$advanceCredited(this.arc$elytraFlyingDistance,
                    this.arc$rawElytraCm, this.arc$creditedElytraCm, arc$creditFactor);
            this.arc$rawElytraCm = this.arc$elytraFlyingDistance;
            MovementEvents.onElytraFly(this, (int) this.arc$creditedElytraCm);
        } else {
            if (this.arc$isElytraFlying) {
                this.arc$isElytraFlying = false;
                MovementEvents.onStopElytraFlying(this);
            } else {
                if (this.isFallFlying()) {
                    this.arc$isElytraFlying = true;
                    MovementEvents.onStartElytraFlying(this);
                }
            }
        }

        if (arc$getServerPlayer().containerMenu instanceof GrindstoneMenu) {
            if (arc$isGrinding) {
                boolean firstSlot = false;
                boolean secondSlot = false;
                for (Slot slot : containerMenu.slots) {
                    if (!(slot.getItem().getItem() instanceof AirItem || slot.container instanceof Inventory)) {

                        if (slot.getContainerSlot() == 0) {
                            firstSlot = true;
                        }
                        if (slot.getContainerSlot() == 1) {
                            secondSlot = true;
                        }
                    }
                }
                if (!firstSlot && !secondSlot) {
                    PlayerEvents.onGrindItem(this);
                }
            }
            boolean firstSlot = false;
            boolean secondSlot = false;
            for (Slot slot : containerMenu.slots) {
                if (!(slot.getItem().getItem() instanceof AirItem || slot.container instanceof Inventory)) {

                    if (slot.getContainerSlot() == 0) {
                        firstSlot = true;
                    }
                    if (slot.getContainerSlot() == 1) {
                        secondSlot = true;
                    }
                }
            }
            arc$isGrinding = firstSlot && secondSlot;
        }
    }

    @Inject(at = @At("TAIL"), method = "awardStat(Lnet/minecraft/stats/Stat;I)V")
    public void awardStat(Stat<?> stat, int amount, CallbackInfo ci) {
        StatEvents.onAwardStat(this, stat, amount);
    }

    @Inject(at = @At("TAIL"), method = "onEffectAdded(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)V")
    public void onEffectAdded(MobEffectInstance effect, @Nullable Entity entity, CallbackInfo ci) {
    }

    @Inject(at = @At("TAIL"), method = "onEnchantmentPerformed(Lnet/minecraft/world/item/ItemStack;I)V")
    public void onEnchantmentPerformed(ItemStack itemStack, int level, CallbackInfo ci) {
        PlayerEvents.onEnchantItem(this, itemStack, level);
    }

    @Inject(at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
            shift = At.Shift.BEFORE),
            method = "hurtServer",
            cancellable = true)
    public void hurt(ServerLevel serverLevel, DamageSource damageSource, float f, CallbackInfoReturnable<Boolean> cir) {
        Entity entity = this.arc$getPlayer();
        if (entity instanceof ArcServerPlayer arcServerPlayer) {
            ActionResult actionResult = new ActionDataBuilder(arcServerPlayer, ActionType.GET_HURT)
                    .withData(ActionDataType.DAMAGE_SOURCE, damageSource)
                    .withData(ActionDataType.DAMAGE_AMOUNT, f)
                    .build()
                    .sendToAction();

            if (actionResult.shouldCancelAction()) {
                f = 0F;
            }
            if (actionResult.getDamageModifier() != 1F) {
                f = f * actionResult.getDamageModifier();
            }
        }

        if (damageSource.getEntity() instanceof ArcServerPlayer arcServerPlayer) {
            ActionResult actionResult = new ActionDataBuilder(arcServerPlayer, ActionType.HURT_PLAYER)
                    .withData(ActionDataType.ENTITY, entity)
                    .withData(ActionDataType.DAMAGE_AMOUNT, f)
                    .build()
                    .sendToAction();

            if (actionResult.shouldCancelAction()) {
                f = 0F;
            }
            if (actionResult.getDamageModifier() != 1F) {
                f = f * actionResult.getDamageModifier();
            }
        }
        cir.setReturnValue(super.hurtServer(serverLevel, damageSource, f));
    }

    @Inject(at = @At("TAIL"), method = "restoreFrom(Lnet/minecraft/server/level/ServerPlayer;Z)V")
    public void restoreFrom(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
        if (oldPlayer instanceof ArcServerPlayer arcServerPlayer) {
            this.arc$actionHolders = arcServerPlayer.arc$getActionHoldersMap();
            this.arc$actionCache.invalidate();
            this.arc$lastDistanceInCm = arcServerPlayer.arc$getLastDistancesInCm();
            this.arc$lastRemainderInCm = arcServerPlayer.arc$getLastRemaindersInCm();
            this.arc$swimmingDistanceInCm = arcServerPlayer.arc$getSwimmingDistanceInCm();
            this.arc$sprintingDistance = arcServerPlayer.arc$getSprintingDistance();
            this.arc$crouchingDistance = arcServerPlayer.arc$getCrouchingDistance();
            this.arc$elytraFlyingDistance = arcServerPlayer.arc$getElytraFlyingDistance();
            this.arc$blockPosCache = arcServerPlayer.arc$getBlockPosCache();

            // A respawned player has a new movement counter. Keeping the old baseline makes
            // walking stop until the new counter catches up and can add negative sprint distance.
            this.arc$isSwimming = false;
            this.arc$isWalking = false;
            this.arc$walkingDistance = this.moveDist;
            this.arc$isSprinting = false;
            this.arc$isCrouching = false;
            this.arc$isElytraFlying = false;
            this.arc$isGrinding = false;
            this.arc$isHorseRiding = false;
        }
    }

    @Inject(at = @At("TAIL"), method = "addAdditionalSaveData")
    private void arc$saveBlockPosCache(ValueOutput valueOutput, CallbackInfo ci) {
        List<CachedBlockPos> positions = this.arc$blockPosCache.getPositions();
        if (!positions.isEmpty()) {
            valueOutput.store(arc$BLOCK_POS_CACHE_TAG, arc$BLOCK_POS_CACHE_CODEC, positions);
        }
    }

    @Inject(at = @At("TAIL"), method = "readAdditionalSaveData")
    private void arc$loadBlockPosCache(ValueInput valueInput, CallbackInfo ci) {
        Optional<List<CachedBlockPos>> positions = valueInput.read(arc$BLOCK_POS_CACHE_TAG,
                arc$BLOCK_POS_CACHE_CODEC);
        if (positions.isPresent()) {
            this.arc$blockPosCache.restore(positions.get());
            return;
        }

        List<Long> legacyPositions = valueInput.read(arc$LEGACY_BLOCK_POS_CACHE_TAG,
                arc$LEGACY_BLOCK_POS_CACHE_CODEC).orElse(List.of());
        List<CachedBlockPos> migratedPositions = legacyPositions.stream()
                .map(BlockPos::of)
                .map(pos -> CachedBlockPos.of(this.level(), pos))
                .toList();
        this.arc$blockPosCache.restore(migratedPositions);
    }

    @Inject(at = @At("TAIL"), method = "<init>")
    public void readAdditionalSaveData(MinecraftServer minecraftServer, ServerLevel serverLevel, GameProfile gameProfile, ClientInformation clientInformation, CallbackInfo ci) {
        if (((ServerPlayer) (Object) this) instanceof ArcPlayer arcPlayer) {
            arcPlayer.arc$addActionHolders(PlayerActionHolderManager.getInstance().getPlayerActionHoldersList());
        }
    }
}
