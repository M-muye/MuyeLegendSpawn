package com.muye.muyelegendspawn;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MMessage {
    public static final Map<String, String> MessageList = new HashMap<>();


    public static void reloadMes(FileConfiguration configuration) {
        MessageList.clear();
        Set<String> messages = configuration.getConfigurationSection("Messages").getKeys(false);
        for (String mes : messages) {
            MessageList.put(mes, configuration.getString("Messages." + mes).replace("&", "§"));
        }
    }

    public static void sendMes(Player player, String goal) {
        String string = MessageList.get(goal);
        if (string != null) {
            string = MessageList.get(goal);
        } else {
            string = goal;
        }
        String parse = Papi.parse(player, MessageList.get("Prefix") + string);
        player.sendMessage(parse);
    }

    public static void sendMes(CommandSender sender, String goal) {
        String string = MessageList.get(goal);
        if (string != null) {
            string = MessageList.get(goal);
        } else {
            string = goal;
        }
        String parse = Papi.parse(null, MessageList.get("Prefix") + string);
        sender.sendMessage(parse);
    }

    public static void sendMes(String s, String goal) {
        if (s.equalsIgnoreCase("console")) {
            String string = MessageList.get(goal);
            if (string != null) {
                string = MessageList.get("Prefix") + MessageList.get(goal);
            }
            String parse = Papi.parse(null, string);
            MuyeLegendSpawn.getInstance().getLogger().info(parse);
        }
    }


}
