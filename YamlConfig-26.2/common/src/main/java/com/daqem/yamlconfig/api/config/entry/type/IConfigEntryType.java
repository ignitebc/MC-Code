package com.daqem.yamlconfig.api.config.entry.type;

import com.daqem.yamlconfig.api.config.entry.serializer.IConfigEntrySerializer;
import com.daqem.yamlconfig.api.config.entry.IConfigEntry;
import net.minecraft.resources.Identifier;

public interface IConfigEntryType<C extends IConfigEntry<T>, T> {

    Identifier getId();

    IConfigEntrySerializer<C, T> getSerializer();
}
