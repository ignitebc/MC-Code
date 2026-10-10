package com.tacz.guns.resource.pojo.data.block;

import cn.sh1rocu.tacz.util.forge.CraftingHelper;
import com.google.gson.*;
import com.tacz.guns.GunMod;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.api.item.builder.AttachmentItemBuilder;
import com.tacz.guns.api.item.builder.GunItemBuilder;
import com.tacz.guns.init.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Supplier;

/**
 * 26.2 수정: icon을 지연 로드 Supplier로 바꿨다.
 * 이유: MC 26.2에서 new ItemStack(item, count)는 item의 Holder.Reference.components가 bind되어 있어야 한다.
 * 총기 팩 크리에이티브 탭 icon은 자원 다시 불러오기(apply) 단계에서 해석하는데, 이때는 컴포넌트가 아직 bind되지 않아 바로 만들면
 * "Components not bound yet"을 던져 서버/클라이언트가 월드에 들어갈 때 충돌한다.
 * 이제 해석 단계에서는 원본 JSON만 잡아 두고, ItemStack 생성은 GUI 실행 시점(그때는 컴포넌트가 bind됨)으로 미룬다.
 */
public record TabConfig(Identifier id, String name, Supplier<ItemStack> icon) {
    public static final Identifier TAB_AMMO = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "ammo");

    public static final Identifier TAB_PISTOL = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "pistol");
    public static final Identifier TAB_SNIPER = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "sniper");
    public static final Identifier TAB_RIFLE = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "rifle");
    public static final Identifier TAB_SHOTGUN = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "shotgun");
    public static final Identifier TAB_SMG = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "smg");
    public static final Identifier TAB_RPG = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "rpg");
    public static final Identifier TAB_MG = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "mg");

    public static final Identifier TAB_SCOPE = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "scope");
    public static final Identifier TAB_MUZZLE = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "muzzle");
    public static final Identifier TAB_STOCK = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "stock");
    public static final Identifier TAB_GRIP = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "grip");
    public static final Identifier TAB_EXTENDED_MAG = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "extended_mag");
    public static final Identifier TAB_LASER = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "laser");

    public static final Identifier TAB_MISC = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "misc");
    public static final Identifier TAB_EMPTY = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "empty");

    /**
     * 팩이 탭을 적지 않았거나 작업대 제한을 끈 경우에 쓰는 기본 분류. 기본 총기팩의 총기 작업대 탭과 같은 구성이다.
     * 탄약 제작법은 구경별 세부 분류를 쓰므로 그 분류들도 함께 둔다.
     */
    public static final List<TabConfig> DEFAULT_TABS = List.of(
            new TabConfig(TabConfig.TAB_PISTOL, "tacz.type.pistol.name", () -> GunItemBuilder.create().setId(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "p18c")).forceBuild()),
            new TabConfig(TabConfig.TAB_SNIPER, "tacz.type.sniper.name", () -> GunItemBuilder.create().setId(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "ai_awp")).forceBuild()),
            new TabConfig(TabConfig.TAB_RIFLE, "tacz.type.rifle.name", () -> GunItemBuilder.create().setId(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "ak47")).forceBuild()),
            new TabConfig(TabConfig.TAB_SHOTGUN, "tacz.type.shotgun.name", () -> GunItemBuilder.create().setId(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "sawed_off")).forceBuild()),
            new TabConfig(TabConfig.TAB_SMG, "tacz.type.smg.name", () -> GunItemBuilder.create().setId(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "mp5k")).forceBuild()),
            new TabConfig(TabConfig.TAB_RPG, "tacz.type.rpg.name", () -> GunItemBuilder.create().setId(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "rpg7")).forceBuild()),
            new TabConfig(TabConfig.TAB_MG, "tacz.type.mg.name", () -> GunItemBuilder.create().setId(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "m249")).forceBuild()),
            new TabConfig(TabConfig.TAB_SCOPE, "tacz.type.scope.name", () -> AttachmentItemBuilder.create().setId(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "scope_acog_ta31")).build()),
            new TabConfig(TabConfig.TAB_MUZZLE, "tacz.type.muzzle.name", () -> AttachmentItemBuilder.create().setId(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "muzzle_compensator_trident")).build()),
            new TabConfig(TabConfig.TAB_STOCK, "tacz.type.stock.name", () -> AttachmentItemBuilder.create().setId(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "stock_militech_b5")).build()),
            new TabConfig(TabConfig.TAB_GRIP, "tacz.type.grip.name", () -> AttachmentItemBuilder.create().setId(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "grip_magpul_afg_2")).build()),
            new TabConfig(TabConfig.TAB_EXTENDED_MAG, "tacz.type.extended_mag.name", () -> AttachmentItemBuilder.create().setId(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "extended_mag_3")).build()),
            new TabConfig(TabConfig.TAB_LASER, "tacz.type.laser.name", () -> AttachmentItemBuilder.create().setId(Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "laser_compact")).build()),
            new TabConfig(TabConfig.TAB_AMMO, "tacz.type.ammo.name", () -> AmmoItemBuilder.create().setId(DefaultAssets.DEFAULT_AMMO_ID).build()),
            new TabConfig(taczId("pd_cartridges"), "tacz.type.pd_cartridges.name", () -> AmmoItemBuilder.create().setId(taczId("9mm")).build()),
            new TabConfig(taczId("ifp_rifle_cartridges"), "tacz.type.ifp_rifle_cartridges.name", () -> AmmoItemBuilder.create().setId(taczId("556x45")).build()),
            new TabConfig(taczId("lc_specialized"), "tacz.type.lc_specialized.name", () -> AmmoItemBuilder.create().setId(taczId("50bmg")).build()),
            new TabConfig(taczId("explosives"), "tacz.type.explosives.name", () -> AmmoItemBuilder.create().setId(taczId("rpg_rocket")).build()),
            new TabConfig(taczId("shotgun_shells"), "tacz.type.shotgun_shells.name", () -> AmmoItemBuilder.create().setId(taczId("12g")).build()),
            new TabConfig(TabConfig.TAB_MISC, "tacz.type.misc.name", () -> ModItems.GUN_SMITH_TABLE.getDefaultInstance())
    );

    private static Identifier taczId(String path) {
        return Identifier.fromNamespaceAndPath(GunMod.MOD_ID, path);
    }

    public static class Deserializer implements JsonDeserializer<TabConfig> {
        @Override
        public TabConfig deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (!json.isJsonObject()) {
                throw new JsonParseException("TabConfig must be a JSON object");
            }
            JsonObject object = json.getAsJsonObject();
            if (!object.has("id") || !object.get("id").isJsonPrimitive()) {
                throw new JsonParseException("TabConfig must have an id");
            }
            Identifier id = context.deserialize(object.get("id"), Identifier.class);
            // 원본 JSON만 잡아 두고 다시 불러오기 단계에서는 ItemStack을 만들지 않는다("Components not bound yet" 방지)
            final JsonObject iconObj = object.has("icon") && object.get("icon").isJsonObject()
                    ? object.getAsJsonObject("icon") : null;
            Supplier<ItemStack> icon = () -> {
                if (iconObj == null) {
                    return ItemStack.EMPTY;
                }
                try {
                    return CraftingHelper.getItemStack(iconObj, true);
                } catch (Exception e) {
                    // 실행 중에도 실패하면(예: 총기 팩 icon이 없는 아이템을 참조) 빈 스택으로 대체해 GUI 충돌을 막는다
                    GunMod.LOGGER.error("Failed to build tab icon for {}", id, e);
                    return ItemStack.EMPTY;
                }
            };
            String name = GsonHelper.getAsString(object, "name", "tacz.type.unknown.name");
            return new TabConfig(id, name, icon);
        }
    }

    @NotNull
    public Component getName() {
        return Component.translatable(name == null ? "tacz.type.unknown.name" : name);
    }
}
