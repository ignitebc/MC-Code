package com.daqem.arc.api.action.type;

import com.daqem.arc.Arc;
import com.daqem.arc.api.action.IAction;
import com.daqem.arc.api.action.serializer.IActionSerializer;
import com.daqem.arc.data.action.advancement.AdvancementAction;
import com.daqem.arc.data.action.block.*;
import com.daqem.arc.data.action.entity.*;
import com.daqem.arc.data.action.item.*;
import com.daqem.arc.data.action.movement.*;
import com.daqem.arc.data.action.player.*;
import com.daqem.arc.event.events.RegistryEvent;
import com.daqem.arc.registry.ArcRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public interface ActionType<T extends IAction> extends IActionType<T> {

    ActionType<SwimAction> SWIM = register(Arc.getId("on_swim"), new SwimAction.Serializer());
    ActionType<SwimStartAction> SWIM_START = register(Arc.getId("on_swim_start"), new SwimStartAction.Serializer());
    ActionType<SwimStopAction> SWIM_STOP = register(Arc.getId("on_swim_stop"), new SwimStopAction.Serializer());
    ActionType<WalkAction> WALK = register(Arc.getId("on_walk"), new WalkAction.Serializer());
    ActionType<WalkStartAction> WALK_START = register(Arc.getId("on_walk_start"), new WalkStartAction.Serializer());
    ActionType<WalkStopAction> WALK_STOP = register(Arc.getId("on_walk_stop"), new WalkStopAction.Serializer());
    ActionType<SprintAction> SPRINT = register(Arc.getId("on_sprint"), new SprintAction.Serializer());
    ActionType<SprintStartAction> SPRINT_START = register(Arc.getId("on_sprint_start"), new SprintStartAction.Serializer());
    ActionType<SprintStopAction> SPRINT_STOP = register(Arc.getId("on_sprint_stop"), new SprintStopAction.Serializer());
    ActionType<CrouchAction> CROUCH = register(Arc.getId("on_crouch"), new CrouchAction.Serializer());
    ActionType<CrouchStartAction> CROUCH_START = register(Arc.getId("on_crouch_start"), new CrouchStartAction.Serializer());
    ActionType<CrouchStopAction> CROUCH_STOP = register(Arc.getId("on_crouch_stop"), new CrouchStopAction.Serializer());
    ActionType<ElytraFlyAction> ELYTRA_FLY = register(Arc.getId("on_elytra_fly"), new ElytraFlyAction.Serializer());
    ActionType<ElytraFlyStartAction> ELYTRA_FLY_START = register(Arc.getId("on_elytra_fly_start"), new ElytraFlyStartAction.Serializer());
    ActionType<ElytraFlyStopAction> ELYTRA_FLY_STOP = register(Arc.getId("on_elytra_fly_stop"), new ElytraFlyStopAction.Serializer());
    ActionType<HorseRideAction> HORSE_RIDE = register(Arc.getId("on_horse_ride"), new HorseRideAction.Serializer());
    ActionType<HorseRideStartAction> HORSE_RIDE_START = register(Arc.getId("on_horse_ride_start"), new HorseRideStartAction.Serializer());
    ActionType<HorseRideStopAction> HORSE_RIDE_STOP = register(Arc.getId("on_horse_ride_stop"), new HorseRideStopAction.Serializer());

    ActionType<PlaceBlockAction> PLACE_BLOCK = register(Arc.getId("on_place_block"), new PlaceBlockAction.Serializer());
    ActionType<BreakBlockAction> BREAK_BLOCK = register(Arc.getId("on_break_block"), new BreakBlockAction.Serializer());
    ActionType<InteractBlockAction> INTERACT_BLOCK = register(Arc.getId("on_interact_block"), new InteractBlockAction.Serializer());
    ActionType<DeathAction> DEATH = register(Arc.getId("on_death"), new DeathAction.Serializer());
    ActionType<GetHurtAction> GET_HURT = register(Arc.getId("on_get_hurt"), new GetHurtAction.Serializer());
    ActionType<KillEntityAction> KILL_ENTITY = register(Arc.getId("on_kill_entity"), new KillEntityAction.Serializer());
    ActionType<HurtEntityAction> HURT_ENTITY = register(Arc.getId("on_hurt_entity"), new HurtEntityAction.Serializer());
    ActionType<HurtPlayerAction> HURT_PLAYER = register(Arc.getId("on_hurt_player"), new HurtPlayerAction.Serializer());
    ActionType<CraftItemAction> CRAFT_ITEM = register(Arc.getId("on_craft_item"), new CraftItemAction.Serializer());
    ActionType<DropItemAction> DROP_ITEM = register(Arc.getId("on_drop_item"), new DropItemAction.Serializer());
    ActionType<UseItemAction> USE_ITEM = register(Arc.getId("on_use_item"), new UseItemAction.Serializer());
    ActionType<AdvancementAction> ADVANCEMENT = register(Arc.getId("on_advancement"), new AdvancementAction.Serializer());
    ActionType<EatAction> EAT = register(Arc.getId("on_eat"), new EatAction.Serializer());
    ActionType<DrinkAction> DRINK = register(Arc.getId("on_drink"), new DrinkAction.Serializer());
    ActionType<ThrowItemAction> THROW_ITEM = register(Arc.getId("on_throw_item"), new ThrowItemAction.Serializer());
    ActionType<ShootProjectileAction> SHOOT_PROJECTILE = register(Arc.getId("on_shoot_projectile"), new ShootProjectileAction.Serializer());
    ActionType<BrewPotionAction> BREW_POTION = register(Arc.getId("on_brew_potion"), new BrewPotionAction.Serializer());
    ActionType<EffectAddedAction> EFFECT_ADDED = register(Arc.getId("on_effect_added"), new EffectAddedAction.Serializer());
    ActionType<SmeltItemAction> SMELT_ITEM = register(Arc.getId("on_smelt_item"), new SmeltItemAction.Serializer());
    ActionType<EnchantItemAction> ENCHANT_ITEM = register(Arc.getId("on_enchant_item"), new EnchantItemAction.Serializer());
    ActionType<PlantCropAction> PLANT_CROP = register(Arc.getId("on_plant_crop"), new PlantCropAction.Serializer());
    ActionType<HarvestCropAction> HARVEST_CROP = register(Arc.getId("on_harvest_crop"), new HarvestCropAction.Serializer());
    ActionType<TameAnimalAction> TAME_ANIMAL = register(Arc.getId("on_tame_animal"), new TameAnimalAction.Serializer());
    ActionType<InteractEntityAction> INTERACT_ENTITY = register(Arc.getId("on_interact_entity"), new InteractEntityAction.Serializer());
    ActionType<BreedAnimalAction> BREED_ANIMAL = register(Arc.getId("on_breed_animal"), new BreedAnimalAction.Serializer());
    ActionType<FishedUpItemAction> FISHED_UP_ITEM = register(Arc.getId("on_fished_up_item"), new FishedUpItemAction.Serializer());
    ActionType<StripLogAction> STRIP_LOG = register(Arc.getId("on_strip_log"), new StripLogAction.Serializer());
    ActionType<GrindItemAction> GRIND_ITEM = register(Arc.getId("on_grind_item"), new GrindItemAction.Serializer());
    ActionType<UseAnvilAction> USE_ANVIL = register(Arc.getId("on_use_anvil"), new UseAnvilAction.Serializer());
    ActionType<HurtItemAction> HURT_ITEM = register(Arc.getId("on_hurt_item"), new HurtItemAction.Serializer());
    ActionType<GetDestroySpeedAction> GET_DESTROY_SPEED = register(Arc.getId("on_get_destroy_speed"), new GetDestroySpeedAction.Serializer());
    ActionType<GetAttackSpeedAction> GET_ATTACK_SPEED = register(Arc.getId("on_get_attack_speed"), new GetAttackSpeedAction.Serializer());
    ActionType<RodReelInAction> ROD_REEL_IN = register(Arc.getId("on_rod_reel_in"), new RodReelInAction.Serializer());

    static <T extends IAction> ActionType<T> register(final ResourceLocation location, final IActionSerializer<T> serializer) {
        return Registry.register(ArcRegistry.ACTION, location, new ActionType<T>(){

            @Override
            public ResourceLocation getLocation() {
                return location;
            }

            @Override
            public IActionSerializer<T> getSerializer() {
                return serializer;
            }

            @Override
            public String toString() {
                return location.toString();
            }
        });
    }

    static void init() {
        RegistryEvent.REGISTER_ACTION_TYPE.invoker().registerActionType();
    }
}
