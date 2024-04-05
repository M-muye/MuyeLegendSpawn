package com.muye.muyelegendspawn;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class Tab implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        List<String> get = com.muye.muyelegendspawn.Command.args1;
        List<String> tab = new ArrayList<>();
        if (args.length == 1) {
            for (String string : get) {
                if (string.startsWith(args[0].toLowerCase())) {
                    tab.add(string);
                }
            }
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add")) {
                List<String> players = new ArrayList<>();
                Bukkit.getOnlinePlayers().forEach(player -> {
                    players.add(player.getName());
                });
                tab.addAll(players);
            }
        }
        return tab;
    }
}
