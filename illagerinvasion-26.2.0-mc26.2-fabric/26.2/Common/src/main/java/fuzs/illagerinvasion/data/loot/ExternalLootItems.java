package fuzs.illagerinvasion.data.loot;

import fuzs.illagerinvasion.IllagerInvasion;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.entries.NestedLootTable;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

/**
 * 컴파일 의존성이 없는 외부 모드 아이템을 전리품 Provider에서 참조한다.
 * 실제 아이템 ID는 main 리소스의 작은 중첩 전리품 테이블에만 기록하므로 데이터 생성 시 사라지지 않는다.
 */
public final class ExternalLootItems {

    public static final Identifier ENHANCEMENT_SHARD = advancedNetheriteId("enhancement_shard");
    public static final Identifier ENHANCEMENT_GEM = advancedNetheriteId("enhancement_gem");
    public static final Identifier REWARD_KEY_I = advancedNetheriteId("reward_key_i");
    public static final Identifier REWARD_KEY_II = advancedNetheriteId("reward_key_ii");
    public static final Identifier REWARD_KEY_III = advancedNetheriteId("reward_key_iii");
    public static final Identifier REWARD_KEY_IV = advancedNetheriteId("reward_key_iv");

    private ExternalLootItems() {
    }

    public static LootPool.Builder randomPool(Identifier itemId, float chance) {
        return randomCountPool(itemId, ConstantValue.exactly(1.0F), chance);
    }

    public static LootPool.Builder randomCountPool(Identifier itemId, NumberProvider count, float chance) {
        return LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1.0F))
                .add(item(itemId)
                        .apply(SetItemCountFunction.setCount(count))
                        .when(LootItemRandomChanceCondition.randomChance(chance)));
    }

    public static LootPoolSingletonContainer.Builder<?> item(Identifier itemId) {
        String tablePath = "external_items/" + itemId.getNamespace() + "/" + itemId.getPath();
        Identifier tableId = IllagerInvasion.id(tablePath);
        ResourceKey<LootTable> tableKey = ResourceKey.create(Registries.LOOT_TABLE, tableId);
        return NestedLootTable.lootTableReference(tableKey);
    }

    private static Identifier advancedNetheriteId(String path) {
        return Identifier.fromNamespaceAndPath("advancednetherite", path);
    }
}
