package com.example.shardplugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import com.example.shardplugin.ShardPlugin;

public class PlayerJoinListener implements Listener {
    
    private final ShardPlugin plugin;
    
    public PlayerJoinListener(final ShardPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        // Load player data when they join
        this.plugin.getShardManager().loadPlayerData(event.getPlayer());
    }
}