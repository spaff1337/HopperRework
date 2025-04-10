package me.spaff.hopperrework.menu;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class ViewerData {
    private static List<ViewerData> data = new ArrayList<>();

    private final UUID viewerUUID;
    private final Location viewingFrom;

    public ViewerData(UUID viewer, Location viewingFrom) {
        this.viewerUUID = viewer;
        this.viewingFrom = viewingFrom;
        data.add(this);
    }

    public UUID getViewerUUID() {
        return viewerUUID;
    }

    public Location getViewingFrom() {
        return viewingFrom;
    }

    public void clear() {
        data.remove(this);
    }

    public static void clearAndClose(Location viewingFrom) {
        Iterator<ViewerData> iterator = data.iterator();
        while (iterator.hasNext()) {
            ViewerData vdata = iterator.next();
            if (!viewingFrom.equals(vdata.getViewingFrom())) continue;

            if (Bukkit.getPlayer(vdata.getViewerUUID()) != null) {
                Bukkit.getPlayer(vdata.getViewerUUID()).closeInventory();
                clear(vdata.getViewerUUID());
                break;
            }
        }
    }

    public static void clear(UUID uuid) {
        Iterator<ViewerData> iterator = data.iterator();
        while (iterator.hasNext()) {
            ViewerData vdata = iterator.next();
            if (vdata.getViewerUUID() == uuid) {
                iterator.remove();
                break;
            }
        }
    }

    public static void flush() {
        for (ViewerData vdata : data) {
            if (Bukkit.getPlayer(vdata.getViewerUUID()) != null) continue;
            clear(vdata.getViewerUUID());
        }
    }

    public static boolean isViewing(Location viewingFrom) {
        for (ViewerData vdata : data) {
            if (viewingFrom.equals(vdata.getViewingFrom()))
                return true;
        }
        return false;
    }

    public static List<ViewerData> getData() {
        return data;
    }
}