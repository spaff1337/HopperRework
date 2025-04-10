package me.spaff.hopperrework.cmd;

import me.spaff.hopperrework.Main;
import me.spaff.hopperrework.hopper.HopperUpgrade;
import me.spaff.spflib.utils.BukkitUtils;
import org.apache.commons.lang3.EnumUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {
    private final String prefix = "&6[HopperRework] ";

    private void sendHelp(Player player) {
        BukkitUtils.sendMessage(player, "");
        BukkitUtils.sendMessage(player, "&6                HopperRework");
        BukkitUtils.sendMessage(player, "&6- /hpr give <upgrade> - gives a hopper");
        BukkitUtils.sendMessage(player, "&6upgrade item.");
        BukkitUtils.sendMessage(player, "&6- /hpr version - shows version of the plugin.");
        BukkitUtils.sendMessage(player, "");
    }

    private void sendPluginMessage(Player player, String message) {
        BukkitUtils.sendMessage(player, prefix + message);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;
        if (args.length == 0) return false;

        if (!player.hasPermission("hpr.command.execute")) {
            BukkitUtils.sendMessage(player, "&cYou don't have permission to execute that!");
            return false;
        }

        //---------------- Commands ----------------//

        if (args[0].equalsIgnoreCase("give")) {
            if (args.length == 2) {
                if (!EnumUtils.isValidEnum(HopperUpgrade.class, args[1])) {
                    sendPluginMessage(player, "&cInvalid hopper upgrade! Use: VACUUM, SIGNAL_AMPLIFIER, FASTER_TRANSFER");
                    return false;
                }

                HopperUpgrade upgrade = HopperUpgrade.valueOf(args[1]);

                player.getInventory().addItem(upgrade.getItem());
                sendPluginMessage(player, "&7Gave yourself upgrade item.");
            }
        }
        else if (args[0].equalsIgnoreCase("version")) {
            sendPluginMessage(player, "&7Version: &fv" + Main.getVersion());
        }
        else
            sendHelp(player);

        return true;
    }
}
