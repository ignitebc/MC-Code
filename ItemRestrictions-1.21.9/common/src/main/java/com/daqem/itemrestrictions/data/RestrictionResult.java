package com.daqem.itemrestrictions.data;

import com.daqem.itemrestrictions.ItemRestrictions;
import net.minecraft.network.chat.MutableComponent;

import java.util.ArrayList;
import java.util.List;

public class RestrictionResult {

    private final List<RestrictionType> restrictedBy;

    public RestrictionResult() {
        restrictedBy = new ArrayList<>();
    }

    public RestrictionResult(List<RestrictionType> restrictedBy) {
        this.restrictedBy = restrictedBy;
    }

    public RestrictionResult merge(RestrictionResult other) {
        for (RestrictionType type : other.restrictedBy) {
            if (!this.restrictedBy.contains(type)) {
                this.restrictedBy.add(type);
            }
        }
        return this;
    }

    public boolean isRestricted() {
        return !this.restrictedBy.isEmpty();
    }

    public boolean isRestricted(RestrictionType restrictionType) {
        return this.restrictedBy.contains(restrictionType);
    }

    @SuppressWarnings("unused")
    public List<RestrictionType> getRestrictedBy() {
        return restrictedBy;
    }

    @SuppressWarnings("unused")
    public MutableComponent getMessage() {
        return ItemRestrictions.translatable("inventory.restricted", this.restrictedBy.size());
    }
}
