package org.mc.perfectmoderation;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.mc.perfectmoderation.Events.Events;
import org.mc.perfectmoderation.MySql.Data;
import org.mc.perfectmoderation.MySql.MySQL;
import org.mc.perfectmoderation.commands.*;
import org.mc.perfectmoderation.configs.MySQLConfig;
import org.mc.perfectmoderation.configs.Names;

import java.sql.SQLException;
import java.util.*;

public final class PerfectModeration extends JavaPlugin {
    public MySQL SQL;
    public Data data;
    @Override
    public void onEnable() {

        if (Bukkit.getPluginManager().getPlugin("LuckPerms") == null) {
            getLogger().warning(org.bukkit.ChatColor.RED + "The LuckPerms wasn't found. The PerfectMoney may not work correctly");

        }

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        MySQLConfig.setup();
        MySQLConfig.get().addDefault("Storage", "MySQL");
        MySQLConfig.get().addDefault("host", "host");
        MySQLConfig.get().addDefault("port", "port");
        MySQLConfig.get().addDefault("database", "database");
        MySQLConfig.get().addDefault("username", "username");
        MySQLConfig.get().addDefault("password", "password");
        MySQLConfig.get().options().copyDefaults(true);
        MySQLConfig.save();

        this.SQL = new MySQL();
        this.data = new Data(this);
        if(Objects.requireNonNull(MySQLConfig.get().get("Storage")).equals("MySQL")) {
            try {
                SQL.Connect();
            } catch (ClassNotFoundException | SQLException e) {
                //e.printStackTrace();
                Bukkit.getConsoleSender().sendMessage(org.bukkit.ChatColor.AQUA + "[PerfectModeration]" + ChatColor.RED + " Database isn't connected");
            }
            if (SQL.isConnected()) {
                Bukkit.getConsoleSender().sendMessage(org.bukkit.ChatColor.AQUA + "[PerfectModeration]" + ChatColor.GREEN + " Database is connected");
                data.createPunishmentsTable();
                data.createReportsTable();
                data.createAccountsTable();
                data.createMuteTable();
                data.createBanTable();
                data.createWarnTable();
            }
        }
        List<String> commands = new ArrayList<>();
        commands.add("msg");
        List<String> banNotification = new ArrayList<>();
        banNotification.add("&cATTENTION!");
        banNotification.add(" ");
        banNotification.add("&cYou have been banned from this server.");
        banNotification.add(" ");
        banNotification.add("&cDate: &b%PFModeration_Date%");
        banNotification.add(" ");
        banNotification.add("&cReason: &b%PFModeration_Reason%");
        banNotification.add(" ");
        banNotification.add("&cBanned by: &b%PFModeration_Operator%");
        banNotification.add(" ");
        banNotification.add("&cExpires in: &b%PFModeration_Duration%");

        Names.setup();
        Names.get().addDefault("ServerName", "ServerName");
        Names.get().addDefault("Vanished.Checked", "&aChecked");
        Names.get().addDefault("Vanished.Back", "&cBack");
        Names.get().addDefault("Do-not-have-permission", "&cYou don't have a permission for that");
        Names.get().addDefault("Reports.Usage", "&cUsage: /report <target> <description>");
        Names.get().addDefault("Reports.Player-not-found", "&cI couldn't find such a player.");
        Names.get().addDefault("Reports.Successfully-reported", "&cYour report has been submitted successfully.");
        Names.get().addDefault("Reports.No-Reports", "&cThere are no active reports");
        Names.get().addDefault("Reports.Inventory.Title", "&cReports page");
        Names.get().addDefault("Reports.Inventory.Next-page", "&cNext page");
        Names.get().addDefault("Reports.Inventory.Previous-page", "&cPrevious page");
        Names.get().addDefault("Reports.Inventory.Lore.Sender", "&cSender:");
        Names.get().addDefault("Reports.Inventory.Lore.Target", "&cTarget:");
        Names.get().addDefault("Reports.Inventory.Lore.Description", "&cDescription:");
        Names.get().addDefault("Reports.Inventory.Lore.ServerName", "&cServerName");
        Names.get().addDefault("Reports.Inventory.Lore.Date", "&cDate:");
        Names.get().addDefault("Reports.Inventory.Lore.Ip", "&cIp:");
        Names.get().addDefault("Reports.Inventory-for-checking-reports.Name", "&cInventory for checking reports");
        Names.get().addDefault("Reports.Inventory-for-checking-reports.Back.Name", "&cBack");
        Names.get().addDefault("Reports.Inventory-for-checking-reports.Back.Lore", "&cPress to go back.");
        Names.get().addDefault("Reports.Inventory-for-checking-reports.Checked.Name", "&cChecked");
        Names.get().addDefault("Reports.Inventory-for-checking-reports.Checked.Lore", "&cPress to change the status of the report to 'verified'.");
        Names.get().addDefault("Reports.Inventory-for-checking-reports.Successfully-checked-report", "&cYou have successfully checked the report");
        Names.get().addDefault("Reports.Inventory-for-Info.Name", "&cInventory for info");
        Names.get().addDefault("Reports.Inventory-for-Info.Back.Name", "&cBack");
        Names.get().addDefault("Reports.Inventory-for-Info.Back.Lore", "&cPress to go back.");
        Names.get().addDefault("Reports.Inventory-for-Info.Lore.Accounts", "&cAccounts: ");

        Names.get().addDefault("TempMute.Usage", "&cUsage: /temp-mute <username> <time> <reason>");
        Names.get().addDefault("TempMute.Commands", commands);
        Names.get().addDefault("TempMute.NON-Player", "&cSuch player does not exist.");
        Names.get().addDefault("TempMute.Invalid-Mute-Duration", "&cInvalid mute duration.");
        Names.get().addDefault("TempMute.Max-Duration", "&cYou are not authorized to issue a mute for longer than %PFModeration_MAX%");
        Names.get().addDefault("TempMute.Done", "&cYou have successfully muted the player with the username: %PFModeration_Player_Name%");
        Names.get().addDefault("TempMute.Already-Muted", "&cThe player is already muted.");
        Names.get().addDefault("TempMute.Layout.Duration", "&cYou have been muted for %PFModeration_Duration%");
        Names.get().addDefault("TempMute.Layout.Reason", "&cReason: %PFModeration_Reason%");
        Names.get().addDefault("TempMute.Notification.Done", "&c%PFModeration_Operator% muted - %PFModeration_Player_Name%");
        Names.get().addDefault("TempMute.Notification.Reason", "&cReason: %PFModeration_Reason%");
        Names.get().addDefault("TempMute.Notification.Duration", "&cDuration: %PFModeration_Duration%");
        Names.get().addDefault("TempMute.Group.Less-Than-Minute", " &сFor less than a minute.");
        Names.get().addDefault("TempMute.Group.Seconds", " &cSeconds ");
        Names.get().addDefault("TempMute.Group.Minutes", " &cMinutes");
        Names.get().addDefault("TempMute.Group.Hours", " &cHours ");
        Names.get().addDefault("TempMute.Group.Days", " &cDays ");
        Names.get().addDefault("TempMute.Group.Months", " &cMonths ");

        Names.get().addDefault("UnMute.Usage", "&cUsage: /unmute <username>");
        Names.get().addDefault("UnMute.Notification", "&c%PFModeration_Operator% unmuted - %PFModeration_Player_Name%");
        Names.get().addDefault("UnMute.NON-Player", "&cSuch player does not exist.");
        Names.get().addDefault("UnMute.NotMuted", "&cThis player is not muted.");
        Names.get().addDefault("UnMute.Done.ForPlayer", "&cYour mute has been lifted by the administrator - %PFModeration_Operator%");
        Names.get().addDefault("UnMute.Done.ForOperator", "&cPlayer %PFModeration_Player_Name% has been successfully unmuted.");

        Names.get().addDefault("TempBan.Usage", "&cUsage: /temp-ban <username> <time> <reason>");
        Names.get().addDefault("TempBan.Layout", banNotification);
        Names.get().addDefault("TempBan.NON-Player", "&cSuch player does not exist.");
        Names.get().addDefault("TempBan.Invalid-Mute-Duration", "&cInvalid ban duration.");
        Names.get().addDefault("TempBan.Max-Duration", "&cYou are not authorized to issue a ban for longer than %PFModeration_MAX%");
        Names.get().addDefault("TempBan.Done", "&cYou have successfully banned the player with the username: %PFModeration_Player_Name%");
        Names.get().addDefault("TempBan.Already-Banned", "&cThe player is already banned.");
        Names.get().addDefault("TempBan.Notification.Done", "&c%PFModeration_Operator% banned - %PFModeration_Player_Name%");
        Names.get().addDefault("TempBan.Notification.Reason", "&cReason: %PFModeration_Reason%");
        Names.get().addDefault("TempBan.Notification.Duration", "&cDuration: %PFModeration_Duration%");
        Names.get().addDefault("TempBan.Group.Less-Than-Minute", " &сFor less than a minute.");
        Names.get().addDefault("TempBan.Group.Seconds", " &cSeconds ");
        Names.get().addDefault("TempBan.Group.Minutes", " &cMinutes");
        Names.get().addDefault("TempBan.Group.Hours", " &cHours ");
        Names.get().addDefault("TempBan.Group.Days", " &cDays ");
        Names.get().addDefault("TempBan.Group.Months", " &cMonths ");

        Names.get().addDefault("UnBan.Usage", "&cUsage: /unban <username>");
        Names.get().addDefault("UnBan.Notification", "&c%PFModeration_Operator% unbanned - %PFModeration_Player_Name%");
        Names.get().addDefault("UnBan.NON-Player", "&cSuch player does not exist.");
        Names.get().addDefault("UnBan.NotBanned", "&cThis player is not banned.");
        Names.get().addDefault("UnBan.Done.ForOperator", "&cPlayer %PFModeration_Player_Name% has been successfully unbanned.");

        Names.get().addDefault("Warn.NON-Player", "&cSuch player does not exist.");
        Names.get().addDefault("Warn.Done.ForPlayer", "&cYou have received a warning for the following reason: %PFModeration_Reason%");
        Names.get().addDefault("Warn.Done.ForOperator", "&cYou have successfully issued a warning to a player with a nickname: %PFModeration_Player_Name%");
        Names.get().addDefault("Warn.3.Punishment","30m");
        Names.get().addDefault("Warn.3.Reason",">3warns");
        Names.get().addDefault("Warn.6.Punishment","1140m");
        Names.get().addDefault("Warn.6.Reason",">6warns");
        Names.get().addDefault("Warn.9.Punishment","1w");
        Names.get().addDefault("Warn.9.Reason",">9warns");
        Names.get().addDefault("Warn.12.Punishment","1o");
        Names.get().addDefault("Warn.12.Reason",">12warns");
        Names.get().addDefault("Warn.15.Punishment","12o");
        Names.get().addDefault("Warn.15.Reason",">15warns");

        Names.get().options().copyDefaults(true);
        Names.save();
        Bukkit.getPluginManager().registerEvents(new Events(this), this);
        Objects.requireNonNull(Bukkit.getServer().getPluginCommand("report")).setExecutor(new ReportCommand(this));
        Objects.requireNonNull(Bukkit.getServer().getPluginCommand("reports")).setExecutor(new ReportsCommand(this));
        Objects.requireNonNull(Bukkit.getServer().getPluginCommand("temp-mute")).setExecutor(new TempMute(this));
        Objects.requireNonNull(Bukkit.getServer().getPluginCommand("unmute")).setExecutor(new Unmute(this));
        Objects.requireNonNull(Bukkit.getServer().getPluginCommand("temp-ban")).setExecutor(new TempBan(this));
        Objects.requireNonNull(Bukkit.getServer().getPluginCommand("unban")).setExecutor(new UnBan(this));
        Objects.requireNonNull(Bukkit.getServer().getPluginCommand("warn")).setExecutor(new Warn(this));
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[PerfectModeration]" + ChatColor.WHITE + " PerfectModeration was enabled");


    }

    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage(ChatColor.AQUA + "[PerfectModeration]" + ChatColor.WHITE + " PerfectModeration was disabled");
    }
}
