package com.autovw.advancednetherite.datagen.providers;

import com.autovw.advancednetherite.core.ModBlocks;
import com.autovw.advancednetherite.core.ModItems;
import com.autovw.advancednetherite.core.ModBackpackItems;
import com.autovw.advancednetherite.core.ModRewardCouponItems;
import com.autovw.advancednetherite.core.util.ModEquipmentAssets;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.EquipmentAsset;

import java.util.List;
import java.util.Optional;

/**
 * @author Autovw
 */
public class ModModelProvider extends FabricModelProvider
{
    public ModModelProvider(FabricPackOutput output)
    {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators generator)
    {
        generator.createTrivialCube(ModBlocks.ASH_BLOCK);
        generator.createTrivialCube(ModBlocks.SUNLIGHT_BLOCK);
        generator.createTrivialCube(ModBlocks.SOUL_BLOCK);
        generator.createTrivialCube(ModBlocks.FROST_BLOCK);

        generator.createFlatItemModel(ModBlocks.ASH_BLOCK.asItem());
        generator.createFlatItemModel(ModBlocks.SUNLIGHT_BLOCK.asItem());
        generator.createFlatItemModel(ModBlocks.SOUL_BLOCK.asItem());
        generator.createFlatItemModel(ModBlocks.FROST_BLOCK.asItem());
    }

    @Override
    public void generateItemModels(ItemModelGenerators generator)
    {
        itemModel(generator, ModItems.ASH_INGOT);
        itemModel(generator, ModItems.SUNLIGHT_INGOT);
        itemModel(generator, ModItems.SOUL_INGOT);
        itemModel(generator, ModItems.FROST_INGOT);
        
        // bitcoin 추가
        itemModel(generator, ModItems.BITCOIN);
        itemModel(generator, ModRewardCouponItems.EXPERIENCE_TRIPLE_COUPON);
        itemModel(generator, ModRewardCouponItems.BITCOIN_DOUBLE_COUPON);
        itemModel(generator, ModRewardCouponItems.BITCOIN_TRIPLE_COUPON);
        itemModel(generator, ModBackpackItems.LEVEL_1);
        itemModel(generator, ModBackpackItems.LEVEL_2);
        itemModel(generator, ModBackpackItems.LEVEL_3);
        
        // randomBox 1~4
        itemModel(generator, ModItems.RANDOM_BOX_I);
        itemModel(generator, ModItems.RANDOM_BOX_II);
        itemModel(generator, ModItems.RANDOM_BOX_III);
        itemModel(generator, ModItems.RANDOM_BOX_IV);
        
        // rewardKey 1~4
        itemModel(generator, ModItems.REWARD_KEY_I);
        itemModel(generator, ModItems.REWARD_KEY_II);
        itemModel(generator, ModItems.REWARD_KEY_III);
        itemModel(generator, ModItems.REWARD_KEY_IV);
        
        // 강화조각 / 강화보석
        itemModel(generator, ModItems.ENHANCEMENT_SHARD);
        itemModel(generator, ModItems.ENHANCEMENT_GEM);

        // 직업선택권 외 주문서
        itemModel(generator, ModItems.JOB_SELECT_TICKET);
        itemModel(generator, ModItems.DEATH_ITEM_PROTECTION_SCROLL);
        itemModel(generator, ModItems.LAND_PURCHASE_DOCUMENT);
        itemModel(generator, ModItems.ENHANCE_PROTECTION_SCROLL);
        itemModel(generator, ModItems.ENHANCE_SUCCESS_SCROLL_3);
        itemModel(generator, ModItems.ENHANCE_SUCCESS_SCROLL_5);
        itemModel(generator, ModItems.ENHANCE_SUCCESS_SCROLL_7);
        itemModel(generator, ModItems.ENHANCE_SUCCESS_SCROLL_10);

        // petbox
        itemModel(generator, ModItems.NOMAL_PETBOX);
        itemModel(generator, ModItems.RARE_PETBOX);
        itemModel(generator, ModItems.LEGEND_PETBOX);

        armorModel(generator, ModItems.ASH_HELMET, ModEquipmentAssets.ASH);
        armorModel(generator, ModItems.ASH_CHESTPLATE, ModEquipmentAssets.ASH);
        armorModel(generator, ModItems.ASH_LEGGINGS, ModEquipmentAssets.ASH);
        armorModel(generator, ModItems.ASH_BOOTS, ModEquipmentAssets.ASH);

        armorModel(generator, ModItems.SUNLIGHT_HELMET, ModEquipmentAssets.SUNLIGHT);
        armorModel(generator, ModItems.SUNLIGHT_CHESTPLATE, ModEquipmentAssets.SUNLIGHT);
        armorModel(generator, ModItems.SUNLIGHT_LEGGINGS, ModEquipmentAssets.SUNLIGHT);
        armorModel(generator, ModItems.SUNLIGHT_BOOTS, ModEquipmentAssets.SUNLIGHT);

        armorModel(generator, ModItems.SOUL_HELMET, ModEquipmentAssets.SOUL);
        armorModel(generator, ModItems.SOUL_CHESTPLATE, ModEquipmentAssets.SOUL);
        armorModel(generator, ModItems.SOUL_LEGGINGS, ModEquipmentAssets.SOUL);
        armorModel(generator, ModItems.SOUL_BOOTS, ModEquipmentAssets.SOUL);

        armorModel(generator, ModItems.FROST_HELMET, ModEquipmentAssets.FROST);
        armorModel(generator, ModItems.FROST_CHESTPLATE, ModEquipmentAssets.FROST);
        armorModel(generator, ModItems.FROST_LEGGINGS, ModEquipmentAssets.FROST);
        armorModel(generator, ModItems.FROST_BOOTS, ModEquipmentAssets.FROST);

        toolModel(generator, ModItems.ASH_AXE);
        toolModel(generator, ModItems.SUNLIGHT_AXE);
        toolModel(generator, ModItems.SOUL_AXE);
        toolModel(generator, ModItems.FROST_AXE);

        toolModel(generator, ModItems.ASH_HOE);
        toolModel(generator, ModItems.SUNLIGHT_HOE);
        toolModel(generator, ModItems.SOUL_HOE);
        toolModel(generator, ModItems.FROST_HOE);

        toolModel(generator, ModItems.ASH_PICKAXE);
        toolModel(generator, ModItems.SUNLIGHT_PICKAXE);
        toolModel(generator, ModItems.SOUL_PICKAXE);
        toolModel(generator, ModItems.FROST_PICKAXE);

        toolModel(generator, ModItems.ASH_SHOVEL);
        toolModel(generator, ModItems.SUNLIGHT_SHOVEL);
        toolModel(generator, ModItems.SOUL_SHOVEL);
        toolModel(generator, ModItems.FROST_SHOVEL);

        toolModel(generator, ModItems.ASH_SWORD);
        toolModel(generator, ModItems.SUNLIGHT_SWORD);
        toolModel(generator, ModItems.SOUL_SWORD);
        toolModel(generator, ModItems.FROST_SWORD);

        spearModel(generator, ModItems.ASH_SPEAR);
        spearModel(generator, ModItems.SUNLIGHT_SPEAR);
        spearModel(generator, ModItems.SOUL_SPEAR);
        spearModel(generator, ModItems.FROST_SPEAR);
    }

    public void itemModel(ItemModelGenerators itemModels, Item item)
    {
        this.itemModel(itemModels, item, ModelTemplates.FLAT_ITEM);
    }

    public void toolModel(ItemModelGenerators itemModels, Item item)
    {
        this.itemModel(itemModels, item, ModelTemplates.FLAT_HANDHELD_ITEM);
    }

    public void itemModel(ItemModelGenerators itemModels, Item item, ModelTemplate template)
    {
        Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
        Identifier textureLoc = Identifier.fromNamespaceAndPath(itemId.getNamespace(), "item/" + itemId.getPath());
        TextureMapping textureMapping = new TextureMapping().put(TextureSlot.LAYER0, new Material(textureLoc));
        itemModels.itemModelOutput.accept(item, new CuboidItemModelWrapper.Unbaked(template.create(item, textureMapping, itemModels.modelOutput), Optional.empty(), List.of()));
    }

    public void armorModel(ItemModelGenerators itemModels, Item item, ResourceKey<EquipmentAsset> equipmentKey)
    {
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        Identifier armorType = null;
        if (id.getPath().contains("helmet"))
            armorType = ItemModelGenerators.TRIM_PREFIX_HELMET;
        else if (id.getPath().contains("chestplate"))
            armorType = ItemModelGenerators.TRIM_PREFIX_CHESTPLATE;
        else if (id.getPath().contains("leggings"))
            armorType = ItemModelGenerators.TRIM_PREFIX_LEGGINGS;
        else if (id.getPath().contains("boots"))
            armorType = ItemModelGenerators.TRIM_PREFIX_BOOTS;
        itemModels.generateTrimmableItem(item, equipmentKey, armorType, false);
    }

    public void spearModel(ItemModelGenerators itemModels, Item item)
    {
        Identifier itemId = BuiltInRegistries.ITEM.getKey(item);
        Identifier textureLoc = Identifier.fromNamespaceAndPath(itemId.getNamespace(), "item/" + itemId.getPath());
        TextureMapping textureMapping = new TextureMapping().put(TextureSlot.LAYER0, new Material(textureLoc));
        CuboidItemModelWrapper.Unbaked model = new CuboidItemModelWrapper.Unbaked(ModelTemplates.FLAT_ITEM.create(item, textureMapping, itemModels.modelOutput), Optional.empty(), List.of());

        TextureMapping textureMappingInHand = new TextureMapping().put(TextureSlot.LAYER0, new Material(Identifier.fromNamespaceAndPath(itemId.getNamespace(), "item/" + itemId.getPath() + "_in_hand")));
        CuboidItemModelWrapper.Unbaked modelInHand = new CuboidItemModelWrapper.Unbaked(ModelTemplates.SPEAR_IN_HAND.create(item, textureMappingInHand, itemModels.modelOutput), Optional.empty(), List.of());

        itemModels.itemModelOutput.accept(item, ItemModelGenerators.createFlatModelDispatch(model, modelInHand), new ClientItem.Properties(true, false, 1.95F));
    }
}
