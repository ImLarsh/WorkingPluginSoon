package com.example.shardplugin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import com.example.shardplugin.ShardPlugin;
import com.example.shardplugin.gui.ArmorShopGUI;

public class ShardCommands implements CommandExecutor {
    
    private final ShardPlugin plugin;
    
    public ShardCommands(final ShardPlugin plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof Player) && !label.equals("giveshards") && !(label.equals("armorshop") && args.length > 0 && (args[0].equals("reload") || args[0].equals("shards")))) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        
        switch (label.toLowerCase()) {
            case "shards":
                return this.handleShardsCommand((Player) sender);
            case "armorshop":
                return this.handleArmorShopCommand(sender, args);
            case "giveshards":
                return this.handleGiveShardsCommand(sender, args);
            default:
                return false;
        }
    }
    
    private boolean handleShardsCommand(final Player player) {
        final int shards = this.plugin.getShardManager().getShards(player);
        player.sendMessage("§aYou have " + shards + " shards!");
        return true;
    }
    
    private boolean handleArmorShopCommand(final CommandSender sender, final String[] args) {
        if (args.length > 0) {
            if (args[0].equals("reload")) {
                if (!sender.hasPermission("shardplugin.admin")) {
                    sender.sendMessage("§cYou don't have permission to reload the plugin!");
                    return true;
                }
                
                // Reload configuration
                this.plugin.getConfigManager().reloadConfig();
                
                // Reload managers that depend on config
                this.plugin.reloadManagers();
                
                sender.sendMessage("§aPlugin configuration reloaded successfully!");
                return true;
            } else if (args[0].equals("shards") && args.length > 1 && args[1].equals("give")) {
                if (!sender.hasPermission("shardplugin.admin")) {
                    sender.sendMessage("§cYou don't have permission to give shards!");
                    return true;
                }
                
                if (args.length != 4) {
                    sender.sendMessage("§cUsage: /armorshop shards give <player> <amount>");
                    return true;
                }
                
                final Player target = Bukkit.getPlayer(args[2]);
                if (target == null) {
                    sender.sendMessage("§cPlayer not found!");
                    return true;
                }
                
                try {
                    final int amount = Integer.parseInt(args[3]);
                    if (amount <= 0) {
                        sender.sendMessage("§cAmount must be greater than 0!");
                        return true;
                    }
                    
                    this.plugin.getShardManager().addShards(target, amount);
                    sender.sendMessage("§aGave " + amount + " shards to " + target.getName());
                    target.sendMessage("§aYou received " + amount + " shards!");
                    return true;
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid amount!");
                    return true;
                }
            }
        }
        
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        
        new ArmorShopGUI(this.plugin, (Player) sender).open();
        return true;
    }
    
    private boolean handleGiveShardsCommand(final CommandSender sender, final String[] args) {
        if (!sender.hasPermission("shardplugin.admin")) {
            sender.sendMessage("§cYou don't have permission to use this command!");
            return true;
        }
        
        if (args.length != 2) {
            sender.sendMessage("§cUsage: /giveshards <player> <amount>");
            return true;
        }
        
        final Player target = Bukkit.getPlayer(args[0]);
        
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }
        
        try {
            final int amount = Integer.parseInt(args[1]);
            this.plugin.getShardManager().addShards(target, amount);
            sender.sendMessage("§aGave " + amount + " shards to " + target.getName());
            target.sendMessage("§aYou received " + amount + " shards!");
            return true;
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount!");
            return true;
        }
    }
}