package com.example.shardplugin.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import com.example.shardplugin.ShardPlugin;

public class MessageManager {
    
    private final ShardPlugin plugin;
    private final ConfigurationSection messageConfig;
    private final boolean enabled;
    private final String prefix;
    
    public MessageManager(final ShardPlugin plugin) {
        this.plugin = plugin;
        this.messageConfig = plugin.getConfig().getConfigurationSection("settings.messages");
        this.enabled = this.messageConfig.getBoolean("enabled", true);
        this.prefix = this.messageConfig.getString("prefix", "§8[§bShards§8]");
    }
    
    public void sendMessage(final Player player, final String key, final Object... args) {
        if (!this.enabled) return;
        
        String message = this.messageConfig.getString(key, "Message not found: " + key);
        message = message.replace("%prefix%", this.prefix);
        
        for (int i = 0; i < args.length; i += 2) {
            if (i + 1 < args.length) {
                message = message.replace(args[i].toString(), args[i + 1].toString());
            }
        }
        
        player.sendMessage(message);
    }
}