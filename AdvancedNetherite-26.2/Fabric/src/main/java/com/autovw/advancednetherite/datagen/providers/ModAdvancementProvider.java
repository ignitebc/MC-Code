package com.autovw.advancednetherite.datagen.providers;

import com.autovw.advancednetherite.AdvancedNetherite;
import com.autovw.advancednetherite.core.ModItems;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricAdvancementProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * @author Autovw
 */
public class ModAdvancementProvider extends FabricAdvancementProvider
{
    public ModAdvancementProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookup)
    {
        super(output, registryLookup);
    }

    @Override
    public void generateAdvancement(HolderLookup.Provider registries, Consumer<AdvancementHolder> consumer)
    {
        Advancement.Builder.advancement()
                .parent(Identifier.withDefaultNamespace("husbandry/obtain_netherite_hoe"))
                .display(ModItems.FROST_HOE, Component.translatable("advancements.advancednetherite.husbandry.frost_hoe.title"), Component.translatable("advancements.advancednetherite.husbandry.frost_hoe.description"), null, AdvancementType.CHALLENGE, true, true, false)
                .rewards(AdvancementRewards.Builder.experience(200))
                .addCriterion("frost_hoe", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.FROST_HOE))
                .save(consumer, Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "husbandry/obtain_frost_hoe").toString());

        AdvancementHolder ashArmor = Advancement.Builder.advancement()
                .parent(Identifier.withDefaultNamespace("nether/netherite_armor"))
                .display(ModItems.ASH_CHESTPLATE, Component.translatable("advancements.advancednetherite.nether.ash_armor.title"), Component.translatable("advancements.advancednetherite.nether.ash_armor.description"), null, AdvancementType.CHALLENGE, true, true, false)
                .rewards(AdvancementRewards.Builder.experience(100))
                .addCriterion("ash_armor", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.ASH_HELMET, ModItems.ASH_CHESTPLATE, ModItems.ASH_LEGGINGS, ModItems.ASH_BOOTS))
                .save(consumer, Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "nether/ash_armor").toString());

        AdvancementHolder sunlightArmor = Advancement.Builder.advancement()
                .parent(ashArmor)
                .display(ModItems.SUNLIGHT_CHESTPLATE, Component.translatable("advancements.advancednetherite.nether.sunlight_armor.title"), Component.translatable("advancements.advancednetherite.nether.sunlight_armor.description"), null, AdvancementType.CHALLENGE, true, true, false)
                .rewards(AdvancementRewards.Builder.experience(100))
                .addCriterion("sunlight_armor", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.SUNLIGHT_HELMET, ModItems.SUNLIGHT_CHESTPLATE, ModItems.SUNLIGHT_LEGGINGS, ModItems.SUNLIGHT_BOOTS))
                .save(consumer, Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "nether/sunlight_armor").toString());

        AdvancementHolder soulArmor = Advancement.Builder.advancement()
                .parent(sunlightArmor)
                .display(ModItems.SOUL_CHESTPLATE, Component.translatable("advancements.advancednetherite.nether.soul_armor.title"), Component.translatable("advancements.advancednetherite.nether.soul_armor.description"), null, AdvancementType.CHALLENGE, true, true, false)
                .rewards(AdvancementRewards.Builder.experience(100))
                .addCriterion("soul_armor", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.SOUL_HELMET, ModItems.SOUL_CHESTPLATE, ModItems.SOUL_LEGGINGS, ModItems.SOUL_BOOTS))
                .save(consumer, Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "nether/soul_armor").toString());

        Advancement.Builder.advancement()
                .parent(soulArmor)
                .display(ModItems.FROST_CHESTPLATE, Component.translatable("advancements.advancednetherite.nether.frost_armor.title"), Component.translatable("advancements.advancednetherite.nether.frost_armor.description"), null, AdvancementType.CHALLENGE, true, true, false)
                .rewards(AdvancementRewards.Builder.experience(200))
                .addCriterion("frost_armor", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.FROST_HELMET, ModItems.FROST_CHESTPLATE, ModItems.FROST_LEGGINGS, ModItems.FROST_BOOTS))
                .save(consumer, Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "nether/frost_armor").toString());

        AdvancementHolder ashIngot = Advancement.Builder.advancement()
                .parent(Identifier.withDefaultNamespace("nether/obtain_ancient_debris"))
                .display(ModItems.ASH_INGOT, Component.translatable("advancements.advancednetherite.nether.obtain_ash_ingot.title"), Component.translatable("advancements.advancednetherite.nether.obtain_ash_ingot.description"), null, AdvancementType.TASK, true, true, false)
                .rewards(AdvancementRewards.Builder.experience(20))
                .addCriterion("ash_ingot", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.ASH_INGOT))
                .save(consumer, Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "nether/obtain_ash_ingot").toString());

        Advancement.Builder.advancement()
                .parent(ashIngot)
                .display(ModItems.FROST_BLOCK, Component.translatable("advancements.advancednetherite.nether.obtain_frost_block.title"), Component.translatable("advancements.advancednetherite.nether.obtain_frost_block.description"), null, AdvancementType.TASK, true, true, false)
                .rewards(AdvancementRewards.Builder.experience(20))
                .addCriterion("frost_block", InventoryChangeTrigger.TriggerInstance.hasItems(ModItems.FROST_BLOCK))
                .save(consumer, Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, "nether/obtain_frost_block").toString());
    }
}
