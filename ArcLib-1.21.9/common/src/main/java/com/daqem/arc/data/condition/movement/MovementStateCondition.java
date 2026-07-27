package com.daqem.arc.data.condition.movement;

import com.daqem.arc.api.action.data.ActionData;
import com.daqem.arc.api.condition.AbstractCondition;
import com.daqem.arc.api.condition.serializer.IConditionSerializer;
import com.daqem.arc.api.condition.type.ConditionType;
import com.daqem.arc.api.condition.type.IConditionType;
import com.daqem.arc.api.player.ArcServerPlayer;
import com.google.gson.JsonObject;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import java.util.Locale;

/**
 * 플레이어가 특정 이동 상태인지 검사한다.
 * <p>
 * 이동 관련 액션은 서로 겹쳐서 발동한다. 예를 들어 달리는 동안에는 걷기 누적 거리도 함께 늘어나기
 * 때문에 {@code arc:on_walk}와 {@code arc:on_sprint}가 같은 틱에 모두 발동한다. 이동 거리로 보상을
 * 줄 때 중복 지급을 막으려면 "더 빠른 이동 상태가 아닐 때만" 이라는 조건이 필요해서 추가했다.
 * <p>
 * 사용 예 (걷기 보상을 달리기·승마·활공 중에는 주지 않음):
 * <pre>
 * { "type": "arc:movement_state", "state": "sprinting", "inverted": true }
 * </pre>
 */
public class MovementStateCondition extends AbstractCondition
{
    public enum State
    {
        WALKING,
        SPRINTING,
        SWIMMING,
        CROUCHING,
        ELYTRA_FLYING,
        HORSE_RIDING
    }

    private final State state;

    public MovementStateCondition(boolean inverted, State state)
    {
        super(inverted);
        this.state = state;
    }

    public State getState()
    {
        return state;
    }

    @Override
    public boolean isMet(ActionData actionData)
    {
        if (!(actionData.getPlayer() instanceof ArcServerPlayer serverPlayer))
        {
            return false;
        }

        return switch (this.state)
        {
            case WALKING -> serverPlayer.arc$isWalking();
            case SPRINTING -> serverPlayer.arc$isSprinting();
            case SWIMMING -> serverPlayer.arc$isSwimming();
            case CROUCHING -> serverPlayer.arc$isCrouching();
            case ELYTRA_FLYING -> serverPlayer.arc$isElytraFlying();
            case HORSE_RIDING -> serverPlayer.arc$isHorseRiding();
        };
    }

    @Override
    public Component getDescription()
    {
        return getDescription(this.state.name().toLowerCase(Locale.ROOT));
    }

    @Override
    public IConditionType<?> getType()
    {
        return ConditionType.MOVEMENT_STATE;
    }

    public static class Serializer implements IConditionSerializer<MovementStateCondition>
    {

        @Override
        public MovementStateCondition fromJson(ResourceLocation location, JsonObject jsonObject, boolean inverted)
        {
            String stateName = GsonHelper.getAsString(jsonObject, "state");
            return new MovementStateCondition(inverted, parseState(location, stateName));
        }

        @Override
        public MovementStateCondition fromNetwork(ResourceLocation location, RegistryFriendlyByteBuf friendlyByteBuf,
                                                  boolean inverted)
        {
            return new MovementStateCondition(inverted, friendlyByteBuf.readEnum(State.class));
        }

        @Override
        public void toNetwork(RegistryFriendlyByteBuf friendlyByteBuf, MovementStateCondition type)
        {
            IConditionSerializer.super.toNetwork(friendlyByteBuf, type);
            friendlyByteBuf.writeEnum(type.state);
        }

        private static State parseState(ResourceLocation location, String stateName)
        {
            try
            {
                return State.valueOf(stateName.toUpperCase(Locale.ROOT));
            }
            catch (IllegalArgumentException e)
            {
                throw new IllegalArgumentException(
                        "Unknown movement state '" + stateName + "' in condition " + location);
            }
        }
    }
}
