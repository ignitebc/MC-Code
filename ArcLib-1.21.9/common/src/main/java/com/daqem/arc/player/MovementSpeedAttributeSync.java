package com.daqem.arc.player;

import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.arc.api.player.ArcPlayer;
import com.daqem.arc.api.reward.IReward;
import com.daqem.arc.data.reward.player.MovementSpeedAttributeModifierReward;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.*;

public final class MovementSpeedAttributeSync {

    private MovementSpeedAttributeSync() {}

    public static void sync(ArcPlayer arcPlayer) {
        if (arcPlayer == null) return;
        if (!(arcPlayer.arc$getPlayer() instanceof ServerPlayer serverPlayer)) return;

        AttributeInstance instance = serverPlayer.getAttribute(Attributes.MOVEMENT_SPEED);
        if (instance == null) return;

        // 1.21.10+ (official mappings) 기준: AttributeModifier 식별자는 ResourceLocation 입니다.
        Map<ResourceLocation, AttributeModifier> desired = new HashMap<>();

        for (IActionHolder holder : arcPlayer.arc$getActionHolders()) {
            if (holder == null) continue;

            holder.getActions().forEach(action -> {
                for (IReward reward : action.getRewards()) {
                    if (!(reward instanceof MovementSpeedAttributeModifierReward msReward)) continue;

                    ResourceLocation id = MovementSpeedAttributeModifierReward.computeModifierId(holder.getLocation(), action.getLocation());

                    // (ADD_MULTIPLIED_TOTAL) = 구버전 MULTIPLY_TOTAL 과 동일 계열(총합 곱연산)
                    AttributeModifier modifier = new AttributeModifier(
                            id,
                            msReward.getAttributeAmount(),
                            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                    );
                    desired.put(id, modifier);
                }
            });
        }

        // arc namespace의 movement_speed/* modifier 중 필요 없는 것은 제거
        List<ResourceLocation> toRemove = new ArrayList<>();
        for (AttributeModifier existing : instance.getModifiers()) {
            ResourceLocation id = existing.id();
            if (id != null && id.getNamespace().equals("arc") && id.getPath().startsWith("movement_speed/")) {
                if (!desired.containsKey(id)) {
                    toRemove.add(id);
                }
            }
        }
        for (ResourceLocation id : toRemove) {
            instance.removeModifier(id);
        }

        // 필요한 modifier는 추가/갱신
        for (Map.Entry<ResourceLocation, AttributeModifier> e : desired.entrySet()) {
            ResourceLocation id = e.getKey();
            AttributeModifier want = e.getValue();

            AttributeModifier current = instance.getModifier(id);
            if (current == null) {
                instance.addPermanentModifier(want);
            } else {
                boolean same = current.operation() == want.operation()
                        && Double.compare(current.amount(), want.amount()) == 0;

                if (!same) {
                    instance.removeModifier(id);
                    instance.addPermanentModifier(want);
                }
            }
        }
    }
}
