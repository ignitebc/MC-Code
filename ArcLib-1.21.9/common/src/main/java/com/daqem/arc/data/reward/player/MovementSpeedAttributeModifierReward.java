package com.daqem.arc.data.reward.player;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.action.result.ActionResult;
import com.daqem.arc.api.reward.AbstractReward;
import com.daqem.arc.api.reward.serializer.IRewardSerializer;
import com.daqem.arc.api.reward.type.IRewardType;
import com.daqem.arc.api.reward.type.RewardType;
import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class MovementSpeedAttributeModifierReward extends AbstractReward {

    private final float multiplierPercent;

    public MovementSpeedAttributeModifierReward(double chance, int priority, float multiplierPercent) {
        super(chance, priority);
        this.multiplierPercent = multiplierPercent;
    }

    public float getMultiplierPercent() {
        return multiplierPercent;
    }

    /**
     * (ADD_MULTIPLIED_TOTAL 기준)
     * 0.01 = +1%
     */
    public double getAttributeAmount() {
        return (double) multiplierPercent / 100.0D;
    }

    /**
     * 구버전 호환/참고용 UUID (현재 1.21.10+에서는 AttributeModifier 식별자로 사용하지 않음)
     */
    public static UUID computeModifierUuid(ResourceLocation holderId, ResourceLocation actionId) {
        String key = "arc:movement_speed_modifier:" + holderId + ":" + actionId;
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 1.21.10+ 기준 AttributeModifier 식별자는 ResourceLocation 입니다.
     * 신규 코드는 이 id를 사용합니다.
     */
    public static ResourceLocation computeModifierId(ResourceLocation holderId, ResourceLocation actionId) {
        // path는 [a-z0-9/._-] 허용. namespace/path 조합도 안전하게 변환합니다.
        String holderKey = sanitizeForPath(holderId.getNamespace() + "_" + holderId.getPath());
        String actionKey = sanitizeForPath(actionId.getNamespace() + "_" + actionId.getPath());

        String path = "movement_speed/" + holderKey + "/" + actionKey;

        // 1.21.10에서는 생성자 대신 팩토리 메서드 사용
        return ResourceLocation.fromNamespaceAndPath("arc", path);
    }

    private static String sanitizeForPath(String s) {
        // 허용 문자 외는 '_'로 치환 (슬래시는 경로 구분용으로 유지하지 않고 '_'로 바꿔도 무방)
        // 여기서는 경로 안정성을 위해 '/'도 '_'로 치환합니다.
        String lower = s.toLowerCase();
        return lower
                .replace(':', '_')
                .replace('/', '_')
                .replace('\\', '_')
                .replace(' ', '_');
    }

    /**
     * 구버전 호환/디버그용 name (현재 1.21.10+에서는 AttributeModifier에 name이 없음)
     */
    public static String computeModifierName(ResourceLocation holderId, ResourceLocation actionId) {
        return "arc_movement_speed:" + holderId + ":" + actionId;
    }

    @Override
    public IRewardType<?> getType() {
        return RewardType.MOVEMENT_SPEED_ATTRIBUTE_MODIFIER;
    }

    @Override
    public ActionResult apply(ActionData actionData) {
        // 실제 적용은 서버 tick 동기화에서 처리
        return new ActionResult();
    }

    @Override
    public Component getName() {
        return Component.literal("Movement Speed Modifier");
    }

    @Override
    public Component getDescription(Object... args) {
        return Component.literal("Increases movement speed by " + multiplierPercent + "% (AttributeModifier)");
    }

    public static class Serializer implements IRewardSerializer<MovementSpeedAttributeModifierReward> {

        @Override
        public MovementSpeedAttributeModifierReward fromJson(JsonObject jsonObject, double chance, int priority) {
            float multiplier = GsonHelper.getAsFloat(jsonObject, "multiplier");
            return new MovementSpeedAttributeModifierReward(chance, priority, multiplier);
        }

        @Override
        public MovementSpeedAttributeModifierReward fromNetwork(RegistryFriendlyByteBuf friendlyByteBuf, double chance, int priority) {
            return new MovementSpeedAttributeModifierReward(chance, priority, friendlyByteBuf.readFloat());
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, MovementSpeedAttributeModifierReward type) {
            IRewardSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeFloat(type.multiplierPercent);
        }
    }
}
