package com.muye.muyelegendspawn;

import catserver.api.bukkit.event.ForgeEvent;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.events.BattleStartedEvent;
import com.pixelmonmod.pixelmon.api.events.CaptureEvent;
import com.pixelmonmod.pixelmon.api.events.spawning.SpawnEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.entities.pixelmon.EntityPixelmon;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.EVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.IVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Moveset;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import net.md_5.bungee.api.chat.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.io.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class Listener implements org.bukkit.event.Listener {
    public static final Map<Player, Pokemon> playerEntityPixelmonMap = new HashMap<>();
    public static List<String> BanWorldList = new ArrayList<>();
    public static List<String> BlackList = new ArrayList<>();
    public static Map<String, String> ReplaceList = new HashMap<>();
    public static Map<String, Integer> IvsChance = new HashMap<>();
    public static boolean worldBan = false;
    public static boolean IvsTurnOn = false;
    public static boolean Ivs0 = false;
    public static int defaultTime;
    public static int vipTime;
    public static List<String> lore;
    public static int ProtectRange;
    public static double money;
    final File LegendaryCapture = new File(MuyeLegendSpawn.getInstance().getDataFolder(), "/Logs/LegendaryCapture.log");
    final File LegendarySpawn = new File(MuyeLegendSpawn.getInstance().getDataFolder(), "/Logs/LegendarySpawn.log");
    SimpleDateFormat dateFormat = new SimpleDateFormat("[MM-dd]");
    SimpleDateFormat timeFormat = new SimpleDateFormat("[HH:mm:ss]");

    //检测是否能传送
    public static boolean CheckIfTeleportToPokemon(Player player) {
        Pokemon pokemon = playerEntityPixelmonMap.get(player);
        if (pokemon == null) {
            return false;
        }
        LivingEntity livingEntity = getPokemon(pokemon.getUUID());
        return livingEntity != null;
    }

    //传送玩家至宝可梦
    public static void TeleportToPokemon(Player player) {
        Pokemon Pokemon = playerEntityPixelmonMap.get(player);
        if (Pokemon == null) {
            MMessage.sendMes(player, "FailTeleport");
            return;
        }
        LivingEntity livingEntity = getPokemon(Pokemon.getUUID());
        if (livingEntity == null) {
            MMessage.sendMes(player, "FailTeleport");
            return;
        }
        player.teleport(livingEntity);
    }

    //通过实体uuid获取实体
    public static LivingEntity getPokemon(UUID uuid) {
        LivingEntity legend = null;
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof LivingEntity && entity.getUniqueId() == uuid) legend = (LivingEntity) entity;
            }
        }
        return legend;
    }

    public static void Load(FileConfiguration configuration) {
        Listener.money = configuration.getDouble("TeleportConsume");
        Listener.lore = configuration.getStringList("LegendAttributes.Lore");
        Listener.defaultTime = configuration.getInt("RefreshLegend.ProtectTime.default");
        Listener.vipTime = configuration.getInt("RefreshLegend.ProtectTime.vip");
        Listener.ProtectRange = configuration.getInt("RefreshLegend.ProtectRange");
        Listener.worldBan = configuration.getBoolean("World.TurnOn");
        if (Listener.worldBan) {
            Listener.BanWorldList = configuration.getStringList("World.list");
        }
        Listener.IvsTurnOn = configuration.getBoolean("LegendAttributes.IvsTurOn");
        if (Listener.IvsTurnOn) {
            Set<String> get = configuration.getConfigurationSection("LegendAttributes.Ivs").getKeys(false);
            for (String s : get) {
                Listener.IvsChance.put(s, configuration.getInt("LegendAttributes.Ivs." + s));
            }
        }
        Listener.Ivs0 = configuration.getBoolean("LegendAttributes.Ivs=0");
        Listener.BlackList = configuration.getStringList("LegendAttributes.Blacklist");
        List<String> list = configuration.getStringList("LegendAttributes.ReplaceList");
        for (String s : list) {
            String[] get = s.split("-");
            Listener.ReplaceList.put(get[0], get[1]);
        }
    }

    public static World getWorld(String worldName) {
        for (WorldServer ws : FMLCommonHandler.instance().getMinecraftServerInstance().worldServerList) {
            if (ws.getWorld().getName().equals(worldName)) {
                return (World) ws;
            }
        }
        return null;
    }

    private int getTime(Player player) {
        if (player.hasPermission("MuyeLegendSpawn.vip")) {
            return vipTime;
        }
        return defaultTime;
    }

    @EventHandler
    public void onForge(ForgeEvent forgeEvent) {
        //神兽生成判断，并写入日志
        if (forgeEvent.getForgeEvent() instanceof SpawnEvent) {
            SpawnEvent event = (SpawnEvent) forgeEvent.getForgeEvent();
            if (event.action.getOrCreateEntity() instanceof EntityPixelmon) {
                EntityPixelmon entityPixelmon = (EntityPixelmon) event.action.getOrCreateEntity();
                if (entityPixelmon == null || event.isCanceled() || !entityPixelmon.isLegendary() || entityPixelmon.isBossPokemon())
                    return;
                if (Legend.LegendRefreshTime != 0 && !Command.Spawn) {
                    event.setCanceled(true);
                    MuyeLegendSpawn.getInstance().getLogger().info("原版刷神已被取消");
                    return;
                }
                Command.Spawn = false;
                String worldName = event.action.spawnLocation.location.world.getWorld().getName();
                if (worldBan && BanWorldList.contains(worldName)) { //世界黑名单
                    event.setCanceled(true);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "legendaryspawn");
                    MuyeLegendSpawn.getInstance().getLogger().info("世界 " + worldName + " 中刷新了神兽，已取消并重新刷新");
                    if (Legend.LegendRefreshTime != 0) Command.Spawn = true;
                    return;
                }
                //判断宝可梦是否需要替换或者是否在黑名单内
                String pokemonName = entityPixelmon.getLocalizedName();
                if (BlackList.contains(pokemonName)) { //黑名单
                    event.setCanceled(true);
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "legendaryspawn");
                    MuyeLegendSpawn.getInstance().getLogger().info("已刷新黑名单中的 " + pokemonName + " ，已替换成其他神兽");
                    if (Legend.LegendRefreshTime != 0) Command.Spawn = true;
                    return;
                }
                Pokemon pokemon = null;
                if (ReplaceList.containsKey(pokemonName)) { //替换名单
                    Bukkit.getScheduler().runTaskLater(MuyeLegendSpawn.getInstance(), () -> {
                        LivingEntity entity = getPokemon(entityPixelmon.getPokemonData().getUUID());
                        if (entity == null) return;
                        Location loc = entity.getLocation().clone();
                        entity.remove();
                        EnumSpecies enumSpecies = EnumSpecies.valueOf(MuyeLegendSpawn.getInstance().PokemonList.get(ReplaceList.get(pokemonName)).split("_")[0]);
                        Pokemon ReplacePokemon = Pixelmon.pokemonFactory.create(enumSpecies);
                        ReplacePokemon.getOrSpawnPixelmon(entityPixelmon.func_130014_f_(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
                        MuyeLegendSpawn.getInstance().getLogger().info("检测到神兽 " + pokemonName + " 刷新，已被替换成 " + ReplacePokemon.getLocalizedName());
                        extracted(ReplacePokemon, event);
                    }, 0);
                } else {
                    pokemon = entityPixelmon.getStoragePokemonData();
                }
                if (pokemon != null) {
                    if (Timer.getTimer().EmptyTurnOn) {
                        Timer.getTimer().empty = false;
                        Legend.main.setLegendRefreshTime();
                        Timer.getTimer().resetTimes();
                    }
                    extracted(pokemon, event);
                }
            }
        }
        /*捕获神兽写入日志，神兽捕获成功事件，并写入日志*/
        if (forgeEvent.getForgeEvent() instanceof CaptureEvent.SuccessfulCapture) {
            Date day = new Date();
            String MM = dateFormat.format(day);
            String time = timeFormat.format(day);
            CaptureEvent.SuccessfulCapture event = (CaptureEvent.SuccessfulCapture) forgeEvent.getForgeEvent();
            if (event.getPokemon().isLegendary()) {
                Player player = Bukkit.getPlayer(event.player.getPersistentID());
                String pokemon = event.getPokemon().getLocalizedName();
                String logMessage = MM + time + ": 玩家 " + player.getName() + " 捕捉了 " + pokemon + "，个体值:" + Arrays.toString(event.getPokemon().getPokemonData().getIVs().getArray());
                Timer.getTimer().SuccessfulCapture(event.getPokemon().getPokemonData());
                try (FileWriter fw = new FileWriter(LegendaryCapture.getAbsolutePath(), true); BufferedWriter bw = new BufferedWriter(fw); PrintWriter out = new PrintWriter(bw)) {
                    out.println(logMessage);
                    System.out.println(logMessage);
                } catch (IOException e) {
                    System.err.println("写入日志信息时出现异常: " + e.getMessage());
                }
            }
        }
        //宝可梦战斗开始事件
        if (forgeEvent.getForgeEvent() instanceof BattleStartedEvent) {
            BattleStartedEvent event = (BattleStartedEvent) forgeEvent.getForgeEvent();
            if (event.bc.getPlayers().size() != 1) return;
            Player player = Bukkit.getPlayer(event.bc.getPlayers().get(0).player.getPersistentID());
            for (BattleParticipant participant : event.bc.participants) {
                for (PixelmonWrapper pixelmonWrapper : participant.allPokemon) {
                    Pokemon pokemon = pixelmonWrapper.pokemon;
                    if (Timer.legendProtect.containsKey(pokemon)) {
                        if (Timer.getTimer().CheckIsNotTrust(pokemon, player)) {
                            if (Trust.getTrustList(player.getName()).contains(player.getUniqueId())) {
                                Player trust = Bukkit.getPlayer(player.getUniqueId());
                                if (trust != null) {
                                    MMessage.sendMes(player, MMessage.MessageList.get("Prefix") + MMessage.MessageList.get("TrustBattle").replace("%goal%", trust.getName()));
                                }
                                return;
                            }
                            event.setCanceled(true);
                            MMessage.sendMes(player, MMessage.MessageList.get("Prefix") + MMessage.MessageList.get("Protect").replace("%player%", Timer.legendProtect.get(pokemon).get(0)));
                        }
                    }
                }
            }

        }
        //宝可梦开始捕获事件
        if (forgeEvent.getForgeEvent() instanceof CaptureEvent.StartCapture) {
            CaptureEvent.StartCapture event = (CaptureEvent.StartCapture) forgeEvent.getForgeEvent();
            Pokemon pokemon = event.getPokemon().getPokemonData();
            if (Timer.legendProtect.containsKey(pokemon)) {
                Player player = Bukkit.getPlayer(event.player.getPersistentID());
                if (Timer.getTimer().CheckIsNotTrust(pokemon, player)) {
                    if (Trust.getTrustList(player.getName()).contains(player.getUniqueId())) {
                        Player trust = Bukkit.getPlayer(player.getUniqueId());
                        if (trust != null) {
                            MMessage.sendMes(player, MMessage.MessageList.get("Prefix") + MMessage.MessageList.get("TrustCapture").replace("%goal%", trust.getName()));
                        }
                        return;
                    }
                    event.setCanceled(true);
                    MMessage.sendMes(player, MMessage.MessageList.get("Prefix") + MMessage.MessageList.get("Protect").replace("%player%", Timer.legendProtect.get(pokemon).get(0)));
                }
            }
        }
    }


    //处理神兽刷新事件
    private void extracted(Pokemon pokemon, SpawnEvent event) {
        editPokemon(pokemon); //编辑宝可梦
        writeLogs(pokemon);  //写入日志
        Player player = null;
        String pokemonName = pokemon.getLocalizedName();
        BlockPos.MutableBlockPos pos = event.action.spawnLocation.location.pos;
        net.minecraft.world.World world = event.action.spawnLocation.location.world;
        EntityPlayer protectPlayer_Entity = world.func_184137_a(pos.func_177958_n(), pos.func_177956_o(), pos.func_177952_p(), 100, false);
        if (protectPlayer_Entity != null) {
            player = Bukkit.getPlayer(protectPlayer_Entity.getPersistentID());
            Timer.getTimer().TimingProtect(pokemon, getTime(player), player); //神兽保护
            Timer.getTimer().RunCommand(Legend.SuccessCommands, player); //执行指令
            inquire(player, pokemon); //询问是否需要传送
        }
        MuyeLegendSpawn.getInstance().getServer().broadcastMessage(MMessage.MessageList.get("Prefix") + MMessage.MessageList.get("Refresh").replace("%player%", player != null ? player.getName() : "无").replace("%pokemon%", pokemonName).replace("%time%", player != null ? String.valueOf(getTime(player)) : "0").replace("%x%", String.valueOf(pos.func_177958_n())).replace("%y%", String.valueOf(pos.func_177956_o())).replace("%z%", String.valueOf(pos.func_177952_p())));
        broadcast(pokemon); //报告全服宝可梦属性
    }

    //询问是否需要传送，并存放进Map
    private void inquire(Player player, Pokemon pokemon) {
        playerEntityPixelmonMap.put(player, pokemon);
        TextComponent message = new TextComponent(MMessage.MessageList.get("Prefix") + MMessage.MessageList.get("Teleport").replace("%money%", String.valueOf(money)));
        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mls tp"));
        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§f[§a§l点击传送§f]").create()));
        player.spigot().sendMessage(message);
    }

    //公告查看宝可梦属性
    private void broadcast(Pokemon pokemon) {
        TextComponent message = new TextComponent(MMessage.MessageList.get("Prefix") + MMessage.MessageList.get("Broadcast"));
        IVStore ivStore = pokemon.getIVs();
        EVStore evStore = pokemon.getEVs();
        Moveset moveset = pokemon.getMoveset();
        StringBuilder moves = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            if (moveset.get(i) != null) {
                moves.append(moveset.get(i).getMove().getLocalizedName());
            } else {
                moves.append("空");
            }
            if (i != 3) {
                moves.append("-");
            }
        }
        String get = String.join("\n", lore).replace("&", "§").replace("%pokemon%", pokemon.getLocalizedName()).replace("%level%", String.valueOf(pokemon.getLevel())).replace("%shiny%", pokemon.isShiny() ? "闪光" : "非闪光").replace("%helditem%", pokemon.getHeldItem().func_82833_r().equals("Air") ? "无" : pokemon.getHeldItem().func_82833_r()).replace("%ivstotal%", ivStore.getPercentageString(1) + "%").replace("%evstotal%", (new DecimalFormat("#0.0%")).format(evStore.getTotal() / 510)).replace("%ivHp%", String.valueOf(ivStore.getStat(StatsType.HP))).replace("%ivAttack%", String.valueOf(ivStore.getStat(StatsType.Attack))).replace("%ivSpecialAttack%", String.valueOf(ivStore.getStat(StatsType.SpecialAttack))).replace("%ivDefence%", String.valueOf(ivStore.getStat(StatsType.Defence))).replace("%ivSpecialDefence%", String.valueOf(ivStore.getStat(StatsType.SpecialDefence))).replace("%ivSpeed%", String.valueOf(ivStore.getStat(StatsType.Speed))).replace("%evHp%", String.valueOf(evStore.getStat(StatsType.HP))).replace("%evAttack%", String.valueOf(evStore.getStat(StatsType.Attack))).replace("%evSpecialAttack%", String.valueOf(evStore.getStat(StatsType.SpecialAttack))).replace("%evDefence%", String.valueOf(evStore.getStat(StatsType.Defence))).replace("%evSpecialDefence%", String.valueOf(evStore.getStat(StatsType.SpecialDefence))).replace("%evSpeed%", String.valueOf(evStore.getStat(StatsType.Speed))).replace("%nature%", pokemon.getNature().getLocalizedName()).replace("%gender%", pokemon.getGender().getLocalizedName()).replace("%growth%", pokemon.getGrowth().getLocalizedName()).replace("%ability%", pokemon.getAbility().getLocalizedName()).replace("%moves%", moves);
        BaseComponent[] hover = new ComponentBuilder(get).create();
        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hover));
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.spigot().sendMessage(message);
        }
    }

    //宝可梦写入日志
    private void writeLogs(Pokemon pokemon) {
        Date day = new Date();
        String MM = dateFormat.format(day);
        String time = timeFormat.format(day);
        String logMessage = MM + time + ": 神兽 " + pokemon.getLocalizedName() + " 生成，个体值:" + Arrays.toString(pokemon.getIVs().getArray());
        try (FileWriter fw = new FileWriter(LegendarySpawn.getAbsolutePath(), true); BufferedWriter bw = new BufferedWriter(fw); PrintWriter out = new PrintWriter(bw)) {
            out.println(logMessage);
            System.out.println(logMessage);
        } catch (IOException e) {
            System.err.println("写入日志信息时出现异常: " + e.getMessage());
        }
    }

    //编辑刷新的神兽
    private void editPokemon(Pokemon pokemon) {
        if (pokemon == null || !pokemon.isLegendary()) return;
        if (Ivs0) {
            for (StatsType statsType : StatsType.values()) {
                pokemon.getIVs().setStat(statsType, 0);
            }
            return;
        }
        int sum = IvsChance.values().stream().mapToInt(Integer::intValue).sum();
        int getV = (int) Arrays.stream(StatsType.getStatValues()).filter(s -> pokemon.getIVs().getStat(s) >= 31).count();
        if (sum != 0) {
            int randomValue = (new Random()).nextInt(sum + 1);
            int k = 0;
            int randomV = getV;
            for (String s : IvsChance.keySet()) {
                int Num = IvsChance.get(s);
                if (randomValue <= k + Num) {
                    randomV = Integer.parseInt(s.substring(0, s.length() - 1));
                    break;
                } else {
                    k = k + Num;
                }
            }
            for (int i = 0; i < (randomV - getV); i++) {
                Arrays.stream(StatsType.getStatValues()).filter(s -> (pokemon.getIVs().getStat(s) < 31)).findAny().ifPresent(st -> pokemon.getIVs().setStat(st, 31));
            }
        }
    }
}
