package com.daqem.itemrestrictions.mixin;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.itemrestrictions.data.ItemRestriction;
import com.daqem.itemrestrictions.data.ItemRestrictionManager;
import com.daqem.itemrestrictions.data.RestrictionResult;
import com.daqem.itemrestrictions.level.player.ItemRestrictionsServerPlayer;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;
import java.util.Optional;

@Mixin(ServerPlayer.class)
public abstract class MixinServerPlayer implements ItemRestrictionsServerPlayer {

    @Override
    public RestrictionResult itemrestrictions$isRestricted(ActionData actionData) {
        List<ItemRestriction> itemRestrictions = ItemRestrictionManager.getInstance().getItemRestrictions();
        Optional<RestrictionResult> optionalRestrictionResult = itemRestrictions.stream()
                .map(itemRestriction -> itemRestriction.isRestricted(actionData))
                .reduce((restrictionResult1, restrictionResult2) -> {
                    restrictionResult2.merge(restrictionResult1);
                    return restrictionResult2;
                });
        return optionalRestrictionResult.orElse(new RestrictionResult());
    }
}
