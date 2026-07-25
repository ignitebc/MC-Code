package com.daqem.arc.client;

import com.daqem.arc.Arc;
import com.daqem.arc.api.action.IAction;
import com.daqem.arc.api.action.holder.ActionHolderManager;
import com.daqem.arc.client.gui.action.ActionScreen;
import com.daqem.arc.config.ArcCommonConfig;
import com.mojang.logging.LogUtils;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientRawInputEvent;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

import java.util.List;

public class ArcClient {

    public static final Logger LOGGER = LogUtils.getLogger();

    public static void init() {
        registerEvents();
    }

    private static void registerEvents() {
    }
}
