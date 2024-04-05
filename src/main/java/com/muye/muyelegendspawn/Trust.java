package com.muye.muyelegendspawn;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Trust implements Listener {

    static final File trust = new File(MuyeLegendSpawn.getInstance().getDataFolder(), "/Data/trust.yml");
    public static Map<String, List<UUID>> PlayerTrustList = new HashMap<>();
    static YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(trust);

    public static String BackCommand;

    static Inventory gui;

    static Inventory players;

    public static int defaultNumber;

    public static int vipNumber;

    public static void Load(FileConfiguration configuration) {
        Trust.defaultNumber = configuration.getInt("Trust.Number.default");
        Trust.vipNumber = configuration.getInt("Trust.Number.vip");
        Trust.BackCommand = configuration.getString("Gui.BackCommand");
        gui = Bukkit.createInventory(null, 36, "§a信任名单");
        ItemStack itemStack = new ItemStack(Material.getMaterial("PIXELMON_COIN_CASE"), 1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName("§a点击添加信任人员");
        itemStack.setItemMeta(itemMeta);
        gui.setItem(31, itemStack);
        ItemStack itemStack1 = new ItemStack(Material.BARRIER, 1);
        ItemMeta itemMeta1 = itemStack.getItemMeta();
        itemMeta1.setDisplayName("§f关闭");
        itemStack1.setItemMeta(itemMeta1);
        gui.setItem(35, itemStack1);
        ItemStack itemStack2 = new ItemStack(Material.SPRUCE_DOOR_ITEM, 1);
        ItemMeta itemMeta2 = itemStack.getItemMeta();
        itemMeta2.setDisplayName("§f返回");
        itemStack2.setItemMeta(itemMeta2);
        gui.setItem(27, itemStack2);
        players = Bukkit.createInventory(null, 54, "§a在线玩家列表");
        ItemStack itemStack3 = new ItemStack(Material.ARROW, 1);
        ItemMeta itemMeta3 = itemStack.getItemMeta();
        itemMeta3.setDisplayName("§f下一页");
        itemStack3.setItemMeta(itemMeta3);
        players.setItem(53, itemStack3);
        ItemStack itemStack4 = new ItemStack(Material.ARROW, 1);
        ItemMeta itemMeta4 = itemStack.getItemMeta();
        itemMeta4.setDisplayName("§f上一页");
        itemStack4.setItemMeta(itemMeta4);
        players.setItem(45, itemStack4);
        players.setItem(49, itemStack2);
    }

    public static void OpenTrustGui(Player player, Inventory Gui) {
        Inventory inventory = Bukkit.createInventory(null, Gui.getSize(), Gui.getTitle());
        inventory.setContents(Gui.getContents());
        List<UUID> list = getTrustList(player.getName());
        if (list != null) {
            for (UUID uuid : list) {
                if (uuid == null) {
                    continue;
                }
                ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
                SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
                Bukkit.getScheduler().runTaskAsynchronously(MuyeLegendSpawn.getInstance(), () -> {
                    skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(uuid));
                    skull.setItemMeta(skullMeta);
                });
                skullMeta.setDisplayName("§f" + Bukkit.getOfflinePlayer(uuid).getName());
                List<String> lore = Arrays.asList("", "§f右键即可删除");
                skullMeta.setLore(lore);
                skull.setItemMeta(skullMeta);
                inventory.addItem(skull);
            }
        }
        player.openInventory(inventory);
    }

    private void OpenPlayersGui(Player player, Inventory Gui) {
        Inventory inventory = Bukkit.createInventory(null, Gui.getSize(), Gui.getTitle());
        inventory.setContents(Gui.getContents());
        for (Player goal : Bukkit.getOnlinePlayers()){
            if (goal == player){
                continue;
            }
            ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
            Bukkit.getScheduler().runTaskAsynchronously(MuyeLegendSpawn.getInstance(), () -> {
                skullMeta.setOwningPlayer(goal);
                skull.setItemMeta(skullMeta);
            });
            skullMeta.setDisplayName("§f" + goal.getName());
            List<String> lore = Arrays.asList("", "§f左键即可添加");
            skullMeta.setLore(lore);
            skull.setItemMeta(skullMeta);
            inventory.addItem(skull);
        }
        player.openInventory(inventory);
    }

    public static void AddToTrustList(String player, UUID uuid) {
        List<UUID> trustlist = PlayerTrustList.get(player);
        if (trustlist == null) {
            trustlist = new ArrayList<>();
        }
        trustlist.add(uuid);
        PlayerTrustList.put(player, trustlist);
    }

    public static void DeleteFromTrustList(String player, UUID uuid) {
        List<UUID> trustlist = PlayerTrustList.get(player);
        trustlist.forEach(s -> {
            if (s.equals(uuid)) {
                trustlist.set(trustlist.indexOf(s), null);
            }
        });
        PlayerTrustList.put(player, trustlist);
    }

    public static List<UUID> getTrustList(String player) {
        if (PlayerTrustList.get(player) == null) {
            return new ArrayList<>();
        }
        return PlayerTrustList.get(player);
    }

    private static List<UUID> Parse(List<String> stringList) {
        return stringList.stream().map(UUID::fromString).collect(Collectors.toList());
    }

    public static void LoadData() {
        Set<String> get = yamlConfiguration.getConfigurationSection("").getKeys(false);
        for (String s : get) {
            PlayerTrustList.put(s, Parse(yamlConfiguration.getStringList(s)));
        }
    }

    public static void ClearData() {
        PlayerTrustList.clear();
    }

    public static void SaveData() {
        for (Map.Entry<String, List<UUID>> entry : PlayerTrustList.entrySet()) {
            String player = entry.getKey();
            List<UUID> value = entry.getValue();
            List<String> stringList = value.stream().filter(Objects::nonNull).map(UUID::toString).collect(Collectors.toList());
            yamlConfiguration.set(player, stringList);
        }
        SaveTrustList();
    }

    private static void SaveTrustList() {
        try {
            yamlConfiguration.save(trust);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @EventHandler
    public void ClickTrustGui(InventoryClickEvent event) {
        if (event.getRawSlot() < 0 || event.getCurrentItem().getType().equals(Material.AIR)) {
            return;
        }
        if (event.getClickedInventory().getName().equals("§a信任名单")) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            if (event.getCurrentItem().getType().equals(Material.BARRIER)) {
                player.closeInventory();
            }
            if (event.getRawSlot() == 31) {
                OpenPlayersGui(player, players);
            }
            if (event.getRawSlot() == 27) {
                Bukkit.getServer().dispatchCommand(MuyeLegendSpawn.getInstance().getServer().getConsoleSender(), Papi.parse(player, BackCommand.replace("%player%", player.getName())));
            }
            if (event.getCurrentItem().getType().equals(Material.SKULL_ITEM) && event.getClick().isRightClick()) {
                DeleteFromTrustList(player.getName(), Objects.requireNonNull(getTrustList(player.getName())).get(event.getRawSlot()));
                MMessage.sendMes(player, MMessage.MessageList.get("DeleteFromTrust").replace("%goal%", event.getCurrentItem().getItemMeta().getDisplayName()));
                OpenTrustGui(player, gui);
            }
        }
        if (event.getClickedInventory().getName().equals("§a在线玩家列表")){
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            if (event.getRawSlot() == 49) {
                OpenTrustGui(player, gui);
            }
        }
    }

    @EventHandler
    public void PlayerJoinEvent(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        List<UUID> trustList = getTrustList(player.getName());
        if (trustList != null) {
            PlayerTrustList.put(player.getName(), trustList);
        }
    }

    @EventHandler
    public void PlayerLeaveEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        List<UUID> trustList = getTrustList(player.getName());
        if (trustList != null) {
            yamlConfiguration.set(player.getName(), trustList);
            SaveTrustList();
        }
    }

}
