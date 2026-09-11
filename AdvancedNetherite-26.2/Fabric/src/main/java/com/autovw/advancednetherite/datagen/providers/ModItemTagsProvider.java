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
                .add(ModItems.ASH_BLOCK.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_BLOCK.builtInRegistryHolder().key())
                .add(ModItems.SOUL_BLOCK.builtInRegistryHolder().key())
                .add(ModItems.FROST_BLOCK.builtInRegistryHolder().key());
        tag(ModTags.NETHERITE_INGOTS)
                .add(ModItems.ASH_INGOT.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_INGOT.builtInRegistryHolder().key())
                .add(ModItems.SOUL_INGOT.builtInRegistryHolder().key())
                .add(ModItems.FROST_INGOT.builtInRegistryHolder().key());
        tag(ModTags.UPGRADE_TO_ASH).addTag(ModTags.INGOTS_NETHERITES_IRON);
        tag(ModTags.UPGRADE_TO_SUNLIGHT).addTag(ModTags.INGOTS_NETHERITES_GOLD);
        tag(ModTags.UPGRADE_TO_SOUL).addTag(ModTags.INGOTS_NETHERITES_EMERALD);
        tag(ModTags.UPGRADE_TO_FROST).addTag(ModTags.INGOTS_NETHERITES_DIAMOND);

        tag(ModTags.INGOTS_NETHERITES).add(Items.NETHERITE_INGOT.builtInRegistryHolder().key());
        tag(ModTags.INGOTS_NETHERITES_IRON).add(ModItems.ASH_INGOT.builtInRegistryHolder().key());
        tag(ModTags.INGOTS_NETHERITES_GOLD).add(ModItems.SUNLIGHT_INGOT.builtInRegistryHolder().key());
        tag(ModTags.INGOTS_NETHERITES_EMERALD).add(ModItems.SOUL_INGOT.builtInRegistryHolder().key());
        tag(ModTags.INGOTS_NETHERITES_DIAMOND).add(ModItems.FROST_INGOT.builtInRegistryHolder().key());
        tag(ModTags.INGOTS_UPGRADE_TO_ASH).addTag(ModTags.INGOTS_NETHERITES);
        tag(ModTags.INGOTS_UPGRADE_TO_SUNLIGHT).addTag(ModTags.INGOTS_NETHERITES_IRON);
        tag(ModTags.INGOTS_UPGRADE_TO_SOUL).addTag(ModTags.INGOTS_NETHERITES_GOLD);
        tag(ModTags.INGOTS_UPGRADE_TO_FROST).addTag(ModTags.INGOTS_NETHERITES_EMERALD);

        tag(ModTags.AXE_NETHERITE).add(Items.NETHERITE_AXE.builtInRegistryHolder().key());
        tag(ModTags.AXE_ASH).add(ModItems.ASH_AXE.builtInRegistryHolder().key());
        tag(ModTags.AXE_SUNLIGHT).add(ModItems.SUNLIGHT_AXE.builtInRegistryHolder().key());
        tag(ModTags.AXE_SOUL).add(ModItems.SOUL_AXE.builtInRegistryHolder().key());
        tag(ModTags.AXE_FROST).add(ModItems.FROST_AXE.builtInRegistryHolder().key());
        tag(ModTags.AXE_UPGRADE_TO_ASH).addTag(ModTags.AXE_NETHERITE);
        tag(ModTags.AXE_UPGRADE_TO_SUNLIGHT).addTag(ModTags.AXE_ASH);
        tag(ModTags.AXE_UPGRADE_TO_SOUL).addTag(ModTags.AXE_SUNLIGHT);
        tag(ModTags.AXE_UPGRADE_TO_FROST).addTag(ModTags.AXE_SOUL);

        tag(ModTags.HOE_NETHERITE).add(Items.NETHERITE_HOE.builtInRegistryHolder().key());
        tag(ModTags.HOE_ASH).add(ModItems.ASH_HOE.builtInRegistryHolder().key());
        tag(ModTags.HOE_SUNLIGHT).add(ModItems.SUNLIGHT_HOE.builtInRegistryHolder().key());
        tag(ModTags.HOE_SOUL).add(ModItems.SOUL_HOE.builtInRegistryHolder().key());
        tag(ModTags.HOE_FROST).add(ModItems.FROST_HOE.builtInRegistryHolder().key());
        tag(ModTags.HOE_UPGRADE_TO_ASH).addTag(ModTags.HOE_NETHERITE);
        tag(ModTags.HOE_UPGRADE_TO_SUNLIGHT).addTag(ModTags.HOE_ASH);
        tag(ModTags.HOE_UPGRADE_TO_SOUL).addTag(ModTags.HOE_SUNLIGHT);
        tag(ModTags.HOE_UPGRADE_TO_FROST).addTag(ModTags.HOE_SOUL);

        tag(ModTags.PICKAXE_NETHERITE).add(Items.NETHERITE_PICKAXE.builtInRegistryHolder().key());
        tag(ModTags.PICKAXE_ASH).add(ModItems.ASH_PICKAXE.builtInRegistryHolder().key());
        tag(ModTags.PICKAXE_SUNLIGHT).add(ModItems.SUNLIGHT_PICKAXE.builtInRegistryHolder().key());
        tag(ModTags.PICKAXE_SOUL).add(ModItems.SOUL_PICKAXE.builtInRegistryHolder().key());
        tag(ModTags.PICKAXE_FROST).add(ModItems.FROST_PICKAXE.builtInRegistryHolder().key());
        tag(ModTags.PICKAXE_UPGRADE_TO_ASH).addTag(ModTags.PICKAXE_NETHERITE);
        tag(ModTags.PICKAXE_UPGRADE_TO_SUNLIGHT).addTag(ModTags.PICKAXE_ASH);
        tag(ModTags.PICKAXE_UPGRADE_TO_SOUL).addTag(ModTags.PICKAXE_SUNLIGHT);
        tag(ModTags.PICKAXE_UPGRADE_TO_FROST).addTag(ModTags.PICKAXE_SOUL);

        tag(ModTags.SHOVEL_NETHERITE).add(Items.NETHERITE_SHOVEL.builtInRegistryHolder().key());
        tag(ModTags.SHOVEL_ASH).add(ModItems.ASH_SHOVEL.builtInRegistryHolder().key());
        tag(ModTags.SHOVEL_SUNLIGHT).add(ModItems.SUNLIGHT_SHOVEL.builtInRegistryHolder().key());
        tag(ModTags.SHOVEL_SOUL).add(ModItems.SOUL_SHOVEL.builtInRegistryHolder().key());
        tag(ModTags.SHOVEL_FROST).add(ModItems.FROST_SHOVEL.builtInRegistryHolder().key());
        tag(ModTags.SHOVEL_UPGRADE_TO_ASH).addTag(ModTags.SHOVEL_NETHERITE);
        tag(ModTags.SHOVEL_UPGRADE_TO_SUNLIGHT).addTag(ModTags.SHOVEL_ASH);
        tag(ModTags.SHOVEL_UPGRADE_TO_SOUL).addTag(ModTags.SHOVEL_SUNLIGHT);
        tag(ModTags.SHOVEL_UPGRADE_TO_FROST).addTag(ModTags.SHOVEL_SOUL);

        tag(ModTags.SWORD_NETHERITE).add(Items.NETHERITE_SWORD.builtInRegistryHolder().key());
        tag(ModTags.SWORD_ASH).add(ModItems.ASH_SWORD.builtInRegistryHolder().key());
        tag(ModTags.SWORD_SUNLIGHT).add(ModItems.SUNLIGHT_SWORD.builtInRegistryHolder().key());
        tag(ModTags.SWORD_SOUL).add(ModItems.SOUL_SWORD.builtInRegistryHolder().key());
        tag(ModTags.SWORD_FROST).add(ModItems.FROST_SWORD.builtInRegistryHolder().key());
        tag(ModTags.SWORD_UPGRADE_TO_ASH).addTag(ModTags.SWORD_NETHERITE);
        tag(ModTags.SWORD_UPGRADE_TO_SUNLIGHT).addTag(ModTags.SWORD_ASH);
        tag(ModTags.SWORD_UPGRADE_TO_SOUL).addTag(ModTags.SWORD_SUNLIGHT);
        tag(ModTags.SWORD_UPGRADE_TO_FROST).addTag(ModTags.SWORD_SOUL);

        tag(ModTags.SPEAR_NETHERITE).add(Items.NETHERITE_SPEAR.builtInRegistryHolder().key());
        tag(ModTags.SPEAR_ASH).add(ModItems.ASH_SPEAR.builtInRegistryHolder().key());
        tag(ModTags.SPEAR_SUNLIGHT).add(ModItems.SUNLIGHT_SPEAR.builtInRegistryHolder().key());
        tag(ModTags.SPEAR_SOUL).add(ModItems.SOUL_SPEAR.builtInRegistryHolder().key());
        tag(ModTags.SPEAR_FROST).add(ModItems.FROST_SPEAR.builtInRegistryHolder().key());
        tag(ModTags.SPEAR_UPGRADE_TO_ASH).addTag(ModTags.SPEAR_NETHERITE);
        tag(ModTags.SPEAR_UPGRADE_TO_SUNLIGHT).addTag(ModTags.SPEAR_ASH);
        tag(ModTags.SPEAR_UPGRADE_TO_SOUL).addTag(ModTags.SPEAR_SUNLIGHT);
        tag(ModTags.SPEAR_UPGRADE_TO_FROST).addTag(ModTags.SPEAR_SOUL);

        tag(ModTags.HELMET_NETHERITE).add(Items.NETHERITE_HELMET.builtInRegistryHolder().key());
        tag(ModTags.HELMET_ASH).add(ModItems.ASH_HELMET.builtInRegistryHolder().key());
        tag(ModTags.HELMET_SUNLIGHT).add(ModItems.SUNLIGHT_HELMET.builtInRegistryHolder().key());
        tag(ModTags.HELMET_SOUL).add(ModItems.SOUL_HELMET.builtInRegistryHolder().key());
        tag(ModTags.HELMET_FROST).add(ModItems.FROST_HELMET.builtInRegistryHolder().key());
        tag(ModTags.HELMET_UPGRADE_TO_ASH).addTag(ModTags.HELMET_NETHERITE);
        tag(ModTags.HELMET_UPGRADE_TO_SUNLIGHT).addTag(ModTags.HELMET_ASH);
        tag(ModTags.HELMET_UPGRADE_TO_SOUL).addTag(ModTags.HELMET_SUNLIGHT);
        tag(ModTags.HELMET_UPGRADE_TO_FROST).addTag(ModTags.HELMET_SOUL);

        tag(ModTags.CHESTPLATE_NETHERITE).add(Items.NETHERITE_CHESTPLATE.builtInRegistryHolder().key());
        tag(ModTags.CHESTPLATE_ASH).add(ModItems.ASH_CHESTPLATE.builtInRegistryHolder().key());
        tag(ModTags.CHESTPLATE_SUNLIGHT).add(ModItems.SUNLIGHT_CHESTPLATE.builtInRegistryHolder().key());
        tag(ModTags.CHESTPLATE_SOUL).add(ModItems.SOUL_CHESTPLATE.builtInRegistryHolder().key());
        tag(ModTags.CHESTPLATE_FROST).add(ModItems.FROST_CHESTPLATE.builtInRegistryHolder().key());
        tag(ModTags.CHESTPLATE_UPGRADE_TO_ASH).addTag(ModTags.CHESTPLATE_NETHERITE);
        tag(ModTags.CHESTPLATE_UPGRADE_TO_SUNLIGHT).addTag(ModTags.CHESTPLATE_ASH);
        tag(ModTags.CHESTPLATE_UPGRADE_TO_SOUL).addTag(ModTags.CHESTPLATE_SUNLIGHT);
        tag(ModTags.CHESTPLATE_UPGRADE_TO_FROST).addTag(ModTags.CHESTPLATE_SOUL);

        tag(ModTags.LEGGINGS_NETHERITE).add(Items.NETHERITE_LEGGINGS.builtInRegistryHolder().key());
        tag(ModTags.LEGGINGS_ASH).add(ModItems.ASH_LEGGINGS.builtInRegistryHolder().key());
        tag(ModTags.LEGGINGS_SUNLIGHT).add(ModItems.SUNLIGHT_LEGGINGS.builtInRegistryHolder().key());
        tag(ModTags.LEGGINGS_SOUL).add(ModItems.SOUL_LEGGINGS.builtInRegistryHolder().key());
        tag(ModTags.LEGGINGS_FROST).add(ModItems.FROST_LEGGINGS.builtInRegistryHolder().key());
        tag(ModTags.LEGGINGS_UPGRADE_TO_ASH).addTag(ModTags.LEGGINGS_NETHERITE);
        tag(ModTags.LEGGINGS_UPGRADE_TO_SUNLIGHT).addTag(ModTags.LEGGINGS_ASH);
        tag(ModTags.LEGGINGS_UPGRADE_TO_SOUL).addTag(ModTags.LEGGINGS_SUNLIGHT);
        tag(ModTags.LEGGINGS_UPGRADE_TO_FROST).addTag(ModTags.LEGGINGS_SOUL);

        tag(ModTags.BOOTS_NETHERITE).add(Items.NETHERITE_BOOTS.builtInRegistryHolder().key());
        tag(ModTags.BOOTS_ASH).add(ModItems.ASH_BOOTS.builtInRegistryHolder().key());
        tag(ModTags.BOOTS_SUNLIGHT).add(ModItems.SUNLIGHT_BOOTS.builtInRegistryHolder().key());
        tag(ModTags.BOOTS_SOUL).add(ModItems.SOUL_BOOTS.builtInRegistryHolder().key());
        tag(ModTags.BOOTS_FROST).add(ModItems.FROST_BOOTS.builtInRegistryHolder().key());
        tag(ModTags.BOOTS_UPGRADE_TO_ASH).addTag(ModTags.BOOTS_NETHERITE);
        tag(ModTags.BOOTS_UPGRADE_TO_SUNLIGHT).addTag(ModTags.BOOTS_ASH);
        tag(ModTags.BOOTS_UPGRADE_TO_SOUL).addTag(ModTags.BOOTS_SUNLIGHT);
        tag(ModTags.BOOTS_UPGRADE_TO_FROST).addTag(ModTags.BOOTS_SOUL);

        // tiers
        tag(ModTags.TIER_ARMOR)
                .addTag(ModTags.TIER_ARMOR_ASH)
                .addTag(ModTags.TIER_ARMOR_SUNLIGHT)
                .addTag(ModTags.TIER_ARMOR_SOUL)
                .addTag(ModTags.TIER_ARMOR_FROST);
        tag(ModTags.TIER_ARMOR_ASH)
                .add(ModItems.ASH_HELMET.builtInRegistryHolder().key())
                .add(ModItems.ASH_CHESTPLATE.builtInRegistryHolder().key())
                .add(ModItems.ASH_LEGGINGS.builtInRegistryHolder().key())
                .add(ModItems.ASH_BOOTS.builtInRegistryHolder().key());
        tag(ModTags.TIER_ARMOR_SUNLIGHT)
                .add(ModItems.SUNLIGHT_HELMET.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_CHESTPLATE.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_LEGGINGS.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_BOOTS.builtInRegistryHolder().key());
        tag(ModTags.TIER_ARMOR_SOUL)
                .add(ModItems.SOUL_HELMET.builtInRegistryHolder().key())
                .add(ModItems.SOUL_CHESTPLATE.builtInRegistryHolder().key())
                .add(ModItems.SOUL_LEGGINGS.builtInRegistryHolder().key())
                .add(ModItems.SOUL_BOOTS.builtInRegistryHolder().key());
        tag(ModTags.TIER_ARMOR_FROST)
                .add(ModItems.FROST_HELMET.builtInRegistryHolder().key())
                .add(ModItems.FROST_CHESTPLATE.builtInRegistryHolder().key())
                .add(ModItems.FROST_LEGGINGS.builtInRegistryHolder().key())
                .add(ModItems.FROST_BOOTS.builtInRegistryHolder().key());
        tag(ModTags.TIER_TOOLS)
                .addTag(ModTags.TIER_TOOL_ASH)
                .addTag(ModTags.TIER_TOOL_SUNLIGHT)
                .addTag(ModTags.TIER_TOOL_SOUL)
                .addTag(ModTags.TIER_TOOL_FROST);
        tag(ModTags.TIER_TOOL_ASH)
                .add(ModItems.ASH_AXE.builtInRegistryHolder().key())
                .add(ModItems.ASH_HOE.builtInRegistryHolder().key())
                .add(ModItems.ASH_PICKAXE.builtInRegistryHolder().key())
                .add(ModItems.ASH_SHOVEL.builtInRegistryHolder().key())
                .add(ModItems.ASH_SWORD.builtInRegistryHolder().key());
        tag(ModTags.TIER_TOOL_SUNLIGHT)
                .add(ModItems.SUNLIGHT_AXE.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_HOE.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_PICKAXE.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_SHOVEL.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_SWORD.builtInRegistryHolder().key());
        tag(ModTags.TIER_TOOL_SOUL)
                .add(ModItems.SOUL_AXE.builtInRegistryHolder().key())
                .add(ModItems.SOUL_HOE.builtInRegistryHolder().key())
                .add(ModItems.SOUL_PICKAXE.builtInRegistryHolder().key())
                .add(ModItems.SOUL_SHOVEL.builtInRegistryHolder().key())
                .add(ModItems.SOUL_SWORD.builtInRegistryHolder().key());
        tag(ModTags.TIER_TOOL_FROST)
                .add(ModItems.FROST_AXE.builtInRegistryHolder().key())
                .add(ModItems.FROST_HOE.builtInRegistryHolder().key())
                .add(ModItems.FROST_PICKAXE.builtInRegistryHolder().key())
                .add(ModItems.FROST_SHOVEL.builtInRegistryHolder().key())
                .add(ModItems.FROST_SWORD.builtInRegistryHolder().key());

        // Repair tags
        tag(ModTags.REPAIRS_ASH_ARMOR)
                .add(ModItems.ASH_INGOT.builtInRegistryHolder().key());
        tag(ModTags.REPAIRS_SUNLIGHT_ARMOR)
                .add(ModItems.SUNLIGHT_INGOT.builtInRegistryHolder().key());
        tag(ModTags.REPAIRS_SOUL_ARMOR)
                .add(ModItems.SOUL_INGOT.builtInRegistryHolder().key());
        tag(ModTags.REPAIRS_FROST_ARMOR)
                .add(ModItems.FROST_INGOT.builtInRegistryHolder().key());

        tag(ModTags.REPAIRS_ASH_TOOLS)
                .add(ModItems.ASH_INGOT.builtInRegistryHolder().key());
        tag(ModTags.REPAIRS_SUNLIGHT_TOOLS)
                .add(ModItems.SUNLIGHT_INGOT.builtInRegistryHolder().key());
        tag(ModTags.REPAIRS_SOUL_TOOLS)
                .add(ModItems.SOUL_INGOT.builtInRegistryHolder().key());
        tag(ModTags.REPAIRS_FROST_TOOLS)
                .add(ModItems.FROST_INGOT.builtInRegistryHolder().key());

        // Pacify armor tags
        tag(ModTags.PACIFY_PHANTOMS_ARMOR)
                .addTag(ModTags.TIER_ARMOR_ASH)
                .addTag(ModTags.TIER_ARMOR_FROST);
        tag(ModTags.PACIFY_PIGLINS_ARMOR)
                .addTag(ModTags.TIER_ARMOR_SUNLIGHT)
                .addTag(ModTags.TIER_ARMOR_FROST);
        tag(ModTags.PACIFY_ENDERMEN_ARMOR)
                .addTag(ModTags.TIER_ARMOR_SOUL)
                .addTag(ModTags.TIER_ARMOR_FROST);

        // additional drop tags
        tag(ModTags.DROPS_ADDITIONAL_CROPS)
                .add(ModItems.ASH_HOE.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_HOE.builtInRegistryHolder().key())
                .add(ModItems.SOUL_HOE.builtInRegistryHolder().key())
                .add(ModItems.FROST_HOE.builtInRegistryHolder().key());

        tag(ModTags.DROPS_ADDITIONAL_IRON)
                .add(ModItems.ASH_PICKAXE.builtInRegistryHolder().key());
        tag(ModTags.DROPS_ADDITIONAL_GOLD)
                .add(ModItems.SUNLIGHT_PICKAXE.builtInRegistryHolder().key());
        tag(ModTags.DROPS_ADDITIONAL_EMERALD)
                .add(ModItems.SOUL_PICKAXE.builtInRegistryHolder().key());
        tag(ModTags.DROPS_ADDITIONAL_DIAMOND)
                .add(ModItems.FROST_PICKAXE.builtInRegistryHolder().key());

        tag(ModTags.DROPS_ADDITIONAL_PHANTOM_LOOT)
                .add(ModItems.ASH_SWORD.builtInRegistryHolder().key())
                .add(ModItems.FROST_SWORD.builtInRegistryHolder().key());
        tag(ModTags.DROPS_ADDITIONAL_ZOMBIFIED_PIGLIN_LOOT)
                .add(ModItems.SUNLIGHT_SWORD.builtInRegistryHolder().key())
                .add(ModItems.FROST_SWORD.builtInRegistryHolder().key());
        tag(ModTags.DROPS_ADDITIONAL_PIGLIN_LOOT)
                .add(ModItems.SUNLIGHT_SWORD.builtInRegistryHolder().key())
                .add(ModItems.FROST_SWORD.builtInRegistryHolder().key());
        tag(ModTags.DROPS_ADDITIONAL_ENDERMAN_LOOT)
                .add(ModItems.SOUL_SWORD.builtInRegistryHolder().key())
                .add(ModItems.FROST_SWORD.builtInRegistryHolder().key());


        // Vanilla item tags
        tag(ItemTags.BEACON_PAYMENT_ITEMS)
                .addTag(ModTags.NETHERITE_INGOTS);
        tag(ItemTags.PIGLIN_LOVED)
                .add(ModItems.SUNLIGHT_BLOCK.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_INGOT.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_HELMET.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_CHESTPLATE.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_LEGGINGS.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_BOOTS.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_AXE.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_HOE.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_PICKAXE.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_SHOVEL.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_SWORD.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_SPEAR.builtInRegistryHolder().key());
        tag(ItemTags.PIGLIN_SAFE_ARMOR)
                .addTag(ModTags.PACIFY_PIGLINS_ARMOR);

        tag(ItemTags.CLUSTER_MAX_HARVESTABLES)
                .add(ModItems.ASH_PICKAXE.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_PICKAXE.builtInRegistryHolder().key())
                .add(ModItems.SOUL_PICKAXE.builtInRegistryHolder().key())
                .add(ModItems.FROST_PICKAXE.builtInRegistryHolder().key());
        tag(ItemTags.TRIMMABLE_ARMOR)
                .add(ModItems.FROST_HELMET.builtInRegistryHolder().key())
                .add(ModItems.FROST_CHESTPLATE.builtInRegistryHolder().key())
                .add(ModItems.FROST_LEGGINGS.builtInRegistryHolder().key())
                .add(ModItems.FROST_BOOTS.builtInRegistryHolder().key())
                .add(ModItems.SOUL_HELMET.builtInRegistryHolder().key())
                .add(ModItems.SOUL_CHESTPLATE.builtInRegistryHolder().key())
                .add(ModItems.SOUL_LEGGINGS.builtInRegistryHolder().key())
                .add(ModItems.SOUL_BOOTS.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_HELMET.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_CHESTPLATE.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_LEGGINGS.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_BOOTS.builtInRegistryHolder().key())
                .add(ModItems.ASH_HELMET.builtInRegistryHolder().key())
                .add(ModItems.ASH_CHESTPLATE.builtInRegistryHolder().key())
                .add(ModItems.ASH_LEGGINGS.builtInRegistryHolder().key())
                .add(ModItems.ASH_BOOTS.builtInRegistryHolder().key());

        tag(ItemTags.HEAD_ARMOR)
                .add(ModItems.ASH_HELMET.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_HELMET.builtInRegistryHolder().key())
                .add(ModItems.SOUL_HELMET.builtInRegistryHolder().key())
                .add(ModItems.FROST_HELMET.builtInRegistryHolder().key());
        tag(ItemTags.CHEST_ARMOR)
                .add(ModItems.ASH_CHESTPLATE.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_CHESTPLATE.builtInRegistryHolder().key())
                .add(ModItems.SOUL_CHESTPLATE.builtInRegistryHolder().key())
                .add(ModItems.FROST_CHESTPLATE.builtInRegistryHolder().key());
        tag(ItemTags.LEG_ARMOR)
                .add(ModItems.ASH_LEGGINGS.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_LEGGINGS.builtInRegistryHolder().key())
                .add(ModItems.SOUL_LEGGINGS.builtInRegistryHolder().key())
                .add(ModItems.FROST_LEGGINGS.builtInRegistryHolder().key());
        tag(ItemTags.FOOT_ARMOR)
                .add(ModItems.ASH_BOOTS.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_BOOTS.builtInRegistryHolder().key())
                .add(ModItems.SOUL_BOOTS.builtInRegistryHolder().key())
                .add(ModItems.FROST_BOOTS.builtInRegistryHolder().key());

        tag(ItemTags.AXES)
                .add(ModItems.ASH_AXE.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_AXE.builtInRegistryHolder().key())
                .add(ModItems.SOUL_AXE.builtInRegistryHolder().key())
                .add(ModItems.FROST_AXE.builtInRegistryHolder().key());
        tag(ItemTags.HOES)
                .add(ModItems.ASH_HOE.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_HOE.builtInRegistryHolder().key())
                .add(ModItems.SOUL_HOE.builtInRegistryHolder().key())
                .add(ModItems.FROST_HOE.builtInRegistryHolder().key());
        tag(ItemTags.PICKAXES)
                .add(ModItems.ASH_PICKAXE.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_PICKAXE.builtInRegistryHolder().key())
                .add(ModItems.SOUL_PICKAXE.builtInRegistryHolder().key())
                .add(ModItems.FROST_PICKAXE.builtInRegistryHolder().key());
        tag(ItemTags.SHOVELS)
                .add(ModItems.ASH_SHOVEL.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_SHOVEL.builtInRegistryHolder().key())
                .add(ModItems.SOUL_SHOVEL.builtInRegistryHolder().key())
                .add(ModItems.FROST_SHOVEL.builtInRegistryHolder().key());
        tag(ItemTags.SWORDS)
                .add(ModItems.ASH_SWORD.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_SWORD.builtInRegistryHolder().key())
                .add(ModItems.SOUL_SWORD.builtInRegistryHolder().key())
                .add(ModItems.FROST_SWORD.builtInRegistryHolder().key());
        tag(ItemTags.SPEARS)
                .add(ModItems.ASH_SPEAR.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_SPEAR.builtInRegistryHolder().key())
                .add(ModItems.SOUL_SPEAR.builtInRegistryHolder().key())
                .add(ModItems.FROST_SPEAR.builtInRegistryHolder().key());


        // Tooltip Rareness item tags
        tag(FabricModTags.TOOLTIP_RARENESS_EPIC_ITEM)
                .addTag(ModTags.NETHERITE_BLOCKITEMS)
                .addTag(ModTags.NETHERITE_INGOTS)
                .add(ModItems.ASH_AXE.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_AXE.builtInRegistryHolder().key())
                .add(ModItems.SOUL_AXE.builtInRegistryHolder().key())
                .add(ModItems.FROST_AXE.builtInRegistryHolder().key())
                .add(ModItems.ASH_HOE.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_HOE.builtInRegistryHolder().key())
                .add(ModItems.SOUL_HOE.builtInRegistryHolder().key())
                .add(ModItems.FROST_HOE.builtInRegistryHolder().key())
                .add(ModItems.ASH_PICKAXE.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_PICKAXE.builtInRegistryHolder().key())
                .add(ModItems.SOUL_PICKAXE.builtInRegistryHolder().key())
                .add(ModItems.FROST_PICKAXE.builtInRegistryHolder().key())
                .add(ModItems.ASH_SHOVEL.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_SHOVEL.builtInRegistryHolder().key())
                .add(ModItems.SOUL_SHOVEL.builtInRegistryHolder().key())
                .add(ModItems.FROST_SHOVEL.builtInRegistryHolder().key())
                .add(ModItems.ASH_SWORD.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_SWORD.builtInRegistryHolder().key())
                .add(ModItems.SOUL_SWORD.builtInRegistryHolder().key())
                .add(ModItems.FROST_SWORD.builtInRegistryHolder().key())
                .add(ModItems.ASH_HELMET.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_HELMET.builtInRegistryHolder().key())
                .add(ModItems.SOUL_HELMET.builtInRegistryHolder().key())
                .add(ModItems.FROST_HELMET.builtInRegistryHolder().key())
                .add(ModItems.ASH_CHESTPLATE.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_CHESTPLATE.builtInRegistryHolder().key())
                .add(ModItems.SOUL_CHESTPLATE.builtInRegistryHolder().key())
                .add(ModItems.FROST_CHESTPLATE.builtInRegistryHolder().key())
                .add(ModItems.ASH_LEGGINGS.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_LEGGINGS.builtInRegistryHolder().key())
                .add(ModItems.SOUL_LEGGINGS.builtInRegistryHolder().key())
                .add(ModItems.FROST_LEGGINGS.builtInRegistryHolder().key())
                .add(ModItems.ASH_BOOTS.builtInRegistryHolder().key())
                .add(ModItems.SUNLIGHT_BOOTS.builtInRegistryHolder().key())
                .add(ModItems.SOUL_BOOTS.builtInRegistryHolder().key())
                .add(ModItems.FROST_BOOTS.builtInRegistryHolder().key());
    }
}
