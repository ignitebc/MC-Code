package com.daqem.itemrestrictions.data;

public enum RestrictionType {

    CRAFT("inventory.cant_craft"),
    SMELT("inventory.cant_smelt"),
    BREW("inventory.cant_brew"),
    ENCHANT("inventory.cant_enchant"),
    REPAIR("inventory.cant_repair"),
    USE_ITEM("inventory.cant_use_item"),
    BREAK_BLOCK("inventory.cant_break_block"),
    ITEM_BREAK_BLOCK("inventory.cant_item_break_block"),
    PLACE_BLOCK("inventory.cant_place_block"),
    HURT_ENTITY("inventory.cant_hurt_entity"),
    NONE("");

    private final String translationKey;

    RestrictionType(String translationKey) {
        this.translationKey = translationKey;
    }

    public String getTranslationKey() {
        return translationKey;
    }
}
