package com.daqem.jobsplus.achievement;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductionTrackerTest
{
    private static final String FIRST = "61f57c65-d57e-493b-8cde-f2c2ab2aef91";
    private static final String SECOND = "a2f958f7-8315-423d-bd1c-d8a1a65f9be0";

    @Test
    void resultRemovalCreditsOnlyTheMatchingOwnersActualQuantity()
    {
        String json = """
                {"2":{"item":"minecraft:iron_ingot","batches":[
                    {"owner":"","amount":2},
                    {"owner":"61f57c65-d57e-493b-8cde-f2c2ab2aef91","amount":3},
                    {"owner":"a2f958f7-8315-423d-bd1c-d8a1a65f9be0","amount":4}
                ]}}
                """;
        ProductionTracker tracker = ProductionTracker.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
        assertEquals(2, tracker.take(2, 4, FIRST));
        assertEquals(FIRST, tracker.firstOwner(2));
        assertEquals(0, tracker.take(2, 1, SECOND));
        assertEquals(SECOND, tracker.firstOwner(2));
        assertEquals(4, tracker.take(2, 10, SECOND));
        assertEquals(0, tracker.take(2, 10, SECOND));
        assertTrue(tracker.consumeDirty());
        assertFalse(tracker.consumeDirty());
    }

    @Test
    void automatedRemovalConsumesOwnershipWithoutGrantingCredit()
    {
        String json = """
                {"2":{"item":"minecraft:iron_ingot","batches":[
                    {"owner":"61f57c65-d57e-493b-8cde-f2c2ab2aef91","amount":5}
                ]}}
                """;
        ProductionTracker tracker = ProductionTracker.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).getOrThrow();
        assertEquals(0, tracker.take(2, 3, ""));
        var saved = ProductionTracker.CODEC.encodeStart(JsonOps.INSTANCE, tracker).getOrThrow();
        ProductionTracker loaded = ProductionTracker.CODEC.parse(JsonOps.INSTANCE, saved).getOrThrow();
        assertEquals(2, loaded.take(2, 5, FIRST));
    }

    @Test
    void malformedOwnershipAndOverflowingQuantitiesAreRejected()
    {
        String invalidOwner = """
                {"0":{"item":"minecraft:iron_ore","batches":[{"owner":"invalid","amount":1}]}}
                """;
        String overflow = """
                {"0":{"item":"minecraft:iron_ore","batches":[
                    {"owner":"","amount":2147483647},{"owner":"","amount":1}
                ]}}
                """;
        assertTrue(ProductionTracker.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(invalidOwner)).result().isEmpty());
        assertTrue(ProductionTracker.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(overflow)).result().isEmpty());
    }
}
