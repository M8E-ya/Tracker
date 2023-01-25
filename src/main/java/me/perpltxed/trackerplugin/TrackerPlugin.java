package me.perpltxed.trackerplugin;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class TrackerPlugin extends JavaPlugin {
    Tracker tracker = new Tracker(new File("plugins/TrackerPlugin"));

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if (getConfig().getBoolean("tracker-enabled")) {
            getServer().getPluginManager().registerEvents(tracker, this);
        }
    }
}


