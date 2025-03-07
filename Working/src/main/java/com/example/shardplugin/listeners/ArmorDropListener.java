package com.example.shardplugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.NamespacedKey;
import com.example.shardplugin.ShardPlugin;

public class ArmorDropListener implements Listener {
    
    private final ShardPlugin plugin;
    private final NamespacedKey armorSetKey;
    
    public ArmorDropListener(final ShardPlugin plugin) {
        this.plugin = plugin;
        this.armorSetKey = new NamespacedKey(plugin, "armor_set");
    }
    
    @EventHandler
    public void onPlayerDropItem(final PlayerDropItemEvent event) {
        final ItemStack item = event.getItemDrop().getItemStack();
        
        if (item.hasItemMeta()) {
            final ItemMeta meta = item.getItemMeta();
            final PersistentDataContainer container = meta.getPersistentDataContainer();
            
            // Check if the item is a custom armor piece
            if (container.has(this.armorSetKey, PersistentDataType.STRING)) {
                event.setCancelled(true);
                this.plugin.getMessageManager().sendMessage(
                    event.getPlayer(),
                    "cannot_drop_armor"
                );
            }
        }
    }
}