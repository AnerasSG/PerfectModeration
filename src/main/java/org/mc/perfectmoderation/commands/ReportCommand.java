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

import java.sql.Timestamp;
import java.util.*;

public class ReportCommand implements CommandExecutor {
    PerfectModeration plugin;
    String ServerName;
    public ReportCommand(PerfectModeration perfectModeration) {
        plugin = perfectModeration;

    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(commandSender instanceof Player)) return false;
        Player reporter = (Player) commandSender;

        if(command.getName().equalsIgnoreCase("report")){
            if (args.length < 2) {
                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Names.get().getString("Reports.Usage"))));
                return false;
            }

            Player p = Bukkit.getPlayer(args[0]);
            if (p != null) {
                String description = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                String ip = Objects.requireNonNull(p.getAddress()).getAddress().getHostAddress();
                Timestamp dateOfIssue = new Timestamp(System.currentTimeMillis());
                plugin.data.addReport(ip, reporter.getName(), p.getName(), description, ServerName, dateOfIssue);
            } else {
                commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Names.get().getString("Reports.Player-not-found"))));
                return true;
            }

            reporter.sendMessage(ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Names.get().getString("Reports.Successfully-reported"))));
            return true;
        }
        return true;
    }
}
