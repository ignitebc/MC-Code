package com.autovw.advancednetherite.common.pet;

import com.autovw.advancednetherite.common.entity.DialgaPetEntity;
import com.autovw.advancednetherite.core.ModEntityTypes;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 펫 소환과 회수를 담당한다.
 * <p>
 * 펫 엔티티는 주인이 접속 중일 때만 존재하는 일회성 개체이고, 소유의 원본은
 * {@link PetStorage}의 기록이다. 접속하면 기록에서 소환하고, 접속을 종료하면 회수한다.
 */
public final class PetManager
{
    private static final Logger LOGGER = LogUtils.getLogger();

    /** 기록 ID → 살아 있는 펫 엔티티. 기록 하나에 펫 한 마리만 존재하도록 보장한다. */
    private static final Map<UUID, DialgaPetEntity> LIVE_PETS = new ConcurrentHashMap<>();

    /** 토글 사이에 두는 최소 간격(0.5초). 정상 조작으로는 걸리지 않는 값이다. */
    private static final int TOGGLE_COOLDOWN_TICKS = 10;

    /** 플레이어 UUID → 마지막 토글을 처리한 서버 틱 */
    private static final Map<UUID, Integer> LAST_TOGGLE_TICKS = new ConcurrentHashMap<>();

    /**
     * 기록 ID → 회수될 때의 체력. 껐다 켜는 것만으로 체력이 가득 차지 않게 한다.
     * 파일에는 남기지 않으므로 서버를 다시 켜면 가득 찬 체력으로 시작한다.
     */
    private static final Map<UUID, Float> LAST_HEALTH = new ConcurrentHashMap<>();

    /** 경험치가 바뀌었지만 아직 클라이언트에 알리지 않은 플레이어. 한 대마다 패킷을 보내지 않으려고 모아 둔다. */
    private static final Set<UUID> EXP_DIRTY_PLAYERS = ConcurrentHashMap.newKeySet();

    /** 부활 시각 확인 주기(1초) */
    private static final int REVIVE_CHECK_INTERVAL_TICKS = 20;
    /** 모아 둔 경험치 변경을 클라이언트에 보내는 주기(5초) */
    private static final int EXP_SYNC_INTERVAL_TICKS = 100;

    /** 펫 목록이 바뀔 때 클라이언트에 동기화 패킷을 보내는 훅. 플랫폼 초기화 코드가 등록한다. */
    private static Consumer<ServerPlayer> syncHandler;

    private static Map<String, EntityType<DialgaPetEntity>> petTypesById;

    private PetManager()
    {
    }

    public static void setSyncHandler(Consumer<ServerPlayer> handler)
    {
        syncHandler = handler;
    }

    public static void syncPets(ServerPlayer player)
    {
        // 펫이 늘거나 이름이 바뀌면 번호가 달라질 수 있으므로 살아 있는 펫의 이름표도 같이 맞춘다.
        refreshNameTags(player);
        if (syncHandler != null)
        {
            syncHandler.accept(player);
        }
    }

    /** 살아 있는 펫의 머리 위 이름표를 현재 기록 기준으로 다시 붙인다. */
    private static void refreshNameTags(ServerPlayer player)
    {
        List<PetRecord> records = PetStorage.getPets(player.getUUID());
        for (PetRecord record : records)
        {
            DialgaPetEntity livePet = LIVE_PETS.get(record.id());
            if (livePet != null && !livePet.isRemoved())
            {
                applyNameTag(livePet, records, record);
            }
        }
    }

    private static void applyNameTag(DialgaPetEntity pet, List<PetRecord> records, PetRecord record)
    {
        Component name = PetNames.withLevel(PetNames.displayName(records, record), record.rarity(), record.level());
        pet.setCustomName(name);
        pet.setCustomNameVisible(true);
    }

    /**
     * 펫 이름을 바꾼다. 빈 이름은 붙인 이름을 지우고 종류 이름으로 되돌린다.
     * 다듬은 뒤에도 이전과 같은 이름이면 아무것도 하지 않는다.
     */
    public static void renamePet(ServerPlayer player, UUID recordId, String requestedName)
    {
        PetRecord record = PetStorage.findPet(player.getUUID(), recordId);
        if (record == null)
        {
            return;
        }

        if (passedToggleCooldown(player))
        {
            String name = PetNames.sanitize(requestedName);
            if (!name.equals(record.name()))
            {
                PetStorage.setName(player.getUUID(), recordId, name);
            }
        }
        // 거부되었거나 이름이 그대로여도 클라이언트가 먼저 바꿔 둔 표시를 실제 상태로 되돌린다.
        syncPets(player);
    }

    /** 펫 상자 사용 시 호출된다. 기록을 만들고 즉시 소환한다. */
    public static boolean createPet(ServerPlayer player, EntityType<DialgaPetEntity> petType)
    {
        if (!PetStorage.isAvailable())
        {
            return false;
        }

        String petTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(petType).toString();
        PetRecord record = new PetRecord(UUID.randomUUID(), petTypeId);
        if (!PetStorage.addPet(player.getUUID(), record))
        {
            return false;
        }

        summonPet(player, record);
        syncPets(player);
        return true;
    }

    /** 접속한 플레이어의 ON 상태 펫을 전부 소환한다. */
    public static void handlePlayerJoin(ServerPlayer player)
    {
        for (PetRecord record : PetStorage.getPets(player.getUUID()))
        {
            if (record.enabled())
            {
                summonPet(player, record);
            }
        }
        syncPets(player);
    }

    /**
     * 주인이 죽으면 데리고 있던 펫을 모두 회수하고 기록을 OFF로 돌린다.
     *
     * <p>부활해도 저절로 따라 나오지 않는다. 직업 화면(J키)의 펫관리 탭에서 다시 켜야 소환된다.
     * 접속 종료와 달리 기록까지 끄는 이유는, 죽은 자리에 두고 온 펫이 부활 지점으로
     * 순간이동해 따라오는 것을 의도한 동작으로 보지 않기 때문이다.
     */
    public static void handlePlayerDeath(ServerPlayer player)
    {
        boolean changed = false;
        for (PetRecord record : PetStorage.getPets(player.getUUID()))
        {
            DialgaPetEntity livePet = LIVE_PETS.remove(record.id());
            if (livePet != null)
            {
                livePet.discard();
            }
            if (record.enabled() && PetStorage.setEnabled(player.getUUID(), record.id(), false) != null)
            {
                changed = true;
            }
        }

        if (changed)
        {
            syncPets(player);
        }
    }

    /**
     * 부활한 플레이어에게 펫 목록을 다시 보낸다.
     *
     * <p>부활하면 플레이어 개체가 새로 만들어지므로, 죽을 때 보낸 목록이 사라졌을 수 있다.
     * 소환은 하지 않는다.
     */
    public static void handlePlayerRespawn(ServerPlayer player)
    {
        syncPets(player);
    }

    /** 접속을 종료한 플레이어의 펫을 전부 회수한다. */
    public static void handlePlayerQuit(ServerPlayer player)
    {
        for (PetRecord record : PetStorage.getPets(player.getUUID()))
        {
            DialgaPetEntity livePet = LIVE_PETS.remove(record.id());
            if (livePet != null)
            {
                livePet.discard();
            }
        }
        LAST_TOGGLE_TICKS.remove(player.getUUID());
        EXP_DIRTY_PLAYERS.remove(player.getUUID());

        // 아직 파일에 반영되지 않은 토글 상태가 남아 있으면 이 시점에 기록해 둔다.
        PetStorage.saveIfDirty();
    }

    /**
     * 토글 패킷을 연타해 서버에 부하를 주는 것을 막는다.
     * 간격 안에 들어온 요청은 조용히 무시한다.
     */
    private static boolean passedToggleCooldown(ServerPlayer player)
    {
        MinecraftServer server = player.level().getServer();
        if (server == null)
        {
            return true;
        }

        int currentTick = server.getTickCount();
        Integer lastTick = LAST_TOGGLE_TICKS.get(player.getUUID());
        if (lastTick != null && currentTick - lastTick < TOGGLE_COOLDOWN_TICKS)
        {
            return false;
        }

        LAST_TOGGLE_TICKS.put(player.getUUID(), currentTick);
        return true;
    }

    /** ON/OFF 토글. OFF면 펫을 회수하고, ON이면 곁에 소환한다. */
    public static void togglePet(ServerPlayer player, UUID recordId)
    {
        if (!passedToggleCooldown(player))
        {
            return;
        }

        PetRecord record = PetStorage.findPet(player.getUUID(), recordId);
        if (record == null)
        {
            return;
        }

        long now = System.currentTimeMillis();
        if (!record.enabled() && record.isReviving(now))
        {
            long secondsLeft = (record.reviveAtMillis() - now + 999L) / 1000L;
            player.sendSystemMessage(Component.empty().append(petLabel(player, record))
                    .append("은(는) 부활까지 " + secondsLeft + "초 남았습니다."));
            // 클라이언트가 먼저 뒤집어 둔 단추를 실제 상태로 되돌린다.
            syncPets(player);
            return;
        }

        PetRecord updated = PetStorage.setEnabled(player.getUUID(), recordId, !record.enabled());
        if (updated == null)
        {
            return;
        }

        if (updated.enabled())
        {
            summonPet(player, updated);
        }
        else
        {
            DialgaPetEntity livePet = LIVE_PETS.remove(recordId);
            if (livePet != null)
            {
                livePet.discard();
            }
        }
        syncPets(player);
    }

    /**
     * 펫이 매 틱 자신의 존재 자격을 확인한다. false면 펫은 스스로 소멸해야 한다.
     * 기록이 없거나 삭제되었거나 OFF 상태이거나, 같은 기록의 다른 펫이 이미 살아 있으면 자격이 없다.
     */
    public static boolean validatePet(ServerPlayer owner, DialgaPetEntity pet)
    {
        UUID recordId = pet.getRecordId();
        if (recordId == null)
        {
            return false;
        }

        PetRecord record = PetStorage.findPet(owner.getUUID(), recordId);
        if (record == null || !record.enabled())
        {
            LIVE_PETS.remove(recordId, pet);
            return false;
        }

        DialgaPetEntity livePet = LIVE_PETS.get(recordId);
        if (livePet == null || livePet.isRemoved())
        {
            LIVE_PETS.put(recordId, pet);
            return true;
        }
        return livePet == pet;
    }

    /**
     * 사라진 펫을 살아 있는 목록에서 지운다. 펫이 어떤 경로로 제거되든 호출된다.
     * <p>
     * 이미 다른 개체가 같은 기록으로 등록된 뒤라면 그 개체는 건드리지 않는다.
     * 차원 이동처럼 옛 개체가 사라진 직후 새 개체가 등록되는 경우가 있기 때문이다.
     */
    public static void releasePet(DialgaPetEntity pet)
    {
        UUID recordId = pet.getRecordId();
        if (recordId != null)
        {
            // 죽어서 사라진 펫은 부활할 때 체력이 가득 차므로 기억하지 않는다.
            if (pet.isAlive())
            {
                LAST_HEALTH.put(recordId, pet.getHealth());
            }
            LIVE_PETS.remove(recordId, pet);
        }
    }

    /**
     * 펫이 몹을 한 대 때릴 때마다 호출된다. 경험치 1을 주고, 가득 차면 레벨을 올린다.
     */
    public static void handlePetHit(DialgaPetEntity pet)
    {
        UUID recordId = pet.getRecordId();
        if (recordId == null || !(pet.getOwner() instanceof ServerPlayer owner))
        {
            return;
        }

        PetRecord before = PetStorage.findPet(owner.getUUID(), recordId);
        if (before == null || before.level() >= PetStats.MAX_LEVEL)
        {
            return;
        }

        int level = before.level();
        int exp = before.exp() + 1;
        while (level < PetStats.MAX_LEVEL && exp >= PetStats.expToLevelUp(level))
        {
            exp -= PetStats.expToLevelUp(level);
            level++;
        }
        if (level >= PetStats.MAX_LEVEL)
        {
            exp = 0;
        }

        int newLevel = level;
        int newExp = exp;
        PetRecord after = PetStorage.update(owner.getUUID(), recordId, record -> record.withProgress(newLevel, newExp));
        if (after == null)
        {
            return;
        }

        if (after.level() == before.level())
        {
            EXP_DIRTY_PLAYERS.add(owner.getUUID());
            return;
        }

        // 늘어난 최대 체력만큼은 바로 채워 준다.
        float gainedHealth = (float) (PetStats.maxHealth(after.rarity(), after.level())
                - PetStats.maxHealth(before.rarity(), before.level()));
        applyStats(pet, after);
        pet.heal(gainedHealth);
        owner.sendSystemMessage(Component.empty().append(petLabel(owner, after)).append(" 달성!"));
        syncPets(owner);
    }

    /**
     * 펫이 죽으면 기록을 끄고 부활 시각을 남긴다. 부활 시각이 지나기 전에는 다시 켤 수 없다.
     * 부활해도 꺼진 상태로 남으므로 주인이 펫관리에서 직접 켜야 한다.
     */
    public static void handlePetDeath(DialgaPetEntity pet)
    {
        UUID recordId = pet.getRecordId();
        if (recordId == null || pet.getOwnerReference() == null)
        {
            return;
        }

        UUID ownerId = pet.getOwnerReference().getUUID();
        PetRecord record = PetStorage.findPet(ownerId, recordId);
        if (record == null)
        {
            return;
        }

        LIVE_PETS.remove(recordId, pet);
        LAST_HEALTH.remove(recordId);
        long reviveAt = System.currentTimeMillis() + PetStats.reviveMillis(record.level());
        PetRecord updated = PetStorage.update(ownerId, recordId,
                current -> current.withEnabled(false).withReviveAt(reviveAt));
        if (updated == null || !(pet.getOwner() instanceof ServerPlayer owner))
        {
            return;
        }

        owner.sendSystemMessage(Component.empty().append(petLabel(owner, updated))
                .append("이(가) 쓰러졌습니다. " + PetStats.reviveMillis(updated.level()) / 1000L + "초 뒤에 부활합니다."));
        syncPets(owner);
    }

    /** 서버가 매 틱 호출한다. 부활 시각이 지난 펫을 풀어 주고, 모아 둔 경험치 변경을 보낸다. */
    public static void tick(MinecraftServer server)
    {
        int tick = server.getTickCount();
        if (tick % REVIVE_CHECK_INTERVAL_TICKS == 0)
        {
            long now = System.currentTimeMillis();
            for (ServerPlayer player : server.getPlayerList().getPlayers())
            {
                reviveExpiredPets(player, now);
            }
        }

        if (tick % EXP_SYNC_INTERVAL_TICKS == 0 && !EXP_DIRTY_PLAYERS.isEmpty())
        {
            for (UUID playerId : List.copyOf(EXP_DIRTY_PLAYERS))
            {
                EXP_DIRTY_PLAYERS.remove(playerId);
                ServerPlayer player = server.getPlayerList().getPlayer(playerId);
                if (player != null)
                {
                    syncPets(player);
                }
            }
        }
    }

    private static void reviveExpiredPets(ServerPlayer player, long now)
    {
        boolean revived = false;
        for (PetRecord record : PetStorage.getPets(player.getUUID()))
        {
            if (record.reviveAtMillis() == 0L || record.isReviving(now))
            {
                continue;
            }
            if (PetStorage.update(player.getUUID(), record.id(), current -> current.withReviveAt(0L)) != null)
            {
                player.sendSystemMessage(Component.empty().append(petLabel(player, record))
                        .append("이(가) 부활했습니다. 펫관리에서 다시 켤 수 있습니다."));
                revived = true;
            }
        }
        if (revived)
        {
            syncPets(player);
        }
    }

    /** 채팅에 쓰는 펫 이름. 머리 위 이름표와 같은 모양이다. */
    private static Component petLabel(ServerPlayer owner, PetRecord record)
    {
        List<PetRecord> records = PetStorage.getPets(owner.getUUID());
        return PetNames.withLevel(PetNames.displayName(records, record), record.rarity(), record.level());
    }

    /** 등급과 레벨에서 계산한 공격력·최대 체력을 펫에 입힌다. */
    private static void applyStats(LivingEntity pet, PetRecord record)
    {
        AttributeInstance attackAttribute = pet.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttribute != null)
        {
            attackAttribute.setBaseValue(PetStats.attackDamage(record.rarity(), record.level()));
        }
        AttributeInstance healthAttribute = pet.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttribute != null)
        {
            healthAttribute.setBaseValue(PetStats.maxHealth(record.rarity(), record.level()));
        }
    }

    /** 서버 종료 시 런타임 상태를 비운다. */
    public static void clearRuntimeState()
    {
        LIVE_PETS.clear();
        LAST_TOGGLE_TICKS.clear();
        LAST_HEALTH.clear();
        EXP_DIRTY_PLAYERS.clear();
    }

    /**
     * 주인의 켜진 펫 가운데 이 기록이 몇 번째인지.
     *
     * <p>펫들이 주인 둘레에 겹치지 않고 자리를 나눠 서는 데 쓴다.
     *
     * @return 0부터 세는 번호. 기록이 없거나 꺼져 있으면 -1
     */
    public static int enabledPetIndex(UUID ownerId, UUID recordId)
    {
        int index = 0;
        for (PetRecord record : PetStorage.getPets(ownerId))
        {
            if (!record.enabled()) continue;
            if (record.id().equals(recordId)) return index;
            index++;
        }
        return -1;
    }

    /** 주인의 켜진 펫 수 */
    public static int enabledPetCount(UUID ownerId)
    {
        int count = 0;
        for (PetRecord record : PetStorage.getPets(ownerId))
        {
            if (record.enabled()) count++;
        }
        return count;
    }

    /** 레지스트리 ID로 펫 타입을 찾는다. 알 수 없는 ID면 null. */
    public static EntityType<DialgaPetEntity> getPetType(String petTypeId)
    {
        return getPetTypesById().get(petTypeId);
    }

    private static void summonPet(ServerPlayer player, PetRecord record)
    {
        // 같은 기록의 펫이 이미 살아 있으면 회수하고 새로 소환한다. (재접속 시 행동 초기화)
        DialgaPetEntity existingPet = LIVE_PETS.remove(record.id());
        if (existingPet != null && !existingPet.isRemoved())
        {
            existingPet.discard();
        }

        if (!(player.level() instanceof ServerLevel serverLevel))
        {
            return;
        }

        EntityType<DialgaPetEntity> petType = getPetTypesById().get(record.petTypeId());
        if (petType == null)
        {
            LOGGER.warn("Unknown pet type {} for {}", record.petTypeId(), player.getScoreboardName());
            return;
        }

        DialgaPetEntity pet = petType.create(serverLevel, EntitySpawnReason.SPAWN_ITEM_USE);
        if (pet == null)
        {
            return;
        }

        pet.snapTo(player.getX() + 1.0, player.getY(), player.getZ() + 1.0, player.getYRot(), 0.0F);
        pet.tame(player);
        pet.setRecordId(record.id());
        // 엔티티는 저장되지 않으므로 소환할 때마다 이름표를 다시 붙인다.
        applyNameTag(pet, PetStorage.getPets(player.getUUID()), record);

        applyStats(pet, record);
        Float lastHealth = LAST_HEALTH.remove(record.id());
        pet.setHealth(lastHealth == null ? pet.getMaxHealth() : Math.max(1.0F, Math.min(lastHealth, pet.getMaxHealth())));

        if (serverLevel.addFreshEntity(pet))
        {
            LIVE_PETS.put(record.id(), pet);
        }
    }

    private static synchronized Map<String, EntityType<DialgaPetEntity>> getPetTypesById()
    {
        if (petTypesById == null)
        {
            petTypesById = Map.copyOf(buildPetTypesById());
        }
        return petTypesById;
    }

    private static Map<String, EntityType<DialgaPetEntity>> buildPetTypesById()
    {
        Map<String, EntityType<DialgaPetEntity>> typesById = new ConcurrentHashMap<>();
        List<EntityType<DialgaPetEntity>> petTypes = List.of(
                ModEntityTypes.DIALGA_PET,
                ModEntityTypes.KIRBY_PET,
                ModEntityTypes.UNICORN_PET,
                ModEntityTypes.GAZELLE_PET,
                ModEntityTypes.FAIRLINS_PET,
                ModEntityTypes.DARK_DRAGON_PET,
                ModEntityTypes.SCULKEN_RAVEN_PET,
                ModEntityTypes.GOMI_PET,
                ModEntityTypes.SUPER_GOMI_PET);
        for (EntityType<DialgaPetEntity> petType : petTypes)
        {
            typesById.put(BuiltInRegistries.ENTITY_TYPE.getKey(petType).toString(), petType);
        }
        return typesById;
    }
}
