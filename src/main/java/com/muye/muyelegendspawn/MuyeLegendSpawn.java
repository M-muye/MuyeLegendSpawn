package com.muye.muyelegendspawn;

import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MuyeLegendSpawn extends JavaPlugin {

    private static MuyeLegendSpawn instance;
    public final Map<String, String> Messages = new HashMap<>();
    public Map<String, String> PokemonList = new HashMap<>();
    public Economy economy = null;

    public static MuyeLegendSpawn getInstance() {
        return instance;
    }

    public static List<String> ReplaceList(List<String> strings) {
        strings.replaceAll(s -> s.replace("&", "§"));
        return strings;
    }

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("§6-----------------");
        getLogger().info("§6| §f作者: §b§l沐夜");
        getLogger().info("§6| §f联系方式: Q2103074851");
        getLogger().info("§6| §f接TrV3，技术，小插件定制");
        getLogger().info("§6-----------------");
        getLogger().info("§6| §b" + getDescription().getName() + " §fis §aStarting!");
        Timer.setTimer(new Timer());
        Reload();
        Trust.LoadData();
        Papi.checkPapi();
        for (EnumSpecies species : EnumSpecies.values()) {
            PokemonList.put(species.getLocalizedName(), species.name + "_" + species.getNationalPokedexNumber());
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        economy = rsp.getProvider();
        getCommand("MuyeLegendSpawn").setExecutor(new Command());
        getCommand("mls").setExecutor(new Command());
        Bukkit.getPluginCommand("MuyeLegendSpawn").setTabCompleter(new Tab());
        Bukkit.getPluginCommand("mls").setTabCompleter(new Tab());
        Bukkit.getPluginManager().registerEvents(new Listener(), this);
        Bukkit.getPluginManager().registerEvents(new Trust(), this);
        getLogger().info("§6| §b" + getDescription().getName() + " §fwas §aSuccessfully Launched!");
        getLogger().info("§6-----------------");
    }


    //重载
    public void Reload() {
        saveDefaultConfig();
        reloadConfig();
        File LegendarySpawn = new File(getDataFolder(), "/Logs/LegendarySpawn.log");
        if (!LegendarySpawn.exists()) {
            try {
                if (!LegendarySpawn.getParentFile().exists())
                    Files.createDirectory(LegendarySpawn.getParentFile().toPath());
                LegendarySpawn.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        File LegendaryCapture = new File(getDataFolder(), "/Logs/LegendaryCapture.log");
        if (!LegendaryCapture.exists()) {
            try {
                if (!LegendaryCapture.getParentFile().exists())
                    Files.createDirectory(LegendaryCapture.getParentFile().toPath());
                LegendaryCapture.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        File trust = new File(getDataFolder(), "/Data/trust.yml");
        if (!trust.exists()) {
            try {
                if (!trust.getParentFile().exists()) Files.createDirectory(trust.getParentFile().toPath());
                trust.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        File data = new File(getDataFolder(), "/Data/data.yml");
        if (!data.exists()) {
            try {
                if (!data.getParentFile().exists()) Files.createDirectory(data.getParentFile().toPath());
                data.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        FileConfiguration configuration = getConfig();
        MMessage.reloadMes(configuration);
        Trust.Load(configuration);
        Timer.Load(configuration);
        Listener.Load(configuration);
        Legend.main.Load(configuration);
    }


    @Override
    public void onDisable() {
        getLogger().info("§6-----------------");
        if (Timer.getTimer().bukkitRunnable != null) {
            Timer.getTimer().bukkitRunnable.cancel();
        }
        Trust.SaveData();
        Trust.ClearData();
        Legend.Save();
        getLogger().info("§6| §b" + getDescription().getName() + " §fis §cClosing!");
        getLogger().info("§6-----------------");
    }
}
