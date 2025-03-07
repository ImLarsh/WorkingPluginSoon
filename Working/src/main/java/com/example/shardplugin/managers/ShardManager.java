package com.example.shardplugin.managers;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.NamespacedKey;
import com.example.shardplugin.ShardPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShardManager {

    private final ShardPlugin plugin;
    private final Map<UUID, Integer> playerShards;
    private final Map<UUID, Integer> playerEnchantedShards;
    private final Map<UUID, Integer> playerVoidShards;
    private final Map<UUID, Integer> playerPickaxeShards;
    private final Map<UUID, Integer> playerEnchantedPickaxeShards;
    private final NamespacedKey shardKey;
    private final NamespacedKey enchantedShardKey;
    private final NamespacedKey voidShardKey;
    private final NamespacedKey pickaxeShardKey;
    private final NamespacedKey enchantedPickaxeShardKey;

    public ShardManager(final ShardPlugin plugin) {
        this.plugin = plugin;
        this.playerShards = new HashMap<>();
        this.playerEnchantedShards = new HashMap<>();
        this.playerVoidShards = new HashMap<>();
        this.playerPickaxeShards = new HashMap<>();
        this.playerEnchantedPickaxeShards = new HashMap<>();
        this.shardKey = new NamespacedKey(plugin, "shards");
        this.enchantedShardKey = new NamespacedKey(plugin, "enchanted_shards");
        this.voidShardKey = new NamespacedKey(plugin, "void_shards");
        this.pickaxeShardKey = new NamespacedKey(plugin, "pickaxe_shards");
        this.enchantedPickaxeShardKey = new NamespacedKey(plugin, "enchanted_pickaxe_shards");
    }

    public void addShards(final Player player, final int amount) {
        final UUID playerUUID = player.getUniqueId();
        final int currentShards = this.getShards(player);
        this.playerShards.put(playerUUID, currentShards + amount);
        this.savePlayerData(player);
    }

    public void addPickaxeShards(final Player player, final int amount) {
        final UUID playerUUID = player.getUniqueId();
        final int currentShards = this.getPickaxeShards(player);
        this.playerPickaxeShards.put(playerUUID, currentShards + amount);
        this.savePlayerData(player);
    }

    public void addEnchantedPickaxeShards(final Player player, final int amount) {
        final UUID playerUUID = player.getUniqueId();
        final int currentShards = this.getEnchantedPickaxeShards(player);
        this.playerEnchantedPickaxeShards.put(playerUUID, currentShards + amount);
        this.savePlayerData(player);
    }

    public void addEnchantedShards(final Player player, final int amount) {
        final UUID playerUUID = player.getUniqueId();
        final int currentShards = this.getEnchantedShards(player);
        this.playerEnchantedShards.put(playerUUID, currentShards + amount);
        this.savePlayerData(player);
    }

    public void addVoidShards(final Player player, final int amount) {
        final UUID playerUUID = player.getUniqueId();
        final int currentShards = this.getVoidShards(player);
        this.playerVoidShards.put(playerUUID, currentShards + amount);
        this.savePlayerData(player);
    }

    public boolean removeShards(final Player player, final int amount) {
        final UUID playerUUID = player.getUniqueId();
        final int currentShards = this.getShards(player);

        if (currentShards >= amount) {
            this.playerShards.put(playerUUID, currentShards - amount);
            this.savePlayerData(player);
            return true;
        }

        return false;
    }

    public boolean removePickaxeShards(final Player player, final int amount) {
        final UUID playerUUID = player.getUniqueId();
        final int currentShards = this.getPickaxeShards(player);

        if (currentShards >= amount) {
            this.playerPickaxeShards.put(playerUUID, currentShards - amount);
            this.savePlayerData(player);
            return true;
        }

        return false;
    }

    public boolean removeEnchantedPickaxeShards(final Player player, final int amount) {
        final UUID playerUUID = player.getUniqueId();
        final int currentShards = this.getEnchantedPickaxeShards(player);

        if (currentShards >= amount) {
            this.playerEnchantedPickaxeShards.put(playerUUID, currentShards - amount);
            this.savePlayerData(player);
            return true;
        }

        return false;
    }

    public boolean removeEnchantedShards(final Player player, final int amount) {
        final UUID playerUUID = player.getUniqueId();
        final int currentShards = this.getEnchantedShards(player);

        if (currentShards >= amount) {
            this.playerEnchantedShards.put(playerUUID, currentShards - amount);
            this.savePlayerData(player);
            return true;
        }

        return false;
    }

    public boolean removeVoidShards(final Player player, final int amount) {
        final UUID playerUUID = player.getUniqueId();
        final int currentShards = this.getVoidShards(player);

        if (currentShards >= amount) {
            this.playerVoidShards.put(playerUUID, currentShards - amount);
            this.savePlayerData(player);
            return true;
        }

        return false;
    }

    public int getShards(final Player player) {
        return this.playerShards.getOrDefault(player.getUniqueId(), 0);
    }

    public int getPickaxeShards(final Player player) {
        return this.playerPickaxeShards.getOrDefault(player.getUniqueId(), 0);
    }

    public int getEnchantedPickaxeShards(final Player player) {
        return this.playerEnchantedPickaxeShards.getOrDefault(player.getUniqueId(), 0);
    }

    public int getEnchantedShards(final Player player) {
        return this.playerEnchantedShards.getOrDefault(player.getUniqueId(), 0);
    }

    public int getVoidShards(final Player player) {
        return this.playerVoidShards.getOrDefault(player.getUniqueId(), 0);
    }

    public void loadPlayerData(final Player player) {
        final PersistentDataContainer container = player.getPersistentDataContainer();
        final int shards = container.getOrDefault(this.shardKey, PersistentDataType.INTEGER, 0);
        final int enchantedShards = container.getOrDefault(this.enchantedShardKey, PersistentDataType.INTEGER, 0);
        final int voidShards = container.getOrDefault(this.voidShardKey, PersistentDataType.INTEGER, 0);
        final int pickaxeShards = container.getOrDefault(this.pickaxeShardKey, PersistentDataType.INTEGER, 0);
        final int enchantedPickaxeShards = container.getOrDefault(this.enchantedPickaxeShardKey, PersistentDataType.INTEGER, 0);
        
        this.playerShards.put(player.getUniqueId(), shards);
        this.playerEnchantedShards.put(player.getUniqueId(), enchantedShards);
        this.playerVoidShards.put(player.getUniqueId(), voidShards);
        this.playerPickaxeShards.put(player.getUniqueId(), pickaxeShards);
        this.playerEnchantedPickaxeShards.put(player.getUniqueId(), enchantedPickaxeShards);
    }

    public void savePlayerData(final Player player) {
        final PersistentDataContainer container = player.getPersistentDataContainer();
        container.set(this.shardKey, PersistentDataType.INTEGER, this.getShards(player));
        container.set(this.enchantedShardKey, PersistentDataType.INTEGER, this.getEnchantedShards(player));
        container.set(this.voidShardKey, PersistentDataType.INTEGER, this.getVoidShards(player));
        container.set(this.pickaxeShardKey, PersistentDataType.INTEGER, this.getPickaxeShards(player));
        container.set(this.enchantedPickaxeShardKey, PersistentDataType.INTEGER, this.getEnchantedPickaxeShards(player));
    }

    public void saveAllData() {
        for (final Player player : this.plugin.getServer().getOnlinePlayers()) {
            this.savePlayerData(player);
        }
    }
}