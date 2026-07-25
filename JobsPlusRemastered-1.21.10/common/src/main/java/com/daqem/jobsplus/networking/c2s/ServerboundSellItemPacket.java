package com.daqem.jobsplus.networking.c2s;

import com.daqem.jobsplus.JobsPlus;
import com.daqem.jobsplus.networking.JobsPlusNetworking;
import com.daqem.jobsplus.networking.s2c.ClientboundOpenJobsScreenPacket;
import com.daqem.jobsplus.player.JobsServerPlayer;
import com.daqem.jobsplus.shop.ShopOffer;
import dev.architectury.networking.NetworkManager;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * 상점 판매(C2S)
 *
 * - 입력 아이템/수량을 소비하고, 출력 아이템/수량을 지급한다.
 * - 인벤토리가 가득 찬 경우 바닥에 드랍한다.
 * - 1.21.x 레지스트리/인벤토리 API 변경 대응(Optional<Holder.Reference<Item>>).
 *
 * 핵심 수정:
 * - inv.add(outStack) -> inv.placeItemBackInInventory(outStack, true)
 *   (전용 서버에서 클라 인벤에 안 보이는/가끔 보이는 문제 해결)
 */
public class ServerboundSellItemPacket implements CustomPacketPayload {

    private final ResourceLocation inputItemId;
    private final int inputAmount;
    private final ResourceLocation outputItemId;
    private final int outputAmount;

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundSellItemPacket> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public @NotNull ServerboundSellItemPacket decode(RegistryFriendlyByteBuf buf) {
            return new ServerboundSellItemPacket(buf);
        }

        @Override
        public void encode(RegistryFriendlyByteBuf buf, ServerboundSellItemPacket packet) {
            buf.writeResourceLocation(packet.inputItemId);
            buf.writeInt(packet.inputAmount);
            buf.writeResourceLocation(packet.outputItemId);
            buf.writeInt(packet.outputAmount);
        }
    };

    public ServerboundSellItemPacket(ShopOffer offer) {
        this(offer.inputItemId(), offer.inputAmount(), offer.outputItemId(), offer.outputAmount());
    }

    public ServerboundSellItemPacket(ResourceLocation inputItemId, int inputAmount, ResourceLocation outputItemId, int outputAmount) {
        this.inputItemId = inputItemId;
        this.inputAmount = inputAmount;
        this.outputItemId = outputItemId;
        this.outputAmount = outputAmount;
    }

    public ServerboundSellItemPacket(RegistryFriendlyByteBuf friendlyByteBuf) {
        this.inputItemId = friendlyByteBuf.readResourceLocation();
        this.inputAmount = friendlyByteBuf.readInt();
        this.outputItemId = friendlyByteBuf.readResourceLocation();
        this.outputAmount = friendlyByteBuf.readInt();
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return JobsPlusNetworking.SERVERBOUND_SELL_ITEM;
    }

    public static void handleServerSide(ServerboundSellItemPacket packet, NetworkManager.PacketContext context) {
        if (!(context.getPlayer() instanceof JobsServerPlayer serverPlayer)) {
            return;
        }

        ServerPlayer player = serverPlayer.jobsplus$getServerPlayer();

        if (packet.inputAmount <= 0 || packet.outputAmount <= 0) {
            return;
        }

        Optional<Holder.Reference<Item>> inputHolder = BuiltInRegistries.ITEM.get(packet.inputItemId);
        Optional<Holder.Reference<Item>> outputHolder = BuiltInRegistries.ITEM.get(packet.outputItemId);

        if (inputHolder.isEmpty()) {
            player.sendSystemMessage(JobsPlus.translatable("error.shop_input_item_not_found", packet.inputItemId.toString()));
            return;
        }

        if (outputHolder.isEmpty()) {
            player.sendSystemMessage(JobsPlus.translatable("error.shop_output_item_not_found", packet.outputItemId.toString()));
            return;
        }

        Item inputItem = inputHolder.get().value();
        Item outputItem = outputHolder.get().value();

        Inventory inv = player.getInventory();

        // 입력 아이템 보유량 계산
        int inputCount = 0;
        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.getItem() == inputItem) {
                inputCount += stack.getCount();
            }
        }

        if (inputCount < packet.inputAmount) {
            player.sendSystemMessage(JobsPlus.translatable(
                    "error.not_enough_items",
                    inputItem.getName(new ItemStack(inputItem)),
                    packet.inputAmount));
            return;
        }

        // 지급 전 스냅샷(기존 로직 유지: diff 계산은 하지만 출력은 하지 않음)
        ItemStack[] before = snapshotMainInventory(inv);

        // 입력 아이템 제거
        int remainingToRemove = packet.inputAmount;
        for (int i = 0; i < inv.getContainerSize() && remainingToRemove > 0; i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.getItem() == inputItem) {
                int removeAmount = Math.min(stack.getCount(), remainingToRemove);
                stack.shrink(removeAmount);
                remainingToRemove -= removeAmount;

                if (stack.isEmpty()) {
                    inv.setItem(i, ItemStack.EMPTY);
                }
            }
        }

        inv.setChanged();

        // 출력 아이템 지급(핵심 수정)
        ItemStack outStack = new ItemStack(outputItem, packet.outputAmount);

        // 1.21.10 정석: 슬롯 업데이트 패킷까지 고려한 지급
        inv.placeItemBackInInventory(outStack, true);

        // 추가 동기화(안정성 강화)
        inv.setChanged();
        player.containerMenu.broadcastChanges();

        // 기존 로직 유지: 호출은 하되, 내부에서 로그를 찍지 않게 처리(현재 클래스에서는 로그 자체가 없음)
        int nowHasOutput = countItem(inv, outputItem);
        dumpInventoryDiff(inv, before, outputItem);

        // 화면 업데이트 (maxJobs 포함)
        NetworkManager.sendToPlayer(
                player,
                new ClientboundOpenJobsScreenPacket(
                        Stream.concat(serverPlayer.jobsplus$getJobs().stream(),
                                serverPlayer.jobsplus$getInactiveJobs().stream()).toList(),
                        serverPlayer.jobsplus$getCoins(),
                        serverPlayer.jobsplus$getEffectiveMaxJobs())) ;

        player.sendSystemMessage(JobsPlus.translatable(
                "gui.jobs.shop.sold",
                inputItem.getName(new ItemStack(inputItem)),
                packet.inputAmount,
                outputItem.getName(new ItemStack(outputItem)),
                packet.outputAmount));
    }

    private static ItemStack[] snapshotMainInventory(Inventory inv) {
        int size = Math.min(inv.getContainerSize(), 36);
        ItemStack[] snap = new ItemStack[size];
        for (int i = 0; i < size; i++) {
            snap[i] = inv.getItem(i).copy();
        }
        return snap;
    }

    private static void dumpInventoryDiff(Inventory inv, ItemStack[] before, Item targetItem) {
        int size = Math.min(inv.getContainerSize(), 36);

        for (int i = 0; i < size; i++) {
            ItemStack after = inv.getItem(i);
            ItemStack b = (before != null && i < before.length) ? before[i] : ItemStack.EMPTY;

            boolean changed = !ItemStack.matches(after, b);
            if (changed) {
                // 로그 제거 요청에 따라 출력하지 않음
            }

            // targetItemFound 여부도 로그 제거 요청에 따라 출력하지 않음
            if (!after.isEmpty() && after.getItem() == targetItem) {
                // no-op
            }
        }
    }

    private static int countItem(Inventory inv, Item item) {
        int total = 0;
        int size = inv.getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                total += stack.getCount();
            }
        }
        return total;
    }
}
