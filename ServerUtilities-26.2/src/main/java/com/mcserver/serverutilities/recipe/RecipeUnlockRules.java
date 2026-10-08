package com.mcserver.serverutilities.recipe;

import com.mcserver.serverutilities.ServerUtilities;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * 조합법 책에 나오는 레시피를 접속할 때 모두 해금한다.
 *
 * <p>바닐라는 정해진 아이템을 인벤토리에 넣거나 직접 한 번 조합해야 조합법 책에 보여 준다. 횃불은 돌 곡괭이가
 * 해금 조건이라 금 도구로 시작하는 플레이어는 직접 조합해 보기 전까지 책에서 찾지 못한다. 강화 네더라이트 주괴도
 * 철·금·에메랄드·다이아몬드를 인벤토리에 넣기 전까지 보이지 않는다. 그래서 접속과 데이터팩 리로드 때 한꺼번에 해금한다.
 *
 * <p>레시피 타입이 minecraft 네임스페이스인 것(제작대·화로·용광로·훈연기·모닥불·석재 절단기·대장장이 작업대)만 고른다.
 * TACZ 총기 제작대 같은 모드 전용 레시피는 바닐라 조합법 책에 나오지 않고, 등록되지 않은 책 분류를 돌려주기도 한다.
 * 이미 아는 조합법은 바닐라가 건너뛰므로 재접속 때는 새로 추가된 레시피만 보낸다.
 */
public final class RecipeUnlockRules {
    private static final String VANILLA_NAMESPACE = "minecraft";

    private RecipeUnlockRules() { }

    public static void onJoin(ServerPlayer player) {
        if (!ServerUtilities.config().recipeUnlockAll()) return;
        MinecraftServer server = player.level().getServer();
        player.awardRecipes(collectBookRecipes(server));
    }

    /** 데이터팩 리로드나 설정 재적용으로 레시피·설정이 바뀌었을 때 접속 중인 플레이어에게 적용한다. */
    public static void unlockForOnlinePlayers(MinecraftServer server) {
        if (!ServerUtilities.config().recipeUnlockAll()) return;
        List<RecipeHolder<?>> recipes = collectBookRecipes(server);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            player.awardRecipes(recipes);
        }
    }

    private static List<RecipeHolder<?>> collectBookRecipes(MinecraftServer server) {
        List<RecipeHolder<?>> recipes = new ArrayList<>();
        for (RecipeHolder<?> holder : server.getRecipeManager().getRecipes()) {
            if (isVanillaRecipeType(holder)) recipes.add(holder);
        }
        return recipes;
    }

    private static boolean isVanillaRecipeType(RecipeHolder<?> holder) {
        Identifier typeId = BuiltInRegistries.RECIPE_TYPE.getKey(holder.value().getType());
        if (typeId == null) return false;
        return VANILLA_NAMESPACE.equals(typeId.getNamespace());
    }
}
