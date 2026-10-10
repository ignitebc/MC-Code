package com.tacz.guns.inventory;

import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.config.sync.SyncConfig;
import com.tacz.guns.crafting.GunSmithTableIngredient;
import com.tacz.guns.crafting.GunSmithTableRecipe;
import com.tacz.guns.network.NetworkHandler;
import com.tacz.guns.network.message.ServerMessageCraft;
import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.filter.RecipeFilter;
import com.tacz.guns.resource.index.CommonBlockIndex;
import com.tacz.guns.resource.pojo.data.recipe.TableRecipe;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class GunSmithTableMenu extends AbstractContainerMenu {
    // 26.2: ExtendedMenuType과 StreamCodec으로 Identifier 데이터를 넘긴다
    public static final ExtendedMenuType<GunSmithTableMenu, Identifier> TYPE = new ExtendedMenuType<>(
            (windowId, inv, data) -> new GunSmithTableMenu(windowId, inv, data),
            Identifier.STREAM_CODEC);

    private final Identifier blockId;
    private final RecipeFilter filter;

    public GunSmithTableMenu(int id, Inventory inventory, @Nullable Identifier resourceLocation) {
        super(TYPE, id);
        this.blockId = resourceLocation;
        this.filter = TimelessAPI.getCommonBlockIndex(getBlockId()).map(CommonBlockIndex::getFilter).orElse(null);
    }

    @Nullable
    public Identifier getBlockId() {
        return blockId;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int pIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.isAlive();
    }

    /**
     * id로 작업대 레시피를 꺼내 권한을 검사한다.
     *
     * <h2>바닐라 {@code RecipeManager} 대신 {@code CommonAssetsManager}를 쓰는 이유</h2>
     * 원래 여기는 {@code recipeManager.byKey(...)}였다(원본 1.21.1 그대로). 그런데 이 프로젝트는 12차에
     * 작업대 레시피 전체를 mod 자체 {@code DataType.RECIPES} 경로로 옮겼고
     * (이유: 26.2 클라이언트에는 완전한 레시피 표가 없음. {@code ICommonResourceProvider#getTableRecipe} 참고),
     * 실제로 제작을 실행하는 이 서버 메서드만 바닐라 경로에 남아 있었다</b> — 그래서 두 데이터 원본이 갈라졌다.
     *
     * <p>갈라진 결과는 "<b>보이는데 눌러도 안 됨</b>"이었다: 레시피가 우리 경로에는 있고
     * 바닐라 {@code RecipeManager}에는 없으면, 화면은 정상으로 목록을 보여 주고 재료 수도 정상으로 세지만
     * 제작을 누르면 {@code byKey}가 빈 값을 돌려준다 → 이 메서드가 {@code null}을 돌려준다 →
     * {@code doCraft}가 바로 return하며 <b>오류도, 안내도, 재료 차감도 없다</b>.
     *
     * <p>실측한 발생 상황: 예전 총기 팩은 레시피를 {@code data/<ns>/recipes/}(복수)에 둔다.
     * 우리 {@code TableRecipeManager#prepare}는 이 예전 디렉터리를 일부러 지원하지만,
     * 바닐라 {@code RecipeManager}의 {@code RECIPE_LISTER}는
     * {@code FileToIdConverter.registry(Registries.RECIPE)}이고, 바이트코드로 단계별 확인한 결과 디렉터리 이름은
     * {@code registryDirPath} → {@code ResourceKey.identifier().getPath()} = {@code "recipe"}에서 온다
     * (<b>단수이며 상수라 확장할 수 없음</b>). 그래서 이 레시피들은 바닐라 경로에서 <b>영원히 보이지 않고</b>,
     * 예전 총기 팩의 레시피는 하나도 제작할 수 없었다.
     *
     * <p>화면과 같은 원본으로 바꾼 뒤에는 양쪽 판정이 완전히 같아져, 예전/새 디렉터리와 기본/서드파티 팩을 똑같이 다룬다.
     * 검사 로직(필터 + 탭 소속)은 그대로 두며 <b>어떤 제한도 완화하지 않는다</b> —
     * 여전히 "현재 블록 탭에 실제로 있는" 레시피만 제작할 수 있다.
     */
    @Nullable
    private GunSmithTableRecipe getRecipe(Identifier recipeId) {
        if (!DefaultAssets.DEFAULT_BLOCK_ID.equals(getBlockId()) || SyncConfig.ENABLE_TABLE_FILTER.get()) {
            if (filter != null && !filter.contains(recipeId)) {
                return null;
            }
        }

        TableRecipe pojo = CommonAssetsManager.get().getTableRecipe(recipeId);
        if (pojo == null || pojo.getResult() == null) {
            return null;
        }
        GunSmithTableRecipe gunSmithTableRecipe = new GunSmithTableRecipe(recipeId, pojo);
        // 반드시 init()해야 한다: Gson 역직렬화는 raw 데이터만 채우므로,
        // 실제 ItemStack과 group(=탭)은 이것으로 해석해야 한다.
        // 이 단계가 빠지면 getTab()이 항상 null이라 아래 탭 검사가 반드시 실패한다.
        gunSmithTableRecipe.init();

        boolean flag = TimelessAPI.getCommonBlockIndex(getBlockId()).map(blockIndex -> {
            return blockIndex.getData().getTabs().stream().noneMatch(tab -> tab.id().equals(gunSmithTableRecipe.getTab()));
        }).orElse(true);
        if (DefaultAssets.DEFAULT_BLOCK_ID.equals(getBlockId()) && !SyncConfig.ENABLE_TABLE_FILTER.get()) {
            flag = false;
        }
        if (flag) {
            return null;
        }
        return gunSmithTableRecipe;
    }

    public void doCraft(Identifier recipeId, Player player) {
        // 여전히 서버 환경을 요구한다: 제작은 서버가 권한을 갖고 실행해야 한다(드롭 생성, 재료 차감).
        // 레시피 데이터 자체는 이제 CommonAssetsManager에서 가져오며 level의 RecipeManager에 기대지 않는다. getRecipe 참고.
        Level level = player.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        GunSmithTableRecipe recipe = getRecipe(recipeId);
        if (recipe == null) {
            return;
        }
        player.tacz$getItemHandler(null).ifPresent(handler -> {
            // 크리에이티브 모드이면 재료를 차감하지 않는다
            if (!player.isCreative()) {
                Int2IntArrayMap recordCount = new Int2IntArrayMap();
                List<GunSmithTableIngredient> ingredients = recipe.getInputs();

                for (GunSmithTableIngredient ingredient : ingredients) {
                    int count = 0;
                    // 14차: 재료 지연 해석. 해석할 수 없으면(tag 없음 등)
                    // 그 재료를 건너뛰지 말고 반드시 <b>제작을 거부</b>해야 한다 — 아니면 플레이어가 완성품을 공짜로 얻는다.
                    net.minecraft.world.item.crafting.Ingredient resolved = ingredient.getIngredient();
                    if (resolved == null) {
                        return;
                    }
                    for (int slotIndex = 0; slotIndex < handler.getSlots(); slotIndex++) {
                        ItemStack stack = handler.getStackInSlot(slotIndex);
                        int stackCount = stack.getCount();
                        if (!stack.isEmpty() && resolved.test(stack)) {
                            count = count + stackCount;
                            // 차감한 slot과 수량을 기록한다
                            if (count <= ingredient.getCount()) {
                                // 수량이 모자라면 전부 차감한다
                                recordCount.put(slotIndex, stackCount);
                            } else {
                                //  수량이 충분하면 필요한 수량만 차감한다
                                int remaining = count - ingredient.getCount();
                                recordCount.put(slotIndex, stackCount - remaining);
                                break;
                            }
                        }
                    }
                    // 수량이 모자라면 이후 로직을 실행하지 않고 제작에 실패한다
                    if (count < ingredient.getCount()) {
                        return;
                    }
                }

                // 재료 차감 시작
                for (int slotIndex : recordCount.keySet()) {
                    handler.extractItem(slotIndex, recordCount.get(slotIndex), false);
                }
            }

            // 플레이어에게 해당 아이템을 준다
            if (!level.isClientSide()) {
                ItemEntity itemEntity = new ItemEntity(level, player.getX(), player.getY() + 0.5, player.getZ(), recipe.getOutput().copy());
                itemEntity.setPickUpDelay(0);
                level.addFreshEntity(itemEntity);
            }
            // 갱신한다. 아니면 클라이언트 표시가 틀린다
            player.inventoryMenu.broadcastFullState();
            if (player instanceof ServerPlayer serverPlayer)
                NetworkHandler.sendToClientPlayer(new ServerMessageCraft(this.containerId), serverPlayer);
        });
    }
}
