package com.autovw.advancednetherite.datagen.providers;

import com.autovw.advancednetherite.core.ModItems;
import com.autovw.advancednetherite.core.util.FabricModTags;
import com.autovw.advancednetherite.core.util.ModTags;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

/**
 * @author Autovw
 */
public class ModItemTagsProvider extends FabricTagsProvider.ItemTagsProvider
{
    public ModItemTagsProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> completableFuture)
    {
        super(output, completableFuture, null);
    }

    @Override
    protected void addTags(HolderLookup.Provider arg)
    {
        // Mod item tags
        tag(ModTags.NETHERITE_BLOCKITEMS)
                .add(ModItems.NETHERITE_IRON_BLOCK.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_BLOCK.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_EMERALD_BLOCK.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_DIAMOND_BLOCK.builtInRegistryHolder().key());
        tag(ModTags.NETHERITE_INGOTS)
                .add(ModItems.NETHERITE_IRON_INGOT.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_INGOT.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_EMERALD_INGOT.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_DIAMOND_INGOT.builtInRegistryHolder().key());
        tag(ModTags.UPGRADE_TO_NETHERITE_IRON).addTag(ModTags.INGOTS_NETHERITES_IRON);
        tag(ModTags.UPGRADE_TO_NETHERITE_GOLD).addTag(ModTags.INGOTS_NETHERITES_GOLD);
        tag(ModTags.UPGRADE_TO_NETHERITE_EMERALD).addTag(ModTags.INGOTS_NETHERITES_EMERALD);
        tag(ModTags.UPGRADE_TO_NETHERITE_DIAMOND).addTag(ModTags.INGOTS_NETHERITES_DIAMOND);

        tag(ModTags.INGOTS_NETHERITES).add(Items.NETHERITE_INGOT.builtInRegistryHolder().key());
        tag(ModTags.INGOTS_NETHERITES_IRON).add(ModItems.NETHERITE_IRON_INGOT.builtInRegistryHolder().key());
        tag(ModTags.INGOTS_NETHERITES_GOLD).add(ModItems.NETHERITE_GOLD_INGOT.builtInRegistryHolder().key());
        tag(ModTags.INGOTS_NETHERITES_EMERALD).add(ModItems.NETHERITE_EMERALD_INGOT.builtInRegistryHolder().key());
        tag(ModTags.INGOTS_NETHERITES_DIAMOND).add(ModItems.NETHERITE_DIAMOND_INGOT.builtInRegistryHolder().key());
        tag(ModTags.INGOTS_UPGRADE_TO_NETHERITE_IRON).addTag(ModTags.INGOTS_NETHERITES);
        tag(ModTags.INGOTS_UPGRADE_TO_NETHERITE_GOLD).addTag(ModTags.INGOTS_NETHERITES_IRON);
        tag(ModTags.INGOTS_UPGRADE_TO_NETHERITE_EMERALD).addTag(ModTags.INGOTS_NETHERITES_GOLD);
        tag(ModTags.INGOTS_UPGRADE_TO_NETHERITE_DIAMOND).addTag(ModTags.INGOTS_NETHERITES_EMERALD);

        tag(ModTags.AXE_NETHERITE).add(Items.NETHERITE_AXE.builtInRegistryHolder().key());
        tag(ModTags.AXE_NETHERITE_IRON).add(ModItems.NETHERITE_IRON_AXE.builtInRegistryHolder().key());
        tag(ModTags.AXE_NETHERITE_GOLD).add(ModItems.NETHERITE_GOLD_AXE.builtInRegistryHolder().key());
        tag(ModTags.AXE_NETHERITE_EMERALD).add(ModItems.NETHERITE_EMERALD_AXE.builtInRegistryHolder().key());
        tag(ModTags.AXE_NETHERITE_DIAMOND).add(ModItems.NETHERITE_DIAMOND_AXE.builtInRegistryHolder().key());
        tag(ModTags.AXE_UPGRADE_TO_NETHERITE_IRON).addTag(ModTags.AXE_NETHERITE);
        tag(ModTags.AXE_UPGRADE_TO_NETHERITE_GOLD).addTag(ModTags.AXE_NETHERITE_IRON);
        tag(ModTags.AXE_UPGRADE_TO_NETHERITE_EMERALD).addTag(ModTags.AXE_NETHERITE_GOLD);
        tag(ModTags.AXE_UPGRADE_TO_NETHERITE_DIAMOND).addTag(ModTags.AXE_NETHERITE_EMERALD);

        tag(ModTags.HOE_NETHERITE).add(Items.NETHERITE_HOE.builtInRegistryHolder().key());
        tag(ModTags.HOE_NETHERITE_IRON).add(ModItems.NETHERITE_IRON_HOE.builtInRegistryHolder().key());
        tag(ModTags.HOE_NETHERITE_GOLD).add(ModItems.NETHERITE_GOLD_HOE.builtInRegistryHolder().key());
        tag(ModTags.HOE_NETHERITE_EMERALD).add(ModItems.NETHERITE_EMERALD_HOE.builtInRegistryHolder().key());
        tag(ModTags.HOE_NETHERITE_DIAMOND).add(ModItems.NETHERITE_DIAMOND_HOE.builtInRegistryHolder().key());
        tag(ModTags.HOE_UPGRADE_TO_NETHERITE_IRON).addTag(ModTags.HOE_NETHERITE);
        tag(ModTags.HOE_UPGRADE_TO_NETHERITE_GOLD).addTag(ModTags.HOE_NETHERITE_IRON);
        tag(ModTags.HOE_UPGRADE_TO_NETHERITE_EMERALD).addTag(ModTags.HOE_NETHERITE_GOLD);
        tag(ModTags.HOE_UPGRADE_TO_NETHERITE_DIAMOND).addTag(ModTags.HOE_NETHERITE_EMERALD);

        tag(ModTags.PICKAXE_NETHERITE).add(Items.NETHERITE_PICKAXE.builtInRegistryHolder().key());
        tag(ModTags.PICKAXE_NETHERITE_IRON).add(ModItems.NETHERITE_IRON_PICKAXE.builtInRegistryHolder().key());
        tag(ModTags.PICKAXE_NETHERITE_GOLD).add(ModItems.NETHERITE_GOLD_PICKAXE.builtInRegistryHolder().key());
        tag(ModTags.PICKAXE_NETHERITE_EMERALD).add(ModItems.NETHERITE_EMERALD_PICKAXE.builtInRegistryHolder().key());
        tag(ModTags.PICKAXE_NETHERITE_DIAMOND).add(ModItems.NETHERITE_DIAMOND_PICKAXE.builtInRegistryHolder().key());
        tag(ModTags.PICKAXE_UPGRADE_TO_NETHERITE_IRON).addTag(ModTags.PICKAXE_NETHERITE);
        tag(ModTags.PICKAXE_UPGRADE_TO_NETHERITE_GOLD).addTag(ModTags.PICKAXE_NETHERITE_IRON);
        tag(ModTags.PICKAXE_UPGRADE_TO_NETHERITE_EMERALD).addTag(ModTags.PICKAXE_NETHERITE_GOLD);
        tag(ModTags.PICKAXE_UPGRADE_TO_NETHERITE_DIAMOND).addTag(ModTags.PICKAXE_NETHERITE_EMERALD);

        tag(ModTags.SHOVEL_NETHERITE).add(Items.NETHERITE_SHOVEL.builtInRegistryHolder().key());
        tag(ModTags.SHOVEL_NETHERITE_IRON).add(ModItems.NETHERITE_IRON_SHOVEL.builtInRegistryHolder().key());
        tag(ModTags.SHOVEL_NETHERITE_GOLD).add(ModItems.NETHERITE_GOLD_SHOVEL.builtInRegistryHolder().key());
        tag(ModTags.SHOVEL_NETHERITE_EMERALD).add(ModItems.NETHERITE_EMERALD_SHOVEL.builtInRegistryHolder().key());
        tag(ModTags.SHOVEL_NETHERITE_DIAMOND).add(ModItems.NETHERITE_DIAMOND_SHOVEL.builtInRegistryHolder().key());
        tag(ModTags.SHOVEL_UPGRADE_TO_NETHERITE_IRON).addTag(ModTags.SHOVEL_NETHERITE);
        tag(ModTags.SHOVEL_UPGRADE_TO_NETHERITE_GOLD).addTag(ModTags.SHOVEL_NETHERITE_IRON);
        tag(ModTags.SHOVEL_UPGRADE_TO_NETHERITE_EMERALD).addTag(ModTags.SHOVEL_NETHERITE_GOLD);
        tag(ModTags.SHOVEL_UPGRADE_TO_NETHERITE_DIAMOND).addTag(ModTags.SHOVEL_NETHERITE_EMERALD);

        tag(ModTags.SWORD_NETHERITE).add(Items.NETHERITE_SWORD.builtInRegistryHolder().key());
        tag(ModTags.SWORD_NETHERITE_IRON).add(ModItems.NETHERITE_IRON_SWORD.builtInRegistryHolder().key());
        tag(ModTags.SWORD_NETHERITE_GOLD).add(ModItems.NETHERITE_GOLD_SWORD.builtInRegistryHolder().key());
        tag(ModTags.SWORD_NETHERITE_EMERALD).add(ModItems.NETHERITE_EMERALD_SWORD.builtInRegistryHolder().key());
        tag(ModTags.SWORD_NETHERITE_DIAMOND).add(ModItems.NETHERITE_DIAMOND_SWORD.builtInRegistryHolder().key());
        tag(ModTags.SWORD_UPGRADE_TO_NETHERITE_IRON).addTag(ModTags.SWORD_NETHERITE);
        tag(ModTags.SWORD_UPGRADE_TO_NETHERITE_GOLD).addTag(ModTags.SWORD_NETHERITE_IRON);
        tag(ModTags.SWORD_UPGRADE_TO_NETHERITE_EMERALD).addTag(ModTags.SWORD_NETHERITE_GOLD);
        tag(ModTags.SWORD_UPGRADE_TO_NETHERITE_DIAMOND).addTag(ModTags.SWORD_NETHERITE_EMERALD);

        tag(ModTags.SPEAR_NETHERITE).add(Items.NETHERITE_SPEAR.builtInRegistryHolder().key());
        tag(ModTags.SPEAR_NETHERITE_IRON).add(ModItems.NETHERITE_IRON_SPEAR.builtInRegistryHolder().key());
        tag(ModTags.SPEAR_NETHERITE_GOLD).add(ModItems.NETHERITE_GOLD_SPEAR.builtInRegistryHolder().key());
        tag(ModTags.SPEAR_NETHERITE_EMERALD).add(ModItems.NETHERITE_EMERALD_SPEAR.builtInRegistryHolder().key());
        tag(ModTags.SPEAR_NETHERITE_DIAMOND).add(ModItems.NETHERITE_DIAMOND_SPEAR.builtInRegistryHolder().key());
        tag(ModTags.SPEAR_UPGRADE_TO_NETHERITE_IRON).addTag(ModTags.SPEAR_NETHERITE);
        tag(ModTags.SPEAR_UPGRADE_TO_NETHERITE_GOLD).addTag(ModTags.SPEAR_NETHERITE_IRON);
        tag(ModTags.SPEAR_UPGRADE_TO_NETHERITE_EMERALD).addTag(ModTags.SPEAR_NETHERITE_GOLD);
        tag(ModTags.SPEAR_UPGRADE_TO_NETHERITE_DIAMOND).addTag(ModTags.SPEAR_NETHERITE_EMERALD);

        tag(ModTags.HELMET_NETHERITE).add(Items.NETHERITE_HELMET.builtInRegistryHolder().key());
        tag(ModTags.HELMET_NETHERITE_IRON).add(ModItems.NETHERITE_IRON_HELMET.builtInRegistryHolder().key());
        tag(ModTags.HELMET_NETHERITE_GOLD).add(ModItems.NETHERITE_GOLD_HELMET.builtInRegistryHolder().key());
        tag(ModTags.HELMET_NETHERITE_EMERALD).add(ModItems.NETHERITE_EMERALD_HELMET.builtInRegistryHolder().key());
        tag(ModTags.HELMET_NETHERITE_DIAMOND).add(ModItems.NETHERITE_DIAMOND_HELMET.builtInRegistryHolder().key());
        tag(ModTags.HELMET_UPGRADE_TO_NETHERITE_IRON).addTag(ModTags.HELMET_NETHERITE);
        tag(ModTags.HELMET_UPGRADE_TO_NETHERITE_GOLD).addTag(ModTags.HELMET_NETHERITE_IRON);
        tag(ModTags.HELMET_UPGRADE_TO_NETHERITE_EMERALD).addTag(ModTags.HELMET_NETHERITE_GOLD);
        tag(ModTags.HELMET_UPGRADE_TO_NETHERITE_DIAMOND).addTag(ModTags.HELMET_NETHERITE_EMERALD);

        tag(ModTags.CHESTPLATE_NETHERITE).add(Items.NETHERITE_CHESTPLATE.builtInRegistryHolder().key());
        tag(ModTags.CHESTPLATE_NETHERITE_IRON).add(ModItems.NETHERITE_IRON_CHESTPLATE.builtInRegistryHolder().key());
        tag(ModTags.CHESTPLATE_NETHERITE_GOLD).add(ModItems.NETHERITE_GOLD_CHESTPLATE.builtInRegistryHolder().key());
        tag(ModTags.CHESTPLATE_NETHERITE_EMERALD).add(ModItems.NETHERITE_EMERALD_CHESTPLATE.builtInRegistryHolder().key());
        tag(ModTags.CHESTPLATE_NETHERITE_DIAMOND).add(ModItems.NETHERITE_DIAMOND_CHESTPLATE.builtInRegistryHolder().key());
        tag(ModTags.CHESTPLATE_UPGRADE_TO_NETHERITE_IRON).addTag(ModTags.CHESTPLATE_NETHERITE);
        tag(ModTags.CHESTPLATE_UPGRADE_TO_NETHERITE_GOLD).addTag(ModTags.CHESTPLATE_NETHERITE_IRON);
        tag(ModTags.CHESTPLATE_UPGRADE_TO_NETHERITE_EMERALD).addTag(ModTags.CHESTPLATE_NETHERITE_GOLD);
        tag(ModTags.CHESTPLATE_UPGRADE_TO_NETHERITE_DIAMOND).addTag(ModTags.CHESTPLATE_NETHERITE_EMERALD);

        tag(ModTags.LEGGINGS_NETHERITE).add(Items.NETHERITE_LEGGINGS.builtInRegistryHolder().key());
        tag(ModTags.LEGGINGS_NETHERITE_IRON).add(ModItems.NETHERITE_IRON_LEGGINGS.builtInRegistryHolder().key());
        tag(ModTags.LEGGINGS_NETHERITE_GOLD).add(ModItems.NETHERITE_GOLD_LEGGINGS.builtInRegistryHolder().key());
        tag(ModTags.LEGGINGS_NETHERITE_EMERALD).add(ModItems.NETHERITE_EMERALD_LEGGINGS.builtInRegistryHolder().key());
        tag(ModTags.LEGGINGS_NETHERITE_DIAMOND).add(ModItems.NETHERITE_DIAMOND_LEGGINGS.builtInRegistryHolder().key());
        tag(ModTags.LEGGINGS_UPGRADE_TO_NETHERITE_IRON).addTag(ModTags.LEGGINGS_NETHERITE);
        tag(ModTags.LEGGINGS_UPGRADE_TO_NETHERITE_GOLD).addTag(ModTags.LEGGINGS_NETHERITE_IRON);
        tag(ModTags.LEGGINGS_UPGRADE_TO_NETHERITE_EMERALD).addTag(ModTags.LEGGINGS_NETHERITE_GOLD);
        tag(ModTags.LEGGINGS_UPGRADE_TO_NETHERITE_DIAMOND).addTag(ModTags.LEGGINGS_NETHERITE_EMERALD);

        tag(ModTags.BOOTS_NETHERITE).add(Items.NETHERITE_BOOTS.builtInRegistryHolder().key());
        tag(ModTags.BOOTS_NETHERITE_IRON).add(ModItems.NETHERITE_IRON_BOOTS.builtInRegistryHolder().key());
        tag(ModTags.BOOTS_NETHERITE_GOLD).add(ModItems.NETHERITE_GOLD_BOOTS.builtInRegistryHolder().key());
        tag(ModTags.BOOTS_NETHERITE_EMERALD).add(ModItems.NETHERITE_EMERALD_BOOTS.builtInRegistryHolder().key());
        tag(ModTags.BOOTS_NETHERITE_DIAMOND).add(ModItems.NETHERITE_DIAMOND_BOOTS.builtInRegistryHolder().key());
        tag(ModTags.BOOTS_UPGRADE_TO_NETHERITE_IRON).addTag(ModTags.BOOTS_NETHERITE);
        tag(ModTags.BOOTS_UPGRADE_TO_NETHERITE_GOLD).addTag(ModTags.BOOTS_NETHERITE_IRON);
        tag(ModTags.BOOTS_UPGRADE_TO_NETHERITE_EMERALD).addTag(ModTags.BOOTS_NETHERITE_GOLD);
        tag(ModTags.BOOTS_UPGRADE_TO_NETHERITE_DIAMOND).addTag(ModTags.BOOTS_NETHERITE_EMERALD);

        // tiers
        tag(ModTags.TIER_ARMOR)
                .addTag(ModTags.TIER_ARMOR_NETHERITE_IRON)
                .addTag(ModTags.TIER_ARMOR_NETHERITE_GOLD)
                .addTag(ModTags.TIER_ARMOR_NETHERITE_EMERALD)
                .addTag(ModTags.TIER_ARMOR_NETHERITE_DIAMOND);
        tag(ModTags.TIER_ARMOR_NETHERITE_IRON)
                .add(ModItems.NETHERITE_IRON_HELMET.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_IRON_CHESTPLATE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_IRON_LEGGINGS.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_IRON_BOOTS.builtInRegistryHolder().key());
        tag(ModTags.TIER_ARMOR_NETHERITE_GOLD)
                .add(ModItems.NETHERITE_GOLD_HELMET.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_CHESTPLATE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_LEGGINGS.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_BOOTS.builtInRegistryHolder().key());
        tag(ModTags.TIER_ARMOR_NETHERITE_EMERALD)
                .add(ModItems.NETHERITE_EMERALD_HELMET.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_EMERALD_CHESTPLATE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_EMERALD_LEGGINGS.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_EMERALD_BOOTS.builtInRegistryHolder().key());
        tag(ModTags.TIER_ARMOR_NETHERITE_DIAMOND)
                .add(ModItems.NETHERITE_DIAMOND_HELMET.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_DIAMOND_CHESTPLATE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_DIAMOND_LEGGINGS.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_DIAMOND_BOOTS.builtInRegistryHolder().key());
        tag(ModTags.TIER_TOOLS)
                .addTag(ModTags.TIER_TOOL_NETHERITE_IRON)
                .addTag(ModTags.TIER_TOOL_NETHERITE_GOLD)
                .addTag(ModTags.TIER_TOOL_NETHERITE_EMERALD)
                .addTag(ModTags.TIER_TOOL_NETHERITE_DIAMOND);
        tag(ModTags.TIER_TOOL_NETHERITE_IRON)
                .add(ModItems.NETHERITE_IRON_AXE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_IRON_HOE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_IRON_PICKAXE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_IRON_SHOVEL.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_IRON_SWORD.builtInRegistryHolder().key());
        tag(ModTags.TIER_TOOL_NETHERITE_GOLD)
                .add(ModItems.NETHERITE_GOLD_AXE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_HOE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_PICKAXE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_SHOVEL.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_SWORD.builtInRegistryHolder().key());
        tag(ModTags.TIER_TOOL_NETHERITE_EMERALD)
                .add(ModItems.NETHERITE_EMERALD_AXE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_EMERALD_HOE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_EMERALD_PICKAXE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_EMERALD_SHOVEL.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_EMERALD_SWORD.builtInRegistryHolder().key());
        tag(ModTags.TIER_TOOL_NETHERITE_DIAMOND)
                .add(ModItems.NETHERITE_DIAMOND_AXE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_DIAMOND_HOE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_DIAMOND_PICKAXE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_DIAMOND_SHOVEL.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_DIAMOND_SWORD.builtInRegistryHolder().key());

        // Repair tags
        tag(ModTags.REPAIRS_NETHERITE_IRON_ARMOR)
                .add(ModItems.NETHERITE_IRON_INGOT.builtInRegistryHolder().key());
        tag(ModTags.REPAIRS_NETHERITE_GOLD_ARMOR)
                .add(ModItems.NETHERITE_GOLD_INGOT.builtInRegistryHolder().key());
        tag(ModTags.REPAIRS_NETHERITE_EMERALD_ARMOR)
                .add(ModItems.NETHERITE_EMERALD_INGOT.builtInRegistryHolder().key());
        tag(ModTags.REPAIRS_NETHERITE_DIAMOND_ARMOR)
                .add(ModItems.NETHERITE_DIAMOND_INGOT.builtInRegistryHolder().key());

        tag(ModTags.REPAIRS_NETHERITE_IRON_TOOLS)
                .add(ModItems.NETHERITE_IRON_INGOT.builtInRegistryHolder().key());
        tag(ModTags.REPAIRS_NETHERITE_GOLD_TOOLS)
                .add(ModItems.NETHERITE_GOLD_INGOT.builtInRegistryHolder().key());
        tag(ModTags.REPAIRS_NETHERITE_EMERALD_TOOLS)
                .add(ModItems.NETHERITE_EMERALD_INGOT.builtInRegistryHolder().key());
        tag(ModTags.REPAIRS_NETHERITE_DIAMOND_TOOLS)
                .add(ModItems.NETHERITE_DIAMOND_INGOT.builtInRegistryHolder().key());

        // Pacify armor tags
        tag(ModTags.PACIFY_PHANTOMS_ARMOR)
                .addTag(ModTags.TIER_ARMOR_NETHERITE_IRON)
                .addTag(ModTags.TIER_ARMOR_NETHERITE_DIAMOND);
        tag(ModTags.PACIFY_PIGLINS_ARMOR)
                .addTag(ModTags.TIER_ARMOR_NETHERITE_GOLD)
                .addTag(ModTags.TIER_ARMOR_NETHERITE_DIAMOND);
        tag(ModTags.PACIFY_ENDERMEN_ARMOR)
                .addTag(ModTags.TIER_ARMOR_NETHERITE_EMERALD)
                .addTag(ModTags.TIER_ARMOR_NETHERITE_DIAMOND);

        // additional drop tags
        tag(ModTags.DROPS_ADDITIONAL_CROPS)
                .add(ModItems.NETHERITE_IRON_HOE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_HOE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_EMERALD_HOE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_DIAMOND_HOE.builtInRegistryHolder().key());

        tag(ModTags.DROPS_ADDITIONAL_IRON)
                .add(ModItems.NETHERITE_IRON_PICKAXE.builtInRegistryHolder().key());
        tag(ModTags.DROPS_ADDITIONAL_GOLD)
                .add(ModItems.NETHERITE_GOLD_PICKAXE.builtInRegistryHolder().key());
        tag(ModTags.DROPS_ADDITIONAL_EMERALD)
                .add(ModItems.NETHERITE_EMERALD_PICKAXE.builtInRegistryHolder().key());
        tag(ModTags.DROPS_ADDITIONAL_DIAMOND)
                .add(ModItems.NETHERITE_DIAMOND_PICKAXE.builtInRegistryHolder().key());

        tag(ModTags.DROPS_ADDITIONAL_PHANTOM_LOOT)
                .add(ModItems.NETHERITE_IRON_SWORD.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_DIAMOND_SWORD.builtInRegistryHolder().key());
        tag(ModTags.DROPS_ADDITIONAL_ZOMBIFIED_PIGLIN_LOOT)
                .add(ModItems.NETHERITE_GOLD_SWORD.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_DIAMOND_SWORD.builtInRegistryHolder().key());
        tag(ModTags.DROPS_ADDITIONAL_PIGLIN_LOOT)
                .add(ModItems.NETHERITE_GOLD_SWORD.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_DIAMOND_SWORD.builtInRegistryHolder().key());
        tag(ModTags.DROPS_ADDITIONAL_ENDERMAN_LOOT)
                .add(ModItems.NETHERITE_EMERALD_SWORD.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_DIAMOND_SWORD.builtInRegistryHolder().key());


        // Vanilla item tags
        tag(ItemTags.BEACON_PAYMENT_ITEMS)
                .addTag(ModTags.NETHERITE_INGOTS);
        tag(ItemTags.PIGLIN_LOVED)
                .add(ModItems.NETHERITE_GOLD_BLOCK.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_INGOT.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_HELMET.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_CHESTPLATE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_LEGGINGS.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_BOOTS.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_AXE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_HOE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_PICKAXE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_SHOVEL.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_SWORD.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_SPEAR.builtInRegistryHolder().key());
        tag(ItemTags.PIGLIN_SAFE_ARMOR)
                .addTag(ModTags.PACIFY_PIGLINS_ARMOR);

        tag(ItemTags.CLUSTER_MAX_HARVESTABLES)
                .add(ModItems.NETHERITE_IRON_PICKAXE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_PICKAXE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_EMERALD_PICKAXE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_DIAMOND_PICKAXE.builtInRegistryHolder().key());
        tag(ItemTags.TRIMMABLE_ARMOR)
                .add(ModItems.NETHERITE_DIAMOND_HELMET.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_DIAMOND_CHESTPLATE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_DIAMOND_LEGGINGS.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_DIAMOND_BOOTS.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_EMERALD_HELMET.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_EMERALD_CHESTPLATE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_EMERALD_LEGGINGS.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_EMERALD_BOOTS.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_HELMET.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_CHESTPLATE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_LEGGINGS.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_BOOTS.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_IRON_HELMET.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_IRON_CHESTPLATE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_IRON_LEGGINGS.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_IRON_BOOTS.builtInRegistryHolder().key());

        tag(ItemTags.HEAD_ARMOR)
                .add(ModItems.NETHERITE_IRON_HELMET.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_HELMET.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_EMERALD_HELMET.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_DIAMOND_HELMET.builtInRegistryHolder().key());
        tag(ItemTags.CHEST_ARMOR)
                .add(ModItems.NETHERITE_IRON_CHESTPLATE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_CHESTPLATE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_EMERALD_CHESTPLATE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_DIAMOND_CHESTPLATE.builtInRegistryHolder().key());
        tag(ItemTags.LEG_ARMOR)
                .add(ModItems.NETHERITE_IRON_LEGGINGS.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_LEGGINGS.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_EMERALD_LEGGINGS.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_DIAMOND_LEGGINGS.builtInRegistryHolder().key());
        tag(ItemTags.FOOT_ARMOR)
                .add(ModItems.NETHERITE_IRON_BOOTS.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_BOOTS.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_EMERALD_BOOTS.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_DIAMOND_BOOTS.builtInRegistryHolder().key());

        tag(ItemTags.AXES)
                .add(ModItems.NETHERITE_IRON_AXE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_AXE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_EMERALD_AXE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_DIAMOND_AXE.builtInRegistryHolder().key());
        tag(ItemTags.HOES)
                .add(ModItems.NETHERITE_IRON_HOE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_HOE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_EMERALD_HOE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_DIAMOND_HOE.builtInRegistryHolder().key());
        tag(ItemTags.PICKAXES)
                .add(ModItems.NETHERITE_IRON_PICKAXE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_PICKAXE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_EMERALD_PICKAXE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_DIAMOND_PICKAXE.builtInRegistryHolder().key());
        tag(ItemTags.SHOVELS)
                .add(ModItems.NETHERITE_IRON_SHOVEL.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_SHOVEL.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_EMERALD_SHOVEL.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_DIAMOND_SHOVEL.builtInRegistryHolder().key());
        tag(ItemTags.SWORDS)
                .add(ModItems.NETHERITE_IRON_SWORD.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_SWORD.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_EMERALD_SWORD.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_DIAMOND_SWORD.builtInRegistryHolder().key());
        tag(ItemTags.SPEARS)
                .add(ModItems.NETHERITE_IRON_SPEAR.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_SPEAR.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_EMERALD_SPEAR.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_DIAMOND_SPEAR.builtInRegistryHolder().key());


        // Tooltip Rareness item tags
        tag(FabricModTags.TOOLTIP_RARENESS_EPIC_ITEM)
                .addTag(ModTags.NETHERITE_BLOCKITEMS)
                .addTag(ModTags.NETHERITE_INGOTS)
                .add(ModItems.NETHERITE_IRON_AXE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_AXE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_EMERALD_AXE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_DIAMOND_AXE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_IRON_HOE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_HOE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_EMERALD_HOE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_DIAMOND_HOE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_IRON_PICKAXE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_PICKAXE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_EMERALD_PICKAXE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_DIAMOND_PICKAXE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_IRON_SHOVEL.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_SHOVEL.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_EMERALD_SHOVEL.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_DIAMOND_SHOVEL.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_IRON_SWORD.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_SWORD.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_EMERALD_SWORD.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_DIAMOND_SWORD.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_IRON_HELMET.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_HELMET.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_EMERALD_HELMET.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_DIAMOND_HELMET.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_IRON_CHESTPLATE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_CHESTPLATE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_EMERALD_CHESTPLATE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_DIAMOND_CHESTPLATE.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_IRON_LEGGINGS.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_LEGGINGS.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_EMERALD_LEGGINGS.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_DIAMOND_LEGGINGS.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_IRON_BOOTS.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_GOLD_BOOTS.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_EMERALD_BOOTS.builtInRegistryHolder().key())
                .add(ModItems.NETHERITE_DIAMOND_BOOTS.builtInRegistryHolder().key());
    }
}
