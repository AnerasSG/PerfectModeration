package org.mc.perfectmoderation.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.mc.perfectmoderation.PerfectModeration;
import org.mc.perfectmoderation.configs.Names;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TempMute implements CommandExecutor {
    PerfectModeration plugin;
    public TempMute(PerfectModeration perfectModeration)   {
        plugin = perfectModeration;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player)) return false;
        Player sender = (Player) commandSender;

        if(command.getName().equalsIgnoreCase("temp-mute")) {
            if(!sender.hasPermission("PerfectModeration.TempMute")){
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Names.get().getString("Do-not-have-permission"))));
                return true;
            }
            if (args.length < 3) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Names.get().getString("TempMute.Usage"))));
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
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Names.get().getString("TempMute.NON-Player"))));
                    return true;
                }
            }else {
                uuid = player.getUniqueId();
            }
            String timeArg = args[1];
            long muteDuration = parseTimeArgument(timeArg, sender);
            if (muteDuration == -555){
                return true;
            }else  if (muteDuration <= 0) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Names.get().getString("TempMute.Invalid-Mute-Duration"))));
                return true;
            }
            if (plugin.data.isPlayerInMuteTable(uuid)) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Names.get().getString("TempMute.Already-Muted"))));
                return true;
            }

            long currentTime = System.currentTimeMillis();
            long muteEndTime = currentTime + muteDuration;

            String reason = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
            plugin.data.addPlayerToMuteTable(playerName, uuid, muteEndTime,reason);
            plugin.data.addPunishment(plugin.data.getIPByUUID(uuid), "Mute", new Timestamp(currentTime), new Timestamp(muteEndTime), sender.getName());
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Names.get().getString("TempMute.Done").replaceAll("%PFModeration_Player_Name%", playerName)));

            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', (Objects.requireNonNull(Names.get().getString("TempMute.Notification.Done")).replaceAll("%PFModeration_Operator%", sender.getName()).replaceAll("%PFModeration_Player_Name%", playerName))));
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', (Objects.requireNonNull(Names.get().getString("TempMute.Notification.Reason")).replaceAll("%PFModeration_Reason%", plugin.data.getReasonFromMuteTable(player.getUniqueId())))));
            Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&',(Objects.requireNonNull(Names.get().getString("TempMute.Notification.Duration")).replaceAll("%PFModeration_Duration%", formatTime(muteDuration)))));

            if (player.isOnline()) {
                String message1 = Objects.requireNonNull(Names.get().getString("TempMute.Layout.Duration")).replaceAll("%PFModeration_Duration%", formatTime(muteDuration));
                String message2 = Objects.requireNonNull(Names.get().getString("TempMute.Layout.Reason")).replaceAll("%PFModeration_Reason%", plugin.data.getReasonFromMuteTable(player.getUniqueId()));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message1));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message2));
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
        if (months > 0) {sb.append(months).append(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Names.get().getString("TempMute.Group.Months"))));}
        if (days > 0) {sb.append(days).append(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Names.get().getString("TempMute.Group.Days"))));}
        if (hours > 0) {sb.append(hours).append(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Names.get().getString("TempMute.Group.Hours"))));}
        if (minutes > 0) {sb.append(minutes).append(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Names.get().getString("TempMute.Group.Minutes"))));}
        if (seconds > 0) {sb.append(seconds).append(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Names.get().getString("TempMute.Group.Seconds"))));}

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
            long maxTime = getMaxMuteTime(sender);
            if (duration > maxTime) {
                String message = Objects.requireNonNull(Names.get().getString("TempMute.Max-Duration")).replaceAll("%PFModeration_MAX%", formatTime(maxTime) + ".");
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                return -555;
            }
            return duration;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    private long getMaxMuteTime(Player player) {
        if (player.hasPermission("PerfectModeration.TempMute.Minute.20")) {
            return 20 * 60 * 1000;
        } else if (player.hasPermission("PerfectModeration.TempMute.Minute.60")) {
            return 60 * 60 * 1000;
        } else if (player.hasPermission("PerfectModeration.TempMute.Minute.360")) {
            return 360 * 60 * 1000;
        } else if (player.hasPermission("PerfectModeration.TempMute.Minute.1440")) {
            return 24 * 60 * 60 * 1000;
        } else if (player.hasPermission("PerfectModeration.TempMute.Week.1")) {
            return 7 * 24 * 60 * 60 * 1000;
        } else if (player.hasPermission("PerfectModeration.TempMute.Week.2")) {
            return 14 * 24 * 60 * 60 * 1000;
        } else if (player.hasPermission("PerfectModeration.TempMute.Month.1")) {
            return 30 * 24 * 60 * 60 * 1000L;
        } else if (player.hasPermission("PerfectModeration.TempMute.Month.6")) {
            return 6 * 30 * 24 * 60 * 60 * 1000L;
        } else if (player.hasPermission("PerfectModeration.TempMute.Month.12")) {
            return 12 * 30 * 24 * 60 * 60 * 1000L;
        } else {
            return 0;
        }
    }

}
