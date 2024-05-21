package org.mc.perfectmoderation.commands;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.mc.perfectmoderation.PerfectModeration;
import org.mc.perfectmoderation.configs.Names;
import org.mc.perfectmoderation.custom.Punishment;
import org.mc.perfectmoderation.custom.Report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportsCommand implements CommandExecutor, Listener {
    PerfectModeration plugin;
    private final int reportsPerPage = 28;
    List<Inventory> inventories = new ArrayList<>();
    private final Map<Player, Integer> playerPages = new HashMap<>();
    private final Map<Player, List<Inventory>> playerInventories = new HashMap<>();
    private final Map<Player, Inventory> playerInventoriesForAll = new HashMap<>();

    public ReportsCommand(PerfectModeration perfectModeration) {
        plugin = perfectModeration;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player)) return false;
        Player player = (Player) commandSender;

        if (command.getName().equalsIgnoreCase("reports")) {
            if(player.hasPermission("PerfectModeration.Reports")){
                if(playerInventories.get(player) != null){
                    playerInventories.get(player).clear();
                    playerInventories.remove(player);
                }
                if(plugin.data.getReportsFromDatabase().isEmpty()){
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', Names.get().getString("Reports.No-Reports")));
                }else{
                    openReportsInventory(player, plugin.data.getReportsFromDatabase().size());
                }
            }else{
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', Names.get().getString("Do-not-have-permission")));
            }
            return true;
        }

        return false;
    }
    public void openReportsInventory(Player player, int totalReports) {
        int totalPages = (int) Math.ceil((double) totalReports / reportsPerPage);

        for (int page = 1; page <= totalPages; page++) {
            Inventory inventory = createReportInventory(player, page, totalReports);
            inventories.add(inventory);
        }
        if(!inventories.isEmpty()) {
            playerPages.put(player, 0);
            playerInventories.put(player, inventories);
            player.openInventory(inventories.get(0));
        }

    }
    private Inventory createReportInventory(Player player, int page, int totalReports) {
        Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', Names.get().getString("Reports.Inventory.Title") + " " + page));
        int startIndex = (page - 1) * reportsPerPage;
        int endIndex = Math.min(page * reportsPerPage, totalReports);

        List<Report> reports = plugin.data.getReportsFromDatabase();
        List<Report> reportsOnPage = reports.subList(startIndex, endIndex);


        setPanels(inventory);

        for(Report report : reportsOnPage){
            ItemStack reportItem = createReportItem(report);
            inventory.addItem(reportItem);
        }

        if (page < Math.ceil((double) totalReports / reportsPerPage)) {
            ItemStack nextPageItem = createNextPageItem();
            inventory.setItem(53, nextPageItem);
        }
        if(page > 1){
            ItemStack previousPageItem = createPreviousPageItem();
            inventory.setItem(45, previousPageItem);
        }

        return inventory;
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();

        if (clickedInventory != null && event.getView().getTitle().startsWith(ChatColor.translateAlternateColorCodes('&', Names.get().getString("Reports.Inventory.Title")))) {
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.getItemMeta() != null) {
                String itemName = clickedItem.getItemMeta().getDisplayName();
                if (itemName != null && itemName.equals(ChatColor.translateAlternateColorCodes('&', Names.get().getString("Reports.Inventory.Next-page")))) {

                    int currentPage = playerPages.get(player) + 1;
                    playerPages.put(player, currentPage);
                    player.openInventory(inventories.get(currentPage));
                } else if (itemName != null && itemName.equals(ChatColor.translateAlternateColorCodes('&', Names.get().getString("Reports.Inventory.Previous-page")))) {
                    int currentPage = playerPages.get(player) - 1;
                    playerPages.put(player, currentPage);
                    player.openInventory(inventories.get(currentPage));
                } else if (event.getClick() == ClickType.RIGHT && itemName != null) {
                    try {
                    int Id = Integer.parseInt(itemName);
                    playerInventoriesForAll.put(player, createInventoryForReport(Id));
                    player.openInventory(playerInventoriesForAll.get(player));
                    playerInventoriesForAll.remove(player);
                    } catch (NumberFormatException e) {
                    }
                } else if (event.getClick() == ClickType.LEFT && itemName != null) {
                    try {
                        int Id = Integer.parseInt(itemName);
                        playerInventoriesForAll.put(player, createInventoryForInfo(Id));
                        player.openInventory(playerInventoriesForAll.get(player));
                        playerInventoriesForAll.remove(player);
                    } catch (NumberFormatException e) {
                    }
                }
            }

        }else if(clickedInventory!= null && event.getView().getTitle().startsWith(ChatColor.translateAlternateColorCodes('&', Names.get().getString("Reports.Inventory-for-checking-reports.Name")))){
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.getItemMeta() != null) {
                String itemName = clickedItem.getItemMeta().getDisplayName();
                if (itemName != null && itemName.equals(ChatColor.translateAlternateColorCodes('&', Names.get().getString("Reports.Inventory-for-checking-reports.Back.Name")))) {
                    int currentPage = playerPages.get(player);
                    playerPages.put(player, currentPage);
                    player.openInventory(inventories.get(currentPage));
                } else if (itemName != null && itemName.equals(ChatColor.translateAlternateColorCodes('&', Names.get().getString("Reports.Inventory-for-checking-reports.Checked.Name")))) {
                    ItemStack item = clickedInventory.getItem(13);
                    int ID = Integer.valueOf(item.getItemMeta().getDisplayName());
                    Report report = plugin.data.getReportByID(ID);
                    ProxiedPlayer targetPlayer = ProxyServer.getInstance().getPlayer(report.getTarget());
                    if (targetPlayer == null) {
                        player.sendMessage("Player is not online!");
                        return;
                    }
                    if(arePlayersOnSameServer(player, targetPlayer)){

                    }
                    plugin.data.deleteReportById(Integer.valueOf(ID));
                    player.closeInventory();
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', Names.get().getString("Reports.Inventory-for-checking-reports.Successfully-checked-report")));
                }
            }
        } else if (clickedInventory!=null && event.getView().getTitle().startsWith(ChatColor.translateAlternateColorCodes('&', Names.get().getString("Reports.Inventory-for-Info.Name")))) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if(clickedItem != null && clickedItem.getItemMeta() != null){
                String itemName = clickedItem.getItemMeta().getDisplayName();
                if (itemName != null && itemName.equals(ChatColor.translateAlternateColorCodes('&', Names.get().getString("Reports.Inventory-for-Info.Back.Name")))) {
                    int currentPage = playerPages.get(player);
                    playerPages.put(player, currentPage);
                    player.openInventory(inventories.get(currentPage));
                }
            }
        }
    }
    public void teleportPlayerToServer(Player player, ServerInfo server) {
        ProxiedPlayer proxiedPlayer = (ProxiedPlayer) player;

        // Получаем экземпляр ProxyServer
        ProxyServer proxyServer = ProxyServer.getInstance();

        // Телепортируем игрока на другой сервер
        proxiedPlayer.connect(server);
    }
    public void teleportPlayer(Player player1, ProxiedPlayer player2) {
        player1.teleport(player2.);
    }
    public boolean arePlayersOnSameServer(Player player1, Player player2) {
        ProxiedPlayer proxiedPlayer1 = (ProxiedPlayer) player1;
        ProxiedPlayer proxiedPlayer2 = (ProxiedPlayer) player2;

        ServerInfo server1 = proxiedPlayer1.getServer().getInfo();
        ServerInfo server2 = proxiedPlayer2.getServer().getInfo();

        return server1.equals(server2);
    }
    private Inventory createInventoryForReport(int ID) {
        Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', Names.get().getString("Reports.Inventory-for-checking-reports.Name")));
        Report report = plugin.data.getReportByID(ID);
        setPanels(inventory);
        ItemStack list = new ItemStack(Material.PAPER);
        ItemMeta meta = list.getItemMeta();
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.translateAlternateColorCodes('&', Names.get().getString("Reports.Inventory.Lore.Sender") + report.getReporter()));
        lore.add(ChatColor.translateAlternateColorCodes('&', Names.get().getString("Reports.Inventory.Lore.Target") + report.getTarget()));
        lore.add(ChatColor.translateAlternateColorCodes('&', Names.get().getString("Reports.Inventory.Lore.Description") + report.getDescription()));
        lore.add(ChatColor.translateAlternateColorCodes('&', Names.get().getString("Reports.Inventory.Lore.Date") + report.getDateOfIssue()));
        lore.add(ChatColor.translateAlternateColorCodes('&', Names.get().getString("Reports.Inventory.Lore.Ip") + report.getIp()));
        meta.setLore(lore);
        meta.setDisplayName(String.valueOf(ID));
        list.setItemMeta(meta);
        inventory.setItem(13, list);

        ItemStack redDye = new ItemStack(Material.RED_DYE);
        ItemMeta redDyeMeta = redDye.getItemMeta();
        List<String> redDyeLore = new ArrayList<>();
        redDyeLore.add(ChatColor.translateAlternateColorCodes('&', Names.get().getString("Reports.Inventory-for-checking-reports.Back.Lore")));
        redDyeMeta.setLore(redDyeLore);
        redDyeMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', Names.get().getString("Reports.Inventory-for-checking-reports.Back.Name")));
        redDye.setItemMeta(redDyeMeta);
        inventory.setItem(30, redDye);

        ItemStack greenDye = new ItemStack(Material.LIME_DYE);
        ItemMeta greenDyeMeta = redDye.getItemMeta();
        List<String> greenDyeLore = new ArrayList<>();
        greenDyeLore.add(ChatColor.translateAlternateColorCodes('&', Names.get().getString("Reports.Inventory-for-checking-reports.Checked.Lore")));
        greenDyeMeta.setLore(greenDyeLore);
        greenDyeMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', Names.get().getString("Reports.Inventory-for-checking-reports.Checked.Name")));
        greenDye.setItemMeta(greenDyeMeta);

        inventory.setItem(32, greenDye);

        return inventory;
    }
    private Inventory createInventoryForInfo(int ID){
        Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.translateAlternateColorCodes('&', Names.get().getString("Reports.Inventory-for-Info.Name")));
        Report report = plugin.data.getReportByID(ID);
        String string = "";
        setPanels(inventory);
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName(report.getIp());
        List<String> lore = new ArrayList<>();
        List<String> names = plugin.data.getNamesByIP(report.getIp());
        for (int i = 0; i < names.size(); i++) {
            String name = names.get(i);
            string += name;
            if (i < names.size() - 1) {
                string += ", ";
            }
        }
        lore.add(ChatColor.translateAlternateColorCodes('&', Names.get().getString("Reports.Inventory-for-Info.Lore.Accounts")) + string);
        List<Punishment> punishments = plugin.data.getPunishmentsByIP(report.getIp());
        for(Punishment p : punishments){
            String pString = p.getPunishment() + ": " + p.getDateOfIssue() +  "; " + p.getEndDate();
            lore.add(pString);
        }
        itemMeta.setLore(lore);
        item.setItemMeta(itemMeta);
        inventory.setItem(22, item);

        ItemStack redDye = new ItemStack(Material.RED_DYE);
        ItemMeta redDyeMeta = redDye.getItemMeta();
        List<String> redDyeLore = new ArrayList<>();
        redDyeLore.add(ChatColor.translateAlternateColorCodes('&', Names.get().getString("Reports.Inventory-for-Info.Back.Lore")));
        redDyeMeta.setLore(redDyeLore);
        redDyeMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', Names.get().getString("Reports.Inventory-for-Info.Back.Name")));
        redDye.setItemMeta(redDyeMeta);
        inventory.setItem(40, redDye);
        return inventory;
    }
    private ItemStack createNextPageItem() {
        ItemStack nextPageItem = new ItemStack(Material.ARROW);
        ItemMeta meta = nextPageItem.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', Names.get().getString("Reports.Inventory.Next-page")));
        nextPageItem.setItemMeta(meta);
        return nextPageItem;
    }
    private ItemStack createPreviousPageItem(){
        ItemStack previousPageItem = new ItemStack(Material.ARROW);
        ItemMeta meta = previousPageItem.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', Names.get().getString("Reports.Inventory.Previous-page")));
        previousPageItem.setItemMeta(meta);
        return previousPageItem;
    }
    public void setPanels(Inventory inventory){
        ItemStack item = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setDisplayName("§r");
        item.setItemMeta(itemMeta);
        inventory.setItem(0, item);
        inventory.setItem(1, item);
        inventory.setItem(2, item);
        inventory.setItem(3, item);
        inventory.setItem(4, item);
        inventory.setItem(5, item);
        inventory.setItem(6, item);
        inventory.setItem(7, item);
        inventory.setItem(8, item);
        inventory.setItem(9, item);
        inventory.setItem(17, item);
        inventory.setItem(18, item);
        inventory.setItem(26, item);
        inventory.setItem(27, item);
        inventory.setItem(35, item);
        inventory.setItem(36, item);
        inventory.setItem(44, item);
        inventory.setItem(45, item);
        inventory.setItem(46, item);
        inventory.setItem(47, item);
        inventory.setItem(48, item);
        inventory.setItem(49, item);
        inventory.setItem(50, item);
        inventory.setItem(51, item);
        inventory.setItem(52, item);
        inventory.setItem(53, item);
    }
    private ItemStack createReportItem (Report report){
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(String.valueOf(report.getId()));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.translateAlternateColorCodes('&', Names.get().getString("Reports.Inventory.Lore.Sender") + report.getReporter()));
            lore.add(ChatColor.translateAlternateColorCodes('&', Names.get().getString("Reports.Inventory.Lore.Target") + report.getTarget()));
            lore.add(ChatColor.translateAlternateColorCodes('&', Names.get().getString("Reports.Inventory.Lore.Description") + report.getDescription()));
            lore.add(ChatColor.translateAlternateColorCodes('&', Names.get().getString("Reports.Inventory.Lore.Date") + report.getDateOfIssue()));
            lore.add(ChatColor.translateAlternateColorCodes('&', Names.get().getString("Reports.Inventory.Lore.Ip") + report.getIp()));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }
}
