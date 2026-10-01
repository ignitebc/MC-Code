package fuzs.illagerinvasion.common.handler;

import fuzs.illagerinvasion.common.IllagerInvasion;
import fuzs.illagerinvasion.common.init.ModStructures;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.jspecify.annotations.Nullable;

import java.util.List;

public final class MonsterAwarenessHandler {
    private static final Identifier DOUBLE_FOLLOW_RANGE_MODIFIER_ID = Identifier.fromNamespaceAndPath(
            IllagerInvasion.MOD_ID,
            "double_follow_range");
    private static final double DOUBLE_FOLLOW_RANGE_AMOUNT = 1.0;
    private static final List<ResourceKey<Structure>> ILLAGER_INVASION_STRUCTURES = List.of(
            ModStructures.FIRECALLER_HUT,
            ModStructures.ILLAGER_FORT,
            ModStructures.ILLUSIONER_TOWER,
            ModStructures.LABYRINTH,
            ModStructures.SORCERER_HUT);

    private MonsterAwarenessHandler() {
    }

    public static void onEntityLoad(Entity entity, ServerLevel serverLevel, boolean isLoadedFromDisk,
                                    @Nullable EntitySpawnReason entitySpawnReason) {
        if (!(entity instanceof Mob mob) || !isHostile(mob)) {
            return;
        }
        if (mob instanceof Zombie || mob instanceof AbstractSkeleton) {
            return;
        }
        if (!isIllagerInvasionMob(mob) && !isInsideIllagerInvasionStructure(mob, serverLevel)) {
            return;
        }
        doubleFollowRange(mob);
    }

    private static void doubleFollowRange(Mob mob) {
        AttributeInstance followRange = mob.getAttribute(Attributes.FOLLOW_RANGE);
        if (followRange == null || followRange.hasModifier(DOUBLE_FOLLOW_RANGE_MODIFIER_ID)) {
            return;
        }
        followRange.addPermanentModifier(new AttributeModifier(
                DOUBLE_FOLLOW_RANGE_MODIFIER_ID,
                DOUBLE_FOLLOW_RANGE_AMOUNT,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    }

    private static boolean isHostile(Mob mob) {
        return mob instanceof Enemy || mob.getType().getCategory() == MobCategory.MONSTER;
    }

    private static boolean isIllagerInvasionMob(Mob mob) {
        Identifier entityTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType());
        return IllagerInvasion.MOD_ID.equals(entityTypeId.getNamespace());
    }

    private static boolean isInsideIllagerInvasionStructure(Mob mob, ServerLevel serverLevel) {
        if (!serverLevel.structureManager().hasAnyStructureAt(mob.blockPosition())) {
            return false;
        }
        Registry<Structure> structureRegistry = serverLevel.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        for (ResourceKey<Structure> structureKey : ILLAGER_INVASION_STRUCTURES) {
            Structure structure = structureRegistry.getValue(structureKey);
            if (structure == null) {
                continue;
            }
            if (serverLevel.structureManager().getStructureAt(mob.blockPosition(), structure).isValid()) {
                return true;
            }
        }
        return false;
    }
}
