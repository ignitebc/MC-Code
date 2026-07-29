package com.daqem.jobsplus.networking.s2c;

import com.daqem.jobsplus.networking.JobsPlusNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public class ClientboundStockAlertPacket implements CustomPacketPayload
{
    private final String message;
    private final String buttonMessage;

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundStockAlertPacket> STREAM_CODEC =
            new StreamCodec<>()
            {
                @Override
                public @NotNull ClientboundStockAlertPacket decode(RegistryFriendlyByteBuf buffer)
                {
                    return new ClientboundStockAlertPacket(buffer.readUtf(), buffer.readUtf());
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buffer, ClientboundStockAlertPacket packet)
                {
                    buffer.writeUtf(packet.message);
                    buffer.writeUtf(packet.buttonMessage);
                }
            };

    public ClientboundStockAlertPacket(String message)
    {
        this(message, "닫기");
    }

    public ClientboundStockAlertPacket(String message, String buttonMessage)
    {
        this.message = message;
        this.buttonMessage = buttonMessage;
    }

    public String getMessage()
    {
        return message;
    }

    public String getButtonMessage()
    {
        return buttonMessage;
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type()
    {
        return JobsPlusNetworking.CLIENTBOUND_STOCK_ALERT;
    }
}
