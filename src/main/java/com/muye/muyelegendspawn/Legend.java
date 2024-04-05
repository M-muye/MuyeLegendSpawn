package com.muye.muyelegendspawn;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class Legend {

    static final File data = new File(MuyeLegendSpawn.getInstance().getDataFolder(), "/Data/data.yml");
    public static Legend main = new Legend();
    public static Map<String, Integer> PlayerLucky = new HashMap<>();
    public static String MaxStringLegendRefreshTime;

    public static String MinStringLegendRefreshTime;

    public static String LegendRefreshChance;

    //神兽刷新时间
    public static int LegendRefreshTime;

    static List<String> SuccessCommands = new ArrayList<>();
    static boolean SuccessTurnOn = false;

    static boolean FailTurnOn = false;

    static List<String> FailCommands = new ArrayList<>();

    static String format1;
    static String format2;
    static String format3;

    private static YamlConfiguration yaml() {
        return YamlConfiguration.loadConfiguration(data);
    }

    //刷新神兽
    public static void RefreshLegend() {
        if (getLegendRefreshChance() <= 0) {
            Timer.getTimer().RunCommand(FailCommands, null);
            if (Timer.getTimer().EmptyTurnOn) {
                Timer.getTimer().empty = false;
                Legend.main.setLegendRefreshTime();
                Timer.getTimer().resetTimes();
            }
        } else {
            if (Math.random() < getLegendRefreshChance() || getLegendRefreshChance() == 1) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "legendaryspawn");
            } else {
                Timer.getTimer().RunCommand(FailCommands, null);
                if (Timer.getTimer().EmptyTurnOn) {
                    Timer.getTimer().empty = false;
                    Legend.main.setLegendRefreshTime();
                    Timer.getTimer().resetTimes();
                }
            }
        }
        if (Timer.getTimer().EmptyTurnOn) {
            Timer.getTimer().empty = true;
        }
    }

    //获取神兽刷新概率
    public static double getLegendRefreshChance() {
        try {
            return (double) ((new ScriptEngineManager()).getEngineByName("js")).eval(Papi.parse(null, LegendRefreshChance.replace("%players%", String.valueOf(Bukkit.getServer().getOnlinePlayers().size()))));
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }

    //获取神兽刷新时间文本变量
    public static String getStringLegendRefreshTime() {
        String format;
        int time = LegendRefreshTime;
        long hours = time / 3600;
        long minutes = time / 60;
        long seconds = time % 60;
        if (hours == 0) {
            if (minutes == 0) {
                format = format3;
            } else {
                format = format2;
            }
        } else {
            format = format1;
        }
        return format.replace("%hour%", String.valueOf(hours)).replace("%minutes%", String.valueOf(minutes)).replace("%seconds%", String.valueOf(seconds));
    }

    public static void Save() {
        yaml().set("LastRefreshTime", LegendRefreshTime);
        try {
            yaml().save(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void Load(FileConfiguration configuration) {
        format1 = configuration.getString("Format.time1").replace("&", "§");
        format2 = configuration.getString("Format.time2").replace("&", "§");
        format3 = configuration.getString("Format.time3").replace("&", "§");
        SuccessCommands = MuyeLegendSpawn.ReplaceList(configuration.getStringList("Success.commands"));
        FailCommands = MuyeLegendSpawn.ReplaceList(configuration.getStringList("Fail.commands"));
        SuccessTurnOn = configuration.getBoolean("Success.TurnOn");
        FailTurnOn = configuration.getBoolean("Fail.TurnOn");
        LegendRefreshChance = configuration.getString("RefreshLegend.Chance");
        String[] getTime = configuration.getString("RefreshLegend.RefreshTime").split(";");
        MaxStringLegendRefreshTime = getTime[1];
        MinStringLegendRefreshTime = getTime[0];
        LegendRefreshTime = (yaml().get("LastRefreshTime") != null ? yaml().getInt("LastRefreshTime") : getLegendRefreshTime());
    }

    //设置神兽刷新时间
    public void setLegendRefreshTime() {
        LegendRefreshTime = getLegendRefreshTime();
    }

    //获取默认神兽刷新时间
    public int getLegendRefreshTime() {
        int max;
        int min;
        try {
            max = (int) ((new ScriptEngineManager()).getEngineByName("js")).eval(Papi.parse(null, MaxStringLegendRefreshTime.replace("%players%", String.valueOf(Bukkit.getServer().getOnlinePlayers().size()))));
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
        try {
            min = (int) ((new ScriptEngineManager()).getEngineByName("js")).eval(Papi.parse(null, MinStringLegendRefreshTime.replace("%players%", String.valueOf(Bukkit.getServer().getOnlinePlayers().size()))));
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
        return (new Random()).nextInt(max - min + 1) + min;
    }

}
