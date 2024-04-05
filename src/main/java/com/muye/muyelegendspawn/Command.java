package com.muye.muyelegendspawn;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Command implements CommandExecutor {

    public static List<String> args1 = Stream.of( "spawn3", "spawn2", "help", "spawn1", "tp", "open", "reload", "add", "del").sorted().collect(Collectors.toList());

    public static boolean Spawn = false;

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("help")){
                sender.sendMessage("§6-----------------");
                sender.sendMessage("§6| §6§lMuyeLegendSpawn §b§l[HELP]");
                sender.sendMessage("§6| §f/mls help 打开插件帮助");
                sender.sendMessage("§6| §f/mls open 打开信任名单");
                sender.sendMessage("§6| §f/mls tp 传送至守护神兽");
                sender.sendMessage("§6| §f/mls add [玩家] 添加玩家至信任名单");
                sender.sendMessage("§6| §f/mls del [玩家] 删除玩家至信任名单");
                if (sender.isOp()){
                    sender.sendMessage("§6| §f/mls reload 重载插件");
                    sender.sendMessage("§6| §f/mls spawn1 按照概率生成神兽");
                    sender.sendMessage("§6| §f/mls spawn2 无视概率生成神兽");
                    sender.sendMessage("§6| §f/mls spawn3 刷新本次神兽(时间清零)");
                }
                sender.sendMessage("§6-----------------");
                return false;
            }
            if (sender.isOp()){
                if (args[0].equalsIgnoreCase("reload")) {
                    MuyeLegendSpawn.getInstance().Reload();
                    sender.sendMessage(MuyeLegendSpawn.getInstance().Messages.get("Reload"));
                    return false;
                }
                if (args[0].equalsIgnoreCase("spawn1")) {
                    Spawn = true;
                    Legend.RefreshLegend();
                    return false;
                }
                if (args[0].equalsIgnoreCase("spawn2")) {
                    Spawn = true;
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "legendaryspawn");
                    return false;
                }
                if (args[0].equalsIgnoreCase("spawn3")) {
                    Legend.LegendRefreshTime = 0;
                    return false;
                }
            }
            if (args[0].equalsIgnoreCase("open") && sender instanceof Player) {
                Player player = (Player) sender;
                Trust.OpenTrustGui(player, Trust.gui);
                MMessage.sendMes(player, "OpenGui");
                return false;
            }
            if (args[0].equalsIgnoreCase("tp") && sender instanceof Player) {
                Player player = (Player) sender;
                if (Listener.CheckIfTeleportToPokemon(player)){
                    if (MuyeLegendSpawn.getInstance().economy.has(player, Listener.money)) {
                        MuyeLegendSpawn.getInstance().economy.withdrawPlayer(player, Listener.money);
                        Listener.TeleportToPokemon(player);
                        MMessage.sendMes(player, "SuccessTeleport");
                    } else {
                        MMessage.sendMes(player, "NoMoney");
                    }
                } else {
                    MMessage.sendMes(player, "FailTeleport");
                }
                return false;
            }
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add") && sender instanceof Player) {
                Player player = (Player) sender;
                if (args[1].equals(player.getName())){
                    MMessage.sendMes(player, "AddYourself");
                    return false;
                }
                int upper;
                if (player.hasPermission("MuyeLegendSpawn.vip")){
                    upper = Trust.defaultNumber;
                } else {
                    upper = Trust.vipNumber;
                }
                if (Trust.getTrustList(player.getName()).size() >= upper){
                    MMessage.sendMes(player, "ReachUpperLimit");
                    return false;
                }
                UUID goal = null;
                for (Player get : Bukkit.getOnlinePlayers()) {
                    if (args[1].equals(get.getName())) {
                        goal = get.getUniqueId();
                        break;
                    }
                }
                if (goal == null) {
                    MMessage.sendMes(player, "NotFound");
                } else {
                    if (Trust.getTrustList(player.getName()).contains(goal)){
                        MMessage.sendMes(player, MMessage.MessageList.get("AlreadyAdded").replace("%goal%", args[1]));
                    } else {
                        Trust.AddToTrustList(player.getName(), goal);
                        MMessage.sendMes(player, MMessage.MessageList.get("AddToTrust").replace("%goal%", args[1]));
                    }
                }
                return false;
            }
            if (args[0].equalsIgnoreCase("del") && sender instanceof Player) {
                Player player = (Player) sender;
                UUID goal = null;
                for (OfflinePlayer get : Bukkit.getOfflinePlayers()) {
                    if (args[1].equals(get.getName())) {
                        goal = get.getUniqueId();
                        break;
                    }
                }
                if (goal == null) {
                    MMessage.sendMes(player, "DeleteError");
                } else {
                    Trust.DeleteFromTrustList(player.getName(), goal);
                    MMessage.sendMes(player, MMessage.MessageList.get("DeleteFromTrust").replace("%goal%", args[1]));
                }
                return false;
            }
        }
        MMessage.sendMes(sender, "UnKnown");
        return false;
    }
}
