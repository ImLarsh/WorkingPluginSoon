package com.example.shardplugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import com.example.shardplugin.ShardPlugin;
import com.example.shardplugin.gui.ArmorShopGUI;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class InventoryListener implements Listener {

    private final ShardPlugin plugin;
    private final Map<UUID, ArmorShopGUI> openGUIs;

    public InventoryListener(final ShardPlugin plugin) {
        this.plugin = plugin;
        this.openGUIs = new HashMap<>();
    }

    public void registerGUI(final UUID playerUUID, final ArmorShopGUI gui) {
        this.openGUIs.put(playerUUID, gui);
    }

    public void unregisterGUI(final UUID playerUUID) {
        this.openGUIs.remove(playerUUID);
    }

    @EventHandler
    public void onInventoryClick(final InventoryClickEvent event) {
        final UUID playerUUID = event.getWhoClicked().getUniqueId();
        final ArmorShopGUI gui = this.openGUIs.get(playerUUID);
        
        if (gui != null) {
            gui.handleClick(event);
        }
    }

    @EventHandler
    public void onInventoryClose(final InventoryCloseEvent event) {
        final UUID playerUUID = event.getPlayer().getUniqueId();
        this.unregisterGUI(playerUUID);
    }
}