package org.mc.perfectmoderation.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.mc.perfectmoderation.PerfectModeration;
import org.mc.perfectmoderation.configs.Names;

import java.util.Objects;


public class Unmute implements CommandExecutor {
    PerfectModeration plugin;
    public Unmute(PerfectModeration perfectModeration) {
        plugin = perfectModeration;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player)) return false;
        Player sender = (Player) commandSender;

        if(command.getName().equalsIgnoreCase("unmute")){
            if(!sender.hasPermission("PerfectModeration.UnMute")){
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Names.get().getString("Do-not-have-permission"))));
                return true;
            }
            if (args.length < 1) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Names.get().getString("UnMute.Usage"))));
                return true;
            }

            String playerName = args[0];
            Player player = Bukkit.getPlayer(playerName);
            if(player != null){
                if(plugin.data.isPlayerInMuteTable(player.getUniqueId())){
                    plugin.data.removePlayerFromMuteTable(player.getUniqueId());
                    if (player.isOnline()) {
                        String string = Objects.requireNonNull(Names.get().getString("UnMute.Done.ForPlayer")).replaceAll("%PFModeration_Operator%", sender.getName());
                        player.sendMessage(ChatColor.translateAlternateColorCodes('&', string));
                    }
                    String string = Objects.requireNonNull(Names.get().getString("UnMute.Done.ForOperator")).replaceAll("%PFModeration_Player_Name%", playerName);
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', string));
                    String broadcastMessage = Objects.requireNonNull(Names.get().getString("UnMute.Notification")).replaceAll("%PFModeration_Operator%", sender.getName()).replaceAll("%PFModeration_Player_Name%", playerName);
                    Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', broadcastMessage));
                }else{
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Names.get().getString("UnMute.NotMuted"))));
                }
            }else{
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Names.get().getString("UnMute.NON-Player"))));
            }
            return true;
        }
        return false;
    }
}
