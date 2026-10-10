package com.autovw.advancednetherite.common;

import com.autovw.advancednetherite.api.annotation.Internal;
import com.autovw.advancednetherite.api.impl.IAdvancedHooks;
import com.autovw.advancednetherite.api.impl.IArmorMaterial;
import com.autovw.advancednetherite.api.impl.IToolMaterial;
import com.autovw.advancednetherite.common.item.AdvancedArmorItem;
import com.autovw.advancednetherite.config.ConfigHelper;
import com.autovw.advancednetherite.core.util.ModArmorMaterials;
import com.autovw.advancednetherite.core.util.ModToolMaterials;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Objects;

/**
 * @author Autovw
 */
public class AdvancedUtil
{
    /**
     * 갑옷 재질의 내구도 배율을 구하는 도우미 메서드
     * @param material 배율을 구할 재질
     * @return 해당 갑옷 재질의 내구도 배율
     */
    public static int getArmorDurabilityMultiplier(ArmorMaterial material)
    {
        if (material == ModArmorMaterials.ASH)
            return 38;
        if (material == ModArmorMaterials.SUNLIGHT)
            return 39;
        if (material == ModArmorMaterials.SOUL)
            return 40;
        if (material == ModArmorMaterials.FROST)
            return 41;
        return 0;
    }

    /**
     * 도구에 맞는 내구도 막대 색을 구하는 도우미 메서드
     * @param originalColor 원래 내구도 막대 색
     * @param stack 도구 스택
     * @return 등급과 설정에 따른 막대 색
     */
    public static int getDurabilityBarColor(int originalColor, ItemStack stack)
    {
        int newColor = originalColor;

        if (ConfigHelper.get().getClient().matchingDurabilityBars())
        {
            // 도구
            if (stack.getItem() instanceof IToolMaterial material)
            {
                if (material.isMaterial(ModToolMaterials.ASH))
                    newColor = getColor(ChatFormatting.GRAY);
                if (material.isMaterial(ModToolMaterials.SUNLIGHT))
                    newColor = getColor(ChatFormatting.GOLD);
                if (material.isMaterial(ModToolMaterials.SOUL))
                    newColor = getColor(ChatFormatting.DARK_GREEN);
                if (material.isMaterial(ModToolMaterials.FROST))
                    newColor = getColor(ChatFormatting.AQUA);
            }

            // 갑옷
            if (stack.getItem() instanceof IArmorMaterial material)
            {
                if (material.isMaterial(ModArmorMaterials.ASH))
                    newColor = getColor(ChatFormatting.GRAY);
                if (material.isMaterial(ModArmorMaterials.SUNLIGHT))
                    newColor = getColor(ChatFormatting.GOLD);
                if (material.isMaterial(ModArmorMaterials.SOUL))
                    newColor = getColor(ChatFormatting.DARK_GREEN);
                if (material.isMaterial(ModArmorMaterials.FROST))
                    newColor = getColor(ChatFormatting.AQUA);
            }
        }

        return newColor;
    }

    @Internal
    private static int getColor(ChatFormatting color)
    {
        return Objects.requireNonNull(Style.EMPTY.withColor(color).getColor()).getValue();
    }

    /**
     * 도구에 맞는 블록 파괴 속도를 적용하는 도우미 메서드
     * @param originalSpeed 원래 파괴 속도
     * @param stack 도구 스택
     * @param state 부수는 블록의 상태
     * @return 새 파괴 속도
     */
    public static float getDestroySpeed(float originalSpeed, ItemStack stack, BlockState state)
    {
        float newSpeed = originalSpeed;

        if (stack.getItem() instanceof IToolMaterial material)
        {
            if (stack.getItem().isCorrectToolForDrops(stack, state))
            {
                if (material.isMaterial(ModToolMaterials.ASH))
                    newSpeed *= ConfigHelper.get().getServer().getToolProperties().getIronBreakingSpeedMultiplier();
                if (material.isMaterial(ModToolMaterials.SUNLIGHT))
                    newSpeed *= ConfigHelper.get().getServer().getToolProperties().getGoldBreakingSpeedMultiplier();
                if (material.isMaterial(ModToolMaterials.SOUL))
                    newSpeed *= ConfigHelper.get().getServer().getToolProperties().getEmeraldBreakingSpeedMultiplier();
                if (material.isMaterial(ModToolMaterials.FROST))
                    newSpeed *= ConfigHelper.get().getServer().getToolProperties().getDiamondBreakingSpeedMultiplier();
            }
        }

        return newSpeed;
    }

    /**
     * 엔더맨이 자극받기 전까지 플레이어에게 우호적으로 행동할지 정한다.
     * @param player 갑옷을 착용한 플레이어
     * @return 엔더맨이 우호적으로 행동해야 하면 true
     */
    public static boolean isWearingEndermanPassiveArmor(Player player)
    {
        for (EquipmentSlot slot : EquipmentSlotGroup.ARMOR)
        {
            ItemStack stack = player.getItemBySlot(slot);
            Item item = stack.getItem();
            if ((item instanceof AdvancedArmorItem && ((AdvancedArmorItem) item).pacifiesEndermen(stack)) || (item instanceof IAdvancedHooks && ((IAdvancedHooks) item).pacifyEndermen(stack)))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * 팬텀이 자극받기 전까지 플레이어에게 우호적으로 행동할지 정한다.
     * @param player 갑옷을 착용한 플레이어
     * @return 팬텀이 우호적으로 행동해야 하면 true
     */
    public static boolean isWearingPhantomPassiveArmor(Player player)
    {
        for (EquipmentSlot slot : EquipmentSlotGroup.ARMOR)
        {
            ItemStack stack = player.getItemBySlot(slot);
            Item item = stack.getItem();
            if ((item instanceof AdvancedArmorItem && ((AdvancedArmorItem) item).pacifiesPhantoms(stack)) || (item instanceof IAdvancedHooks && ((IAdvancedHooks) item).pacifyPhantoms(stack)))
            {
                return true;
            }
        }

        return false;
    }
}
