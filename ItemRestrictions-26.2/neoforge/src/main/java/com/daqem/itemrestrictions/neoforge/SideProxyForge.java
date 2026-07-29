package com.daqem.itemrestrictions.neoforge;

import com.daqem.itemrestrictions.client.ItemRestrictionsClient;

public class SideProxyForge {

    SideProxyForge() {
    }

    public static class Server extends SideProxyForge {
        Server() {
        }
    }

    public static class Client extends SideProxyForge {
        Client() {
            ItemRestrictionsClient.init();
        }
    }
}
