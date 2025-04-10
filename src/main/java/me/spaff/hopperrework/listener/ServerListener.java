package me.spaff.hopperrework.listener;

import me.spaff.hopperrework.Main;
import me.spaff.hopperrework.hopper.*;
import me.spaff.hopperrework.menu.ViewerData;
import me.spaff.hopperrework.utils.Utils;
import me.spaff.spflib.utils.InventoryUtils;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.HopperInventorySearchEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;

public class ServerListener implements Listener {
    private static int hopperVacuumCheckDelayTicks = 0;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHopperInventorySearchEvent(HopperInventorySearchEvent e) {
        if (hopperVacuumCheckDelayTicks < Main.getTicksPerHopperTransfer()) {
            hopperVacuumCheckDelayTicks++;
            return;
        }

        Hopper hopper = (Hopper) e.getBlock().getState();
        HopperData hopperData = new HopperData(hopper);
        hopperData.load();

        if (!hopperData.hasVacuumUpgrade()) return;

        for (Entity entity : e.getBlock().getWorld().getNearbyEntities(e.getBlock().getLocation(), 2, 0.5, 2)) {
            if (!(entity instanceof Item item)) continue;
            if (!hopperData.canFilterItem(item.getItemStack())) continue;

            ItemStack droppedItem = item.getItemStack().clone();

            for (int amount = droppedItem.getAmount(); amount > 0; amount--) {
                droppedItem.setAmount(amount);

                if (InventoryUtils.canFitItem(hopper.getInventory(), droppedItem)) {
                    item.getItemStack().setAmount(item.getItemStack().getAmount() - amount);
                    hopper.getInventory().addItem(droppedItem);

                    entity.getWorld().spawnParticle(
                            Particle.HAPPY_VILLAGER,
                            entity.getLocation().clone().add(0, 0.35, 0),
                            5,
                            0.1,
                            0.1,
                            0.1,
                            0.15
                    );
                    break;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryMoveItemEvent(InventoryMoveItemEvent e) {
        ItemStack transferItem = e.getItem();

        // Check if source is hopper if it is get destination and transfer extra items from hopper to destination
        Block sourceBlock = e.getSource().getLocation().getBlock();
        if (sourceBlock.getType().equals(Material.HOPPER)) {
            Hopper sourceHopper = (Hopper) sourceBlock.getState();

            HopperData sourceHopperData = new HopperData(sourceHopper);
            sourceHopperData.load();

            // Transfer extra item if hopper has the upgrade
            if (sourceHopperData.getUpgrades().contains(HopperUpgrade.FASTER_TRANSFER)) {
                HopperManager.transferItems(e.getSource(), e.getDestination(), 1);
            }
        }

        // Check whitelist/blacklist when transferring item to a hopper
        Block destinationBlock = e.getDestination().getLocation().getBlock();
        if (destinationBlock.getType().equals(Material.HOPPER)) {
            Hopper destinationHopper = (Hopper) destinationBlock.getState();

            HopperData destinationHopperData = new HopperData(destinationHopper);
            destinationHopperData.load();

            // Cancel event if can't transfer items
            e.setCancelled(!destinationHopperData.canFilterItem(transferItem));

            if (!destinationHopperData.canFilterItem(transferItem))
                return;

            // Transfer extra items to hopper if has upgrade
            if (destinationHopperData.getUpgrades().contains(HopperUpgrade.FASTER_TRANSFER)) {
                HopperManager.transferItems(e.getSource(), e.getDestination(), 1);
            }
        }
    }

    @EventHandler
    public void onChunkLoadEvent(ChunkLoadEvent e) {
        if (e.isNewChunk()) return;
        Chunk chunk = e.getChunk();

        for (BlockState blockState : chunk.getTileEntities()) {
            if (!blockState.getBlock().getType().equals(Material.HOPPER)) continue;
            Hopper hopper = (Hopper) blockState;
            HopperManager.addHopper(hopper);
        }
    }

    @EventHandler
    public void onChunkUnloadEvent(ChunkUnloadEvent e) {
        Chunk chunk = e.getChunk();

        for (BlockState blockState : chunk.getTileEntities()) {
            if (!blockState.getBlock().getType().equals(Material.HOPPER)) continue;
            Hopper hopper = (Hopper) blockState;
            HopperManager.removeHopper(hopper);
        }
    }

    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent e) {
        if (Utils.isHopperUpgrade(e.getItemInHand())) {
            e.setCancelled(true);
            return;
        }

        if (!e.getBlockPlaced().getType().equals(Material.HOPPER))
            return;

        Hopper hopper = (Hopper) e.getBlockPlaced().getState();
        HopperManager.addHopper(hopper);
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent e) {
        handleHopperBreaking(e.getBlock());
        handleContainerBreaking(e.getBlock());
    }

    @EventHandler
    public void onEntityExplodeEvent(EntityExplodeEvent e) {
        for (Block block : e.blockList()) {
            handleHopperBreaking(block);
            handleContainerBreaking(block);
        }
    }

    private void handleContainerBreaking(Block block) {
        if (!(block.getState() instanceof Container)) return;

        // Checks if broken container was linked
        // if it was find the hopper it was linked to and
        // clear the linked container location
        for (var hopper : HopperManager.getHoppers()) {
            HopperData hopperData = new HopperData(hopper);
            hopperData.load();

            if (!hopperData.isLinked()) continue;
            if (!hopperData.getLinkedContainer().equals(block.getLocation())) continue;
            
            hopperData.setLinkedContainer(null);
            hopperData.save();
            break;
        }
    }

    private void handleHopperBreaking(Block block) {
        if (!block.getType().equals(Material.HOPPER)) return;
        Hopper hopper = (Hopper) block.getState();
        HopperManager.removeHopper(hopper);

        HopperData hopperData = new HopperData(hopper);
        hopperData.load();

        // Kick whoever is viewing this hopper menu
        ViewerData.clearAndClose(block.getLocation());

        // Check if someone is linking this hopper and cancel it
        if (ContainerLinking.isBeingLinked(hopper))
            ContainerLinking.getLinkInstance(hopper).stop();

        // Drop any upgrades the hopper could have
        hopperData.getUpgrades().forEach(upgrade -> {
            block.getWorld().dropItem(block.getLocation().clone().add(0.5, 0.5, 0.5), upgrade.getItem());
        });

        hopperData.clear();
    }
}
