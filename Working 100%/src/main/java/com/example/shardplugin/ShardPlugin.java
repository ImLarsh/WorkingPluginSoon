package com.example.shardplugin;

import org.bukkit.plugin.java.JavaPlugin;
import com.example.shardplugin.managers.*;
import com.example.shardplugin.listeners.*;
import com.example.shardplugin.commands.*;

public class ShardPlugin extends JavaPlugin {
    
    private static ShardPlugin instance;
    private ShardManager shardManager;
    private ArmorManager armorManager;
    private PickaxeManager pickaxeManager;
    private MessageManager messageManager;
    private InventoryListener inventoryListener;
    private ConfigManager configManager;

    public static ShardPlugin getInstance() {
        return instance;
    }

    public ShardManager getShardManager() {
        return this.shardManager;
    }

    public ArmorManager getArmorManager() {
        return this.armorManager;
    }

    public PickaxeManager getPickaxeManager() {
        return this.pickaxeManager;
    }

    public MessageManager getMessageManager() {
        return this.messageManager;
    }

    public InventoryListener getInventoryListener() {
        return this.inventoryListener;
    }

    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize managers in correct order
        this.configManager = new ConfigManager(this);
        this.messageManager = new MessageManager(this);
        this.shardManager = new ShardManager(this);
        this.armorManager = new ArmorManager(this);
        this.pickaxeManager = new PickaxeManager(this);
        this.inventoryListener = new InventoryListener(this);
        
        // Register everything
        this.registerListeners();
        this.registerCommands();
        
        this.getLogger().info("ShardPlugin has been enabled!");
    }

    @Override
    public void onDisable() {
        this.shardManager.saveAllData();
        this.configManager.saveAll();
        this.getLogger().info("ShardPlugin has been disabled!");
    }

    private void registerListeners() {
        this.getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);
        this.getServer().getPluginManager().registerEvents(this.inventoryListener, this);
        this.getServer().getPluginManager().registerEvents(new ArmorDropListener(this), this);
        this.getServer().getPluginManager().registerEvents(new PickaxeDropListener(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
    }

    private void registerCommands() {
        this.getCommand("shards").setExecutor(new ShardCommands(this));
        this.getCommand("armorshop").setExecutor(new ShardCommands(this));
        this.getCommand("giveshards").setExecutor(new ShardCommands(this));
    }

    public void reloadManagers() {
        // Reload managers that depend on config
        this.messageManager = new MessageManager(this);
        this.armorManager = new ArmorManager(this);
        this.pickaxeManager = new PickaxeManager(this);
    }
}