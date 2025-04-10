package me.spaff.hopperrework.hopper;

import me.spaff.hopperrework.Constants;
import me.spaff.hopperrework.Main;
import me.spaff.spflib.builder.ItemBuilder;
import me.spaff.spflib.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public enum HopperUpgrade {
    VACUUM("Vacuum", () -> {
        ItemStack item = new ItemBuilder.Builder(Material.WIND_CHARGE)
                .name("&eVacuum")
                .lore(List.of(
                        "&8Hopper Upgrade",
                        "&7Sucks in nearby items."
                )).build().getItem();

        // Set item id
        ItemUtils.setPersistentData(
                item,
                new NamespacedKey(Constants.HOPPER_DATA_NAMESPACE, Constants.HOPPER_DATA_UPGRADES_KEY),
                PersistentDataType.STRING,
                "VACUUM"
        );

        return item;
    }),
    SIGNAL_AMPLIFIER("Signal Amplifier", () -> {
        ItemStack item = new ItemBuilder.Builder(Material.REDSTONE_TORCH)
                .name("&eSignal Amplifier")
                .lore(List.of(
                        "&8Hopper Upgrade",
                        "&7Doubles container linking distance."
                )).build().getItem();

        // Set item id
        ItemUtils.setPersistentData(
                item,
                new NamespacedKey(Constants.HOPPER_DATA_NAMESPACE, Constants.HOPPER_DATA_UPGRADES_KEY),
                PersistentDataType.STRING,
                "SIGNAL_AMPLIFIER"
        );

        return item;
    }),
    FASTER_TRANSFER("Faster Transfer", () -> {
        ItemStack item = new ItemBuilder.Builder(Material.GRINDSTONE)
                .name("&ePerfect Gear")
                .lore(List.of(
                        "&8Hopper Upgrade",
                        "&7Doubles item transfer amount."
                )).build().getItem();

        // Set item id
        ItemUtils.setPersistentData(
                item,
                new NamespacedKey(Constants.HOPPER_DATA_NAMESPACE, Constants.HOPPER_DATA_UPGRADES_KEY),
                PersistentDataType.STRING,
                "FASTER_TRANSFER"
        );

        return item;
    });

    private final String name;
    private final ItemStack item;
    HopperUpgrade(String name, Supplier<ItemStack> item) {
        this.name = name;
        this.item = item.get();
    }

    public String getName() {
        return name;
    }

    public ItemStack getItem() {
        // Set random UUID to make item not stackable
        ItemUtils.setPersistentData(
                this.item,
                new NamespacedKey("dummy", "id"),
                PersistentDataType.STRING,
                UUID.randomUUID().toString()
        );

        return item;
    }
}
