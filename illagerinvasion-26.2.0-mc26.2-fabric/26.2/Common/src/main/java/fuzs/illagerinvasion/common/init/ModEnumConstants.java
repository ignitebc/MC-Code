package fuzs.illagerinvasion.common.init;

import fuzs.illagerinvasion.common.IllagerInvasion;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.raid.Raid;

import java.util.Locale;
import java.util.function.Function;

public class ModEnumConstants {
    public static final Raid.RaiderType BASHER_RAIDER_TYPE = getRaiderType(ModEntityTypes.BASHER_ENTITY_TYPE);
    public static final Raid.RaiderType PROVOKER_RAIDER_TYPE = getRaiderType(ModEntityTypes.PROVOKER_ENTITY_TYPE);
    public static final Raid.RaiderType NECROMANCER_RAIDER_TYPE = getRaiderType(ModEntityTypes.NECROMANCER_ENTITY_TYPE);
    public static final Raid.RaiderType SORCERER_RAIDER_TYPE = getRaiderType(ModEntityTypes.SORCERER_ENTITY_TYPE);
    public static final Raid.RaiderType ILLUSIONER_RAIDER_TYPE = getRaiderType(EntityTypes.ILLUSIONER.builtInRegistryHolder());
    public static final Raid.RaiderType ARCHIVIST_RAIDER_TYPE = getRaiderType(ModEntityTypes.ARCHIVIST_ENTITY_TYPE);
    public static final Raid.RaiderType MARAUDER_RAIDER_TYPE = getRaiderType(ModEntityTypes.MARAUDER_ENTITY_TYPE);
    public static final Raid.RaiderType INQUISITOR_RAIDER_TYPE = getRaiderType(ModEntityTypes.INQUISITOR_ENTITY_TYPE);
    public static final Raid.RaiderType ALCHEMIST_RAIDER_TYPE = getRaiderType(ModEntityTypes.ALCHEMIST_ENTITY_TYPE);
    public static final Raid.RaiderType FIRECALLER_RAIDER_TYPE = getRaiderType(ModEntityTypes.FIRECALLER_ENTITY_TYPE);
    public static final Raid.RaiderType INVOKER_RAIDER_TYPE = getRaiderType(ModEntityTypes.INVOKER_ENTITY_TYPE);

    public static void bootstrap() {
        // NO-OP
    }

    static Raid.RaiderType getRaiderType(Holder<?> holder) {
        Identifier resourceLocation = IllagerInvasion.id(holder.unwrapKey()
                .map(ResourceKey::identifier)
                .map(Identifier::getPath)
                .orElseThrow());
        return getEnumConstant(resourceLocation, Raid.RaiderType::valueOf);
    }

    static <E extends Enum<E>> E getEnumConstant(Identifier resourceLocation,
            Function<String, E> valueOfInvoker) {
        return valueOfInvoker.apply(resourceLocation.toDebugFileName().toUpperCase(Locale.ROOT));
    }
}
