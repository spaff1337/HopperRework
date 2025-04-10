package me.spaff.hopperrework.cmd;

import me.spaff.hopperrework.Constants;
import me.spaff.hopperrework.Main;
import me.spaff.hopperrework.hopper.ContainerLinking;
import me.spaff.hopperrework.hopper.HopperData;
import me.spaff.hopperrework.hopper.HopperUpgrade;
import me.spaff.hopperrework.menu.HopperMenu;
import me.spaff.hopperrework.menu.ViewerData;
import me.spaff.hopperrework.utils.Utils;
import me.spaff.spflib.SPFLib;
import me.spaff.spflib.builder.ItemBuilder;
import me.spaff.spflib.chunk.ChunkData;
import me.spaff.spflib.file.FileManager;
import me.spaff.spflib.utils.BukkitUtils;
import me.spaff.spflib.utils.ItemUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Hopper;
import org.bukkit.block.data.Directional;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Commands implements CommandExecutor {
    private static Location locA;
    private static Location locB;

    private static HopperData hopperDataTest;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;
        if (args.length == 0) return false;

        if (args[0].equalsIgnoreCase("givelinker")) {
            ItemStack linker = new ItemBuilder.Builder(Material.SHEARS)
                    .name("&bTransfer Linker")
                    .wrappedLore("&7Click on a hopper and then click on a container to link it.")
                    .build().getItem();

            player.getInventory().addItem(linker);
        }

        if (args[0].equalsIgnoreCase("itemparticletest")) {
            Location loc = player.getLocation();

            for (var entity : loc.getWorld().getNearbyEntities(loc, 15, 15, 15)) {
                if (!(entity instanceof Item item)) continue;

                loc.getWorld().spawnParticle(
                        Particle.HAPPY_VILLAGER,
                        entity.getLocation().clone().add(0, 0.35, 0),
                        5,
                        0.1,
                        0.1,
                        0.1,
                        0.15
                );
            }
        }

        if (args[0].equalsIgnoreCase("cdtest")) {
            /*MenuCooldown clickDelay = new MenuCooldown(player, 5000, 0);
            new BukkitRunnable() {
                @Override
                public void run() {
                    System.out.println("is on cooldown: " + clickDelay.isOnCooldown());
                }
            }.runTaskTimer(Main.getInstance(), 0 ,20);*/
        }

        if (args[0].equalsIgnoreCase("test")) {
            locA = player.getLocation();

            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Location loc : Utils.getLineBetweenLocations(locA, player.getLocation().clone().add(0, 5, 0), 4)) {
                        Particle.DustTransition dustTransition = new Particle.DustTransition(
                                Color.fromRGB(3, 252, 140),
                                Color.fromRGB(127, 127, 127),
                                1f
                        );

                        loc.getWorld().spawnParticle(
                                Particle.DUST_COLOR_TRANSITION,
                                loc,
                                1,
                                dustTransition
                        );
                    }
                }
            }.runTaskTimer(Main.getInstance(), 0 ,5);
        }

        if (args[0].equalsIgnoreCase("transfer")) {
            Location locA = player.getLocation().clone().add(0.5, 0.5, 0.5);

            new BukkitRunnable() {
                @Override
                public void run() {
                    Vector direction = player.getEyeLocation().getDirection().normalize();
                    direction.multiply(4);

                    if (player.getTargetBlockExact(4) == null)
                        locB = player.getEyeLocation().add(direction).getBlock().getLocation().add(0.5, 0.5, 0.5);
                    else
                        locB = player.getTargetBlockExact(4).getLocation().add(0.5, 0.5, 0.5);

                    /*inFrontBlock.getWorld().spawnParticle(
                            Particle.HAPPY_VILLAGER,
                            inFrontBlock.getX() + 0.5,
                            inFrontBlock.getY(),
                            inFrontBlock.getZ() + 0.5,
                            1, // Count
                            0,
                            0,
                            0,
                            0,
                            null,
                            true
                    );*/

                    Color color = Color.fromRGB(3, 252, 140);
                    if (!locB.getBlock().getType().equals(Material.HOPPER))
                        color = Color.fromRGB(255, 66, 66); // 255, 66, 66

                    for (Location loc : Utils.getLineBetweenLocations(locA, locB, 3)) {
                        Particle.DustTransition dustTransition = new Particle.DustTransition(
                                color,
                                Color.fromRGB(127, 127, 127),
                                1f
                        );

                        loc.getWorld().spawnParticle(
                                Particle.DUST,
                                loc,
                                1,
                                dustTransition
                        );
                    }
                }
            }.runTaskTimer(Main.getInstance(), 0 ,3);
        }

        if (args[0].equalsIgnoreCase("fillchunk")) {
            Location loc = player.getLocation();

            for (int x = 0; x < 16; x++) {
                for (int y = -64; y < 319; y++) {
                    for (int z = 0; z < 16; z++) {
                        loc.getChunk().getBlock(x, y, z).setType(Material.HOPPER);
                        Hopper hopper = (Hopper) loc.getChunk().getBlock(x, y, z).getState();

                        HopperData hopperData = new HopperData(hopper);
                        hopperData.load();
                        hopperData.clear();

                        loc.getChunk().getBlock(x, y, z).setType(Material.AIR);
                    }
                }
            }
        }

        if (args[0].equalsIgnoreCase("linkthis")) {
            new ContainerLinking(player, (Hopper) player.getTargetBlockExact(15).getState()).start();
        }

        if (args[0].equalsIgnoreCase("destroy")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.getTargetBlockExact(5).setType(Material.AIR);
                    System.out.println("destroyed block");
                }
            }.runTaskLater(Main.getInstance(), 3 * 20);
        }

        if (args[0].equalsIgnoreCase("isconnected")) {
            Block block = player.getTargetBlockExact(15);
            Hopper hopper = (Hopper) block.getState();

            if (hopper.getBlockData() instanceof Directional directional) {
                System.out.println("direction: " + directional.getFacing());
                Block connectedBlock = block.getWorld().getBlockAt(
                        block.getLocation().getBlockX() + directional.getFacing().getModX(),
                        block.getLocation().getBlockY() + directional.getFacing().getModY(),
                        block.getLocation().getBlockZ() + directional.getFacing().getModZ()
                );

                System.out.println("connected block: " + connectedBlock);

                connectedBlock.setType(Material.DIAMOND_BLOCK);
            }
            else
                System.out.println("is not directional");

            //System.out.println("yaw: " + block.getState().getBlockData().);
            //System.out.println("pitch: " + hopper.getLocation().getPitch());
        }

        if (args[0].equalsIgnoreCase("gethopperinfo")) {
            FileManager fileManager = new FileManager(System.getProperty("user.dir"), "spigot");
            System.out.println(fileManager.read("world-settings.default.ticks-per.hopper-transfer"));
        }

        if (args[0].equalsIgnoreCase("boxtest")) {
            Block block = player.getTargetBlockExact(10);

            Location loc1 = block.getLocation().clone();
            Location loc2 = loc1.clone().add(1, 1, 1);

            new BukkitRunnable() {
                @Override
                public void run() {
                    for (var loc : Utils.getCuboidLocations(loc1, loc2, true, 0.25)) {
                        //loc = loc.getBlock().getLocation().add(0.5, 0.5, 0.5);

                        loc.getWorld().spawnParticle(
                                Particle.ELECTRIC_SPARK,
                                loc.getX(),
                                loc.getY(),
                                loc.getZ(),
                                1, // Count
                                0,
                                0,
                                0,
                                0,
                                null,
                                true
                        );
                    }
                }
            }.runTaskTimer(Main.getInstance(), 0, 2);

            /*Location loc1 = new Location(player.getWorld(), -253, 72, 195);
            Location loc2 = new Location(player.getWorld(), -261, 81, 187);

            new BukkitRunnable() {
                @Override
                public void run() {
                    for (var loc : Utils.getCuboidLocations(loc1, loc2, true)) {
                        loc = loc.getBlock().getLocation().add(0.5, 0.5, 0.5);

                        player.spawnParticle(
                                Particle.ELECTRIC_SPARK,
                                loc.getX(),
                                loc.getY(),
                                loc.getZ(),
                                1, // Count
                                0,
                                0,
                                0,
                                0,
                                null,
                                true
                        );
                    }
                }
            }.runTaskTimer(Main.getInstance(), 0, 2);*/
        }

        /*for (int x = (int) Math.min(loc1.getX(), loc2.getX()); x < (int) Math.max(loc1.getX(), loc2.getX()) ; x++) {
                        for (int y = (int) Math.min(loc1.getY(), loc2.getY()); y < (int) Math.max(loc1.getY(), loc2.getY()) ; y++) {
                            for (int z = (int) Math.min(loc1.getZ(), loc2.getZ()); z < (int) Math.max(loc1.getZ(), loc2.getZ()); z++) {

                                Particle particle = Particle.ELECTRIC_SPARK;

                                int edges = 0;
                                if (x == Math.min(loc1.getX(), loc2.getX()) || x == (Math.max(loc1.getX(), loc2.getX()) - 1)) edges++;
                                if (y == Math.min(loc1.getY(), loc2.getY()) || y == (Math.max(loc1.getY(), loc2.getY()) - 1)) edges++;
                                if (z == Math.min(loc1.getZ(), loc2.getZ()) || z == (Math.max(loc1.getZ(), loc2.getZ()) - 1)) edges++;

                                if (edges >= 2)
                                    particle = Particle.HAPPY_VILLAGER;

                                player.spawnParticle(
                                        particle,
                                        x,
                                        y,
                                        z,
                                        1, // Count
                                        0,
                                        0,
                                        0,
                                        0,
                                        null,
                                        true
                                );
                            }
                        }
                    }*/

        if (args[0].equalsIgnoreCase("iscontainer")) {
            Block block = player.getTargetBlockExact(15);
            //System.out.println(block.getType() + " is container? " + (block.getState() instanceof Container));

            Container container = (Container) block.getState();
            //container.getInventory().addItem(new ItemStack(Material.DIAMOND_SWORD));

            for (var item : container.getInventory()) {
                if (ItemUtils.isNull(item) || !item.getType().equals(Material.OAK_LOG)) continue;
                container.getInventory().remove(item);
            }
        }

        if (args[0].equalsIgnoreCase("testlore")) {
            ItemStack blockToItem = new ItemStack(player.getLocation().getBlock().getType());
            BukkitUtils.sendMessage(player, "&f" + SPFLib.nms().getDisplayName(blockToItem) + " &7at " + player.getLocation());
        }

        /*if (args[0].equalsIgnoreCase("data")) {
            if (hopperDataTest == null)
                hopperDataTest = new HopperData(player.getTargetBlockExact(5));

            if (args[1].equalsIgnoreCase("load")) {
                hopperDataTest.loadData();
            }
            if (args[1].equalsIgnoreCase("save")) {
                hopperDataTest.saveData();
            }
        }*/

        if (args[0].equalsIgnoreCase("data")) {
            if (args[1].equalsIgnoreCase("get")) {
                ChunkData test = new ChunkData(player.getTargetBlockExact(5), new NamespacedKey(Constants.HOPPER_DATA_NAMESPACE, Constants.HOPPER_DATA_FILTER_KEY));
                System.out.println(test.getData());
            }
        }

        if (args[0].equalsIgnoreCase("menutest")) {
            Block block = player.getTargetBlockExact(15);
            if (block != null && block.getType().equals(Material.HOPPER)) {
                Hopper hopper = (Hopper) block.getState();
                new HopperMenu(player, hopper).open();
            }
        }

        if (args[0].equalsIgnoreCase("getitems")) {
            Block block = player.getTargetBlockExact(15);
            if (block != null && block.getType().equals(Material.HOPPER)) {
                Hopper hopper = (Hopper) block.getState();

                for (ItemStack item : hopper.getInventory().getContents()) {
                    System.out.println("stored item: " + item);
                }
            }
        }

        if (args[0].equalsIgnoreCase("getupgrades")) {
            for (HopperUpgrade upgrade : HopperUpgrade.values()) {
                player.getInventory().addItem(upgrade.getItem());
            }
        }

        if (args[0].equalsIgnoreCase("vdata")) {
            if (args[1].equalsIgnoreCase("clearme")) {
                ViewerData.clear(player.getUniqueId());
            }
            if (args[1].equalsIgnoreCase("flush")) {
                ViewerData.flush();
            }
            if (args[1].equalsIgnoreCase("print")) {
                System.out.println("-----------------");
                ViewerData.getData().forEach(data -> {
                    System.out.println(data.getViewerUUID() + " viewing from " + data.getViewingFrom());
                });
            }
        }

        return true;
    }
}
