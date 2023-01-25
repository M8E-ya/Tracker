package me.perpltxed.tracker;

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
    private Map<String, Integer> playerKills;
    private Map<String, Integer> playerPotions;
    private Map<String, Integer> playerFood;
    private Map<String, Integer> playerEnderPearls;

    public Tracker(File dataFolder) {
        statsFile = new File(dataFolder, "stats.json");
        gson = new GsonBuilder().setPrettyPrinting().create();
        playerKills = new HashMap<>();
        playerPotions = new HashMap<>();
        playerFood = new HashMap<>();
        playerEnderPearls = new HashMap<>();
        loadStats();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        String playerName = event.getEntity().getName();
        if (event.getEntity().getKiller() != null) {
            String killerName = event.getEntity().getKiller().getName();
            int kills = playerKills.getOrDefault(killerName, 0) + 1;
            playerKills.put(killerName, kills);
            saveStats();
        }
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        String playerName = event.getPlayer().getName();
        ItemStack item = event.getItem();
        if (item.getType().equals(Material.POTION)) {
            int potions = playerPotions.getOrDefault(playerName, 0) + 1;
            playerPotions.put(playerName, potions);
            saveStats();
        } else if (item.getType().isEdible()) {
            int food = playerFood.getOrDefault(playerName, 0) + 1;
            playerFood.put(playerName, food);
            saveStats();
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        String playerName = event.getPlayer().getName();
        if (event.getCause().equals(PlayerTeleportEvent.TeleportCause.ENDER_PEARL)) {
            int enderPearls = playerEnderPearls.getOrDefault(playerName, 0) + 1;
            playerEnderPearls.put(playerName, enderPearls);
            saveStats();
        }
    }

    void loadStats() {
        if (!statsFile.exists()) {
            try {
                statsFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try (FileReader reader = new FileReader(statsFile)) {
                Type mapType = new TypeToken<Map<String, Integer>>() {}.getType();
                playerKills = gson.fromJson(reader, mapType);
                playerPotions = gson.fromJson(reader, mapType);
                playerFood = gson.fromJson(reader, mapType);
                playerEnderPearls = gson.fromJson(reader, mapType);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void saveStats() {
        try (FileWriter writer = new FileWriter(statsFile)) {
            gson.toJson(playerKills, writer);
            gson.toJson(playerPotions, writer);
            gson.toJson(playerFood, writer);
            gson.toJson(playerEnderPearls, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
