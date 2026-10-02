package com.daqem.jobsplus.achievement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** 슬롯에 실제로 남은 수량만 FIFO로 추적한다. 작업자를 모르는 투입은 자동화/기존 재료로 취급한다. */
public final class ProductionTracker
{
    public record Batch(String owner, int amount)
    {
        private static final Codec<String> OWNER_CODEC = Codec.STRING.validate(owner -> {
            if (owner.isEmpty())
            {
                return DataResult.success(owner);
            }
            try
            {
                UUID.fromString(owner);
                return DataResult.success(owner);
            }
            catch (IllegalArgumentException exception)
            {
                return DataResult.error(() -> "Invalid production owner UUID");
            }
        });
        private static final Codec<Batch> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                OWNER_CODEC.fieldOf("owner").forGetter(Batch::owner),
                Codec.intRange(1, Integer.MAX_VALUE).fieldOf("amount").forGetter(Batch::amount)
        ).apply(instance, Batch::new));
    }

    public record TrackedSlot(String item, List<Batch> batches)
    {
        private static final Codec<TrackedSlot> CODEC = RecordCodecBuilder.<TrackedSlot>create(instance -> instance.group(
                Codec.STRING.fieldOf("item").forGetter(TrackedSlot::item),
                Batch.CODEC.listOf().fieldOf("batches").forGetter(TrackedSlot::batches)
        ).apply(instance, TrackedSlot::new)).validate(slot -> {
            long amount = 0;
            for (Batch batch : slot.batches())
            {
                amount += batch.amount();
                if (amount > Integer.MAX_VALUE)
                {
                    return DataResult.error(() -> "Production slot quantity is too large");
                }
            }
            return DataResult.success(slot);
        });

        public TrackedSlot
        {
            batches = new ArrayList<>(batches);
        }
    }

    public static final Codec<ProductionTracker> CODEC = Codec.unboundedMap(Codec.STRING, TrackedSlot.CODEC)
            .xmap(ProductionTracker::new, tracker -> tracker.slots);
    private final Map<String, TrackedSlot> slots;
    private boolean dirty;

    public ProductionTracker()
    {
        this(Map.of());
    }

    private ProductionTracker(Map<String, TrackedSlot> slots)
    {
        this.slots = new HashMap<>(slots);
    }

    public void reconcile(int slot, ItemStack current)
    {
        String key = Integer.toString(slot);
        TrackedSlot tracked = slots.get(key);
        if (current.isEmpty())
        {
            if (slots.remove(key) != null)
            {
                dirty = true;
            }
            return;
        }
        String item = itemId(current);
        if (tracked == null || !tracked.item().equals(item))
        {
            slots.put(key, new TrackedSlot(item, List.of(new Batch("", current.getCount()))));
            dirty = true;
            return;
        }
        int recorded = total(tracked);
        if (recorded > current.getCount())
        {
            take(slot, recorded - current.getCount(), "");
        }
        else if (recorded < current.getCount())
        {
            append(slot, current, "", current.getCount() - recorded);
        }
    }

    public void manualChange(int slot, ItemStack before, ItemStack after, UUID player)
    {
        if (after.isEmpty())
        {
            reconcile(slot, after);
            return;
        }
        boolean same = ItemStack.isSameItemSameComponents(before, after);
        if (!same)
        {
            replaceManually(slot, after, player);
            return;
        }
        if (after.getCount() > before.getCount())
        {
            append(slot, after, player.toString(), after.getCount() - before.getCount());
        }
        else
        {
            reconcile(slot, after);
        }
    }

    public void replaceManually(int slot, ItemStack after, UUID player)
    {
        if (after.isEmpty())
        {
            reconcile(slot, after);
            return;
        }
        slots.put(Integer.toString(slot), new TrackedSlot(itemId(after), List.of(new Batch(player.toString(), after.getCount()))));
        dirty = true;
    }

    /** 양조에서 병의 아이템 종류가 바뀌어도 같은 병의 투입 작업자를 계승한다. */
    public void transform(int slot, ItemStack result)
    {
        String key = Integer.toString(slot);
        TrackedSlot tracked = slots.get(key);
        if (result.isEmpty() || tracked == null)
        {
            reconcile(slot, result);
            return;
        }
        String resultItem = itemId(result);
        if (!tracked.item().equals(resultItem))
        {
            slots.put(key, new TrackedSlot(resultItem, tracked.batches()));
            dirty = true;
        }
        reconcile(slot, result);
    }

    public void append(int slot, ItemStack stack, String owner, int amount)
    {
        if (amount <= 0 || stack.isEmpty())
        {
            return;
        }
        String key = Integer.toString(slot);
        String item = itemId(stack);
        TrackedSlot tracked = slots.get(key);
        if (tracked == null || !tracked.item().equals(item))
        {
            tracked = new TrackedSlot(item, List.of());
            slots.put(key, tracked);
        }
        List<Batch> batches = tracked.batches();
        if (!batches.isEmpty() && batches.getLast().owner().equals(owner))
        {
            Batch last = batches.removeLast();
            batches.add(new Batch(owner, last.amount() + amount));
        }
        else
        {
            batches.add(new Batch(owner, amount));
        }
        dirty = true;
    }

    public String firstOwner(int slot)
    {
        TrackedSlot tracked = slots.get(Integer.toString(slot));
        if (tracked != null && !tracked.batches().isEmpty())
        {
            return tracked.batches().getFirst().owner();
        }
        return "";
    }

    public int take(int slot, int amount, String recipient)
    {
        TrackedSlot tracked = slots.get(Integer.toString(slot));
        if (tracked == null || amount <= 0)
        {
            return 0;
        }
        int credited = 0;
        int remaining = amount;
        List<Batch> batches = tracked.batches();
        while (remaining > 0 && !batches.isEmpty())
        {
            Batch first = batches.removeFirst();
            int taken = Math.min(remaining, first.amount());
            if (!recipient.isEmpty() && recipient.equals(first.owner()))
            {
                credited += taken;
            }
            if (taken < first.amount())
            {
                batches.addFirst(new Batch(first.owner(), first.amount() - taken));
            }
            remaining -= taken;
            dirty = true;
        }
        return credited;
    }

    public boolean consumeDirty()
    {
        boolean changed = dirty;
        dirty = false;
        return changed;
    }

    private static int total(TrackedSlot slot)
    {
        int total = 0;
        for (Batch batch : slot.batches())
        {
            total += batch.amount();
        }
        return total;
    }

    private static String itemId(ItemStack stack)
    {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }
}
