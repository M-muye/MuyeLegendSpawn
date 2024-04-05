package com.muye.muyelegendspawn;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.text.DecimalFormat;

public class Papi extends PlaceholderExpansion {

    public static PlaceholderExpansion placeholderExpansion;

    private static Plugin getInstance() {
        return MuyeLegendSpawn.getInstance();
    }

    public static void checkPapi() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            getInstance().getLogger().info("§6| §aFound §6PlaceholderAPI");
            if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                placeholderExpansion = new Papi();
                if (!placeholderExpansion.isRegistered() && placeholderExpansion.canRegister()){
                    Papi.placeholderExpansion.register();
                    getInstance().getLogger().info("§6| §aRegister §6PapiHook");
                }
            }
        } else {
            getInstance().getLogger().info("§6| §cCould not find §6PlaceholderAPI §c! ");
        }
    }

    public static String parse(Player player, String mes) {
        if (player != null) {
            return PlaceholderAPI.setPlaceholders(player, mes).replace("%player%", player.getName());
        }
        return PlaceholderAPI.setPlaceholders(null, mes);
    }


    @Override
    public @NotNull String getIdentifier() {
        return "mls";
    }

    @Override
    public @NotNull String getAuthor() {
        return getInstance().getDescription().getAuthors().get(0);
    }

    @Override
    public @NotNull String getVersion() {
        return getInstance().getDescription().getVersion();
    }

    @Nullable
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (params.equals("time")) {
            return Legend.getStringLegendRefreshTime();
        }
        if (params.equals("chance")) {
            if (Legend.getLegendRefreshChance() >= 1) {
                return "100%";
            }
            if (Legend.getLegendRefreshChance() <= 0) {
                return "0%";
            }
            return (new DecimalFormat("#0.00%")).format(Legend.getLegendRefreshChance());
        }
        return "§7未知变量";
    }
}
