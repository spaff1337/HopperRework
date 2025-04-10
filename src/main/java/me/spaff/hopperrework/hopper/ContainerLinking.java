package me.spaff.hopperrework.hopper;

import me.spaff.hopperrework.Constants;
import me.spaff.hopperrework.Main;
import me.spaff.hopperrework.menu.ViewerData;
import me.spaff.hopperrework.utils.Utils;
import me.spaff.spflib.utils.BukkitUtils;
import org.bukkit.*;
import org.bukkit.block.Container;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class ContainerLinking {
    private static List<ContainerLinking> links = new ArrayList<>();
    private BukkitTask task;
    private final Player who;

    private final Hopper hopper;
    private Location containerLinked;

    public ContainerLinking(Player who, Hopper hopper) {
        this.who = who;
        this.hopper = hopper;
    }

    public void start() {
        if (isBeingLinked(hopper)) {
            BukkitUtils.sendMessage(who, "&cThis hopper is already being linked!");
            return;
        }

        who.closeInventory();

        // Kick whoever is viewing this hopper from the menu
        ViewerData.clearAndClose(hopper.getLocation());
        links.add(this);

        HopperData hopperData = new HopperData(hopper);
        hopperData.load();

        BukkitUtils.sendMessage(who, "");
        BukkitUtils.sendMessage(who, "&eLinking started! Left-click any container to link it.");
        BukkitUtils.sendMessage(who, "&eType 'cancel' in chat to cancel linking.");
        BukkitUtils.sendMessage(who, "");

        task = new BukkitRunnable() {
            @Override
            public void run() {
                if (isLinked()) {
                    this.cancel();
                    task = null;
                    return;
                }

                Vector direction = who.getEyeLocation().getDirection().normalize();
                direction.multiply(4);

                Location hopperLoc = hopper.getLocation().clone().add(0.5, 0.5, 0.5);

                Location targetLocation;
                if (who.getTargetBlockExact(4) == null)
                    targetLocation = who.getEyeLocation().add(direction).getBlock().getLocation().add(0.5, 0.5, 0.5);
                else
                    targetLocation = who.getTargetBlockExact(4).getLocation().add(0.5, 0.5, 0.5);

                int maxLinkDistance = hopperData.hasSignalAmplifierUpgrade() ? Constants.EXTENDED_LINK_MAX_DISTANCE : Constants.NORMAL_LINK_MAX_DISTANCE;
                Color color = Constants.LINKING_COLOR_ALLOWED;

                if (!(targetLocation.getBlock().getState() instanceof Container)
                        || hopperLoc.equals(targetLocation)
                        || hopperLoc.distance(targetLocation) > maxLinkDistance)
                {
                    color = Constants.LINKING_COLOR_NOT_ALLOWED;

                    if (hopperLoc.distance(targetLocation) > (maxLinkDistance + 10)) {
                        stop();
                        BukkitUtils.sendMessage(who, "&cYou went too far away from the hopper! linking cancelled.");
                    }
                }

                Particle.DustTransition dustTransition = new Particle.DustTransition(
                        color,
                        Color.fromRGB(127, 127, 127),
                        1f
                );

                // Draw line
                for (Location loc : Utils.getLineBetweenLocations(hopperLoc, targetLocation, 3)) {
                    who.spawnParticle(
                            Particle.DUST,
                            loc,
                            1,
                            dustTransition
                    );
                }

                // Draw cuboid around block
                Location loc1 = targetLocation.getBlock().getLocation().clone();
                Location loc2 = loc1.clone().add(1, 1, 1);

                for (var loc : Utils.getCuboidLocations(loc1, loc2, true, 0.25)) {
                    who.spawnParticle(
                            Particle.DUST,
                            loc,
                            1,
                            dustTransition
                    );
                }
            }
        }.runTaskTimer(Main.getInstance(), 0, 3);
    }

    public void linkTo(Location container) {
        this.containerLinked = container;
        complete();
    }

    private void complete() {
        if (!isLinked()) return;
        stop();

        HopperManager.addHopper(hopper);

        HopperData hopperData = new HopperData(hopper);
        hopperData.load();
        hopperData.setLinkedContainer(getContainerLinked());
        hopperData.save();

        BukkitUtils.sendMessage(who, "&aContainer was successfully linked!");
    }

    public void stop() {
        task.cancel();
        task = null;
        links.remove(this);
    }

    public Player getWho() {
        return who;
    }

    public Hopper getHopper() {
        return hopper;
    }

    public Location getContainerLinked() {
        return containerLinked;
    }

    public boolean isLinked() {
        return containerLinked != null;
    }

    public static boolean isBeingLinked(Hopper hopper) {
        return getLinkInstance(hopper) != null;
    }

    public static ContainerLinking getLinkInstance(Hopper hopper) {
        for (var link : links) {
            if (link.getHopper().getLocation().equals(hopper.getLocation()))
                return link;
        }
        return null;
    }

    public static boolean isPlayerLinking(Player player) {
        return getPlayerLink(player) != null;
    }

    public static ContainerLinking getPlayerLink(Player player) {
        for (var link : links) {
            if (link.getWho().getUniqueId().equals(player.getUniqueId()))
                return link;
        }
        return null;
    }
}
