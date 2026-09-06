package com.tacz.guns.client.renderer.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.tacz.guns.GunMod;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.item.IAmmoBox;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * 弹药盒外观变体的 {@code select} 属性，对应上游 1.21.1 的
 * {@code ItemProperties.register(AmmoBoxItem.PROPERTY_NAME, AmmoBoxItem::getStatue)}。
 *
 * <h2>为什么必须自定义，不能用原版的 {@code minecraft:component}</h2>
 * 26.2 内置的 {@code select} 属性里，唯一能读物品数据的是
 * {@code minecraft:component}，它的实现就一行：
 * <pre>
 * return stack.get(this.componentType);   // ComponentContents#get
 * </pre>
 * 也就是只能<b>整体</b>取出某个组件、再与 {@code when} 里的字面量比较。
 * 而弹药盒的状态（等级 / 开合）是塞在
 * {@code DataComponents.CUSTOM_DATA} 这一个组件<b>内部的若干 NBT 字段</b>里
 * （见 {@code AmmoBoxItemDataAccessor}：{@code Level} / {@code AmmoId} /
 * {@code AmmoCount}），
 * 且最终外观还要由这些字段<b>组合运算</b>得出。
 * 原版属性没有任何一个能表达「读组件内部字段并做运算」，因此必须自建。
 *
 * <h2>取值语义与上游逐位对齐</h2>
 * 直接移植 {@code AmmoBoxItem#getStatue} 的算法（上游返回 float，这里返回 int，
 * 因为 {@code select} 按值精确匹配，用整数更稳妥）：
 * <pre>
 * 普通                              -> 2 * 等级 + 开合(0/1)
 * 开合：ammoId 为空 或 数量 &lt;= 0 记 0（open），否则记 1（close）
 * </pre>
 * 于是 0..5 正好对应 {@code models/item/ammo_box/} 下现成的 6 个变体模型，
 * 与旧 {@code overrides} 格式里的 {@code tacz:ammo_statue} 谓词一一对应。
 *
 * <h2>为什么不再走自定义渲染器</h2>
 * 此前的 {@code AmmoBoxItemRenderer} 把 128×128 的<b>3D 模型 UV 展开图</b>
 * （{@code textures/item/ammo_box.png}）当成平面图标，贴在 {@code SlotModel}
 * 那个 16×16 的四边形上。那张图只有左上角 69×69 像素非透明、且是六面展开的碎片，
 * 拉伸到 16×16 后既不是图标也不是模型，物品栏与手持看到的都是错乱的色块。
 * 上游<b>从来没有</b>这个渲染器 —— 这里使用 6 个普通弹药盒 JSON 模型交给原版渲染。
 * 本类补上 26.2 缺失的那一环（属性注册），让弹药盒回到原版渲染路径。
 */
public record AmmoBoxStatueProperty() implements SelectItemModelProperty<Integer> {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(GunMod.MOD_ID, "ammo_statue");

    public static final MapCodec<AmmoBoxStatueProperty> MAP_CODEC =
            MapCodec.unit(new AmmoBoxStatueProperty());

    public static final SelectItemModelProperty.Type<AmmoBoxStatueProperty, Integer> TYPE =
            SelectItemModelProperty.Type.create(MAP_CODEC, Codec.INT);

    /** 盒盖打开（没装弹药）。 */
    private static final int OPEN = 0;
    /** 盒盖关闭（装了弹药）。 */
    private static final int CLOSE = 1;

    @Override
    @Nullable
    public Integer get(ItemStack stack,
                       @Nullable ClientLevel level,
                       @Nullable LivingEntity entity,
                       int seed,
                       ItemDisplayContext displayContext) {
        if (!(stack.getItem() instanceof IAmmoBox iAmmoBox)) {
            return null;
        }
        int openStatue = getOpenStatue(stack, iAmmoBox);
        return openStatue + 2 * iAmmoBox.getAmmoLevel(stack);
    }

    private static int getOpenStatue(ItemStack stack, IAmmoBox iAmmoBox) {
        boolean idIsEmpty = iAmmoBox.getAmmoId(stack).equals(DefaultAssets.EMPTY_AMMO_ID);
        boolean countIsZero = iAmmoBox.getAmmoCount(stack) <= 0;
        return (idIsEmpty || countIsZero) ? OPEN : CLOSE;
    }

    @Override
    public Codec<Integer> valueCodec() {
        return Codec.INT;
    }

    @Override
    public SelectItemModelProperty.Type<AmmoBoxStatueProperty, Integer> type() {
        return TYPE;
    }
}
