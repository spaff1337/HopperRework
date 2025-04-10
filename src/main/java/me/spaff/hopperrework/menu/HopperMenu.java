package me.spaff.hopperrework.menu;

import me.spaff.hopperrework.Constants;
import me.spaff.hopperrework.HPRColors;
import me.spaff.hopperrework.hopper.ContainerLinking;
import me.spaff.hopperrework.hopper.FilterMode;
import me.spaff.hopperrework.hopper.HopperData;
import me.spaff.hopperrework.hopper.HopperUpgrade;
import me.spaff.hopperrework.utils.Utils;
import me.spaff.spflib.SPFLib;
import me.spaff.spflib.builder.ItemBuilder;
import me.spaff.spflib.menu.UpdatableMenu;
import me.spaff.spflib.utils.BukkitUtils;
import me.spaff.spflib.utils.InventoryUtils;
import me.spaff.spflib.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class HopperMenu extends UpdatableMenu {
    private static final int LINKED_CONTAINER_SLOT = 34;
    private static final int FILTER_SLOT = 32;

    private static final int[] UPGRADE_SLOTS = {28, 29, 30};
    private static final int[] HOPPER_SLOTS = {11, 12, 13, 14, 15};

    private MenuCooldown menuCooldown;
    private ViewerData viewerData;

    private final Hopper hopper;
    private HopperData hopperData;

    public HopperMenu(Player player, Hopper hopper) {
        super(player, 5, "Hopper");
        this.hopper = hopper;

        if (ViewerData.isViewing(hopper.getLocation()) || ContainerLinking.isBeingLinked(hopper))
            return;

        this.hopperData = new HopperData(hopper);
        this.hopperData.load();

        this.menuCooldown = new MenuCooldown();

        this.fillWith(new ItemBuilder.Builder(Material.BLACK_STAINED_GLASS_PANE).name("&7").build().getItem());

        // Item filter
        List<String> itemFilteringLore = new ArrayList<>();
        itemFilteringLore.add("&7Currently filtered items:" + (hopperData.getFilterItems().isEmpty() ? " &fNone" : ""));

        for (Material filteredMaterial : hopperData.getFilterItems()) {
            itemFilteringLore.add("&8- &f" + SPFLib.nms().getDisplayName(new ItemStack(filteredMaterial)));
        }

        itemFilteringLore.add("");
        itemFilteringLore.add("&7Filter Mode: " + hopperData.getFilterMode().getColor() + hopperData.getFilterMode().getName());
        itemFilteringLore.add("");
        itemFilteringLore.add("&aLeft-Click &7to edit filtered items.");
        itemFilteringLore.add("&aRight-Click &7to toggle between");
        itemFilteringLore.add("&fWhitelist &7and &8Blacklist &7mode.");

        this.setItem(FILTER_SLOT, new ItemBuilder.Builder(Material.HOPPER)
                .name("&eItem Filtering")
                .lore(itemFilteringLore)
                .build().getItem());

        // Linked container
        Material icon = hopperData.isLinked() ? Material.REDSTONE_TORCH : Material.LEVER;

        List<String> linkLore = new ArrayList<>();
        if (hopperData.isLinked() && hopperData.getLinkedContainer().getBlock().getState() instanceof Container) {
            linkLore.add("&7This hopper is currently linked to");

            ItemStack blockToItem = new ItemStack(hopperData.getLinkedContainer().getBlock().getType());
            linkLore.add("&7a &f" + SPFLib.nms().getDisplayName(blockToItem) + " &7at" +
                    " &ex: &b" + hopperData.getLinkedContainer().getX() + "&7, &ey: &b" + hopperData.getLinkedContainer().getY() + "&7, &ez: &b" + hopperData.getLinkedContainer().getZ() + "&7."
            );

            linkLore.add("");
            linkLore.add(HPRColors.LIGHT_GRAY + "Click to re-link.");
        }
        else {
            linkLore.add("&7This hopper is not linked");
            linkLore.add("&7to any container.");
            linkLore.add("");
            linkLore.add(HPRColors.LIGHT_GRAY + "Click to link.");
        }

        this.setItem(LINKED_CONTAINER_SLOT, new ItemBuilder.Builder(icon)
                .name("&6Linked Container")
                .lore(linkLore)
                .build()
                .getItem());

        // Upgrades
        for (int slot : UPGRADE_SLOTS) {
            this.setItem(slot, new ItemBuilder.Builder(Material.ORANGE_STAINED_GLASS_PANE)
                    .name("&6Upgrade Slot")
                    .wrappedLore("&7Click at any upgrade in your inventory, to add to this hopper.")
                    .build().getItem());
        }

        int upgradeSlot = UPGRADE_SLOTS[0];
        for (HopperUpgrade upgrade : hopperData.getUpgrades()) {
            this.setItem(upgradeSlot, upgrade.getItem());
            upgradeSlot++;
        }

        this.onClick((e) -> {
            e.setCancelled(true);

            if (!hopper.getBlock().getType().equals(Material.HOPPER)) {
                player.closeInventory();
                return;
            }

            if (menuCooldown.isOnCooldown()) {
                return;
            }

            menuCooldown.setOnCooldown(Constants.MENU_CLICK_DELAY_MILLISECONDS);

            if (e.getClickedInventory().equals(player.getInventory())) {
                ItemStack clickedItem = e.getCurrentItem();
                if (ItemUtils.isNull(clickedItem)) return;
                if (!Utils.isHopperUpgrade(clickedItem)) return;

                HopperUpgrade upgrade = Utils.getHopperUpgrade(clickedItem);
                player.getInventory().setItem(e.getSlot(), new ItemStack(Material.AIR));

                // Add upgrade to list
                hopperData.addUpgrade(upgrade);
                hopperData.save();
                refreshMenu();
                return;
            }

            if (e.getSlot() == FILTER_SLOT) {
                if (e.getClick().equals(ClickType.LEFT)) {
                    viewerData.clear();
                    new FilterMenu(player, hopper).open();
                }
                else if (e.getClick().equals(ClickType.RIGHT)) {
                    hopperData.setFilterMode(hopperData.getFilterMode() == FilterMode.BLACKLIST ? FilterMode.WHITELIST : FilterMode.BLACKLIST);
                    hopperData.save();
                    refreshMenu();
                }
                return;
            }

            if (e.getSlot() == LINKED_CONTAINER_SLOT) {
                new ContainerLinking(player, hopper).start();
                return;
            }

            int upgradeIndex = 0;
            for (int slot : UPGRADE_SLOTS) {
                if (e.getSlot() == slot) {
                    Iterator<HopperUpgrade> iterator = hopperData.getUpgrades().iterator();

                    // Remove upgrade from list
                    int upgIndex = 0;
                    while (iterator.hasNext()) {
                        HopperUpgrade upgrade = iterator.next();
                        if (upgradeIndex == upgIndex) {
                            if (InventoryUtils.canFitItem(player, upgrade.getItem()))
                                player.getInventory().addItem(upgrade.getItem());
                            else
                                break;

                            iterator.remove();

                            hopperData.save();
                            refreshMenu();
                            break;
                        }
                        upgIndex++;
                    }
                }

                upgradeIndex++;
            }

            int hopperSlot = 0;
            for (int slot : HOPPER_SLOTS) {
                if (e.getSlot() == slot && InventoryUtils.canFitItem(player, hopper.getInventory().getItem(hopperSlot))) {
                    ItemStack item = hopper.getInventory().getItem(hopperSlot);
                    if (ItemUtils.isNull(item)) continue;

                    hopper.getInventory().setItem(hopperSlot, new ItemStack(Material.AIR));
                    player.getInventory().addItem(item);

                    refreshMenu();
                    break;
                }

                hopperSlot++;
            }
        });

        this.startUpdate(10);
        this.onUpdate(() -> {
            int hopperSlot = 0;
            for (int slot : HOPPER_SLOTS) {
                this.setItem(slot, hopper.getInventory().getItem(hopperSlot));
                hopperSlot++;
            }

            // Close the menu when player gets too far away
            if (getPlayer().getLocation().distance(hopper.getLocation()) >= Constants.MINIMUM_MENU_CLOSE_DISTANCE)
                getPlayer().closeInventory();
        });
    }

    @Override
    public void open() {
        if (ViewerData.isViewing(hopper.getLocation()) || ContainerLinking.isBeingLinked(hopper)) {
            BukkitUtils.sendMessage(getPlayer(), "&cSomeone is already using this hopper!");
            return;
        }

        super.open();
        viewerData = new ViewerData(getPlayer().getUniqueId(), hopper.getLocation());
    }

    @Override
    public void unregister() {
        super.unregister();
        viewerData.clear();
    }

    private void refreshMenu() {
        viewerData.clear();
        new HopperMenu(getPlayer(), hopper).open();
    }
}
