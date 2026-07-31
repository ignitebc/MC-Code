package com.autovw.advancednetherite.common.randombox;

import net.minecraft.resources.Identifier;

import java.util.List;

public final class RandomBoxConfig {

    public enum RollMode {
        INDEPENDENT,
        SINGLE
    }

    public Identifier item;
    public Identifier required_key;
    public Consume consume;

    public RollMode roll_mode = RollMode.SINGLE;

    public List<Reward> rewards;

    public static final class Consume {
        public int box = 1;
        public int key = 1;
    }

    public static final class Reward {
        public Identifier item;
        public int count = 1;

        /**
         * 항상 % 단위. (0.5 = 0.5%, 50 = 50%)
         * INDEPENDENT: 항목별 독립 판정 확률
         * SINGLE: 가중치로 사용하며, 합이 100이면 표기 %가 그대로 확률이 된다.
         */
        public double chance = 1.0;
    }
}
