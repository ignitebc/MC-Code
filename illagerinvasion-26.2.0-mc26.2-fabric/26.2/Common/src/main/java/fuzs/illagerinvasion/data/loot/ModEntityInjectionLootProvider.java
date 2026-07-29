package fuzs.illagerinvasion.data.loot;

import fuzs.illagerinvasion.init.ModLootTables;
import fuzs.puzzleslib.api.data.v2.AbstractLootProvider;
import fuzs.puzzleslib.api.data.v2.core.DataProviderContext;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public class ModEntityInjectionLootProvider extends AbstractLootProvider.Simple {

    public ModEntityInjectionLootProvider(DataProviderContext context) {
        super(LootContextParamSets.ENTITY, context);
        // 중첩 테이블은 main 리소스에 있어 데이터 생성기의 임시 전리품 레지스트리에는 포함되지 않는다.
        this.skipValidation(ModLootTables.ILLUSIONER_INJECTION);
        this.skipValidation(ModLootTables.PILLAGER_INJECTION);
        this.skipValidation(ModLootTables.RAVAGER_INJECTION);
    }

    @Override
    public void addLootTables() {
        this.add(ModLootTables.ILLUSIONER_INJECTION,
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .add(LootItem.lootTableItem(Items.EMERALD)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F)))
                                        .apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.registries(),
                                                UniformGenerator.between(0.0F, 1.0F)))
                                        .when(LootItemKilledByPlayerCondition.killedByPlayer())))
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .add(LootItem.lootTableItem(Items.ARROW)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 2.0F)))
                                        .apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.registries(),
                                                UniformGenerator.between(0.0F, 1.0F)))))
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .add(ExternalLootItems.item(ExternalLootItems.ENHANCEMENT_SHARD))
                                .when(LootItemRandomChanceWithEnchantedBonusCondition.randomChanceAndLootingBoost(
                                        this.registries(),
                                        0.4F,
                                        0.2F))
                                .when(LootItemKilledByPlayerCondition.killedByPlayer())));
        this.add(ModLootTables.PILLAGER_INJECTION,
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .add(LootItem.lootTableItem(Items.EMERALD)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F)))
                                        .apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.registries(),
                                                UniformGenerator.between(0.0F, 1.0F)))
                                        .when(LootItemKilledByPlayerCondition.killedByPlayer())))
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .add(ExternalLootItems.item(ExternalLootItems.ENHANCEMENT_SHARD)
                                        .when(LootItemRandomChanceWithEnchantedBonusCondition.randomChanceAndLootingBoost(
                                                this.registries(),
                                                0.5F,
                                                0.0625F))
                                        .when(LootItemKilledByPlayerCondition.killedByPlayer()))));
        this.add(ModLootTables.RAVAGER_INJECTION,
                LootTable.lootTable()
                        .withPool(LootPool.lootPool()
                                .setRolls(ConstantValue.exactly(1.0F))
                                .add(ExternalLootItems.item(ExternalLootItems.ENHANCEMENT_SHARD)
                                        .when(LootItemRandomChanceWithEnchantedBonusCondition.randomChanceAndLootingBoost(
                                                this.registries(),
                                                0.5F,
                                                0.0625F))
                                        .when(LootItemKilledByPlayerCondition.killedByPlayer()))));
    }
}
