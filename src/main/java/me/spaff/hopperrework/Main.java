package me.spaff.hopperrework;

import me.spaff.hopperrework.cmd.Commands;
import me.spaff.hopperrework.hopper.HopperManager;
import me.spaff.hopperrework.hopper.HopperUpgrade;
import me.spaff.hopperrework.listener.PlayerListener;
import me.spaff.hopperrework.listener.ServerListener;
import me.spaff.spflib.SPFLib;
import me.spaff.spflib.file.FileManager;
import me.spaff.spflib.utils.RecipesUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class Main extends JavaPlugin {
    private static final String version = "1.0.1";
    private static JavaPlugin instance;

    // Spigot configuration
    private static int TICKS_PER_HOPPER_TRANSFER = 8;

    @Override
    public void onEnable() {
        instance = this;
        SPFLib.init(this);

        HopperManager.addHoppersFromLoadedChunks();
        HopperManager.startTransferTask();

        registerListeners();
        registerCommand();
        readSpigotConfig();

        registerRecipes();

        Bukkit.getOnlinePlayers().forEach(RecipesUtils::discoverRecipes);
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    private void registerListeners() {
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        Bukkit.getPluginManager().registerEvents(new ServerListener(), this);
    }

    private void registerCommand() {
        this.getCommand("hpr").setExecutor(new Commands());
    }

    public void readSpigotConfig() {
        FileManager fileManager = new FileManager(System.getProperty("user.dir"), "spigot");
        TICKS_PER_HOPPER_TRANSFER = (int) fileManager.read("world-settings.default.ticks-per.hopper-transfer");
    }

    public static int getTicksPerHopperTransfer() {
        return TICKS_PER_HOPPER_TRANSFER;
    }

    private void registerRecipes() {
        ItemStack[] results = {
                HopperUpgrade.FASTER_TRANSFER.getItem(),
                HopperUpgrade.SIGNAL_AMPLIFIER.getItem(),
                HopperUpgrade.VACUUM.getItem()
        };

        String[] namespaceKeys = {
                "faster_transfer",
                "signal_amplifier",
                "vacuum"
        };

        String[][] shapes = {
                {
                        " CR",
                        "CHC",
                        "RC "
                },
                {
                        "RTR",
                        " S ",
                        " S "
                },
                {
                        "T T",
                        "IRI",
                        " I "
                }
        };

        Map<String, Map<Character, Material>> ingredients = Map.of(
                "faster_transfer", Map.of(
                        'C', Material.COBBLESTONE,
                        'H', Material.CHAIN,
                        'R', Material.REDSTONE),
                "signal_amplifier", Map.of(
                        'R', Material.REPEATER,
                        'T', Material.REDSTONE_TORCH,
                        'S', Material.STICK),
                "vacuum", Map.of(
                        'T', Material.REDSTONE_TORCH,
                        'I', Material.IRON_INGOT,
                        'R', Material.REDSTONE)
        );

        for (int i = 0; i < HopperUpgrade.values().length; i++) {
            ItemStack result = results[i];
            String namespaceKey = namespaceKeys[i];
            String[] shape = shapes[i];

            NamespacedKey key = new NamespacedKey(Main.getInstance(), namespaceKey);
            ShapedRecipe recipe = new ShapedRecipe(key, result);

            recipe.shape(shape);

            for (var entry : ingredients.entrySet()) {
                if (!entry.getKey().equals(namespaceKey)) continue;

                for (var ingredient : entry.getValue().entrySet()) {
                    recipe.setIngredient(ingredient.getKey(), ingredient.getValue());
                }
            }

            Bukkit.addRecipe(recipe);
            RecipesUtils.addRecipeNamespaceKey(key);
        }
    }

    public static String getVersion() {
        return version;
    }

    public static JavaPlugin getInstance() {
        return instance;
    }
}