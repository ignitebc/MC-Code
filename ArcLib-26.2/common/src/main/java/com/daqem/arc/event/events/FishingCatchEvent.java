package com.daqem.arc.event.events;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;

/** 자동 낚시 판정은 한 번만 실행하고, 추가 드롭 적용 전 원본 어획을 전달한다. */
public interface FishingCatchEvent
{
    Event<FishingCatchEvent> CAUGHT = EventFactory.createLoop();

    void onCatch(ServerPlayer player, Collection<ItemStack> originalLoot);
}
