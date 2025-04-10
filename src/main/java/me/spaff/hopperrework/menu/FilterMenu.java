package me.spaff.hopperrework.menu;

import me.spaff.hopperrework.Constants;
import me.spaff.hopperrework.hopper.ContainerLinking;
import me.spaff.hopperrework.hopper.HopperData;
import me.spaff.spflib.SPFLib;
import me.spaff.spflib.builder.ItemBuilder;
import me.spaff.spflib.menu.UpdatableMenu;
import me.spaff.spflib.utils.BukkitUtils;
import me.spaff.spflib.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.Iterator;

public class FilterMenu extends UpdatableMenu {
    private static final int[] FILTER_SLOTS = {10, 11, 12, 13, 14, 15, 16};
    private static final int GO_BACK_SLOT = 22;
    private static final int CLEAR_ITEMS_SLOT = 23;

    private MenuCooldown menuCooldown;
    private ViewerData viewerData;

    private final Hopper hopper;
    private HopperData hopperData;

    public FilterMenu(Player player, Hopper hopper) {
        super(player, 3, "Item Filter");
        this.hopper = hopper;

        if (ViewerData.isViewing(hopper.getLocation()) || ContainerLinking.isBeingLinked(hopper))
            return;

        this.hopperData = new HopperData(hopper);
        this.hopperData.load();

        this.menuCooldown = new MenuCooldown();

        this.fillWith(new ItemBuilder.Builder(Material.BLACK_STAINED_GLASS_PANE).name("&7").build().getItem());

        this.setItem(GO_BACK_SLOT, new ItemBuilder.Builder(Material.ARROW).name("&eGo Back").build().getItem());
        this.setItem(CLEAR_ITEMS_SLOT, new ItemBuilder.Builder(Material.CAULDRON)
                .name("&cClear Filter")
                .wrappedLore("&7Removes all items from the filter.")
                .build().getItem());

        // TODO: Refactor
        for (int slot : FILTER_SLOTS) {
            this.setItem(slot, new ItemBuilder.Builder(Material.YELLOW_STAINED_GLASS_PANE)
                    .name("&eFilter Slot")
                    .wrappedLore("&7Click any item in your inventory to add to the filter list.")
                    .build().getItem()
            );
        }

        // TODO: Refactor
        int slot = FILTER_SLOTS[0];
        for (Material mat : hopperData.getFilterItems()) {
            this.setItem(slot, new ItemBuilder.Builder(mat)
                    .name("&e" + SPFLib.nms().getDisplayName(new ItemStack(mat)))
                    .wrappedLore("&7This item will be filtered by the hopper.")
                    .flags(ItemFlag.HIDE_ATTRIBUTES)
                    .build().getItem()
            );
            slot++;
        }

        // Handle clicking
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

                // Add item to filter list
                hopperData.addFilterItems(clickedItem.getType());
                hopperData.save();
                refreshMenu();
                return;
            }

            if (e.getSlot() == GO_BACK_SLOT) {
                viewerData.clear();
                new HopperMenu(player, hopper).open();
                return;
            }

            if (e.getSlot() == CLEAR_ITEMS_SLOT) {
                hopperData.getFilterItems().clear();
                hopperData.save();
                refreshMenu();
                return;
            }

            int setIndex = 0;
            for (int filterSlot : FILTER_SLOTS) {
                if (e.getSlot() == filterSlot) {
                    Iterator<Material> iterator = hopperData.getFilterItems().iterator();

                    // Remove item from filter list
                    int matIndex = 0;
                    while (iterator.hasNext()) {
                        Material mat = iterator.next();
                        if (setIndex == matIndex) {
                            iterator.remove();
                            hopperData.save();
                            refreshMenu();
                            break;
                        }
                        matIndex++;
                    }
                }

                setIndex++;
            }
        });

        this.startUpdate(10);
        this.onUpdate(() -> {
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
        new FilterMenu(getPlayer(), hopper).open();
    }
}
