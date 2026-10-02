package com.daqem.jobsplus.achievement;

import com.autovw.advancednetherite.common.pet.PetRarity;
import com.autovw.advancednetherite.common.pet.PetRecord;
import com.autovw.advancednetherite.common.pet.PetStorage;
import com.daqem.arc.event.events.FishingCatchEvent;
import com.daqem.itemrestrictions.chunk.ChunkOwnership;
import com.daqem.jobsplus.integration.arc.holder.holders.powerup.PowerupInstance;
import com.daqem.jobsplus.integration.arc.holder.holders.powerup.PowerupManager;
import com.daqem.jobsplus.networking.s2c.ClientboundAchievementPacket;
import com.daqem.jobsplus.player.JobsServerPlayer;
import com.daqem.jobsplus.player.job.Job;
import com.daqem.jobsplus.player.job.powerup.Powerup;
import com.daqem.jobsplus.player.job.powerup.PowerupState;
import com.daqem.jobsplus.player.stock.StockAchievementTrade;
import dev.architectury.event.events.common.LifecycleEvent;
import dev.architectury.event.events.common.PlayerEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.networking.NetworkManager;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

/** 모든 판정과 누적은 서버에서 수행한다. 클라이언트는 표시와 수령 요청만 담당한다. */
public final class AchievementManager
{
    private static final Identifier ADVENTURING_TIME = Identifier.withDefaultNamespace("adventure/adventuring_time");
    private static final Map<UUID, Integer> LAST_REQUESTS = new HashMap<>();
    private static final Map<UUID, Integer> LAST_CLAIMS = new HashMap<>();
    private static final Map<UUID, Integer> VIEWERS = new HashMap<>();
    private static final Map<UUID, PlayerLocation> LAST_LOCATIONS = new HashMap<>();
    private static Set<String> overworldBiomes;

    private AchievementManager()
    {
    }

    public static void registerEvents()
    {
        LifecycleEvent.SERVER_STARTING.register(server -> {
            AchievementCatalog.all();
            AchievementStorage.load(server);
        });
        LifecycleEvent.SERVER_STOPPING.register(server -> AchievementStorage.save());
        LifecycleEvent.SERVER_STOPPED.register(server -> {
            AchievementStorage.close();
            LAST_REQUESTS.clear();
            LAST_CLAIMS.clear();
            VIEWERS.clear();
            LAST_LOCATIONS.clear();
            overworldBiomes = null;
        });
        PlayerEvent.PLAYER_JOIN.register(player -> refresh(player));
        PlayerEvent.CHANGE_DIMENSION.register((player, previous, current) -> {
            if (current.equals(Level.END))
            {
                add(player, "dimension:end", 1);
            }
            refresh(player);
        });
        PlayerEvent.PLAYER_QUIT.register(player -> {
            LAST_REQUESTS.remove(player.getUUID());
            LAST_CLAIMS.remove(player.getUUID());
            VIEWERS.remove(player.getUUID());
            LAST_LOCATIONS.remove(player.getUUID());
            AchievementStorage.save();
        });
        FishingCatchEvent.CAUGHT.register(AchievementManager::recordFishing);
        TickEvent.SERVER_POST.register(AchievementManager::tick);
    }

    private static void tick(MinecraftServer server)
    {
        // 방문은 매초 갱신만으로 놓칠 수 있다. 서버 틱마다 위치가 바뀐 플레이어를 확인한다.
        for (ServerPlayer player : server.getPlayerList().getPlayers())
        {
            if (!isEligible(player) || !player.isAlive())
            {
                continue;
            }
            PlayerLocation current = new PlayerLocation(player.level().dimension(), player.blockPosition());
            if (!current.equals(LAST_LOCATIONS.get(player.getUUID())))
            {
                LAST_LOCATIONS.put(player.getUUID(), current);
                AchievementProgress progress = AchievementStorage.get(server, player.getUUID());
                refreshExploration(player, progress);
                AchievementStorage.markDirty();
                evaluate(progress);
            }
        }
        if (server.getTickCount() % 20 == 0)
        {
            for (ServerPlayer player : server.getPlayerList().getPlayers())
            {
                refresh(player);
                Integer lastView = VIEWERS.get(player.getUUID());
                if (lastView != null && server.getTickCount() - lastView < 60)
                {
                    sendSnapshot(player);
                }
            }
        }
        if (server.getTickCount() % 1200 == 0)
        {
            AchievementStorage.save();
        }
    }

    public static boolean isEligible(ServerPlayer player)
    {
        if (player.isCreative() || player.isSpectator())
        {
            return false;
        }
        return true;
    }

    public static void add(ServerPlayer player, String key, long amount)
    {
        if (!isEligible(player))
        {
            return;
        }
        add(player.level().getServer(), player.getUUID(), key, amount);
    }

    public static void add(MinecraftServer server, UUID playerId, String key, long amount)
    {
        if (server == null || playerId == null || amount <= 0)
        {
            return;
        }
        AchievementProgress progress = AchievementStorage.get(server, playerId);
        if (progress.add(key, amount))
        {
            AchievementStorage.markDirty();
        }
        evaluate(progress);
    }

    public static boolean recordOnce(MinecraftServer server, UUID playerId, String eventId, String metric)
    {
        AchievementProgress progress = AchievementStorage.get(server, playerId);
        String key = "event:" + eventId;
        if (progress.value(key) != 0)
        {
            return false;
        }
        progress.set(key, 1);
        progress.add(metric, 1);
        AchievementStorage.markDirty();
        evaluate(progress);
        return true;
    }

    public static void recordBlock(ServerPlayer player, BlockState state, boolean natural)
    {
        if (natural && AchievementRules.isOre(state))
        {
            add(player, "ores", 1);
            if (AchievementRules.blockId(state).equals("minecraft:ancient_debris"))
            {
                add(player, "ancient_debris", 1);
            }
        }
        if (natural && AchievementRules.isExcavation(state))
        {
            add(player, "excavation", 1);
        }
        recordHarvest(player, state);
    }

    public static void recordHarvest(ServerPlayer player, BlockState state)
    {
        if (!AchievementRules.isMatureCrop(state))
        {
            return;
        }
        add(player, "harvests", 1);
        add(player, "harvest:" + AchievementRules.cropType(state), 1);
    }

    private static void recordFishing(ServerPlayer player, Collection<ItemStack> loot)
    {
        if (loot.isEmpty())
        {
            return;
        }
        add(player, "fishing", 1);
        for (ItemStack stack : loot)
        {
            Identifier id = BuiltInRegistries.ITEM.getKey(stack.getItem());
            if (id.getNamespace().equals("minecraft") && AchievementCatalog.FISH.contains(id.getPath()))
            {
                add(player, "fish:" + id.getPath(), stack.getCount());
            }
        }
    }

    public static void recordKill(ServerPlayer player, LivingEntity victim)
    {
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType()).toString();
        add(player, "kill:" + id, 1);
        if (victim instanceof Enemy)
        {
            add(player, "hostile_kills", 1);
        }
    }

    public static UUID equipmentId(ItemStack stack)
    {
        CompoundTag data = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        String savedId = data.getStringOr("JobsPlusEquipmentId", "");
        try
        {
            if (!savedId.isEmpty())
            {
                return UUID.fromString(savedId);
            }
        }
        catch (IllegalArgumentException ignored)
        {
            // 잘못된 식별자는 새 서버 UUID로 대체한다.
        }
        UUID id = UUID.randomUUID();
        CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putString("JobsPlusEquipmentId", id.toString()));
        return id;
    }

    public static void recordEquipment(ServerPlayer player, UUID itemId, String kind, String type,
                                       int oldExperience, int newExperience)
    {
        if (!isEligible(player) || newExperience <= oldExperience || type.isEmpty())
        {
            return;
        }
        AchievementProgress progress = AchievementStorage.get(player.level().getServer(), player.getUUID());
        String key = kind + ":" + itemId;
        AchievementProgress.GearProgress old = progress.equipment().get(key);
        long contribution = 0;
        int reached = 0;
        if (old != null)
        {
            contribution = old.contributedExperience();
            reached = old.reachedLevel();
        }
        long gained = (long) newExperience - oldExperience;
        if (gained > Long.MAX_VALUE - contribution)
        {
            contribution = Long.MAX_VALUE;
        }
        else
        {
            contribution += gained;
        }
        boolean milestoneChanged = false;
        // 고레벨 장비에 EXP를 한 번 넣은 사실만으로 이미 지나간 목표를 인정하지 않는다.
        for (int threshold : AchievementRules.crossedEquipmentMilestones(oldExperience, newExperience))
        {
            reached = Math.max(reached, threshold);
            progress.set("gear_milestone:" + key + ":" + threshold, 1);
            progress.maximum(kind + "_level" + threshold, threshold);
            milestoneChanged = true;
        }
        progress.equipment().put(key, new AchievementProgress.GearProgress(kind, type, contribution, reached));
        if (reached > 0)
        {
            progress.maximum(kind + "_max_level", reached);
        }
        if (milestoneChanged)
        {
            refreshEquipmentCounts(progress);
            evaluate(progress);
        }
        AchievementStorage.markDirty();
    }

    private static void refreshEquipmentCounts(AchievementProgress progress)
    {
        Set<String> guns50 = new HashSet<>();
        Set<String> guns100 = new HashSet<>();
        Set<String> tools100 = new HashSet<>();
        for (Map.Entry<String, AchievementProgress.GearProgress> entry : progress.equipment().entrySet())
        {
            AchievementProgress.GearProgress equipment = entry.getValue();
            String prefix = "gear_milestone:" + entry.getKey() + ":";
            if (equipment.kind().equals("gun"))
            {
                if (progress.value(prefix + "50") > 0)
                {
                    guns50.add(equipment.type());
                }
                if (progress.value(prefix + "100") > 0)
                {
                    guns100.add(equipment.type());
                }
            }
            if (equipment.kind().equals("tool") && progress.value(prefix + "100") > 0)
            {
                tools100.add(equipment.type());
            }
        }
        progress.set("gun_models_level50", guns50.size());
        progress.set("gun_models_level100", guns100.size());
        progress.set("tool_types_level100", tools100.size());
    }

    public static void recordEnhancement(ServerPlayer player, ItemStack equipment, int level)
    {
        if (!isEligible(player))
        {
            return;
        }
        AchievementProgress progress = AchievementStorage.get(player.level().getServer(), player.getUUID());
        UUID id = equipmentId(equipment);
        progress.maximum("enhancement:" + id, level);
        progress.maximum("enhancement_max", level);
        AchievementStorage.markDirty();
        evaluate(progress);
    }

    public static void refresh(ServerPlayer player)
    {
        if (!isEligible(player))
        {
            return;
        }
        AchievementProgress progress = AchievementStorage.get(player.level().getServer(), player.getUUID());
        Map<String, Long> previous = Map.copyOf(progress.counters());
        refreshJobs(player, progress);
        refreshPets(player, progress);
        refreshExploration(player, progress);
        refreshEconomy(player, progress);
        if (!previous.equals(progress.counters()))
        {
            AchievementStorage.markDirty();
        }
        evaluate(progress);
    }

    private static void refreshJobs(ServerPlayer player, AchievementProgress progress)
    {
        if (!(player instanceof JobsServerPlayer jobsPlayer))
        {
            return;
        }
        Collection<PowerupInstance> definitions = PowerupManager.getInstance().getAllPowerups().values();
        List<Job> jobs = Stream.concat(jobsPlayer.jobsplus$getJobs().stream(),
                jobsPlayer.jobsplus$getInactiveJobs().stream()).toList();
        for (Job job : jobs)
        {
            Identifier jobId = job.getJobInstance().getLocation();
            if (!jobId.getNamespace().equals("jobsplus") || !AchievementCatalog.JOBS.contains(jobId.getPath()))
            {
                continue;
            }
            progress.maximum("job_level:" + jobId.getPath(), job.getLevel());
            progress.maximum("job_max_level", job.getLevel());
            Set<Identifier> owned = new HashSet<>();
            for (Powerup powerup : job.getPowerupManager().getAllPowerups())
            {
                if (powerup.getState() == PowerupState.ACTIVE || powerup.getState() == PowerupState.INACTIVE)
                {
                    owned.add(powerup.getPowerupLocation());
                }
            }
            int required = 0;
            int purchased = 0;
            for (PowerupInstance definition : definitions)
            {
                if (jobId.equals(definition.getJobLocation()))
                {
                    required++;
                    if (owned.contains(definition.getLocation()))
                    {
                        purchased++;
                    }
                }
            }
            if (required > 0 && purchased == required)
            {
                progress.set("job_master:" + jobId.getPath(), 1);
            }
            if (jobId.getPath().equals("miner") || jobId.getPath().equals("digger"))
            {
                progress.maximum("hyper_max_level", job.getHyperSkill().level());
            }
        }
        int masters = 0;
        int level50 = 0;
        int level20 = 0;
        for (String job : AchievementCatalog.JOBS)
        {
            if (progress.value("job_master:" + job) > 0)
            {
                masters++;
            }
            if (progress.value("job_level:" + job) >= 50)
            {
                level50++;
            }
            if (progress.value("job_level:" + job) >= 20)
            {
                level20++;
            }
        }
        progress.set("job_master_count", masters);
        progress.set("jobs_level50", level50);
        progress.set("jobs_level20", level20);
    }

    private static void refreshPets(ServerPlayer player, AchievementProgress progress)
    {
        for (PetRecord pet : PetStorage.getPets(player.getUUID()))
        {
            progress.maximum("pet_max_level", pet.level());
            progress.maximum("pet_type:" + pet.petTypeId(), pet.level());
            if (pet.rarity() == PetRarity.RARE)
            {
                progress.set("pet_rare", 1);
            }
            if (pet.rarity() == PetRarity.LEGEND)
            {
                progress.set("pet_legend", 1);
                progress.maximum("legend_pet_max_level", pet.level());
            }
        }
        long types = progress.counters().entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("pet_type:") && entry.getValue() >= 20).count();
        progress.set("pet_types_level20", types);
    }

    private static void refreshEconomy(ServerPlayer player, AchievementProgress progress)
    {
        long chunks = 0;
        for (Map<Long, ChunkOwnership.Owner> dimension : ChunkOwnership.getAll().values())
        {
            for (ChunkOwnership.Owner owner : dimension.values())
            {
                if (owner.uuid().equals(player.getUUID()))
                {
                    chunks++;
                }
            }
        }
        progress.maximum("claimed_chunks", chunks);
        if (player instanceof JobsServerPlayer jobsPlayer)
        {
            for (Map.Entry<String, StockAchievementTrade> entry : jobsPlayer.jobsplus$getStockAccount().achievementTrades().entrySet())
            {
                String prefix = AchievementStorage.season() + "/";
                if (entry.getKey().startsWith(prefix) && entry.getValue().completed())
                {
                    progress.set("stock_completed:" + entry.getKey().substring(prefix.length()), 1);
                }
            }
            long completed = progress.counters().keySet().stream().filter(key -> key.startsWith("stock_completed:")).count();
            progress.set("stock_round_trips", completed);
        }
    }

    private static void refreshExploration(ServerPlayer player, AchievementProgress progress)
    {
        if (!player.isAlive())
        {
            return;
        }
        var biomeKey = player.level().getBiome(player.blockPosition()).unwrapKey();
        Identifier biome = Identifier.withDefaultNamespace("unknown");
        if (biomeKey.isPresent())
        {
            biome = biomeKey.get().identifier();
        }
        if (biomeKey.isPresent() && player.level().dimension().equals(Level.OVERWORLD)
                && getOverworldBiomes(player.level().getServer()).contains(biome.toString()))
        {
            progress.set("overworld_biome:" + biome, 1);
            long visited = progress.counters().keySet().stream().filter(key -> key.startsWith("overworld_biome:")).count();
            progress.set("overworld_biomes", visited);
        }
        if (biomeKey.isPresent() && player.level().dimension().equals(Level.NETHER) && biome.getNamespace().equals("minecraft")
                && AchievementCatalog.NETHER_BIOMES.contains(biome.getPath()))
        {
            progress.set("nether_biome:" + biome.getPath(), 1);
            long visited = progress.counters().keySet().stream().filter(key -> key.startsWith("nether_biome:")).count();
            progress.set("nether_biomes", visited);
        }
        if (player.level().dimension().equals(Level.END))
        {
            progress.set("dimension:end", 1);
        }
        for (String structureId : List.of("stronghold", "fortress", "bastion_remnant", "end_city"))
        {
            if (progress.value("structure:" + structureId) != 0)
            {
                continue;
            }
            var holder = player.registryAccess().lookupOrThrow(Registries.STRUCTURE)
                    .get(Identifier.withDefaultNamespace(structureId));
            if (holder.isPresent())
            {
                Structure structure = holder.get().value();
                if (player.level().structureManager().getStructureAt(player.blockPosition(), structure).isValid())
                {
                    progress.set("structure:" + structureId, 1);
                }
            }
        }
        AdvancementHolder advancement = player.level().getServer().getAdvancements().get(ADVENTURING_TIME);
        if (advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone())
        {
            progress.set("adventuring_time", 1);
        }
    }

    private static void evaluate(AchievementProgress progress)
    {
        // 정의가 선행 순서로 검증되어 있으므로 한 번 순회해 연속 단계까지 확정할 수 있다.
        for (AchievementDefinition definition : AchievementCatalog.all())
        {
            if (!progress.completed().contains(definition.id()) && definition.isUnlocked(progress)
                    && definition.hasMetObjectives(progress))
            {
                progress.completed().add(definition.id());
                AchievementStorage.markDirty();
            }
        }
    }

    public static boolean requestAllowed(ServerPlayer player, boolean claiming)
    {
        Map<UUID, Integer> requests = LAST_REQUESTS;
        if (claiming)
        {
            requests = LAST_CLAIMS;
        }
        int current = player.level().getServer().getTickCount();
        Integer previous = requests.get(player.getUUID());
        if (previous != null && current >= previous && current - previous < 5)
        {
            return false;
        }
        requests.put(player.getUUID(), current);
        return true;
    }

    public static void view(ServerPlayer player)
    {
        VIEWERS.put(player.getUUID(), player.level().getServer().getTickCount());
        refresh(player);
        sendSnapshot(player);
    }

    public static void sendSnapshot(ServerPlayer player)
    {
        AchievementProgress progress = AchievementStorage.get(player.level().getServer(), player.getUUID());
        Map<String, Long> values = new HashMap<>();
        for (AchievementDefinition definition : AchievementCatalog.all())
        {
            for (AchievementDefinition.Objective objective : definition.objectives())
            {
                values.put(objective.key(), progress.value(objective.key()));
            }
        }
        Set<String> claimed = new HashSet<>();
        if (player instanceof AchievementPlayer achievementPlayer)
        {
            for (AchievementDefinition definition : AchievementCatalog.all())
            {
                if (achievementPlayer.jobsplus$getClaimedAchievements().contains(claimKey(definition.id())))
                {
                    claimed.add(definition.id());
                }
            }
        }
        List<String> adventureBiomes = List.of();
        AdvancementHolder adventure = player.level().getServer().getAdvancements().get(ADVENTURING_TIME);
        if (adventure != null)
        {
            adventureBiomes = adventure.value().criteria().keySet().stream().sorted().toList();
        }
        NetworkManager.sendToPlayer(player, new ClientboundAchievementPacket(
                AchievementStorage.season(), values, Set.copyOf(progress.completed()), claimed, adventureBiomes,
                getOverworldBiomes(player.level().getServer()).stream().sorted().toList()));
    }

    private static Set<String> getOverworldBiomes(MinecraftServer server)
    {
        if (overworldBiomes == null)
        {
            Set<String> possible = new HashSet<>();
            for (var holder : server.overworld().getChunkSource().getGenerator().getBiomeSource().possibleBiomes())
            {
                holder.unwrapKey().ifPresent(key -> possible.add(key.identifier().toString()));
            }
            overworldBiomes = Set.copyOf(possible);
        }
        return overworldBiomes;
    }

    private record PlayerLocation(ResourceKey<Level> dimension, BlockPos position)
    {
    }

    public static String claimKey(String id)
    {
        return AchievementStorage.season() + "/" + id;
    }
}
