package com.daqem.jobsplus.player.job.hyper;

import com.daqem.arc.player.SkillActivationNotifier;
import com.daqem.jobsplus.JobsPlus;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 황금사과 제작 변환과 황금당근 섭취 시 임시 체력을 처리한다.
 * 변환은 결과를 실제로 꺼낸 뒤 판정하며, 조합 미리보기와 자동 제작기는 추첨하지 않는다.
 */
public final class HyperFarmerHandler
{
    private HyperFarmerHandler() {}

    /** 바닐라 섭취 효과를 모두 적용한 뒤 호출된다. 발동하면 황금사과류와 중첩되는 흡수 몫을 등록한다. */
    public static void eatGoldenCarrot(LivingEntity entity, ItemStack stack)
    {
        if (!(entity instanceof ServerPlayer player) || !stack.is(Items.GOLDEN_CARROT))
        {
            return;
        }
        int level = HyperSkillRules.getActiveLevel(player, HyperSkillRules.FARMER);
        if (level == 0)
        {
            return;
        }
        if (player.getRandom().nextInt(100) >= HyperSkillRules.getFarmerCarrotChance(level))
        {
            return;
        }
        // 연금술사 스킬 발동 알림이 이 알림 뒤에 이어지도록 먼저 보낸다.
        SkillActivationNotifier.notifySkillActivated(player, JobsPlus.translatable("hyper.farmer.carrot_activated"));
        MobEffectInstance absorption = new MobEffectInstance(MobEffects.ABSORPTION,
                HyperSkillRules.FARMER_CARROT_ABSORPTION_TICKS, HyperSkillRules.FARMER_CARROT_ABSORPTION_AMPLIFIER);
        FoodAbsorptionStack.add(player, FoodAbsorptionShare.Source.GOLDEN_CARROT, absorption);
    }

    public static boolean craft(AbstractContainerMenu menu, int slotIndex, int button,
                                ContainerInput input, Player player)
    {
        if (!(player instanceof ServerPlayer serverPlayer) || slotIndex != 0
                || !(menu instanceof CraftingMenu || menu instanceof InventoryMenu))
        {
            return false;
        }
        int level = HyperSkillRules.getActiveLevel(serverPlayer, HyperSkillRules.FARMER);
        Slot slot = menu.getSlot(slotIndex);
        if (level == 0 || !(slot instanceof ResultSlot) || !slot.mayPickup(player)
                || !slot.getItem().is(Items.GOLDEN_APPLE))
        {
            return false;
        }
        boolean pickup = input == ContainerInput.PICKUP && (button == 0 || button == 1);
        boolean quickMove = input == ContainerInput.QUICK_MOVE && (button == 0 || button == 1);
        boolean swap = input == ContainerInput.SWAP && (button >= 0 && button < 9 || button == 40);
        boolean drop = input == ContainerInput.THROW && (button == 0 || button == 1);
        if (!pickup && !quickMove && !swap && !drop)
        {
            return false;
        }
        ItemStack carried = menu.getCarried();
        if (pickup && !carried.isEmpty() && (!ItemStack.isSameItemSameComponents(carried, slot.getItem())
                || carried.getCount() + slot.getItem().getCount() > carried.getMaxStackSize()))
        {
            return true;
        }
        if (swap && !player.getInventory().getItem(button).isEmpty()
                || drop && (!carried.isEmpty() || !player.canDropItems()))
        {
            return true;
        }
        int conversions = 0;
        int limit = quickMove || drop && button == 1 ? 64 : 1;
        for (int craft = 0; craft < limit && slot.getItem().is(Items.GOLDEN_APPLE); craft++)
        {
            int count = slot.getItem().getCount();
            // safeTake가 재료 차감·레시피/직업 이벤트를 한 번 완료한 뒤 결과를 돌려준다.
            ItemStack result = slot.safeTake(count, count, player);
            if (result.isEmpty()) break;
            int changed = 0;
            for (int item = 0; item < result.getCount(); item++)
            {
                if (player.getRandom().nextInt(100) < HyperSkillRules.getFarmerChance(level)) changed++;
            }
            conversions += changed;
            ItemStack ordinary = result.copyWithCount(result.getCount() - changed);
            ItemStack enchanted = new ItemStack(Items.ENCHANTED_GOLDEN_APPLE, changed);
            if (pickup)
            {
                placeOnCursor(menu, serverPlayer, ordinary.isEmpty() ? enchanted : ordinary);
                if (!ordinary.isEmpty()) give(serverPlayer, enchanted);
            }
            else if (swap)
            {
                player.getInventory().setItem(button, ordinary.isEmpty() ? enchanted : ordinary);
                if (!ordinary.isEmpty()) give(serverPlayer, enchanted);
            }
            else if (drop)
            {
                if (!ordinary.isEmpty()) player.drop(ordinary, false);
                if (!enchanted.isEmpty()) player.drop(enchanted, false);
            }
            else
            {
                give(serverPlayer, ordinary);
                give(serverPlayer, enchanted);
            }
            give(serverPlayer, new ItemStack(Items.APPLE, changed));
        }
        if (conversions > 0)
        {
            SkillActivationNotifier.notifySkillActivated(serverPlayer,
                    JobsPlus.translatable("hyper.farmer.activated", conversions));
        }
        menu.broadcastChanges();
        player.getInventory().setChanged();
        return true;
    }

    private static void placeOnCursor(AbstractContainerMenu menu, ServerPlayer player, ItemStack stack)
    {
        if (stack.isEmpty()) return;
        ItemStack carried = menu.getCarried();
        if (carried.isEmpty())
        {
            menu.setCarried(stack);
        }
        else if (ItemStack.isSameItemSameComponents(carried, stack)
                && carried.getCount() + stack.getCount() <= carried.getMaxStackSize())
        {
            carried.grow(stack.getCount());
        }
        else
        {
            give(player, stack);
        }
    }

    private static void give(ServerPlayer player, ItemStack stack)
    {
        if (stack.isEmpty()) return;
        player.getInventory().add(stack);
        // add가 일부만 넣고 true를 반환해도 남은 개수는 반드시 보존한다.
        if (!stack.isEmpty()) player.drop(stack, false);
    }
}
