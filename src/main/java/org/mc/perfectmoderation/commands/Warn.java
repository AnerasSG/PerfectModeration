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

public class Warn implements CommandExecutor {
    PerfectModeration plugin;

    public Warn(PerfectModeration perfectModeration) {
        plugin = perfectModeration;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player)) return false;
        Player sender = (Player) commandSender;

        if(command.getName().equalsIgnoreCase("warn")) {
            String playerName = args[0];
            Player player = Bukkit.getPlayer(playerName);
            UUID uuid;
            if (player == null) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
                if (offlinePlayer.hasPlayedBefore()) {
                    uuid = offlinePlayer.getUniqueId();
                }else{
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Names.get().getString("Warn.NON-Player"))));
                    return true;
                }
            }else {
                uuid = player.getUniqueId();
            }
            String timeArg = "1w";
            long warnDuration = parseTimeArgument(timeArg);
            long currentTime = System.currentTimeMillis();
            long warnEndTime = currentTime + warnDuration;

            String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            plugin.data.addWarn(uuid, playerName, reason, new Timestamp(warnEndTime), warnEndTime);
            if(plugin.data.getWarnCount(uuid) == 15){
                long banDuration = parseTimeArgument(Objects.requireNonNull(Names.get().getString("Warn.15.Punishment")));
                long banEndTime = currentTime + banDuration;
                plugin.data.addPlayerToBanTable(playerName, uuid, banEndTime, Names.get().getString("Warn.15.Reason"), sender.getName());
                plugin.data.addPunishment(plugin.data.getIPByUUID(uuid), "Ban", new Timestamp(currentTime), new Timestamp(banEndTime), sender.getName());
                takeMessage(player, banDuration);
            }else if(plugin.data.getWarnCount(uuid) == 12){
                long banDuration = parseTimeArgument(Objects.requireNonNull(Names.get().getString("Warn.12.Punishment")));
                long banEndTime = currentTime + banDuration;
                plugin.data.addPlayerToBanTable(playerName, uuid, banEndTime, Names.get().getString("Warn.12.Reason"), sender.getName());
                plugin.data.addPunishment(plugin.data.getIPByUUID(uuid), "Ban", new Timestamp(currentTime), new Timestamp(banEndTime), sender.getName());
                takeMessage(player, banDuration);
            }else if(plugin.data.getWarnCount(uuid) == 9){
                long banDuration = parseTimeArgument(Objects.requireNonNull(Names.get().getString("Warn.9.Punishment")));
                long banEndTime = currentTime + banDuration;
                plugin.data.addPlayerToBanTable(playerName, uuid, banEndTime, Names.get().getString("Warn.9.Reason"), sender.getName());
                plugin.data.addPunishment(plugin.data.getIPByUUID(uuid), "Ban", new Timestamp(currentTime), new Timestamp(banEndTime), sender.getName());
                takeMessage(player, banDuration);
            }else if(plugin.data.getWarnCount(uuid) == 6){
                long banDuration = parseTimeArgument(Objects.requireNonNull(Names.get().getString("Warn.6.Punishment")));
                long banEndTime = currentTime + banDuration;
                plugin.data.addPlayerToBanTable(playerName, uuid, banEndTime, Names.get().getString("Warn.6.Reason"), sender.getName());
                plugin.data.addPunishment(plugin.data.getIPByUUID(uuid), "Ban", new Timestamp(currentTime), new Timestamp(banEndTime), sender.getName());
                takeMessage(player, banDuration);
            }else if(plugin.data.getWarnCount(uuid) == 3){
                long banDuration = parseTimeArgument(Objects.requireNonNull(Names.get().getString("Warn.3.Punishment")));
                long banEndTime = currentTime + banDuration;
                plugin.data.addPlayerToBanTable(playerName, uuid, banEndTime, Names.get().getString("Warn.3.Reason"), sender.getName());
                plugin.data.addPunishment(plugin.data.getIPByUUID(uuid), "Ban", new Timestamp(currentTime), new Timestamp(banEndTime), sender.getName());
                takeMessage(player, banDuration);
            }
            String string = Names.get().getString("Warn.Done.ForOperator").replaceAll("%PFModeration_Player_Name%", playerName);
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', string));
            if(player != null & player.isOnline()){
                String message = Names.get().getString("Warn.Done.ForPlayer").replaceAll("%PFModeration_Reason%", reason);
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            }
        }
            return false;
    }

    public void takeMessage(Player player, long banDuration){
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
    private long parseTimeArgument(String timeArg) {
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
            return duration;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
