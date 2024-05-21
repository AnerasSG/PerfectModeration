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

import java.util.Objects;
import java.util.UUID;

public class UnBan implements CommandExecutor {
    PerfectModeration plugin;
    public UnBan(PerfectModeration perfectModeration) {
        plugin = perfectModeration;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player)) return false;
        Player sender = (Player) commandSender;

        if(command.getName().equalsIgnoreCase("unban")){
            if(!sender.hasPermission("PerfectModeration.UnBan")){
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Names.get().getString("Do-not-have-permission"))));
                return true;
            }
            if (args.length < 1) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Names.get().getString("UnBan.Usage"))));
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
            if(plugin.data.isPlayerInBanTable(uuid)){
                plugin.data.removePlayerFromBanTable(uuid);
                plugin.data.removeLastPunishment(plugin.data.getIPByUUID(uuid));
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Names.get().getString("UnBan.Done.ForOperator").replaceAll("%PFModeration_Player_Name%", playerName)));
            }else{
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Names.get().getString("UnBan.NotBanned")));
            }
        }
        return false;
    }
}