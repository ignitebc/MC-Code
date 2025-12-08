package com.daqem.arc.api.action.holder.type;

import com.daqem.arc.api.action.holder.IActionHolder;
import com.daqem.arc.api.action.holder.serializer.IActionHolderSerializer;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public interface IActionHolderType<T extends IActionHolder> {

    ResourceLocation getLocation();

    IActionHolderSerializer<T> getSerializer();
}
