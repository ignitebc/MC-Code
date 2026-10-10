package com.tacz.guns.api;

import com.tacz.guns.api.client.other.IThirdPersonAnimation;
import com.tacz.guns.api.client.other.ThirdPersonManager;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.resource.ClientIndexManager;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.index.ClientAmmoIndex;
import com.tacz.guns.client.resource.index.ClientAttachmentIndex;
import com.tacz.guns.client.resource.index.ClientBlockIndex;
import com.tacz.guns.client.resource.index.ClientGunIndex;
import com.tacz.guns.crafting.GunSmithTableRecipe;
import com.tacz.guns.resource.CommonAssetsManager;
import com.tacz.guns.resource.index.CommonAmmoIndex;
import com.tacz.guns.resource.index.CommonAttachmentIndex;
import com.tacz.guns.resource.index.CommonBlockIndex;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.recipe.TableRecipe;
import com.tacz.guns.util.GunIdAliases;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class TimelessAPI {
    @Environment(EnvType.CLIENT)
    public static Optional<GunDisplayInstance> getGunDisplay(ItemStack stack) {
        if (stack.getItem() instanceof IGun iGun) {
            Identifier gunId = iGun.getGunId(stack);
            if (getCommonGunIndex(gunId).isEmpty()) {
                return Optional.empty();
            }
            Identifier displayId = iGun.getGunDisplayId(stack);
            if (displayId.equals(DefaultAssets.DEFAULT_GUN_DISPLAY_ID)) {
                return getClientGunIndex(gunId).map(ClientGunIndex::getDefaultDisplay);
            } else {
                return getGunDisplay(displayId, gunId);
            }
        }
        return Optional.empty();
    }

    @Environment(EnvType.CLIENT)
    public static Optional<ClientGunIndex> getClientGunIndex(Identifier gunId) {
        return Optional.ofNullable(ClientIndexManager.GUN_INDEX.get(GunIdAliases.canonicalGunId(gunId)));
    }

    @Environment(EnvType.CLIENT)
    public static Optional<GunDisplayInstance> getGunDisplay(Identifier displayId, Identifier fallbackGunId) {
        if (displayId == null || displayId.equals(DefaultAssets.DEFAULT_GUN_DISPLAY_ID)) {
            return getClientGunIndex(fallbackGunId).map(ClientGunIndex::getDefaultDisplay);
        }

        GunDisplayInstance instance = ClientIndexManager.getOrCreateGunDisplay(GunIdAliases.canonicalDisplayId(displayId));
        if (instance == null) {
            return getClientGunIndex(fallbackGunId).map(ClientGunIndex::getDefaultDisplay);
        }
        return Optional.of(instance);
    }

    @Environment(EnvType.CLIENT)
    public static Optional<ClientAttachmentIndex> getClientAttachmentIndex(Identifier attachmentId) {
        return Optional.ofNullable(ClientIndexManager.ATTACHMENT_INDEX.get(attachmentId));
    }

    @Environment(EnvType.CLIENT)
    public static Optional<ClientAmmoIndex> getClientAmmoIndex(Identifier ammoId) {
        return Optional.ofNullable(ClientIndexManager.AMMO_INDEX.get(ammoId));
    }

    @Environment(EnvType.CLIENT)
    public static Optional<ClientBlockIndex> getClientBlockIndex(Identifier blockId) {
        return Optional.ofNullable(ClientIndexManager.BLOCK_INDEX.get(blockId));
    }

    @Environment(EnvType.CLIENT)
    public static Set<Map.Entry<Identifier, ClientGunIndex>> getAllClientGunIndex() {
        return ClientIndexManager.getAllGuns();
    }

    @Environment(EnvType.CLIENT)
    public static Set<Map.Entry<Identifier, ClientAmmoIndex>> getAllClientAmmoIndex() {
        return ClientIndexManager.getAllAmmo();
    }

    @Environment(EnvType.CLIENT)
    public static Set<Map.Entry<Identifier, ClientAttachmentIndex>> getAllClientAttachmentIndex() {
        return ClientIndexManager.getAllAttachments();
    }

    public static Optional<CommonBlockIndex> getCommonBlockIndex(Identifier blockId) {
        return Optional.ofNullable(CommonAssetsManager.get().getBlockIndex(blockId));
    }

    public static Optional<CommonGunIndex> getCommonGunIndex(Identifier gunId) {
        return Optional.ofNullable(CommonAssetsManager.get().getGunIndex(GunIdAliases.canonicalGunId(gunId)));
    }

    public static Optional<CommonAttachmentIndex> getCommonAttachmentIndex(Identifier attachmentId) {
        return Optional.ofNullable(CommonAssetsManager.get().getAttachmentIndex(attachmentId));
    }

    public static Optional<CommonAmmoIndex> getCommonAmmoIndex(Identifier ammoId) {
        return Optional.ofNullable(CommonAssetsManager.get().getAmmoIndex(ammoId));
    }

    /**
     * ID로 작업대 제작법을 꺼낸다.
     *
     * <p>예전에는 여기 주석에 "바닐라 RecipeManager로 제작법을 가져오라"고 적혀 있었는데, 26.2에서는 <b>낡고 오해를 부르는</b> 내용이다:
     * 이 프로젝트는 12차부터 작업대 제작법을 모드가 직접 만든 {@code DataType.RECIPES} 경로로 다룬다
     * (26.2 클라이언트에는 전체 제작법 표가 없다). 바닐라 {@code RecipeManager}로는 예전 총기 팩의
     * {@code recipes/}(복수형) 폴더 제작법을 얻을 수 없고, 클라이언트에서는 아예 비어 있다.
     * 항상 {@code empty()}만 돌려주는 빈 껍데기를 남기면 호출하는 쪽이 "그 제작법이 없다"고 오해할 뿐이다.
     *
     */
    public static Optional<GunSmithTableRecipe> getRecipe(Identifier recipeId) {
        TableRecipe pojo = CommonAssetsManager.get().getTableRecipe(recipeId);
        if (pojo == null || pojo.getResult() == null) {
            return Optional.empty();
        }
        GunSmithTableRecipe recipe = new GunSmithTableRecipe(recipeId, pojo);
        recipe.init();
        return Optional.of(recipe);
    }

    public static Set<Map.Entry<Identifier, CommonBlockIndex>> getAllCommonBlockIndex() {
        return CommonAssetsManager.get().getAllBlocks();
    }

    public static Set<Map.Entry<Identifier, CommonGunIndex>> getAllCommonGunIndex() {
        return CommonAssetsManager.get().getAllGuns();
    }

    public static Set<Map.Entry<Identifier, CommonAmmoIndex>> getAllCommonAmmoIndex() {
        return CommonAssetsManager.get().getAllAmmos();
    }

    public static Set<Map.Entry<Identifier, CommonAttachmentIndex>> getAllCommonAttachmentIndex() {
        return CommonAssetsManager.get().getAllAttachments();
    }

    /**
     * 모든 작업대 제작법. {@link #getRecipe(Identifier)}와 출처가 같으며, 이유는 그 메서드 주석을 참고한다.
     */
    public static Map<Identifier, GunSmithTableRecipe> getAllRecipes() {
        Map<Identifier, GunSmithTableRecipe> result = new java.util.LinkedHashMap<>();
        for (Map.Entry<Identifier, TableRecipe> entry : CommonAssetsManager.get().getAllTableRecipes()) {
            TableRecipe pojo = entry.getValue();
            if (pojo == null || pojo.getResult() == null) {
                continue;
            }
            GunSmithTableRecipe recipe = new GunSmithTableRecipe(entry.getKey(), pojo);
            recipe.init();
            result.put(entry.getKey(), recipe);
        }
        return result;
    }

    public static void registerThirdPersonAnimation(String name, IThirdPersonAnimation animation) {
        ThirdPersonManager.register(name, animation);
    }
}
