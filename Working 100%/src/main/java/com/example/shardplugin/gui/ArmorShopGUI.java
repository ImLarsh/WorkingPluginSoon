package com.example.shardplugin.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.Color;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import com.example.shardplugin.ShardPlugin;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.List;

public class ArmorShopGUI {
    private final ShardPlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private final Map<Integer, String> slotToArmorSet;
    private final Map<Integer, String> slotToPickaxe;
    private int currentPage;
    private final int maxPage;
    private final int enchantedShardSlot;
    private final int enchantedShardPage;
    private final int voidShardSlot;
    private final int voidShardPage;
    private final int enchantedPickaxeShardSlot;
    private final int enchantedPickaxeShardPage;

    public ArmorShopGUI(final ShardPlugin plugin, final Player player) {
        this.plugin = plugin;
        this.player = player;
        this.slotToArmorSet = new HashMap<>();
        this.slotToPickaxe = new HashMap<>();
        this.currentPage = 1;

        final ConfigurationSection guiConfig = plugin.getConfig().getConfigurationSection("settings.gui");
        final int size = guiConfig.getInt("size", 54);
        final String title = guiConfig.getString("title", "Equipment Shop")
                .replace("%page%", String.valueOf(this.currentPage));

        this.inventory = Bukkit.createInventory(null, size, title);

        final ConfigurationSection enchantedConfig = guiConfig.getConfigurationSection("enchanted_shard");
        this.enchantedShardSlot = enchantedConfig.getInt("slot", 4);
        this.enchantedShardPage = enchantedConfig.getInt("page", 1);

        final ConfigurationSection voidConfig = guiConfig.getConfigurationSection("void_shard");
        this.voidShardSlot = voidConfig.getInt("slot", 5);
        this.voidShardPage = voidConfig.getInt("page", 1);

        final ConfigurationSection enchantedPickaxeConfig = guiConfig.getConfigurationSection("enchanted_pickaxe_shard");
        this.enchantedPickaxeShardSlot = enchantedPickaxeConfig.getInt("slot", 25);
        this.enchantedPickaxeShardPage = enchantedPickaxeConfig.getInt("page", 2);

        int maxArmorPage = 1;
        int maxPickaxePage = 1;

        final ConfigurationSection armorSection = plugin.getConfig().getConfigurationSection("armor_sets");
        if (armorSection != null) {
            for (String key : armorSection.getKeys(false)) {
                maxArmorPage = Math.max(maxArmorPage, armorSection.getInt(key + ".page", 1));
            }
        }

        final ConfigurationSection pickaxeSection = plugin.getConfig().getConfigurationSection("pickaxes");
        if (pickaxeSection != null) {
            for (String key : pickaxeSection.getKeys(false)) {
                maxPickaxePage = Math.max(maxPickaxePage, pickaxeSection.getInt(key + ".page", 1));
            }
        }

        this.maxPage = Math.max(Math.max(maxArmorPage, maxPickaxePage),
                Math.max(this.enchantedShardPage, Math.max(this.voidShardPage, this.enchantedPickaxeShardPage)));

        this.setupGUI();
    }

    public void open() {
        this.player.openInventory(this.inventory);
        this.plugin.getInventoryListener().registerGUI(this.player.getUniqueId(), this);
    }

    private void setupGUI() {
        this.inventory.clear();
        this.slotToArmorSet.clear();
        this.slotToPickaxe.clear();

        this.setupArmorSets();
        this.setupPickaxes();

        if (this.currentPage == this.enchantedShardPage) {
            this.setupEnchantedShardConverter();
        }
        if (this.currentPage == this.voidShardPage) {
            this.setupVoidShardConverter();
        }
        if (this.currentPage == this.enchantedPickaxeShardPage) {
            this.setupEnchantedPickaxeShardConverter();
        }

        this.setupNavigation();
        this.fillEmptySlots();
    }

    private void setupEnchantedShardConverter() {
        final ConfigurationSection enchantedConfig = this.plugin.getConfig().getConfigurationSection("settings.gui.enchanted_shard");
        if (enchantedConfig != null) {
            final ItemStack enchantedItem = this.createGuiItem(enchantedConfig);
            this.inventory.setItem(this.enchantedShardSlot, enchantedItem);
        }
    }

    private void setupVoidShardConverter() {
        final ConfigurationSection voidConfig = this.plugin.getConfig().getConfigurationSection("settings.gui.void_shard");
        if (voidConfig != null) {
            final ItemStack voidItem = this.createGuiItem(voidConfig);
            this.inventory.setItem(this.voidShardSlot, voidItem);
        }
    }

    private void setupEnchantedPickaxeShardConverter() {
        final ConfigurationSection enchantedPickaxeConfig = this.plugin.getConfig().getConfigurationSection("settings.gui.enchanted_pickaxe_shard");
        if (enchantedPickaxeConfig != null) {
            final ItemStack enchantedPickaxeItem = this.createGuiItem(enchantedPickaxeConfig);
            this.inventory.setItem(this.enchantedPickaxeShardSlot, enchantedPickaxeItem);
        }
    }

    private void setupArmorSets() {
        final ConfigurationSection armorSection = this.plugin.getConfig().getConfigurationSection("armor_sets");

        if (armorSection != null) {
            for (String setName : armorSection.getKeys(false)) {
                final ConfigurationSection setConfig = armorSection.getConfigurationSection(setName);

                if (setConfig != null && setConfig.getInt("page", 1) == this.currentPage) {
                    final ConfigurationSection slots = setConfig.getConfigurationSection("slots");
                    final ConfigurationSection gui = setConfig.getConfigurationSection("gui");

                    if (slots != null && gui != null) {
                        this.setupArmorPieces(setName, slots, gui, setConfig);
                    }
                }
            }
        }
    }

    private void setupPickaxes() {
        final ConfigurationSection pickaxeSection = this.plugin.getConfig().getConfigurationSection("pickaxes");

        if (pickaxeSection != null) {
            for (String pickaxeName : pickaxeSection.getKeys(false)) {
                final ConfigurationSection pickaxeConfig = pickaxeSection.getConfigurationSection(pickaxeName);

                if (pickaxeConfig != null && pickaxeConfig.getInt("page", 1) == this.currentPage) {
                    final int slot = pickaxeConfig.getInt("slot", -1);
                    final ConfigurationSection gui = pickaxeConfig.getConfigurationSection("gui");

                    if (slot != -1 && gui != null) {
                        final ItemStack item = this.createGuiItem(gui);
                        
                        // Add attribute modifiers to set base values to 0
                        final ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            meta.addAttributeModifier(
                                Attribute.GENERIC_ATTACK_DAMAGE,
                                new AttributeModifier(
                                    UUID.randomUUID(),
                                    "generic.attack_damage",
                                    0,
                                    AttributeModifier.Operation.ADD_NUMBER,
                                    EquipmentSlot.HAND
                                )
                            );
                            meta.addAttributeModifier(
                                Attribute.GENERIC_ATTACK_SPEED,
                                new AttributeModifier(
                                    UUID.randomUUID(),
                                    "generic.attack_speed",
                                    0,
                                    AttributeModifier.Operation.ADD_NUMBER,
                                    EquipmentSlot.HAND
                                )
                            );
                            item.setItemMeta(meta);
                        }
                        
                        this.inventory.setItem(slot, item);
                        this.slotToPickaxe.put(slot, pickaxeName);
                    }
                }
            }
        }
    }

    private void setupArmorPieces(final String setName, final ConfigurationSection slots, final ConfigurationSection gui, final ConfigurationSection setConfig) {
        final String[] pieces = {"helmet", "chestplate", "leggings", "boots"};

        for (String piece : pieces) {
            final int slot = slots.getInt(piece, -1);
            if (slot != -1) {
                final ConfigurationSection pieceGui = gui.getConfigurationSection(piece);
                if (pieceGui != null) {
                    final ItemStack item = this.createGuiItem(pieceGui);
                    
                    // Add attribute modifiers to set base armor values to 0
                    final ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.addAttributeModifier(
                            Attribute.GENERIC_ARMOR,
                            new AttributeModifier(
                                UUID.randomUUID(),
                                "generic.armor",
                                0,
                                AttributeModifier.Operation.ADD_NUMBER,
                                getEquipmentSlot(piece)
                            )
                        );
                        meta.addAttributeModifier(
                            Attribute.GENERIC_ARMOR_TOUGHNESS,
                            new AttributeModifier(
                                UUID.randomUUID(),
                                "generic.armor_toughness",
                                0,
                                AttributeModifier.Operation.ADD_NUMBER,
                                getEquipmentSlot(piece)
                            )
                        );
                        item.setItemMeta(meta);
                    }
                    
                    this.inventory.setItem(slot, item);
                    this.slotToArmorSet.put(slot, setName + ":" + piece);
                }
            }
        }
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

    private void setupNavigation() {
        final ConfigurationSection navConfig = this.plugin.getConfig().getConfigurationSection("settings.gui.navigation");

        if (navConfig != null) {
            if (this.currentPage > 1) {
                final ConfigurationSection prevConfig = navConfig.getConfigurationSection("previous_page");
                if (prevConfig != null) {
                    final ItemStack prevItem = this.createGuiItem(prevConfig);
                    this.inventory.setItem(prevConfig.getInt("slot", 45), prevItem);
                }
            }

            if (this.currentPage < this.maxPage) {
                final ConfigurationSection nextConfig = navConfig.getConfigurationSection("next_page");
                if (nextConfig != null) {
                    final ItemStack nextItem = this.createGuiItem(nextConfig);
                    this.inventory.setItem(nextConfig.getInt("slot", 53), nextItem);
                }
            }

            final ConfigurationSection infoConfig = navConfig.getConfigurationSection("info");
            if (infoConfig != null) {
                final ItemStack infoItem = this.createGuiItem(infoConfig);
                final ItemMeta meta = infoItem.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(meta.getDisplayName()
                            .replace("%page%", String.valueOf(this.currentPage))
                            .replace("%total%", String.valueOf(this.maxPage)));
                    infoItem.setItemMeta(meta);
                }
                this.inventory.setItem(infoConfig.getInt("slot", 49), infoItem);
            }
        }
    }

    private void fillEmptySlots() {
        final ConfigurationSection fillerConfig = this.plugin.getConfig().getConfigurationSection("settings.gui.filler");

        if (fillerConfig != null) {
            final ItemStack filler = this.createGuiItem(fillerConfig);

            for (int i = 0; i < this.inventory.getSize(); i++) {
                if (this.inventory.getItem(i) == null) {
                    this.inventory.setItem(i, filler);
                }
            }
        }
    }

    private ItemStack createGuiItem(final ConfigurationSection config) {
        final Material material = Material.valueOf(config.getString("material", "STONE"));
        final ItemStack item = new ItemStack(material);
        final ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            if (config.contains("display_name")) {
                meta.setDisplayName(config.getString("display_name"));
            } else if (config.contains("name")) {
                meta.setDisplayName(config.getString("name"));
            }

            if (config.contains("custom_model_data")) {
                meta.setCustomModelData(config.getInt("custom_model_data"));
            }

            if (config.contains("lore")) {
                meta.setLore(config.getStringList("lore"));
            }

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

            if (meta instanceof LeatherArmorMeta && config.contains("color")) {
                final ConfigurationSection colorSection = config.getConfigurationSection("color");
                if (colorSection != null) {
                    final int red = colorSection.getInt("red", 0);
                    final int green = colorSection.getInt("green", 0);
                    final int blue = colorSection.getInt("blue", 0);
                    ((LeatherArmorMeta) meta).setColor(Color.fromRGB(red, green, blue));
                }
            }

            meta.setUnbreakable(true);

            item.setItemMeta(meta);
        }

        return item;
    }

    public void handleClick(final InventoryClickEvent event) {
        event.setCancelled(true);

        final ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        final int slot = event.getRawSlot();
        if (slot >= this.inventory.getSize()) return;

        if (slot == this.enchantedShardSlot && this.currentPage == this.enchantedShardPage) {
            if (event.getClick() == ClickType.LEFT) {
                this.handleEnchantedShardConversion();
            } else if (event.getClick() == ClickType.RIGHT) {
                this.handleEnchantedShardReversion();
            }
            return;
        }

        if (slot == this.voidShardSlot && this.currentPage == this.voidShardPage) {
            if (event.getClick() == ClickType.LEFT) {
                this.handleVoidShardConversion();
            } else if (event.getClick() == ClickType.RIGHT) {
                this.handleVoidShardReversion();
            }
            return;
        }

        if (slot == this.enchantedPickaxeShardSlot && this.currentPage == this.enchantedPickaxeShardPage) {
            if (event.getClick() == ClickType.LEFT) {
                this.handleEnchantedPickaxeShardConversion();
            } else if (event.getClick() == ClickType.RIGHT) {
                this.handleEnchantedPickaxeShardReversion();
            }
            return;
        }

        final ConfigurationSection navConfig = this.plugin.getConfig().getConfigurationSection("settings.gui.navigation");
        if (navConfig != null) {
            final int prevSlot = navConfig.getInt("previous_page.slot", 45);
            final int nextSlot = navConfig.getInt("next_page.slot", 53);

            if (slot == prevSlot && this.currentPage > 1) {
                this.currentPage--;
                this.setupGUI();
                return;
            }

            if (slot == nextSlot && this.currentPage < this.maxPage) {
                this.currentPage++;
                this.setupGUI();
                return;
            }
        }

        final String armorInfo = this.slotToArmorSet.get(slot);
        if (armorInfo != null) {
            final String[] parts = armorInfo.split(":");
            final String setName = parts[0];
            final String type = parts[1];

            final ConfigurationSection armorSection = this.plugin.getConfig().getConfigurationSection("armor_sets." + setName);
            if (armorSection == null) return;

            final String requiredSet = armorSection.getString("requires");
            if (!hasRequiredArmorPiece(requiredSet, type)) {
                final ConfigurationSection requiredArmorSection = this.plugin.getConfig().getConfigurationSection("armor_sets." + requiredSet + ".armor." + type);
                if (requiredArmorSection != null) {
                    final String requiredDisplayName = requiredArmorSection.getString("display_name", requiredSet + " " + type);
                    player.sendMessage("§cYou need the " + requiredDisplayName + " §cto purchase this!");
                }
                return;
            }

            int cost;
            boolean isEnchantedCost = armorSection.contains("cost_enchanted");
            boolean isVoidCost = armorSection.contains("cost_void");

            if (isVoidCost) {
                cost = armorSection.getInt("cost_void." + type, 0);
                if (!removeVoidShardItems(cost)) {
                    this.player.sendMessage("§cYou don't have enough void shards!");
                    return;
                }
            } else if (isEnchantedCost) {
                cost = armorSection.getInt("cost_enchanted." + type, 0);
                if (!removeEnchantedShardItems(cost)) {
                    this.player.sendMessage("§cYou don't have enough enchanted shards!");
                    return;
                }
            } else {
                cost = armorSection.getInt("cost." + type, 0);
                if (!removeShardItems(cost)) {
                    this.player.sendMessage("§cYou don't have enough shards!");
                    return;
                }
            }

            final ItemStack armor = this.plugin.getArmorManager().createArmorPiece(setName, clicked.getType());
            if (armor != null) {
                removeRequiredArmorPiece(requiredSet, type);
                this.player.getInventory().addItem(armor);
                this.player.sendMessage("§aYou purchased " + clicked.getItemMeta().getDisplayName() + "§a!");
            }
            return;
        }

        final String pickaxeName = this.slotToPickaxe.get(slot);
        if (pickaxeName != null) {
            final ConfigurationSection pickaxeSection = this.plugin.getConfig().getConfigurationSection("pickaxes." + pickaxeName);
            if (pickaxeSection == null) return;

            final String requiredPickaxe = pickaxeSection.getString("requires");
            if (!hasRequiredPickaxe(requiredPickaxe)) {
                final ConfigurationSection requiredPickaxeConfig = this.plugin.getConfig().getConfigurationSection("pickaxes." + requiredPickaxe + ".item");
                if (requiredPickaxeConfig != null) {
                    final String requiredDisplayName = requiredPickaxeConfig.getString("display_name", requiredPickaxe);
                    player.sendMessage("§cYou need the " + requiredDisplayName + " §cto purchase this!");
                }
                return;
            }

            int cost;
            boolean isEnchantedCost = pickaxeSection.contains("cost_enchanted_pickaxe");

            if (isEnchantedCost) {
                cost = pickaxeSection.getInt("cost_enchanted_pickaxe", 0);
                if (!removeEnchantedPickaxeShardItems(cost)) {
                    this.player.sendMessage("§cYou don't have enough enchanted pickaxe shards!");
                    return;
                }
            } else {
                cost = pickaxeSection.getInt("cost_pickaxe", 0);
                if (!removePickaxeShardItems(cost)) {
                    this.player.sendMessage("§cYou don't have enough pickaxe shards!");
                    return;
                }
            }

            final ItemStack pickaxe = this.plugin.getPickaxeManager().createPickaxe(pickaxeName);
            if (pickaxe != null) {
                removeRequiredPickaxe(requiredPickaxe);
                this.player.getInventory().addItem(pickaxe);
                this.player.sendMessage("§aYou purchased " + clicked.getItemMeta().getDisplayName() + "§a!");
            }
        }
    }

    private boolean removeShardItems(final int amount) {
        final ConfigurationSection currencyConfig = this.plugin.getConfig().getConfigurationSection("settings.currency");
        if (currencyConfig == null) return false;

        final Material shardMaterial = Material.valueOf(currencyConfig.getString("material", "AMETHYST_SHARD"));
        final int customModelData = currencyConfig.getInt("custom_model_data", 1001);

        int remainingAmount = amount;
        int totalShards = 0;

        // First count total available shards
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == shardMaterial && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasCustomModelData() && meta.getCustomModelData() == customModelData) {
                    totalShards += item.getAmount();
                }
            }
        }

        // Check if we have enough shards
        if (totalShards < amount) {
            return false;
        }

        // Remove shards
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length && remainingAmount > 0; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == shardMaterial && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasCustomModelData() && meta.getCustomModelData() == customModelData) {
                    if (item.getAmount() <= remainingAmount) {
                        remainingAmount -= item.getAmount();
                        player.getInventory().setItem(i, null);
                    } else {
                        item.setAmount(item.getAmount() - remainingAmount);
                        remainingAmount = 0;
                    }
                }
            }
        }

        return remainingAmount == 0;
    }

    private boolean removePickaxeShardItems(final int amount) {
        final ConfigurationSection currencyConfig = this.plugin.getConfig().getConfigurationSection("settings.currency.pickaxe");
        if (currencyConfig == null) return false;

        final Material shardMaterial = Material.valueOf(currencyConfig.getString("material", "AMETHYST_SHARD"));
        final int customModelData = currencyConfig.getInt("custom_model_data", 1004);

        int remainingAmount = amount;
        int totalShards = 0;

        // First count total available shards
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == shardMaterial && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasCustomModelData() && meta.getCustomModelData() == customModelData) {
                    totalShards += item.getAmount();
                }
            }
        }

        // Check if we have enough shards
        if (totalShards < amount) {
            return false;
        }

        // Remove shards
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length && remainingAmount > 0; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == shardMaterial && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasCustomModelData() && meta.getCustomModelData() == customModelData) {
                    if (item.getAmount() <= remainingAmount) {
                        remainingAmount -= item.getAmount();
                        player.getInventory().setItem(i, null);
                    } else {
                        item.setAmount(item.getAmount() - remainingAmount);
                        remainingAmount = 0;
                    }
                }
            }
        }

        return remainingAmount == 0;
    }

    private boolean removeEnchantedShardItems(final int amount) {
        final ConfigurationSection currencyConfig = this.plugin.getConfig().getConfigurationSection("settings.currency.enchanted");
        if (currencyConfig == null) return false;

        final Material shardMaterial = Material.valueOf(currencyConfig.getString("material", "AMETHYST_SHARD"));
        final int customModelData = currencyConfig.getInt("custom_model_data", 1002);

        int remainingAmount = amount;
        int totalShards = 0;

        // First count total available shards
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == shardMaterial && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasCustomModelData() && meta.getCustomModelData() == customModelData) {
                    totalShards += item.getAmount();
                }
            }
        }

        // Check if we have enough shards
        if (totalShards < amount) {
            return false;
        }

        // Remove shards
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length && remainingAmount > 0; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == shardMaterial && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasCustomModelData() && meta.getCustomModelData() == customModelData) {
                    if (item.getAmount() <= remainingAmount) {
                        remainingAmount -= item.getAmount();
                        player.getInventory().setItem(i, null);
                    } else {
                        item.setAmount(item.getAmount() - remainingAmount);
                        remainingAmount = 0;
                    }
                }
            }
        }

        return remainingAmount == 0;
    }

    private boolean removeEnchantedPickaxeShardItems(final int amount) {
        final ConfigurationSection currencyConfig = this.plugin.getConfig().getConfigurationSection("settings.currency.enchanted_pickaxe");
        if (currencyConfig == null) return false;

        final Material shardMaterial = Material.valueOf(currencyConfig.getString("material", "AMETHYST_SHARD"));
        final int customModelData = currencyConfig.getInt("custom_model_data", 1005);

        int remainingAmount = amount;
        int totalShards = 0;

        // First count total available shards
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == shardMaterial && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasCustomModelData() && meta.getCustomModelData() == customModelData) {
                    totalShards += item.getAmount();
                }
            }
        }

        // Check if we have enough shards
        if (totalShards < amount) {
            return false;
        }

        // Remove shards
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length && remainingAmount > 0; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == shardMaterial && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasCustomModelData() && meta.getCustomModelData() == customModelData) {
                    if (item.getAmount() <= remainingAmount) {
                        remainingAmount -= item.getAmount();
                        player.getInventory().setItem(i, null);
                    } else {
                        item.setAmount(item.getAmount() - remainingAmount);
                        remainingAmount = 0;
                    }
                }
            }
        }

        return remainingAmount == 0;
    }

    private boolean removeVoidShardItems(final int amount) {
        final ConfigurationSection currencyConfig = this.plugin.getConfig().getConfigurationSection("settings.currency.void");
        if (currencyConfig == null) return false;

        final Material shardMaterial = Material.valueOf(currencyConfig.getString("material", "AMETHYST_SHARD"));
        final int customModelData = currencyConfig.getInt("custom_model_data", 1003);

        int remainingAmount = amount;
        int totalShards = 0;

        // First count total available shards
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == shardMaterial && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasCustomModelData() && meta.getCustomModelData() == customModelData) {
                    totalShards += item.getAmount();
                }
            }
        }

        // Check if we have enough shards
        if (totalShards < amount) {
            return false;
        }

        // Remove shards
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length && remainingAmount > 0; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == shardMaterial && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null && meta.hasCustomModelData() && meta.getCustomModelData() == customModelData) {
                    if (item.getAmount() <= remainingAmount) {
                        remainingAmount -= item.getAmount();
                        player.getInventory().setItem(i, null);
                    } else {
                        item.setAmount(item.getAmount() - remainingAmount);
                        remainingAmount = 0;
                    }
                }
            }
        }

        return remainingAmount == 0;
    }

    private void handleEnchantedShardConversion() {
        final ConfigurationSection currencyConfig = this.plugin.getConfig().getConfigurationSection("settings.currency");
        final ConfigurationSection enchantedConfig = currencyConfig.getConfigurationSection("enchanted");
        if (enchantedConfig == null) return;

        final int conversionRate = enchantedConfig.getInt("conversion_rate", 10);

        if (!removeShardItems(conversionRate)) {
            this.plugin.getMessageManager().sendMessage(player, "insufficient_shards");
            return;
        }

        final Material material = Material.valueOf(enchantedConfig.getString("material", "AMETHYST_SHARD"));
        final ItemStack enchantedShard = new ItemStack(material);
        final ItemMeta meta = enchantedShard.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(enchantedConfig.getString("name", "§d✨ Enchanted Shard"));
            meta.setCustomModelData(enchantedConfig.getInt("custom_model_data", 1002));
            meta.setLore(enchantedConfig.getStringList("lore"));
            enchantedShard.setItemMeta(meta);
        }
        this.player.getInventory().addItem(enchantedShard);

        this.plugin.getMessageManager().sendMessage(
                player,
                "enchanted_shard_purchase",
                "%regular%", String.valueOf(conversionRate),
                "%enchanted%", "1"
        );
    }

    private void handleEnchantedShardReversion() {
        final ConfigurationSection currencyConfig = this.plugin.getConfig().getConfigurationSection("settings.currency");
        final ConfigurationSection enchantedConfig = currencyConfig.getConfigurationSection("enchanted");
        if (enchantedConfig == null) return;

        final int conversionRate = enchantedConfig.getInt("conversion_rate", 10);

        if (!removeEnchantedShardItems(1)) {
            this.plugin.getMessageManager().sendMessage(player, "insufficient_enchanted_shards");
            return;
        }

        final Material material = Material.valueOf(currencyConfig.getString("material", "AMETHYST_SHARD"));
        final ItemStack regularShard = new ItemStack(material, conversionRate);
        final ItemMeta meta = regularShard.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(currencyConfig.getInt("custom_model_data", 1001));
            regularShard.setItemMeta(meta);
        }
        this.player.getInventory().addItem(regularShard);

        this.plugin.getMessageManager().sendMessage(
                player,
                "enchanted_shard_revert",
                "%enchanted%", "1",
                "%regular%", String.valueOf(conversionRate)
        );
    }

    private void handleVoidShardConversion() {
        final ConfigurationSection currencyConfig = this.plugin.getConfig().getConfigurationSection("settings.currency");
        final ConfigurationSection voidConfig = currencyConfig.getConfigurationSection("void");
        if (voidConfig == null) return;

        final int conversionRate = voidConfig.getInt("conversion_rate", 5);

        if (!removeEnchantedShardItems(conversionRate)) {
            this.plugin.getMessageManager().sendMessage(player, "insufficient_enchanted_shards");
            return;
        }

        final Material material = Material.valueOf(voidConfig.getString("material", "AMETHYST_SHARD"));
        final ItemStack voidShard = new ItemStack(material);
        final ItemMeta meta = voidShard.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(voidConfig.getString("name", "§5✧ Void Shard"));
            meta.setCustomModelData(voidConfig.getInt("custom_model_data", 1003));
            meta.setLore(voidConfig.getStringList("lore"));
            voidShard.setItemMeta(meta);
        }
        this.player.getInventory().addItem(voidShard);

        this.plugin.getMessageManager().sendMessage(
                player,
                "void_shard_purchase",
                "%enchanted%", String.valueOf(conversionRate),
                "%void%", "1"
        );
    }

    private void handleVoidShardReversion() {
        final ConfigurationSection currencyConfig = this.plugin.getConfig().getConfigurationSection("settings.currency");
        final ConfigurationSection voidConfig = currencyConfig.getConfigurationSection("void");
        if (voidConfig == null) return;

        final int conversionRate = voidConfig.getInt("conversion_rate", 5);

        if (!removeVoidShardItems(1)) {
            this.plugin.getMessageManager().sendMessage(player, "insufficient_void_shards");
            return;
        }

        final Material material = Material.valueOf(currencyConfig.getString("material", "AMETHYST_SHARD"));
        final ItemStack enchantedShard = new ItemStack(material, conversionRate);
        final ItemMeta meta = enchantedShard.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(currencyConfig.getInt("enchanted.custom_model_data", 1002));
            enchantedShard.setItemMeta(meta);
        }
        this.player.getInventory().addItem(enchantedShard);

        this.plugin.getMessageManager().sendMessage(
                player,
                "void_shard_revert",
                "%void%", "1",
                "%enchanted%", String.valueOf(conversionRate)
        );
    }

    private void handleEnchantedPickaxeShardConversion() {
        final ConfigurationSection currencyConfig = this.plugin.getConfig().getConfigurationSection("settings.currency");
        final ConfigurationSection enchantedPickaxeConfig = currencyConfig.getConfigurationSection("enchanted_pickaxe");
        if (enchantedPickaxeConfig == null) return;

        final int conversionRate = enchantedPickaxeConfig.getInt("conversion_rate", 8);

        if (!removePickaxeShardItems(conversionRate)) {
            this.plugin.getMessageManager().sendMessage(player, "insufficient_pickaxe_shards");
            return;
        }

        final Material material = Material.valueOf(enchantedPickaxeConfig.getString("material", "AMETHYST_SHARD"));
        final ItemStack enchantedPickaxeShard = new ItemStack(material);
        final ItemMeta meta = enchantedPickaxeShard.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(enchantedPickaxeConfig.getString("name", "§d⛏ Enchanted Pickaxe Shard"));
            meta.setCustomModelData(enchantedPickaxeConfig.getInt("custom_model_data", 1005));
            meta.setLore(enchantedPickaxeConfig.getStringList("lore"));
            enchantedPickaxeShard.setItemMeta(meta);
        }
        this.player.getInventory().addItem(enchantedPickaxeShard);

        this.plugin.getMessageManager().sendMessage(
                player,
                "enchanted_pickaxe_shard_purchase",
                "%regular%", String.valueOf(conversionRate),
                "%enchanted%", "1"
        );
    }

    private void handleEnchantedPickaxeShardReversion() {
        final ConfigurationSection currencyConfig = this.plugin.getConfig().getConfigurationSection("settings.currency");
        final ConfigurationSection enchantedPickaxeConfig = currencyConfig.getConfigurationSection("enchanted_pickaxe");
        if (enchantedPickaxeConfig == null) return;

        final int conversionRate = enchantedPickaxeConfig.getInt("conversion_rate", 8);

        if (!removeEnchantedPickaxeShardItems(1)) {
            this.plugin.getMessageManager().sendMessage(player, "insufficient_enchanted_pickaxe_shards");
            return;
        }

        final Material material = Material.valueOf(currencyConfig.getString("material", "AMETHYST_SHARD"));
        final ItemStack pickaxeShard = new ItemStack(material, conversionRate);
        final ItemMeta meta = pickaxeShard.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(currencyConfig.getInt("pickaxe.custom_model_data", 1004));
            pickaxeShard.setItemMeta(meta);
        }
        this.player.getInventory().addItem(pickaxeShard);

        this.plugin.getMessageManager().sendMessage(
                player,
                "enchanted_pickaxe_shard_revert",
                "%enchanted%", "1",
                "%regular%", String.valueOf(conversionRate)
        );
    }

    private boolean hasRequiredArmorPiece(final String requiredSet, final String type) {
        if (requiredSet == null) return true;

        final ConfigurationSection armorSection = this.plugin.getConfig().getConfigurationSection("armor_sets." + requiredSet + ".armor." + type);
        if (armorSection == null) return false;

        final String requiredDisplayName = armorSection.getString("display_name");
        if (requiredDisplayName == null) return false;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta.hasDisplayName() && meta.getDisplayName().equals(requiredDisplayName)) {
                    String itemType = item.getType().name().toLowerCase();
                    if (itemType.contains(type.toLowerCase())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean hasRequiredPickaxe(final String requiredPickaxe) {
        if (requiredPickaxe == null) return true;

        final ConfigurationSection pickaxeSection = this.plugin.getConfig().getConfigurationSection("pickaxes." + requiredPickaxe + ".item");
        if (pickaxeSection == null) return false;

        final String requiredDisplayName = pickaxeSection.getString("display_name");
        if (requiredDisplayName == null) return false;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta.hasDisplayName() && meta.getDisplayName().equals(requiredDisplayName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private void removeRequiredArmorPiece(final String requiredSet, final String type) {
        if (requiredSet == null) return;

        final ConfigurationSection armorSection = this.plugin.getConfig().getConfigurationSection("armor_sets." + requiredSet + ".armor." + type);
        if (armorSection == null) return;

        final String requiredDisplayName = armorSection.getString("display_name");
        if (requiredDisplayName == null) return;

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta.hasDisplayName() && meta.getDisplayName().equals(requiredDisplayName)) {
                    String itemType = item.getType().name().toLowerCase();
                    if (itemType.contains(type.toLowerCase())) {
                        player.getInventory().setItem(i, null);
                        return;
                    }
                }
            }
        }
    }

    private void removeRequiredPickaxe(final String requiredPickaxe) {
        if (requiredPickaxe == null) return;

        final ConfigurationSection pickaxeSection = this.plugin.getConfig().getConfigurationSection("pickaxes." + requiredPickaxe + ".item");
        if (pickaxeSection == null) return;

        final String requiredDisplayName = pickaxeSection.getString("display_name");
        if (requiredDisplayName == null) return;

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta.hasDisplayName() && meta.getDisplayName().equals(requiredDisplayName)) {
                    player.getInventory().setItem(i, null);
                    return;
                }
            }
        }
    }
}