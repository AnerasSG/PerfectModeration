package org.mc.perfectmoderation.Events;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import org.mc.perfectmoderation.PerfectModeration;
import org.mc.perfectmoderation.configs.Names;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class Events implements Listener {
    PerfectModeration plugin;

    public Events(PerfectModeration perfectModeration) {
        plugin = perfectModeration;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        plugin.data.createPlayer(String.valueOf(player.getAddress().getAddress().getHostAddress()), player.getName(), player.getUniqueId(), null);
        if(!plugin.data.isVanished(player.getUniqueId())){
            plugin.data.updateServerName(player.getUniqueId(), Names.get().getString("ServerName"));
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (plugin.data.isPlayerInMuteTable(player.getUniqueId())) {
            long muteEndTime = plugin.data.getTimeFromMuteTable(player.getUniqueId());
            long currentTime = System.currentTimeMillis();
            long remainingTime = muteEndTime - currentTime;
            String[] commandArgs = event.getMessage().split(" ");
            String commandName = commandArgs[0].substring(1);
            List<String> restrictedCommands = Names.get().getStringList("TempMute.Commands");
            for (String command : restrictedCommands) {
                if (commandName.equalsIgnoreCase(command)) {
                    event.setCancelled(true);
                    String message1 = Objects.requireNonNull(Names.get().getString("TempMute.Layout.Duration")).replaceAll("%PFModeration_Duration%", formatTime(remainingTime));
                    String message2 = Objects.requireNonNull(Names.get().getString("TempMute.Layout.Reason")).replaceAll("%PFModeration_Reason%", plugin.data.getReasonFromMuteTable(player.getUniqueId()));
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', message1));
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', message2));
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        if (plugin.data.isPlayerInBanTable(player.getUniqueId())) {
            long muteEndTime = plugin.data.getTimeFromBanTable(player.getUniqueId());
            long currentTime = System.currentTimeMillis();
            long remainingTime = muteEndTime - currentTime;
            StringBuilder kickMessage = new StringBuilder();
            if (remainingTime > 0) {
                for (String string : Names.get().getStringList("TempBan.Layout")) {
                    string = string.replaceAll("%PFModeration_Duration%", formatTime(remainingTime));
                    string = string.replaceAll("%PFModeration_Reason%", plugin.data.getReasonFromBanTable(player.getUniqueId()));
                    string = string.replaceAll("%PFModeration_Date%", plugin.data.getDateFromBanTable(player.getUniqueId()));
                    string = string.replaceAll("%PFModeration_Operator%", plugin.data.getOperatorFromBanTable(player.getUniqueId()));
                    kickMessage.append(ChatColor.translateAlternateColorCodes('&', string)).append("\n");
                }
                player.kickPlayer(kickMessage.toString());
            } else {
                plugin.data.removePlayerFromBanTable(player.getUniqueId());
            }
            return;
        }
        if(plugin.data.hasWarnings(player.getUniqueId())){
            long muteEndTime = plugin.data.getTimeFromWarnTable(player.getUniqueId());
            long currentTime = System.currentTimeMillis();
            long remainingTime = muteEndTime - currentTime;
            if (remainingTime <= 0) {
                plugin.data.removeWarnFromWarnTable(player.getUniqueId());
            }
        }
    }


    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (plugin.data.isPlayerInMuteTable(player.getUniqueId())) {
            long muteEndTime = plugin.data.getTimeFromMuteTable(player.getUniqueId());
            long currentTime = System.currentTimeMillis();
            long remainingTime = muteEndTime - currentTime;

            if (remainingTime > 0) {
                event.setCancelled(true);
                String message1 = Objects.requireNonNull(Names.get().getString("TempMute.Layout.Duration")).replaceAll("%PFModeration_Duration%", formatTime(remainingTime));
                String message2 = Objects.requireNonNull(Names.get().getString("TempMute.Layout.Reason")).replaceAll("%PFModeration_Reason%", plugin.data.getReasonFromMuteTable(player.getUniqueId()));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message1));
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message2));
            } else {
                plugin.data.removePlayerFromMuteTable(player.getUniqueId());
            }
        }

    }


    public static String formatTime(long milliseconds) {
        if (milliseconds <= 0) {
            return "меньше минуты";
        } else {
            return formatTimeRemaining(milliseconds);
        }
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

}
