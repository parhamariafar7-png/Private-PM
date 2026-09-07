package com.privatepm.plugin;

import org.bukkit.plugin.java.JavaPlugin;

public final class PrivatePMPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new PrivateMessageListener(this), this);
        getLogger().info("PrivatePM enabled - intercepting /msg, /tell, /w, /m, /pm");
    }

    @Override
    public void onDisable() {
        getLogger().info("PrivatePM disabled.");
    }
}
