package com.pricejoshua.blockcontest;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class BlockPlaceContest extends JavaPlugin implements Listener {

    public Map<String, Integer> playerBlocks = new HashMap<>();
    public Map<String, Boolean> playerList = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("Starting up");
        this.saveDefaultConfig();
        if (this.getConfig().contains("data")){
            this.restoreBlocks();
        }

        if (this.getConfig().contains("players")){
            this.restorePlayers();
        }

        getServer().getPluginManager().registerEvents(this, this);

    }

    public void saveBlocks(){
        for (Map.Entry<String, Integer> entry : playerBlocks.entrySet()){
            this.getConfig().set("data." + entry.getKey(), entry.getValue());
        }

        for (Map.Entry<String, Boolean> entry : playerList.entrySet()){
            this.getConfig().set("player." + entry.getKey(), entry.getValue());
        }
        this.saveConfig();
    }


    public void restoreBlocks(){
        this.getConfig().getConfigurationSection("data").getKeys(false).forEach(key ->{
            Integer placed = (Integer) this.getConfig().get("data." + key);
            playerBlocks.put(key, placed);
        });

    }

    public void restorePlayers() {
        this.getConfig().getConfigurationSection("player").getKeys(false).forEach(key -> {
            playerList.put(key, (Boolean) this.getConfig().get("player." + key));
        });
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info("Shut down");
        saveBlocks();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent blockPlaceEvent){
        String uuid = blockPlaceEvent.getPlayer().getUniqueId().toString();
        if (playerBlocks.containsKey(uuid)){
            playerBlocks.put(uuid, playerBlocks.get(uuid)+1);
        }else{
            playerBlocks.put(uuid, 1);
        }
    }

    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        if(label.equalsIgnoreCase("stats")){
            if(!(sender instanceof Player)){
                sender.sendMessage("NO");
                return true;
            }
            Player player = (Player) sender;
            String uuid = player.getUniqueId().toString();
            if (playerBlocks.containsKey(uuid)){
                sender.sendMessage(playerBlocks.get(uuid) + " blocks placed");
            }
        }

        if(label.equalsIgnoreCase("enter_contest")){
            if(!(sender instanceof Player)){
                sender.sendMessage("NO");
                return true;
            }
            Player player = (Player) sender;
            String uuid = player.getUniqueId().toString();

            if (playerList.get(uuid) != null){
                playerList.put(uuid, true);
                sender.sendMessage("You have already entered the contest");
            } else {
                playerList.put(uuid, true);
                sender.sendMessage("You have entered the contest!");
            }
        }

        if(label.equalsIgnoreCase("reset_blocks")){
            if(!(sender instanceof Player)){
                sender.sendMessage("NO");
                return true;
            }
            Player player = (Player) sender;
            if (player.hasPermission("papertest.reset")){
                playerBlocks = new HashMap<>();
                playerList = new HashMap<>();
                player.sendMessage("reset the contest");
            }else{
                player.sendMessage("Meanie");
            }
        }

        if(label.equalsIgnoreCase("scoreboard")){
            class PlayerScore implements Comparable<PlayerScore>{
                String uuid;
                int score;

                public PlayerScore(String uuid, int score) {
                    this.uuid = uuid;
                    this.score = score;
                }

                @Override
                public int compareTo(PlayerScore o) {
                    return Integer.valueOf(this.score).compareTo(Integer.valueOf(o.score));
                }
            }
            List<PlayerScore> playerScores = new ArrayList<>();

            for (Player player : Bukkit.getOnlinePlayers()){
                String uuid = player.getUniqueId().toString();
                if (playerList.containsKey(uuid)){
                    playerScores.add(new PlayerScore(uuid, playerBlocks.get(uuid)));
                }
            }

            sender.sendMessage();
        }

        if(label.equalsIgnoreCase("save")){
            saveBlocks();
        }

        return false;
    }

    @EventHandler
    public void playerLeaveHandler(PlayerQuitEvent event){
        saveBlocks();
    }
}
