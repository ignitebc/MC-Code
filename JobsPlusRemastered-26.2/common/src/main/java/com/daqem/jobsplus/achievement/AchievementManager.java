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
import com.mcserver.serverutilities.monster.MonsterEquipmentAccess;
import com.mcserver.serverutilities.monster.MonsterLevel;
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
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import org.jetbrains.annotations.Nullable;

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
    /** TACZ 탄환 피해. TACZ에 의존하지 않도록 태그 ID로만 확인한다. */
    private static final TagKey<DamageType> GUN_BULLETS =
            TagKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath("tacz", "bullets"));
    /** 강적 처치 업적(G13)이 세는 최소 위험 단계. 장비 몬스터 LV12 이상, 크리퍼 LV6 이상이다. */
    private static final int STRONG_MONSTER_STAGE = 6;
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
            String oreType = AchievementRules.oreType(state);
            if (!oreType.isEmpty())
            {
                add(player, "ore:" + oreType, 1);
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
            if (AchievementRules.isFishingTreasure(stack))
            {
                add(player, "fish_treasure", 1);
            }
        }
    }

    public static void recordKill(ServerPlayer player, LivingEntity victim)
    {
        recordKill(player, victim, null);
    }

    /** @param source 처치한 피해. 드래곤처럼 기여도로 처치를 정하는 경우에는 null이다. */
    public static void recordKill(ServerPlayer player, LivingEntity victim, @Nullable DamageSource source)
    {
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(victim.getType()).toString();
        add(player, "kill:" + id, 1);
        if (!(victim instanceof Enemy))
        {
            return;
        }
        add(player, "hostile_kills", 1);
        if (source != null && source.is(GUN_BULLETS))
        {
            add(player, "gun_kills", 1);
        }
        // Server Utilities가 정한 머리 위 레벨의 위험 단계. 장비를 추첨하지 않은 몬스터는 레벨이 없다.
        // 장비 몬스터는 레벨 구간, 크리퍼는 레벨 그대로 단계가 되므로 크리퍼도 같은 기준으로 집계된다.
        // 이미 쌓인 처치 수를 이어 쓰도록 기록 이름은 예전 기준(LV5 이상, LV7)의 이름을 그대로 둔다.
        if (victim instanceof MonsterEquipmentAccess monster)
        {
            boolean creeper = victim instanceof Creeper;
            int stage = MonsterLevel.stage(monster.serverutilities$monsterLevel(), creeper);
            if (stage >= STRONG_MONSTER_STAGE)
            {
                add(player, "monster_kills_level5", 1);
            }
            if (stage >= MonsterLevel.MAX_STAGE)
            {
                add(player, "monster_kills_level7", 1);
            }
        }
    }

    /** 펫이 마지막 공격으로 적대몹을 처치하면 주인의 펫 처치로 센다. 펫 처치는 주인의 직접 처치로 보지 않는다. */
    public static void recordPetKill(ServerPlayer owner, LivingEntity victim)
    {
        if (victim instanceof Enemy)
        {
            add(owner, "pet_kills", 1);
        }
    }

    /** 대장장이 작업대에서 네더라이트 단계 장비를 꺼냈을 때 */
    public static void recordForge(ServerPlayer player, ItemStack result)
    {
        String tier = AchievementRules.forgeTier(result);
        if (!tier.isEmpty())
        {
            add(player, "forge:" + tier, 1);
        }
    }

    /** 랜덤 상자를 열어 보상을 받았을 때 */
    public static void recordRandomBox(ServerPlayer player, Identifier boxId)
    {
        add(player, "random_boxes", 1);
        if (boxId.getPath().equals("random_box_iv"))
        {
            add(player, "random_box:iv", 1);
        }
    }

    /**
     * 주식 매도 결과. 원금 1 BTC 미만은 소액 반복 매매로 쉽게 채울 수 있어 제외한다.
     *
     * @param principal 이번에 판 투자 원금
     * @param proceeds  수수료를 뺀 회수 금액
     * @param costBasis 이번에 판 몫의 매수 원가
     */
    public static void recordStockSale(ServerPlayer player, double principal, double proceeds, double costBasis)
    {
        if (principal < 1 || costBasis <= 0 || proceeds <= costBasis)
        {
            return;
        }
        if (!isEligible(player))
        {
            return;
        }
        AchievementProgress progress = AchievementStorage.get(player.level().getServer(), player.getUUID());
        progress.add("stock_profitable_sells", 1);
        // 수익률은 1% 단위로 내림해 최고 기록만 남긴다.
        progress.maximum("stock_best_return", (long) Math.floor((proceeds / costBasis - 1) * 100));
        AchievementStorage.markDirty();
        evaluate(progress);
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
        refreshEquipment(player, progress);
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
        long skillsPurchased = 0;
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
            skillsPurchased += purchased;
            if (jobId.getPath().equals("miner") || jobId.getPath().equals("digger"))
            {
                progress.maximum("hyper_max_level", job.getHyperSkill().level());
                progress.maximum("hyper_level:" + jobId.getPath(), job.getHyperSkill().level());
            }
        }
        // 현재 구매 상태의 합계다. 시즌 중 직업을 바꿔도 줄어든 값으로 업적을 되돌리지는 않는다.
        progress.maximum("skills_purchased", skillsPurchased);
        int masters = 0;
        int level100 = 0;
        int level50 = 0;
        int level20 = 0;
        for (String job : AchievementCatalog.JOBS)
        {
            if (progress.value("job_master:" + job) > 0)
            {
                masters++;
            }
            if (progress.value("job_level:" + job) >= 100)
            {
                level100++;
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
        progress.set("jobs_level100", level100);
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
                progress.maximum("rare_pet_max_level", pet.level());
            }
            if (pet.rarity() == PetRarity.LEGEND)
            {
                progress.set("pet_legend", 1);
                progress.maximum("legend_pet_max_level", pet.level());
            }
        }
        progress.set("pet_types_level20", countPetTypes(progress, 20));
        progress.set("pet_types_level50", countPetTypes(progress, 50));
        long legendTypes100 = AchievementCatalog.LEGEND_PETS.stream()
                .filter(type -> progress.value("pet_type:" + type) >= 100).count();
        progress.set("legend_pets_level100", legendTypes100);
    }

    private static long countPetTypes(AchievementProgress progress, int level)
    {
        return progress.counters().entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("pet_type:") && entry.getValue() >= level).count();
    }

    /** 착용 장비와 강화 기록. 착용은 지금 상태만 보므로 한 번 달성하면 벗어도 업적은 유지된다. */
    private static void refreshEquipment(ServerPlayer player, AchievementProgress progress)
    {
        boolean frostArmor = true;
        for (EquipmentSlot slot : List.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET))
        {
            Identifier id = BuiltInRegistries.ITEM.getKey(player.getItemBySlot(slot).getItem());
            String piece = switch (slot)
            {
                case HEAD -> "frost_helmet";
                case CHEST -> "frost_chestplate";
                case LEGS -> "frost_leggings";
                default -> "frost_boots";
            };
            if (!id.getNamespace().equals("advancednetherite") || !id.getPath().equals(piece))
            {
                frostArmor = false;
            }
        }
        if (frostArmor)
        {
            progress.set("frost_armor_set", 1);
        }
        // 강화 단계는 장비 개체별 최고 성공 단계로 남아 있으므로 실패로 내려가도 다시 세지 않는다.
        long enhanced7 = progress.counters().entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("enhancement:") && entry.getValue() >= 7).count();
        progress.maximum("enhanced7_items", enhanced7);
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
        var structures = player.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        long visitedStructures = 0;
        for (Map.Entry<String, List<String>> target : AchievementCatalog.STRUCTURES.entrySet())
        {
            String key = "structure:" + target.getKey();
            if (progress.value(key) != 0)
            {
                visitedStructures++;
                continue;
            }
            for (String structureId : target.getValue())
            {
                // Illager Invasion이 없는 서버에서는 그 구조물을 건너뛴다.
                var holder = structures.get(Identifier.parse(structureId));
                if (holder.isEmpty())
                {
                    continue;
                }
                Structure structure = holder.get().value();
                if (player.level().structureManager().getStructureAt(player.blockPosition(), structure).isValid())
                {
                    progress.set(key, 1);
                    visitedStructures++;
                    break;
                }
            }
        }
        progress.set("structures_visited", visitedStructures);
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
