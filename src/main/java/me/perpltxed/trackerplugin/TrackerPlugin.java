package me.perpltxed.trackerplugin;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class TrackerPlugin extends JavaPlugin {
    Tracker tracker = new Tracker(new File("plugins/TrackerPlugin"));

    @Override
    public void onEnable() {
        saveDefaultConfig();
        //check if tracker is enabled
        if (getConfig().getBoolean("tracker-enabled")) {
            tracker.loadStats();
        }
    }

    @Override
    public void onDisable() {
        if (getConfig().getBoolean("tracker-enabled")) {
            tracker.saveStats();
        }
    }
}
