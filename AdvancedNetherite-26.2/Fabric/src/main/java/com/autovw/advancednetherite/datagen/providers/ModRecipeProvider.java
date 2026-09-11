package com.autovw.advancednetherite.datagen.providers;

import com.autovw.advancednetherite.AdvancedNetherite;
import com.autovw.advancednetherite.core.ModBackpackItems;
import com.autovw.advancednetherite.core.ModBlocks;
import com.autovw.advancednetherite.core.ModItems;
import com.autovw.advancednetherite.core.util.ModTags;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.util.concurrent.CompletableFuture;

/**
 * @author Autovw
 */
public class ModRecipeProvider extends FabricRecipeProvider
{
    public ModRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture)
    {
        super(output, registriesFuture);
    }

    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider provider, RecipeOutput recipeOutput)
    {
        return new RecipeProvider(provider, recipeOutput)
        {
            @Override
            public void buildRecipes()
            {
                HolderLookup.RegistryLookup<Item> registryLookup = provider.lookupOrThrow(Registries.ITEM);
                /* Blocks */
                baseBlockRecipe(registryLookup, output, ModItems.ASH_INGOT, ModBlocks.ASH_BLOCK);
                baseBlockRecipe(registryLookup, output, ModItems.SUNLIGHT_INGOT, ModBlocks.SUNLIGHT_BLOCK);
                baseBlockRecipe(registryLookup, output, ModItems.SOUL_INGOT, ModBlocks.SOUL_BLOCK);
                baseBlockRecipe(registryLookup, output, ModItems.FROST_INGOT, ModBlocks.FROST_BLOCK);

                /* Ingots */
                baseIngotRecipe(registryLookup, output, ModTags.INGOTS_UPGRADE_TO_ASH, Items.IRON_INGOT, ModItems.ASH_INGOT);
                baseIngotRecipe(registryLookup, output, ModTags.INGOTS_UPGRADE_TO_SUNLIGHT, Items.GOLD_INGOT, ModItems.SUNLIGHT_INGOT);
                baseIngotRecipe(registryLookup, output, ModTags.INGOTS_UPGRADE_TO_SOUL, Items.EMERALD, ModItems.SOUL_INGOT);
                baseIngotRecipe(registryLookup, output, ModTags.INGOTS_UPGRADE_TO_FROST, Items.DIAMOND, ModItems.FROST_INGOT);

                /* Axes */
                baseSmithingRecipe(registryLookup, output, ModTags.AXE_UPGRADE_TO_ASH, ModTags.UPGRADE_TO_ASH, ModItems.ASH_AXE);
                baseSmithingRecipe(registryLookup, output, ModTags.AXE_UPGRADE_TO_SUNLIGHT, ModTags.UPGRADE_TO_SUNLIGHT, ModItems.SUNLIGHT_AXE);
                baseSmithingRecipe(registryLookup, output, ModTags.AXE_UPGRADE_TO_SOUL, ModTags.UPGRADE_TO_SOUL, ModItems.SOUL_AXE);
                baseSmithingRecipe(registryLookup, output, ModTags.AXE_UPGRADE_TO_FROST, ModTags.UPGRADE_TO_FROST, ModItems.FROST_AXE);

                /* Boots */
                baseSmithingRecipe(registryLookup, output, ModTags.BOOTS_UPGRADE_TO_ASH, ModTags.UPGRADE_TO_ASH, ModItems.ASH_BOOTS);
                baseSmithingRecipe(registryLookup, output, ModTags.BOOTS_UPGRADE_TO_SUNLIGHT, ModTags.UPGRADE_TO_SUNLIGHT, ModItems.SUNLIGHT_BOOTS);
                baseSmithingRecipe(registryLookup, output, ModTags.BOOTS_UPGRADE_TO_SOUL, ModTags.UPGRADE_TO_SOUL, ModItems.SOUL_BOOTS);
                baseSmithingRecipe(registryLookup, output, ModTags.BOOTS_UPGRADE_TO_FROST, ModTags.UPGRADE_TO_FROST, ModItems.FROST_BOOTS);

                /* Chestplates */
                baseSmithingRecipe(registryLookup, output, ModTags.CHESTPLATE_UPGRADE_TO_ASH, ModTags.UPGRADE_TO_ASH, ModItems.ASH_CHESTPLATE);
                baseSmithingRecipe(registryLookup, output, ModTags.CHESTPLATE_UPGRADE_TO_SUNLIGHT, ModTags.UPGRADE_TO_SUNLIGHT, ModItems.SUNLIGHT_CHESTPLATE);
                baseSmithingRecipe(registryLookup, output, ModTags.CHESTPLATE_UPGRADE_TO_SOUL, ModTags.UPGRADE_TO_SOUL, ModItems.SOUL_CHESTPLATE);
                baseSmithingRecipe(registryLookup, output, ModTags.CHESTPLATE_UPGRADE_TO_FROST, ModTags.UPGRADE_TO_FROST, ModItems.FROST_CHESTPLATE);

                /* Helmets */
                baseSmithingRecipe(registryLookup, output, ModTags.HELMET_UPGRADE_TO_ASH, ModTags.UPGRADE_TO_ASH, ModItems.ASH_HELMET);
                baseSmithingRecipe(registryLookup, output, ModTags.HELMET_UPGRADE_TO_SUNLIGHT, ModTags.UPGRADE_TO_SUNLIGHT, ModItems.SUNLIGHT_HELMET);
                baseSmithingRecipe(registryLookup, output, ModTags.HELMET_UPGRADE_TO_SOUL, ModTags.UPGRADE_TO_SOUL, ModItems.SOUL_HELMET);
                baseSmithingRecipe(registryLookup, output, ModTags.HELMET_UPGRADE_TO_FROST, ModTags.UPGRADE_TO_FROST, ModItems.FROST_HELMET);

                /* Hoes */
                baseSmithingRecipe(registryLookup, output, ModTags.HOE_UPGRADE_TO_ASH, ModTags.UPGRADE_TO_ASH, ModItems.ASH_HOE);
                baseSmithingRecipe(registryLookup, output, ModTags.HOE_UPGRADE_TO_SUNLIGHT, ModTags.UPGRADE_TO_SUNLIGHT, ModItems.SUNLIGHT_HOE);
                baseSmithingRecipe(registryLookup, output, ModTags.HOE_UPGRADE_TO_SOUL, ModTags.UPGRADE_TO_SOUL, ModItems.SOUL_HOE);
                baseSmithingRecipe(registryLookup, output, ModTags.HOE_UPGRADE_TO_FROST, ModTags.UPGRADE_TO_FROST, ModItems.FROST_HOE);

                /* Leggings */
                baseSmithingRecipe(registryLookup, output, ModTags.LEGGINGS_UPGRADE_TO_ASH, ModTags.UPGRADE_TO_ASH, ModItems.ASH_LEGGINGS);
                baseSmithingRecipe(registryLookup, output, ModTags.LEGGINGS_UPGRADE_TO_SUNLIGHT, ModTags.UPGRADE_TO_SUNLIGHT, ModItems.SUNLIGHT_LEGGINGS);
                baseSmithingRecipe(registryLookup, output, ModTags.LEGGINGS_UPGRADE_TO_SOUL, ModTags.UPGRADE_TO_SOUL, ModItems.SOUL_LEGGINGS);
                baseSmithingRecipe(registryLookup, output, ModTags.LEGGINGS_UPGRADE_TO_FROST, ModTags.UPGRADE_TO_FROST, ModItems.FROST_LEGGINGS);

                /* Pickaxes */
                baseSmithingRecipe(registryLookup, output, ModTags.PICKAXE_UPGRADE_TO_ASH, ModTags.UPGRADE_TO_ASH, ModItems.ASH_PICKAXE);
                baseSmithingRecipe(registryLookup, output, ModTags.PICKAXE_UPGRADE_TO_SUNLIGHT, ModTags.UPGRADE_TO_SUNLIGHT, ModItems.SUNLIGHT_PICKAXE);
                baseSmithingRecipe(registryLookup, output, ModTags.PICKAXE_UPGRADE_TO_SOUL, ModTags.UPGRADE_TO_SOUL, ModItems.SOUL_PICKAXE);
                baseSmithingRecipe(registryLookup, output, ModTags.PICKAXE_UPGRADE_TO_FROST, ModTags.UPGRADE_TO_FROST, ModItems.FROST_PICKAXE);

                /* Shovels */
                baseSmithingRecipe(registryLookup, output, ModTags.SHOVEL_UPGRADE_TO_ASH, ModTags.UPGRADE_TO_ASH, ModItems.ASH_SHOVEL);
                baseSmithingRecipe(registryLookup, output, ModTags.SHOVEL_UPGRADE_TO_SUNLIGHT, ModTags.UPGRADE_TO_SUNLIGHT, ModItems.SUNLIGHT_SHOVEL);
                baseSmithingRecipe(registryLookup, output, ModTags.SHOVEL_UPGRADE_TO_SOUL, ModTags.UPGRADE_TO_SOUL, ModItems.SOUL_SHOVEL);
                baseSmithingRecipe(registryLookup, output, ModTags.SHOVEL_UPGRADE_TO_FROST, ModTags.UPGRADE_TO_FROST, ModItems.FROST_SHOVEL);

                /* Swords */
                baseSmithingRecipe(registryLookup, output, ModTags.SWORD_UPGRADE_TO_ASH, ModTags.UPGRADE_TO_ASH, ModItems.ASH_SWORD);
                baseSmithingRecipe(registryLookup, output, ModTags.SWORD_UPGRADE_TO_SUNLIGHT, ModTags.UPGRADE_TO_SUNLIGHT, ModItems.SUNLIGHT_SWORD);
                baseSmithingRecipe(registryLookup, output, ModTags.SWORD_UPGRADE_TO_SOUL, ModTags.UPGRADE_TO_SOUL, ModItems.SOUL_SWORD);
                baseSmithingRecipe(registryLookup, output, ModTags.SWORD_UPGRADE_TO_FROST, ModTags.UPGRADE_TO_FROST, ModItems.FROST_SWORD);

                /* Spears */
                baseSmithingRecipe(registryLookup, output, ModTags.SPEAR_UPGRADE_TO_ASH, ModTags.UPGRADE_TO_ASH, ModItems.ASH_SPEAR);
                baseSmithingRecipe(registryLookup, output, ModTags.SPEAR_UPGRADE_TO_SUNLIGHT, ModTags.UPGRADE_TO_SUNLIGHT, ModItems.SUNLIGHT_SPEAR);
                baseSmithingRecipe(registryLookup, output, ModTags.SPEAR_UPGRADE_TO_SOUL, ModTags.UPGRADE_TO_SOUL, ModItems.SOUL_SPEAR);
                baseSmithingRecipe(registryLookup, output, ModTags.SPEAR_UPGRADE_TO_FROST, ModTags.UPGRADE_TO_FROST, ModItems.FROST_SPEAR);

                /* Misc */
                backpackRecipes(registryLookup, output);
                lodestoneRecipe(registryLookup, output);
            }

            public void baseSmithingRecipe(HolderLookup.RegistryLookup<Item> registryLookup, RecipeOutput output, TagKey<Item> ingredient, TagKey<Item> upgradeIngredient, Item result)
            {
                Identifier resultId = AdvancedNetherite.getRegistryHelper().getItemById(result);
                SmithingTransformRecipeBuilder.smithing(Ingredient.of(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE), tag(ingredient), tag(upgradeIngredient), RecipeCategory.MISC, result)
                        .unlocks("has_ingredients", has(upgradeIngredient))
                        .save(output, Identifier.fromNamespaceAndPath(resultId.getNamespace(), resultId.getPath() + "_smithing").toString());
            }

            public void baseIngotRecipe(HolderLookup.RegistryLookup<Item> registryLookup, RecipeOutput output, TagKey<Item> ingotIngredient, Item upgradeIngredient, Item result)
            {
                ShapelessRecipeBuilder.shapeless(registryLookup, RecipeCategory.MISC, result)
                        .requires(ingotIngredient)
                        .requires(upgradeIngredient).requires(upgradeIngredient).requires(upgradeIngredient).requires(upgradeIngredient)
                        .unlockedBy("has_" + upgradeIngredient.toString(), has(upgradeIngredient))
                        .save(output);
            }

            public void baseBlockRecipe(HolderLookup.RegistryLookup<Item> registryLookup, RecipeOutput output, ItemLike ingredient, Block result)
            {
                Identifier resultId = AdvancedNetherite.getRegistryHelper().getBlockById(result);
                Identifier ingredientId = AdvancedNetherite.getRegistryHelper().getItemById((Item) ingredient);

                // Items to Block
                ShapedRecipeBuilder.shaped(registryLookup, RecipeCategory.BUILDING_BLOCKS, result)
                        .define('#', ingredient)
                        .pattern("###")
                        .pattern("###")
                        .pattern("###")
                        .unlockedBy("has_" + ingredientId.getPath(), has(ingredient))
                        .save(output);

                // Block to Items
                ShapelessRecipeBuilder.shapeless(registryLookup, RecipeCategory.MISC, ingredient, 9)
                        .requires(result)
                        .unlockedBy("has_" + resultId.getPath(), has(result))
                        .save(output, Identifier.fromNamespaceAndPath(resultId.getNamespace(), ingredientId.getPath() + "_from_block").toString());
            }

            private void backpackRecipes(HolderLookup.RegistryLookup<Item> registryLookup, RecipeOutput output)
            {
                ShapelessRecipeBuilder.shapeless(registryLookup, RecipeCategory.MISC, ModBackpackItems.LEVEL_1)
                        .requires(Items.LEATHER).requires(Items.LEATHER).requires(Items.LEATHER)
                        .requires(Items.STRING).requires(Items.STRING).requires(Items.STRING)
                        .requires(Items.GOLD_INGOT).requires(Items.GOLD_INGOT).requires(Items.GOLD_INGOT)
                        .unlockedBy("has_leather", has(Items.LEATHER))
                        .save(output);

                ShapelessRecipeBuilder.shapeless(registryLookup, RecipeCategory.MISC, ModBackpackItems.LEVEL_2)
                        .requires(ModBackpackItems.LEVEL_1)
                        .requires(Items.DIAMOND).requires(Items.DIAMOND).requires(Items.DIAMOND).requires(Items.DIAMOND)
                        .requires(Items.IRON_INGOT).requires(Items.IRON_INGOT)
                        .requires(Items.LEATHER).requires(Items.LEATHER)
                        .unlockedBy("has_backpack_level_1", has(ModBackpackItems.LEVEL_1))
                        .save(output);

                ShapelessRecipeBuilder.shapeless(registryLookup, RecipeCategory.MISC, ModBackpackItems.LEVEL_3)
                        .requires(ModBackpackItems.LEVEL_2)
                        .requires(Items.NETHERITE_INGOT).requires(Items.NETHERITE_INGOT)
                        .requires(Items.DIAMOND).requires(Items.DIAMOND)
                        .requires(Items.BLAZE_ROD).requires(Items.BLAZE_ROD)
                        .requires(Items.LEATHER).requires(Items.LEATHER)
                        .unlockedBy("has_backpack_level_2", has(ModBackpackItems.LEVEL_2))
                        .save(output);
            }

            private void lodestoneRecipe(HolderLookup.RegistryLookup<Item> registryLookup, RecipeOutput output)
            {
                ShapedRecipeBuilder.shaped(registryLookup, RecipeCategory.MISC, Items.LODESTONE)
                        .define('S', Items.CHISELED_STONE_BRICKS)
                        .define('#', ModTags.NETHERITE_INGOTS)
                        .pattern("SSS")
                        .pattern("S#S")
                        .pattern("SSS")
                        .unlockedBy("has_chiseled_stone_bricks", has(Items.CHISELED_STONE_BRICKS))
                        .unlockedBy("has_netherite_ingots", has(ModTags.NETHERITE_INGOTS))
                        .save(output, Identifier.fromNamespaceAndPath(AdvancedNetherite.MOD_ID, AdvancedNetherite.getRegistryHelper().getItemById(Items.LODESTONE).getPath()).toString());
            }
        };
    }

    @Override
    public String getName()
    {
        return "Advanced Netherite Recipe Provider";
    }
}
