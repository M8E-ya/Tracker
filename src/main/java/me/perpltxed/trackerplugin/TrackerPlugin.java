package me.perpltxed.trackerplugin;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class TrackerPlugin extends JavaPlugin {
    private Tracker tracker;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        File dataFolder = new File(getDataFolder(), "TrackerPlugin");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        tracker = new Tracker(dataFolder);
        if (getConfig().getBoolean("tracker-enabled")) {
            getServer().getPluginManager().registerEvents(tracker, this);
        }
    }
}
