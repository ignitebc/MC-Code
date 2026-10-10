package com.daqem.arc.client;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;


public class ArcClient {

    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        registerEvents();
    }

    private static void registerEvents() {
    }
}
