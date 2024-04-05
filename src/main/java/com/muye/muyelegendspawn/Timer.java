package com.muye.muyelegendspawn;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Timer {

    public static Map<Pokemon, List<String>> legendProtect = new HashMap<>();
    private static Timer timer;
    public BukkitRunnable bukkitRunnable = null;
    public boolean CountDownTurnOn;
    public boolean LoopingTurnOn;
    public int CountDown;
    public int Looping;
    public int time;
    public List<String> CountDownCommands;
    public List<String> LoopingCommands;
    //本次神兽刷新是否刷空了
    public boolean empty = false;
    public boolean EmptyTurnOn = true;
    public int defaultTimes;
    //刷空指令
    public List<String> emptyCommands = new ArrayList<>();
    public boolean OnlyOne = true;
    private int emptyTimes = 0;

    public static void Load(FileConfiguration configuration) {
        Timer.getTimer().CountDownCommands = MuyeLegendSpawn.ReplaceList(configuration.getStringList("CountDown.commands"));
        Timer.getTimer().LoopingCommands = MuyeLegendSpawn.ReplaceList(configuration.getStringList("Looping.commands"));
        Timer.getTimer().CountDown = configuration.getInt("CountDown.time");
        Timer.getTimer().Looping = configuration.getInt("Looping.time");
        Timer.getTimer().time = Timer.getTimer().Looping;
        Timer.getTimer().CountDownTurnOn = configuration.getBoolean("CountDown.TurnOn");
        Timer.getTimer().LoopingTurnOn = configuration.getBoolean("Looping.TurnOn");
        Timer.getTimer().defaultTimes = configuration.getInt("Empty.Times");
        Timer.getTimer().emptyCommands = configuration.getStringList("Empty.commands");
        Timer.getTimer().EmptyTurnOn = configuration.getBoolean("Empty.TurnOn");
        if (Timer.getTimer().CountDownTurnOn || Timer.getTimer().LoopingTurnOn) {
            Timer.getTimer().timerOn();
        }
    }

    public static Timer getTimer() {
        return timer;
    }

    public static void setTimer(Timer timer) {
        Timer.timer = timer;
    }

    //重置次数
    public void resetTimes() {
        emptyTimes = 0;
    }

    //执行刷空动作
    public void RefreshEmpty() {
        for (; emptyTimes < defaultTimes; emptyTimes++) {
            RunCommand(emptyCommands, null);
            if (empty) {
                break;
            }
        }
        empty = false;
        resetTimes();
        Legend.main.setLegendRefreshTime();
        Bukkit.getServer().broadcastMessage(MMessage.MessageList.get("Prefix") + MMessage.MessageList.get("ErrorRefresh"));
    }

    //倒计时和循环播报计时器
    void timerOn() {
        bukkitRunnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (CountDownTurnOn && Legend.LegendRefreshTime <= CountDown) { //倒计时
                    RunCommand(CountDownCommands, null);
                }
                if (LoopingTurnOn) { //循环
                    if (time != 0) {
                        time--;
                    } else {
                        time = Looping;
                        RunCommand(LoopingCommands, null);
                    }
                }
                if (Legend.LegendRefreshTime != 0) {
                    Legend.LegendRefreshTime--;
                } else {
                    if (!EmptyTurnOn) {
                        Legend.main.setLegendRefreshTime();
                    } else {
                        if (empty) {
                            RefreshEmpty();
                        }
                    }
                    Legend.RefreshLegend();
                }
            }
        };
        bukkitRunnable.runTaskTimerAsynchronously(MuyeLegendSpawn.getInstance(), 0L, 20L);
    }

    //神兽保护倒计时
    public void TimingProtect(Pokemon pokemon, int time, Player player) {
        List<String> playerList = new ArrayList<>();
        playerList.add(player.getName());
        for (UUID uuid : Trust.getTrustList(player.getName())) {
            if (uuid != null) {
                playerList.add(Bukkit.getOfflinePlayer(uuid).getName());
            }
        }
        Timer.legendProtect.put(pokemon, playerList);
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.schedule(() -> {
            Bukkit.getServer().broadcastMessage(MMessage.MessageList.get("Prefix") + MMessage.MessageList.get("Ending").replace("%pokemon%", pokemon.getLocalizedName()).replace("%player%", legendProtect.get(pokemon).get(0)));
            legendProtect.remove(pokemon);
            Listener.playerEntityPixelmonMap.remove(player);
        }, time, TimeUnit.MINUTES);
    }

    public void SuccessfulCapture(Pokemon pokemon) {
        legendProtect.remove(pokemon);
    }

    //检测玩家是否被信任
    public boolean CheckIsNotTrust(Pokemon pokemon, Player player) {
        for (String name : legendProtect.get(pokemon)) {
            if (player.getName().equals(name)) {
                return false;
            }
        }
        return true;
    }

    //执行指令
    public void RunCommand(List<String> type, Player player) {
        for (String get : type) {
            String action;
            if (get.startsWith("NoPlayer")){
                action = get.replace("%player%", player != null ? player.getName() : "空").replace("&", "§");
            } else {
                action = Papi.parse(player, get.replace("%player%", player != null ? player.getName() : "空").replace("&", "§"));
            }
            if (action.startsWith("players: ")) {
                String parse = action.substring(9);
                for (Player player1 : Bukkit.getOnlinePlayers()) {
                    Bukkit.getServer().dispatchCommand(MuyeLegendSpawn.getInstance().getServer().getConsoleSender(), Papi.parse(player1, parse.replace("%player%", player1.getName())));
                }
            } else if (action.startsWith("NoPlayer-title: ")) {
                String[] strings = action.substring(16).split(";");
                for (Player getPlayer : Bukkit.getOnlinePlayers()) {
                    if (getPlayer != player) {
                        getPlayer.sendTitle(Papi.parse(getPlayer, strings[0]), Papi.parse(getPlayer, strings[1]), Integer.parseInt(strings[2]), Integer.parseInt(strings[3]), Integer.parseInt(strings[4]));
                    }
                }
            } else if (action.startsWith("NoPlayer-sound: ")) {
                String sound = action.substring(16);
                for (Player getPlayer : Bukkit.getOnlinePlayers()) {
                    if (getPlayer != player) {
                        getPlayer.playSound(getPlayer.getLocation(), Sound.valueOf(sound), 1.0f, 1.0f);
                    }
                }
            } else if (action.startsWith("NoPlayer: ")) {
                String substring = action.substring(10);
                for (Player getPlayer : Bukkit.getOnlinePlayers()) {
                    if (getPlayer != player) {
                        String command = substring.replace("%noplayer%", getPlayer.getName());
                        Bukkit.getServer().dispatchCommand(MuyeLegendSpawn.getInstance().getServer().getConsoleSender(), Papi.parse(getPlayer, command));
                    }
                }
            } else if (action.startsWith("player-title: ") && player != null) {
                String[] strings = action.substring(14).split(";");
                player.sendTitle(strings[0], strings[1], Integer.parseInt(strings[2]), Integer.parseInt(strings[3]), Integer.parseInt(strings[4]));
            } else if (action.startsWith("player-sound: ") && player != null) {
                String sound = action.substring(14);
                player.playSound(player.getLocation(), Sound.valueOf(sound), 1.0f, 1.0f);
            } else if (action.startsWith("title: ")) {
                String[] strings = action.substring(9).split(";");
                for (Player player1 : Bukkit.getOnlinePlayers()) {
                    player1.sendTitle(strings[0], strings[1], Integer.parseInt(strings[2]), Integer.parseInt(strings[3]), Integer.parseInt(strings[4]));
                }
            } else if (action.startsWith("broadcast: ")) {
                String substrings = action.substring(11);
                Bukkit.getServer().broadcastMessage(substrings);
            } else {
                Bukkit.getServer().dispatchCommand(MuyeLegendSpawn.getInstance().getServer().getConsoleSender(), action);
            }
        }
    }

}
