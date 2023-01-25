package me.perpltxed.trackerplugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class Tracker implements Listener {

    private File statsFile;
    private Gson gson;
    private Map<String, Map<String, Integer>> playerData;

    public Tracker(File dataFolder) {
        statsFile = new File(dataFolder, "stats.json");
        gson = new GsonBuilder().setPrettyPrinting().create();
        playerData = new HashMap<>();
        loadStats();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        String playerName = event.getEntity().getName();
        writeToJson(playerName, "deaths", 1);
        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            writeToJson(killer.getName(), "kills", 1);
        }
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        String playerName = event.getPlayer().getName();
        ItemStack item = event.getItem();
        if (item.getType() == Material.POTION || item.getType() == Material.SPLASH_POTION) {
            Potion potion = Potion.fromItemStack(item);
            if (potion != null) {
                writeToJson(playerName, "potions", 1);
            }
        } else if (item.getType().isEdible()) {
            writeToJson(playerName, "food", 1);
        }
    }


    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        String playerName = event.getPlayer().getName();
        if (event.getCause().equals(PlayerTeleportEvent.TeleportCause.ENDER_PEARL)) {
            writeToJson(playerName, "ender_pearls", 1);
        }
    }

    private void writeToJson(String playerName, String action, int value) {
        try {
            FileReader reader = new FileReader(statsFile);
            Type type = new TypeToken<Map<String, Map<String, Integer>>>(){}.getType();
            playerData = gson.fromJson(reader, type);
            reader.close();
        } catch (IOException e) {
            playerData = new HashMap<>();
        }
        Map<String, Integer> playerActions = playerData.getOrDefault(playerName, new HashMap<>());
        int currentValue = playerActions.getOrDefault(action, 0);
        playerActions.put(action, currentValue + value);
        playerData.put(playerName, playerActions);
        try {
            FileWriter writer = new FileWriter(statsFile);
            gson.toJson(playerData, writer);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadStats() {
        if (!statsFile.exists()) {
            try {
                statsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

