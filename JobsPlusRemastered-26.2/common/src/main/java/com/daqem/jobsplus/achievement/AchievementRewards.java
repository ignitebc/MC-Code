package com.daqem.jobsplus.achievement;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.networking.s2c.ClientboundAlertPacket;
import dev.architectury.networking.NetworkManager;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.TagValueOutput;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

/** 지급 전에 전체 공간을 확인하고 인벤토리·수령 기록을 하나의 플레이어 NBT로 저장한다. */
public final class AchievementRewards
{
    private AchievementRewards()
    {
    }

    public static void claim(ServerPlayer player, String season, String id)
    {
        if (!AchievementManager.isEligible(player) || !AchievementStorage.season().equals(season)
                || !(player instanceof AchievementPlayer achievementPlayer))
        {
            return;
        }
        AchievementDefinition definition = AchievementCatalog.get(id);
        if (definition == null)
        {
            return;
        }
        AchievementManager.refresh(player);
        AchievementProgress progress = AchievementStorage.get(player.level().getServer(), player.getUUID());
        String claimKey = AchievementManager.claimKey(id);
        if (achievementPlayer.jobsplus$getClaimedAchievements().contains(claimKey))
        {
            return;
        }
        if (!progress.completed().contains(id) || !definition.isUnlocked(progress))
        {
            return;
        }

        Inventory inventory = player.getInventory();
        List<ItemStack> original = new ArrayList<>();
        List<ItemStack> planned = new ArrayList<>();
        for (int slot = 0; slot < 36; slot++)
        {
            ItemStack current = inventory.getItem(slot).copy();
            original.add(current);
            planned.add(current.copy());
        }
        int remaining = definition.diamonds();
        ItemStack reward = new ItemStack(Items.DIAMOND);
        // 기존 스택부터 채우고, 남은 수량은 빈 슬롯에 넣는다. 장비·오프핸드·커서는 사용하지 않는다.
        for (ItemStack stack : planned)
        {
            if (!stack.isEmpty() && ItemStack.isSameItemSameComponents(stack, reward))
            {
                int available = Math.max(0, Math.min(inventory.getMaxStackSize(), stack.getMaxStackSize()) - stack.getCount());
                int amount = Math.min(remaining, available);
                stack.grow(amount);
                remaining -= amount;
            }
        }
        for (int slot = 0; slot < planned.size() && remaining > 0; slot++)
        {
            if (planned.get(slot).isEmpty())
            {
                int amount = Math.min(remaining, Math.min(inventory.getMaxStackSize(), reward.getMaxStackSize()));
                planned.set(slot, new ItemStack(Items.DIAMOND, amount));
                remaining -= amount;
            }
        }
        if (remaining > 0)
        {
            showResult(player, "인벤토리 공간이 부족합니다. 업적 보상은 수령 대기 상태로 유지됩니다.");
            AchievementManager.sendSnapshot(player);
            return;
        }
        if (!AchievementStorage.save())
        {
            showResult(player, "업적 기록을 저장하지 못했습니다. 보상은 수령 대기 상태로 유지됩니다.");
            return;
        }

        for (int slot = 0; slot < planned.size(); slot++)
        {
            inventory.setItem(slot, planned.get(slot));
        }
        achievementPlayer.jobsplus$getClaimedAchievements().add(claimKey);
        if (!savePlayer(player))
        {
            for (int slot = 0; slot < original.size(); slot++)
            {
                inventory.setItem(slot, original.get(slot));
            }
            achievementPlayer.jobsplus$getClaimedAchievements().remove(claimKey);
            showResult(player, "보상 저장에 실패했습니다. 인벤토리를 복원했으며 다시 수령할 수 있습니다.");
        }
        else
        {
            showResult(player, definition.name() + " 완료 보상: 다이아몬드 " + definition.diamonds() + "개");
        }
        inventory.setChanged();
        player.inventoryMenu.broadcastChanges();
        player.containerMenu.broadcastChanges();
        AchievementManager.sendSnapshot(player);
    }

    private static void showResult(ServerPlayer player, String message)
    {
        NetworkManager.sendToPlayer(player, new ClientboundAlertPacket(Component.literal(message)));
    }

    private static boolean savePlayer(ServerPlayer player)
    {
        // 바닐라 PlayerDataStorage.save는 실패를 내부에서 삼킨다. 수령은 저장 성공을 확인해야 한다.
        Path directory = player.level().getServer().getWorldPath(LevelResource.PLAYER_DATA_DIR);
        Path temporary = null;
        try
        {
            Files.createDirectories(directory);
            TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, player.registryAccess());
            player.saveWithoutId(output);
            temporary = Files.createTempFile(directory, player.getStringUUID() + "-achievement-", ".dat");
            NbtIo.writeCompressed(output.buildResult(), temporary);
            Path destination = directory.resolve(player.getStringUUID() + ".dat");
            if (Files.exists(destination))
            {
                Files.copy(destination, directory.resolve(player.getStringUUID() + ".dat_old"), StandardCopyOption.REPLACE_EXISTING);
            }
            AchievementStorage.replace(temporary, destination);
            return true;
        }
        catch (Exception exception)
        {
            JobsPlus.LOGGER.error("Failed to persist achievement reward for {}", player.getUUID(), exception);
            if (temporary != null)
            {
                try
                {
                    Files.deleteIfExists(temporary);
                }
                catch (Exception cleanupFailure)
                {
                    JobsPlus.LOGGER.warn("Could not remove temporary achievement player save", cleanupFailure);
                }
            }
            return false;
        }
    }
}
