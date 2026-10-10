package com.tacz.guns.inventory.tooltip;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public class AmmoBoxTooltip implements TooltipComponent {
    private final ItemStack ammoBox;
    private final ItemStack ammo;
    private final int count;
    private final int capacity;

    public AmmoBoxTooltip(ItemStack ammoBox, ItemStack ammo, int count, int capacity) {
        this.ammoBox = ammoBox;
        this.ammo = ammo;
        this.count = count;
        this.capacity = capacity;
    }

    public ItemStack getAmmoBox() {
        return ammoBox;
    }

    public ItemStack getAmmo() {
        return ammo;
    }

    public int getCount() {
        return count;
    }

    public int getCapacity() {
        return capacity;
    }
}
