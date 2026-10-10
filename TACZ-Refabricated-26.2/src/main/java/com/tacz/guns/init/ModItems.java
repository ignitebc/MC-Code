package com.tacz.guns.init;

import com.tacz.guns.GunMod;
import com.tacz.guns.api.item.gun.GunItemManager;
import com.tacz.guns.item.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;

public class ModItems {
    public static void init() {
        GunItemManager.registerGunItem(ModernKineticGunItem.TYPE_NAME, MODERN_KINETIC_GUN);
    }

    public static ModernKineticGunItem MODERN_KINETIC_GUN = register("modern_kinetic_gun", new ModernKineticGunItem(itemProps("modern_kinetic_gun")));


    public static Item AMMO = register("ammo", new AmmoItem(itemProps("ammo")));
    public static AttachmentItem ATTACHMENT = register("attachment", new AttachmentItem(itemProps("attachment")));

    public static GunSmithTableItem GUN_SMITH_TABLE = register("gun_smith_table", new DefaultTableItem(ModBlocks.GUN_SMITH_TABLE, blockItemProps("gun_smith_table")));

    public static Item TARGET = register("target", new BlockItem(ModBlocks.TARGET, blockItemProps("target")));
    public static Item STATUE = register("statue", new BlockItem(ModBlocks.STATUE, blockItemProps("statue")));
    public static Item AMMO_BOX = register("ammo_box", new AmmoBoxItem(itemProps("ammo_box")));
    public static Item TARGET_MINECART = register("target_minecart", new TargetMinecartItem(itemProps("target_minecart")));

    private static ResourceKey<Item> itemKey(String name) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(GunMod.MOD_ID, name));
    }

    private static Item.Properties itemProps(String name) {
        return new Item.Properties().setId(itemKey(name));
    }

    /**
     * 블록 아이템({@link BlockItem}과 그 하위 클래스) 전용 Properties.
     *
     * <h2>{@code useBlockDescriptionPrefix()}를 명시적으로 호출해야 하는 이유</h2>
     * 26.2 이전에는 {@code BlockItem}이 {@code getDescriptionId()}를 스스로 재정의해
     * 소속 블록의 {@code block.<ns>.<name>}을 바로 돌려줬으므로 등록할 때 아무것도 할 필요가 없었다.
     *
     * <p>26.2는 이 방식을 "<b>Properties에 선언하는</b>" 방식으로 바꿨다:
     * <ul>
     *   <li>{@code BlockItem}은 {@code getDescriptionId()}를 <b>더는 재정의하지 않고</b>(바이트코드 확인,
     *       이 클래스에 그 메서드가 없음) {@code Item#getDescriptionId}를 그대로 상속해
     *       생성할 때 계산해 둔 {@code descriptionId} 필드를 돌려준다;</li>
     *   <li>이 필드는 {@code Properties#effectiveDescriptionId()}에서 오며,
     *       접두사는 {@code Properties.descriptionId}라는 {@code DependantName}이 정하고
     *       <b>기본값은 {@code ITEM_DESCRIPTION_ID}</b>(곧 {@code item.} 접두사)다;</li>
     *   <li>{@code block.} 접두사를 얻으려면 반드시
     *       {@code useBlockDescriptionPrefix()}를 명시적으로 호출해 {@code BLOCK_DESCRIPTION_ID}로 바꿔야 한다.</li>
     * </ul>
     * 바닐라 자체의 {@code Items#registerBlock}도 바로 이렇게 한다(바이트코드 확인).
     *
     * <p>이식할 때 예전 표기({@code setId}만)를 그대로 써서 표적과 조각상의 이름이
     * {@code item.tacz.target} / {@code item.tacz.statue}가 되었다 — 언어 파일에는
     * {@code block.tacz.target} / {@code block.tacz.statue}만 있어 키가 맞지 않으니 원래 키 이름이 그대로 표시되었다.
     * 이는 원본 언어 파일과 같으므로(원본도 {@code block.} 쪽만 있음),
     * 올바른 수정은 언어 파일을 고치는 것이 아니라 코드를 26.2의 새 규약에 맞추는 것이다.
     *
     * <p>총기 작업대와 작업대 세 개도 {@code BlockItem} 하위 클래스이므로 함께 이 메서드로 바꿨다:
     * {@code gun_smith_table}의 {@code block.} 키는 원래 있었고(이전에는 마찬가지로 잘못 표시됨),
     * workbench 세 개는 원본과 이식본 모두 해당 키가 없다(총기 팩/원본 자체의 누락이라 이번 범위가 아님).
     */
    private static Item.Properties blockItemProps(String name) {
        return new Item.Properties().setId(itemKey(name)).useBlockDescriptionPrefix();
    }

    private static <T extends Item> T register(String name, T item) {
        return Registry.register(BuiltInRegistries.ITEM, Identifier.fromNamespaceAndPath(GunMod.MOD_ID, name), item);
    }
}