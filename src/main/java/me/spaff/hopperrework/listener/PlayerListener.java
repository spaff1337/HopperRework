package me.spaff.hopperrework.listener;

import me.spaff.hopperrework.Constants;
import me.spaff.hopperrework.hopper.ContainerLinking;
import me.spaff.hopperrework.hopper.HopperData;
import me.spaff.hopperrework.hopper.HopperUpgrade;
import me.spaff.hopperrework.menu.HopperMenu;
import me.spaff.hopperrework.menu.ViewerData;
import me.spaff.hopperrework.utils.Utils;
import me.spaff.spflib.utils.BukkitUtils;
import me.spaff.spflib.utils.RecipesUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {
    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent e) {
        RecipesUtils.discoverRecipes(e.getPlayer());
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent e) {
        if (ContainerLinking.isPlayerLinking(e.getPlayer()))
            ContainerLinking.getPlayerLink(e.getPlayer()).stop();
        ViewerData.flush();
    }

    @EventHandler
    public void onPrepareItemCraftEvent(PrepareItemCraftEvent e) {
        if (e.getRecipe() == null || !Utils.isHopperUpgrade(e.getRecipe().getResult())) return;
        ItemStack result = e.getRecipe().getResult();

        // To make item not stack
        HopperUpgrade upgrade = Utils.getHopperUpgrade(result);
        e.getInventory().setResult(upgrade.getItem());
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Block clickedBlock = e.getClickedBlock();

        if (Utils.isHopperUpgrade(e.getItem())) {
            e.setCancelled(true);
            return;
        }

        // Handle opening a hopper
        if (e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
            if (clickedBlock == null || !clickedBlock.getType().equals(Material.HOPPER)) return;
            if (e.isBlockInHand() && player.isSneaking()) return;

            e.setCancelled(true);

            new HopperMenu(player, (Hopper) clickedBlock.getState()).open();
        }
        // Handle linking container
        else if (e.getAction().equals(Action.LEFT_CLICK_BLOCK)) {
            if (!ContainerLinking.isPlayerLinking(player)) return;
            e.setCancelled(true);

            ContainerLinking link = ContainerLinking.getPlayerLink(player);
            if (!link.isLinked()) {
                // Make sure the block is a container
                if (!(clickedBlock.getState() instanceof Container)) {
                    BukkitUtils.sendMessage(player, "&cYou can't link hopper to this block!");
                    return;
                }

                // Make sure block is not too far away
                HopperData hopperData = new HopperData(link.getHopper());
                hopperData.load();
                int maxLinkDistance = hopperData.hasSignalAmplifierUpgrade() ? Constants.EXTENDED_LINK_MAX_DISTANCE : Constants.NORMAL_LINK_MAX_DISTANCE;

                if (link.getHopper().getLocation().distance(clickedBlock.getLocation()) > maxLinkDistance) {
                    BukkitUtils.sendMessage(player, "&cThis container is too far away to be linked!");
                    return;
                }

                // Make sure that player is not trying to link hopper to itself
                if (link.getHopper().getLocation().equals(clickedBlock.getLocation())) {
                    BukkitUtils.sendMessage(player, "&cYou can't link hopper to itself!");
                    return;
                }

                link.linkTo(clickedBlock.getLocation());
            }
            else {
                link.stop();
                BukkitUtils.sendMessage(player, "&cThis hopper is already linked!");
            }
        }
    }

    @EventHandler
    public void onAsyncPlayerChatEvent(AsyncPlayerChatEvent e) {
        if (!ContainerLinking.isPlayerLinking(e.getPlayer())) return;
        if (e.getMessage().equals("stop") || e.getMessage().equals("cancel")) {
            e.setCancelled(true);

            ContainerLinking.getPlayerLink(e.getPlayer()).stop();
            BukkitUtils.sendMessage(e.getPlayer(), "&eHopper linking cancelled.");
        }
    }
}
