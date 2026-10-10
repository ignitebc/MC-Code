package com.tacz.guns.resource;

import com.tacz.guns.resource.filter.RecipeFilter;
import com.tacz.guns.resource.index.CommonAmmoIndex;
import com.tacz.guns.resource.index.CommonAttachmentIndex;
import com.tacz.guns.resource.index.CommonBlockIndex;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.attachment.AttachmentData;
import com.tacz.guns.resource.pojo.data.block.BlockData;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.resource.pojo.data.recipe.TableRecipe;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.luaj.vm2.LuaTable;

import java.util.Map;
import java.util.Set;

public interface ICommonResourceProvider {
    @Nullable GunData getGunData(Identifier id);

    @Nullable AttachmentData getAttachmentData(Identifier attachmentId);

    @Nullable BlockData getBlockData(Identifier id);

    @Nullable RecipeFilter getRecipeFilter(Identifier id);

    @Nullable CommonGunIndex getGunIndex(Identifier gunId);

    @Nullable CommonAmmoIndex getAmmoIndex(Identifier ammoId);

    @Nullable CommonAttachmentIndex getAttachmentIndex(Identifier attachmentId);

    @Nullable CommonBlockIndex getBlockIndex(Identifier blockId);

    @Nullable
    public LuaTable getScript(Identifier scriptId);

    Set<Map.Entry<Identifier, CommonGunIndex>> getAllGuns();

    Set<Map.Entry<Identifier, CommonAmmoIndex>> getAllAmmos();

    Set<Map.Entry<Identifier, CommonAttachmentIndex>> getAllAttachments();

    Set<Map.Entry<Identifier, CommonBlockIndex>> getAllBlocks();

    Set<String> getAttachmentTags(Identifier registryName);

    Set<String> getAllowAttachmentTags(Identifier registryName);

    /**
     * 총기 작업대 레시피.
     *
     * <p><b>필요한 이유(12차)</b>: 26.2 클라이언트에는 완전한 레시피 표가 <b>없다</b> —
     * {@code ClientLevel#recipeAccess()}가 돌려주는 {@code RecipeAccess}에는
     * {@code propertySet(...)}과 {@code stonecutterRecipes()}만 있고,
     * 바닐라는 레시피 책에 필요한 부분만 내려보낸다. 원본 1.21.1이 쓴
     * {@code recipeManager.getAllRecipesFor(...)}는 26.2 클라이언트에서 더는 쓸 수 없다.</p>
     *
     * <p>그래서 작업대 화면에 필요한 레시피는 mod가 직접 동기화해야 하며,
     * 기존 {@code DataType.RECIPES} 경로를 탄다(이 열거값은 예전부터 선언만 되고 연결된 적이 없었다).</p>
     */
    @Nullable TableRecipe getTableRecipe(Identifier recipeId);

    Set<Map.Entry<Identifier, TableRecipe>> getAllTableRecipes();
}
