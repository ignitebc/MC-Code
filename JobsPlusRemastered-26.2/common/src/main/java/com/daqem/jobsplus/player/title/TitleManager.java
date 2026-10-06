package com.daqem.jobsplus.player.title;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.networking.s2c.ClientboundTitlesPacket;
import dev.architectury.networking.NetworkManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * 칭호 획득 판정, 장착, 운영자 지급·회수, 클라이언트 동기화를 맡는다.
 *
 * <p>모든 칭호는 선착순이다. 먼저 조건을 채운 한 명이 갖고, 운영자가 회수하기 전까지 유지된다.
 */
public final class TitleManager
{
    public static final int SOUL_KILL_GOAL = 10_000;

    /**
     * 영원한정점에 필요한 서리빛 방어구 4부위.
     * 강화 기능이 있는 Illager Invasion과 장비를 만드는 Advanced Netherite에 의존하지 않도록 ID로 찾는다.
     */
    private static final Map<EquipmentSlot, Identifier> FROST_ARMOR = Map.of(
            EquipmentSlot.HEAD, Identifier.parse("advancednetherite:frost_helmet"),
            EquipmentSlot.CHEST, Identifier.parse("advancednetherite:frost_chestplate"),
            EquipmentSlot.LEGS, Identifier.parse("advancednetherite:frost_leggings"),
            EquipmentSlot.FEET, Identifier.parse("advancednetherite:frost_boots"));
    /** Illager Invasion의 EnhancementHelper가 강화 단계를 기록하는 커스텀 데이터 키. 같은 이름을 써야 한다. */
    private static final String ENHANCEMENT_LEVEL_KEY = "EnhancementLevel";
    private static final int MAX_ENHANCEMENT_LEVEL = 10;

    /** 적대 몹 판정에 쓰는 엔티티 종류. 레지스트리가 굳은 뒤 처음 쓸 때 한 번만 모은다. */
    private static List<EntityType<?>> monsterTypes;

    private TitleManager()
    {
    }

    /**
     * 처치 공로를 받은 플레이어에게 불린다.
     *
     * <p>바닐라가 처치 공로를 마지막으로 피해를 준 플레이어에게 주므로
     * 위더와 엔더 드래곤은 이 시점의 플레이어가 곧 마지막 일격을 한 사람이다.
     * 처치 통계도 이 호출 안에서 이미 올라간 뒤다.
     */
    public static void onKill(ServerPlayer killer, Entity killed)
    {
        EntityType<?> type = killed.getType();
        if (type == EntityTypes.WITHER)
        {
            award(killer, TitleType.NETHER_STAR);
        }
        if (type == EntityTypes.ENDER_DRAGON)
        {
            award(killer, TitleType.LAST_STRIKE);
        }
        if (type.getCategory() == MobCategory.MONSTER)
        {
            checkSoulKills(killer);
        }
    }

    public static void onHyperSkillOpened(ServerPlayer player)
    {
        award(player, TitleType.SEAL_BREAKER);
    }

    /**
     * 장비로 얻는 칭호를 1초마다 확인한다. 아직 주인이 없는 칭호만 살펴본다.
     *
     * <p>크리에이티브와 관전자는 아이템을 마음대로 꺼낼 수 있으므로 제외한다. 운영자가 명령어로 준 아이템은
     * 정상 획득과 구분할 수 없으므로 잘못 지급되면 {@code /job title revoke}로 회수한다.
     */
    public static void onEquipmentCheck(ServerPlayer player)
    {
        if (player.isCreative() || player.isSpectator())
        {
            return;
        }

        TitleLedger ledger = TitleLedger.get(player.level().getServer());
        boolean skyRulerOpen = ledger.getHolder(TitleType.SKY_RULER).isEmpty();
        if (skyRulerOpen && hasElytra(player))
        {
            award(player, TitleType.SKY_RULER);
        }
        boolean eternalPeakOpen = ledger.getHolder(TitleType.ETERNAL_PEAK).isEmpty();
        if (eternalPeakOpen && wearsMaxEnhancedFrostArmor(player))
        {
            award(player, TitleType.ETERNAL_PEAK);
        }
    }

    /**
     * 겉날개는 엔드 배 액자와 상점 구매로 얻는다. 경로마다 따로 연결하지 않고
     * 인벤토리나 가슴 칸에 들어왔는지로 판정해 두 경로를 함께 처리한다.
     */
    private static boolean hasElytra(ServerPlayer player)
    {
        if (player.getItemBySlot(EquipmentSlot.CHEST).is(Items.ELYTRA))
        {
            return true;
        }
        return player.getInventory().contains(stack -> stack.is(Items.ELYTRA));
    }

    /** 서리빛 방어구 4부위를 모두 +10강으로 동시에 입고 있는지 */
    private static boolean wearsMaxEnhancedFrostArmor(ServerPlayer player)
    {
        for (Map.Entry<EquipmentSlot, Identifier> armor : FROST_ARMOR.entrySet())
        {
            ItemStack worn = player.getItemBySlot(armor.getKey());
            boolean frostArmor = BuiltInRegistries.ITEM.getKey(worn.getItem()).equals(armor.getValue());
            if (!frostArmor || getEnhancementLevel(worn) < MAX_ENHANCEMENT_LEVEL)
            {
                return false;
            }
        }
        return true;
    }

    private static int getEnhancementLevel(ItemStack stack)
    {
        CustomData customData = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY);
        return customData.copyTag().getIntOr(ENHANCEMENT_LEVEL_KEY, 0);
    }

    /** 닉네임 변경을 반영하고 팀 소속과 칭호 탭 정보를 맞춘다. */
    public static void onPlayerJoin(ServerPlayer player)
    {
        MinecraftServer server = player.level().getServer();
        TitleLedger ledger = TitleLedger.get(server);
        UUID playerId = player.getUUID();
        String playerName = player.getScoreboardName();

        // 예전 이름이 칭호 팀에 남으면 그 이름으로 접속한 다른 사람에게 배지가 붙는다.
        Optional<String> previousName = ledger.updateHolderName(playerId, playerName);
        previousName.ifPresent(name -> TitleTeams.apply(server, name, null));

        TitleTeams.apply(server, playerName, ledger.getEquipped(playerId).orElse(null));
        syncTo(player);
    }

    /** @param type {@code null}이면 장착을 푼다. 보유하지 않은 칭호는 장착하지 않는다. */
    public static void equip(ServerPlayer player, @Nullable TitleType type)
    {
        MinecraftServer server = player.level().getServer();
        TitleLedger ledger = TitleLedger.get(server);
        UUID playerId = player.getUUID();
        boolean notOwned = type != null && !ledger.isHolder(playerId, type);
        if (notOwned)
        {
            syncTo(player);
            return;
        }
        ledger.setEquipped(playerId, type);
        TitleTeams.apply(server, player.getScoreboardName(), type);
        syncTo(player);
    }

    /** 운영자 지급. 기존 보유자가 있으면 그 사람의 칭호를 회수하고 대상에게 준다. */
    public static void grant(MinecraftServer server, TitleType type, ServerPlayer target)
    {
        TitleLedger ledger = TitleLedger.get(server);
        Optional<TitleLedger.Holder> previous = ledger.getHolder(type);
        ledger.setHolder(type, target.getUUID(), target.getScoreboardName());
        previous.filter(holder -> !holder.playerId().equals(target.getUUID()))
                .ifPresent(holder -> refreshTeam(server, ledger, holder));
        giveTo(server, ledger, target, type);
    }

    /** @return 회수한 보유자. 보유자가 없었으면 비어 있다. */
    public static Optional<TitleLedger.Holder> revoke(MinecraftServer server, TitleType type)
    {
        TitleLedger ledger = TitleLedger.get(server);
        Optional<TitleLedger.Holder> removed = ledger.removeHolder(type);
        removed.ifPresent(holder -> refreshTeam(server, ledger, holder));
        syncAll(server);
        return removed;
    }

    private static void award(ServerPlayer player, TitleType type)
    {
        MinecraftServer server = player.level().getServer();
        TitleLedger ledger = TitleLedger.get(server);
        if (!ledger.claim(type, player.getUUID(), player.getScoreboardName()))
        {
            return;
        }
        giveTo(server, ledger, player, type);
    }

    /** 보유자로 기록된 플레이어에게 처음 받은 칭호를 장착하고 서버 전체에 알린다. */
    private static void giveTo(MinecraftServer server, TitleLedger ledger, ServerPlayer player, TitleType type)
    {
        UUID playerId = player.getUUID();
        if (ledger.getEquipped(playerId).isEmpty())
        {
            ledger.setEquipped(playerId, type);
        }
        TitleTeams.apply(server, player.getScoreboardName(), ledger.getEquipped(playerId).orElse(null));

        Component message = JobsPlus.translatable("title.acquired",
                player.getName(), type.getBadge(), type.getCondition()).withStyle(ChatFormatting.GOLD);
        server.getPlayerList().broadcastSystemMessage(message, false);
        syncAll(server);
    }

    private static void refreshTeam(MinecraftServer server, TitleLedger ledger, TitleLedger.Holder holder)
    {
        TitleTeams.apply(server, holder.playerName(), ledger.getEquipped(holder.playerId()).orElse(null));
    }

    private static void checkSoulKills(ServerPlayer player)
    {
        TitleLedger ledger = TitleLedger.get(player.level().getServer());
        if (ledger.getHolder(TitleType.TEN_THOUSAND_SOULS).isPresent())
        {
            return;
        }
        if (countMonsterKills(player) >= SOUL_KILL_GOAL)
        {
            award(player, TitleType.TEN_THOUSAND_SOULS);
        }
    }

    /**
     * 바닐라 처치 통계에서 몬스터 분류 엔티티를 모두 더한다.
     * 통계를 쓰면 칭호 기능을 넣기 전의 처치 수도 함께 센다.
     */
    private static int countMonsterKills(ServerPlayer player)
    {
        int total = 0;
        for (EntityType<?> type : getMonsterTypes())
        {
            total += player.getStats().getValue(Stats.ENTITY_KILLED, type);
        }
        return total;
    }

    private static List<EntityType<?>> getMonsterTypes()
    {
        if (monsterTypes == null)
        {
            List<EntityType<?>> types = new ArrayList<>();
            for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE)
            {
                if (type.getCategory() == MobCategory.MONSTER)
                {
                    types.add(type);
                }
            }
            monsterTypes = List.copyOf(types);
        }
        return monsterTypes;
    }

    public static void syncAll(MinecraftServer server)
    {
        for (ServerPlayer player : server.getPlayerList().getPlayers())
        {
            syncTo(player);
        }
    }

    public static void syncTo(ServerPlayer player)
    {
        TitleLedger ledger = TitleLedger.get(player.level().getServer());
        UUID playerId = player.getUUID();
        List<ClientboundTitlesPacket.Entry> entries = new ArrayList<>();
        for (TitleType type : TitleType.values())
        {
            Optional<TitleLedger.Holder> holder = ledger.getHolder(type);
            String holderName = holder.map(TitleLedger.Holder::playerName).orElse("");
            boolean mine = holder.filter(found -> found.playerId().equals(playerId)).isPresent();
            entries.add(new ClientboundTitlesPacket.Entry(type.getId(), holderName, mine));
        }
        String equippedId = ledger.getEquipped(playerId).map(TitleType::getId).orElse("");
        NetworkManager.sendToPlayer(player, new ClientboundTitlesPacket(entries, equippedId));
    }
}
