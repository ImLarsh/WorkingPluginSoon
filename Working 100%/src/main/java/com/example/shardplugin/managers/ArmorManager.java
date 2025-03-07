package com.example.shardplugin.managers;

import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.Color;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import com.example.shardplugin.ShardPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;

public class ArmorManager {
    
    private final ShardPlugin plugin;
    private final Map<String, Double> armorMultipliers;
    private final Map<String, Double> armorRolls;
    private final NamespacedKey armorSetKey;
    private final NamespacedKey multiplierKey;
    private final NamespacedKey rollsKey;
    
    public ArmorManager(final ShardPlugin plugin) {
        this.plugin = plugin;
        this.armorMultipliers = new HashMap<>();
        this.armorRolls = new HashMap<>();
        this.armorSetKey = new NamespacedKey(plugin, "armor_set");
        this.multiplierKey = new NamespacedKey(plugin, "multiplier");
        this.rollsKey = new NamespacedKey(plugin, "rolls");
        this.loadArmorSets();
    }
    
    private void loadArmorSets() {
        final ConfigurationSection armorSection = this.plugin.getConfig().getConfigurationSection("armor_sets");
        
        if (armorSection != null) {
            for (final String key : armorSection.getKeys(false)) {
                final ConfigurationSection pieces = armorSection.getConfigurationSection(key + ".pieces");
                if (pieces != null) {
                    for (String piece : pieces.getKeys(false)) {
                        String fullKey = key + "." + piece;
                        double multiplier = pieces.getDouble(piece + ".multiplier", 1.0);
                        double rolls = pieces.getDouble(piece + ".rolls", 1.0);
                        this.armorMultipliers.put(fullKey, multiplier);
                        this.armorRolls.put(fullKey, rolls);
                    }
                }
            }
        }
    }
    
    public double getArmorMultiplier(final Player player) {
        double totalMultiplier = 1.0;
        final ItemStack[] armor = player.getInventory().getArmorContents();
        
        if (armor != null) {
            for (ItemStack piece : armor) {
                if (piece != null && piece.hasItemMeta()) {
                    ItemMeta meta = piece.getItemMeta();
                    PersistentDataContainer container = meta.getPersistentDataContainer();
                    Double pieceMultiplier = container.get(this.multiplierKey, PersistentDataType.DOUBLE);
                    if (pieceMultiplier != null) {
                        totalMultiplier *= pieceMultiplier;
                    }
                }
            }
        }
        
        return totalMultiplier;
    }
    
    public double getArmorRolls(final Player player) {
        double totalRolls = 0.0;
        final ItemStack[] armor = player.getInventory().getArmorContents();
        
        if (armor != null) {
            for (ItemStack piece : armor) {
                if (piece != null && piece.hasItemMeta()) {
                    ItemMeta meta = piece.getItemMeta();
                    PersistentDataContainer container = meta.getPersistentDataContainer();
                    Double pieceRolls = container.get(this.rollsKey, PersistentDataType.DOUBLE);
                    if (pieceRolls != null) {
                        totalRolls += pieceRolls;
                    }
                }
            }
        }
        
        return Math.max(0.0, totalRolls);
    }
    
    public ItemStack createArmorPiece(final String setName, final Material defaultMaterial) {
        final ConfigurationSection setSection = this.plugin.getConfig().getConfigurationSection("armor_sets." + setName);
        
        if (setSection != null) {
            String armorType = "";
            if (defaultMaterial.name().contains("HELMET")) armorType = "helmet";
            else if (defaultMaterial.name().contains("CHESTPLATE")) armorType = "chestplate";
            else if (defaultMaterial.name().contains("LEGGINGS")) armorType = "leggings";
            else if (defaultMaterial.name().contains("BOOTS")) armorType = "boots";
            
            final ConfigurationSection armorSection = setSection.getConfigurationSection("armor." + armorType);
            final ConfigurationSection piecesSection = setSection.getConfigurationSection("pieces." + armorType);
            
            if (armorSection != null && piecesSection != null) {
                final Material material = Material.valueOf(armorSection.getString("material", defaultMaterial.name()));
                final ItemStack armorPiece = new ItemStack(material);
                final ItemMeta meta = armorPiece.getItemMeta();
                
                if (meta != null) {
                    // Set display name
                    final String displayName = armorSection.getString("display_name", setSection.getString("name", setName));
                    meta.setDisplayName(displayName);
                    
                    // Set custom model data if specified
                    if (armorSection.contains("custom_model_data")) {
                        meta.setCustomModelData(armorSection.getInt("custom_model_data"));
                    }
                    
                    // Set lore if specified
                    if (armorSection.contains("lore")) {
                        meta.setLore(armorSection.getStringList("lore"));
                    }
                    
                    // Hide all item flags
                    meta.addItemFlags(
                        ItemFlag.HIDE_ATTRIBUTES,
                        ItemFlag.HIDE_DYE,
                        ItemFlag.HIDE_ARMOR_TRIM,
                        ItemFlag.HIDE_ENCHANTS,
                        ItemFlag.HIDE_DESTROYS,
                        ItemFlag.HIDE_PLACED_ON,
                        ItemFlag.HIDE_POTION_EFFECTS,
                        ItemFlag.HIDE_UNBREAKABLE
                    );
                    
                    // Set color for leather armor
                    if (meta instanceof LeatherArmorMeta && armorSection.contains("color")) {
                        final ConfigurationSection colorSection = armorSection.getConfigurationSection("color");
                        if (colorSection != null) {
                            final int red = colorSection.getInt("red", 0);
                            final int green = colorSection.getInt("green", 0);
                            final int blue = colorSection.getInt("blue", 0);
                            ((LeatherArmorMeta) meta).setColor(Color.fromRGB(red, green, blue));
                        }
                    }
                    
                    // Set armor set identifier
                    final PersistentDataContainer container = meta.getPersistentDataContainer();
                    container.set(this.armorSetKey, PersistentDataType.STRING, setName);
                    
                    // Set individual piece multiplier and rolls
                    container.set(this.multiplierKey, PersistentDataType.DOUBLE, piecesSection.getDouble("multiplier", 1.0));
                    container.set(this.rollsKey, PersistentDataType.DOUBLE, piecesSection.getDouble("rolls", 1.0));

                    // Set base armor attributes to 0
                    meta.addAttributeModifier(
                        Attribute.GENERIC_ARMOR,
                        new AttributeModifier(
                            UUID.randomUUID(),
                            "generic.armor",
                            0,
                            AttributeModifier.Operation.ADD_NUMBER,
                            getEquipmentSlot(armorType)
                        )
                    );
                    meta.addAttributeModifier(
                        Attribute.GENERIC_ARMOR_TOUGHNESS,
                        new AttributeModifier(
                            UUID.randomUUID(),
                            "generic.armor_toughness",
                            0,
                            AttributeModifier.Operation.ADD_NUMBER,
                            getEquipmentSlot(armorType)
                        )
                    );
                    
                    // Make item unbreakable and hide the unbreakable flag
                    meta.setUnbreakable(true);
                    
                    armorPiece.setItemMeta(meta);
                    return armorPiece;
                }
            }
        }
        
        return null;
    }

    private EquipmentSlot getEquipmentSlot(String armorType) {
        switch (armorType.toLowerCase()) {
            case "helmet":
                return EquipmentSlot.HEAD;
            case "chestplate":
                return EquipmentSlot.CHEST;
            case "leggings":
                return EquipmentSlot.LEGS;
            case "boots":
                return EquipmentSlot.FEET;
            default:
                return EquipmentSlot.HAND;
        }
    }
}