package me.spaff.hopperrework.hopper;

import me.spaff.hopperrework.Constants;
import me.spaff.hopperrework.Main;
import me.spaff.spflib.utils.InventoryUtils;
import me.spaff.spflib.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.Hopper;
import org.bukkit.block.data.Directional;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;

public class HopperManager {
    private static BukkitTask task;
    private static final Set<Hopper> hoppers = new HashSet<>();

    public static void startTransferTask() {
        if (task != null) return;
        int intervalTicks = Main.getTicksPerHopperTransfer();

        task = new BukkitRunnable() {
            @Override
            public void run() {
                for (var hopper : hoppers) {
                    transferItemsToLinkedContainer(hopper);
                }
            }
        }.runTaskTimer(Main.getInstance(), intervalTicks, intervalTicks);
    }

    public static void transferItemsToLinkedContainer(Hopper hopper) {
        if (!isValidForTransfer(hopper)) return;
        if (isConnectedDirectlyToContainer(hopper)) return;

        HopperData hopperData = new HopperData(hopper);
        hopperData.load();

        Container container = (Container) hopperData.getLinkedContainer().getBlock().getState();

        int amountToTransfer = hopperData.hasFasterTransferUpgrade() ? 2 : 1;
        transferItems(hopper.getInventory(), container.getInventory(), amountToTransfer);
    }

    public static void transferItems(Inventory source, Inventory destination, int amountToTransfer) {
        HopperData hopperData = null;
        if (destination.getLocation().getBlock().getType().equals(Material.HOPPER)) {
            Hopper hopper = (Hopper) destination.getLocation().getBlock().getState();

            hopperData = new HopperData(hopper);
            hopperData.load();
        }

        // Transfer items from hopper to linked container
        for (var item : source.getContents()) {
            if (ItemUtils.isNull(item)) continue;
            if (!InventoryUtils.canFitItem(destination, item)) continue;
            if (hopperData != null && !hopperData.canFilterItem(item)) continue;

            ItemStack transferItem = item.clone();

            // If the item amount is 0 or below remove the item
            if ((item.getAmount() - amountToTransfer) <= 0) {
                source.remove(item);
            }
            else {
                // If the item amount is greater than amount to transfer
                // subtract the amount from the item
                if (item.getAmount() > amountToTransfer) {
                    item.setAmount(item.getAmount() - amountToTransfer);
                    transferItem.setAmount(amountToTransfer);
                }
                // If the item amount is equal or below the amount to transfer
                // remove the item
                else {
                    source.remove(item);
                }
            }

            destination.addItem(transferItem);
            break;
        }
    }

    private static boolean isValidForTransfer(Hopper hopper) {
        HopperData hopperData = new HopperData(hopper);
        hopperData.load();

        // Check if hopper is locked by redstone signal
        if (hopper.getBlock().getBlockPower() > 0) {
            return false;
        }

        // Check if hopper is linked to container
        if (!hopperData.isLinked()) {
            return false;
        }

        // Check if block at link location is a container
        Location linkLocation = hopperData.getLinkedContainer();
        if (!(linkLocation.getBlock().getState() instanceof Container)) {
            return false;
        }

        // Check distance between hopper and linked container
        int maxLinkDistance = hopperData.hasSignalAmplifierUpgrade() ? Constants.EXTENDED_LINK_MAX_DISTANCE : Constants.NORMAL_LINK_MAX_DISTANCE;
        if (hopper.getLocation().distance(linkLocation) > maxLinkDistance) {
            return false;
        }

        return true;
    }

    public static void addHoppersFromLoadedChunks() {
        // Iterate through worlds
        for (var world : Bukkit.getWorlds()) {
            // Get loaded chunks from these worlds
            for (var chunk : world.getLoadedChunks()) {
                // Find any hoppers and check if they are linked
                for (BlockState blockState : chunk.getTileEntities()) {
                    if (!blockState.getBlock().getType().equals(Material.HOPPER)) continue;
                    Hopper hopper = (Hopper) blockState;

                    HopperData hopperData = new HopperData(hopper);
                    hopperData.load();

                    if (hopperData.isLinked())
                        addHopper(hopper);
                }
            }
        }
    }

    public static void addHopper(Hopper hopper) {
        hoppers.add(hopper);
    }

    public static void removeHopper(Hopper hopper) {
        hoppers.remove(hopper);
    }

    public static Set<Hopper> getHoppers() {
        return hoppers;
    }

    public static boolean isConnectedDirectlyToContainer(Hopper hopper) {
        Directional directional = (Directional) hopper.getBlockData();

        Block connectedBlock = hopper.getWorld().getBlockAt(
                hopper.getLocation().getBlockX() + directional.getFacing().getModX(),
                hopper.getLocation().getBlockY() + directional.getFacing().getModY(),
                hopper.getLocation().getBlockZ() + directional.getFacing().getModZ()
        );

        return connectedBlock.getState() instanceof Container;
    }
}