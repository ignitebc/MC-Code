package com.daqem.jobsplus.client.gunguide;

import com.daqem.jobsplus.JobsPlus;
import dev.architectury.platform.Platform;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Read-only optional integration: JobsPlus still loads when TACZ is absent. */
public final class TaczCatalog {
    public enum Kind {
        GUN("총기", "Gun", "IGun", "getGunId"),
        AMMO("탄약", "Ammo", "IAmmo", "getAmmoId"),
        ATTACHMENT("조준경 · 파츠", "Attachment", "IAttachment", "getAttachmentId");

        public final String label;
        private final String apiName;
        private final String itemInterface;
        private final String idMethod;

        Kind(String label, String apiName, String itemInterface, String idMethod) {
            this.label = label;
            this.apiName = apiName;
            this.itemInterface = itemInterface;
            this.idMethod = idMethod;
        }
    }

    public record Material(Ingredient ingredient, int count) { }
    public record Recipe(Identifier id, int outputCount, List<Material> materials) { }
    public record Link(String label, String target) { }
    public record Entry(String key, Kind kind, Identifier id, ItemStack icon, Component name,
                        List<String> description, List<Recipe> recipes, List<Link> links) { }
    public record Snapshot(List<Entry> entries, String message) { }

    private TaczCatalog() { }

    public static Snapshot load() {
        if (!Platform.isModLoaded("tacz")) {
            return new Snapshot(List.of(), "TACZ가 설치되어 있지 않습니다.");
        }
        try {
            return readCatalog();
        } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            JobsPlus.LOGGER.error("Unable to load TACZ guide data", exception);
            return new Snapshot(List.of(), "총기 도감 정보를 불러오지 못했습니다. 다시 열어 주세요.");
        }
    }

    private static Snapshot readCatalog() throws ReflectiveOperationException {
        Class<?> api = Class.forName("com.tacz.guns.api.TimelessAPI");
        Map<String, List<Recipe>> recipes = readRecipes(api);
        Map<String, Entry> entries = new LinkedHashMap<>();
        Map<String, Object> gunData = new LinkedHashMap<>();
        Map<String, Object> attachmentTypes = new LinkedHashMap<>();
        for (Kind kind : Kind.values()) {
            Iterable<?> indexes = (Iterable<?>) api.getMethod("getAllClient" + kind.apiName + "Index").invoke(null);
            for (Object value : indexes) {
                Map.Entry<?, ?> index = (Map.Entry<?, ?>) value;
                Identifier id = (Identifier) index.getKey();
                Object client = index.getValue();
                Optional<?> common = (Optional<?>) api.getMethod("getCommon" + kind.apiName + "Index", Identifier.class).invoke(null, id);
                if (common.isEmpty()) {
                    continue;
                }
                String key = key(kind, id);
                ItemStack icon = build(kind, id);
                if (icon.isEmpty()) {
                    continue;
                }
                List<String> description = new ArrayList<>();
                List<Link> links = new ArrayList<>();
                if (kind == Kind.GUN) {
                    Object data = call(common.get(), "getGunData");
                    gunData.put(key, data);
                    description.add("기본 장탄수: " + call(data, "getAmmoAmount"));
                    description.add("발사 방식: " + ((List<?>) call(data, "getFireModeSet")).stream()
                            .map(mode -> TaczStats.modeName(mode.toString())).collect(java.util.stream.Collectors.joining(" / ")));
                    TaczStats.gun(description, data);
                    Object display = call(client, "getDefaultDisplay");
                    if (display != null) {
                        description.add("기본 조준 배율: " + call(display, "getIronZoom") + "×");
                    }
                    links.add(new Link("사용 탄환", key(Kind.AMMO, (Identifier) call(data, "getAmmoId"))));
                } else if (kind == Kind.ATTACHMENT) {
                    TaczStats.attachment(description, call(common.get(), "getData"));
                    if (description.isEmpty()) {
                        description.add("추가 능력치 보정 없음");
                    }
                    Object type = call(common.get(), "getType");
                    attachmentTypes.put(key, type);
                    description.add("슬롯: " + slotName(type));
                    float[] zoom = (float[]) call(client, "getZoom");
                    if (zoom != null && zoom.length > 0) {
                        List<String> magnifications = new ArrayList<>();
                        for (float amount : zoom) {
                            magnifications.add(amount + "×");
                        }
                        description.add("조준 배율: " + String.join(" / ", magnifications));
                    }
                    if (Boolean.TRUE.equals(call(call(common.get(), "getPojo"), "isHidden"))) {
                        description.add("숨김 부품 · 일반 목록에서 획득 불가");
                    }
                }
                entries.put(key, new Entry(key, kind, id, icon,
                        Component.translatable((String) call(client, "getName")), description,
                        recipes.getOrDefault(key, List.of()), links));
            }
        }
        Class<?> gunInterface = Class.forName("com.tacz.guns.api.item.IGun");
        Method allow = gunInterface.getMethod("allowAttachment", ItemStack.class, ItemStack.class);
        for (Entry gun : entries.values()) {
            if (gun.kind() != Kind.GUN) {
                continue;
            }
            Object data = gunData.get(gun.key());
            List<?> slots = (List<?>) call(data, "getAllowAttachments");
            for (Entry part : entries.values()) {
                if (part.kind() != Kind.ATTACHMENT || slots == null || !slots.contains(attachmentTypes.get(part.key()))) {
                    continue;
                }
                if (Boolean.TRUE.equals(allow.invoke(gun.icon().getItem(), gun.icon(), part.icon()))) {
                    gun.links().add(new Link(slotName(attachmentTypes.get(part.key())), part.key()));
                    part.links().add(new Link("호환 총기", gun.key()));
                    Map<?, ?> exclusive = (Map<?, ?>) call(data, "getExclusiveAttachments");
                    Object override = exclusive.get(part.id());
                    if (override != null) {
                        gun.description().add("전용 파츠 효과 · " + part.name().getString());
                        TaczStats.attachment(gun.description(), override);
                        part.description().add("전용 효과 · " + gun.name().getString() + " 장착 시");
                        TaczStats.attachment(part.description(), override);
                    }
                }
            }
            Map<?, ?> builtIns = (Map<?, ?>) call(data, "getBuiltInAttachments");
            if (builtIns != null) {
                for (Object id : builtIns.values()) {
                    gun.links().add(new Link("기본 내장", key(Kind.ATTACHMENT, (Identifier) id)));
                }
            }
            Entry ammo = entries.get(key(Kind.AMMO, (Identifier) call(data, "getAmmoId")));
            if (ammo != null) {
                ammo.links().add(new Link("사용 총기", gun.key()));
            }
        }
        List<Entry> result = new ArrayList<>(entries.values());
        result.sort(Comparator.comparing(Entry::kind).thenComparing(entry -> entry.name().getString()));
        return new Snapshot(List.copyOf(result), result.isEmpty() ? "총기 팩 데이터가 없습니다. 로딩 후 다시 열어 주세요." : "");
    }

    private static Map<String, List<Recipe>> readRecipes(Class<?> api) throws ReflectiveOperationException {
        Map<String, List<Recipe>> result = new LinkedHashMap<>();
        Map<?, ?> recipes = (Map<?, ?>) api.getMethod("getAllRecipes").invoke(null);
        for (Map.Entry<?, ?> recipe : recipes.entrySet()) {
            ItemStack output = (ItemStack) call(recipe.getValue(), "getOutput");
            for (Kind kind : Kind.values()) {
                Class<?> itemInterface = Class.forName("com.tacz.guns.api.item." + kind.itemInterface);
                if (!itemInterface.isInstance(output.getItem())) {
                    continue;
                }
                Identifier id = (Identifier) itemInterface.getMethod(kind.idMethod, ItemStack.class).invoke(output.getItem(), output);
                List<Material> materials = new ArrayList<>();
                for (Object input : (List<?>) call(recipe.getValue(), "getInputs")) {
                    materials.add(new Material((Ingredient) call(input, "getIngredient"), (int) call(input, "getCount")));
                }
                result.computeIfAbsent(key(kind, id), ignored -> new ArrayList<>())
                        .add(new Recipe((Identifier) recipe.getKey(), output.getCount(), List.copyOf(materials)));
                break;
            }
        }
        result.values().forEach(list -> list.sort(Comparator.comparing(recipe -> recipe.id().toString())));
        return result;
    }

    private static ItemStack build(Kind kind, Identifier id) throws ReflectiveOperationException {
        Class<?> type = Class.forName("com.tacz.guns.api.item.builder." + kind.apiName + "ItemBuilder");
        Object builder = type.getMethod("create").invoke(null);
        type.getMethod("setId", Identifier.class).invoke(builder, id);
        return ((ItemStack) type.getMethod("build").invoke(builder)).copy();
    }

    private static Object call(Object target, String method) throws ReflectiveOperationException {
        return target.getClass().getMethod(method).invoke(target);
    }

    private static String key(Kind kind, Identifier id) {
        return kind.name() + ":" + id;
    }

    private static String slotName(Object type) {
        return switch (type.toString()) {
            case "SCOPE" -> "조준경";
            case "MUZZLE" -> "총구";
            case "GRIP" -> "손잡이";
            case "STOCK" -> "개머리판";
            case "LASER" -> "레이저";
            case "EXTENDED_MAG" -> "탄창 · 특수탄";
            default -> type.toString();
        };
    }
}
