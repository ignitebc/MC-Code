package com.daqem.jobsplus.networking.s2c;

import com.daqem.jobsplus.networking.JobsPlusNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

/**
 * 직업 화면(J키 메뉴)에서 일어난 일의 결과를 채팅이 아닌 모달 알림 창으로 전달한다.
 * <p>
 * 화면을 열어 둔 상태에서 채팅으로 알리면 화면 뒤에 가려 놓치기 쉬우므로,
 * 상점, 직업 선택, 스킬 구매, 주식 거래의 결과는 모두 이 패킷으로 보낸다.
 * <p>
 * 문구는 문자열이 아니라 {@link Component}로 보낸다. 번역 키는 클라이언트에서 풀어야
 * 서버에 없는 모드 언어 파일도 정상적으로 표시되기 때문이다.
 */
public class ClientboundAlertPacket implements CustomPacketPayload
{
    private static final Component DEFAULT_BUTTON_MESSAGE = Component.literal("닫기");

    private final Component message;
    private final Component buttonMessage;

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundAlertPacket> STREAM_CODEC =
            new StreamCodec<>()
            {
                @Override
                public @NotNull ClientboundAlertPacket decode(RegistryFriendlyByteBuf buffer)
                {
                    Component message = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buffer);
                    Component buttonMessage = ComponentSerialization.TRUSTED_STREAM_CODEC.decode(buffer);
                    return new ClientboundAlertPacket(message, buttonMessage);
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, ClientboundAlertPacket packet)
                {
                    ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buffer, packet.message);
                    ComponentSerialization.TRUSTED_STREAM_CODEC.encode(buffer, packet.buttonMessage);
                }
            };

    public ClientboundAlertPacket(String message)
    {
        this(Component.literal(message), DEFAULT_BUTTON_MESSAGE);
    }

    public ClientboundAlertPacket(String message, String buttonMessage)
    {
        this(Component.literal(message), Component.literal(buttonMessage));
    }

    public ClientboundAlertPacket(Component message)
    {
        this(message, DEFAULT_BUTTON_MESSAGE);
    }

    public ClientboundAlertPacket(Component message, Component buttonMessage)
    {
        this.message = message;
        this.buttonMessage = buttonMessage;
    }

    public Component getMessage()
    {
        return message;
    }

    public Component getButtonMessage()
    {
        return buttonMessage;
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type()
    {
        return JobsPlusNetworking.CLIENTBOUND_ALERT;
    }
}
