package com.tacz.guns.resource;

import cn.sh1rocu.tacz.TaCZFabric;
import cn.sh1rocu.tacz.api.event.AddReloadListenerEvent;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tacz.guns.api.vmlib.LuaGunLogicConstant;
import com.tacz.guns.api.vmlib.LuaLibrary;
import com.tacz.guns.crafting.GunSmithTableIngredient;
import com.tacz.guns.crafting.GunSmithTableRecipe;
import com.tacz.guns.resource.pojo.data.recipe.TableRecipe;
import com.tacz.guns.crafting.result.GunSmithTableResult;
import com.tacz.guns.init.ModRecipe;
import com.tacz.guns.network.NetworkHandler;
import com.tacz.guns.network.message.ServerMessageSyncGunPack;
import com.tacz.guns.resource.filter.RecipeFilter;
import com.tacz.guns.resource.index.CommonAmmoIndex;
import com.tacz.guns.resource.index.CommonAttachmentIndex;
import com.tacz.guns.resource.index.CommonBlockIndex;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.manager.*;
import com.tacz.guns.resource.network.CommonNetworkCache;
import com.tacz.guns.resource.network.DataType;
import com.tacz.guns.resource.pojo.data.attachment.AttachmentData;
import com.tacz.guns.resource.pojo.data.block.BlockData;
import com.tacz.guns.resource.pojo.data.block.TabConfig;
import com.tacz.guns.resource.pojo.data.gun.ExtraDamage;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.resource.pojo.data.gun.Ignite;
import com.tacz.guns.resource.pojo.data.loot.LootTableInjection;
import com.tacz.guns.resource.serialize.*;
import com.tacz.guns.util.AllowAttachmentTagMatcher;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.LuaTable;

import java.util.*;
import java.util.function.Consumer;

public class CommonAssetsManager implements ICommonResourceProvider {
    private static CommonAssetsManager INSTANCE;
    public static final Gson GSON = new GsonBuilder()
            // 총기 팩은 JSON5 형식의 주석과 끝 쉼표를 일부러 지원한다.
            .setStrictness(com.google.gson.Strictness.LENIENT)
            .registerTypeAdapter(Identifier.class, new IdentifierSerializer())
            .registerTypeAdapter(Pair.class, new PairSerializer())
            .registerTypeAdapter(GunSmithTableIngredient.class, new GunSmithTableIngredientSerializer())
            .registerTypeAdapter(GunSmithTableResult.class, new GunSmithTableResultSerializer())
            .registerTypeAdapter(ExtraDamage.DistanceDamagePair.class, new DistanceDamagePairSerializer())
            .registerTypeAdapter(Vec3.class, new Vec3Serializer())
            .registerTypeAdapter(Ignite.class, new IgniteSerializer())
            .registerTypeAdapter(RecipeFilter.class, new RecipeFilter.Deserializer())
            .registerTypeAdapter(CommonGunIndex.class, new CommonGunIndexSerializer())
            .registerTypeAdapter(CommonAmmoIndex.class, new CommonAmmoIndexSerializer())
            .registerTypeAdapter(CommonAttachmentIndex.class, new CommonAttachmentIndexSerializer())
            .registerTypeAdapter(CommonBlockIndex.class, new CommonBlockIndexSerializer())
            .registerTypeAdapter(TabConfig.class, new TabConfig.Deserializer())
            .create();

    private final List<INetworkCacheReloadListener> listeners = new ArrayList<>();
    private CommonDataManager<GunData> gunData;
    private CommonDataManager<AttachmentData> attachmentData;
    private CommonDataManager<BlockData> blockData;
    private CommonDataManager<CommonAmmoIndex> ammoIndex;
    private CommonDataManager<CommonGunIndex> gunIndex;
    private CommonDataManager<CommonAttachmentIndex> attachmentIndex;
    private CommonDataManager<CommonBlockIndex> blockIndex;
    /** 12차: 총기 작업대 레시피. GunSmithTableScreen이 쓰도록 클라이언트에 동기화해야 한다. */
    private CommonDataManager<TableRecipe> tableRecipe;
    private RecipeFilterManager recipeFilterManager;
    private LootInjectionManager lootInjectionManager;

    private AttachmentsTagManager attachmentsTagManager;
    List<LuaLibrary> libList = List.of(new LuaGunLogicConstant());
    private final ScriptManager scriptManager = new ScriptManager(new FileToIdConverter("scripts", ".lua"), libList);

    public void reloadAndRegister(Consumer<PreparableReloadListener> register) {
        // 여기는 순서대로 다시 불러오므로 index처럼 data에 기대는 것은 뒤에 둬야 한다
        gunData = register(new CommonDataManager<>(DataType.GUN_DATA, GunData.class, GSON, "data/guns", "GunDataLoader"));
        attachmentData = register(new AttachmentDataManager());
        attachmentsTagManager = register(new AttachmentsTagManager());
        recipeFilterManager = register(new RecipeFilterManager());
        lootInjectionManager = new LootInjectionManager();
        register.accept(lootInjectionManager);
        blockData = register(new CommonDataManager<>(DataType.BLOCK_DATA, BlockData.class, GSON, "data/blocks", "BlockDataLoader"));
        register.accept(scriptManager);

        ammoIndex = register(new CommonDataManager<>(DataType.AMMO_INDEX, CommonAmmoIndex.class, GSON, "index/ammo", "AmmoIndexLoader"));
        gunIndex = register(new CommonDataManager<>(DataType.GUN_INDEX, CommonGunIndex.class, GSON, "index/guns", "GunIndexLoader"));
        attachmentIndex = register(new CommonDataManager<>(DataType.ATTACHMENT_INDEX, CommonAttachmentIndex.class, GSON, "index/attachments", "AttachmentIndexLoader"));
        blockIndex = register(new CommonDataManager<>(DataType.BLOCK_INDEX, CommonBlockIndex.class, GSON, "index/blocks", "BlockIndexLoader"));
        // 12차: 작업대 레시피도 동기화 대상에 넣는다. 디렉터리는 바닐라 데이터 팩 레시피와 같다(data/<ns>/recipe).
        // 그러면 클라이언트는 RecipeManager 없이도 레시피를 나열할 수 있다(26.2 클라이언트에는 완전한 레시피 표가 없다).
        //
        // 맨 CommonDataManager가 아니라 반드시 TableRecipeManager를 써야 한다: 이 디렉터리에는 바닐라와
        // 다른 모드의 레시피가 섞여 있어(실측 바닐라 1585개) "type"으로 거르지 않으면 모두
        // TableRecipe 해석기로 들어가 로그가 넘치고 그대로 동기화 패킷에도 실린다. 자세한 내용은 그 클래스 주석 참고.
        tableRecipe = register(new TableRecipeManager());

        listeners.forEach(register);
        register.accept((sharedState, backgroundExecutor, barrier, gameExecutor) -> {
            return barrier
                    .wait(null)
                    .thenRunAsync(AllowAttachmentTagMatcher::resetCache, gameExecutor);
        });
    }

    private <T extends INetworkCacheReloadListener> T register(T listener) {
        listeners.add(listener);
        return listener;
    }

    public Map<DataType, Map<Identifier, String>> getNetworkCache() {
        ImmutableMap.Builder<DataType, Map<Identifier, String>> builder = ImmutableMap.builder();
        for (INetworkCacheReloadListener listener : listeners) {
            builder.put(listener.getType(), listener.getNetworkCache());
        }
        return builder.build();
    }

    @Nullable
    @Override
    public GunData getGunData(Identifier id) {
        return gunData.getData(id);
    }

    @Nullable
    @Override
    public AttachmentData getAttachmentData(Identifier id) {
        return attachmentData.getData(id);
    }

    @Nullable
    @Override
    public BlockData getBlockData(Identifier id) {
        return blockData.getData(id);
    }

    @Override
    @Nullable
    public RecipeFilter getRecipeFilter(Identifier id) {
        return recipeFilterManager.getFilter(id);
    }

    /**
     * 총기 팩이 주입하겠다고 선언한 <b>대상 전리품 표 ID 집합</b>.
     *
     * <p>{@code LootTableInjectorModifier}의 "정방향 조회"에 쓴다: 26.2에서는 LootTable 인스턴스로
     * 등록 ID를 거꾸로 찾을 수 없으므로(RELOADABLE 계층은 HolderLookup만 주고 역조회 인터페이스가 없음)
     * 이 후보 집합으로 하나씩 정방향 조회해 인스턴스를 비교한다. 기본 총기 팩의 대상 표는 1개뿐이다.</p>
     */
    public Set<Identifier> getLootInjectionTargets() {
        if (lootInjectionManager == null) {
            return Set.of();
        }
        return lootInjectionManager.getInjectionTargets();
    }

    public List<LootTableInjection> getLootTableInjections(Identifier lootTable) {
        if (lootInjectionManager == null) {
            return List.of();
        }
        return lootInjectionManager.getInjections(lootTable);
    }

    @Nullable
    @Override
    public CommonGunIndex getGunIndex(Identifier gunId) {
        return gunIndex.getData(gunId);
    }

    @Override
    public Set<Map.Entry<Identifier, CommonGunIndex>> getAllGuns() {
        return gunIndex.getAllData().entrySet();
    }

    @Nullable
    @Override
    public CommonAmmoIndex getAmmoIndex(Identifier ammoId) {
        return ammoIndex.getData(ammoId);
    }

    @Override
    public Set<Map.Entry<Identifier, CommonAmmoIndex>> getAllAmmos() {
        return ammoIndex.getAllData().entrySet();
    }

    @Nullable
    @Override
    public CommonAttachmentIndex getAttachmentIndex(Identifier attachmentId) {
        return attachmentIndex.getData(attachmentId);
    }

    @Override
    public Set<Map.Entry<Identifier, CommonAttachmentIndex>> getAllAttachments() {
        return attachmentIndex.getAllData().entrySet();
    }

    @Override
    public LuaTable getScript(Identifier scriptId) {
        return scriptManager.getScript(scriptId);
    }

    @Nullable
    @Override
    public CommonBlockIndex getBlockIndex(Identifier blockId) {
        return blockIndex.getData(blockId);
    }

    @Override
    public TableRecipe getTableRecipe(Identifier recipeId) {
        return tableRecipe == null ? null : tableRecipe.getData(recipeId);
    }

    @Override
    public Set<Map.Entry<Identifier, TableRecipe>> getAllTableRecipes() {
        return tableRecipe == null ? java.util.Collections.emptySet() : tableRecipe.getAllData().entrySet();
    }

    public Set<Map.Entry<Identifier, CommonBlockIndex>> getAllBlocks() {
        return blockIndex.getAllData().entrySet();
    }

    @Override
    public Set<String> getAttachmentTags(Identifier registryName) {
        return attachmentsTagManager.getAttachmentTags(registryName);
    }

    @Override
    public Set<String> getAllowAttachmentTags(Identifier registryName) {
        return attachmentsTagManager.getAllowAttachmentTags(registryName);
    }

    /**
     * 인스턴스를 가져온다<br/>
     * 인스턴스는 내장 서버/전용 서버가 시작할 때만 만들어진다<br/>
     * 클라이언트가 멀티플레이에 접속 중이면 이 메서드는 null을 돌려준다
     *
     * @return CommonAssetsManger 인스턴스
     */
    @Nullable
    public static CommonAssetsManager getInstance() {
        return INSTANCE;
    }

    public static void clearInstance() {
        INSTANCE = null;
    }

    /**
     * 현재 환경에 맞는 캐시를 고른다<br/>
     * 싱글플레이나 멀티플레이 서버이면 CommonAssetsManger 인스턴스를 돌려준다<br/>
     * 멀티플레이 클라이언트이면 CommonNetworkCache 인스턴스를 돌려준다
     *
     * @return ICommonResourceProvider 인스턴스
     */
    public static ICommonResourceProvider get() {
        return INSTANCE == null ? CommonNetworkCache.INSTANCE : INSTANCE;
    }

    public static void onReload(AddReloadListenerEvent event) {
        var commonAssetsManager = new CommonAssetsManager();
        commonAssetsManager.reloadAndRegister(event::addListener);
        INSTANCE = commonAssetsManager;
        INSTANCE.recipeManager = event.getServerResources().getRecipeManager();
    }

    public RecipeManager recipeManager;

    public RecipeManager getRecipeManager() {
        return recipeManager;
    }

    /**
     * 이 이벤트는 이론상 server resource가 다시 불러오기를 마치고 클라이언트로 전송되기 전에 발생한다<br/>
     * common data를 바탕으로 지연 로드한 레시피를 초기화해 본다
     */
    public static void onReload(RegistryAccess registries, boolean client) {
        if (!client) {
            if (getInstance() != null && getInstance().recipeManager != null) {
                List<GunSmithTableRecipe> recipes = getInstance().recipeManager.getRecipes().stream()
                        .map(net.minecraft.world.item.crafting.RecipeHolder::value)
                        .filter(recipe -> recipe.getType() == ModRecipe.GUN_SMITH_TABLE_CRAFTING)
                        .map(GunSmithTableRecipe.class::cast)
                        .toList();
                for (GunSmithTableRecipe recipe : recipes) {
                    recipe.init();
                }
            }
        }
    }

    public static void onServerStopped(MinecraftServer server) {
        clearInstance();
    }

    public static void OnDatapackSync(ServerPlayer player, boolean joined) {
        if (getInstance() == null) {
            return;
        }
        ServerMessageSyncGunPack message = new ServerMessageSyncGunPack(getInstance().getNetworkCache());
        NetworkHandler.sendToClientPlayer(message, player);

    }

    public static void reloadAllPack() {
        var server = TaCZFabric.getServer();
        if (server == null) {
            return;
        }
        PackRepository packrepository = server.getPackRepository();
        packrepository.reload();

        Collection<String> collection = packrepository.getSelectedIds();
        server.reloadResources(collection);
    }
}


