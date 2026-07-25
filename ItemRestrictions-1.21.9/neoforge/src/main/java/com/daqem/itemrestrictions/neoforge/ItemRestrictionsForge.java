package com.daqem.itemrestrictions.neoforge;

import com.daqem.itemrestrictions.ItemRestrictions;
import dev.architectury.utils.EnvExecutor;
import net.neoforged.fml.common.Mod;

@Mod(ItemRestrictions.MOD_ID)
public class ItemRestrictionsForge {
    public ItemRestrictionsForge() {
        ItemRestrictions.init();

        EnvExecutor.getEnvSpecific(
                () -> SideProxyForge.Client::new,
                () -> SideProxyForge.Server::new
        );
    }
}
