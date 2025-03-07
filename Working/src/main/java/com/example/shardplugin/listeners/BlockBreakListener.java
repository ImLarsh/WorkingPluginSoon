package com.example.shardplugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;
import org.bukkit.scheduler.BukkitRunnable;
import com.example.shardplugin.ShardPlugin;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class BlockBreakListener implements Listener {

    private final ShardPlugin plugin;
    private final Random random;

    public BlockBreakListener(final ShardPlugin plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    @EventHandler
    public void onBlockBreak(final BlockBreakEvent event) {
        final Player player = event.getPlayer();
        final ConfigurationSection blockSection = this.plugin.getConfigManager().getConfig().getConfigurationSection("settings.shard_blocks");

        if (blockSection != null) {
            final String blockType = event.getBlock().getType().name();

            if (blockSection.contains(blockType)) {
                final ConfigurationSection blockConfig = blockSection.getConfigurationSection(blockType);

                if (blockConfig != null) {
                    final int baseShards = blockConfig.getInt("amount");
                    final int basePickaxeShards = blockConfig.getInt("pickaxe_shard_amount");

                    // Get multipliers from both armor and pickaxe
                    final double armorMultiplier = this.plugin.getArmorManager().getArmorMultiplier(player);
                    final double pickaxeMultiplier = this.plugin.getPickaxeManager().getPickaxeMultiplier(player);
                    final double totalMultiplier = armorMultiplier * pickaxeMultiplier;

                    final int totalShards = (int) Math.round(baseShards * totalMultiplier);
                    final int totalPickaxeShards = (int) Math.round(basePickaxeShards * totalMultiplier);

                    // Give shards to player
                    this.plugin.getShardManager().addShards(player, totalShards);
                    this.plugin.getShardManager().addPickaxeShards(player, totalPickaxeShards);

                    // Create and give physical shard items
                    final ItemStack shardItem = this.createShardItem(totalShards);
                    final ItemStack pickaxeShardItem = this.createPickaxeShardItem(totalPickaxeShards);
                    
                    player.getInventory().addItem(shardItem);
                    player.getInventory().addItem(pickaxeShardItem);

                    // Send individual shard messages with delay
                    new BukkitRunnable() {
                        private int count = 0;
                        private int pickaxeCount = 0;

                        @Override
                        public void run() {
                            if (count < totalShards) {
                                plugin.getMessageManager().sendMessage(player, "shard_gain", "%amount%", "1");
                                count++;
                            } else if (pickaxeCount < totalPickaxeShards) {
                                plugin.getMessageManager().sendMessage(player, "pickaxe_shard_gain", "%amount%", "1");
                                pickaxeCount++;
                            } else {
                                this.cancel();
                            }
                        }
                    }.runTaskTimer(plugin, 0L, 2L); // Run every 2 ticks (0.1 seconds)

                    // Check if we should execute a command based on the command_chance
                    final double commandChance = blockConfig.getDouble("command_chance", 0.5);
                    if (this.random.nextDouble() < commandChance) {
                        final List<String> commands = blockConfig.getStringList("commands");
                        if (!commands.isEmpty()) {
                            // Execute a random command from the list
                            final String command = commands.get(this.random.nextInt(commands.size()));
                            final String formattedCommand = command.replace("%player%", player.getName());
                            this.plugin.getServer().dispatchCommand(
                                    this.plugin.getServer().getConsoleSender(),
                                    formattedCommand
                            );
                        }
                    }
                }
            }
        }
    }

    private ItemStack createShardItem(final int amount) {
        final ConfigurationSection currencySection = this.plugin.getConfigManager().getConfig().getConfigurationSection("settings.currency");
        final ItemStack shardItem = new ItemStack(
                Material.valueOf(currencySection.getString("material", "AMETHYST_SHARD")),
                amount
        );
        final ItemMeta meta = shardItem.getItemMeta();

        if (meta != null) {
            meta.setCustomModelData(currencySection.getInt("custom_model_data", 1001));
            shardItem.setItemMeta(meta);
        }

        return shardItem;
    }

    private ItemStack createPickaxeShardItem(final int amount) {
        final ConfigurationSection currencySection = this.plugin.getConfigManager().getConfig().getConfigurationSection("settings.currency.pickaxe");
        final ItemStack shardItem = new ItemStack(
                Material.valueOf(currencySection.getString("material", "AMETHYST_SHARD")),
                amount
        );
        final ItemMeta meta = shardItem.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(currencySection.getString("name", "§6⛏ Pickaxe Shard"));
            meta.setCustomModelData(currencySection.getInt("custom_model_data", 1004));
            meta.setLore(currencySection.getStringList("lore"));
            shardItem.setItemMeta(meta);
        }

        return shardItem;
    }
}