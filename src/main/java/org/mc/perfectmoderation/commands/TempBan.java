package org.mc.perfectmoderation.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.mc.perfectmoderation.PerfectModeration;
import org.mc.perfectmoderation.configs.Names;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TempBan implements CommandExecutor, Listener {
    PerfectModeration plugin;

    public TempBan(PerfectModeration perfectModeration) {
        plugin = perfectModeration;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player)) return false;
        Player sender = (Player) commandSender;

        if (command.getName().equalsIgnoreCase("temp-ban")) {
            if (!sender.hasPermission("PerfectModeration.TempBan")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Names.get().getString("Do-not-have-permission"))));
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Names.get().getString("TempBan.Usage"))));
                return true;
            }
            String playerName = args[0];
            Player player = Bukkit.getPlayer(playerName);
            UUID uuid;
            if (player == null) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
                if (offlinePlayer.hasPlayedBefore()) {
                    uuid = offlinePlayer.getUniqueId();
                }else{
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Names.get().getString("TempBan.NON-Player"))));
                    return true;
                }
            }else {
                uuid = player.getUniqueId();
            }
            String timeArg = args[1];
            long banDuration = parseTimeArgument(timeArg, sender);
            if (banDuration == -555) {
                return true;
            } else if (banDuration <= 0) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Names.get().getString("TempBan.Invalid-Ban-Duration"))));
                return true;
            }
            if (plugin.data.isPlayerInBanTable(uuid)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Names.get().getString("TempBan.Already-Banned"))));
                return true;
            }
            long currentTime = System.currentTimeMillis();
            long banEndTime = currentTime + banDuration;
            String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));


            plugin.data.addPlayerToBanTable(playerName, uuid, banEndTime, reason, sender.getName());
            plugin.data.addPunishment(plugin.data.getIPByUUID(uuid), "Ban", new Timestamp(currentTime), new Timestamp(banEndTime), sender.getName());


            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Names.get().getString("TempBan.Done").replaceAll("%PFModeration_Player_Name%", playerName)));
              if (player != null && player.isOnline()) {
                  StringBuilder kickMessage = new StringBuilder();
                  for(String string: Names.get().getStringList("TempBan.Layout")){
                      string = string.replaceAll("%PFModeration_Duration%", formatTime(banDuration));
                      string = string.replaceAll("%PFModeration_Reason%", plugin.data.getReasonFromBanTable(player.getUniqueId()));
                      string = string.replaceAll("%PFModeration_Date%", plugin.data.getDateFromBanTable(player.getUniqueId()));
                      string = string.replaceAll("%PFModeration_Operator%", plugin.data.getOperatorFromBanTable(player.getUniqueId()));
                      kickMessage.append(ChatColor.translateAlternateColorCodes('&', string)).append("\n");
                  }
                  player.kickPlayer(kickMessage.toString());
            }

        }
        return true;
    }

    public static String formatTimeRemaining(long milliseconds) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60;
        long days = TimeUnit.MILLISECONDS.toDays(milliseconds) % 30;
        long months = TimeUnit.MILLISECONDS.toDays(milliseconds) / 30;
        long hours = TimeUnit.MILLISECONDS.toHours(milliseconds) % 24;

        StringBuilder sb = new StringBuilder();
        if (months > 0) {
            sb.append(months).append(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Names.get().getString("TempBan.Group.Months"))));
        }
        if (days > 0) {
            sb.append(days).append(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Names.get().getString("TempBan.Group.Days"))));
        }
        if (hours > 0) {
           sb.append(hours).append(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Names.get().getString("TempBan.Group.Hours"))));
        }
        if (minutes > 0) {
            sb.append(minutes).append(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Names.get().getString("TempBan.Group.Minutes"))));
        }
        if (seconds > 0) {
            sb.append(seconds).append(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Names.get().getString("TempBan.Group.Seconds"))));
        }

        return sb.toString();
    }

    public static String formatTime(long milliseconds) {
        if (milliseconds <= 0) {
            return " ";
        } else {
            return formatTimeRemaining(milliseconds);
        }
    }

    private long parseTimeArgument(String timeArg, Player sender) {
        long multiplier;
        switch (timeArg.charAt(timeArg.length() - 1)) {
            case 'm':
                multiplier = 60 * 1000;
                break;
            case 'w':
                multiplier = 7 * 24 * 60 * 60 * 1000;
                break;
            case 'o':
                multiplier = 30L * 24L * 60L * 60L * 1000L;
                break;
            default:
                return -1;
        }

        try {
            int value = Integer.parseInt(timeArg.substring(0, timeArg.length() - 1));
            long duration = value * multiplier;
            long maxTime = getMaxBanTime(sender);
            if (duration > maxTime) {
                String message = Objects.requireNonNull(Names.get().getString("TempBan.Max-Duration")).replaceAll("%PFModeration_MAX%", formatTime(maxTime) + ".");
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                return -555;
            }
            return duration;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private long getMaxBanTime(Player player) {
        if (player.hasPermission("PerfectModeration.TempBan.Minute.20")) {
            return 20 * 60 * 1000;
        } else if (player.hasPermission("PerfectModeration.TempBan.Minute.60")) {
            return 60 * 60 * 1000;
        } else if (player.hasPermission("PerfectModeration.TempBan.Minute.360")) {
            return 360 * 60 * 1000;
        } else if (player.hasPermission("PerfectModeration.TempBan.Minute.1440")) {
            return 24 * 60 * 60 * 1000;
        } else if (player.hasPermission("PerfectModeration.TempBan.Week.1")) {
            return 7 * 24 * 60 * 60 * 1000;
        } else if (player.hasPermission("PerfectModeration.TempBan.Week.2")) {
            return 14 * 24 * 60 * 60 * 1000;
        } else if (player.hasPermission("PerfectModeration.TempBan.Month.1")) {
            return 30 * 24 * 60 * 60 * 1000L;
        } else if (player.hasPermission("PerfectModeration.TempBan.Month.6")) {
            return 6 * 30 * 24 * 60 * 60 * 1000L;
        } else if (player.hasPermission("PerfectModeration.TempBan.Month.12")) {
            return 12 * 30 * 24 * 60 * 60 * 1000L;
        } else {
            return 0;
        }
    }
}

