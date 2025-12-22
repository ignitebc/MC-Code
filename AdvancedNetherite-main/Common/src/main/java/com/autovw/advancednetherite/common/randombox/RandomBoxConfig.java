package com.autovw.advancednetherite.common.randombox;

import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class RandomBoxConfig {

    public enum RollMode {
        INDEPENDENT,
        SINGLE
    }

    public ResourceLocation item;
    public ResourceLocation required_key;
    public Consume consume;

    public RollMode roll_mode = RollMode.INDEPENDENT;

    public List<Reward> rewards;

    public static final class Consume {
        public int box = 1;
        public int key = 1;
    }

    public static final class Reward {
        public ResourceLocation item;
        public int count = 1;

        /**
         * INDEPENDENT: 0~1 확률
         * SINGLE: 가중치(상대값)로 사용 (0.9/0.1 또는 90/10 가능)
         */
        public double chance = 1.0;
    }
}
