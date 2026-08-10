package com.autovw.advancednetherite.common.pet;

import com.autovw.advancednetherite.common.entity.DialgaPetEntity;
import com.autovw.advancednetherite.core.ModEntityTypes;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
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
        if (syncHandler != null)
        {
            syncHandler.accept(player);
        }
    }

    /** 펫 상자 사용 시 호출된다. 기록을 만들고 즉시 소환한다. */
    public static boolean createPet(ServerPlayer player, EntityType<DialgaPetEntity> petType, double attackDamage)
    {
        if (!PetStorage.isAvailable())
        {
            return false;
        }

        String petTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(petType).toString();
        PetRecord record = new PetRecord(UUID.randomUUID(), petTypeId, attackDamage, true);
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
            LIVE_PETS.remove(recordId, pet);
        }
    }

    /** 서버 종료 시 런타임 상태를 비운다. */
    public static void clearRuntimeState()
    {
        LIVE_PETS.clear();
        LAST_TOGGLE_TICKS.clear();
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

        AttributeInstance attackAttribute = pet.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttribute != null)
        {
            attackAttribute.setBaseValue(record.attackDamage());
        }

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
