package com.daqem.arc.api.action.type;

import com.daqem.arc.api.action.IAction;
import com.daqem.arc.api.action.serializer.IActionSerializer;
import net.minecraft.resources.ResourceLocation;

public interface IActionType<T extends IAction> {

    ResourceLocation getLocation();

    IActionSerializer<T> getSerializer();
}
