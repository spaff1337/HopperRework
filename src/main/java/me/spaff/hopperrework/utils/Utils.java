package me.spaff.hopperrework.utils;

import me.spaff.hopperrework.Constants;
import me.spaff.hopperrework.hopper.HopperUpgrade;
import me.spaff.spflib.utils.ItemUtils;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static List<Location> getLineBetweenLocations(Location locA, Location locB, int density) {
        List<Location> locs = new ArrayList<>();

        Vector direction = locA.toVector().subtract(locB.toVector()).normalize();
        double distance = locB.distance(locA);

        Location finalLoc = locB.clone();
        for (int i = 0; i <= distance * density; i++) { // distance
            locs.add(finalLoc.clone());
            finalLoc = finalLoc.add(new Vector(direction.getX() / density, direction.getY() / density, direction.getZ()/ density)); // direction multiply(1)
        }
        return locs;
    }

    public static List<Location> getCuboidLocations(Location locA, Location locB, boolean sidesOnly, double density) {
        List<Location> locs = new ArrayList<>();

        Location min = locA.clone();
        min.setX(Math.min(locA.getX(), locB.getX()));
        min.setY(Math.min(locA.getY(), locB.getY()));
        min.setZ(Math.min(locA.getZ(), locB.getZ()));

        Location max = locB.clone();
        max.setX(Math.max(locA.getX(), locB.getX()));
        max.setY(Math.max(locA.getY(), locB.getY()));
        max.setZ(Math.max(locA.getZ(), locB.getZ()));

        for (double x = min.getX(); x <= max.getX(); x+=density) {
            for (double y = min.getY(); y <= max.getY(); y+=density) {
                for (double z = min.getZ(); z <= max.getZ(); z+=density) {
                    int edges = 0;

                    // Check for axis
                    if (x == min.getX() || x == (max.getX())) edges++;
                    if (y == min.getY() || y == (max.getY())) edges++;
                    if (z == min.getZ() || z == (max.getZ())) edges++;

                    if (!sidesOnly) {
                        locs.add(new Location(locA.getWorld(), x, y, z));
                    }
                    else {
                        if (edges >= 2)
                            locs.add(new Location(locA.getWorld(), x, y, z));
                    }
                }
            }
        }

        return locs;
    }

    public static boolean isHopperUpgrade(ItemStack item) {
        if (item == null) return false;
        return getHopperUpgrade(item) != null;
    }

    public static HopperUpgrade getHopperUpgrade(ItemStack item) {
        if (item == null) return null;

        String id = (String) ItemUtils.getPersistentData(
                item,
                new NamespacedKey(Constants.HOPPER_DATA_NAMESPACE, Constants.HOPPER_DATA_UPGRADES_KEY),
                PersistentDataType.STRING
        );

        if (id != null) {
            for (HopperUpgrade upgrade : HopperUpgrade.values()) {
                if (id.equals(upgrade.toString()))
                    return upgrade;
            }
        }
        return null;
    }
}
