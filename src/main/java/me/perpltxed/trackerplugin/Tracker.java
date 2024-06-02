package me.perpltxed.trackerplugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
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
import java.util.UUID;

public class Tracker implements Listener {
    private final File statsFile;
    private final Gson gson;
    private Map<String, Map<String, Integer>> playerData;

    private final Map<UUID, Double> damageDealt;
    private final Map<UUID, Double> damageReceived;

    public Tracker(File dataFolder) {
        statsFile = new File(dataFolder, "stats.json");
        gson = new GsonBuilder().setPrettyPrinting().create();
        playerData = new HashMap<>();
        damageDealt = new HashMap<>();
        damageReceived = new HashMap<>();
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
        if (item.getType().isEdible()) {
            writeToJson(playerName, "food", 1);
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        ThrownPotion potion = event.getPotion();
        if (potion.getShooter() instanceof Player) {
            Player player = (Player) potion.getShooter();
            writeToJson(player.getName(), "splash_potions", 1);
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player) {
            Player player = (Player) event.getEntity().getShooter();
            if (event.getEntityType() == EntityType.ARROW) {
                writeToJson(player.getName(), "arrows_shot", 1);
            }
        }
    }

    @EventHandler
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        String playerName = event.getPlayer().getName();
        if (event.getCause().equals(PlayerTeleportEvent.TeleportCause.ENDER_PEARL)) {
            writeToJson(playerName, "ender_pearls", 1);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player && event.getEntity() instanceof Player) {
            Player damager = (Player) event.getDamager();
            Player damaged = (Player) event.getEntity();
            double damage = event.getDamage();

            // Update cumulative damage dealt and received
            UUID damagerUUID = damager.getUniqueId();
            UUID damagedUUID = damaged.getUniqueId();
            damageDealt.put(damagerUUID, damageDealt.getOrDefault(damagerUUID, 0.0) + damage);
            damageReceived.put(damagedUUID, damageReceived.getOrDefault(damagedUUID, 0.0) + damage);

            // Update the stats file
            writeToJson(damager.getName(), "damage_dealt", damage);
            writeToJson(damaged.getName(), "damage_received", damage);
        }
    }

    private void writeToJson(String playerName, String action, double value) {
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
        playerActions.put(action, (int)(currentValue + value));
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
