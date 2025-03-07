package com.example.shardplugin.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import com.example.shardplugin.ShardPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PickaxeManager {
    
    private final ShardPlugin plugin;
    private final Map<String, Double> pickaxeMultipliers;
    private final Map<String, Double> pickaxeRolls;
    private final NamespacedKey pickaxeKey;
    private final NamespacedKey multiplierKey;
    private final NamespacedKey rollsKey;
    
    public PickaxeManager(final ShardPlugin plugin) {
        this.plugin = plugin;
        this.pickaxeMultipliers = new HashMap<>();
        this.pickaxeRolls = new HashMap<>();
        this.pickaxeKey = new NamespacedKey(plugin, "pickaxe");
        this.multiplierKey = new NamespacedKey(plugin, "multiplier");
        this.rollsKey = new NamespacedKey(plugin, "rolls");
        this.loadPickaxes();
    }
    
    private void loadPickaxes() {
        final ConfigurationSection pickaxeSection = this.plugin.getConfig().getConfigurationSection("pickaxes");
        
        if (pickaxeSection != null) {
            for (final String key : pickaxeSection.getKeys(false)) {
                final double multiplier = pickaxeSection.getDouble(key + ".multiplier", 1.0);
                final double rolls = pickaxeSection.getDouble(key + ".rolls", 1.0);
                this.pickaxeMultipliers.put(key, multiplier);
                this.pickaxeRolls.put(key, rolls);
            }
        }
    }
    
    public double getPickaxeMultiplier(final Player player) {
        double multiplier = 1.0;
        final ItemStack mainHand = player.getInventory().getItemInMainHand();
        
        if (mainHand != null && mainHand.hasItemMeta()) {
            final PersistentDataContainer container = mainHand.getItemMeta().getPersistentDataContainer();
            final String pickaxeName = container.get(this.pickaxeKey, PersistentDataType.STRING);
            
            if (pickaxeName != null) {
                multiplier = this.pickaxeMultipliers.getOrDefault(pickaxeName, 1.0);
            }
        }
        
        return multiplier;
    }
    
    public double getPickaxeRolls(final Player player) {
        double rolls = 0.0;
        final ItemStack mainHand = player.getInventory().getItemInMainHand();
        
        if (mainHand != null && mainHand.hasItemMeta()) {
            final PersistentDataContainer container = mainHand.getItemMeta().getPersistentDataContainer();
            final String pickaxeName = container.get(this.pickaxeKey, PersistentDataType.STRING);
            
            if (pickaxeName != null) {
                rolls = this.pickaxeRolls.getOrDefault(pickaxeName, 1.0);
            }
        }
        
        return rolls;
    }
    
    public ItemStack createPickaxe(final String pickaxeName) {
        final ConfigurationSection pickaxeSection = this.plugin.getConfig().getConfigurationSection("pickaxes." + pickaxeName);
        
        if (pickaxeSection != null) {
            final ConfigurationSection itemSection = pickaxeSection.getConfigurationSection("item");
            if (itemSection != null) {
                final Material material = Material.valueOf(itemSection.getString("material", "IRON_PICKAXE"));
                final ItemStack pickaxe = new ItemStack(material);
                final ItemMeta meta = pickaxe.getItemMeta();
                
                if (meta != null) {
                    // Set display name
                    final String displayName = itemSection.getString("display_name", pickaxeSection.getString("name", pickaxeName));
                    meta.setDisplayName(displayName);
                    
                    // Set custom model data if specified
                    if (itemSection.contains("custom_model_data")) {
                        meta.setCustomModelData(itemSection.getInt("custom_model_data"));
                    }
                    
                    // Set lore if specified
                    if (itemSection.contains("lore")) {
                        meta.setLore(itemSection.getStringList("lore"));
                    }
                    
                    // Set pickaxe identifier and stats
                    final PersistentDataContainer container = meta.getPersistentDataContainer();
                    container.set(this.pickaxeKey, PersistentDataType.STRING, pickaxeName);
                    container.set(this.multiplierKey, PersistentDataType.DOUBLE, pickaxeSection.getDouble("multiplier", 1.0));
                    container.set(this.rollsKey, PersistentDataType.DOUBLE, pickaxeSection.getDouble("rolls", 1.0));

                    // Hide attributes if specified
                    if (itemSection.getBoolean("hide_attributes", false)) {
                        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_DAMAGE, new AttributeModifier(UUID.randomUUID(), "generic.attack_damage", 0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));
                        meta.addAttributeModifier(Attribute.GENERIC_ATTACK_SPEED, new AttributeModifier(UUID.randomUUID(), "generic.attack_speed", 0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND));
                    }
                    
                    pickaxe.setItemMeta(meta);
                    return pickaxe;
                }
            }
        }
        
        return null;
    }
}